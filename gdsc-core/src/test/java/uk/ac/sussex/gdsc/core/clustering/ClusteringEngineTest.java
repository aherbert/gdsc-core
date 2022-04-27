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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

package uk.ac.sussex.gdsc.core.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.logging.TrackProgressAdapter;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogging;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FormatSupplier;

@SuppressWarnings({"javadoc"})
class ClusteringEngineTest {
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
  int ii;
  int jj;

  /**
   * Test the pairwise clustering.
   *
   * <p>Note: ClusteringAlgorithm.PAIRWISE fails the centroid linkage clustering test (see
   * {@code #testClusting}) so the algorithm it explicitly tested.
   */
  @Test
  void canClusterPairwise() {
    final ClusteringEngine engine = new ClusteringEngine();
    engine.setClusteringAlgorithm(ClusteringAlgorithm.PAIRWISE);
    final ArrayList<ClusterPoint> points = new ArrayList<>();
    int id = 0;
    points.add(new ClusterPoint(id++, 0, 0));
    points.add(new ClusterPoint(id++, 0, 1));
    points.add(new ClusterPoint(id++, 5, 5));
    points.add(new ClusterPoint(id++, 5, 6));
    points.add(new ClusterPoint(id++, 3, 5));
    // Require some outliers to ensure a full grid is created using multiple bins on x and y
    points.add(new ClusterPoint(id++, 0, 10));
    points.add(new ClusterPoint(id++, 0, 11));
    points.add(new ClusterPoint(id++, 12, 10));
    points.add(new ClusterPoint(id++, 12, 11));
    points.add(new ClusterPoint(id++, 12, 0));
    points.add(new ClusterPoint(id++, 12, 1));
    // @formatter:off
    // Expected:
    // +   0  1  2  3  4  5             12
    // 0   0  1                          9
    // 1                                10
    // 2
    // 3
    // 4
    // 5             5    2
    // 6                  3
    //
    // 10  5                             7
    // 11  6                             8
    // @formatter:on
    id = 0;
    final List<Cluster> exp = new ArrayList<>();
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));

    // Radius close enough to join point 2 to 5 but not cluster 1 to cluster 2
    double radius = 0.1 + points.get(1).distance(points.get(4));

    // Scramble the points
    Collections.shuffle(points, ThreadLocalRandom.current());

    final List<Cluster> obs = engine.findClusters(points, radius);
    compareClusters(exp, obs);

    // Smaller radius to create multiple bins.
    // Note that the radius must join 5 to the cluster of 3-4.
    radius = Math.sqrt(2 * 2 + 0.5 * 0.5) * 1.01;
    compareClusters(exp, engine.findClusters(points, radius));

    // Test with no candidates (i.e. radius too small)
    final List<Cluster> singles = points.stream().map(Cluster::new).collect(Collectors.toList());
    compareClusters(singles, engine.findClusters(points, 0.1));
  }

  @Test
  void testWithRadiusZero() {
    final ClusteringEngine engine = new ClusteringEngine();
    final ArrayList<ClusterPoint> points = new ArrayList<>();

    // Handle empty
    final double radius = 0.0;
    compareClusters(Collections.emptyList(), engine.findClusters(points, radius));

    // Create colocated points with time
    int id = 0;
    points.add(new ClusterPoint(id++, 0, 0, 1, 1));
    points.add(new ClusterPoint(id++, 0, 0, 2, 2));
    points.add(new ClusterPoint(id++, 5, 5, 2, 3));
    points.add(new ClusterPoint(id++, 5, 5, 5, 5));
    points.add(new ClusterPoint(id++, 5, 5, 6, 6));
    points.add(new ClusterPoint(id++, 5, 15, 5, 5));
    points.add(new ClusterPoint(id++, 5, 15, 5, 5)); // Same time

    // Scramble the test points
    List<ClusterPoint> testPoints = new ArrayList<>(points);
    Collections.shuffle(testPoints, ThreadLocalRandom.current());

    // Clustering with no time information
    id = 0;
    final List<Cluster> exp = new ArrayList<>();
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));
    compareClusters(exp, engine.findClusters(testPoints, radius));
    int time = 45;
    engine.setClusteringAlgorithm(ClusteringAlgorithm.CENTROID_LINKAGE);
    compareClusters(exp, engine.findClusters(testPoints, radius, time));

    // Clustering with time information should split the two points with the same time
    id = 0;
    exp.clear();
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    engine.setClusteringAlgorithm(ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
    compareClusters(exp, engine.findClusters(testPoints, radius, time));

    // Smaller time gap should split the clusters
    time = 1;
    id = 0;
    exp.clear();
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    compareClusters(exp, engine.findClusters(testPoints, radius, time));

    time = 0;
    id = 0;
    exp.clear();
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    compareClusters(exp, engine.findClusters(testPoints, radius, time));

    // Add test using the pulse interval to prevent merge.
    engine.setPulseInterval(3);
    time = 10;
    id = 0;
    exp.clear();
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++)));
    exp.add(create(points.get(id++)));
    compareClusters(exp, engine.findClusters(testPoints, radius, time));

    // Remove time information and the clustering should ignore the time
    testPoints = testPoints.stream().map(p -> new ClusterPoint(p.getId(), p.getX(), p.getY()))
        .collect(Collectors.toList());
    engine.setPulseInterval(0);
    id = 0;
    exp.clear();
    exp.add(create(points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++), points.get(id++)));
    exp.add(create(points.get(id++), points.get(id++)));
    compareClusters(exp, engine.findClusters(testPoints, radius, 45));
    compareClusters(exp, engine.findClusters(testPoints, radius, 1));
    compareClusters(exp, engine.findClusters(testPoints, radius, 0));
  }

  @Test
  void testParticleSingleLinkageWithNoClusters() {
    final ClusteringEngine engine = new ClusteringEngine();
    engine.setClusteringAlgorithm(ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
    final ArrayList<ClusterPoint> points = new ArrayList<>();
    points.add(new ClusterPoint(0, 0, 0));
    points.add(new ClusterPoint(1, 0, 1));

    // Test with no candidates (i.e. radius too small)
    final List<Cluster> singles = points.stream().map(Cluster::new).collect(Collectors.toList());
    compareClusters(singles, engine.findClusters(points, 0.1));
  }

  @Test
  void testParticleSingleLinkageWithTrackJoins() {
    final ClusteringEngine engine = new ClusteringEngine();
    Assertions.assertFalse(engine.isTrackJoins());
    engine.setClusteringAlgorithm(ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
    engine.setTrackJoins(true);
    Assertions.assertTrue(engine.isTrackJoins());

    final ArrayList<ClusterPoint> points = new ArrayList<>();
    // Control IDs as we measure intra and inter ID distances
    points.add(new ClusterPoint(0, 0, 0));
    points.add(new ClusterPoint(0, 0, 1));
    points.add(new ClusterPoint(1, 5, 5));
    points.add(new ClusterPoint(2, 5, 6));
    points.add(new ClusterPoint(2, 3, 5));
    // @formatter:off
    // Expected:
    // +   0  1  2  3  4  5
    // 0   0  0
    // 1
    // 2
    // 3
    // 4
    // 5             2    1
    // 6                  2
    // @formatter:on
    int id = 0;
    final Cluster c1 = new Cluster(points.get(id++));
    c1.add(points.get(id++));
    final Cluster c2 = new Cluster(points.get(id++));
    c2.add(points.get(id++));
    c2.add(points.get(id++));
    // Radius close enough to join point 2 to 5 but not cluster 1 to cluster 2
    final double radius = 0.1 + points.get(1).distance(points.get(4));

    // Scramble the points
    Collections.shuffle(points, ThreadLocalRandom.current());

    final List<Cluster> obs = engine.findClusters(points, radius);
    final List<Cluster> exp = Arrays.asList(c1, c2);
    compareClusters(exp, obs);

    assertDistances(new double[] {1}, engine.getIntraIdDistances());
    assertDistances(new double[] {1, 2}, engine.getInterIdDistances());

    // Test with no candidates (i.e. radius too small)
    final List<Cluster> singles = points.stream().map(Cluster::new).collect(Collectors.toList());
    compareClusters(singles, engine.findClusters(points, 0.1));

    assertDistances(new double[] {}, engine.getIntraIdDistances());
    assertDistances(new double[] {}, engine.getInterIdDistances());
  }

  private static void assertDistances(double[] exp, double[] obs) {
    Arrays.sort(obs);
    Assertions.assertArrayEquals(exp, obs);
  }

  @Test
  void testWithEndedTracker() {
    final ClusteringEngine engine = new ClusteringEngine();
    final ArrayList<ClusterPoint> points = new ArrayList<>();
    int id = 0;
    // Require time information for some algorithms
    points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 0, 1, 1));
    points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 1, 2, 2));
    points.add(ClusterPoint.newTimeClusterPoint(id++, 1, 0, 3, 3));

    // Test with an ended tracker
    final TrackProgress tracker = new TrackProgressAdapter() {
      @Override
      public boolean isEnded() {
        return true;
      }

      @Override
      public void log(String format, Object... args) {
        // Do nothing
      }
    };
    engine.setTracker(tracker);
    Assertions.assertSame(tracker, engine.getTracker());

    final double radius = 5;
    final int time = 1;
    for (final ClusteringAlgorithm algorithm : ClusteringAlgorithm.values()) {
      engine.setClusteringAlgorithm(algorithm);
      Assertions.assertEquals(algorithm, engine.getClusteringAlgorithm());
      Assertions.assertNull(engine.findClusters(points, radius, time), () -> algorithm.toString());
    }
  }

  @Test
  void testWithTracker() {
    final ClusteringEngine engine = new ClusteringEngine();
    final ArrayList<ClusterPoint> points = new ArrayList<>();
    int id = 0;
    // Require time information for some algorithms
    points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 0, 1, 1));
    points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 1, 2, 2));
    points.add(ClusterPoint.newTimeClusterPoint(id++, 1, 0, 3, 3));

    final Cluster c = new Cluster(points.get(0));
    c.add(points.get(1));
    c.add(points.get(2));
    final List<Cluster> all = Collections.singletonList(c);

    final List<Cluster> singles = points.stream().map(Cluster::new).collect(Collectors.toList());

    // Test with a tracker to exercise code paths
    final TrackProgress tracker = new TrackProgressAdapter() {
      @Override
      public void log(String format, Object... args) {
        // Do nothing
      }
    };
    engine.setTracker(tracker);
    Assertions.assertSame(tracker, engine.getTracker());

    final double radius = 5;
    final double smallRadius = 0.1;
    final int time = 1;
    for (final ClusteringAlgorithm algorithm : ClusteringAlgorithm.values()) {
      engine.setClusteringAlgorithm(algorithm);
      compareClusters(all, engine.findClusters(points, radius, time));
      compareClusters(singles, engine.findClusters(points, smallRadius, time));
    }
  }

  @Test
  void testWithPulseInterval() {
    final ClusteringEngine engine = new ClusteringEngine();
    final ArrayList<ClusterPoint> points = new ArrayList<>();
    int id = 0;
    // Require time information for some algorithms
    points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 0, 1, 1));
    points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 1, 2, 2));
    points.add(ClusterPoint.newTimeClusterPoint(id++, 1, 0, 3, 3));

    final Cluster c = new Cluster(points.get(0));
    c.add(points.get(1));
    c.add(points.get(2));
    final List<Cluster> all = Collections.singletonList(c);

    final Cluster c1 = new Cluster(points.get(0));
    c1.add(points.get(1));
    final Cluster c2 = new Cluster(points.get(2));
    final List<Cluster> twoClusters = Arrays.asList(c1, c2);

    final List<Cluster> singles = points.stream().map(Cluster::new).collect(Collectors.toList());

    for (int i = 0; i <= 3; i++) {
      engine.setPulseInterval(i);
      Assertions.assertEquals(i, engine.getPulseInterval());
    }

    final double radius = 5;
    final int time = 1;
    for (final ClusteringAlgorithm algorithm : EnumSet.of(
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY,
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY,
        ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY,
        ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY)) {
      engine.setClusteringAlgorithm(algorithm);
      engine.setPulseInterval(1);
      compareClusters(singles, engine.findClusters(points, radius, time));
      engine.setPulseInterval(2);
      compareClusters(twoClusters, engine.findClusters(points, radius, time));
      engine.setPulseInterval(3);
      compareClusters(all, engine.findClusters(points, radius, time));
    }
  }

  @Test
  void testWithTimeInformation() {
    // No time information
    assertTimeInformation(new int[][] {{0, 0}, {0, 0}}, new int[][] {{0, 1}});
    assertTimeInformation(new int[][] {{0, 1}, {0, 1}}, new int[][] {{0, 1}});
    // Different frames
    assertTimeInformation(new int[][] {{0, 0}, {1, 1}}, new int[][] {{0, 1}});
    assertTimeInformation(new int[][] {{0, 0}, {1, 1}, {3, 3}}, new int[][] {{0, 1}, {2}});
    assertTimeInformation(new int[][] {{0, 0}, {1, 1}, {3, 3}}, new int[][] {{0, 1, 2}}, 3);
    assertTimeInformation(new int[][] {{0, 0}, {3, 3}, {1, 1}}, new int[][] {{0, 2}, {1}});
    assertTimeInformation(new int[][] {{0, 0}, {3, 3}, {1, 1}}, new int[][] {{0, 1, 2}}, 3);
    // Overlap frames
    assertTimeInformation(new int[][] {{0, 1}, {0, 0}}, new int[][] {{0}, {1}});
    assertTimeInformation(new int[][] {{0, 1}, {1, 1}}, new int[][] {{0}, {1}});
  }

  private static void assertTimeInformation(int[][] times, int[][] expected) {
    assertTimeInformation(times, expected, 1);
  }

  private static void assertTimeInformation(int[][] times, int[][] expected, int time) {
    final ArrayList<ClusterPoint> points = new ArrayList<>();
    int id = 0;
    for (final int[] t : times) {
      points.add(ClusterPoint.newTimeClusterPoint(id++, 0, 0, t[0], t[1]));
    }

    final ArrayList<Cluster> clusters = new ArrayList<>();
    for (final int[] e : expected) {
      final Cluster c = new Cluster(points.get(e[0]));
      for (int i = 1; i < e.length; i++) {
        c.add(points.get(e[i]));
      }
      clusters.add(c);
    }

    // These should switch to the non-time based algorithm
    final EnumSet<ClusteringAlgorithm> set =
        EnumSet.of(ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY,
            ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY,
            ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY,
            ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);

    final double radius = 5;
    final ClusteringEngine engine = new ClusteringEngine();
    for (final ClusteringAlgorithm algorithm : set) {
      engine.setClusteringAlgorithm(algorithm);
      compareClusters(clusters, engine.findClusters(points, radius, time));
    }
  }

  @SeededTest
  void canClusterClusterPointsAtDifferentDensitiesUsingCentroidLinkage(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    for (final double radius : new double[] {5, 10, 20}) {
      for (final int size : new int[] {1000, 500, 300, 100}) {
        testClusting(rg, ClusteringAlgorithm.CENTROID_LINKAGE, radius, 100, size);
      }
    }
  }

  @SeededTest
  void canClusterClusterPointsAtDifferentDensitiesUsingPairwiseWithoutNeighbours(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    for (final double radius : new double[] {5, 10, 20}) {
      for (final int size : new int[] {1000, 500, 300, 100}) {
        testClusting(rg, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius, 100, size);
      }
    }
  }

  @SpeedTag
  @SeededTest
  void pairwiseWithoutNeighboursIsFasterThanCentroidLinkageAtLowDensities(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final int repeats = 10;
    final double radius = 50;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createClusters(rg, 20, 1000, 2, radius / 2);
    }

    final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
    final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius);

    logger.log(TestLogging.getTimingRecord("(Low Density) Centroid-linkage", t1,
        "PairwiseWithoutNeighbours", t2));
  }

  @SpeedTag
  @SeededTest
  void pairwiseWithoutNeighboursIsSlowerThanCentroidLinkageAtHighDensities(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final int repeats = 10;
    final double radius = 50;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createClusters(rg, 500, 1000, 2, radius / 2);
    }

    final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
    final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS, radius);

    logger.log(TestLogging.getTimingRecord("(High Density) Centroid-linkage", t1,
        "PairwiseWithoutNeighbours", t2));
    Assertions.assertTrue(t1 <= t2);
  }

  @SeededTest
  void pairwiseIsFasterThanCentroidLinkage(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final int repeats = 20;
    final Object[] points = new Object[repeats];
    for (int i = 0; i < repeats; i++) {
      points[i] = createPoints(rg, 500, 1000);
    }
    final double radius = 50;

    final long t1 = runSpeedTest(points, ClusteringAlgorithm.CENTROID_LINKAGE, radius);
    final long t2 = runSpeedTest(points, ClusteringAlgorithm.PAIRWISE, radius);

    logger.log(TestLogging.getTimingRecord("Centroid-linkage", t1, "Pairwise", t2));
    Assertions.assertTrue(t2 <= t1);
  }

  @SeededTest
  void canMultithreadParticleSingleLinkage(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
  }

  @SpeedTag
  @SeededTest
  void multithreadedParticleSingleLinkageIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE);
  }

  @SeededTest
  void canMultithreadClosest(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.CENTROID_LINKAGE);
  }

  @SpeedTag
  @SeededTest
  void multithreadedClosestIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()), ClusteringAlgorithm.CENTROID_LINKAGE);
  }

  @SeededTest
  void canMultithreadClosestParticle(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
  }

  @SpeedTag
  @SeededTest
  void multithreadedClosestParticleIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
  }

  @SeededTest
  void canMultithreadClosestDistancePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  void multithreadedClosestDistancePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SeededTest
  void canMultithreadClosestTimePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  void multithreadedClosestTimePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SeededTest
  void canMultithreadClosestParticleDistancePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  void multithreadedClosestParticleDistancePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
  }

  @SeededTest
  void canMultithreadClosestParticleTimePriority(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SpeedTag
  @SeededTest
  void multithreadedClosestParticleTimePriorityIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
  }

  @SeededTest
  void canMultithreadPairwiseWithoutNeighbours(RandomSeed seed) {
    runMultithreadingAlgorithmTest(RngFactory.create(seed.get()),
        ClusteringAlgorithm.PAIRWISE_WITHOUT_NEIGHBOURS);
  }

  @SpeedTag
  @SeededTest
  void multithreadedPairwiseWithoutNeighboursIsFaster(RandomSeed seed) {
    runMultithreadingSpeedTest(RngFactory.create(seed.get()),
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
    Assertions.assertEquals(8, engine.getThreadCount());
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

    logger.log(TestLogging.getTimingRecord(algorithm.toString() + " Single", t1,
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

  private static void compareClusters(List<Cluster> exp, List<Cluster> obs) throws AssertionError {
    Collections.sort(exp, ClusterComparator.getInstance());
    Collections.sort(obs, ClusterComparator.getInstance());

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
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier(name + " : size=%d", clusters.size()));
    for (int i = 0; i < clusters.size(); i++) {
      final Cluster c = clusters.get(i);
      logger.log(TestLevel.TEST_INFO,
          FormatSupplier.getSupplier("[%d] : head=%d, n=%d, cx=%g, cy=%g", i,
              c.getHeadClusterPoint().getId(), c.getSize(), c.getX(), c.getY()));
    }
  }

  private static void assertEqual(int index, Cluster cluster, Cluster cluster2) {
    Assertions.assertEquals(cluster.getSize(), cluster2.getSize(),
        () -> String.format("Cluster %d: Size is different", index));
    Assertions.assertEquals(cluster.getX(), cluster2.getX(), 1e-4,
        () -> String.format("Cluster %d: X is different", index));
    Assertions.assertEquals(cluster.getY(), cluster2.getY(), 1e-4,
        () -> String.format("Cluster %d: Y is different", index));
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
   * Create points in a 2D distribution of size * size.
   *
   * @param rg the random generator
   * @param totalClusters the totalClusters
   * @param size the size
   * @return The points
   */
  private static ArrayList<ClusterPoint> createPoints(UniformRandomProvider rg, int totalClusters,
      int size) {
    final ArrayList<ClusterPoint> points = new ArrayList<>(totalClusters);
    while (totalClusters-- > 0) {
      points.add(ClusterPoint.newClusterPoint(totalClusters, rg.nextDouble() * size,
          rg.nextDouble() * size));
    }
    return points;
  }

  /**
   * Create clusters of clusterSize points in a 2D distribution of size * size. Clusters will be
   * spread in a radius*radius square.
   *
   * @param rg the random generator
   * @param totalClusters the totalClusters
   * @param size the size
   * @param clusterSize the clusterSize
   * @param radius the radius
   * @return The points
   */
  private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int totalClusters,
      int size, int clusterSize, double radius) {
    return createClusters(rg, totalClusters, size, clusterSize, radius, null);
  }

  /**
   * Create clusters of clusterSize points in a 2D distribution of size * size. Clusters will be
   * spread in a radius*radius square. Points will be selected randomly from the given number of
   * frames.
   *
   * @param rg the random generator
   * @param totalClusters the totalClusters
   * @param size the size
   * @param clusterSize the clusterSize
   * @param radius the radius
   * @param maxTime the maxTime
   * @return The points
   */
  private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int totalClusters,
      int size, int clusterSize, double radius, int maxTime) {
    final int[] time = new int[maxTime];
    for (int i = 0; i < maxTime; i++) {
      time[i] = i + 1;
    }
    return createClusters(rg, totalClusters, size, clusterSize, radius, time);
  }

  /**
   * Create clusters of clusterSize points in a 2D distribution of size * size. Clusters will be
   * spread in a radius*radius square. Points will be selected randomly from the given frames.
   *
   * @param rg the random generator
   * @param totalClusters the totalClusters
   * @param size the size
   * @param clusterSize the clusterSize
   * @param radius the radius
   * @param time the time
   * @return The points
   */
  private static ArrayList<ClusterPoint> createClusters(UniformRandomProvider rg, int totalClusters,
      int size, int clusterSize, double radius, int[] time) {
    final ArrayList<ClusterPoint> points = new ArrayList<>(totalClusters);
    int id = 0;
    if (time != null) {
      if (time.length < clusterSize) {
        throw new RuntimeException(
            "Input time array must be at least as large as the number of points");
      }
    }
    while (totalClusters-- > 0) {
      final double x = rg.nextDouble() * size;
      final double y = rg.nextDouble() * size;
      if (time != null) {
        RandomUtils.shuffle(time, rg);
        for (int i = clusterSize; i-- > 0;) {
          points.add(ClusterPoint.newTimeClusterPoint(id++, x + rg.nextDouble() * radius,
              y + rg.nextDouble() * radius, time[i], time[i]));
        }
      } else {
        for (int i = clusterSize; i-- > 0;) {
          points.add(ClusterPoint.newClusterPoint(id++, x + rg.nextDouble() * radius,
              y + rg.nextDouble() * radius));
        }
      }
    }
    return points;
  }

  /**
   * Creates the cluster.
   *
   * @param points the points
   * @return the cluster
   */
  private static Cluster create(ClusterPoint p0, ClusterPoint... points) {
    final Cluster c = new Cluster(p0);
    Arrays.stream(points).forEach(c::add);
    return c;
  }
}
