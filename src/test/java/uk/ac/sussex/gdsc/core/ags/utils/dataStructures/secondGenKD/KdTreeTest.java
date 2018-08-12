package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.secondGenKD;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.MaxHeap;
import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree.Entry;
import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction2D;
import uk.ac.sussex.gdsc.core.utils.Maths;
import uk.ac.sussex.gdsc.core.utils.PartialSort;
import uk.ac.sussex.gdsc.core.utils.Random;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.BaseTimingTask;
import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.TimingService;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class KdTreeTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(KdTreeTest.class.getName());
    }

    @AfterAll
    public static void afterAll()
    {
        logger = null;
    }

    int size = 256;
    int[] N = new int[] { 100, 200, 400, 2000 };
    int[] K = new int[] { 2, 4, 8, 16 };

    @SeededTest
    public void canComputeKNNSecondGen(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        for (final int n : N)
        {
            final double[][] data = createData(r, size, n, false);

            // Create the KDtree
            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<>(
                    null);
            for (final double[] location : data)
                tree.addPoint(location, null);

            // Compute all-vs-all distances
            final double[][] d = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);

            // For each point
            for (int i = 0; i < n; i++)
            {
                // Get the sorted distances to neighbours
                final double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

                // Get the KNN
                for (final int k : K)
                {
                    final List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, true);
                    final double[] observed = new double[k];
                    // Neighbours will be in reverse order
                    int j = k;
                    for (final Entry<Object> e : neighbours)
                        observed[--j] = e.distance;

                    final double[] expected = Arrays.copyOf(d2, k);
                    //TestLog.debug(logger,"[%d] k=%d  E=%s, O=%s", i, k, Arrays.toString(expected),
                    //		Arrays.toString(observed));

                    Assertions.assertArrayEquals(expected, observed);
                }
            }
        }
    }

    @SeededTest
    public void canComputeKNNSecondGenWithDuplicates(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        for (final int n : N)
        {
            final double[][] data = createData(r, size, n, true);

            // Create the KDtree
            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<>(
                    null);
            for (final double[] location : data)
                tree.addPoint(location, null);

            // Compute all-vs-all distances
            final double[][] d = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);

            // For each point
            for (int i = 0; i < n; i++)
            {
                // Get the sorted distances to neighbours
                final double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

                // Get the KNN
                for (final int k : K)
                {
                    final List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, true);
                    final double[] observed = new double[k];
                    // Neighbours will be in reverse order
                    int j = k;
                    for (final Entry<Object> e : neighbours)
                        observed[--j] = e.distance;

                    final double[] expected = Arrays.copyOf(d2, k);
                    //TestLog.debug(logger,"[%d] k=%d  E=%s, O=%s", i, k, Arrays.toString(expected),
                    //		Arrays.toString(observed));

                    Assertions.assertArrayEquals(expected, observed);
                }
            }
        }
    }

    @SeededTest
    public void canComputeKNNDistanceSecondGen(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        for (final int n : N)
        {
            final double[][] data = createData(r, size, n, true);

            // Create the KDtree
            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<>(
                    null);
            for (final double[] location : data)
                tree.addPoint(location, null);

            // Compute all-vs-all distances
            final double[][] d = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);

            // For each point
            for (int i = 0; i < n; i++)
            {
                // Get the sorted distances to neighbours
                final double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

                // Get the KNN
                for (final int k : K)
                {
                    final List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, false);

                    Assertions.assertEquals(d2[k - 1], neighbours.get(0).distance);
                }
            }
        }
    }

    @SeededTest
    public void canComputeKNNThirdGen(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        for (final int n : N)
        {
            final double[][] data = createData(r, size, n, false);

            // Create the KDtree
            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<>(
                    2);
            for (final double[] location : data)
                tree.addPoint(location, null);

            // Compute all-vs-all distances
            final double[][] d = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);

            // For each point
            for (int i = 0; i < n; i++)
            {
                // Get the sorted distances to neighbours
                final double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

                // Get the KNN
                for (final int k : K)
                {
                    final MaxHeap<Object> neighbours = tree.findNearestNeighbors(data[i], k,
                            new SquareEuclideanDistanceFunction2D());
                    final double[] observed = new double[k];
                    // Neighbours will be in reverse order
                    int j = k;
                    while (neighbours.size() > 0)
                    {
                        observed[--j] = neighbours.getMaxKey();
                        neighbours.removeMax();
                    }

                    final double[] expected = Arrays.copyOf(d2, k);
                    //TestLog.debug(logger,"[%d] k=%d  E=%s, O=%s", i, k, Arrays.toString(expected),
                    //		Arrays.toString(observed));

                    Assertions.assertArrayEquals(expected, observed);
                }
            }
        }
    }

    @SeededTest
    public void canComputeKNNThirdGenWithDuplicates(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        for (final int n : N)
        {
            final double[][] data = createData(r, size, n, true);

            // Create the KDtree
            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<>(
                    2);
            for (final double[] location : data)
                tree.addPoint(location, null);

            // Compute all-vs-all distances
            final double[][] d = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);

            // For each point
            for (int i = 0; i < n; i++)
            {
                // Get the sorted distances to neighbours
                final double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

                // Get the KNN
                for (final int k : K)
                {
                    final MaxHeap<Object> neighbours = tree.findNearestNeighbors(data[i], k,
                            new SquareEuclideanDistanceFunction2D());
                    final double[] observed = new double[k];
                    // Neighbours will be in reverse order
                    int j = k;
                    while (neighbours.size() > 0)
                    {
                        observed[--j] = neighbours.getMaxKey();
                        neighbours.removeMax();
                    }

                    final double[] expected = Arrays.copyOf(d2, k);
                    //TestLog.debug(logger,"[%d] k=%d  E=%s, O=%s", i, k, Arrays.toString(expected),
                    //		Arrays.toString(observed));

                    Assertions.assertArrayEquals(expected, observed);
                }
            }
        }
    }

    @SeededTest
    public void canComputeKNNDistanceThirdGen(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        for (final int n : N)
        {
            final double[][] data = createData(r, size, n, true);

            // Create the KDtree
            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<>(
                    2);
            for (final double[] location : data)
                tree.addPoint(location, null);

            // Compute all-vs-all distances
            final double[][] d = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);

            // For each point
            for (int i = 0; i < n; i++)
            {
                // Get the sorted distances to neighbours
                final double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

                // Get the KNN
                for (final int k : K)
                {
                    final MaxHeap<Object> neighbours = tree.findNearestNeighbors(data[i], k,
                            new SquareEuclideanDistanceFunction2D());

                    Assertions.assertEquals(d2[k - 1], neighbours.getMaxKey());
                }
            }
        }
    }

    private abstract class NNTimingTask extends BaseTimingTask
    {
        Object data;
        double[] expected;
        double eps;

        public NNTimingTask(String name, double[][] data, double[] expected)
        {
            super(name);
            this.data = data;
            this.expected = expected;
            this.eps = 0;
        }

        public NNTimingTask(String name, double[][] data, double[] expected, double eps)
        {
            super(name);
            // Convert to float
            final double[][] d = data;
            final int n = d.length;
            final float[][] d2 = new float[n][];
            for (int i = 0; i < n; i++)
                d2[i] = new float[] { (float) d[i][0], (float) d[i][1] };
            this.data = d2;
            this.expected = expected;
            this.eps = eps;
        }

        @Override
        public int getSize()
        {
            return 1;
        }

        @Override
        public Object getData(int i)
        {
            return data;
        }

        @Override
        public void check(int i, Object result)
        {
            final double[] observed = (double[]) result;
            if (eps == 0)
                Assertions.assertArrayEquals(expected, observed);
            else
                Assertions.assertArrayEquals(expected, observed, eps);
        }
    }

    @SpeedTag
    @SeededTest
    public void secondGenIsFasterThanThirdGen(RandomSeed seed)
    {
        // No assertions are made since the timings are similar
        ExtraAssumptions.assume(logger, Level.INFO);
        ExtraAssumptions.assume(TestComplexity.MEDIUM);

        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        final TimingService ts = new TimingService(15);
        final int n = 5000;
        final double[][] data = createData(r, size, n, true);
        final int k = 4;

        long time = System.nanoTime();
        final double[] expected = new double[n];
        final double[][] d = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
        for (int i = 0; i < n; i++)
            // Get the sorted distances to neighbours
            expected[i] = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d[i], n, k)[0];
        time = System.nanoTime() - time;

        ts.execute(new NNTimingTask("Second", data, expected)
        {
            @Override
            public Object run(Object oData)
            {
                final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<>(
                        null);
                final double[][] data = (double[][]) oData;
                for (final double[] location : data)
                    tree.addPoint(location, null);
                final double[] o = new double[data.length];
                for (int i = 0; i < data.length; i++)
                    o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
                return o;
            }
        });

        ts.execute(new NNTimingTask("Second2D", data, expected)
        {
            @Override
            public Object run(Object oData)
            {
                final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree2D<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.KdTree2D.SqrEuclid2D<>();
                final double[][] data = (double[][]) oData;
                for (final double[] location : data)
                    tree.addPoint(location, null);
                final double[] o = new double[data.length];
                for (int i = 0; i < data.length; i++)
                    o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
                return o;
            }
        });

        ts.execute(new NNTimingTask("SecondSimple2D", data, expected)
        {
            @Override
            public Object run(Object oData)
            {
                final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.SimpleKdTree2D tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.SimpleKdTree2D.SqrEuclid2D();
                final double[][] data = (double[][]) oData;
                for (final double[] location : data)
                    tree.addPoint(location);
                final double[] o = new double[data.length];
                for (int i = 0; i < data.length; i++)
                    o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
                return o;
            }
        });

        ts.execute(new NNTimingTask("SecondSimpleFloat2D", data, expected, 1e-3)
        {
            @Override
            public Object run(Object oData)
            {
                final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.SqrEuclid2D();
                final float[][] data = (float[][]) oData;
                for (final float[] location : data)
                    tree.addPoint(location);
                final double[] o = new double[data.length];
                for (int i = 0; i < data.length; i++)
                    o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
                return o;
            }
        });

        ts.execute(new NNTimingTask("Third", data, expected)
        {
            @Override
            public Object run(Object oData)
            {
                final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTreeND<>(
                        2);
                final double[][] data = (double[][]) oData;
                for (final double[] location : data)
                    tree.addPoint(location, null);
                final DistanceFunction distanceFunction = new SquareEuclideanDistanceFunction2D();
                final double[] o = new double[data.length];
                for (int i = 0; i < data.length; i++)
                    o[i] = tree.findNearestNeighbors(data[i], k, distanceFunction).getMaxKey();
                return o;
            }
        });

        ts.execute(new NNTimingTask("Third2D", data, expected)
        {
            @Override
            public Object run(Object oData)
            {
                final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree<Object> tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<>();
                final double[][] data = (double[][]) oData;
                for (final double[] location : data)
                    tree.addPoint(location, null);
                final DistanceFunction distanceFunction = new SquareEuclideanDistanceFunction2D();
                final double[] o = new double[data.length];
                for (int i = 0; i < data.length; i++)
                    o[i] = tree.findNearestNeighbors(data[i], k, distanceFunction).getMaxKey();
                return o;
            }
        });

        ts.check();
        final int number = ts.getSize();
        ts.repeat(number);
        ts.repeat(number);

        logger.info(TestLog.getSupplier("All-vs-all = %d", time));
        logger.info(ts.getReport());
    }

    class Float2DNNTimingTask extends NNTimingTask
    {
        int k;
        int buckectSize;

        public Float2DNNTimingTask(double[][] data, int k, int buckectSize)
        {
            super("Bucket" + buckectSize, data, null, 0);
            this.k = k;
            this.buckectSize = buckectSize;
        }

        @Override
        public Object run(Object oData)
        {
            // The following tests the bucket size is optimal. It requires the bucketSize be set to public non-final.
            // This prevents some code optimisation and so is not the default. The default uses a final bucket size of 24.
            //int b = ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize;
            //ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize = buckectSize;

            final uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D tree = new uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.SqrEuclid2D();
            final float[][] data = (float[][]) oData;
            for (final float[] location : data)
                tree.addPoint(location);
            final double[] o = new double[data.length];
            for (int i = 0; i < data.length; i++)
                o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
            //ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize = b;
            return o;
        }
    }

    // Requires code modification of the SimpleFloatKdTree2D class to make bucketSize size visible and not final ...
    //@SeededTest
    public void secondGenBucket24IsFastest(RandomSeed seed)
    {
        logger.isLoggable(Level.INFO);

        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        final TimingService ts = new TimingService(15);
        final int n = 5000;
        final double[][] data = createData(r, size, n, true);
        final int k = 4;

        for (final int b : new int[] { 1, 2, 3, 4, 5, 8, 16, 24, 32 })
            ts.execute(new Float2DNNTimingTask(data, k, b));

        final int number = ts.getSize();
        ts.repeat(number);
        ts.repeat(number);

        logger.info(ts.getReport());
    }

    private static double[][] createData(UniformRandomProvider r, int size, int n, boolean allowDuplicates)
    {
        final double[][] data = new double[n][];
        if (allowDuplicates)
        {
            final int half = n / 2;
            for (int i = half; i < n; i++)
                data[i] = new double[] { r.nextDouble() * size, r.nextDouble() * size };
            for (int i = 0, j = half; i < half; i++, j++)
                data[i] = data[j];
        }
        else
        {
            final double[] x = SimpleArrayUtils.newArray(n, 0, (double) size / n);
            final double[] y = x.clone();
            Random.shuffle(x, r);
            Random.shuffle(y, r);
            for (int i = 0; i < n; i++)
                data[i] = new double[] { x[i], y[i] };
        }
        return data;
    }
}
