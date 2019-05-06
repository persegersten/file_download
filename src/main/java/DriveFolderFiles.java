import com.fasterxml.jackson.databind.ObjectMapper;
import model.DriveFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class DriveFolderFiles {
    public static void write(Path path, List<DriveFolder> folders) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(path.toFile(), folders);
    }

    public static List<DriveFolder> read(Path path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return (List<DriveFolder>) objectMapper.readValue(path.toFile(), List.class);
    }
}
