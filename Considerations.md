# 🤔 Überlegungen zur Überarbeitung

#### 🔍 Realisierung eines HTTP-Servers

Das Entscheidenste bleibt die Realisierung eines HTTP-Server.

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

#### ✅ String Templates besser ausnutzen

Derzeit benutze ich ein `preIndex.html` und ein `postIndex.html`. Vermutlich täte es auch ein `indexTemplate.html` mit einem Template-Ausdruck (_template expression_) `\{content}`, was die Gesamtkomposition der `index.html` erleichtern würde.

```html
<!DOCTYPE html>
<meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0">
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="2">
    <title>Clerk in Java Prototype</title>
</head>
<body>
<!-- begin include content -->
\{content}
<!-- end include content -->
</body>
</html>
```

Konsequenterweise ist es überflüssig, `preIndexHTML` und `postIndexHTML` und wohl auch `contentHTML` vom Typ `List<String>` vorzuhalten. Es genügt dann ein `String indexHTML` zu haben und ein `contentHTML` vom Typ `StringBuilder`, da hier beständig HTML ergänzt wird; aber auch da kann man vielleicht auch gut mit einem Template-Ausdruck arbeiten. Vielleicht gibt es auch einen netten Trick, die `indexHTML` mit einem Template-Ausdruck "offen" für Ergänzungen zu halten.

> Das habe ich umgesetzt; der Code ist lesbarer und gleichzeitig geschrumpt.

#### ✅ `setUp` und `refresh` "doppelt"

Die `refresh`-Methode ist eigentlich überflüssig, `setUp` genügt. Ob man die Methode dann noch einmal umbenennt, ist zu überlegen.

> Umgesetzt; es gibt nur noch die Methode `setUp`.

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

#### 🤷 `script`-Methode überflüssig

Die Kasse `Turtle` nutzt die `script`-Methode noch nicht, was den Code verkürzen würde. Aber sinnvoller wäre eine `write`-Methode, die ein Tag selber schließt und so für _balanced tags_ sorgt:

```java
static void write(String openingTag, String content) {
    Pattern pattern = Pattern.compile("<(\\w+).*>");
    Matcher matcher = pattern.matcher(openingTag);
    if (!matcher.matches())
        System.err.printf("Invalid opening tag: %s\n", openingTag);
    write(STR.
    """
    \{openingTag}
      \{content}
    \{matcher.matches() ? "</" + matcher.group(1) + ">" : "</???>"}
    """);
}
```

Damit lassen sich keine verschachtelten Tags realisieren, da direkt `write` aufgerufen wird. Sinnvoller könnte eine `tag`-Methode sein, die einen String zurückgibt und innerhalb eines `write` verwendet werden kann.

```java
static String htmlTag(String openingTag, String content) {
    Pattern pattern = Pattern.compile("<(\\w+).*>");
    Matcher matcher = pattern.matcher(openingTag);
    if (!matcher.matches())
        System.err.printf("Invalid opening tag: %s\n", openingTag);
    return STR.
    """
    \{openingTag}
    \{content}
    \{matcher.matches() ? "</" + matcher.group(1) + ">" : "</???>"}
    """;
}
```

> Die Idee hat sich in einem ersten Versuch als nicht notwendig ergeben.

#### 🤷 Markdown als Klasse ausgliedern

Die `markdown`-Methode sollte wie Turtle als eigenständiger Aspekt ausgelagert werden, ebenso wie es mit der Klasse `Turtle` geschehen ist.

> Im Moment stelle ich das zurück. Die Auslagerung als Aspekt macht eher dann Sinn, wenn man die Markdown-Verarbeitung irgendwie konfigurieren können möchte.

#### 🤷 Mit `Clerk`-Instanzen oder Targets arbeiten

Wie ich schon im [README.md](README.md) erwähne, macht es eventuell Sinn, mehrere Instanzen von Clerk anlegen zu können oder verschiedene Targets angeben zu können: Was soll über den HTTP-Server raus, was in eine Datei geschrieben werden.

Zum Beispiel könnte man eine Markdown-Datei `text.md` erzeugen und gleichzeitig eine HTML-Datei `text.md.html`, so dass man sich das gerenderte Ergebnis im Browser anschauen kann. So eine Art der Anwendung könnte mir [Markdeep](https://casual-effects.com/markdeep/) überflüssig machen. Wenn man mehrere Targets hat, könnte man in der gleichen Java-Datei sowohl eine Aufgabe stellen mit einer Erzeugung der Dateien `exercise.md` und `exercise.md.html` und einer gesonderten Erzeugung der Dateien `solution.md` und `solution.md.html`.

Die Idee der Targets gefällt mir im Moment etwas besser. Clerk könnte auch Abhängigkeiten der Targets berücksichtigen: Targets, in die gleichzeitig geschrieben wird. Target-Aktivitäten, die die gleiche oder eine andere Aktivität bei einem anderen Target auslösen.

> Ohne HTTP-Server im Angebot kann ich im Moment nicht einschätzen, wieviel Sinn das macht.

