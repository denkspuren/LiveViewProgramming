Clerk.markdown("""
# Die Code-Dokumentation mit Markdown

Für die Code-Dokumentation mit Markdown sind Textblöcke und der Text-Skill entscheidende Hilfsmittel.

* Mit Textblöcken lassen sich String-Literale als Textblöcke über mehrere Zeilen hinweg angeben. Ein solcher Textblock beginnt und endet mit drei Anführungszeichen `\"""`.

* Das LVP bringt einen Text-Skill mit, der hauptsächlich dafür da ist,
  - um Text aus einer Datei auszuschneiden (Methode `cutOut`); der Bereich, der ausgeschnitten werden soll, wird durch Textmarken (Labels) ausgewiesen.
  - um Text mit Aufüllfeldern zu versehen (Methode `fillOut`), in die Ergebnisse aus Auswertungen als Zeichenkette eingefügt werden.

> In den Java-Versionen 21 und 22 gab es [String-Templates](https://docs.oracle.com/en/java/javase/22/language/string-templates.html) als Preview-Feature. Damit ließen sich sehr elegant die Auswertungen von Ausdrücken mitten in einen String einfügen. Das wird in anderen Programmiersprachen auch als String-Interpolation bezeichnet. Leider sind die String-Templates mit Java 23 wieder entfernt worden -- ein einmaliger Vorgang für Preview-Features in der Historie von Java. Als leichtgewichtigen Ersatz gibt es deshalb im Text-Skill die statische Methode `fillOut`.

""");

// Fakultätsfunktion
long factorial(int n) {
    assert n >= 0 : "Positive Ganzzahl erforderlich";
    if (n == 1 || n == 0) return 1;
    return n * factorial(n - 1);
}
// Testfälle
assert factorial(0) == 1 && factorial(1) == 1;
assert factorial(2) == 2 && factorial(3) == 6;
assert factorial(4) == 24 && factorial(5) == 120;
// Ende Fakultätsfunktion

// Beispiel
int num;
String s = "Die Fakultät von " + (num = 6) + " ist " + factorial(num) + ".";
// Beispiel


Clerk.markdown(Text.fillOut("""
## Dynamische Inhalte in Zeichenketten einbetten

Wenn Inhalte in einer Zeichenkette dynamisch berechnet und eingefügt werden sollen, kann man das beispielsweise wie folgt machen:

```
${Beispiel}
```

Das Ergebnis der Zeichenkette `s` ist

```
${Resultat}
```

Das sieht dann, wenn man die Zeichenkette im Markdown einfügt (mit `Text.fillOut`), so aus: ${Resultat}

Diese Technik der Einbettung von dynamischen Inhalten in eine Zeichenkette lässt sich ausreizen mit der Text-Skill. Damit kann der Java-Quelltext sich zur Laufzeit selbst ausschneiden zur Einbettung in Markdown! Das ist der Schlüssel zu sich selbst dokumentierendem Programmcode.
""", Map.of("Beispiel", Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", "// Beispiel"),
            "Resultat", s)));

Clerk.markdown(Text.fillOut(Map.of(
"LabelCff",
Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", "// LabelCff"),
"ResultLabelCff",
// LabelCff
Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", false, false, "// LabelC")
// LabelCff
, "LabelCft",
Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", "// LabelCft"),
"ResultLabelCft",
// LabelCft
Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", false, true, "// LabelC")
// LabelCft
, "LabelAB",
Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", "// LabelAB"),
"ResultLabelAB",
// LabelAB
Text.cutOut("views/Markdown/CodeDokuMitMarkdown.java", "// LabelA", "// LabelB")
// LabelAB
, "TextCutOut",
Text.cutOut("skills/Text/Text.java", "// core method", "// end")
), """
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

Der folgende Aufruf

```
${LabelCff}
```

liefert als Zeichenkette diesen Auszug (Snippet) aus der Datei zurück:

```
${ResultLabelCff}
```

> Der Witz an diesem Beispiel ist das, was man hier nicht sieht, aber wichtig für die Idee einer eingebetteten, dynamischen Dokumentation ist: Der obige Aufruf ist tatsächlich ein Snippet von dem Code, der das resultierende Snippet erzeugt. Das klingt ein wenig seltsam, aber das ist genau der Kunstgriff, der garantiert, dass der Aufruf wirklich der ist, der das Ergebnis produziert. Wenn Sie einen Blick in die Java-Datei werfen, die diese View im Browser erzeugt hat, werden Sie das vermutlich verstehen und nachvollziehen können. Vergleichen Sie den Java-Quellcode mit dem Text im Browser.

Setzt man einen der boolschen Werte auf `true`, wird das entsprechende Label mit übernommen.

```
${LabelCft}
```

Das Ergebnis sieht so aus:

```
${ResultLabelCft}
```

Sind mehrere Stellen mit dem gleichen Label belegt, kann man diese Bereiche ausschneiden. Wenn die boolschen Werte beide `false` sind, kann man den Aufruf verkürzen.

```
${LabelAB}
```

Zunächst wird die erste Textstelle zwischen `LabelA` und `LabelB` ausgeschnitten, dann die zweite.

```
${ResultLabelAB}
```

### Der Algorithmus zu `Text.cutOut`

Der Algorithmus zu `Text.cutOut(...)`, um einen Bereich aus einer Textdatei zu schneiden und ein sogenanntes Snippet davon zu erstellen, funktioniert wie folgt:

0. Starte im Modus, die Textzeilen einer Datei zu überspringen: `skipLines = true`.
1. Gehe die Datei Textzeile für Textzeile durch.
2. Wenn die Textzeile einem Label entspricht, dann gehe wie folgt vor: (a) Wenn entweder `skipLines` und `includeStartLabel` wahr sind, oder wenn `!skipLines` und `includeEndLabel` wahr sind, dann ergänze die Labelzeile zum Snippet. (b) Wechsel den Modus `skipLines = !skipLines` und gehe zur nächsten Textzeile (Schritt 1).
3. Entspricht die Textzeile keinem Label, dann: (a) Füge die Zeile nur dann dem Snippet hinzu, wenn `skipLines` nicht wahr ist. (b) Gehe zur nächsten Textzeile (Schritt 1).

Als Java-Methode:

```java
${TextCutOut}
```
"""));
