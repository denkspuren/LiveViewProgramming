package lvp.commands.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lvp.Processor.MetaInformation;
import lvp.skills.TextUtils;
import lvp.skills.logging.Logger;

//TODO: multiple actual and expect
public class Test {
    private static final String JSHELL_PROMPT = "jshell>";
    public static String test(MetaInformation meta, String content) {
        Map<String, String> fields = content.lines()
            .filter(line -> !line.isBlank())
            .map(line -> line.split(":", 2))
            .filter(parts -> parts.length == 2)
            .map(parts -> Map.entry(parts[0].strip().toLowerCase(), parts[1].strip()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String send = fields.get("send");
        String expect = fields.get("expect");

        if (send == null || expect == null) {
            Logger.logError("Test command requires 'Send' and 'Expect' fields.");
            return null;
        }

        Logger.logDebug("Parsed test command: send=" + send + ", expect=" + expect);
        String actual = executeJshell(send);
        if (actual == null) return "No Result";
        String actualParsed = actual.lines().map(Test::parseJshellOutput).findFirst().orElse("");

        return TextUtils.fillOut("""
            Result for Test ${0}:
            Input: ${1}
            Response: ${2}
            Actual: ${3}
            Expected: ${4}
            Status: ${5}
            """, meta.id(), send, actual, actualParsed, expect, actualParsed.equals(expect) ? "Success" : "Failure");
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
