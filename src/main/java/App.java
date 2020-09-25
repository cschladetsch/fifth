import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class App {
    private static ILogger log;

    public App() {
        FileUtil.contents("config.json").ifPresent(this::parseConfig);
    }

    private void parseConfig(List<String> jsonText) {
        String allText = StringUtils.join(jsonText, "");
        JSONObject json = JSONObject.fromObject(allText);
        Object tests = json.get("tests");
        Object options = json.get("options");
    }

    public static void main(String[] argv) {
        log = new Logger();
        log.info("fifth-lang v0.1");
        log.setVerbosity(0);

        int result = 0;
        try {
            result = new App().run(argv);
        } catch (Exception e) {
            for (StackTraceElement frame : e.getStackTrace()) {
                log.error(frame.toString());
            }

            result = -1;
        }

        log.close();

        System.exit(result);
    }

    private boolean stageFailed(ProcessBase process) {
        if (process.run() && !process.hasFailed()) {
            return false;
        }

        log.debug(process.toString());
        log.debug(process.getClass().getSimpleName() + " Failed");
        return true;
    }

    private int run(String[] argv) {
        for (String fileName : argv) {
            if (run(fileName) != 0) {
                return -1;
            }
        }

        //Repl();

        return 0;
    }

    private int run(String fileName) {
        File root = Paths.get(fileName).toFile();
        if (root.isDirectory()) {
            return runAll(root) ? 0 : -1;
        }

        CodeSource processor = new CodeSource(log, Paths.get(fileName));
        log.debug("File: " + fileName);
        processor.run();
        PiExecutionContext context = new PiExecutionContext(log, processor.getCodeText());
        if (!context.run()) {
            log.verbose(10, context);
        }

        processor.close();
        return 0;
    }

    private boolean runAll(File root) {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (file.isFile()) {
                if (run(file.getAbsolutePath()) != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private void Repl() {
        Executor executor = new Executor(log);
        while (true) {
            System.out.print("λ ");
            String text = System.console().readLine();

            Lexer lexer = new Lexer(log, text);
            if (!lexer.run()) {
                continue;
            }

            Parser parser = new Parser(lexer);
            if (!parser.run()) {
                continue;
            }

            Translator translator = new Translator(parser);
            if (!translator.run()) {
                continue;
            }

            if (!executor.run(translator.getContinuation())) {
                continue;
            }

            int n = 0;
            for (Object obj : executor.getDataStack()) {
                System.out.printf("[%d]: %s%n", n++, obj.toString());
            }
        }
    }
}

