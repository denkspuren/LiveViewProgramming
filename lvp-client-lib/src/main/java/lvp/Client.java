package lvp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class Client {
    String address = "http://localhost:50001/emit";
    HttpClient client = HttpClient.newHttpClient();

    public static Client of() {
        return new Client();
    }

    private Client() {}

    public void emit(SSEType event, String data) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(address))
            .POST(BodyPublishers.ofString(event.toString() + ":" + data))
            .build();
        
            try {
                var response = client.send(request, BodyHandlers.discarding());
                System.out.println(response.statusCode());
            } catch (Exception e) {
                System.err.println("Request not sent!");
            }
    }
}
