import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.min;

enum ETokenType
{
    None,
    Number,
    //String,
    Plus,
    Minus,
    Equiv,
    NotEquiv,
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
    Whitespace,

    OpenSquareBracket,
    CloseSquareBracket,

    OpenParan,
    CloseParan,

    OpenBrace,
    CloseBrace,

    Exit,
}

interface ICharCategory {
    Boolean matches(char ch);
}

public class Lexer extends ProcessBase {
    private final List<String> lines;
    private final List<Token> tokens = new ArrayList<>();
    private int lineNumber;
    private int offset;

    public Lexer(ILogger logger, List<String> lines) {
        super(logger);
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "Lexer{" +
                "lines=" + lines +
                ", tokens=" + tokens +
                ", lineNumber=" + lineNumber +
                ", offset=" + offset +
                ", logger=" + logger +
                ", Process=" + super.toString() +
                '}';
    }

    //@Override
    public String toString2() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Lexer: ");
        String comma = "";
        for (Token token : tokens) {
            stringBuilder.append(comma);
            stringBuilder.append(token.getType());
            comma = ", ";
        }

        return stringBuilder.toString();
    }

    public Optional<String> getText(StringSplice splice) {
        if (splice.empty()) {
            return Optional.of("");
        }

        int spliceLine = splice.getLine();
        if (spliceLine < 0 || spliceLine > lines.size()) {
            return badSplice(splice);
        }

        String line = lines.get(spliceLine);
        int offset = splice.getOffset();
        int length = line.length();
        int end = min(length, length + offset);

        return Optional.of(line.substring(offset, offset + end));
    }

    private Optional<String> badSplice(StringSplice splice) {
        fail(String.format("Invalid splice %s", splice.toString()));
        return Optional.empty();
    }

    public Optional<String> getLocation() {
        return getText(new StringSplice(lineNumber, offset, 1));
    }

    public boolean run() {
        for (String line : lines) {
            if (!parseLine(line)) {
                return false;
            }

            ++lineNumber;
        }

        return true;
    }

    private boolean parseLine(String line) {
        if (line.isEmpty()) {
            ++lineNumber;
            return addToken(ETokenType.Whitespace);
        }

        char curr = getCurrent();
        if (Character.isSpaceChar(curr)) {
            return addToken(ETokenType.Whitespace, gatherSplice(Character::isSpaceChar));
        }

        if (Character.isDigit(curr)) {
            return addToken(ETokenType.Number, gatherSplice(Character::isDigit));
        }

        if (processAlpha(curr)) {
            return true;
        }

        if (processOperation(curr)) {
            return true;
        }

        return false;
    }

    private boolean processOperation(char curr) {
        switch (curr) {
            case '+': return addToken(ETokenType.Plus);
            case '-': return addToken(ETokenType.Plus);
            case '=': {
                if (peek('=')) {
                    return addToken(ETokenType.Equiv, 2);
                }
                return addToken(ETokenType.Store);
            }
        }

        return false;
    }

    private boolean processAlpha(char curr) {
        if (!Character.isAlphabetic(curr)) {
            return false;
        }

        StringSplice stringSplice = gatherSplice(Character::isAlphabetic);
        Optional<String> textOpt = getText(stringSplice);
        if (!textOpt.isPresent()) {
            fail("Failed to gather text @" + stringSplice);
            return false;
        }

        String text = textOpt.get();
        if (text.equals("assert")) {
            return addToken(ETokenType.Assert);
        }

        if (text.equals("exit")) {
            return addToken(ETokenType.Exit);
        }

        return false;
    }

    private boolean peek(char ch) {
        String line = lines.get(lineNumber);
        if (line.length() == offset)
            return false;

        return line.charAt(offset + 1) == ch;
    }

    private boolean addToken(ETokenType type) {
        tokens.add(new Token(type, currentSplice(), this));
        return true;
    }

    private boolean addToken(ETokenType type, int len) {
        tokens.add(new Token(type, currentSplice(len), this));
        return true;
    }

    private boolean addToken(ETokenType type, StringSplice splice) {
        tokens.add(new Token(type, splice, this));
        return true;
    }

    private StringSplice gatherSplice(ICharCategory cat) {
        int length = 0;
        int startOffset = offset;
        while (cat.matches(getCurrent())) {
            ++length;
        }

        offset += length;
        return new StringSplice(lineNumber, startOffset, length);
    }

    private StringSplice currentSplice() {
        return new StringSplice(lineNumber, offset, 1);
    }

    private StringSplice currentSplice(int len) {
        return new StringSplice(lineNumber, offset, len);
    }

    private char getCurrent() {
        return lines.get(lineNumber).charAt(offset);
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
