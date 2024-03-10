package clerk;

import clerk.websocket.TextMessage;
import clerk.websocket.WebSocketServer;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Turtle {

    public static final WebSocketServer websocket;

    static {
        try {
            final Server server = new Server(8082);
            server.start();
            websocket = new WebSocketServer(8083);
            websocket.start();
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static final Pattern createResponsePattern = Pattern.compile("\\s*\\{\\s*\"id\"\\s*:\\s*([0-9]+)\\s*\\}\\s*");
    private static final Pattern awaitResponsePattern = Pattern.compile("\\s*\\{\\s*\"id\"\\s*:\\s*([0-9]+)\\s*\\}\\s*");

    public final int id;

    private int x = 0;

    private int y = 0;

    private Turtle(final int id) {
        this.id = id;
    }

    public Turtle move(final int x, final int y) {
        this.x += x;
        this.y += y;
        update(true);
        return this;
    }

    public Turtle set(final int x, final int y) {
        this.x = x;
        this.y = y;
        update(false);
        return this;
    }

    public boolean await() {
        final String message = String.format("{ \"command\": \"await\", \"id\": %d }", this.id);
        return websocket.getFirstAvailableClient().map(client -> {
            if (client.send(new TextMessage(message))) {
                if (client.receive() instanceof TextMessage response) {
                    final Matcher matcher = awaitResponsePattern.matcher(response.message);
                    if (!matcher.find()) {
                        return false;
                    }

                    final int id = Integer.parseInt(matcher.group(1));
                    return id == this.id;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }).orElse(false);
    }

    private boolean update(final boolean move) {
        final String message;

        if (move) {
            message = String.format(
                "{ \"command\": \"update\", \"id\": %d, \"data\": { \"destination\": { \"x\": %d, \"y\": %d } } }",
                this.id,
                this.x,
                this.y
            );
        } else {
            // In case we want to set the turtle, we have to overwrite its destination and its position
            message = String.format(
                "{ \"command\": \"update\", \"id\": %d, \"data\": { \"destination\": { \"x\": %d, \"y\": %d }, \"position\": { \"x\": %d, \"y\": %d } } }",
                this.id,
                this.x,
                this.y,
                this.x,
                this.y
            );
        }

        return websocket.getFirstAvailableClient().map(client -> client.send(new TextMessage(message))).orElse(false);
    }

    @Override
    public String toString() {
        return String.format("Turtle(id := %d)", id);
    }

    @Override
    public final boolean equals(final Object object) {
        if (object instanceof Turtle turtle) {
            return turtle.id == id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static Turtle create() {
        return websocket.getFirstAvailableClient().flatMap(client -> {
            client.send(new TextMessage("{ \"command\": \"create\" }"));
            if (client.receive() instanceof TextMessage message) {
                final Matcher matcher = createResponsePattern.matcher(message.message);
                if (!matcher.matches()) {
                    return Optional.empty();
                }
                final int id = Integer.parseInt(matcher.group(1));
                return Optional.of(new Turtle(id));
            } else {
                return Optional.empty();
            }
        }).orElse(null);
    }

}
