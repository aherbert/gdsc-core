package uk.ac.sussex.gdsc.core.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import uk.ac.sussex.gdsc.core.utils.Random;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class ClusteringEngineTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(ClusteringEngineTest.class.getName());

    }

    @AfterAll
    public static void afterAll()
    {
        logger.severe("destroyed");
        logger = null;
    }

    // Store the closest pair of clusters
    int ii, jj;

    @SeededTest
    public void canClusterClusterPointsAtDifferentDensitiesUsingClosest(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        for (final double radius : new double[] { 5, 10, 20 })
            for (final int size : new int[] { 1000, 500, 300, 100 })
                testClusting(rg, ClusteringAlgorithm.CENTROID_LINKAGE, radius, 100, size);
    }

    @SeededTest
    public void canClusterClusterPointsAtDifferentDensitiesUsingPairwiseWithoutNeighbours(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        for (final double radius : new double[] { 5, 10, 20 })
            for (final int size : new int[] { 1000, 500, 300, 100 })
                testClusting(rg, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius, 100, size);
    }

    @SpeedTag
    @SeededTest
    public void pairwiseWithoutNeighboursIsFasterAtLowDensities(RandomSeed seed)
    {
        ExtraAssumptions.assumeMediumComplexity();

        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int repeats = 10;
        final double radius = 50;
        final Object[] points = new Object[repeats];
        for (int i = 0; i < repeats; i++)
            points[i] = createClusters(rg, 20, 1000, 2, radius / 2);

        final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
        final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius);

        TestLog.logTestResult(logger, (t2 <= t1),
                "SpeedTest (Low Density) Closest %d, PairwiseWithoutNeighbours %d = %fx faster", t1, t2,
                (double) t1 / t2);
    }

    @SpeedTag
    @SeededTest
    public void pairwiseWithoutNeighboursIsSlowerAtHighDensities(RandomSeed seed)
    {
        ExtraAssumptions.assumeMediumComplexity();

        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int repeats = 10;
        final double radius = 50;
        final Object[] points = new Object[repeats];
        for (int i = 0; i < repeats; i++)
            points[i] = createClusters(rg, 500, 1000, 2, radius / 2);

        final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
        final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius);

        TestLog.info(logger, "SpeedTest (High Density) Closest %d, PairwiseWithoutNeighbours %d = %fx faster", t1, t2,
                (double) t1 / t2);
        Assertions.assertTrue(t1 <= t2);
    }

    @SeededTest
    public void pairwiseIsFaster(RandomSeed seed)
    {
        ExtraAssumptions.assumeMediumComplexity();

        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int repeats = 20;
        final Object[] points = new Object[repeats];
        for (int i = 0; i < repeats; i++)
            points[i] = createPoints(rg, 500, 1000);
        final double radius = 50;

        final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
        final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE, radius);

        TestLog.info(logger, "SpeedTest Closest %d, Pairwise %d = %fx faster", t1, t2, (double) t1 / t2);
        Assertions.assertTrue(t2 < t1);
    }

    @SeededTest
    public void canMultithreadParticleSingleLinkage(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedParticleSingleLinkageIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
    }

    @SeededTest
    public void canMultithreadClosest(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.CENTROID_LINKAGE);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedClosestIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.CENTROID_LINKAGE);
    }

    @SeededTest
    public void canMultithreadClosestParticle(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedClosestParticleIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
    }

    @SeededTest
    public void canMultithreadClosestDistancePriority(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedClosestDistancePriorityIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY);
    }

    @SeededTest
    public void canMultithreadClosestTimePriority(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedClosestTimePriorityIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
    }

    @SeededTest
    public void canMultithreadClosestParticleDistancePriority(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedClosestParticleDistancePriorityIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
    }

    @SeededTest
    public void canMultithreadClosestParticleTimePriority(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedClosestParticleTimePriorityIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
    }

    @SeededTest
    public void canMultithreadPairwiseWithoutNeighbours(RandomSeed seed)
    {
        runMultithreadingAlgorithmTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS);
    }

    @SpeedTag
    @SeededTest
    public void multithreadedPairwiseWithoutNeighboursIsFaster(RandomSeed seed)
    {
        runMultithreadingSpeedTest(TestSettings.getRandomGenerator(seed.getSeed()),
                ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS);
    }

    private static void runMultithreadingAlgorithmTest(UniformRandomProvider rg, ClusteringAlgorithm algorithm)
    {
        final double radius = 50;
        final int time = 10;
        final ArrayList<ClusterPoint> points = createClusters(rg, 500, 1000, 2, radius / 2, time);
        final ClusteringEngine engine = new ClusteringEngine(0, algorithm);
        final ArrayList<Cluster> exp = engine.findClusters(points, radius, time);
        engine.setThreadCount(8);
        final ArrayList<Cluster> obs = engine.findClusters(points, radius, time);
        compareClusters(exp, obs);
    }

    private static void runMultithreadingSpeedTest(UniformRandomProvider rg, ClusteringAlgorithm algorithm)
    {
        ExtraAssumptions.assumeMediumComplexity();

        final int repeats = 5;
        final double radius = 50;
        final int time = 10;
        final Object[] points = new Object[repeats];
        for (int i = 0; i < repeats; i++)
            points[i] = createClusters(rg, 1000, 1000, 2, radius / 2, time);

        final long t1 = runSpeedTest(points, algorithm, radius, time, 1);
        final long t2 = runSpeedTest(points, algorithm, radius, time, 8);

        TestLog.info(logger, "Threading SpeedTest %s : Single %d, Multi-threaded %d = %fx faster", algorithm.toString(),
                t1, t2, (double) t1 / t2);
        Assertions.assertTrue(t2 <= t1);
    }

    private static long runSpeedTest(Object[] points, ClusteringAlgorithm algorithm, double radius)
    {
        return runSpeedTest(points, algorithm, radius, 0, 1);
    }

    @SuppressWarnings("unchecked")
    private static long runSpeedTest(Object[] points, ClusteringAlgorithm algorithm, double radius, int time,
            int threadCount)
    {
        final ClusteringEngine engine = new ClusteringEngine(threadCount, algorithm);

        // Initialise
        engine.findClusters((ArrayList<ClusterPoint>) points[0], radius, time);

        final long start = System.nanoTime();
        for (int i = 0; i < points.length; i++)
            engine.findClusters((ArrayList<ClusterPoint>) points[i], radius, time);
        return System.nanoTime() - start;
    }

    private void testClusting(UniformRandomProvider rg, ClusteringAlgorithm algorithm, double radius, int n, int size)
    {
        final ClusteringEngine engine = new ClusteringEngine();
        engine.setClusteringAlgorithm(algorithm);
        final ArrayList<ClusterPoint> points = createPoints(rg, n, size);

        // Report density of the clustering we are testing. Size/radius are in nm
        //TestLog.debug(logger,"Testing n=%d, Size=%d, Density=%s um^-2, Radius=%s nm", n, size,
        //		Utils.rounded(n * 1e6 / (size * size)), Utils.rounded(radius));

        final ArrayList<Cluster> exp = findClusters(points, radius);
        final ArrayList<Cluster> obs = engine.findClusters(points, radius);
        compareClusters(exp, obs);
    }

    private static void compareClusters(ArrayList<Cluster> exp, ArrayList<Cluster> obs) throws AssertionError
    {
        Collections.sort(exp);
        Collections.sort(obs);

        try
        {
            Assertions.assertEquals(exp.size(), obs.size(), "# clusters is different");
            for (int i = 0; i < exp.size(); i++)
                assertEqual(i, exp.get(i), obs.get(i));
        }
        catch (final AssertionError e)
        {
            print("Expected", exp);
            print("Observed", obs);
            throw e;
        }
    }

    private static void print(String name, ArrayList<Cluster> clusters)
    {
        TestLog.info(logger, name + " : size=%d", clusters.size());
        for (int i = 0; i < clusters.size(); i++)
        {
            final Cluster c = clusters.get(i);
            TestLog.info(logger, "[%d] : head=%d, n=%d, cx=%g, cy=%g", i, c.head.id, c.n, c.x, c.y);
        }
    }

    private static void assertEqual(int i, Cluster cluster, Cluster cluster2)
    {
        Assertions.assertEquals(cluster.n, cluster2.n, () -> String.format("Cluster %d: Size is different", i));
        Assertions.assertEquals(cluster.x, cluster2.x, 1e-4, () -> String.format("Cluster %d: X is different", i));
        Assertions.assertEquals(cluster.y, cluster2.y, 1e-4, () -> String.format("Cluster %d: Y is different", i));
        // Q. Should we check each cluster member is the same ?
    }

    /**
     * Perform centroid-linkage clustering up to the given radius.
     *
     * @param points
     *            the points
     * @param radius
     *            the radius
     * @return The clusters
     */
    private ArrayList<Cluster> findClusters(ArrayList<ClusterPoint> points, double radius)
    {
        // Initialise all clusters with one molecule
        final ArrayList<Cluster> clusters = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++)
        {
            final ClusterPoint m = points.get(i);
            clusters.add(new Cluster(ClusterPoint.newClusterPoint(i, m.x, m.y)));
        }

        // Iteratively find the closest pair
        while (findClosest(clusters, radius))
        {
            clusters.get(ii).add(clusters.get(jj));
            clusters.remove(jj);
        }

        return clusters;
    }

    /**
     * Implement and all-vs-all search for the closest pair of clusters within the given radius. Set the class level
     * variables ii and jj to the indices of the closest pair.
     *
     * @param clusters
     *            the clusters
     * @param radius
     *            the radius
     * @return True if a pair was found
     */
    private boolean findClosest(ArrayList<Cluster> clusters, double radius)
    {
        double minD = radius * radius;
        ii = -1;
        for (int i = 0; i < clusters.size(); i++)
        {
            final Cluster c1 = clusters.get(i);
            for (int j = i + 1; j < clusters.size(); j++)
            {
                final double d2 = c1.distance2(clusters.get(j));
                if (d2 < minD)
                {
                    ii = i;
                    jj = j;
                    minD = d2;
                }
            }
        }

        return ii > -1;
    }

    /**
     * Create n points in a 2D distribution of size * size.
     *
     * @param rg
     *            the rg
     * @param n
     *            the n
     * @param size
     *            the size
     * @return The points
     */
    private static ArrayList<ClusterPoint> createPoints(UniformRandomProvider rg, int n, int size)
    {
        final ArrayList<ClusterPoint> points = new ArrayList<>(n);
        while (n-- > 0)
            points.add(ClusterPoint.newClusterPoint(n, rg.nextDouble() * size, rg.nextDouble() * size));
        return points;
    }

    /**
     * Create n clusters of m points in a 2D distribution of size * size. Clusters will be spread in a radius*radius
     * square.
     *
     * @param rg
     *            the rg
     * @param n
     *            the n
     * @param size
     *            the size
     * @param m
     *            the m
     * @param radius
     *            the radius
     * @return The points
     */
    private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int n, int size, int m,
            double radius)
    {
        return createClusters(rg, n, size, m, radius, null);
    }

    /**
     * Create n clusters of m points in a 2D distribution of size * size. Clusters will be spread in a radius*radius
     * square. Points will be selected randomly from the given number of frames.
     *
     * @param rg
     *            the rg
     * @param n
     *            the n
     * @param size
     *            the size
     * @param m
     *            the m
     * @param radius
     *            the radius
     * @param t
     *            the t
     * @return The points
     */
    private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int n, int size, int m,
            double radius, int t)
    {
        final int[] time = new int[t];
        for (int i = 0; i < t; i++)
            time[i] = i + 1;
        return createClusters(rg, n, size, m, radius, time);
    }

    /**
     * Create n clusters of m points in a 2D distribution of size * size. Clusters will be spread in a radius*radius
     * square. Points will be selected randomly from the given frames.
     *
     * @param rg
     *            the rg
     * @param n
     *            the n
     * @param size
     *            the size
     * @param m
     *            the m
     * @param radius
     *            the radius
     * @param time
     *            the time
     * @return The points
     */
    private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int n, int size, int m,
            double radius, int[] time)
    {
        final ArrayList<ClusterPoint> points = new ArrayList<>(n);
        int id = 0;
        if (time != null)
            if (time.length < m)
                throw new RuntimeException("Input time array must be at least as large as the number of points");
        while (n-- > 0)
        {
            final double x = rg.nextDouble() * size;
            final double y = rg.nextDouble() * size;
            if (time != null)
            {
                Random.shuffle(time, rg);
                for (int i = m; i-- > 0;)
                    points.add(ClusterPoint.newTimeClusterPoint(id++, x + rg.nextDouble() * radius,
                            y + rg.nextDouble() * radius, time[i], time[i]));
            }
            else
                for (int i = m; i-- > 0;)
                    points.add(ClusterPoint.newClusterPoint(id++, x + rg.nextDouble() * radius,
                            y + rg.nextDouble() * radius));
        }
        return points;
    }
}
