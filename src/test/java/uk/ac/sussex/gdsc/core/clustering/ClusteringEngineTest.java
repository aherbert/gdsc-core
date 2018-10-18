package uk.ac.sussex.gdsc.core.clustering;

import uk.ac.sussex.gdsc.core.utils.Random;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLog;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class ClusteringEngineTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(ClusteringEngineTest.class.getName());

  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  // Store the closest pair of clusters
  int ii, jj;

  @SeededTest
  public void canClusterClusterPointsAtDifferentDensitiesUsingCentroidLinkage(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.getSeedAsLong());
    for (final double radius : new double[] {5, 10, 20}) {
      for (final int size : new int[] {1000, 500, 300, 100}) {
        testClusting(rg, ClusteringAlgorithm.CENTROID_LINKAGE, radius, 100, size);
      }
    }
  }

  @SeededTest
  public void canClusterClusterPointsAtDifferentDensitiesUsingPairwiseWithoutNeighbours(
      RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.getSeedAsLong());
    for (final double radius : new double[] {5, 10, 20}) {
      for (final int size : new int[] {1000, 500, 300, 100}) {
        testClusting(rg, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius, 100, size);
      }
    }
  }

  @SpeedTag
  @SeededTest
  public void pairwiseWithoutNeighboursIsFasterThanCentroidLinkageAtLowDensities(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.getSeedAsLong());
    final int repeats = 10;
    final double radius = 50;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createClusters(rg, 20, 1000, 2, radius / 2);
    }

    final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
    final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius);

    logger.log(TestLog.getTimingRecord("(Low Density) Centroid-linkage", t1,
        "PairwiseWithoutNeighbours", t2));
  }

  @SpeedTag
  @SeededTest
  public void pairwiseWithoutNeighboursIsSlowerThanCentroidLinkageAtHighDensities(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.getSeedAsLong());
    final int repeats = 10;
    final double radius = 50;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createClusters(rg, 500, 1000, 2, radius / 2);
    }

    final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
    final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius);

    logger.log(TestLog.getTimingRecord("(High Density) Centroid-linkage", t1,
        "PairwiseWithoutNeighbours", t2));
    Assertions.assertTrue(t1 <= t2);
  }

  @SeededTest
  public void pairwiseIsFasterThanCentroidLinkage(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.getSeedAsLong());
    final int repeats = 20;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createPoints(rg, 500, 1000);
    }
    final double radius = 50;

    final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
    final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE, radius);

    logger.log(TestLog.getTimingRecord("Centroid-linkage", t1, "Pairwise", t2));
    Assertions.assertTrue(t2 <= t1);
  }

  @SeededTest
  public void canMultithreadParticleSingleLinkage(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedParticleSingleLinkageIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
  }

  @SeededTest
  public void canMultithreadClosest(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.CENTROID_LINKAGE);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedClosestIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.CENTROID_LINKAGE);
  }

  @SeededTest
  public void canMultithreadClosestParticle(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedClosestParticleIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
  }

  @SeededTest
  public void canMultithreadClosestDistancePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedClosestDistancePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SeededTest
  public void canMultithreadClosestTimePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedClosestTimePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SeededTest
  public void canMultithreadClosestParticleDistancePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedClosestParticleDistancePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SeededTest
  public void canMultithreadClosestParticleTimePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedClosestParticleTimePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SeededTest
  public void canMultithreadPairwiseWithoutNeighbours(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS);
  }

  @SpeedTag
  @SeededTest
  public void multithreadedPairwiseWithoutNeighboursIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.getSeedAsLong()),
        ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS);
  }

  private static void runMultithreadingAlgorithmTest(UniformRandomProvider rg,
      ClusteringAlgorithm algorithm) {
    final double radius = 50;
    final int time = 10;
    final ArrayList<ClusterPoint> points = createClusters(rg, 500, 1000, 2, radius / 2, time);
    final ClusteringEngine engine = new ClusteringEngine(0, algorithm);
    final List<Cluster> exp = engine.findClusters(points, radius, time);
    engine.setThreadCount(8);
    final List<Cluster> obs = engine.findClusters(points, radius, time);
    compareClusters(exp, obs);
  }

  private static void runMultithreadingSpeedTest(UniformRandomProvider rg,
      ClusteringAlgorithm algorithm) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final int cores = Runtime.getRuntime().availableProcessors();
    final int testCores = 4;
    Assumptions.assumeTrue(cores >= testCores,
        () -> String.format("Multi-threading test requires %d cores", testCores));

    final int repeats = 5;
    final double radius = 50;
    final int time = 10;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createClusters(rg, 1000, 1000, 2, radius / 2, time);
    }

    final long t1 = runSpeedTest(points, algorithm, radius, time, 1);
    final long t2 = runSpeedTest(points, algorithm, radius, time, testCores);

    logger.log(TestLog.getTimingRecord(algorithm.toString() + " Single", t1,
        "Multi-threaded 4-cores", t2));
    // Assertions.assertTrue(t2 <= t1);
  }

  private static long runSpeedTest(Object[] points, ClusteringAlgorithm algorithm, double radius) {
    return runSpeedTest(points, algorithm, radius, 0, 1);
  }

  @SuppressWarnings("unchecked")
  private static long runSpeedTest(Object[] points, ClusteringAlgorithm algorithm, double radius,
      int time, int threadCount) {
    final ClusteringEngine engine = new ClusteringEngine(threadCount, algorithm);

    // Initialise
    engine.findClusters((ArrayList<ClusterPoint>) points[0], radius, time);

    final long start = System.nanoTime();
    for (int i = 0; i < points.length; i++) {
      engine.findClusters((ArrayList<ClusterPoint>) points[i], radius, time);
    }
    return System.nanoTime() - start;
  }

  private void testClusting(UniformRandomProvider rg, ClusteringAlgorithm algorithm, double radius,
      int n, int size) {
    final ClusteringEngine engine = new ClusteringEngine();
    engine.setClusteringAlgorithm(algorithm);
    final ArrayList<ClusterPoint> points = createPoints(rg, n, size);

    // Report density of the clustering we are testing. Size/radius are in nm
    // TestLog.debug(logger,"Testing n=%d, Size=%d, Density=%s um^-2, Radius=%s nm", n, size,
    // MathUtils.rounded(n * 1e6 / (size * size)), MathUtils.rounded(radius));

    final List<Cluster> exp = findClusters(points, radius);
    final List<Cluster> obs = engine.findClusters(points, radius);
    compareClusters(exp, obs);
  }

  private static void compareClusters(List<Cluster> exp, List<Cluster> obs)
      throws AssertionError {
    Collections.sort(exp);
    Collections.sort(obs);

    try {
      Assertions.assertEquals(exp.size(), obs.size(), "# clusters is different");
      for (int i = 0; i < exp.size(); i++) {
        assertEqual(i, exp.get(i), obs.get(i));
      }
    } catch (final AssertionError ex) {
      print("Expected", exp);
      print("Observed", obs);
      throw ex;
    }
  }

  private static void print(String name, List<Cluster> clusters) {
    logger.info(FunctionUtils.getSupplier(name + " : size=%d", clusters.size()));
    for (int i = 0; i < clusters.size(); i++) {
      final Cluster c = clusters.get(i);
      logger.info(FunctionUtils.getSupplier("[%d] : head=%d, n=%d, cx=%g, cy=%g", i,
          c.getHeadClusterPoint().getId(), c.getSize(), c.getX(), c.getY()));
    }
  }

  private static void assertEqual(int i, Cluster cluster, Cluster cluster2) {
    Assertions.assertEquals(cluster.getSize(), cluster2.getSize(),
        () -> String.format("Cluster %d: Size is different", i));
    Assertions.assertEquals(cluster.getX(), cluster2.getX(), 1e-4,
        () -> String.format("Cluster %d: X is different", i));
    Assertions.assertEquals(cluster.getY(), cluster2.getY(), 1e-4,
        () -> String.format("Cluster %d: Y is different", i));
    // Q. Should we check each cluster member is the same ?
  }

  /**
   * Perform centroid-linkage clustering up to the given radius.
   *
   * @param points the points
   * @param radius the radius
   * @return The clusters
   */
  private ArrayList<Cluster> findClusters(ArrayList<ClusterPoint> points, double radius) {
    // Initialise all clusters with one molecule
    final ArrayList<Cluster> clusters = new ArrayList<>(points.size());
    for (int i = 0; i < points.size(); i++) {
      final ClusterPoint m = points.get(i);
      clusters.add(new Cluster(ClusterPoint.newClusterPoint(i, m.getX(), m.getY())));
    }

    // Iteratively find the closest pair
    while (findClosest(clusters, radius)) {
      clusters.get(ii).add(clusters.get(jj));
      clusters.remove(jj);
    }

    return clusters;
  }

  /**
   * Implement and all-vs-all search for the closest pair of clusters within the given radius. Set
   * the class level variables ii and jj to the indices of the closest pair.
   *
   * @param clusters the clusters
   * @param radius the radius
   * @return True if a pair was found
   */
  private boolean findClosest(ArrayList<Cluster> clusters, double radius) {
    double minD = radius * radius;
    ii = -1;
    for (int i = 0; i < clusters.size(); i++) {
      final Cluster c1 = clusters.get(i);
      for (int j = i + 1; j < clusters.size(); j++) {
        final double d2 = c1.distance2(clusters.get(j));
        if (d2 < minD) {
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
   * @param rg the rg
   * @param n the n
   * @param size the size
   * @return The points
   */
  private static ArrayList<ClusterPoint> createPoints(UniformRandomProvider rg, int n, int size) {
    final ArrayList<ClusterPoint> points = new ArrayList<>(n);
    while (n-- > 0) {
      points.add(ClusterPoint.newClusterPoint(n, rg.nextDouble() * size, rg.nextDouble() * size));
    }
    return points;
  }

  /**
   * Create n clusters of m points in a 2D distribution of size * size. Clusters will be spread in a
   * radius*radius square.
   *
   * @param rg the rg
   * @param n the n
   * @param size the size
   * @param m the m
   * @param radius the radius
   * @return The points
   */
  private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int n, int size,
      int m, double radius) {
    return createClusters(rg, n, size, m, radius, null);
  }

  /**
   * Create n clusters of m points in a 2D distribution of size * size. Clusters will be spread in a
   * radius*radius square. Points will be selected randomly from the given number of frames.
   *
   * @param rg the rg
   * @param n the n
   * @param size the size
   * @param m the m
   * @param radius the radius
   * @param t the t
   * @return The points
   */
  private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int n, int size,
      int m, double radius, int t) {
    final int[] time = new int[t];
    for (int i = 0; i < t; i++) {
      time[i] = i + 1;
    }
    return createClusters(rg, n, size, m, radius, time);
  }

  /**
   * Create n clusters of m points in a 2D distribution of size * size. Clusters will be spread in a
   * radius*radius square. Points will be selected randomly from the given frames.
   *
   * @param rg the rg
   * @param n the n
   * @param size the size
   * @param m the m
   * @param radius the radius
   * @param time the time
   * @return The points
   */
  private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int n, int size,
      int m, double radius, int[] time) {
    final ArrayList<ClusterPoint> points = new ArrayList<>(n);
    int id = 0;
    if (time != null) {
      if (time.length < m) {
        throw new RuntimeException(
            "Input time array must be at least as large as the number of points");
      }
    }
    while (n-- > 0) {
      final double x = rg.nextDouble() * size;
      final double y = rg.nextDouble() * size;
      if (time != null) {
        Random.shuffle(time, rg);
        for (int i = m; i-- > 0;) {
          points.add(ClusterPoint.newTimeClusterPoint(id++, x + rg.nextDouble() * radius,
              y + rg.nextDouble() * radius, time[i], time[i]));
        }
      } else {
        for (int i = m; i-- > 0;) {
          points.add(ClusterPoint.newClusterPoint(id++, x + rg.nextDouble() * radius,
              y + rg.nextDouble() * radius));
        }
      }
    }
    return points;
  }
}
