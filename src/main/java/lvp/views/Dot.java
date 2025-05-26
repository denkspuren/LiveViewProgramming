package lvp.views;

import lvp.Clerk;

public class Dot implements Clerk {    
    final String ID;
    int width, height;

    public Dot(int width, int height) {
        this.width = width;
        this.height = height;
        ID = Clerk.getHashID(this);

        Clerk.write("<div id='dotContainer" + ID + "'></div>");
        Clerk.script("clerk.dot" + ID + " = new Dot(document.getElementById('dotContainer" + ID + "'), " + this.width + ", " + this.height + ");");
    }

    public Dot() { this(500, 500); }

    public Dot draw(String dotString) {
        String escaped = dotString.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "");
        Clerk.script("clerk.dot" + ID + ".draw(\"" + escaped + "\")");
        return this;
    }
}