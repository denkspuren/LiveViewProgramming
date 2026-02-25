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
            ## Kommandoinhalte durch Pipelines reichen
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
        
        Register: Reverse java --enable-preview ./examples/ExternerService.java
        Text[reverse-text]:
        So kann man natürlich auch eigene Kommandos erstellen, die dann in LVP genutzt werden können. In diesem Fall wird ein Kommando registriert, das einen Text umkehrt. Das Kommando erwartet den Text als Eingabe und gibt den umgekehrten Text als Ausgabe zurück.
        ~~~
        | Reverse | Markdown        
        """);

        //TODO: Turtle Beispiel

        //TODO: Interaktive HTML Elemente Beispiel
    
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
