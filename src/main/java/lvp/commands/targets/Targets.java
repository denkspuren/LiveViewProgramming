package lvp.commands.targets;

import lvp.SSEType;
import lvp.Server;
import lvp.commands.targets.dot.GraphSpec;

public class Targets {
    Server server;

    public static Targets of(Server server) { return new Targets(server); }

    private Targets(Server server) {
        this.server = server;
    }

    public void consumeClear(String id, String content) {
        server.sendServerEvent(SSEType.CLEAR, "");
    }
    
    public void consumeHTML(String id, String content) {
        server.sendServerEvent(SSEType.WRITE, content);
    }

    public void consumeJS(String id, String content) {
        server.sendServerEvent(SSEType.SCRIPT, content);
    }

    public void consumeJSCall(String id, String content) {
        server.sendServerEvent(SSEType.CALL, content);
    }

    public void consumeMarkdown(String id, String content) {
        consumeHTML("container" + id, "<script id='" + id + "' type='preformatted'>" + content + "</script>");
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        
        consumeJSCall("call" + id, "var scriptElement = document.getElementById('" + id  + "');"
        +
        """
        var divElement = document.createElement('div');
        divElement.id = scriptElement.id;
        divElement.innerHTML = window.md.render(scriptElement.textContent);
        scriptElement.parentNode.replaceChild(divElement, scriptElement);
        """
        );
    }

    public void consumeDot(String id, String content) {
        GraphSpec specs = GraphSpec.fromContent(content);

        consumeHTML("container" + id, "<div id='dotContainer" + id + "'></div>");
        consumeJS("script" + id, "clerk.dot" + id + " = new Dot(document.getElementById('dotContainer" + id + "'), " + specs.width().orElse(500) + ", " + specs.height().orElse(500) + ");");
        consumeJSCall("call" + id, "clerk.dot" + id + ".draw(\"" + specs.dot() + "\")");
    }
    
}
