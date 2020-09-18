import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class App {
    private ILogger logger;

    public static void main(String[] argv) {
        new App().run(argv);
    }

    private int run(String[] argv) {
        logger = new Logger();

        logger.info("Fifth-lang Repl");

        Lexer lexer = new Lexer(logger, fileContents(argv[0]));
        if (!lexer.run()) {
            logger.info(String.format("Failed to lex @%s", lexer.getLocation()));
        }

        return 0;
    }

    private List<String> fileContents(String fileName) {
        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }
}
