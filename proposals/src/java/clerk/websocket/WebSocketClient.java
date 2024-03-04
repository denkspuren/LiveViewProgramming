package clerk.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

public final class WebSocketClient {

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;
    private final Queue<Message> buffer = new ArrayDeque<>();
    private final Thread reader = new Thread(new Runnable() {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final Message message;
                    synchronized (input) {
                        message = Message.read(input);
                    }

                    switch (message) {
                        case null:
                            close(1003, "malformed data");
                            break;
                        case CloseMessage close:
                            close(close.status, "close request");
                            break;
                        case PingMessage ping:
                            send(ping.toPongMessage());
                            break;
                        case PongMessage ignored:
                            // We do not send pings, so we won't receive pongs.
                            break;
                        default:
                            synchronized (buffer) {
                                buffer.add(message);
                                buffer.notify();
                            }
                            break;
                    }
                } catch (final IOException exception) {
                    close(1011, exception.getLocalizedMessage());
                }
            }
        }
    });

    WebSocketClient(final Socket socket) throws IOException {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        reader.start();
    }

    public boolean send(final Message message) {
        try {
            WebSocketServer.info("Sending message %s ...", message);

            synchronized (this.output) {
                message.write(this.output);
            }

            return true;
        } catch (final IOException exception) {
            close(1011, exception.getLocalizedMessage());
            return false;
        }
    }

    public Message receive() {
        try {
            synchronized (buffer) {
                WebSocketServer.info("Waiting for any messages ...");
                while (buffer.isEmpty() && !isClosed()) {
                    buffer.wait();
                }

                if (isClosed()) {
                    return null;
                } else {
                    final Message message = buffer.remove();
                    WebSocketServer.info("Received message %s!", message);
                    return message;
                }
            }
        } catch (final InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public InetAddress getAddress() {
        return socket.getInetAddress();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close(final int status, final String reason) {
        WebSocketServer.info("Closing connection (status %d)!", status);
        try {
            reader.interrupt();
            synchronized (this.output) {
                new CloseMessage((short) status, reason).write(this.output);
            }
            socket.close();
        } catch (final IOException exception) {
            WebSocketServer.error(exception.getLocalizedMessage());
        } finally {
            synchronized (buffer) {
                buffer.clear();
                buffer.notify();
            }
        }
    }

}
