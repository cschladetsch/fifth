import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Logger implements ILogger {
    //private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
    private static final LocalDateTime startTime = LocalDateTime.now();
    private final List<IPrinter> chainedLogs = new ArrayList<>();
    private EnumSet<ELogLevel> logLevels = EnumSet.allOf(ELogLevel.class);
    private int verbosity = 0;

    @Override
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    @Override
    public int getVerbosity() {
        return verbosity;
    }

    @Override
    public void close() {
        for (IPrinter other : chainedLogs) {
            other.close();
        }
    }

    @Override
    public void setOutputs(EnumSet<ELogLevel> logLevels) {
        this.logLevels = logLevels;
    }

    @Override
    public void addLogger(IPrinter next) {
        chainedLogs.add(next);
    }

    // use Object and not String as input arguments for all logging methods
    public void debug(Object text) {
        print(System.out, ELogLevel.Debug, text);
    }

    public void warn(Object text) {
        print(System.out, ELogLevel.Warn, text);
    }

    public void info(Object text) {
        print(System.out, ELogLevel.Info, text);
    }

    public void error(Object text) {
        print(System.err, ELogLevel.Error, text);
    }

    public void error(Exception e) {
        error(e.toString());
        e.printStackTrace();
    }

    public void verbose(int verbosity, Object text) {
        if (this.verbosity < verbosity)
            return;
        print(System.out, ELogLevel.Verbose, text.toString());
    }

    private String timeStamp() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Duration since = Duration.between(startTime, localDateTime);
        long secs = since.getSeconds();
        long mins = secs / 60;
        long millis = since.toMillis();
        return String.format("%2s:%2s:%3s", mins, secs, millis);
    }

    private void print(PrintStream out, ELogLevel type, Object text) {
        String output = String.format("%s: %s: %s", timeStamp(), type, text);
        if (logLevels.contains(type)) {
            out.println(output);
        }

        for (IPrinter other : chainedLogs) {
            other.print(type, output);
        }
    }
}
