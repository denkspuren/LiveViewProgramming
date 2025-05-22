package lvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import lvp.logging.Logger;

public class FileWatcher {
    private WatchService watcher;
    private ScheduledExecutorService debounceExecutor;
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();
    
    private boolean isRunning = true;
    Path dir;
    String fileNamePattern;

    public FileWatcher(Path dir, String fileNamePattern) throws IOException{
        this.dir = dir;
        this.fileNamePattern = fileNamePattern;

        watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        Logger.logInfo("Watching in " + dir.normalize().toAbsolutePath() + "");
    }

    public void watchLoop(Server server) {
        debounceExecutor = Executors.newSingleThreadScheduledExecutor();
        long debounceDelay = 200;            
        while (isRunning) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                if (isRunning)
                    Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
                break;
            }
            for (WatchEvent<?> ev : key.pollEvents()) {
                Path changed = (Path) ev.context();
                if (FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern).matches(changed)) {
                    Logger.logInfo("Event für Datei: " + changed.toAbsolutePath() + " (" + ev.kind().name() + ")");
                    
                    ScheduledFuture<?> prev = pendingTask.getAndSet(
                        debounceExecutor.schedule(() -> runJava(changed, server), debounceDelay, TimeUnit.MILLISECONDS)
                    );
                    if (prev != null && !prev.isDone()) prev.cancel(false);
                }
            }
            
            if (!key.reset()) break;
        }
    }

    public void stop() {
        isRunning = false;
        if (watcher != null) try { watcher.close(); } catch (IOException e) { e.printStackTrace(); }
        if (debounceExecutor != null) debounceExecutor.shutdownNow();
    }

    private void runJava(Path path, Server server) {
        try {
            Path jarLocation = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            
            Logger.logInfo("Executing java --enable-preview --class-path " + jarLocation + " " + dir.resolve(path).toString());
            ProcessBuilder pb = new ProcessBuilder("java", "--enable-preview", "--class-path", jarLocation.toString(), dir.resolve(path).toString())
                .redirectErrorStream(true);
            Process process = pb.start();

            try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Logger.logDebug("(JavaClient) " + line);
                        server.read(line);
                    }
                }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Logger.logError("Timeout: process killed");
            }

        } catch (Exception e) {
            Logger.logError("Error in Java Process", e);
        }
    }
}
