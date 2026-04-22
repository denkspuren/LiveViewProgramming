void main() {
    println("""
            Clear
            Markdown:
            # Die Code-Dokumentation mit Markdown

            Für die Code-Dokumentation mit Markdown sind Textblöcke, sowie die Text und Codeblock Kommandos entscheidende Hilfsmittel.

            * Mit Textblöcken lassen sich String-Literale als Textblöcke über mehrere Zeilen hinweg angeben. Ein solcher Textblock beginnt und endet mit drei Anführungszeichen `\"""`.

            * Das Kommando 'Codeblock' ist hauptsächlich dafür da, um Text aus einer Datei auszuschneiden und in einem Markdown Codeblock einzufügen, welcher im Browser editiert werden kann; der Bereich, der ausgeschnitten werden soll, wird durch Textmarken (Labels) ausgewiesen.
                - Für einen nicht editierbaren Codeblock kann das Kommando `Cutout` verwendet werden.
            * Das Kommando 'Text' erlaubt es definierte Strings zu speichern und mit Aufüllfeldern versehene Texte, mit weiteren Inhalten aufzufüllen.
            ~~~
            """);
        
    // Testfälle
    assert factorial(0) == 1 && factorial(1) == 1;
    assert factorial(2) == 2 && factorial(3) == 6;
    assert factorial(4) == 24 && factorial(5) == 120;
    // Beispiel
    int num;
    String s = "Die Fakultät von " + (num = 6) + " ist " + factorial(num) + ".";
    // Beispiel

    println("""
            Text[Template]:
            ## Dynamische Inhalte in Zeichenketten einbetten

            Wenn Inhalte in einer Zeichenkette dynamisch berechnet und eingefügt werden sollen, kann man das beispielsweise wie folgt machen:

            ```
            ${Beispiel}
            ```

            Das Ergebnis der Zeichenkette `s` ist

            ```
            ${Resultat}
            ```

            Das sieht dann, wenn man die Zeichenkette im Markdown einfügt, so aus: ${Resultat}

            Diese Technik der Einbettung von dynamischen Inhalten in eine Zeichenkette lässt sich ausreizen mit den Kommandos `Codeblock` oder `Cutout`. Damit kann der Java-Quelltext sich zur Laufzeit selbst ausschneiden zur Einbettung in Markdown! Das ist der Schlüssel zu sich selbst dokumentierendem Programmcode.
            ~~~
            """);
    println("Text[Resultat]: " + s);
    println("""
            
            Codeblock: examples/CodeDokuMitMarkdown.java; // Beispiel
            | Text[Template] | Text[TemplateMitBeispiel]
            Text[Resultat]
            | Text[TemplateMitBeispiel] | Markdown
            """);
}

// Fakultätsfunktion
long factorial(int n) {
    assert n >= 0 : "Positive Ganzzahl erforderlich";
    if (n == 1 || n == 0) return 1;
    return n * factorial(n - 1);
}
// Fakultätsfunktion
