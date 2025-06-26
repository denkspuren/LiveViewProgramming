package lvp.views;

import lvp.Clerk;

//TODO: Rewrite for next minor / major release
// This is a temporary solution to use the Viz.js library without changing the api.
public class Dot implements Clerk {    
    final String ID;

    public Dot(int width, int height) {
        ID = Clerk.getHashID(this);

        Clerk.write("<div id='dotContainer" + ID + "'></div>");
        Clerk.script("clerk.dot" + ID + " = new Dot(document.getElementById('dotContainer" + ID + "'));");
    }

    public Dot() { this(500, 500); }

    public Dot draw(String dotString) {
        String escaped = dotString.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "");
        Clerk.script("clerk.dot" + ID + ".draw(\"" + escaped + "\")");
        return this;
    }
}