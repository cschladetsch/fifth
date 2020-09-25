import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Logger implements ILogger {
    private static final LocalDateTime startTime = LocalDateTime.now();
    private final List<ILogSink> chainedLogs = new ArrayList<>();
    private EnumSet<ELogLevel> logLevels = EnumSet.allOf(ELogLevel.class);
    private int verbosity = 0;

    public Logger() {
        logLevels.remove(ELogLevel.StackTrace);
    }

    @Override
    public int getVerbosity() {
        return verbosity;
    }

    @Override
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    @Override
    public void removeLogger(ILogSink logSink) {
        chainedLogs.remove(logSink);
    }

    @Override
    public void close() {
        for (ILogSink other : chainedLogs) {
            other.close();
        }
    }

    @Override
    public void setOutputs(EnumSet<ELogLevel> logLevels) {
        this.logLevels = logLevels;
    }

    @Override
    public void addLogger(ILogSink next) {
        if (!chainedLogs.contains(next)) {
            chainedLogs.add(next);
        }
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
        return String.format("%02d:%02d:%03d", mins, secs, millis);
    }

    private void print(PrintStream out, ELogLevel type, Object text) {
        String output = String.format("%s: %7s: %s", timeStamp(), type, text);
        if (logLevels.contains(type)) {
            out.println(output);
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (ILogSink other : chainedLogs) {
            if (other.contains(ELogLevel.StackTrace)) {
                for (StackTraceElement st : stackTrace) {
                    other.print(type, st.toString());
                }
            }
        }

        if (logLevels.contains(ELogLevel.StackTrace)) {
            for (StackTraceElement st : stackTrace) {
                out.println(st);
            }
        }
    }
}
