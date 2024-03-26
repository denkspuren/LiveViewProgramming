import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.StringTemplate.STR;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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

    void write(String html)        { sendServerEvent(SSEType.WRITE, html); }
    void call(String javascript)   { sendServerEvent(SSEType.CALL, javascript); }
    void script(String javascript) { sendServerEvent(SSEType.SCRIPT, javascript); }
    void load(String path)         { sendServerEvent(SSEType.LOAD, path); }

    public void stop() {
        sseClientConnections.clear();
        server.stop(0);
    }
}

class File { // Class with static methods for file operations
    static void write(String fileName, String text) {
        try {
            Files.writeString(Path.of(fileName), text);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
            System.exit(1);
        }
    }

    static String cutOut(Path path, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        List<String> snippet = new ArrayList<>();
        boolean skipLines = true;
        boolean isInLabels;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                isInLabels = Arrays.stream(labels).anyMatch(label -> line.trim().equals(label));
                if (isInLabels) {
                    if (skipLines && includeStartLabel)
                        snippet.add(line);
                    if (!skipLines && includeEndLabel)
                        snippet.add(line);
                    skipLines = !skipLines;
                    continue;
                }
                if (skipLines)
                    continue;
                snippet.add(line);
            }
        } catch (IOException e) {
            System.err.printf("Error reading %s\n", e.getMessage());
            System.exit(1);
        }
        return snippet.stream().collect(Collectors.joining("\n")) + "\n";
    }

    static String cutOut(Path path, String... labels) { return cutOut(path, false, false, labels); }
    static String read(Path path) { return cutOut(path, true, true, ""); }

    static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        return cutOut(Path.of(fileName), includeStartLabel, includeEndLabel, labels);
    }
    static String cutOut(String fileName, String... labels) {
        return cutOut(fileName, false, false, labels);
    }
    static String read(String fileName) {
        return cutOut(fileName, true, true, "");
    }
}

class Turtle {
    static List<LiveView> views = new ArrayList<>();
    final LiveView view;
    final String ID;
    final int width, height;

    Turtle(LiveView view, int width, int height) {
        if (Objects.isNull(view)) view = !views.isEmpty() ? views.getFirst() : null;
        if (Objects.isNull(view)) throw new IllegalArgumentException("No view defined");
        if (!views.contains(view)) {
            view.load("Turtle/turtle.js");
            views.add(view);
        }
        this.view = view;
        this.width  = Math.max(1, Math.abs(width));  // width is at least of size 1
        this.height = Math.max(1, Math.abs(height)); // height is at least of size 1
        ID = Clerk.generateID(6);
        view.write(STR."""
            <canvas id="turtleCanvas\{ID}" width="\{this.width}" height="\{this.height}" style="border:1px solid #000;">
            </canvas>
        """);
        view.script(STR."const turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));");
    }

    Turtle(LiveView view) { this(view, 500, 500); }
    Turtle(int width, int height) { this(null, width, height); }
    Turtle() { this(null); }

    Turtle penDown() {
        view.call(STR."turtle\{ID}.penDown();");
        return this;
    }

    Turtle penUp() {
        view.call(STR."turtle\{ID}.penUp();");
        return this;
    }

    Turtle forward(double distance) {
        view.call(STR."turtle\{ID}.forward(\{distance});");
        return this;
    }

    Turtle backward(double distance) {
        view.call(STR."turtle\{ID}.backward(\{distance});");
        return this;
    }

    Turtle left(double degrees) {
        view.call(STR."turtle\{ID}.left(\{degrees});");
        return this;
    }

    Turtle right(double degrees) {
        view.call(STR."turtle\{ID}.right(\{degrees});");
        return this;
    }
}

record Markdown(LiveView view) {
    public Markdown { view.load("https://cdn.jsdelivr.net/npm/marked/marked.min.js"); }
    public Markdown markdown(String markdownText) {
        String ID = Clerk.generateID(10);
        view.write(STR."""
            <div id="\{ID}">
            \{markdownText}
            </div>
            """);
        view.script(STR."""
            var markdownContent = document.getElementById("\{ID}").textContent;
            var renderedHTML = marked.parse(markdownContent);
            document.getElementById("\{ID}").innerHTML = renderedHTML;
            """);
        return this;
    }    
}
