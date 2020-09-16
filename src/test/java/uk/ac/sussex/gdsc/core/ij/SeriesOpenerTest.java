/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2020 Alex Herbert
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    IJ.save(new ImagePlus(null, new ByteProcessor(2, 3, data2)), path + "/image2.tif");
    IJ.save(new ImagePlus(null, new ByteProcessor(3, 2, data1)), path + "/image3.tif");
    IJ.save(new ImagePlus(null, new ByteProcessor(2, 4, data3)), path + "/image4.tif");

    // Create a directory and non-image file
    Files.createDirectory(Paths.get(path, "subDirectory"));
    Files.createFile(Paths.get(path, "notAnImage.list"));

    SeriesOpener series = SeriesOpener.create(path, false, 2);
    Assertions.assertEquals(5, series.getNumberOfImages());
    Assertions.assertEquals(path, series.getPath());
    Assertions.assertArrayEquals(
        new String[] {"image1.tif", "image2.tif", "image3.tif", "image4.tif", "notAnImage.list"},
        series.getImageList());
    Assertions.assertEquals(2, series.getNumberOfThreads());

    // Open the images
    ImagePlus imp = series.nextImage();
    Assertions.assertEquals("image1.tif", imp.getTitle());
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertArrayEquals(data1, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertEquals("image2.tif", imp.getTitle());
    Assertions.assertArrayEquals(data2, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertNull(imp);

    // Repeat but allow variable size
    series = SeriesOpener.create(path, false, 2);
    series.setVariableSize(true);
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertEquals("image1.tif", imp.getTitle());
    Assertions.assertArrayEquals(data1, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(3, imp.getHeight());
    Assertions.assertEquals("image2.tif", imp.getTitle());
    Assertions.assertArrayEquals(data2, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(3, imp.getWidth());
    Assertions.assertEquals(2, imp.getHeight());
    Assertions.assertEquals("image3.tif", imp.getTitle());
    Assertions.assertArrayEquals(data1, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertEquals(2, imp.getWidth());
    Assertions.assertEquals(4, imp.getHeight());
    Assertions.assertEquals("image4.tif", imp.getTitle());
    Assertions.assertArrayEquals(data3, (byte[]) imp.getProcessor().getPixels());
    imp = series.nextImage();
    Assertions.assertNull(imp);

    FileUtils.deleteDirectory(tmpDir.toFile());
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
  void testSeriesOpenerWithTextFiles() throws IOException {
    final Path tmpDir = Files.createTempDirectory("SeriesOpenerTest");
    final String path = tmpDir.toString();
    Files.createFile(Paths.get(path, "file1.txt"));
    Files.createFile(Paths.get(path, "file2.txt"));
    final SeriesOpener series = new SeriesOpener(path);
    Assertions.assertEquals(0, series.getNumberOfImages());
    Assertions.assertEquals(path, series.getPath());
    Assertions.assertArrayEquals(new String[0], series.getImageList());
    FileUtils.deleteDirectory(tmpDir.toFile());
  }
}
