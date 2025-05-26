package lvp.views;
import lvp.Clerk;

public record MarkdownIt() implements Clerk {
    public String write(String markdownText) {
        String ID = Clerk.generateID(10);
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        Clerk.write("<script id='" + ID + "' type='preformatted'>" + markdownText + "</script>");
        Clerk.call("var scriptElement = document.getElementById('" + ID  + "');"
        +
        """
        var divElement = document.createElement('div');
        divElement.id = scriptElement.id;
        divElement.innerHTML = window.md.render(scriptElement.textContent);
        scriptElement.parentNode.replaceChild(divElement, scriptElement);
        """
        );
        return ID;
    }
}
