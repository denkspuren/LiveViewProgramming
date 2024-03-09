# 🤔 Überlegungen zur Überarbeitung

#### ✅ Realisierung eines HTTP-Servers

Das Entscheidenste bleibt die Realisierung eines HTTP-Server. Hier hat @kuchenkruste eine Websocket-Implementierung auf den Weg gebracht (siehe [hier](proposals/src/)). Derzeit favorisiere ich eine wesentlich schlankere Lösung mit Server Sent Events (SSE), wozu @RamonDevPrivate einen ersten Entwurf vorgelegt hat.

#### 🔍 Object Inspector

@RamonDevPrivate hat [hier](https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee) schon ganze Arbeit geleistet. Der Code wird ebenfalls in Clerk eingebaut werden. Mittlerweile frage ich mich allerdings, wie man Clerk modular erweitern kann, am besten zur Laufzeit.

#### 🔍 Testing Framework

Dieses [minimalistische Testing-Framework](https://gist.github.com/denkspuren/c379cd6d4512144e595d1dab98bba5ff) soll ebenso Bestandteil von Clerk werden.

#### 💡 JShell-Ausgaben abgreifen und testen

Was Clerk derzeit fehlt, ist, dass man Code der JShell zur Ausführung vorlegt und die REPL-Antwort bekommt, die man zusätzlich auf Richtigkeit überprüfen kann.

#### 💡 Generisches JavaScript-Interface

Man kann die Klasse `Turtle` auch ganz anders gestalten, generischer -- damit aber auch fehlerträchtiger -- beim Aufruf der JavaScript-Methoden. In etwa so mit einer Command-Methode `cmd`:

```java
Turtle cmd(String methodName, Object... args) {
    String argsString = Arrays.stream(args).
                               map(o -> String.valueOf(o)).
                               collect(Collectors.joining(","));
    Clerk.script(STR."turtle\{ID}.\{methodName}(\{argsString})");
}
```

Die Idee kann man im Hinterkopf behalten.

#### ✅ `cutOut` erweitern

Die Methode `cutOut` gefällt mir, sie kann sehr flexibel Zeilen aus einer Textdatei ausschneiden. Wenn die Zeile mit einem Label nicht per default übersprungen, sondern mit ausgeschnitten werden soll, benötigt es eines boolschen Flags. Mir scheint es zu genügen, wenn es zwei boolsche Flags gibt: eines für den Anfang eines Ausschneidevorgangs (`includeStartLabel`), eines für das Ende eines Ausschneidevorgangs (`includeEndLabel`). Der Methodenkopf sähe dann wie folgt aus:

```java
static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels)
```

Nützlich ist das z.B., wenn man einen Methodenkopf als StartLabel und am Ende der Methode in einer Zeile darunter ein `// end` als EndLabel verwendet:

```
Clerk.cutOut("clerk.java", true, false, "static String cutOut(", "// end");
```

> Die Erweiterung ist umgesetzt und funktioniert. Sie hat sogar die Implementierung der Methode `readFile` obsolet gemacht, sie ist jetzt nur noch ein Sonderfall der `cutOut`-Methode.

```java
static String readFile(String fileName) {
    return cutOut(fileName, true, true, "");
}
```

#### 🤷 Markdown als Klasse ausgliedern

Die `markdown`-Methode sollte wie Turtle als eigenständiger Aspekt ausgelagert werden, ebenso wie es mit der Klasse `Turtle` geschehen ist.

> Im Moment stelle ich das zurück. Die Auslagerung als Aspekt macht eher dann Sinn, wenn man die Markdown-Verarbeitung irgendwie konfigurieren können möchte.

#### 🤷 Mit `Clerk`-Instanzen oder Targets arbeiten

Eventuell macht es Sinn, mehrere Instanzen von Clerk anlegen zu können oder verschiedene Targets angeben zu können: Was soll über den HTTP-Server raus, was in eine Datei geschrieben werden.

Zum Beispiel könnte man eine Markdown-Datei `text.md` erzeugen und gleichzeitig eine HTML-Datei `text.md.html`, so dass man sich das gerenderte Ergebnis im Browser anschauen kann. So eine Art der Anwendung könnte mir [Markdeep](https://casual-effects.com/markdeep/) überflüssig machen. Wenn man mehrere Targets hat, könnte man in der gleichen Java-Datei sowohl eine Aufgabe stellen mit einer Erzeugung der Dateien `exercise.md` und `exercise.md.html` und einer gesonderten Erzeugung der Dateien `solution.md` und `solution.md.html`.

Die Idee der Targets gefällt mir im Moment etwas besser. Clerk könnte auch Abhängigkeiten der Targets berücksichtigen: Targets, in die gleichzeitig geschrieben wird. Target-Aktivitäten, die die gleiche oder eine andere Aktivität bei einem anderen Target auslösen.

> Mit mehreren Instanzen arbeiten zu können, halte ich für sinnvoll. Aber ich bin im Begriff, die Idee zu verwerfen, in eine Textdatei zu dokumentieren. Könnte man nicht stattdessen den DOM im Browser auslesen und das Ergebnis abspeichern?

