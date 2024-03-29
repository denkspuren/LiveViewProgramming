import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

// To run this code type `jshell -R-ea --enable-preview`

enum SSEType { WRITE, CALL, SCRIPT, LOAD; }

class LiveView {
    final HttpServer server;
    final int port;
    static final String index = "./web/index.html";
    static Map<Integer,LiveView> views = new HashMap<>();
    List<String> paths = new ArrayList<>();

    List<HttpExchange> sseClientConnections;

    // barrier required to temporarily block SSE event of type `SSEType.LOAD`
    private final CyclicBarrier barrier = new CyclicBarrier(2);

    static LiveView onPort(int port) {
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

    private static final Map<String, String> mimeTypes = 
        Map.of("html", "text/html",
               "js", "text/javascript",
               "css", "text/css",
               "ico", "image/x-icon");

    private LiveView(int port) throws IOException {
        this.port = port;
        sseClientConnections = new CopyOnWriteArrayList<>(); // thread-safe variant of ArrayList

        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        server.createContext("/loaded", exchange -> {
            // System.out.println("loaded: " + exchange.toString());
            if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            try {
                barrier.await(30L, TimeUnit.SECONDS);
            } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                System.err.print(e);
                System.exit(1);
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
            // System.out.println("Added exchange " + exchange);
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
                exchange.getResponseHeaders().add("Content-Type", guessMimeType(path) + "; charset=utf-8");
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

    String guessMimeType(final String path) {
        final int index = path.lastIndexOf('.');
        if (index >= 0) {
            final String ending = path.substring(index + 1);
            return mimeTypes.getOrDefault(ending, "text/plain");
        } else {
            return "text/plain";
        }
    }

    void sendServerEvent(SSEType sseType, String data) {
        List<HttpExchange> deadConnections = new ArrayList<>();
        for (HttpExchange connection : sseClientConnections) {
            try {
                connection.getResponseBody()
                          .write(("data: " + (sseType + ":" + data).replaceAll("(\\r|\\n|\\r\\n)", "\\\\n") + "\n\n")
                                  .getBytes(StandardCharsets.UTF_8));
                connection.getResponseBody().flush();
                if (sseType == SSEType.LOAD) {
                    try {
                        barrier.await(30L, TimeUnit.SECONDS);
                    } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                        System.err.print(SSEType.LOAD + " missed confirmation: " + e);
                        deadConnections.add(connection); // connection is assumed to be dead
                    }                    
                }
            } catch (IOException e) {
                deadConnections.add(connection);
            }
        }
        sseClientConnections.removeAll(deadConnections);
    }

    void createResponseContext(String path, Consumer<String> delegate) throws IOException {
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

    void write(String html)        { sendServerEvent(SSEType.WRITE, html); }
    void call(String javascript)   { sendServerEvent(SSEType.CALL, javascript); }
    void script(String javascript) { sendServerEvent(SSEType.SCRIPT, javascript); }
    void load(String path) { 
        if (!paths.contains(path)) {
            sendServerEvent(SSEType.LOAD, path);
            paths.add(path);
        }
    } 

    public void stop() {
        sseClientConnections.clear();
        views.remove(port);
        server.stop(0);
    }
}

interface Clerk {
    static final int DEFAULT_PORT = 50_001;

    static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }

    static LiveView checkView(LiveView view) {
        if (view == null) throw new NullPointerException("view must not be null");
        return view;
    }

    static LiveView loadPath(LiveView view, String path) {
        if (path != null && !path.isEmpty() && !path.isBlank()) {
            checkView(view).load(path);
            return view;
        }
        return null;
    }

    static LiveView view(int port) { return LiveView.onPort(port); }
    static LiveView view() { return view(DEFAULT_PORT); }

    static void markdown(String text) { new Markdown(Clerk.view()).write(text); }
}

/open skills/File/File.java
/open skills/Turtle/Turtle.java
/open skills/Markdown/Markdown.java

LiveView view = Clerk.view();