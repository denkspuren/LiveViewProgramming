import lvp.Clerk;
import lvp.views.*;
import lvp.skills.*;
import lvp.skills.NodeGenerator;

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
    Clerk.write(Interaction.button("Click me", 200, 50, Interaction.eventFunction("// hello", "Clerk.markdown(\"# Goodbye World\");")));

    TestClass t1 = new TestClass(null, 1);
    TestClass t2 = new TestClass(t1, 2);
    NodeGenerator ng = NodeGenerator.inspect(t2, "t2");
    Dot d = new Dot(1200, 500);
    d.draw(ng.toString());

    var turtle = new Turtle(0, 300, 0, 200, 150, 25, 0);
    drawing(turtle, 100);
    turtle.write().timelineSlider();

    Clerk.write(Interaction.button("Red", Interaction.eventFunction("// turtle color", "turtle.color(255, i * 256 / 37, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Green", Interaction.eventFunction("// turtle color", "turtle.color(i * 256 / 37, 255, i * 256 / 37, 1);")));
    Clerk.write(Interaction.button("Blue", Interaction.eventFunction("// turtle color", "turtle.color(i * 256 / 37, i * 256 / 37, 255, 1);")));
}

void triangle(Turtle turtle, double size) {
    turtle.forward(size).right(60).backward(size).right(60).forward(size).right(60 + 180);
}

void drawing(Turtle turtle, double size) {
    for (int i = 1; i <= 36; i++) {
        turtle.color(255, i * 256 / 37, i * 256 / 37, 1); // turtle color
        turtle.width(1.0 - 1.0 / 36.0 * i);
        triangle(turtle, size + 1 - 2 * i);
        turtle.left(10).forward(10);
    }
}
