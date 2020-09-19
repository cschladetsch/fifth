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
        while (node != null) {
            switch (node.getType()) {
                case Token:
                case Operation:
                case Value:
                    code.add(node.getValue());
                    break;
                case None:
                    break;
                case Array:
                case Continuation:
                case Comment:
                    notImplemented();
                    break;
                default:
                    fail("Unhandled node " + node);
                    return false;
            }
        }

        return true;
    }
}
