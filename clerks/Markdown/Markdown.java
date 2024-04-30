import static java.lang.StringTemplate.STR;

record Markdown(LiveView view) implements Clerk {
    public Markdown {
        String onlinePath = "https://cdn.jsdelivr.net/npm/marked/marked.min.js";
        String localPath = "clerks/Markdown/marked.min.js";
        Clerk.load(view, onlinePath, localPath);
    }
    public Markdown write(String markdownText) {
        String ID = Clerk.generateID(10);
        Clerk.write(view, STR."""
            <div id="\{ID}">
            \{markdownText}
            </div>
            """);
        Clerk.script(view, STR."""
            var markdownContent = document.getElementById("\{ID}").textContent;
            var renderedHTML = marked.parse(markdownContent);
            document.getElementById("\{ID}").innerHTML = renderedHTML;
            """);
        return this;
    }

    public Markdown writeEscaped(String markdownText) {
        String escapedString = markdownText.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("'", "&#39;")
            .replaceAll("\"", "&quot;");

        write(escapedString);
        return this;
    }
}
