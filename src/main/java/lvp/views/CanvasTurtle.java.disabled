package lvp.views;

import lvp.Clerk;
import lvp.views.canvasturtle.Font;

public class CanvasTurtle implements Clerk {
    public final String ID;
    final int width, height;
    Font textFont = Font.SANSSERIF;
    double textSize = 10;
    Font.Align textAlign = Font.Align.CENTER;

    public CanvasTurtle(int width, int height) {
        this.width  = Math.max(1, Math.abs(width));  // width is at least of size 1
        this.height = Math.max(1, Math.abs(height)); // height is at least of size 1
        ID = Clerk.getHashID(this);
        Clerk.load("views/canvasturtle/turtle.js");
        Clerk.write("<canvas id='turtleCanvas" + ID + "' width='" + this.width + "' height='" + this.height + "' style='border:1px solid #000;'></canvas>");
        Clerk.script("let turtle" + ID + " = new Turtle(document.getElementById('turtleCanvas" + ID + "'));");
    }

    public CanvasTurtle() { this(500, 500); }

    public CanvasTurtle penDown() {
        Clerk.call("turtle" + ID + ".penDown();");
        return this;
    }

    public CanvasTurtle penUp() {
        Clerk.call("turtle" + ID + ".penUp();");
        return this;
    }

    public CanvasTurtle forward(double distance) {
        Clerk.call("turtle" + ID + ".forward(" + distance + ");");
        return this;
    }

    public CanvasTurtle backward(double distance) {
        Clerk.call("turtle" + ID + ".backward(" + distance + ");");
        return this;
    }

    public CanvasTurtle left(double degrees) {
        Clerk.call("turtle" + ID + ".left(" + degrees + ");");
        return this;
    }

    public CanvasTurtle right(double degrees) {
        Clerk.call("turtle" + ID + ".right(" + degrees + ");");
        return this;
    }

    public CanvasTurtle color(int red, int green, int blue) {
        Clerk.call("turtle" + ID + ".color('rgb(" + (red & 0xFF) + ", " + (green & 0xFF) + ", " + (blue & 0xFF) + ")');");
        return this;
    }

    public CanvasTurtle color(int rgb) {
        color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        return this;
    }

    public CanvasTurtle lineWidth(double width) {
        Clerk.call("turtle" + ID + ".lineWidth('" + width + "');");
        return this;
    }

    public CanvasTurtle reset() {
        Clerk.call("turtle" + ID + ".reset();");
        return this;
    }

    public CanvasTurtle text(String text, Font font, double size, Font.Align align) {
        textFont = font;
        textSize = size;
        textAlign = align;
        Clerk.call("turtle" + ID + ".text('" + text + "', '" + "" + size + "px " + font + "', '" + align + "')");
        return this;
    }

    public CanvasTurtle text(String text) { return text(text, textFont, textSize, textAlign); }

    public CanvasTurtle moveTo(double x, double y) {
        Clerk.call("turtle" + ID + ".moveTo(" + x + ", " + y + ");");
        return this;
    }

    public CanvasTurtle lineTo(double x, double y) {
        Clerk.call("turtle" + ID + ".lineTo(" + x + ", " + y + ");");
        return this;
    }
}