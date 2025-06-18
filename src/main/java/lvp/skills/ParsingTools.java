package lvp.skills;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.OptionalInt;

public class ParsingTools {
    private ParsingTools() {}

    public static OptionalInt tryInt(String s) {
        try {
            return OptionalInt.of(Integer.parseInt(s.strip()));
        } catch (NumberFormatException _) {
            return OptionalInt.empty();
        }
    }

    public static Optional<Path> tryPath(String s) {
        try {
           return Optional.of(Path.of(s));
        } catch (InvalidPathException _) {
            return Optional.empty();
        }
    }

    public static String stripQuotes(String s) {
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1).strip();
        }
        return s;
    }
}
