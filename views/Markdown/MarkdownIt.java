record MarkdownIt(LiveView view) implements Clerk {
    public MarkdownIt {
        String onlinePath = "https://cdn.jsdelivr.net/npm/markdown-it@14.1.0/dist/markdown-it.min.js";
        String localPath = "views/Markdown/markdown-it.min.js";
        Clerk.load(view, onlinePath, localPath);
        Clerk.load(view, "views/Markdown/highlight.min.js");
        Clerk.load(view, "views/Markdown/mathjax3.js");
        // Clerk.script(view, """
        //     var md = markdownit({
        //         html: true,
        //         linkify: true,
        //         typographer: true
        //     });
        //     """);
        Clerk.script(view, """
            var md = markdownit({
                highlight: function (str, lang) {
                    if (lang && hljs.getLanguage(lang)) {
                        try {
                            return hljs.highlight(str, { language: lang }).value;
                        } catch (__) {}
                    }
                    return ''; // use external default escaping
                },
                html: true,
                linkify: true,
                typographer: true
            });
            md.use(window.mathjax3);
            """);
    }
    public String write(String markdownText) {
        String ID = Clerk.generateID(10);
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        Clerk.write(view, "<script id='" + ID + "' type='preformatted'>" + markdownText + "</script>");
        Clerk.call(view, "var scriptElement = document.getElementById('" + ID  + "');"
        +
        """
        var divElement = document.createElement('div');
        divElement.id = scriptElement.id;
        divElement.innerHTML = md.render(scriptElement.textContent);
        scriptElement.parentNode.replaceChild(divElement, scriptElement);
        """
        );
        return ID;
    }
}
