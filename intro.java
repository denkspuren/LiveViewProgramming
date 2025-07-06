List<String> obst = List.of("Apfel", "Birne", "Banane");
void main() {
    println("""
            Clear
            Markdown: ## Einkaufsliste
            Markdown:
            """);
    println(buildObstListe());
    println("~~~");
    println("""
            Text[template]:
            ## Beispiel
            Irgendein ${0} anzeigen.
            ~~~
            | Markdown

            Text: Text
            | Text[template] | Markdown

            Text[t2]:
            ## Syntax Überschrift
            Das ist die Syntax
            ${0}
            Danach
            ~~~

            Cutout: ./syntax.md
            | Text[t2] | Markdown

            Text[t3]:
            ```java
            ${0}
            ```
            ~~~

            Codeblock:./intro.java;// example
            | Text[t3] | Markdown

            Register[skipId]: Counter wc
            Text:
            Hello World
            ~~~
            | Counter | Html

            """);
    
}

// example
String buildObstListe() {
    String out = "";
    for (String o : obst) {
        out += "**" + o + "**\n";
    }
    return out;
}
// example
