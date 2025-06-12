import static java.io.IO.println;

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

    println("""
            Text{2}:
            init 0 200 0 25 50 0 0
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
