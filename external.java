import static java.io.IO.println;

import java.util.Scanner;

void main() {
    Scanner scanner = new Scanner(System.in);
    String id = scanner.nextLine();
    String content = scanner.nextLine();

    println(new StringBuilder(content).reverse().toString());
}