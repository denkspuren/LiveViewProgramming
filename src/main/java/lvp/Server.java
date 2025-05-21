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
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lvp.logging.LogLevel;
import lvp.logging.Logger;


public class Server {

    private final HttpServer httpServer;

    final int port;
    static int defaultPort = 50_001;
    static final String index = "/web/index.html";

    static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    static int getDefaultPort() { return defaultPort; }

    public List<HttpExchange> webClients;
    List<String> paths = new CopyOnWriteArrayList<>();

    // lock required to temporarily block processing of `SSEType.LOAD`
    Lock lock = new ReentrantLock();
    Condition loadEventOccurredCondition = lock.newCondition();
    boolean loadEventOccured = false;

    public Server(int port) throws IOException {
        this.port = port;
        webClients = new CopyOnWriteArrayList<>(); // thread-safe variant of ArrayList

        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        httpServer.createContext("/loaded", this::handleLoaded);
        httpServer.createContext("/log", this::handleLog);
        httpServer.createContext("/interact", this::handleInteract);
        httpServer.createContext("/events", this::handleEvents);
        httpServer.createContext("/", this::handleRoot);

        httpServer.setExecutor(Executors.newFixedThreadPool(5));
        httpServer.start();
    }

    private void handleLoaded(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            Logger.logError("Method not allowed in '/loaded'");
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
        lock.lock();
        try {
            loadEventOccured = true;
            loadEventOccurredCondition.signalAll();
        } finally {
            lock.unlock();
        }
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
        String[] parts = message.split(":", 3);
        if (parts.length != 3) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
            Logger.logError("Illegal interaction message: " + message);
            return;
        }

        exchange.sendResponseHeaders(200, 0);
        exchange.close();

        String path = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String label = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String replacement = new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8);
        updateFile(path, label, replacement);
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
        sendLoads(exchange);
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
        if (parts.length != 2) return;

        Optional<SSEType> event = Arrays.stream(SSEType.values())
            .filter(sseType -> sseType.name().equals(parts[0]))
            .findFirst();

        if (event.isEmpty()) {
            Logger.logError(message);
            return;
        }

        if (event.get().equals(SSEType.LOAD)) {
            if (!paths.contains(parts[1])) load(parts[1]);
        } else {
            sendServerEvent(event.get(), parts[1]);
        }
    }

    public void load(String data) {
        if (paths.contains(data)) return;
        lock.lock();
        loadEventOccured = false;

        try {
            sendServerEvent(SSEType.LOAD, data);

            loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
            if (loadEventOccured) paths.add(data);
            else Logger.logError("LOAD-Timeout: " + data);
        } catch (InterruptedException e) {
            Logger.logError("LOAD-Interruption: " + data, e);
        } finally {
            lock.unlock();
        }
    }

    public void sendServerEvent(SSEType sseType, String data) {
        if (webClients.size() == 0) {
            System.out.println("Open http://localhost:" + port + " in your browser");
            return;
        }
        webClients.removeIf(connection -> !sendMessageToClient(connection, sseType, data));
    }

    public void sendLoads(HttpExchange connection) {
        if(paths.size() == 0) return;

        Logger.logInfo("Sending " + paths.size() + " paths to '" + connection.getRemoteAddress() + "'...");
        lock.lock();
        for (String path : paths) {
            loadEventOccured = false;
            
            try {
                sendMessageToClient(connection, SSEType.LOAD, path);
                loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
                if (!loadEventOccured) System.err.println("LOAD-Timeout: " + path);
            } catch (InterruptedException e) {
                Logger.logError("LOAD-Interruption: " + path, e);
            }
        }
        lock.unlock();
        Logger.logInfo("Successfully sent " + paths.size() + " paths to '" + connection.getRemoteAddress() + "'");
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

    private void updateFile(String path, String label, String replacement) {
        try {
            Path filePath = Path.of(path);
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().endsWith(label)) {
                    String line = lines.get(i);
                    int spaces = (int) IntStream.range(0, line.length())
                        .takeWhile(pos -> line.charAt(pos) == ' ')
                        .count();
                    lines.set(i, " ".repeat(spaces) + replacement + " " + label);
                }
           }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.logError("Error updating file: " + path, e);
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