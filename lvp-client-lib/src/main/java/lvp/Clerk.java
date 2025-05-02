package lvp;

import java.util.Random;

import java.util.stream.Collectors;

import lvp.views.MarkdownIt;



public interface Clerk {
    static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }

    
    static Client serve() { return Client.of(); }

    static void write(Client client, String html)        { client.emit(SSEType.WRITE, html); }
    static void call(Client client, String javascript)   { client.emit(SSEType.CALL, javascript); }
    static void script(Client client, String javascript) { client.emit(SSEType.SCRIPT, javascript); }
    static void load(Client client, String path) { client.emit(SSEType.LOAD, path); }
    static void load(Client client, String onlinePath, String offlinePath) {
        load(client, onlinePath + ", " + offlinePath);
    }
    static void clear(Client client) { client.emit(SSEType.CLEAR, ""); }
    static void clear() { clear(serve()); };

    static void markdown(String text) { new MarkdownIt(serve()).write(text); }
}