import java.util.Optional;

public class Token {
    private final ETokenType type;
    private final StringSplice splice;
    private final Lexer lexer;

    public Token(ETokenType token, StringSplice splice, Lexer lexer) {
        this.type = token;
        this.splice = splice;
        this.lexer = lexer;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                "text=" + (getText().isPresent() ? getText().get() : "") +
                '}';
    }

    public Optional<String> getText() {
        return lexer.getText(splice);
    }

    public ETokenType getType() {
        return type;
    }
}
