package clerk;

import clerk.websocket.Message;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public final class Server {

    private static final Map<String, String> mimeTypes = new HashMap<>();

    static {
        mimeTypes.put("html", "text/html");
        mimeTypes.put("js", "text/javascript");
        mimeTypes.put("css", "text/css");
    }

    private final HttpServer server;
    private final String index = "/html/index.html";
    private final String notFound = "/html/404.html";

    public Server(final int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        this.server.createContext("/", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("get")) {
                final String path = exchange.getRequestURI().getPath().equals("/") ? index : exchange.getRequestURI().getPath();
                try (final InputStream stream = Server.class.getResourceAsStream(path)) {
                    if (stream == null) {
                        try (final InputStream notFoundStream = Server.class.getResourceAsStream(notFound)) {
                            if (notFoundStream == null) {
                                throw new RuntimeException("internal error (failed to load 404 page)");
                            }
                            final byte[] bytes = notFoundStream.readAllBytes();
                            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                            exchange.getResponseHeaders().add("Accept-Encoding", "UTF-8");
                            exchange.sendResponseHeaders(404, bytes.length);
                            exchange.getResponseBody().write(bytes);
                            exchange.getResponseBody().flush();
                            exchange.close();
                        }
                    } else {
                        final byte[] bytes = stream.readAllBytes();
                        exchange.getResponseHeaders().add("Content-Type", guessMimeType(path) + "; charset=utf-8");
                        exchange.getResponseHeaders().add("Accept-Encoding", "UTF-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        exchange.getResponseBody().write(bytes);
                        exchange.getResponseBody().flush();
                        exchange.close();
                    }
                }
            }
        });

    }

    private String guessMimeType(final String path) {
        final int index = path.lastIndexOf('.');
        if (index >= 0) {
            final String ending = path.substring(index + 1);
            return mimeTypes.getOrDefault(ending, "text/plain");
        } else {
            return "text/plain";
        }
    }

    public String url() {
        final InetSocketAddress address = server.getAddress();
        return "http://" + address.getAddress().getCanonicalHostName() + ":" + address.getPort();
    }


    public void start() {
        server.start();
        System.out.println("[info] The jClerk server is ready. Please visit " + url() + " in your favorite browser.");
    }

}