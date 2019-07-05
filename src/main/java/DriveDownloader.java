import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import model.DriveFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DriveDownloader {
    private static Logger logger = LoggerFactory.getLogger(DriveDownloader.class);

    public static void main(String... args) throws IOException, GeneralSecurityException {
        AppProperties.load(args);
        Drive service = DriveConnection.getService();

        Path driveFilesname = AppProperties.getDriveFilenamePath();
        if (Files.exists(driveFilesname)) {
            logger.info("Remote folder info files already exists: {}", driveFilesname);
        } else {
            fetchRemoteFolderInfo(service, driveFilesname, AppProperties.getRemoteId());
        }

        Path localRoot = AppProperties.getLocalRoot();
        String remoteId = AppProperties.getRemoteId();
        List<DriveFolder> folders = DriveFolderFiles.read(driveFilesname);
        if (Files.exists(localRoot)) {
            logger.info("Local folders already exists: {}", localRoot);
        } else {
            createLocalFolders(localRoot, folders);
        }
        logger.info("Start downloading files...");
        downloadFiles(service, localRoot, remoteId, folders);
    }

    private static void downloadFiles(Drive service, Path localRoot, String remoteId, List<DriveFolder> folders) {
        try {
            FilesDownloader.downloadFolder(service, localRoot, remoteId, folders);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download files to local folder", e);
        }
    }

    private static void createLocalFolders(Path localRoot, List<DriveFolder> folders) throws IOException {
        logger.info("Create folder structure");
        FolderFactory.createFolder(localRoot, folders);
    }

    private static void fetchRemoteFolderInfo(Drive service, Path driveFilesPath, String remoteId) throws IOException {
        logger.info("Create: {} Fetch info from drive", driveFilesPath);
        List<DriveFolder> folders = fetchFolders(service, remoteId);
        DriveFolderFiles.write(driveFilesPath, folders);
        logger.info("Fetched done");
    }

    private static List<DriveFolder> fetchFolders(Drive service, String remoteId) {
        return fetchFilesInFolder(service, remoteId);
    }

    private static List<DriveFolder> fetchFilesInFolder(Drive service, String parentID) {
        try {
            int counter = 10;
            FileList result;
            while (true) {
                try {
                    result = service.files().list()
                            .setQ("'" + parentID + "' in parents and " +
                                    "mimeType = 'application/vnd.google-apps.folder'")
                            .setPageSize(100)
                            .setFields("nextPageToken, files(id, name)")
                            .execute();
                    break;
                } catch (SocketTimeoutException e) {
                    counter--;
                    if (counter == 0) {
                        throw new IllegalStateException(e);
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException("Failed too many times", e);
                    }
                }
            }

            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                return Collections.emptyList();
            } else {
                return files.stream().map(f -> createDriveFolder(service, f)).collect(Collectors.toList());
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DriveFolder createDriveFolder(Drive service, File file) {
        logger.info("{} {}", file.getName(), file.getId());
        return new DriveFolder(file.getName(), file.getId(), fetchFilesInFolder(service, file.getId()));
    }

}