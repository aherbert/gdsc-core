/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Permission;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class FileUtilsTest {
  private static final String HEADER = "my header";

  @Test
  void canSaveStringData() throws IOException {
    final LocalList<String> expected = new LocalList<>();
    expected.add(HEADER);
    assertSaveData(HEADER, (d, filename) -> FileUtils.save(filename, d), expected);
  }

  @SeededTest
  void canSaveDoubleData(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    double[] data = new double[10];
    final LocalList<String> expected = new LocalList<>(data.length + 1);
    for (int i = 0; i < data.length; i++) {
      data[i] = rng.nextDouble();
      expected.add(String.valueOf(data[i]));
    }
    assertSaveData(data, (d, filename) -> FileUtils.save(d, filename), expected);
    expected.add(0, HEADER);
    assertSaveData(data, (d, filename) -> FileUtils.save(HEADER, d, filename), expected);
    expected.clear();
    data = null;
    assertSaveData(data, (d, filename) -> FileUtils.save(d, filename), expected);
    expected.add(HEADER);
    assertSaveData(data, (d, filename) -> FileUtils.save(HEADER, d, filename), expected);
  }

  @SeededTest
  void canSaveFloatData(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    float[] data = new float[10];
    final LocalList<String> expected = new LocalList<>(data.length + 1);
    for (int i = 0; i < data.length; i++) {
      data[i] = rng.nextFloat();
      expected.add(String.valueOf(data[i]));
    }
    assertSaveData(data, (d, filename) -> FileUtils.save(d, filename), expected);
    expected.add(0, HEADER);
    assertSaveData(data, (d, filename) -> FileUtils.save(HEADER, d, filename), expected);
    expected.clear();
    data = null;
    assertSaveData(data, (d, filename) -> FileUtils.save(d, filename), expected);
    expected.add(HEADER);
    assertSaveData(data, (d, filename) -> FileUtils.save(HEADER, d, filename), expected);
  }

  @SeededTest
  void canSaveIntData(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    int[] data = new int[10];
    final LocalList<String> expected = new LocalList<>(data.length + 1);
    for (int i = 0; i < data.length; i++) {
      data[i] = rng.nextInt();
      expected.add(String.valueOf(data[i]));
    }
    assertSaveData(data, (d, filename) -> FileUtils.save(d, filename), expected);
    expected.add(0, HEADER);
    assertSaveData(data, (d, filename) -> FileUtils.save(HEADER, d, filename), expected);
    expected.clear();
    data = null;
    assertSaveData(data, (d, filename) -> FileUtils.save(d, filename), expected);
    expected.add(HEADER);
    assertSaveData(data, (d, filename) -> FileUtils.save(HEADER, d, filename), expected);
  }

  private static <T> void assertSaveData(T data, BiPredicate<T, String> save, List<String> expected)
      throws IOException {
    final Path filename = Files.createTempFile("FileUtilsTest", ".dat");
    Assertions.assertTrue(save.test(data, filename.toString()));
    // Check
    final List<String> lines =
        org.apache.commons.io.FileUtils.readLines(filename.toFile(), StandardCharsets.ISO_8859_1);
    Assertions.assertEquals(expected, lines);
    Files.delete(filename);
  }

  @Test
  void canAddFileSeparator() {
    Assertions.assertEquals("tmp" + File.separatorChar, FileUtils.addFileSeparator("tmp"));
    Assertions.assertEquals("tmp/", FileUtils.addFileSeparator("tmp/"));
    Assertions.assertEquals("tmp\\", FileUtils.addFileSeparator("tmp\\"));
  }

  @Test
  void canCreateParent() throws IOException {
    final Path tmpDir = Files.createTempDirectory("FileUtilsTest");
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
    // Clean up
    org.apache.commons.io.FileUtils.deleteDirectory(tmpDir.toFile());
  }

  @Test
  void canCreateParentIgnoresNullParent() throws IOException {
    final Path filename = Paths.get("filename");
    final Path parent = filename.getParent();
    Assertions.assertNull(parent, "Parent should be null");
    FileUtils.createParent(filename);
  }

  @Test
  void canCreateParentWrapsSecurityException() throws IOException {
    // Check there is no security manager
    // Note: The SecurityManager is deprecated for removal from JDK 17
    String version = System.getProperty("java.specification.version");
    // Do not care about low version 1.8. From JDK 9 this should lead with the major version.
    int i = version.indexOf('.');
    if (i >= 0) {
      version = version.substring(0, i);
    }
    try {
      Assumptions.assumeTrue(Integer.parseInt(version) < 17 && System.getSecurityManager() == null,
          "Require no security manager and pre-JDK 17");
    } catch (NumberFormatException e) {
      Logger.getAnonymousLogger()
          .warning(() -> "Unknown version: " + System.getProperty("java.specification.version"));
      return;
    }

    Path tmpDir = null;
    try {
      // First create the temp directory
      final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ);
      tmpDir =
          Files.createTempDirectory("FileUtilsTest", PosixFilePermissions.asFileAttribute(perms));

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
      // Clean up
      if (tmpDir != null) {
        org.apache.commons.io.FileUtils.deleteDirectory(tmpDir.toFile());
      }
    }

    Assertions.assertNull(System.getSecurityManager(), "Failed to reset security manager");
  }

  @Test
  void canRemoveExtension() {
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
  void canGetExtensionAndLength() {
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
  void canReplaceExtension() {
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
  void canAddExtensionIfAbsentExtension() {
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
  void canGetName() {
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

  @Test
  void canSkip() throws IOException {
    final ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
    FileUtils.skip(in, 0);
    Assertions.assertEquals(10, in.available());
    FileUtils.skip(in, 5);
    Assertions.assertEquals(5, in.available());
    Assertions.assertThrows(EOFException.class, () -> FileUtils.skip(in, 6));
  }

  @Test
  void canFailToSaveStringData() throws IOException {
    assertFailToSaveData(HEADER, (d, filename) -> FileUtils.save(filename, d));
  }

  @Test
  void canFailToSaveDoubleData() throws IOException {
    final double[] data = {1, 2, 3};
    assertFailToSaveData(data, (d, filename) -> FileUtils.save(d, filename));
  }

  @Test
  void canFailToSaveFloatData() throws IOException {
    final float[] data = {1, 2, 3};
    assertFailToSaveData(data, (d, filename) -> FileUtils.save(d, filename));
  }

  @Test
  void canFailToSaveIntData() throws IOException {
    final int[] data = {1, 2, 3};
    assertFailToSaveData(data, (d, filename) -> FileUtils.save(d, filename));
  }

  private static <T> void assertFailToSaveData(T data, BiPredicate<T, String> save)
      throws IOException {
    final Logger logger = Logger.getLogger(FileUtils.class.getName());
    final Level level = logger.getLevel();
    logger.setLevel(Level.OFF);
    try {
      final Path filename = Files.createTempFile("FileUtilsTest", ".dat");
      final File file = filename.toFile();
      file.setWritable(false, false);
      Assertions.assertFalse(save.test(data, filename.toString()));
      Files.delete(filename);
    } finally {
      logger.setLevel(level);
    }
  }
}
