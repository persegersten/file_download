package service;

import java.nio.file.Path;

public class AppProperties {
    private static Path driveFilesPath = Path.of("drive.files.json");

    public static void load(String[] args) {
        if (args.length == 1) {
            driveFilesPath = Path.of(args[0]);
        }
        System.out.println("drive.files.json="+driveFilesPath);
    }

    public static Path getDriveFilesPath() {
        return driveFilesPath;
    }
}
