package lvp.server;

import com.sun.net.httpserver.HttpExchange;

import lvp.SSEType;
import lvp.logging.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SSEManager {
    public final List<String> paths = new CopyOnWriteArrayList<>();
    private final List<HttpExchange> webClients = new CopyOnWriteArrayList<>();
    private volatile HttpExchange javaClient;

    // lock required to temporarily block processing of `SSEType.LOAD`
    Lock lock = new ReentrantLock();
    Condition loadEventOccurredCondition = lock.newCondition();
    boolean loadEventOccurred = false;

    private final int port;

    public SSEManager(int port) {
        this.port = port;
    }

    public void handleEvents(HttpExchange exchange) throws IOException {
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

        String type = Server.parseQueryParams(exchange.getRequestURI().getRawQuery()).getOrDefault("type", "web");
        if (type.equalsIgnoreCase("java")) {
            javaClient = exchange;
        } else {
            webClients.add(exchange);
            sendLoads(exchange);
        }
    }

    public void processReceive(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        SSEType event = SSEType.valueOf(parts[0]);
        Logger.logDebug("Received '" + event + "' Event!");
        if (event.equals(SSEType.LOAD)) {
            if (!paths.contains(parts[1])) load(parts[1]);
        } else {
            sendToWeb(event, parts[1]);
        }
    }

    public void processLoaded(String message) {
        lock.lock();
        try {
            loadEventOccurred = true;
            loadEventOccurredCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void sendToWeb(SSEType sseType, String data) {
        if (webClients.isEmpty()) {
            System.out.println("Open http://localhost:" + port + " in your browser");
            return;
        }

        Logger.logDebug("New '" + sseType + "' Event");
        Logger.logDebug("Data: " + data);

        String message = "data: " + sseType + ":" + Server.encodeData(data) + "\n\n";
        Logger.logDebug("Event Message: " + message.trim());

        webClients.removeIf(connection -> {
            try {
                OutputStream os = connection.getResponseBody();
                os.write(message.getBytes(StandardCharsets.UTF_8));
                os.flush();
                return false;
            } catch (IOException _) {
                Logger.logError("Web exchange '" + connection.getRemoteAddress() + "' did not respond. Closing...");
                connection.close();
                return true;
            }
        });
    }

    public void sendToJava(String id, String path, String data) {
        if (javaClient == null) {
            Logger.logError("No Java Client connected. Cannot send data.");
            return;
        }
        Logger.logInfo("Sending Response from '" + path + "'");
        Logger.logDebug("Data: " + data);

        String message = "data: " + id + ":" + Server.encodeData(data) + "\n\n";
        Logger.logDebug("Event Message: " + message.trim());

        try {
            OutputStream os = javaClient.getResponseBody();
            os.write(message.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException _) {
            Logger.logError("Java exchange '" + javaClient.getRemoteAddress() + "' did not respond. Closing...");
            javaClient.close();
            javaClient = null;
        }
    }

    public void sendLoads(HttpExchange connection) {
        if (paths.isEmpty()) return;

        Logger.logInfo("Sending " + paths.size() + " paths to '" + connection.getRemoteAddress() + "'...");
        lock.lock();
        for (String path : paths) {
            loadEventOccurred = false;
            String message = "data: " + SSEType.LOAD + ":" + Server.encodeData(path) + "\n\n";
            Logger.logDebug("Event Message: " + message.trim());

            try {
                OutputStream os = connection.getResponseBody();
                os.write(message.getBytes(StandardCharsets.UTF_8));
                os.flush();

                loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
                if (!loadEventOccurred) System.err.println("LOAD-Timeout: " + path);
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

    public void load(String data) {
        if (paths.contains(data)) return;
        lock.lock();
        loadEventOccurred = false;

        try {
            sendToWeb(SSEType.LOAD, data);

            loadEventOccurredCondition.await(1_000, TimeUnit.MILLISECONDS);
            if (loadEventOccurred) paths.add(data);
            else System.err.println("LOAD-Timeout: " + data);

        } catch (InterruptedException e) {
            Logger.logError("LOAD-Interruption: " + data, e);
        } finally {
            lock.unlock();
        }
    }

    public void closeJavaClient() {
        if (javaClient != null) {
            javaClient.close();
            javaClient = null;
        }
    }
    
    public void closeConnections() {
        for (HttpExchange connection : webClients) {
            connection.close();
        }
    }
}