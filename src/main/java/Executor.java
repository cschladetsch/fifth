import java.util.*;

public class Executor extends ProcessBase {
    private final Map<String, Object> globals = new HashMap<>();
    private final Stack<Continuation> context = new Stack<>();
    private final Stack<Object> data = new Stack<>();
    private final float FLOAT_EPSILON = 0.00000001f;
    private final boolean showTypesInPrint = false;
    private Continuation continuation;
    private boolean exitProcess;
    private boolean breakFlow;

    public Executor(ILogger logger) {
        super(logger);
    }

    public Executor(ILogger logger, Continuation continuation) {
        super(logger);
        run(continuation);
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
        contextPop().ifPresent(this::process);
        return !hasFailed();
    }

    public boolean run(Continuation continuation) {
        return process(continuation);
    }

    private boolean execute(Object object) {
        if (object instanceof ETokenType) {
            return execute(((ETokenType) object));
        }

        return dataPush(object);
    }

    private boolean execute(ETokenType token) {
        if (hasFailed()) {
            return false;
        }

        switch (token) {
            case Plus:
            case Minus:
            case Multiply:
            case Divide:
            case Equiv:
            case Less:
            case LessEqual:
            case Greater:
            case GreaterEqual:
                return doBinaryOp(token);
            case Assert:
                return doUnaryOp(EOperation.Assert);
            case Break:
                return breakFlow = true;
            case Dup:
                return doDuplicate();
            case Print:
                return doPrint();
            case Suspend:
                return doSuspend();
            case Replace:
                return doReplace();
            case Resume:
                return doResume();
            case Not:
                return doNot();
            case Dump:
                log.debug(this.toString());
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
            case If:
                return doIf();
            case IfElse:
                return doIfElse();
            case For:
                return doFor();
            case True:
                return dataPush(true);
            case False:
                return dataPush(false);
            case Exit:
                return exitProcess = true;
            case ShowStack:
                return doShowStack();
            case Size:
                return doSize();
            case ToArray:
                return doToArray();
            case Expand:
                return doExpand();
            case At:
                return doAt();
            default:
                break;
        }

        return fail("Unsupported operation " + token);
    }

    private boolean doAt() {
        int index = (int)dataPop();
        List array = (List)dataPop();
        return dataPush(array.get(index));
    }

    private boolean doExpand() {
        List array = (List)(dataPop());
        for (Object item : array) {
            dataPush(item);
        }

        return dataPush(array.size());
    }

    private boolean doToArray() {
        if (data.empty()) {
            return fail("Empty stack.");
        }

        // Dirk: How to ensure popped item is of a given type?
        int len = (int) dataPop();
        if (data.size() < len) {
            return fail("ToArray: Empty Stack");
        }

        List<Object> array = new ArrayList<>();
        while (len-- > 0) {
           array.add(dataPop());
        }
        dataPush(array);

        return true;
    }

    private boolean doSize() {
        Object container = dataPop();

        if (container instanceof String) {
            return dataPush(((String)container).length());
        }

        // Dirk: How to use List rather than ArrayList?
        if (container instanceof ArrayList) {
            ArrayList array = (ArrayList)container;
            return dataPush(array.size());
        }

        // Dirk: how to generalise maps?
//        if (container instanceof Map<>)
//            Map map = (Map)map;
//            return dataPush(map.size());
//        }

        return false;
    }

    private boolean doResume() {
        return breakFlow = true;
    }

    public String createStackString() {
        StringBuilder stringBuilder = new StringBuilder();
        int n = data.size() - 1;
        for (Object obj : data) {
            String text = String.format("[%2d]: %s", n--, obj);
            stringBuilder.append(text);
            if (n > -1)
                stringBuilder.append("\n");
        }
        stringBuilder.insert(0, "\n");
        return stringBuilder.toString();
    }

    private boolean doShowStack() {
        log.info(createStackString());
        return true;
    }

    private boolean doFor() {
        return false;
    }

    private boolean doIfElse() {
        if (dataPopEvalFalse())
            doSwap();
        dataPop();
        return true;
    }

    private boolean doIf() {
        if (dataPopEvalFalse())
            dataPop();
        return true;
    }

    private boolean dataPopEvalFalse() {
        return !trueEval(dataPop());
    }

    private boolean doErase() {
        Optional<Identifier> ident = popIdentifier();
        if (!ident.isPresent()) {
            return false;
        }

        String name = ident.get().getName();
        if (removeLocal(continuation, name)) {
            return true;
        }

        for (Continuation continuation : context) {
            if (removeLocal(continuation, name)) {
                return true;
            }
        }

        if (globals.containsKey(name)) {
            globals.remove(name);
            return true;
        }

        return fail("Couldn't find a '" + name + "' to erase");
    }

    private boolean removeLocal(Continuation continuation, String name) {
        if (!continuation.hasLocal(name)) {
            return false;
        }

        continuation.removeLocal(name);
        return true;
    }

    private Optional<Identifier> popIdentifier() {
        Object object = dataPop();
        if (object instanceof Identifier) {
            return Optional.of((Identifier) object);
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
        Identifier ident = (Identifier) dataPop();
        if (ident == null)
            return false;

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

        for (Continuation up : context) {
            if (up.hasLocal(name)) {
                return dataPush(up.getLocal(name));
            }
        }

        if (globals.containsKey(name)) {
            return dataPush(globals.get(name));
        }

        return fail("No ident called '" + name + "' in local or global scope.");
    }

    private boolean doStore() {
        Identifier ident = (Identifier) dataPop();
        if (ident == null) {
            return fail("Identifier expected");
        }

        Object val = dataPop();
        continuation.setLocal(ident.getName(), val);
        return true;
    }

    private boolean doNot() {
        Object obj = dataPop();
        if (obj == null) {
            return true;
        }

        if (obj instanceof Boolean) {
            return dataPush(!(boolean) obj);
        }

        if (obj instanceof Integer) {
            return dataPush((int) obj == 0);
        }

        if (obj instanceof Float) {
            return dataPush(Math.abs((float) obj) > FLOAT_EPSILON);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            return dataPush(!str.isEmpty());
        }

        return notImplemented("Cannot negate type " + obj.getClass().getSimpleName());
    }

    private boolean doPrint() {
        Object obj = dataPop();
        if (obj == null) {
            log.info("null");
        } else if (showTypesInPrint) {
            log.info(obj.getClass().getSimpleName() + "=" + obj.toString());
        } else {
            log.info(obj.toString());
        }

        return true;
    }

    private boolean doDuplicate() {
        Object orig = data.peek();
        if (orig instanceof Integer) {
            return dataPush(orig);
        }

        if (orig instanceof Float) {
            return dataPush(orig);
        }

        if (orig instanceof String) {
            return dataPush(orig);
        }

        return notImplemented("Duplicate " + orig.getClass().getName());
    }

    private boolean doSuspend() {
        Object next = dataPop();
        if (next instanceof Continuation) {
            contextPush(continuation);
            contextPush((Continuation) next);
            return breakFlow = true;
        }

        dataPush(next);
        return fail("Continuation expected.");
    }

    private boolean doReplace() {
        if (!context.empty()) {
            context.pop();
        }

        contextPush((Continuation) dataPop());
        return breakFlow = true;
    }

    private boolean process(Continuation current) {
        continuation = current;
        while (true) {
            Object next = current.next();
            while (next != null) {
                if (!execute(next) || hasFailed()) {
                    return hasFailed() || fail("Failed to continue " + current + " at object " + current.getCurrent());
                }

                if (exitProcess) {
                    return true;
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

            current = continuation = nextContinuation.get();
        }

        return true;
    }

    public Stack<Object> getDataStack() {
        return data;
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

    private boolean doBinaryOp(ETokenType operation) {
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
            case Less:
                return doLess(first, second);
            case LessEqual:
                return doLessEqual(first, second);
            case Greater:
                return doGreater(first, second);
            case GreaterEqual:
                return doGreaterEqual(first, second);
            default:
                return notImplemented();
        }
    }

    private boolean doGreaterEqual(Object first, Object second) {
        return isEquiv(first, second) || doGreater(first, second);
    }

    private boolean doLessEqual(Object first, Object second) {
        return isEquiv(first, second) || doLess(first, second);
    }

    private boolean isEquiv(Object first, Object second) {
        if (performEquiv(first, second)) {
            return true;
        }

        dataPop();
        return false;
    }

    private boolean performEquiv(Object first, Object second) {
        return doEquiv(first, second) && trueEval(data.peek());
    }

    private boolean doGreater(Object first, Object second) {
        if (first instanceof Integer) {
            return dataPush((int) first > (int) second);
        }

        if (first instanceof Float) {
            return dataPush(Math.abs((Float) first - (float) second) < FLOAT_EPSILON);
        }

        return notImplemented("Greater", first, second);
    }

    private boolean doLess(Object first, Object second) {
        if (first instanceof Integer) {
            return dataPush((int) first < (int) second);
        }

        if (first instanceof Float) {
            return dataPush(Math.abs((Float) first - (float) second) > FLOAT_EPSILON);
        }

        return notImplemented("Less ", first, second);
    }

    private boolean doDivide(Object first, Object second) {
        if (first instanceof Integer) {
            return dataPush((int) first / (int) second);
        }

        if (first instanceof Float) {
            return dataPush((Float) first / (float) second);
        }

        return notImplemented("Divide ", first, second);
    }

    private boolean doMultiply(Object first, Object second) {
        if (first instanceof Integer) {
            return dataPush((int) first * (int) second);
        }

        if (first instanceof Float) {
            return dataPush((Float) first * (float) second);
        }

        return notImplemented("Multiply ", first, second);
    }

    private boolean notImplemented(String operation, Object first, Object second) {
        return notImplemented(operation + " " + first.getClass().getSimpleName() + " with " + second.getClass().getSimpleName());
    }

    private boolean doEquiv(Object first, Object second) {
        if (first == null) {
            return second == null;
        }

        if (second == null) {
            return fail("Cannot compare value to null");
        }

        if (first instanceof Float || second instanceof Float) {
            return Math.abs((float) first - (float) second) > FLOAT_EPSILON;
        }

        /* not needed?
        if (first instanceof List) {
            List left = (List)first;
            if (second instanceof List) {
                List right = (List)second;
                if (left.size() != right.size()) {
                    return false;
                }

                for (int n = 0; n < left.size(); ++n) {
                    if (!doEquiv(left.get(n), right.get(n))) {
                        return false;
                    }
                }

                return true;
            }
        }
        */

        return dataPush(first.equals(second));
    }

    private boolean neitherNull(Object first, Object second) {
        if (first == null || second == null) {
            return fail("Unexpected null value.");
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
            return dataPush(first + (String) second);
        }

        return notImplemented(first.getClass().getTypeName() + " + " + second.getClass().getName());
    }

    // TODO: Optional<Object> dataPop() { .. }
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
                    return fail("Failed");
                }

                log.debug("Passed");
                return true;
            }

            case Print: {
                log.info(dataPop());
                return true;
            }

            default:
                return notImplemented(operation.toString());
        }
    }

    public boolean dataPush(Object object) {
        if (object instanceof Identifier) {
            Identifier ident = (Identifier) object;
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
            return Math.abs((float) object) > FLOAT_EPSILON;
        }

        if (object instanceof Boolean) {
            return (Boolean) object;
        }

        return fail("Cannot convert type " + object.getClass().getSimpleName() + " to boolean.");
    }
}
