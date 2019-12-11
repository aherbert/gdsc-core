package uk.ac.sussex.gdsc.core.ij.io;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.RoiDecoder;
import ij.io.TiffDecoder;
import ij.process.ByteProcessor;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This tests reading image metadata using the the {@link FastTiffDecoder} matches the metadata read
 * by the ImageJ {@link TiffDecoder}.
 */
@SuppressWarnings({"javadoc"})
public class FastTiffDecoderTest {

  @SeededTest
  public void canGetOrigin(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());

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
  public void canGetOriginSkipsBadPatterns() {
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
  public void testCreateTiffDecoder() throws IOException {
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

    // With an input stream
    try (SeekableStream ss = new DummySeekableStream(new byte[] {ii, ii, magic, zero})) {
      FastTiffDecoder.create((InputStream) ss, "test");
    }
  }

  private static FastTiffDecoder createWithBytes(byte... bytes) throws IOException {
    return FastTiffDecoder.create(ByteArraySeekableStream.wrap(bytes), "test");
  }

  @Test
  public void testSeek() throws IOException {
    final byte[] bytes = {73, 73, 42, 0};
    final long[] position = new long[1];
    SeekableStream ss = new DummySeekableStream(bytes) {
      @Override
      public void seek(long loc) {
        position[0] = loc;
      }
    };
    FastTiffDecoder decoder = FastTiffDecoder.create(ss, "test");
    decoder.reset();
    Assertions.assertEquals(4, position[0]);
  }

  @Test
  public void testStandardTiffMetadata() throws IOException {
    int width = 5;
    int height = 6;
    ImagePlus imp = new ImagePlus("test", new ByteProcessor(width, height));
    imp.setProperty("Info", "something");
    imp.setProperty("Label", "my label");
    imp.setRoi(1, 2, 3, 4);
    Plot plot = new Plot("plot", "x data", "y data");
    imp.setProperty(Plot.PROPERTY_KEY, plot);
    Overlay overlay = new Overlay();
    Roi roi = new Roi(0, 1, 3, 2);
    overlay.add(roi);
    imp.setOverlay(overlay);

    // Use IJ to write the image
    String path = File.createTempFile("test", ".tif").toString();

    final boolean intelByteOrder = ij.Prefs.intelByteOrder;
    for (boolean littleEndian : new boolean[] {true, false}) {
      ij.Prefs.intelByteOrder = littleEndian;
      IJ.saveAsTiff(imp, path);
      ij.Prefs.intelByteOrder = intelByteOrder;

      // Read back
      try (FileSeekableStream ss = new FileSeekableStream(path)) {
        FastTiffDecoder decoder = FastTiffDecoder.create(ss, path);

        FileInfo[] info = decoder.getTiffInfo(true);

        // Check
        Assertions.assertEquals(1, info.length);
        FileInfo fi = info[0];
        Assertions.assertEquals(width, fi.width);
        Assertions.assertEquals(height, fi.height);
        Assertions.assertEquals(FileInfo.GRAY8, fi.fileType);
        Assertions.assertEquals(imp.getInfoProperty(), fi.info);
        Assertions.assertEquals(imp.getProperty("Label"), fi.sliceLabels[0]);
        Assertions.assertEquals(imp.getRoi(), RoiDecoder.openFromByteArray(fi.roi));
        Assertions.assertArrayEquals(plot.toByteArray(), fi.plot);
        Assertions.assertEquals(1, fi.overlay.length);
        Assertions.assertEquals(roi, RoiDecoder.openFromByteArray(fi.overlay[0]));
        Assertions.assertNotNull(fi.description);
        Assertions.assertTrue(fi.description.contains(ImageJ.VERSION));

        Assertions.assertEquals(width, fi.width);
      }
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
      int length = Math.min(buffer.length - pos, len);
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
