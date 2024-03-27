package clerk.websocket;

import java.nio.charset.StandardCharsets;

public final class PongMessage extends Message {

    public final String message;
    private final byte[] payload;

    public PongMessage(final String message) {
        super(Type.Pong);
        this.message = message;
        this.payload = message.getBytes(StandardCharsets.UTF_8);
    }

    public PongMessage() {
        this("");
    }

    public PingMessage toPingMessage() {
        return new PingMessage(message);
    }

    @Override
    protected byte[] getPayloadData() {
        return this.payload;
    }

    @Override
    public String toString() {
        return String.format("PongMessage(message := %s)", message);
    }

}
