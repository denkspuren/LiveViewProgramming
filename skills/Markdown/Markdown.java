import static java.lang.StringTemplate.STR;

record Markdown(LiveView view) implements Clerk {
    public Markdown { 
        Clerk.loadPath(view, "https://cdn.jsdelivr.net/npm/marked/marked.min.js");
    }
    public Markdown write(String markdownText) {
        String ID = Clerk.generateID(10);
        view.write(STR."""
            <div id="\{ID}">
            \{markdownText}
            </div>
            """);
        view.script(STR."""
            var markdownContent = document.getElementById("\{ID}").textContent;
            var renderedHTML = marked.parse(markdownContent);
            document.getElementById("\{ID}").innerHTML = renderedHTML;
            """);
        return this;
    }
}
