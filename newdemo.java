import static java.io.IO.println;

import java.util.Scanner;

void main() {
    println("Clear:~");
    println("Markdown: # Hello World!");
    println("""
            Markdown:
            ## Hello World!
            This is a simple example of a markdown block.

            ~~~
            """);
    println("""
            Text{0}:
            # Text und Pipes
            Der ${0} Command ${1} es ${0} Templates zu definieren.
            In diesen Templates können Platzhalter genutzt werden, die
            später durch Pipes mit Content befüllt werden.
            Dieser ${0} kann zum Beispiel in die Markdown View "gepiped" werden.
            ~~~
            | Markdown
            """);


    println("""
            Text: Text
            | Text{0} | Markdown
            """);

    println("""
            Text: erlaubt
            | Text{0} | Markdown
            """);

// ex1
    println("""
            Text{1}:
            # Codeblocks
            This is a codeblock example:
            ```java
            ${0}
            ```
            ~~~
            Codeblock: newdemo.java:// ex1
            | Text{1} | Markdown
            """);
// ex1

    println("Markdown: # Blocking Input");
    println("Read:");
    Scanner scanner = new Scanner(System.in);
    String d = scanner.nextLine();

    println("Markdown: Your input was: " + d);

    println("""
            Text{2}:
            init 0 200 0 25 50 0 0
            """
            +
            "color 37 255 37 1" // turtle color
            +
            """
            
            forward 25
            right 60
            backward 25
            right 60
            forward 25
            timeline
            ~~~
            | Turtle | Html
            Text{2}: ~
            | Markdown
            Text{3}:
            ```
            ${0}
            ```
            ~~~
            Text{2}: -
            | Text{3} | Markdown
            """);

    println("""
            Button:
            Text: Green
            width: 200
            height: 50
            path: newdemo.java
            label: "// turtle color"
            replacement: "color 37 255 37 1"
            ~~~
            | Html
            Button:
            Text: Red
            width: 200
            height: 50
            path: newdemo.java
            label: "// turtle color"
            replacement: "color 255 37 37 1"
            ~~~
            | Html
            """);

    int n = 55; // input
    boolean b = true; // bool
    println("""
            Input:
            path: newdemo.java
            label: "// input"
            placeholder: Enter a number
            template: int n = $;
            type: text
            ~~~
            | Html
            Checkbox:
            path: newdemo.java
            label: "// bool"
            template: boolean b = $;
            """
            +
            "checked:" + b
            +
            """

            ~~~
            | Html
            """);

    
    println("""
            Dot:
            width: 1000
            height: 600
            digraph G {
                A -> B;
                B -> C;
            }
            ~~~
            """);
}
