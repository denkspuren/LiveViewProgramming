package lvp.commands.services;

import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import lvp.skills.HTMLElements;
import lvp.skills.ParsingTools;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;

public class Interaction {
    private Interaction() {}
    public static String button(String id, String content) {
        Map<String, String> fields = content.lines()
            .filter(line -> !line.isBlank())
            .map(line -> line.split(":", 2))
            .filter(parts -> parts.length == 2)
            .map(parts -> Map.entry(parts[0].strip().toLowerCase(), parts[1].strip()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String text = fields.get("text");
        String pathString = fields.get("path");
        String label = fields.get("label");
        String replacement = fields.get("replacement");

        if (text == null || pathString == null || label == null || replacement == null) {
            Logger.logError("Missing required button field (text, path, label, or replacement)");
            return null;
        }

        Optional<Path> path = ParsingTools.tryPath(pathString);
        if (path.isEmpty()) {
            Logger.logError("Invalid path in button command");
            return null;
        }
        OptionalInt width = ParsingTools.tryInt(fields.get("width"));
        OptionalInt height = ParsingTools.tryInt(fields.get("height"));

        Logger.logDebug("Parsed button with text=" + text + ", path=" + path + ", label=" + label + ", size=" + 
            (width.isPresent() ? width.getAsInt() + "x" + height.getAsInt() : "default"));

        String func = eventFunction(path.get(), ParsingTools.stripQuotes(label), replacement);

        return width.isPresent() || height.isPresent()
            ? HTMLElements.button(id, text, width.orElse(height.getAsInt()), height.orElse(width.getAsInt()), func)
            : HTMLElements.button(id, text, func);
    }

    public static String input(String id, String content) {
        Map<String, String> fields = content.lines()
            .filter(line -> !line.isBlank())
            .map(line -> line.split(":", 2))
            .filter(parts -> parts.length == 2)
            .map(parts -> Map.entry(parts[0].strip().toLowerCase(), parts[1].strip()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String pathString = fields.get("path");
        String label = fields.get("label");
        String template = fields.get("template");
        String placeholder = fields.getOrDefault("placeholder", "");
        String type = fields.getOrDefault("type", "text");

        if (pathString == null || label == null || template == null) {
            Logger.logError("Missing required field(s): path, label, and template are mandatory.");
            return null;
        }

        Optional<Path> path = ParsingTools.tryPath(pathString);
        if (path.isEmpty()) {
            Logger.logError("Invalid path in input command");
            return null;
        }

        Logger.logDebug("Parsed input with path=" + path + ", label=" + label + ", type=" + type);
        String inputElement = HTMLElements.input("input" + id, placeholder, type, ParsingTools.stripQuotes(label).replaceFirst("//", "").strip());
        String button = HTMLElements.button("button" + id, "Send", TextUtils.fillOut("""
            (() => {
                const input = document.getElementById("input${0}");
                const result = `${3}`.replace("$", input.value);
                fetch("interact", { method: "post", body: "${1}:${2}:single:" + btoa(String.fromCharCode(...new TextEncoder().encode(result))) }).catch(console.error);
            })()
            """, id, 
                Base64.getEncoder().encodeToString(path.get().normalize().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(ParsingTools.stripQuotes(label).getBytes(StandardCharsets.UTF_8)),
                template));
        return inputElement + button;
    }

    public static String checkbox(String id, String content) {
         Map<String, String> fields = content.lines()
            .filter(line -> !line.isBlank())
            .map(line -> line.split(":", 2))
            .filter(parts -> parts.length == 2)
            .map(parts -> Map.entry(parts[0].strip().toLowerCase(), parts[1].strip()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String pathString = fields.get("path");
        String label = fields.get("label");
        String template = fields.get("template");

        if (pathString == null || label == null || template == null) {
            Logger.logError("Missing required checkbox field (path, label, or template)");
            return null;
        }

        Optional<Path> path = ParsingTools.tryPath(pathString);
        if (path.isEmpty()) {
            Logger.logError("Invalid path in checkbox command");
            return null;
        }

        boolean checked = Boolean.parseBoolean(fields.getOrDefault("checked", "false"));

        Logger.logDebug("Parsed checkbox with path=" + path + ", label=" + label + ", checked=" + checked);
        return HTMLElements.checkbox(id, ParsingTools.stripQuotes(label).replaceFirst("//", "").strip(), checked, TextUtils.fillOut("""
                (() => {
                    const result = `${2}`.replace("$", this.checked);
                    fetch("interact", { method: "post", body: "${0}:${1}:single:" + btoa(String.fromCharCode(...new TextEncoder().encode(result))) }).catch(console.error);
                })()
                """, Base64.getEncoder().encodeToString(path.get().normalize().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(ParsingTools.stripQuotes(label).getBytes(StandardCharsets.UTF_8)),
                template));
    }

    private static String eventFunction(Path path, String label, String replacement) {
        return TextUtils.fillOut("fetch(\"interact\", { method: \"post\", body: \"${0}:${1}:single:${2}\" }).catch(console.error);", 
                Base64.getEncoder().encodeToString(path.normalize().toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString(replacement.getBytes(StandardCharsets.UTF_8)));
    }
}
