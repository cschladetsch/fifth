public class StringSplice {
    private final int lineNumber;
    private final int offset;
    private final int length;

    public StringSplice(int lineNumber, int start, int length) {
        this.lineNumber = lineNumber;
        this.offset = start;
        this.length = length;
    }

    @Override
    public String toString() {
        return "StringSplice{" +
                "lineNumber=" + lineNumber +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }

    public int getLength() {
        return length;
    }

    public boolean empty() {
        return length == 0;
    }

    public int getLine() {
        return lineNumber;
    }

    public int getOffset() {
        return offset;
    }
}
