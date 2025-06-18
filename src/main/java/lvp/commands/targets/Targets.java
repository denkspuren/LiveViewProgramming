package lvp.commands.targets;

import lvp.SSEType;
import lvp.Server;
import lvp.commands.targets.dot.GraphSpec;

public class Targets {
    public record MetaInformation(String sourceId, String id) {}
    Server server;

    public static Targets of(Server server) { return new Targets(server); }

    private Targets(Server server) {
        this.server = server;
    }

    public void consumeClear(MetaInformation meta, String content) {
        server.sendServerEvent(SSEType.CLEAR, "", meta.id(), meta.sourceId());
    }
    
    public void consumeHTML(MetaInformation meta, String content) {
        server.sendServerEvent(SSEType.WRITE, content, meta.id(), meta.sourceId());
    }

    public void consumeJS(MetaInformation meta, String content) {
        server.sendServerEvent(SSEType.SCRIPT, content, meta.id(), meta.sourceId());
    }

    public void consumeJSCall(MetaInformation meta, String content) {
        server.sendServerEvent(SSEType.CALL, content, meta.id(), meta.sourceId());
    }

    public void consumeCss(MetaInformation meta, String content) {
        server.sendServerEvent(SSEType.CSS, content, meta.id(), meta.sourceId());
    }

    public void consumeSubViewStyle(MetaInformation meta, String content) {
        consumeCss(meta, "#subViewContainer-" + meta.sourceId() + " { " + content + " }");
    }

    public void consumeMarkdown(MetaInformation meta, String content) {
        consumeHTML(new MetaInformation(meta.sourceId(), "container" + meta.id()), "<script id='" + meta.id() + "' type='preformatted'>" + content + "</script>");
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        
        consumeJSCall(new MetaInformation(meta.sourceId(), "call" + meta.id()), "var scriptElement = document.getElementById('" + meta.id() + "');"
        +
        """
        var divElement = document.createElement('div');
        divElement.id = scriptElement.id;
        divElement.innerHTML = window.md.render(scriptElement.textContent);
        scriptElement.parentNode.replaceChild(divElement, scriptElement);
        """
        );
    }

    public void consumeDot(MetaInformation meta, String content) {
        GraphSpec specs = GraphSpec.fromContent(content);

        consumeHTML(new MetaInformation(meta.sourceId(), "container" + meta.id()), "<div id='dotContainer" + meta.id() + "'></div>");
        consumeJS(new MetaInformation(meta.sourceId(), "script" + meta.id()), "clerk['" + meta.sourceId() + "'].dot" + meta.id() + " = new Dot(document.getElementById('dotContainer" + meta.id() + "'), " + specs.width().orElse(500) + ", " + specs.height().orElse(500) + ");");
        consumeJSCall(new MetaInformation(meta.sourceId(), "call" + meta.id()), "clerk['" + meta.sourceId() + "'].dot" + meta.id() + ".draw(\"" + specs.dot() + "\")");
    }
    
}
