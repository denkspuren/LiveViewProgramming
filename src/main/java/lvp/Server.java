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

public class Server {
    public final HttpServer httpServer;
    final int port;
    static int defaultPort = 50_001;
    static final String index = "/web/index.html";
    static Map<Integer,Server> serverInstances = new ConcurrentHashMap<>();
    List<String> paths = new ArrayList<>();

    static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    static int getDefaultPort() { return defaultPort; }

    public List<HttpExchange> sseClientConnections;

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

        // SSE context
        httpServer.createContext("/events", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.getResponseHeaders().add("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);
            sseClientConnections.add(exchange);
        });

        // initial html site
        httpServer.createContext("/", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            final String path = exchange.getRequestURI().getPath().equals("/") ? index : exchange.getRequestURI().getPath();

            try (final InputStream stream = Server.class.getResourceAsStream(path)) {
                final byte[] bytes = stream.readAllBytes();
                exchange.getResponseHeaders().add("Content-Type", Files.probeContentType(Path.of(path)) + "; charset=utf-8");
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

    public void sendServerEvent(SSEType sseType, String data) {
        List<HttpExchange> deadConnections = new ArrayList<>();
        for (HttpExchange connection : sseClientConnections) {
            if (sseType == SSEType.LOAD) {
                lock.lock();
                loadEventOccured = false; // NEU
            }
            try {
                byte[] binaryData = data.getBytes(StandardCharsets.UTF_8);
                String base64Data = Base64.getEncoder().encodeToString(binaryData);
                String message = "data: " + sseType + ":" + base64Data + "\n\n";
                connection.getResponseBody().flush();
                connection.getResponseBody()
                          .write(message.getBytes());
                connection.getResponseBody().flush();
                if (sseType == SSEType.LOAD) {
                    loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
                    if (loadEventOccured) paths.add(data);
                    else System.err.println("LOAD-Timeout: " + data);
                }
            } catch (IOException e) {
                deadConnections.add(connection);
                System.out.println("Dead Connection!");
            } catch (InterruptedException e) {
                System.err.println("LOAD-Interruption: " + data + ", " + e);
            } finally {
                if (sseType == SSEType.LOAD) {
                    // loadEventOccured = false; // REMOVED
                    lock.unlock();
                }
            }
        }
        sseClientConnections.removeAll(deadConnections); // TODO: need to be closed
    }

    public void createResponseContext(String path, Consumer<String> delegate) {
        createResponseContext(path, delegate, "-1");
    }

    public void createResponseContext(String path, Consumer<String> delegate, String id) {
        httpServer.createContext(path, exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            String content_length = exchange.getRequestHeaders().getFirst("Content-length");
            if (content_length == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            try {
                int length = Integer.parseInt(content_length);
                byte[] data = new byte[length];
                exchange.getRequestBody().read(data);
                delegate.accept(new String(data));
                sendServerEvent(SSEType.RELEASE, id);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
    }

    public void stop() {
        sseClientConnections.clear();
        serverInstances.remove(port);
        httpServer.stop(0);
    }

    public static void shutdown() {
        serverInstances.forEach((k, v) -> v.stop());
    }
}