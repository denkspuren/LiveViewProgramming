package lvp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

import lvp.commands.services.Text;
import lvp.commands.services.Test;
import lvp.commands.services.Turtle;
import lvp.commands.services.Interaction;
import lvp.commands.targets.Targets;
import lvp.commands.targets.Targets.MetaInformation;
import lvp.skills.HTMLElements;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;
import lvp.skills.parser.InstructionParser;
import lvp.skills.parser.InstructionParser.Command;
import lvp.skills.parser.InstructionParser.CommandRef;
import lvp.skills.parser.InstructionParser.Pipe;
import lvp.skills.parser.InstructionParser.Scan;
import lvp.skills.parser.InstructionParser.Register;
public class Processor {
    Server server;
    Targets targetProcessor;
    Map<String, BiConsumer<MetaInformation, String>> targets;
    Map<String, BiFunction<String, String, String>> services = new HashMap<>(Map.of(
            "Text", Text::of, 
            "Codeblock", Text::codeblock, 
            "Turtle", Turtle::of,
            "Button", Interaction::button,
            "Input", Interaction::input,
            "Checkbox", Interaction::checkbox,
            "Test", Test::test));

    public Processor(Server server) {
        this.server = server;
        targetProcessor = Targets.of(server);
        targets = Map.of(
            "Markdown", targetProcessor::consumeMarkdown, 
            "Dot", targetProcessor::consumeDot,
            "Html", targetProcessor::consumeHTML, 
            "JavaScript", targetProcessor::consumeJS, 
            "JavaScriptCall", targetProcessor::consumeJSCall,
            "Css", targetProcessor::consumeCss,
            "SubViewStyle", targetProcessor::consumeSubViewStyle,
            "Clear", targetProcessor::consumeClear);
    }

    void process(Process process, String sourceId) {
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            InstructionParser.parse(reader.lines()).gather(Gatherers.fold(() -> "", (prev, curr) ->
                switch (curr) {
                    case Command cmd -> processCommands(cmd, sourceId);
                    case Pipe pipe -> processPipe(pipe, prev, sourceId);
                    case Scan scan -> processScan(scan, process, sourceId);
                    case Register register -> processRegister(register);
                    default -> null;
                })).forEachOrdered(_->{});
                
        }
        catch (Exception e) {
            Logger.logError("Error reading process output: " + e.getMessage(), e);
        }
    }

    String processCommands(Command command, String sourceId) {
        Logger.logDebug("Command: " + command.name() + "{" + command.id() + "}, " + command.content());
        
        if (targets.containsKey(command.name())) {
            targets.get(command.name()).accept(new MetaInformation(sourceId, command.id()), command.content());
        }
        else if (services.containsKey(command.name())) {
            return services.get(command.name()).apply(command.id(), command.content());
        } else {
            Logger.logError("Command not found: " + command.name());
        }

        return null;
    }

    String processPipe(Pipe pipe, String input, String sourceId) {
        if (input == null) return null;
        String current = input;
        for (CommandRef ref : pipe.commands()) {
            Logger.logDebug("Command: " + ref.name() + "{" + ref.id() + "}, " + current);
            if (targets.containsKey(ref.name())) {
                targets.get(ref.name()).accept(new MetaInformation(sourceId, ref.id()), current);
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

    String processScan(Scan scan, Process process, String sourceId) {
        server.waitingProcesses.put(sourceId, process);
        String inputField = HTMLElements.input("input" + scan.id());
        String button = HTMLElements.button("button" + scan.id(), "Send", TextUtils.fillOut("""
                (()=>{
                    const input = document.getElementById("input${0}");
                    fetch("scan", { method: "post", body: "${1}:" + btoa(String.fromCharCode(...new TextEncoder().encode(input.value))) }).catch(console.error);
                })()
                """, scan.id(), sourceId));
        targetProcessor.consumeHTML(new MetaInformation(sourceId, scan.id()), inputField + button);
        return null;
    }

    String processRegister(Register register) {
        services.put(register.name(), (id, content) -> {
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            String out = null;
            try {
                Logger.logInfo("Executing " + register.call());
                ProcessBuilder pb = new ProcessBuilder(isWindows ? new String[]{"cmd.exe", "/c", register.call()} : new String[]{"sh", "-c", register.call()})
                    .redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedWriter writer = new BufferedWriter(
                       new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    if (!register.skipId()) writer.write(id + "\n");
                    writer.write(content + "\n");
                    writer.flush();
                }
                try (var reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    out = reader.lines().collect(Collectors.joining("\n"));
                }
                boolean finished = process.waitFor(10, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    Logger.logError("Timeout: process " + register.name() + " killed");
                }
            } catch (Exception e) {
                Logger.logError("Error in " + register.name(), e);
            }
            return out;
        });
        return null;
    }

    void init(String sourceId) {
        server.clearEvents(sourceId);
        server.sendServerEvent(SSEType.CLEAR, "", "", sourceId);
        Text.clear();
    }
    
}
