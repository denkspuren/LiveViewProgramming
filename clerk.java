import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

// jshell -R-ea --enable-preview

class Clerk {
    static LiveView view;
    static Markdown markdown;

    static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    // Open Server at default port
    static void serve() {
        serve(LiveView.defaultPort);
    }

    // Open Server at custom port
    static void serve(int port) {
        if (view != null) {
            view.stop();
        }
        try {
            view = new LiveView(port);
        } catch (Exception e) {
            System.err.printf("Error starting Server: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    static Markdown markdown(String markdownText) {
        if (markdown == null) markdown = new Markdown(Clerk.view);
        return markdown.markdown(markdownText);
    }
}

enum SSEType { WRITE, CALL, SCRIPT, LOAD; }

class LiveView {
    final HttpServer server;
    final int port;
    static final int defaultPort = 50001;
    static final String index = "./web/index.html";

    List<HttpExchange> sseClientConnections;

    // barrier required to temporarily block SSE event of type `SSEType.LOAD`
    private final CyclicBarrier barrier = new CyclicBarrier(2);

    private static final Map<String, String> mimeTypes = 
        Map.of("html", "text/html",
               "js", "text/javascript",
               "css", "text/css",
               "ico", "image/x-icon");

    public LiveView(int port) throws IOException {
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
    void load(String path)         { sendServerEvent(SSEType.LOAD, path); }

    public void stop() {
        sseClientConnections.clear();
        server.stop(0);
    }
}

interface ViewManagement {
    default LiveView view() {
        if (view == null) throw new NullPointerException("No view is set");
        return view;
    }
}

abstract class ViewManager implements ViewManagement {
    static LiveView lastView;
    LiveView view;
    ViewManager(LiveView view, String path) {
        if (view == null) view = lastView;
        if (view == null) throw new NullPointerException("No view is given and default view is not set");
        if (view != lastView) lastView = view;
        (this.view = view).load(path);
    }
    ViewManager(String path) { this(null, path); }
}


/open skills/File/File.java
/open skills/Turtle/Turtle.java
/open skills/Markdown/Markdown.java

