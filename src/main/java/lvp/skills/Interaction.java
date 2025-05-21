package lvp.skills;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

public class Interaction {
    public static String eventFunction(Path path, String label, String replacement) {
        return String.format("fetch('interact', { method: 'post', body: `%s:%s:%s` }).catch(console.error);", 
                Base64.getEncoder().encodeToString(path.normalize().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(replacement.getBytes(StandardCharsets.UTF_8)));
    }

    public static String button(String text, int width, int height, String onClick) {
        return String.format("<button style=\"width: %dpx; height: %dpx;\" onclick=\"%s\">%s</button>", width, height, onClick, text);
    }

    
}
