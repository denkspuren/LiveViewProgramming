package lvp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import lvp.sinks.Sink;
import lvp.skills.Scan;
import lvp.skills.TriConsumer;
import lvp.skills.logging.Logger;
import lvp.skills.parser.InstructionParser;
import lvp.skills.parser.InstructionParser.*;
import lvp.transformer.*;
public class Processor {
    public record MetaInformation(String sourceId, String id, boolean standalone) {}
    
    Map<String, BiConsumer<MetaInformation, String>> channel = new HashMap<>();
    Map<String, BiFunction<MetaInformation, String, String>> transformer = new HashMap<>(Map.of(
            "Text", Text::of, 
            "Codeblock", Text::codeblock,
            "Cutout", Text::cutout,
            "Turtle", Turtle::of,
            "Test", Test::test));
    Map<String, TriConsumer<MetaInformation, Process, String>> scans = new HashMap<>(Map.of(
        "CommandScan", this::consumeCommandScan
    ));
    List<Sink> sinks = new ArrayList<>();

    void process(Process process, String sourceId) {
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            
            InstructionParser.parse(reader.lines()).gather(Gatherers.fold(() -> "", (prev, curr) ->
                    switch (curr) {
                        case Command cmd -> processCommands(cmd, sourceId, process);
                        case Pipe pipe -> processPipe(pipe, prev, sourceId, process);
                        case Register register -> processRegister(register);
                        case Unknown unknown -> processUnknown(unknown, sourceId);
                        default -> null;
                    })).forEachOrdered(_->{});              
        }
        catch (Exception e) {
            Logger.logError("Error reading process output: " + e.getMessage(), e);
        }
    }

    String processCommands(Command command, String sourceId, Process process) {
        Logger.logDebug("Command: " + command.name() + "{" + command.id() + "}, " + command.content());
        MetaInformation meta = new MetaInformation(sourceId, command.id(), true);

        return executeCommand(command.name(), command.content(), meta, process);
    }

    String processPipe(Pipe pipe, String input, String sourceId, Process process) {
        String current = input;
        for (CommandRef ref : pipe.commands()) {
            Logger.logDebug("Command: " + ref.name() + "{" + ref.id() + "}, " + current);
            if (current == null) return null;
            MetaInformation meta = new MetaInformation(sourceId, ref.id(), false);

            current = executeCommand(ref.name(), current, meta, process);
        }
        return current;
    }

    String executeCommand(String name, String content, MetaInformation meta, Process process) {
        if (channel.containsKey(name)) {
            channel.get(name).accept(meta, content);
            return null;
        }
        else if (transformer.containsKey(name)) {
            return transformer.get(name).apply(meta, content);
        }
        else if (scans.containsKey(name))  {
            scans.get(name).accept(meta, process, content);
            return null;
        }
        else {
            Logger.logError("Command not found: " + name);
            sinks.forEach(s -> s.error(meta, name + content));
        }
        return null;
    }

    String consumeCommandScan(MetaInformation meta, Process process, String prev) {
        if (prev != null) {
            try {
                Scan.sendToSource(process, prev);
            } catch (IOException e) {
                Logger.logError("Error while writing in OutputStream for " + meta.sourceId(), e);
            }
        } else {
            Logger.logError("No previous command output to can.");
        }
        return null;
    }

    String processRegister(Register register) {
        transformer.put(register.name(), (meta, content) -> {
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            String out = null;
            try {
                Logger.logInfo("Executing " + register.call());
                ProcessBuilder pb = new ProcessBuilder(isWindows ? new String[]{"cmd.exe", "/c", register.call()} : new String[]{"sh", "-c", register.call()})
                    .redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedWriter writer = new BufferedWriter(
                       new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    if (!register.skipId()) writer.write(meta.id() + "\n");
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

    String processUnknown(Unknown unknown, String sourceId) {
        sinks.forEach(s -> s.error(new MetaInformation(sourceId, "", true), unknown.message()));
        return null;
    }

    void init(String sourceId) {
        sinks.forEach(s -> s.clear(sourceId));
        Text.clear(sourceId);
    }

    void registerSink(Sink sink) {
        channel.putAll(sink.registerChannel());
        transformer.putAll(sink.registerTransformer());
        scans.putAll(sink.registerScan());
        sinks.add(sink);
    }
    
}
