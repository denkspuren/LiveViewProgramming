package lvp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import lvp.logging.LogLevel;
import lvp.logging.Logger;

public class Main {
    private record Config(Path path, String fileNamePattern, int port, LogLevel logLevel){}
    public static void main(String[] args) {
        Config cfg = parseArgs(args);
        Logger.setLogLevel(cfg.logLevel());

        if (!isLatestRelease()) {
            System.out.println("Warning: You are not using the latest release of Live View Programming. Please visit https://github.com/denkspuren/LiveViewProgramming/releases");
        }
        
        try {
            Server server = new Server(Math.abs(cfg.port()), cfg.logLevel().equals(LogLevel.Debug));
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            if(cfg.path() != null) {
                FileWatcher watcher = new FileWatcher(cfg.path(), cfg.fileNamePattern(), server);
                Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
                watcher.watchLoop(server);
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Config parseArgs(String[] args) {
        String fileNamePattern = null;
        Path fileName = null;
        Path path = null;
        int port = Server.getDefaultPort();
        LogLevel logLevel = LogLevel.Error;

        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            String key = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : "";

            switch (key) {
                case "-l":
                case "--log":
                    logLevel = value.isBlank() ? LogLevel.Info : LogLevel.fromString(value);
                    break;
                case "-p":
                case "--pattern":
                    fileNamePattern = value.isBlank() ? "*" : value;
                    break;
                case "--watch":
                case "-w":
                    path = value.isBlank() ? Paths.get(".") : Paths.get(value).normalize();
                    break;
                default:
                    try { port = Integer.parseInt(arg.trim()); } catch(NumberFormatException _) {}
                    break;
            }
        }

        if (port < 1 || port > 65535) {
            System.err.println("Error: Invalid port number. Must be between 1 and 65535.");
            System.exit(1);
        }

        if (path == null) return new Config(null, null, port, logLevel);

        if (!Files.exists(path)) {
            System.err.println("Error: Path not found " + path);
            System.exit(1);
        }

        if(!Files.isDirectory(path)) {
            if (path.getFileName().toString().endsWith(".java")) fileName = path.getFileName();
            path = path.getParent() != null ?  path.getParent() : Paths.get(".");
        }

        if (fileName == null && fileNamePattern == null) {
            System.err.println("Error: No Java file or pattern specified.");
            System.exit(1);
        }

        return new Config(path, fileNamePattern != null ? fileNamePattern : fileName.toString(), port, logLevel);
    }

    public static boolean isLatestRelease() {
        try {
            HttpClient client = HttpClient.newHttpClient();
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

    public static String extractJsonField(String json, String field) {
        Matcher matcher = java.util.regex.Pattern
            .compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"")
            .matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
