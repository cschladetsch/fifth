import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
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
    }

    @Override
    boolean run() {
        String pathName = path.toAbsolutePath().toString();
        extension = FileUtil.getFileExtension(pathName);
        if (!extension.equals("md") && !extension.equals("f"))
            return false;

        FileUtil.newWriter(pathName + ".txt").ifPresent(f -> fileWriter = f);
        FileUtil.contents(pathName).ifPresent(this::gatherCode);
        log.addLogger(this);
        return true;
    }

    @Override
    public void setOutputs(EnumSet<ELogLevel> logLevels) {
        this.logLevels = logLevels;
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
        String fileName = path.getFileName().toString();
        if (extension.equals("md")) {
            readMarkdownContents();
        } else if (extension.equals("f")) {
            code = text;
        }
    }

    private void readMarkdownContents() {
        int lineNumber = 0;
        while (lineNumber < text.size()) {
            String line = text.get(lineNumber);
            if (line.trim().startsWith("```f")) {
                lineNumber = readCodeLines(lineNumber);
            } else {
                ++lineNumber;
            }
        }
    }

    private int readCodeLines(int lineNumber) {
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
