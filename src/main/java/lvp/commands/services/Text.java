package lvp.commands.services;

import java.util.HashMap;
import java.util.Map;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;

public class Text {
    private Text() {}
    static Map<String, String> templates = new HashMap<>();

    public static void clear() {
        templates.clear();
    }

    public static String codeblock(String id, String content) {
        String[] parts = content.split(";");
        if (parts.length != 2) {
            Logger.logError("Invalid Codeblock Format.");
            return null;
        }
        return TextUtils.codeBlock(parts[0].strip(), parts[1].strip());
    }

    public static String of(String id, String content) {
        String newValue = templates.merge(id, content, TextUtils::linearFillOut);
        return newValue == null ? content : newValue;
    }
}
