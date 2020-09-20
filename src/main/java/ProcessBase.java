public abstract class ProcessBase {
    protected ILogger logger = null;
    private boolean failed = false;
    private String text = "";

    protected ProcessBase(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public String toString() {
        return "ProcessBase{" +
                "failed=" + failed +
                ", text='" + text + '\'' +
                '}';
    }

    protected Boolean fail(String text) {
        failed = true;
        this.text = text;
        logger.error(text);
        return false;
    }

    public Boolean hasFailed() {
        return failed;
    }

    protected boolean notImplemented() {
        return notImplemented("");
    }

    protected boolean notImplemented(String what) {
        return fail("Not implemented " + what);
    }

    public void reset() {
        failed = false;
        text = "";
    }

    abstract boolean run();
}
