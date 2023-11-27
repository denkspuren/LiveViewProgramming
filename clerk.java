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
    static String preFileName = "./preIndex.html",
                  postFileName = "./postIndex.html",
                  resultFileName = "./index.html";
    static List<String> preIndexHTML;
    static List<String> postIndexHTML;
    static List<String> contentHTML = new ArrayList<>();
    static {
        setUp();
    }
    static void readPrePost() {
        try {
            preIndexHTML = Files.readAllLines(Path.of(preFileName));
            postIndexHTML = Files.readAllLines(Path.of(postFileName));
        } catch (IOException e) {
            System.err.printf("Error reading %s\n", e.getMessage());
        }
    }
    static void writeResult() {
        List<String> resultHTML = List.of(preIndexHTML, contentHTML, postIndexHTML).
                                       stream().flatMap(List::stream).toList();
        try {
            Files.write(Path.of(resultFileName), resultHTML);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
        }
    }
    static String cutOut(String fileName, String... labels) {
        List<String> snippet = new ArrayList<>();
        boolean skipLines = true;
        boolean isInLabels;
        try {
            List<String> lines = Files.readAllLines(Path.of(fileName));
            for (String line : lines) {
                isInLabels = Arrays.stream(labels).anyMatch(label -> line.trim().equals(label));
                if (isInLabels) { 
                    skipLines = !skipLines;
                    continue;
                }
                if (skipLines) continue;
                snippet.add(line);
            }
        } catch (IOException e) {
            System.err.printf("Error reading %s\n", e.getMessage());
        }
        return snippet.stream().collect(Collectors.joining("\n")) + "\n";
    }
    static void refresh() {
        contentHTML = new ArrayList<>();
        writeResult();
    }
    static void setUp() {
        readPrePost();
        refresh();
    }
    static void write(String html) {
        contentHTML.add(html);
        writeResult();
    }
    static void write(Object obj) {
        write("<code><pre>" + String.valueOf(obj) + "</pre></code>");
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
    String ID;
    int width, height;
    Turtle(int width, int height) {
        this.width = width;
        this.height = height;
        ID = Clerk.generateID(6);
        Clerk.write(STR.
        """
        <canvas id="turtleCanvas\{ID}" width="\{width}" height="\{height}" style="border:1px solid #000;"></canvas>
        <script src="./Turtle/turtle.js"></script>
        <script>
            // const canvas = document.getElementById('turtleCanvas');
            const turtle\{ID} = new Turtle(document.getElementById('turtleCanvas\{ID}'));
        </script>
        """);
    }
    Turtle() {
        this(500, 500);
    }
    Turtle penDown() {
        Clerk.write(STR.
        """
        <script>
            turtle\{ID}.penDown();
        </script>
        """);
        return this;
    }
    Turtle penUp() {
        Clerk.write(STR.
        """
        <script>
            turtle\{ID}.penUp();
        </script>
        """);
        return this;
    }
    Turtle forward(double distance) {
        Clerk.write(STR.
        """
        <script>
            turtle\{ID}.forward(\{String.valueOf(distance)});
        </script>
        """);
        return this;
    }
    Turtle backward(double distance) {
        Clerk.write(STR.
        """
        <script>
            turtle\{ID}.backward(\{String.valueOf(distance)});
        </script>
        """);
        return this;
    }
    Turtle left(double degrees) {
        Clerk.write(STR.
        """
        <script>
            turtle\{ID}.left(\{String.valueOf(degrees)});
        </script>
        """);
        return this;
    }
    Turtle right(double degrees) {
        Clerk.write(STR.
        """
        <script>
            turtle\{ID}.right(\{String.valueOf(degrees)});
        </script>
        """);
        return this;
    }
}
