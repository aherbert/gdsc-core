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

package uk.ac.sussex.gdsc.core.ij.io;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.io.FileInfo;
import ij.io.TiffEncoder;
import ij.measure.Calibration;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * This tests writing pixels and image metadata using the the {@link CustomTiffEncoder} matches the
 * binary data written by the ImageJ {@link TiffEncoder}.
 */
@SuppressWarnings({"javadoc"})
public class CustomTiffEncoderTest {

  @SeededTest
  public void canWriteTiffGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(90, 100, rng, CustomTiffEncoderTest::createGrey8, 1));
  }

  @SeededTest
  public void canWriteTiffGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(90, 50, rng, CustomTiffEncoderTest::createGrey16, 1));
  }

  @SeededTest
  public void canWriteTiffGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(90, 25, rng, CustomTiffEncoderTest::createGrey32, 1));
  }

  @SeededTest
  public void canWriteTiffRgb(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(90, 75, rng, CustomTiffEncoderTest::createRgb, 1));
  }

  @SeededTest
  public void canWriteTiffStackGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(2, 3, rng, CustomTiffEncoderTest::createGrey8, 3));
  }

  @SeededTest
  public void canWriteTiffStackGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(2, 3, rng, CustomTiffEncoderTest::createGrey16, 3));
  }

  @SeededTest
  public void canWriteTiffStackGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(2, 3, rng, CustomTiffEncoderTest::createGrey32, 3));
  }

  @SeededTest
  public void canWriteTiffStackRgb(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createImage(2, 3, rng, CustomTiffEncoderTest::createRgb, 3));
  }

  @SeededTest
  public void canWriteVirtualTiffStackGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createVirtualImage(2, 3, rng, CustomTiffEncoderTest::createGrey8, 2));
  }

  @SeededTest
  public void canWriteVirtualTiffStackGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createVirtualImage(2, 3, rng, CustomTiffEncoderTest::createGrey16, 2));
  }

  @SeededTest
  public void canWriteVirtualTiffStackGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createVirtualImage(2, 3, rng, CustomTiffEncoderTest::createGrey32, 2));
  }

  @SeededTest
  public void canWriteVirtualTiffStackRgb(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canWriteTiff(createVirtualImage(2, 3, rng, CustomTiffEncoderTest::createRgb, 2));
  }

  @SeededTest
  public void canWriteTiffRgb48(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey16, 3);
    final ImageStack stack = imp.getImageStack();
    stack.setSliceLabel("Red", 1);
    stack.setSliceLabel("Green", 2);
    stack.setSliceLabel("Blue", 3);
    final CompositeImage comp = new CompositeImage(imp);
    canWriteTiff(comp);
  }

  private static Object createGrey8(UniformRandomProvider rng, Integer size) {
    final byte[] pixels = new byte[size];
    rng.nextBytes(pixels);
    return pixels;
  }

  private static Object createGrey16(UniformRandomProvider rng, Integer size) {
    final short[] pixels = new short[size];
    for (int i = 0; i < size; i++) {
      pixels[i] = (short) rng.nextInt();
    }
    return pixels;
  }

  private static Object createGrey32(UniformRandomProvider rng, Integer size) {
    final float[] pixels = new float[size];
    for (int i = 0; i < size; i++) {
      pixels[i] = rng.nextFloat();
    }
    return pixels;
  }

  private static Object createRgb(UniformRandomProvider rng, Integer size) {
    final int[] pixels = new int[size];
    for (int i = 0; i < size; i++) {
      pixels[i] = rng.nextInt() >>> 8;
    }
    return pixels;
  }

  private static ImagePlus createImage(int width, int height, UniformRandomProvider rng,
      BiFunction<UniformRandomProvider, Integer, Object> pixels, int slices) {
    final ImageStack stack = new ImageStack(width, height);
    for (int i = 0; i < slices; i++) {
      stack.addSlice(null, pixels.apply(rng, width * height));
    }
    return new ImagePlus("test", stack);
  }

  private static ImagePlus createVirtualImage(int width, int height, UniformRandomProvider rng,
      BiFunction<UniformRandomProvider, Integer, Object> pixels, int slices) throws IOException {
    final String dir = Files.createTempDirectory(CustomTiffEncoderTest.class.getName()).toString();
    final VirtualStack stack = new VirtualStack(width, height, null, dir.toString());
    for (int i = 0; i < slices; i++) {
      final ImagePlus imp = createImage(width, height, rng, pixels, 1);
      final String name = i + ".tif";
      IJ.save(imp, Paths.get(dir, name).toString());
      stack.addSlice(name);
    }
    final ImagePlus imp = new ImagePlus("test", stack);
    return imp;
  }

  private static void canWriteTiff(ImagePlus imp) throws IOException {
    canWriteTiff(imp, true);
    canWriteTiff(imp, false);
  }

  private static void canWriteTiff(ImagePlus imp, boolean littleEndian) throws IOException {
    canWriteTiff(imp, littleEndian, imp.getFileInfo());
  }

  private static void canWriteTiff(ImagePlus imp, boolean littleEndian, FileInfo fi)
      throws IOException {
    final boolean virtualStack = imp.getStack().isVirtual();
    if (virtualStack) {
      fi.virtualStack = (VirtualStack) imp.getStack();
    }

    // Use IJ version
    final ByteArrayOutputStream expected = new ByteArrayOutputStream();
    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    ij.Prefs.intelByteOrder = littleEndian;
    new TiffEncoder(fi).write(expected);
    ij.Prefs.intelByteOrder = intelByteOrder;

    // Use custom version
    fi.intelByteOrder = littleEndian;
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    new CustomTiffEncoder(fi).write(data);

    // Check equal
    Assertions.assertArrayEquals(expected.toByteArray(), data.toByteArray(),
        () -> "Litte-Endian: " + littleEndian);
  }

  @SeededTest
  public void canWriteInvertedGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey8, 1);
    imp.setLut(imp.getProcessor().getLut().createInvertedLut());
    canWriteTiff(imp);
  }

  @SeededTest
  public void canWriteInvertedGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey16, 1);
    imp.setLut(imp.getProcessor().getLut().createInvertedLut());
    canWriteTiff(imp);
  }

  @SeededTest
  public void canWriteInvertedGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey32, 1);
    imp.setLut(imp.getProcessor().getLut().createInvertedLut());
    canWriteTiff(imp);
  }

  @SeededTest
  public void canWriteColourMappedGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey8, 1);
    changeColorModel(imp);
    canWriteTiff(imp);
  }

  @SeededTest
  public void canWriteColourMappedGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey16, 1);
    changeColorModel(imp);
    canWriteTiff(imp);
  }

  @SeededTest
  public void canWriteColourMappedGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey32, 1);
    changeColorModel(imp);
    canWriteTiff(imp);
  }

  private static void changeColorModel(ImagePlus imp) {
    final byte[] b1 = {0, 33, 77};
    final byte[] b2 = {10, (byte) 133, (byte) 255};
    imp.getProcessor().setColorModel(new IndexColorModel(8, 3, b1, b2, b1));
  }

  @SeededTest
  public void canWriteMetadata(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey8, 3);
    final Calibration cal = imp.getCalibration();
    cal.pixelWidth = 23.6;
    cal.pixelHeight = 43.2;
    cal.setUnit("cm");
    imp.setCalibration(cal);
    final FileInfo fi = imp.getFileInfo();
    // Must have null at the end.
    fi.description = new StringBuilder("Hello world").append((char) 0).toString();
    fi.info = "something";
    fi.sliceLabels = new String[] {"1", null, "3"};
    final int channels = imp.getNChannels();
    fi.displayRanges = new double[channels * 2];
    for (int i = 1; i <= channels; i++) {
      fi.displayRanges[(i - 1) * 2] = i * 2;
      fi.displayRanges[(i - 1) * 2 + 1] = i * 2 + 1;
    }
    fi.channelLuts = new byte[channels][];
    for (int i = 0; i < channels; i++) {
      // Must be the correct size
      fi.channelLuts[i] = randomBytes(rng, 256 * 3);
    }
    fi.plot = randomBytes(rng, 23);
    fi.roi = randomBytes(rng, 32);
    fi.overlay = new byte[][] {randomBytes(rng, 11), randomBytes(rng, 13)};
    fi.metaDataTypes = new int[] {3456, 7897};
    fi.metaData = new byte[][] {randomBytes(rng, 7), randomBytes(rng, 15)};

    canWriteTiff(imp, true, fi);
    canWriteTiff(imp, false, fi);
  }

  @SeededTest
  public void canWriteMetadata2(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ImagePlus imp = createImage(2, 3, rng, CustomTiffEncoderTest::createGrey8, 3);
    final Calibration cal = imp.getCalibration();
    cal.pixelWidth = 1000000.0 / (Integer.MAX_VALUE + 1.0);
    cal.pixelHeight = 43.2;
    cal.setUnit("inch");
    imp.setCalibration(cal);
    final FileInfo fi = imp.getFileInfo();

    // Create some metadata
    fi.roi = randomBytes(rng, 32);

    canWriteTiff(imp, true, fi);
    canWriteTiff(imp, false, fi);
  }

  private static byte[] randomBytes(UniformRandomProvider rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);
    return bytes;
  }
}
