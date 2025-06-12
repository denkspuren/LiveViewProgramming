package lvp.skills;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lvp.commands.services.Turtle;
import lvp.skills.logging.Logger;

public class TurtleParser {
    private TurtleParser() {}

    private static final Pattern INIT_PATTERN = Pattern.compile("^init\\s+(\\d+)\\s+(\\d+)$");
    private static final Pattern INIT_ALT_PATTERN = Pattern.compile("^init\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)$");

    public static Turtle parse(String id, String content) {
        Stream<String> lines = content.lines().map(String::trim).filter(line -> !line.isEmpty());

        java.util.Iterator<String> iterator = lines.iterator();
        Optional<Turtle> turtleOptional = Optional.empty();

        while (iterator.hasNext()) {
            String line = iterator.next();
            Logger.logDebug("Parsing line: " + line);

            if (turtleOptional.isEmpty()) {
                turtleOptional = parseInit(id, line);
                if (turtleOptional.isPresent()) {
                    Logger.logDebug("Turtle initialized.");
                    continue;
                }
                Logger.logDebug("No init line detected, using default size.");
                turtleOptional = Optional.of(new Turtle(id, 400, 400)); // Fallback
            }

            Turtle turtle = turtleOptional.get();
            parseCommand(turtle, line);
        }

        return turtleOptional.orElse(new Turtle(id, 400, 400));
    }

    private static Optional<Turtle> parseInit(String id, String line) {
        Matcher mSimple = INIT_PATTERN.matcher(line);
        if (mSimple.matches()) {
            int width = Integer.parseInt(mSimple.group(1));
            int height = Integer.parseInt(mSimple.group(2));
            return Optional.of(new Turtle(id, width, height));
        }

        Matcher mExtended = INIT_ALT_PATTERN.matcher(line);
        if (mExtended.matches()) {
            double xFrom = Double.parseDouble(mExtended.group(1));
            double xTo = Double.parseDouble(mExtended.group(2));
            double yFrom = Double.parseDouble(mExtended.group(3));
            double yTo = Double.parseDouble(mExtended.group(4));
            double startX = Double.parseDouble(mExtended.group(5));
            double startY = Double.parseDouble(mExtended.group(6));
            double angle = Double.parseDouble(mExtended.group(7));

            return Optional.of(new Turtle(id, xFrom, xTo, yFrom, yTo, startX, startY, angle));
        }

        return Optional.empty();
    }

    private static void parseCommand(Turtle turtle, String line) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return;

        try {
            switch (parts[0]) {
                case "penUp":
                    turtle.penUp();
                    break;
                case "penDown":
                    turtle.penDown();
                    break;
                case "forward":
                    turtle.forward(Double.parseDouble(parts[1]));
                    break;
                case "backward":
                    turtle.backward(Double.parseDouble(parts[1]));
                    break;
                case "right":
                    turtle.right(Double.parseDouble(parts[1]));
                    break;
                case "left":
                    turtle.left(Double.parseDouble(parts[1]));
                    break;
                case "color":
                    int r = Integer.parseInt(parts[1]);
                    int g = Integer.parseInt(parts[2]);
                    int b = Integer.parseInt(parts[3]);
                    if (parts.length == 5) {
                        double a = Double.parseDouble(parts[4]);
                        turtle.color(r, g, b, a);
                    } else {
                        turtle.color(r, g, b);
                    }
                    break;
                case "text":
                    String text = parts[1];
                    if (parts.length >= 3) {
                        String font = parts[2];
                        turtle.text(text, font);
                    } else {
                        turtle.text(text);
                    }
                    break;
                case "width":
                    turtle.width(Double.parseDouble(parts[1]));
                    break;
                case "push":
                    turtle.push();
                    break;
                case "pop":
                    turtle.pop();
                    break;
                case "timeline":
                    turtle.timeline();
                    break;
                case "save":
                    if (parts.length >= 2) {
                        String filename = parts[1];
                        turtle.save(filename);
                        Logger.logInfo("Saved SVG to: " + filename);
                    }
                    break;
                default:
                    Logger.logError("Unknown command: '" + line + "'");
            }
        } catch (Exception e) {
            Logger.logError("Failed to parse command: '" + line + "' → " + e.getMessage(), e);
        }
    }
}
