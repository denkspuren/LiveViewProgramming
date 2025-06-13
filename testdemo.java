void main() {
    println("""
            Clear: -
            Markdown: # Test Demo
            Text{0}:
            ```
            ${0}
            ```
            ~~~
            Test{0}:
            Send: int i = 2;
            Expect: 2
            ~~~
            | Text{0} | Markdown
            
            """);
}