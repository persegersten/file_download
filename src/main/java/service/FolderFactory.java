package service;

import model.DriveFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FolderFactory {
    private static Logger logger = LoggerFactory.getLogger(FolderFactory.class);

    public static void createFolder(Path path, List<DriveFolder> folders) {
        createDirectory(path);
        folders.forEach(f -> createFolder(Path.of(path.toString(), f.getName()), f.getChildren()));
    }

    private static void createDirectory(Path path) {
        if (Files.exists(path)) {
            logger.info("WARN already exists: {}", path);
            return;
        }
        try {
            logger.info("mkdir " + path);
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
