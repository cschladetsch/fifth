import java.util.EnumSet;

public interface ILogSink {
    void setOutputs(EnumSet<ELogLevel> logLevels);
    boolean contains(ELogLevel logLevel);
    void print(ELogLevel level, String text);
    void close();
}
