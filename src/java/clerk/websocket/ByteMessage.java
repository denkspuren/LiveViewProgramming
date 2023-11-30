package clerk.websocket;

import java.io.IOException;
import java.io.OutputStream;

public final class ByteMessage extends Message {

    public final byte[] bytes;

    public ByteMessage(final byte[] bytes) {
        super(2);
        this.bytes = bytes;
    }

    @Override
    protected byte[] getPayloadData() {
        return bytes;
    }

}
