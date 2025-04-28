package lvp.views;

import lvp.Clerk;
import lvp.Server;
import lvp.views.turtle.Font;

public class Turtle implements Clerk {
    public final String ID;
    Server server;
    final int width, height;
    Font textFont = Font.SANSSERIF;
    double textSize = 10;
    Font.Align textAlign = Font.Align.CENTER;

    public Turtle(Server server, int width, int height) {
        this.server = server;
        this.width  = Math.max(1, Math.abs(width));  // width is at least of size 1
        this.height = Math.max(1, Math.abs(height)); // height is at least of size 1
        ID = Clerk.getHashID(this);
        Clerk.load(server, "views/turtle/turtle.js");
        Clerk.write(server, "<canvas id='turtleCanvas" + ID + "' width='" + this.width + "' height='" + this.height + "' style='border:1px solid #000;'></canvas>");
        Clerk.script(server, "const turtle" + ID + " = new Turtle(document.getElementById('turtleCanvas" + ID + "'));");
    }

    public Turtle(Server server) { this(server, 500, 500); }
    public Turtle(int width, int height) { this(Clerk.serve(), width, height); }
    public Turtle() { this(Clerk.serve()); }

    public Turtle penDown() {
        Clerk.call(server, "turtle" + ID + ".penDown();");
        return this;
    }

    public Turtle penUp() {
        Clerk.call(server, "turtle" + ID + ".penUp();");
        return this;
    }

    public Turtle forward(double distance) {
        Clerk.call(server, "turtle" + ID + ".forward(" + distance + ");");
        return this;
    }

    public Turtle backward(double distance) {
        Clerk.call(server, "turtle" + ID + ".backward(" + distance + ");");
        return this;
    }

    public Turtle left(double degrees) {
        Clerk.call(server, "turtle" + ID + ".left(" + degrees + ");");
        return this;
    }

    public Turtle right(double degrees) {
        Clerk.call(server, "turtle" + ID + ".right(" + degrees + ");");
        return this;
    }

    public Turtle color(int red, int green, int blue) {
        Clerk.call(server, "turtle" + ID + ".color('rgb(" + (red & 0xFF) + ", " + (green & 0xFF) + ", " + (blue & 0xFF) + ")');");
        return this;
    }

    public Turtle color(int rgb) {
        color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        return this;
    }

    public Turtle lineWidth(double width) {
        Clerk.call(server, "turtle" + ID + ".lineWidth('" + width + "');");
        return this;
    }

    public Turtle reset() {
        Clerk.call(server, "turtle" + ID + ".reset();");
        return this;
    }

    public Turtle text(String text, Font font, double size, Font.Align align) {
        textFont = font;
        textSize = size;
        textAlign = align;
        Clerk.call(server, "turtle" + ID + ".text('" + text + "', '" + "" + size + "px " + font + "', '" + align + "')");
        return this;
    }

    public Turtle text(String text) { return text(text, textFont, textSize, textAlign); }

    public Turtle moveTo(double x, double y) {
        Clerk.call(server, "turtle" + ID + ".moveTo(" + x + ", " + y + ");");
        return this;
    }

    public Turtle lineTo(double x, double y) {
        Clerk.call(server, "turtle" + ID + ".lineTo(" + x + ", " + y + ");");
        return this;
    }
}