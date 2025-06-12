package lvp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Gatherers;

import lvp.InstructionParser.Command;
import lvp.InstructionParser.CommandRef;
import lvp.InstructionParser.Pipe;
import lvp.commands.services.Text;
import lvp.commands.services.Turtle;
import lvp.commands.targets.Targets;
import lvp.logging.Logger;
public class Processor {
    Server server;
    Targets targetProcessor;
    Map<String, BiFunction<String, String, String>> services = new HashMap<>(Map.of("Text", Text::of, "Codeblock", Text::codeblock, "Turtle", Turtle::of));
    Map<String, BiConsumer<String, String>> targets;

    public Processor(Server server) {
        this.server = server;
        targetProcessor = Targets.of(server);
        targets = Map.of(
            "Markdown", targetProcessor::consumeMarkdown, 
            "Dot", targetProcessor::consumeDot,
            "Html", targetProcessor::consumeHTML, 
            "JavaScript", targetProcessor::consumeJS, 
            "JavaScriptCall", targetProcessor::consumeJSCall, 
            "Clear", targetProcessor::consumeClear);
    }

    void process(Process process) {
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            InstructionParser.parse(reader.lines()).gather(Gatherers.fold(() -> "", (prev, curr) ->
                switch (curr) {
                    case Command cmd -> processCommands(cmd);
                    case Pipe pipe -> processPipe(pipe, prev);
                    default -> null;
                })).forEachOrdered(_->{});
        }
        catch (Exception e) {
            Logger.logError("Error reading process output: " + e.getMessage(), e);
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
                targets.get(ref.name()).accept(ref.id(), current);
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
        Text.clear();
    }
    
}
