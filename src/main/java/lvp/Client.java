package lvp;

import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Client {
    public static Client instance;
    final int port;
    final String address;
    static int defaultPort = 50_001;
    static String baseAddress = "http://localhost";

    private volatile boolean running = true;
    private Thread worker;
    private final HttpClient client;

    Map<String, Consumer<String>> callbacks = new HashMap<>();

    public static Client of(int port) {
        return instance == null ? (instance = new Client(port)) : instance;
    }

    private Client(int port) {
        this.port = port;
        address = baseAddress + ":" + port;
        client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SSE] Stopping client...");
            running = false;
            if (worker != null) {
                worker.interrupt();
            }
        }));

        worker = new Thread(this::sseLoop, "SSE-Client-Thread");
        worker.setDaemon(false);
        worker.start();
    }

    public void send(SSEType event, String data) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseAddress + ":" + port + "/receive"))
            .POST(BodyPublishers.ofString(event.toString() + ":" + data))
            .build();
        
            try {
                client.send(request, BodyHandlers.discarding());
            } catch (Exception e) {
                System.err.println("Request not sent!");
            }
    }

    public void createCallback(String path, String id, Consumer<String> delegate) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(address + "/new"))
            .POST(BodyPublishers.ofString(path + ":" + id))
            .build();
        
            try {
                client.send(request, BodyHandlers.discarding());
                callbacks.put(id, delegate); //TODO: if 200
            } catch (Exception e) {
                System.err.println("Request not sent!");
            }
    }

    private void sseLoop() {
        URI uri = URI.create(address + "/events?type=java");
        try {
            System.out.println("[SSE] Connecting to " + uri);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "text/event-stream")
                .GET()
                .build();

            HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                System.err.println("[SSE] Failed to connect: HTTP " + response.statusCode());
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    System.out.println("[SSE] " + line);
                    String[] parts = line.split(":", 3);
                    if (parts.length != 3) continue;
                    Consumer<String> callback = callbacks.get(parts[1].trim());
                    if (callback == null) continue;                    
                    callback.accept(new String(Base64.getDecoder().decode(parts[2])));
                    send(SSEType.RELEASE, parts[1].trim());
                }
                
            } catch (Exception e) {
                System.err.println("[SSE] Connection lost: " + e.getMessage());
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("[SSE] Unexpected error: " + e.getMessage());
            }
        }
    }

    public void onEvent(String data) {
        System.out.println("[SSE] Received: " + data);
    }


}