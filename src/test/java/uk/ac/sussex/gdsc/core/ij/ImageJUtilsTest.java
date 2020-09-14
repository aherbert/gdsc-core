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

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

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
  void cannotIterateOverNullList() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      for (final int i : getIdList()) {
        // This will not run as an exception should be generated
        logger.info("Window ID = " + i);
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
      logger.info("Window ID = " + i);
    }
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
    int width = 4;
    int height = 5;
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
    float[] data = new float[64 * 64];
    // Require histogram to have some bins > 10% of the pixel count and some > 0.02%
    FloatProcessor fp = new FloatProcessor(64, 64, data);
    for (int i = 1; i < 100; i++) {
      fp.setf(i, 42);
    }
    for (int i = 100; i < 10; i++) {
      fp.setf(i, 32);
    }
    for (int i = 110; i < data.length / 2; i++) {
      fp.setf(i, 12);
    }
    for (int i = data.length / 2; i < data.length; i++) {
      fp.setf(i, 64);
    }
    final ImagePlus imp = new ImagePlus(null, fp);
    imp.setDisplayRange(-99, 999);
    ImageJUtils.autoAdjust(imp, false);
    Assertions.assertEquals(-99, imp.getDisplayRangeMin());
    Assertions.assertEquals(999, imp.getDisplayRangeMax());

    double[] limits = ImageJUtils.autoAdjust(imp, true);
    Assertions.assertEquals(limits[0], imp.getDisplayRangeMin());
    Assertions.assertEquals(limits[1], imp.getDisplayRangeMax());

    // Reverse
    SimpleArrayUtils.reverse(data);
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
    ImageJUtils.finished();
  }
}
