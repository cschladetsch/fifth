import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class App {
    private ILogger logger;

    public static void main(String[] argv) {
        try {
            System.exit(new App().run(argv));
        } catch (Exception e) {
            System.err.println(e);
            System.exit(-1);
        }
    }

    private int run(String[] argv) {
        logger = new Logger();
        logger.info("Fifth-lang Repl");

        String inputFilename = argv[0];
        Optional<List<String>> lines = fileContents(inputFilename);
        if (!lines.isPresent()) {
            logger.error("Failed to read " + inputFilename);
            return -1;
        }

        Lexer lexer = new Lexer(logger, lines.get());
        if (!lexer.run()) {
            logger.debug(lexer.toString());
            logger.error("Failed to lex.");
            return -1;
        }

        logger.info(lexer.toString());

        return 0;
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
