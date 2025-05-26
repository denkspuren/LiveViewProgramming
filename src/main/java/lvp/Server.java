package lvp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lvp.logging.LogLevel;
import lvp.logging.Logger;


public class Server {
    private enum ReplacementType {
        Single, Multi, Block
    }
    private record EventMessage(SSEType event, String data) {}

    private final HttpServer httpServer;

    final int port;
    static int defaultPort = 50_001;
    static final String index = "/web/index.html";

    static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    static int getDefaultPort() { return defaultPort; }

    public List<HttpExchange> webClients;
    List<EventMessage> events = new CopyOnWriteArrayList<>();

    boolean isVerbose = false;

    public Server(int port, boolean isVerbose) throws IOException {
        this.port = port;
        this.isVerbose = isVerbose;
        webClients = new CopyOnWriteArrayList<>(); // thread-safe variant of ArrayList

        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        httpServer.createContext("/log", this::handleLog);
        httpServer.createContext("/interact", this::handleInteract);
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
        updateFile(path, label, rType.get(), replacement);
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

        if (isVerbose) sendMessageToClient(exchange, SSEType.DEBUG, "");

        webClients.add(exchange);
        if (events.size() > 0) {
            events.forEach(event -> sendMessageToClient(exchange, event.event, event.data));
        }
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            Logger.logError("Method not allowed in '/'");
            return;
        }

        final String resourcePath = exchange.getRequestURI().getPath().equals("/") ? index : exchange.getRequestURI().getPath();
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

    public void read(String message) {
        String[] parts = message.split(":", 2);
        Optional<SSEType> event = Optional.empty();
        if (parts.length == 2) {
            event = Arrays.stream(SSEType.values())
                .filter(sseType -> sseType.name().equals(parts[0]))
                .findFirst();
        }
        if (event.isEmpty()) Logger.logError("Error: + " + message);

        SSEType eventMessage = event.orElse(SSEType.LOG);
        String data = event.isEmpty() ? Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8)) : parts[1];

        events.add(new EventMessage(eventMessage, data));
        if (webClients.size() == 0) return;        
        sendServerEvent(eventMessage, data);
    }

    public void sendServerEvent(SSEType sseType, String data) {
        webClients.removeIf(connection -> !sendMessageToClient(connection, sseType, data));
    }

    private boolean sendMessageToClient(HttpExchange connection, SSEType event, String data) {
        Logger.logDebug("Event: " + event + " with data: " + data);
        try {
            String message = "data: " + event + ":" + data + "\n\n";
            OutputStream os = connection.getResponseBody();
            os.write(message.getBytes(StandardCharsets.UTF_8));
            os.flush();
            return true;
        } catch (IOException e) {
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

        String content_length = exchange.getRequestHeaders().getFirst("Content-length");
        if (content_length == null) {
            Logger.logError("content-length header in '" + exchange.getRequestURI().getPath() + "' is missing");
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
            return null;
        }

        try {
            int length = Integer.parseInt(content_length);
            byte[] data = exchange.getRequestBody().readNBytes(length);
            if (data.length != length) {
                Logger.logError("Premature end of stream in '" + exchange.getRequestURI().getPath() + "'");
                exchange.sendResponseHeaders(400, -1); // Bad Request
                exchange.close();
                return null;
            }
            return new String(data, StandardCharsets.UTF_8);
        } catch (NumberFormatException e) {
            Logger.logError("illegal content-length header in '" + exchange.getRequestURI().getPath() + "'");
            exchange.sendResponseHeaders(400, -1); // Bad Request
        } catch (IOException e) {
            Logger.logError("Error reading request body in '" + exchange.getRequestURI().getPath() + "'", e);
            exchange.sendResponseHeaders(400, -1); // Bad Request
        }
        exchange.close();
        return null;
    }

    private void updateFile(String path, String label, ReplacementType rType, String replacement) {
        try {
            Path filePath = Path.of(path);
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            switch (rType) {
                case Single:
                    updateSingleLine(lines, label, replacement);
                    break;
                case Multi:
                    updateMultiLine(lines, label, replacement);
                    break;
                default:
                    break;
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.logError("Error updating file: " + path, e);
        }
    }

    private void updateSingleLine(List<String> lines, String label, String replacement) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().endsWith(label)) {
                String line = lines.get(i);
                int spaces = (int) IntStream.range(0, line.length())
                    .takeWhile(pos -> line.charAt(pos) == ' ')
                    .count();
                lines.set(i, " ".repeat(spaces) + replacement + " " + label);
            }
        }
    }

    private void updateMultiLine(List<String> lines, String label, String replacement) {
        int openingLabel = -1;
        int closingLabel = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(label)) {
                if (openingLabel == -1) {
                    openingLabel = i;
                } else {
                    closingLabel = i;
                    break;
                }
            }
        }
        if (openingLabel == -1 || closingLabel == -1) {
            Logger.logError("Labels not found for multi-line replacement: " + label);
            return;
        }
        if (closingLabel <= openingLabel) {
            Logger.logError("Closing label is before opening label for multi-line replacement: " + label);
            return;
        }
        String startingLine = lines.get(openingLabel + 1);
        int spaces = (int) IntStream.range(0, startingLine.length())
                    .takeWhile(pos -> startingLine.charAt(pos) == ' ')
                    .count();

        for (int i = openingLabel + 1; i < closingLabel; i++) {
            lines.remove(openingLabel + 1);
        }
        lines.add(openingLabel + 1, " ".repeat(spaces) + replacement);
    }

    public void stop() {
        Logger.logInfo("Closing Server on port '" + port + "'");
        for (HttpExchange connection : webClients) {
            connection.close();
        }
        httpServer.stop(0);
    }
}