
Clerk.markdown(STR."""
# Die Code-Dokumentation mit Markdown

Für die Code-Dokumentation mit Markdown sind String-Templates und der Text-Skill entscheidende Hilfsmittel. Zum einen erlauben sie, Code in Markdown einzubinden, zum anderen, den Gebrauch von "spitzen" Klammern (`<>`) zu ermöglichen.
""");

// Fakultätsfunktion
long factorial(int n) {
    assert n >= 0 : "Positive Ganzzahl erforderlich";
    if (n == 0 || n == 1) return n;
    return n * factorial(n - 1);
}
// Testfälle
assert factorial(0) == 1 && factorial(1) == 1;
assert factorial(2) == 2 && factorial(3) == 6;
assert factorial(4) == 24 && factorial(5) == 120;
// Ende Fakultätsfunktion

// STR-Beispiel
int num;
String s = STR."Die Fakultät von \{num = 6} ist \{factorial(num)}."
// STR-Beispiel


Clerk.markdown(STR."""
## String-Templates mit eingebetteten Ausdrücken

Mit Java 21 haben String-Templates als Preview-Feature Einzug in Java gehalten ‒ mehr Informationen zu diesem neuen Sprachkonstrukt finden sich [hier](https://docs.oracle.com/en/java/javase/22/language/string-templates.html). Mit String-Templates lassen sich Ausdrücke zur Auswertung in einen String einbinden.

Ein Beispiel: Der Template-Prozessor `STR` bekommt einen String übergeben mit zwei eingebetteten Ausdrücken, die jeweils durch `\\{` und `}`markiert sind. Die Ergebnisse der Auswertung werden vom Template-Prozessor in den resultierenden String eingefügt.

```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "// STR-Beispiel")}
```

Das Ergebnis der Zeichenkette ist

```
\{s}
```

String-Templates bieten die Basis, um auf einfache Weise ganze Textauszüge in den Markdown-Text, der mit `Clerk.markdown()` erzeugt wird, einzubinden.
""");

Clerk.markdown(STR."""
## Texte ausschneiden mit `Text.cut`

Mit dem Skill `Text` kann Text aus einer Datei ausgeschnitten werden. Der Methodenkopf von `cutOut` erwartet einen Dateinamen, zwei boolsche Werte und eine beliebige Anzahl an Labels.

```
static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels)
```

Labels sind Zeichenketten, nach denen als vollständige Textzeile in der angegebenen Datei gesucht wird. Mit den boolschen Werten wird angegeben, ob das öffnende bzw. schliessende Label beim Ausschnitt mit inkludiert, d.h. einbezogen werden soll oder nicht.

### Beispiele

Nehmen wir eine Datei mit folgendem Inhalt:

```text
// LabelA
1. Textstelle, gerahmt von einem LabelA und einem LabelB
// LabelB
// LabelC
Textstelle, umschlossen von einem LabelC
// LabelC
// LabelA
2. Textstelle, gerahmt von einem LabelA und einem LabelB
// LabelB
```

Mit dem folgenden Aufruf

```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "<!-- LabelCff -->").replaceAll("(\\n)?```(\\n)?","")}
```

ergibt sich

<!-- LabelCff -->
```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", false, false, "// LabelC")}
```
<!-- LabelCff -->

Setzt man einen der boolschen Werte auf `true`, wird das entsprechende Label mit übernommen.

```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "<!-- LabelCft -->").replaceAll("(\\n)?```(\\n)?","")}
```

Das Ergebnis sieht so aus:

<!-- LabelCft -->
```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", false, true, "// LabelC")}
```
<!-- LabelCft -->

Sind mehrere Stellen mit dem gleichen Label belegt, kann man diese Bereiche ausschneiden. Wenn die boolschen Werte beide `false` sind, kann man den Aufruf verkürzen.

```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "<!-- LabelAB -->").replaceAll("(\\n)?```(\\n)?","")}
```

Zunächst wird die erste Textstelle zwischen `LabelA` und `LabelB` ausgeschnitten, dann die zweite.

<!-- LabelAB -->
```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "// LabelA", "// LabelB")}
```
<!-- LabelAB -->

### Der Algorithmus

Der Algorithmus zu `Text.cutOut(...)`, um einen Ausschnitt aus einer Textdatei, ein Snippet davon zu erstellen, funktioniert wie folgt:

0. Starte im Modus, die Textzeilen einer Datei zu überspringen: `skipLines = true`.
1. Gehe die Datei Textzeile für Textzeile durch
2. Wenn die Textzeile einem Label entspricht, dann gehe wie folgt vor: (a) Wenn entweder `skipLines` und `includeStartLabel` wahr sind, oder wenn `!skipLines` und `includeEndLabel` wahr sind, dann ergänze die Labelzeile zum Snippet. (b) Wechsel den Modus `skipLines = !skipLines` und gehe zur nächsten Textzeile, d.h. zum Anfang von Schritt 2.
3. Entspricht die Textzeile keinem Label, dann: (a) Füge die Zeile nur dann dem Snippet hinzu, wenn `skipLines` nicht wahr ist. (b) Gehe zur nächsten Textzeile, d.h. zu Schritt 2.

Als Java-Methode:

```java
\{Text.cutOut("skills/Text/Text.java", "// Cut out a snippet", "// done")}
```

""");
