package lvp.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lvp.SSEType;
import lvp.logging.LogLevel;
import lvp.logging.Logger;


public class Server {
    private final HttpServer httpServer;
    private final List<String> createdContexts = new CopyOnWriteArrayList<>();

    private FileWatcher fileWatcher;

    final int port;
    static int defaultPort = 50_001;
    static final String index = "/web/index.html";

    public static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    public static int getDefaultPort() { return defaultPort; }

    private final SSEManager sseManager;


    public Server(int port) throws IOException {
        this.port = port;
        sseManager = new SSEManager(port);

        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        httpServer.createContext("/loaded", exchange -> handlePostRequest(exchange, sseManager::processLoaded));
        httpServer.createContext("/log", exchange -> handlePostRequest(exchange, this::processLog));
        httpServer.createContext("/receive", exchange -> handlePostRequest(exchange, sseManager::processReceive));
        httpServer.createContext("/new", exchange -> handlePostRequest(exchange, this::processNew));
        httpServer.createContext("/events", sseManager::handleEvents);
        httpServer.createContext("/", this::handleRoot);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "ShutdownHook"));

        httpServer.setExecutor(Executors.newFixedThreadPool(5));
        httpServer.start();
    }
    
    public void startFileWatcher(Path path, String fileNamePattern) throws IOException {
        this.fileWatcher = new FileWatcher(path, fileNamePattern, sseManager::closeJavaClient);
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

    private void handlePostRequest(HttpExchange exchange, Consumer<String> processor) throws IOException {
        String message = readRequestBody(exchange);
        if (message == null) return;

        exchange.sendResponseHeaders(200, 0);
        exchange.close();
        processor.accept(message);
    }

    private void processLog(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        Logger.log(LogLevel.fromString(parts[0]), parts[1]);
    }

    private void processNew(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        createResponseContext(parts[0], parts[1]);
    }
    
    
    public void load(String data) { sseManager.load(data); }
    public void sendServerEvent(SSEType sseType, String data) { sseManager.sendToWeb(sseType, data); }
        
    public void createResponseContext(String path, String id) {
        if (createdContexts.contains(path)) {
            Logger.logDebug("Context '" + path + "' already exists");
            return;
        }
        
        createdContexts.add(path);
        httpServer.createContext(path, exchange -> {
           String data = readRequestBody(exchange);
           if(data == null) return;
           exchange.sendResponseHeaders(200, 0);
           exchange.close();
           sseManager.sendToJava(id, path, data);
        });
    }

    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
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

    public static String encodeData(String data) {
        byte[] binaryData = data.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(binaryData);
    }

    public void stop() {
        Logger.logInfo("Closing Server on port '" + port + "'");
        sseManager.closeConnections();
        sseManager.closeJavaClient();
        if (fileWatcher != null) fileWatcher.stop();
        httpServer.stop(0);
        System.out.println("Server stopped.");
    }
}
