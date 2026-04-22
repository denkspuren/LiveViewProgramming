package lvp.skills.parser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lvp.skills.ParsingTools;
import lvp.skills.logging.Logger;

public class PathParser {
    private PathParser() {}

    public static Optional<List<Path>> parse(String file) {
        if (file.contains("*") || file.contains("?") || file.contains("["))
            return resolveGlob(file);
        
        Optional<Path> path = ParsingTools.tryPath(file);
        if (path.isEmpty()) {
            Logger.logError("Invalid Path: '" + file + "'");
            return Optional.empty();
        }

        Path normalizedPath = path.get().normalize();

        if (!Files.exists(normalizedPath)) {
            Logger.logError("File does not exists: '" + normalizedPath.toAbsolutePath() + "'");
            return Optional.empty();
        }

        if (Files.isDirectory(normalizedPath)) {
            Logger.logError("File is a directory: '" + normalizedPath.toAbsolutePath() + "'");
            return Optional.empty();
        }

        return Optional.of(List.of(normalizedPath.toAbsolutePath()));
    }

    private static Optional<List<Path>> resolveGlob(String file) {
        int[] indices = {
            file.indexOf('*'),
            file.indexOf('?'),
            file.indexOf('['),
            file.indexOf(']')
        };
        int firstGlob = Arrays.stream(indices).filter(i -> i >= 0).min().orElse(0);
        int lastSlash = Math.max(file.substring(0, firstGlob).lastIndexOf('/'), file.substring(0, firstGlob).lastIndexOf('\\'));
        String validPart = lastSlash >= 0 ? file.substring(0, lastSlash) : ".";

        Optional<Path> path = ParsingTools.tryPath(validPart);
        if (path.isEmpty()) {
            Logger.logError("Invalid Path: '" + validPart + "'");
            return Optional.empty();
        }
        Path dir = path.get().normalize();

        if (!Files.isDirectory(dir)) {
            Logger.logError("Invalid Path: '" + file + "'. '" + validPart + "' is not a directory.");
            return Optional.empty();
        }

        Logger.logDebug("Valid Part: '" + validPart + "' -> Directory: '" + dir.toAbsolutePath() + "'");
        return walkDir(dir, file);
    }

    private static Optional<List<Path>> walkDir(Path dir, String globPart) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPart);
            List<Path> matchingFiles = new ArrayList<>();
            for (Path entry : stream) {
                Logger.logDebug("Checking file: " + entry);
                if (Files.isDirectory(entry)) {
                    matchingFiles.addAll(walkDir(entry, globPart).orElse(List.of()));
                }
                if (Files.isRegularFile(entry) && matcher.matches(entry)) {
                    Logger.logDebug("Match found.");
                    matchingFiles.add(entry.toAbsolutePath());
                }
            }
            return matchingFiles.isEmpty() ? Optional.empty() : Optional.of(matchingFiles);
        } catch (IOException e) {
            Logger.logError("Invalid Path: '" + dir + "'", e);
            return Optional.empty();
        }
    }
}
