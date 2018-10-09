package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.*;import uk.ac.sussex.gdsc.test.rng.RngFactory;import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
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
    public Object getData(int i) {
      return data[i].clone();
    }
  }

  int[] testN = new int[] {2, 3, 5, 10, 30, 50};
  int[] testM = new int[] {50, 100};

  @SeededTest
  public void bottomNofMIsCorrect(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : testN) {
      for (final int m : testM) {
        bottomCompute(r, 100, n, m);
      }
    }
  }

  static double[] bottom(int n, double[] d) {
    bottomSort(d);
    return Arrays.copyOf(d, n);
  }

  static void bottomSort(double[] d) {
    Arrays.sort(d);
  }

  @Test
  public void bottomCanHandleNullData() {
    final double[] o = PartialSort.bottom((double[]) null, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  public void bottomCanHandleEmptyData() {
    final double[] o = PartialSort.bottom(new double[0], 5);
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

  private void bottomCompute(UniformRandomProvider r, int length, final int n, final int m) {
    final double[][] data = createData(r, length, m);
    final String msg = String.format(" %d of %d", n, m);

    final MyTimingTask expected = new MyTimingTask("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return bottom(n, (double[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    //@formatter:off
		final TimingService ts = new TimingService(runs);
		ts.execute(expected);
		ts.execute(new MyTimingTask("bottomSort" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.bottom((double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				Assertions.assertArrayEquals(e, o);
			}
		});
		ts.execute(new MyTimingTask("bottomHead" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				Assertions.assertEquals(e[n-1], o[0]);
			}
		});
		ts.execute(new MyTimingTask("bottom" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.bottom(0, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				bottomSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});
		final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(n);
		ts.execute(new MyTimingTask("DoubleSelector" + msg, data)
		{
			@Override
			public Object run(Object data) { return ps.bottom(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				bottomSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});

		final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(n);
		ts.execute(new MyTimingTask("DoubleHeap" + msg, data)
		{
			@Override
			public Object run(Object data) { return heap.bottom(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				bottomSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});
		ts.execute(new MyTimingTask("select" + msg, data)
		{
			@Override
			public Object run(Object data) {
				final double[] arr = (double[]) data;
				PartialSort.select(n-1, arr.length, arr);
				return Arrays.copyOf(arr, n);
			}
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				bottomSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});

		//@formatter:on

    // Sometimes this fails
    // if ((double) n / m > 0.5)
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
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : testN) {
      for (final int m : testM) {
        topCompute(r, 100, n, m);
      }
    }
  }

  static double[] top(int n, double[] d) {
    topSort(d);
    return Arrays.copyOf(d, n);
  }

  static void topSort(double[] d) {
    Arrays.sort(d);
    SimpleArrayUtils.reverse(d);
  }

  @Test
  public void topCanHandleNullData() {
    final double[] o = PartialSort.top((double[]) null, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  public void topCanHandleEmptyData() {
    final double[] o = PartialSort.top(new double[0], 5);
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

  private void topCompute(UniformRandomProvider r, int length, final int n, final int m) {
    final double[][] data = createData(r, length, m);
    final String msg = String.format(" %d of %d", n, m);

    final MyTimingTask expected = new MyTimingTask("Sort" + msg, data) {
      @Override
      public Object run(Object data) {
        return top(n, (double[]) data);
      }
    };

    final int runs = (logger.isLoggable(Level.INFO)) ? 5 : 1;
    //@formatter:off
		final TimingService ts = new TimingService(runs);
		ts.execute(expected);
		ts.execute(new MyTimingTask("topSort" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.top((double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				Assertions.assertArrayEquals(e, o);
			}
		});
		ts.execute(new MyTimingTask("topHead" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.top(PartialSort.OPTION_HEAD_FIRST, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				Assertions.assertEquals(e[n-1], o[0]);
			}
		});
		ts.execute(new MyTimingTask("top" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.top(0, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				topSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});
		final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(n);
		ts.execute(new MyTimingTask("DoubleSelector" + msg, data)
		{
			@Override
			public Object run(Object data) { return ps.top(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				topSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});

		final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(n);
		ts.execute(new MyTimingTask("DoubleHeap" + msg, data)
		{
			@Override
			public Object run(Object data) { return heap.top(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				final double[] e = (double[])expected.run(expected.getData(i));
				final double[] o = (double[])result;
				topSort(o);
				Assertions.assertArrayEquals(e, o);
			}
		});

		//@formatter:on

    // // Sometimes this fails
    // if ((double) n / m > 0.5)
    // Assertions.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(),
    // ts.get(1).getMean()),
    // ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

    ts.check();

    if (runs > 1) {
      logger.info(ts.getReport());
    }
  }

  private static double[][] createData(UniformRandomProvider r, int size, int m) {
    final double[][] data = new double[size][];
    for (int i = 0; i < size; i++) {
      final double[] d = new double[m];
      for (int j = 0; j < m; j++) {
        d[j] = r.nextDouble() * 4 * Math.PI;
      }
      data[i] = d;
    }
    return data;
  }
}
