import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class CodeSource extends ProcessBase implements ILogSink {
    private final Path path;
    private List<String> code = new ArrayList<>();
    private List<String> text = new ArrayList<>();
    private EnumSet<ELogLevel> logLevels = EnumSet.allOf(ELogLevel.class);
    private FileWriter fileWriter;
    private String extension;

    public CodeSource(ILogger logger, Path path) {
        super(logger);
        this.path = path;
        logLevels.remove(ELogLevel.StackTrace);
    }

    @Override
    boolean run() {
        String pathName = path.toAbsolutePath().toString();
        extension = FileUtil.getFileExtension(pathName);
        if (!(extension.equals("md") || extension.equals("pi")))
            return false;

        String newExtension = log.getOutputMarkDown() ? ".out.md" : ".txt";
        FileUtil.newWriter(pathName + newExtension).ifPresent(f -> fileWriter = f);
        FileUtil.contents(pathName).ifPresent(this::gatherCode);
        log.addLogger(this);
        LocalDateTime localDateTime = LocalDateTime.now();
        log.info(String.format("Started *%s* on %s", path.getFileName().toString(), new Date().toString()));
        return true;
    }

    @Override
    public void setOutputs(EnumSet<ELogLevel> logLevels) {
        this.logLevels = logLevels;
    }

    @Override
    public boolean contains(ELogLevel logLevel) {
        return logLevels.contains(logLevel);
    }

    @Override
    public void print(ELogLevel level, String text) {
        if (logLevels.contains(level)) {
            FileUtil.println(fileWriter, text);
        }
    }

    @Override
    public void close() {
        FileUtil.close(fileWriter);
        log.removeLogger(this);
    }

    public List<String> getCodeText() {
        return code;
    }

    private void gatherCode(List<String> text) {
        this.text = text;
        if (extension.equals("md")) {
            readMarkdownContents();
        } else if (extension.equals("pi")) {
            code = text;
        }
    }

    private void readMarkdownContents() {
        int lineNumber = 0;
        while (lineNumber < text.size()) {
            String line = text.get(lineNumber);
            if (line.trim().startsWith("```pi")) {
                lineNumber = readMarkdownCodeLines(lineNumber);
            } else {
                ++lineNumber;
            }
        }
    }

    private int readMarkdownCodeLines(int lineNumber) {
        ++lineNumber;
        while (lineNumber < text.size()) {
            String line = text.get(lineNumber++);
            if (line.trim().startsWith("```")) {
                break;
            }

            code.add(line);
        }

        return lineNumber;
    }
}
