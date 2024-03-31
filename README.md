# Clerk: Live View Programming with Java's JShell

Ich stelle Ihnen hier Clerk, die daraus hervorgegangene Idee des _Live View Programming_ und einen Clerk-Prototypen für Java bzw. die JShell vor. Wenn Sie Gefallen an der Idee und dem Projekt finden: Ganz unten gibt es einen Mitmach-Aufruf und Vorschläge, woran man arbeiten und worüber man nachdenken könnte.

## 💟 Motivation: Clerk, das will ich auch haben!

Wer in Python programmiert, hat meist schon von der [Notizbuch-Programmierung](https://en.wikipedia.org/wiki/Notebook_interface) mit [Jupyter bzw. JupyterLab](https://jupyter.org/) gehört oder sie sogar schon genutzt. Man programmiert direkt im Browser, wo eine Notizbuch-Umgebung über einen Server bereitgestellt wird. Das Notizbuch ermöglicht die Kombination von Programmcode und Dokumentation in beliebiger Abfolge, wobei die Programmentwicklung inkrementell und explorativ, d.h. in kleinen Schritten und im Stil einer Erkundung verläuft. Das Notizbuch zeigt Zwischenausgaben der Programmausführung an und Datenplots und andere visuelle und teils interaktive Darstellungen können erzeugt und angezeigt werden. Die Notizbuch-Programmierung ist z.B. in den Datenwissenschaften, im Quantencomputing und in der KI-Entwicklung weit verbreitet.[^1]

[^1]: Wer einen Eindruck von der Notizbuch-Programmierung gewinnen möchte, kann sich z.B. meinen [Simulator für Quantenschaltungen](https://github.com/denkspuren/qcsim/blob/main/qcsim-dev.ipynb) anschauen.

Als ich eine besondere Variante der Notizbuch-Programmierung namens Clerk für die Programmiersprache Clojure entdeckte, war es um mich geschehen: Statt im Browser, d.h. im Notizbuch zu programmieren, bleibt man bei Clerk in der gewohnten Entwicklungsumgebung, und die Browseransicht wird währenddessen automatisch und live generiert. Diese Art des Programmierens bezeichnen die Entwickler von Clerk als _Moldable Live Programming_, mehr Infos dazu finden sich unter https://clerk.vision/.

Clerk für Clojure ist ein mächtiges und eindrucksvolles Werkzeug -- Hut ab vor den Entwicklern. Was mich an diesem Ansatz  so fasziniert, ist seine konzeptuelle Eleganz und Einfachkeit: Es genügt ein simpler Webserver, den man programmierend ansteuern und erweitern kann, um im Browser Inhalte, gegebenenfalls sogar interaktive Inhalte anzeigen zu können. Damit kann man einen einfachen Satz an Darstellungsmöglichkeiten für Programmieranfänger:innen bereitstellen. Und erfahrene Programmierer:innen können eigene Erweiterungen für ihre Zwecke entwickeln.

Diese Grundidee wollte ich so einfach und unkompliziert wie möglich für Java und die JShell umsetzen. Ich nenne diese Idee _Live View Programming_ (LVP). Clerk als Namen habe ich beibehalten, allerdings arbeitet das _Live View Programming_ nicht mit einem Clerk (engl. für Sachbearbeiter, Büroangestellter, Schreibkraft), sondern mit vielen Clerks. Jeder Clerk ist für eine spezielle _Live View_ zuständig. Dazu kommen _Skills_, die generelle Fähigkeiten beisteuern, die nicht an eine _Live View_ gebunden sind.

Das _Live View Programming_ mit seinen Clerks und Skills ist mit einem sehr schlanken _Live View_-Webserver umsetzbar. Es braucht nur wenige Mittel, um damit die Notizbuch-Programmierung umzusetzen. Aber es geht noch viel mehr! Ein Beispiel ist das [Literate Programming](https://en.wikipedia.org/wiki/Literate_programming), das ganz andere Wege bei der Kombination von Code und Dokumentation geht. Ein anderes Beispiel ist ein Clerk für [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik), was zur Grafik-Programmierung animiert. Ein weiteres Beispiel ist ein Clerk, der eine GUI für das Spiel [TicTacToe] bereitstellt. In all diesen Beispielen programmiert man wie gewohnt mit Java in der IDE oder mittels JShell-Skripten und einem Editor und instruiert den Browser, was er anzeigen soll. Das ist -- ehrlich gesagt -- ziemlich cool!

## 💻 Ein erster Kontakt: _Live View Programming_ für die JShell

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

Geben Sie die folgende Anweisung view Mal für die Schildkröte ein.

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

![Ein Turtle-Beispiel](/Turtle/TurtleExample.png)

> Das ist also die Idee des _Live View Programming_: Man kann mit Java-Code sichtbare Effekte in der Browseransicht erzeugen. 

Das wirkt wie Spielerei. Ohne Frage, Programmieren darf Spaß machen -- und das wird befeuert, wenn man dabei etwas sehen und mit einem optischen Feedback interagieren kann. Die Möglichkeiten des _Live View Programming_ gehen jedoch weit über die "Spielerei" hinaus.

### 📄 Live View Programming zur Dokumentation

Mit dem _Live View Programming_ kann man -- ganz im Sinne des Literate Programming -- eine _Live View_ zur Dokumentation von Java-Code erzeugen -- und das alles aus der Java-Datei heraus, in der man das Programm geschrieben hat. Code und Dokumentation können miteinander kombiniert werden.

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

![Das Ergebnis von `logo.java`](logo.png)

Ich finde das Ergebnis ziemlich eindrucksvoll, mich begeistert das. Die Bilder werden durch die Abarbeitung in der JShell erst erzeugt. Mit der Skill namens `File` können Codeauszüge an geeigneten Stellen in die Dokumentation gesetzt werden. Der Code in [`logo.java`](/logo.java) erklärt sich durch die hinzugefügte Dokumentation, den darin enthaltenen Code und dessen Ausführung sozusagen von selbst.

Um das besser zu verstehen, schauen Sie sich den Code in der Datei [`logo.java`](/logo.java) mit einem Editor Ihrer Wahl an.

> Das Java-Preview-Feature der String-Templates wird offenbar noch nicht in jedem Editor (oder von einer entsprechenden Erweiterung) richtig dargestellt. Das Syntax-Highlighting kommt durch die String-Templates möglicherweise durcheinander und der Java-Code wird eventuell nicht sehr leserlich angezeigt.

# 📝 Skizze zur Arbeitsweise des Clerk-Prototypen

Wenn Sie sich die Datei [`clerk.java`](/clerk.java) anschauen, werden Sie feststellen, dass nicht viel Code erforderlich ist:

**TEXT IN ÜBERARBEITUNG**
<!--
* Die Klasse `LiveView` setzt mit den Boardmitteln von Java einen Webserver mit [Server Sent Events](https://en.wikipedia.org/wiki/Server-sent_events) (SSE) auf, der die in `SSEType` kodierten Events kennt. Hier verbirgt sich die entscheidende Infrastruktur.
* Die Klasse `Clerk` aktiviert die LiveView mit der Methode `serve` und schickt Text in Markdown-Syntax mit `markdown` an den Browser.

Daneben gibt es eine Reihe von Skills, die Clerk erst interessant und nützlich machen:

* Die Klasse `File` bietet Methoden für die Verarbeitung von Dateien an. Mit der Methode `cutOut` kann man markierte Textabschnitte aus einer Datei ausschneiden. Das ist ein entscheidendes Feature, um Code zu dokumentieren.
* Die Klasse `Turtle` erweitert Clerk und erlaubt die Verwendung der Turtle-Implementierung [`turtle.js`](skills/Turtle/turtle.js) durch Java. Die verschiedenen Turtle-Methoden rufen im Browser ihre Entsprechungen in `turtle.js` auf.
* Die Klasse `Markdown` ist eine weitere Erweiterung, um mit Markdown arbeiten zu können.
-->

In der Datei [`logo.java`](/logo.java) sehen Sie ein Beispiel der Verwendung dieser wenigen grundlegenden Fähigkeiten von Clerk. Das Beispiel zeigt, wie Sie mit Java-Code eine Dokumentation des eigenen Programms erstellen können, das zudem beispielhaft seine Verwendung erläutert und zeigt.

# 🚀 Der Prototyp ist erst der Anfang

## 🌴 Vision

Meine Vision ist, Clerk in der Programmierausbildung meiner Informatik-Studierenden an der THM zum Einsatz kommen zu lassen. Wenn einmal ein HTTP-Server realisiert ist, wird Clerk ein schönes Beispiel für webbasierte Client/Server-Programmierung abgeben, und es kann in seinen Fähigkeiten kontinuierlich erweitert werden. Mit Clerk wäre damit auch ein Rahmenwerk gegeben für die Programmierung von Web-Anwendungen. Generell ist der hier vorgestellte Ansatz für jede andere Programmiersprache ebenso umsetzbar.

Zum einen scheint mir Clerk für Programmier-Anfänger:innen geeignet zu sein: Es macht vermutlich mehr Sinn und Spaß, wenn man Schleifen-Konstrukte erlernt, indem man Logo-Zeichnungen generiert. Gerne würde ich Clerk erweitern um die Möglichkeit, automatisiert ein Objektdiagramm zu einer gegebenen Objektreferenz zu erzeugen -- das geht mit dem Java-Reflection-API und z.B. [Graphviz-Online](https://dreampuf.github.io/GraphvizOnline); @RamonDevPrivate hat das bereits mit diesem [Gist](https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee) vorbereitet. Clerk kann also dabei helfen, den zur Laufzeit entstandenen Graphen aus Objekten und Referenzen zu verstehen. Mit solchen Erweiterungen kann Clerk Teil der Entwicklungswerkzeuge beim Programmieren werden.

Zum anderen können auch erfahrene Entwickler:innen mit Clerk eine anschauliche und verständliche Dokumentation zu ihrem Code erstellen. Wenn visuelle Anteile das unterstützen können, umso besser. Man kann Clerk aber ebenso für Experimente, exploratives Programmieren und Notebook-basierte Programmierung verwenden. Sicher gibt es noch viele andere, denkbare Anwendungsszenarien.

## 💃🕺 Mitmach-Aufruf

> Sie sind gerne willkommen, sich an der Entwicklung der Clerk-Idee, eines _Live View Programming with Java's JShell_, zu beteiligen.

Dazu ein paar Punkte, die mir in den Sinn kommen:

* Ich habe wenig Ahnung von Web-Technologien, d.h. von HTML, CSS und JavaScript, z.B. hat ChatGPT 3.5 den Code für `turtle.js` beigesteuert. Allerdings hat sich mein Verständnis von Web-Technologien durch die Arbeit an Clerk deutlich verbessert bzw. eine Auffrischung erfahren. Dennoch gibt es sicher welche, die Verbesserungsvorschläge zur Umsetzung von Clerk haben.

* Wie könnte man z.B. eine Bibliothek wie `https://www.chartjs.org/` in Clerk einbinden? Das würde die Einsatzmöglichkeiten für Clerk bereichern.

* Eine interaktive Anwendung wäre eine schöne Vorzeige-Demo. Wie wäre es mit Tic-Tac-Toe? Natürlich soll im Browser nur das Spielbrett dargestellt und das UI abgebildet werden, die Berechnung von Spielzügen etc. findet javaseitig statt. Dafür wird man Clerk ein wenig erweitern müssen.

* Der Einsatz von Clerk könnte auch sinnvoll ohne Browser sein, um eine Dokumentation in einer Dokumentationsdatei etwa im Markdown-Format vorzunehmen. Dafür braucht es keinen HTTP-Server. Wenn zudem der Browser verwendet wird, könnte Clerk Medien auslesen (z.B. eine erzeugte Turtle-Grafik als Bild exporteiren), abspeichern und in eine Dokumentation einfügen.

Weitere Überlegungen zur Überarbeitung des aktuellen Prototypen sind unter [Considerations.md](Considerations.md) zu finden.

Wie man Clerk modular gestalten könnte zum Zwecke der Erweiterung, ob man es doch als `jar`-Datei ausliefern sollte, ... diesen Fragen kann man sich widmen, wenn der Prototyp reift und mit einem HTTP-Server ausgestattet ist.

## 🙏 Dank für Beiträge


Nach einem _Proof of Concept_ ([hier](https://github.com/denkspuren/clerk/releases/tag/0.1.0)) ist mit der Hilfe und Unterstützung von @RamonDevPrivate (mittlerweile Co-Entwickler in diesem Repo 💪) eine erste Umsetzung mit einem Webserver entstanden! Man kann mit dieser Clerk-Variante aus der JShell heraus Markdown erzeugen, Abschnitte aus Code- und Textdateien herausschneiden und Zeichnungen mit einer Logo-Schildkröte erstellen. Insbesondere das Herausschneiden von Text- bzw. Codeabschnitten aus Dateien und das neue Java-Feature der String-Template (Preview-Feature in Java 21) sind sehr einfache aber mächtige Instrumente zur Code-Dokumentation und zur Unterstützung des [_Literate Programming_](https://en.wikipedia.org/wiki/Literate_programming).

Es ist schon krass cool, wenn man in der JShell mit Java-Code "nebenan" im Browser etwas hineinschreibt und Logo-Bilder entstehen. Da geht noch viel, viel mehr!

Wer mag, kann den entstandenen Prototypen ausprobieren!

-- und damit eine Blaupause für die Realisierung in jeder anderen Programmiersprache liefern. 

[@BjoernLoetters](https://github.com/kuchenkruste) war von Clerk ebenso angefixt wie ich und lieferte spontan einen beeindruckenden Server-Entwurf mit Websockets bei. Vielen Dank dafür! Ich habe mich vorerst dennoch für eine einfachere Lösung entschieden, einen Webserver mit Server Sent Events (SSE). Für Interessierte ist der Code von Björn im Branch [websockets](https://github.com/denkspuren/clerk/tree/websockets) hinterlegt.

Vielen Dank an [@ginschel](https://github.com/ginschel) für einen ersten [CSS-Vorschlag](https://github.com/denkspuren/clerk/pull/5)!

[@RamonDevPrivate](https://github.com/RamonDevPrivate) ist nach seinen entscheidenden Beiträgen zur Umsetzung eines Webservers mit Server Sent Events (SSE) zum Mitentwickler von Clerk geworden. Von ihm gibt es auch die Umsetzung eines ObjectInspectors (siehe dieses [Gist](https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee)), der sicher auch noch Eingang in die Skills von Clerk finden wird.

Herzlichst,<br>
Dominikus Herzberg


