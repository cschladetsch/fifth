public abstract class ProcessBase {
    protected ILogger log = null;
    protected boolean failed = false;
    protected String text = "";

    protected ProcessBase(ILogger logger) {
        this.log = logger;
    }

    abstract boolean run();

    @Override
    public String toString() {
        return "ProcessBase{" +
                "failed=" + failed +
                ", text='" + text + '\'' +
                '}';
    }

    protected boolean fail(Object object) {
        failed = true;
        text = object.toString();
        log.error(text);
        return false;
    }

    public boolean hasFailed() {
        return failed;
    }

    public String getErrorText() {
        return text;
    }

    protected boolean notImplemented() {
        return notImplemented("");
    }

    protected boolean notImplemented(String what) {
        return fail("Not implemented " + what);
    }

    protected void baseReset() {
        failed = false;
        text = "";
    }
}
