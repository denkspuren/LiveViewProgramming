package clerk.websocket;

import java.nio.charset.StandardCharsets;

public final class PingMessage extends Message {

    public final String message;
    private final byte[] payload;

    public PingMessage(final String message) {
        super(Type.Ping);
        this.message = message;
        this.payload = message.getBytes(StandardCharsets.UTF_8);
    }

    public PingMessage() {
        this("");
    }

    public PongMessage toPongMessage() {
        return new PongMessage(message);
    }

    @Override
    protected byte[] getPayloadData() {
        return this.payload;
    }

    @Override
    public String toString() {
        return String.format("PingMessage(message := %s)", message);
    }

}
