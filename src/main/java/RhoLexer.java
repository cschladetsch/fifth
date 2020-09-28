import java.util.List;

public class RhoLexer extends Lexer {
    public RhoLexer(ILogger logger) {
        super(logger);
    }

    @Override
    protected void addKeywords() {
        super.addKeywords();
        tokenNames.put("fun", ETokenType.Fun);
    }

    @Override
    public boolean run(List<String> lines) {
        return super.run(lines);
    }

    @Override
    protected boolean parseLine(String line) {
        while (peek("    ")) {
            addToken(ETokenType.Tab, 4);
        }

        return super.parseLine(line);
    }

    private boolean peek(String text) {
        int n = 0;
        for (; n < text.length(); ++n) {
            if (!peek(' ', offset + n)) {
                return false;
            }
        }

        offset += n;
        return true;
    }

    private boolean peek(char ch, int n) {
        String line = lines.get(lineNumber);
        if (line.length() <= offset + 1 + n) {
            return false;
        }

        return line.charAt(offset + 1 + n) == ch;
    }
}
