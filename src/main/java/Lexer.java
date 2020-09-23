import java.util.*;

enum ETokenType
{
    None,
    Not,
    Number,
    String,
    Plus,
    Minus,
    Multiply,
    Divide,
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
    Whitespace,
    True,
    False,

    Dup,
    Swap,
    Rot,
    RotN,
    Drop,

    Erase,
    Exists,

    OpenSquareBracket,
    CloseSquareBracket,

    OpenParan,
    CloseParan,

    OpenBrace,
    CloseBrace,

    Break,
    Exit,
    Dump,
    Modulo,
    Comment,
    Depth, Clear, QuotedIdent, If, IfElse, While, For,
    Less, Greater, LessEqual, GreaterEqual,
}

interface ICharCategory {
    Boolean matches(char ch);
}

public class Lexer extends ProcessBase {
    private final List<String> lines;
    private final List<Token> tokens = new ArrayList<>();
    private int lineNumber;
    private int offset;
    private final Map<String, ETokenType> tokenNames = new HashMap<String,ETokenType>();

    public Lexer(ILogger logger, String line) {
        this(logger, makeLines(line));
    }

    private static List<String> makeLines(String line) {
        List<String> lines = new ArrayList<>();
        lines.add(line);
        return lines;
    }

    public Lexer(ILogger logger, List<String> lines) {
        super(logger);
        this.lines = lines;
        addKeywords();
    }

    private void addKeywords() {
        tokenNames.put("assert", ETokenType.Assert);
        tokenNames.put("exit", ETokenType.Exit);
        tokenNames.put("break", ETokenType.Break);
        tokenNames.put("dup", ETokenType.Dup);
        tokenNames.put("print", ETokenType.Print);
        tokenNames.put("dump", ETokenType.Dump);
        tokenNames.put("not", ETokenType.Not);
        tokenNames.put("depth", ETokenType.Depth);
        tokenNames.put("exists", ETokenType.Exists);
        tokenNames.put("erase", ETokenType.Erase);
        tokenNames.put("swap", ETokenType.Swap);
        tokenNames.put("drop", ETokenType.Drop);
        tokenNames.put("clear", ETokenType.Clear);
        tokenNames.put("true", ETokenType.True);
        tokenNames.put("false", ETokenType.False);
        tokenNames.put("if", ETokenType.If);
        tokenNames.put("ifelse", ETokenType.IfElse);
        tokenNames.put("while", ETokenType.While);
        tokenNames.put("for", ETokenType.For);
    }

    @Override
    public String toString() {
        return "Lexer{" +
                tokens +
                ", lineNumber=" + lineNumber +
                ", offset=" + offset +
                '}';
    }

    @Override
    protected boolean fail(String text) {
        String prefix = String.format("@:%d:%d: ", lineNumber, offset);
        return super.fail(prefix + text);
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
        fail("Invalid splice " + splice);
        return Optional.empty();
    }

    public Optional<String> getLocation() {
        return getText(new StringSplice(lineNumber, offset, 1));
    }

    public boolean run() {
        for (String line : lines) {
            if (!parseLine(line)) {
                break;
            }

            if (hasFailed()) {
                break;
            }

            ++lineNumber;
        }

        return !hasFailed();
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

    private boolean processOperation(char ch) {
        switch (ch) {
            case '#': return addComment();
            case '%': return addToken(ETokenType.Modulo, 1);
            case '*': return addToken(ETokenType.Multiply, 1);
            case '/': return addToken(ETokenType.Divide, 1);
            case '+': return addToken(ETokenType.Plus, 1);
            case '-': return addToken(ETokenType.Minus, 1);
            case '{': return addToken(ETokenType.OpenBrace, 1);
            case '}': return addToken(ETokenType.CloseBrace, 1);
            case '@': return addToken(ETokenType.Get, 1);
            case '&': return addToken(ETokenType.Suspend, 1);
            case '\'': return addQuotedIdent();
            case '<': {
                if (peek('=')) {
                    return addToken(ETokenType.LessEqual, 2);
                }
                return addToken(ETokenType.Less, 1);
            }
            case '>': {
                if (peek('=')) {
                    return addToken(ETokenType.GreaterEqual, 2);
                }
                return addToken(ETokenType.Greater, 1);
            }
            case '=': {
                if (peek('=')) {
                    return addToken(ETokenType.Equiv, 2);
                }
                return addToken(ETokenType.Store, 1);
            }
            default:
                fail("Unrecognised operator starting with '" + ch + "'");
                break;
        }

        return false;
    }

    private boolean addQuotedIdent() {
        ++offset;
        if (atEnd()) {
            return fail("Identifier expected.");
        }

        StringSplice stringSplice = gatherSplice(Character::isAlphabetic);
        if (stringSplice.getLength() == 0) {
            return fail("Identifier expected");
        }

        return addToken(ETokenType.QuotedIdent, stringSplice);
    }

    private boolean addComment() {
        int length = 0;
        int lineLength = lines.get(lineNumber).length();
        while (length < lineLength) {
            ++length;
        }

        return addToken(ETokenType.Comment, length);
    }

    private boolean processAlpha(char curr) {
        if (!Character.isAlphabetic(curr)) {
            return false;
        }

        StringSplice stringSplice = gatherSplice(Character::isAlphabetic);
        Optional<String> textOpt = getText(stringSplice);
        if (!textOpt.isPresent()) {
            return fail("Failed to gather text @" + stringSplice);
        }

        String text = textOpt.get();
        if (tokenNames.containsKey(text)) {
            return addToken(tokenNames.get(text), stringSplice);
        }

        return addToken(ETokenType.Ident, stringSplice);
    }

    private boolean peek(char ch) {
        String line = lines.get(lineNumber);
        if (line.length() <= offset + 1)
            return false;

        return line.charAt(offset + 1) == ch;
    }

    private boolean addToken(ETokenType type, int len) {
        return addToken(new Token(type, currentSplice(len), this));
    }

    private boolean addToken(ETokenType type, StringSplice splice) {
        return addToken(new Token(type, splice, this));
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
