package lvp.commands.targets.dot;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public record GraphSpec(OptionalInt width, OptionalInt height, String dot) {

    public static GraphSpec fromContent(String content) {
        OptionalInt width = OptionalInt.empty();
        OptionalInt height = OptionalInt.empty();

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

    private static OptionalInt tryInt(String s) {
        try {
            return OptionalInt.of(Integer.parseInt(s.trim()));
        } catch (NumberFormatException _) {
            return OptionalInt.empty();
        }
    }
}