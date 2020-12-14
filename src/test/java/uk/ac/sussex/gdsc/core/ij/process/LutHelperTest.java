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

package uk.ac.sussex.gdsc.core.ij.process;

import ij.process.LUT;
import java.awt.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.DefaultLutMapper;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.LutColour;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.LutMapper;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.NonZeroLutMapper;

@SuppressWarnings({"javadoc"})
class LutHelperTest {
  @Test
  void canLutColour() {
    Assertions.assertNull(LutColour.forNumber(-1));
    Assertions.assertNull(LutColour.forNumber(Integer.MAX_VALUE));
    Assertions.assertNull(LutColour.forName(null));
    Assertions.assertNull(LutColour.forName("something else"));
    final LutColour[] colours = LutColour.values();
    final String[] names = LutHelper.getLutNames();
    Assertions.assertEquals(colours.length, names.length);
    for (int i = 0; i < colours.length; i++) {
      Assertions.assertEquals(colours[i], LutColour.forNumber(i));
      Assertions.assertEquals(colours[i], LutColour.forName(names[i]));
      Assertions.assertEquals(names[i], LutColour.forNumber(i).getName());
      Assertions.assertEquals(names[i], LutColour.forNumber(i).toString());
    }
  }

  @Test
  void testLutColourDistinct() {
    Assertions.assertFalse(LutColour.RED.isDistinct());
    Assertions.assertTrue(LutColour.INTENSE.isDistinct());
    Assertions.assertTrue(LutColour.PIMP.isDistinct());
    Assertions.assertTrue(LutColour.PIMP_LIGHT.isDistinct());
    Assertions.assertTrue(LutColour.DISTINCT.isDistinct());
  }

  @Test
  void canCreateLut() {
    Assertions.assertNull(LutHelper.createLut(-1));
    Assertions.assertNull(LutHelper.createLut(Integer.MAX_VALUE));
    Assertions.assertThrows(NullPointerException.class, () -> LutHelper.createLut(null));
    Assertions.assertNotNull(LutHelper.createLut(0));
    Assertions.assertNotNull(LutHelper.getColorModel());
    for (final LutColour colour : LutColour.values()) {
      final LUT lut1 = LutHelper.createLut(colour);
      Assertions.assertNotNull(lut1);
      final LUT lut2 = LutHelper.createLut(colour, true);
      Assertions.assertNotNull(lut2);
      Assertions.assertEquals(Color.black, LutHelper.getColour(lut2, 0));
    }
  }

  @Test
  void testGetColour8Bit() {
    final LUT lut = LutHelper.createLut(LutColour.RED, true);
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, -1));
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, 0));
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, -1, 256));
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, 0, 256));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 255));
    Assertions.assertNotEquals(Color.red, LutHelper.getColour(lut, 254));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 255, 256));
  }

  @Test
  void testGetColour16Bit() {
    final LUT lut = LutHelper.createLut(LutColour.RED, false);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> LutHelper.getColour(lut, 0, 10, 5));
    final Color zero = LutHelper.getColour(lut, 0);
    Assertions.assertEquals(zero, LutHelper.getColour(lut, -10, 500));
    Assertions.assertEquals(zero, LutHelper.getColour(lut, 0, 500));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 599, 600));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 500, 500));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 1500, 500));

    Assertions.assertEquals(zero, LutHelper.getColour(lut, 0, 100, 500));
    Assertions.assertEquals(zero, LutHelper.getColour(lut, 100, 100, 500));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 500, 100, 500));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 1500, 100, 500));

    // Interpolated
    final Color c = LutHelper.getColour(lut, 300, 100, 500);
    Assertions.assertNotEquals(zero, c);
    Assertions.assertNotEquals(Color.red, c);
    Assertions.assertEquals(0, c.getGreen());
    Assertions.assertEquals(0, c.getBlue());
    final int redRange = Color.red.getRed() - zero.getRed();
    Assertions.assertTrue(c.getRed() < zero.getRed() + redRange * 0.52
        && c.getRed() > zero.getRed() + redRange * 0.48);
  }

  @Test
  void testGetColour32Bit() {
    final LUT lut = LutHelper.createLut(LutColour.RED, false);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> LutHelper.getColour(lut, 0, 10.0f, 5.0f));
    final Color zero = LutHelper.getColour(lut, 0);

    Assertions.assertEquals(zero, LutHelper.getColour(lut, 0f, 100f, 500f));
    Assertions.assertEquals(zero, LutHelper.getColour(lut, 100f, 100f, 500f));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 500f, 100f, 500f));
    Assertions.assertEquals(Color.red, LutHelper.getColour(lut, 1500f, 100f, 500f));

    // Interpolated
    final Color c = LutHelper.getColour(lut, 300f, 100f, 500f);
    Assertions.assertNotEquals(zero, c);
    Assertions.assertNotEquals(Color.red, c);
    Assertions.assertEquals(0, c.getGreen());
    Assertions.assertEquals(0, c.getBlue());
    final int redRange = Color.red.getRed() - zero.getRed();
    Assertions.assertTrue(c.getRed() < zero.getRed() + redRange * 0.52
        && c.getRed() > zero.getRed() + redRange * 0.48);
  }

  @Test
  void testGetNonZeroColour8Bit() {
    final LUT lut = LutHelper.createLut(LutColour.RED, true);
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, 0));
    final Color zero = LutHelper.getColour(lut, 1);
    Assertions.assertNotEquals(Color.black, zero);
    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, -1, 255));
    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, 0, 255));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 255, 255));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 254, 255));
    Assertions.assertNotEquals(Color.red, LutHelper.getNonZeroColour(lut, 253, 255));
  }

  @Test
  void testGetNonZeroColour16Bit() {
    final LUT lut = LutHelper.createLut(LutColour.RED, true);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> LutHelper.getNonZeroColour(lut, 0, 10, 5));
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, 0));
    final Color zero = LutHelper.getColour(lut, 1);
    Assertions.assertNotEquals(Color.black, zero);

    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, -10, 500));
    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, 0, 500));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 599, 600));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 500, 500));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 1500, 500));

    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, 0, 100, 500));
    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, 100, 100, 500));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 500, 100, 500));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 1500, 100, 500));

    // Interpolated
    final Color c = LutHelper.getNonZeroColour(lut, 300, 100, 500);
    Assertions.assertNotEquals(zero, c);
    Assertions.assertNotEquals(Color.red, c);
    Assertions.assertEquals(0, c.getGreen());
    Assertions.assertEquals(0, c.getBlue());
    final int redRange = Color.red.getRed() - zero.getRed();
    Assertions.assertTrue(c.getRed() < zero.getRed() + redRange * 0.52
        && c.getRed() > zero.getRed() + redRange * 0.48);
  }

  @Test
  void testGetNonZeroColour32Bit() {
    final LUT lut = LutHelper.createLut(LutColour.RED, true);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> LutHelper.getNonZeroColour(lut, 0, 10.0f, 5.0f));
    Assertions.assertEquals(Color.black, LutHelper.getColour(lut, 0));
    final Color zero = LutHelper.getColour(lut, 1);
    Assertions.assertNotEquals(Color.black, zero);

    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, 0f, 100f, 500f));
    Assertions.assertEquals(zero, LutHelper.getNonZeroColour(lut, 100f, 100f, 500f));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 500f, 100f, 500f));
    Assertions.assertEquals(Color.red, LutHelper.getNonZeroColour(lut, 1500f, 100f, 500f));

    // Interpolated
    final Color c = LutHelper.getNonZeroColour(lut, 300f, 100f, 500f);
    Assertions.assertNotEquals(zero, c);
    Assertions.assertNotEquals(Color.red, c);
    Assertions.assertEquals(0, c.getGreen());
    Assertions.assertEquals(0, c.getBlue());
    final int redRange = Color.red.getRed() - zero.getRed();
    Assertions.assertTrue(c.getRed() < zero.getRed() + redRange * 0.52
        && c.getRed() > zero.getRed() + redRange * 0.48);
  }

  @Test
  void testNullLutMapper() {
    final LutHelper.NullLutMapper mapper = new LutHelper.NullLutMapper();
    assertMapper(mapper, 0, 255);
    // No mapping of float input
    for (final float f : new float[] {1.23f, -44f, 765f, 255f}) {
      Assertions.assertEquals(f, mapper.mapf(f));
    }
    final LUT lut = LutHelper.createLut(LutColour.RED, false);
    final Color zero = LutHelper.getColour(lut, 0);
    Assertions.assertEquals(zero, mapper.getColour(lut, 0));
    Assertions.assertEquals(Color.red, mapper.getColour(lut, 255));
  }

  @Test
  void canMapTo0to255() {
    mapTo0to255(0, 0);
    mapTo0to255(0, 1);
    mapTo0to255(0, 255);
    mapTo0to255(0, 1000);

    mapTo0to255(4.3f, 32.5f);
    mapTo0to255(-4.3f, 0f);
    mapTo0to255(-4.3f, 32.5f);
    mapTo0to255(0f, 32.5f);
  }

  @Test
  void canMapTo1to255() {
    mapTo1to255(1, 1);
    mapTo1to255(1, 2);
    mapTo1to255(1, 255);
    mapTo1to255(1, 1000);

    mapTo1to255(4.3f, 32.5f);
    mapTo1to255(-4.3f, 0f);
    mapTo1to255(-4.3f, 32.5f);
    mapTo1to255(0f, 32.5f);
  }

  private static void mapTo0to255(float min, float max) {
    final LutMapper map = new DefaultLutMapper(min, max);
    Assertions.assertEquals(0, map.getMin());
    Assertions.assertEquals(255, map.getMax());
    assertMapper(map, min, max);
  }

  private static void mapTo1to255(float min, float max) {
    final LutMapper map = new NonZeroLutMapper(min, max);
    Assertions.assertEquals(1, map.getMin());
    Assertions.assertEquals(255, map.getMax());
    assertMapper(map, min, max);
  }

  private static void assertMapper(LutMapper mapper, float min, float max) {
    Assertions.assertEquals(mapper.getMin(), mapper.map(min - 1));
    Assertions.assertEquals(mapper.getMin(), mapper.map(min));
    if (max != min) {
      Assertions.assertEquals(mapper.getMax(), mapper.map(max));
      Assertions.assertEquals(mapper.getMax(), mapper.map(max + 1));
      final float range = mapper.getMax() - mapper.getMin();
      for (final float fraction : new float[] {0.25f, 0.5f, 0.75f}) {
        final float value = (max - min) * fraction + min;
        final float lower = range * fraction * 0.98f + mapper.getMin();
        final float upper = range * fraction * 1.02f + mapper.getMin();
        final int i = mapper.map(value);
        Assertions.assertTrue(i > lower);
        Assertions.assertTrue(i < upper);
        final float f = mapper.mapf(value);
        Assertions.assertTrue(f > lower);
        Assertions.assertTrue(f < upper);
      }
    }
  }
}
