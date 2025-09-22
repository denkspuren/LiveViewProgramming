import java.util.Scanner;

/// Register with:
/// Register: Reverse java --enable-preview external.java

void main() {
    Scanner scanner = new Scanner(System.in);
    String id = scanner.nextLine();
    String content = scanner.nextLine();

    println(new StringBuilder(content).reverse().toString());
}