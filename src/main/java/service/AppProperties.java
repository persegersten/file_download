package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppProperties {
    private static Logger logger = LoggerFactory.getLogger(AppProperties.class);
    private static Path driveFilenamePath;
    private static Path localRoot;
    private static Path propertyFile = Path.of("download.config");
    private static Set<String> extensions;

    public static void load(String[] args) {
        if (args.length == 1) {
            propertyFile = Path.of(args[0]);
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(propertyFile.toFile()));
        } catch (IOException e) {
            logger.info(propertyFile.toAbsolutePath() + " not found, running with default values");
            properties.put("drive.files", "drive.files.json");
            properties.put("local.root", "temp");
            properties.put("extensions", "pdf,jpg,png");
        }

        logger.info("Config: " + properties);

        driveFilenamePath = Path.of(properties.getProperty("drive.files"));
        localRoot = Path.of(properties.getProperty("local.root"));
        extensions = Collections.unmodifiableSet(Stream.of(properties.get("extensions").toString().split(","))
                .map(String::trim)
                .collect(Collectors.toSet()));
    }

    public static Path getDriveFilenamePath() {
        return driveFilenamePath;
    }

    public static Path getLocalRoot() {
        return localRoot;
    }

    public static Set<String> getExtensions() {
        return extensions;
    }
}
