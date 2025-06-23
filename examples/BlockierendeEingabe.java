import static java.io.IO.println;

import java.util.Scanner;

void main() {
    println("Clear: ~");
    println("Markdown: # Blocking Input");
    println("Read:");
    Scanner scanner = new Scanner(System.in);
    String d = scanner.nextLine();

    println("Markdown: Your input was: **" + d + "**");
}