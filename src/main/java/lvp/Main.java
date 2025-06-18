package lvp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import lvp.skills.logging.LogLevel;
import lvp.skills.logging.Logger;
import lvp.skills.parser.ConfigParser;
import lvp.skills.parser.InstructionParser;
import lvp.skills.parser.PathParser;
import lvp.skills.parser.ConfigParser.Source;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    private record Config(List<Source> sources, int port, LogLevel logLevel, Optional<String> watchFilter, boolean sourceOnly){}

    private static final Path LVP_CONFIG_PATH = Path.of("./sources.json");
    public static void main(String[] args) {
        Config cfg = parseArgs(args);

        if (!isLatestRelease()) {
            System.out.println("Warning: You are not using the latest release of Live View Programming. Please visit https://github.com/denkspuren/LiveViewProgramming/releases");
        }

        Server server = null;
        FileWatcher watcher = null;
        Processor processor = null;
        
        try {
            server = new Server(Math.abs(cfg.port()));
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            processor = new Processor(server);
            watcher = new FileWatcher(cfg.sources(), cfg.watchFilter(), cfg.sourceOnly(), processor);
            Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
            watcher.start();
        } catch (IOException e) {
            System.err.println("Error starting lvp: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine().strip();
            if (input.startsWith("/"))
                handleServerCommands(input.substring(1).strip());
            else if (!input.isBlank() && !input.startsWith("Scan")) {
                processor.process(Stream.of(input),  Base64.getUrlEncoder().withoutPadding().encodeToString("stdin".getBytes(StandardCharsets.UTF_8)), null);
            } else {
                System.err.println("Error: Invalid command. Use '/help' for available commands.");
            }
        }
    }

    private static void handleServerCommands(String command) {
        String[] parts = command.split(" ", 2);
        if (command.isBlank() || parts.length == 0) {
            System.out.println("No command entered. Type '/help' for available commands.");
            return;
        }

        switch (parts[0].toLowerCase()) {
            case "exit" -> {
                System.out.println("Exiting Live View Programming...");
                System.exit(0);
            }
            case "log" -> {
                if (parts.length < 2) {
                    System.out.println("Usage: /log <level>");
                    return;
                }
                LogLevel level = LogLevel.fromString(parts[1]);
                if (level == null) {
                    System.out.println("Invalid log level.");
                } else {
                    Logger.setLogLevel(level);
                    System.out.println("Log level set to: " + level);
                }
            }
            case "help" -> System.out.println("Available commands: /exit, /help, /log");
            default -> System.out.println("Unknown command: " + command);
        }
    }

    private static Config parseArgs(String[] args) {
        List<String> files = new ArrayList<>();
        Optional<String> cmd = Optional.empty();
        int port = Server.getDefaultPort();
        LogLevel logLevel = LogLevel.Error;
        Optional<List<Source>> sources = Optional.empty();
        Optional<String> watchFilter = Optional.empty();
        boolean sourceOnly = false;

        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            String key = parts[0].strip();
            String value = parts.length > 1 ? parts[1].strip() : "";

            switch (key) {
                case "-l", "--log":
                    logLevel = value.isBlank() ? LogLevel.Info : LogLevel.fromString(value);
                    break;
                case "--port", "-p":
                    try { port = Integer.parseInt(value); } catch(NumberFormatException _) {}
                    break;
                case "--cmd":
                    cmd = value.isBlank() ? Optional.empty() : Optional.of(value);
                    break;
                case "--config", "-c":
                    sources = loadConfig();
                    break;
                case "--watch-filter", "-w":
                    watchFilter = value.isBlank() ? Optional.empty() : Optional.of(value);
                    break;
                case "--source-only", "-s":
                    sourceOnly = true;
                    break;
                default:
                    if (!arg.isBlank()) files.add(arg.strip());
                    break;
            }
        }

        Logger.setLogLevel(logLevel);

        if (port < 1 || port > 65535) {
            System.err.println("Error: Invalid port number. Must be between 1 and 65535.");
            System.exit(1);
        }
        Logger.logDebug(files.isEmpty() ? "No files provided." : "Files to execute: " + files);
        Optional<List<Path>> paths = getFilePaths(files);

        if (paths.isEmpty() && sources.isEmpty()) {
            System.err.println("Error: No valid files to execute.");
            System.exit(1);
        }

        if (!paths.isEmpty()) {
            sources = Optional.of(sources.orElseGet(ArrayList::new));
            String c = cmd.orElse("java -Dsun.stdout.encoding=UTF-8 --enable-preview");
            List<Source> sourcesFromPaths = paths.get().stream().map(path -> new Source(path, c)).toList();
            sources.ifPresent(lst -> lst.addAll(sourcesFromPaths));
        }

        return new Config(sources.get(), port, logLevel, watchFilter, sourceOnly);
    }

    private static Optional<List<Path>> getFilePaths(List<String> files) {
        List<Path> paths = new ArrayList<>();
        for (String file : files) {
            PathParser.parse(file).ifPresent(paths::addAll);
        }
        return paths.isEmpty() ? Optional.empty() : Optional.of(paths);
    }

    private static Optional<List<Source>> loadConfig() {
        if (!Files.isRegularFile(LVP_CONFIG_PATH) || !Files.exists(LVP_CONFIG_PATH)) {
            Logger.logError("Config not found at: " + LVP_CONFIG_PATH.normalize().toAbsolutePath());
            return Optional.empty();
        }
        return ConfigParser.parse(LVP_CONFIG_PATH);
    }

    public static boolean isLatestRelease() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/denkspuren/LiveViewProgramming/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                Logger.logDebug("Failed to fetch latest release: " + response.statusCode());
                return true;
            }
            String latestTag = extractJsonField(response.body(), "tag_name");
            String currentVersion = Main.class.getPackage().getImplementationVersion();
            Logger.logDebug("Latest release tag: " + latestTag);
            Logger.logDebug("Current Version: " + currentVersion);
            return latestTag == null || latestTag.equals(currentVersion);
        } catch (Exception e) {
            Logger.logDebug("Error checking latest release: " + e.getMessage());
            return true;
        }
    }

    private static String extractJsonField(String json, String field) {
        Matcher matcher = java.util.regex.Pattern
            .compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"")
            .matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
