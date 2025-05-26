package lvp.skills;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

public class Interaction {
    public static String eventFunction(String path, String label, String replacement) {
        return eventFunction(Path.of(path), label, replacement);
    }
    public static String eventFunction(Path path, String label, String replacement) {
        return Text.fillOut("fetch(\"interact\", { method: \"post\", body: \"${0}:${1}:single:${2}\" }).catch(console.error);", 
                Base64.getEncoder().encodeToString(path.normalize().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(replacement.getBytes(StandardCharsets.UTF_8)));
    }

    public static String button(String text, int width, int height, String onClick) {
        return Text.fillOut("<button style='width: ${0}px; height: ${1}px;' onclick='${2}'>${3}</button>", width, height, onClick, text);
    }

    public static String button(String text, String onClick) {
        return button(text, 200, 50, onClick);
    }

    public static String slider(String id, double min, double max, double value, String onInput) {
        return Text.fillOut("<input type='range' id='slider${0}' min='${1}' max='${2}' value='${3}' step='any' oninput='${4}'/>",
                id, min, max, value, onInput);
    }

    
}
