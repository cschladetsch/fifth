import java.util.EnumSet;

public interface IPrinter {
    void setOutputs(EnumSet<ELogLevel> logLevels);
    void print(ELogLevel level, String text);
    void close();
}
