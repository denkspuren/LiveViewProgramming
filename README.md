# Clerk: Live View Programming with Java's JShell

Ich stelle Ihnen hier Clerk, die daraus hervorgegangene Idee des _Live View Programming_ und eine Umsetzung für Java bzw. die JShell vor. Wenn Sie Gefallen an der Idee und dem Projekt finden: Sie können sich gerne an der Entwicklung beteiligen!

## 💟 Motivation: Clerk, das will ich auch haben!

Wer in Python programmiert, hat meist schon von der [Notizbuch-Programmierung](https://en.wikipedia.org/wiki/Notebook_interface) mit [Jupyter bzw. JupyterLab](https://jupyter.org/) gehört oder sie sogar schon genutzt. Man programmiert direkt im Browser, wo eine Notizbuch-Umgebung über einen Server bereitgestellt wird. Das Notizbuch ermöglicht die Kombination von Programmcode und Dokumentation in beliebiger Abfolge, wobei die Programmentwicklung inkrementell und explorativ, d.h. in kleinen Schritten und im Stil einer Erkundung verläuft. Das Notizbuch zeigt Zwischenausgaben der Programmausführung an und Datenplots und andere visuelle und teils interaktive Darstellungen können erzeugt und eingebunden werden. Die Notizbuch-Programmierung ist z.B. in den Datenwissenschaften, im Quantencomputing und in der KI-Entwicklung weit verbreitet.[^1]

[^1]: Wer einen Eindruck von der Notizbuch-Programmierung gewinnen möchte, kann sich z.B. meinen [Simulator für Quantenschaltungen](https://github.com/denkspuren/qcsim/blob/main/qcsim-dev.ipynb) anschauen.

Als ich eine besondere Variante der Notizbuch-Programmierung namens Clerk für die Programmiersprache Clojure entdeckte, war es um mich geschehen: Statt im Browser, d.h. im Notizbuch zu programmieren, bleibt man bei Clerk in der gewohnten Entwicklungsumgebung, und die Browseransicht wird währenddessen automatisch und live generiert. Diese Art des Programmierens bezeichnen die Entwickler von Clerk als _Moldable Live Programming_, mehr Infos dazu finden sich unter https://clerk.vision/.

Clerk für Clojure ist ein mächtiges und eindrucksvolles Werkzeug -- Hut ab vor den Entwicklern. Was mich an diesem Ansatz  so fasziniert, ist seine konzeptuelle Eleganz und Einfachkeit: Es genügt ein simpler Webserver, den man programmierend ansteuern und erweitern kann, um im Browser Inhalte, gegebenenfalls sogar interaktive Inhalte anzeigen zu können. Damit kann man einen einfachen Satz an Darstellungsmöglichkeiten für Programmieranfänger:innen bereitstellen. Und erfahrene Programmierer:innen können eigene Erweiterungen für ihre Zwecke entwickeln.

Diese Grundidee wollte ich so einfach und unkompliziert wie möglich für Java und die JShell umsetzen. Ich nenne diese Idee _Live View Programming_ (LVP). Clerk als Namen habe ich beibehalten, allerdings arbeitet das _Live View Programming_ nicht mit einem Clerk (engl. für Sachbearbeiter, Büroangestellter, Schreibkraft), sondern mit vielen Clerks. Jeder Clerk ist für eine spezielle _Live View_ zuständig. Dazu kommen _Skills_, die generelle Fähigkeiten beisteuern, die nicht an eine _Live View_ gebunden sind.

Das _Live View Programming_ mit seinen Clerks und Skills ist mit einem sehr schlanken _Live View_-Webserver umgesetzt. Es braucht nur wenige Mittel, um damit eine Notizbuch-Programmierung umzusetzen. Aber es geht noch viel mehr! Ein Beispiel ist das [Literate Programming](https://en.wikipedia.org/wiki/Literate_programming), das ganz andere Wege bei der Kombination von Code und Dokumentation geht. Ein anderes Beispiel ist ein Clerk für [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik), was zur Grafik-Programmierung animiert. Ein weiteres Beispiel ist ein Clerk, der eine GUI für das Spiel [TicTacToe](https://de.wikipedia.org/wiki/Tic-Tac-Toe) bereitstellt. In all diesen Beispielen programmiert man wie gewohnt mit Java in der IDE oder mittels JShell-Skripten und einem Editor und instruiert den Browser, was er anzeigen soll. Das ist -- ehrlich gesagt -- ziemlich cool!

## 💻 _Live View Programming_ für die JShell

Zum Ausprobieren muss das Java JDK 21 bzw. 22 installiert (ich verwende das OpenJDK) und dieses Git-Repository heruntergeladen sein. Wer `git` installiert hat, kann das wie folgt machen.

```shell
git clone https://github.com/denkspuren/clerk.git
```

Da der Code mit [String Templates](https://docs.oracle.com/en/java/javase/21/language/string-templates.html) ein Preview-Feature von Java nutzt, muss die JShell im `clerk`-Ordner mit der Option `--enable-preview` aufgerufen werden.
<!-- Zudem aktiviert `-R-ea` die Berücksichtigung von `assert`-Anweisungen. -->

```shell
jshell --enable-preview
```

### 🎹 Clerk zur interaktiven Live-View-Programmierung

Die Datei `clerk.java` wird in die JShell geladen und der Server für die _Live View_ gestartet.

```
jshell> /open clerk.java

jshell> Clerk.view()
Open http://localhost:50001 in your browser
$38 ==> LiveView@2d38eb89
```

Öffnen Sie Ihren Browser (bei mir ist es Chrome) mit dieser Webadresse. Im Browser kann man nun mitverfolgen, was passiert, wenn man die _Live View_ nutzt. 

Probieren wir einen einfachen Begrüßungstext im Markdown-Format:

```java
jshell> Clerk.markdown("Hello, _this_ is **Clerk**!")
```

Im Browser ist "Hello, _this_ is **Clerk**!" zu sehen. 😀

Als nächstes erzeugen wir eine kleine _Turtle_-Grafik. Die Idee, eine Grafik mit einer Schildkröte (_turtle_) zu programmieren, hat die Programmiersprache Logo in die Welt getragen. Mehr dazu erfahren Sie im nächsten Abschnitt.

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

![Ein Turtle-Beispiel](/clerks/Turtle/TurtleExample.png)

> Das ist also die Idee des _Live View Programming_: Man kann mit Java-Code sichtbare Effekte in der Browseransicht erzeugen. 

Das wirkt wie Spielerei. Ohne Frage, Programmieren darf Spaß machen -- und das wird befeuert, wenn man dabei etwas sehen und mit einem optischen Feedback interagieren kann. Die Möglichkeiten des _Live View Programming_ gehen jedoch weit über die "Spielerei" hinaus.

### 📄 Live View Programming zur Dokumentation

Mit dem _Live View Programming_ kann man -- ganz im Sinne des Literate Programming -- eine _Live View_ zur Dokumentation von Java-Code erzeugen; und das alles aus der Java-Datei heraus, in der man das Programm geschrieben hat. Code und Dokumentation können miteinander kombiniert werden.

In dem git-Repository findet sich die Datei [`logo.java`](/logo.java). Mit der folgenden Eingabe erzeugen Sie im Browser die Dokumentation, die Sie in die Logo-Programmierung mit Clerk einführt.

```java
jshell> Clerk.view().stop()

jshell> Clerk.view()
Open http://localhost:50001 in your browser
$76 ==> LiveView@dcf3e99
```

Refreshen Sie die Seite im Browser oder laden Sie sie neu, um eine leere Seite zu sehen. Dann können Sie `logo.java` ausführen.

```java
jshell> /o logo.java  // /o ist Kurzform von /open
```

Im Browser sieht das Ergebnis so aus (Sie sehen hier nur einen Teil der Seite):

![Das Ergebnis von `logo.java`](/README.TurtleProgramming.png)

Ich finde das Ergebnis ziemlich eindrucksvoll, mich begeistert das. Die Bilder werden durch die Abarbeitung in der JShell erst erzeugt. Mit der Skill namens `File` können Codeauszüge an geeigneten Stellen in die Dokumentation gesetzt werden. Der Code in [`logo.java`](/logo.java) erklärt sich durch die hinzugefügte Dokumentation, den darin enthaltenen Code und dessen Ausführung sozusagen von selbst.

Um das besser zu verstehen, schauen Sie sich den Code in der Datei [`logo.java`](/logo.java) mit einem Editor Ihrer Wahl an.

> Das Java-Preview-Feature der String-Templates wird offenbar noch nicht in jedem Editor (oder von einer entsprechenden Erweiterung) richtig dargestellt. Das Syntax-Highlighting kommt durch die String-Templates möglicherweise durcheinander und der Java-Code wird eventuell nicht sehr leserlich angezeigt.

# 📝 Skizze zur Arbeitsweise des Clerk-Prototypen

## 🪟 Live Views 

Wenn Sie sich die Datei [`clerk.java`](/clerk.java) anschauen, werden Sie feststellen, dass nicht viel Code erforderlich ist, um eine Infrastruktur für das _Live View Programming_ aufzusetzen. In der Datei befindet sich im Wesentlichen eine Klasse und ein Interface:

* Die Klasse `LiveView` setzt mit der Methode `onPort` einen Server auf, der eine _Live View_ im Browser bedient. Diese _Live View_ zeigt die `index.html` aus dem `web`-Verzeichnis an und lädt das notwendige Stückchen Client-Code `script.js`.

Der Webserver nutzt _Server Sent Events_ (SSE) als Mittel, um die _Live View_ im Browser beliebig zu erweitern. Man kann mit der Methode `sendServerEvent` entweder HTML-Code, `<script>`-Tags oder JavaScript-Code senden oder JavaScript-Bibliotheken laden.

* Das Interface `Clerk` bietet ein paar statische Methode an, um die Programmierung von Clerks zu erleichtern. Dazu gehören die folgenden Wrapper für die Methode `sendServerEvent` aus der `LiveView`:

    - `write` schickt HTML-Code über eine View an den Browser, wo der HTML-Code gerendert wird
    - `call` schickt JavaScript-Code über eine View zur Ausführung an den Browser
    - `script` schickt JavaScript-Code über eine View an den Browser, der ihn in ein `<script>`-Tag einpackt, im DOM des Browsers hinzufügt und ausführt
    - `load` fordert den Browser über eine View zum Laden einer JavaScript-Bibliothek auf. Eine JavaScript-Bibliothek wird nur genau einmal pro View geladen

Interessant ist noch die statische Methode `markdown` in `Clerk`, mit der direkt Markdown-Text an den Browser der Standard-View (das ist die View zum default Port 50001) geschickt und gerendet wird.

## 🧑‍💼 Clerks

Im Verzeichnis [`clerks`](/clerks/) finden sich ein paar Clerks, um _Views_ zu erzeugen und zu bedienen. Darunter ist ein Clerk für [`Markdown`](https://de.wikipedia.org/wiki/Markdown) zur Nutzung der Markdown-Notation, ein Clerk für [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik) und ein Clerk, der eine GUI für das Spiel [Tic-Tac-Toe](https://de.wikipedia.org/wiki/Tic-Tac-Toe) realisiert.

Clerks sind immer mit einer _Live View_ assoziiert und stellen zudem den browser-seitig benötigten Code zur Verfügung, um die _View_ zu erzeugen. Als Programmierkonvention implementiert ein Clerk stets das Interface `Clerk`.

## 🤹 Skills

Skills sind im Verzeichnis [`skills`](/skills/) zu finden. Skills haben nichts mit einer _View_ zu tun, sie stellen spezielle oder generelle Fähigkeiten zur Verfügung, die man beim _Live View Programming_ oder im Zusammenspiel mit Clerks gebrauchen kann. `File` ist z.B. ein wichtiger Skill, um Text oder Code aus einer Datei "ausschneiden" zu können, was elementar für die Code-Dokumentation ist.

> Solange einzelne Clerks und Skills nicht weiter dokumentiert sind (das wird noch kommen), studieren Sie am besten den Code der Clerks und Skills. In der Datei [`logo.java`](/logo.java) sehen Sie ein Beispiel der Verwendung dieser wenigen grundlegenden Fähigkeiten. Das Beispiel zeigt, wie Sie mit Java-Code eine Dokumentation des eigenen Programms erstellen können, das zudem beispielhaft seine Verwendung erläutert.

# 🚀 Der Prototyp ist erst der Anfang

## 🌴 Vision

Meine Vision für das _Live View Programming_ ist zunächst, dieses Werkzeug in der Programmierungsbildung meiner Informatik-Studierenden an der THM einzusetzen.

Zum einen scheint mir Clerk für Programmier-Anfänger:innen geeignet zu sein: Es macht vermutlich mehr Sinn und Spaß, wenn man z.B. Schleifen-Konstrukte erlernt, indem man Logo-Zeichnungen generiert und am Bild erkennen kann, ob man die Schleife korrekt umgesetzt hat. Gerne würde ich das _Live View Programming_ um die Möglichkeit erweitern, automatisiert ein Objektdiagramm zu einer gegebenen Objektreferenz zu erzeugen -- das geht mit dem Java-Reflection-API und z.B. [Graphviz-Online](https://dreampuf.github.io/GraphvizOnline); @RamonDevPrivate hat das bereits mit diesem [Gist](https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee) vorbereitet. Das _Live View Programming_ kann also dabei helfen, den zur Laufzeit entstandenen Graphen aus Objekten und Referenzen zu verstehen. Mit solchen Erweiterungen können Clerks und Skills Teil der Entwicklungswerkzeuge beim Programmieren werden.

Zum anderen können auch erfahrene Entwickler:innen mit dem _Live View Programming_ eine anschauliche und verständliche Dokumentation zu ihrem Code erstellen. Wenn visuelle Anteile das unterstützen können, umso besser. Man kann das _Live View Programming_ aber ebenso für Experimente, exploratives Programmieren und Notebook-basierte Programmierung verwenden. Sicher gibt es noch viele andere, denkbare Anwendungsszenarien.

Und ich habe noch eine Vision: Dass diese Umsetzung für Java als Blaupause für die Realisierung des _Live View Programming_ in anderen Programmiersprachen dient. Die Idee ist so einfach, dass man sie in ein, zwei Tagen portieren kann für die Lieblingssprache der Wahl.

## 💃🕺 Mitmach-Aufruf

> Sie sind gerne willkommen, sich an der Entwicklung des _Live View Programming with Java's JShell_ zu beteiligen. Schreiben Sie neue Clerks und Skills! Oder entwickeln Sie am Kern der _Live View_ mit.

Ein paar Beiträge hat es schon gegeben:

* Nach einem _Proof of Concept_ ([hier](https://github.com/denkspuren/clerk/releases/tag/0.1.0)) ist mit der Hilfe und Unterstützung von @RamonDevPrivate (mittlerweile Co-Entwickler in diesem Repo 💪) eine erste Umsetzung eines Webservers mit Server Sent Events (SSE) entstanden! Von Ramon stammt auch der TicTacToe-Clerk.

* [@BjoernLoetters](https://github.com/kuchenkruste) war von Clerk ebenso angefixt wie ich und lieferte spontan einen beeindruckenden Server-Entwurf mit Websockets bei. Ich habe mich vorerst dennoch für eine einfachere Lösung entschieden, einen Webserver mit Server Sent Events (SSE). Für Interessierte ist der Code von Björn im Branch [websockets](https://github.com/denkspuren/clerk/tree/websockets) hinterlegt. Ich empfehle das Studium seines Codes sehr, man kann viel daran über Websockets lernen!

* Vielen Dank an [@ginschel](https://github.com/ginschel) für einen ersten [CSS-Vorschlag](https://github.com/denkspuren/clerk/pull/5)!

Wenn Sie Lust haben, beteiligen Sie sich!

Herzlichst,<br>
Dominikus Herzberg


