import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import model.DriveFolder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DriveDownloader {
    //private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final String APPLICATION_NAME = "Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveDownloader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        if (test()) {
            return;
        }

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        List<DriveFolder> folders = fetchFolders(service);
        DriveFolderFiles.write(Path.of("drive.folders.json"), folders);

        System.out.println("****\nFetch folder info from Drive\nCreate drives locally\n****");

        createFolder(Path.of("temp"), folders);
    }

    private static boolean test() throws IOException {
        DriveFolder df = new DriveFolder("name", "id", Arrays.asList(
                new DriveFolder("name2", "id2", Collections.emptyList()),
                new DriveFolder("name3", "id3", Arrays.asList(
                        new DriveFolder("name4", "id4", Collections.emptyList())
                ))
        ));
        Path TEST_FILE = Path.of("test.json");
        Path TEST_OUTPUT_DIR = Path.of("gurka");

        Files.deleteIfExists(TEST_FILE);
        Files.deleteIfExists(TEST_OUTPUT_DIR);

        DriveFolderFiles.write(TEST_FILE, Collections.singletonList(df));
        Collection<DriveFolder> folders = DriveFolderFiles.read(TEST_FILE);

        createFolder(TEST_OUTPUT_DIR, folders);
        return true;
    }

    private static boolean test2() throws IOException {
        System.out.println("Test");
        Path path = Path.of("gurka");
        Files.createDirectory(path);
        Path subDir = Path.of(path.toString(), "banan");
        Files.createDirectory(subDir);
        Path subDir2 = Path.of(subDir.toString(), "sallad");
        Files.createDirectory(subDir2);
        return true;
    }

    private static void createFolder(Path path, Collection<DriveFolder> folders) {
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

    private static List<DriveFolder> fetchFolders(Drive service) throws IOException {
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