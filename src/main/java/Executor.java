import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private boolean breakFlow;
    private final float FLOAT_EPSLION = 0.00000001f;

    public Executor(ILogger logger) {
        super(logger);
    }

    @Override
    public String toString() {
        return "Executor{" +
                "globals=" + globals +
                ", context=" + context +
                ", data=" + data +
                ", continuation=" + (context.empty() ? "" : context.peek()) +
                '}';
    }

    @Override
    boolean run() {
        return execute(EOperation.Suspend);
    }

    public void run(Continuation continuation) {
        context.push(continuation);
        execute(EOperation.Suspend);
    }

    private Boolean execute(Object object) {
        if (object.getClass() == EOperation.class) {
            return executeOperation((EOperation) object);
        }

        data.push(object);
        return true;
    }

    private Boolean executeOperation(EOperation operation) {
        switch (operation) {
            case Plus:
                return doBinaryOp(EOperation.Plus);
            case Equiv:
                return doBinaryOp(EOperation.Equiv);
            case Assert:
                return doUnaryOp(EOperation.Assert);
            case Suspend:
                return doSuspend();
        }

        return fail("Unsupported operation " + operation);
    }

    private boolean doSuspend() {
        if (context.empty()) {
            return fail("Context empty.");
        }

        Continuation current = context.peek();
        Optional<Continuation> prev = contextPop();
        context.push(current);
        prev.ifPresent(context::push);

        return true;
    }

    public void contextPush(Continuation continuation) {
        context.push(continuation);
    }

    public Optional<Continuation> contextPop() {
        if (context.empty()) {
            return Optional.empty();
        }
        return Optional.of(context.pop());
    }

    private boolean doBinaryOp(EOperation operation) {
        Object second = dataPop();
        Object first = dataPop();

        switch (operation) {
            case Plus:
                return notNull(first, second) && doPlus(first, second);
            case Minus:
                return notNull(first, second) && doMinus(first, second);
            case Equiv:
                return doEquiv(first, second);
            default:
                return notImplemented();
        }
    }

    private boolean doEquiv(Object first, Object second) {
        if (first == null) {
            return second == null;
        }

        if (second == null) {
            return fail("Cannot compare value to null");
        }

        if (first.getClass() == Float.class || second.getClass() == Float.class) {
            return Math.abs((float)first - (float)second) > FLOAT_EPSLION;
        }

        return first.equals(second);
    }

    private boolean notNull(Object first, Object second) {
        if (first == null || second == null) {
            return fail("Unexpected null value");
        }

        return true;
    }

    private boolean doMinus(Object first, Object second) {
        return notImplemented("Minus");
    }

    private boolean doPlus(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            data.push((int)first + (int)second);
            return true;
        }

        if (first.getClass() == String.class) {
            data.push((String)first + (String)second);
            return true;
        }

        return fail("Not implemented: " + first.getClass().getName() + " + " + second.getClass().getName());
    }

    private Object dataPop() {
        if (data.empty()) {
            fail("Empty data stack.");
            return null;
        }

        return data.pop();
    }

    private boolean doUnaryOp(EOperation operation) {
        switch (operation) {
            case Assert: {
                if (!trueEval(dataPop())) {
                    fail("Assertion failed.");
                    return false;
                }
            }

            case Print: {
                logger.info(dataPop().toString());
                return true;
            }
        }
        return false;
    }

    private boolean trueEval(Object object) {
        if (object == null) {
            return false;
        }

        if (object.getClass() == Integer.class) {
            return (int) object != 0;
        }

        if (object.getClass() == Float.class) {
            return Math.abs((float)object) > FLOAT_EPSLION;
        }

        if (object.getClass() == Boolean.class) {
            return (Boolean) object;
        }

        return true;
    }
}
