import java.util.List;
import java.util.Optional;

import static java.lang.Integer.min;

enum EToken
{
    None,
    Number,
    String,
    Plus,
    Equiv,
    Assert,
    Print,
    Quit,
    Resume,
    Suspend,
    Replace,
    Get,
    Store,
    Ident,
    QuotedIdent,
}

public class Lexer extends ProcessBase {
    private List<String> lines;
    private int lineNumber;
    private int offset;

    public Lexer(ILogger logger, List<String> lines) {
        super(logger);
        this.lines = lines;
    }

    public Optional<String> getText(StringSplice splice) {
        if (splice.empty()) {
            return badSplice(splice);
        }

        if (splice.getLine() < 0 || splice.getLine() > lines.size()) {
            return badSplice(splice);
        }

        String line = lines.get(splice.getLine());
        int offset = splice.getOffset();
        int length = line.length();
        int end = min(length, length + offset);

        return Optional.of(lines.get(splice.getLine()).substring(offset, offset + end));
    }

    private Optional<String> badSplice(StringSplice splice) {
        logger.error(String.format("Invalid splice %s", splice.toString()));
        return Optional.empty();
    }

    public Optional<String> getLocation() {
        return getText(new StringSplice(lineNumber, offset, 1));
    }

    public boolean run() {
        for (String line : lines) {
            if (!parseLine(line))
                return false;
            ++lineNumber;
        }

        return true;
    }

    private boolean parseLine(String line) {
        return false;
    }
}
