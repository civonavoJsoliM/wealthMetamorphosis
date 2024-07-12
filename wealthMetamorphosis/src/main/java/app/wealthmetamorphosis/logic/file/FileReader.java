package app.wealthmetamorphosis.logic.file;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileReader {
    public List<String> readFromFile(String path) {
        try {
            return Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> readFromFile(URI uri) {
        try {
            return Files.readAllLines(Path.of(uri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
