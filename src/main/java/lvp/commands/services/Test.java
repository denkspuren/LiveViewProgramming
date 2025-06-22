package lvp.commands.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lvp.Processor.MetaInformation;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;

public class Test {
    private Test() {}
    private static final String JSHELL_PROMPT = "jshell>";
    public static String test(MetaInformation meta, String content) {
        Map<String, List<String>> fields = new HashMap<>(); 
        String currentKey = null;

        for (String line: content.lines().toList()) {
            if (line.isBlank()) continue;
            if (line.strip().startsWith("Send:") || line.strip().startsWith("Expect:") || line.strip().startsWith("Type:")) {
                String[] parts = line.split(":", 2);
                currentKey = parts[0].strip().toLowerCase();
                String value = parts[1].strip();
                fields.computeIfAbsent(currentKey, _ -> new ArrayList<>());
                if (!value.isEmpty()) fields.get(currentKey).add(value);
                if (currentKey.equals("type")) currentKey = null;
            } else if (currentKey != null) {
                fields.get(currentKey).add(line);
            } else {
                Logger.logError("Unexpected line " + line);
                return null;
            }
        }
        String send = String.join("\n", fields.get("send"));
        List<String> expect = fields.get("expect");
        List<String> typeL = fields.get("type");
        String type = typeL != null && typeL.size() == 1 ? typeL.getFirst() : "exact";

        if (send == null || expect == null) {
            Logger.logError("Test command requires 'Send' and 'Expect' fields.");
            return null;
        }

        Logger.logDebug("Parsed test command: send=" + send + ", expect=" + expect + ", type=" + type);
        String actual = executeJshell(send);
        if (actual == null) return "No Result";
        List<String> actualParsed = actual.lines().map(Test::parseJshellOutput).filter(s -> !s.isBlank()).toList();

        return TextUtils.fillOut("""
            Result for Test ${0}:
            Input: ${1}
            Response: ${2}
            Actual: ${3}
            Expected: ${4}
            Comparison: '${5}'
            Status: ${6}
            """, meta.id(), send, actual, actualParsed, expect, type, compare(actualParsed, expect, type) ? "Success" : "Failure");
    }

    private static boolean compare(List<String> actual, List<String> expected, String type) {
        return switch(type) {
            case "exact" -> actual.equals(expected);
            case "oneof" -> expected.stream().allMatch(actual::contains);
            case "same" -> actual.size() == expected.size() && new HashSet<>(actual).equals(new HashSet<>(expected));
            default -> {
                Logger.logError("Unknown Comparison Type.");
                yield false;
            }
        };
    }

    private static String executeJshell(String send) {
        String result = null;
         try {
            Logger.logInfo("Executing jshell --enable-preview -R-ea");
            ProcessBuilder pb = new ProcessBuilder("jshell", "--enable-preview", "-R-ea")
                .redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(send + "\n");
                writer.write("/ex");
                writer.flush();
            }
            try (var reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                result = reader.lines()
                    .filter(line -> line.startsWith(JSHELL_PROMPT) && line.strip().length() > JSHELL_PROMPT.length())
                    .collect(Collectors.joining("\n"));
            }
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Logger.logError("Timeout: process jshell killed");
            }
        } catch (Exception e) {
            Logger.logError("Error in jshell", e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private static String parseJshellOutput(String line) {
        int idx = line.indexOf("==>");
        if (idx != -1 && idx + 3 < line.length()) {
            return line.substring(idx + 3).strip();
        } else if (line.startsWith(JSHELL_PROMPT + " |")) {
            return line.substring(9).strip();
        }
        return "";
    }
}
