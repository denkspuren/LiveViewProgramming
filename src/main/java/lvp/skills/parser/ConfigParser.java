package lvp.skills.parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lvp.skills.logging.Logger;

public class ConfigParser {
    public record Source(Path path, String cmd) {
        public String id() {
            return Base64.getEncoder().encodeToString(path().toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static final Pattern OBJECT_PATTERN = Pattern.compile(
        "\\{\\s*\"path\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"cmd\"\\s*:\\s*\"(.*?)\"\\s*\\},?"
    );

    public static Optional<List<Source>> parse(Path path) {
        try {
            String content = Files.readString(path).strip();
            return  parseJson(content);
        } catch (IOException e) {
            Logger.logError("Error reading file: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private static Optional<List<Source>> parseJson(String json) {
        if (!json.startsWith("[") || !json.endsWith("]")) {
            Logger.logError("Expected JSON array.");
            return Optional.empty();
        }

        String arrayContent = json.substring(1, json.length() - 1).strip();
        if (arrayContent.isEmpty()) {
            Logger.logError("JSON array empty.");
            return Optional.empty();
        }

        Matcher matcher = OBJECT_PATTERN.matcher(arrayContent);
        List<Source> sources = new ArrayList<>();

        StringBuilder cleaned = new StringBuilder();
        
        while (matcher.find()) {
            String pathString = matcher.group(1);
            Optional<List<Path>> paths = PathParser.parse(pathString);
            if (paths.isEmpty()) {
                Logger.logError("Invalid Path in JSON: " + pathString);
                return Optional.empty();
            }

            String cmd = matcher.group(2);
            sources.addAll(paths.get().stream()
                .map(path -> new Source(path, cmd))
                .toList());
            matcher.appendReplacement(cleaned, "");
        }
        matcher.appendTail(cleaned);

        String remaining = cleaned.toString().replaceAll("[\\s]*", "");
        if (!remaining.isEmpty()) {
            Logger.logError("Unexpected content in JSON: " + remaining);
            return Optional.empty();
        }
        return sources.isEmpty() ? Optional.empty() : Optional.of(sources);
    }
}
