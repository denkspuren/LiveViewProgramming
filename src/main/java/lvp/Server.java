package lvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lvp.logging.LogLevel;
import lvp.logging.Logger;


public class Server {
    private record Config(Path path, String fileNamePattern, int port, LogLevel logLevel){}

    private final HttpServer httpServer;
    private final List<String> createdContexts = new CopyOnWriteArrayList<>();
    private WatchService watcher;
    private Thread watchThread;
    private ScheduledExecutorService debounceExecutor;
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();

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


    // ---- Main Entry & Configuration ----
    public static void main(String[] args) {
        Config cfg = parseArgs(args);
        Logger.setLogLevel(cfg.logLevel());
        
        try {
            Server server = new Server(Math.abs(cfg.port()));
            if(cfg.path() != null) {
                server.initWatcher(cfg);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Starten des Servers: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Config parseArgs(String[] args) {
        String fileNamePattern = null;
        Path fileName = null;
        Path path = null;
        int port = defaultPort;
        LogLevel logLevel = LogLevel.Error;

        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            String key = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : "";

            switch (key) {
                case "-l":
                case "--log":
                    logLevel = value.isBlank() ? LogLevel.Info : LogLevel.fromString(value);
                    break;
                case "-p":
                case "--pattern":
                    fileNamePattern = value.isBlank() ? "*" : value;
                    break;
                case "--watch":
                case "-w":
                    path = value.isBlank() ? Paths.get(".") : Paths.get(value).normalize();
                    break;
                default:
                    try { port = Integer.parseInt(arg.trim()); } catch(NumberFormatException _) {}
                    break;
            }
        }

        if (path == null) return new Config(null, null, port, logLevel);

        if (!Files.exists(path)) {
            System.err.println("Error: Path not found " + path);
            System.exit(1);
        }

        if(!Files.isDirectory(path)) {
            if (path.getFileName().toString().endsWith(".java")) fileName = path.getFileName();
            path = path.getParent() != null ?  path.getParent() : Paths.get(".");
        }

        if (fileName == null && fileNamePattern == null) {
            System.err.println("Error: No Java file or pattern specified.");
            System.exit(1);
        }

        return new Config(path, fileNamePattern != null ? fileNamePattern : fileName.toString(), port, logLevel);
    }

    private Server(int port) throws IOException {
        this.port = port;
        webClients = new CopyOnWriteArrayList<>(); // thread-safe variant of ArrayList

        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        System.out.println("Open http://localhost:" + port + " in your browser");

        httpServer.createContext("/loaded", exchange -> handlePostRequest(exchange, this::processLoaded));
        httpServer.createContext("/log", exchange -> handlePostRequest(exchange, this::processLog));
        httpServer.createContext("/receive", exchange -> handlePostRequest(exchange, this::processReceive));
        httpServer.createContext("/new", exchange -> handlePostRequest(exchange, this::processNew));
        httpServer.createContext("/events", this::handleEvents);
        httpServer.createContext("/", this::handleRoot);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "ShutdownHook"));

        httpServer.setExecutor(Executors.newFixedThreadPool(5));
        httpServer.start();
    }

    private void handleEvents(HttpExchange exchange) throws IOException {
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
        if (type.equalsIgnoreCase("java")) {
            javaClient = exchange;
        } else {
            webClients.add(exchange);
            sendLoads(exchange);
        }
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
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            Logger.logError("Method not allowed in '" + exchange.getRequestURI().getPath() + "'");
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }
        String message = readRequestBody(exchange);
        if (message == null) return;

        exchange.sendResponseHeaders(200, 0);
        exchange.close();
        processor.accept(message);
    }

    private void processLoaded(String message) {
        lock.lock();
        try {
            loadEventOccured = true;
            loadEventOccurredCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void processLog(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        Logger.log(LogLevel.fromString(parts[0]), parts[1]);
    }

    private void processReceive(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        SSEType event = SSEType.valueOf(parts[0]);
        Logger.logDebug("Received '" + event + "' Event!");
        if (event.equals(SSEType.LOAD)) {
            if (!paths.contains(parts[1])) load(parts[1]);
        } else {
            sendServerEvent(event, parts[1]);
        }
    }

    private void processNew(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        createResponseContext(parts[0], parts[1]);
    }
    
    // ---- File Watching ----
    public void initWatcher(Config cfg) throws IOException{
        watcher = FileSystems.getDefault().newWatchService();
        cfg.path().register(watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        Logger.logInfo("Watching in " + cfg.path().normalize().toAbsolutePath() + "");

        watchThread = new Thread(() -> {
            try {
                watchLoop(cfg);
            } catch (Exception e) {
                Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
            }
        }, "Wachter");
        watchThread.setDaemon(true);
        watchThread.start();
        Logger.logInfo("Watcher started");
    }

    private void watchLoop(Config cfg) {
        debounceExecutor = Executors.newSingleThreadScheduledExecutor();
        long debounceDelay = 200;            
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
                break; // sauber beenden
            }
            for (WatchEvent<?> ev : key.pollEvents()) {
                Path changed = (Path) ev.context();
                if (FileSystems.getDefault().getPathMatcher("glob:" + cfg.fileNamePattern()).matches(changed)) {
                    Logger.logInfo("Event für Datei: " + changed.toAbsolutePath() + " (" + ev.kind().name() + ")");
                    
                    if (javaClient != null) javaClient.close();
                    ScheduledFuture<?> prev = pendingTask.getAndSet(
                        debounceExecutor.schedule(() -> runJava(cfg, changed), debounceDelay, TimeUnit.MILLISECONDS)
                    );
                    if (prev != null && !prev.isDone()) {
                        prev.cancel(false);
                        Logger.logDebug("Previous task cancelled");
                    }
                }
            }
            
            if (!key.reset()) {
                Logger.logInfo("Watch key could not be reset. Exiting watch loop.");
                break;
            }
        }
    }

    private void runJava(Config cfg, Path path) {
        try {
            Path jarLocation = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            
            Logger.logInfo("Executing java --enable-preview --class-path " + jarLocation + " " + path.toString() + " in " + cfg.path().normalize().toAbsolutePath());
            ProcessBuilder pb = new ProcessBuilder("java", "--enable-preview", "--class-path", jarLocation.toString(), path.toString())
                .directory(cfg.path().toFile())
                .redirectErrorStream(true);
            Process process = pb.start();

            try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Logger.logInfo("(JavaClient) " + line);
                    }
                }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Logger.logError("Timeout: process killed");
            }

        } catch (Exception e) {
            Logger.logError("Error in Java Process", e);
        }
    }

    // ---- SSE Interaction ----
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
                synchronized (connection) {
                    OutputStream os = connection.getResponseBody();
                    os.write(message.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
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
           sendResponse(id, path, data);
        });
    }

    // ---- HTTP Request Handling ----
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

    // ---- Connection Management ----
    private void closeConnections(List<HttpExchange> connections) {
        for (HttpExchange connection : connections) {
            connection.close();
        }
    }

    // ---- Utilities ----
    private String encodeData(String data) {
        byte[] binaryData = data.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(binaryData);
    }

    // ---- Lifecycle Management ----

    public void stop() {
        Logger.logInfo("Closing Server on port '" + port + "'");
        closeConnections(webClients);
        if (javaClient != null) javaClient.close();
        httpServer.stop(0);
        if (watcher != null) try { watcher.close(); } catch (IOException e) { e.printStackTrace(); }
        if (debounceExecutor != null) debounceExecutor.shutdownNow();
        if (watchThread != null) watchThread.interrupt();
        System.out.println("Server stopped.");
    }
}
