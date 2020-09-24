import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class App {
    private static ILogger log;

    public static void main(String[] argv) {
        log = new Logger();
        log.info("Fifth-lang Repl");
        log.setVerbosity(0);

        try {
            System.exit(new App().run(argv));
        } catch (Exception e) {
            for (StackTraceElement frame : e.getStackTrace()) {
                log.error(frame.toString());
            }
            System.exit(-1);
        }
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

        Optional<List<String>> lines = fileCodeContents(fileName);
        log.debug("File: " + fileName);
        if (!lines.isPresent()) {
            log.error("Failed to read " + fileName);
            return -1;
        }

        Lexer lexer = new Lexer(log, lines.get());
        if (stageFailed(lexer)) {
            log.warn(lexer);
            return -1;
        }

        Parser parser = new Parser(lexer);
        if (stageFailed(parser)) {
            log.warn(lexer);
            log.warn(parser);
            return -1;
        }

        Translator translator = new Translator(parser);
        if (stageFailed(translator)) {
            log.warn(lexer);
            log.warn(parser);
            log.warn(translator);
            return -1;
        }

        Executor executor = new Executor(log);
        executor.contextPush(translator.getContinuation());
        if (stageFailed(executor)) {
            log.warn(lexer);
            log.warn(parser);
            log.warn(translator);
            log.warn(executor);
            return -1;
        }

        log.verbose(10, lexer);
        log.verbose(10, parser);
        log.verbose(10, translator);
        log.verbose(10, executor);

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
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        return "";
    }

    public static Optional<List<String>> fileContents(String fileName) {
        try {
            return Optional.of(Files.readAllLines(Paths.get(fileName)));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<List<String>> fileCodeContents(String fileName) {
        try {
            Path path = Paths.get(fileName);
            if (!Files.exists(path)) {
                return Optional.empty();
            }

            List<String> code = null;
            if (getFileExtension(path.getFileName().toString()).equals("md")) {
                StripMarkdown stripped = new StripMarkdown(log, path);
                if (stripped.run()) {
                    code = stripped.getCodeText();
                }
            }
            else {
                code = Files.readAllLines(path);
            }

            return Optional.of(code);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return Optional.empty();
    }
}
