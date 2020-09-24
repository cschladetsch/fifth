import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StripMarkdown extends ProcessBase {
    private List<String> code = new ArrayList<>();
    private List<String> text = new ArrayList<>();
    private Path path;

    public StripMarkdown(ILogger logger, Path path) {
        super(logger);
        this.path = path;
    }

    @Override
    boolean run() {
        App.fileContents(path.toString()).ifPresent(this::process);
        return true;
    }

    public List<String> getCodeText() {
        return code;
    }

    private void process(List<String> text) {
        this.text = text;
        int lineNumber = 0;
        while (lineNumber < text.size()) {
            String line = text.get(lineNumber);
            if (line.trim().startsWith("```f")) {
                lineNumber = readCode(lineNumber);
            }
            else {
                ++lineNumber;
            }
        }
    }

    private int readCode(int lineNumber) {
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
