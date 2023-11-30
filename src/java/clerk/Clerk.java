package clerk;

import clerk.websocket.Message;
import clerk.websocket.WebsocketServer;

public class Clerk {

    public static final WebsocketServer websocket;

    static {
        try {
            final Server server = new Server(8082);
            server.start();
            websocket = new WebsocketServer(8083);
            websocket.start();
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void main(String[] args) { }

}
