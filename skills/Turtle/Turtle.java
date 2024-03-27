import static java.lang.StringTemplate.STR;

class Turtle extends ViewManager {
    final String ID;
    final int width, height;

    Turtle(LiveView view, int width, int height) {
        this.view = checkViewAndLoadOnce(view, Turtle.views, "skills/Turtle/turtle.js");
        this.width  = Math.max(1, Math.abs(width));  // width is at least of size 1
        this.height = Math.max(1, Math.abs(height)); // height is at least of size 1
        ID = Clerk.generateID(6);
        this.view.write(STR."""
            <canvas id="turtleCanvas\{ID}" width="\{this.width}" height="\{this.height}" style="border:1px solid #000;">
            </canvas>
            """);
        this.view.script(STR."const turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));");
    }

    Turtle(LiveView view) { this(view, 500, 500); }
    Turtle(int width, int height) { this(null, width, height); }
    Turtle() { this(null); }

    Turtle penDown() {
        view.call(STR."turtle\{ID}.penDown();");
        return this;
    }

    Turtle penUp() {
        view.call(STR."turtle\{ID}.penUp();");
        return this;
    }

    Turtle forward(double distance) {
        view.call(STR."turtle\{ID}.forward(\{distance});");
        return this;
    }

    Turtle backward(double distance) {
        view.call(STR."turtle\{ID}.backward(\{distance});");
        return this;
    }

    Turtle left(double degrees) {
        view.call(STR."turtle\{ID}.left(\{degrees});");
        return this;
    }

    Turtle right(double degrees) {
        view.call(STR."turtle\{ID}.right(\{degrees});");
        return this;
    }
}
