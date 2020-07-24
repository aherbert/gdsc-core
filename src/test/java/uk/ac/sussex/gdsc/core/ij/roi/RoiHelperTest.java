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

package uk.ac.sussex.gdsc.core.ij.roi;

import gnu.trove.set.hash.TIntHashSet;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import java.awt.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.procedures.FValueProcedure;
import uk.ac.sussex.gdsc.core.data.procedures.IValueProcedure;

@SuppressWarnings({"javadoc"})
public class RoiHelperTest {

  @Test
  public void testGetMask() {
    final int size = 8;
    final ImagePlus imp = new ImagePlus(null, new ByteProcessor(size, size));
    // No ROI
    Assertions.assertNull(RoiHelper.getMask(imp));
    // No area ROI
    imp.setRoi(new Line(1, 2, 3, 4));
    Assertions.assertNull(RoiHelper.getMask(imp));
    // Full size rectangle
    final Roi roi = new Roi(0, 0, size, size);
    imp.setRoi(roi);
    Assertions.assertNull(RoiHelper.getMask(imp));
    // Rounded edge
    roi.setRoundRectArcSize(1);
    Assertions.assertNotNull(RoiHelper.getMask(imp));
    // Part size rectangle
    for (final Roi r : new Roi[] {new Roi(1, 2, 3, 4), new Roi(0, 2, size, 4),
        new Roi(1, 0, 3, size)}) {
      imp.setRoi(r);
      final Rectangle bounds = r.getBounds();
      final ByteProcessor bp = RoiHelper.getMask(imp);
      // Just check the sum is correct
      int sum = 0;
      for (int i = bp.getPixelCount(); i-- > 0;) {
        sum += bp.get(i);
      }
      Assertions.assertEquals(255 * bounds.width * bounds.height, sum);
      for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
          Assertions.assertEquals(255, bp.get(x, y));
        }
      }
    }
    // Polygon
    imp.setRoi(new PolygonRoi(new float[] {0, size, size}, new float[] {0, 0, size}, Roi.POLYGON));
    Assertions.assertNotNull(RoiHelper.getMask(imp));
  }

  @Test
  public void testForEachFloat() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ByteProcessor ip = new ByteProcessor(size, size, pixels);
    // No ROI
    final TIntHashSet set = new TIntHashSet(pixels.length);
    RoiHelper.forEach(null, ip, (FValueProcedure) value -> {
      Assertions.assertTrue(set.add((int) value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), ip, (FValueProcedure) value -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), ip, (FValueProcedure) value -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), ip, (FValueProcedure) value -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, ip, (FValueProcedure) value -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, ip, (FValueProcedure) value -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  public void testForEachImageStackFloat() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ImageStack stack = new ImageStack(size, size);
    stack.addSlice(null, pixels);
    // No ROI
    final TIntHashSet set = new TIntHashSet(pixels.length);
    RoiHelper.forEach(null, stack, (FValueProcedure) value -> {
      Assertions.assertTrue(set.add((int) value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), stack, (FValueProcedure) value -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), stack, (FValueProcedure) value -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), stack, (FValueProcedure) value -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, stack, (FValueProcedure) value -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, stack, (FValueProcedure) value -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  public void testForEachInt() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ByteProcessor ip = new ByteProcessor(size, size, pixels);
    // No ROI
    final TIntHashSet set = new TIntHashSet(pixels.length);
    RoiHelper.forEach(null, ip, (IValueProcedure) value -> {
      Assertions.assertTrue(set.add(value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), ip, (IValueProcedure) value -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), ip, (IValueProcedure) value -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), ip, (IValueProcedure) value -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, ip, (IValueProcedure) index -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, ip, (IValueProcedure) index -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  public void testForEachImageStackInt() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ImageStack stack = new ImageStack(size, size);
    stack.addSlice(null, pixels);
    // No ROI
    final TIntHashSet set = new TIntHashSet(pixels.length);
    RoiHelper.forEach(null, stack, (IValueProcedure) value -> {
      Assertions.assertTrue(set.add(value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), stack, (IValueProcedure) value -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), stack, (IValueProcedure) value -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), stack, (IValueProcedure) value -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, stack, (IValueProcedure) index -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, stack, (IValueProcedure) index -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }
}
