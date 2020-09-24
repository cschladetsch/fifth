import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class App {
    private static ILogger log;
    private static List<CodeSource> logFiles = new ArrayList<>();

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

        log.debug("File: " + fileName);
        CodeSource processor = new CodeSource(log, Paths.get(fileName));
        processor.run();
        Lexer lexer = new Lexer(log, processor.getCodeText());
        if (stageFailed(lexer)) {
            log.warn(lexer);
            processor.close();
            return -1;
        }

        Parser parser = new Parser(lexer);
        if (stageFailed(parser)) {
            log.warn(lexer);
            log.warn(parser);
            processor.close();
            return -1;
        }

        Translator translator = new Translator(parser);
        if (stageFailed(translator)) {
            log.warn(lexer);
            log.warn(parser);
            log.warn(translator);
            processor.close();
            return -1;
        }

        Executor executor = new Executor(log);
        executor.contextPush(translator.getContinuation());
        if (stageFailed(executor)) {
            log.warn(lexer);
            log.warn(parser);
            log.warn(translator);
            log.warn(executor);
            processor.close();
            return -1;
        }

        log.verbose(10, lexer);
        log.verbose(10, parser);
        log.verbose(10, translator);
        log.verbose(10, executor);

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
//    public static Optional<List<String>> fileCodeContents(String fileName) {
//        Path path = Paths.get(fileName);
//        if (!Files.exists(path)) {
//            return Optional.empty();
//        }
//
//        return = new MarkdownProcessor(log, path);
//    }
}

