import lvp.Clerk;
import lvp.views.*;
import lvp.skills.*;

public class TestClass {
    public TestClass child;
    public int value;
    public TestClass(TestClass child, int value) {
        this.child = child;
        this.value = value;
    }
}

public void main() {
    Clerk.clear();
    Clerk.markdown("# Hello World"); // hello
    Clerk.write(Interaction.button("Click me", 200, 50, Interaction.eventFunction("./examples/Interactions.java", "// hello", "Clerk.markdown(\"# Goodbye World\");")));

    Clerk.markdown("## Dot Example with Object Inspector");
    TestClass t1 = new TestClass(null, 1);
    TestClass t2 = new TestClass(t1, 2);
    ObjectInspector ng = ObjectInspector.inspect(t2, "t2");
    Dot d = new Dot(1200, 500);
    d.draw(ng.toString());

    Clerk.markdown("## Interactive Turtle");
    var turtle = new Turtle(0, 300, 0, 200, 150, 25, 0);
    drawing(turtle, 100);
    turtle.write().timelineSlider();
    Clerk.markdown("### Choose a Color");
    Clerk.write(Interaction.button("Red", Interaction.eventFunction("./examples/Interactions.java", "// turtle color", "turtle.color(255, i * 256 / 37, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Green", Interaction.eventFunction("./examples/Interactions.java", "// turtle color", "turtle.color(i * 256 / 37, 255, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Blue", Interaction.eventFunction("./examples/Interactions.java", "// turtle color", "turtle.color(i * 256 / 37, i * 256 / 37, 255, 1);")));
    
    Clerk.markdown(Text.fillOut(
        """
            ## Interactive Code Blocks
            ```java
            ${0}
            ```
            """, Text.codeBlock("./examples/Interactions.java", "// drawing")
    ));
}

void triangle(Turtle turtle, double size) {
    turtle.forward(size).right(60).backward(size).right(60).forward(size).right(60 + 180);
}

// drawing
void drawing(Turtle turtle, double size) {
    for (int i = 1; i <= 36; i++) {
        turtle.color(255, i * 256 / 37, i * 256 / 37, 1); // turtle color
        turtle.width(1.0 - 1.0 / 36.0 * i);
        triangle(turtle, size + 1 - 2 * i);
        turtle.left(10).forward(10);
    }
}
// drawing
