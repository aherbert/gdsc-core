package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

@SuppressWarnings({"javadoc"})
public class MathsUtilsTest {
  @SeededTest
  public void canRoundToDecimalPlaces(RandomSeed seed) {
    // 0.1 cannot be an exact double (see constructor documentation for BigDecimal)
    double value = 0.1;
    BigDecimal bd = new BigDecimal(value);
    Assertions.assertNotEquals("0.1", bd.toPlainString());
    Assertions.assertEquals("0.1",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(value, 1).toPlainString());

    // Random test that rounding does the same as String.format
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    for (int i = 0; i < 10; i++) {
      final String format = "%." + i + "f";
      for (int j = 0; j < 10; j++) {
        value = r.nextDouble();
        final String e = String.format(format, value);
        bd = MathUtils.roundUsingDecimalPlacesToBigDecimal(value, i);
        Assertions.assertEquals(e, bd.toPlainString());
      }
    }
  }

  @Test
  public void canRoundToNegativeDecimalPlaces() {
    Assertions.assertEquals("123.0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, 2).toPlainString());
    Assertions.assertEquals("123.0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, 1).toPlainString());
    Assertions.assertEquals("123",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, 0).toPlainString());
    Assertions.assertEquals("120",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -1).toPlainString());
    Assertions.assertEquals("100",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -2).toPlainString());
    Assertions.assertEquals("0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -3).toPlainString());
    Assertions.assertEquals("0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -4).toPlainString());
    Assertions.assertEquals("1000",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(523, -3).toPlainString());
  }

  @Test
  public void canComputeAverageIndex() {
    canComputeAverageIndex(0, 0);
    canComputeAverageIndex(0, 1);
    canComputeAverageIndex(0, 2);
    canComputeAverageIndex(1, 2);
    canComputeAverageIndex(1, 3);
    canComputeAverageIndex(Integer.MAX_VALUE, 1);
    canComputeAverageIndex(Integer.MAX_VALUE - 10, Integer.MAX_VALUE);
    canComputeAverageIndex(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  private static void canComputeAverageIndex(int index1, int index2) {
    final long expected = ((long) index1 + (long) index2) / 2L;
    Assertions.assertEquals(expected, MathUtils.averageIndex(index1, index2),
        () -> "Index1=" + index1 + ", Index2=" + index2);
  }

  @Test
  public void canComputeLog2() {
    Assertions.assertEquals(Integer.MIN_VALUE, MathUtils.log2(0));
    Assertions.assertEquals(0, MathUtils.log2(1));
    Assertions.assertEquals(1, MathUtils.log2(2));
    Assertions.assertEquals(2, MathUtils.log2(4));
    Assertions.assertEquals(8, MathUtils.log2(256));
  }
}
