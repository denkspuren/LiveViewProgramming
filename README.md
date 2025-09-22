# _Live View Programming_ mit jeder Programmiersprache

Das _Live View Programming_ (LVP) bietet Ihnen für die Programmierung ein einfaches Textprotokoll an, um mediale Inhalte im Web-Browser darzustellen, also Texte, Bilder, Grafiken, Videos, inteaktive Animationen etc. Kommandos stellen auch nützliche Fähigkeiten bereit, die man z.B. zur Dokumentation von Code gebrauchen kann.

**Eine detaillierte Übersicht des Protokolls mit allen Kommandos folgt noch.**

## 🚀 Nutze das _Live View Programming_

Wenn Sie das _Live View Programming_ ausprobieren möchten, ist Folgendes zu tun:

### 1. Lade die `.jar`-Datei herunter

* Stellen Sie sicher, dasss Sie mit einem aktuellen JDK (Java Development Kit) arbeiten; es empfiehlt sich das [TemurinJDK](https://adoptium.net/temurin/releases/)
* Laden Sie die aktuelle `.jar`-Datei herunter, die Ihnen als Asset zum [aktuellen Release](https://github.com/denkspuren/LiveViewProgramming/releases) als Download angeboten wird; die Datei hat den Namen `lvp-<Version>.jar`
* Laden Sie `demo.java`-Datei herunter, die Ihnen ebenfalls als Asset zum [aktuellen Release](https://github.com/denkspuren/LiveViewProgramming/releases) als Download angeboten wird.

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
### 2. Starte den LVP-Server

Passen Sie den Beispielaufruf an die aktuelle Version an:

```
java -jar lvp-<Version>.jar --log demo.java
```

Wenn Sie die Version `lvp-1.0.0.jar` heruntergeladen haben, lautet der Aufruf:

```
java -jar lvp-1.0.0.jar --log demo.java
```

#### Übersicht der möglichen Kommandozeilenargumente

| Argument                   | Alias | Bedeutung                                                                 | Beispiel                                 |
|----------------------------|-------|---------------------------------------------------------------------------|------------------------------------------|
| `--cmd=CMD`                |       | Startbefehl für die Ausführung (z. B. Java mit Optionen)                  | `--cmd="java --enable-preview"`          |
| `--log[=LEVEL]`            | `-l`  | Log-Level (`Error`, `Info`, `Debug`)                                      | `--log=Debug`                            |
| `--port`                   | `-p`  | Portnummer für den Server                                                 | `--port=50002`                           |
| `--config`                 | `-c`  | Lädt Konfiguration aus `sources.json`                                     | `--config`                               |
| `--source-only`            | `-s`  | Ignoriert alle Nicht-Source-Dateien                                       | `--source-only`                          |
| `--watch-filter=PATTERN`   | `-w`  | Filter für Dateien, die ein Neuladen der Inhalte auslösen können          | `--watch-filter=./deps/*.java`           |
| `SOURCES`                  |       | Quellen, die durch LVP ausgeführt werden                                  | `demo1.java demo2.java` <br> `sources/*.java` |


> Mehrere Argumente können kombiniert werden, z.B.:  
> `java -jar lvp-<Version>.jar --watch-filter=src/lib/**/*.java --log=Debug --port=50001 --config src/*View.java`

### 3. So nutzt man das _Live View Programming_

Die Datei `demo.java` dient als einfaches Beispiel für den Einstieg in das Live View Programming (LVP).  

Damit LVP funktioniert, **muss der Server die Datei beobachten (watchen)** – sobald Änderungen erkannt werden, wird der Code automatisch neu ausgeführt und die Ausgabe aktualisiert.

Innerhalb einer [`void main()`-Methode](https://openjdk.org/jeps/495) lassen sich interaktive Inhalte erzeugen, indem man `println`-Ausgaben entsprechend dem Protokoll erzeugt. Diese Inhalte werden anschließend im Browser angezeigt.

**Beispiel:**

```java
import lvp.Clerk;

void main() {
    println("Markdown: # Hello World");
}
```
Dieser einfache Aufruf rendert eine Markdown-Überschrift direkt im Browser. Weitere Ausgaben, Grafiken oder Interaktionen können durch zusätzliche Kommandos ergänzt werden.

### Troubleshooting

> Error starting server: Address already in use: bind

Wenn der Port blockiert ist, kann unter Windows mit folgendem Befehl die PID des blockierenden Prozesses ermittelt werden:

```
netstat -a -n -o | findstr ":50001"
```

Beispielausgabe:
```
TCP    127.0.0.1:50001        0.0.0.0:0              LISTENING       11840
```
Die letzte Spalte der Ausgabe zeigt die PID (Prozess-ID) des Prozesses, der den Port verwendet.

Für Linux oder Mac kann folgender [Befehl](https://man7.org/linux/man-pages/man8/lsof.8.html) verwendet werden, um den Prozess zu finden, der den Port blockiert:
```
lsof -i:50001
```

Die PID kann genutzt werden, um den entsprechenden Prozess im Task-Manager zu finden oder ihn direkt zu beenden. In PowerShell kann der Prozess mit folgendem Befehl beendet werden:
```
Stop-Process 11840
```
Um den Prozess unter Linux oder Mac zu beenden, kann folgender Befehl verwendet werden:
```
kill -9 11840
```
Dabei ist 11840 durch die ermittelte PID zu ersetzen.

## 💟 Motivation: Views bereichern das Programmieren (Outdated)

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

Clerk für Clojure ist ein mächtiges und eindrucksvolles Werkzeug. Auch hier stülpt Clerk der Sprache Clojure auf sehr elegante Weise ein Ausführungsmodell über. Aber davon kann man absehen, und es offenbart sich eine ganz simple Idee: Es bedarf eines einfachen Webservers, den man programmierend ansteuern und erweitern kann, um im Webbrowser Inhalte anzeigen und Interaktionen verarbeiten zu können. Diese Grundidee, die ich als _Live View Programming_ bezeichne, wollte ich so einfach und unkompliziert wie möglich für Java realisieren.

> Das _Live View Programming_ belässt die Kontrolle, was wann im Browser wie angezeigt wird, bei der Programmiersprache. Das macht das _Live View Programming_ leicht verstehbar und stellt die Notizbuch-Programmierung nicht in den Mittelpunkt. Man gewinnt Freiheiten, kann aber, wenn man möchte, mit LVP auch Notizbuch-Programme schreiben, nur eben auf etwas andere Art.

### Was es braucht: einen minimalen _Live View Server_

Der entstandene _Live View Server_ kann nach seinem Start über ein Interface namens Clerk (engl. für Sachbearbeiter, Büroangestellter, Schreibkraft) angesteuert werden; der Name soll an die Inspirationsquelle erinnern.

Der _Live View Server_ ist denkbar einfach konzipiert. Über das Clerk-Interface können bereitgestellte oder selbst programmierte Views aktiviert und anschließend genutzt werden. Und die Skills bieten zudem nützliche Hilfsmittel an. Auch hier kann man auf bereitgestellte Skills zurückgreifen oder eigene programmieren.

### Das _Live View Programming_ bietet unzählige Möglichkeiten

Wenn man Programme in Notizbuch-Form oder als [Literate Program](https://en.wikipedia.org/wiki/Literate_programming) dokumentieren möchte, bedarf es nicht mehr als der Markdown-View und der Text-Skill.

Für Anwendungs- oder Darstellungszwecke kann man z.B. die Turtle-View für die Erstellung von [Turtle-Grafiken](https://de.wikipedia.org/wiki/Turtle-Grafik) nutzen. Für die Abbildung von [Graphen]("https://de.wikipedia.org/wiki/Graph_(Graphentheorie)"), die mit ihren Kanten und Knoten oft in der Informatik verwendet werden, gibt es die Dot-View. Zum Beispiel nutzt der Skill zur Objekt-Introspektion die Dot-View.

> Da das _Live View Programming_ nicht wie die Notizbuch-Programmierung eine bestimmte Art der Dokumentation und des Gebrauchs vorgibt, ist es an einem selbst, die Views und Skills in geeigneter Weise für einen bestimmten Zweck zu verwenden.  

## 💃🕺 Das _Live View Programming_ lebt

### Das _Live View Programming_ ist im Einsatz

Das _Live View Programming_ kommt seit dem Sommersemester 2024 in der Programmierausbildung an der [THM](https://www.thm.de/) zum Einsatz. Ich möchte herausfinden, wie das _Live View Programming_ beim Erlernen von Java eine Hilfe und Unterstützung sein kann und wie sich damit Programmierprojekte für die Studierenden gestalten und durchführen lassen. Das sieht alles sehr vielversprechend aus. Die weitere Entwicklung des _Live View Programming_ läuft seitdem parallel weiter, wann immer es die Zeit erlaubt.

### Mitmach-Aufruf

Einige haben schon Beiträge zum LVP geliefert, meist sind es Studierende von mir. Die Contributors sind in dem GitHub-Repo ausgewiesen. Vielen Dank dafür!

> Derzeit arbeiten Ramon und ich an einer neuen Architektur für das Live View Programming. Der Umbau soll im Spätsommer 2025 abgeschlossen sein. Dann ist die Möglichkeit zum Mitmachen wieder eröffnet. In der Zwischenzeit müssen wir Sie vertrösten.

### Historie

* Ramon ist seit den Anfangstagen als Co-Entwickler (💪) an der Umsetzung des _Live View Programming_ beteiligt.
* Nach einem [_Proof of Concept_](https://github.com/denkspuren/LiveViewProgramming/releases/tag/0.1.0) von mir hat Ramon den _Live View Webserver_ mit Server Sent Events (SSE) gebaut und viele wichtige Beiträge geliefert! Server Sent Events machen die Architektur des Servers sehr einfach und kommen der Idee entgegen, primär _Views_ anzubieten.
* [@BjoernLoetters](https://github.com/BjoernLoetters) hatte eine alternative Lösung mit [Websockets](https://github.com/denkspuren/LiveViewProgramming/tree/websockets) eingebracht, die jedoch deutlich komplizierter ausfällt. Auch wenn der SSE-Webserver "gewonnen" hat, empfehle ich das Studium des Codes von Björn sehr, man kann viel daran über Websockets lernen!
* Mein Prototyp zum [Java Live Reloading](https://github.com/denkspuren/JavaLiveReloading) gab den Anstoss, eine neue Architektur umzusetzen und sich von der JShell als interaktives Medium zu verabschieden.
* Die angestrebte und [hier](https://github.com/denkspuren/LiveViewProgramming/issues/77) skizzierte neue Architekturbasis ist aus dem Dialog zwischen Ramon und mir hervorgegangen. Sie wird das LVP mit ganz neuen Fähigkeiten ausstatten.

Herzlichst,<br>
Dominikus Herzberg