package service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AppProperties {
    private static Path driveFilenamePath;
    private static Path localRoot;
    private static Path propertyFile = Path.of("download.config");

    public static void load(String[] args) {
        if (args.length == 1) {
            propertyFile = Path.of(args[0]);
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(propertyFile.toFile()));
        } catch (IOException e) {
            System.out.println(propertyFile.toAbsolutePath() + " not found, running with default values");
            properties.put("drive.files", "drive.files.json");
            properties.put("local.root", "temp");
        }

        System.out.println("Config: " + properties);

        driveFilenamePath = Path.of(properties.getProperty("drive.files"));
        localRoot = Path.of(properties.getProperty("local.root"));
    }

    public static Path getDriveFilenamePath() {
        return driveFilenamePath;
    }

    public static Path getLocalRoot() {
        return localRoot;
    }
}
