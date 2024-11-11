package de.denkspuren.lvp;

import java.util.Random;
import java.util.stream.Collectors;
import de.denkspuren.lvp.views.markdown.MarkdownIt;

public interface Clerk {
    static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }

    static Server view(int port) { return Server.onPort(port); }
    static Server view() { return view(Server.getDefaultPort()); }

    static void write(Server view, String html)        { view.sendServerEvent(SSEType.WRITE, html); }
    static void call(Server view, String javascript)   { view.sendServerEvent(SSEType.CALL, javascript); }
    static void script(Server view, String javascript) { view.sendServerEvent(SSEType.SCRIPT, javascript); }
    static void load(Server view, String path) {
        if (!view.paths.contains(path.trim())) view.sendServerEvent(SSEType.LOAD, path);
    }
    static void load(Server view, String onlinePath, String offlinePath) {
        load(view, offlinePath + ", " + onlinePath);
    }
    static void clear(Server view) { view.sendServerEvent(SSEType.CLEAR, ""); }
    static void clear() { clear(view()); };

    static void markdown(String text) { new MarkdownIt(view()).write(text); }
}