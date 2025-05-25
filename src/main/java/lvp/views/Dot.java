package lvp.views;

import lvp.Clerk;

public class Dot implements Clerk {
    static final String visLibUrl = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js";
    
    final String ID;
    int width, height;

    public Dot(int width, int height) {
        this.width = width;
        this.height = height;

        Clerk.load(visLibUrl, "views/dot/vis-network.min.js");
        Clerk.load( "views/dot/dot.js");

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