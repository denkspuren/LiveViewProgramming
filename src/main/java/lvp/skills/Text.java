package lvp.skills;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Text { // Class with static methods for file operations
    private Text(){}
    
    public static void write(String fileName, String text) {
        try {
            Files.writeString(Path.of(fileName), text);
        } catch (IOException e) {
            System.err.printf("Error writing %s\n", e.getMessage());
            System.exit(1);
        }
    }

    // core method
    public static String cutOut(Path path, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
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

    public static String cutOut(Path path, String... labels) { return cutOut(path, false, false, labels); }
    public static String read(Path path) { return cutOut(path, true, true, ""); }

    public static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels) {
        return cutOut(Path.of(fileName), includeStartLabel, includeEndLabel, labels);
    }
    public static String cutOut(String fileName, String... labels) {
        return cutOut(fileName, false, false, labels);
    }
    public static String read(String fileName) {
        return cutOut(fileName, true, true, "");
    }

    public static String codeBlock(String fileName, String label) {
        return fillOut("""
                src-info: ${0}:${1}:multi |||
                ${2}
                """, Base64.getEncoder().encodeToString(fileName.getBytes(StandardCharsets.UTF_8)), Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8)), cutOut(fileName, label));
    }

    // Method `fillOut` emulates String interpolation, since String Templates
    // have been removed in Java 23 (they were a preview feature in Java 21 and 22).

    public static String fillOut(Map<String, Object> replacements, String template) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}"); // `${<key>}`
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            if (!replacements.containsKey(key))
                System.err.println("WARNING: key \"" + key + "\" not found in template:\n" + template);
            Object replacement = replacements.getOrDefault(key, "${" + key + "}"); 
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String fillOut(String template, Map<String, Object> replacements) {
        return fillOut(replacements, template);
    }

    public static String fillOut(String template, Object... replacements) {
        Map<String, Object> m = new HashMap<>();
        IntStream.range(0, replacements.length)
            .forEach(i -> m.put(Integer.toString(i), replacements[i]));
        return fillOut(m, template);
    }
}
