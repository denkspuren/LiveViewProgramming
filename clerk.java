import java.io.Closeable;
import java.io.IOException;
import static java.lang.StringTemplate.STR;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

// jshell -R-ea --enable-preview

interface ClerkOutput {

    String getTarget();

    void markdown(String markdown, String id);

    void script(String addonIdentifier, String code, Object... args);

    void write(String text);

    void reset();
}

abstract class ClerkAddonOutput {
    static Set<ClerkAddon> addons;
}

class ClerkServer extends ClerkAddonOutput implements ClerkOutput {
    static int defaultPort = 50001;
    private static final Map<String, String> mimeTypes = new HashMap<>();

    final HttpServer server;
    final String index = "./web/server/index.html";
    List<HttpExchange> activeConnections;

    int port;

    private enum Commands {
        Markdown,
        Script,
        Write
    }

    private record Message(Commands cmd, String addonId, String content) {
        @Override
        public String toString() {
            return "{\"command\": \"" + cmd + "\", \"addonId\": \"" + addonId + "\", \"content\": \""
                    + content.replaceAll("(\")+", "\\\\\"") + "\"}";
        }
    }

    static {
        mimeTypes.put("html", "text/html");
        mimeTypes.put("js", "text/javascript");
        mimeTypes.put("css", "text/css");
    }

    public static ClerkServer of() throws IOException {
        return of(defaultPort);
    }

    public static ClerkServer of(int port) throws IOException {
        return new ClerkServer(port);
    }

    public ClerkServer(int port) throws IOException {
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

    void send(String event) {
        List<HttpExchange> dead = new ArrayList<>();

        for (HttpExchange exchange : activeConnections) {
            try {
                exchange.getResponseBody()
                        .write(("data: " + event.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n") + "\n\n")
                                .getBytes(StandardCharsets.UTF_8));
                exchange.getResponseBody().flush();
            } catch (Exception e) {
                dead.add(exchange);
            }
        }
        activeConnections.removeAll(dead);
    }

    @Override
    public String getTarget() {
        return Integer.toString(port);
    }

    @Override
    public void markdown(String markdown, String unused) {
        send(new Message(Commands.Markdown, "", markdown).toString());

    }

    @Override
    public void script(String addonIdentifier, String code, Object... args) {
        Optional<ClerkAddon> addon = addons.stream().filter(x -> x.getIdentifier().equals(addonIdentifier)).findFirst();
        if (addon.isEmpty())
            return;

        send(new Message(Commands.Script, addonIdentifier,
                String.format(Locale.ENGLISH, addon.get().translateToServeStatements(code), args))
                .toString());

    }

    @Override
    public void write(String text) {
        send(new Message(Commands.Write, "", text).toString());

    }

    @Override
    public void reset() {
        server.stop(0);
    }
}

class ClerkFileOutput extends ClerkAddonOutput implements ClerkOutput {
    static String defaultLocation = "./build/index.html";
    String preContent = "",
            postContent = "",
            content = "";
    String outputLocation;

    static ClerkFileOutput of(String preContent, String postContent) {
        return of(defaultLocation, preContent, postContent);
    }

    static ClerkFileOutput of(String outputLocation, String preContent, String postContent) {
        ClerkFileOutput cfo = new ClerkFileOutput();
        cfo.outputLocation = outputLocation;
        cfo.preContent = preContent;
        cfo.postContent = postContent;
        cfo.writeResult(outputLocation);
        return cfo;
    }

    void writeToFile(String fileName, String text) {
        try {
            Files.createDirectories(Path.of(fileName).getParent());
            Files.writeString(Path.of(fileName), text);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
            System.exit(1);
        }
    }

    void writeResult(String fileName) {
        writeToFile(fileName, STR."""
            \{preContent}
            \{content}
            \{postContent}
            """);
    }

    @Override
    public void write(String text) {
        content = STR."""
        \{content}
        \{text}
        """;
        writeResult(outputLocation);
    }

    void write(Object obj) {
        write(STR."<code><pre>\{obj}</pre></code>");
    }

    @Override
    public void markdown(String markdown, String id) {
        write(STR."""
            <div id="\{id}">
            \{markdown}
            </div>
            <script>
                var markdownContent = document.getElementById("\{id}").textContent;
                var renderedHTML = marked.parse(markdownContent);
                document.getElementById("\{id}").innerHTML = renderedHTML;
            </script>
            """);
    }

    @Override
    public void script(String addonIdentifier, String code, Object... args) {
        Optional<ClerkAddon> addon = addons.stream().filter(x -> x.getIdentifier().equals(addonIdentifier)).findFirst();
        if (addon.isEmpty())
            return;

        write(String.format(Locale.ENGLISH, addon.get().translateToBuildStatements(code), args));
    }

    @Override
    public String getTarget() {
        return outputLocation;
    }

    @Override
    public void reset() {
        content = "";
    }
}

class Clerk {
    static String templateFileName = "./web/indexTemplate.html";
    static List<ClerkOutput> outputs = new ArrayList<>();

    static {
        ClerkAddonOutput.addons = Set.of(new Turtle());
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

    static String generateID(int n) {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random rand = new Random();
        return IntStream.rangeClosed(1, n)
                .mapToObj(i -> "" + characters.charAt(rand.nextInt(characters.length())))
                .collect(Collectors.joining());
    }

    static void serve() {
        serve(ClerkServer.defaultPort);
    }

    static void serve(int port) {
        Optional<ClerkOutput> existingOutput = outputs.stream()
                .filter(x -> x.getTarget().equals(Integer.toString(port)))
                .findFirst();
        if (existingOutput.isPresent()) {
            existingOutput.get().reset();
            outputs.remove(existingOutput.get());
        }

        try {
            outputs.add(ClerkServer.of(port));
        } catch (Exception e) {
            System.err.printf("Error starting Server: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    static void addFileOutput() {
        addFileOutput(ClerkFileOutput.defaultLocation);
    }

    static void addFileOutput(String location) {
        String preContent = cutOut(templateFileName, true, false, "<!DOCTYPE html>", "<!-- begin include content -->");
        String postContent = cutOut(templateFileName, "<!-- end include content -->");

        Optional<ClerkOutput> existingOutput = outputs.stream().filter(x -> x.getTarget().equals(location)).findFirst();
        if (existingOutput.isPresent()) {
            outputs.remove(existingOutput.get());
        }
        outputs.add(ClerkFileOutput.of(location, preContent, postContent));
    }

    static void write(String text) {
        outputs.forEach(o -> o.write(text));
    }

    static void script(String addonIdentifier, String code, Object... args) {
        outputs.forEach(o -> o.script(addonIdentifier, code, args));
    }

    static void markdown(String markdown) {
        String ID = generateID(10);
        outputs.forEach(o -> o.markdown(markdown, ID));
    }
}

interface ClerkAddon {
    default String getIdentifier() {
        return this.getClass().getSimpleName();
    }

    String translateToBuildStatements(String statement);

    String translateToServeStatements(String statement);
}

class Turtle implements ClerkAddon {
    private static Map<String, String> toBuild, toServe;
    final String ID;
    final int width, height;

    static {
        populateBuildMap();
        populateServeMap();
    }

    Turtle(int width, int height) {
        this.width = width;
        this.height = height;
        ID = Clerk.generateID(6);

        Clerk.write(STR."""
            <canvas id="turtleCanvas\{ID}" width="\{width}" height="\{height}" style="border:1px solid #000;"></canvas>
        """);
        Clerk.script(this.getClass().getSimpleName(), "init", ID, ID);
    }

    Turtle() {
        this(500, 500);
    }

    private static void populateServeMap() {
        String init = STR."{\"command\": \"init\", \"id\": \"%s\", \"value\": \"%s\"}";
        String penDown = STR."{\"command\": \"penDown\", \"id\": \"%s\", \"value\": \"0\"}";
        String penUp = STR."{\"command\": \"penUp\", \"id\": \"%s\", \"value\": \"0\"}";
        String forward = STR."{\"command\": \"forward\", \"id\": \"%s\", \"value\": \"%f\"}";
        String backward = STR."{\"command\": \"backward\", \"id\": \"%s\", \"value\": \"%f\"}";
        String left = STR."{\"command\": \"left\", \"id\": \"%s\", \"value\": \"%f\"}";
        String right = STR."{\"command\": \"right\", \"id\": \"%s\", \"value\": \"%f\"}";

        toServe = Map.of("init", init,
                "penDown", penDown,
                "penUp", penUp,
                "forward", forward,
                "backward", backward,
                "left", left,
                "right", right);
    }

    private static void populateBuildMap() {
        String init = STR."<script>const turtle%s = new Turtle(document.getElementById('turtleCanvas%s'));</script>";
        String penDown = STR."<script>turtle%s.penDown();</script>";
        String penUp = STR."<script>turtle%s.penUp(); </script>";
        String forward = STR."<script>turtle%s.forward(%f);</script>";
        String backward = STR."<script>turtle%s.backward(%f);</script>";
        String left = STR."<script>turtle%s.left(%f);</script>";
        String right = STR."<script>turtle%s.right(%f);</script>";

        toBuild = Map.of("init", init,
                "penDown", penDown,
                "penUp", penUp,
                "forward", forward,
                "backward", backward,
                "left", left,
                "right", right);
    }

    Turtle penDown() {
        Clerk.script(this.getClass().getSimpleName(), "penDown", ID);
        return this;
    }

    Turtle penUp() {
        Clerk.script(this.getClass().getSimpleName(), "penUp", ID);
        return this;
    }

    Turtle forward(double distance) {
        Clerk.script(this.getClass().getSimpleName(), "forward", ID, distance);
        return this;
    }

    Turtle backward(double distance) {
        Clerk.script(this.getClass().getSimpleName(), "backward", ID, distance);
        return this;
    }

    Turtle left(double degrees) {
        Clerk.script(this.getClass().getSimpleName(), "left", ID, degrees);
        return this;
    }

    Turtle right(double degrees) {
        Clerk.script(this.getClass().getSimpleName(), "right", ID, degrees);
        return this;
    }

    @Override
    public String translateToBuildStatements(String statement) {
        return toBuild.get(statement);
    }

    @Override
    public String translateToServeStatements(String statement) {
        return toServe.get(statement);
    }
}