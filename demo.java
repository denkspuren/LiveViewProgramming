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
    Das folgende Beispiel zeigt, wie ganz einfach Markdown-Text an den Broswser geschickt werden kann und dort als HTML gerendert wird.
    ```java
    ${0}
    ```
    `Clerk.markdown(text)` elaubt den einfachen Zugriff auf die Markdown-View, um Markdown-Text an den Browser zu senden und dort als HTML zu rendern.
    Zusätzlich werden in diesem Beispiel zwei Skills verwendet:
    - `Text.fillOut(...)` zum Definieren von String-Vorlagen, die mit Auswertungen von Ausdrücken gefüllt werden können.
    - `Text.codeBlock(...)` zum ausschneiden von Textabschnitten, die als interaktive Code-Blöcke im Markdown-Text angezeigt werden sollen.

    ## Turtle
    Die Turtle-View ermöglicht das Erstellen von SVG-Grafiken, die im Browser angezeigt werden können.
    ```java
    ${1}
    ```
    """, Text.codeBlock("./demo.java", "// Markdown 1"), Text.codeBlock("./demo.java", "// Turtle 1")));
    // Markdown 1

    // Label Turtle 1
    // Turtle 1
    var turtle = new Turtle(0, 200, 0, 25, 50, 0, 0);
    turtle.forward(25).right(60).backward(25).right(60).forward(25).write();
    // Turtle 1
    // Label Turtle 1

    Clerk.markdown(Text.fillOut("""
        
    ## Interaktionen
    Die Live-View dient nicht nur zur Betrachtung von Inhalten, sondern kann auch als interaktiver Editor die Code-Datei bearbeiten. So führt das Bearbeiten
    der interaktiven Code-Blöcke zu einer Änderung im Source Code selbst, die dann in der Live-View angezeigt wird. Auf diese Weise können Änderungen am
    dokumentierten Code direkt in der Live-View ausgetestet werden. Dazu ermöglicht dies eine interaktive Dokumentation zu erstellen. Ein interaktiver Code-Block 
    kann durch den Skill `Text.codeBlock(...)` erstellt werden. Der auszuschneidende Code wird durch zwei gleichnamige Kommentar-Label makiert.
    ```java
    ${0}
    ```
    Der makierte Code kann dann durch `Text.codeBlock("./demo.java", "// Turtle 1")` ausgeschnitten werden. Dabei wird der Pfad zur Datei und das Label angegeben. Wenn dieser
    Code-Block in einen Markdown-Code-Block eingebettet wird, dann wird er in der Live-View als interaktiver Code-Block angezeigt.

    Neben der interaktiven Code-Blöcken, können auch JavaScript-Funktionen erstellt werden, die den Source Code bearbeiten können. Dazu dient der Skill `Interaction.eventFunction(...)`.
    Dieser Skill liefert eine JavaScript-Funktion, die eine makierte Zeile im Source Code ersetzt. Der Skill wird mit dem Pfad zur Datei, dem Label und dem zu ersetzenden Code aufgerufen.
    Diese Funktion kann dann zum Beispiel in einem Button verwendet werden, um den Source Code zu ändern. Um schnell einen Button zu erstellen, kann der Skill `Interaction.button(...)` verwendet werden.
    ```java 
    ${1}
    ```

    ### Color Change
    Als Beispiel dient die folgende Turtle-Grafik:
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
            Für diese Grafik werden nun drei Buttons definiert, die die Farbe der Turtle ändern. Die Buttons sind so konfiguriert, dass sie den Source Code der Turtle-Grafik ändern, wenn 
            sie geklickt werden. Die Stelle, die geändert wird, ist durch das Label `// turtle color` im Source Code markiert.
            """);

    // Buttons
    Clerk.write(Interaction.button("Red", Interaction.eventFunction("./demo.java", "// turtle color", "turtle.color(255, i * 256 / 37, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Green", Interaction.eventFunction("./demo.java", "// turtle color", "turtle.color(i * 256 / 37, 255, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Blue", Interaction.eventFunction("./demo.java", "// turtle color", "turtle.color(i * 256 / 37, i * 256 / 37, 255, 1);")));
    // Buttons

    Clerk.markdown(Text.fillOut("""
            ### Turtle mit Timeline
            Die Turtle-View bietet zudem die Möglichkeit, durch einen Timeline-Slider die einzelnen Schritte der Grafik zu durchlaufen.
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
    Die Dot-View ermöglicht das Anzeigen von [Dot Graphen](https://graphviz.org/doc/info/lang.html) im Browser. 
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
