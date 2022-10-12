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
 * Copyright (C) 2011 - 2022 Alex Herbert
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
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.RoiDecoder;
import ij.io.TiffDecoder;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.LUT;
import ij.process.ShortProcessor;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.io.FastTiffDecoder.IndexMap;
import uk.ac.sussex.gdsc.core.ij.io.FastTiffDecoder.NumberOfImages;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.LutColour;
import uk.ac.sussex.gdsc.core.logging.TrackProgressAdapter;
import uk.ac.sussex.gdsc.core.utils.rng.SplitMix;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

/**
 * This tests reading image metadata using the {@link FastTiffDecoder} matches the metadata read by
 * the ImageJ {@link TiffDecoder}.
 *
 * <p>Images supported by ImageJ are created, saved and read back. The metadata must be correct.
 *
 * <p>Images not supported by ImageJ are created as a dummy image and a check it is read the same as
 * ImageJ.
 */
@SuppressWarnings({"javadoc"})
class FastTiffDecoderTest {

  @Test
  void testSafeDivide() {
    Assertions.assertEquals(0, FastTiffDecoder.safeDivide(1.0, 0.0));
    Assertions.assertEquals(0.5, FastTiffDecoder.safeDivide(1.0, 2.0));
  }

  @SeededTest
  void canGetOrigin(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());

    for (int i = 0; i < 5; i++) {
      final int x = rng.nextInt(100);
      final int y = rng.nextInt(100);
      final int width = rng.nextInt(100);
      final int height = rng.nextInt(100);

      final ExtendedFileInfo fi = new ExtendedFileInfo();

      // "ROI": [x,y,w,h]
      String text =
          String.format("SummaryMetaData \"ROI\": [%d,%d,%d,%d] asdfasd", x, y, width, height);
      fi.setSummaryMetaData(text);
      checkOrigin(fi, x, y, width, height, text);
      fi.setSummaryMetaData(null);

      // "ROI": "x-y-w-h"
      text = String.format("info \"ROI\": \"%d-%d-%d-%d\" asdfasd", x, y, width, height);
      fi.info = text;
      checkOrigin(fi, x, y, width, height, text);
      fi.info = null;

      // "ROI": "x-y-w-h"
      text =
          String.format("extendedMetaData \"ROI\": \"%d-%d-%d-%d\" asdfasd", x, y, width, height);
      fi.setExtendedMetaData(text);
      checkOrigin(fi, x, y, width, height, text);
    }
  }

  private static void checkOrigin(ExtendedFileInfo fi, int x, int y, int width, int height,
      String text) {
    final Rectangle origin = FastTiffDecoder.getOrigin(fi);
    Assertions.assertNotNull(origin, text);
    Assertions.assertEquals(x, origin.x, () -> "X missing: " + text);
    Assertions.assertEquals(y, origin.y, () -> "Y missing: " + text);
    Assertions.assertEquals(width, origin.width, () -> "Width missing: " + text);
    Assertions.assertEquals(height, origin.height, () -> "Height missing: " + text);
  }

  @Test
  void canGetOriginSkipsBadPatterns() {
    final char start = '[';
    final char delimiter = ',';
    final char end = ']';
    for (final String pattern : new String[] {
        //@formatter:off
        null, "",
        "No ROI tag",
        "\"ROI\" without colon after",
        "\"ROI\": with no start character after",
        "\"ROI\": bad chars before start character[",
        "\"ROI\": [ no end character",
        "\"ROI\": []",
        "\"ROI\": [abc]",
        "\"ROI\": [0]",
        "\"ROI\": [0,0]",
        "\"ROI\": [0,0,0]",
        "\"ROI\": [0,0,,0]",
        "\"ROI\": [0,0,0,0,0]",
        //@formatter:on
    }) {
      Assertions.assertNull(FastTiffDecoder.getOrigin(pattern, start, delimiter, end), pattern);
    }
  }

  @Test
  void testHandleEof() throws EOFException {
    FastTiffDecoder.handleEof(0);
    Assertions.assertThrows(EOFException.class, () -> FastTiffDecoder.handleEof(-1));
    FastTiffDecoder.handleEof(678768, 678768);
    Assertions.assertThrows(EOFException.class, () -> FastTiffDecoder.handleEof(567, 678768));
  }

  @Test
  void testCreateTiffDecoder() throws IOException {
    final byte ii = 73;
    final byte mm = 77;
    final byte magic = 42;
    final byte zero = 0;
    final byte bad = 99;
    // Intel
    createWithBytes(ii, ii, magic, zero);
    Assertions.assertThrows(IOException.class, () -> createWithBytes(ii, bad, magic, zero));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(ii, ii, magic, bad));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(ii, ii, bad, zero));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(ii, ii, bad, bad));
    // Motorola
    createWithBytes(mm, mm, zero, magic);
    Assertions.assertThrows(IOException.class, () -> createWithBytes(mm, bad, zero, magic));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(mm, mm, bad, magic));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(mm, mm, zero, bad));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(mm, mm, bad, bad));

    Assertions.assertThrows(IOException.class, () -> createWithBytes(bad, bad, bad, bad));
    Assertions.assertThrows(IOException.class, () -> createWithBytes(bad, bad, bad));
  }

  private static FastTiffDecoder createWithBytes(byte... bytes) throws IOException {
    return FastTiffDecoder.create(ByteArraySeekableStream.wrap(bytes), "test");
  }

  @Test
  void testSeek() throws IOException {
    final byte[] bytes = {73, 73, 42, 0};
    final long[] position = new long[1];
    @SuppressWarnings("resource")
    final SeekableStream ss = new DummySeekableStream(bytes) {
      @Override
      public void seek(long loc) {
        position[0] = loc;
      }
    };
    final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
    decoder.reset();
    Assertions.assertEquals(4, position[0]);
  }

  @Test
  void testSaveImageDescription() throws IOException {
    final byte[] bytes = {73, 73, 42, 0};
    @SuppressWarnings("resource")
    final SeekableStream ss = new DummySeekableStream(bytes);
    final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
    final ExtendedFileInfo fi = new ExtendedFileInfo();
    // Null safe
    decoder.saveImageDescription(null, fi);
    // Saves tiny descriptions
    decoder.saveImageDescription("d".getBytes(), fi);
    Assertions.assertEquals("d", fi.description);
    // Ignores bad parsing for number of images
    fi.nImages = -1;
    decoder.saveImageDescription("images=".getBytes(), fi);
    Assertions.assertEquals(-1, fi.nImages);
    decoder.saveImageDescription("images=\n".getBytes(), fi);
    Assertions.assertEquals(-1, fi.nImages);
    decoder.saveImageDescription("images=1\n".getBytes(), fi);
    Assertions.assertEquals(1, fi.nImages);
    decoder.saveImageDescription("images=2\n".getBytes(), fi);
    Assertions.assertEquals(2, fi.nImages);
  }

  @Test
  void testStandardTiffMetadata() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImagePlus imp = new ImagePlus("test", new ByteProcessor(width, height));
    imp.setProperty("Info", "something");
    imp.setRoi(1, 2, 3, 4);
    final Plot plot = new Plot("plot", "x data", "y data");
    imp.setProperty(Plot.PROPERTY_KEY, plot);
    final Overlay overlay = new Overlay();
    final Roi roi = new Roi(0, 1, 3, 2);
    overlay.add(roi);
    imp.setOverlay(overlay);
    final Calibration cal = imp.getCalibration();
    cal.pixelWidth = 23.6;
    cal.pixelHeight = 43.2;
    cal.setUnit("cm");

    final LUT lut = LutHelper.createLut(LutColour.DISTINCT);
    imp.setLut(lut);

    // IJ 1.53t uses the actual slice labels. This must be set after the LUT which resets the stack.
    imp.getImageStack().setSliceLabel("my label", 1);

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);
        decoder.setTrackProgress(null);

        // Property test
        int tmp = decoder.getIfdCountForDebugData();
        decoder.setIfdCountForDebugData(tmp + 1);
        Assertions.assertEquals(tmp + 1, decoder.getIfdCountForDebugData());
        tmp = decoder.getIfdCountForMicroManagerMetadata();
        decoder.setIfdCountForMicroManagerMetadata(tmp + 4);
        Assertions.assertEquals(tmp + 4, decoder.getIfdCountForMicroManagerMetadata());

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);
        Assertions.assertNotNull(info);

        // Check
        Assertions.assertEquals(1, info.length);
        final FileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.COLOR8, fi.fileType);
        Assertions.assertEquals(imp.getInfoProperty(), fi.info);
        Assertions.assertEquals(imp.getImageStack().getSliceLabel(1), fi.sliceLabels[0]);
        // The ROI is calibrated using the image. This effects the equals() function.
        final Roi roi2 = RoiDecoder.openFromByteArray(fi.roi);
        roi2.setImage(imp);
        Assertions.assertEquals(imp.getRoi(), roi2);
        Assertions.assertArrayEquals(plot.toByteArray(), fi.plot);
        Assertions.assertEquals(1, fi.overlay.length);
        // The overlay ROI is not calibrated using the image
        Assertions.assertEquals(roi, RoiDecoder.openFromByteArray(fi.overlay[0]));
        Assertions.assertNotNull(fi.description);
        Assertions.assertTrue(fi.description.contains(ImageJ.VERSION));

        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        // These do not round-trip perfectly
        Assertions.assertEquals(cal.pixelWidth, fi.pixelWidth, 1e-3);
        Assertions.assertEquals(cal.pixelHeight, fi.pixelHeight, 1e-3);
        Assertions.assertEquals("cm", fi.unit);

        Assertions.assertEquals(256, fi.lutSize);
        final byte[] bytes = new byte[fi.lutSize];
        lut.getReds(bytes);
        Assertions.assertArrayEquals(bytes, fi.reds);
        lut.getGreens(bytes);
        Assertions.assertArrayEquals(bytes, fi.greens);
        lut.getBlues(bytes);
        Assertions.assertArrayEquals(bytes, fi.blues);

        decoder.reset();
        final NumberOfImages no = decoder.getNumberOfImages();
        Assertions.assertEquals(1, no.getImageCount());

        Assertions.assertNull(decoder.getIndexMap());
      }
    }
  }

  @Test
  void testDecodeCompositeImage() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImageStack stack = new ImageStack(width, height);
    stack.addSlice(null, new byte[width * height]);
    stack.addSlice(null, new byte[width * height]);
    stack.addSlice(null, new byte[width * height]);
    final CompositeImage imp = new CompositeImage(new ImagePlus("test", stack));

    // channel LUTs
    final LUT lut = LutHelper.createLut(LutColour.DISTINCT);
    imp.setChannelLut(lut, 1);

    // Display ranges (must be set after channel luts)
    final double min = 1;
    final double max = 250;
    for (int i = 0; i < 3; i++) {
      imp.setC(i + 1);
      imp.setDisplayRange(min + i, max + i);
    }

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);
        Assertions.assertNotNull(info);

        // Check
        Assertions.assertEquals(1, info.length);
        final FileInfo fi = info[0];

        Assertions.assertEquals(6, fi.displayRanges.length);
        for (int i = 0; i < 3; i++) {
          Assertions.assertEquals(min + i, fi.displayRanges[i * 2]);
          Assertions.assertEquals(max + i, fi.displayRanges[i * 2 + 1]);
        }

        Assertions.assertEquals(3, fi.channelLuts.length);

        // 1 custom lut
        Assertions.assertEquals(256, fi.lutSize);
        final ByteBuffer bb = ByteBuffer.allocate(fi.lutSize * 3);
        final byte[] bytes = new byte[fi.lutSize];
        lut.getReds(bytes);
        bb.put(bytes);
        lut.getGreens(bytes);
        bb.put(bytes);
        lut.getBlues(bytes);
        bb.put(bytes);
        final byte[] allBytes = bb.array();
        Assertions.assertArrayEquals(allBytes, fi.channelLuts[0]);
      }
    }
  }

  @Test
  void testInchUnit() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImagePlus imp = new ImagePlus("test", new ShortProcessor(width, height));
    final Calibration cal = imp.getCalibration();
    cal.setUnit("inch");

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);

        // Check
        final FileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.GRAY16_UNSIGNED, fi.fileType);

        Assertions.assertEquals("inch", fi.unit);
      }
    }
  }

  @Test
  void testUmUnit() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImagePlus imp = new ImagePlus("test", new ByteProcessor(width, height));
    final Calibration cal = imp.getCalibration();
    cal.setUnit("um");

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);

        // Check
        final FileInfo fi = info[0];
        Assertions.assertEquals(" ", fi.unit);
      }
    }
  }

  @Test
  void testRbg() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImagePlus imp = new ImagePlus("test", new ColorProcessor(width, height));

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);

        // Check
        final FileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.RGB, fi.fileType);
      }
    }
  }

  @Test
  void testGrey8() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImagePlus imp = new ImagePlus("test", new ByteProcessor(width, height));

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);

        // Check
        final FileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.GRAY8, fi.fileType);
      }
    }
  }

  @Test
  void testGrey16Stack() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImageStack stack = new ImageStack(width, height);
    stack.addSlice(null, new short[width * height]);
    stack.addSlice(null, new short[width * height]);
    final ImagePlus imp = new ImagePlus("test", stack);

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);

        // Check.
        // ImageJ puts the stack size in the TIFF in a different tag so only uses 1 IFD
        Assertions.assertEquals(1, info.length);
        final FileInfo fi = info[0];
        Assertions.assertEquals(2, fi.nImages);
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.GRAY16_UNSIGNED, fi.fileType);

        decoder.reset();
        final NumberOfImages no = decoder.getNumberOfImages();
        Assertions.assertEquals(2, no.getImageCount());

        Assertions.assertNull(decoder.getIndexMap());
      }
    }
  }

  @Test
  void test72Dpi() throws IOException {
    final int width = 5;
    final int height = 6;
    final ImagePlus imp = new ImagePlus("test", new FloatProcessor(width, height));
    final Calibration cal = imp.getCalibration();
    cal.setUnit("inch");
    cal.pixelWidth = 1.0 / 72;

    // Use IJ to write the image
    final String path = createTmpFile();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (final boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        // Run in debug mode to hit lots of coverage.
        // Do not care what the debug mode outputs.
        if (littleEndian) {
          decoder.enableDebugging();
        }

        final FileInfo[] info = decoder.getTiffInfo(true);

        // Check
        final FileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.GRAY32_FLOAT, fi.fileType);

        Assertions.assertEquals(1.0, fi.pixelWidth);
        Assertions.assertEquals(1.0, fi.pixelHeight);
        Assertions.assertNull(fi.unit);
      }
    }
  }

  @Test
  void testGetName() {
    Assertions.assertEquals("NewSubfileType",
        FastTiffDecoder.getName(FastTiffDecoder.NEW_SUBFILE_TYPE));
    Assertions.assertEquals("ImageWidth", FastTiffDecoder.getName(FastTiffDecoder.IMAGE_WIDTH));
    Assertions.assertEquals("ImageLength", FastTiffDecoder.getName(FastTiffDecoder.IMAGE_LENGTH));
    Assertions.assertEquals("StripOffsets", FastTiffDecoder.getName(FastTiffDecoder.STRIP_OFFSETS));
    Assertions.assertEquals("Orientation", FastTiffDecoder.getName(FastTiffDecoder.ORIENTATION));
    Assertions.assertEquals("PhotoInterp", FastTiffDecoder.getName(FastTiffDecoder.PHOTO_INTERP));
    Assertions.assertEquals("ImageDescription",
        FastTiffDecoder.getName(FastTiffDecoder.IMAGE_DESCRIPTION));
    Assertions.assertEquals("BitsPerSample",
        FastTiffDecoder.getName(FastTiffDecoder.BITS_PER_SAMPLE));
    Assertions.assertEquals("SamplesPerPixel",
        FastTiffDecoder.getName(FastTiffDecoder.SAMPLES_PER_PIXEL));
    Assertions.assertEquals("RowsPerStrip",
        FastTiffDecoder.getName(FastTiffDecoder.ROWS_PER_STRIP));
    Assertions.assertEquals("StripByteCount",
        FastTiffDecoder.getName(FastTiffDecoder.STRIP_BYTE_COUNT));
    Assertions.assertEquals("XResolution", FastTiffDecoder.getName(FastTiffDecoder.X_RESOLUTION));
    Assertions.assertEquals("YResolution", FastTiffDecoder.getName(FastTiffDecoder.Y_RESOLUTION));
    Assertions.assertEquals("ResolutionUnit",
        FastTiffDecoder.getName(FastTiffDecoder.RESOLUTION_UNIT));
    Assertions.assertEquals("Software", FastTiffDecoder.getName(FastTiffDecoder.SOFTWARE));
    Assertions.assertEquals("DateTime", FastTiffDecoder.getName(FastTiffDecoder.DATE_TIME));
    Assertions.assertEquals("Artist", FastTiffDecoder.getName(FastTiffDecoder.ARTIST));
    Assertions.assertEquals("HostComputer", FastTiffDecoder.getName(FastTiffDecoder.HOST_COMPUTER));
    Assertions.assertEquals("PlanarConfiguration",
        FastTiffDecoder.getName(FastTiffDecoder.PLANAR_CONFIGURATION));
    Assertions.assertEquals("Compression", FastTiffDecoder.getName(FastTiffDecoder.COMPRESSION));
    Assertions.assertEquals("Predictor", FastTiffDecoder.getName(FastTiffDecoder.PREDICTOR));
    Assertions.assertEquals("ColorMap", FastTiffDecoder.getName(FastTiffDecoder.COLOR_MAP));
    Assertions.assertEquals("SampleFormat", FastTiffDecoder.getName(FastTiffDecoder.SAMPLE_FORMAT));
    Assertions.assertEquals("JPEGTables", FastTiffDecoder.getName(FastTiffDecoder.JPEG_TABLES));
    Assertions.assertEquals("NIHImageHeader",
        FastTiffDecoder.getName(FastTiffDecoder.NIH_IMAGE_HDR));
    Assertions.assertEquals("MetaDataByteCounts",
        FastTiffDecoder.getName(FastTiffDecoder.META_DATA_BYTE_COUNTS));
    Assertions.assertEquals("MetaData", FastTiffDecoder.getName(FastTiffDecoder.META_DATA));
    Assertions.assertEquals("MicroManagerMetaData",
        FastTiffDecoder.getName(FastTiffDecoder.MICRO_MANAGER_META_DATA));
    Assertions.assertEquals(FastTiffDecoder.getName(-99999), "???");
  }

  @Test
  void testGetFieldTypeName() {
    Assertions.assertEquals("byte", FastTiffDecoder.getFieldTypeName(FastTiffDecoder.BYTE));
    Assertions.assertEquals("ASCII string",
        FastTiffDecoder.getFieldTypeName(FastTiffDecoder.ASCII_STRING));
    Assertions.assertEquals("word", FastTiffDecoder.getFieldTypeName(FastTiffDecoder.WORD));
    Assertions.assertEquals("dword", FastTiffDecoder.getFieldTypeName(FastTiffDecoder.DWORD));
    Assertions.assertEquals("rational", FastTiffDecoder.getFieldTypeName(FastTiffDecoder.RATIONAL));
    Assertions.assertEquals("unknown",
        FastTiffDecoder.getFieldTypeName(FastTiffDecoder.RATIONAL + 100));
  }

  @Test
  void testGetFieldTypeSize() {
    Assertions.assertEquals(1, FastTiffDecoder.getFieldTypeSize(FastTiffDecoder.BYTE));
    Assertions.assertEquals(1, FastTiffDecoder.getFieldTypeSize(FastTiffDecoder.ASCII_STRING));
    Assertions.assertEquals(2, FastTiffDecoder.getFieldTypeSize(FastTiffDecoder.WORD));
    Assertions.assertEquals(4, FastTiffDecoder.getFieldTypeSize(FastTiffDecoder.DWORD));
    Assertions.assertEquals(8, FastTiffDecoder.getFieldTypeSize(FastTiffDecoder.RATIONAL));
    // Invalid. Prevent logging the warning.
    final Logger logger = Logger.getLogger(FastTiffDecoder.class.getName());
    final Level level = logger.getLevel();
    logger.setLevel(Level.OFF);
    Assertions.assertEquals(1, FastTiffDecoder.getFieldTypeSize(FastTiffDecoder.RATIONAL + 100));
    logger.setLevel(level);
  }

  @Test
  void testGetBytesPerPixel() {
    final int width = 3;
    final int height = 5;
    final int size = width * height;
    testGetBytesPerPixel(size, width, height, FileInfo.GRAY8, FileInfo.COLOR8, FileInfo.BITMAP);
    testGetBytesPerPixel((3 * size) / 2, width, height, FileInfo.GRAY12_UNSIGNED);
    testGetBytesPerPixel(2 * size, width, height, FileInfo.GRAY16_SIGNED, FileInfo.GRAY16_UNSIGNED);
    testGetBytesPerPixel(3 * size, width, height, FileInfo.GRAY24_UNSIGNED);
    testGetBytesPerPixel(4 * size, width, height, FileInfo.GRAY32_INT, FileInfo.GRAY32_UNSIGNED,
        FileInfo.GRAY32_FLOAT);
    testGetBytesPerPixel(8 * size, width, height, FileInfo.GRAY64_FLOAT);
    testGetBytesPerPixel(4 * size, width, height, FileInfo.ARGB, FileInfo.BARG, FileInfo.ABGR,
        FileInfo.CMYK);
    testGetBytesPerPixel(3 * size, width, height, FileInfo.RGB, FileInfo.RGB_PLANAR, FileInfo.BGR);
    testGetBytesPerPixel(6 * size, width, height, FileInfo.RGB48);
    testGetBytesPerPixel(2 * size, width, height, FileInfo.RGB48_PLANAR);
    testGetBytesPerPixel(0, width, height, -1);
  }

  private static void testGetBytesPerPixel(int size, int width, int height, int... fileTypes) {
    for (final int fileType : fileTypes) {
      Assertions.assertEquals(size, FastTiffDecoder.getBytesPerImage(width, height, fileType));
    }
  }

  @Test
  void testNumberOfImages() {
    NumberOfImages no = new NumberOfImages(1);
    Assertions.assertEquals(1, no.getImageCount());
    Assertions.assertEquals(0, no.getError());
    Assertions.assertTrue(no.isExact());

    no = new NumberOfImages(43, 0.123);
    Assertions.assertEquals(43, no.getImageCount());
    Assertions.assertEquals(0.123, no.getError());
    Assertions.assertFalse(no.isExact());
  }

  @Test
  void testImageJnImages() {
    // Ignore nImages=1
    Assertions.assertEquals(1, FastTiffDecoder.getImageJnImages("images=1\n"));
    Assertions.assertEquals(2, FastTiffDecoder.getImageJnImages("images=2\n"));
    Assertions.assertEquals(3, FastTiffDecoder.getImageJnImages("images=3\n"));
    Assertions.assertEquals(0, FastTiffDecoder.getImageJnImages("images=x\n"));
    Assertions.assertEquals(0, FastTiffDecoder.getImageJnImages("images=\n"));
    Assertions.assertEquals(0, FastTiffDecoder.getImageJnImages("images=2"));
    Assertions.assertEquals(0, FastTiffDecoder.getImageJnImages(""));
  }

  @Test
  void testGetGapInfo() {
    final ExtendedFileInfo[] fi =
        {new ExtendedFileInfo(), new ExtendedFileInfo(), new ExtendedFileInfo()};
    // Regular spacing
    final long gap = 15;
    fi[0].longOffset = 42;
    fi[1].longOffset = 42 + gap;
    fi[2].longOffset = 42 + 2 * gap;
    Assertions.assertEquals("15", FastTiffDecoder.getGapInfo(fi));
    // Irregular spacing
    fi[2].longOffset++;
    Assertions.assertEquals("varies (15 to 16)", FastTiffDecoder.getGapInfo(fi));
  }

  @Test
  void testIndexMap() {
    // Map is channel, slice, frame, position, offset
    // There is no checking of the input data so we can use the same CZTPO

    // Hit edge case for the same value in all positions
    final IndexMap dummy = new IndexMap(new int[] {1, 2, 3, 4, 5, 1, 2, 3, 4, 1000});
    Assertions.assertTrue(dummy.isSingleChannel());
    Assertions.assertTrue(dummy.isSingleSlice());
    Assertions.assertTrue(dummy.isSingleFrame());
    Assertions.assertTrue(dummy.isSinglePosition());

    final IndexMap single = new IndexMap(new int[] {1, 2, 3, 4, 5});
    IndexMap dual = new IndexMap(new int[] {6, 8, 10, 12, 14, 1, 2, 3, 4, 5});

    Assertions.assertEquals(1, single.getSize());
    Assertions.assertEquals(2, dual.getSize());

    Assertions.assertTrue(single.isSingleChannel());
    Assertions.assertTrue(single.isSingleSlice());
    Assertions.assertTrue(single.isSingleFrame());
    Assertions.assertTrue(single.isSinglePosition());
    Assertions.assertFalse(dual.isSingleChannel());
    Assertions.assertFalse(dual.isSingleSlice());
    Assertions.assertFalse(dual.isSingleFrame());
    Assertions.assertFalse(dual.isSinglePosition());

    Assertions.assertEquals(1, dual.getMinChannelIndex());
    Assertions.assertEquals(6, dual.getMaxChannelIndex());
    Assertions.assertEquals(2, dual.getMinSliceIndex());
    Assertions.assertEquals(8, dual.getMaxSliceIndex());
    Assertions.assertEquals(3, dual.getMinFrameIndex());
    Assertions.assertEquals(10, dual.getMaxFrameIndex());
    Assertions.assertEquals(4, dual.getMinPositionIndex());
    Assertions.assertEquals(12, dual.getMaxPositionIndex());

    Assertions.assertEquals(6, dual.getNChannels());
    Assertions.assertEquals(7, dual.getNSlices());
    Assertions.assertEquals(8, dual.getNFrames());
    Assertions.assertEquals(9, dual.getNPositions());

    Assertions.assertEquals(1, single.getChannelIndex(0));
    Assertions.assertEquals(2, single.getSliceIndex(0));
    Assertions.assertEquals(3, single.getFrameIndex(0));
    Assertions.assertEquals(4, single.getPositionIndex(0));
    Assertions.assertEquals(5L, single.getOffset(0));
    Assertions.assertEquals(6, dual.getChannelIndex(0));
    Assertions.assertEquals(8, dual.getSliceIndex(0));
    Assertions.assertEquals(10, dual.getFrameIndex(0));
    Assertions.assertEquals(12, dual.getPositionIndex(0));
    Assertions.assertEquals(14L, dual.getOffset(0));
    Assertions.assertEquals(1, dual.getChannelIndex(1));
    Assertions.assertEquals(2, dual.getSliceIndex(1));
    Assertions.assertEquals(3, dual.getFrameIndex(1));
    Assertions.assertEquals(4, dual.getPositionIndex(1));
    Assertions.assertEquals(5L, dual.getOffset(1));

    // Reverse order for the min/max limits
    dual = new IndexMap(new int[] {1, 2, 3, 4, 5, 6, 8, 10, 12, 14});
    Assertions.assertEquals(1, dual.getMinChannelIndex());
    Assertions.assertEquals(6, dual.getMaxChannelIndex());
    Assertions.assertEquals(2, dual.getMinSliceIndex());
    Assertions.assertEquals(8, dual.getMaxSliceIndex());
    Assertions.assertEquals(3, dual.getMinFrameIndex());
    Assertions.assertEquals(10, dual.getMaxFrameIndex());
    Assertions.assertEquals(4, dual.getMinPositionIndex());
    Assertions.assertEquals(12, dual.getMaxPositionIndex());

    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> single.getChannelIndex(-1));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> single.getChannelIndex(5));
  }

  // For the remaining metadata create dummy image and check it is read the same as ImageJ.

  @Test
  void testBadIfd() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(9);
    final int width = 15;
    final int height = 30;
    final int offset = 12345;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      // Hit the edge case for no debug info. This is not asserted
      decoder.enableDebugging();
      decoder.setIfdCountForDebugData(0);
      final ExtendedFileInfo[] info = decoder.getTiffInfo(true);
      Assertions.assertNotNull(info);
      final FileInfo fi = info[0];
      Assertions.assertEquals(width, fi.width);
      Assertions.assertEquals(height, fi.height);
      Assertions.assertEquals(offset, fi.offset);
    }
    // Blank out the IFD offset in bytes 4-7.
    byte[] bytes2 = bytes.clone();
    Arrays.fill(bytes2, 4, 8, (byte) 0);
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes2)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      Assertions.assertNull(decoder.getTiffInfo(true));
      decoder.reset();
      Assertions.assertEquals(0, decoder.getNumberOfImages().getImageCount());
    }
    // Blank out the IFD entry count in bytes 8-9.
    bytes2 = bytes.clone();
    bytes2[8] = 0;
    bytes2[9] = 0;
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes2)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      Assertions.assertNull(decoder.getTiffInfo(true));
      decoder.reset();
      Assertions.assertEquals(0, decoder.getNumberOfImages().getImageCount());
    }
    // Set the IFD entry count in bytes 8-9 above 1000.
    bytes2 = bytes.clone();
    bytes2[8] = -1;
    bytes2[9] = -1;
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes2)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      Assertions.assertNull(decoder.getTiffInfo(true));
      decoder.reset();
      Assertions.assertEquals(0, decoder.getNumberOfImages().getImageCount());
    }
    // Truncate the IFD
    for (final boolean bass : new boolean[] {true, false}) {
      try (SeekableStream ss = bass ? ByteArraySeekableStream.wrap(bytes, 20)
          : new MemoryCacheSeekableStream(new ByteArrayInputStream(bytes, 0, 20))) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
        Assertions.assertNull(decoder.getTiffInfo(false));
      }
    }
  }

  @Test
  void testMultiIfd() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(9);
    final int width = 15;
    final int height = 30;
    final int offset = 12345;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(false);
    writer.beginIfd(10);
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset * 2);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    // Adds a tag that should be ignored with pixelDataOnly=true
    writer.writeEntry(FastTiffDecoder.NIH_IMAGE_HDR, FastTiffDecoder.LONG, 256,
        writer.getMetaDataOffset());
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      // Hit the edge case for no debug info. This is not asserted
      decoder.enableDebugging();
      final ExtendedFileInfo[] info = decoder.getTiffInfo(true);
      Assertions.assertNotNull(info);
      Assertions.assertEquals(2, info.length);
      FileInfo fi = info[0];
      Assertions.assertEquals(width, fi.width);
      Assertions.assertEquals(height, fi.height);
      Assertions.assertEquals(offset, fi.offset);
      fi = info[1];
      Assertions.assertEquals(width, fi.width);
      Assertions.assertEquals(height, fi.height);
      Assertions.assertEquals(offset * 2, fi.offset);

      // Test we can get the number of images from IFDs
      decoder.reset();
      Assertions.assertEquals(2, decoder.getNumberOfImages().getImageCount());
    }

    // Hit edge case where the second IFD cannot be read.
    // Set the IFD entry count to zero.
    // The first IFD starts at position 8. The next is [2 + 12 * 9 + 4] bytes later.
    // in bytes 8-9 above 1000.
    bytes[8 + 2 + 12 * 9 + 4] = 0;
    bytes[9 + 2 + 12 * 9 + 4] = 0;
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      final ExtendedFileInfo[] info = decoder.getTiffInfo(true);
      Assertions.assertNotNull(info);
      Assertions.assertEquals(1, info.length);
      final FileInfo fi = info[0];
      Assertions.assertEquals(width, fi.width);
      Assertions.assertEquals(height, fi.height);
      Assertions.assertEquals(offset, fi.offset);

      // Test we can get the number of images from IFDs
      decoder.reset();
      Assertions.assertEquals(1, decoder.getNumberOfImages().getImageCount());
    }
  }

  @Test
  void testNonImageJ16bitLut() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(9);
    final int width = 15;
    final int height = 30;
    final int offset = 12345;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 16);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    // TODO - actually write a LUT. Otherwise this just hits coverage in the code.
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      // Hit the edge case for no debug info. This is not asserted
      decoder.enableDebugging();
      final ExtendedFileInfo[] info = decoder.getTiffInfo(false);
      Assertions.assertNotNull(info);
      final FileInfo fi = info[0];
      Assertions.assertEquals(width, fi.width);
      Assertions.assertEquals(height, fi.height);
      Assertions.assertEquals(offset, fi.offset);
      Assertions.assertEquals(0, fi.lutSize);
    }
  }

  @Test
  void testReadMicroManagerSummaryMetadata() throws IOException {
    final String metaData = "Hello world";
    final DummyTiffWriter writer = new DummyTiffWriter(true, metaData);
    // Write a valid tiff.
    writer.beginIfd(9);
    final int width = 15;
    final int height = 30;
    final int offset = 12345;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      // Hit the edge case for no debug info. This is not asserted
      decoder.enableDebugging();
      final ExtendedFileInfo[] info = decoder.getTiffInfo(false);
      Assertions.assertNotNull(info);
      final ExtendedFileInfo fi = info[0];
      Assertions.assertEquals(width, fi.width);
      Assertions.assertEquals(height, fi.height);
      Assertions.assertEquals(offset, fi.offset);
      Assertions.assertEquals(metaData, fi.getSummaryMetaData());
    }
  }

  @Test
  void testGetIndexMap() throws IOException {
    // Write a valid OME tiff.
    final String metaData = "Dummy OME-TIFF";
    final DummyTiffWriter writer = new DummyTiffWriter(true, metaData);
    final int ifdOffset = writer.beginIfd(9);
    final int width = 1;
    final int height = 2;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    final int offset = writer.getMetaDataOffset();
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(true);

    // Write the image
    final ByteBuffer bb = writer.getTiffByteBuffer();
    Assertions.assertEquals(bb.position(), offset);
    final byte[] pixels = new byte[] {123, -77};
    bb.put(pixels);

    // Write the index map:
    final int mapOffset = bb.position();
    // Magic number 3453623
    bb.putInt(3453623);
    // Entries
    final int entryOffset = bb.position();
    bb.putInt(1);
    // 5 ints per entry: C, Z, T, position, IFD offset
    final int channel = 2;
    final int slice = 3;
    final int frame = 4;
    final int position = 5;
    bb.putInt(channel);
    bb.putInt(slice);
    bb.putInt(frame);
    bb.putInt(position);
    bb.putInt(ifdOffset);

    // Write the map offset into the tiff header
    // Magic number 54773648
    bb.putInt(8, 54773648);
    // Map offset
    bb.putInt(12, mapOffset);

    byte[] bytes = writer.getTiff();

    // Do this twice
    for (final boolean bass : new boolean[] {true, false}) {
      try (SeekableStream ss = bass ? ByteArraySeekableStream.wrap(bytes)
          : new MemoryCacheSeekableStream(new ByteArrayInputStream(bytes))) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
        final ExtendedFileInfo[] info = decoder.getTiffInfo(false);
        Assertions.assertNotNull(info);
        ExtendedFileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(offset, fi.offset);
        final IndexMap map = decoder.getIndexMap();
        Assertions.assertEquals(1, map.getSize());
        Assertions.assertEquals(channel, map.getChannelIndex(0));
        Assertions.assertEquals(slice, map.getSliceIndex(0));
        Assertions.assertEquals(frame, map.getFrameIndex(0));
        Assertions.assertEquals(position, map.getPositionIndex(0));
        Assertions.assertEquals(ifdOffset, map.getOffset(0));

        // Test we can get the number of images from the index map.
        decoder.reset();
        Assertions.assertEquals(1, decoder.getNumberOfImages().getImageCount());

        // Try getting the info direct
        fi = decoder.getTiffInfo(map, 0, false);
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(offset, fi.offset);
        Assertions.assertEquals(metaData, fi.getSummaryMetaData());

        // Hit the edge case for debugging enabled
        decoder.enableDebugging();
        fi = decoder.getTiffInfo(map, 0, true);
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(offset, fi.offset);
        Assertions.assertEquals(null, fi.getSummaryMetaData());
      }
      // Truncate the index map
      byte[] bytes2 = Arrays.copyOf(bytes, bytes.length - 1);
      try (SeekableStream ss = bass ? ByteArraySeekableStream.wrap(bytes2)
          : new MemoryCacheSeekableStream(new ByteArrayInputStream(bytes2))) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
        final IndexMap map = decoder.getIndexMap();
        Assertions.assertNull(map);
      }
      // Truncate the index map totally
      bytes2 = Arrays.copyOf(bytes, mapOffset);
      try (SeekableStream ss = bass ? ByteArraySeekableStream.wrap(bytes2)
          : new MemoryCacheSeekableStream(new ByteArrayInputStream(bytes2))) {
        final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
        // Test we can still get the number of images from IFDs if the index map is broken.
        Assertions.assertEquals(1, decoder.getNumberOfImages().getImageCount());
      }
    }

    // No entries in the index map
    bb.putInt(entryOffset, 0);
    bytes = writer.getTiff();
    try (SeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      final IndexMap map = decoder.getIndexMap();
      Assertions.assertNull(map);

      // Test we can still get the number of images from IFDs if the index map is broken.
      decoder.reset();
      Assertions.assertEquals(1, decoder.getNumberOfImages().getImageCount());
    }

    // No magic header
    bb.putInt(mapOffset, 67876979);
    bytes = writer.getTiff();
    try (SeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      final IndexMap map = decoder.getIndexMap();
      Assertions.assertNull(map);

      // Test we can still get the number of images from IFDs if the index map is broken.
      decoder.reset();
      Assertions.assertEquals(1, decoder.getNumberOfImages().getImageCount());
    }
  }

  @Test
  void testGetNumberOfImagesFromEstimate() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(9);
    final int width = 15;
    final int height = 30;
    final int offset = 12345;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(false);
    writer.beginIfd(9);
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset * 2);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(false);
    writer.beginIfd(9);
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, offset * 2);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");

      // Add the image pixels we did not write:
      final int size = width * height;
      final int actualSize = bytes.length + 3 * size;

      NumberOfImages no = decoder.getNumberOfImages(() -> actualSize);
      Assertions.assertEquals(3, no.getImageCount());
      Assertions.assertEquals(0, no.getError());
      // Smaller
      decoder.reset();
      no = decoder.getNumberOfImages(() -> actualSize - 1);
      Assertions.assertEquals(3, no.getImageCount());
      Assertions.assertNotEquals(0, no.getError());
      // Larger
      decoder.reset();
      no = decoder.getNumberOfImages(() -> actualSize + 1);
      Assertions.assertEquals(3, no.getImageCount());
      Assertions.assertNotEquals(0, no.getError());
    }
  }

  @Test
  void testReadNihHeader() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(10);
    final int width = 2;
    final int height = 3;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    final int pixelOffset = writer.getMetaDataOffset();
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, pixelOffset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    // Write pixels
    writer.addMetadata(new byte[width * height]);
    final int nihOffset = writer.getMetaDataOffset();
    // Not clear how to write this. The count is 256 but the reading of the header
    // extracts information beyond 256 bytes. So write as 512 bytes.
    writer.writeEntry(FastTiffDecoder.NIH_IMAGE_HDR, FastTiffDecoder.LONG, 256, nihOffset);
    writer.addMetadata(new byte[512]);
    writer.endIfd(true);
    final ByteBuffer bb = writer.getTiffByteBuffer();
    bb.order(ByteOrder.BIG_ENDIAN);

    // Write a good header
    // version
    bb.putShort(nihOffset + 12, (short) 153);
    bb.putDouble(nihOffset + 160, 72.0);

    byte[] bytes = writer.getTiff();
    FileInfo fi1 = readImageJ(bytes);
    FileInfo fi2 = readSs(bytes);

    Assertions.assertEquals(1.0 / 72, fi1.pixelWidth);
    Assertions.assertEquals(1.0 / 72, fi1.pixelHeight);
    Assertions.assertEquals(fi1.pixelWidth, fi2.pixelWidth);
    Assertions.assertEquals(fi1.pixelHeight, fi2.pixelHeight);

    // Units
    for (int i = 1; i <= 10; i++) {
      bb.putShort(nihOffset + 172, (short) i);
      bytes = writer.getTiff();
      fi1 = readImageJ(bytes);
      fi2 = readSs(bytes);
      Assertions.assertEquals(fi1.unit, fi2.unit);
    }

    // Density
    bb.put(nihOffset + 182, (byte) 11);
    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);
    Assertions.assertEquals(21, fi1.calibrationFunction);
    Assertions.assertEquals("U. OD", fi1.valueUnit);
    Assertions.assertEquals(21, fi2.calibrationFunction);
    Assertions.assertEquals("U. OD", fi2.valueUnit);

    // nCoefficients >= 1
    bb.putShort(nihOffset + 184, (byte) 1);
    for (int i = 0; i <= 8; i++) {
      bb.put(nihOffset + 182, (byte) i);
      bytes = writer.getTiff();
      fi1 = readImageJ(bytes);
      fi2 = readSs(bytes);
      Assertions.assertEquals(fi1.calibrationFunction, fi2.calibrationFunction);
    }

    // custom value unit
    bb.put(nihOffset + 234, (byte) 3);
    bb.put(nihOffset + 235, (byte) 'A');
    bb.put(nihOffset + 236, (byte) 'b');
    bb.put(nihOffset + 237, (byte) '1');
    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);
    Assertions.assertEquals("Ab1", fi1.valueUnit);
    Assertions.assertEquals(fi1.valueUnit, fi2.valueUnit);

    // Multi-image
    bb.putShort(nihOffset + 260, (short) 3);
    bb.putFloat(nihOffset + 262, 1.5f);
    bb.putFloat(nihOffset + 268, 4f);
    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);
    Assertions.assertEquals(3, fi1.nImages);
    Assertions.assertEquals(1.5f, fi1.pixelDepth);
    Assertions.assertEquals(4f, fi1.frameInterval);
    Assertions.assertEquals(3, fi2.nImages);
    Assertions.assertEquals(1.5f, fi2.pixelDepth);
    Assertions.assertEquals(4f, fi2.frameInterval);

    // Aspect ratio
    bb.putFloat(nihOffset + 272, 2f);
    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);
    Assertions.assertEquals(fi1.pixelWidth / 2f, fi1.pixelHeight);
    Assertions.assertEquals(fi1.pixelHeight, fi2.pixelHeight);
  }

  @Test
  void testReadImageJExtraMetadata() throws IOException {
    Assertions.assertTrue(FastTiffDecoder.empty(null));
    Assertions.assertTrue(FastTiffDecoder.empty(new int[0]));

    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(11);
    final int width = 2;
    final int height = 3;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    final int pixelOffset = writer.getMetaDataOffset();
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, pixelOffset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    // Write pixels
    writer.addMetadata(new byte[width * height]);
    final int countOffset = writer.getMetaDataOffset();

    // Write random metadata
    final SplitMix rng = SplitMix.new64(12345);
    final byte[] meta1 = randomBytes(rng, 10);
    final byte[] meta2 = randomBytes(rng, 15);

    // The meta data field is a long array of bytes.
    // The byte counts contain the length of items in the metadata field as 4-byte ints.
    // The start of the metadata field is a header containing a magic number then
    // the type and number of items of that type in the metadata. The number is used as an index
    // into meta data byte counts to get the number of bytes to extract.

    // Total number of counts = 3 (header + 2 fields)
    writer.writeEntry(FastTiffDecoder.META_DATA_BYTE_COUNTS, FastTiffDecoder.LONG, 3, countOffset);
    writer.addMetadata(new byte[12]);

    // Create the metadata
    final ByteBuffer metadata = ByteBuffer.allocate(100).order(ByteOrder.LITTLE_ENDIAN);
    // Header: magic number
    metadata.putInt(0x494a494a);
    // Fields have a type and then the number to read from the counts,
    // each count stores a length of bytes
    metadata.putInt(42);
    metadata.putInt(1);
    metadata.putInt(0xffffff);
    metadata.putInt(1);
    // 2 fields
    metadata.put(meta1);
    metadata.put(meta2);

    final int metaOffset = writer.getMetaDataOffset();
    writer.writeEntry(FastTiffDecoder.META_DATA, FastTiffDecoder.LONG, metadata.position(),
        metaOffset);
    writer.addMetadata(metadata);
    writer.endIfd(true);

    final ByteBuffer bb = writer.getTiffByteBuffer();

    // Write the metadata counts.
    // Header size
    bb.putInt(countOffset, 20);
    // For the fields this number of length to read
    bb.putInt(countOffset + 4, meta1.length);
    bb.putInt(countOffset + 8, meta2.length);

    byte[] bytes = writer.getTiff();
    FileInfo fi1 = readImageJ(bytes);
    FileInfo fi2 = readSs(bytes);

    Assertions.assertNotNull(fi1.metaDataTypes);
    Assertions.assertNotNull(fi1.metaData);
    Assertions.assertNotNull(fi2.metaDataTypes);
    Assertions.assertNotNull(fi2.metaData);

    // Should read the metadata with a type below 0xffffff, i.e. meta1
    Assertions.assertEquals(1, fi1.metaDataTypes.length);
    Assertions.assertEquals(1, fi1.metaData.length);
    Assertions.assertEquals(1, fi2.metaDataTypes.length);
    Assertions.assertEquals(1, fi2.metaData.length);

    Assertions.assertEquals(42, fi1.metaDataTypes[0]);
    Assertions.assertArrayEquals(meta1, fi1.metaData[0]);
    Assertions.assertEquals(42, fi2.metaDataTypes[0]);
    Assertions.assertArrayEquals(meta1, fi2.metaData[0]);

    // No magic number
    bb.putInt(metaOffset, 99);

    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);

    Assertions.assertNull(fi1.metaDataTypes);
    Assertions.assertNull(fi1.metaData);
    Assertions.assertNull(fi2.metaDataTypes);
    Assertions.assertNull(fi2.metaData);

    // Write size too small/big
    for (final int hdrSize : new int[] {11, 1000}) {
      bb.putInt(countOffset, hdrSize);

      bytes = writer.getTiff();
      fi1 = readImageJ(bytes);
      fi2 = readSs(bytes);

      Assertions.assertNull(fi1.metaDataTypes);
      Assertions.assertNull(fi1.metaData);
      Assertions.assertNull(fi2.metaDataTypes);
      Assertions.assertNull(fi2.metaData);
    }
  }

  @Test
  void testReadImageJExtraMetadataWithNoCounts() throws IOException {
    Assertions.assertTrue(FastTiffDecoder.empty(null));
    Assertions.assertTrue(FastTiffDecoder.empty(new int[0]));

    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(10);
    final int width = 2;
    final int height = 3;
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    final int pixelOffset = writer.getMetaDataOffset();
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1, pixelOffset);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 1, width * height);
    // Write pixels
    writer.addMetadata(new byte[width * height]);

    // Write metadata. This does not matter as there are no counts.
    final int metaOffset = writer.getMetaDataOffset();
    writer.writeEntry(FastTiffDecoder.META_DATA, FastTiffDecoder.LONG, writer.getMetaDataOffset(),
        metaOffset);
    writer.addMetadata(new byte[10]);
    writer.endIfd(true);

    final byte[] bytes = writer.getTiff();
    final FileInfo fi1 = readImageJ(bytes);
    final FileInfo fi2 = readSs(bytes);

    Assertions.assertNull(fi1.metaDataTypes);
    Assertions.assertNull(fi1.metaData);
    Assertions.assertNull(fi2.metaDataTypes);
    Assertions.assertNull(fi2.metaData);
  }

  @Test
  void testReadStripOffsetsAndCounts() throws IOException {
    DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(9);
    final int width = 2;
    final int height = 3;
    final int stripSize = width * height / 2;
    // Write pixels in two strips
    final int offset1 = writer.getMetaDataOffset();
    writer.addMetadata(new byte[stripSize]);
    final int offset2 = writer.getMetaDataOffset();
    writer.addMetadata(new byte[stripSize]);
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);

    // Write offsets
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 2,
        writer.getMetaDataOffset());
    final ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    bb.putInt(offset1);
    bb.putInt(offset2);
    writer.addMetadata(bb);

    // Write byte counts
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.LONG, 2,
        writer.getMetaDataOffset());
    bb.rewind();
    bb.putInt(stripSize);
    bb.putInt(stripSize);
    writer.addMetadata(bb);
    writer.endIfd(true);

    byte[] bytes = writer.getTiff();
    FileInfo fi1 = readImageJ(bytes);
    FileInfo fi2 = readSs(bytes);

    Assertions.assertEquals(offset1, fi1.offset);
    Assertions.assertEquals(offset1, fi1.stripOffsets[0]);
    Assertions.assertEquals(offset2, fi1.stripOffsets[1]);
    Assertions.assertEquals(stripSize, fi1.stripLengths[1]);
    Assertions.assertEquals(stripSize, fi1.stripLengths[1]);

    Assertions.assertEquals(fi1.offset, fi2.offset);
    Assertions.assertArrayEquals(fi1.stripOffsets, fi2.stripOffsets);
    Assertions.assertArrayEquals(fi1.stripLengths, fi2.stripLengths);

    // Write again using short strip byte counts and reverse the order of the offsets

    writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(9);
    // Write pixels in two strips
    writer.addMetadata(new byte[stripSize]);
    writer.addMetadata(new byte[stripSize]);
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);

    // Write offsets
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 2,
        writer.getMetaDataOffset());
    bb.rewind();
    bb.putInt(offset2);
    bb.putInt(offset1);
    writer.addMetadata(bb);

    // Write byte counts
    writer.writeEntry(FastTiffDecoder.STRIP_BYTE_COUNT, FastTiffDecoder.SHORT, 2,
        writer.getMetaDataOffset());
    bb.rewind();
    bb.putShort((short) stripSize);
    bb.putShort((short) stripSize);
    writer.addMetadata(bb);
    writer.endIfd(true);

    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);

    Assertions.assertEquals(offset1, fi1.offset);
    Assertions.assertEquals(offset2, fi1.stripOffsets[0]);
    Assertions.assertEquals(offset1, fi1.stripOffsets[1]);
    Assertions.assertEquals(stripSize, fi1.stripLengths[1]);
    Assertions.assertEquals(stripSize, fi1.stripLengths[1]);

    Assertions.assertEquals(fi1.offset, fi2.offset);
    Assertions.assertArrayEquals(fi1.stripOffsets, fi2.stripOffsets);
    Assertions.assertArrayEquals(fi1.stripLengths, fi2.stripLengths);

    // Write with no count
    writer = new DummyTiffWriter(true);
    // Write a valid tiff first.
    writer.beginIfd(8);
    // Write pixels in one strips
    writer.addMetadata(new byte[stripSize * 2]);
    writer.writeEntry(FastTiffDecoder.NEW_SUBFILE_TYPE, FastTiffDecoder.LONG, 1, 0);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.LONG, 1, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.LONG, 1, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 1);
    writer.writeEntry(FastTiffDecoder.ROWS_PER_STRIP, FastTiffDecoder.SHORT, 1, height);

    // Write offset
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 0, offset1);
    writer.endIfd(true);

    bytes = writer.getTiff();
    fi1 = readImageJ(bytes);
    fi2 = readSs(bytes);

    Assertions.assertEquals(offset1, fi1.offset);
    Assertions.assertEquals(0, fi1.stripOffsets.length);
    Assertions.assertNull(fi1.stripLengths);

    Assertions.assertEquals(fi1.offset, fi2.offset);
    Assertions.assertArrayEquals(fi1.stripOffsets, fi2.stripOffsets);
    Assertions.assertNull(fi2.stripLengths);
  }

  @Test
  void testPhotoInterpAndBitsPerSample() throws IOException {
    int count = 1;
    int[] values = {8, 16, 32, 12, 1};
    int[] expected = {FileInfo.GRAY8, FileInfo.GRAY16_UNSIGNED, FileInfo.GRAY32_INT,
        FileInfo.GRAY12_UNSIGNED, FileInfo.BITMAP};
    for (int i = 0; i < values.length; i++) {
      final int value = values[i];
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(2);
      writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 0, Math.min(i, 1));
      writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, count, value);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      final FileInfo fi1 = readImageJ(bytes);
      final FileInfo fi2 = readSs(bytes);
      Assertions.assertEquals(fi1.whiteIsZero, fi2.whiteIsZero);
      Assertions.assertEquals(expected[i], fi1.fileType, () -> "value " + value);
      Assertions.assertEquals(expected[i], fi2.fileType, () -> "value " + value);
    }

    // Test others throw
    DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a dummy tiff.
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, count, 5);
    writer.endIfd(true);

    final byte[] badBytes = writer.getTiff();
    Assertions.assertThrows(IOException.class, () -> readImageJ(badBytes));
    Assertions.assertThrows(IOException.class, () -> readSs(badBytes));

    // Test reading from an offset
    count = 3;
    values = new int[] {8, 16};
    expected = new int[] {FileInfo.GRAY8, FileInfo.GRAY16_UNSIGNED};
    for (int i = 0; i < values.length; i++) {
      final short value = (short) values[i];
      writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(1);
      writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, count,
          writer.getMetaDataOffset());
      final ByteBuffer bb = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN);
      bb.putShort(value);
      bb.putShort(value);
      bb.putShort(value);
      writer.addMetadata(bb);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      final FileInfo fi1 = readImageJ(bytes);
      final FileInfo fi2 = readSs(bytes);
      Assertions.assertEquals(expected[i], fi1.fileType, () -> "value " + value);
      Assertions.assertEquals(expected[i], fi2.fileType, () -> "value " + value);
    }

    // Test others throw
    writer = new DummyTiffWriter(true);
    // Write a dummy tiff.
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, count, 5);
    writer.endIfd(true);

    final byte[] badBytes2 = writer.getTiff();
    Assertions.assertThrows(IOException.class, () -> readImageJ(badBytes2));
    Assertions.assertThrows(IOException.class, () -> readSs(badBytes2));
  }

  @Test
  void testSamplesPerPixel() throws IOException {
    final int[] photo = {0, 0, 0, 5, 0, 5, 0, 0};
    final int[] bits = {8, 16, 8, 8, 16, 16, 32, 32};
    final int[] samples = {3, 3, 4, 4, 4, 4, 3, 4};
    final int[] expected = {FileInfo.RGB, FileInfo.RGB48, FileInfo.ARGB, FileInfo.CMYK,
        FileInfo.RGB48, FileInfo.RGB48, FileInfo.GRAY32_INT, FileInfo.GRAY32_INT};
    for (int i = 0; i < bits.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(3);
      writer.writeEntry(FastTiffDecoder.PHOTO_INTERP, FastTiffDecoder.SHORT, 0, photo[i]);
      writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, bits[i]);
      writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, samples[i]);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      final FileInfo fi1 = readImageJ(bytes);
      final FileInfo fi2 = readSs(bytes);
      Assertions.assertEquals(fi1.whiteIsZero, fi2.whiteIsZero);
      Assertions.assertEquals(expected[i], fi1.fileType);
      Assertions.assertEquals(expected[i], fi2.fileType);
    }
  }

  @Test
  void testPlanar() throws IOException {
    final int[] bits = {16, 8, 16, 8, 8, 8, 8};
    final int[] samples = {3, 3, 3, 3, 1, 1, 4};
    final int[] planar = {2, 2, 1, 1, 2, 1, 1};
    final int[] expected = {FileInfo.RGB48_PLANAR, FileInfo.RGB_PLANAR, FileInfo.RGB48,
        FileInfo.RGB, FileInfo.GRAY8, FileInfo.GRAY8, FileInfo.ARGB};
    for (int i = 0; i < bits.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(3);
      writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, bits[i]);
      writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, samples[i]);
      writer.writeEntry(FastTiffDecoder.PLANAR_CONFIGURATION, FastTiffDecoder.SHORT, 1, planar[i]);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      final FileInfo fi1 = readImageJ(bytes);
      final FileInfo fi2 = readSs(bytes);
      Assertions.assertEquals(expected[i], fi1.fileType);
      Assertions.assertEquals(expected[i], fi2.fileType);
    }
    // This should throw
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a dummy tiff.
    writer.beginIfd(3);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, 2);
    writer.writeEntry(FastTiffDecoder.PLANAR_CONFIGURATION, FastTiffDecoder.SHORT, 1, 1);
    writer.endIfd(true);

    final byte[] badBytes = writer.getTiff();
    Assertions.assertThrows(IOException.class, () -> readImageJ(badBytes));
    Assertions.assertThrows(IOException.class, () -> readSs(badBytes));
  }

  @Test
  void testCompression() throws IOException {
    int[] values = {5, 32773, 32946, 8, 0, 1, 7, 5, 0};
    final int[] predictor = {0, 0, 0, 0, 0, 0, 0, 2, 2};
    final int[] expected = {FileInfo.LZW, FileInfo.PACK_BITS, FileInfo.ZIP, FileInfo.ZIP,
        FileInfo.COMPRESSION_NONE, FileInfo.COMPRESSION_NONE, FileInfo.COMPRESSION_NONE,
        FileInfo.LZW_WITH_DIFFERENCING, FileInfo.COMPRESSION_NONE};
    for (int i = 0; i < values.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(2);
      writer.writeEntry(FastTiffDecoder.COMPRESSION, FastTiffDecoder.SHORT, 1, values[i]);
      writer.writeEntry(FastTiffDecoder.PREDICTOR, FastTiffDecoder.SHORT, 1, predictor[i]);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      final FileInfo fi1 = readImageJ(bytes);
      final FileInfo fi2 = readSs(bytes);
      Assertions.assertEquals(expected[i], fi1.compression);
      Assertions.assertEquals(expected[i], fi2.compression);
    }
    // This should throw
    final int[] bits = {12, 8, 8};
    values = new int[] {5, 2, 7};
    for (int i = 0; i < values.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(3);
      // Make the width over 500 to trigger exception for Spot camera compressed (7) thumbnails
      writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 1, 500);
      writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, bits[i]);
      writer.writeEntry(FastTiffDecoder.COMPRESSION, FastTiffDecoder.SHORT, 1, values[i]);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      Assertions.assertThrows(IOException.class, () -> readImageJ(bytes));
      Assertions.assertThrows(IOException.class, () -> readSs(bytes));
    }
  }

  @Test
  void testMetadata() throws IOException {
    DummyTiffWriter writer = new DummyTiffWriter(true);
    final String software = "This program";
    final String dateTime = "2001-01-01";
    final String hostComputer = "localhost";
    final String artest = "Something";
    final byte[] b1 = getBytes(software);
    final byte[] b2 = getBytes(dateTime);
    final byte[] b3 = getBytes(hostComputer);
    final byte[] b4 = getBytes(artest);
    // Write a dummy tiff.
    writer.beginIfd(4);
    writer.writeEntry(FastTiffDecoder.SOFTWARE, FastTiffDecoder.SHORT, b1.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b1);
    writer.writeEntry(FastTiffDecoder.DATE_TIME, FastTiffDecoder.SHORT, b2.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b2);
    writer.writeEntry(FastTiffDecoder.HOST_COMPUTER, FastTiffDecoder.SHORT, b3.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b3);
    writer.writeEntry(FastTiffDecoder.ARTIST, FastTiffDecoder.SHORT, b4.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b4);
    writer.endIfd(true);

    byte[] bytes = writer.getTiff();
    final FileInfo fi1 = readImageJ(bytes);
    final FileInfo fi2 = readSs(bytes);
    Assertions.assertTrue(fi1.info.contains(software));
    Assertions.assertTrue(fi1.info.contains(dateTime));
    Assertions.assertTrue(fi1.info.contains(hostComputer));
    Assertions.assertTrue(fi1.info.contains(artest));
    Assertions.assertEquals(fi1.info, fi2.info);

    writer = new DummyTiffWriter(true);
    // Write a dummy tiff with 2 IFDs.
    // Short strings and those in IFD 2 should be ignored.
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.SOFTWARE, FastTiffDecoder.SHORT, 1,
        writer.getMetaDataOffset());
    writer.addMetadata(new byte[1]);
    writer.endIfd(false);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.ARTIST, FastTiffDecoder.SHORT, b4.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b4);
    writer.endIfd(true);

    bytes = writer.getTiff();
    final FileInfo[] info1 = readAllImageJ(bytes);
    final FileInfo[] info2 = readAllSs(bytes);
    for (int i = 0; i < 2; i++) {
      Assertions.assertNull(info1[i].info);
      Assertions.assertNull(info2[i].info);
    }
  }

  @Test
  void testTileWidth() {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.TILE_WIDTH, FastTiffDecoder.SHORT, 1, 500);
    writer.endIfd(true);

    final byte[] bytes = writer.getTiff();
    Assertions.assertThrows(IOException.class, () -> readImageJ(bytes));
    Assertions.assertThrows(IOException.class, () -> readSs(bytes));
  }

  @Test
  void testSampleFormat() throws IOException {
    final int[] bits = {32, 32, 16, 16};
    final int[] format = {FastTiffDecoder.FLOATING_POINT, 0, FastTiffDecoder.SIGNED, 0};
    final int[] expected = {FileInfo.GRAY32_FLOAT, FileInfo.GRAY32_INT, FileInfo.GRAY16_SIGNED,
        FileInfo.GRAY16_UNSIGNED};
    for (int i = 0; i < bits.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      writer.beginIfd(2);
      writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, bits[i]);
      writer.writeEntry(FastTiffDecoder.SAMPLE_FORMAT, FastTiffDecoder.SHORT, 1, format[i]);
      writer.endIfd(true);

      final byte[] bytes = writer.getTiff();
      final FileInfo fi1 = readImageJ(bytes);
      final FileInfo fi2 = readSs(bytes);
      Assertions.assertEquals(expected[i], fi1.fileType);
      Assertions.assertEquals(expected[i], fi2.fileType);
    }
    // This should throw
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a dummy tiff.
    writer.beginIfd(2);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 16);
    writer.writeEntry(FastTiffDecoder.SAMPLE_FORMAT, FastTiffDecoder.SHORT, 1,
        FastTiffDecoder.FLOATING_POINT);
    writer.endIfd(true);

    final byte[] badBytes = writer.getTiff();
    Assertions.assertThrows(IOException.class, () -> readImageJ(badBytes));
    Assertions.assertThrows(IOException.class, () -> readSs(badBytes));
  }

  @Test
  void testImageDescription() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    final String s1 = "Should be read";
    final String s2 = "Should be ignored";
    final byte[] b1 = getBytes(s1);
    final byte[] b2 = getBytes(s2);
    // Write a dummy tiff.
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.IMAGE_DESCRIPTION, FastTiffDecoder.SHORT, b1.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b1);
    writer.endIfd(false);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.IMAGE_DESCRIPTION, FastTiffDecoder.SHORT, b2.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b2);
    writer.endIfd(true);

    final byte[] bytes = writer.getTiff();
    final FileInfo[] info1 = readAllImageJ(bytes);
    final FileInfo[] info2 = readAllSs(bytes);
    for (int i = 0; i < 2; i++) {
      final FileInfo fi1 = info1[i];
      final FileInfo fi2 = info2[i];
      if (i == 0) {
        Assertions.assertNotNull(fi1.info);
        Assertions.assertTrue(fi1.info.contains(s1));
        Assertions.assertEquals(fi1.info, fi2.info);
      } else {
        Assertions.assertNull(fi1.info);
        Assertions.assertNull(fi2.info);
      }
    }
  }

  @Test
  void testOrientation() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    // Write a dummy tiff with the orientataion tag
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.ORIENTATION, FastTiffDecoder.SHORT, 1, 1);
    writer.endIfd(false);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.ORIENTATION, FastTiffDecoder.SHORT, 1, 1);
    writer.endIfd(true);

    // Expect to have read all IFDs
    final byte[] bytes = writer.getTiff();
    final FileInfo[] info1 = readAllImageJ(bytes);
    final FileInfo[] info2 = readAllSs(bytes);
    Assertions.assertEquals(2, info1.length);
    Assertions.assertEquals(2, info2.length);
  }

  @Test
  void testMetamorph() throws IOException {
    DummyTiffWriter writer = new DummyTiffWriter(true);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.METAMORPH1, FastTiffDecoder.SHORT, 1, 1);
    writer.endIfd(false);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.METAMORPH1, FastTiffDecoder.SHORT, 1, 1);
    writer.endIfd(true);

    // Expect to have nImages = 9999
    byte[] bytes = writer.getTiff();
    FileInfo[] info1 = readAllImageJ(bytes, "test.stk");
    FileInfo[] info2 = readAllSs(bytes, "test.stk");
    Assertions.assertEquals(1, info1.length);
    Assertions.assertEquals(1, info2.length);
    Assertions.assertEquals(9999, info1[0].nImages);
    Assertions.assertEquals(9999, info2[0].nImages);

    info1 = readAllImageJ(bytes, "test.STK");
    info2 = readAllSs(bytes, "test.STK");
    Assertions.assertEquals(1, info1.length);
    Assertions.assertEquals(1, info2.length);
    Assertions.assertEquals(9999, info1[0].nImages);
    Assertions.assertEquals(9999, info2[0].nImages);

    // Read all the IFDs
    info1 = readAllImageJ(bytes, "test.tif");
    info2 = readAllSs(bytes, "test.tif");
    Assertions.assertEquals(2, info1.length);
    Assertions.assertEquals(2, info2.length);

    writer = new DummyTiffWriter(true);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.METAMORPH2, FastTiffDecoder.LONG, 42, 11);
    writer.endIfd(true);

    // METAMORPH2 has the number of images in the tag count
    bytes = writer.getTiff();
    info1 = readAllImageJ(bytes, "test.stk");
    info2 = readAllSs(bytes, "test.stk");
    Assertions.assertEquals(1, info1.length);
    Assertions.assertEquals(1, info2.length);
    Assertions.assertEquals(42, info1[0].nImages);
    Assertions.assertEquals(42, info2[0].nImages);
  }

  @Test
  void testIplab() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.IPLAB, FastTiffDecoder.SHORT, 11, 42);
    writer.endIfd(true);

    // IPLAB has the number of images in the tag value
    final byte[] bytes = writer.getTiff();
    final FileInfo[] info1 = readAllImageJ(bytes);
    final FileInfo[] info2 = readAllSs(bytes);
    Assertions.assertEquals(1, info1.length);
    Assertions.assertEquals(1, info2.length);
    Assertions.assertEquals(42, info1[0].nImages);
    Assertions.assertEquals(42, info2[0].nImages);
  }

  @Test
  void testMicroManagerMetaData() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    final String s1 = "data1";
    final String s2 = "data2";
    final byte[] b1 = s1.getBytes(StandardCharsets.UTF_8);
    final byte[] b2 = s2.getBytes(StandardCharsets.UTF_8);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.MICRO_MANAGER_META_DATA, FastTiffDecoder.SHORT, b1.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b1);
    writer.endIfd(false);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.MICRO_MANAGER_META_DATA, FastTiffDecoder.SHORT, b2.length,
        writer.getMetaDataOffset());
    writer.addMetadata(b2);
    writer.endIfd(true);

    // By default this is done for only the first IFD
    final byte[] bytes = writer.getTiff();
    final ExtendedFileInfo[] info = readAllSs(bytes);
    Assertions.assertEquals(2, info.length);
    Assertions.assertEquals(s1, info[0].getExtendedMetaData());
    Assertions.assertNull(info[1].getExtendedMetaData());
  }

  @Test
  void testBadTagInIfd() throws IOException {
    final int[] tags = {10000, 10001, 32768};
    final int[] lengths = {2, 1, 2};
    for (int i = 0; i < tags.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      writer.beginIfd(1);
      writer.writeEntry(tags[i], FastTiffDecoder.SHORT, 1, 1);
      writer.endIfd(false);
      writer.beginIfd(1);
      writer.writeEntry(tags[i], FastTiffDecoder.SHORT, 1, 1);
      writer.endIfd(true);

      // Expect to read only 1 IFD if the tag is bad
      final byte[] bytes = writer.getTiff();
      final FileInfo[] info1 = readAllImageJ(bytes);
      final FileInfo[] info2 = readAllSs(bytes);
      Assertions.assertEquals(lengths[i], info1.length);
      Assertions.assertEquals(lengths[i], info2.length);
    }
  }

  @Test
  void testGetNumberOfImagesNonImageJ() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    writer.beginIfd(3);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, 10);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.SHORT, 0, 15);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.endIfd(false);
    writer.beginIfd(3);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, 10);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.SHORT, 0, 15);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    Assertions.assertEquals(2, readImageCount(bytes));

    // Truncate first IFD. Leave all but the IFD entry count.
    Assertions.assertEquals(0, readImageCount(Arrays.copyOf(bytes, 12)));

    // Truncate final IFD. Remove the 4 byte offset and then make the IFD entries too small.
    // This throws as the IFD is skipped and the stream is at the end. Thus reading the
    // third IFD offset throws EOF.
    final byte[] truncated = Arrays.copyOf(bytes, bytes.length - 5);
    Assertions.assertThrows(EOFException.class, () -> readImageCount(truncated));

    // Same edge case for reading with a file size estimate.
    // This does not throw as the second IFD is actually read and this detects
    // truncation. The third IFD offset is never read.
    Assertions.assertEquals(1, readImageCount(truncated, truncated.length));

    // Test scan with an empty IFD or one with too many entries
    final ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    final int entryCountIndex = 8 + 2 + 3 * 12 + 4;
    Assertions.assertEquals(3, bb.getShort(entryCountIndex));
    // Too few IFD entries
    bb.putShort(entryCountIndex, (short) 0);
    Assertions.assertEquals(1, readImageCount(bytes));
    // Same edge case for reading with a file size estimate
    Assertions.assertEquals(1, readImageCount(bytes, bytes.length));

    // Too many IFD entries
    bb.putShort(entryCountIndex, (short) 1001);
    Assertions.assertEquals(1, readImageCount(bytes));
    // Same edge case for reading with a file size estimate
    Assertions.assertEquals(1, readImageCount(bytes, bytes.length));
  }

  @Test
  void testGetNumberOfImagesNonImageJWithExtraIfdData() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    final int width = 10;
    final int height = 15;
    final byte[] pixels = new byte[width * height];
    writer.beginIfd(8);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, width);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.SHORT, 0, height);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1,
        writer.getMetaDataOffset());
    writer.addMetadata(pixels);
    writer.writeEntry(FastTiffDecoder.IMAGE_DESCRIPTION, FastTiffDecoder.BYTE, 10,
        writer.getMetaDataOffset());
    writer.addMetadata(new byte[10]);
    writer.writeEntry(FastTiffDecoder.X_RESOLUTION, FastTiffDecoder.RATIONAL, 1,
        writer.getMetaDataOffset());
    writer.addMetadata(new byte[8]);

    // Metadata counts
    final byte[] meta1 = new byte[3];
    final byte[] meta2 = new byte[4];
    final ByteBuffer bb = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
    // Header size
    bb.putInt(20);
    // length of metadata fields
    bb.putInt(meta1.length);
    bb.putInt(meta2.length);
    writer.writeEntry(FastTiffDecoder.META_DATA_BYTE_COUNTS, FastTiffDecoder.LONG, 3,
        writer.getMetaDataOffset());
    writer.addMetadata(bb);

    // Add the actual metadata
    final ByteBuffer metadata = ByteBuffer.allocate(100).order(ByteOrder.LITTLE_ENDIAN);
    // Header: magic number
    metadata.putInt(0x494a494a);
    // Fields have a type and then the number to read from the counts,
    // each count stores a length of bytes
    metadata.putInt(42);
    metadata.putInt(1);
    metadata.putInt(0xffffff);
    metadata.putInt(1);
    // 2 fields
    metadata.put(meta1);
    metadata.put(meta2);
    writer.writeEntry(FastTiffDecoder.META_DATA, FastTiffDecoder.LONG, 3,
        writer.getMetaDataOffset());
    writer.addMetadata(metadata);
    writer.endIfd(false);
    writer.beginIfd(4);
    writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, 10);
    writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.SHORT, 0, 15);
    writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, 8);
    writer.writeEntry(FastTiffDecoder.STRIP_OFFSETS, FastTiffDecoder.LONG, 1,
        writer.getMetaDataOffset());
    writer.addMetadata(pixels);
    writer.endIfd(true);
    final byte[] bytes = writer.getTiff();
    Assertions.assertEquals(2, readImageCount(bytes));

    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      final NumberOfImages no = decoder.getNumberOfImages(() -> bytes.length);
      Assertions.assertEquals(2, no.getImageCount());
    }
  }

  @Test
  void testGetPixelSize() throws IOException {
    // Create two IFDs with a set pixel type.
    // Then estimate the number of images with an imaginary file size.
    final int width = 10;
    final int height = 15;
    final int[] bits = {16, 8, 16, 8, 8, 8, 8, 32, 12, 1, 8, 16, 32, 16, 32, 16};
    final int[] bitsCount = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1};
    final int[] samples = {3, 3, 3, 3, 1, 1, 4, 1, 1, 1, 1, 1, 3, 4, 1, 1};
    final int[] planar = {2, 2, 1, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    final int[] format = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, FastTiffDecoder.FLOATING_POINT,
        FastTiffDecoder.SIGNED};
    final int[] expected = {FileInfo.RGB48_PLANAR, FileInfo.RGB_PLANAR, FileInfo.RGB48,
        FileInfo.RGB, FileInfo.GRAY8, FileInfo.GRAY8, FileInfo.ARGB, FileInfo.GRAY32_INT,
        FileInfo.GRAY12_UNSIGNED, FileInfo.BITMAP, FileInfo.GRAY8, FileInfo.GRAY16_UNSIGNED,
        FileInfo.GRAY32_INT, FileInfo.RGB48, FileInfo.GRAY32_FLOAT, FileInfo.GRAY16_SIGNED};
    for (int i = 0; i < bits.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      final int entries = 6;
      int extra = 0;
      for (int j = 0; j < 2; j++) {
        writer.beginIfd(entries);
        writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, width);
        writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.SHORT, 0, height);
        if (bitsCount[i] != 1) {
          writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, bitsCount[i],
              writer.getMetaDataOffset());
          final ByteBuffer bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
          bb.putShort((short) bits[i]);
          writer.addMetadata(bb);
          extra += 2;
        } else {
          writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, bits[i]);
        }
        writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, samples[i]);
        writer.writeEntry(FastTiffDecoder.SAMPLE_FORMAT, FastTiffDecoder.SHORT, 1, format[i]);
        writer.writeEntry(FastTiffDecoder.PLANAR_CONFIGURATION, FastTiffDecoder.SHORT, 1,
            planar[i]);
        writer.endIfd(j == 1);
      }

      final byte[] bytes = writer.getTiff();

      final int headerSize = 8;
      final int size = FastTiffDecoder.getBytesPerImage(width, height, expected[i]);
      final int ifdSize = 2 + entries * 12 + 4 + extra;

      for (final int images : new int[] {2, 10, 50}) {
        final long fileSize = headerSize + images * (size + ifdSize);
        Assertions.assertEquals(images, readImageCount(bytes, fileSize));
      }
    }
  }

  @Test
  void testGetPixelSizeThrows() {
    // Create 2 IFDs with a pixel type that is not supported.
    final long dummyEstimatedSize = 999;
    final int height = 15;
    final int[] width = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 0};
    final int[] bits = {11, 11, 8, 16, 8, 12, 8, 8, 8, 8, 8};
    final int[] bitsCount = {1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    final int[] samples = {1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1};
    final int[] planar = {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1};
    final int[] format = {0, 0, 0, FastTiffDecoder.FLOATING_POINT, 0, 0, 0, 0, 0, 0, 0};
    final int[] compress = {0, 0, 0, 0, 5, 5, 32773, 32946, 8, 3, 0};
    for (int i = 0; i < bits.length; i++) {
      final DummyTiffWriter writer = new DummyTiffWriter(true);
      // Write a dummy tiff.
      final int entries = 7;
      for (int j = 0; j < 2; j++) {
        writer.beginIfd(entries);
        writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, width[i]);
        writer.writeEntry(FastTiffDecoder.IMAGE_LENGTH, FastTiffDecoder.SHORT, 0, height);
        if (bitsCount[i] != 1) {
          writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, bitsCount[i],
              writer.getMetaDataOffset());
          final ByteBuffer bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
          bb.putShort((short) bits[i]);
          writer.addMetadata(bb);
        } else {
          writer.writeEntry(FastTiffDecoder.BITS_PER_SAMPLE, FastTiffDecoder.SHORT, 1, bits[i]);
        }
        writer.writeEntry(FastTiffDecoder.SAMPLES_PER_PIXEL, FastTiffDecoder.SHORT, 1, samples[i]);
        writer.writeEntry(FastTiffDecoder.SAMPLE_FORMAT, FastTiffDecoder.SHORT, 1, format[i]);
        writer.writeEntry(FastTiffDecoder.PLANAR_CONFIGURATION, FastTiffDecoder.SHORT, 1,
            planar[i]);
        writer.writeEntry(FastTiffDecoder.COMPRESSION, FastTiffDecoder.LONG, 1, compress[i]);
        writer.endIfd(j == 1);
      }

      final byte[] bytes = writer.getTiff();
      Assertions.assertThrows(IOException.class, () -> readImageCount(bytes, dummyEstimatedSize));
    }

    // Test throws with TILE_WIDTH
    final DummyTiffWriter writer = new DummyTiffWriter(true);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.TILE_WIDTH, FastTiffDecoder.SHORT, 0, 10);
    writer.endIfd(false);
    writer.beginIfd(1);
    writer.writeEntry(FastTiffDecoder.TILE_WIDTH, FastTiffDecoder.SHORT, 0, 10);
    writer.endIfd(true);

    final byte[] bytes = writer.getTiff();
    Assertions.assertThrows(IOException.class, () -> readImageCount(bytes, dummyEstimatedSize));
  }

  @Test
  void testTrackProgress() throws IOException {
    final DummyTiffWriter writer = new DummyTiffWriter(true, 2048);
    final int count = 64;
    for (int i = 0; i <= count; i++) {
      writer.beginIfd(1);
      writer.writeEntry(FastTiffDecoder.IMAGE_WIDTH, FastTiffDecoder.SHORT, 0, 10);
      writer.endIfd(i == count);
    }
    final byte[] bytes = writer.getTiff();
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      final AtomicInteger calls = new AtomicInteger();
      final TrackProgressAdapter progress = new TrackProgressAdapter() {
        @Override
        public void status(String format, Object... args) {
          calls.getAndIncrement();
        }
      };
      decoder.setTrackProgress(progress);
      decoder.getTiffInfo(true);
      Assertions.assertTrue(calls.get() > 0);
    }
  }

  /**
   * Gets the bytes with an extra null byte character added at the end since ImageJ expects this
   * when decoding.
   *
   * @param text the text
   * @return the bytes
   */
  private static byte[] getBytes(String text) {
    final byte[] bytes = text.getBytes();
    return Arrays.copyOf(bytes, bytes.length + 1);
  }

  private static FileInfo readImageJ(byte[] bytes) throws IOException {
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final TiffDecoder decoder = new TiffDecoder(ss, "test");
      final FileInfo[] info = decoder.getTiffInfo();
      Assertions.assertNotNull(info);
      Assertions.assertEquals(1, info.length);
      return info[0];
    }
  }

  private static ExtendedFileInfo readSs(byte[] bytes) throws IOException {
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      final ExtendedFileInfo[] info = decoder.getTiffInfo(false);
      Assertions.assertNotNull(info);
      Assertions.assertEquals(1, info.length);
      return info[0];
    }
  }

  private static FileInfo[] readAllImageJ(byte[] bytes) throws IOException {
    return readAllImageJ(bytes, "test");
  }

  private static FileInfo[] readAllImageJ(byte[] bytes, String name) throws IOException {
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final TiffDecoder decoder = new TiffDecoder(ss, name);
      final FileInfo[] info = decoder.getTiffInfo();
      Assertions.assertNotNull(info);
      return info;
    }
  }

  private static ExtendedFileInfo[] readAllSs(byte[] bytes) throws IOException {
    return readAllSs(bytes, "test");
  }

  private static ExtendedFileInfo[] readAllSs(byte[] bytes, String name) throws IOException {
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, name);
      final ExtendedFileInfo[] info = decoder.getTiffInfo(false);
      Assertions.assertNotNull(info);
      return info;
    }
  }

  private static int readImageCount(byte[] bytes) throws IOException {
    return readImageCount(bytes, 0);
  }

  private static int readImageCount(byte[] bytes, long estimate) throws IOException {
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
      return decoder.getNumberOfImages(() -> estimate).getImageCount();
    }
  }

  private static String createTmpFile() throws IOException {
    final File file = File.createTempFile("test", ".tif");
    file.deleteOnExit();
    return file.toString();
  }

  private static byte[] randomBytes(UniformRandomProvider rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);
    return bytes;
  }

  /**
   * Minimal implementation of a TIFF IFD (Image File Descriptor) writer.
   *
   * <p>An IFD consists of:
   *
   * <ul>
   *
   * <li>A count of 12-byte entries (as a 16-bit unsigned integer).
   *
   * <li>The 12-bytes entries. Entries may contain an offset to a position in the file to read more
   * data. This can be anywhere. It makes most sense to write it after the block of 12-byte entries
   * and the next IFD offset.
   *
   * <li>The offset for the next IFD (32-bit unsigned integer).
   *
   * </ul>
   *
   * <p>This implementation allows writing a variable number of IFDs. Additional metadata can be
   * written to a buffer. A call to flush() will write the count of IFDs, the
   */
  private static class DummyTiffWriter {
    final ByteBuffer bb;
    ByteArrayOutputStream meta;
    int offset;
    int entryCount;

    DummyTiffWriter(boolean littleEndian) {
      this(littleEndian, 1024);
    }

    DummyTiffWriter(boolean littleEndian, int bufferSize) {
      bb = ByteBuffer.allocate(bufferSize);
      if (littleEndian) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        // Intel
        bb.put(new byte[] {73, 73, 42, 0});
      } else {
        // Motorola
        bb.put(new byte[] {77, 77, 0, 42});
      }
      bb.putInt(8);
    }

    /**
     * Instantiates a new dummy tiff writer.
     *
     * @param littleEndian the little endian
     * @param summaryMetaData the MicroManager summary meta data
     */
    DummyTiffWriter(boolean littleEndian, String summaryMetaData) {
      // MM summary metadata is a UTF-8 encoded string beginning at position 40.
      // Position 32 has the magic number 2355492 and position 36 is the length of the UTF-8 bytes.
      int size = 1024;
      final byte[] bytes = summaryMetaData.getBytes(StandardCharsets.UTF_8);
      size += 32 + bytes.length;
      bb = ByteBuffer.allocate(size);
      if (littleEndian) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        // Intel
        bb.put(new byte[] {73, 73, 42, 0});
      } else {
        // Motorola
        bb.put(new byte[] {77, 77, 0, 42});
      }

      // Offset to first IFD
      bb.putInt(40 + bytes.length);
      // Not sure what all the 32 bytes contain in the MM OME-TIFF so leav empty
      // and skip to the position for the metadata
      bb.position(32);
      bb.putInt(2355492);
      bb.putInt(bytes.length);
      bb.put(bytes);
    }

    /**
     * Begin the IFD.
     *
     * @param count the count of IFD entries
     * @return the offset to the IFD
     */
    int beginIfd(int count) {
      // IFD count (2 bytes) + IFD entries (12 * n) + next IF offset (4 bytes)
      final int current = bb.position();
      offset = current + 2 + 12 * count + 4;
      bb.putShort((short) count);
      entryCount = count;
      return current;
    }

    /**
     * Gets the meta data offset.
     *
     * <p>Use this to determine the offset to the metadata for the IFD entry.
     *
     * @return the meta data offset
     */
    int getMetaDataOffset() {
      return offset;
    }

    /**
     * Adds the metadata. Increments the metadata offset by length.
     *
     * @param bytes the bytes
     */
    void addMetadata(byte[] bytes) {
      addMetadata(bytes, bytes.length);
    }

    /**
     * Adds the metadata. Increments the metadata offset by length.
     *
     * @param buffer the buffer
     */
    void addMetadata(ByteBuffer buffer) {
      addMetadata(toBytes(buffer));
    }

    /**
     * Adds the metadata. Increments the metadata offset by length.
     *
     * @param bytes the bytes
     * @param len the length of bytes to add
     */
    void addMetadata(byte[] bytes, int len) {
      if (meta == null) {
        meta = new ByteArrayOutputStream(len);
      }
      meta.write(bytes, 0, len);
      offset += len;
    }

    /**
     * End the IFD.
     *
     * @param last the last
     */
    void endIfd(boolean last) {
      if (entryCount != 0) {
        Assertions.fail("Not enough IFD entries. Missing: " + entryCount);
      }
      final int metaSize = meta == null ? 0 : meta.size();
      // Next IFD
      if (last) {
        bb.putInt(0);
      } else {
        bb.putInt(4 + bb.position() + metaSize);
      }
      if (metaSize != 0) {
        bb.put(meta.toByteArray());
        meta = null;
      }
    }

    /**
     * Writes one 12-byte IFD entry.
     *
     * @param tag the tag (16-bits)
     * @param fieldType the field type (16-bits)
     * @param count the count (32-bits)
     * @param value the value (32-bits)
     */
    void writeEntry(int tag, int fieldType, int count, int value) {
      if (--entryCount < 0) {
        Assertions.fail("Too many IFD entries");
      }
      bb.putShort((short) tag);
      bb.putShort((short) fieldType);
      bb.putInt(count);
      if (count == 1 && fieldType == FastTiffDecoder.SHORT) {
        bb.putShort((short) value);
        bb.putShort((short) 0);
      } else {
        bb.putInt(value); // may be an offset
      }
    }

    byte[] getTiff() {
      return toBytes(bb);
    }

    static byte[] toBytes(ByteBuffer bb) {
      final byte[] bytes = new byte[bb.position()];
      bb.flip();
      bb.get(bytes);
      return bytes;
    }

    ByteBuffer getTiffByteBuffer() {
      return bb;
    }
  }

  /**
   * Minimal implementation of the SeekableStream.
   */
  private static class DummySeekableStream extends SeekableStream {
    int pos;
    final byte[] buffer;

    DummySeekableStream(byte[] bytes) {
      this.buffer = bytes;
    }

    @Override
    public long getFilePointer() {
      return pos;
    }

    @Override
    public int read() {
      if (pos == buffer.length) {
        return -1;
      }
      return buffer[pos++] & 0xff;
    }

    @Override
    public int read(byte[] bytes, int off, int len) {
      if (pos == bytes.length) {
        return -1;
      }
      final int length = Math.min(buffer.length - pos, len);
      System.arraycopy(buffer, pos, bytes, off, length);
      return length;
    }

    @Override
    public void seek(long loc) {
      pos = uk.ac.sussex.gdsc.core.utils.MathUtils.clip(0, buffer.length, (int) loc);
    }

    @Override
    public void close() {}
  }
}
