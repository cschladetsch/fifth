import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class FileUtil {
    public static Optional<FileWriter> newWriter(String path) {
        try {
            return Optional.of(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Optional<List<String>> contents(String fileName) {
        try {
            return Optional.of(Files.readAllLines(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static void println(FileWriter fileWriter, String s) {
        try {
            fileWriter.write(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(FileWriter fileWriter) {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
