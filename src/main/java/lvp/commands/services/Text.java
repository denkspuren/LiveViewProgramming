package lvp.commands.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lvp.logging.Logger;
import lvp.skills.TextUtils;

public class Text {
    private Text() {}
    static Map<String, String> templates = new HashMap<>();

    public static void clear() {
        templates.clear();
    }

    public static String codeblock(String id, String content) {
        String[] parts = content.split(":");
        if (parts.length != 2) {
            Logger.logError("Invalid Codeblock Format.");
            return null;
        }
        return TextUtils.codeBlock(parts[0].trim(), parts[1].trim());
    }

    public static String of(String id, String content) {
        String newValue = templates.merge(id, content, Text::fillOut);
        return newValue == null ? content : newValue;
    }

    static String fillOut(String template, String replacement) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}"); // `${<key>}`
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        String key = "";

        while (matcher.find()) {
            String group = matcher.group(1);
            if (key.isBlank()) key = group;
            if (!key.equals(group)) continue;
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
