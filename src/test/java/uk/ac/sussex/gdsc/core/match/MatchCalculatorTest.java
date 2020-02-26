package uk.ac.sussex.gdsc.core.match;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.ToDoubleBiFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * Test for {@link MatchCalculator}.
 */
@SuppressWarnings({"javadoc"})
public class MatchCalculatorTest {

  interface Calculator {
    MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold, List<Coordinate> truePositives, List<Coordinate> falsePositives,
        List<Coordinate> falseNegatives, List<PointPair> matches);

    MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold, List<Coordinate> truePositives, List<Coordinate> falsePositives,
        List<Coordinate> falseNegatives);

    MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold);
  }

  static class Calculator2D implements Calculator {
    static final Calculator2D INSTANCE = new Calculator2D();

    @Override
    public MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold, List<Coordinate> truePositives, List<Coordinate> falsePositives,
        List<Coordinate> falseNegatives, List<PointPair> matches) {
      return MatchCalculator.analyseResults2D(actualPoints, predictedPoints, distanceThreshold,
          truePositives, falsePositives, falseNegatives, matches);
    }

    @Override
    public MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold, List<Coordinate> truePositives, List<Coordinate> falsePositives,
        List<Coordinate> falseNegatives) {
      return MatchCalculator.analyseResults2D(actualPoints, predictedPoints, distanceThreshold,
          truePositives, falsePositives, falseNegatives);
    }

    @Override
    public MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold) {
      return MatchCalculator.analyseResults2D(actualPoints, predictedPoints, distanceThreshold);
    }
  }

  static class Calculator3D implements Calculator {
    static final Calculator3D INSTANCE = new Calculator3D();

    @Override
    public MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold, List<Coordinate> truePositives, List<Coordinate> falsePositives,
        List<Coordinate> falseNegatives, List<PointPair> matches) {
      return MatchCalculator.analyseResults3D(actualPoints, predictedPoints, distanceThreshold,
          truePositives, falsePositives, falseNegatives, matches);
    }

    @Override
    public MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold, List<Coordinate> truePositives, List<Coordinate> falsePositives,
        List<Coordinate> falseNegatives) {
      return MatchCalculator.analyseResults3D(actualPoints, predictedPoints, distanceThreshold,
          truePositives, falsePositives, falseNegatives);
    }

    @Override
    public MatchResult analyseResults(Coordinate[] actualPoints, Coordinate[] predictedPoints,
        double distanceThreshold) {
      return MatchCalculator.analyseResults3D(actualPoints, predictedPoints, distanceThreshold);
    }
  }

  @Test
  public void testAnalyseResults2DWithNullPoints() {
    assertAnalyseResults2DWithNullPoints(Calculator2D.INSTANCE);
  }

  @Test
  public void testAnalyseResults2DWithNoPoints() {
    assertAnalyseResults2DWithNoPoints(Calculator2D.INSTANCE);
  }

  @Test
  public void testAnalyseResults2DWithNoMatches() {
    assertAnalyseResults2DWithNoMatches(Calculator2D.INSTANCE);
  }

  @Test
  public void testAnalyseResults2D() {
    assertAnalyseResults2D(Calculator2D.INSTANCE);
  }

  // Exercise the 3D methods with 2D data

  @Test
  public void testAnalyseResults3DWithNullPoints() {
    assertAnalyseResults2DWithNullPoints(Calculator3D.INSTANCE);
  }

  @Test
  public void testAnalyseResults3DWithNoPoints() {
    assertAnalyseResults2DWithNoPoints(Calculator3D.INSTANCE);
  }

  @Test
  public void testAnalyseResults3DWithNoMatches() {
    assertAnalyseResults2DWithNoMatches(Calculator3D.INSTANCE);
  }

  @Test
  public void testAnalyseResults3D() {
    assertAnalyseResults2D(Calculator3D.INSTANCE);
  }

  private static void assertAnalyseResults2DWithNullPoints(Calculator calc) {
    final Coordinate[] actual = null;
    final Coordinate[] predicted = createCoordinates2D(0, 0);
    final double distanceThreshold = 1;
    final List<Coordinate> truePositives = new ArrayList<>();
    final List<Coordinate> falsePositives = new ArrayList<>();
    final List<Coordinate> falseNegatives = new ArrayList<>();
    final List<PointPair> matches = new ArrayList<>();
    MatchResult match = calc.analyseResults(actual, predicted, distanceThreshold, truePositives,
        falsePositives, falseNegatives, matches);
    assertMatch(actual, predicted, 0, 1, 0, 0.0, match, truePositives, falsePositives,
        falseNegatives, matches);
    // Test methods with no lists
    match = calc.analyseResults(actual, predicted, distanceThreshold, truePositives, falsePositives,
        falseNegatives);
    assertMatch(actual, predicted, 0, 1, 0, 0.0, match, truePositives, falsePositives,
        falseNegatives, null);
    match = calc.analyseResults(actual, predicted, distanceThreshold);
    assertMatch(actual, predicted, 0, 1, 0, 0.0, match, null, null, null, null);

    // Reversed
    match = calc.analyseResults(predicted, actual, distanceThreshold, truePositives, falsePositives,
        falseNegatives, matches);
    assertMatch(predicted, actual, 0, 0, 1, 0.0, match, truePositives, falsePositives,
        falseNegatives, matches);
    // Test methods with no lists
    match = calc.analyseResults(predicted, actual, distanceThreshold, truePositives, falsePositives,
        falseNegatives);
    assertMatch(predicted, actual, 0, 0, 1, 0.0, match, truePositives, falsePositives,
        falseNegatives, null);
    match = calc.analyseResults(predicted, actual, distanceThreshold);
    assertMatch(predicted, actual, 0, 0, 1, 0.0, match, null, null, null, null);
  }

  private static void assertAnalyseResults2DWithNoPoints(Calculator calc) {
    final Coordinate[] actual = createCoordinates2D();
    final Coordinate[] predicted = createCoordinates2D(0, 0);
    final double distanceThreshold = 1;
    final List<Coordinate> truePositives = new ArrayList<>();
    final List<Coordinate> falsePositives = new ArrayList<>();
    final List<Coordinate> falseNegatives = new ArrayList<>();
    final List<PointPair> matches = new ArrayList<>();
    MatchResult match = calc.analyseResults(actual, predicted, distanceThreshold, truePositives,
        falsePositives, falseNegatives, matches);
    assertMatch(actual, predicted, 0, 1, 0, 0.0, match, truePositives, falsePositives,
        falseNegatives, matches);

    // Reversed
    match = calc.analyseResults(predicted, actual, distanceThreshold, truePositives, falsePositives,
        falseNegatives, matches);
    assertMatch(predicted, actual, 0, 0, 1, 0.0, match, truePositives, falsePositives,
        falseNegatives, matches);

    // Empty
    match = calc.analyseResults(actual, actual, distanceThreshold, truePositives, falsePositives,
        falseNegatives, matches);
    assertMatch(actual, predicted, 0, 0, 0, 0.0, match, truePositives, falsePositives,
        falseNegatives, matches);

    // Test methods with no lists
    match = calc.analyseResults(actual, predicted, distanceThreshold, truePositives, falsePositives,
        falseNegatives);
    assertMatch(actual, predicted, 0, 1, 0, 0.0, match, truePositives, falsePositives,
        falseNegatives, null);
    match = calc.analyseResults(actual, predicted, distanceThreshold);
    assertMatch(actual, predicted, 0, 1, 0, 0.0, match);
  }

  private static void assertAnalyseResults2DWithNoMatches(Calculator calc) {
    final Coordinate[] actual = createCoordinates2D(0, 0);
    final Coordinate[] predicted = createCoordinates2D(1, 1, 5, 5);
    final double distanceThreshold = 0.5;
    final List<Coordinate> truePositives = new ArrayList<>();
    final List<Coordinate> falsePositives = new ArrayList<>();
    final List<Coordinate> falseNegatives = new ArrayList<>();
    final List<PointPair> matches = new ArrayList<>();
    final MatchResult match = calc.analyseResults(actual, predicted, distanceThreshold,
        truePositives, falsePositives, falseNegatives, matches);
    assertMatch(actual, predicted, 0, 2, 1, 0.0, match, truePositives, falsePositives,
        falseNegatives, matches);
  }

  private static void assertAnalyseResults2D(Calculator calc) {
    // These have been constructed so that when the assignments are sorted by distance
    // all 4 possible branches are hit for 'assigned':
    // actual... N Y N Y
    // predicted N N Y Y
    // matches:
    // 0,0 -> 0,0 distance^2 = 0
    // 1,1 -> 0.4,0.4 distance^2 = 0.6*0.6*2
    final Coordinate[] actual = createCoordinates2D(0, 0, 1, 1, 1.1f, 1.1f, 60, 60);
    final Coordinate[] predicted = createCoordinates2D(0, 0, 0.4f, 0.4f, 5, 5);
    final double distanceThreshold = 1;
    // Compute RMSD as float up-cast to double
    final double v1 = 0.4f;
    final double rmsd = Math.sqrt(MathUtils.pow2(v1 - 1.0));
    final List<Coordinate> truePositives = new ArrayList<>();
    final List<Coordinate> falsePositives = new ArrayList<>();
    final List<Coordinate> falseNegatives = new ArrayList<>();
    final List<PointPair> matches = new ArrayList<>();
    MatchResult match = calc.analyseResults(actual, predicted, distanceThreshold, truePositives,
        falsePositives, falseNegatives, matches);
    assertMatch(actual, predicted, 2, 1, 2, rmsd, match, truePositives, falsePositives,
        falseNegatives, matches);

    // Test methods with no lists
    match = calc.analyseResults(actual, predicted, distanceThreshold);
    assertMatch(actual, predicted, 2, 1, 2, rmsd, match);
  }

  private static Coordinate[] createCoordinates2D(float... data) {
    final ArrayList<Coordinate> list = new ArrayList<>();
    for (int i = 0; i < data.length; i += 2) {
      list.add(new BasePoint(data[i], data[i + 1]));
    }
    return list.toArray(new Coordinate[0]);
  }

  /**
   * Assert the match.
   *
   * @param actual the actual coordinates
   * @param predicted the predicted coordinates
   * @param tp the true positives (number that match)
   * @param fp the false positives (number of predicted that did not match)
   * @param fn the false negatives (number of actual that did not match)
   * @param rmsd the rmsd
   * @param match the match
   */
  private static void assertMatch(Coordinate[] actual, Coordinate[] predicted, int tp, int fp,
      int fn, double rmsd, MatchResult match) {
    assertMatch(actual, predicted, tp, fp, fn, rmsd, match, null, null, null, null);
  }

  /**
   * Assert the match.
   *
   * @param actual the actual coordinates
   * @param predicted the predicted coordinates
   * @param tp the true positives (number that match)
   * @param fp the false positives (number of predicted that did not match)
   * @param fn the false negatives (number of actual that did not match)
   * @param rmsd the rmsd
   * @param match the match
   * @param truePositives the list of true positives
   * @param falsePositives the list of false positives
   * @param falseNegatives the list of false negatives
   * @param matches the matches
   */
  private static void assertMatch(Coordinate[] actual, Coordinate[] predicted, int tp, int fp,
      int fn, double rmsd, MatchResult match, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(rmsd, match.getRmsd(), "rmsd");
    final HashSet<Coordinate> allPredicted = new HashSet<>();
    if (truePositives != null) {
      Assertions.assertEquals(tp, truePositives.size(), "list tp");
      truePositives.forEach(c -> Assertions.assertTrue(ArrayUtils.indexOf(predicted, c) != -1,
          "tp list should contain predicted"));
      allPredicted.addAll(truePositives);
      Assertions.assertEquals(tp, allPredicted.size(), "duplicates in the true positives");
    }
    if (falsePositives != null) {
      Assertions.assertEquals(fp, falsePositives.size(), "list fp");
      falsePositives.forEach(c -> Assertions.assertTrue(ArrayUtils.indexOf(predicted, c) != -1,
          "fp list should contain predicted"));
      allPredicted.addAll(falsePositives);
      final int expected = truePositives == null ? fp : fp + tp;
      Assertions.assertEquals(expected, allPredicted.size(), "duplicates in the false positives");
    }
    final HashSet<Coordinate> allActual = new HashSet<>();
    if (falseNegatives != null) {
      Assertions.assertEquals(fn, falseNegatives.size(), "list fn");
      falseNegatives.forEach(c -> Assertions.assertTrue(ArrayUtils.indexOf(actual, c) != -1,
          "fn list should contain actual"));
      allActual.addAll(falseNegatives);
      Assertions.assertEquals(fn, allActual.size(), "duplicates in the false negatives");
    }
    if (matches != null) {
      Assertions.assertEquals(tp, matches.size(), "list matches");
      final HashSet<Coordinate> matchPredicted = new HashSet<>();
      // Test the matches contains the predicted as the second of the pair.
      for (PointPair pair : matches) {
        Assertions.assertTrue(ArrayUtils.indexOf(actual, pair.getPoint1()) != -1,
            "actual match should be first of the match pair");
        Assertions.assertTrue(ArrayUtils.indexOf(predicted, pair.getPoint2()) != -1,
            "predicted match should be second of the match pair");
        if (truePositives != null) {
          Assertions.assertTrue(truePositives.contains(pair.getPoint2()),
              "predicted match should be in the true positives list");
        }
        if (falseNegatives != null) {
          Assertions.assertFalse(falseNegatives.contains(pair.getPoint1()),
              "actual match should not be in the false negatives list");
        }
        allActual.add(pair.getPoint1());
        matchPredicted.add(pair.getPoint2());
      }
      final int expected = falseNegatives == null ? tp : tp + fn;
      Assertions.assertEquals(expected, allActual.size(), "duplicates in actual matches");
      Assertions.assertEquals(tp, matchPredicted.size(), "duplicates in predicted matches");
    }
  }

  @Test
  public void testCreateEdgeFunction() {
    final double distanceThreshold = 2;
    final ToDoubleBiFunction<Pulse, Pulse> edges =
        MatchCalculator.createEdgeFunction(distanceThreshold);

    // Reference
    final Pulse p1 = new Pulse(0, 0, 0, 0);
    Assertions.assertEquals(-1, edges.applyAsDouble(p1, p1));

    // Twice as long
    final Pulse p2 = new Pulse(0, 0, 0, 1);
    Assertions.assertEquals(-1, edges.applyAsDouble(p1, p2));
    Assertions.assertEquals(-2, edges.applyAsDouble(p2, p2));

    // The score is positive.
    final double expectedInvalidScore = 1.0;

    // Outside distance.
    final Pulse p3 = new Pulse(10, 10, 0, 0);
    Assertions.assertEquals(expectedInvalidScore, edges.applyAsDouble(p1, p3));

    // No overlap
    final Pulse p4 = new Pulse(0, 0, 10, 10);
    Assertions.assertEquals(expectedInvalidScore, edges.applyAsDouble(p1, p4));
  }

  @Test
  public void testAnalyseResults2DWithPulses() {
    // This uses the same algorithm.
    // Just hit the case where the matches are known and check the score is computed correctly.
    // @formatter:off
    final Pulse[] actual = new Pulse[] {
        new Pulse(0, 0, 0, 0),
        new Pulse(1, 1, 0, 0),
        new Pulse(1.1f, 1.1f, 0, 0),
        new Pulse(60, 60, 0, 0),
    };
    final Pulse[] predicted = new Pulse[] {
        new Pulse(0, 0, 0, 0),
        new Pulse(0.4f, 0.4f, 0, 0),
        new Pulse(5, 5, 0, 0),
    };
    // @formatter:on
    final double distanceThreshold = 1;
    final List<Pulse> truePositives = new ArrayList<>();
    final List<Pulse> falsePositives = new ArrayList<>();
    final List<Pulse> falseNegatives = new ArrayList<>();
    final List<PointPair> matches = new ArrayList<>();
    final MatchResult match = MatchCalculator.analyseResults2D(actual, predicted, distanceThreshold,
        truePositives, falsePositives, falseNegatives, matches);
    final int tp = 2;
    final int fp = 1;
    final int fn = 2;
    final double dt = MathUtils.pow2(distanceThreshold / 2);
    final int totalTimePoints = 4;
    final double score =
        (actual[0].score(predicted[0], dt) + actual[1].score(predicted[1], dt)) / totalTimePoints;
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(score, match.getRmsd(), 1e-6, "score");
    // Checks the lists receive data
    Assertions.assertEquals(tp, truePositives.size(), "list tp");
    Assertions.assertEquals(fp, falsePositives.size(), "list fp");
    Assertions.assertEquals(fn, falseNegatives.size(), "list fn");
    Assertions.assertEquals(tp, matches.size(), "list matches");

  }
}
