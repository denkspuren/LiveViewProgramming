package lvp;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    public HttpExchange javaClient;
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

        // loaded-Request to signal successful processing of SSEType.LOAD
        httpServer.createContext("/loaded", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
                Logger.logError("Method not allowed in '/loaded'");
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            lock.lock();
            try { // try/finally pattern for locks
                loadEventOccured = true;
                loadEventOccurredCondition.signalAll();
            } finally {
                lock.unlock();
            }
        });

        //logging
        httpServer.createContext("/log", exchange -> {
            String message = readRequestBody(exchange);
            if(message == null) return;
            
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            String[] parts = message.split(":", 2);
            if(parts.length != 2) return;

            Logger.log(LogLevel.fromString(parts[0]), parts[1]);
        });

        httpServer.createContext("/receive", exchange -> {
            String message = readRequestBody(exchange);
            if(message == null) return;
            String[] parts = message.split(":", 2);
            if(parts.length != 2) return;

            SSEType event = SSEType.valueOf(parts[0]);
            Logger.logDebug("Received '" + event + "' Event!");
            if (event.equals(SSEType.LOAD)) {
                if (!paths.contains(parts[1])) load(parts[1]);
            } else {
                sendServerEvent(event, parts[1]);
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });

        httpServer.createContext("/new", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                Logger.logError("Method not allowed in '/new'");
                return;
            }
            String message = readRequestBody(exchange);
            if(message == null) return;
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            String[] parts = message.split(":", 2);
            if(parts.length != 2) return;
            
            createResponseContext(parts[0], parts[1]);
        });


        // SSE context
        httpServer.createContext("/events", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                Logger.logError("Method not allowed in '/events'");
                return;
            }
            
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());

            Logger.logInfo("New SSE Exchange for '" + exchange.getLocalAddress() + "' at '" + exchange.getRemoteAddress() + "'");
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.getResponseHeaders().add("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);
            
            String type = queryParams.getOrDefault("type", "web");
            if (type.equalsIgnoreCase("web")) { //TODO: Reverse
                webClients.add(exchange);
                sendLoads(exchange);
            }
            else {
                javaClient = exchange;
            }
        });

        // initial html site
        httpServer.createContext("/", exchange -> {
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
        });

        httpServer.setExecutor(Executors.newFixedThreadPool(5));
        httpServer.start();
    }

    private static Map<String, String> parseQueryParams(String query) {
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

    public void load(String data) {
        if (paths.contains(data)) return;
        lock.lock();
        loadEventOccured = false;

        try {
            sendServerEvent(SSEType.LOAD, data);

            loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
            if (loadEventOccured) paths.add(data);
            else System.err.println("LOAD-Timeout: " + data);

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

        Logger.logDebug("New '" + sseType + "' Event");
        Logger.logDebug("Data: " + data);
                
        String message = "data: " + sseType + ":" + encodeData(data) + "\n\n";
        Logger.logDebug("Event Message: " + message.trim());

        webClients.removeIf(connection -> {
            try {
                connection.getResponseBody().flush();
                connection.getResponseBody().write(message.getBytes());
                connection.getResponseBody().flush();
                return false;
            } catch (IOException _) {
                Logger.logError("Web exchange '" + connection.getRemoteAddress() + "' did not respond. Closing...");
                connection.close();
                return true;

            }
        });
    }

    public void sendResponse(String id, String path, String data) {
        Logger.logInfo("Sending Response from '" + path + "'");
        Logger.logDebug("Data: " + data);
                
        String message = "data: " + id + ":" + encodeData(data) + "\n\n";
        Logger.logDebug("Event Message: " + message.trim());

        if (javaClient != null) {
            try {
                javaClient.getResponseBody().flush();
                javaClient.getResponseBody().write(message.getBytes());
                javaClient.getResponseBody().flush();
            }  catch (IOException _) {
                Logger.logError("Java exchange '" + javaClient.getRemoteAddress() + "' did not respond. Closing...");
                javaClient.close();
                javaClient = null;

            }
        }
    }

    public void sendLoads(HttpExchange connection) {
        if(paths.size() == 0) return;

        Logger.logInfo("Sending " + paths.size() + " paths to '" + connection.getRemoteAddress() + "'...");
        lock.lock();
        for (String path : paths) {
            loadEventOccured = false;
            String message = "data: " + SSEType.LOAD + ":" + encodeData(path) + "\n\n";
            Logger.logDebug("Event Message: " + message.trim());
            
            try {
                connection.getResponseBody().flush();
                connection.getResponseBody().write(message.getBytes());
                connection.getResponseBody().flush();

                loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
                if (!loadEventOccured) System.err.println("LOAD-Timeout: " + path);
            } catch (InterruptedException e) {
                Logger.logError("LOAD-Interruption: " + path, e);
            } catch (IOException e) {
                Logger.logError("Exchange '" + connection.getRemoteAddress() + "' did not respond.");
                break;
            }
        }
        lock.unlock();
        Logger.logInfo("Successfully sent " + paths.size() + " paths to '" + connection.getRemoteAddress() + "'");
    }

    public void createResponseContext(String path, String id) {
        httpServer.createContext(path, exchange -> {
           String data = readRequestBody(exchange);
           if(data == null) return;
           exchange.sendResponseHeaders(200, 0);
           exchange.close();
           sendResponse(id, path, data);
           //sendServerEvent(SSEType.RELEASE, id); // TODO: Send release through client
        });
    }

    private String encodeData(String data) {
        byte[] binaryData = data.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(binaryData);
    }

    private void closeConnections(List<HttpExchange> connections) {
        for (HttpExchange connection : connections) {
            connection.close();
        }
    }

    public void closeJavaClient() {
        if (javaClient != null) {
            javaClient.close();
            javaClient = null;
        }
    }

    //TODO: Sauberes schließen im Fall von Fehlern
    private String readRequestBody(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            Logger.logError("Method not allowed in '" + exchange.getRequestURI().getPath() + "'");
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return null;
        }

        String content_length = exchange.getRequestHeaders().getFirst("Content-length");
        if (content_length == null) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            Logger.logError("content-length header in '" + exchange.getRequestURI().getPath() + "' is missing");
            return null;
        }

        try {
            int length = Integer.parseInt(content_length);
            byte[] data = new byte[length];
            exchange.getRequestBody().read(data);   // Reads the request body into the byte array 'data'
            return new String(data);
        } catch (NumberFormatException e) {
            Logger.logError("illegal content-length header in '" + exchange.getRequestURI().getPath() + "'");
            exchange.sendResponseHeaders(400, -1); // Bad Request
        }
        return null;
    }

    

    public void stop() {
        Logger.logInfo("Closing Server on port '" + port + "'");
        closeConnections(webClients);
        if (javaClient != null) javaClient.close();
        httpServer.stop(0);
    }
}