package lvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Client {
    private static volatile Client instance;
    final int port;
    final String address;
    static int defaultPort = 50_001;
    static String baseAddress = "http://localhost";
    
    private Thread worker;
    private final HttpClient client;

    Map<String, Consumer<String>> callbacks = new ConcurrentHashMap<>();

    public static Client of(int port) {
        return instance == null ? (instance = new Client(port)) : instance;
    }

    private Client(int port) {
        this.port = port;
        address = baseAddress + ":" + port;

        client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        worker = new Thread(this::startSseWorker, "SSE-Worker");
        worker.start();
    }

    // Trigger Server-Sent Events (SSE) by sending data to the server
    public void send(SSEType event, String data) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(address + "/receive"))
            .POST(BodyPublishers.ofString(event + ":" + data))
            .timeout(Duration.ofSeconds(10))
            .build();
        sendRequest(request);            
    }

    // Create a new server route and register a callback to handle requests to it
    public void createCallback(String path, String id, Consumer<String> delegate) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(address + "/new"))
            .POST(BodyPublishers.ofString(path + ":" + id))
            .timeout(Duration.ofSeconds(10))
            .build();
        
        if (sendRequest(request)) callbacks.put(id, delegate);
    }

    // Stop the SSE thread
    public void stop() {
        System.out.println("[SSE] Stopping client...");
        if (worker != null) {
            worker.interrupt();
        }
        System.out.println("[SSE] Client stopped.");
    }

    private boolean sendRequest(HttpRequest request) {
        try {
            HttpResponse<Void> response = client.send(request, BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[HTTP] Request failed: " + e.getMessage());
            return false;
        }
    }

    private void startSseWorker() {
        int attempts = 0;
        while (attempts < 2) {
            try {
                connectToSse();
            } catch (Exception e) {
                System.err.println("[SSE] (" + attempts +") Unexpected Exception: " + e.getMessage());
            }
            attempts++;
        }
        stop();
    }

    private void connectToSse() throws InterruptedException, IOException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(address + "/events?type=java"))
            .header("Accept", "text/event-stream")
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();

        HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            System.err.println("[SSE] Failed to connect: HTTP " + response.statusCode());
            return;
        }

        System.out.println("[SSE] Connected");
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) continue;

                String[] parts = line.split(":", 3);
                if (parts.length != 3) continue;

                String id = parts[1].trim();
                String data = new String(Base64.getDecoder().decode(parts[2]));

                callbacks.getOrDefault(id, _ -> {}).accept(data);
                send(SSEType.RELEASE, id);
            }       
        }
    }
}