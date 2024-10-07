Clerk.markdown(
    Text.fillOut(
"""
# Turtle-Programmierung

_Dominikus Herzberg_, _Technische Hochschule Mittelhessen_

Bei der Programmiersprache [Logo](https://de.wikipedia.org/wiki/Logo_(Programmiersprache)) steht eine Schildkröte (_turtle_) im Mittelpunkt – und zwar im wahrsten Sinne des Wortes. Auf einer weißen Fläche ist in der Mitte die Schildkröte platziert. An ihr ist ein Stift befestigt und sie ist zu Beginn nach rechts ausgerichtet, sie blickt Richtung Osten.

Die Schildkröte kennt die folgenden Kommandos:

Befehl | Bedeutung
-------|----------
`penDown()` | Setze den Stift auf die Zeichenfläche (Anfangseinstellung)
`penUp()`   | Hebe den Stift von der Zeichenfläche ab
`forward(double distance)`  | Bewege dich um _distance_ vorwärts
`backward(double distance)` | Bewege dich um _distance_ rückwärts 
`right(double degrees)`     | Drehe dich um die Gradzahl _degrees_ nach rechts
`left(double degrees)`      | Drehe dich um die Gradzahl _degrees_ nach links
`color(int red, int green, int blue)` | Setze Stiftfarbe mit den RGB-Farbanteilen _red_, _green_ und _blue_
`color(int rgb)`            | Setze Stiftfarbe auf den kodierten RGB-Farbwert _rgb_
`lineWidth(double width)`   | Setze Stiftbreite auf _width_
`text(String text, Font font, double size, Font.Align align)` | Schreibe Text vor deinen Kopf mit Angabe des Text-Fonts, der Größe und der Ausrichtung
`text(String text)` | Schreibe Text vor deinen Kopf
`reset()`                   | Lösche Zeichenfläche, gehe zurück in Bildmitte


Mit diesen Kommandos wird die Schildkröte über die Zeichenfläche geschickt und das Zeichnen gesteuert. Wenn man Abfolgen von diesen Kommandos programmiert, kann man teils mit sehr wenig Code interessante Zeichnungen erstellen.

> Wenn man die Befehle in der JShell zur Verfügung hat, benötigt man kein weiteres Wissen zu Logo. Man kann mit den Sprachkonstrukten von Java arbeiten.

## Beispiel 1: Ein Quadrat aus Pfeilen

Mit `new Turtle(300,300)` wird eine neue Schildkröte mittig auf eine Zeichenfläche der angegebenen Größe (Breite, Höhe) gesetzt. In den Grundeinstellungen sind die Breite und die Höhe auf 500 gesetzt. 

Die folgende Logo-Anwendung demonstriert, wie man mittels Methoden schrittweise graphische Einheiten erstellen und zusammensetzen kann.

```java
${0}
```

Das Ergebnis sieht dann so aus: ein Quadrat aus Pfeilen, wobei absichtlich kleine Zwischenräume gelassen wurden, mit Angaben der Pfeilausrichtung.
""", Text.cutOut("./logo.java", "// myFirstTurtle")));

// myFirstTurtle
Turtle myFirstTurtle = new Turtle(300, 300);

Turtle arrowhead(Turtle t) {
    return t.right(30).backward(10).forward(10).
             left(60).backward(10).forward(10).right(30);
}
Turtle arrow(Turtle t, double length) {
    return arrowhead(t.forward(length));
}
Turtle edge(Turtle t, double length, double space) {
    return arrow(t, length).penUp().forward(space).penDown();
}
Turtle write(Turtle t, String text) { 
    return t.penUp().forward(10).text(text).backward(10).penDown();
}
myFirstTurtle = edge(myFirstTurtle, 100, 5);
myFirstTurtle = write(myFirstTurtle, "East").right(90);
myFirstTurtle = edge(myFirstTurtle, 100, 5);
myFirstTurtle = write(myFirstTurtle, "South").right(90);
myFirstTurtle = edge(myFirstTurtle, 100, 5);
myFirstTurtle = write(myFirstTurtle, "West").right(90);
myFirstTurtle = edge(myFirstTurtle, 100, 5);
myFirstTurtle = write(myFirstTurtle, "North").right(90);
// myFirstTurtle

Clerk.markdown(
    Text.fillOut(
"""
## Beispiel 2: Umsetzung eines Logo-Programms in Java

Die Programmiersprache Logo ist nicht so schwer zu verstehen, wie das nachstehende Beispiel zeigt, das von dieser [Webseite](https://calormen.com/jslogo/) stammt. Auch wenn man kein Logo spricht, der Code ist leicht in Java umzusetzen.

```logo
TO tree :size
   if :size < 5 [forward :size back :size stop]
   forward :size/3
   left 30 tree :size*2/3 right 30
   forward :size/6
   right 25 tree :size/2 left 25
   forward :size/3
   right 25 tree :size/2 left 25
   forward :size/6
   back :size
END
clearscreen
tree 150
```

Die Java-Methode `tree` bildet das obige Logo-Programm nach; lediglich aus praktischen Überlegungen lasse ich den Rekursionsabbruch etwas früher greifen.

```java
${turtle_tree}
```

Der Aufruf der Methode `tree` erzeugt etwas, was einem "Baum" ähnelt.

```java
${tree}
```

""", Map.of("turtle_tree", Text.cutOut("./logo.java", "// turtle tree"),
            "tree", Text.cutOut("./logo.java", "// tree"))));

// turtle tree
Turtle turtle = new Turtle().left(90);

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
// turtle tree

// tree
tree(turtle, 150);
// tree

Clerk.markdown(
    Text.fillOut(
"""
## Beispiel 3: Es kommt Farbe ins Spiel

Mit Farbe wird die Welt bunter und interessanter, und die Strichstärke kann man ebenfalls für Effekte einsetzen. Im nachfolgenden Beispiel verblasst die Farbe zunehmend und die Strichstärke lässt allmählich nach.

```java
${0}
```
""", Text.cutOut("./logo.java", "// triangles")));

// triangles
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

drawing(turtle, 100);
// triangles

Clerk.markdown(Text.fillOut(
"""
## Beispiel 4: Interaktivität mit Slider (Preview-Feature, _unstable_)

Es ist auch möglich, eine Turtle-Grafik mit einer Slider-View zu koppeln – und es entsteht eine interaktive Anwendung.

```java
${0}
```

Das macht noch mehr Spaß! Die Zeichnungen werden auf Seiten des Java-Programms mit jeder Änderung am Slider neu erzeugt.

""", Text.cutOut("./logo.java", "// interactivity")));

// interactivity
turtle = new Turtle(300, 350);

drawing(turtle, (200.0 + 10.0) / 2.0);

Slider slider = new Slider(Clerk.view(), 10, 200);
slider.attachTo(response -> {
    double size = Double.parseDouble(response);
    turtle.reset();
    drawing(turtle, size);
});
// interactivity


Clerk.markdown("""
Soviel möge als Demo vorerst genügen! _More features to come_ 😉
""");
