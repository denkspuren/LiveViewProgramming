import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

// To run this code type `jshell -R-ea --enable-preview`

enum SSEType { HTML, CALL, LOAD, CLEAR, EXECUTE, STORE, RESTORE; }

class LiveView {
    final HttpServer server;
    final int port;
    static int defaultPort = 50_001;
    static final String index = "./web/index.html";
    static Map<Integer,LiveView> views = new ConcurrentHashMap<>();
    List<String> paths = new ArrayList<>();

    static void setDefaultPort(int port) { defaultPort = port != 0 ? Math.abs(port) : 50_001; }
    static int getDefaultPort() { return defaultPort; }

    List<HttpExchange> sseClientConnections;

    // lock required to temporarily block processing of `SSEType.LOAD`
    Lock lock = new ReentrantLock();
    Condition loadEventOccurredCondition = lock.newCondition();
    boolean loadEventOccured = false;

    List<String> cache = new ArrayList<>();


    static LiveView onPort(int port) {
        port = Math.abs(port);
        try {
            if (!views.containsKey(port))
                views.put(port, new LiveView(port));
            return views.get(port);
        } catch (IOException e) {
            System.err.printf("Error starting Server: %s\n", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    static LiveView onPort() { return onPort(defaultPort); }

    private LiveView(int port) throws IOException {
        this.port = port;
        sseClientConnections = new CopyOnWriteArrayList<>(); // thread-safe variant of ArrayList

        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        // loaded-Request to signal successful processing of SSEType.LOAD
        server.createContext("/loaded", exchange -> {
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
        server.createContext("/events", exchange -> {
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
        server.createContext("/", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            final String path = exchange.getRequestURI().getPath().equals("/") ? index
                    : "." + exchange.getRequestURI().getPath();
            try (final InputStream stream = new FileInputStream(path)) {
                final byte[] bytes = stream.readAllBytes();
                exchange.getResponseHeaders().add("Content-Type", Files.probeContentType(Path.of(path)) + "; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().flush();
            } finally {
                exchange.close();
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(5));
        server.start();
    }

    String encode(String data) {
        byte[] binaryData = data.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(binaryData);
    }

    void cache(SSEType action, String data) {
        cache.add(pack(action, data));
    }

    void cache(SSEType action) {
        cache.add(action.toString());
    }

    void packAndSend() {
        if (cache.isEmpty()) {
            System.err.println("Nothing to send!");
            return;
        }

        String message = cache.get(0);
        for (int i = 1; i < cache.size(); i++) {
            message += ":" + cache.get(i);
        }
        sendServerEvent(message);
        cache.clear();
    }

    String pack(SSEType action, String data) {
        return action + ":" + encode(data);
    }

    void load(String data) {
        lock.lock();
        try {
            String message = pack(SSEType.LOAD, data);
            sendServerEvent(message);
            if (!loadEventOccured) {
                loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
                if (loadEventOccured) paths.add(data);
                else System.err.println("LOAD-Timeout: " + data);
            }
        } catch (InterruptedException e) {
            System.err.println("LOAD-Timeout: " + data + ", " + e);
        } finally {
            loadEventOccured = false;
            lock.unlock();
            
        }
    }

    void sendServerEvent(String message) {
        message = "data:" + message + "\n\n";
        List<HttpExchange> deadConnections = new ArrayList<>();
        for (HttpExchange connection : sseClientConnections) {
            try {
                connection.getResponseBody().write(message.getBytes());
                connection.getResponseBody().flush();                
            } catch (IOException e) {
                deadConnections.add(connection);
            } 
        }
        sseClientConnections.removeAll(deadConnections);
    }

    void createResponseContext(String path, Consumer<String> delegate) {
        server.createContext(path, exchange -> {
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
        views.remove(port);
        server.stop(0);
    }

    static void shutdown() {
        views.forEach((k, v) -> v.stop());
    }
}

interface Clerk {
    static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }

    static LiveView view(int port) { return LiveView.onPort(port); }
    static LiveView view() { return view(LiveView.getDefaultPort()); }

    static void html(LiveView view, String text)         { view.cache(SSEType.HTML, text); }
    static void callJs(LiveView view, String javascript)   { view.cache(SSEType.CALL, javascript); }
    static void execute(LiveView view)                   { view.cache(SSEType.EXECUTE); }
    static void send(LiveView view)                      { view.packAndSend(); }

    //old methods


    static void load(LiveView view, String path) {
        if (!view.paths.contains(path.trim())) view.load(path);
    }
    static void load(LiveView view, String onlinePath, String offlinePath) {
        load(view, onlinePath + ", " + offlinePath);
    }
    static void clear(LiveView view) { view.cache(SSEType.CLEAR); }
    // Clear + Execute + instant send
    static void clear() { 
        clear(view());
        execute(view());
        send(view());
     }

    // static void store(String id);
    // static void restore(String id);

    // static void markdown(String text) { new MarkdownIt(view()).write(text); }
}

// /open skills/Text/Text.java
// /open skills/ObjectInspector/ObjectInspector.java
// /open clerks/Turtle/Turtle.java
// /open clerks/Markdown/Marked.java
// /open clerks/Markdown/MarkdownIt.java
// /open clerks/TicTacToe/TicTacToe.java
// /open clerks/Dot/Dot.java
// /open clerks/Input/Slider.java

LiveView view = Clerk.view();
Clerk.html(view, "<h1>Hello World</h1>");
Clerk.html(view, "<p>This will be a test</p>");
Clerk.execute(view);
// Clerk.send(view);