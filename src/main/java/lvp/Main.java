package lvp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lvp.logging.LogLevel;
import lvp.logging.Logger;

public class Main {
    private record Config(Path path, String fileNamePattern, int port, LogLevel logLevel){}
    public static void main(String[] args) {
        Config cfg = parseArgs(args);
        Logger.setLogLevel(cfg.logLevel());
        
        try {
            Server server = new Server(Math.abs(cfg.port()), cfg.logLevel().equals(LogLevel.Debug));
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            if(cfg.path() != null) {
                FileWatcher watcher = new FileWatcher(cfg.path(), cfg.fileNamePattern());
                Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
                watcher.watchLoop(server);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Starten des Servers: " + e.getMessage());
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


}
