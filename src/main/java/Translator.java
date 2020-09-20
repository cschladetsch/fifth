import java.util.ArrayList;
import java.util.List;

public class Translator extends ProcessBase {
    private final Parser parser;
    private Continuation continuation = new Continuation();
    private List<Object> code = new ArrayList<Object>();

    public Translator(Parser parser) {
        super(parser.logger);
        this.parser = parser;
    }

    @Override
    public boolean run() {
        AstNode node = parser.getRoot();
        continuation = translateContinuation(node);
        return !hasFailed();
    }

    private Continuation translateContinuation(AstNode node) {
        Continuation continuation = new Continuation(translateChildren(node));
        return continuation;
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
}
