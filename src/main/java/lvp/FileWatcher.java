package lvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import lvp.skills.logging.Logger;

public class FileWatcher {
    private WatchService watcher;
    private ScheduledExecutorService debounceExecutor;
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();
    
    private boolean isRunning = true;
    Path dir;
    String fileNamePattern;
    Processor processor;

    public FileWatcher(Path dir, String fileNamePattern, Processor processor) throws IOException{
        this.dir = dir;
        this.fileNamePattern = fileNamePattern;
        this.processor = processor;

        watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        Logger.logInfo("Watching in " + dir.normalize().toAbsolutePath() + "");
        
        for (Path path : getMatchingFiles()) {
            Logger.logInfo("Running initial file: " + path.toAbsolutePath().normalize());
            run(path);
        }
    }

    public void watchLoop() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern);
        debounceExecutor = Executors.newSingleThreadScheduledExecutor();
        long debounceDelay = 200;            
        while (isRunning) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
                break;
            }
            for (WatchEvent<?> ev : key.pollEvents()) {
                Path changed = (Path) ev.context();
                if (matcher.matches(changed) && !Files.isDirectory(changed)) {
                    Logger.logInfo("Event für Datei: " + changed.toAbsolutePath() + " (" + ev.kind().name() + ")");
                    
                    ScheduledFuture<?> prev = pendingTask.getAndSet(
                        debounceExecutor.schedule(() -> run(dir.resolve(changed)), debounceDelay, TimeUnit.MILLISECONDS)
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

    private void run(Path path) {
        try {
            processor.init();
            Logger.logInfo("Executing java --enable-preview " + path.normalize().toString());
            ProcessBuilder pb = new ProcessBuilder("java", "-Dsun.stdout.encoding=UTF-8", "--enable-preview", path.normalize().toString())
                .redirectErrorStream(true);
            Process process = pb.start();
            processor.process(process);

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Logger.logError("Timeout: process killed");
            } else {
                Logger.logInfo("Process finished successfully");
            }

        } catch (Exception e) {
            Logger.logError("Error in Java Process", e);
        }
    }

    public List<Path> getMatchingFiles() throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry) && matcher.matches(entry.getFileName())) {
                    matchingFiles.add(entry);
                }
            }
        }
        return matchingFiles;
    }
}
