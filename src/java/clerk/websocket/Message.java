package clerk.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public abstract class Message {

    /** According to the RFC 6455 standard, a server MUST NOT mask any frames sent to the client. */
    private static final boolean serverSideMasking = false;

    private static final SecureRandom generator = new SecureRandom();

    public final Type type;

    public Message(final Type type) {
        this.type = type;
    }

    public final void write(final OutputStream stream) throws IOException {
        final byte[] payload = getPayloadData();

        // We try to write anything with a single fragment ...
        stream.write(0b10000000 | (this.type.id & 0b1111));

        // Write the length and mask-bit
        final byte mask = serverSideMasking ? 0b10000000 : 0;
        if (payload.length > 126) {
            if ((payload.length & 0xFFFFFFFFFFFF0000L) != 0) {
                // We use the full 8 bytes to encode the length
                stream.write(mask | (byte) 127);
                for (int i = 0; i < 8; ++i) {
                    stream.write((byte) (payload.length >> (56 - i * 8)));
                }
            } else {
                // We just use 2 bytes here to encode the length
                stream.write(mask | (byte) 126);
                stream.write((byte) ((payload.length & 0xFF00) >> 8));
                stream.write((byte) (payload.length & 0xFF));
            }
        } else {
            stream.write(mask | (byte) (payload.length & 0b01111111));
        }

        if (serverSideMasking) {
            // Write the masking key if necessary
            final int maskingKey = generator.nextInt();
            for (int i = 0; i < 4; ++i) {
                stream.write((byte) (maskingKey >> (24 - i * 8)));
            }

            // Encode the payload
            for (int i = 0; i < payload.length; ++i) {
                payload[i] = (byte) (payload[i] ^ (maskingKey >> (24 - (i & 0x3) * 8)));
            }
        }

        stream.write(payload);
    }

    protected abstract byte[] getPayloadData();

    /**
     * Reads a message from the websocket connection. For implementation details refer to the RFC 6455 standard.
     * @param stream The input stream of the websocket connection.
     * @return The read message or `null` if the data was corrupted or incomplete.
     * @throws IOException When the underlying stream throws an IOException.
     */
    public static Message read(final InputStream stream) throws IOException {
        int next;

        // Here we check if:
        // - The first bit equals 0 (i.e., we do not support multiple fragments)
        // - The RSV1, RSV2 and RSV3 equal 0 (we have no extensions)
        if ((next = stream.read()) == -1) return null;
        if ((next & 0b11110000) != 0b10000000) {
            return null;
        }

        // Now we get the interpretation mode for the payload
        final Type interpretation = Type.getBy(next & 0b00001111);

        // Get the masking bit (indicates whether a mask was used to encode the data)
        if ((next = stream.read()) == -1) return null;
        final boolean mask = (next & 0b10000000) != 0;

        if (!mask) {
            // According to the RFC 6455 standard a server MUST close the connection when the client sends unmasked data
            return null;
        }

        // Compute the length of the payload data
        final long length;
        if ((next & ~0b10000000) == 126) {
            long result = 0;
            for (int i = 0; i < 2; ++i) {
                if ((next = stream.read()) == -1) return null;
                result <<= 8;
                result |= next & 0xFF;
            }
            length = result;
        } else if ((next & ~0b10000000) == 127) {
            long result = 0;
            for (int i = 0; i < 4; ++i) {
                if ((next = stream.read()) == -1) return null;
                result <<= 8;
                result |= next & 0xFF;
            }
            length = result;
        } else {
            length = next & ~0b10000000;
        }

        // Is there a masking key?
        final int maskingKey;
        if (mask) {
            int result = 0;
            for (int i = 0; i < 4; ++i) {
                if ((next = stream.read()) == -1) return null;
                result <<= 8;
                result |= next & 0xFF;
            }
            maskingKey = result;
        } else {
            maskingKey = 0;
        }

        // Here we actually load the payload data.
        // Let's just ignore the fact that we can't handle payload of > Integer.MAX_VALUE ...
        final byte[] payload = new byte[(int) length];
        if (stream.read(payload, 0, payload.length) != payload.length) {
            return null;
        }

        // When the client masked the message, we have to decode it here.
        if (mask) {
            for (int i = 0; i < payload.length; ++i) {
                payload[i] = (byte) (payload[i] ^ (maskingKey >> (24 - (i & 0x3) * 8)));
            }
        }

        switch (interpretation) {
            case Close:
                if (payload.length > 0) {
                    short status = (short) ((payload[0] << 8) | payload[1]);
                    return new CloseMessage(status, new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8));
                } else {
                    return new CloseMessage();
                }
            case Text:
                return new TextMessage(new String(payload, StandardCharsets.UTF_8));
            case Binary:
                return new BinaryMessage(payload);
            case Ping:
                return new PingMessage(new String(payload, StandardCharsets.UTF_8));
            case Pong:
                return new PongMessage(new String(payload, StandardCharsets.UTF_8));
            default:
                throw new UnsupportedOperationException("The interpretation mode " + interpretation + " is not supported yet.");
        }
    }

    public enum Type {
        Continuation(0x0),
        Text(0x1),
        Binary(0x2),
        Close(0x8),
        Ping(0x9),
        Pong(0xA),
        Unknown(0x10);

        public final int id;

        Type(final int id) {
            this.id = id;
        }

        static Type getBy(final int id) {
            switch (id) {
                case 0x0: return Continuation;
                case 0x1: return Text;
                case 0x2: return Binary;
                case 0x8: return Close;
                case 0x9: return Ping;
                case 0xA: return Pong;
                default: return Unknown;
            }
        }

    }

}
