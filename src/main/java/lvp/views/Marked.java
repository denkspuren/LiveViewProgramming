package lvp.views;
import lvp.Clerk;
import lvp.Server;

public record Marked(Server server) implements Clerk {
    public Marked {
        String onlinePath = "https://cdn.jsdelivr.net/npm/marked/marked.min.js";
        String localPath = "views/markdown/marked.min.js";
        Clerk.load(server, onlinePath, localPath);
        Clerk.script(server, """
            var md = marked.use({
                gfm: true
            });
            """);
    }
    public String write(String markdownText) {
        String ID = Clerk.generateID(10);
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        Clerk.write(server, "<script id='" + ID + "' type='preformatted'>" + markdownText + "</script>");
        Clerk.call(server, "var scriptElement = document.getElementById('" + ID + "');"
        +
        """
        var divElement = document.createElement('div');
        divElement.id = scriptElement.id;
        divElement.innerHTML = md.parse(scriptElement.textContent);
        scriptElement.parentNode.replaceChild(divElement, scriptElement);
        """);
        return ID;
    }
}

