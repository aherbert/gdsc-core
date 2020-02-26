package uk.ac.sussex.gdsc.core.match;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link AucCalculator}.
 */
@SuppressWarnings({"javadoc"})
public class ClosestPairCalculatorTest {
  @Test
  public void testGetSize() {
    Assertions.assertEquals(0, ClosestPairCalculator.getSize(null));
    Assertions.assertEquals(1,
        ClosestPairCalculator.getSize(Arrays.asList(new Point2D.Double(1, 2))));
  }

  @Test
  public void testToRandomAccess() {
    final Point2D[] raw = new Point2D[] {new Point2D.Double(1, 2), new Point2D.Double(3, 4)};

    final Collection<Point2D> points = new TreeSet<>(ClosestPairCalculatorTest::compare);
    Arrays.stream(raw).forEachOrdered(points::add);

    assertRandomAccess(raw, ClosestPairCalculator.toRandomAccess(points));
    assertRandomAccess(raw, ClosestPairCalculator.toRandomAccess(Arrays.asList(raw)));
    assertRandomAccess(raw, ClosestPairCalculator.toRandomAccess(new LinkedList<>(points)));
  }

  private static void assertRandomAccess(Point2D[] raw, IntFunction<Point2D> list) {
    for (int i = 0; i < raw.length; i++) {
      Assertions.assertEquals(raw[i], list.apply(i));
    }
  }

  @Test
  public void testIdRecursion() {
    // Mimic the id increase during recursion in the worst case scenario.
    // As long as the value does not wrap an unsigned 32-bit integer this is OK as
    // negative values are still unique from the unallocated Id of zero.
    int low = 0;
    final int high = Integer.MAX_VALUE;
    int size = high - low;
    long id = 1;
    while (size > 3) {
      low = (low + high) >>> 1;
      id = id * 2 + 1;
      size = high - low;
    }
    // Check the value has not wrapped an unsigned 32-bit integer
    Assertions.assertTrue(id < (1L << 32));
  }

  @Test
  public void testBadArguments() {
    // Valid arguments
    final Point2D[] raw = new Point2D[] {new Point2D.Double(1, 2), new Point2D.Double(3, 4)};
    final List<Point2D> points = Arrays.asList(raw);
    final ToDoubleFunction<Point2D> getX = Point2D::getX;
    final ToDoubleFunction<Point2D> getY = Point2D::getY;

    // Point2D[] method
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(null), "null Point2D[]");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(new Point2D[0]), "empty Point2D[]");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(new Point2D[] {raw[0]}),
        "size 1 Point2D[]");

    // Generic method
    Assertions.assertThrows(NullPointerException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(null, getX, getY), "null Collection<T>");
    Assertions.assertThrows(NullPointerException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(points, null, getY), "null getX");
    Assertions.assertThrows(NullPointerException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(points, getX, null), "null getY");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ClosestPairCalculator.closestPairPartitioned(new ArrayList<>(), getX, getY),
        "empty Collection<T>");
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> ClosestPairCalculator
            .closestPairPartitioned(Arrays.asList(new Point2D.Double(0, 0)), getX, getY),
        "size 1 Collection<T>");
  }

  @Test
  public void testClosestPairOfThree() {
    final Point2D p1 = new Point2D.Double(0, 0);
    final Point2D p2 = new Point2D.Double(1, 0);
    final Point2D p3 = new Point2D.Double(4, 0);
    assertClosestPairOfThree(p1, p2, Arrays.asList(p1, p2, p3));
    assertClosestPairOfThree(p1, p2, Arrays.asList(p1, p3, p2));
    assertClosestPairOfThree(p1, p2, Arrays.asList(p2, p1, p3));
    assertClosestPairOfThree(p1, p2, Arrays.asList(p2, p3, p1));
    assertClosestPairOfThree(p1, p2, Arrays.asList(p3, p1, p2));
    assertClosestPairOfThree(p1, p2, Arrays.asList(p3, p2, p1));
  }

  private static void assertClosestPairOfThree(Point2D p1, Point2D p2, List<Point2D> list) {
    final Point2D[] expected = new Point2D[] {p1, p2};
    sort(expected);

    // Point2D[] method
    Pair<Point2D, Point2D> pair =
        ClosestPairCalculator.closestPairPartitioned(list.toArray(new Point2D[0]));
    Point2D[] actual = new Point2D[] {pair.getLeft(), pair.getRight()};
    Assertions.assertArrayEquals(expected, actual, "Point2D[] method");

    // Generic method
    pair = ClosestPairCalculator.closestPairPartitioned(list, Point2D::getX, Point2D::getY);
    actual = new Point2D[] {pair.getLeft(), pair.getRight()};
    Assertions.assertArrayEquals(expected, actual, "Collection<T> method");
  }

  @SeededTest
  public void testClosestPairPartitioned2(RandomSeed seed) {
    assertClosestPairPartitioned(2, seed);
  }

  @SeededTest
  public void testClosestPairPartitioned3(RandomSeed seed) {
    assertClosestPairPartitioned(3, seed);
  }

  @SeededTest
  public void testClosestPairPartitioned10(RandomSeed seed) {
    assertClosestPairPartitioned(10, seed);
  }

  @SeededTest
  public void testClosestPairPartitioned100(RandomSeed seed) {
    assertClosestPairPartitioned(100, seed);
  }

  private static void assertClosestPairPartitioned(int size, RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final List<Point2D> list = IntStream.range(0, size)
        .mapToObj(i -> (Point2D) new Point2D.Double(rng.nextDouble(), rng.nextDouble()))
        .collect(Collectors.toList());

    final Point2D[] expected = findClosest(list);

    // Point2D[] method
    Pair<Point2D, Point2D> pair =
        ClosestPairCalculator.closestPairPartitioned(list.toArray(new Point2D[0]));
    Point2D[] actual = new Point2D[] {pair.getLeft(), pair.getRight()};
    sort(actual);
    Assertions.assertArrayEquals(expected, actual);

    // Generic method
    pair = ClosestPairCalculator.closestPairPartitioned(list, Point2D::getX, Point2D::getY);
    actual = new Point2D[] {pair.getLeft(), pair.getRight()};
    sort(actual);
    Assertions.assertArrayEquals(expected, actual);
  }

  @SeededTest
  public void testClosestPairAllVsAll10(RandomSeed seed) {
    assertClosestPairAllVsAll(10, seed);
  }

  private static void assertClosestPairAllVsAll(int size, RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final List<Point2D> list = IntStream.range(0, size)
        .mapToObj(i -> (Point2D) new Point2D.Double(rng.nextDouble(), rng.nextDouble()))
        .collect(Collectors.toList());

    final Point2D[] expected = findClosest(list);

    // Point2D[] method
    Pair<Point2D, Point2D> pair =
        ClosestPairCalculator.closestPairAllVsAll(list.toArray(new Point2D[0]));
    Point2D[] actual = new Point2D[] {pair.getLeft(), pair.getRight()};
    sort(actual);
    Assertions.assertArrayEquals(expected, actual);

    // Generic method
    pair = ClosestPairCalculator.closestPairAllVsAll(list, Point2D::getX, Point2D::getY);
    actual = new Point2D[] {pair.getLeft(), pair.getRight()};
    sort(actual);
    Assertions.assertArrayEquals(expected, actual);
  }

  private static Point2D[] findClosest(List<? extends Point2D> list) {
    double min = Double.POSITIVE_INFINITY;
    final Point2D[] pair = new Point2D[2];
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        final double distance = list.get(i).distanceSq(list.get(j));
        if (distance < min) {
          min = distance;
          pair[0] = list.get(i);
          pair[1] = list.get(j);
        }
      }
    }
    sort(pair);
    return pair;
  }

  @SeededTest
  public void testClosestPairBelowAlgorithmSwicthPoint(RandomSeed seed) {
    assertClosestPair(3, seed);
  }

  @SeededTest
  public void testClosestPairAboveAlgorithmSwicthPoint(RandomSeed seed) {
    assertClosestPair(ClosestPairCalculator.ALGORITHM_SWITCH, seed);
  }

  private static void assertClosestPair(int size, RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final List<Point2D> list = IntStream.range(0, size)
        .mapToObj(i -> (Point2D) new Point2D.Double(rng.nextDouble(), rng.nextDouble()))
        .collect(Collectors.toList());

    // Compare to each other

    // Point2D[] method
    final Pair<Point2D, Point2D> pair1 =
        ClosestPairCalculator.closestPair(list.toArray(new Point2D[0]));
    final Point2D[] expected = new Point2D[] {pair1.getLeft(), pair1.getRight()};

    // Generic method
    final Pair<Point2D, Point2D> pair2 =
        ClosestPairCalculator.closestPair(list, Point2D::getX, Point2D::getY);
    final Point2D[] actual = new Point2D[] {pair2.getLeft(), pair2.getRight()};

    Assertions.assertArrayEquals(expected, actual);
  }

  private static void sort(Point2D[] points) {
    Arrays.sort(points, ClosestPairCalculatorTest::compare);
  }

  private static int compare(Point2D o1, Point2D o2) {
    // Order by x then y
    int result = Double.compare(o1.getX(), o2.getX());
    if (result == 0) {
      result = Double.compare(o1.getY(), o2.getY());
    }
    return result;
  }
}
