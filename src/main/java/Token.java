import java.util.Optional;

public class Token {
    private final EToken type;
    private final StringSplice splice;
    private final Lexer lexer;

    public Token(EToken token, StringSplice splice, Lexer lexer) {
        this.type = token;
        this.splice = splice;
        this.lexer = lexer;
    }

    public Optional<String> getText() {
        return lexer.getText(splice);
    }
}
