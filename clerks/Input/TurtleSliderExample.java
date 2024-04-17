Turtle turtle = new Turtle(300,350);

void triangle(Turtle turtle, double size) {
    turtle.forward(size).right(60).backward(size).right(60).forward(size).right(60 + 180);
}

void drawing(Turtle turtle, double size) {
    for (int i = 1; i <= 36; i++) {
        turtle.color(255,i * 256 / 37, i * 256 / 37);
        turtle.lineWidth(1.0 - 1.0 / 36.0 * i);
        triangle(turtle, size + 1 - 2 * i);
        turtle.left(10).forward(10);
    }
}

drawing(turtle, 210.0/2);

Slider slider = new Slider(Clerk.view(), 10, 200);
slider.attachTo(response -> {
    double size = Double.parseDouble(response);
    turtle.reset();
    drawing(turtle, size);
});








