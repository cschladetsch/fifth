import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger implements ILogger {
    //private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");

    public void debug(String text) {
        print(System.out, "debug", text);
    }

    public void warning(String text) {
        print(System.out, "warn", text);
    }

    public void info(String text) {
        print(System.out, "info", text);
    }

    public void error(String text) {
        print(System.err, "error", text);
    }

    public void error(Exception e) {
        error(e.toString());
        e.printStackTrace();
    }

    private String timeNow() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.format(formatter);
    }

    private void print(PrintStream out, String type, String text) {
        out.println(String.format("%s: %s: %s", timeNow(), type, text));
    }
}
