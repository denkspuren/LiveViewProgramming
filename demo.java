import static java.lang.IO.println;
List<String> obst = List.of("Apfel", "Birne", "Banane");
void main() {
    println("""
            Clear
            Markdown: # Wie nutze ich LVP?
            Markdown:
            Das neue LVP Protokoll bietet viele Möglichkeiten, um Textausgaben zu transformieren und
            daraus mediale Darstellungen im Browser anzeigen zu lassen. Diese Demo soll einen Überblick über die Möglichkeiten geben, die LVP bietet. Hier sieht man wie statische Markdown Texte erstellt werden können, die dann im Browser angezeigt werden. Diese können einzeilig sein, oder auch mehrzeilig, wie hier. Wesentlich spannender ist aber die Möglichkeit, dynamische Inhalte zu erstellen, wie im folgenden Beispiel zu sehen ist.
            ~~~

            Markdown:
            """);
    println(buildObstListe());
    println("~~~");

    println("""
            Markdown:
            ## Pipes
            Ergebnisse von Kommandos können durch Pipelines an andere Kommandos weitergereicht werden, um diese weiter zu verarbeiten. Das ist aber nur für Kommandos möglich, die auch tatsächlich eine Ausgabe haben. Das Markdown Kommando hat zum Beispiel keine Ausgabe, die weiterverarbeitet werden könnte, da es sein Ergebnis direkt im Browser anzeigt.
            ~~~
            """);

    
    println("""
            Text[template]:
            ## Beispiel
            Im folgenden Beispiel sieht man, wie das Text Kommando genutzt wird, um ein Template zu erstellen. Das Kommando selbst erzeugt keine Anzeige im Browser. Es ermöglicht aber, Platzhalter zu definieren, die dann durch die Pipeline mit ${0} gefüllt werden können.
            ~~~
            | Markdown

            Text: Inhalten
            | Text[template] | Markdown
            """);
              

    println("""
            Markdown:
            Man sieht hier auch, dass sich das Text Kommando Inhalte merken kann, indem man eine ID vergibt, in diesem Fall "template". So kann ein Template mehrmals benutzt werden.
            ~~~
            """);


    println("""
        Text[t2]:
        Jetzt fehlt nur noch die Dokumentation des Codes. Dazu kann das Codeblock Kommando genutzt werden. Es erstellt Codeblöcke, die im Browser angezeigt werden und dort auch bearbeitet werden können. Damit das funktioniert, muss das Ergebnis dieses Kommandos in einem Markdown Codeblock eingebettet werden.

        ```java
        ${0}
        ```
        ~~~
        
        Codeblock:./demo.java;// example
        | Text[t2] | Markdown
        """);

    println("""
        Register[skipId]: Counter wc
        Text[wc-text]:
        ## Externe Kommandos
        In diesem Beispiel wird gezeigt, wie LVP um externe Kommandos erweitert werden kann. Durch die Register Anweisung können beliebige Kommandos registriert werden, die dann in LVP genutzt werden können. In diesem Fall wird das Kommando "wc" registriert, das die Anzahl der Wörter in einem Text zählt. Das Kommando erwartet den Text als Eingabe und gibt die Anzahl der Wörter als Ausgabe zurück.
        </br>
        Das Ergebnis: ${0}
        ~~~
        | Counter | Text[wc-text] | Markdown

        Markdown: ### Eigene Kommandos registrieren
        
        Register: Reverse java -Dsun.stdout.encoding=UTF-8 ./examples/ExternerService.java
        Text[reverse-text]:
        So kann man natürlich auch eigene Kommandos erstellen, die dann in LVP genutzt werden können. In diesem Fall wird ein Kommando registriert, das einen Text umkehrt. Das Kommando erwartet den Text als Eingabe und gibt den umgekehrten Text als Ausgabe zurück.
        ~~~
        | Reverse | Markdown        
        """);

        
    println("""
            Markdown:
            ## Turtle
            Nun zum spannendsten Teil, der Turtle. Mit diesem Kommando können Grafiken erstellt werden, die im Browser angezeigt werden.
            Das Turtle Kommando erwartet eine Reihe von Anweisungen, die dann in eine Grafik umgesetzt werden. Es gibt verschiedene Anweisungen, wie zum Beispiel "forward", "left", "right" und "color". Mit diesen
            Anweisungen können Linien gezeichnet und die Farbe geändert werden. Der Zeichenbereich wird durch "init" definiert, das die Größe des Zeichenbereichs und die Startposition der Turtle festlegt. 
            In diesem Beispiel wird ein Dreieck gezeichnet, indem die Turtle 25 Einheiten vorwärts bewegt und dann um 120 Grad nach links gedreht wird. Das wird dreimal wiederholt, um ein Dreieck zu zeichnen.
            Gleichzeitig wird die Farbe der Turtle auf Grün gesetzt, indem die "color" Anweisung genutzt wird.
            Normalerweise kann man die Anweisungen direkt in das Turtle Kommando schreiben, aber hier wird wieder ein Template genutzt, um die Anweisungen zu definieren. So können die Anweisungen mehrmals verarbeitet werden.
            ~~~

            Text[turtle-example]:
            init 0 200 0 26 50 1 0
            """
            +
            "color 37 255 37 1" // turtle color
            +
            """
            
            forward 25
            left 120
            forward 25
            left 120
            forward 25
            timeline
            ~~~
            | Turtle | Html

            Markdown:
            Zu Erst werden die Anweisungen an das Turtle Kommando weitergegeben, welches daraus eine SVG Grafik erstellt, die dann durch das Html Kommando im Browser angezeigt wird.
            Danach werden die Anweisungen direkt an Markdown weitergegeben. Hier sieht man, dass die Zeilenumbrüche nicht erhalten bleiben, wodurch die Anweisungen nicht gut lesbar sind.
            Daher werden die gleichen Anweisungen in einen Markdown Codeblock eingebettet.
            ~~~

            Text[turtle-example]
            | Markdown

            Text[turtle-codeblock]:
            ```
            ${0}
            ```
            ~~~

            Text[turtle-example]
            | Text[turtle-codeblock] | Markdown
            """);

    println("""
            Markdown:
            ### Interaktive Codebearbeitung durch Buttons
            Interaktive Elemente, wie Buttons, können genutzt werden, um Code zu verändern. In diesem Beispiel werden zwei Buttons erstellt, die die Farbe der Turtle ändern,
            indem sie die Farbanweisung im Java Code ersetzen. Das funktioniert, indem die Anweisung, die ersetzt werden soll, mit einem Label versehen wird, 
            in diesem Fall `// turtle color`. Wenn der Button geklickt wird, wird die Anweisung durch die Anweisung ersetzt, die im "replacement" Feld definiert ist.

            ~~~
            Button:
            Text: Green
            width: 200
            height: 50
            path: demo.java
            label: "// turtle color"
            replacement: "color 37 255 37 1"
            ~~~
            | Html

            Button:
            Text: Red
            width: 200
            height: 50
            path: demo.java
            label: "// turtle color"
            replacement: "color 255 37 37 1"
            ~~~
            | Html
            """);

    int n = 55; // input
    boolean b = true; // bool
    println("""
            Markdown:
            ## Interaktive Html Eingabefelder
            Neben Buttons können auch andere interaktive Elemente, wie Eingabefelder oder Checkboxen genutzt werden, um Code zu verändern. Das funktioniert ähnlich wie bei den Buttons, indem
            die Anweisung, die ersetzt werden soll, mit einem Label versehen wird, und die neue Anweisung im "replacement" Feld definiert wird. Anders als bei Buttons, die nur eine
            vordefinierte Anweisung einsetzen können, können bei Eingabefeldern auch Werte aus dem Eingabefeld genutzt werden. Zu diesem Zweck können Platzhalter in der Replacement Anweisung definiert
            werden, die dann durch die Eingabe ersetzt werden. 
            ~~~

            Input:
            path: demo.java
            label: "// input"
            placeholder: Enter a number
            template: int n = $;
            type: text
            ~~~
            | Html

            Checkbox:
            path: demo.java
            label: "// bool"
            template: boolean b = $;
            """
            +
            "checked:" + b
            +
            """

            ~~~
            | Html
            """);

    
    println("""
            Markdown:
            ## Graphen mit Dot
            Mit dem Dot Kommando können Graphen erstellt werden, die im Browser angezeigt werden. Das Dot Kommando erwartet eine Beschreibung des Graphen in der Dot Sprache, die dann in eine Grafik umgesetzt wird.
            ~~~
            Dot:
            digraph G {
                A -> B;
                B -> C;
            }
            ~~~
            """);

    println("""
            Markdown:
            ## Fazit
            Das LVP Protokoll bietet viele Möglichkeiten, um Textausgaben zu transformieren und daraus mediale Darstellungen im Browser anzeigen zu lassen. 
            Hier wurden nur einige Beispiele gezeigt, aber es gibt noch viele weitere Möglichkeiten, wie zum Beispiel das Definieren von blockierenden Eingaben, das Testen von Code Snippets
            oder das Einbinden von CSS. Einige weitere Beispiele sind im `examples` Ordner zu finden. Auch gibt es die Möglichkeit durch das Nutzen von mehreren Quellen die Browseransicht
            in mehrere sogennanter "Subviews" aufzuteilen. Eine Übersicht aller Kommandos ist im `README.md` zu finden.
            ~~~
            """);
}

// example
String buildObstListe() {
    String out = "";
    for (String o : obst) {
        out += "- **" + o + "**\n";
    }
    return out;
}
// example
