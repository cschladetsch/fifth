import java.util.List;

public class PiExecutionContext extends ProcessBase {
    private Lexer lexer;
    private List<String> piCode;

    public PiExecutionContext(ILogger log) {
        super(log);
    }

    public PiExecutionContext(ILogger log, List<String> piCode) {
        super(log);
        lexer = new Lexer(log, piCode);
    }

    public boolean run(List<String> piCode) {
        reset();
        this.piCode = piCode;
        return run();
    }

    @Override
    boolean run() {
        if (piCode.isEmpty()) {
            return true;
        }

        boolean showProcess = false;

        if (!lexer.run(piCode)) {
            if (showProcess) {
                log.error(lexer);
            }
            return false;
        }

        Parser parser = new Parser(lexer);
        if (!parser.run()) {
            if (showProcess) {
                log.error(parser);
            }
            return false;
        }

        Translator translator = new Translator(parser);
        if (!translator.run()) {
            if (showProcess) {
                log.error(translator);
            }
            return false;
        }

        Executor executor = new Executor(log, translator.getContinuation());
        if (!executor.run(translator.getContinuation())) {
            if (showProcess) {
                log.error(executor);
            }
            return false;
        }
        return true;
    }

    void reset() {
        lexer.reset();
    }
}
