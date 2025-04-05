package lvp.views;

import java.util.Map;
import java.util.function.Consumer;
import lvp.Clerk;
import lvp.Server;
import lvp.skills.Text;

public class Slider implements Clerk {
    public final String ID;
    Server server;
    public Slider(Server server, double min, double max) {
        this.server = server;
        ID = Clerk.getHashID(this);
        Clerk.write(server, "<div><input type='range' id='slider" + ID + "' min='" + min + "' max='" + max + "' step='any'/> </div>");
        Clerk.script(server, "const slider" + ID + " = document.getElementById('slider" + ID + "');");
    }
    public Slider attachTo(Consumer<String> delegate) {
        this.server.createResponseContext("/slider" + ID, delegate, ID);
        Clerk.script(server, Text.fillOut(
            """
            slider${0}.addEventListener('input', (event) => {
                if (locks.includes('${0}')) return;
                locks.push('${0}');
                const value = event.target.value;
                console.log(`slider${0}: value = ${value}`);
                fetch('slider${0}', {
                   method: 'post',
                    body: value.toString()
                }).catch(console.error);
            });
            """, Map.of("0", ID, "value", "${value}")));
        return this;
    }
}

// https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/range
// Alternatives Event: slider\{ID}.addEventListener("change", (event) => {