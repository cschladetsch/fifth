public interface ILogger {
    void addLogger(IPrinter chain);
    void debug(Object text);
    void warn(Object text);
    void info(Object text);
    void error(Object text);
    void error(Exception e);
    void verbose(int verbosity, Object text);
    void setVerbosity(int verbosity);
    int getVerbosity();
}
