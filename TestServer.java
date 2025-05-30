import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import jdk.jshell.*;

/**
 * DevServer startet einen HTTP-Server mit SSE-Unterstützung und einem File-Watcher,
 * der bei Änderungen an einer Java-Quelldatei automatisch das Programm neu ausführt
 * und verbundenen Clients über Server-Sent Events ein Reload-Signal sendet.
 * Außerdem wird die Ausgabe des Java-Prozesses in console.log geschrieben und
 * per HTTP-Server bereitgestellt.
 */
public class DevServer {
    /**
     * Config: enthält javaFile, port, verbose und Arbeitsverzeichnis
     */
    private record Config(String javaFile, int port, boolean verbose, Path dir) {}


    private WatchService watcher;
    private ScheduledExecutorService debounceExecutor;
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();

    public static void main(String[] args) throws Exception {
        Config cfg = parseArgs(args);
        new DevServer().start(cfg);
    }

    private static Config parseArgs(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java DevServer <JavaFile.java> [port] [--log]");
            System.exit(1);
        }
        String javaFile = null;
        int port = 8000;
        boolean verbose = false;
        for (String arg : args) {
            if ("--log".equals(arg) || "-l".equals(arg)) {
                verbose = true;
            } else if (arg.endsWith(".java") && javaFile == null) {
                javaFile = arg;
            } else {
                try { port = Integer.parseInt(arg); } catch (NumberFormatException ignored) {}
            }
        }
        if (javaFile == null) {
            System.err.println("Error: No Java file specified.");
            System.exit(1);
        }
        return new Config(javaFile, port, verbose, Paths.get("."));
}

    private void start(Config cfg) throws Exception {
        initWatcher(cfg);
        addShutdownHook();
        watchLoop(cfg);
    }

    private void initWatcher(Config cfg) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        cfg.dir().register(watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        log(cfg, "Watching " + cfg.javaFile() + "…");
    }

    private void watchLoop(Config cfg) {
        debounceExecutor = Executors.newSingleThreadScheduledExecutor();
        long debounceDelay = 200;
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break; // sauber beenden
            }
            for (WatchEvent<?> ev : key.pollEvents()) {
                Path changed = (Path) ev.context();
                if (changed.endsWith(cfg.javaFile())) {
                    log(cfg, "Change detected: scheduling execution...");
                    ScheduledFuture<?> prev = pendingTask.getAndSet(
                        debounceExecutor.schedule(() -> runJava(cfg), debounceDelay, TimeUnit.MILLISECONDS)
                    );
                    if (prev != null && !prev.isDone()) prev.cancel(false);
                }
            }
            if (!key.reset()) break;
        }
    }

    private void runJava(Config cfg) {
        try {
            log(cfg, "Executing java --enable-preview " + cfg.javaFile());
            System.out.println();
            ProcessBuilder pb = new ProcessBuilder("java", "--enable-preview", cfg.javaFile())
                .directory(cfg.dir().toFile())
                .redirectErrorStream(true);
            Process p = pb.start();
            //https://docs.oracle.com/en/java/javase/24/docs/api/jdk.jshell/jdk/jshell/package-summary.html
            try (JShell js = JShell.create()) {
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        List<SnippetEvent> events = js.eval(line);
                        for (SnippetEvent event : events) {
                            if (event.causeSnippet() != null) 
                            {
                                System.out.println("Cause: " + event.causeSnippet());
                                continue;
                            } 

                            System.out.println("JShell event: " + event.status());
                            System.out.println("Previous: " + event.previousStatus());
                            if (event.status() == Snippet.Status.VALID) {
                                System.out.println("JShell: " + event.snippet().source() + " => " + event.value());
                            } else {
                                System.err.println("JShell error: " + event.snippet().source() + " - " + event.status());
                            }
                            System.out.println();
                        }
                    }
                }
            }

            boolean finished = p.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                System.err.println("Timeout: process killed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
           System.out.println("Shutting down watcher...");
            try { watcher.close(); } catch (IOException e) { e.printStackTrace(); }
            debounceExecutor.shutdownNow();
        }));
    }

    private void log(Config cfg, String msg) {
        if (cfg.verbose()) System.out.println(msg);
    }
}

// java --enable-preview .\TestServer.java JshellTest.java --log