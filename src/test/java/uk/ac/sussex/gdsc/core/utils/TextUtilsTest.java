package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class TextUtilsTest {

  @Test
  public void canConvertMillisToString() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtils.timeToString(-1));
    // Milliseconds reported exactly
    Assertions.assertEquals("0ms", TextUtils.timeToString(0));
    Assertions.assertEquals("999ms", TextUtils.timeToString(999));
    // Seconds to 3 s.f.
    Assertions.assertEquals("1s", TextUtils.timeToString(1000));
    Assertions.assertEquals("1.00s", TextUtils.timeToString(1001));
    Assertions.assertEquals("1.01s", TextUtils.timeToString(1005));
    Assertions.assertEquals("1.1s", TextUtils.timeToString(1100));
    Assertions.assertEquals("9.99s", TextUtils.timeToString(9990));
    Assertions.assertEquals("10.0s", TextUtils.timeToString(9995));
    Assertions.assertEquals("10s", TextUtils.timeToString(10000));
    Assertions.assertEquals("10.0s", TextUtils.timeToString(10010));
    Assertions.assertEquals("10.1s", TextUtils.timeToString(10050));
    Assertions.assertEquals("10.1s", TextUtils.timeToString(10100));
    Assertions.assertEquals("59.9s", TextUtils.timeToString(59900));
    // Minutes. Seconds should be 1 decimal place
    Assertions.assertEquals("1m00.0s", TextUtils.timeToString(59950));
    Assertions.assertEquals("1m00s", TextUtils.timeToString(60000));
    Assertions.assertEquals("1m00.0s", TextUtils.timeToString(60001));
    Assertions.assertEquals("1m00.0s", TextUtils.timeToString(60010));
    Assertions.assertEquals("1m00.1s", TextUtils.timeToString(60050));
    Assertions.assertEquals("1m00.1s", TextUtils.timeToString(60100));
    Assertions.assertEquals("1m01s", TextUtils.timeToString(61000));
    Assertions.assertEquals("59m59.9s", TextUtils.timeToString(3599900));
    // Hours. Seconds should be 1 decimal place
    Assertions.assertEquals("1h00m00.0s", TextUtils.timeToString(3599950));
    Assertions.assertEquals("1h00m00s", TextUtils.timeToString(3600000));
    Assertions.assertEquals("1h00m00.0s", TextUtils.timeToString(3600001));
    Assertions.assertEquals("1h00m00.0s", TextUtils.timeToString(3600010));
    Assertions.assertEquals("1h00m00.1s", TextUtils.timeToString(3600050));
    Assertions.assertEquals("1h00m00.1s", TextUtils.timeToString(3600100));
    Assertions.assertEquals("1h00m01s", TextUtils.timeToString(3601000));
    Assertions.assertEquals("1h00m10s", TextUtils.timeToString(3610000));
    Assertions.assertEquals("1h01m00s", TextUtils.timeToString(3660000));
    Assertions.assertEquals("1h59m59.9s", TextUtils.timeToString(7199900));
    Assertions.assertEquals("2h00m00.0s", TextUtils.timeToString(7199950));
    Assertions.assertEquals("2h00m00s", TextUtils.timeToString(7200000));
  }

  @Test
  public void canConvertNanosToString() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtils.nanosToString(-1));
    Assertions.assertEquals("0ns", TextUtils.nanosToString(0));
    Assertions.assertEquals("999ns", TextUtils.nanosToString(999));
    // Microseconds to 3 s.f.
    Assertions.assertEquals("1µs", TextUtils.nanosToString(1000));
    Assertions.assertEquals("1.00µs", TextUtils.nanosToString(1001));
    Assertions.assertEquals("1.01µs", TextUtils.nanosToString(1005));
    Assertions.assertEquals("999µs", TextUtils.nanosToString(999000));
    // Milliseconds to 3 s.f.
    Assertions.assertEquals("1.00ms", TextUtils.nanosToString(999500));
    Assertions.assertEquals("1ms", TextUtils.nanosToString(1000000));
    Assertions.assertEquals("1.00ms", TextUtils.nanosToString(1001000));
    Assertions.assertEquals("1.01ms", TextUtils.nanosToString(1005000));
    Assertions.assertEquals("999ms", TextUtils.nanosToString(999000000));
    // Seconds to 3 s.f.
    Assertions.assertEquals("1.00s", TextUtils.nanosToString(999500000));
    Assertions.assertEquals("1s", TextUtils.nanosToString(1000000000));
    Assertions.assertEquals("1.00s", TextUtils.nanosToString(1001000000));
    Assertions.assertEquals("1.01s", TextUtils.nanosToString(1005000000));
    Assertions.assertEquals("1.1s", TextUtils.nanosToString(1100000000));
    Assertions.assertEquals("9.99s", TextUtils.nanosToString(9990000000L));
    Assertions.assertEquals("10.0s", TextUtils.nanosToString(9995000000L));
    Assertions.assertEquals("10s", TextUtils.nanosToString(10000000000L));
    Assertions.assertEquals("10.0s", TextUtils.nanosToString(10010000000L));
    Assertions.assertEquals("10.1s", TextUtils.nanosToString(10050000000L));
    Assertions.assertEquals("10.1s", TextUtils.nanosToString(10100000000L));
    Assertions.assertEquals("59.9s", TextUtils.nanosToString(59900000000L));
    // Minutes. Seconds should be 1 decimal place
    Assertions.assertEquals("1m00.0s", TextUtils.nanosToString(59950000000L));
    Assertions.assertEquals("1m00s", TextUtils.nanosToString(60000000000L));
    Assertions.assertEquals("1m00.0s", TextUtils.nanosToString(60001000000L));
    Assertions.assertEquals("1m00.0s", TextUtils.nanosToString(60010000000L));
    Assertions.assertEquals("1m00.1s", TextUtils.nanosToString(60050000000L));
    Assertions.assertEquals("1m00.1s", TextUtils.nanosToString(60100000000L));
    Assertions.assertEquals("1m01s", TextUtils.nanosToString(61000000000L));
    Assertions.assertEquals("59m59.9s", TextUtils.nanosToString(3599900000000L));
    // Hours. Seconds should be 1 decimal place
    Assertions.assertEquals("1h00m00.0s", TextUtils.nanosToString(3599950000000L));
    Assertions.assertEquals("1h00m00s", TextUtils.nanosToString(3600000000000L));
    Assertions.assertEquals("1h00m00.0s", TextUtils.nanosToString(3600001000000L));
    Assertions.assertEquals("1h00m00.0s", TextUtils.nanosToString(3600010000000L));
    Assertions.assertEquals("1h00m00.1s", TextUtils.nanosToString(3600050000000L));
    Assertions.assertEquals("1h00m00.1s", TextUtils.nanosToString(3600100000000L));
    Assertions.assertEquals("1h00m01s", TextUtils.nanosToString(3601000000000L));
    Assertions.assertEquals("1h00m10s", TextUtils.nanosToString(3610000000000L));
    Assertions.assertEquals("1h01m00s", TextUtils.nanosToString(3660000000000L));
    Assertions.assertEquals("1h59m59.9s", TextUtils.nanosToString(7199900000000L));
    Assertions.assertEquals("2h00m00.0s", TextUtils.nanosToString(7199950000000L));
    Assertions.assertEquals("2h00m00s", TextUtils.nanosToString(7200000000000L));
  }

  @Test
  public void canConvertDecisToString() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> TextUtils.decisToString(-1, false));
    // Seconds should be 1 decimal place
    Assertions.assertEquals("0s", TextUtils.decisToString(0, false));
    Assertions.assertEquals("0.0s", TextUtils.decisToString(0, true));
    Assertions.assertEquals("1s", TextUtils.decisToString(10, false));
    Assertions.assertEquals("1.0s", TextUtils.decisToString(10, true));
    Assertions.assertEquals("1.1s", TextUtils.decisToString(11, false));
    Assertions.assertEquals("9.9s", TextUtils.decisToString(99, false));
    Assertions.assertEquals("10s", TextUtils.decisToString(100, false));
    Assertions.assertEquals("10.1s", TextUtils.decisToString(101, false));
    Assertions.assertEquals("59.9s", TextUtils.decisToString(599, false));
    // Minutes. Seconds should be 1 decimal place
    Assertions.assertEquals("1m00s", TextUtils.decisToString(600, false));
    Assertions.assertEquals("1m00.1s", TextUtils.decisToString(601, false));
    Assertions.assertEquals("1m01s", TextUtils.decisToString(610, false));
    Assertions.assertEquals("59m59.9s", TextUtils.decisToString(35999, false));
    // Hours. Seconds should be 1 decimal place
    Assertions.assertEquals("1h00m00s", TextUtils.decisToString(36000, false));
    Assertions.assertEquals("1h00m00.1s", TextUtils.decisToString(36001, false));
    Assertions.assertEquals("1h00m01s", TextUtils.decisToString(36010, false));
    Assertions.assertEquals("1h00m10s", TextUtils.decisToString(36100, false));
    Assertions.assertEquals("1h01m00s", TextUtils.decisToString(36600, false));
    Assertions.assertEquals("1h59m59.9s", TextUtils.decisToString(71999, false));
    Assertions.assertEquals("2h00m00s", TextUtils.decisToString(72000, false));
  }

}
