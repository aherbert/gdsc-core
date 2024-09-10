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
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.ZProjector;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.plugin.WindowOrganiser;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;

@SuppressWarnings({"javadoc"})
class ImageJUtilsTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(ImageJUtilsTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  void testDecodePath() {
    Assertions.assertArrayEquals(new String[] {null, ""}, ImageJUtils.decodePath(null));
    Assertions.assertArrayEquals(new String[] {null, "tmp"}, ImageJUtils.decodePath("tmp"));
    Assertions.assertArrayEquals(new String[] {"dir/", "tmp"}, ImageJUtils.decodePath("dir/tmp"));
    Assertions.assertArrayEquals(new String[] {"dir\\", "tmp"}, ImageJUtils.decodePath("dir\\tmp"));
    Assertions.assertArrayEquals(new String[] {"dir/", ""}, ImageJUtils.decodePath("dir/"));
    Assertions.assertArrayEquals(new String[] {"dir1/dir2/", "tmp"},
        ImageJUtils.decodePath("dir1/dir2/tmp"));
  }

  @Test
  void testAddImage() {
    final ImagePlus imp = new ImagePlus();
    ImageJUtils.addImage(null, imp);
    final WindowOrganiser wo = new WindowOrganiser();
    ImageJUtils.addImage(wo, imp);
    Assertions.assertEquals(1, wo.size());
  }

  @Test
  void testAddPlot() {
    // No assertions
    ImageJUtils.addPlot(null, null);
  }

  @Test
  void testPreserveLimits() {
    final Plot plot = new Plot("test", "x", "y");
    plot.addPoints(new float[] {0, 1, 2}, new float[] {4, 5, 6}, Plot.LINE);
    plot.draw();
    final double[] currentLimits = plot.getLimits().clone();
    final double xMin = currentLimits[0];
    final double xMax = currentLimits[1];
    final double yMin = currentLimits[2];
    final double yMax = currentLimits[3];
    final double[] limits = {42, 99, -13, 17};

    // Do nothing
    ImageJUtils.preserveLimits(plot, 0, limits);
    Assertions.assertArrayEquals(currentLimits, plot.getLimits());

    // Preserve
    ImageJUtils.preserveLimits(plot, ImageJUtils.PRESERVE_X_MIN, limits.clone());
    Assertions.assertArrayEquals(new double[] {limits[0], xMax, yMin, yMax}, plot.getLimits());
    plot.setLimits(xMin, xMax, yMin, yMax);

    ImageJUtils.preserveLimits(plot, ImageJUtils.PRESERVE_X_MAX, limits.clone());
    Assertions.assertArrayEquals(new double[] {xMin, limits[1], yMin, yMax}, plot.getLimits());
    plot.setLimits(xMin, xMax, yMin, yMax);

    ImageJUtils.preserveLimits(plot, ImageJUtils.PRESERVE_Y_MIN, limits.clone());
    Assertions.assertArrayEquals(new double[] {xMin, xMax, limits[2], yMax}, plot.getLimits());
    plot.setLimits(xMin, xMax, yMin, yMax);

    ImageJUtils.preserveLimits(plot, ImageJUtils.PRESERVE_Y_MAX, limits.clone());
    Assertions.assertArrayEquals(new double[] {xMin, xMax, yMin, limits[3]}, plot.getLimits());
  }

  @Test
  void cannotIterateOverNullList() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      for (final int i : getIdList()) {
        // This will not run as an exception should be generated
        logger.log(TestLevel.TEST_INFO, "Window ID = " + i);
      }
    });
  }

  private static int[] getIdList() {
    return null;
  }

  @Test
  void cantIterateOver_getIdList() {
    for (final int i : ImageJUtils.getIdList()) {
      // This will not run as the ID list should be empty
      logger.log(TestLevel.TEST_INFO, "Window ID = " + i);
    }
  }

  @Test
  void testGetIdList() {
    // The default list
    int[] list = WindowManager.getIDList();
    if (list == null) {
      list = new int[0];
    }
    Assertions.assertArrayEquals(list, ImageJUtils.getIdList());
  }

  @Test
  void testGetImageList() {
    // The default list
    final LocalList<String> list = new LocalList<>();
    for (final int id : ImageJUtils.getIdList()) {
      final ImagePlus imp = WindowManager.getImage(id);
      list.add(imp.getTitle());
    }

    final String[] defaultList = list.toArray(new String[0]);
    list.add(0, ImageJUtils.NO_IMAGE_TITLE);
    final String[] defaultNoImageList = list.toArray(new String[0]);

    Assertions.assertArrayEquals(defaultList, ImageJUtils.getImageList(0));
    Assertions.assertArrayEquals(defaultList, ImageJUtils.getImageList(0, (String[]) null));
    Assertions.assertArrayEquals(defaultNoImageList,
        ImageJUtils.getImageList(ImageJUtils.NO_IMAGE));
    Assertions.assertArrayEquals(new String[0], ImageJUtils.getImageList(0, new String[] {".txt"}));
    Assertions.assertArrayEquals(new String[] {ImageJUtils.NO_IMAGE_TITLE},
        ImageJUtils.getImageList(ImageJUtils.NO_IMAGE, new String[] {".txt"}));
    Assertions.assertArrayEquals(new String[0],
        ImageJUtils.getImageList(imp -> imp.getBitDepth() == 17));
  }

  @Test
  void testIgnoreImage() {
    Assertions.assertFalse(ImageJUtils.ignoreImage(new String[0], null));
    Assertions.assertFalse(ImageJUtils.ignoreImage(new String[0], "test.tif"));
    Assertions.assertFalse(ImageJUtils.ignoreImage(new String[] {"tif"}, null));
    Assertions.assertTrue(ImageJUtils.ignoreImage(new String[] {"tif"}, "test.tif"));
    // Test the filter too
    final Predicate<ImagePlus> filter = ImageJUtils.createImageFilter("jpg");
    final ImagePlus imp = IJ.createImage("test.tif", 3, 4, 1, 8);
    Assertions.assertFalse(filter.test(imp));
    imp.setTitle("test.jpg");
    Assertions.assertTrue(filter.test(imp));
  }

  @Test
  void testCreateImageFilter() {
    final ImagePlus single = IJ.createImage(null, 3, 4, 1, 8);
    final ImagePlus binary = IJ.createImage(null, 3, 4, 1, 8);
    binary.getProcessor().threshold(15);
    final ImagePlus color = IJ.createImage(null, 3, 4, 1, 24);
    final ImagePlus grey16Stack = IJ.createImage(null, 3, 4, 2, 16);
    Assertions.assertTrue(ImageJUtils.createImageFilter(0).test(single));
    Assertions.assertFalse(ImageJUtils.createImageFilter(0).test(null));
    Assertions.assertTrue(ImageJUtils.createImageFilter(ImageJUtils.SINGLE).test(single));
    Assertions.assertFalse(ImageJUtils.createImageFilter(ImageJUtils.SINGLE).test(grey16Stack));
    Assertions.assertTrue(ImageJUtils.createImageFilter(ImageJUtils.BINARY).test(binary));
    Assertions.assertFalse(ImageJUtils.createImageFilter(ImageJUtils.BINARY).test(grey16Stack));
    Assertions.assertTrue(ImageJUtils.createImageFilter(ImageJUtils.GREY_SCALE).test(binary));
    Assertions.assertFalse(ImageJUtils.createImageFilter(ImageJUtils.GREY_SCALE).test(color));
    Assertions.assertTrue(ImageJUtils.createImageFilter(ImageJUtils.GREY_8_16).test(binary));
    Assertions.assertTrue(ImageJUtils.createImageFilter(ImageJUtils.GREY_8_16).test(grey16Stack));
    Assertions.assertFalse(ImageJUtils.createImageFilter(ImageJUtils.GREY_8_16).test(color));
  }

  @Test
  void testExtractTile() {
    // Multi channel, slice, frame image
    final int width = 3;
    final int height = 4;
    final ImageStack imageStack = new ImageStack(width, height);
    final int c = 3;
    final int z = 4;
    final int t = 2;
    for (int i = 0; i < t * c * z; i++) {
      imageStack.addSlice(null, SimpleArrayUtils.newByteArray(12, (byte) i));
    }
    final ImagePlus imp = new ImagePlus(null, imageStack);
    imp.setDimensions(c, z, t);

    ImageProcessor ip;
    ip = ImageJUtils.extractTile(imp, 1, 2, ZProjector.SUM_METHOD);
    // 4+5+6+7
    // Sum or SD method uses float processor result
    Assertions.assertArrayEquals(SimpleArrayUtils.newFloatArray(12, 22f), (float[]) ip.getPixels());
    ip = ImageJUtils.extractTile(imp, 2, 3, ZProjector.AVG_METHOD);
    // (17+18+19+20)/4
    final float expected = (17f + 18f + 19f + 20f) / 4;
    // Others use input processor type.
    // Note: Rounding was added between 1.53f and 1.53t
    Assertions.assertArrayEquals(SimpleArrayUtils.newByteArray(12, (byte) (expected + 0.5f)),
        (byte[]) ip.getPixels());
  }

  @Test
  void testRannageColummns() {
    // No Assertions
    ImageJUtils.rearrangeColumns(null);
    ImageJUtils.rearrangeColumns(null, new int[0]);
  }

  @Test
  void testClose() {
    // No assertions
    ImageJUtils.close("not a window");
  }

  @Test
  void testShowStatus() throws InterruptedException {
    // Wait until status has expired. This should be 150ms
    final int interval = 200;
    Thread.sleep(interval);
    Assertions.assertTrue(ImageJUtils.showStatus("first"));
    Assertions.assertFalse(ImageJUtils.showStatus("second"));
    Assertions.assertFalse(ImageJUtils.showStatus(() -> "third"));
    // Wait until status has expired. This should be 150ms
    Thread.sleep(interval);
    Assertions.assertTrue(ImageJUtils.showStatus(() -> "fourth"));
  }

  @Test
  void testGetProgressInterval() {
    Assertions.assertEquals(1, ImageJUtils.getProgressInterval(0));
    Assertions.assertEquals(1, ImageJUtils.getProgressInterval(1));
    Assertions.assertEquals(1, ImageJUtils.getProgressInterval(10));
    Assertions.assertEquals(1, ImageJUtils.getProgressInterval(100));
    Assertions.assertEquals(10, ImageJUtils.getProgressInterval(1000));
    Assertions.assertEquals(50, ImageJUtils.getProgressInterval(5000));
    Assertions.assertEquals(1L, ImageJUtils.getProgressInterval(0L));
    Assertions.assertEquals(1L, ImageJUtils.getProgressInterval(1L));
    Assertions.assertEquals(1L, ImageJUtils.getProgressInterval(10L));
    Assertions.assertEquals(1L, ImageJUtils.getProgressInterval(100L));
    Assertions.assertEquals(10L, ImageJUtils.getProgressInterval(1000L));
    Assertions.assertEquals(50L, ImageJUtils.getProgressInterval(5000L));
  }

  @Test
  void testLog() {
    // Prevent System.out logging here
    final PrintStream orig = System.out;
    final AtomicInteger count = new AtomicInteger();
    try (PrintStream ps = new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        count.incrementAndGet();
      }
    })) {
      System.setOut(ps);
      ImageJUtils.log("hello %s", "world");
      Assertions.assertNotEquals(0, count.get());
    } finally {
      System.setOut(orig);
    }
  }

  @Test
  void testAddMessage() {
    Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
    final GenericDialog gd = new GenericDialog("test");
    // No assertions
    ImageJUtils.addMessage(gd, "hello %s", "world");
  }

  @Test
  void testShowSlowProgress() {
    // No assertions
    ImageJUtils.showSlowProgress(0, 10);
    ImageJUtils.showSlowProgress(3, 10);
    ImageJUtils.showSlowProgress(10, 10);
    ImageJUtils.showSlowProgress(0, 10L);
    ImageJUtils.showSlowProgress(3, 10L);
    ImageJUtils.showSlowProgress(10, 10L);
    ImageJUtils.clearSlowProgress();
  }

  @Test
  void testIsShowing() {
    Assertions.assertFalse(ImageJUtils.isShowing(null));
    // Cannot create java.awt.Window in a headless environment
    if (!java.awt.GraphicsEnvironment.isHeadless()) {
      final Window w = new Window(null);
      Assertions.assertFalse(ImageJUtils.isShowing(w));
    }
  }

  @Test
  void testIsMacro() {
    Assertions.assertFalse(ImageJUtils.isMacro());
  }

  @Test
  void testIsInterrupted() {
    Assertions.assertFalse(ImageJUtils.isInterrupted());
    IJ.setKeyDown(KeyEvent.VK_ESCAPE);
    try {
      Assertions.assertTrue(ImageJUtils.isInterrupted());
    } finally {
      IJ.resetEscape();
    }
  }

  @Test
  void testIsExtraOptions() {
    // The isExtraOptions() method will write "extraoptions" to the Recorder.
    // This will generate duplicates (and a warning message) if not reset.
    IJ.setKeyUp(KeyEvent.VK_ALT);
    IJ.setKeyUp(KeyEvent.VK_SHIFT);
    Recorder.setCommand(null);

    Assertions.assertFalse(ImageJUtils.isExtraOptions());
    IJ.setKeyDown(KeyEvent.VK_ALT);
    try {
      Assertions.assertTrue(ImageJUtils.isExtraOptions());
    } finally {
      IJ.setKeyUp(KeyEvent.VK_ALT);
      Recorder.setCommand(null);
    }
    Assertions.assertFalse(ImageJUtils.isExtraOptions());

    IJ.setKeyDown(KeyEvent.VK_SHIFT);
    try {
      Assertions.assertTrue(ImageJUtils.isExtraOptions());
    } finally {
      IJ.setKeyUp(KeyEvent.VK_SHIFT);
      Recorder.setCommand(null);
    }
  }

  @Test
  void testIsShowGenericDialog() {
    // No assertions
    ImageJUtils.isShowGenericDialog();
  }

  @Test
  void testGetBitDepth() {
    Assertions.assertEquals(8, ImageJUtils.getBitDepth(new byte[0]));
    Assertions.assertEquals(16, ImageJUtils.getBitDepth(new short[0]));
    Assertions.assertEquals(32, ImageJUtils.getBitDepth(new float[0]));
    Assertions.assertEquals(24, ImageJUtils.getBitDepth(new int[0]));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageJUtils.getBitDepth(new double[0]));
  }

  @Test
  void testCreateProcessor() {
    ImageProcessor ip;
    final int width = 4;
    final int height = 5;
    ip = ImageJUtils.createProcessor(width, height, new byte[width * height]);
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());
    Assertions.assertTrue(ip instanceof ByteProcessor);
    ip = ImageJUtils.createProcessor(width, height, new short[width * height]);
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());
    Assertions.assertTrue(ip instanceof ShortProcessor);
    ip = ImageJUtils.createProcessor(width, height, new float[width * height]);
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());
    Assertions.assertTrue(ip instanceof FloatProcessor);
    ip = ImageJUtils.createProcessor(width, height, new int[width * height]);
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());
    Assertions.assertTrue(ip instanceof ColorProcessor);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageJUtils.createProcessor(width, height, new double[width * height]));
  }

  @Test
  void testAutoAdjust() {
    final byte[] data = new byte[64 * 64];
    final ByteProcessor ip = new ByteProcessor(64, 64, data);
    final ImagePlus imp = new ImagePlus(null, ip);

    // No data
    double[] limits = ImageJUtils.autoAdjust(imp, true);
    Assertions.assertEquals(limits[0], imp.getDisplayRangeMin());
    Assertions.assertEquals(limits[1], imp.getDisplayRangeMax());

    // Uniform histogram
    // Require histogram to have some bins > 10% of the pixel count and some > 0.02%
    // high-low-mid
    for (int i = 0; i < data.length; i++) {
      ip.set(i, i / 256);
    }
    // Make first bin too high
    for (int i = 0; i < 500; i++) {
      ip.set(i, 0);
    }
    imp.setDisplayRange(-99, 999);
    ImageJUtils.autoAdjust(imp, false);
    Assertions.assertEquals(-99, imp.getDisplayRangeMin());
    Assertions.assertEquals(999, imp.getDisplayRangeMax());

    limits = ImageJUtils.autoAdjust(imp, true);
    Assertions.assertEquals(limits[0], imp.getDisplayRangeMin());
    Assertions.assertEquals(limits[1], imp.getDisplayRangeMax());

    // Reverse histogram
    for (int i = 0; i < data.length; i++) {
      ip.set(i, 255 - ip.get(i));
    }
    imp.setDisplayRange(-99, 999);
    limits = ImageJUtils.autoAdjust(imp, true);
    Assertions.assertEquals(limits[0], imp.getDisplayRangeMin());
    Assertions.assertEquals(limits[1], imp.getDisplayRangeMax());
  }

  @Test
  void testCreateTicker() {
    Ticker ticker;
    ticker = ImageJUtils.createTicker(100, 1);
    Assertions.assertFalse(ticker.isThreadSafe());
    ticker = ImageJUtils.createTicker(100, 12);
    Assertions.assertTrue(ticker.isThreadSafe());
    Assertions.assertNotNull(ImageJUtils.createTicker(100, 1, "Starting ..."));
  }

  @Test
  void testFinished() {
    // No assertions
    ImageJUtils.finished();
  }

  @Test
  void testShowUrl() {
    // No assertions
    ImageJUtils.showUrl("https://www.google.com");
  }
}
