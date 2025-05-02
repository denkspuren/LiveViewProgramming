import java.util.Scanner;

import lvp.Clerk;
import lvp.Server;
import lvp.skills.Text;
import lvp.views.MarkdownIt;
import lvp.views.Slider;
import lvp.views.Turtle;



// java --enable-preview -cp .\target\lvp-0.4.0.jar .\examples\lvpOutsideOfJshell.java
// Bei Absturz, etc bleibt kein Orphan zurück.
// Scanner könnte durch ersetzt werden, wenn bei einer neuen Verbindung alle SSE Befehele gesendet werden.
// Notebook Aspekt geht durch Wegfallen der JShell etwas verloren
// VSCode Notebook API?
// Server muss jedes mal mit Anwendung neugestartet werden


Scanner scanner = new Scanner(System.in);

void main() {
    Server s = Clerk.serve();
    
    scanner.nextLine();

    Clerk.markdown("# Hello World");
    Turtle turtle = new Turtle().left(90);
    tree(turtle, 20);

}

void tree(Turtle turtle, double size) {
    if (size < 10) {
        turtle.forward(size).backward(size);
        return;
    }
    turtle.forward(size / 3).left(30);
    tree(turtle, size * 2.0 / 3.0);
    turtle.right(30);

    turtle.forward(size / 6).right(25);
    tree(turtle, size / 2.0);
    turtle.left(25);

    turtle.forward(size / 3).right(25);
    tree(turtle, size / 2.0);
    turtle.left(25);

    turtle.forward(size / 6).backward(size);
}