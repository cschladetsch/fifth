import java.util.EnumSet;

public interface ILogger {
    void setOutputs(EnumSet<ELogLevel> logLevels);
    void addLogger(ILogSink chain);
    void debug(Object text);
    void warn(Object text);
    void info(Object text);
    void error(Object text);
    void error(Exception e);
    void verbose(int verbosity, Object text);
    void setVerbosity(int verbosity);
    void close();
    int getVerbosity();

    void removeLogger(ILogSink logSink);
}
