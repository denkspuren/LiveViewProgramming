# _Live View Programming_ mit jeder Programmiersprache

Live View Programming (LVP) stellt ein einfaches Textprotokoll bereit, mit dem sich mediale Inhalte direkt im Web-Browser darstellen lassen, darunter Texte, Bilder, Grafiken, Videos und interaktive Animationen.

Das Protokoll dient als sprachunabhängige Schnittstelle zwischen Ihrem eigenen Programm und LVP. Die Kommunikation erfolgt über Kommandos, die durch gewöhnliche Konsolenausgaben erzeugt werden. Diese Kommandos können Inhalte transformieren und sie im Browser medial aufbereitet anzeigen.

Auf diese Weise stellt LVP praktische Funktionen bereit, die sich beispielsweise für die Code-Dokumentation, die Erzeugung von Turtle-Grafiken oder das Erstellen interaktiver HTML-Elemente nutzen lassen.



## 🚀 Nutze das _Live View Programming_

Wenn Sie das _Live View Programming_ ausprobieren möchten, ist Folgendes zu tun:

### 1. Lade die `.jar`-Datei herunter

* Stellen Sie sicher, dasss Sie mit einem aktuellen JDK (Java Development Kit) arbeiten; es empfiehlt sich das [OpenJDK](https://jdk.java.net/25/)
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

### 3. Einbinden von Quellen
Damit eigene Programme mit LVP kommunizieren können, werden sie innerhalb von LVP als Laufzeitumgebung ausgeführt. Diese Programme werden im Kontext von LVP als Quellen bezeichnet.

Die Programme selbst benötigen keine zusätzlichen Abhängigkeiten und können in beliebigen Programmiersprachen geschrieben sein. Es gibt zwei Möglichkeiten, Quellen in LVP zu definieren.

#### Variante 1: Übergabe über die Konsole

Wie oben gezeigt, können Quellen als Argument beim Konsolenaufruf übergeben werden. Dabei lassen sich beliebig viele Quellen definieren. Alle angegebenen Quellen werden mit dem über --cmd übergebenen Befehl ausgeführt.

Wird das Argument `--cmd` nicht gesetzt, versucht LVP standardmäßig, die Quellen als Java-Programme auszuführen.

#### Variante 2: Konfigurationsdatei (sources.json)

Bei einer größeren Anzahl von Quellen oder wenn Quellen unterschiedlich gestartet werden sollen (z. B. mit verschiedenen Startbefehlen), empfiehlt sich die Verwendung einer sources.json-Datei im Wurzelverzeichnis der Ausführung.

Wird LVP mit dem Argument `--config` gestartet, werden die in dieser Datei definierten Quellen zusätzlich ausgeführt.

Ein Beispiel für den Aufbau der Datei befindet sich im Ordner examples.

Beide Varianten lassen sich auch kombinieren.

### 4. Das Protokoll
Im Folgenden eine grobe Übersicht über den generellen Aufbau des Protokolls. Ein ausführlicheres Beispiel finden Sie in `demo.java`, sowie weitere kleine Beispiele in dem Ordner `examples`.

Einzeilige Kommandos:
```java
println("Markdown: # Ich bin eine Überschrift");
```

Mehrzeilige Kommandos:
```java
println("""
  Markdown:
  # Ich bin eine Überschrift
  Ich bin **Text**.
  ~~~
""");
```
Kommandos bestehen aus einem Namen und einem Inhalt. Dabei wird zwischen einzeiligen und mehrzeiligen Kommandos unterschieden. Ein mehrzeiliges Kommando wird durch `~~~` beendet. Die meisten Kommandos können sowohl einzeilig als auch mehrzeilig genutzt werden. Es gibt hier jedoch auch einige Ausnahmen, wie das Turtle-Kommando, das nur mehrzeilig genutzt werden kann oder das Codeblock-Kommando, das nur einzeilig genutzt werden kann.

<br/>

```java
println("""
  Text[template]:
  ## Beispiel
  Irgendein ${0} anzeigen.
  ~~~
  | Markdown

  Text: Beispiel
  | Text[template] | Markdown
""")

```
Durch Piping können die Ergebnisse eines Kommandos an ein anderes Kommando weitergegeben werden.

## Kommandos und Anweisungen

### Channelkommandos
Kommandos, wie `Markdown`, `Html` oder `Dot`, die Inhalte im Browser anzeigen, werden als Channelkommandos bezeichnet. Sie haben keine Ausgabe, die weiterverarbeitet werden könnte, da ihr Ergebnis direkt im Browser landet.

#### Markdown
**Eingabe:** Markdown-Text

**Ergebnis:** Umwandlung des Markdown-Textes in HTML und Anzeige im Browser.

#### Html
**Eingabe:** HTML-Text

**Ergebnis:** Einfügen des HTML-Textes in den Html-Body.

#### Css
**Eingabe:** CSS-Text

**Ergebnis:** Einbettung des CSS-Textes in einen Style-Tag im Browser.

#### JavaScript
**Eingabe:** JavaScript-Code

**Ergebnis:** Einfügen des JavaScript-Codes in einen Script-Tag im Browser.

#### JavaScriptCall
**Eingabe:** JavaScript-Code

**Ergebnis:** Ausführung des JavaScript-Codes im Browser.

#### SubViewStyle
**Eingabe:** CSS-Text

**Ergebnis:** Einbettung des CSS-Textes in eine CSS-Klasse, die auf die SubView angewendet wird, in der das Kommando ausgeführt wird.

#### Dot
**Eingabe:** Graphenbeschreibung in der Dot-Sprache

**Ergebnis:** Umwandlung der Graphenbeschreibung in eine Grafik und Anzeige im Browser.

#### Clear
**Eingabe:** Keine

**Ergebnis:** Löschen aller Inhalte im Browser.

### Servicekommandos
Servicekommandos, wie `Text` oder `Codeblock`, haben eine Ausgabe, die weiterverarbeitet werden kann. Sie können zum Beispiel durch Piping an Channelkommandos weitergegeben werden, um sie im Browser anzuzeigen.

#### Text
**Eingabe:** Text

**Ausgabe:** Der eingegebene Text wird als Ausgabe zurückgegeben und kann weiterverarbeitet werden.

Dieses Kommando hat zwei besondere Eigenschaften:
1. Es kann sich Inhalte merken, wenn eine ID angegeben wird. Diese Inhalte können später durch die Angabe der ID wieder abgerufen werden.
2. Es kann als Template genutzt werden, indem Platzhalter definiert werden. Diese werden aufgefüllt, wenn der gespeicherte Inhalt durch Piping weitere Eingaben erhält.

#### Codeblock
**Eingabe:** `path;label`
- Path: Pfad zu einer Quelldatei
- Label: Kommentarlabel, das den Bereich in der Quelldatei markiert, der angezeigt werden soll.

**Ausgabe:** Der Code-Abschnitt, der durch das Label markiert ist, wird als Ausgabe zurückgegeben, dazu Metainhalte, um den Code-Block im Browser interaktiv zu machen.

#### Cutout
**Eingabe:** `path;label`
- Path: Pfad zu einer Quelldatei
- Label: Kommentarlabel, das den Bereich in der Quelldatei markiert, der angezeigt werden soll.

**Ausgabe:** Der Code-Abschnitt, der durch das Label markiert ist, wird als Ausgabe zurückgegeben.

(Anders als beim Codeblock-Kommando können hier keine interaktiven Code-Blöcke erzeugt werden)

#### Turtle
**Eingabe:**
- `init WIDTH HEIGHT` -> Initialisiert die Zeichenfläche mit der angegebenen Breite und Höhe
- `init XMIN XMAX YMIN YMAX STARTX STARTY STARTANGLE` -> Initialisiert die Zeichenfläche mit den angegebenen Koordinaten und der Startposition der Schildkröte
- `penup` -> Hebt den Stift der Schildkröte
- `pendown` -> Senkt den Stift der Schildkröte
- `forward DISTANCE` -> Bewegt die Schildkröte um die angegebene Distanz vorwärts
- `backward DISTANCE` -> Bewegt die Schildkröte um die angegebene Distanz rückwärts
- `left ANGLE` -> Dreht die Schildkröte um den angegebenen Winkel nach links
- `right ANGLE` -> Dreht die Schildkröte um den angegebenen Winkel
- `color RGB[A]` -> Setzt die Farbe des Stifts der Schildkröte auf die angegebene Farbe, definiert durch die RGB-Werte und optional einem Alpha-Wert für die Transparenz
- `text TEXT [FONT]` -> Zeichnet den angegebenen Text an der aktuellen Position der Schildkröte, optional mit einer Schriftart
- `width WIDTH` -> Setzt die Breite des Stifts der Schildkröte auf die angegebene Breite
- `push` -> Speichert die aktuelle Position und Ausrichtung der Schildkröte auf einem Stack
- `pop` -> Stellt die zuletzt gespeicherte Position und Ausrichtung der Schildkröte wieder her
- `timeline` -> Erzeugt einen Slider im Browser, mit dem durch den Ausführungsverlauf der Turtle-Befehle navigiert werden kann
- `save NAME` -> Speichert die Turtle-Grafik unter dem angegebenen Namen als SVG-Datei

**Ausgabe:** Die Turtle-Grafik als SVG-Text

#### Test
Dieses Kommando ermöglicht es, Java-Code zu testen, indem es die Ausgabe des Codes mit einer erwarteten Ausgabe vergleicht.

**Eingabe:**
-  `Send CODE` -> Java-Code, der ausgewertet werden soll
- `Expect STRING` -> Erwartete Ausgabe des Java-Codes
- `Type TYPE` -> Optionaler Parameter, der den Typ des Vergleichs angibt:
  - `Exact` (Standard): Erwartete Ausgabe muss genau mit der tatsächlichen Ausgabe übereinstimmen (Equals)
  - `same`: Erwartete Ausgabe muss mit der tatsächlichen Ausgabe identisch sein (==)
  - `oneof`: Erwartete Ausgabe muss in der tatsächlichen Ausgabe enthalten sein (contains)

**Ausgabe:** Die Zusammenfassung und das Ergebnis des Tests.

#### Button
**Eingabe:**
- `Text: TEXT` -> Text, der auf dem Button angezeigt wird
- `[width: WIDTH]` -> Optionaler Parameter, der die Breite des Buttons angibt
- `[height: HEIGHT]` -> Optionaler Parameter, der die Höhe des Buttons angibt
- `path: PATH` -> Pfad zu einer Quelldatei, in der Code ausgetauscht werden soll, wenn der Button geklickt wird
- `label: "LABEL"` -> Kommentarlabel, das den Bereich in der Quelldatei markiert, der ausgetauscht werden soll
- `replacement: REPLACEMENT` -> Der Code, der in die Quelldatei eingesetzt werden soll, wenn der Button geklickt wird.

**Ausgabe:** HTML-Text für einen Button mit dazugehörigem JavaScript, der die angegebene Ersetzung in der Quelldatei vornimmt, wenn der Button geklickt wird.

#### Input
**Eingabe:**
- `path: PATH` -> Pfad zu einer Quelldatei, in der Code ausgetauscht werden soll, wenn eine Eingabe bestätigt wird
- `label: "LABEL"` -> Kommentarlabel, das den Bereich in der Quelldatei markiert, der ausgetauscht werden soll
- `template: TEMPLATE` -> Das Template, in das die Eingabe des Input-Feldes eingesetzt werden soll. Der Platzhalter `$` im Template wird durch die Eingabe ersetzt. Das Resultat wird in die Quelldatei eingesetzt.
- `[placeholder: PLACEHOLDER]` -> Optionaler Parameter, der einen Platzhaltertext für das Eingabefeld angibt
- `[type: TYPE]` -> Optionaler Parameter, der den Typ des Eingabefelds angibt (z.B. "text", "number", "email")

**Ausgabe:** HTML-Text für ein Eingabefeld mit dazugehörigem JavaScript, der die angegebene Ersetzung in der Quelldatei vornimmt, wenn eine Eingabe bestätigt wird.

#### Checkbox
**Eingabe:**
- `path: PATH` -> Pfad zu einer Quelldatei, in der Code ausgetauscht werden soll, wenn die Checkbox aktiviert oder deaktiviert wird
- `label: "LABEL"` -> Kommentarlabel, das den Bereich in der Quelldatei markiert, der ausgetauscht werden soll
- `template: TEMPLATE` -> Das Template, in das der aktuelle Wert der Checkbox eingesetzt werden soll. Der Platzhalter `$` im Template wird durch den aktuellen Wert der Checkbox (true oder false) ersetzt. Das Resultat wird in die Quelldatei eingesetzt.

**Ausgabe:** HTML-Text für eine Checkbox mit dazugehörigem JavaScript, der die angegebene Ersetzung in der Quelldatei vornimmt, wenn die Checkbox aktiviert oder deaktiviert wird.

### Scankommandos
Scankommandos werden genutzt, um Informationen zurück an die Quelle zu senden.

#### CommandScan
**Eingabe:** Keine (Kann nur durch Piping einen Input erhalten)

**Ausgabe:** Das Kommando empfängt die Ausgabe des vorherigen Kommandos und sendet sie zurück an die Quelle, die das Kommando ursprünglich ausgeführt hat.

#### InputScan
**Eingabe:** Keine

**Ausgabe:** Das Kommando erzeugt ein Eingabefeld im Browser und sendet die Benutzereingabe zurück an die Quelle, die das Kommando ursprünglich ausgeführt hat.

## Troubleshooting

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