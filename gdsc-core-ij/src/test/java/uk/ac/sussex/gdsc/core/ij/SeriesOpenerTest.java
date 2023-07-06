/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
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

package uk.ac.sussex.gdsc.core.ij;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import java.awt.HeadlessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.sussex.gdsc.core.utils.LocalList;

@SuppressWarnings({"javadoc"})
class SeriesOpenerTest {

  @Test
  void testSeriesOpener() throws IOException {
    final Path tmpDir = Files.createTempDirectory("SeriesOpenerTest");
    final String path = tmpDir.toString();
    final byte[] data1 = {0, 1, 2, 3, 4, 5};
    final byte[] data2 = {9, 8, 7, 6, 5, 4};
    final byte[] data3 = {9, 8, 7, 6, 5, 4, 3, 2};
    IJ.save(new ImagePlus(null, new ByteProcessor(2, 3, data1)), path + "/image1.tif");
    IJ.save(new ImagePlus(null, new ByteProcessor(2, 3, data2)), path + "/image002.tif");
    IJ.save(new ImagePlus(null, new ByteProcessor(3, 2, data1)), path + "/image03.tif");
    IJ.save(new ImagePlus(null, new ByteProcessor(2, 4, data3)), path + "/image4.tif");

    // Create a directory and non-image file
    Files.createDirectory(Paths.get(path, "subDirectory"));
    Files.createFile(Paths.get(path, "notAnImage.list"));

    SeriesOpener series = SeriesOpener.create(path, false);
    Assertions.assertEquals(5, series.getNumberOfImages());
    Assertions.assertEquals(path, series.getPath());
    Assertions.assertArrayEquals(
        new String[] {"image1.tif", "image002.tif", "image03.tif", "image4.tif", "notAnImage.list"},
        series.getImageList());

    // Open the images
    ImagePlus imp = series.nextImage();
    Assertions.assertEquals("image1.tif", imp.getTitle());
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertArrayEquals(data1, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertEquals("image002.tif", imp.getTitle());
    Assertions.assertArrayEquals(data2, (byte[]) imp.getProcessor().getPixels());
    // Default is not for variable size
    try {
      imp = series.nextImage();
      Assertions.assertNull(imp);
    } catch (HeadlessException ignored) {
      // Mismatched dimensions so will process all remaining images.
      // Newer versions of IJ require a graphic environment to handle extra files
      // and the .list file throws an exception on a headless CI build
    }

    // Repeat but allow variable size
    series = SeriesOpener.create(path, false);
    series.setVariableSize(true);
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertEquals("image1.tif", imp.getTitle());
    Assertions.assertArrayEquals(data1, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertEquals("image002.tif", imp.getTitle());
    Assertions.assertArrayEquals(data2, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(3, imp.getWidth());
    Assertions.assertEquals(2, imp.getHeight());
    Assertions.assertEquals("image03.tif", imp.getTitle());
    Assertions.assertArrayEquals(data1, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(4, imp.getHeight());
    Assertions.assertEquals("image4.tif", imp.getTitle());
    Assertions.assertArrayEquals(data3, (byte[]) imp.getProcessor().getPixels());
    try {
      imp = series.nextImage();
      Assertions.assertNull(imp);
    } catch (HeadlessException ignored) {
      // Again ignore the .list file if headless
    }

    FileUtils.deleteDirectory(tmpDir.toFile());
  }

  @Test
  void testCreateSeriesOpenerWithNullAndShowDialog() {
    final SeriesOpener series = SeriesOpener.create(null, true);
    Assertions.assertEquals(0, series.getNumberOfImages());
    Assertions.assertNull(series.getPath());
    Assertions.assertArrayEquals(new String[0], series.getImageList());
  }

  @Test
  void testSeriesOpenerWithNull() {
    final SeriesOpener series = new SeriesOpener(null);
    Assertions.assertEquals(0, series.getNumberOfImages());
    Assertions.assertNull(series.getPath());
    Assertions.assertArrayEquals(new String[0], series.getImageList());
  }

  @Test
  void testSeriesOpenerWithBadDirectory() throws IOException {
    final Path tmpDir = Files.createTempDirectory("SeriesOpenerTest");
    final String path = tmpDir.toString() + "/noDirectory";
    final SeriesOpener series = new SeriesOpener(path);
    Assertions.assertEquals(0, series.getNumberOfImages());
    Assertions.assertEquals(path, series.getPath());
    Assertions.assertArrayEquals(new String[0], series.getImageList());
    FileUtils.deleteDirectory(tmpDir.toFile());
  }

  @Test
  void testSeriesOpenerWithEmptyDirectory() throws IOException {
    final Path tmpDir = Files.createTempDirectory("SeriesOpenerTest");
    final String path = tmpDir.toString();
    final SeriesOpener series = new SeriesOpener(path);
    Assertions.assertEquals(0, series.getNumberOfImages());
    Assertions.assertEquals(path, series.getPath());
    Assertions.assertArrayEquals(new String[0], series.getImageList());
    FileUtils.deleteDirectory(tmpDir.toFile());
  }

  @Test
  void testSeriesOpenerWithIgnoredFiles() throws IOException {
    final Path tmpDir = Files.createTempDirectory("SeriesOpenerTest");
    final String path = tmpDir.toString();
    Files.createFile(Paths.get(path, "file1.txt"));
    Files.createFile(Paths.get(path, "file2.txt"));
    // MicroManager metadata files
    Files.createFile(Paths.get(path, "DisplaySettings.json"));
    final SeriesOpener series = new SeriesOpener(path);
    Assertions.assertEquals(0, series.getNumberOfImages());
    Assertions.assertEquals(path, series.getPath());
    Assertions.assertArrayEquals(new String[0], series.getImageList());
    FileUtils.deleteDirectory(tmpDir.toFile());
  }

  @ParameterizedTest
  @MethodSource
  void testTrimFileList(String[] in, String[] expected) {
    Assertions.assertArrayEquals(expected, SeriesOpener.trimFileList(in));
  }

  static Stream<Arguments> testTrimFileList() {
    return Stream.of(
    // @formatter:off
      Arguments.of(null, null),
      Arguments.of(new String[0], null),
      Arguments.of(new String[] {"1.tif"}, new String[] {"1.tif"}),
      Arguments.of(new String[] {".", "..", "Thumbs.db", "1.tif", "2.txt", "3.json"},
                   new String[] {"1.tif"})
    // @formatter:on
    );
  }

  @Test
  void testFilterImageList() {
    final String[] list = {"image1.tif", "image2.tif", "image3.tif", "test4.tif", "file5.jpg"};
    final LocalList<String> errorMessageAction = new LocalList<>();
    final LocalList<String[]> listStringAction = new LocalList<>();
    final ObjIntConsumer<String[]> listAction = (filteredList, size) -> {
      listStringAction.add(Arrays.copyOf(filteredList, size));
    };

    // Null filter
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, null, false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(list, listStringAction.pop());
    // Empty filter
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, "", false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(list, listStringAction.pop());
    // Wild card filter
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, "*", false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(list, listStringAction.pop());

    // Filter by max images
    SeriesOpener.filterImageList(list.clone(), 3, 0, 0, null, false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(Arrays.copyOf(list, 3), listStringAction.pop());
    // Filter by start and increment
    SeriesOpener.filterImageList(list.clone(), 0, 2, 2, null, false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(new String[] {list[1], list[3]}, listStringAction.pop());
    // Start above list length resets to 1
    SeriesOpener.filterImageList(list.clone(), 0, 10, 2, null, false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(new String[] {list[0], list[2], list[4]}, listStringAction.pop());

    // Plain filter
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, ".tif", false, errorMessageAction::add,
        listAction);
    Assertions.assertArrayEquals(Arrays.copyOf(list, 4), listStringAction.pop());
    // Regex filter
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, "image[13].tif", true,
        errorMessageAction::add, listAction);
    Assertions.assertArrayEquals(new String[] {list[0], list[2]}, listStringAction.pop());

    // Filter everything - plain
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, "foobar", false, errorMessageAction::add,
        listAction);
    Assertions.assertEquals(0, listStringAction.size());
    Assertions.assertEquals(1, errorMessageAction.size());
    // Filter everything - regex
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, ".*png", true, errorMessageAction::add,
        listAction);
    Assertions.assertEquals(0, listStringAction.size());
    Assertions.assertEquals(2, errorMessageAction.size());
    // Bad regex
    SeriesOpener.filterImageList(list.clone(), 0, 0, 0, "*png", true, errorMessageAction::add,
        listAction);
    Assertions.assertEquals(0, listStringAction.size());
    Assertions.assertEquals(3, errorMessageAction.size());
  }
}
