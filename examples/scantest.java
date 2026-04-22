import java.util.Scanner;

void main() {
    
    Scanner scanner = new Scanner(System.in);
    println("Clear");
    println("Markdown[test]: # Hello World");
    println("""
            Text: Hello There
            | CommandScan
            """);
    String input = scanner.nextLine();
    println("Markdown: " + input);
    println("""
            InputScan
            """);
    String newInput = scanner.nextLine();
    println("Markdown: " + newInput);
    println("""
            InputScan
            """);
    String newInput2 = scanner.nextLine();
    println("Markdown: " + newInput2);
}