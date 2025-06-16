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

import lvp.skills.logging.Logger;

public class TextUtils { // Class with static methods for file operations
    private TextUtils(){}

    
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

    public static String linearFillOut(String template, String replacement) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}"); // `${<key>}`
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        String key = "";

        while (matcher.find()) {
            String group = matcher.group(1);
            if (key.isBlank()) key = group;
            if (!key.equals(group)) continue;
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    

    public enum ReplacementType {
        SINGLE, MULTI, BLOCK
    }

    public static void updateFile(String path, String label, ReplacementType rType, String replacement) {
        try {
            Path filePath = Path.of(path);
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            switch (rType) {
                case SINGLE:
                    updateSingleLine(lines, label, replacement);
                    break;
                case MULTI:
                    updateMultiLine(lines, label, replacement);
                    break;
                default:
                    break;
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.logError("Error updating file: " + path, e);
        }
    }

    private static void updateSingleLine(List<String> lines, String label, String replacement) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().endsWith(label)) {
                String line = lines.get(i);
                int spaces = (int) IntStream.range(0, line.length())
                    .takeWhile(pos -> line.charAt(pos) == ' ')
                    .count();
                lines.set(i, " ".repeat(spaces) + replacement + " " + label);
            }
        }
    }

    private static void updateMultiLine(List<String> lines, String label, String replacement) {
        int openingLabel = -1;
        int closingLabel = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(label)) {
                if (openingLabel == -1) {
                    openingLabel = i;
                } else {
                    closingLabel = i;
                    break;
                }
            }
        }
        if (openingLabel == -1 || closingLabel == -1) {
            Logger.logError("Labels not found for multi-line replacement: " + label);
            return;
        }
        if (closingLabel <= openingLabel) {
            Logger.logError("Closing label is before opening label for multi-line replacement: " + label);
            return;
        }
        String startingLine = lines.get(openingLabel + 1);
        int spaces = (int) IntStream.range(0, startingLine.length())
                    .takeWhile(pos -> startingLine.charAt(pos) == ' ')
                    .count();

        for (int i = openingLabel + 1; i < closingLabel; i++) {
            lines.remove(openingLabel + 1);
        }
        lines.add(openingLabel + 1, " ".repeat(spaces) + replacement);
    }
}
