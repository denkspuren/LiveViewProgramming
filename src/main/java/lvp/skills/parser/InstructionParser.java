package lvp.skills.parser;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.Gatherer.Downstream;

import lvp.skills.IdGen;
import lvp.skills.logging.Logger;

import java.util.stream.Gatherer;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;

public class InstructionParser {

    // ---- Instruction Types ----
    public sealed interface Instruction permits Command, Register, Read, Pipe {}

    public record Command(String name, String id, String content) implements Instruction {}
    public record Register(String name, String call, boolean skipId) implements Instruction {}
    public record Read(String id) implements Instruction {}
    public record Pipe(List<CommandRef> commands) implements Instruction {}

    public record CommandRef(String name, String id) {}

    // ---- Patterns ----
    private static final Pattern SINGLE_LINE_COMMAND = Pattern.compile("^(\\w+)(?:\\{([^}]+)\\})?:\\s*(.+)$");
    private static final Pattern BLOCK_START = Pattern.compile("^(\\w+)(?:\\{([^}]+)\\})?:\\s*$");
    private static final Pattern READ = Pattern.compile("^Read(?:\\{([^}]+)\\})?:\\s*$");
    private static final Pattern REGISTER = Pattern.compile("^Register(?:\\{([^}]+)\\})?:\\s+(\\w+)\\s+(.+)$");
    private static final Pattern PIPE_LINE = Pattern.compile("^\\s*\\|(.+)$");
    private static final Pattern PIPE_ENTRY = Pattern.compile("^(\\w+)(?:\\{([^}]+)\\})?$");

    // ---- Block Parsing State ----
    private static class BlockState {
        String name = null;
        String id = null;
        StringJoiner content = null;
        boolean inBlock = false;

        void init(String name, String id) {
            this.name = name;
            this.id = id;
            this.content = new StringJoiner("\n");
            this.inBlock = true;
            Logger.logDebug("Started block command: " + name + formatFlag(id));
        }

        void append(String line) {
            content.add(line);
        }

        void reset() {
            name = null;
            id = null;
            content = null;
            inBlock = false;
        }
    }

    // ---- Main Entry Point ----
    public static Stream<Instruction> parse(Stream<String> lines) {
        return lines.gather(Gatherer.ofSequential(
            BlockState::new,
            (state, line, downstream) -> {
                handleLine(state, line, downstream);
                return true;
            }
        ));
    }

    // ---- Dispatcher ----
    private static void handleLine(BlockState state, String line, Downstream<? super Instruction> out) {
        if (line.isBlank()) return;
        if (state.inBlock) {
            handleBlockContent(state, line, out);
            return;
        }

        if (tryPipe(line, out)) return;
        if (tryRegister(line, out)) return;
        if (tryRead(line, out)) return;
        if (tryBlockStart(state, line)) return;
        if (trySingleCommand(line, out)) return;

        Logger.logError("Ignored unrecognized line: " + line);
    }

    // ---- Handlers ----

    private static boolean tryPipe(String line, Downstream<? super Instruction>  out) {
        Matcher matcher = PIPE_LINE.matcher(line);
        if (!matcher.matches()) return false;

        List<CommandRef> commands = Arrays.stream(matcher.group(1).split("\\|"))
            .map(String::strip)
            .map(cmd -> {
                Matcher m = PIPE_ENTRY.matcher(cmd);
                if (!m.matches()) {
                    Logger.logError("Invalid pipe format: " + cmd);
                    return null;
                }
                String id = m.group(2) == null ? IdGen.generateID(10) : m.group(2);
                return new CommandRef(m.group(1), id);
            })
            .filter(Objects::nonNull)
            .toList();

        if (!commands.isEmpty()) {
            Logger.logDebug("Parsed pipe: " + commands);
            out.push(new Pipe(commands));
        } else {
            Logger.logError("Pipe instruction without valid commands: " + line);
        }

        return true;
    }

    private static boolean tryRegister(String line, Downstream<? super Instruction>  out) {
        Matcher matcher = REGISTER.matcher(line);
        if (!matcher.matches()) return false;

        String skipIdFlag = matcher.group(1);
        Logger.logDebug("Parsed register" + formatFlag(skipIdFlag) + ": " + matcher.group(2) + " -> " + matcher.group(3));
        out.push(new Register(matcher.group(2), matcher.group(3), skipIdFlag != null && skipIdFlag.equals("skipId")));
        return true;
    }

    private static boolean tryRead(String line, Downstream<? super Instruction>  out) {
        Matcher matcher = READ.matcher(line);
        if (!matcher.matches()) return false;

        String id = matcher.group(1) == null ? IdGen.generateID(10) : matcher.group(1);
        Logger.logDebug("Parsed Read" + formatFlag(id));
        out.push(new Read(id));
        return true;
    }

    private static boolean trySingleCommand(String line, Downstream<? super Instruction>  out) {
        Matcher matcher = SINGLE_LINE_COMMAND.matcher(line);
        if (!matcher.matches()) return false;
        String id = matcher.group(2) == null ? IdGen.generateID(10) : matcher.group(2);
        Logger.logDebug("Parsed single-line command: " + matcher.group(1) + formatFlag(id));
        out.push(new Command(matcher.group(1), id, matcher.group(3)));
        return true;
    }

    private static boolean tryBlockStart(BlockState state, String line) {
        Matcher matcher = BLOCK_START.matcher(line);
        if (!matcher.matches()) return false;

        String id = matcher.group(2) == null ? IdGen.generateID(10) : matcher.group(2);
        state.init(matcher.group(1), id);
        return true;
    }

    private static void handleBlockContent(BlockState state, String line, Downstream<? super Instruction>  out) {
        if (line.equals("~~~")) {
            Logger.logDebug("Parsed block command: " + state.name + formatFlag(state.id));
            out.push(new Command(state.name, state.id, state.content.toString()));
            state.reset();
        } else {
            state.append(line);
        }
    }

    private static String formatFlag(String id) {
        return id != null ? "{" + id + "}" : "";
    }
}
