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
            
            """);
}