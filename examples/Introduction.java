import static java.io.IO.println;

void main() {
    println("""
            Clear
            Text[Intro]:
            # LiveViewProgramming Konzept
            Auszug aus dem Readme:
            ~~~
            | Markdown

            Text[Template1]:
            > "${Zitat}"
            ~~~

            Cutout: README.md;## 💟 Motivation: Views bereichern das Programmieren;### Views und Skills zum Programmverständnis
            | Text[Template1] | Markdown

            Markdown:
            ## Ziele
            - Visualisierung von Programmierung
            - Programmdokumentation 
            - Einfache Interaktion
            - Sprachunabhängigkeit
            - Erweiterbarkeit
            ~~~

            Markdown: # Umsetzung

            Cutout: syntax.md; # LVP Syntax
            | Markdown
            """);
}