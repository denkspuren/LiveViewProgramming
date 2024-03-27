package clerk.websocket;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class CloseMessage extends Message {

    /** The status of this close message or Short.MIN_VALUE if no payload data was provided. */
    public final short status;

    /** The reason for this close message or `null` if no payload data was provided. */
    public final String reason;

    private final byte[] payload;

    public CloseMessage(final short status, final String reason) {
        super(Type.Close);
        this.status = status;
        this.reason = reason;

        if (this.reason != null) {
            final byte[] bytes = this.reason.getBytes(StandardCharsets.UTF_8);
            if (bytes.length + 2 >= 125) {
                throw new IllegalArgumentException("payload length exceeds the allowed limit of 125 bytes for control frames");
            }

            payload = new byte[bytes.length + 2];
            payload[0] = (byte) ((status & 0xFF00) >> 8);
            payload[1] = (byte) (status & 0x00FF);
            System.arraycopy(bytes, 0, payload, 2, bytes.length);
        } else {
            payload = new byte[0];
        }
    }

    public CloseMessage() {
        this(Short.MIN_VALUE, null);
    }

    @Override
    protected byte[] getPayloadData() {
        return this.payload;
    }

    @Override
    public String toString() {
        return String.format("CloseMessage(status := %d, reason := %s)", status, reason);
    }

}
