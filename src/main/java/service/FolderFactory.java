package service;

import model.DriveFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FolderFactory {

    public static void createFolder(Path path, List<DriveFolder> folders) {
        createDirectory(path);
        folders.forEach(f -> createFolder(Path.of(path.toString(), f.getName()), f.getChildren()));
    }

    private static void createDirectory(Path path) {
        try {
            System.out.println("mkdir " + path.getFileName());
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
