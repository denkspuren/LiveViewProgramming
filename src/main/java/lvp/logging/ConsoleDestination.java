package lvp.logging;

public class ConsoleDestination implements LogDestination {

    public static ConsoleDestination of() { return new ConsoleDestination(); }

    private ConsoleDestination() {}

    @Override
    public void log(String formattedMessage) {
        System.out.println(formattedMessage);
    }

}