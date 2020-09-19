public class ProcessBase {
    protected ILogger logger;
    private Boolean failed;
    private String text;

    protected ProcessBase(ILogger logger) {
        this.logger = logger;
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

    @Override
    public String toString() {
        return "ProcessBase{" +
                "failed=" + failed +
                ", text='" + text + '\'' +
                '}';
    }

    public void reset() {
        failed = false;
        text = "";
    }
}
