import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

enum EOperation {
    None,

    Plus,
    Minus,

    Equiv,
    NotEquiv,

    Assert,
    Break,
    Exit,
    Print,

    Resume,
    Suspend,
    Replace,

    Store,
    Get,
    Exists,
}

public class Executor extends ProcessBase {
    private final Map<String, Object> globals = new HashMap<String, Object>();
    private final Stack<Continuation> context = new Stack<>();
    private final Stack<Object> data = new Stack<>();
    private Continuation currentContinuation;

    public Executor(ILogger logger) {
        super(logger);
    }

    @Override
    public String toString() {
        return "Executor{" +
                "globals=" + globals +
                ", context=" + context +
                ", data=" + data +
                ", continuation=" + currentContinuation +
                '}';
    }

    public void process(Continuation continuation) {
        context.push(continuation);
        execute(EOperation.Suspend);
    }

    private Boolean execute(Object object) {
        if (object.getClass() == EOperation.class) {
            return Execute((EOperation)object);
        }

        data.push(object);
        return true;
    }

    private Boolean Execute(EOperation operation) {
        switch (operation) {
            case Plus: return doBinaryOp(EOperation.Plus);
            case Equiv: return doBinaryOp(EOperation.Equiv);
            case Assert: return doUnaryOp(EOperation.Assert);
            case Suspend: return doSuspend(contextPop());
        }

        return fail("Unsupported operation " + operation);
    }

    private Boolean doSuspend(Continuation continuation) {
        if (context.empty()) {
            return fail("Context empty.");
        }

        currentContinuation = continuation;
        return true;
    }

    private Continuation contextPop() {
        if (context.empty()) {
            fail("Context stack empty.");
            return null;
        }

        return context.pop();
    }

    private Boolean doBinaryOp(EOperation operation) {
        Object second = dataPop();
        Object first = dataPop();

        if (first.getClass() == Integer.class) {
            int firstInt = (int)first;
            int secondInt = (int)second;
            data.push(firstInt + secondInt);
            return true;
        }

        return false;
    }

    private Object dataPop() {
        if (data.empty()) {
            fail("Empty data stack.");
            return null;
        }

        return data.pop();
    }

    private Boolean doUnaryOp(EOperation eOperation) {
        return false;
    }
}
