import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Text { // Class with static methods for file operations
    static void write(String fileName, String text) {
        try {
            Files.writeString(Path.of(fileName), text);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
            System.exit(1);
        }
    }

    // core method
    static String cutOut(Path path, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        List<String> snippet = new ArrayList<>();
        boolean skipLines = true;
        boolean isInLabels;
        try {
            List<String> lines = Files.readAllLines(path);
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
                if (skipLines)
                    continue;
                snippet.add(line);
            }
        } catch (IOException e) {
            System.err.printf("Error reading %s\n", e.getMessage());
            System.exit(1);
        }
        return snippet.stream().collect(Collectors.joining("\n"));
    }
    // end

    static String cutOut(Path path, String... labels) { return cutOut(path, false, false, labels); }
    static String read(Path path) { return cutOut(path, true, true, ""); }

    static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        return cutOut(Path.of(fileName), includeStartLabel, includeEndLabel, labels);
    }
    static String cutOut(String fileName, String... labels) {
        return cutOut(fileName, false, false, labels);
    }
    static String read(String fileName) {
        return cutOut(fileName, true, true, "");
    }

    static String escapeHtml(String text) {
        return text.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
    }
}
