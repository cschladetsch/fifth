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
                tokens +
                ", lineNumber=" + lineNumber +
                ", offset=" + offset +
                '}';
    }

    public Optional<String> getText(StringSplice splice) {
        if (splice.empty()) {
            return Optional.of("");
        }

        int spliceLine = splice.getLine();
        if (spliceLine < 0 || spliceLine >= lines.size()) {
            return badSplice(splice);
        }

        String line = lines.get(spliceLine);
        int spliceLength = splice.getLength();
        int spliceOffset = splice.getOffset();
        int lineLength = line.length();
        if (spliceOffset > lineLength || spliceOffset + spliceLength > lineLength) {
            return badSplice(splice);
        }

        return Optional.of(line.substring(spliceOffset, spliceOffset + spliceLength));
    }

    private boolean atEnd() {
        return atEnd(offset);
    }

    private boolean atEnd(int offset) {
        if (lineNumber == lines.size()) {
            return true;
        }

        String line = lines.get(lineNumber);
        return offset == line.length();
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

            if (hasFailed()) {
                return false;
            }

            ++lineNumber;
        }

        return true;
    }

    private boolean parseLine(String line) {
        if (line.isEmpty()) {
            ++lineNumber;
            return addToken(ETokenType.Whitespace, 0);
        }

        offset = 0;
        while (offset < line.length()) {
            if (!nextToken()) {
                return false;
            }
        }

        return true;
    }

    private boolean nextToken() {
        if (atEnd()) {
            return false;
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

        return fail("Cannot parse " + getCurrent() + " at line:" + lineNumber + ": " + offset);
    }

    private boolean processOperation(char curr) {
        switch (curr) {
            case '+': return addToken(ETokenType.Plus, 1);
            case '-': return addToken(ETokenType.Minus, 1);
            case '=': {
                if (peek('=')) {
                    return addToken(ETokenType.Equiv, 2);
                }
                return addToken(ETokenType.Store, 1);
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
            return addToken(ETokenType.Assert, stringSplice);
        }

        if (text.equals("exit")) {
            return addToken(ETokenType.Exit, stringSplice);
        }

        return false;
    }

    private boolean peek(char ch) {
        String line = lines.get(lineNumber);
        if (line.length() == offset)
            return false;

        return line.charAt(offset + 1) == ch;
    }

    private boolean addToken(ETokenType type, int len) {
        addToken(new Token(type, currentSplice(len), this));
        return true;
    }

    private boolean addToken(ETokenType type, StringSplice splice) {
        addToken(new Token(type, splice, this));
        return true;
    }

    private boolean addToken(Token token)
    {
        tokens.add(token);
        offset += token.getSplice().getLength();
        return true;
    }

    private StringSplice gatherSplice(ICharCategory cat) {
        int length = 0;
        while (!atEnd(offset + length) && cat.matches(getCurrent(offset + length))) {
            ++length;
        }

        return new StringSplice(lineNumber, offset, length);
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

    private char getCurrent(int offset) {
        return lines.get(lineNumber).charAt(offset);
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
