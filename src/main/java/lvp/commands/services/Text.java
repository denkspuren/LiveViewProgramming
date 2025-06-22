package lvp.commands.services;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lvp.Processor.MetaInformation;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;

public class Text {
    private Text() {}
    static Map<String, String> memory = new ConcurrentHashMap<>();

    public static void clear(String sourceId) {
        Iterator<String> iterator = memory.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.startsWith(sourceId + ":")) {
                iterator.remove();
            }
        }
    }

    public static String codeblock(MetaInformation meta, String content) {
        String[] parts = content.split(";");
        if (parts.length != 2) {
            Logger.logError("(" + meta.id() + ") Invalid Codeblock Format.");
            return null;
        }
        return TextUtils.codeBlock(parts[0].strip(), parts[1].strip());
    }

    public static String of(MetaInformation meta, String content) {
        String existing = memory.get(meta.sourceId() + ":" + meta.id());
        if (existing == null || meta.standalone() && !content.isBlank()) {
            memory.put(meta.sourceId() + ":" + meta.id(), content);
            return content;
        }

        if (content.isBlank()) {
            return existing;
        }

        return TextUtils.linearFillOut(content, existing);
    }
}
