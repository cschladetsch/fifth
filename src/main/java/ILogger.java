import java.io.IOException;

public interface ILogger {
    void debug(String text);
    void warn(String text);
    void info(String text);
    void error(String text);
    void error(Exception e);
    void verbose(int verbosity, String text);
    void setVerbosity(int verbosity);
    int getVerbosity();
}
