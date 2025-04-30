package lvp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lvp.logging.Logger;


public class Server {
    public final HttpServer httpServer;
    final int port;
    static int defaultPort = 50_001;
    static final String index = "/web/index.html";
    static Map<Integer,Server> serverInstances = new ConcurrentHashMap<>();

    static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    static int getDefaultPort() { return defaultPort; }

    public List<HttpExchange> sseClientConnections;
    List<String> paths = new CopyOnWriteArrayList<>();

    // lock required to temporarily block processing of `SSEType.LOAD`
    Lock lock = new ReentrantLock();
    Condition loadEventOccurredCondition = lock.newCondition();
    boolean loadEventOccured = false;


    public static Server onPort(int port) {
        port = Math.abs(port);
        try {
            if (!serverInstances.containsKey(port)) 
                serverInstances.put(port, new Server(port));
            return serverInstances.get(port);
        } catch (IOException e) {
            System.err.printf("Error starting Server: %s\n", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Server onPort() { return onPort(defaultPort); }

    private Server(int port) throws IOException {
        this.port = port;
        sseClientConnections = new CopyOnWriteArrayList<>(); // thread-safe variant of ArrayList

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
            String[] parts = message.split(":", 2);
            if(parts.length != 2) return;

            switch (parts[0]) {
                case "debug":
                    Logger.logDebug("(Client) " + parts[1]);       
                    break;
                case "info":
                    Logger.logInfo("(Client) " + parts[1]);
                    break;
                default:
                    Logger.logError("(Client) " + parts[1]);
                    break;
            }
        });

        // SSE context
        httpServer.createContext("/events", exchange -> {
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
            sseClientConnections.add(exchange);
            sendLoads(exchange);
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

    public void load(String data) {
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
        List<HttpExchange> deadConnections = new ArrayList<>();
        if (sseClientConnections.size() == 0) {
            System.out.println("Open http://localhost:" + port + " in your browser");
            return;
        }

        Logger.logInfo("New '" + sseType + "' Event");
        Logger.logDebug("Data: " + data);
                
        String message = "data: " + sseType + ":" + encodeData(data) + "\n\n";
        Logger.logDebug("Event Message: " + message.trim());

        for (HttpExchange connection : sseClientConnections) {
            try {                
                connection.getResponseBody().flush();
                connection.getResponseBody().write(message.getBytes());
                connection.getResponseBody().flush();
            } catch (IOException e) {
                deadConnections.add(connection);
                Logger.logError("Exchange '" + connection.getRemoteAddress() + "' did not respond. Closing...");    
            }
        }

        closeConnections(deadConnections);
        sseClientConnections.removeAll(deadConnections);
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
    
    public void createResponseContext(String path, Consumer<String> delegate) {
        createResponseContext(path, delegate, "-1");
    }

    public void createResponseContext(String path, Consumer<String> delegate, String id) {
        httpServer.createContext(path, exchange -> {
           String data = readRequestBody(exchange);
           if(data == null) return;
           delegate.accept(new String(data));
           sendServerEvent(SSEType.RELEASE, id);   
        });
    }

    public void stop() {
        Logger.logInfo("Closing Server on port '" + port + "'");
        sseClientConnections.clear();
        serverInstances.remove(port);
        httpServer.stop(0);
    }

    public static void shutdown() {
        serverInstances.forEach((_, v) -> v.stop());
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
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return new String(data);
        } catch (NumberFormatException e) {
            Logger.logError("illegal content-length header in '" + exchange.getRequestURI().getPath() + "'");
            exchange.sendResponseHeaders(400, -1); // Bad Request
            exchange.close();
        }
        return null;
    }
}