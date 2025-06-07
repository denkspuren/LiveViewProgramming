package lvp.skills;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

import lvp.Clerk;

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
        return Text.fillOut("<button style='padding: 2px 15px;' onclick='${0}'>${1}</button>", onClick, text);
    }

    public static String slider(String id, double min, double max, double value, String onInput) {
        return Text.fillOut("<input type='range' id='slider${0}' min='${1}' max='${2}' value='${3}' step='any' oninput='${4}'/>",
                id, min, max, value, onInput);
    }

    public static String input(String path, String label, String template, String placeholder) {
        return input(Path.of(path), label, template, placeholder);
    }
    public static String input(Path path, String label, String template, String placeholder) {
        String id = Clerk.generateID(10);
        String inputField = Text.fillOut("<input style='padding: 5px; margin: 0 5px 0 0;' type='text' id='input${0}' placeholder='${1}' />", id, placeholder);
        String button = button("Send", Text.fillOut("""
            (() => {
                const input = document.getElementById("input${0}");
                const result = "${3}".replace("$", input.value);
                fetch("interact", { method: "post", body: "${1}:${2}:single:" + btoa(String.fromCharCode(...new TextEncoder().encode(result))) }).catch(console.error);
            })()
            """, id, 
                Base64.getEncoder().encodeToString(path.normalize().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8)),
                template));
        return inputField + button;
    }

    
}
