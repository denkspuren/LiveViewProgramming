package clerk.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebsocketServer {

    private final ServerSocket socket;
    private Socket connection;

    public WebsocketServer(final int port) throws IOException {
        this.socket = new ServerSocket(port);
    }

    public void start() {
        final Thread thread = new Thread(() -> {
            while (true) {
                try {
                    negotiateWebsocketConnection();
                } catch (final IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void negotiateWebsocketConnection() throws IOException {
        connection = this.socket.accept();
        final InputStream input = connection.getInputStream();
        final OutputStream output = connection.getOutputStream();
        final Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);

        try {
            final String data = scanner.useDelimiter("\r\n\r\n").next();
            final Matcher get = Pattern.compile("^GET").matcher(data);
            if (get.find()) {
                final Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);

                if (!match.find()) {
                    connection.close();
                    connection = null;
                    return;
                }

                final String responseKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)));
                final String response = "HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: " + responseKey + "\r\n\r\n";
                final byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                output.write(bytes, 0, bytes.length);
                output.flush();
            }
        } catch (final NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }

    public InputStream getInputStream() {
        if (connection != null) {
            try {
                return connection.getInputStream();
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            throw new RuntimeException("[error] Please open the live view in your browser.");
        }
    }

    public OutputStream getOutputStream() {
        if (connection != null) {
            try {
                return connection.getOutputStream();
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            throw new RuntimeException("[error] Please open the live view in your browser.");
        }
    }

}
