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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TimingService;

@SuppressWarnings({"javadoc"})
class PartialSortTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(PartialSortTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private abstract class MyTimingTaskDouble extends BaseTimingTask {
    double[][] data;

    public MyTimingTaskDouble(String name, double[][] data) {
      super(name);
      this.data = data;
    }

    @Override
    public int getSize() {
      return data.length;
    }

    @Override
    public Object getData(int index) {
      return data[index].clone();
    }
  }

  private abstract class MyTimingTaskFloat extends BaseTimingTask {
    float[][] data;

    public MyTimingTaskFloat(String name, float[][] data) {
      super(name);
      this.data = data;
    }

    @Override
    public int getSize() {
      return data.length;
    }

    @Override
    public Object getData(int index) {
      return data[index].clone();
    }
  }

  int[] testN = new int[] {2, 3, 5, 10, 30, 50};
  int[] testM = new int[] {50, 100};

  // XXX copy double from here

  @SeededTest
  void bottomNofMIsCorrectDouble(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (final int size : testN) {
      for (final int total : testM) {
        bottomComputeDouble(rng, 100, size, total);
      }
    }
  }

  static double[] bottomDouble(int size, double[] values) {
    bottomSortDouble(values);
    return Arrays.copyOf(values, size);
  }

  static void bottomSortDouble(double[] values) {
    Arrays.sort(values);
  }

  @Test
  void bottomCanHandleNullDataDouble() {
    Assertions.assertEquals(0, PartialSort.bottom((double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (double[]) null, 5, 3).length);
  }

  @Test
  void bottomCanHandleEmptyDataDouble() {
    final double[] o = PartialSort.bottom(ArrayUtils.EMPTY_DOUBLE_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void bottomCanHandleIncompleteDataDouble() {
    final double[] d = {1, 3, 2};
    final double[] e = {1, 2, 3};
    final double[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void bottomCanHandleNaNDataDouble() {
    Assertions.assertArrayEquals(new double[0],
        PartialSort.bottom(new double[] {Double.NaN, Double.NaN}, 1));
    final double[] d = {1, 2, Double.NaN, 3};
    final double[] e = {1, 2, 3};
    final double[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private void bottomComputeDouble(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final double[][] data = createDataDouble(rng, length, total);
    final String msg = String.format(" %d of %d", size, total);

    final MyTimingTaskDouble expected = new MyTimingTaskDouble("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return bottomDouble(size, (double[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    final TimingService ts = new TimingService(runs);
    ts.execute(expected);
    ts.execute(new MyTimingTaskDouble("bottomSort" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom((double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskDouble("bottomHead" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertEquals(e[size - 1], o[0], () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskDouble("bottom" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom(0, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(size);
    ts.execute(new MyTimingTaskDouble("DoubleSelector" + msg, data) {
      @Override
      public Object run(Object data) {
        return ps.bottom(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(size);
    ts.execute(new MyTimingTaskDouble("DoubleMinHeap" + msg, data) {
      @Override
      public Object run(Object data) {
        return heap.bottom(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskDouble("select" + msg, data) {
      @Override
      public Object run(Object data) {
        final double[] arr = (double[]) data;
        PartialSort.select(size - 1, arr.length, arr);
        return Arrays.copyOf(arr, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    // Sometimes this fails
    // if ((double) size / total > 0.5)
    // Assertions.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(),
    // ts.get(1).getMean()),
    // ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

    ts.check();

    if (runs > 1) {
      logger.info(ts.getReport());
    }
  }

  @SeededTest
  void topNofMIsCorrectDouble(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (final int size : testN) {
      for (final int total : testM) {
        topComputeDouble(rng, 100, size, total);
      }
    }
  }

  static double[] topDouble(int size, double[] values) {
    topSortDouble(values);
    return Arrays.copyOf(values, size);
  }

  static void topSortDouble(double[] values) {
    Arrays.sort(values);
    SimpleArrayUtils.reverse(values);
  }

  @Test
  void topCanHandleNullDataDouble() {
    Assertions.assertEquals(0, PartialSort.top((double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (double[]) null, 5, 3).length);
  }

  @Test
  void topCanHandleEmptyDataDouble() {
    final double[] o = PartialSort.top(ArrayUtils.EMPTY_DOUBLE_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void topCanHandleIncompleteDataDouble() {
    final double[] d = {1, 3, 2};
    final double[] e = {3, 2, 1};
    final double[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void topCanHandleNaNDataDouble() {
    Assertions.assertArrayEquals(new double[0],
        PartialSort.top(new double[] {Double.NaN, Double.NaN}, 1));
    final double[] d = {1, 2, Double.NaN, 3};
    final double[] e = {3, 2, 1};
    final double[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private void topComputeDouble(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final double[][] data = createDataDouble(rng, length, total);
    final String msg = String.format(" %d of %d", size, total);

    final MyTimingTaskDouble expected = new MyTimingTaskDouble("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return topDouble(size, (double[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    final TimingService ts = new TimingService(runs);
    ts.execute(expected);
    ts.execute(new MyTimingTaskDouble("topSort" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top((double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskDouble("topHead" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top(PartialSort.OPTION_HEAD_FIRST, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertEquals(e[size - 1], o[0], () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskDouble("top" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top(0, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        topSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(size);
    ts.execute(new MyTimingTaskDouble("DoubleSelector" + msg, data) {
      @Override
      public Object run(Object data) {
        return ps.top(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        topSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(size);
    ts.execute(new MyTimingTaskDouble("DoubleMinHeap" + msg, data) {
      @Override
      public Object run(Object data) {
        return heap.top(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        topSortDouble(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    // // Sometimes this fails
    // if ((double) size / total > 0.5)
    // Assertions.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(),
    // ts.get(1).getMean()),
    // ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

    ts.check();

    if (runs > 1) {
      logger.info(ts.getReport());
    }
  }

  private static double[][] createDataDouble(UniformRandomProvider rng, int size, int total) {
    final double[][] data = new double[size][];
    for (int i = 0; i < size; i++) {
      final double[] d = new double[total];
      for (int j = 0; j < total; j++) {
        d[j] = rng.nextDouble() * 4 * Math.PI;
      }
      data[i] = d;
    }
    return data;
  }

  // XXX copy to here

  @SeededTest
  void bottomNofMIsCorrectFloat(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (final int size : testN) {
      for (final int total : testM) {
        bottomComputeFloat(rng, 100, size, total);
      }
    }
  }

  static float[] bottomFloat(int size, float[] values) {
    bottomSortFloat(values);
    return Arrays.copyOf(values, size);
  }

  static void bottomSortFloat(float[] values) {
    Arrays.sort(values);
  }

  @Test
  void bottomCanHandleNullDataFloat() {
    Assertions.assertEquals(0, PartialSort.bottom((float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (float[]) null, 5, 3).length);
  }

  @Test
  void bottomCanHandleEmptyDataFloat() {
    final float[] o = PartialSort.bottom(ArrayUtils.EMPTY_FLOAT_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void bottomCanHandleIncompleteDataFloat() {
    final float[] d = {1, 3, 2};
    final float[] e = {1, 2, 3};
    final float[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void bottomCanHandleNaNDataFloat() {
    Assertions.assertArrayEquals(new float[0],
        PartialSort.bottom(new float[] {Float.NaN, Float.NaN}, 1));
    final float[] d = {1, 2, Float.NaN, 3};
    final float[] e = {1, 2, 3};
    final float[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private void bottomComputeFloat(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final float[][] data = createDataFloat(rng, length, total);
    final String msg = String.format(" %d of %d", size, total);

    final MyTimingTaskFloat expected = new MyTimingTaskFloat("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return bottomFloat(size, (float[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    final TimingService ts = new TimingService(runs);
    ts.execute(expected);
    ts.execute(new MyTimingTaskFloat("bottomSort" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom((float[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskFloat("bottomHead" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, (float[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        Assertions.assertEquals(e[size - 1], o[0], () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskFloat("bottom" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom(0, (float[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        bottomSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    final PartialSort.FloatSelector ps = new PartialSort.FloatSelector(size);
    ts.execute(new MyTimingTaskFloat("FloatSelector" + msg, data) {
      @Override
      public Object run(Object data) {
        return ps.bottom(0, (float[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        bottomSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    final PartialSort.FloatHeap heap = new PartialSort.FloatHeap(size);
    ts.execute(new MyTimingTaskFloat("FloatMinHeap" + msg, data) {
      @Override
      public Object run(Object data) {
        return heap.bottom(0, (float[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        bottomSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskFloat("select" + msg, data) {
      @Override
      public Object run(Object data) {
        final float[] arr = (float[]) data;
        PartialSort.select(size - 1, arr.length, arr);
        return Arrays.copyOf(arr, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        bottomSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    // Sometimes this fails
    // if ((float) size / total > 0.5)
    // Assertions.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(),
    // ts.get(1).getMean()),
    // ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

    ts.check();

    if (runs > 1) {
      logger.info(ts.getReport());
    }
  }

  @SeededTest
  void topNofMIsCorrectFloat(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (final int size : testN) {
      for (final int total : testM) {
        topComputeFloat(rng, 100, size, total);
      }
    }
  }

  static float[] topFloat(int size, float[] values) {
    topSortFloat(values);
    return Arrays.copyOf(values, size);
  }

  static void topSortFloat(float[] values) {
    Arrays.sort(values);
    SimpleArrayUtils.reverse(values);
  }

  @Test
  void topCanHandleNullDataFloat() {
    Assertions.assertEquals(0, PartialSort.top((float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (float[]) null, 5, 3).length);
  }

  @Test
  void topCanHandleEmptyDataFloat() {
    final float[] o = PartialSort.top(ArrayUtils.EMPTY_FLOAT_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void topCanHandleIncompleteDataFloat() {
    final float[] d = {1, 3, 2};
    final float[] e = {3, 2, 1};
    final float[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void topCanHandleNaNDataFloat() {
    Assertions.assertArrayEquals(new float[0],
        PartialSort.top(new float[] {Float.NaN, Float.NaN}, 1));
    final float[] d = {1, 2, Float.NaN, 3};
    final float[] e = {3, 2, 1};
    final float[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private void topComputeFloat(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final float[][] data = createDataFloat(rng, length, total);
    final String msg = String.format(" %d of %d", size, total);

    final MyTimingTaskFloat expected = new MyTimingTaskFloat("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return topFloat(size, (float[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    final TimingService ts = new TimingService(runs);
    ts.execute(expected);
    ts.execute(new MyTimingTaskFloat("topSort" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top((float[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskFloat("topHead" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top(PartialSort.OPTION_HEAD_FIRST, (float[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        Assertions.assertEquals(e[size - 1], o[0], () -> this.getName());
      }
    });
    ts.execute(new MyTimingTaskFloat("top" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top(0, (float[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        topSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });
    final PartialSort.FloatSelector ps = new PartialSort.FloatSelector(size);
    ts.execute(new MyTimingTaskFloat("FloatSelector" + msg, data) {
      @Override
      public Object run(Object data) {
        return ps.top(0, (float[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        topSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    final PartialSort.FloatHeap heap = new PartialSort.FloatHeap(size);
    ts.execute(new MyTimingTaskFloat("FloatMinHeap" + msg, data) {
      @Override
      public Object run(Object data) {
        return heap.top(0, (float[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final float[] e = (float[]) expected.run(expected.getData(index));
        final float[] o = (float[]) result;
        topSortFloat(o);
        Assertions.assertArrayEquals(e, o, () -> this.getName());
      }
    });

    // // Sometimes this fails
    // if ((float) size / total > 0.5)
    // Assertions.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(),
    // ts.get(1).getMean()),
    // ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

    ts.check();

    if (runs > 1) {
      logger.info(ts.getReport());
    }
  }

  private static float[][] createDataFloat(UniformRandomProvider rng, int size, int total) {
    final float[][] data = new float[size][];
    for (int i = 0; i < size; i++) {
      final float[] d = new float[total];
      for (int j = 0; j < total; j++) {
        d[j] = (float) (rng.nextFloat() * 4 * Math.PI);
      }
      data[i] = d;
    }
    return data;
  }
}
