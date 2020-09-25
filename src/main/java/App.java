import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class App {
    private static ILogger log;
    private Object tests;
    private Object options;

    public static void main(String[] argv) {
        log = new Logger();
        log.info("fifth-sys v0.1");
        log.setVerbosity(0);

        int result = 0;
        try {
            result = new App().run(argv);
        } catch (Exception e) {
            log.error("Main: " + e.toString());
            for (StackTraceElement frame : e.getStackTrace()) {
                log.error("Main: " + frame.toString());
            }

            result = -1;
        }

        log.close();

        System.exit(result);
    }

    public App() {
        FileUtil.contents("config.json").ifPresent(this::parseConfig);
    }

    private void parseConfig(List<String> jsonText) {
        String allText = StringUtils.join(jsonText, "");
        JSONObject json = JSONObject.fromObject(allText);
        tests = json.get("tests");
        options = json.get("options");
    }

    private int run(String[] argv) {
        for (String fileName : argv) {
            if (!run(fileName)) {
                return -1;
            }
        }

        return 0;
    }

    private boolean run(String fileName) {
        File root = Paths.get(fileName).toFile();
        if (root.isDirectory()) {
            return runAll(root);
        }

        CodeSource processor = new CodeSource(log, Paths.get(fileName));
        log.debug("File: " + fileName);
        processor.run();
        PiExecutionContext context = new PiExecutionContext(log, processor.getCodeText());
        if (!context.run()) {
            log.debug(context);
        }

        processor.close();
        return true;
    }

    private boolean runAll(File root) {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (file.isFile()) {
                if (!run(file.getAbsolutePath())) {
                    return false;
                }
            }
        }

        return true;
    }
}
