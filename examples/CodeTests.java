import static java.io.IO.println;

void main() {
    println("""
            Clear: -
            Markdown: # Test Demo
            Text[0]:
            ```
            ${0}
            ```
            ~~~
            Test[0]:
            Send: 1 + 1
            2 + 2
            Expect: 
            2
            4
            ~~~
            | Text[0] | Markdown
            
            Text[FactorialMethod]:
            Send: ${0}
            factorial(5)
            Expect: 120
            ~~~

            Cutout: examples/CodeDokuMitMarkdown.java; // Fakultätsfunktion
            | Text[FactorialMethod] | Test | Text[0] | Markdown

            Test:
            Send: 2 + 2
            int j = 0;
            for(int i = 0; i < $1; i++) {
                j = 2 * i;
            }
            j
            Expect: 6
            Type: oneof
            ~~~
            | Text[0] | Markdown

            """);
}