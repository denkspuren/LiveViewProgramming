package lvp.views;

import lvp.Clerk;
import lvp.Server;

public class Dot implements Clerk {
    final String visLibOnlinePath = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js";
    final String visLibOfflinePath = "views/dot/vis-network.min.js";
    final String dotLibPath = "views/dot/dot.js";
    final String ID;
    Server server;
    int width, height;

    public Dot(Server server, int width, int height) {
        this.server = server;
        this.width = width;
        this.height = height;

        Clerk.load(server, visLibOnlinePath, visLibOfflinePath);
        Clerk.load(server, dotLibPath);

        ID = Clerk.getHashID(this);

        Clerk.write(server, "<div id='dotContainer" + ID + "'></div>");
        Clerk.script(server, "const dot" + ID + " = new Dot(document.getElementById('dotContainer" + ID + "'), " + this.width + ", " + this.height + ");");
    }

    public Dot(Server server) { this(server, 500, 500); }
    public Dot(int width, int height) { this(Clerk.serve(), width, height); }
    public Dot() { this(Clerk.serve());}

    public Dot draw(String dotString) {
        String escaped = dotString.replaceAll("\\\"", "\\\\\"").replaceAll("\\n", "");
        Clerk.script(server, "dot" + ID + ".draw(\"dinetwork{" + escaped + "}\")");
        return this;
    }
}