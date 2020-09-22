import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

enum EOperation {
    None,

    Plus,
    Minus,
    Multiply,
    Divide,

    Not,
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
    Erase,
    Duplicate,

    Dump,
    Depth, Swap, Drop, Clear,
}

public class Executor extends ProcessBase {
    private final Map<String, Object> globals = new HashMap<String, Object>();
    private final Stack<Continuation> context = new Stack<>();
    private final Stack<Object> data = new Stack<>();
    private boolean breakFlow;
    private final float FLOAT_EPSLION = 0.00000001f;
    private Continuation continuation;

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
        if (context.empty()) {
            return false;
        }

        return process(contextPop().get());
    }

    public boolean run(Continuation continuation) {
        dataPush(continuation);
        return execute(EOperation.Replace);
    }

    private boolean execute(Object object) {
        if (object instanceof EOperation) {
            return executeOperation((EOperation) object) || fail("Failed to execute " + object);
        }

        if (object instanceof ETokenType) {
            return execute(convertToken((ETokenType)object));
        }

        return dataPush(object);
    }

    private Object convertToken(ETokenType token) {
        switch (token) {
            case Plus:
                return EOperation.Plus;
            case Minus:
                return EOperation.Minus;
            case Equiv:
                return EOperation.Equiv;
            case NotEquiv:
                return EOperation.NotEquiv;
            case Assert:
                return EOperation.Assert;
            case Dup:
                return EOperation.Duplicate;
            case Print:
                return EOperation.Print;
            case Break:
                return EOperation.Break;
            case Dump:
                return EOperation.Dump;
            case Not:
                return EOperation.Not;
            case Multiply:
                return EOperation.Multiply;
            case Divide:
                return EOperation.Divide;
            case Depth:
                return EOperation.Depth;
            case Suspend:
                return EOperation.Suspend;
            case Replace:
                return EOperation.Replace;
            case Comment:
                return true;
            case Swap:
                return EOperation.Swap;
            case Drop:
                return EOperation.Drop;
            case Erase:
                return EOperation.Erase;
            case Exists:
                return EOperation.Exists;
            case Store:
                return EOperation.Store;
            case Get:
                return EOperation.Get;
            case Clear:
                return EOperation.Clear;
            default:
                return fail("Couldn't convert token " + token + " to something to do.");
        }
    }

    private boolean executeOperation(EOperation operation) {
        switch (operation) {
            case Plus:
            case Minus:
            case Multiply:
            case Divide:
            case Equiv:
                return doBinaryOp(operation);
            case Assert:
                return doUnaryOp(EOperation.Assert);
            case Break:
                return breakFlow = true;
            case Duplicate:
                return doDuplicate();
            case Print:
                return doPrint();
            case Suspend:
                return doSuspend();
            case Replace:
                return doReplace();
            case Not:
                return doNot();
            case Dump:
                logger.debug(this.toString());
                return true;
            case Depth:
                return dataPush(data.size());
            case Store:
                return doStore();
            case Get:
                return doGet();
            case Exists:
                return doExists();
            case Erase:
                return doErase();
            case Swap:
                return doSwap();
            case Drop:
                return doDrop();
            case Clear:
                return doClear();
            default:
                break;
        }

        return fail("Unsupported operation " + operation);
    }

    private boolean doErase() {
        Optional<Identifier> ident = popIdentifier();
        if (!ident.isPresent()) {
            return false;
        }

        String name = ident.get().getName();
        if (continuation.hasLocal(name)) {
            continuation.removeLocal(name);
            return true;
        }

        if (globals.containsKey(name)) {
            globals.remove(name);
            return true;
        }

        return fail("Couldn't find a '" + name + "' to erase");
    }

    private Optional<Identifier> popIdentifier() {
        Object object = dataPop();
        if (object instanceof Identifier) {
            return Optional.of((Identifier)object);
        }

        fail("Expected ident, got " + object);
        return Optional.empty();
    }

    private boolean doClear() {
        data.clear();
        return true;
    }

    private boolean doDrop() {
        if (data.empty()) {
            return fail("Empty stack.");
        }
        dataPop();
        return true;
    }

    private boolean doSwap() {
        if (data.size() < 2) {
            return fail("Swap: data stack too small.");
        }

        Object a = dataPop();
        Object b = dataPop();
        dataPush(a);
        dataPush(b);
        return true;
    }

    private boolean doExists() {
        Identifier ident = (Identifier)dataPop();
        String name = ident.getName();
        boolean exists = continuation.hasLocal(name) || globals.containsKey(name);
        return dataPush(exists);
    }

    private boolean doGet() {
        return resolve((Identifier) dataPop());
    }

    private boolean resolve(Identifier ident) {
        if (ident.isQuoted()) {
            return dataPush(ident);
        }

        String name = ident.getName();
        Object local = continuation.getLocal(name);
        if (local != null) {
            return dataPush(local);
        }

        if (globals.containsKey(name)) {
            return dataPush(globals.get(name));
        }

        return fail("No ident called '" + name + "' in local or global scope.");
    }

    private boolean doStore() {
        Identifier ident = (Identifier)dataPop();
        Object val = dataPop();
        continuation.setLocal(ident.getName(), val);
        return true;
    }

    private boolean doNot() {
        Object obj = dataPop();
        if (obj.getClass() == Boolean.class) {
            return dataPush(!(boolean)obj);
        }

        if (obj.getClass() == Integer.class) {
            return dataPush((int)obj != 0);
        }

        if (obj.getClass() == Float.class) {
            return dataPush(Math.abs((float)obj) > FLOAT_EPSLION);
        }

        if (obj.getClass() == String.class) {
            String str = (String)obj;
            return dataPush(!str.isEmpty());
        }

        return notImplemented("Cannot negate type " + obj.getClass().getSimpleName());
    }

    private boolean doPrint() {
        Object obj = dataPop();
        logger.info(obj.getClass().getSimpleName() + "=" + obj.toString());
        return true;
    }

    private boolean doDuplicate() {
        Object orig = data.peek();
        if (orig.getClass() == Integer.class) {
            return dataPush((int)orig);
        }

        if (orig.getClass() == Float.class) {
            return dataPush((float)orig);
        }

        if (orig.getClass() == String.class) {
            return dataPush((String)orig);
        }

        return notImplemented("Duplicate " + orig.getClass().getName());
    }

    private boolean doSuspend() {
        if (context.empty()) {
            return fail("Context empty.");
        }

        Optional<Continuation> prev = contextPop();
        dataPush(continuation);
        prev.ifPresent(context::push);

        return run();
    }

    private boolean doReplace() {
        if (!context.empty()) {
            context.pop();
        }

        contextPush((Continuation)dataPop());
        return breakFlow = true;
    }

    private boolean process(Continuation current) {
        continuation = current;
        while (true) {
            Object next = current.next();
            while (next != null) {
                if (!execute(next) || hasFailed()) {
                    return fail("Failed to continue " + current + " at object " + current.getCurrent());
                }

                if (breakFlow) {
                    breakFlow = false;
                    break;
                }

                next = current.next();
            }

            Optional<Continuation> nextContinuation = contextPop();
            if (!nextContinuation.isPresent()) {
                break;
            }

            continuation = nextContinuation.get();
        }

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
                return neitherNull(first, second) && doPlus(first, second);
            case Minus:
                return neitherNull(first, second) && doMinus(first, second);
            case Multiply:
                return neitherNull(first, second) && doMultiply(first, second);
            case Divide:
                return neitherNull(first, second) && doDivide(first, second);
            case Equiv:
                return doEquiv(first, second);
            default:
                return notImplemented();
        }
    }

    private boolean doDivide(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            return dataPush((int)first / (int)second);
        }

        if (first.getClass() == Float.class) {
            return dataPush((Float)first / (float)second);
        }

        return notImplemented("Multiply " + first.getClass().getSimpleName() + " by " + second.getClass().getSimpleName());
    }

    private boolean doMultiply(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            return dataPush((int)first * (int)second);
        }

        if (first.getClass() == Float.class) {
            return dataPush((Float)first * (float)second);
        }

        return notImplemented("Multiply " + first.getClass().getSimpleName() + " by " + second.getClass().getSimpleName());
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

        return dataPush(first.equals(second));
    }

    private boolean neitherNull(Object first, Object second) {
        if (first == null || second == null) {
            return fail("Unexpected null value");
        }

        return true;
    }

    private boolean doMinus(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            return dataPush((int) first - (int) second);
        }

        return notImplemented(first.getClass().getName() + " - " + second.getClass().getName());
    }

    private boolean doPlus(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            return dataPush((int) first + (int) second);
        }

        if (first.getClass() == String.class) {
            return dataPush((String)first + (String)second);
        }

        return notImplemented(first.getClass().getTypeName() + " + " + second.getClass().getName());
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
                    //fail("FAILED");
                    return false;
                }

                logger.debug("Passed");
                return true;
            }

            case Print: {
                logger.info(dataPop().toString());
                return true;
            }

            default:
                return notImplemented(operation.toString());
        }
    }

    public boolean dataPush(Object object) {
        if (object instanceof Identifier) {
            Identifier ident = (Identifier)object;
            if (!ident.isQuoted()) {
                return resolve(ident);
            }
        }

        data.push(object);
        return true;
    }

    private boolean trueEval(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof Integer) {
            return (int) object != 0;
        }

        if (object instanceof Float) {
            return Math.abs((float)object) > FLOAT_EPSLION;
        }

        if (object instanceof Boolean) {
            return (Boolean) object;
        }

        return fail("Cannot convert type " + object.getClass().getSimpleName() + " to boolean.");
    }

    public Stack<Object> getDataStack() {
        return data;
    }
}
