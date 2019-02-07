package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingService;

import gnu.trove.set.hash.TIntHashSet;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class SimpleArrayUtilsTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(SimpleArrayUtilsTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @SeededTest
  public void canFlatten(RandomSeed seed) {
    Assertions.assertArrayEquals(new int[0], SimpleArrayUtils.flatten(null), "Null input");
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final TIntHashSet set = new TIntHashSet();
    testFlatten(set, new int[0]);
    testFlatten(set, new int[10]);
    for (int i = 0; i < 10; i++) {
      testFlatten(set, next(rng, 1, 10));
      testFlatten(set, next(rng, 10, 10));
      testFlatten(set, next(rng, 100, 10));
    }
  }

  private static void testFlatten(TIntHashSet set, int[] s1) {
    set.clear();
    set.addAll(s1);
    final int[] e = set.toArray();
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
  public void canMerge() {
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

  private abstract class MyTimingTask extends BaseTimingTask {
    int[][][] data;

    public MyTimingTask(String name, int[][][] data) {
      super(name);
      this.data = data;
    }

    @Override
    public int getSize() {
      return data.length;
    }

    @Override
    public Object getData(int index) {
      return new int[][] {data[index][0].clone(), data[index][1].clone()};
    }
  }

  @SpeedTag
  @SeededTest
  public void testMergeOnIndexData(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());

    final double[] f = new double[] {0.1, 0.5, 0.75};
    for (final int size : new int[] {100, 1000, 10000}) {
      for (int i = 0; i < f.length; i++) {
        for (int j = i; j < f.length; j++) {
          testMergeOnIndexData(rng, 100, size, (int) (size * f[i]), (int) (size * f[j]));
        }
      }
    }
  }

  private void testMergeOnIndexData(UniformRandomProvider rng, int length, final int size,
      final int n1, final int n2) {
    final int[][][] data = new int[length][2][];
    final int[] s1 = SimpleArrayUtils.natural(size);
    for (int i = 0; i < length; i++) {
      PermutationSampler.shuffle(rng, s1);
      data[i][0] = Arrays.copyOf(s1, n1);
      PermutationSampler.shuffle(rng, s1);
      data[i][1] = Arrays.copyOf(s1, n2);
    }
    final String msg = String.format("[%d] %d vs %d", size, n1, n2);

    final TimingService ts = new TimingService();
    ts.execute(new MyTimingTask("merge+sort" + msg, data) {
      @Override
      public Object run(Object data) {
        final int[][] d = (int[][]) data;
        final int[] a = SimpleArrayUtils.merge(d[0], d[1]);
        Arrays.sort(a);
        return a;
      }
    });
    ts.execute(new MyTimingTask("merge+sort unique" + msg, data) {
      @Override
      public Object run(Object data) {
        final int[][] d = (int[][]) data;
        final int[] a = SimpleArrayUtils.merge(d[0], d[1], true);
        Arrays.sort(a);
        return a;
      }
    });

    ts.repeat(ts.getSize());
    logger.info(ts.getReport());
  }

  @SpeedTag
  @SeededTest
  public void testMergeOnRedundantData(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());

    final int[] n = new int[] {2, 4, 8};
    final int[] size = new int[] {100, 1000, 10000};

    for (int i = 0; i < n.length; i++) {
      for (int j = i; j < n.length; j++) {
        for (int ii = 0; ii < size.length; ii++) {
          for (int jj = ii; jj < size.length; jj++) {
            testMergeOnRedundantData(rng, 50, size[ii], n[i], size[jj], n[j]);
          }
        }
      }
    }
  }

  private void testMergeOnRedundantData(UniformRandomProvider rng, int length, final int n1,
      final int r1, final int n2, final int r2) {

    final int[][][] data = new int[length][2][];
    final int[] s1 = new int[n1];
    for (int i = 0; i < n1; i++) {
      s1[i] = i % r1;
    }
    final int[] s2 = new int[n2];
    for (int i = 0; i < n2; i++) {
      s2[i] = i % r2;
    }
    for (int i = 0; i < length; i++) {
      PermutationSampler.shuffle(rng, s1);
      data[i][0] = s1.clone();
      PermutationSampler.shuffle(rng, s2);
      data[i][1] = s2.clone();
    }
    final String msg = String.format("%d%%%d vs %d%%%d", n1, r1, n2, r2);

    final TimingService ts = new TimingService();
    ts.execute(new MyTimingTask("merge+sort" + msg, data) {
      @Override
      public Object run(Object data) {
        final int[][] d = (int[][]) data;
        final int[] a = SimpleArrayUtils.merge(d[0], d[1]);
        Arrays.sort(a);
        return a;
      }
    });

    ts.repeat(ts.getSize());
    logger.info(ts.getReport());
  }

  @Test
  public void canConvertIntToDouble() {
    int[] array = null;
    Assertions.assertArrayEquals(new double[0], SimpleArrayUtils.toDouble(array), "Null argument");
    array = new int[] {1, 3, 7};
    final double[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toDouble(array));
  }

  @Test
  public void canConvertFloatToDouble() {
    float[] array = null;
    Assertions.assertArrayEquals(new double[0], SimpleArrayUtils.toDouble(array), "Null argument");
    array = new float[] {1, 3, 7};
    final double[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toDouble(array));
  }

  @Test
  public void canConvertDoubleToFloat() {
    double[] array = null;
    Assertions.assertArrayEquals(new float[0], SimpleArrayUtils.toFloat(array), "Null argument");
    array = new double[] {1, 3, 7};
    final float[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toFloat(array));
  }

  @Test
  public void canConvertIntToFloat() {
    int[] array = null;
    Assertions.assertArrayEquals(new float[0], SimpleArrayUtils.toFloat(array), "Null argument");
    array = new int[] {1, 3, 7};
    final float[] expected = {1, 3, 7};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.toFloat(array));
  }

  @Test
  public void canCreateNewDoubleArray() {
    final double[] expected = {0.5, 1.5, 2.5};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newArray(3, 0.5, 1));
  }

  @Test
  public void canCreateNewFloatArray() {
    final float[] expected = {0.5f, 1.5f, 2.5f};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newArray(3, 0.5f, 1));
  }

  @Test
  public void canCreateNewIntArray() {
    final int[] expected = {2, 5, 8};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newArray(3, 2, 3));
  }

  @Test
  public void canCreateNewNaturalArray() {
    final int[] expected = {0, 1, 2};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.natural(3));
  }

  @Test
  public void canEnsureStrictlyPositive() {
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
  public void canFindMinAboveZero() {
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
  public void canCreateNewFilledDoubleArray() {
    final double[] expected = {0.5, 0.5, 0.5};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newDoubleArray(3, 0.5));
  }

  @Test
  public void canCreateNewFilledFloatArray() {
    final float[] expected = {0.5f, 0.5f, 0.5f};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newFloatArray(3, 0.5f));
  }

  @Test
  public void canCreateNewFilledIntArray() {
    final int[] expected = {2, 2, 2};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newIntArray(3, 2));
  }

  @Test
  public void canCreateNewFilledByteArray() {
    final byte[] expected = {2, 2, 2};
    Assertions.assertArrayEquals(expected, SimpleArrayUtils.newByteArray(3, (byte) 2));
  }

  @Test
  public void canReverseIntArray() {
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
  public void canReverseFloatArray() {
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
  public void canReverseDoubleArray() {
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
  public void canReverseByteArray() {
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
  public void canReverseShortArray() {
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
  public void canTestDoubleIsInteger() {
    Assertions.assertTrue(SimpleArrayUtils.isInteger(new double[0]), "Empty array");
    final double[] data = new double[] {Integer.MIN_VALUE, 1, 2, Integer.MAX_VALUE};
    Assertions.assertTrue(SimpleArrayUtils.isInteger(data), "Full range int array");
    data[0] -= 1;
    Assertions.assertFalse(SimpleArrayUtils.isInteger(data), "Exceed full range int array");
    data[0] = 0.5;
    Assertions.assertFalse(SimpleArrayUtils.isInteger(data), "non-int values in array");
  }

  @Test
  public void canTestFloatIsInteger() {
    Assertions.assertTrue(SimpleArrayUtils.isInteger(new float[0]), "Empty array");
    final float[] data = new float[] {1, 2, 3};
    Assertions.assertTrue(SimpleArrayUtils.isInteger(data), "Valid int array");
    data[0] = 0.5f;
    Assertions.assertFalse(SimpleArrayUtils.isInteger(data), "non-int values in array");
  }

  @Test
  public void canTestIntIsUniform() {
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
  public void canTestDoubleIsUniform() {
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
  public void canMultiplyFloat() {
    final float[] data = {1, 2, 3};
    final float[] expected = {2, 4, 6};
    SimpleArrayUtils.multiply(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canMultiplyFloatByDouble() {
    final float[] data = {1, 2, 3};
    final float[] expected = {2, 4, 6};
    SimpleArrayUtils.multiply(data, 2.0);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canMultiplyDouble() {
    final double[] data = {1, 2, 3};
    final double[] expected = {2, 4, 6};
    SimpleArrayUtils.multiply(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canAddFloat() {
    final float[] data = {1, 2, 3};
    final float[] expected = {3, 4, 5};
    SimpleArrayUtils.add(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canAddDouble() {
    final double[] data = {1, 2, 3};
    final double[] expected = {3, 4, 5};
    SimpleArrayUtils.add(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canAddInt() {
    final int[] data = {1, 2, 3};
    final int[] expected = {3, 4, 5};
    SimpleArrayUtils.add(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canSubtractInt() {
    final int[] data = {3, 4, 5};
    final int[] expected = {1, 2, 3};
    SimpleArrayUtils.subtract(data, 2);
    Assertions.assertArrayEquals(expected, data);
  }

  @Test
  public void canFindIntMinIndex() {
    final int[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = -1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMinIndex(data));
      data[i] = 0;
    }
  }

  @Test
  public void canFindFloatMinIndex() {
    final float[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = -1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMinIndex(data));
      data[i] = 0;
    }
  }

  @Test
  public void canFindDoubleMinIndex() {
    final double[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = -1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMinIndex(data));
      data[i] = 0;
    }
  }

  @Test
  public void canFindIntMaxIndex() {
    final int[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = 1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMaxIndex(data));
      data[i] = 0;
    }
  }

  @Test
  public void canFindFloatMaxIndex() {
    final float[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = 1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMaxIndex(data));
      data[i] = 0;
    }
  }

  @Test
  public void canFindDoubleMaxIndex() {
    final double[] data = {0, 0, 0};
    for (int i = 0; i < data.length; i++) {
      data[i] = 1;
      Assertions.assertEquals(i, SimpleArrayUtils.findMaxIndex(data));
      data[i] = 0;
    }
  }

  @Test
  public void canFindIntMinMaxIndex() {
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
  public void canFindFloatMinMaxIndex() {
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
  public void canFindDoubleMinMaxIndex() {
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
  public void canGetRanges() {
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
  public void canGetRangesWithDuplicates() {
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
  public void canCheck2DSize() {
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
  public void canHas2DFloatData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (float[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new float[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new float[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new float[1]);
  }

  @Test
  public void canHas2DDoubleData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (double[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new double[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new double[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new double[1]);
  }

  @Test
  public void canHas2DIntData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (int[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new int[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new int[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new int[1]);
  }

  @Test
  public void canHas2DByteData() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, (byte[]) null), "null data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(0, 0, new byte[0]), "zero length data");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SimpleArrayUtils.hasData2D(1, 2, new byte[1]), "width*height != data.length");
    SimpleArrayUtils.hasData2D(1, 1, new byte[1]);
  }

  @Test
  public void canCheckIsArray() {
    Assertions.assertTrue(SimpleArrayUtils.isArray(new int[0]), "int[] data");
    Assertions.assertTrue(SimpleArrayUtils.isArray(new Object[0]), "Object[] data");
    Assertions.assertTrue(SimpleArrayUtils.isArray(new int[0][0]), "int[][] data");
    Assertions.assertFalse(SimpleArrayUtils.isArray(null), "null data");
    Assertions.assertFalse(SimpleArrayUtils.isArray(new Object()), "invalid array object");
  }

  @Test
  public void canToString() {
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
  public void canDeepCopyDouble2DArray() {
    final double[][] data = {{1, 2, 3}, {44, 55, 66}};
    final double[][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  public void canDeepCopyFloat2DArray() {
    final float[][] data = {{1, 2, 3}, {44, 55, 66}};
    final float[][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  public void canDeepCopyInt2DArray() {
    final int[][] data = {{1, 2, 3}, {44, 55, 66}};
    final int[][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  public void canDeepCopyDouble3DArray() {
    final double[][][] data = {{{1, 2, 3}, {44, 55, 66}}, {{9, 8}, {4, 3}}};
    final double[][][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  public void canDeepCopyFloat3DArray() {
    final float[][][] data = {{{1, 2, 3}, {44, 55, 66}}, {{9, 8}, {4, 3}}};
    final float[][][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  public void canDeepCopyInt3DArray() {
    final int[][][] data = {{{1, 2, 3}, {44, 55, 66}}, {{9, 8}, {4, 3}}};
    final int[][][] result = SimpleArrayUtils.deepCopy(data);
    Assertions.assertNotSame(result, data, "Same object");
    Assertions.assertArrayEquals(result, data, "Same data");
  }

  @Test
  public void canTestDoubleIsFinite() {
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new double[0]), "zero length array");
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new double[10]), "non-zero length array");
    for (final double value : new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        Double.NaN}) {
      Assertions.assertFalse(SimpleArrayUtils.isFinite(new double[] {value}),
          () -> Double.toString(value));
    }
  }

  @Test
  public void canTestFloatIsFinite() {
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new float[0]), "zero length array");
    Assertions.assertTrue(SimpleArrayUtils.isFinite(new float[10]), "non-zero length array");
    for (final float value : new float[] {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
        Float.NaN}) {
      Assertions.assertFalse(SimpleArrayUtils.isFinite(new float[] {value}),
          () -> Float.toString(value));
    }
  }

  @Test
  public void canSwapIntData() {
    final int[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new int[] {4, 3}, data);
  }

  @Test
  public void canSwapFloatData() {
    final float[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new float[] {4, 3}, data);
  }

  @Test
  public void canSwapDoubleData() {
    final double[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new double[] {4, 3}, data);
  }

  @Test
  public void canSwapByteData() {
    final byte[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new byte[] {4, 3}, data);
  }

  @Test
  public void canSwapShortData() {
    final short[] data = {3, 4};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new short[] {4, 3}, data);
  }

  @Test
  public void canSwapTData() {
    final String[] data = {"3", "4"};
    SimpleArrayUtils.swap(data, 0, 1);
    Assertions.assertArrayEquals(new String[] {"4", "3"}, data);
  }

  @Test
  public void canEnsureFloatSize() {
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

  @Test
  public void canEnsureIntSize() {
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

  @Test
  public void canEnsureDoubleSize() {
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

  @Test
  public void canEnsurByteSize() {
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

  @Test
  public void canEnsureShortSize() {
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
  public void canGetIndex() {
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
}
