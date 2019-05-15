package service;

import model.DriveFolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FolderFactoryTest {

    Path TEST_OUTPUT_DIR = Path.of("folderTest");

    @Before
    public void before() throws IOException {
        Files.deleteIfExists(Path.of(TEST_OUTPUT_DIR.toString(), "fd1"));
        Files.deleteIfExists(Path.of(TEST_OUTPUT_DIR.toString(), "fd2", "fd3"));
        Files.deleteIfExists(Path.of(TEST_OUTPUT_DIR.toString(), "fd2"));
        Files.deleteIfExists(TEST_OUTPUT_DIR);
    }

    @Test
    public void test() {
        Assert.assertFalse(Files.exists(TEST_OUTPUT_DIR));

        DriveFolder fd1 = new DriveFolder("fd1", "id1", Collections.emptyList());
        DriveFolder fd3 = new DriveFolder("fd3", "id3", Collections.emptyList());
        DriveFolder fd2 = new DriveFolder("fd2", "id2", Collections.singletonList(fd3));
        List<DriveFolder> folders = Arrays.asList(fd1, fd2);
        FolderFactory.createFolder(TEST_OUTPUT_DIR, folders);

        Assert.assertTrue(Files.exists(TEST_OUTPUT_DIR));
        Assert.assertTrue(Files.exists(Path.of(TEST_OUTPUT_DIR.toString(), "fd1")));
        Assert.assertTrue(Files.exists(Path.of(TEST_OUTPUT_DIR.toString(), "fd2")));
        Assert.assertTrue(Files.exists(Path.of(TEST_OUTPUT_DIR.toString(), "fd2", "fd3")));
    }

}