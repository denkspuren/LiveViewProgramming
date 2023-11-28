import java.io.IOException;
import static java.lang.StringTemplate.STR;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.*;

// jshell -R-ea --enable-preview

class Clerk {
    static String templateFileName = "./indexTemplate.html",
                  resultFileName = "./index.html";
    static String preContent = "",
                  content = "",
                  postContent = "";
    static {
        setUp();
    }
    static void writeToFile(String fileName, String text) {
        try {
            Files.writeString(Path.of(fileName), text);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
            System.exit(1);
        }
    }
    static void writeResult(String fileName) {
        writeToFile(fileName, STR.
            """
            \{preContent}
            \{content}
            \{postContent}
            """);
    }
    static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        List<String> snippet = new ArrayList<>();
        boolean skipLines = true;
        boolean isInLabels;
        try {
            List<String> lines = Files.readAllLines(Path.of(fileName));
            for (String line : lines) {
                isInLabels = Arrays.stream(labels).anyMatch(label -> line.trim().equals(label));
                if (isInLabels) { 
                    if (skipLines && includeStartLabel)
                        snippet.add(line);
                    if (!skipLines && includeEndLabel)
                        snippet.add(line);
                    skipLines = !skipLines;
                    continue;
                }
                if (skipLines) continue;
                snippet.add(line);
            }
        } catch (IOException e) {
            System.err.printf("Error reading %s\n", e.getMessage());
            System.exit(1);
        }
        return snippet.stream().collect(Collectors.joining("\n")) + "\n";
    }
    static String cutOut(String fileName, String... labels) {
        return cutOut(fileName, false, false, labels);
    }
    static String readFile(String fileName) {
        return cutOut(fileName, true, true, "");
    }
    static void setUp() {
        preContent = cutOut(templateFileName, "<!DOCTYPE html>", "<!-- begin include content -->");
        postContent = cutOut(templateFileName, "<!-- end include content -->");
        content = "";
        writeResult(resultFileName); 
    }
    static void write(String text) {
        content = STR.
        """
        \{content}
        \{text}
        """;
        writeResult(resultFileName);
    }
    static void write(Object obj) {
        write(STR."<code><pre>\{obj}</pre></code>");
    }
    static void script(String code) {
        write(STR.
        """
        <script>
            \{code}
        </script>
        """);
    }
    static String generateID(int n) {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random rand = new Random();
        return IntStream.rangeClosed(1, n).
                         mapToObj(i -> "" + characters.charAt(rand.nextInt(characters.length()))).
                         collect(Collectors.joining());
    }
    static void markdown(String markdown) {
        String ID = generateID(10);
        write(STR.
            """
            <div id="\{ID}">
            \{markdown}
            </div>
            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
            <script>
                var markdownContent = document.getElementById("\{ID}").textContent;
                var renderedHTML = marked.parse(markdownContent);
                document.getElementById("\{ID}").innerHTML = renderedHTML;
            </script>
            """);
    }
}

class Turtle {
    final String ID;
    final int width, height;
    Turtle(int width, int height) {
        this.width = width;
        this.height = height;
        ID = Clerk.generateID(6);
        Clerk.write(STR.
        """
        <canvas id="turtleCanvas\{ID}" width="\{width}" height="\{height}" style="border:1px solid #000;"></canvas>
        <script src="./Turtle/turtle.js"></script>
        <script>
            const turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));
        </script>
        """);
    }
    Turtle() {
        this(500, 500);
    }
    Turtle penDown() {
        Clerk.script(STR."turtle\{ID}.penDown();");
        return this;
    }
    Turtle penUp() {
        Clerk.script(STR."turtle\{ID}.penUp();");
        return this;
    }
    Turtle forward(double distance) {
        Clerk.script(STR."turtle\{ID}.forward(\{distance});");
        return this;
    }
    Turtle backward(double distance) {
        Clerk.script(STR."turtle\{ID}.backward(\{distance});");
        return this;
    }
    Turtle left(double degrees) {
        Clerk.script(STR."turtle\{ID}.left(\{degrees});");
        return this;
    }
    Turtle right(double degrees) {
        Clerk.script(STR."turtle\{ID}.right(\{degrees});");
        return this;
    }
}