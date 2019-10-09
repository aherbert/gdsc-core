package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class RegressionUtilsTest {
  // Use the abbreviated names
  // CHECKSTYLE.OFF: MemberName
  private final double rss1 = 10;
  private final int p1 = 1;
  private final double rss2 = 9;
  private final int p2 = 2;
  private final int n = 20;

  @Test
  public void testResidualsFStatisticThrows() {
    Assertions.assertDoesNotThrow(() -> RegressionUtils.residualsFStatistic(rss1, p1, rss1, p2, n),
        "rss1 == rss2");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RegressionUtils.residualsFStatistic(rss2, p1, rss1, p2, n), "rss1 > rss2");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RegressionUtils.residualsFStatistic(rss1, p1, rss2, p1, n), "p1 == p2");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RegressionUtils.residualsFStatistic(rss1, p1, rss2, p2, p2), "p2 == n");
  }

  @Test
  public void testResidualsFTestWithAlphaThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RegressionUtils.residualsFTest(rss1, p1, rss1, p2, n, 0), "alpha 0");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RegressionUtils.residualsFTest(rss1, p1, rss2, p1, n, 0.51), "alpha > 0.5");
  }

  @Test
  public void canComputeResidualsFStatisticWithZeroResiduals() {
    final double f1 = RegressionUtils.residualsFStatistic(0, p1, 0, p2, n);
    // This is the result if the divide was done without checks: NaN
    Assertions.assertEquals(Double.NaN, 0.0 / 0.0);
    // The F-statistic should be 0 as model 2 is not better
    Assertions.assertEquals(0.0, f1, "F: zero / zero");

    final double pvalue1 = RegressionUtils.residualsFTest(0, p1, 0, p2, n);
    // p-value should be one as the null hypothesis cannot be rejected, model 2 is not better
    Assertions.assertEquals(1.0, pvalue1, "P-value: zero / zero");

    final double f2 = RegressionUtils.residualsFStatistic(rss1, p1, 0, p2, n);
    // This is the expected result if divide was done without checks: infinity
    Assertions.assertEquals(Double.POSITIVE_INFINITY, 10.0 / 0.0);
    // The F-statistic should be infinity as model 2 is perfect
    Assertions.assertEquals(Double.POSITIVE_INFINITY, f2, "F: positive / zero");

    final double pvalue2 = RegressionUtils.residualsFTest(rss1, p1, 0, p2, n);
    // p-value should be zero as the null hypothesis is always rejected, model 2 is perfect
    Assertions.assertEquals(0.0, pvalue2, "P-value: positive / zero");
  }

  @Test
  public void canComputeResidualsFStatistic() {
    final double f1 = RegressionUtils.residualsFStatistic(rss1, p1, rss2, p2, n);
    final double f2 = RegressionUtils.residualsFStatistic(rss1, p1, rss2 / 2, p2, n);
    Assertions.assertTrue(f2 > f1, "Better model should be higher F");
  }

  @Test
  public void canComputeResidualsFTest() {
    final double pvalue1 = RegressionUtils.residualsFTest(rss1, p1, rss2, p2, n);
    final double pvalue2 = RegressionUtils.residualsFTest(rss1, p1, rss2 / 2, p2, n);
    Assertions.assertTrue(pvalue2 < pvalue1, "Better model should be lower p-value");
  }

  @Test
  public void canComputeResidualsFTestWithAlpha() {
    final double pvalue = RegressionUtils.residualsFTest(rss1, p1, rss2, p2, n);
    Assertions.assertFalse(RegressionUtils.residualsFTest(rss1, p1, rss2, p2, n, pvalue * 0.99),
        "p-value above alpha should be accepted");
    Assertions.assertTrue(RegressionUtils.residualsFTest(rss1, p1, rss2, p2, n, pvalue * 1.01),
        "p-value below alpha should be rejected");
  }
}
