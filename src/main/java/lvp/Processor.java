package lvp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import lvp.logging.Logger;
import lvp.skills.IdGen;
public class Processor {
    Server server;
    Map<String, Function<String, String>> services = new HashMap<>(Map.of("Text", content -> content));
    Map<String, Consumer<String>> targets = Map.of(
            "Markdown", this::consumeMarkdown, 
            "Html", this::consumeHTML, 
            "JavaScript", this::consumeJS, 
            "JavaScriptCall", this::consumeJSCall, 
            "Clear", this::consumeClear);

    public Processor(Server server) {
        this.server = server;
    }

    void process(Process process) {
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                String commandName = "";
                String content = "";
                boolean inBlock = false;
                while ((line = reader.readLine()) != null) {
                    Logger.logDebug("(Source) " + line);                    
                    if (!inBlock) {
                        int i = line.trim().indexOf(":");
                        if (i == -1) {
                            Logger.logError("(Source) " + line);
                            continue;
                        }
                        String name = line.trim().substring(0, i);
                        if (name.equals("Register")) {
                            //TODO: Register
                            continue;
                        } else if (!services.containsKey(name) && name.equals("Text") && !targets.containsKey(name)) {
                            Logger.logError("CommandName not found: " + name);
                            continue;
                        }
                        commandName = name;

                        if (line.trim().length() == name.length() + 1) {
                            inBlock = true;
                            continue;
                        }

                        content = line.trim().substring(i + 1);
                        if (targets.containsKey(commandName)) targets.get(commandName).accept(content);
                        else services.get(commandName).apply(content);
                        content = "";
                    } else {
                        if (line.trim().equals("~~~")) {
                            inBlock = false;
                            if (targets.containsKey(commandName)) targets.get(commandName).accept(content);
                            else services.get(commandName).apply(content);
                            content = "";
                            continue;
                        }

                        content += line + '\n';
                    }
                }                
            }
        catch (Exception e) {
            Logger.logError("Error reading process output: " + e.getMessage());
        }
    }

    void init() {
        server.events.clear();
    }

    void consumeClear(String content) {
        server.sendServerEvent(SSEType.CLEAR, "");
    }
    
    void consumeHTML(String content) {
        server.sendServerEvent(SSEType.WRITE, content);
    }

    void consumeJS(String content) {
        server.sendServerEvent(SSEType.SCRIPT, content);
    }

    void consumeJSCall(String content) {
        server.sendServerEvent(SSEType.CALL, content);
    }

    void consumeMarkdown(String content) {
        String ID = IdGen.generateID(10);
        consumeHTML("<script id='" + ID + "' type='preformatted'>" + content + "</script>");
        // Using `preformatted` is a hack to get a Java String into the Browser without interpretation
        
        consumeJSCall("var scriptElement = document.getElementById('" + ID  + "');"
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
