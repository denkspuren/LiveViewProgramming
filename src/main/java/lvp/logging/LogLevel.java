package lvp.logging;

public enum LogLevel {
    Debug,
    Info,
    Error;

    public static LogLevel fromString(String input) {
        return switch (input.trim().toLowerCase()) {
            case "debug", "dbg", "verbose" -> Debug;
            case "info" -> Info;
            case "error", "err" -> Error;
            default -> Error;
        };
    }
}