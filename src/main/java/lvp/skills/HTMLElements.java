package lvp.skills;


public class HTMLElements {
    private HTMLElements() {}
    public static String button(String id, String text, int width, int height, String onClick) {
        return TextUtils.fillOut("<button id='${0}' style='width: ${1}px; height: ${2}px;' onclick='${3}'>${4}</button>", id, width, height, onClick, text);
    }

    public static String button(String id, String text, String onClick) {
        return TextUtils.fillOut("<button id='${0}' style='padding: 2px 15px;' onclick='${1}'>${2}</button>", id, onClick, text);
    }

    public static String slider(String id, double min, double max, double value, String onInput) {
        return TextUtils.fillOut("<input type='range' id='${0}' min='${1}' max='${2}' value='${3}' step='any' oninput='${4}'/>",
                id, min, max, value, onInput);
    }

    public static String input(String id) {
        return TextUtils.fillOut("""
                <input id='${0}' type='text' style='padding: 5px; margin: 5px 5px 0 0;' />
                """, id);
    }

    public static String input(String id, String placeholder, String type, String label) {
        return TextUtils.fillOut("""
                <label for='${0}' style='margin-right: 5px;'>${3}</label>
                <input id='${0}' type='${1}' style='padding: 5px; margin: 5px 5px 0 0;' placeholder='${2}' />
                """, id, type, placeholder, label);
    }
    public static String input(String id, String placeholder, String type, String label, String eventType, String event) {
        return TextUtils.fillOut("""
                <label for='${0}' style='margin-right: 5px;'>${5}</label>
                <input id='${0}' type='${1}' style='padding: 5px; margin: 5px 5px 0 0;' placeholder='${2}' ${3}='${4}' />
                """, id, type, placeholder, eventType, event, label);
    }
    public static String checkbox(String id, String label, boolean checked, String event) {
        return TextUtils.fillOut("""
                <label for='${0}' style='margin-right: 5px;'>${1}</label>
                <input id='${0}' type='checkbox' style='padding: 5px; margin: 5px 5px 0 0;' ${2} onclick='${3}' />
                """, id, label, checked ? "checked" : "", event);
    }
}
