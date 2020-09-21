public class Identifier {
    private final boolean quoted;
    private final String name;

    public Identifier(String name) {
        this.quoted = false;
        this.name = name;
    }

    public Identifier(boolean quoted, String name) {
        this.quoted = quoted;
        this.name = name;
    }

    public boolean isQuoted() {
        return quoted;
    }

    public String getName() {
        return name;
    }
}
