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

    public void watchLoop(Runnable closeClient) {
        debounceExecutor = Executors.newSingleThreadScheduledExecutor();
        long debounceDelay = 200;            
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
                break;
            }
            for (WatchEvent<?> ev : key.pollEvents()) {
                Path changed = (Path) ev.context();
                if (FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern).matches(changed)) {
                    Logger.logInfo("Event für Datei: " + changed.toAbsolutePath() + " (" + ev.kind().name() + ")");
                    
                    closeClient.run();
                    ScheduledFuture<?> prev = pendingTask.getAndSet(
                        debounceExecutor.schedule(() -> runJava(changed), debounceDelay, TimeUnit.MILLISECONDS)
                    );
                    if (prev != null && !prev.isDone()) prev.cancel(false);
                }
            }
            
            if (!key.reset()) break;
        }
    }

    public void stop() {
        if (watcher != null) try { watcher.close(); } catch (IOException e) { e.printStackTrace(); }
        if (debounceExecutor != null) debounceExecutor.shutdownNow();
    }

    private void runJava(Path path) {
        try {
            Path jarLocation = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            
            Logger.logInfo("Executing java --enable-preview --class-path " + jarLocation + " " + path.toString() + " in " + dir.normalize().toAbsolutePath());
            ProcessBuilder pb = new ProcessBuilder("java", "--enable-preview", "--class-path", jarLocation.toString(), path.toString())
                .directory(dir.toFile())
                .redirectErrorStream(true);
            Process process = pb.start();

            try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Logger.logInfo("(JavaClient) " + line);
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
