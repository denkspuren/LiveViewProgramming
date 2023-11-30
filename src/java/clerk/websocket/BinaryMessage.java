package clerk.websocket;

import java.util.Arrays;

public final class BinaryMessage extends Message {

    public final byte[] bytes;

    public BinaryMessage(final byte[] bytes) {
        super(Type.Binary);
        this.bytes = bytes;
    }

    @Override
    protected byte[] getPayloadData() {
        return bytes;
    }

    @Override
    public String toString() {
        return String.format("BinaryMessage(bytes := %s)", Arrays.toString(bytes));
    }

}
