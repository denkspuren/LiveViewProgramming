# _Live View Programming_ mit Java und der JShell

Das _Live View Programming_ (LVP) bietet Ihnen für die Java- bzw. JShell-Programmierung _Views_ und _Skills_ an. Views sind dazu da, um mediale Inhalte im Web-Browser darzustellen, also Texte, Bilder, Grafiken, Videos, inteaktive Animationen etc. Skills stellen nützliche Fähigkeiten bereit, die man in Kombination mit Views (z.B. zur Dokumentation von Code) gebrauchen kann.

## 💟 Motivation: Views bereichern das Programmieren

### Was fehlt: Mediale Ausgabe- und Interaktionsformate

Für Programmieranfänger:innen ist es eine echte Herausforderung für das Verständnis in Programmabläufe, wenn man einzig über eingestreute `System.out.println`-Aufrufe Einblicke in die Ausführung eines kompilierten Programms erhält. Ein Debugger ist für Anfängerinnen und Anfänger keine wirkliche Alternative. Verwendet man zum Einstieg in die Programmierung hingegen die JShell, so kommt man praktisch ohne `println` aus. In der JShell interagiert man direkt mit Javas Sprach- und Datenkonstrukten und kann Programme inkrementell aufbauen und erkunden. Dennoch fehlen zusätzliche Ausgabe- und Interaktionsmöglichkeiten, die den Programmiereinstieg interessant und die Effekte und Auswirkungen von Programmabläufen "sichtbar" machen. Hier kommt das _Live View Programming_ ins Spiel.

### Visualisierungen als eigenständiger Programmierzweck

Das _Live View Programming_ versteht sich als ein Angebot, in ein bestehendes Programm _Views_ einzubauen und zu verwenden, die im Web-Browser angezeigt werden. Es macht nicht nur Spaß, wenn man zum Beispiel Grafiken im Browser erzeugen kann -- man sieht auch die Programmierfehler, die einem unterlaufen. Wenn man etwa in der Turtle-View eine Schildkröte mit einem Stift über die Zeichenfläche schickt, zeigt sich unmittelbar, ob man Wiederholungen über Schleifen richtig aufgesetzt oder die Rekursion korrekt umgesetzt hat. Die visuelle Repräsentation gibt über das Auge eine direkte Rückmeldung. Feedback motiviert und hilft beim Verständnis.

### Views und Skills zum Programmverständnis

Für Fortgeschrittene kommen andere Aspekte hinzu, die von einer Visualisierung profitieren. Zum Beispiel lassen sich Datenstrukturen sehr gut mit Hilfe von Graphen darstellen. Mit der Dot-View können Graphen, die in der [DOT-Sprache](https://de.wikipedia.org/wiki/DOT_(Graphviz)) beschrieben sind, im Browser gerendert werden. Die Dot-View wird beispielsweise von dem ObjectInspector genutzt, einem Skill, den das LVP bereitstellt. Der `ObjectInspector` bekommt ein Java-Objekt übergeben, reflektiert das Objekt und erstellt ein Objektdiagramm, das sich von diesem Objekt über die Referenzen zu anderen Objekten ergibt. Das Objektdiagramm wird in der Dot-Sprache beschrieben und mit Hilfe der Dot-View zur Anzeige gebracht. Das ist eine sehr hilfreiche visuelle und leicht zugängliche Form der Objekt-Introspektion.

### Kombination von Anschauungsbeispielen und Programmdokumentation

Mit LVP kann man jedoch noch sehr viel mehr machen. Mit der Markdown-View kann man Markdown-Texte im Browser anzeigen. Zusammen mit der Text-Skill können beispielsweise Code-Abschnitte aus einer Java-Datei ausgeschnitten und im Markdown-Text in einem Code-Block eingefügt werden. Man kann also ein Java-Programm erstellen, das seine eigene Dokumentation in Markdown enthält, wobei die in der Dokumentation angezeigten Code-Fragmente immer aktuell sind. Das ist eine besondere Form des [_Literate Programming_](https://en.wikipedia.org/wiki/Literate_programming).

### Konzeptstudien und Prototyp-Entwicklung

Das LVP kann man auch dafür einsetzen, um Anwendungen zu schreiben. Die Views werden zusammen mit interaktiven Elementen, wie Buttons, Slidern etc. als graphische Oberfläche verstanden und ausgelegt. Damit lassen sich vor allem Konzeptstudien und Prototypen entwickeln.

## 🐣 Ursprünge: Clerk als Inspiration

### Ausgangspunkt Notizbuch-Programmierung

Wer in Python programmiert, hat meist schon von der [Notizbuch-Programmierung](https://en.wikipedia.org/wiki/Notebook_interface) mit [Jupyter bzw. JupyterLab](https://jupyter.org/) gehört oder sie sogar schon genutzt. Man programmiert direkt im Webbrowser, wo eine Notizbuch-Umgebung über einen Server bereitgestellt wird. Das Notizbuch ermöglicht die Kombination von Programmcode und Dokumentation in beliebiger Abfolge, wobei die Programmentwicklung inkrementell und explorativ, d.h. in kleinen Schritten und im Stil einer Erkundung verläuft. Das Notizbuch zeigt Zwischenausgaben der Programmausführung, wobei Datenplots und andere visuelle und teils interaktive Darstellungen erzeugt und eingebunden werden können. Die Notizbuch-Programmierung ist z.B. in den Datenwissenschaften, im Quantencomputing und in der KI-Entwicklung weit verbreitet.[^1]

> Die Notizbuch-Programmierung hat die Besonderheit, dass sie mit der Aktualisierung von Dokumentations- und Code-Zellen ein eigenes Ausführungsmodell über das der verwendeten Programmiersprache legt. Das ist nicht immer wünschenswert und gewollt!

[^1]: Wer einen Eindruck von der Notizbuch-Programmierung gewinnen möchte, kann sich z.B. meinen [Simulator für Quantenschaltungen](https://github.com/denkspuren/qcsim/blob/main/qcsim-dev.ipynb) anschauen.

### Die Idee: Nutze den Browser lediglich zur Darstellung

Als ich eine besondere Variante der Notizbuch-Programmierung namens Clerk für die Programmiersprache Clojure entdeckte, war es um mich geschehen: Statt im Browser, d.h. im Notizbuch zu programmieren, bleibt man bei Clerk in der gewohnten Entwicklungsumgebung. Die Browseransicht wird während des Programmierens automatisch und live generiert. Diese Art des Programmierens bezeichnen die Entwickler von Clerk als _Moldable Live Programming_, mehr Infos dazu finden sich unter https://clerk.vision/.

Clerk für Clojure ist ein mächtiges und eindrucksvolles Werkzeug. Auch hier stülpt Clerk der Sprache Clojure auf sehr elegante Weise ein Ausführungsmodell über. Aber davon kann man absehen, und es offenbart sich eine ganz einfache Idee: Es bedarf eines einfachen Webservers, den man programmierend ansteuern und erweitern kann, um im Webbrowser Inhalte anzeigen und Interaktionen verarbeiten zu können. Diese Grundidee, die ich als _Live View Programming_ bezeichne, wollte ich so einfach und unkompliziert wie möglich für Java und die JShell realisieren.

> Das _Live View Programming_ belässt die Kontrolle, was wann im Browser wie angezeigt wird, bei der Programmiersprache. Das macht das _Live View Programming_ leicht verstehbar und stellt die Notizbuch-Programmierung nicht in den Mittelpunkt. Man gewinnt Freiheiten, kann aber, wenn man möchte, mit LVP auch Notizbuch-Programme schreiben, nur eben auf etwas andere Art.

### Was es braucht: einen minimalen _Live View Server_

Der entstandene _Live View Server_ kann nach seinem Start über ein Interface namens Clerk (engl. für Sachbearbeiter, Büroangestellter, Schreibkraft) angesteuert werden; der Name soll an die Inspirationsquelle erinnern.

Der _Live View Server_ ist denkbar einfach konzipiert. Über das Clerk-Interface können bereitgestellte oder selbst programmierte Views aktiviert und anschließend genutzt werden. Und die Skills bieten zudem nützliche Hilfsmittel an. Auch hier kann man auf bereitgestellte Skills zurückgreifen oder eigene programmieren.

### Das _Live View Programming_ bietet unzählige Möglichkeiten

Wenn man Programme in Notizbuch-Form oder als [Literate Program](https://en.wikipedia.org/wiki/Literate_programming) dokumentieren möchte, bedarf es nicht mehr als der Markdown-View und der Text-Skill.

Für Anwendungs- oder Darstellungszwecke kann man z.B. die Turtle-View für die Erstellung von [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik) nutzen. Für das Spiel [TicTacToe](https://de.wikipedia.org/wiki/Tic-Tac-Toe) steht eine einfache, interaktive graphische Darstellung bereit. Für die Abbildung von [Graphen]("https://de.wikipedia.org/wiki/Graph_(Graphentheorie)"), die mit ihren Kanten und Knoten oft in der Informatik verwendet werden, gibt es die Dot-View. Zum Beispiel nutzt der Skill zur Objekt-Introspektion die Dot-View.

> Da das _Live View Programming_ nicht wie die Notizbuch-Programmierung eine bestimmte Art der Dokumentation und des Gebrauchs vorgibt, ist es an einem selbst, die Views und Skills in geeigneter Weise für einen bestimmten Zweck zu verwenden. 

All diese Views und Skills nutzt man programmierend mit Java in der IDE oder mittels JShell-Skripten und einem Editor. Es ist – ehrlich gesagt – ziemlich cool, wenn man die Ergebnisse dann im Browser sieht. 

## 🚀 Nutze das _Live View Programming_ mit der JShell

Wenn Sie das _Live View Programming_ ausprobieren möchten, ist Folgendes zu tun:

### 1. Lade die `.jar`-Datei herunter

* Stellen Sie sicher, dasss Sie mit einem aktuellen JDK (Java Development Kit) arbeiten; es empfiehlt sich das [OpenJDK](https://openjdk.org/)
* Laden Sie die aktuelle `.jar`-Datei herunter, die Ihnen als Asset zum [aktuellen Release](https://github.com/denkspuren/LiveViewProgramming/releases) als Download angeboten wird; die Datei hat den Namen `lvp-<Version>.jar`
* Legen Sie die Datei an einem geeigneten Ort ab, der Klassenpfad muss auf die `.jar`-Datei gesetzt werden können. Am einfachsten ist es, die `jar`-Datei dorthin zu verschieben, wo Sie die JShell oder Java aufrufen

### Die Alternative: `jar`-Datei selber erstellen

Sie können die `.jar`-Datei auch selber generieren, wenn Sie zudem die Versionsverwaltungssoftware [Git](https://git-scm.com/) und das Build-Werkzeug [Maven](https://maven.apache.org/) installiert haben:

* Laden Sie das Git-Repository herunter mit
  ```
  git clone https://github.com/denkspuren/LiveViewProgramming.git
  ```
* Nach dem Maven-Durchlauf finden Sie die `.jar`-Datei im `target`-Verzeichnis
  ```
  mvn clean package
  ```  

    `git clone https://github.com/denkspuren/LiveViewProgramming.git`

### 2. Setze den Klassenpfad auf die `.jar`-Datei

Wenn Sie mit der JShell arbeiten, rufen Sie die JShell mit dem Argument `--class-path` oder kurz `-c` auf. Das _Live View Programming_ wird auf diese Weise als Softwarepaket bereitgestellt. Passen Sie den Beispielaufruf an die aktuelle Version an:

```
jshell -c lvp-<Version>.jar
```

Wenn Sie die Version `lvp-0.4.0.jar` heruntergeladen haben, lautet der Aufruf:

```
jshell -c lvp-0.4.0.jar
```


<!-- Zudem aktiviert `-R-ea` die Berücksichtigung von `assert`-Anweisungen. -->

### 3. So nutzt man das _Live View Programming_

Importieren Sie das Programmpaket zum einfacheren Zugriff in der JShell. Anschließend kann der _Live View Server_ gestartet werden:

```
jshell> import lvp.*;

jshell> Clerk.serve()
Open http://localhost:50001 in your browser
$38 ==> LiveView@2d38eb89
```

Öffnen Sie Ihren Browser (bei mir ist es Chrome) mit dieser Webadresse. Im Browser kann man nun mitverfolgen, was passiert, wenn man die _Live View_ nutzt. 

Erstellen wir einen einfachen Begrüßungstext im Markdown-Format:

```java
jshell> Clerk.markdown("Hello, _this_ is **Live View Programming** in action!")
```

Im Browser ist "Hello, _this_ is **Live View Programming** in action!" zu sehen. 😀

Als nächstes erzeugen wir eine kleine _Turtle_-Grafik. Die Idee, Grafiken mit einer Schildkröte (_turtle_) zu programmieren, stammt von der Programmiersprache [Logo]("https://de.wikipedia.org/wiki/Logo_(Programmiersprache)").

```java
jshell> import lvp.views.Turtle;

jshell> Turtle turtle = new Turtle(200, 200)
turtle ==> lvp.views.turtle.Turtle@33f88ab
```

Ein Kästchen, die Zeichenfläche, von 200 x 200 Punkten ist im Browser zu sehen. In der Mitte befindet sich eine unsichtbare Schildkröte, die nach Osten (also nach rechts) ausgerichtet und mit einem Zeichenstift ausgestattet ist und die wir mit ein paar Anweisungen so umherschicken, dass schrittweise ein Quadrat entsteht.

Geben Sie die folgende Anweisung vier Mal für die Schildkröte ein.

```java
turtle.forward(80).left(90);
turtle.forward(80).left(90);
turtle.forward(80).left(90);
turtle.forward(80).left(90);
```

Sie sollten nun ein Quadrat im rechten oberen Zeichenfeld sehen. Die Schildkröte blickt am Schluss ihres Wegs wieder gen Osten. Ergänzen wir einen "Kreis", den wir aus 12 Strichen zusammensetzen.

```java
for (int i = 1; i <= 12; i++)
    turtle.right(360.0 / 12).forward(20);
```

Links unten ist nun außerdem ein kantiger "Kreis" zu sehen. 😊

![Ein Turtle-Beispiel](/src/main/java/lvp/views/turtle/TurtleExample.png)

> Das ist also die Idee des _Live View Programming_: Man kann mit Java-Code sichtbare Effekte in der Browseransicht erzeugen.

Es ist zudem möglich, Views interaktiv zu koppeln. In der Animation sieht man, wie eine _Turtle_-Grafik mit einem _Slider_ verbunden ist.

![Interaktives Beispiel: Slider gekoppelt mit Turtle-Grafik](/docs/SliderAndTurtle.gif)

So macht das Programmieren ganz anders Spaß! Und das wird befeuert, wenn man dabei etwas sehen und mit einem optischen Feedback interagieren kann. Die Möglichkeiten des _Live View Programming_ gehen jedoch weit über die "Spielerei" hinaus.

Eine kurze Demo zur Einführung bietet das YouTube-Video [Einführung in das Live View Programming mit Javas JShell](https://www.youtube.com/watch?v=itWU15ywmzA). 

_**Beachten Sie:** Das Video zeigt Ihnen eine frühere LVP-Version, wo es noch keine `.jar`-Datei gab. Das ist aber auch schon alles._

## 🐢-Beispiel: Kombination von Anwendung und Dokumentation

Mit dem _Live View Programming_ kann man _Views_ sowohl zur Dokumentation als auch zur Erzeugung von Anwendungsbeispielen heranziehen. Code und Dokumentation können miteinander kombiniert werden.

In dem git-Repository findet sich die Datei [`logo.java`](/logo.java). Laden Sie die Datei herunter in das Verzeichnis, in dem Sie die JShell gestartet haben.

Mit der folgenden Eingabe erzeugen Sie im Browser die Dokumentation, die Sie in die Logo-Programmierung einführt. Gleichzeitig werden dabei die Turtle-Beispiele generiert, die in der Dokumentation vorgestellt werden.

Löschen Sie die Inhalte der aktuellen _Views_ und führen Sie `logo.java` aus.

```java
jshell> Clerk.clear()

jshell> /o logo.java  // /o ist Kurzform von /open
```

Im Browser sieht das Ergebnis so aus (Sie sehen hier nur einen Teil der Seite):

![Das Ergebnis von `logo.java`](/README.TurtleProgramming.png)

Ich finde das Ergebnis ziemlich eindrucksvoll, mich begeistert das. Die Bilder werden durch die Abarbeitung in der JShell erst erzeugt. Mit der Skill namens `Text` können Codeauszüge an geeigneten Stellen in die Dokumentation gesetzt werden. Der Code in [`logo.java`](/logo.java) erklärt sich durch die hinzugefügte Dokumentation, den darin enthaltenen Code und dessen Ausführung sozusagen von selbst.

Um das besser zu verstehen, schauen Sie sich den Code in der Datei [`logo.java`](/logo.java) mit einem Editor Ihrer Wahl an.

## 📝 Skizze zur Arbeitsweise des LVP-Prototypen

### Live Views 

Wenn Sie sich die Datei [`lvp.java`](/lvp.java) anschauen, werden Sie feststellen, dass nicht viel Code erforderlich ist, um eine Infrastruktur für das _Live View Programming_ aufzusetzen. In der Datei befindet sich im Wesentlichen eine Klasse und ein Interface:

* Die Klasse `LiveViewServer` setzt mit der Methode `onPort` einen Server auf, der eine _Live View_ im Browser bedient. Diese _Live View_ zeigt die `index.html` aus dem `web`-Verzeichnis an und lädt das notwendige Stückchen Client-Code `script.js`.

Der Webserver nutzt _Server Sent Events_ (SSE) als Mittel, um die _Live View_ im Browser beliebig zu erweitern. Man kann mit der Methode `sendServerEvent` entweder HTML-Code, `<script>`-Tags oder JavaScript-Code senden oder JavaScript-Bibliotheken laden.

* Das Interface `Clerk` bietet ein paar statische Methoden an, um die Programmierung von Views zu erleichtern. Dazu gehören die folgenden Wrapper für die Methode `sendServerEvent` aus der `LiveView`:

    - `write` schickt HTML-Code über eine View an den Browser, wo der HTML-Code gerendert wird
    - `call` schickt JavaScript-Code über eine View zur Ausführung an den Browser
    - `script` schickt JavaScript-Code über eine View an den Browser, der ihn in ein `<script>`-Tag einpackt, im DOM des Browsers hinzufügt und ausführt
    - `load` fordert den Browser über eine View zum Laden einer JavaScript-Bibliothek auf. Eine JavaScript-Bibliothek wird nur genau einmal pro View geladen
    - `clear` entfernt alle HTML-Tags im DOM, die mit `id="events"` ausgewiesen sind, d.h. es werden alle `write`-Einträge gelöscht.

Interessant ist noch die statische Methode `markdown` in `Clerk`, mit der direkt Markdown-Text an den Browser der Standard-View (das ist die View zum default Port 50001) geschickt und gerendet wird.

### Views

Im Verzeichnis [`views`](/views/) finden sich ein paar Views. Darunter ist eine View für [`Markdown`](https://de.wikipedia.org/wiki/Markdown) zur Nutzung der Markdown-Notation, eine View für [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik) und eine View, die eine GUI für das Spiel [Tic-Tac-Toe](https://de.wikipedia.org/wiki/Tic-Tac-Toe) realisiert.

Views werden immer mit einer _Live View_ assoziiert und stellen zudem den browser-seitig benötigten Code zur Verfügung, um die _View_ zu erzeugen. Als Programmierkonvention implementiert eine View stets das Interface `Clerk`.

### Skills

Skills sind im Verzeichnis [`skills`](/skills/) zu finden. Skills haben nichts mit einer _View_ zu tun, sie stellen spezielle oder generelle Fähigkeiten zur Verfügung, die man beim _Live View Programming_ oder im Zusammenspiel mit Views gebrauchen kann. `Text` ist z.B. ein wichtiger Skill, um Text oder Code aus einer Datei "ausschneiden" zu können, was elementar für die Code-Dokumentation ist.

> Solange einzelne Views und Skills nicht weiter dokumentiert sind (das wird noch kommen), studieren Sie am besten den Code der Views und Skills. In der Datei [`logo.java`](/logo.java) sehen Sie ein Beispiel der Verwendung dieser grundlegenden Fähigkeiten. Das Beispiel zeigt, wie Sie mit Java-Code eine Dokumentation des eigenen Programms erstellen können, das zudem beispielhaft seine Verwendung erläutert.

### Ein Blick hinter die Kulissen

In dem YouTube-Video ["Live View Programming: Ein Blick hinter die Kulissen"](https://youtu.be/Qj6PEYNSXnM) erkläre ich Ihnen, wie der LVP-Server an den Browser Daten schickt, den HTML-Code im Browser verändert und JavaScript-Programme nachlädt. Die Idee zum LVP besteht aus einem sehr einfachen Kern, der sich einfach erweitern lässt.

## 💃🕺 Das _Live View Programming_ lebt

### Das _Live View Programming_ ist im Einsatz

Das _Live View Programming_ kommt seit dem Sommersemester 2024 in der Programmierausbildung an der [THM](https://www.thm.de/) zum Einsatz. Ich möchte herausfinden, wie das _Live View Programming_ beim Erlernen von Java eine Hilfe und Unterstützung sein kann und wie sich damit Programmierprojekte für die Studierenden gestalten und durchführen lassen. Das sieht alles sehr vielversprechend aus. Die weitere Entwicklung des _Live View Programming_ läuft seitdem parallel weiter, wann immer es die Zeit erlaubt.

Daneben hoffe ich, dass die hier umgesetzte Java-Version als Blaupause für die Realisierung des _Live View Programming_ in anderen Programmiersprachen dient. Die Idee ist so einfach, dass man sie in ein, zwei Tagen portieren kann für die Lieblingssprache der Wahl.

### Mitmach-Aufruf

> Sie sind gerne willkommen, sich an der Entwicklung des _Live View Programming_ zu beteiligen. Schreiben Sie neue Views und Skills! Oder entwickeln Sie am Kern des _Live View Servers_ mit.

Einige haben schon Beiträge zum LVP geliefert, meist sind es Studierende von mir. Die Contributors sind in dem GitHub-Repo ausgewiesen. Vielen Dank dafür!

Ramon ist seit den Anfangstagen als Co-Entwickler (💪) an der Umsetzung des _Live View Programming_ beteiligt. Nach einem [_Proof of Concept_](https://github.com/denkspuren/LiveViewProgramming/releases/tag/0.1.0) von mir hat Ramon den _Live View Webserver_ mit Server Sent Events (SSE) gebaut und viele wichtige Beiträge geliefert! Server Sent Events machen die Architektur des Servers sehr einfach und kommen der Idee entgegen, primär _Views_ anzubieten. [@BjoernLoetters](https://github.com/BjoernLoetters) hatte eine alternative Lösung mit [Websockets](https://github.com/denkspuren/LiveViewProgramming/tree/websockets) eingebracht, die jedoch deutlich komplizierter ausfällt. Auch wenn der SSE-Webserver "gewonnen" hat, empfehle ich das Studium des Code von Björn sehr, man kann viel daran über Websockets lernen!

Also: Wenn Sie Lust haben, beteiligen Sie sich!

Herzlichst,<br>
Dominikus Herzberg
