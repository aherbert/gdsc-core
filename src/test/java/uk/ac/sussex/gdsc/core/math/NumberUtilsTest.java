package uk.ac.sussex.gdsc.core.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link NumberUtils}.
 */
@SuppressWarnings({"javadoc"})
public class NumberUtilsTest {
  @Test
  public void testIsPrime() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.isPrime(-1));
    Assertions.assertFalse(NumberUtils.isPrime(1), "1 should not be prime");
    Assertions.assertTrue(NumberUtils.isPrime(2), "2 should be prime");
    // Small primes
    for (int prime : new int[] {3, 5, 7, 11, 13, 17}) {
      Assertions.assertTrue(NumberUtils.isPrime(prime), () -> prime + " is prime");
      Assertions.assertFalse(NumberUtils.isPrime(prime + 1), () -> (prime + 1) + " is not prime");
    }
    // Bigger primes. Must not be prime 2 above the value
    for (int prime : new int[] {883, 1777, 3313}) {
      Assertions.assertTrue(NumberUtils.isPrime(prime), () -> prime + " is prime");
      Assertions.assertFalse(NumberUtils.isPrime(prime + 2), () -> (prime + 2) + " is not prime");
    }
  }

  @Test
  public void testUlps() {
    Assertions.assertEquals(0, NumberUtils.ulps(4.56, 4.56));
    Assertions.assertEquals(1, NumberUtils.ulps(4.56, Math.nextUp(4.56)));
    Assertions.assertEquals(0, NumberUtils.ulps(0.0, 0.0));
    Assertions.assertEquals(1, NumberUtils.ulps(-0.0, 0.0));
    Assertions.assertEquals(1, NumberUtils.ulps(0.0, -0.0));
    Assertions.assertEquals(2, NumberUtils.ulps(0.0, -Double.MIN_VALUE));
    Assertions.assertEquals(3, NumberUtils.ulps(Double.MIN_VALUE, -Double.MIN_VALUE));
    Assertions.assertEquals(Long.MAX_VALUE, NumberUtils.ulps(Double.MAX_VALUE, -Double.MAX_VALUE));
    Assertions.assertEquals(Long.MAX_VALUE, NumberUtils.ulps(Double.NaN, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(Long.MAX_VALUE,
        NumberUtils.ulps(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(0,
        NumberUtils.ulps(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(0, NumberUtils.ulps(Double.NaN, Double.NaN));
  }
}
