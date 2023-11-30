package clerk.websocket;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class TextMessage extends Message {

    public final String message;
    private final byte[] payload;

    public TextMessage(final String string) {
        super(Type.Text);
        this.message = string;
        this.payload = this.message.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] getPayloadData() {
        return this.payload;
    }

    @Override
    public String toString() {
        return String.format("TextMessage(message := %s)", message);
    }

}
