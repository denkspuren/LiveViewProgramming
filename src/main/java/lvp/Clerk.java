package lvp;

import java.util.Random;
import java.util.stream.Collectors;

import lvp.views.MarkdownIt;

public class Clerk {
    public static Client client;
    public static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    public static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }

    public static Client connect(int port) { return client = Client.of(port); }
    public static Client connect() { return connect(Client.defaultPort); }

    public static void write(Client client, String html)        { client.emit(SSEType.WRITE, html); }
    public static void call(Client client, String javascript)   { client.emit(SSEType.CALL, javascript); }
    public static void script(Client client, String javascript) { client.emit(SSEType.SCRIPT, javascript); }
    public static void load(Client client, String path) { client.emit(SSEType.LOAD, path); }
    public static void load(Client client, String onlinePath, String offlinePath) {
        load(client, onlinePath + ", " + offlinePath);
    }
    public static void clear(Client client) { client.emit(SSEType.CLEAR, ""); }

    public static void markdown(String text) { new MarkdownIt(client).write(text); }
}