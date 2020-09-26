import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class FileUtil {

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex != 0) {
            return fileName.substring(dotIndex + 1);
        }

        return "";
    }

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
            if (fileWriter != null) {
                fileWriter.write(s + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(FileWriter fileWriter) {
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
