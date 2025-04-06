record Marked(LiveView view) implements Clerk {
    public Marked {
        String onlinePath = "https://cdn.jsdelivr.net/npm/marked/marked.min.js";
        String localPath = "views/Markdown/marked.min.js";
        Clerk.load(view, onlinePath, localPath);
        Clerk.script(view, """
            var md = marked.use({
                gfm: true
            });
            """);
    }
    public String write(String markdownText) {
        String ID = Clerk.generateID(10);
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        Clerk.write(view, "<script id='" + ID + "' type='preformatted'>" + markdownText + "</script>");
        Clerk.call(view, "var scriptElement = document.getElementById('" + ID + "');"
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

