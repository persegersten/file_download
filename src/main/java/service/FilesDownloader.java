package service;

import com.google.api.services.drive.Drive;
import model.DriveFolder;

import java.nio.file.Path;
import java.util.List;

public class FilesDownloader {
    public static void downloadFolder(Drive service, Path localRoot, List<DriveFolder> folders) {
        downLoadFolderInternal(service, localRoot, "root", folders);
    }

    private static void downLoadFolderInternal(Drive service, Path localFolder, String remoteID, List<DriveFolder> children) {
        for (DriveFolder child  : children) {
            downLoadFolderInternal(service,
                    Path.of(localFolder.toString(), child.getName()),
                    child.getId(),
                    child.getChildren());
        }
        // TODO ladda ner remoteID till localFolder
    }

}
