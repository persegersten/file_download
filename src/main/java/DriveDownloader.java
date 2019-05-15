import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import model.DriveFolder;
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

    public static void main(String... args) throws IOException, GeneralSecurityException {
        AppProperties.load(args);
        Drive service = DriveConnection.getService();

        Path driveFilesname = AppProperties.getDriveFilenamePath();
        if (Files.exists(driveFilesname)) {
            System.out.println("Remote folder info files already exists: " + driveFilesname);
        } else {
            fetchRemoteFolderInfo(service, driveFilesname);
        }

        Path localRoot = AppProperties.getLocalRoot();
        List<DriveFolder> folders = DriveFolderFiles.read(driveFilesname);
        if (Files.exists(localRoot)) {
            System.out.println("Local folders already exists: " + localRoot);
        } else {
            createLocalFolders(localRoot, folders);
        }

        downloadFiles(service, localRoot, folders);
    }

    private static void downloadFiles(Drive service, Path localRoot, List<DriveFolder> folders) {
        FilesDownloader.downloadFolder(service, localRoot, folders);
    }

    private static void createLocalFolders(Path localRoot, List<DriveFolder> folders) throws IOException {
        System.out.println("Create folder structure");
        FolderFactory.createFolder(localRoot, folders);
    }

    private static void fetchRemoteFolderInfo(Drive service, Path driveFilesPath) throws IOException {
        System.out.println("Create: " + driveFilesPath + " Fetch info from drive");
        List<DriveFolder> folders = fetchFolders(service);
        DriveFolderFiles.write(driveFilesPath, folders);
        System.out.println("Fetched done");
    }

    private static List<DriveFolder> fetchFolders(Drive service) {
        return fetchFilesInFolder(service, "root");
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
        System.out.printf("%s (%s)\n", file.getName(), file.getId());
        return new DriveFolder(file.getName(), file.getId(), fetchFilesInFolder(service, file.getId()));
    }

}