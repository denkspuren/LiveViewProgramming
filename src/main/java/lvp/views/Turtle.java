package lvp.views;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import lvp.Clerk;
import lvp.skills.Interaction;
import lvp.skills.Text;

/**
 * Turtle ermöglicht das Erstellen einfacher Turtle-Grafiken als SVG-Datei.
 * Das Koordinatensystem ist kartesisch (0°=rechts, Winkel gegen den Uhrzeigersinn,
 * Y-Achse positiv nach oben). SVG verwendet hingegen eine Y-Achse, die nach unten zeigt.
 * Daher werden die Y-Koordinaten beim Export invertiert.
 * Die einzelnen graphischen Elemente werden durchnummeriert in der Reihenfolge ihrer Erzeugung.
 */
public class Turtle implements Clerk{
    public final String ID = Clerk.getHashID(this);
    private final double xFrom, yFrom, viewWidth, viewHeight;
    private final List<Element> elements = new ArrayList<>();
    private int elementCounter = 0;
    private State state;
    private final Deque<State> stack = new ArrayDeque<>();

    public Turtle() {
        this(500, 500);
    }
    public Turtle(int width, int height) {
        this(0, width, 0, height, width / 2.0, height / 2.0, 0);        
    }

    /**
     * @param xFrom      linke Begrenzung des Sichtbereichs
     * @param xTo        rechte Begrenzung des Sichtbereichs
     * @param yFrom      untere Begrenzung des Sichtbereichs
     * @param yTo        obere Begrenzung des Sichtbereichs
     * @param startX     Start-X-Koordinate der Turtle
     * @param startY     Start-Y-Koordinate der Turtle
     * @param startAngle Blickrichtung in Grad (0°=rechts, 90°=oben, gegen den Uhrzeigersinn)
     */
    public Turtle(double xFrom, double xTo, double yFrom, double yTo,
                     double startX, double startY, double startAngle) {
        this.xFrom = xFrom;
        this.yFrom = yFrom;
        this.viewWidth = xTo - xFrom;
        this.viewHeight = yTo - yFrom;
        this.state = new State(
            startX, startY, startAngle,
            new Color(0, 0, 0, 1.0), 1.0, true);
    }

    public Turtle penUp() {
        state = state.withPenDown(false);
        return this;
    }

    public Turtle penDown() {
        state = state.withPenDown(true);
        return this;
    }

    public Turtle forward(double distance) {
        double rad = Math.toRadians(state.angle());
        double dx = Math.cos(rad) * distance;
        double dy = Math.sin(rad) * distance;
        double newX = state.x() + dx;
        double newY = state.y() + dy;
        if (state.penDown()) {
            elements.add(new Line(++elementCounter,
                    state.x(), state.y(), newX, newY,
                    state.color(), state.width()));
        }
        state = state.withPosition(newX, newY);
        return this;
    }

    public Turtle backward(double distance) {
        forward(-distance);
        return this;
    }

    public Turtle right(double angle) {
        // Normalize angle to be in [0, 360)
        double newAngle = state.angle() - angle;
        state = state.withAngle((newAngle % 360 + 360) % 360);
        return this;
    }

    public Turtle left(double angle) {
        // Normalize angle to be in [0, 360)
        double newAngle = state.angle() + angle;
        state = state.withAngle((newAngle % 360 + 360) % 360);
        return this;
    }

    public Turtle color(int r, int g, int b, double a) {
        if (!(0 <= r && r <= 255 && 0 <= g && g <= 255 && 0 <= b && b <= 255 && 0 <= a && a <= 1))
            throw new IllegalArgumentException(
                String.format(Locale.US, "Invalid color values: r=%d, g=%d, b=%d, a=%.2f. " +
                                         "RGB must be [0,255], alpha must be [0.0,1.0].", r, g, b, a)
            );
        state = state.withColor(new Color(r, g, b, a));
        return this;
    }

    public Turtle color(int r, int g, int b) {
        return color(r, g, b, state.color().a());
    }

    public Turtle text(String text) {
        return text(text, "16px sans-serif");
    }

    public Turtle text(String text, String font) {
        double rad = Math.toRadians(state.angle());
        double dx = Math.cos(rad);
        double dy = Math.sin(rad);

        elements.add(new SvgText(++elementCounter, text,
                state.x(), state.y(), dx, dy, state.color(), font));
        return this;
    }

    public Turtle lineWidth(double w) {
        return width(w);
    }

    public Turtle width(double w) {
        state = state.withWidth(w);
        return this;
    }

    public Turtle push() {
        stack.push(state);
        return this;
    }

    public Turtle pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty turtle state stack.");
        }
        state = stack.pop();
        return this;
    }

    public Turtle write() {
        Clerk.write(Text.fillOut("<div id='turtle${0}'>${1}</div>", ID, toString()));
        return this;
    }

    public Turtle timelineSlider() {
        Clerk.write(Text.fillOut("""
                <div>
                    Linien sichtbar: <span id="currentLine${0}">${1}</span> / <span>${1}</span>
                </div>
                """, ID, elements.size()));
        Clerk.write(
            Interaction.slider(ID, 0, elements.size(), elements.size(), Text.fillOut("""
                ((e) => {
                    const n = e.target.value;
                    const statusCurrent = document.getElementById("currentLine${0}");
                    statusCurrent.textContent = n;
                    const svgElement = document.getElementById("turtle${0}");
                    const lineIds = Array.from(svgElement.querySelectorAll("[svg-id]")).map(el => el.getAttribute("svg-id"));
                    lineIds.forEach((id,i) => {
                        const el = svgElement.querySelector(`[svg-id="` + id + `"]`);
                        if (el) el.style.display = i < n ? "" : "none";
                    });
                })(event)
                """, ID))
        );

        return this;
    }

    public void save(String filename) throws IOException {
        Path path = Path.of(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(
                String.format(Locale.US,
                    """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="%.2f %.2f %.2f %.2f">
                    """, xFrom, yFrom, viewWidth, viewHeight)
            );
            for (Element e : elements) {
                writer.write(elementString(e));
            }
            writer.write("</svg>\n");
        }
    }

    public void save() throws IOException { save("output.svg"); }

    @Override
    public String toString() {
        String out = String.format(Locale.US,
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="%.2f %.2f %.2f %.2f">
        """, xFrom, yFrom, viewWidth, viewHeight);

        
        for (Element e : elements) {
            out += elementString(e);
        }
        

        out += "</svg>\n";
        return out;
    }

    private String elementString(Element e) {
        return switch (e) {
            case Line line -> {
                double y1Svg = (viewHeight - (line.y1() - yFrom)) + yFrom;
                double y2Svg = (viewHeight - (line.y2() - yFrom)) + yFrom;
                
                yield String.format(Locale.US,
                    """
                        <line svg-id="%d" x1="%.2f" y1="%.2f" x2="%.2f" y2="%.2f"
                            stroke="rgba(%d,%d,%d,%.2f)" stroke-width="%.2f" />
                    """,
                    line.id(), line.x1(), y1Svg, line.x2(), y2Svg,
                    line.color().r(), line.color().g(), line.color().b(), line.color().a(),
                    line.width());
            }
            case SvgText text -> {
                double ySvg = (viewHeight - (text.y() - yFrom)) + yFrom;
                yield String.format(Locale.US,
                    """
                        <text svg-id="%d" x="%.2f" y="%.2f" dx="%.2f" dy="%.2f"
                            style="color: rgba(%d,%d,%d,%.2f); font:%s;">%s</text>
                    """,
                    text.id(), text.x(), ySvg, text.dx(), text.dy(),
                    text.color().r(), text.color().g(), text.color().b(), text.color().a(),
                    text.font(), text.text());
            }
        };
    }

    private static record State(double x, double y, double angle, Color color, double width, boolean penDown) {
        public State withPosition(double newX, double newY) {
            return new State(newX, newY, angle, color, width, penDown);
        }
        public State withAngle(double newAngle) {
            return new State(x, y, newAngle, color, width, penDown);
        }
        public State withColor(Color newColor) {
            return new State(x, y, angle, newColor, width, penDown);
        }
        public State withWidth(double newWidth) {
            return new State(x, y, angle, color, newWidth, penDown);
        }
        public State withPenDown(boolean isDown) {
            return new State(x, y, angle, color, width, isDown);
        }
    }

    private static record Line(int id, double x1, double y1, double x2, double y2, Color color, double width) implements Element {}

    private static record SvgText(int id, String text, double x, double y, double dx, double dy, Color color, String font) implements Element {}

    private static record Color(int r, int g, int b, double a) {}

    public sealed interface Element permits Line, SvgText {}
}