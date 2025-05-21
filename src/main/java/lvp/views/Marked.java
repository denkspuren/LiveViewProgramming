package lvp.views;
import lvp.Clerk;

public record Marked() implements Clerk {
    static final String markedUrl = "https://cdn.jsdelivr.net/npm/marked/marked.min.js";
    
    public Marked {
        Clerk.load(markedUrl, "views/markdown/marked.min.js");
        Clerk.script("""
            var md = marked.use({
                gfm: true
            });
            """);
    }
    public String write(String markdownText) {
        String ID = Clerk.generateID(10);
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        Clerk.write("<script id='" + ID + "' type='preformatted'>" + markdownText + "</script>");
        Clerk.call("var scriptElement = document.getElementById('" + ID + "');"
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

