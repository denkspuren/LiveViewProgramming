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
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

// jshell -R-ea --enable-preview

class Clerk {
    static String templateFileName = "./web/indexTemplate.html";
    static String preContent = "",
                  content = "",
                  postContent = "";

    static LiveView view;

    static void writeToFile(String fileName, String text) {
        try {
            Files.writeString(Path.of(fileName), text);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
            System.exit(1);
        }
    }
    static void writeResult(String fileName) {
        writeToFile(fileName, STR.
            """
            \{preContent}
            \{content}
            \{postContent}
            """);
    }

    static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        List<String> snippet = new ArrayList<>();
        boolean skipLines = true;
        boolean isInLabels;
        try {
            List<String> lines = Files.readAllLines(Path.of(fileName));
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

    static String cutOut(String fileName, String... labels) {
        return cutOut(fileName, false, false, labels);
    }

    static String readFile(String fileName) {
        return cutOut(fileName, true, true, "");
    }

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
            view = LiveView.of(port);
        } catch (Exception e) {
            System.err.printf("Error starting Server: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    // Send HTML Elements
    static void write(String text) {
        view.send(STR."write:\{text}");
    }

    // Send Javascript code
    static void script(String code) {
        // bind declarations to this to make them accessible between calls
        String evalCode = code;
                //.replaceAll("(var |const |let )", "this.")
                //.replaceAll("function\\s+(\\S+)\\s*\\((.*)\\)\\s*\\{", "this.$1 = ($2) => {");
        view.send(STR."script:\{evalCode}");
    }

    // Markdown as example on how to use write() and script()
    static void markdown(String markdown) {
        String ID = generateID(10);
        write(STR."""
            <div id="\{ID}">
            \{markdown}
            </div>
            """);
        script(STR."""
            var markdownContent = document.getElementById("\{ID}").textContent;
            var renderedHTML = marked.parse(markdownContent);
            document.getElementById("\{ID}").innerHTML = renderedHTML;
            """);
    }
}

class LiveView {
    final HttpServer server;
    final String index = "./web/index.html";

    int port;
    static final int defaultPort = 50001;
    List<HttpExchange> activeConnections;

    private static final Map<String, String> mimeTypes = 
        Map.of("html", "text/html",
               "js", "text/javascript",
               "css", "text/css");

    public static LiveView of() throws IOException {
        return of(defaultPort);
    }

    public static LiveView of(int port) throws IOException {
        return new LiveView(port);
    }

    public LiveView(int port) throws IOException {
        this.port = port;
        activeConnections = new ArrayList<>();

        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        // SSE context
        server.createContext("/events", (exchange) -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.getResponseHeaders().add("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);
            activeConnections.add(exchange);
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

    // event = "action:data"
    void send(String event) {
        List<HttpExchange> deadConnections = new ArrayList<>();

        for (HttpExchange exchange : activeConnections) {
            try {
                exchange.getResponseBody()
                        .write(("data: " + event.replaceAll("(\\r|\\n|\\r\\n)", "\\\\n") + "\n\n")
                                .getBytes(StandardCharsets.UTF_8));
                exchange.getResponseBody().flush();
            } catch (Exception e) {
                deadConnections.add(exchange);
            }
        }
        activeConnections.removeAll(deadConnections);
    }

    public void stop() {
        server.stop(0);
    }
}

class Turtle {
    final String ID;
    final int width, height;

    Turtle(int width, int height) {
        this.width = width;
        this.height = height;
        ID = Clerk.generateID(6);
        Clerk.write(STR."""
        <canvas id="turtleCanvas\{ID}" width="\{width}" height="\{height}" style="border:1px solid #000;"></canvas>
        """);
        // Clerk.script(STR."const turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));");
        Clerk.script(STR."turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));");
    }

    Turtle() {
        this(500, 500);
    }

    Turtle penDown() {
        Clerk.script(STR."turtle\{ID}.penDown();");
        return this;
    }

    Turtle penUp() {
        Clerk.script(STR."turtle\{ID}.penUp();");
        return this;
    }

    Turtle forward(double distance) {
        Clerk.script(STR."turtle\{ID}.forward(\{distance});");
        return this;
    }

    Turtle backward(double distance) {
        Clerk.script(STR."turtle\{ID}.backward(\{distance});");
        return this;
    }

    Turtle left(double degrees) {
        Clerk.script(STR."turtle\{ID}.left(\{degrees});");
        return this;
    }

    Turtle right(double degrees) {
        Clerk.script(STR."turtle\{ID}.right(\{degrees});");
        return this;
    }
}