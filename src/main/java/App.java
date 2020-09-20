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

    private int run(String fileName) {
        Optional<List<String>> lines = fileContents(fileName);
        if (!lines.isPresent()) {
            logger.error("Failed to read " + fileName);
            return -1;
        }

        Lexer lexer = new Lexer(logger, lines.get());
        if (!lexer.run() || lexer.hasFailed()) {
            logger.debug(lexer.toString());
            logger.error("Failed to lex.");
            return -1;
        }

        Parser parser = new Parser(lexer);
        if (!parser.run() || parser.hasFailed()) {
            logger.debug(parser.toString());
            logger.error("Failed to parse.");
            return -1;
        }

        Translator translator = new Translator(parser);
        if (!translator.run() || translator.hasFailed()) {
            logger.debug(translator.toString());
            logger.error("Failed to translate.");
            return -1;
        }

        Executor executor = new Executor(logger);
        executor.run(translator.getContinuation());
        if (executor.hasFailed()) {
            logger.debug(executor.toString());
            logger.error("Failed to execute.");
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

        if (argv.length > 0) {
            return run(argv[0]);
        }

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
                System.out.println(String.format("[%n]: %s", n++, obj.toString()));
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
