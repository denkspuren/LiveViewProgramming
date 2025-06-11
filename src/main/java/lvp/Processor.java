package lvp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Gatherers;

import lvp.InstructionParser.Command;
import lvp.InstructionParser.CommandRef;
import lvp.InstructionParser.Pipe;
import lvp.logging.Logger;
import lvp.skills.IdGen;
public class Processor {
    Server server;
    Map<String, BiFunction<String, String, String>> services = new HashMap<>(Map.of("Text", this::text));
    Map<String, BiConsumer<String, String>> targets = Map.of(
            "Markdown", this::consumeMarkdown, 
            "Html", this::consumeHTML, 
            "JavaScript", this::consumeJS, 
            "JavaScriptCall", this::consumeJSCall, 
            "Clear", this::consumeClear);
    Map<String, String> templates = new HashMap<>();

    public Processor(Server server) {
        this.server = server;
    }

    void process(Process process) {
        templates.clear();
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            InstructionParser.parse(reader.lines()).gather(Gatherers.fold(() -> "", (prev, curr) ->
                switch (curr) {
                    case Command cmd -> processCommands(cmd);
                    case Pipe pipe -> processPipe(pipe, prev);
                    default -> null;
                })).findFirst();
        }
        catch (Exception e) {
            Logger.logError("Error reading process output: " + e.getMessage());
        }
    }

    String processCommands(Command command) {
        Logger.logDebug("Command: " + command.name() + "{" + command.id() + "}, " + command.content());
        
        if (targets.containsKey(command.name())) {
            targets.get(command.name()).accept(command.id(), command.content());
        }
        else if (services.containsKey(command.name())) {
            return services.get(command.name()).apply(command.id(), command.content());
        } else {
                Logger.logError("Command not found: " + command.name());
        }

        return null;
    }

    String processPipe(Pipe pipe, String input) {
        if (input == null) return null;
        String current = input;
        for (CommandRef ref : pipe.commands()) {
            Logger.logDebug("Command: " + ref.name() + "{" + ref.id() + "}, " + current);
            if (targets.containsKey(ref.name())) {
                targets.get(ref.name()).accept(ref.id() == null ? IdGen.generateID(10) : ref.id(), current);
                return null;
            }
            else if (services.containsKey(ref.name())) {
                current = services.get(ref.name()).apply(ref.id(), current);
            } else {
                Logger.logError("Command not found: " + ref.name());
            }
        }
        return current;
    }

    void init() {
        server.events.clear();
    }

    String text(String id, String content) {
        String newValue = templates.merge(id, content, this::fillOut);
        return newValue == null ? content : newValue;
    }

    String fillOut(String template, String replacement) {
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

    void consumeClear(String id, String content) {
        server.sendServerEvent(SSEType.CLEAR, "");
    }
    
    void consumeHTML(String id, String content) {
        server.sendServerEvent(SSEType.WRITE, content);
    }

    void consumeJS(String id, String content) {
        server.sendServerEvent(SSEType.SCRIPT, content);
    }

    void consumeJSCall(String id, String content) {
        server.sendServerEvent(SSEType.CALL, content);
    }

    void consumeMarkdown(String id, String content) {
        consumeHTML(id, "<script id='" + id + "' type='preformatted'>" + content + "</script>");
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        
        consumeJSCall(id, "var scriptElement = document.getElementById('" + id  + "');"
        +
        """
        var divElement = document.createElement('div');
        divElement.id = scriptElement.id;
        divElement.innerHTML = window.md.render(scriptElement.textContent);
        scriptElement.parentNode.replaceChild(divElement, scriptElement);
        """
        );
    }
    
}
