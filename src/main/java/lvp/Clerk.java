package lvp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.stream.Collectors;

import lvp.views.MarkdownIt;

public interface Clerk {
    public static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    public static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }


    public static void write(String html)        { out(SSEType.WRITE, html); }
    public static void call(String javascript)   { out(SSEType.CALL, javascript); }
    public static void script(String javascript) { out(SSEType.SCRIPT, javascript); }
    public static void clear() { out(SSEType.CLEAR, ""); }

    public static void markdown(String text) { new MarkdownIt().write(text); }

    public static void out(SSEType event, String data) { System.out.println(event + ":" + Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8))); }
}