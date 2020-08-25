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

package uk.ac.sussex.gdsc.core.filters;

import ij.plugin.filter.GaussianBlur;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class FilteredNonMaximumSuppressionTest {
  static final int ITER = 5;

  // int[] primes = new int[] { 113, 97, 53, 29, 17, 7 };
  // int[] primes = new int[] { 509, 251 };
  int[] primes = new int[] {113, 29};
  // int[] primes = new int[] { 17 };
  // int[] smallPrimes = new int[] { 113, 97, 53, 29, 17, 7 };
  int[] smallPrimes = new int[] {17};
  int[] boxSizes = new int[] {9, 5, 3, 2, 1};
  // int[] boxSizes = new int[] { 2, 3, 5, 9, 15 };
  // int[] boxSizes = new int[] { 1 };

  int[] borderSizes = new int[] {9, 5, 3, 2, 1, 0};

  final int[] sizes2x2 = {2, 3, 6, 9};
  final int[] modulus2x2 = {3, 5, Integer.MAX_VALUE};

  /**
   * Creates the spot data using Gaussian spots of {@code [amplitude,x,y,sx,sy]}.
   *
   * @param maxx the maxx
   * @param maxy the maxy
   * @param spots the spots
   * @return the spot data
   */
  private static float[] floatCreateSpotData(int maxx, int maxy, double... spots) {
    final GaussianBlur gb = new GaussianBlur();
    final FloatProcessor fp = new FloatProcessor(maxx, maxy);
    for (int i = 0; i < spots.length; i += 5) {
      final FloatProcessor fp2 = new FloatProcessor(maxx, maxy);
      final double amplitude = spots[i];
      final int x = (int) spots[i + 1];
      final int y = (int) spots[i + 2];
      final double sx = spots[i + 3];
      final double sy = spots[i + 4];
      final double intensity = amplitude * sx * sy * Math.PI * 2;
      fp2.putPixelValue(x, y, intensity);
      gb.blurGaussian(fp2, sx, sy, 0.02);
      Assertions.assertEquals(amplitude, fp2.getMax(), amplitude * 0.05);
      fp.copyBits(fp2, 0, 0, Blitter.ADD);
    }
    return (float[]) fp.getPixels();
  }

  /**
   * Creates the spot data using Gaussian spots of {@code [amplitude,x,y,sx,sy]}.
   *
   * @param maxx the maxx
   * @param maxy the maxy
   * @param spots the spots
   * @return the spot data
   */
  private static int[] intCreateSpotData(int maxx, int maxy, double... spots) {
    final float[] data = floatCreateSpotData(maxx, maxy, spots);
    final int[] out = new int[data.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = (int) data[i];
    }
    return out;
  }

  @Test
  void testCopy() {
    final boolean[] flags = {true, false};
    for (final boolean f1 : flags) {
      for (final boolean f2 : flags) {
        final FilteredNonMaximumSuppression nms = new FilteredNonMaximumSuppression();
        nms.setDataBuffer(f1);
        nms.setNeighbourCheck(f2);
        final FilteredNonMaximumSuppression nms2 = nms.copy();
        Assertions.assertEquals(f1, nms2.isBufferData());
        Assertions.assertEquals(f2, nms2.isNeighbourCheck());
      }
    }
    for (int i = 1; i <= 2; i++) {
      final FilteredNonMaximumSuppression nms = new FilteredNonMaximumSuppression();
      nms.setFractionAboveBackground(i * 4 + 1);
      nms.setMinimumHeight(i * 4 + 2);
      nms.setMinimumWidth(i * 4 + 3);
      nms.setBackground(i * 4 + 4);
      final FilteredNonMaximumSuppression nms2 = nms.copy();
      Assertions.assertEquals(i * 4 + 1, nms2.getFractionAboveBackground());
      Assertions.assertEquals(i * 4 + 2, nms2.getMinimumHeight());
      Assertions.assertEquals(i * 4 + 3, nms2.getMinimumWidth());
      Assertions.assertEquals(i * 4 + 4, nms2.getBackground());
    }
    final FilteredNonMaximumSuppression nms = new FilteredNonMaximumSuppression();
    Assertions.assertEquals(0, nms.getFractionAboveBackground());
    Assertions.assertEquals(0, nms.getMinimumHeight());
    Assertions.assertEquals(0, nms.getMinimumWidth());
    Assertions.assertEquals(0, nms.getBackground());
  }

  @Test
  void testGetHeightThreshold() {
    final FilteredNonMaximumSuppression nms = new FilteredNonMaximumSuppression();
    Assertions.assertEquals(0, nms.getHeightThreshold());
    nms.setBackground(10);
    Assertions.assertEquals(10, nms.getHeightThreshold());
    nms.setMinimumHeight(100);
    Assertions.assertEquals(110, nms.getHeightThreshold());
    nms.setFractionAboveBackground(0.99f);
    Assertions.assertEquals(10.0 / 0.01, nms.getHeightThreshold(), 0.01);
    nms.setFractionAboveBackground(1);
    Assertions.assertEquals(110, nms.getHeightThreshold());
  }

  // XXX: Copy from here..
  @Test
  void floatMaxFindCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4   // index 25*40+14 = 1014
        );
    //@formatter:on
    final NonMaximumSuppression nms1 = new NonMaximumSuppression();
    final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
    final int[] expected = nms1.maxFind(data, maxx, maxy, 5);
    final int[] actual = nms2.maxFind(data, maxx, maxy, 5);
    Assertions.assertArrayEquals(expected, actual);
    // Remove noise
    nms2.setMinimumHeight(5);
    Assertions.assertArrayEquals(new int[] {614, 624, 1014}, nms2.maxFind(data, maxx, maxy, 5));
    // Remove narrow peaks
    nms2.setMinimumWidth(8);
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFind(data, maxx, maxy, 5));
  }

  @Test
  void floatMaxFindInternalCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    final NonMaximumSuppression nms1 = new NonMaximumSuppression();
    final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
    final int[] expected = nms1.maxFindInternal(data, maxx, maxy, 5);
    final int[] actual = nms2.maxFindInternal(data, maxx, maxy, 5);
    Assertions.assertArrayEquals(expected, actual);
    // Remove noise
    nms2.setMinimumHeight(5);
    Assertions.assertArrayEquals(new int[] {614, 624, 1014},
        nms2.maxFindInternal(data, maxx, maxy, 5));
    // Remove narrow peaks
    nms2.setMinimumWidth(8);
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5));
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5, 5));
  }

  @Test
  void floatMaxFindInternalWithBorderCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    final NonMaximumSuppression nms1 = new NonMaximumSuppression();
    final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
    final int[] expected = nms1.maxFindInternal(data, maxx, maxy, 5, 3);
    final int[] actual = nms2.maxFindInternal(data, maxx, maxy, 5, 3);
    Assertions.assertArrayEquals(expected, actual);
    // Remove noise
    nms2.setMinimumHeight(5);
    Assertions.assertArrayEquals(new int[] {614, 624, 1014},
        nms2.maxFindInternal(data, maxx, maxy, 5, 3));
    // Remove narrow peaks
    nms2.setMinimumWidth(8);
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5, 3));
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5, 6));
  }

  @Test
  void floatBlockFindCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4   // index 25*40+14 = 1014
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFind(data, maxx, maxy, 5);
      final int[] actual = nms2.blockFind(data, maxx, maxy, 5);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014}, nms2.blockFind(data, maxx, maxy, 5));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014}, nms2.blockFind(data, maxx, maxy, 5));
    }
  }

  @Test
  void floatBlockFindInternalCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFindInternal(data, maxx, maxy, 5, 3);
      final int[] actual = nms2.blockFindInternal(data, maxx, maxy, 5, 3);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014},
          nms2.blockFindInternal(data, maxx, maxy, 5, 3));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014},
          nms2.blockFindInternal(data, maxx, maxy, 5, 3));
    }
  }

  @Test
  void floatBlockFind3x3CanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4   // index 25*40+14 = 1014
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFind(data, maxx, maxy, 1);
      final int[] actual = nms2.blockFind(data, maxx, maxy, 1);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014}, nms2.blockFind(data, maxx, maxy, 1));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014}, nms2.blockFind(data, maxx, maxy, 1));
    }
  }

  @Test
  void floatBlockFind3x3InternalCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final float[] data = floatCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFindInternal(data, maxx, maxy, 1, 3);
      final int[] actual = nms2.blockFindInternal(data, maxx, maxy, 1, 3);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014},
          nms2.blockFindInternal(data, maxx, maxy, 1, 3));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014},
          nms2.blockFindInternal(data, maxx, maxy, 1, 3));
    }
  }

  @SeededTest
  void floatBlockFind3x3InternalIgnoresBadBorder(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int maxx = 13;
    final int maxy = 17;
    final float[] data = NonMaximumSuppressionTest.floatCreateData(rg, maxx, maxy);
    final FilteredNonMaximumSuppression nms = new FilteredNonMaximumSuppression();
    Assertions.assertArrayEquals(nms.blockFind3x3(data, maxx, maxy),
        nms.blockFind3x3Internal(data, maxx, maxy, 0));
  }

  // XXX: Copy methods up to here for 'int' versions
  @Test
  void intMaxFindCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4   // index 25*40+14 = 1014
        );
    //@formatter:on
    final NonMaximumSuppression nms1 = new NonMaximumSuppression();
    final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
    final int[] expected = nms1.maxFind(data, maxx, maxy, 5);
    final int[] actual = nms2.maxFind(data, maxx, maxy, 5);
    Assertions.assertArrayEquals(expected, actual);
    // Remove noise
    nms2.setMinimumHeight(5);
    Assertions.assertArrayEquals(new int[] {614, 624, 1014}, nms2.maxFind(data, maxx, maxy, 5));
    // Remove narrow peaks
    nms2.setMinimumWidth(8);
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFind(data, maxx, maxy, 5));
  }

  @Test
  void intMaxFindInternalCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    final NonMaximumSuppression nms1 = new NonMaximumSuppression();
    final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
    final int[] expected = nms1.maxFindInternal(data, maxx, maxy, 5);
    final int[] actual = nms2.maxFindInternal(data, maxx, maxy, 5);
    Assertions.assertArrayEquals(expected, actual);
    // Remove noise
    nms2.setMinimumHeight(5);
    Assertions.assertArrayEquals(new int[] {614, 624, 1014},
        nms2.maxFindInternal(data, maxx, maxy, 5));
    // Remove narrow peaks
    nms2.setMinimumWidth(8);
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5));
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5, 5));
  }

  @Test
  void intMaxFindInternalWithBorderCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    final NonMaximumSuppression nms1 = new NonMaximumSuppression();
    final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
    final int[] expected = nms1.maxFindInternal(data, maxx, maxy, 5, 3);
    final int[] actual = nms2.maxFindInternal(data, maxx, maxy, 5, 3);
    Assertions.assertArrayEquals(expected, actual);
    // Remove noise
    nms2.setMinimumHeight(5);
    Assertions.assertArrayEquals(new int[] {614, 624, 1014},
        nms2.maxFindInternal(data, maxx, maxy, 5, 3));
    // Remove narrow peaks
    nms2.setMinimumWidth(8);
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5, 3));
    Assertions.assertArrayEquals(new int[] {1014}, nms2.maxFindInternal(data, maxx, maxy, 5, 6));
  }

  @Test
  void intBlockFindCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4   // index 25*40+14 = 1014
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFind(data, maxx, maxy, 5);
      final int[] actual = nms2.blockFind(data, maxx, maxy, 5);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014}, nms2.blockFind(data, maxx, maxy, 5));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014}, nms2.blockFind(data, maxx, maxy, 5));
    }
  }

  @Test
  void intBlockFindInternalCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFindInternal(data, maxx, maxy, 5, 3);
      final int[] actual = nms2.blockFindInternal(data, maxx, maxy, 5, 3);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014},
          nms2.blockFindInternal(data, maxx, maxy, 5, 3));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014},
          nms2.blockFindInternal(data, maxx, maxy, 5, 3));
    }
  }

  @Test
  void intBlockFind3x3CanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4   // index 25*40+14 = 1014
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFind(data, maxx, maxy, 1);
      final int[] actual = nms2.blockFind(data, maxx, maxy, 1);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014}, nms2.blockFind(data, maxx, maxy, 1));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014}, nms2.blockFind(data, maxx, maxy, 1));
    }
  }

  @Test
  void intBlockFind3x3InternalCanFilter() {
    final int maxx = 40;
    final int maxy = 41;
    //@formatter:off
    final int[] data = intCreateSpotData(maxx, maxy,
        100, 14, 15, 2, 2, // index 15*40+14 = 614
        30, 24, 15, 4, 2,  // index 15*40+24 = 624
        30, 14, 25, 4, 4,  // index 25*40+14 = 1014
        30, 4, 1, 4, 4     // index 1*40+4 = 41
        );
    //@formatter:on
    for (final boolean check : new boolean[] {true, false}) {
      final NonMaximumSuppression nms1 = new NonMaximumSuppression();
      final FilteredNonMaximumSuppression nms2 = new FilteredNonMaximumSuppression();
      nms1.setNeighbourCheck(check);
      nms2.setNeighbourCheck(check);
      final int[] expected = nms1.blockFindInternal(data, maxx, maxy, 1, 3);
      final int[] actual = nms2.blockFindInternal(data, maxx, maxy, 1, 3);
      Assertions.assertArrayEquals(expected, actual);
      // Remove noise
      nms2.setMinimumHeight(5);
      Assertions.assertArrayEquals(new int[] {614, 624, 1014},
          nms2.blockFindInternal(data, maxx, maxy, 1, 3));
      // Remove narrow peaks
      nms2.setMinimumWidth(8);
      Assertions.assertArrayEquals(new int[] {1014},
          nms2.blockFindInternal(data, maxx, maxy, 1, 3));
    }
  }

  @SeededTest
  void intBlockFind3x3InternalIgnoresBadBorder(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int maxx = 13;
    final int maxy = 17;
    final int[] data = NonMaximumSuppressionTest.intCreateData(rg, maxx, maxy);
    final FilteredNonMaximumSuppression nms = new FilteredNonMaximumSuppression();
    Assertions.assertArrayEquals(nms.blockFind3x3(data, maxx, maxy),
        nms.blockFind3x3Internal(data, maxx, maxy, 0));
  }
}
