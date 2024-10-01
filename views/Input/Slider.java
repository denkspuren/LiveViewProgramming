
class Slider implements Clerk {
    final String ID;
    LiveView view;
    Slider(LiveView view, double min, double max) {
        this.view = view;
        ID = Clerk.getHashID(this);
        Clerk.write(view, "<div><input type='range' id='slider" + ID + "' min='" + min + "' max='" + max + "' step='any'/> </div>");
        Clerk.script(view, "const slider" + ID + " = document.getElementById('slider" + ID + "');");
    }
    Slider attachTo(Consumer<String> delegate) {
        this.view.createResponseContext("/slider" + ID, delegate);
        Clerk.script(view, "slider" + ID +
        """
        .addEventListener("input", (event) => {
            const value = event.target.value;
            console.log(`slider""" + ID + 
        """
        : value = ${value}`);
        fetch('slider""" + ID +
        """
            ', {
                method: "post",
                body: value.toString()
            }).catch(console.log);
        });
        """
        );
        return this;
    }
}

// https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/range
// Alternatives Event: slider\{ID}.addEventListener("change", (event) => {