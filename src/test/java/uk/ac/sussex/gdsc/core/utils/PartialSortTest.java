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
public class PartialSortTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(PartialSortTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private abstract class MyTimingTask extends BaseTimingTask {
    double[][] data;

    public MyTimingTask(String name, double[][] data) {
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

  @SeededTest
  public void bottomNofMIsCorrect(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (final int size : testN) {
      for (final int total : testM) {
        bottomCompute(rng, 100, size, total);
      }
    }
  }

  static double[] bottom(int size, double[] values) {
    bottomSort(values);
    return Arrays.copyOf(values, size);
  }

  static void bottomSort(double[] values) {
    Arrays.sort(values);
  }

  @Test
  public void bottomCanHandleNullData() {
    final double[] o = PartialSort.bottom((double[]) null, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  public void bottomCanHandleEmptyData() {
    final double[] o = PartialSort.bottom(ArrayUtils.EMPTY_DOUBLE_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  public void bottomCanHandleIncompleteData() {
    final double[] d = {1, 3, 2};
    final double[] e = {1, 2, 3};
    final double[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  public void bottomCanHandleNaNData() {
    final double[] d = {1, 2, Double.NaN, 3};
    final double[] e = {1, 2, 3};
    final double[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private void bottomCompute(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final double[][] data = createData(rng, length, total);
    final String msg = String.format(" %d of %d", size, total);

    final MyTimingTask expected = new MyTimingTask("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return bottom(size, (double[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    final TimingService ts = new TimingService(runs);
    ts.execute(expected);
    ts.execute(new MyTimingTask("bottomSort" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom((double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertArrayEquals(e, o);
      }
    });
    ts.execute(new MyTimingTask("bottomHead" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertEquals(e[size - 1], o[0]);
      }
    });
    ts.execute(new MyTimingTask("bottom" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.bottom(0, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSort(o);
        Assertions.assertArrayEquals(e, o);
      }
    });
    final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(size);
    ts.execute(new MyTimingTask("DoubleSelector" + msg, data) {
      @Override
      public Object run(Object data) {
        return ps.bottom(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSort(o);
        Assertions.assertArrayEquals(e, o);
      }
    });

    final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(size);
    ts.execute(new MyTimingTask("DoubleMinHeap" + msg, data) {
      @Override
      public Object run(Object data) {
        return heap.bottom(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        bottomSort(o);
        Assertions.assertArrayEquals(e, o);
      }
    });
    ts.execute(new MyTimingTask("select" + msg, data) {
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
        bottomSort(o);
        Assertions.assertArrayEquals(e, o);
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
  public void topNofMIsCorrect(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (final int size : testN) {
      for (final int total : testM) {
        topCompute(rng, 100, size, total);
      }
    }
  }

  static double[] top(int size, double[] values) {
    topSort(values);
    return Arrays.copyOf(values, size);
  }

  static void topSort(double[] values) {
    Arrays.sort(values);
    SimpleArrayUtils.reverse(values);
  }

  @Test
  public void topCanHandleNullData() {
    final double[] o = PartialSort.top((double[]) null, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  public void topCanHandleEmptyData() {
    final double[] o = PartialSort.top(ArrayUtils.EMPTY_DOUBLE_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  public void topCanHandleIncompleteData() {
    final double[] d = {1, 3, 2};
    final double[] e = {3, 2, 1};
    final double[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  public void topCanHandleNaNData() {
    final double[] d = {1, 2, Double.NaN, 3};
    final double[] e = {3, 2, 1};
    final double[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private void topCompute(UniformRandomProvider rng, int length, final int size, final int total) {
    final double[][] data = createData(rng, length, total);
    final String msg = String.format(" %d of %d", size, total);

    final MyTimingTask expected = new MyTimingTask("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return top(size, (double[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    final TimingService ts = new TimingService(runs);
    ts.execute(expected);
    ts.execute(new MyTimingTask("topSort" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top((double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertArrayEquals(e, o);
      }
    });
    ts.execute(new MyTimingTask("topHead" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top(PartialSort.OPTION_HEAD_FIRST, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        Assertions.assertEquals(e[size - 1], o[0]);
      }
    });
    ts.execute(new MyTimingTask("top" + msg, data) {
      @Override
      public Object run(Object data) {
        return PartialSort.top(0, (double[]) data, size);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        topSort(o);
        Assertions.assertArrayEquals(e, o);
      }
    });
    final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(size);
    ts.execute(new MyTimingTask("DoubleSelector" + msg, data) {
      @Override
      public Object run(Object data) {
        return ps.top(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        topSort(o);
        Assertions.assertArrayEquals(e, o);
      }
    });

    final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(size);
    ts.execute(new MyTimingTask("DoubleMinHeap" + msg, data) {
      @Override
      public Object run(Object data) {
        return heap.top(0, (double[]) data);
      }

      @Override
      public void check(int index, Object result) {
        final double[] e = (double[]) expected.run(expected.getData(index));
        final double[] o = (double[]) result;
        topSort(o);
        Assertions.assertArrayEquals(e, o);
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

  private static double[][] createData(UniformRandomProvider rng, int size, int total) {
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
}
