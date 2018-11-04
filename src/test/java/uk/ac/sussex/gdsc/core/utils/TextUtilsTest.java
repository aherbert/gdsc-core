package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class TextUtilsTest {

  @Test
  public void canConvertMillisToString() {
    // TOD0 - fix this test
    Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtils.timeToString(-1));
    // Milliseconds reported exactly
    Assertions.assertEquals("0ms", TextUtils.timeToString(0));
    Assertions.assertEquals("999ms", TextUtils.timeToString(999));
    // TODO - Seconds to 4 s.f.
    Assertions.assertEquals("1.000s", TextUtils.timeToString(1000));
    Assertions.assertEquals("1.001s", TextUtils.timeToString(1001));
    Assertions.assertEquals("1.010s", TextUtils.timeToString(1010));
    Assertions.assertEquals("1.100s", TextUtils.timeToString(1100));
    Assertions.assertEquals("9.999s", TextUtils.timeToString(9999));
    Assertions.assertEquals("10.000s", TextUtils.timeToString(10000));
    Assertions.assertEquals("10.001s", TextUtils.timeToString(10001));
    Assertions.assertEquals("10.010s", TextUtils.timeToString(10010));
    Assertions.assertEquals("10.100s", TextUtils.timeToString(10100));
    Assertions.assertEquals("59.999s", TextUtils.timeToString(59999));
    // TODO Minutes. Seconds should be 1 decimal place

    Assertions.assertEquals("1m00.000s", TextUtils.timeToString(60000));
    Assertions.assertEquals("1m00.001s", TextUtils.timeToString(60001));
    Assertions.assertEquals("1m00.010s", TextUtils.timeToString(60010));
    Assertions.assertEquals("1m00.100s", TextUtils.timeToString(60100));
    Assertions.assertEquals("1m01.000s", TextUtils.timeToString(61000));
    Assertions.assertEquals("59m59.999s", TextUtils.timeToString(3599999));
    // TODO hours. Seconds should be 1 decimal place
    Assertions.assertEquals("1h00m00.000s", TextUtils.timeToString(3600000));
    Assertions.assertEquals("1h00m00.001s", TextUtils.timeToString(3600001));
    Assertions.assertEquals("1h00m00.010s", TextUtils.timeToString(3600010));
    Assertions.assertEquals("1h00m00.100s", TextUtils.timeToString(3600100));
    Assertions.assertEquals("1h00m01.000s", TextUtils.timeToString(3601000));
    Assertions.assertEquals("1h00m10.000s", TextUtils.timeToString(3610000));
    Assertions.assertEquals("1h01m00.000s", TextUtils.timeToString(3660000));
    Assertions.assertEquals("1h59m59.999s", TextUtils.timeToString(7199999));
    Assertions.assertEquals("2h00m00.000s", TextUtils.timeToString(7200000));
  }

  @Test
  public void canConvertNanosToString() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtils.nanosToString(-1));
    Assertions.assertEquals("0ns", TextUtils.nanosToString(0));
    Assertions.assertEquals("999ns", TextUtils.nanosToString(999));
    // Microseconds to 4 s.f.
    Assertions.assertEquals("1µs", TextUtils.nanosToString(1000));
    Assertions.assertEquals("1.001µs", TextUtils.nanosToString(1001));
    Assertions.assertEquals("999.9µs", TextUtils.nanosToString(999900));
    // Milliseconds to 4 s.f.
    Assertions.assertEquals("1.000ms", TextUtils.nanosToString(999950));
    Assertions.assertEquals("1ms", TextUtils.nanosToString(1000000));
    Assertions.assertEquals("1.001ms", TextUtils.nanosToString(1001000));
    Assertions.assertEquals("999ms", TextUtils.nanosToString(999000000));
    Assertions.assertEquals("999.9ms", TextUtils.nanosToString(999900000));

    // TODO - Seconds to 4 s.f.

    Assertions.assertEquals("1.000s", TextUtils.nanosToString(999950000));
    Assertions.assertEquals("1.000s", TextUtils.nanosToString(1000000000));
    Assertions.assertEquals("1.001s", TextUtils.nanosToString(1001000000));
    Assertions.assertEquals("1.010s", TextUtils.nanosToString(1010000000));
    Assertions.assertEquals("1.100s", TextUtils.nanosToString(1100000000));
    Assertions.assertEquals("9.999s", TextUtils.nanosToString(9999000000L));
    Assertions.assertEquals("10.000s", TextUtils.nanosToString(10000000000L));
    Assertions.assertEquals("10.001s", TextUtils.nanosToString(10001000000L));
    Assertions.assertEquals("10.010s", TextUtils.nanosToString(10010000000L));
    Assertions.assertEquals("10.100s", TextUtils.nanosToString(10100000000L));
    Assertions.assertEquals("59.999s", TextUtils.nanosToString(59999000000L));
    // TODO Minutes. Seconds should be 1 decimal place
    Assertions.assertEquals("1m00.000s", TextUtils.nanosToString(60000000000L));
    Assertions.assertEquals("1m00.001s", TextUtils.nanosToString(60001000000L));
    Assertions.assertEquals("1m00.010s", TextUtils.nanosToString(60010000000L));
    Assertions.assertEquals("1m00.100s", TextUtils.nanosToString(60100000000L));
    Assertions.assertEquals("1m01.000s", TextUtils.nanosToString(61000000000L));
    Assertions.assertEquals("59m59.999s", TextUtils.nanosToString(3599999000000L));
    // TODO Hours. Seconds should be 1 decimal place
    Assertions.assertEquals("1h00m00.000s", TextUtils.nanosToString(3600000000000L));
    Assertions.assertEquals("1h00m00.001s", TextUtils.nanosToString(3600001000000L));
    Assertions.assertEquals("1h00m00.010s", TextUtils.nanosToString(3600010000000L));
    Assertions.assertEquals("1h00m00.100s", TextUtils.nanosToString(3600100000000L));
    Assertions.assertEquals("1h00m01.000s", TextUtils.nanosToString(3601000000000L));
    Assertions.assertEquals("1h00m10.000s", TextUtils.nanosToString(3610000000000L));
    Assertions.assertEquals("1h01m00.000s", TextUtils.nanosToString(3660000000000L));
    Assertions.assertEquals("1h59m59.999s", TextUtils.nanosToString(7199999000000L));
    Assertions.assertEquals("2h00m00.000s", TextUtils.nanosToString(7200000000000L));
  }
}
