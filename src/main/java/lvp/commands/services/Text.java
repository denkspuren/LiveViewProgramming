package lvp.commands.services;

import java.util.HashMap;
import java.util.Map;

import lvp.Processor.MetaInformation;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;

public class Text {
    private Text() {}
    static Map<String, String> templates = new HashMap<>();

    public static void clear() {
        templates.clear();
    }

    public static String codeblock(MetaInformation meta, String content) {
        String[] parts = content.split(";");
        if (parts.length != 2) {
            Logger.logError("Invalid Codeblock Format.");
            return null;
        }
        return TextUtils.codeBlock(parts[0].strip(), parts[1].strip());
    }

    public static String of(MetaInformation meta, String content) {
        String existing = templates.get(meta.id());
        if (existing == null || meta.standalone() && !content.isBlank()) {
            templates.put(meta.id(), content);
            return content;
        }

        if (content.isBlank()) {
            return existing;
        }

        return TextUtils.linearFillOut(existing, content);
    }
}
