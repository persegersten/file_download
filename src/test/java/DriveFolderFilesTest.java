import model.DriveFolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class DriveFolderFilesTest {

    Path TEST_FILE = Path.of("target", "test.json");
    Path TEST_OUTPUT_DIR = Path.of("gurka");

    @Before
    public void setup() throws IOException {
        Files.deleteIfExists(TEST_FILE);
        Files.deleteIfExists(TEST_OUTPUT_DIR);
    }

    @Test
    public void writeToFile() throws IOException {
        DriveFolder df = new DriveFolder("name", "id", null);
        DriveFolderFiles.write(TEST_FILE, Collections.singletonList(df));

        Collection<DriveFolder> folders = DriveFolderFiles.read(TEST_FILE);
    }

    @Test
    public void writeToFile2() throws IOException {
        DriveFolder df = new DriveFolder("name", "id", Arrays.asList(
                new DriveFolder("name2", "id2", Collections.emptyList()),
                new DriveFolder("name3", "id3", Arrays.asList(
                        new DriveFolder("name4", "id4", Collections.emptyList())
                ))
        ));

        Files.deleteIfExists(TEST_FILE);
        Files.deleteIfExists(TEST_OUTPUT_DIR);

        DriveFolderFiles.write(TEST_FILE, Collections.singletonList(df));
        List<DriveFolder> folders = DriveFolderFiles.read(TEST_FILE);

        Assert.assertNotNull(folders);
        Assert.assertEquals(1, folders.size());
        Assert.assertEquals(2, folders.get(0).getChildren().size());
        Assert.assertEquals(2, folders.get(0).getChildren().get(0).getChildren().size());
        Assert.assertEquals(2, folders.get(0).getChildren().get(1).getChildren().size());
    }
}
