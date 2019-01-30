package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Permission;
import java.util.EnumSet;
import java.util.Set;

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
}
