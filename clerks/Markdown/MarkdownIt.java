import static java.lang.StringTemplate.STR;

record MarkdownIt(LiveView view) implements Clerk {
    public MarkdownIt {
        String onlinePath = "https://cdn.jsdelivr.net/npm/markdown-it@14.1.0/dist/markdown-it.min.js";
        String localPath = "clerks/Markdown/markdown-it.min.js";
        Clerk.load(view, onlinePath, localPath);
        Clerk.script(view, STR."""
            var md = markdownit({
                html: true,
                linkify: true,
                typographer: true
            });
            """);
    }
    public String write(String markdownText) {
        String ID = Clerk.generateID(10);
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        Clerk.write(view, STR."""
            <script id="\{ID}" type='preformatted'>
            \{markdownText}
            </script>
            """);
        Clerk.call(view, STR."""
            var scriptElement = document.getElementById("\{ID}");
            var divElement = document.createElement('div');
            divElement.id = scriptElement.id;
            divElement.innerHTML = md.render(scriptElement.textContent);
            scriptElement.parentNode.replaceChild(divElement, scriptElement);
            """);
        return ID;
    }
}
