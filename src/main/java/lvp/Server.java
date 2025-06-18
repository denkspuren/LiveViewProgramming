package lvp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lvp.skills.TextUtils;
import lvp.skills.TextUtils.ReplacementType;
import lvp.skills.logging.LogLevel;
import lvp.skills.logging.Logger;


public class Server {
    private record EventMessage(SSEType type, String data, String id, String sourceId) {}

    private final HttpServer httpServer;

    final int port;
    static int defaultPort = 50_001;
    static final String INDEX = "/web/index.html";

    static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    static int getDefaultPort() { return defaultPort; }

    public final List<HttpExchange> webClients = new CopyOnWriteArrayList<>();
    List<EventMessage> events = new CopyOnWriteArrayList<>();
    Map<String, Process> waitingProcesses = new ConcurrentHashMap<>();

    public Server(int port) throws IOException {
        this.port = port;

        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        httpServer.createContext("/log", this::handleLog);
        httpServer.createContext("/interact", this::handleInteract);
        httpServer.createContext("/read", this::handleRead);
        httpServer.createContext("/events", this::handleEvents);
        httpServer.createContext("/", this::handleRoot);

        httpServer.setExecutor(Executors.newFixedThreadPool(5));
        httpServer.start();
    }

    private void handleLog(HttpExchange exchange) throws IOException {
        String message = readRequestBody(exchange);
        if (message == null) return;

        String[] parts = message.split(":", 2);
        if (parts.length != 2) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
            Logger.logError("Illegal log message: " + message);
            return;
        }

        exchange.sendResponseHeaders(200, 0);
        exchange.close();

        Logger.log(LogLevel.fromString(parts[0]), parts[1]);
    }

    private void handleRead(HttpExchange exchange) throws IOException {
        String message = readRequestBody(exchange);
        if (message == null) return;
        String[] parts = message.split(":", 2);
        if (parts.length != 2) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
            Logger.logError("Illegal read message: " + message);
            return;
        }

        Logger.logInfo(message);

        exchange.sendResponseHeaders(200, 0);
        exchange.close();

        OutputStream stream = waitingProcesses.get(parts[0]).getOutputStream();
        if (stream == null) {
            Logger.logError("Stream not found: " + message);
            return;
        }
        try {
            stream.write(Base64.getDecoder().decode(parts[1]));
            stream.flush();
        } catch (IOException e) {
            Logger.logError("Error while writing stream for: " + parts[0], e);
        } finally {
            stream.close();
            waitingProcesses.remove(parts[0]);
        }

    }

    private void handleInteract(HttpExchange exchange) throws IOException {
        String message = readRequestBody(exchange);
        if (message == null) return;
        String[] parts = message.split(":", 4);
        if (parts.length != 4) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
            Logger.logError("Illegal interaction message: " + message);
            return;
        }

        exchange.sendResponseHeaders(200, 0);
        exchange.close();

        String path = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String label = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        Optional<ReplacementType> rType = Arrays.stream(ReplacementType.values())
            .filter(r -> r.name().toLowerCase().equals(parts[2]))
            .findFirst();

        if (rType.isEmpty()) {
            Logger.logError("ReplacementType not found: " + message);
            return;
        }
        
        String replacement = new String(Base64.getDecoder().decode(parts[3]), StandardCharsets.UTF_8);
        TextUtils.updateFile(path, label, rType.get(), replacement);
    }

    private void handleEvents(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            Logger.logError("Method not allowed in '/events'");
            return;
        }

        Logger.logInfo("New SSE Exchange for '" + exchange.getLocalAddress() + "' at '" + exchange.getRemoteAddress() + "'");
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);

        webClients.add(exchange);
        if (!events.isEmpty()) {
            events.forEach(event -> sendMessageToClient(exchange, event));
        }
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            Logger.logError("Method not allowed in '/'");
            return;
        }

        final String resourcePath = exchange.getRequestURI().getPath().equals("/") ? INDEX : exchange.getRequestURI().getPath();
        Logger.logDebug("Sending '" + resourcePath + "'");

        try (final InputStream stream = Server.class.getResourceAsStream(resourcePath)) {
            final byte[] bytes = stream.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", Files.probeContentType(Path.of(resourcePath)) + "; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().flush();
        } finally {
            exchange.close();
        }
    }

    public void sendServerEvent(SSEType type, String data, String id, String sourceId) {
        Logger.logDebug("Event: " + type + " with data: " + data + " to " + sourceId);
        sendServerEvent(new EventMessage(type, Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8)), id, sourceId));
    }

    private void sendServerEvent(EventMessage event) {
        events.add(event);
        if (webClients.isEmpty()) return;        
        webClients.removeIf(connection -> !sendMessageToClient(connection, event));
    }

    private boolean sendMessageToClient(HttpExchange connection, EventMessage event) {
        try {
            String message = "data: " + event.type() 
                + ":" + event.sourceId() 
                + ":" + event.id() 
                + ":" +  event.data() 
                + "\n\n";
            OutputStream os = connection.getResponseBody();
            os.write(message.getBytes(StandardCharsets.UTF_8));
            os.flush();
            return true;
        } catch (IOException _) {
            Logger.logError("Web exchange '" + connection.getRemoteAddress() + "' did not respond. Closing...");
            connection.close();
            return false;
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            Logger.logError("Method not allowed in '" + exchange.getRequestURI().getPath() + "'");
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            exchange.close();
            return null;
        }

        String contentLength = exchange.getRequestHeaders().getFirst("Content-length");
        if (contentLength == null) {
            Logger.logError("content-length header in '" + exchange.getRequestURI().getPath() + "' is missing");
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
            return null;
        }

        try {
            int length = Integer.parseInt(contentLength);
            byte[] data = exchange.getRequestBody().readNBytes(length);
            if (data.length != length) {
                Logger.logError("Premature end of stream in '" + exchange.getRequestURI().getPath() + "'");
                exchange.sendResponseHeaders(400, -1); // Bad Request
                exchange.close();
                return null;
            }
            return new String(data, StandardCharsets.UTF_8);
        } catch (NumberFormatException e) {
            Logger.logError("illegal content-length header in '" + exchange.getRequestURI().getPath() + "'", e);
            exchange.sendResponseHeaders(400, -1); // Bad Request
        } catch (IOException e) {
            Logger.logError("Error reading request body in '" + exchange.getRequestURI().getPath() + "'", e);
            exchange.sendResponseHeaders(400, -1); // Bad Request
        }
        exchange.close();
        return null;
    }

    public void clearEvents(String sourceId) {
        events.removeIf(event -> event.sourceId().equals(sourceId));
        if (waitingProcesses.containsKey(sourceId)) {
            waitingProcesses.get(sourceId).destroyForcibly();
            waitingProcesses.remove(sourceId);
        }
    }

    public void stop() {
        Logger.logInfo("Closing Server on port '" + port + "'");
        for (HttpExchange connection : webClients) {
            connection.close();
        }
        httpServer.stop(0);
    }
}