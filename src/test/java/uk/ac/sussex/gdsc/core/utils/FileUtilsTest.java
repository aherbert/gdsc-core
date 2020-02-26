package uk.ac.sussex.gdsc.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Permission;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class FileUtilsTest {
  @Test
  public void canCreateParent() throws IOException {
    final Path tmpDir = Files.createTempDirectory("FileUtilTest");
    final Path filename = Paths.get(tmpDir.toString(), "parentDir", "filename");
    final Path parent = filename.getParent();
    Assertions.assertFalse(Files.exists(parent), "Child's parent should not exist");
    FileUtils.createParent(filename);
    Assertions.assertTrue(Files.exists(parent), "Child's parent should have been created");
    Assertions.assertFalse(Files.exists(filename), "Child should not exist");
    // Allow repeat
    FileUtils.createParent(filename);
    Assertions.assertTrue(Files.exists(parent), "Child's parent should have been created");
    Assertions.assertFalse(Files.exists(filename), "Child should not exist");
  }

  @Test
  public void canCreateParentIgnoresNullParent() throws IOException {
    final Path filename = Paths.get("filename");
    final Path parent = filename.getParent();
    Assertions.assertNull(parent, "Parent should be null");
    FileUtils.createParent(filename);
  }

  @Test
  public void canCreateParentWrapsSecurityException() throws IOException {
    // Check there is no security manager
    Assumptions.assumeTrue(System.getSecurityManager() == null, "Require no security manager");

    try {
      // First create the temp directory
      final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ);
      final Path tmpDir =
          Files.createTempDirectory("FileUtilTest", PosixFilePermissions.asFileAttribute(perms));

      // Set a dummy manager that will deny access
      System.setSecurityManager(new SecurityManager() {
        @Override
        public void checkWrite(String file) {
          // Throw an exception when requesting to write a file
          throw new SecurityException("No file write");
        }

        @Override
        public void checkPermission(Permission perm) {
          // Allow anything. This allows the security manager to be reset to null.
        }
      });

      // Now try and create the parent of a file. It should be denied by the security manager.
      final Path filename = Paths.get(tmpDir.toString(), "parentDir", "filename");
      final Path parent = filename.getParent();
      Assertions.assertFalse(Files.exists(parent), "Child's parent should not exist");
      final IOException ex =
          Assertions.assertThrows(IOException.class, () -> FileUtils.createParent(filename));
      Assertions.assertTrue(ex.getCause() instanceof SecurityException);
    } finally {
      // Reset to no security manager
      System.setSecurityManager(null);
    }

    Assertions.assertNull(System.getSecurityManager(), "Failed to reset security manager");
  }

  @Test
  public void canRemoveExtension() {
    canRemoveExtension("", null);
    canRemoveExtension("", "");
    canRemoveExtension("/", "/");
    canRemoveExtension("./", "./");
    canRemoveExtension("./", "./.");
    canRemoveExtension("./", "./.e");
    canRemoveExtension("./", "./.ex");
    canRemoveExtension("./", "./.ext");
    canRemoveExtension("./file", "./file");
    canRemoveExtension("./file", "./file.");
    canRemoveExtension("./file", "./file.e");
    canRemoveExtension("./file", "./file.ex");
    canRemoveExtension("./file", "./file.ext");
    canRemoveExtension("./file.ext", "./file.ext.");
    canRemoveExtension("./file.ext", "./file.ext.t");
    canRemoveExtension("./file.ext", "./file.ext.tx");
    canRemoveExtension("./file.ext", "./file.ext.txt");
  }

  private static void canRemoveExtension(String expected, String filename) {
    final String expected2 = sanitise(expected);
    final String filename2 = sanitise(filename);
    Assertions.assertEquals(expected2, FileUtils.removeExtension(filename2),
        () -> "Filename = " + filename2);
  }

  @Test
  public void canGetExtensionAndLength() {
    canGetExtensionAndLength("", null);
    canGetExtensionAndLength("", "");
    canGetExtensionAndLength("", "/");
    canGetExtensionAndLength("", "./");
    canGetExtensionAndLength("", "./.");
    canGetExtensionAndLength("e", "./.e");
    canGetExtensionAndLength("ex", "./.ex");
    canGetExtensionAndLength("ext", "./.ext");
    canGetExtensionAndLength("", "./file");
    canGetExtensionAndLength("", "./file.");
    canGetExtensionAndLength("e", "./file.e");
    canGetExtensionAndLength("ex", "./file.ex");
    canGetExtensionAndLength("ext", "./file.ext");
    canGetExtensionAndLength("", "./file.ext.");
    canGetExtensionAndLength("t", "./file.ext.t");
    canGetExtensionAndLength("tx", "./file.ext.tx");
    canGetExtensionAndLength("txt", "./file.ext.txt");
  }

  private static void canGetExtensionAndLength(String expected, String filename) {
    final String expected2 = sanitise(expected);
    final String filename2 = sanitise(filename);
    Assertions.assertEquals(expected2, FileUtils.getExtension(filename2),
        () -> "Bad extension : Filename = " + filename2);
    Assertions.assertEquals(expected2.length(), FileUtils.getExtensionLength(filename2),
        () -> "Bad extension length : Filename = " + filename2);
  }

  @Test
  public void canReplaceExtension() {
    // Basic use case
    canReplaceExtension("file.ext", "file", "ext");
    canReplaceExtension("file.ext", "file.txt", "ext");
    canReplaceExtension("file.ext", "file.", ".ext");
    canReplaceExtension("file.ext", "file.txt", ".ext");

    // Test it works as documented (i.e. an extension of '.' is a valid extsion)
    canReplaceExtension("file.", "file.", ".");
    canReplaceExtension("file.", "file.txt", ".");

    canReplaceExtension("", null, null);
    canReplaceExtension("", "", "");
    canReplaceExtension("/", "/", "");
    canReplaceExtension("/.ext", "/", "ext");
    canReplaceExtension("/.ext", "/.", "ext");
    canReplaceExtension("/file.ext", "/file.", "ext");
    canReplaceExtension("/file.ext", "/file.txt", "ext");
    canReplaceExtension("/file.extra.ext", "/file.extra.", "ext");
    canReplaceExtension("/file.extra.ext", "/file.extra.txt", "ext");
    canReplaceExtension("dir.extra/.ext", "dir.extra/", "ext");
    canReplaceExtension("dir.extra/file.ext", "dir.extra/file", "ext");
    canReplaceExtension("dir.extra/file.ext", "dir.extra/file.txt", "ext");
  }

  private static void canReplaceExtension(String expected, String filename, String extension) {
    final String expected2 = sanitise(expected);
    final String filename2 = sanitise(filename);
    Assertions.assertEquals(expected2, FileUtils.replaceExtension(filename2, extension),
        () -> "Filename = " + filename2 + ", extension = " + extension);
  }

  @Test
  public void canAddExtensionIfAbsentExtension() {
    canAddExtensionIfAbsentExtension("", null, null);
    canAddExtensionIfAbsentExtension("", "", "");
    canAddExtensionIfAbsentExtension("/", "/", "");
    canAddExtensionIfAbsentExtension("/.ext", "/", "ext");
    canAddExtensionIfAbsentExtension("/.ext", "/.", "ext");
    canAddExtensionIfAbsentExtension("/file.ext", "/file.", "ext");
    canAddExtensionIfAbsentExtension("/file.txt", "/file.txt", "ext");
    canAddExtensionIfAbsentExtension("/file.extra.ext", "/file.extra.", "ext");
    canAddExtensionIfAbsentExtension("/file.extra.txt", "/file.extra.txt", "ext");
    canAddExtensionIfAbsentExtension("dir.extra/.ext", "dir.extra/", "ext");
    canAddExtensionIfAbsentExtension("dir.extra/file.ext", "dir.extra/file", "ext");
    canAddExtensionIfAbsentExtension("dir.extra/file.txt", "dir.extra/file.txt", "ext");
  }

  private static void canAddExtensionIfAbsentExtension(String expected, String filename,
      String extension) {
    final String expected2 = sanitise(expected);
    final String filename2 = sanitise(filename);
    Assertions.assertEquals(expected2, FileUtils.addExtensionIfAbsent(filename2, extension),
        () -> "Filename = " + filename2 + ", extension = " + extension);
  }

  @Test
  public void canGetName() {
    canGetName("", null);
    canGetName("", "");
    canGetName("", "/");
    canGetName("", "//");
    canGetName("a", "a//");
    canGetName("b", "a/b/");
    canGetName("c", "a/b/c");
    canGetName("c", "/b/c");
    canGetName("c", "//c");
  }

  private static void canGetName(String expected, String path) {
    final String expected2 = sanitise(expected);
    final String path2 = sanitise(path);
    Assertions.assertEquals(expected2, FileUtils.getName(path2), () -> "Path = " + path2);
  }

  /**
   * Sanitise the path to use the correct {@link File#separatorChar}.
   *
   * @param path the path
   * @return the sanitised path
   */
  private static String sanitise(String path) {
    return (path != null) ? path.replace('/', File.separatorChar) : null;
  }
}
