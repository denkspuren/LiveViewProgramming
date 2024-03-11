# Clerk: Live View Programming with Java's JShell

Ich stelle Ihnen hier Clerk, die daraus hervorgegangene Idee des _Live View Programming_ und einen Clerk-Prototypen für die JShell vor. Wenn Sie Gefallen an der Idee und dem Projekt finden: Ganz unten gibt es einen Mitmach-Aufruf und Vorschläge, woran man arbeiten und worüber man nachdenken könnte.

## 💟 Motivation: Clerk, das will ich auch haben!

In der Kalenderwoche 47/2023 bin ich per Zufall auf [Clerk](https://clerk.vision/) gestoßen, die Inspiration der hier umgesetzten Variante von Clerk. Das originale Clerk erweitert das Programmieren mit Clojure im Stil der [Notizbuch-Programmierung](https://en.wikipedia.org/wiki/Notebook_interface), wie man das z.B. von [Jupyter bzw. JupyterLab](https://jupyter.org/) her kennt. Der Witz ist jedoch: Während man bei Jupyter im Browser ein webbasiertes Notizbuch für die interaktive Programmierung anlegt und mit Notizen und Code befüllt, bleibt man bei Clerk vollständig in der vertrauten Entwicklungsumgebung und programmiert von dort aus eine Notizbuchansicht, die man sich im Webbrowser anschauen kann.

Auf der originalen Clerk-Webseite läuft diese Art der Notizbuch-Programmierung unter dem Motto "Moldable Live Programming for Clojure". Die Idee von Clerk und der Umgang damit werden ausführlich in dem Dokument https://px23.clerk.vision/ beschrieben. Noch netter ist vielleicht dieses YouTube-Video, um schnell einen Zugang zu bekommen: https://youtu.be/3ANS2NTNgig

> Ich kann mit dem Begriff _moldable_ nicht ganz soviel anfangen und ziehe es vor, die Idee des "Moldable Live Programming" eher als "Live View Programming" (LVP) zu bezeichnen.

Die Idee von Clerk finde ich überaus bestechend. Sie hat mich derart angefixt, das hätte ich auch gerne für die Java-Programmierung, vor allem mit der JShell. Ich sehe das Potenzial, wie Clerk die Programmierausbildung mit meinen Studierenden grundlegend verändern kann.

Ich grübelte über eine Umsetzung nach. Nun ist die JShell nicht Clojure, man kann das nicht einfach 1:1 umsetzen. Und so entstand der Ansatz, Clerk auf die Essenz zu reduzieren und mit Clerk lediglich "Ausgaben" im Browser zu erzeugen. Man programmiert mit Clerk also _live_ ein Notizbuch -- darum _Live View Programming_.

Nach einem _Proof of Concept_ ([hier](https://github.com/denkspuren/clerk/releases/tag/0.1.0)) ist mit der Hilfe und Unterstützung von @RamonDevPrivate (mittlerweile Co-Entwickler in diesem Repo 💪) eine erste Umsetzung mit einem Webserver entstanden! Man kann mit dieser Clerk-Variante aus der JShell heraus Markdown erzeugen, Abschnitte aus Code- und Textdateien herausschneiden und Zeichnungen mit einer Logo-Schildkröte erstellen. Insbesondere das Herausschneiden von Text- bzw. Codeabschnitten aus Dateien und das neue Java-Feature der String-Template (Preview-Feature in Java 21) sind sehr einfache aber mächtige Instrumente zur Code-Dokumentation und zur Unterstützung des [_Literate Programming_](https://en.wikipedia.org/wiki/Literate_programming).

Es ist schon krass cool, wenn man in der JShell mit Java-Code "nebenan" im Browser etwas hineinschreibt und Logo-Bilder entstehen. Da geht noch viel, viel mehr!

Wer mag, kann den entstandenen Prototypen ausprobieren!

## 💻 Ausprobiert: Clerk für die JShell

Zum Ausprobieren muss das Java JDK 21 installiert (ich verwende das OpenJDK) und dieses Git-Repository heruntergeladen sein. Wer `git` installiert hat, kann das wie folgt machen.

```shell
git clone https://github.com/denkspuren/clerk.git
```

Da der Code mit [String Templates](https://docs.oracle.com/en/java/javase/21/language/string-templates.html) ein Preview-Feature von Java nutzt, muss die JShell im `clerk`-Ordner mit der Option `--enable-preview` aufgerufen werden. Zudem aktiviert `-R-ea` die Berücksichtigung von `assert`-Anweisungen.

```shell
jshell -R-ea --enable-preview
```

### 🎹 Clerk zur interaktiven Live-View-Programmierung

Die Datei `clerk.java` wird in die JShell geladen und Clerk gestartet.

```
jshell> /open clerk.java

jshell> Clerk.serve()
Open http://localhost:50001 in your browser
```

Öffnen Sie Ihren Browser (bei mir ist es Chrome) mit dieser Webadresse. Wenn es ein anderer Port sein soll, lautet der Aufruf beispielsweise `Clerk.serve(50000)`. Im Browser kann man mitverfolgen, was passiert, wenn man Clerk nutzt. 

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

Geben Sie nun 4x die folgende Anweisung für die Schildkröte ein.

```java
turtle.forward(80).left(90); // 4x eingeben
```

Sie sollten nun ein Quadrat im Zeichenfeld sehen. Die Schildkröte blickt am Schluss ihres Wegs wieder gen Osten. Ergänzen wir einen "Kreis", den wir aus 12 Strichen zusammensetzen.

```java
for (int i = 1; i <= 12; i++)
    turtle.right(360.0 / 12).forward(20);
```

Links unten ist nun außerdem ein kantiger "Kreis" zu sehen. 😊

![Ein Turtle-Beispiel](/Turtle/TurtleExample.png)

> Das ist also die Idee des _Live View Programming_: Man kann mit Java-Code sichtbare Effekte in der Browseransicht erzeugen. 

Das wirkt wie Spielerei und soll es auch sein. Programmieren darf Spaß machen -- und das wird befeuert, wenn man dabei etwas sehen und mit einem optischen Feedback interagieren kann. Die Nutzungsmöglichkeiten von Clerk gehen jedoch über die "Spielerei" hinaus.

### 📄 Live View Programming zur Dokumentation

Obwohl der Clerk-Prototyp einfach und kurz gehalten ist, kann man noch mehr damit machen. Zum Beispiel kann man ein Notizbuch als Dokumentation zum Java-Code erzeugen -- und das alles aus der Java-Datei heraus in der man programmiert, so wie beim originalen Clerk für Clojure.

In dem git-Repository findet sich die Datei [`logo.java`](/logo.java). Mit der folgenden Eingabe erzeugen Sie im Browser die Dokumentation, die Sie in die Logo-Programmierung mit Clerk einführt.

```java
jshell> Clerk.serve() // Browser refreshen, um leere Seite zu sehen

jshell> /o logo.java  // /o ist Kurzform von /open
```

Im Browser sieht das Ergebnis so aus (Sie sehen hier nur einen Teil der Seite):

![Das Ergebnis von `logo.java`](logo.png)

Ich finde das Ergebnis ziemlich eindrucksvoll, mich begeistert das. Die Bilder werden durch die Abarbeitung in der JShell erst erzeugt. Und ich kann Codeauszüge an geeigneten Stellen in die Dokumentation setzen. Der Code in [`logo.java`](/logo.java) erklärt sich durch die hinzugefügte Dokumentation, den darin enthaltenen Code und dessen Ausführung sozusagen von selbst.

Um das besser zu verstehen, schauen Sie sich den Code und die Benutzung von Clerk in der Datei [`logo.java`](/logo.java) mit einem Editor Ihrer Wahl an.

> Offenbar wird das Java-Preview-Feature der String-Templates offenbar noch nicht in jedem Editor (oder von einer entsprechenden Erweiterung) richtig dargestellt. Das Syntax-Highlighting kommt durch die String-Templates möglicherweise durcheinander und der Java-Code wird eventuell nicht sehr leserlich angezeigt.

# 📝 Skizze zur Arbeitsweise des Clerk-Prototypen

Wenn Sie sich die Datei [`clerk.java`](/clerk.java) anschauen, werden Sie feststellen, dass nicht viel Code erforderlich ist:

* Die Klasse `LiveView` setzt mit den Boardmitteln von Java einen Webserver mit [Server Sent Events](https://en.wikipedia.org/wiki/Server-sent_events) (SSE) auf.
* Die Klasse `Clerk` aktiviert den Server mit der Methode `serve` und schickt HTML-Code mit der Methode `write` an den Client (den Browser). Mit der Methode `script` wird JavaScript-Code und mit `markdown` Text in Markdown-Syntax an den Browser geschickt. Mit der Methode `cutOut` kann man markierte Textabschnitte aus einer Datei ausschneiden.
* Die Klasse `Turtle` erlaubt die Verwendung der Turtle-Implementierung [`turtle.js`](/Turtle/turtle.js) durch Java. Die verschiedenen Turtle-Methoden rufen im Browser ihre Entsprechungen in `turtle.js` auf 

In der Datei [`logo.java`](/logo.java) sehen Sie ein Beispiel der Verwendung dieser wenigen grundlegenden Fähigkeiten von Clerk. Das Beispiel zeigt, wie Sie mit Java-Code eine Dokumentation des eigenen Programms erstellen können, das zudem beispielhaft seine Verwendung erläutert und zeigt.

# 🚀 Der Prototyp ist erst der Anfang

## 🌴 Vision

Meine Vision ist, Clerk in der Programmierausbildung meiner Informatik-Studierenden an der THM zum Einsatz kommen zu lassen. Wenn einmal ein HTTP-Server realisiert ist, wird Clerk ein schönes Beispiel für webbasierte Client/Server-Programmierung abgeben, und es kann in seinen Fähigkeiten kontinuierlich erweitert werden. Mit Clerk wäre damit auch ein Rahmenwerk gegeben für die Programmierung von Web-Anwendungen. Generell ist der hier vorgestellte Ansatz für jede andere Programmiersprache ebenso umsetzbar.

Zum einen scheint mir Clerk für Programmier-Anfänger:innen geeignet zu sein: Es macht vermutlich mehr Sinn und Spaß, wenn man Schleifen-Konstrukte erlernt, indem man Logo-Zeichnungen generiert. Gerne würde ich Clerk erweitern um die Möglichkeit, automatisiert ein Objektdiagramm zu einer gegebenen Objektreferenz zu erzeugen -- das geht mit dem Java-Reflection-API und z.B. [Graphviz-Online](https://dreampuf.github.io/GraphvizOnline); @RamonDevPrivate hat das bereits mit diesem [Gist](https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee) vorbereitet. Clerk kann also dabei helfen, den zur Laufzeit entstandenen Graphen aus Objekten und Referenzen zu verstehen. Mit solchen Erweiterungen kann Clerk Teil der Entwicklungswerkzeuge beim Programmieren werden.

Zum anderen können auch erfahrene Entwickler:innen mit Clerk eine anschauliche und verständliche Dokumentation zu ihrem Code erstellen. Wenn visuelle Anteile das unterstützen können, umso besser. Man kann Clerk aber ebenso für Experimente, exploratives Programmieren und Notebook-basierte Programmierung verwenden. Sicher gibt es noch viele andere, denkbare Anwendungsszenarien.

## 💃🕺 Mitmach-Aufruf

> Sie sind gerne willkommen, sich an der Entwicklung der Clerk-Idee, eines _Live View Programming with Java's JShell_, zu beteiligen.

Dazu ein paar Punkte, die mir in den Sinn kommen:

* Ich habe wenig Ahnung von Web-Technologien, d.h. von HTML, CSS und JavaScript, z.B. hat ChatGPT 3.5 den Code für `turtle.js` beigesteuert. Mag jemand ein CSS beitragen, damit der Prototyp besser aussieht? Macht es Sinn, das z.B. mit einem Framework wie [Bootstrap](https://getbootstrap.com/) zu tun, Stichwort "Responsive Design"? -- Vielen Dank an [ginschel](https://github.com/ginschel) für einen ersten [CSS-Vorschlag](https://github.com/denkspuren/clerk/pull/5), der [hier](proposals/CSSExample/) zu finden ist!

* Wie könnte man z.B. eine Bibliothek wie `https://www.chartjs.org/` in Clerk einbinden? Das würde die Einsatzmöglichkeiten für Clerk bereichern.

* Sobald es Clerk mit einem HTTP-Server gibt, wäre eine interaktive Anwendung eine schöne Vorzeige-Demo. Wie wäre es mit Tic-Tac-Toe? Natürlich soll im Browser nur das Spielbrett dargestellt und das UI abgebildet werden, die Berechnung von Spielzügen etc. findet javaseitig statt. Dafür wird man Clerk ein wenig erweitern müssen.

* Der Einsatz von Clerk könnte auch sinnvoll ohne Browser sein, um eine Dokumentation in einer Dokumentationsdatei etwa im Markdown-Format vorzunehmen. Dafür braucht es keinen HTTP-Server. Wenn zudem der Browser verwendet wird, könnte Clerk Medien auslesen (z.B. eine erzeugte Turtle-Grafik als Bild exporteiren), abspeichern und in eine Dokumentation einfügen.

* Tatsächlich wäre ein Object-Inspektor, der über Reflection ein Object-Diagramm z.B. mit Hilfe von Graphviz erzeugt, eine großartige Sache. Das ist aber ein Problem für sich und kann, wenn gelöst, in Clerk als Dienst eingearbeitet werden.

Weitere Überlegungen zur Überarbeitung des aktuellen Prototypen sind unter [Considerations.md](Considerations.md) zu finden.

Wie man Clerk modular gestalten könnte zum Zwecke der Erweiterung, ob man es doch als `jar`-Datei ausliefern sollte, ... diesen Fragen kann man sich widmen, wenn der Prototyp reift und mit einem HTTP-Server ausgestattet ist.

## 🙏 Dank für Beiträge

[@kuchenkruste](https://github.com/kuchenkruste) ist von Clerk ebenso angefixt wie ich und hat spontan einen beeindruckenden Server-Entwurf im Verzeichnis [`proposals/`](/proposals) [beigesteuert](https://github.com/denkspuren/clerk/pull/2#issue-2019021681), der Websockets realisiert; die `pom.xml`-Datei (in `proposals`) hilft beim Build mit Maven. Vielen Dank dafür! Ich habe mich vorerst dennoch für eine einfachere Lösung entschieden, einen Webserver mit Server Sent Events.
 
@RamonDevPrivate hat mit diesem [Gist](https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee) einen ObjectInspector auf den Weg gebracht, der ebenso Teil von Clerk werden wird. Auch dafür einen großen Dank! Ramon ist auch Mitentwickler von Clerk geworden, der vor allem den Webserver mit den Server Sent Events auf den Weg gebracht hat.

Herzlichst,<br>
Dominikus Herzberg


