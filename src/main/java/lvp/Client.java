package lvp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class Client {
    final int port;
    static int defaultPort = 50_001;
    static String baseAddress = "http://localhost";

    HttpClient client = HttpClient.newHttpClient();

    public static Client of(int port) {
        return new Client(port);
    }

    private Client(int port) {
        this.port = port;
    }

    public void emit(SSEType event, String data) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseAddress + ":" + port + "/emit"))
            .POST(BodyPublishers.ofString(event.toString() + ":" + data))
            .build();
        
            try {
                client.send(request, BodyHandlers.discarding());
            } catch (Exception e) {
                System.err.println("Request not sent!");
            }
    }
}