package uk.ac.sussex.gdsc.core.ags.utils.data.gen2;

import uk.ac.sussex.gdsc.core.ags.utils.data.MaxHeap;
import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree.Entry;
import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.DistanceFunction;
import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.SquareEuclideanDistanceFunction2D;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.PartialSort;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingService;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class KdTreeTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(KdTreeTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  int size = 256;
  int[] ns = new int[] {100, 200, 400, 2000};
  int[] ks = new int[] {2, 4, 8, 16};

  @SeededTest
  public void canComputeknnSecondGen(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (final int n : ns) {
      final double[][] data = createData(r, size, n, false);

      // Create the KDtree
      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree<Object> tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree.SqrEuclid2D<>(0);
      for (final double[] location : data) {
        tree.addPoint(location, null);
      }

      // Compute all-vs-all distances
      final double[][] d = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        }
      }

      // For each point
      for (int i = 0; i < n; i++) {
        // Get the sorted distances to neighbours
        final double[] d2 = PartialSort.bottom(d[i], ks[ks.length - 1]);

        // Get the knn
        for (final int k : ks) {
          final List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, true);
          final double[] observed = new double[k];
          // Neighbours will be in reverse order
          int index = k;
          for (final Entry<Object> e : neighbours) {
            observed[--index] = e.getDistance();
          }

          final double[] expected = Arrays.copyOf(d2, k);
          // TestLog.debug(logger,"[%d] k=%d E=%s, O=%s", i, k, Arrays.toString(expected),
          // Arrays.toString(observed));

          Assertions.assertArrayEquals(expected, observed);
        }
      }
    }
  }

  @SeededTest
  public void canComputeknnSecondGenWithDuplicates(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (final int n : ns) {
      final double[][] data = createData(r, size, n, true);

      // Create the KDtree
      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree<Object> tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree.SqrEuclid2D<>(0);
      for (final double[] location : data) {
        tree.addPoint(location, null);
      }

      // Compute all-vs-all distances
      final double[][] d = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        }
      }

      // For each point
      for (int i = 0; i < n; i++) {
        // Get the sorted distances to neighbours
        final double[] d2 = PartialSort.bottom(d[i], ks[ks.length - 1]);

        // Get the knn
        for (final int k : ks) {
          final List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, true);
          final double[] observed = new double[k];
          // Neighbours will be in reverse order
          int index = k;
          for (final Entry<Object> e : neighbours) {
            observed[--index] = e.getDistance();
          }

          final double[] expected = Arrays.copyOf(d2, k);
          // TestLog.debug(logger,"[%d] k=%d E=%s, O=%s", i, k, Arrays.toString(expected),
          // Arrays.toString(observed));

          Assertions.assertArrayEquals(expected, observed);
        }
      }
    }
  }

  @SeededTest
  public void canComputeknnDistanceSecondGen(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (final int n : ns) {
      final double[][] data = createData(r, size, n, true);

      // Create the KDtree
      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree<Object> tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree.SqrEuclid2D<>(0);
      for (final double[] location : data) {
        tree.addPoint(location, null);
      }

      // Compute all-vs-all distances
      final double[][] d = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        }
      }

      // For each point
      for (int i = 0; i < n; i++) {
        // Get the sorted distances to neighbours
        final double[] d2 = PartialSort.bottom(d[i], ks[ks.length - 1]);

        // Get the knn
        for (final int k : ks) {
          final List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, false);

          Assertions.assertEquals(d2[k - 1], neighbours.get(0).getDistance());
        }
      }
    }
  }

  @SeededTest
  public void canComputeknnThirdGen(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (final int n : ns) {
      final double[][] data = createData(r, size, n, false);

      // Create the KDtree
      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<Object> tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<>(2);
      for (final double[] location : data) {
        tree.addPoint(location, null);
      }

      // Compute all-vs-all distances
      final double[][] d = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        }
      }

      // For each point
      for (int i = 0; i < n; i++) {
        // Get the sorted distances to neighbours
        final double[] d2 = PartialSort.bottom(d[i], ks[ks.length - 1]);

        // Get the knn
        for (final int k : ks) {
          final MaxHeap<Object> neighbours =
              tree.findNearestNeighbors(data[i], k, SquareEuclideanDistanceFunction2D.INSTANCE);
          final double[] observed = new double[k];
          // Neighbours will be in reverse order
          int index = k;
          while (neighbours.size() > 0) {
            observed[--index] = neighbours.getMaxKey();
            neighbours.removeMax();
          }

          final double[] expected = Arrays.copyOf(d2, k);
          // TestLog.debug(logger,"[%d] k=%d E=%s, O=%s", i, k, Arrays.toString(expected),
          // Arrays.toString(observed));

          Assertions.assertArrayEquals(expected, observed);
        }
      }
    }
  }

  @SeededTest
  public void canComputeknnThirdGenWithDuplicates(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (final int n : ns) {
      final double[][] data = createData(r, size, n, true);

      // Create the KDtree
      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<Object> tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<>(2);
      for (final double[] location : data) {
        tree.addPoint(location, null);
      }

      // Compute all-vs-all distances
      final double[][] d = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        }
      }

      // For each point
      for (int i = 0; i < n; i++) {
        // Get the sorted distances to neighbours
        final double[] d2 = PartialSort.bottom(d[i], ks[ks.length - 1]);

        // Get the knn
        for (final int k : ks) {
          final MaxHeap<Object> neighbours =
              tree.findNearestNeighbors(data[i], k, SquareEuclideanDistanceFunction2D.INSTANCE);
          final double[] observed = new double[k];
          // Neighbours will be in reverse order
          int index = k;
          while (neighbours.size() > 0) {
            observed[--index] = neighbours.getMaxKey();
            neighbours.removeMax();
          }

          final double[] expected = Arrays.copyOf(d2, k);
          // TestLog.debug(logger,"[%d] k=%d E=%s, O=%s", i, k, Arrays.toString(expected),
          // Arrays.toString(observed));

          Assertions.assertArrayEquals(expected, observed);
        }
      }
    }
  }

  @SeededTest
  public void canComputeknnDistanceThirdGen(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (final int n : ns) {
      final double[][] data = createData(r, size, n, true);

      // Create the KDtree
      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<Object> tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<>(2);
      for (final double[] location : data) {
        tree.addPoint(location, null);
      }

      // Compute all-vs-all distances
      final double[][] d = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        }
      }

      // For each point
      for (int i = 0; i < n; i++) {
        // Get the sorted distances to neighbours
        final double[] d2 = PartialSort.bottom(d[i], ks[ks.length - 1]);

        // Get the knn
        for (final int k : ks) {
          final MaxHeap<Object> neighbours =
              tree.findNearestNeighbors(data[i], k, SquareEuclideanDistanceFunction2D.INSTANCE);

          Assertions.assertEquals(d2[k - 1], neighbours.getMaxKey());
        }
      }
    }
  }

  private abstract class NnTimingTask extends BaseTimingTask {
    Object data;
    double[] expected;
    double eps;

    public NnTimingTask(String name, double[][] data, double[] expected) {
      super(name);
      this.data = data;
      this.expected = expected;
      this.eps = 0;
    }

    public NnTimingTask(String name, double[][] data, double[] expected, double eps) {
      super(name);
      // Convert to float
      final double[][] d = data;
      final int n = d.length;
      final float[][] d2 = new float[n][];
      for (int i = 0; i < n; i++) {
        d2[i] = new float[] {(float) d[i][0], (float) d[i][1]};
      }
      this.data = d2;
      this.expected = expected;
      this.eps = eps;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public Object getData(int index) {
      return data;
    }

    @Override
    public void check(int index, Object result) {
      final double[] observed = (double[]) result;
      if (eps == 0) {
        Assertions.assertArrayEquals(expected, observed);
      } else {
        Assertions.assertArrayEquals(expected, observed, eps);
      }
    }
  }

  @SpeedTag
  @SeededTest
  public void secondGenIsFasterThanThirdGen(RandomSeed seed) {
    // No assertions are made since the timings are similar
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    final TimingService ts = new TimingService(15);
    final int n = 5000;
    final double[][] data = createData(r, size, n, true);
    final int k = 4;

    long time = System.nanoTime();
    final double[] expected = new double[n];
    final double[][] d = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        d[i][j] = d[j][i] = MathUtils.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
      }
    }
    for (int i = 0; i < n; i++) {
      // Get the sorted distances to neighbours
      expected[i] = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d[i], n, k)[0];
    }
    time = System.nanoTime() - time;

    ts.execute(new NnTimingTask("Second", data, expected) {
      @Override
      public Object run(Object objectData) {
        final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree<Object> tree =
            new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree.SqrEuclid2D<>(0);
        final double[][] data = (double[][]) objectData;
        for (final double[] location : data) {
          tree.addPoint(location, null);
        }
        final double[] o = new double[data.length];
        for (int i = 0; i < data.length; i++) {
          o[i] = tree.nearestNeighbor(data[i], k, false).get(0).getDistance();
        }
        return o;
      }
    });

    ts.execute(new NnTimingTask("Second2D", data, expected) {
      @Override
      public Object run(Object objectData) {
        final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree2D<Object> tree =
            new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree2D.SqrEuclid2D<>();
        final double[][] data = (double[][]) objectData;
        for (final double[] location : data) {
          tree.addPoint(location, null);
        }
        final double[] o = new double[data.length];
        for (int i = 0; i < data.length; i++) {
          o[i] = tree.nearestNeighbor(data[i], k, false).get(0).getDistance();
        }
        return o;
      }
    });

    ts.execute(new NnTimingTask("SecondSimple2D", data, expected) {
      @Override
      public Object run(Object objectData) {
        final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleKdTree2D tree =
            new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleKdTree2D.SqrEuclid2D();
        final double[][] data = (double[][]) objectData;
        for (final double[] location : data) {
          tree.addPoint(location);
        }
        final double[] o = new double[data.length];
        for (int i = 0; i < data.length; i++) {
          o[i] = tree.nearestNeighbor(data[i], k, false).get(0).getDistance();
        }
        return o;
      }
    });

    ts.execute(new NnTimingTask("SecondSimpleFloat2D", data, expected, 1e-3) {
      @Override
      public Object run(Object objectData) {
        final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleFloatKdTree2D tree =
            new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleFloatKdTree2D.SqrEuclid2D();
        final float[][] data = (float[][]) objectData;
        for (final float[] location : data) {
          tree.addPoint(location);
        }
        final double[] o = new double[data.length];
        for (int i = 0; i < data.length; i++) {
          o[i] = tree.nearestNeighbor(data[i], k, false).get(0).getDistance();
        }
        return o;
      }
    });

    ts.execute(new NnTimingTask("Third", data, expected) {
      @Override
      public Object run(Object objectData) {
        final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree<Object> tree =
            new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTreeNd<>(2);
        final double[][] data = (double[][]) objectData;
        for (final double[] location : data) {
          tree.addPoint(location, null);
        }
        final DistanceFunction distanceFunction = SquareEuclideanDistanceFunction2D.INSTANCE;
        final double[] o = new double[data.length];
        for (int i = 0; i < data.length; i++) {
          o[i] = tree.findNearestNeighbors(data[i], k, distanceFunction).getMaxKey();
        }
        return o;
      }
    });

    ts.execute(new NnTimingTask("Third2D", data, expected) {
      @Override
      public Object run(Object objectData) {
        final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree<Object> tree =
            new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D<>();
        final double[][] data = (double[][]) objectData;
        for (final double[] location : data) {
          tree.addPoint(location, null);
        }
        final DistanceFunction distanceFunction = SquareEuclideanDistanceFunction2D.INSTANCE;
        final double[] o = new double[data.length];
        for (int i = 0; i < data.length; i++) {
          o[i] = tree.findNearestNeighbors(data[i], k, distanceFunction).getMaxKey();
        }
        return o;
      }
    });

    ts.check();
    final int number = ts.getSize();
    ts.repeat(number);
    ts.repeat(number);

    logger.info(FunctionUtils.getSupplier("All-vs-all = %d", time));
    logger.info(ts.getReport());
  }

  class Float2DnnTimingTask extends NnTimingTask {
    int count;
    int buckectSize;

    public Float2DnnTimingTask(double[][] data, int count, int buckectSize) {
      super("Bucket" + buckectSize, data, null, 0);
      this.count = count;
      this.buckectSize = buckectSize;
    }

    @Override
    public Object run(Object objectData) {
      // The following tests the bucket size is optimal. It requires the bucketSize be set to public
      // non-final.
      // This prevents some code optimisation and so is not the default. The default uses a final
      // bucket size of 24.
      // int b = ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize;
      // ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize = buckectSize;

      final uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleFloatKdTree2D tree =
          new uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleFloatKdTree2D.SqrEuclid2D();
      final float[][] data = (float[][]) objectData;
      for (final float[] location : data) {
        tree.addPoint(location);
      }
      final double[] o = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        o[i] = tree.nearestNeighbor(data[i], count, false).get(0).getDistance();
      }
      // ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize = b;
      return o;
    }
  }

  @Disabled("Requires code modification to make bucket size visible and not final")
  @SeededTest
  public void secondGenBucket24IsFastest(RandomSeed seed) {
    logger.isLoggable(Level.INFO);

    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    final TimingService ts = new TimingService(15);
    final int n = 5000;
    final double[][] data = createData(r, size, n, true);
    final int k = 4;

    for (final int b : new int[] {1, 2, 3, 4, 5, 8, 16, 24, 32}) {
      ts.execute(new Float2DnnTimingTask(data, k, b));
    }

    final int number = ts.getSize();
    ts.repeat(number);
    ts.repeat(number);

    logger.info(ts.getReport());
  }

  private static double[][] createData(UniformRandomProvider rng, int size, int n,
      boolean allowDuplicates) {
    final double[][] data = new double[n][];
    if (allowDuplicates) {
      final int half = n / 2;
      for (int i = half; i < n; i++) {
        data[i] = new double[] {rng.nextDouble() * size, rng.nextDouble() * size};
      }
      for (int i = 0, j = half; i < half; i++, j++) {
        data[i] = data[j];
      }
    } else {
      final double[] x = SimpleArrayUtils.newArray(n, 0, (double) size / n);
      final double[] y = x.clone();
      RandomUtils.shuffle(x, rng);
      RandomUtils.shuffle(y, rng);
      for (int i = 0; i < n; i++) {
        data[i] = new double[] {x[i], y[i]};
      }
    }
    return data;
  }
}
