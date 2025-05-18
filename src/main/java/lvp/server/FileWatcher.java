package lvp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
    private final WatchService watcher;
    private final ScheduledExecutorService debounceExecutor;
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();

    public FileWatcher(Path path, String fileNamePattern, Runnable closeJavaClient) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        path.register(watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        this.debounceExecutor = Executors.newSingleThreadScheduledExecutor();

        Thread watchThread = new Thread(() -> {
            try {
                watchLoop(path, fileNamePattern, closeJavaClient);
            } catch (Exception e) {
                Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
            }
        }, "Watcher");
        watchThread.setDaemon(true);
        watchThread.start();
        Logger.logInfo("Watcher started");
    }


    private void watchLoop(Path path, String fileNamePattern, Runnable closeJavaClient) {
        long debounceDelay = 200;   
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern);         
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
                if (matcher.matches(changed)) {
                    Logger.logInfo("Event for file: " + path.resolve(changed).toAbsolutePath() + " (" + ev.kind().name() + ")");
                    
                    closeJavaClient.run();
                    ScheduledFuture<?> prev = pendingTask.getAndSet(
                        debounceExecutor.schedule(() -> runJava(path, changed), debounceDelay, TimeUnit.MILLISECONDS)
                    );
                    if (prev != null && !prev.isDone()) {
                        prev.cancel(false);
                        Logger.logDebug("Previous task cancelled");
                    }
                }
            }
            
            if (!key.reset()) {
                Logger.logInfo("Watch key could not be reset. Exiting watch loop.");
                break;
            }
        }
    }

    private void runJava(Path dir, Path path) {
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

    public void stop() {
        if (watcher != null) try { watcher.close(); } catch (IOException e) { e.printStackTrace(); }        
        if (debounceExecutor != null) debounceExecutor.shutdownNow();
    }
}