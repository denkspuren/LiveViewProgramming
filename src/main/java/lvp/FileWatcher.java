package lvp;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lvp.skills.logging.Logger;
import lvp.skills.parser.ConfigParser.Source;

public class FileWatcher {
    private WatchService watcher;
    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private Map<Path, Instant> lastModified = new ConcurrentHashMap<>();
    private boolean isRunning = true;
    private static final Duration DEBOUNCE_DURATION = Duration.ofMillis(500);
    
    List<Source> sources;
    Processor processor;
    Optional<PathMatcher> watchFilter;
    boolean sourceOnly;
    
    public FileWatcher(List<Source> sources, Optional<String> watchFilter, boolean sourceOnly, Processor processor) throws IOException{
        this.processor = processor;
        this.sources = sources;
        this.watchFilter = watchFilter.isEmpty() ? Optional.empty() : 
            Optional.of(FileSystems.getDefault().getPathMatcher("glob:" + watchFilter.get()));
        this.sourceOnly = sourceOnly;
        
        watcher = FileSystems.getDefault().newWatchService();
        sources.stream()
            .map(Source::path)
            .map(Path::getParent)
            .filter(Objects::nonNull)
            .map(Path::normalize)
            .flatMap(root -> {
                try {
                    return Files.find(root, Integer.MAX_VALUE, 
                        (_, attrs) -> attrs.isDirectory());
                } catch (IOException e) {
                    Logger.logError("Error walking directory: " + root.toAbsolutePath(), e);
                    return Stream.empty();
                }
            })
            .distinct()
            .forEach(dir -> {
                try {
                    dir.register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                    Logger.logInfo("Watching in " + dir.toAbsolutePath() + "");
                } catch (IOException e) {
                    Logger.logError("Error registering directory for watching: " + dir.toAbsolutePath(), e);
                }
            });
        
    }

    public void start() {
        for (Source source : sources) {
            Logger.logInfo("Running initial file: " + source.path());
            lastModified.put(source.path(), Instant.now());
            executor.submit(() -> run(source));
        }
        executor.submit(this::watchLoop);
    }

    private void watchLoop() {
        while (isRunning) {
            WatchKey key;
            try {
                key = watcher.take();
                processWatchKeyEvents(key);
            } catch (ClosedWatchServiceException | InterruptedException e) {
                Logger.logError("Watcher loop terminated due to exception: " + e.getMessage(), e);
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                break;
            }
            if (!key.reset()) isRunning = false;
        }
    }

    private void processWatchKeyEvents(WatchKey key) {
        for (WatchEvent<?> ev : key.pollEvents()) {
            Path changed = (Path) ev.context();
            if (Files.isDirectory(changed)) continue;

            Path dir = (Path) key.watchable();
            Path fullPath = dir.resolve(changed).normalize().toAbsolutePath();

            Instant now = Instant.now();
            Instant last = lastModified.getOrDefault(fullPath, Instant.EPOCH);
            Logger.logDebug(last + " -> " + now + " (" + Duration.between(last, now).toMillis() + "ms)");
            if (Duration.between(last, now).compareTo(DEBOUNCE_DURATION) < 0) return;
            lastModified.put(fullPath, now);

            Optional<Source> source = sources.stream()
                .filter(s -> s.path().equals(fullPath))
                .findFirst();
            if (source.isPresent()) {
                Logger.logInfo("Event for source: " + fullPath + " (" + ev.kind().name() + ")");
                executor.submit(() -> run(source.get()));
            }
            else if (!sourceOnly && (watchFilter.isEmpty() || watchFilter.get().matches(changed))) {
                Logger.logInfo("Event for file: " + fullPath + " (" + ev.kind().name() + ")");
                execute(sources);
            }
        }
    }

    private void execute(List<Source> sources) {
        for (Source source : sources) {
            executor.submit(() -> run(source));
        }
    }

    public void stop() {
        isRunning = false;
        if (watcher != null) try { watcher.close(); } catch (IOException _) { }
        if (executor != null) executor.shutdownNow();
    }

    private void run(Source source) {
        processor.init();
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            Logger.logInfo("Running: " + source.cmd() + " " + source.path());
            ProcessBuilder pb = new ProcessBuilder(isWindows ? new String[]{"cmd.exe", "/c", source.cmd(), source.path().toString()} : new String[]{"sh", "-c", source.cmd(),  source.path().toString()})
                .redirectErrorStream(true);
            Process process = pb.start();
            processor.process(process, source.id());

            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
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
}
