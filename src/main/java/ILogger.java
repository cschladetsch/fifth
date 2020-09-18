import java.io.IOException;

public interface ILogger {
    void debug(String text);
    void warning(String text);
    void info(String text);
    void error(String text);
    void error(Exception e);
}
