# Live View Programming mit Java und der JShell

Das _Live View Programming_ (LVP) bietet Ihnen für die Programmierung mit Java _Views_ an, die im Web-Browser dargestellt werden. Mit dem Browser steht Ihnen ein mächtiges Ausgabegerät für Text, Bilder, Grafiken, Videos, interaktive Animationen etc. zur Verfügung. Auf genau diese Browser-Fähigkeiten greift das _Live View Programming_ zurück.

Außerdem gibt es noch sogenannte Skills. Skills stellen nützliche LVP-Fähigkeiten bereit, z.B. zur Dokumentation von Code, die mit Views zusammenarbeiten oder mit Ihnen kombiniert genutzt werden.

Sie können, wenn Sie wollen, das LVP um eigene Views und Skills erweitern. Generell gilt: Wenn Sie Gefallen an der Idee und dem Projekt finden, dann können Sie sich gerne an der Entwicklung beteiligen und neue Views und Skills beisteuern.

## 💟 Motivation

Für Programmieranfänger:innen ist es eine echte Herausforderung für das Verständnis in Programmabläufe, wenn man einzig über eingestreute `System.out.println`-Aufrufe Einblicke in die Ausführung eines kompilierten Programms erhält. Ein Debugger ist für Anfängerinnen und Anfänger keine wirkliche Alternative. Verwendet man zum Einstieg in die Programmierung hingegen die JShell, so kommt man praktisch ohne `println` aus. In der JShell interagiert man direkt mit Javas Sprach- und Datenkonstrukten und kann Programme inkrementell aufbauen und erkunden. Dennoch fehlen zusätzliche Ausgabe- und Interaktionsmöglichkeiten, die den Programmiereinstieg interessant und die Effekte und Auswirkungen von Programmabläufen "sichtbar" machen. Hier kommt das _Live View Programming_ ins Spiel.

Das _Live View Programming_ versteht sich als ein Angebot, in ein bestehendes Programm _Views_ einzubauen und zu verwenden, die im Web-Browser angezeigt werden. Es macht nicht nur Spaß, wenn man zum Beispiel Grafiken im Browser erzeugen kann -- man sieht auch die Programmierfehler, die einem unterlaufen. Wenn man etwa in der Turtle-View eine Schildkröte mit einem Stift über die Zeichenfläche schickt, zeigt sich unmittelbar, ob man Wiederholungen über Schleifen richtig aufgesetzt oder die Rekursion korrekt umgesetzt hat. Die visuelle Repräsentation gibt über das Auge eine direkte Rückmeldung. Feedback motiviert und hilft beim Verständnis.

Für Fortgeschrittene kommen andere Aspekte hinzu, die von einer Visualisierung profitieren. Zum Beispiel lassen sich Datenstrukturen sehr gut mit Hilfe von Graphen darstellen. Mit der Dot-View können Graphen, die in der [DOT-Sprache](https://de.wikipedia.org/wiki/DOT_(Graphviz)) beschrieben sind, im Browser gerendert werden. Die Dot-View wird beispielsweise von dem ObjectInspector genutzt, einer Skill, die das LVP bereitstellt. Der `ObjectInspector` bekommt ein Java-Objekt übergeben, reflektiert das Objekt und erstellt ein Objektdiagramm, das sich von diesem Objekt über die Referenzen zu anderen Objekten ergibt. Das Objektdiagramm wird in der Dot-Sprache beschrieben und mit Hilfe der Dot-View zur Anzeige gebracht. Das ist eine sehr hilfreiche, niederschwellige und visuelle Form der Objekt-Introspektion.

Mit LVP kann man jedoch noch sehr viel mehr machen. Mit der Markdown-View kann man Markdown-Texte im Browser anzeigen. Zusammen mit der Text-Skill können beispielsweise Code-Abschnitte aus einer Java-Datei ausgeschnitten und im Markdown-Text in einem Code-Block eingefügt werden. Man kann also ein Java-Programm erstellen, das seine eigene Dokumentation in Markdown enthält, wobei in der Dokumentation angezeigte Code-Fragmente immer aktuell sind. Das ist eine besondere Form des [_Literate Programming_](https://en.wikipedia.org/wiki/Literate_programming).

Das LVP kann man auch dafür einsetzen, um Anwendungen zu schreiben. Die Views werden zusammen mit interaktiven Elementen, wie Buttons, Slidern etc. als graphische Oberfläche verstanden und ausgelegt. Damit lassen sich auch Konzeptstudien und Prototypen entwickeln.

In der Lehre kombiniere ich beides, die Anwendungsentwicklung mit der Code-Dokumentation. Das Ergebnis sind Java-Programme, die eine Anwendung umsetzen _und_ gleichzeitig Anwendungs- und Programmdokumentation sind. Man kann sich also sicher sein, dass der dokumentierte Code genau der Code ist, der auch die Anwendung realisiert. 

## 🐣 Ursprünge: Das will ich auch haben!

Wer in Python programmiert, hat meist schon von der [Notizbuch-Programmierung](https://en.wikipedia.org/wiki/Notebook_interface) mit [Jupyter bzw. JupyterLab](https://jupyter.org/) gehört oder sie sogar schon genutzt. Man programmiert direkt im Browser, wo eine Notizbuch-Umgebung über einen Server bereitgestellt wird. Das Notizbuch ermöglicht die Kombination von Programmcode und Dokumentation in beliebiger Abfolge, wobei die Programmentwicklung inkrementell und explorativ, d.h. in kleinen Schritten und im Stil einer Erkundung verläuft. Das Notizbuch zeigt Zwischenausgaben der Programmausführung an und Datenplots und andere visuelle und teils interaktive Darstellungen können erzeugt und eingebunden werden. Die Notizbuch-Programmierung ist z.B. in den Datenwissenschaften, im Quantencomputing und in der KI-Entwicklung weit verbreitet.[^1]

[^1]: Wer einen Eindruck von der Notizbuch-Programmierung gewinnen möchte, kann sich z.B. meinen [Simulator für Quantenschaltungen](https://github.com/denkspuren/qcsim/blob/main/qcsim-dev.ipynb) anschauen.

Als ich eine besondere Variante der Notizbuch-Programmierung namens Clerk für die Programmiersprache Clojure entdeckte, war es um mich geschehen: Statt im Browser, d.h. im Notizbuch zu programmieren, bleibt man bei Clerk in der gewohnten Entwicklungsumgebung, und die Browseransicht wird währenddessen automatisch und live generiert. Diese Art des Programmierens bezeichnen die Entwickler von Clerk als _Moldable Live Programming_, mehr Infos dazu finden sich unter https://clerk.vision/.

Clerk für Clojure ist ein mächtiges und eindrucksvolles Werkzeug -- Hut ab vor den Entwicklern. Was mich an diesem Ansatz so fasziniert, ist die hinter dieser Mächtigkeit liegende grundlegende Idee: Es genügt ein simpler Webserver, den man programmierend ansteuern und erweitern kann, um im Browser Inhalte, gegebenenfalls sogar interaktive Inhalte anzeigen zu können. Damit kann man einen einfachen Satz an Darstellungsmöglichkeiten für Programmieranfänger:innen bereitstellen. Und erfahrene Programmierer:innen können eigene Erweiterungen für ihre Zwecke entwickeln.

Diese Grundidee wollte ich so einfach und unkompliziert wie möglich für Java und die JShell umsetzen. Das, was daraus entstanden ist, nenne ich _Live View Programming_ (LVP). Eingedenk der Inspirationsquelle habe ich das Interface, das es ermöglicht verschiedenene _Views_ mit einer _Live View_ (einem Tab im Browser) kommunizieren zu lassen, als _Clerk_ bezeichnet (engl. für Sachbearbeiter, Büroangestellter, Schreibkraft). Jede _View_ bietet eigene Möglichkeiten, unterschiedliche Inhalte in einer _Live View_ darzustellen. Dazu kommen _Skills_, die generelle Fähigkeiten beisteuern, die nicht an eine _Live View_ gebunden sind.

Das _Live View Programming_ mit seinen Views und Skills ist mit einem sehr schlanken _Live View_-Webserver umgesetzt. Es braucht nur wenige Mittel, um damit eine Notizbuch-Programmierung umzusetzen. Aber es geht noch viel mehr, wie ich es anfangs beschrieben habe! Ein Beispiel ist das [Literate Programming](https://en.wikipedia.org/wiki/Literate_programming), das ganz andere Wege bei der Kombination von Code und Dokumentation geht. Ein anderes Beispiel ist eine View für [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik), was zur Grafik-Programmierung animiert. Ein weiteres Beispiel ist eine View, die eine GUI für das Spiel [TicTacToe](https://de.wikipedia.org/wiki/Tic-Tac-Toe) bereitstellt. In all diesen Beispielen programmiert man wie gewohnt mit Java in der IDE oder mittels JShell-Skripten und einem Editor und instruiert den Browser, was er anzeigen soll. Das ist -- ehrlich gesagt -- ziemlich cool!

## 💻 _Live View Programming_ für die JShell

Zum Ausprobieren muss ein aktuelles Java JDK (ich verwende das OpenJDK) installiert und dieses Git-Repository heruntergeladen sein. Wer `git` installiert hat, kann das wie folgt machen.

```
git clone https://github.com/denkspuren/LiveViewProgramming.git
```

Danach wechselt man in das Verzeichnis und startet die JShell:

```
jshell
```
<!-- Zudem aktiviert `-R-ea` die Berücksichtigung von `assert`-Anweisungen. -->

### 🎹 Ein Beispiel zur Live-View-Programmierung

Die Datei `lvp.java` (kurz für _Live View Programming_) wird in die JShell geladen und der Server für die _Live View_ gestartet.

```
jshell> /open lvp.java

jshell> Clerk.view()
Open http://localhost:50001 in your browser
$38 ==> LiveView@2d38eb89
```

Öffnen Sie Ihren Browser (bei mir ist es Chrome) mit dieser Webadresse. Im Browser kann man nun mitverfolgen, was passiert, wenn man die _Live View_ nutzt. 

Probieren wir einen einfachen Begrüßungstext im Markdown-Format:

```java
jshell> Clerk.markdown("Hello, _this_ is **Live View Programming** in action!")
```

Im Browser ist "Hello, _this_ is **Live View Programming** in action!" zu sehen. 😀

Als nächstes erzeugen wir eine kleine _Turtle_-Grafik. Die Idee, eine Grafik mit einer Schildkröte (_turtle_) zu programmieren, hat die Programmiersprache Logo in die Welt getragen.

```java
jshell> Turtle turtle = new Turtle(200, 200)
turtle ==> Turtle@3b764bce
```

Ein Kästchen, die Zeichenfläche, von 200 x 200 Punkten ist im Browser zu sehen. In der Mitte befindet sich eine unsichtbare Schildkröte, die nach Osten ausgerichtet und mit einem Zeichenstift ausgestattet ist und die wir mit ein paar Anweisungen so umherschicken, dass schrittweise ein Quadrat entsteht.

Geben Sie die folgende Anweisung vier Mal für die Schildkröte ein.

```java
turtle.forward(80).left(90);
turtle.forward(80).left(90);
turtle.forward(80).left(90);
turtle.forward(80).left(90);
```

Sie sollten nun ein Quadrat im Zeichenfeld sehen. Die Schildkröte blickt am Schluss ihres Wegs wieder gen Osten. Ergänzen wir einen "Kreis", den wir aus 12 Strichen zusammensetzen.

```java
for (int i = 1; i <= 12; i++)
    turtle.right(360.0 / 12).forward(20);
```

Links unten ist nun außerdem ein kantiger "Kreis" zu sehen. 😊

![Ein Turtle-Beispiel](/views/Turtle/TurtleExample.png)

> Das ist also die Idee des _Live View Programming_: Man kann mit Java-Code sichtbare Effekte in der Browseransicht erzeugen.

Es ist zudem möglich, Views interaktiv zu koppeln. In der Animation sieht man, wie eine _Turtle_-Grafik mit einem _Slider_ verbunden ist.

![Interaktives Beispiel: Slider gekoppelt mit Turtle-Grafik](/docs/SliderAndTurtle.gif)

So macht das Programmieren ganz anders Spaß! Und das wird befeuert, wenn man dabei etwas sehen und mit einem optischen Feedback interagieren kann. Die Möglichkeiten des _Live View Programming_ gehen jedoch weit über die "Spielerei" hinaus.

Eine kurze Demo zur Einführung bietet das YouTube-Video [Einführung in das Live View Programming mit Javas JShell](https://www.youtube.com/watch?v=itWU15ywmzA).

### 📄 Live View Programming zur Dokumentation

Mit dem _Live View Programming_ kann man -- ganz im Sinne des Literate Programming -- eine _Live View_ zur Dokumentation von Java-Code erzeugen; und das alles aus der Java-Datei heraus, in der man das Programm geschrieben hat. Code und Dokumentation können miteinander kombiniert werden.

In dem git-Repository findet sich die Datei [`logo.java`](/logo.java). Mit der folgenden Eingabe erzeugen Sie im Browser die Dokumentation, die Sie in die Logo-Programmierung mit Clerk einführt.

Löschen Sie die Inhalte in der aktuellen _Live View_ und führen Sie `logo.java` aus.

```java
jshell> Clerk.clear()

jshell> /o logo.java  // /o ist Kurzform von /open
```

Im Browser sieht das Ergebnis so aus (Sie sehen hier nur einen Teil der Seite):

![Das Ergebnis von `logo.java`](/README.TurtleProgramming.png)

Ich finde das Ergebnis ziemlich eindrucksvoll, mich begeistert das. Die Bilder werden durch die Abarbeitung in der JShell erst erzeugt. Mit der Skill namens `Text` können Codeauszüge an geeigneten Stellen in die Dokumentation gesetzt werden. Der Code in [`logo.java`](/logo.java) erklärt sich durch die hinzugefügte Dokumentation, den darin enthaltenen Code und dessen Ausführung sozusagen von selbst.

Um das besser zu verstehen, schauen Sie sich den Code in der Datei [`logo.java`](/logo.java) mit einem Editor Ihrer Wahl an.

# 📝 Skizze zur Arbeitsweise des LVP-Prototypen

## 🪟 Live Views 

Wenn Sie sich die Datei [`lvp.java`](/lvp.java) anschauen, werden Sie feststellen, dass nicht viel Code erforderlich ist, um eine Infrastruktur für das _Live View Programming_ aufzusetzen. In der Datei befindet sich im Wesentlichen eine Klasse und ein Interface:

* Die Klasse `LiveView` setzt mit der Methode `onPort` einen Server auf, der eine _Live View_ im Browser bedient. Diese _Live View_ zeigt die `index.html` aus dem `web`-Verzeichnis an und lädt das notwendige Stückchen Client-Code `script.js`.

Der Webserver nutzt _Server Sent Events_ (SSE) als Mittel, um die _Live View_ im Browser beliebig zu erweitern. Man kann mit der Methode `sendServerEvent` entweder HTML-Code, `<script>`-Tags oder JavaScript-Code senden oder JavaScript-Bibliotheken laden.

* Das Interface `Clerk` bietet ein paar statische Methoden an, um die Programmierung von Views zu erleichtern. Dazu gehören die folgenden Wrapper für die Methode `sendServerEvent` aus der `LiveView`:

    - `write` schickt HTML-Code über eine View an den Browser, wo der HTML-Code gerendert wird
    - `call` schickt JavaScript-Code über eine View zur Ausführung an den Browser
    - `script` schickt JavaScript-Code über eine View an den Browser, der ihn in ein `<script>`-Tag einpackt, im DOM des Browsers hinzufügt und ausführt
    - `load` fordert den Browser über eine View zum Laden einer JavaScript-Bibliothek auf. Eine JavaScript-Bibliothek wird nur genau einmal pro View geladen
    - `clear` entfernt alle HTML-Tags im DOM, die mit `id="events"` ausgewiesen sind, d.h. es werden alle `write`-Einträge gelöscht.

Interessant ist noch die statische Methode `markdown` in `Clerk`, mit der direkt Markdown-Text an den Browser der Standard-View (das ist die View zum default Port 50001) geschickt und gerendet wird.

## 🧑‍💼 Views

Im Verzeichnis [`views`](/views/) finden sich ein paar Views. Darunter ist eine View für [`Markdown`](https://de.wikipedia.org/wiki/Markdown) zur Nutzung der Markdown-Notation, eine View für [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik) und eine View, die eine GUI für das Spiel [Tic-Tac-Toe](https://de.wikipedia.org/wiki/Tic-Tac-Toe) realisiert.

Views werden immer mit einer _Live View_ assoziiert und stellen zudem den browser-seitig benötigten Code zur Verfügung, um die _View_ zu erzeugen. Als Programmierkonvention implementiert eine View stets das Interface `Clerk`.

## 🤹 Skills

Skills sind im Verzeichnis [`skills`](/skills/) zu finden. Skills haben nichts mit einer _View_ zu tun, sie stellen spezielle oder generelle Fähigkeiten zur Verfügung, die man beim _Live View Programming_ oder im Zusammenspiel mit Views gebrauchen kann. `Text` ist z.B. ein wichtiger Skill, um Text oder Code aus einer Datei "ausschneiden" zu können, was elementar für die Code-Dokumentation ist.

> Solange einzelne Views und Skills nicht weiter dokumentiert sind (das wird noch kommen), studieren Sie am besten den Code der Views und Skills. In der Datei [`logo.java`](/logo.java) sehen Sie ein Beispiel der Verwendung dieser grundlegenden Fähigkeiten. Das Beispiel zeigt, wie Sie mit Java-Code eine Dokumentation des eigenen Programms erstellen können, das zudem beispielhaft seine Verwendung erläutert.

## 👁️ Ein Blick hinter die Kulissen

In dem YouTube-Video ["Live View Programming: Ein Blick hinter die Kulissen"](https://youtu.be/Qj6PEYNSXnM) erkläre ich Ihnen, wie der LVP-Server an den Browser Daten schickt, den HTML-Code im Browser verändert und JavaScript-Programme nachlädt. Die Idee zum LVP besteht aus einem sehr einfachen Kern, der sich einfach erweitern lässt.

# 🚀 Der Prototyp ist erst der Anfang

## 🌴 Vision

Meine Vision für das _Live View Programming_ ist zunächst, dieses Werkzeug in der Programmierungsbildung meiner Informatik-Studierenden an der THM einzusetzen. Damit habe ich im Sommersemester 2024 begonnen. Ich möchte herausfinden, wie das LVP beim Erlernen von Java eine Hilfe und Unterstützung sein kann. Die Entwicklung zum LVP läuft seitdem parallel weiter.

Daneben hoffe ich, dass diese Umsetzung für Java als Blaupause für die Realisierung des _Live View Programming_ in anderen Programmiersprachen dient. Die Idee ist so einfach, dass man sie in ein, zwei Tagen portieren kann für die Lieblingssprache der Wahl.

## 💃🕺 Mitmach-Aufruf

> Sie sind gerne willkommen, sich an der Entwicklung des _Live View Programming_ zu beteiligen. Schreiben Sie neue Views und Skills! Oder entwickeln Sie am Kern der _Live View_ mit.

Zwei Personen haben geholfen, das LVP aus der Taufe zu heben: Ramon und Björn.

* Nach einem _Proof of Concept_ ([hier](https://github.com/denkspuren/LiveViewProgramming/releases/tag/0.1.0)) ist mit der Hilfe und Unterstützung von @RamonDevPrivate (mittlerweile Co-Entwickler in diesem Repo 💪) eine erste Umsetzung eines Webservers mit Server Sent Events (SSE) entstanden! Von Ramon stammen u.a. die TicTacToe-View, die Dot-View und die ObjectInspector-Skill.

* [@BjoernLoetters](https://github.com/BjoernLoetters) war von der Idee des _Live View Programming_ ebenso angefixt wie ich und lieferte spontan einen beeindruckenden Server-Entwurf mit Websockets bei. Ich habe mich vorerst dennoch für eine einfachere Lösung entschieden, einen Webserver mit Server Sent Events (SSE). Für Interessierte ist der Code von Björn im Branch [websockets](https://github.com/denkspuren/LiveViewProgramming/tree/websockets) hinterlegt. Ich empfehle das Studium seines Codes sehr, man kann viel daran über Websockets lernen!

Seitdem haben auch einige andere, meist Studierende von mir, Beiträge zum LVP geliefert. Die Contributors sind in dem GitHub-Repo ausgewiesen. Vielen Dank dafür!  

Wenn Sie Lust haben, beteiligen Sie sich!

Herzlichst,<br>
Dominikus Herzberg
