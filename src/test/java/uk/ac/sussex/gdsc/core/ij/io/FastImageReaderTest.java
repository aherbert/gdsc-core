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

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.ImageWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * This tests read pixels using the {@link FastImageReader} matches the original binary data written
 * by the ImageJ {@link ImageWriter}.
 */
@SuppressWarnings({"javadoc"})
public class FastImageReaderTest {
  @Test
  public void testBadFileTypeThrows() {
    try (ByteArraySeekableStream data = ByteArraySeekableStream.wrap(new byte[10])) {
      final FileInfo fi = new FileInfo();
      fi.fileType = -1;
      final FastImageReader reader = new FastImageReader(fi);
      Assertions.assertThrows(IOException.class, () -> reader.readPixels(data));
      Assertions.assertThrows(IOException.class, () -> reader.readPixels((SeekableStream) data));
    }
  }

  @Test
  public void testHandleEof() throws EOFException {
    FastImageReader.handleEof(0);
    Assertions.assertThrows(EOFException.class, () -> FastImageReader.handleEof(-1));
  }

  @Test
  public void testCompressedImageThrows() {
    final FileInfo fi = new FileInfo();
    fi.compression = FileInfo.COMPRESSION_NONE + 1;
    Assertions.assertThrows(IllegalArgumentException.class, () -> new FastImageReader(fi));
  }

  @Test
  public void testGetRbg48Channels() {
    final FileInfo fi = new FileInfo();
    Assertions.assertEquals(3, FastImageReader.getRbg48Channels(fi));
    fi.samplesPerPixel = 2;
    Assertions.assertEquals(2, FastImageReader.getRbg48Channels(fi));
  }

  @Test
  public void testIsCompressedOrStrips() {
    FileInfo fi = new FileInfo();
    Assertions.assertEquals(false, FastImageReader.isCompressedOrStrips(fi));
    fi.compression = FileInfo.LZW;
    Assertions.assertEquals(true, FastImageReader.isCompressedOrStrips(fi));
    fi = new FileInfo();
    fi.stripOffsets = new int[1];
    Assertions.assertEquals(false, FastImageReader.isCompressedOrStrips(fi));
    fi.stripOffsets = new int[2];
    Assertions.assertEquals(true, FastImageReader.isCompressedOrStrips(fi));
  }

  @Test
  public void testIsCompressedOrStripsAndNotRbg48Planar() {
    FileInfo fi = new FileInfo();
    Assertions.assertEquals(false, FastImageReader.isCompressedOrStripsAndNotRgb48Planar(fi));
    fi.compression = FileInfo.LZW;
    Assertions.assertEquals(true, FastImageReader.isCompressedOrStripsAndNotRgb48Planar(fi));
    fi = new FileInfo();
    fi.stripOffsets = new int[1];
    Assertions.assertEquals(false, FastImageReader.isCompressedOrStripsAndNotRgb48Planar(fi));
    fi.stripOffsets = new int[2];
    Assertions.assertEquals(true, FastImageReader.isCompressedOrStripsAndNotRgb48Planar(fi));
    fi.fileType = FileInfo.RGB48_PLANAR;
    Assertions.assertEquals(false, FastImageReader.isCompressedOrStripsAndNotRgb48Planar(fi));
  }

  @Test
  public void testGetBufferSize() {
    Assertions.assertEquals(8192, FastImageReader.getBufferSize(0));
    Assertions.assertEquals(8192, FastImageReader.getBufferSize(8193));
    // The byte count must be > 32x above the default buffer size before expansion in blocks
    // for each doubling
    Assertions.assertEquals(8192, FastImageReader.getBufferSize(8192 * 32));
    Assertions.assertEquals(8192 * 2, FastImageReader.getBufferSize(8192 * 64));
    Assertions.assertEquals(8192 * 3, FastImageReader.getBufferSize(8192 * 96));
    // Maximum is the 2^30 = 1GiB
    Assertions.assertEquals(1 << 30, FastImageReader.getBufferSize(Long.MAX_VALUE));
  }

  @SeededTest
  public void canReadGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 100, rng, FastImageReaderTest::createGrey8, 1));
  }

  @SeededTest
  public void canReadGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 50, rng, FastImageReaderTest::createGrey16, 1));
  }

  @SeededTest
  public void canReadGrey16Signed(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 50, rng, FastImageReaderTest::createGrey16, 1),
        fi -> fi.fileType = FileInfo.GRAY16_SIGNED);
  }

  @SeededTest
  public void canReadGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 25, rng, FastImageReaderTest::createGrey32, 1));
  }

  @SeededTest
  public void canReadGrey32Unsigned(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 50, rng, FastImageReaderTest::createGrey32, 1),
        fi -> fi.fileType = FileInfo.GRAY32_UNSIGNED);
  }

  @SeededTest
  public void canReadGrey32Int(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 50, rng, FastImageReaderTest::createGrey32, 1),
        fi -> fi.fileType = FileInfo.GRAY32_INT);
  }

  @SeededTest
  public void canReadGrey64Float(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(96, 100, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.GRAY64_FLOAT;
      fi.width /= 8;
    });
  }

  @SeededTest
  public void canReadRgb(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 75, rng, FastImageReaderTest::createRgb, 1));
  }

  @SeededTest
  public void canReadBgr(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 75, rng, FastImageReaderTest::createRgb, 1),
        fi -> fi.fileType = FileInfo.BGR);
  }

  @SeededTest
  public void canReadArgb(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(16, 5, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.ARGB;
      fi.width /= 4;
    });
  }

  @SeededTest
  public void canReadAbgr(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(16, 5, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.ABGR;
      fi.width /= 4;
    });
  }

  @SeededTest
  public void canReadBarg(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(16, 5, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.BARG;
      fi.width /= 4;
    });
  }

  @SeededTest
  public void canReadCmyk(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    // Bigger image so hopefully some black pixels (1/256 chance)
    canRead(createImage(96, 100, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.CMYK;
      fi.width /= 4;
    });
  }

  @SeededTest
  public void canReadRgbPlanar(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(90, 75, rng, FastImageReaderTest::createRgb, 1),
        fi -> fi.fileType = FileInfo.RGB_PLANAR);
  }

  @SeededTest
  public void canReadBitmap(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(10, 5, rng, FastImageReaderTest::createGrey8, 1),
        fi -> fi.fileType = FileInfo.BITMAP);
  }

  @SeededTest
  public void canReadTinyBitmap(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Edge case where the width is modulo 8
    canRead(createImage(8, 8, rng, FastImageReaderTest::createGrey8, 1),
        fi -> fi.fileType = FileInfo.BITMAP);
  }

  @SeededTest
  public void canReadRgb48(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(12, 5, rng, FastImageReaderTest::createGrey16, 1), fi -> {
      fi.fileType = FileInfo.RGB48;
      fi.width /= 3;
      fi.stripOffsets = new int[] {0};
    });
  }

  @SeededTest
  public void canReadRgb48Planar(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(12, 5, rng, FastImageReaderTest::createGrey16, 1), fi -> {
      fi.fileType = FileInfo.RGB48_PLANAR;
      fi.width /= 3;
    });
  }

  @SeededTest
  public void canReadGrey12Unsigned(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(96, 100, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.GRAY12_UNSIGNED;
      fi.width = fi.width * 2 / 3;
    });
  }

  @SeededTest
  public void canReadGrey12UnsignedOddWidth(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(95, 100, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.GRAY12_UNSIGNED;
      fi.width = fi.width * 2 / 3;
    });
  }

  @SeededTest
  public void canReadGrey24Unsigned(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Use random bytes and then set to correct width for reading
    canRead(createImage(96, 100, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      fi.fileType = FileInfo.GRAY24_UNSIGNED;
      fi.width /= 3;
    });
  }

  @SeededTest
  public void canReadStrippedGrey8(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(200, 90, rng, FastImageReaderTest::createGrey8, 1), fi -> {
      final int size = fi.width * fi.height;
      fi.stripOffsets = new int[] {0, size / 2};
      fi.stripLengths = new int[] {size / 2, size / 2};
    });
  }

  @SeededTest
  public void canReadStrippedGrey16(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(200, 90, rng, FastImageReaderTest::createGrey16, 1), fi -> {
      final int size = fi.width * fi.height * 2;
      fi.stripOffsets = new int[] {0, size / 2};
      fi.stripLengths = new int[] {size / 2, size / 2};
    });
  }

  @SeededTest
  public void canReadStrippedGrey16Signed(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(5, 10, rng, FastImageReaderTest::createGrey16, 1), fi -> {
      fi.fileType = FileInfo.GRAY16_SIGNED;
      final int size = fi.width * fi.height * 2;
      fi.stripOffsets = new int[] {0, size / 2};
      fi.stripLengths = new int[] {size / 2, size / 2};
    });
  }

  @SeededTest
  public void canReadStrippedGrey32(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(200, 90, rng, FastImageReaderTest::createGrey32, 1), fi -> {
      final int size = fi.width * fi.height * 4;
      fi.stripOffsets = new int[] {0, size / 2};
      fi.stripLengths = new int[] {size / 2, size / 2};
    });
  }

  @SeededTest
  public void canReadStrippedGrey32Unsigned(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(5, 10, rng, FastImageReaderTest::createGrey32, 1), fi -> {
      fi.fileType = FileInfo.GRAY32_UNSIGNED;
      final int size = fi.width * fi.height * 4;
      fi.stripOffsets = new int[] {0, size / 2};
      fi.stripLengths = new int[] {size / 2, size / 2};
    });
  }

  @SeededTest
  public void canReadStrippedGrey32Int(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(5, 10, rng, FastImageReaderTest::createGrey32, 1), fi -> {
      fi.fileType = FileInfo.GRAY32_INT;
      final int size = fi.width * fi.height * 4;
      fi.stripOffsets = new int[] {0, size / 2};
      fi.stripLengths = new int[] {size / 2, size / 2};
    });
  }

  @SeededTest
  public void canReadStrippedRgb48(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    canRead(createImage(12, 5, rng, FastImageReaderTest::createGrey16, 1), fi -> {
      fi.fileType = FileInfo.RGB48;
      fi.width /= 3;
      final int size = fi.width * fi.height * 6;
      fi.stripOffsets = new int[] {0, size / 2};
      // Make the final strip too big on purpose to hit an edge case
      fi.stripLengths = new int[] {size / 2, size / 2 + 1};
    });
  }

  private static ImagePlus createImage(int width, int height, UniformRandomProvider rng,
      BiFunction<UniformRandomProvider, Integer, Object> pixels, int slices) {
    final ImageStack stack = new ImageStack(width, height);
    for (int i = 0; i < slices; i++) {
      stack.addSlice(null, pixels.apply(rng, width * height));
    }
    return new ImagePlus("test", stack);
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

  private static void canRead(ImagePlus imp) throws IOException {
    canRead(imp, FastImageReaderTest::consume);
  }

  private static void canRead(ImagePlus imp, Consumer<FileInfo> modifier) throws IOException {
    canRead(imp, true, modifier);
    canRead(imp, false, modifier);
  }

  private static void canRead(ImagePlus imp, boolean littleEndian, Consumer<FileInfo> modifier)
      throws IOException {
    canRead(littleEndian, imp.getFileInfo(), modifier);
  }

  private static void canRead(boolean littleEndian, FileInfo fi, Consumer<FileInfo> modifier)
      throws IOException {
    // Use IJ to write the image
    final ByteArrayOutputStream expected = new ByteArrayOutputStream();
    fi.intelByteOrder = littleEndian;
    new ImageWriter(fi).write(expected);

    modifier.accept(fi);

    final byte[] bytes = expected.toByteArray();

    // The IJ reader may modify the strip lengths to non-null
    final boolean noStripLengths = fi.stripLengths == null;

    // Read back with IJ
    final Object[] stack = new Object[fi.nImages];
    final double[] min = new double[fi.nImages];
    final double[] max = new double[fi.nImages];
    try (ByteArrayInputStream data = new ByteArrayInputStream(bytes)) {
      final ImageReader reader = new ImageReader(fi);
      for (int i = 0; i < fi.nImages; i++) {
        stack[i] = reader.readPixels(data);
        min[i] = reader.min;
        max[i] = reader.max;
      }
    }

    if (noStripLengths) {
      fi.stripLengths = null;
    }

    // Read back
    try (SeekableStream data = ByteArraySeekableStream.wrap(bytes)) {
      final FastImageReader reader = new FastImageReader(fi);
      for (int i = 0; i < fi.nImages; i++) {
        final Object pixels = reader.readPixels(data, 0);

        // Check equal
        Assertions.assertArrayEquals(new Object[] {stack[i]}, new Object[] {pixels},
            () -> "SeekableStream Litte-Endian: " + littleEndian);
        Assertions.assertEquals(min[i], reader.getMinRgb48Value());
        Assertions.assertEquals(max[i], reader.getMaxRgb48Value());
      }
    }

    // Read as a ByteArraySeekableStream
    try (ByteArraySeekableStream data = ByteArraySeekableStream.wrap(bytes)) {
      final FastImageReader reader = new FastImageReader(fi);
      for (int i = 0; i < fi.nImages; i++) {
        final Object pixels = reader.readPixels(data, 0);

        // Check equal
        Assertions.assertArrayEquals(new Object[] {stack[i]}, new Object[] {pixels},
            () -> "ByteArraySeekableStream Litte-Endian: " + littleEndian);
        Assertions.assertEquals(min[i], reader.getMinRgb48Value());
        Assertions.assertEquals(max[i], reader.getMaxRgb48Value());
      }
    }
  }

  /**
   * Consume the file info. This does nothing.
   *
   * @param fi the file info
   */
  private static void consume(FileInfo fi) {
    // Do nothing
  }
}
