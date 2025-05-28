import lvp.Clerk;
import lvp.skills.Text;
import lvp.skills.Interaction;
import lvp.views.Dot;
import lvp.views.Turtle;


void main() {
    Clerk.clear();
    // Markdown 1
    Clerk.markdown(Text.fillOut("""
    # Interaktive LVP Demo

    ## Markdown
    Die Markdown-View erlaubt es, Markdown-Text direkt im Browser darzustellen. Der folgende Code zeigt ein einfaches Beispiel, wie Text im Markdown-Format 
    an den Browser gesendet und dort automatisch als HTML gerendert wird:
    ```java
    ${0}
    ```
    Der Aufruf `Clerk.markdown(text)` elaubt den einfachen Zugriff auf die Markdown-View.
    In diesem Beispiel werden zusätzlich zwei unterstützende Skills verwendet:
    - `Text.fillOut(...)`: Zum Befüllen von String-Vorlagen mit dynamischen Inhalten, indem Platzhalter (z.B. ${2}) durch die Auswertung von übergebenen Ausdrücken ersetzt werden.
    - `Text.codeBlock(...)`: Zum Einbinden von Codeabschnitten als interaktive Blöcke im Markdown-Text.

    ## Turtle
    Die Turtle-View ermöglicht das Zeichnen und Anzeigen von SVG-Grafiken im Browser. Diese können Schritt für Schritt aufgebaut werden:
    ```java
    ${1}
    ```
    """, Text.codeBlock("./demo.java", "// Markdown 1"), Text.codeBlock("./demo.java", "// Turtle 1"), "${0}"));
    // Markdown 1

    // Label Turtle 1
    // Turtle 1
    var turtle = new Turtle(0, 200, 0, 25, 50, 0, 0);
    turtle.forward(25).right(60).backward(25).right(60).forward(25).write();
    // Turtle 1
    // Label Turtle 1

    Clerk.markdown(Text.fillOut("""
        
    ## Interaktionen
    Die Live-View ist nicht nur ein Anzeigewerkzeug, sondern dient auch als interaktiver Editor. Änderungen an eingebetteten Code-Blöcken wirken sich direkt auf die zugrunde liegende 
    Datei aus. Dadurch kann der dokumentierte Code live ausprobiert und bearbeitet werden.
    Ein interaktiver Code-Block wird mithilfe von `Text.codeBlock(...)` definiert. Der entsprechende Code im Quelltext muss durch Kommentar-Labels (z.B. `// Turtle 1`) markiert werden:
    ```java
    ${0}
    ```
    Dieser markierte Block kann anschließend über `Text.codeBlock("./demo.java", "// Turtle 1")` eingebunden werden. Wird dieser Block in einen Markdown-Abschnitt eingefügt, erscheint 
    er in der Live-View als editierbarer Code-Bereich.

    Zusätzlich können JavaScript-Funktionen eingebunden werden, die gezielt Teile des Quelltexts verändern. Dafür wird `Interaction.eventFunction(...)` verwendet. Dieser Skill liefert 
    eine Funktion, die anhand des Dateipfads, eines Labels und des neuen Codes eine markierte Zeile ersetzt.
    
    Um solche Funktionen interaktiv nutzbar zu machen, kann `Interaction.button(...)` verwendet werden. Damit lässt sich ein Button erstellen, der bei Klick eine bestimmte Stelle im Code anpasst:
    ```java 
    ${1}
    ```

    ### Color Change
    Im folgenden Beispiel wird eine Turtle-Grafik dargestellt:
    ```java
    ${2}
    ```
    
    """, Text.codeBlock("./demo.java", "// Label Turtle 1"), Text.codeBlock("./demo.java", "// Buttons"), Text.codeBlock("./demo.java", "// Turtle triangle")));

    // Turtle 2
    var turtle2 = new Turtle(0, 200, 0, 50, 100, 12, 0);
    drawing(turtle2, 24);
    turtle2.write();
    // Turtle 2
    
    Clerk.markdown("""
            Darunter befinden sich drei Buttons, die jeweils die Farbe der Turtle ändern. Die zu ersetzende Stelle im Quellcode ist durch das Label `// turtle color` markiert. Beim Klick auf einen Button wird
            dieser Teil des Codes automatisch angepasst.
            """);

    // Buttons
    Clerk.write(Interaction.button("Red", Interaction.eventFunction("./demo.java", "// turtle color", "turtle.color(255, i * 256 / 37, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Green", Interaction.eventFunction("./demo.java", "// turtle color", "turtle.color(i * 256 / 37, 255, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Blue", Interaction.eventFunction("./demo.java", "// turtle color", "turtle.color(i * 256 / 37, i * 256 / 37, 255, 1);")));
    // Buttons

    Clerk.markdown(Text.fillOut("""
            ### Turtle mit Timeline
            Die Turtle-View unterstützt außerdem eine Timeline, über die sich die Zeichenreihenfolge der Grafik Schritt für Schritt nachvollziehen lässt:
            ```java
            ${0}
            ```
            """, Text.codeBlock("./demo.java", "// Turtle 3")));

    // Turtle 3
    var turtle3 = new Turtle(0, 200, 0, 50, 100, 12, 0);
    drawing(turtle3, 24);
    turtle3.write().timelineSlider();
    // Turtle 3

    Clerk.markdown(Text.fillOut("""
    ## Dot View
    Die Dot-View erlaubt das Anzeigen von Graphen, die im [DOT-Format](https://graphviz.org/doc/info/lang.html) beschrieben sind.
    ```java
    ${0}
    ```
    """, Text.codeBlock("./demo.java", "// Dot")));

    // Dot
    Dot dot = new Dot();
    dot.draw("""
    digraph G {
        A -> B;
        B -> C;
    }
    """);
    // Dot
}


// Turtle triangle
void triangle(Turtle turtle, double size) {
    turtle.forward(size).right(60).backward(size).right(60).forward(size).right(60 + 180);
}

void drawing(Turtle turtle, double size) {
    for (int i = 1; i <= 18; i++) {
        turtle.color(255, i * 256 / 37, i * 256 / 37, 1); // turtle color
        turtle.width(1.0 - 1.0 / 36.0 * i);
        triangle(turtle, size + 1 - 2 * i);
        turtle.left(20).forward(5);
    }
}
// Turtle triangle
