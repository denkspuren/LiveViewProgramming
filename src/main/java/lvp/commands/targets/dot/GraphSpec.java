package lvp.commands.targets.dot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record GraphSpec(Optional<Integer> width, Optional<Integer> height, String dot) {

    public static GraphSpec fromContent(String content) {
        Optional<Integer> width = Optional.empty();
        Optional<Integer> height = Optional.empty();

        List<String> dotLines = new ArrayList<>();

        for (String line : content.lines().toList()) {
            String trimmed = line.trim();
            if (trimmed.startsWith("width:")) {
                width = tryInt(trimmed.substring(6));
            } else if (trimmed.startsWith("height:")) {
                height = tryInt(trimmed.substring(7));
            } else {
                dotLines.add(line);
            }
        }

        String dot = String.join(" ", dotLines).trim();
        return new GraphSpec(width, height, dot);
    }

    private static Optional<Integer> tryInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s.trim()));
        } catch (NumberFormatException _) {
            return Optional.empty();
        }
    }
}