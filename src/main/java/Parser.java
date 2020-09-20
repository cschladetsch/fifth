import java.util.Stack;

public class Parser extends ProcessBase {
    private final Lexer lexer;
    private final Stack<AstNode> stack = new Stack<>();
    private AstNode current;

    public Parser(Lexer lexer) {
        super(lexer.logger);
        this.lexer = lexer;
        this.current = enterNode(EAstNodeType.Continuation);
    }

    @Override
    public String toString() {
        return "Parser{" +
                "stack=" + stack +
                ", current=" + current +
                '}';
    }

    public boolean run() {
        for (Token token : lexer.getTokens()) {
            if (!parseToken(token)) {
                break;
            }
        }

        return !hasFailed();
    }

    private boolean parseToken(Token token) {
        switch (token.getType()) {
            case Number:
                return addNumber(token);
            case Plus:
            case Minus:
            case Equiv:
            case Assert:
            case Break:
            case Dup:
            case Print:
            case Dump:
            case Comment:
                return addToken(token.getType());
            case OpenParan:
            case OpenBrace:
            case OpenSquareBracket:
            case CloseParan:
            case CloseBrace:
            case CloseSquareBracket:
                return notImplemented();
            case Whitespace:
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + token.getType());
        }
    }

    private boolean addToken(ETokenType tokenType) {
        return addChild(new AstNode(tokenType));
    }

    private boolean addChild(EAstNodeType type, Object val) {
        return addChild(new AstNode(type, val));
    }

    private boolean addChild(AstNode child) {
        current.addChild(child);
        return true;
    }

    private boolean addNumber(Token token) {
        try {
            int val = Integer.parseInt(token.getText().get());
            return addChild(EAstNodeType.Value, val);
        } catch (Exception e) {
            fail("Failed to convert " + token.getText() + " to a number.");
        }

        return false;
    }

    private AstNode enterNode(EAstNodeType type) {
        AstNode node = new AstNode(type);
        stack.push(node);
        return node;
    }

    private AstNode leaveNode() {
        return current = stack.pop();
    }

    public AstNode getRoot() {
        if (stack.size() != 1) {
            fail("Unbalanced parser stack.");
            return null;
        }

        return stack.peek();
    }
}
