package lvp.views.turtle;

public enum Font { 
    ARIAL("Arial"),
    VERDANA("Verdana"),
    TIMES("Times New Roman"),
    COURIER("Courier New"),
    SERIF("serif"),
    SANSSERIF("sans-serif");

    final String fullName; 

    private Font(String fullName) { this.fullName = fullName; }
    
    @Override
    public String toString() { return fullName;}

    public enum Align {
        CENTER, LEFT, RIGHT;

        @Override
        public String toString() { return name().toLowerCase(); }    
    }
}
