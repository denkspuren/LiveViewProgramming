package de.denkspuren.lvp.views.dot;

import de.denkspuren.lvp.Clerk;
import de.denkspuren.lvp.Server;

class Dot implements Clerk {
    final String visLibOnlinePath = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js";
    final String visLibOfflinePath = "views/dot/vis-network.min.js";
    final String dotLibPath = "views/dot/dot.js";
    final String ID;
    Server view;
    int width, height;

    Dot(Server view, int width, int height) {
        this.view = view;
        this.width = width;
        this.height = height;

        Clerk.load(view, visLibOnlinePath, visLibOfflinePath);
        Clerk.load(view, dotLibPath);

        ID = Clerk.getHashID(this);

        Clerk.write(view, "<div id='dotContainer" + ID + "'></div>");
        Clerk.script(view, "const dot" + ID + " = new Dot(document.getElementById('dotContainer" + ID + "'), " + this.width + ", " + this.height + ");");
    }

    Dot(Server view) { this(view, 500, 500); }
    Dot(int width, int height) { this(Clerk.view(), width, height); }
    Dot() { this(Clerk.view());}

    Dot draw(String dotString) {
        String escaped = dotString.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "");
        Clerk.script(view, "dot" + ID + ".draw(\"dinetwork{" + escaped + "}\")");
        return this;
    }
}