import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger implements ILogger {
    //private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
    private static final LocalDateTime startTime = LocalDateTime.now();
    private int verbosity = 0;

    @Override
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    @Override
    public int getVerbosity() {
        return verbosity;
    }

    // use Object and not String as input arguments for all logging methods
    public void debug(Object text) {
        print(System.out, "debug", text);
    }

    public void warn(Object text) {
        print(System.out, "warn", text);
    }

    public void info(Object text) {
        print(System.out, "info", text);
    }

    public void error(Object text) {
        print(System.err, "error", text);
    }

    public void error(Exception e) {
        error(e.toString());
        e.printStackTrace();
    }

    @Override
    public void verbose(int verbosity, Object text) {
        if (this.verbosity < verbosity)
            return;
        print(System.out, "verbose", text.toString());
    }

    private String timeStamp() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Duration since = Duration.between(startTime, localDateTime);
        long secs = since.getSeconds();
        long mins = secs / 60;
        long millis = since.toMillis();
        return String.format("%2s:%2s:%3s", mins, secs, millis);
    }

    private void print(PrintStream out, String type, Object text) {
        //out.println(String.format("%s: %s: %s: %s", timeStamp(), type, getClass().getTypeName(), text));
        out.println(String.format("%s: %s: %s", timeStamp(), type, text));
    }
}
