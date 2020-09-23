import java.util.Stack;

public class Parser extends ProcessBase {
    private final Lexer lexer;
    private final Stack<AstNode> stack = new Stack<>();

    public Parser(Lexer lexer) {
        super(lexer.log);
        this.lexer = lexer;
        enterNode(EAstNodeType.Continuation);
    }

    @Override
    public String toString() {
        return "Parser{" +
                "stack=" + stack +
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
            case OpenParan:
            case OpenSquareBracket:
            case CloseParan:
            case CloseSquareBracket:
                return notImplemented();
            case Whitespace:
            case Comment:
                return true;
            case OpenBrace:
                return newContinuation();
            case CloseBrace:
                return endContinuation();
            case QuotedIdent:
                return addChild(EAstNodeType.Value, new Identifier(true, token.getText().get()));
            case Ident:
                return addChild(EAstNodeType.Value, new Identifier(token.getText().get()));
            default:
                return addToken(token.getType());
        }
    }

    private boolean endContinuation() {
        if (stack.empty()) {
            return fail("Parse stack empty.");
        }

        leaveNode();
        return true;
    }

    private boolean newContinuation() {
        enterNode(EAstNodeType.Continuation);
        return true;
    }

    private boolean addToken(ETokenType tokenType) {
        return addChild(new AstNode(tokenType));
    }

    private boolean addChild(EAstNodeType type, Object val) {
        return addChild(new AstNode(type, val));
    }

    private boolean addChild(AstNode child) {
        current().addChild(child);
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

    private void enterNode(EAstNodeType type) {
        AstNode node = new AstNode(type);
        stack.push(node);
    }

    private void leaveNode() {
        if (stack.empty()) {
            fail("Empty Parser context stack");
            throw new IllegalStateException("Parser");
        }

        AstNode inner = stack.pop();
        current().addChild(inner);
    }

    private AstNode current() {
        return stack.peek();
    }

    public AstNode getRoot() {
        if (stack.size() > 1) {
            fail("Unbalanced parser");
            return null;
        }

        return stack.peek();
    }
}
