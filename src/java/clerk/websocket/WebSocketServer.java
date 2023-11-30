package clerk.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebSocketServer {

    private final ServerSocket socket;
    private final List<WebSocketClient> clients = new ArrayList<>();

    private static boolean isLoggingEnabled = false;

    public WebSocketServer(final int port) throws IOException {
        this.socket = new ServerSocket(port);
    }

    public List<WebSocketClient> getClients() {
        synchronized (clients) {
            return Collections.unmodifiableList(clients);
        }
    }

    public Optional<WebSocketClient> getFirstAvailableClient() {
        synchronized (clients) {
            return clients.stream().filter(Predicate.not(WebSocketClient::isClosed)).findFirst();
        }
    }

    public void start() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    info("Waiting for a new WebSocket connection ...");
                    final WebSocketClient client = new WebSocketClient(negotiateWebsocketConnection());
                    info("Connected to %s!", client.getAddress());

                    synchronized (clients) {
                        // Clean up of closed connections
                        for (int i = 0; i < clients.size(); ++i) {
                            if (clients.get(i).isClosed()) {
                                clients.remove(i);
                                i -= 1;
                            }
                        }

                        // Enqueue the new client
                        clients.add(client);
                    }
                } catch (final IOException exception) {
                    error(exception.getLocalizedMessage());
                }
            }
        }).start();
    }

    public static void enableLogging() {
        isLoggingEnabled = true;
    }

    public static void disableLogging() {
        isLoggingEnabled = false;
    }

    public static void info(final String message, final Object ... objects) {
        log(System.out, "info", message, objects);
    }

    public static void error(final String message, final Object ... objects) {
        log(System.err, "error", message, objects);
    }

    private static void log(final PrintStream stream, final String type, final String message, final Object ... objects) {
        if (isLoggingEnabled) {
            final LocalTime time = LocalTime.now();
            stream.printf("[%s - %02d:%02d:%02d] ", type, time.getHour(), time.getMinute(), time.getSecond());
            stream.println(String.format(message, objects));
        }
    }

    private Socket negotiateWebsocketConnection() throws IOException {
        final Socket socket = this.socket.accept();
        try {
            final InputStream input = socket.getInputStream();
            final OutputStream output = socket.getOutputStream();
            final Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);

            try {
                final String data = scanner.useDelimiter("\r\n\r\n").next();
                final Matcher get = Pattern.compile("^GET").matcher(data);
                if (get.find()) {
                    final Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);

                    if (!match.find()) {
                        socket.close();
                        return socket;
                    }

                    final String responseKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)));
                    final String response = "HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: " + responseKey + "\r\n\r\n";
                    final byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    output.write(bytes, 0, bytes.length);
                    output.flush();
                }
            } catch (final NoSuchAlgorithmException exception) {
                socket.close();
                throw new RuntimeException(exception);
            }
            return socket;
        } catch (final IOException exception) {
            socket.close();
            return socket;
        }
    }

}
