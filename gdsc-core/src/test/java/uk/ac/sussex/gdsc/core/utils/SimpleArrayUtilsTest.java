/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class SimpleArrayUtilsTest {

  @SeededTest
  void canFlatten(RandomSeed seed) {
    Assertions.assertArrayEquals(new int[0], SimpleArrayUtils.flatten(null), "Null input");
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final IntOpenHashSet set = new IntOpenHashSet();
    testFlatten(set, new int[0]);
    testFlatten(set, new int[10]);
    for (int i = 0; i < 10; i++) {
      testFlatten(set, next(rng, 1, 10));
      testFlatten(set, next(rng, 10, 10));
      testFlatten(set, next(rng, 100, 10));
    }
  }

  private static void testFlatten(IntOpenHashSet set, int[] s1) {
    set.clear();
    set.addAll(IntArrayList.wrap(s1));
    final int[] e = set.toIntArray();
    Arrays.sort(e);

    final int[] o = SimpleArrayUtils.flatten(s1);
    // TestLog.debug(logger,"%s =? %s", Arrays.toString(e), Arrays.toString(o));
    Assertions.assertArrayEquals(e, o);
  }

  private static int[] next(UniformRandomProvider rng, int size, int max) {
    final int[] a = new int[size];
    for (int i = 0; i < size; i++) {
      a[i] = rng.nextInt(max);
    }
    return a;
  }

  @Test
  void canMerge() {
    final int[] data1 = {1, 2, 3, 4, 4, 4, 5, 6, 6, 7, 8, 8, 8, 9};
    final int[] data2 = {1, 5, 10};
    int[] result = SimpleArrayUtils.merge(data1, data2);
    Arrays.sort(result);
    final int[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    Assertions.assertArrayEquals(expected, result);

    result = SimpleArrayUtils.merge(data1, data2, true);
    Arrays.sort(result);
    Assertions.assertArrayEquals(expected, result, "with unique flag");
  }

  @Test
  void canConvertIntToDouble() {
    int[] array = null;
    Assertions.assertArrayEquals(new double[0], SimpleArrayUtils.toDouble(array), "Null argument");
    array = new int[] {1, 3, 7};
    final double[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toDouble(array));
  }

  @Test
  void canConvertFloatToDouble() {
    float[] array = null;
    Assertions.assertArrayEquals(new double[0], SimpleArrayUtils.toDouble(array), "Null argument");
    array = new float[] {1, 3, 7};
    final double[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toDouble(array));
  }

  @Test
  void canConvertDoubleToFloat() {
    double[] array = null;
    Assertions.assertArrayEquals(new float[0], SimpleArrayUtils.toFloat(array), "Null argument");
    array = new double[] {1, 3, 7};
    final float[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toFloat(array));
  }

  @Test
  void canConvertIntToFloat() {
    int[] array = null;
    Assertions.assertArrayEquals(new float[0], SimpleArrayUtils.toFloat(array), "Null argument");
    array = new int[] {1, 3, 7};
    final float[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toFloat(array));
  }

  @Test
  void canCreateNewDoubleArray() {
    final double[] expected = {0.5, 1.5, 2.5};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newArray(3, 0.5, 1));
  }

  @Test
  void canCreateNewFloatArray() {
    final float[] expected = {0.5f, 1.5f, 2.5f};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newArray(3, 0.5f, 1));
  }

  @Test
  void canCreateNewIntArray() {
    final int[] expected = {2, 5, 8};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newArray(3, 2, 3));
  }

  @Test
  void canCreateNewNaturalArray() {
    final int[] expected = {0, 1, 2};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.natural(3));
  }

  @Test
  void canEnsureStrictlyPositive() {
    float[] data = {1, 2, 3, 4, 5};
    Assertions.assertSame(data, SimpleArrayUtils.ensureStrictlyPositive(data),
        "Positive data should be unchanged");
    data = new float[] {1, -2, 3, 0, 5};
    float[] expected = {1, 1, 3, 1, 5};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.ensureStrictlyPositive(data),
        "Not strictly positive");
    data = new float[] {-1, -2, -1};
    expected = new float[data.length];
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.ensureStrictlyPositive(data),
        "Not array of zero with all non-positive data");
  }

  @Test
  void canFindMinAboveZero() {
    float[] data = {-1, 0, 1, 0.5f, 2};
    Assertions.assertEquals(0.5f, SimpleArrayUtils.minAboveZero(data),
        "Failed using standard array");
    data = new float[0];
    Assertions.assertEquals(Float.NaN, SimpleArrayUtils.minAboveZero(data),
        "Failed using empty array");
    data = new float[] {Float.NaN};
    Assertions.assertEquals(Float.NaN, SimpleArrayUtils.minAboveZero(data),
        "Failed using NaN array");
    data = new float[5];
    Assertions.assertEquals(Float.NaN, SimpleArrayUtils.minAboveZero(data),
        "Failed using zero filled array");
    data = new float[] {-1, -2, -1};
    Assertions.assertEquals(Float.NaN, SimpleArrayUtils.minAboveZero(data),
        "Failed using non-positive array");
  }

  @Test
  void canCreateNewFilledDoubleArray() {
    final double[] expected = {0.5, 0.5, 0.5};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newDoubleArray(3, 0.5));
  }

  @Test
  void canCreateNewFilledFloatArray() {
    final float[] expected = {0.5f, 0.5f, 0.5f};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newFloatArray(3, 0.5f));
  }

  @Test
  void canCreateNewFilledIntArray() {
    final int[] expected = {2, 2, 2};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newIntArray(3, 2));
  }

  @Test
  void canCreateNewFilledByteArray() {
    final byte[] expected = {2, 2, 2};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newByteArray(3, (byte) 2));
  }

  @Test
  void canReverseIntArray() {
    for (int size = 0; size < 5; size++) {
      final int[] data = new int[size];
      final int[] expected = new int[size];
      for (int i = 0, j = size - 1; i < size; i++, j--) {
        data[i] = i;
        expected[j] = i;
      }
      SimpleArrayUtils.reverse(data);
      Assertions.assertArrayEquals(expected, data);
    }
  }

  @Test
  void canReverseFloatArray() {
    for (int size = 0; size < 5; size++) {
      final float[] data = new float[size];
      final float[] expected = new float[size];
      for (int i = 0, j = size - 1; i < size; i++, j--) {
        data[i] = i;
        expected[j] = i;
      }
      SimpleArrayUtils.reverse(data);
      Assertions.assertArrayEquals(expected, data);
    }
  }

  @Test
  void canReverseDoubleArray() {
    for (int size = 0; size < 5; size++) {
      final double[] data = new double[size];
      final double[] expected = new double[size];
      for (int i = 0, j = size - 1; i < size; i++, j--) {
        data[i] = i;
        expected[j] = i;
      }
      SimpleArrayUtils.reverse(data);
      Assertions.assertArrayEquals(expected, data);
    }
  }

  @Test
  void canReverseByteArray() {
    for (int size = 0; size < 5; size++) {
      final byte[] data = new byte[size];
      final byte[] expected = new byte[size];
      for (int i = 0, j = size - 1; i < size; i++, j--) {
        data[i] = (byte) i;
        expected[j] = (byte) i;
      }
      SimpleArrayUtils.reverse(data);
      Assertions.assertArrayEquals(expected, data);
    }
  }

  @Test
  void canReverseShortArray() {
    for (int size = 0; size < 5; size++) {
      final short[] data = new short[size];
      final short[] expected = new short[size];
      for (int i = 0, j = size - 1; i < size; i++, j--) {
        data[i] = (short) i;
        expected[j] = (short) i;
      }
      SimpleArrayUtils.reverse(data);
      Assertions.assertArrayEquals(expected, data);
    }
  }

  @Test
  void canTestDoubleIsInteger() {
    Assertions.assertTrue(SimpleArrayUtils.isInteger(new double[0]), "Empty array");
    final double[] data = new double[] {Integer.MIN_VALUE, 1, 2, Integer.MAX_VALUE};
    Assertions.assertTrue(SimpleArrayUtils.isInteger(data), "Full range int array");
    data[0] -= 1;
    Assertions.assertFalse(SimpleArrayUtils.isInteger(data), "Exceed full range int array");
    data[0] = 0.5;
    Assertions.assertFalse(SimpleArrayUtils.isInteger(data), "non-int values in array");
  }

  @Test
  void canTestFloatIsInteger() {
    Assertions.assertTrue(SimpleArrayUtils.isInteger(new float[0]), "Empty array");
    final float[] data = new float[] {1, 2, 3};
    Assertions.assertTrue(SimpleArrayUtils.isInteger(data), "Valid int array");
    data[0] = 0.5f;
    Assertions.assertFalse(SimpleArrayUtils.isInteger(data), "non-int values in array");
  }

  @Test
  void canTestIntIsUniform() {
    for (int size = 0; size < 2; size++) {
      Assertions.assertTrue(SimpleArrayUtils.isUniform(new int[size]), "Below min size");
    }
    int[] data = new int[] {3, 5, 7};
    Assertions.assertTrue(SimpleArrayUtils.isUniform(data), "Valid uniform array");
    data[0] -= 1;
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data), "Non-valid uniform array");
    data = new int[] {3, 3, 7};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data), "No reference first interval");
    data = new int[] {Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Integer.MAX_VALUE + 1};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data), "Overflow in series");
    data = new int[] {Integer.MIN_VALUE - 1, Integer.MIN_VALUE, Integer.MIN_VALUE + 1};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data), "Overflow in series");
  }

  @Test
  void canTestDoubleIsUniform() {
    for (int size = 0; size < 3; size++) {
      Assertions.assertTrue(SimpleArrayUtils.isUniform(new double[size], 0), "Below min size");
    }
    double[] data = new double[] {3, 5, 7, 9};
    Assertions.assertTrue(SimpleArrayUtils.isUniform(data, 0), "Valid uniform array");
    data[0] -= 1;
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0), "Non-valid uniform array");
    data = new double[] {3, 4, 6};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0),
        "Not within tolerance on 2nd interval");
    data = new double[] {3, 4, 6};
    Assertions.assertTrue(SimpleArrayUtils.isUniform(data, 2), "Within tolerance on 2nd interval");
    data = new double[] {2, 3, 4, 6};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0),
        "Not within tolerance on 3rd interval");
    data = new double[] {2, 3, 4, 6};
    Assertions.assertTrue(SimpleArrayUtils.isUniform(data, 2), "Within tolerance on 3rd interval");
    data = new double[] {3, 3, 7};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0), "No reference first interval");
    data = new double[] {3, Double.NaN, 5};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0), "NaN in first interval");
    data = new double[] {3, 4, Double.NaN, 6};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0), "NaN in series");
    data = new double[] {3, 4, Double.POSITIVE_INFINITY, 6};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 0), "+Infinity in series");

    // Special case of increasing interval.
    // Each interval is within the absolute tolerance of the last interval.
    final double tolerance = 0.01;
    double interval = 1;
    final double increase = tolerance * 0.9999;
    data = new double[10];
    for (int i = 1; i < data.length; i++) {
      data[i] = data[i - 1] + interval;
      interval += increase;
    }
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, tolerance),
        "increasing step size in series");

    // Special case of direction change
    data = new double[] {0, 1, 2, 1};
    Assertions.assertFalse(SimpleArrayUtils.isUniform(data, 1), "direction change in series");
  }

  @Test
  void canMultiplyFloat() {
    final float[] data = {1, 2, 3};
    final float[] expected = {2, 4, 6};
    SimpleArrayUtils.multiply(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canMultiplyFloatByDouble() {
    final float[] data = {1, 2, 3};
    final float[] expected = {2, 4, 6};
    SimpleArrayUtils.multiply(data, 2.0);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canMultiplyDouble() {
    final double[] data = {1, 2, 3};
    final double[] expected = {2, 4, 6};
    SimpleArrayUtils.multiply(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canAddFloat() {
    final float[] data = {1, 2, 3};
    final float[] expected = {3, 4, 5};
    SimpleArrayUtils.add(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canAddDouble() {
    final double[] data = {1, 2, 3};
    final double[] expected = {3, 4, 5};
    SimpleArrayUtils.add(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canAddInt() {
    final int[] data = {1, 2, 3};
    final int[] expected = {3, 4, 5};
    SimpleArrayUtils.add(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canSubtractInt() {
    final int[] data = {3, 4, 5};
    final int[] expected = {1, 2, 3};
    SimpleArrayUtils.subtract(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canFindIntMinIndex() {
    final int[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = -1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMinIndex(data));
      data[i] = 0;
    }
  }

  @Test
  void canFindFloatMinIndex() {
    final float[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = -1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMinIndex(data));
      data[i] = 0;
    }
  }

  @Test
  void canFindDoubleMinIndex() {
    final double[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = -1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMinIndex(data));
      data[i] = 0;
    }
  }

  @Test
  void canFindIntMaxIndex() {
    final int[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = 1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMaxIndex(data));
      data[i] = 0;
    }
  }

  @Test
  void canFindFloatMaxIndex() {
    final float[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = 1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMaxIndex(data));
      data[i] = 0;
    }
  }

  @Test
  void canFindDoubleMaxIndex() {
    final double[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = 1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMaxIndex(data));
      data[i] = 0;
    }
  }

  @Test
  void canFindIntMinMaxIndex() {
    final int[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      final int j = (i + 1) % data.length;
      data[i] = -1;
      data[j] = 1;
      Assertions.assertArrayEquals(new int[] {i, j}, SimpleArrayUtils.findMinMaxIndex(data));
      data[i] = 0;
      data[j] = 0;
    }
  }

  @Test
  void canFindFloatMinMaxIndex() {
    final float[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      final int j = (i + 1) % data.length;
      data[i] = -1;
      data[j] = 1;
      Assertions.assertArrayEquals(new int[] {i, j}, SimpleArrayUtils.findMinMaxIndex(data));
      data[i] = 0;
      data[j] = 0;
    }
  }

  @Test
  void canFindDoubleMinMaxIndex() {
    final double[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      final int j = (i + 1) % data.length;
      data[i] = -1;
      data[j] = 1;
      Assertions.assertArrayEquals(new int[] {i, j}, SimpleArrayUtils.findMinMaxIndex(data));
      data[i] = 0;
      data[j] = 0;
    }
  }

  @Test
  void canFindIntIndex() {
    Assertions.assertEquals(-1, SimpleArrayUtils.findIndex(new int[0], j -> j == 10));
    final int[] data = {1, 2, 3, 1, 2};
    Assertions.assertEquals(-1, SimpleArrayUtils.findIndex(data, j -> j == 10));
    Assertions.assertEquals(0, SimpleArrayUtils.findIndex(data, j -> j == 1));
    Assertions.assertEquals(1, SimpleArrayUtils.findIndex(data, j -> j == 2));
    Assertions.assertEquals(2, SimpleArrayUtils.findIndex(data, j -> j == 3));
  }

  @Test
  void canFindFloatIndex() {
    Assertions.assertEquals(-1, SimpleArrayUtils.findIndex(new float[0], j -> j == 10));
    final float[] data = {1, 2, 3, 1, 2};
    Assertions.assertEquals(-1, SimpleArrayUtils.findIndex(data, j -> j == 10));
    Assertions.assertEquals(0, SimpleArrayUtils.findIndex(data, j -> j == 1));
    Assertions.assertEquals(1, SimpleArrayUtils.findIndex(data, j -> j == 2));
    Assertions.assertEquals(2, SimpleArrayUtils.findIndex(data, j -> j == 3));
  }

  @Test
  void canFindDoubleIndex() {
    Assertions.assertEquals(-1, SimpleArrayUtils.findIndex(new double[0], j -> j == 10));
    final double[] data = {1, 2, 3, 1, 2};
    Assertions.assertEquals(-1, SimpleArrayUtils.findIndex(data, j -> j == 10));
    Assertions.assertEquals(0, SimpleArrayUtils.findIndex(data, j -> j == 1));
    Assertions.assertEquals(1, SimpleArrayUtils.findIndex(data, j -> j == 2));
    Assertions.assertEquals(2, SimpleArrayUtils.findIndex(data, j -> j == 3));
  }

  @Test
  void canFindLastIntIndex() {
    Assertions.assertEquals(-1, SimpleArrayUtils.findLastIndex(new int[0], j -> j == 10));
    final int[] data = {1, 2, 3, 1, 2};
    Assertions.assertEquals(-1, SimpleArrayUtils.findLastIndex(data, j -> j == 10));
    Assertions.assertEquals(3, SimpleArrayUtils.findLastIndex(data, j -> j == 1));
    Assertions.assertEquals(4, SimpleArrayUtils.findLastIndex(data, j -> j == 2));
    Assertions.assertEquals(2, SimpleArrayUtils.findLastIndex(data, j -> j == 3));
  }

  @Test
  void canFindLastFloatIndex() {
    Assertions.assertEquals(-1, SimpleArrayUtils.findLastIndex(new float[0], j -> j == 10));
    final float[] data = {1, 2, 3, 1, 2};
    Assertions.assertEquals(-1, SimpleArrayUtils.findLastIndex(data, j -> j == 10));
    Assertions.assertEquals(3, SimpleArrayUtils.findLastIndex(data, j -> j == 1));
    Assertions.assertEquals(4, SimpleArrayUtils.findLastIndex(data, j -> j == 2));
    Assertions.assertEquals(2, SimpleArrayUtils.findLastIndex(data, j -> j == 3));
  }

  @Test
  void canFindLastDoubleIndex() {
    Assertions.assertEquals(-1, SimpleArrayUtils.findLastIndex(new double[0], j -> j == 10));
    final double[] data = {1, 2, 3, 1, 2};
    Assertions.assertEquals(-1, SimpleArrayUtils.findLastIndex(data, j -> j == 10));
    Assertions.assertEquals(3, SimpleArrayUtils.findLastIndex(data, j -> j == 1));
    Assertions.assertEquals(4, SimpleArrayUtils.findLastIndex(data, j -> j == 2));
    Assertions.assertEquals(2, SimpleArrayUtils.findLastIndex(data, j -> j == 3));
  }

  @Test
  void canGetRanges() {
    testGetRanges(null, new int[0]);
    testGetRanges(new int[0], new int[0]);
    testGetRanges(new int[] {0}, new int[] {0, 0});
    testGetRanges(new int[] {1}, new int[] {1, 1});
    testGetRanges(new int[] {0, 1}, new int[] {0, 1});
    testGetRanges(new int[] {0, 1, 2, 3}, new int[] {0, 3});
    testGetRanges(new int[] {0, 1, 3, 4, 5, 7}, new int[] {0, 1, 3, 5, 7, 7});
    testGetRanges(new int[] {0, 3, 5, 7}, new int[] {0, 0, 3, 3, 5, 5, 7, 7});
    testGetRanges(new int[] {-1, 0, 1}, new int[] {-1, 1});
    testGetRanges(new int[] {-2, -1, 1}, new int[] {-2, -1, 1, 1});

    // With duplicates
    testGetRanges(new int[] {0}, new int[] {0, 0});
    testGetRanges(new int[] {1}, new int[] {1, 1});
    testGetRanges(new int[] {0, 1}, new int[] {0, 1});
    testGetRanges(new int[] {0, 1, 2, 3}, new int[] {0, 3});
    testGetRanges(new int[] {0, 1, 3, 4, 5, 7}, new int[] {0, 1, 3, 5, 7, 7});
    testGetRanges(new int[] {0, 3, 5, 7}, new int[] {0, 0, 3, 3, 5, 5, 7, 7});
    testGetRanges(new int[] {-1, 0, 1}, new int[] {-1, 1});
    testGetRanges(new int[] {-2, -1, 1}, new int[] {-2, -1, 1, 1});
  }

  @Test
  void canGetRangesWithDuplicates() {
    testGetRanges(new int[] {0, 0, 0}, new int[] {0, 0});
    testGetRanges(new int[] {1, 1}, new int[] {1, 1});
    testGetRanges(new int[] {0, 1, 1}, new int[] {0, 1});
    testGetRanges(new int[] {0, 1, 2, 2, 2, 3, 3}, new int[] {0, 3});
    testGetRanges(new int[] {0, 1, 1, 3, 3, 4, 5, 7, 7}, new int[] {0, 1, 3, 5, 7, 7});
    testGetRanges(new int[] {0, 3, 5, 5, 5, 7}, new int[] {0, 0, 3, 3, 5, 5, 7, 7});
    testGetRanges(new int[] {-1, 0, 0, 0, 1, 1}, new int[] {-1, 1});
    testGetRanges(new int[] {-2, -2, -1, 1}, new int[] {-2, -1, 1, 1});
  }

  private static void testGetRanges(int[] in, int[] expected) {
    final int[] observed = SimpleArrayUtils.getRanges(in);
    Assertions.assertArrayEquals(expected, observed);
  }

  @Test
  void canCheck2DSize() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.check2DSize(-1, 1), "negative width");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.check2DSize(1, -1), "negative height");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.check2DSize(Integer.MAX_VALUE, Integer.MAX_VALUE), "max value");
    SimpleArrayUtils.check2DSize(1, 1);
    SimpleArrayUtils.check2DSize(0, 0);
  }

  @Test
  void canHas2DFloatData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (float[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new float[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new float[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new float[1]);
  }

  @Test
  void canHas2DDoubleData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (double[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new double[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new double[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new double[1]);
  }

  @Test
  void canHas2DIntData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (int[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new int[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new int[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new int[1]);
  }

  @Test
  void canHas2DByteData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (byte[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new byte[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new byte[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new byte[1]);
  }

  @Test
  void canCheckIsArray() {
    Assertions.assertTrue(SimpleArrayUtils.isArray(new int[0]), "int[] data");
    Assertions.assertTrue(SimpleArrayUtils.isArray(new Object[0]), "Object[] data");
    Assertions.assertTrue(SimpleArrayUtils.isArray(new int[0][0]), "int[][] data");
    Assertions.assertFalse(SimpleArrayUtils.isArray(null), "null data");
    Assertions.assertFalse(SimpleArrayUtils.isArray(new Object()), "invalid array object");
  }

  @Test
  void canToString() {
    Assertions.assertEquals("null", SimpleArrayUtils.toString(null));
    final String expected = "Not an array";
    Assertions.assertEquals(expected, SimpleArrayUtils.toString(expected));

    Assertions.assertEquals("[0.5, 1.0]", SimpleArrayUtils.toString(new float[] {0.5f, 1f}));
    Assertions.assertEquals("[0.5, 1.0]", SimpleArrayUtils.toString(new double[] {0.5, 1}));

    Assertions.assertEquals("[c, a]", SimpleArrayUtils.toString(new char[] {'c', 'a'}));

    Assertions.assertEquals("[true, false]",
        SimpleArrayUtils.toString(new boolean[] {true, false}));

    Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new byte[] {2, 1}));
    Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new short[] {2, 1}));
    Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new int[] {2, 1}));
    Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new long[] {2, 1}));

    // Check objects
    Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new Object[] {2, 1}));
    Assertions.assertEquals("[foo, bar]", SimpleArrayUtils.toString(new Object[] {"foo", "bar"}));
    Assertions.assertEquals("[foo, 1]", SimpleArrayUtils.toString(new Object[] {"foo", 1}));

    // Check recursion
    final Object[] array = new int[][] {{2, 1}, {3, 4}};
    Assertions.assertEquals(Arrays.deepToString(array), SimpleArrayUtils.toString(array),
        "Default Array.deepToString");
    Assertions.assertEquals(Arrays.toString(array), SimpleArrayUtils.toString(array, false),
        "Expected Array.toString");
  }

  @Test
  void canDeepCopyDouble2DArray() {
    final double[][] data = {{1, 2, 3}, {44, 55, 66}};
    final double[][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  void canDeepCopyFloat2DArray() {
    final float[][] data = {{1, 2, 3}, {44, 55, 66}};
    final float[][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  void canDeepCopyInt2DArray() {
    final int[][] data = {{1, 2, 3}, {44, 55, 66}};
    final int[][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  void canDeepCopyDouble3DArray() {
    final double[][][] data = {{{1, 2, 3}, {44, 55, 66}}, {{9, 8}, {4, 3}}};
    final double[][][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  void canDeepCopyFloat3DArray() {
    final float[][][] data = {{{1, 2, 3}, {44, 55, 66}}, {{9, 8}, {4, 3}}};
    final float[][][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  void canDeepCopyInt3DArray() {
    final int[][][] data = {{{1, 2, 3}, {44, 55, 66}}, {{9, 8}, {4, 3}}};
    final int[][][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  void canTestDoubleIsFinite() {
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new double[0]), "zero length array");
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new double[10]), "non-zero length array");
    for (final double value : new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        Double.NaN}) {
      Assertions.assertFalse(SimpleArrayUtils.isFinite(new double[] {value}),
          () -> Double.toString(value));
    }
  }

  @Test
  void canTestFloatIsFinite() {
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new float[0]), "zero length array");
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new float[10]), "non-zero length array");
    for (final float value : new float[] {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
        Float.NaN}) {
      Assertions.assertFalse(SimpleArrayUtils.isFinite(new float[] {value}),
          () -> Float.toString(value));
    }
  }

  @Test
  void canSwapIntData() {
    final int[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new int[] {4, 3}, data);
  }

  @Test
  void canSwapFloatData() {
    final float[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new float[] {4, 3}, data);
  }

  @Test
  void canSwapDoubleData() {
    final double[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new double[] {4, 3}, data);
  }

  @Test
  void canSwapByteData() {
    final byte[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new byte[] {4, 3}, data);
  }

  @Test
  void canSwapShortData() {
    final short[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new short[] {4, 3}, data);
  }

  @Test
  void canSwapTData() {
    final String[] data = {"3", "4"};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new String[] {"4", "3"}, data);
  }

  @SuppressWarnings("null")
  @Test
  void canEnsureFloatSize() {
    int[] data = null;
    for (int i = 0; i < 3; i++) {
      data = SimpleArrayUtils.ensureSize(data, i);
      Assertions.assertEquals(i, data.length, "Did not resize");
    }
    for (int i = data.length; i > 0; i--) {
      final int[] newData = SimpleArrayUtils.ensureSize(data, data.length - 1);
      Assertions.assertSame(data, newData, "Should not create a new array");
    }
  }

  @SuppressWarnings("null")
  @Test
  void canEnsureIntSize() {
    float[] data = null;
    for (int i = 0; i < 3; i++) {
      data = SimpleArrayUtils.ensureSize(data, i);
      Assertions.assertEquals(i, data.length, "Did not resize");
    }
    for (int i = data.length; i > 0; i--) {
      final float[] newData = SimpleArrayUtils.ensureSize(data, data.length - 1);
      Assertions.assertSame(data, newData, "Should not create a new array");
    }
  }

  @SuppressWarnings("null")
  @Test
  void canEnsureDoubleSize() {
    double[] data = null;
    for (int i = 0; i < 3; i++) {
      data = SimpleArrayUtils.ensureSize(data, i);
      Assertions.assertEquals(i, data.length, "Did not resize");
    }
    for (int i = data.length; i > 0; i--) {
      final double[] newData = SimpleArrayUtils.ensureSize(data, data.length - 1);
      Assertions.assertSame(data, newData, "Should not create a new array");
    }
  }

  @SuppressWarnings("null")
  @Test
  void canEnsurByteSize() {
    byte[] data = null;
    for (int i = 0; i < 3; i++) {
      data = SimpleArrayUtils.ensureSize(data, i);
      Assertions.assertEquals(i, data.length, "Did not resize");
    }
    for (int i = data.length; i > 0; i--) {
      final byte[] newData = SimpleArrayUtils.ensureSize(data, data.length - 1);
      Assertions.assertSame(data, newData, "Should not create a new array");
    }
  }

  @SuppressWarnings("null")
  @Test
  void canEnsureShortSize() {
    short[] data = null;
    for (int i = 0; i < 3; i++) {
      data = SimpleArrayUtils.ensureSize(data, i);
      Assertions.assertEquals(i, data.length, "Did not resize");
    }
    for (int i = data.length; i > 0; i--) {
      final short[] newData = SimpleArrayUtils.ensureSize(data, data.length - 1);
      Assertions.assertSame(data, newData, "Should not create a new array");
    }
  }

  @Test
  void canGetIndex() {
    final Object o1 = new Object();
    final Object o2 = new Object();
    final Object[] array = {o1};
    Assertions.assertNull(SimpleArrayUtils.getIndex(-1, array, null),
        "negative index, default null");
    Assertions.assertNull(SimpleArrayUtils.getIndex(1, array, null),
        "out-of-bounds index, default null");
    Assertions.assertSame(o1, SimpleArrayUtils.getIndex(0, array, null),
        "good index, default null");
    Assertions.assertSame(o2, SimpleArrayUtils.getIndex(-1, array, o2),
        "negative index, default object");
    Assertions.assertSame(o2, SimpleArrayUtils.getIndex(1, array, o2),
        "out-of-bounds index, default object");
  }

  @Test
  void canApplyIntOperator() {
    final int[] data = {1, 2, 3};
    final int[] expected = {2, 4, 6};
    SimpleArrayUtils.apply(data, v -> v * 2);
    Assertions.assertArrayEquals(new int[] {2, 4, 6}, data);
  }

  @Test
  void canApplyLongOperator() {
    final long[] data = {1, 2, 3};
    final long[] expected = {2, 4, 6};
    SimpleArrayUtils.apply(data, v -> v * 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canApplyFloatOperator() {
    final float[] data = {1, 2, 3};
    final float[] expected = {2, 4, 6};
    SimpleArrayUtils.apply(data, v -> v * 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canApplyDoubleOperator() {
    final double[] data = {1, 2, 3};
    final double[] expected = {2, 4, 6};
    SimpleArrayUtils.apply(data, v -> v * 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  void canApplyIntOperatorWithRange() {
    final int[] data = {1, 2, 3};
    java.util.function.IntUnaryOperator fun = v -> v * 2;
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, -1, data.length, fun));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, 0, data.length + 1, fun));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.apply(data, 2, 1, fun));
    SimpleArrayUtils.apply(data, 0, 0, fun);
    Assertions.assertArrayEquals(data, data);
    SimpleArrayUtils.apply(data, 0, 1, fun);
    Assertions.assertArrayEquals(new int[] {2, 2, 3}, data);
    SimpleArrayUtils.apply(data, 1, 3, fun);
    Assertions.assertArrayEquals(new int[] {2, 4, 6}, data);
    SimpleArrayUtils.apply(data, 0, 3, fun);
    Assertions.assertArrayEquals(new int[] {4, 8, 12}, data);
  }

  @Test
  void canApplyLongOperatorWithRange() {
    final long[] data = {1, 2, 3};
    java.util.function.LongUnaryOperator fun = v -> v * 2;
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, -1, data.length, fun));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, 0, data.length + 1, fun));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.apply(data, 2, 1, fun));
    SimpleArrayUtils.apply(data, 0, 0, fun);
    Assertions.assertArrayEquals(data, data);
    SimpleArrayUtils.apply(data, 0, 1, fun);
    Assertions.assertArrayEquals(new long[] {2, 2, 3}, data);
    SimpleArrayUtils.apply(data, 1, 3, fun);
    Assertions.assertArrayEquals(new long[] {2, 4, 6}, data);
    SimpleArrayUtils.apply(data, 0, 3, fun);
    Assertions.assertArrayEquals(new long[] {4, 8, 12}, data);
  }

  @Test
  void canApplyFloatOperatorWithRange() {
    final float[] data = {1, 2, 3};
    uk.ac.sussex.gdsc.core.utils.function.FloatUnaryOperator fun = v -> v * 2;
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, -1, data.length, fun));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, 0, data.length + 1, fun));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.apply(data, 2, 1, fun));
    SimpleArrayUtils.apply(data, 0, 0, fun);
    Assertions.assertArrayEquals(data, data);
    SimpleArrayUtils.apply(data, 0, 1, fun);
    Assertions.assertArrayEquals(new float[] {2, 2, 3}, data);
    SimpleArrayUtils.apply(data, 1, 3, fun);
    Assertions.assertArrayEquals(new float[] {2, 4, 6}, data);
    SimpleArrayUtils.apply(data, 0, 3, fun);
    Assertions.assertArrayEquals(new float[] {4, 8, 12}, data);
  }

  @Test
  void canApplyDoubleOperatorWithRange() {
    final double[] data = {1, 2, 3};
    java.util.function.DoubleUnaryOperator fun = v -> v * 2;
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, -1, data.length, fun));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> SimpleArrayUtils.apply(data, 0, data.length + 1, fun));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.apply(data, 2, 1, fun));
    SimpleArrayUtils.apply(data, 0, 0, fun);
    Assertions.assertArrayEquals(data, data);
    SimpleArrayUtils.apply(data, 0, 1, fun);
    Assertions.assertArrayEquals(new double[] {2, 2, 3}, data);
    SimpleArrayUtils.apply(data, 1, 3, fun);
    Assertions.assertArrayEquals(new double[] {2, 4, 6}, data);
    SimpleArrayUtils.apply(data, 0, 3, fun);
    Assertions.assertArrayEquals(new double[] {4, 8, 12}, data);
  }

  @Test
  void canFillWithIntFunction() {
    final Number[] data1 = new Number[3];
    final Number[] data = SimpleArrayUtils.fill(data1, i -> Integer.valueOf(i + 1));
    Assertions.assertSame(data1, data);
    Assertions.assertArrayEquals(new Integer[] {1, 2, 3}, data);
  }

  @Test
  void canFillWithSupplier() {
    final String[] data1 = new String[3];
    final String[] data = SimpleArrayUtils.fill(data1, () -> "");
    Assertions.assertSame(data1, data);
    Assertions.assertArrayEquals(new String[] {"", "", ""}, data);
  }
}
