import com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class App {
    private ILogger logger;

    public static void main(String[] argv) {
        try {
            System.exit(new App().run(argv));
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.exit(-1);
        }
    }

    private boolean runStage(String fileName, ProcessBase process) {
        if (!process.run() || process.hasFailed()) {
            logger.debug(process.toString());
            logger.debug(process.getClass().getSimpleName() + " Failed");
            return false;
        }

        return true;
    }

    private int run(String fileName) {
        Optional<List<String>> lines = fileContents(fileName);
        logger.debug("File: " + fileName);
        if (!lines.isPresent()) {
            logger.error("Failed to read " + fileName);
            return -1;
        }

        Lexer lexer = new Lexer(logger, lines.get());
        if (!runStage(fileName, lexer)) {
            return -1;
        }

        Parser parser = new Parser(lexer);
        if (!runStage(fileName, parser)) {
            return -1;
        }

        Translator translator = new Translator(parser);
        if (!runStage(fileName, translator)) {
            return -1;
        }

        Executor executor = new Executor(logger);
        executor.contextPush(translator.getContinuation());
        if (!runStage(fileName, executor)) {
            return -1;
        }

//        logger.info(lexer.toString());
//        logger.info(parser.toString());
//        logger.info(translator.toString());
//        logger.info(executor.toString());

        return 0;
    }

    private int run(String[] argv) {
        logger = new Logger();
        logger.info("Fifth-lang Repl");

        for (String fileName : argv) {
            if (run(fileName) != 0) {
                return -1;
            }
        }

        //Repl();

        return 0;
    }

    private void Repl()
    {
        Executor executor = new Executor(logger);
        while (true) {
            System.out.print("λ ");
            String text = System.console().readLine();

            Lexer lexer = new Lexer(logger, text);
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

    private Optional<List<String>> fileContents(String fileName) {
        try {
            return Optional.of(Files.readAllLines(Paths.get(fileName)));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return Optional.empty();
    }
}
