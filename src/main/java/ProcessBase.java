public abstract class ProcessBase {
    protected ILogger log = null;
    private boolean failed = false;
    private String text = "";

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

    protected boolean fail(String text) {
        failed = true;
        this.text = text;
        log.error(text);
        return false;
    }

    public boolean hasFailed() {
        return failed;
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
