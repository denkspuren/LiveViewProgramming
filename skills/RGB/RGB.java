enum RGB { // https://w3schools.sinsixx.com/css/css_colornames.asp.htm
    AQUA(0x00FFFF),
    BLACK(0x000000),
    BLUE(0, 0, 255),
    FUCHSIA(0xFF00FF),
    GRAY(0x808080),
    GREEN(0, 255, 0),
    LIME(0x00FF00),
    MAROON(0x800000),
    NAVY(0x000080),
    OLIVE(0x808000),
    PURPLE(0x800080),
    RED(255, 0, 0),
    SILVER(0xC0C0C0),
    TEAL(0x008080),
    WHITE(0xFF, 0xFF ,0xFF),
    YELLOW(0xFFFF00);

    final int colorCode;

    private RGB(int red, int green, int blue) { colorCode = color(red, green, blue); }
    private RGB(int code) { this(red(code), green(code), blue(code)); }

    static int color(int code) {
        return color(red(code), green(code), blue(code));
    }

    static int color(int red, int green, int blue) {
        return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    static int red(int colorCode)   { return (colorCode & 0b11111111_00000000_00000000) >> 16; }
    static int green(int colorCode) { return (colorCode & 0b11111111_00000000) >> 8;           }
    static int blue(int colorCode)  { return  colorCode & 0b11111111;                          }

    static String hex(int code) { return String.format("0x%06x", color(code)); }
    String hex() { return hex(this.colorcode); }
}