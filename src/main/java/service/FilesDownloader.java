package service;

import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import model.DriveFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FilesDownloader {
    private static Logger logger = LoggerFactory.getLogger(FilesDownloader.class);
    private static final Path TEMP_FILE = Path.of("temporary.download.file");

    public static void downloadFolder(Drive service, Path localRoot, String remoteRoot, List<DriveFolder> folders) throws IOException {
        downLoadFolderInternal(service, localRoot, remoteRoot, folders);
    }

    private static void downLoadFolderInternal(Drive service, Path localFolder, String remoteID, List<DriveFolder> children) throws IOException {
        if (!Files.exists(localFolder)) {
            throw new IllegalStateException("Local target folder does not exist:" + localFolder);
        }

        downloadFiles(service, localFolder, remoteID);

        for (DriveFolder child : children) {
            downLoadFolderInternal(service,
                    Path.of(localFolder.toString(), child.getName()),
                    child.getId(),
                    child.getChildren());
        }
    }

    private static void downloadFiles(Drive service, Path localFolder, String remoteID) throws IOException {
        List<File> files = retrieveAllFiles(service, remoteID);

        int count = 0;
        int total = 0;
        for (File file : files) {
            total++;
            Path locaFilePath = null;
            try {
                locaFilePath = Path.of(localFolder.toString(), file.getName());
            } catch (InvalidPathException e) {
                System.out.println("Invalid path: " + file.getName());
                continue;
            }
            if (Files.isDirectory(locaFilePath)) {
                logger.info("Path is a directory: {}", locaFilePath);
            } else if (Files.exists(locaFilePath)) {
                logger.info("File exists on local path: {}", locaFilePath);
            } else {
                download(service, locaFilePath, file);
                count++;
            }
        }
        logger.info("Downloaded {} of {} files", count, total);
    }

    private static List<File> retrieveAllFiles(Drive service, String parentID) throws IOException {
        List<File> result = new ArrayList<File>();
        Drive.Files.List request = service.files().list();

        do {
            try {
                FileList files = request.setQ("'" + parentID + "' in parents")
                        .setFields("nextPageToken, files(id, name)")
                        .execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
                System.out.print(".");
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    private static void download(Drive service, Path locaFilePath, File file) throws IOException {
        String filename = file.getName();
        String ex = filename.substring(filename.lastIndexOf(".") + 1);

        if (AppProperties.getExtensions().contains(ex.toLowerCase())) {

            logger.info("Download: {} to {}", file.getName(), locaFilePath.toAbsolutePath());
            Drive.Files.Get get = service.files().get(file.getId());
            HttpResponse resp = get.executeMedia();

            // String webContentLink = Objects.requireNonNull(file.getWebContentLink(), "No web contennt link for file: " + file);
            // HttpResponse resp =
            //        service.getRequestFactory().buildGetRequest(new GenericUrl(webContentLink))
            //                .execute();
            try (InputStream instream = resp.getContent();
                 FileOutputStream output = new FileOutputStream(TEMP_FILE.toFile())) {
                int l;
                byte[] tmp = new byte[2048];
                while ((l = instream.read(tmp)) != -1) {
                    output.write(tmp, 0, l);
                }
            }

            Files.move(TEMP_FILE, locaFilePath);
        }
    }

}
