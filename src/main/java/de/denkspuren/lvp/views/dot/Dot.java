package de.denkspuren.lvp.views.dot;

import de.denkspuren.lvp.Clerk;
import de.denkspuren.lvp.Server;

class Dot implements Clerk {
    final String visLibOnlinePath = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js";
    final String visLibOfflinePath = "views/dot/vis-network.min.js";
    final String dotLibPath = "views/dot/dot.js";
    final String ID;
    Server server;
    int width, height;

    Dot(Server server, int width, int height) {
        this.server = server;
        this.width = width;
        this.height = height;

        Clerk.load(server, visLibOnlinePath, visLibOfflinePath);
        Clerk.load(server, dotLibPath);

        ID = Clerk.getHashID(this);

        Clerk.write(server, "<div id='dotContainer" + ID + "'></div>");
        Clerk.script(server, "const dot" + ID + " = new Dot(document.getElementById('dotContainer" + ID + "'), " + this.width + ", " + this.height + ");");
    }

    Dot(Server server) { this(server, 500, 500); }
    Dot(int width, int height) { this(Clerk.serve(), width, height); }
    Dot() { this(Clerk.serve());}

    Dot draw(String dotString) {
        String escaped = dotString.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "");
        Clerk.script(server, "dot" + ID + ".draw(\"dinetwork{" + escaped + "}\")");
        return this;
    }
}