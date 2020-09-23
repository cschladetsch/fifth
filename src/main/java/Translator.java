import java.util.ArrayList;
import java.util.List;

public class Translator extends ProcessBase {
    private final Parser parser;
    private Continuation continuation = new Continuation();

    public Translator(Parser parser) {
        super(parser.log);
        this.parser = parser;
    }

    @Override
    public boolean run() {
        continuation = translateContinuation(parser.getRoot());
        return !hasFailed();
    }

    @Override
    public String toString() {
        return "Translator{" +
                continuation +
                '}';
    }

    private Continuation translateContinuation(AstNode node) {
        return new Continuation(translateChildren(node));
    }

    private List<Object> translateChildren(AstNode node) {
        List<Object> code = new ArrayList<>();
        for (AstNode child : node.getChildren()) {
            code.add(translate(child));
            if (hasFailed()) {
                break;
            }
        }

        return code;
    }

    private Object translate(AstNode node) {
        switch (node.getType()) {
            case Operation:
            case Value:
            case Token:
                return node.getValue();
            case Continuation:
                return translateContinuation(node);
            default:
                notImplemented(node.toString());
                return null;
        }
    }

    public Continuation getContinuation() {
        return continuation;
    }
}
