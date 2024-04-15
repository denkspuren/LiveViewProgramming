import static java.lang.StringTemplate.STR;

class Turtle implements Clerk {
    final String ID;
    LiveView view;
    final int width, height;

    Turtle(LiveView view, int width, int height) {
        this.view = view;
        this.width  = Math.max(1, Math.abs(width));  // width is at least of size 1
        this.height = Math.max(1, Math.abs(height)); // height is at least of size 1
        ID = Clerk.getHashID(this);
        Clerk.load(view, "clerks/Turtle/turtle.js");
        Clerk.write(view, STR."""
            <canvas id="turtleCanvas\{ID}" width="\{this.width}" height="\{this.height}" style="border:1px solid #000;">
            </canvas>
            """);
        Clerk.script(view, STR."const turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));");
    }

    Turtle(LiveView view) { this(view, 500, 500); }
    Turtle(int width, int height) { this(Clerk.view(), width, height); }
    Turtle() { this(Clerk.view()); }

    Turtle penDown() {
        Clerk.call(view, STR."turtle\{ID}.penDown();");
        return this;
    }

    Turtle penUp() {
        Clerk.call(view, STR."turtle\{ID}.penUp();");
        return this;
    }

    Turtle forward(double distance) {
        Clerk.call(view, STR."turtle\{ID}.forward(\{distance});");
        return this;
    }

    Turtle backward(double distance) {
        Clerk.call(view, STR."turtle\{ID}.backward(\{distance});");
        return this;
    }

    Turtle left(double degrees) {
        Clerk.call(view, STR."turtle\{ID}.left(\{degrees});");
        return this;
    }

    Turtle right(double degrees) {
        Clerk.call(view, STR."turtle\{ID}.right(\{degrees});");
        return this;
    }

    Turtle color(String color) {
        Clerk.call(view, STR."turtle\{ID}.color('\{color}');");
        return this;
    }

    Turtle lineWidth(double width) {
        Clerk.call(view, STR."turtle\{ID}.lineWidth('\{width}')");
        return this;
    }
}
