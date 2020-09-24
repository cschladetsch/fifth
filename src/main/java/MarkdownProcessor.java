import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// TODO: Use for all files, not just Markdown.
public class MarkdownProcessor extends ProcessBase implements IPrinter {
    private final List<String> code = new ArrayList<>();
    private final Path path;
    private EnumSet<ELogLevel> logLevels = EnumSet.allOf(ELogLevel.class);
    private List<String> text = new ArrayList<>();
    private FileWriter fileWriter;

    public MarkdownProcessor(ILogger logger, Path path) {
        super(logger);
        this.path = path;
        logger.addLogger(this);
    }

    @Override
    boolean run() {
        String pathName = path.toString();
        FileUtil.newWriter(pathName + ".txt").ifPresent(f -> fileWriter = f);
        FileUtil.contents(pathName).ifPresent(this::gatherCode);
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
    }

    public List<String> getCodeText() {
        return code;
    }

    private void gatherCode(List<String> text) {
        this.text = text;
        int lineNumber = 0;
        while (lineNumber < text.size()) {
            String line = text.get(lineNumber);
            if (line.trim().startsWith("```f")) {
                lineNumber = readCodeLines(lineNumber);
            }
            else {
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
