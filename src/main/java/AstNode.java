import java.util.ArrayList;
import java.util.List;

enum EAstNodeType {
    None,
    Operation,
    Value,
    Array,
    Continuation,
    Comment,
    Token,
}

public class AstNode {
    private EAstNodeType type;
    private Object value;
    private ETokenType tokenType;
    private List<AstNode> children = new ArrayList<>();

    public AstNode(EAstNodeType type) {
        this.type = type;
    }

    public AstNode(EAstNodeType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public AstNode(ETokenType tokenType) {
        this.type = EAstNodeType.Token;
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "AstNode{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }

    public EAstNodeType getType() {
        return type;
    }

    public Object getValue() {
        return type == EAstNodeType.Token ? tokenType : value;
    }

    public List<AstNode> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void addChild(AstNode node) {
        children.add(node);
    }
}
