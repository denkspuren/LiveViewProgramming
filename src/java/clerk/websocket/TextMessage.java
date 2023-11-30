package clerk.websocket;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class TextMessage extends Message {

    public final String message;

    public TextMessage(final String string) {
        super(1);
        this.message = string;
    }

    @Override
    protected byte[] getPayloadData() {
        return this.message.getBytes(StandardCharsets.UTF_8);
    }

}
