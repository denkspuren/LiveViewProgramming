package clerk;

import clerk.websocket.TextMessage;

public class Turtle {

    public Turtle() {

    }

    public void move(final int x, final int y) {
        new TextMessage("{ command: 'move', position: { x: " + x + ", y: " + y + " } }").write(Clerk.websocket.getOutputStream());
    }

}
