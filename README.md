# Clerk: Live View Programming with Java's JShell

## Motivation: Clerk, das will ich auch haben!

In der Kalenderwoche 47/2023 bin ich per Zufall auf [Clerk](https://clerk.vision/) gestoßen. Clerk erweitert das Programmieren mit Clojure im Stil der [Notizbuch-Programmierung](https://en.wikipedia.org/wiki/Notebook_interface), wie man das z.B. von [Jupyter bzw. JupyterLab](https://jupyter.org/) her kennt. Der Witz ist jedoch: Während man in Jupyter im Browser ein webbasiertes Notizbuch für die interaktive Programmierung anlegt und mit Notizen und Code befüllt, bleibt man bei Clerk vollständig in der lieb gewordenen Entwicklungsumgebung und programmiert von dort aus eine Notizbuchsicht, die man sich im Webbrowser anschauen kann.

Auf der Clerk-Webseite läuft diese Art der Notizbuch-Programmierung unter dem Motto "Moldable Live Programming for Clojure". Die Idee von Clerk und der Umgang damit wird ausführlich in dem Dokument https://px23.clerk.vision/ beschrieben. Noch netter ist vielleicht dieses YouTube-Video, um schnell einen Zugang zu bekommen: https://youtu.be/3ANS2NTNgig

Die Idee von Clerk finde ich überaus bestechend: Das hätte ich auch gerne für die Java-Programmierung, vor allem mit der JShell. Die Idee hat das Potenzial, die Programmierausbildung mit meinen Studierenden grundlegend zu verändern.

> Ich kann mit dem Begriff _moldable_ nicht ganz soviel anfangen und ziehe es vor, die Idee des "Moldable Live Programming" eher als "Live View Programming" (LVP) zu bezeichnen.

Weil mich die Idee von Clerk derart angefixt hat, grübelte ich über eine Umsetzung nach. Nun ist die JShell nicht Clojure, man kann das nicht einfach 1:1 umsetzen. Aber mir ließ das keine Ruhe! Also habe ich mich am Samstag und Sonntag der Kalenderwoche 47 an einen Prototypen gemacht, um herauszufinden, ob ich nicht was ähnliches für die JShell mit wenigen Zeilen Code hinbekomme.

Der _Proof of Concept_ mit dem Prototyp ist geglückt! Man kann mit meiner Clerk-Variante aus der JShell heraus Markdown erzeugen, Code dokumentieren und Zeichnungen mit einer Logo-Schildkröte erstellen. Es ist schon krass cool, wenn man in der JShell mit Java-Code "nebenan" im Browser etwas hineinschreibt und Logo-Bilder entstehen. Da geht noch viel, viel mehr!

Wer mag, kann den Prototypen ausprobieren!

## Ausprobiert: Clerk für die JShell

Zum Ausprobieren muss das Java JDK 21 installiert (ich verwende das OpenJDK) und dieses Git-Repository heruntergeladen sein. Wer `git` installiert hat, kann das wie folgt machen.

```shell
git clone https://github.com/denkspuren/clerk.git
```

Da der Code mit [String Templates](https://docs.oracle.com/en/java/javase/21/language/string-templates.html) ein Preview-Feature von Java nutzt, muss die JShell im `clerk`-Ordner mit der Option `--enable-preview` aufgerufen werden. Zudem aktiviert `-R-ea` die Berücksichtigung von `assert`-Anweisungen.

```shell
jshell -R-ea --enable-preview
```

### Clerk zur interaktiven Live-View-Programmierung

Die Datei `clerk.java` wird in die JShell geladen und Clerk frisch aufgesetzt.

```
jshell> /open clerk.java

jshell> Clerk.setUp()
```

Wenn nicht aus einer vorherigen Clerk-Sitzung eine `index.html`-Datei im `clerk`-Ordner zu finden ist, wird sie hiermit angelegt. Diese Datei öffnet man in einem Browser. Im Browser kann man mitverfolgen, was passiert, wenn man `Clerk` nutzt.

> Bitte nicht wundern, wenn es bei der Darstellung im Browser zu einem Flickern kommt. Um den Prototypen einfach zu halten, habe ich das in Kauf genommen. Die Webseite aktualisiert sich automatisch alle zwei Sekunden.

Probieren wir einen einfachen Begrüßungstext im Markdown-Format:

```java
jshell> Clerk.markdown("Hello, _this_ is **Clerk**!")
```

Im Browser ist "Hello, _this_ is **Clerk**!" zu sehen. 😀

Als nächstes erzeugen wir eine kleine Logo-Zeichnung. Mehr zu Logo erfahren Sie im nächsten Abschnitt.

```java
jshell> Turtle turtle = new Turtle(200,200)
turtle ==> Turtle@5ef04b5
```

Ein Kästchen, die Zeichenfläche, von 200 x 200 Punkten ist im Browser zu sehen. In der Mitte befindet sich eine unsichtbare Schildkröte, die nach Osten ausgerichtet und mit einem Zeichenstift ausgestattet ist und die wir mit ein paar Anweisungen so umherschicken, dass schrittweise ein Quadrat entsteht.

Geben Sie nun 4x die folgende Anweisung für die Schildkröte ein. Warten Sie nach jeder Anweisung ein wenig, bis der Browser die Ansicht aktualisiert hat.

```java
turtle.forward(80).left(90)
```

Sie sollten nun ein Quadrat im Zeichenfeld sehen. Die Schildkröte blickt am Schluss ihres Wegs nach unten gen Süden. Ergänzen wir einen "Kreis", den wir aus 12 Strichen zusammensetzen.

```java
for (int i = 1; i <= 12; i++)
    turtle.right(360.0 / 12).forward(20);
```

Links unten ist nun außerdem ein kantiger "Kreis" zu sehen. 😊

![Ein Turtle-Beispiel](/Turtle/TurtleExample.png)

> Das ist also die Idee des _Live View Programming_: Man kann mit Java-Code sichtbare Effekte in der Browseransicht erzeugen. 

Das wirkt wie Spielerei und soll es auch sein. Programmieren darf Spaß machen -- und das wird befeuert, wenn man dabei etwas sehen und mit einem optischen Feedback interagieren kann.

### Clerk zur Code-Dokumentation

Obwohl der Clerk-Prototyp einfach und kurz gehalten ist, kann man noch mehr damit machen. Zum Beispiel kann man ein Notizbuch als Dokumentation zum Java-Code erzeugen -- und das alles aus der Java-Datei heraus, so wie beim originalen Clerk für Clojure.

In dem git-Repository findet sich die Datei `logo.java`. Mit der folgenden Eingabe erzeugen Sie im Browser die Dokumentation, die Sie in die Logo-Programmierung mit Clerk einführt.

```java
jshell> Clerk.refresh() // Clerk.setUp() geht auch

jshell> /o logo.java    // /o ist Kurzform von /open
```

> Ich weiß, dass Dokument im Browser sieht nicht besonders schön aus. Das ist dem aktuellen Status des Prototypen geschuldet. Auch kenne ich mich mit CSS nicht wirklich aus. Hilfe ist gerne willkommen.

Ich finde das Ergebnis ziemlich eindrucksvoll, mich begeistert das. Die Bilder werden durch die Abarbeitung in der JShell erst erzeugt. Und ich kann mir Codeauszüge an geeignete Stellen in meine Dokumentation setzen. Der Code in `logo.java` erklärt sich durch die hinzugefügte Dokumentation, den darin enthaltenen Code und dessen Ausführung sozusagen von selbst. 🚀

Um das besser zu verstehen, schauen Sie sich den Code und die Benutzung von Clerk in der Datei `logo.java` mit einem Editor Ihrer Wahl an.

> Wie ich feststellen musste, wird das Preview-Feature der String-Templates offenbar noch nicht in jedem Editor (oder von einer entsprechenden Erweiterung) richtig dargestellt. Das Syntax-Highlighting kommt durch die String-Template durcheinander und der Java-Code wird nicht sehr leserlich angezeigt.

# Skizze zur Arbeitsweise des Clerk-Prototypen

Wenn Sie sich den Inhalt der `index.html`-Datei anschauen, werden Sie vielleicht sofort verstehen, wie Clerk (siehe `clerk.java`) arbeitet und wie die Klasse `Turtle` (ebenfalls in `clerk.java`) sich Clerk zunutze macht:

Clerk nutzt zwar HTML und JavaScript im Hintergrund -- anders ist eine Browser-Ansicht nicht zu erzeugen --, aber macht das, indem die entsprechenden Clerk-Methoden die `index.html`-Datei mit HTML- bzw. JavaScript-Anteilen vollschreibt. Die `index.html`-Datei wächst mit jedem weiteren Clerk-Methodenaufruf an. Nach der Abarbeitung von `logo.java` zählt `index.html` fast 10.000 Zeilen HTML-Code.

> Der Trick: Der Kopf der `index.html`-Datei weist den Browser an, diese Datei alle zwei Sekunden neu zu lesen und die Darstellung zu aktualisieren. Wenn `index.html` mit den Clerk-Methodenaufrufen wächst und wächst entsteht der Eindruck einer Interaktion.

Der Prototyp kommt auf diese Weise ohne einen HTTP-Server aus! Das ist für einen _Proof of Concept_ akzeptabel, ist aber die entscheidende Baustelle, die man als nächstes angehen muss. Die Lösung mit einer anwachsenden `index.html` zeigt jedoch, wie inkrementelle Anteile im Wechselspiel von Browser und Server übertragen werden müssen. Mit einem HTTP-Server steigen die Möglichkeiten noch einmal: Dann kann der Client-Code aus dem Browser, Funktionalität aus dem Java/JShell-Programm abrufen -- damit sind dann interaktive Anwendungen möglich.

> Wenn Sie bei der Umsetzung eines HTTP-Servers einspringen, mithelfen und mitdenken wollen: Meine Idealvorstellung ist, dass Clerk ohne Abhängigkeiten von anderen Libraries auskommt und sich nur der Boardmittel des JDK bedient. In dem API `com.sun.net.httpserver` sind drei neue Klassen dazu gekommen, die eine Realisierung vielleicht etwas einfacher machen, siehe [JEP 408](https://openjdk.org/jeps/408).

# Anmerkungen

Meine Vision ist, Clerk in der Programmierausbildung meiner Informatik-Studierenden an der THM zum Einsatz kommen zu lassen. Wenn einmal ein HTTP-Server realisiert ist, wird Clerk ein schönes Beispiel für webbasierte Server-Client-Programmierung abgeben und es kann in seinen Fähigkeiten kontinuierlich erweitert werden. Mit Clerk wäre damit auch ein Rahmenwerk gegeben für die Programmierung von Web-Anwendungen.

Besonders geignet scheint mir Clerk aber auch für Programmier-Anfänger:innen zu sein: Es macht vermutlich mehr Sinn und Spaß, wenn man Schleifen-Konstrukte daran erprobt, indem man Logo-Zeichnungen generiert. Gerne würde ich auch Clerk erweitern um die Möglichkeit, automatisiert ein Objektdiagramm zu einer gegebenen Objektreferenz zu erzeugen -- das geht mit dem Java-Reflection-API und z.B. [Graphviz-Online](https://dreampuf.github.io/GraphvizOnline). Clerk kann also dabei helfen, den zur Laufzeit entstandenen Graphen aus Objekten und Referenzen zu verstehen.

Ein paar weitere Notizen:

* So behelfsmäßig mein Prototyp mit `index.html` und einem kontinuierlichen Browserrefresh arbeitet: Dennoch könnte das Beschreiben und Erweitern z.B. einer Dokumentationsdatei z.B. im Markdown-Format genau auf diese Weise erfolgen. Dafür braucht es keinen HTTP-Server.

> So weit so gut. Sie sind gerne willkommen, sich an der Entwicklung der Clerk-Idee, eines Live View Programming with Java's JShell, zu beteiligen.

Herzlichst,<br>
Dominikus Herzberg


