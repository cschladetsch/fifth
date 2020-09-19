import java.util.ArrayList;
import java.util.List;

public class Parser extends ProcessBase {
    private Lexer lexer;
    private final List<AstNode> tree = new ArrayList<>();
    private AstNode current;

    public Parser(Lexer lexer) {
        super(lexer.logger);
    }

    public boolean run() {
        for (Token token : lexer.getTokens()) {
            switch (token.getType()) {
                case Number:
                    if (!addNumber(token))
                        return false;
                    break;
                case Plus:
                case Equiv:
                case Assert:
                    addNode(token.getType());
                    break;
                case OpenParan:
                case OpenBrace:
                case OpenSquareBracket:
                    return fail("Not implemented");
                default:
                    throw new IllegalStateException("Unexpected value: " + token.getType());
            }
        }

        return true;
    }

    private void addNode(ETokenType tokenType) {
        current = new AstNode(tokenType);
        tree.add(current);
    }

    private void addNode(EAstNodeType type, int val) {
        current = new AstNode(type, val);
        tree.add(current);
    }

    private boolean addNumber(Token token) {
        try {
            int val = Integer.parseInt(token.getText().get());
            addNode(EAstNodeType.Value, val);
        } catch (Exception e) {
            fail("Failed to convert " + token.getText() + " to a number.");
        }

        return true;
    }

    private void addChildNode(EAstNodeType type, int val) {
        current.addChild(new AstNode(type, val));
    }

    private void leaveChild() {
        current = tree.get(tree.size() - 1);
    }

    public AstNode getRoot() {
        return tree.get(0);
    }
}
