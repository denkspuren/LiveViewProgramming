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

    static Server serve(int port) { return Server.onPort(port); }
    static Server serve() { return serve(Server.getDefaultPort()); }

    static void write(Server server, String html)        { server.sendServerEvent(SSEType.WRITE, html); }
    static void call(Server server, String javascript)   { server.sendServerEvent(SSEType.CALL, javascript); }
    static void script(Server server, String javascript) { server.sendServerEvent(SSEType.SCRIPT, javascript); }
    static void load(Server server, String path) {
        if (!server.paths.contains(path.trim())) server.sendServerEvent(SSEType.LOAD, path);
    }
    static void load(Server server, String onlinePath, String offlinePath) {
        load(server, offlinePath + ", " + onlinePath);
    }
    static void clear(Server server) { server.sendServerEvent(SSEType.CLEAR, ""); }
    static void clear() { clear(serve()); };

    static void markdown(String text) { new MarkdownIt(serve()).write(text); }
}