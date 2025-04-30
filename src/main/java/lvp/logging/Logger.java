package lvp.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;


public class Logger {
    // Formatting of log messages
    public static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public static Function<LogEntry, String> stringFormatter = entry -> String.format("[%s] [%s] %s", entry.time(), entry.level(), entry.message());

    // Configuration
    private static LogLevel minLogLevel = LogLevel.Error;
    private static final List<LogDestination> destinations = new CopyOnWriteArrayList<>(List.of(ConsoleDestination.of()));  // Initializes with ConsoleDestination as the default logging output

    private Logger() {}

    
    // Configuration
    public static void setLogLevel(LogLevel level) { minLogLevel = level; }
    public static void addDestination(LogDestination destination) { destinations.add(destination); }

    // Logging
    public static void log(LogLevel level, String message) {
        log(level, message, null);
    }
    public static void log(LogLevel level, String message, Throwable error) {
        if(level.ordinal() < minLogLevel.ordinal()) return;

        String timestamp = LocalDateTime.now().format(timeFormat);
        String formattedMessage = stringFormatter.apply(new LogEntry(timestamp, level, message));

        if (error != null) {
            formattedMessage += System.lineSeparator();
            formattedMessage += getStackTrace(error);
        }

        for (LogDestination destination : destinations) {
            destination.log(formattedMessage);
        }
    }

    // Helper Methods
    public static void logError(String message, Throwable error) { log(LogLevel.Error, message, error); }
    public static void logError(String message) { log(LogLevel.Error, message); }
    public static void logInfo(String message, Throwable error) { log(LogLevel.Info, message, error); }
    public static void logInfo(String message) { log(LogLevel.Info, message); }
    public static void logDebug(String message, Throwable error) { log(LogLevel.Debug, message, error); }
    public static void logDebug(String message) { log(LogLevel.Debug, message); }


    // https://www.baeldung.com/java-stacktrace-to-string
    private static String getStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }
}
