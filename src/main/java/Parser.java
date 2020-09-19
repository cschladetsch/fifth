import java.util.ArrayList;
import java.util.List;

public class Parser extends ProcessBase {
    private Lexer lexer;
    private final List<AstNode> tree = new ArrayList<>();

    public Parser(Lexer lexer) {
        super(lexer.logger);
    }
}
