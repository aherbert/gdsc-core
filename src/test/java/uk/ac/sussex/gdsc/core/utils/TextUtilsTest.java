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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class TextUtilsTest {

  @Test
  void canWrap() {
    Assertions.assertNull(TextUtils.wrap(null, 100));
    final String str = "This is some text";
    Assertions.assertEquals("This is some text", TextUtils.wrap(str, 100));
    Assertions.assertEquals("This is some text", TextUtils.wrap(str, 100, "\n", false));
    Assertions.assertEquals("This is\nsome text", TextUtils.wrap(str, 10, "\n", false));
    Assertions.assertEquals("xxxxxxxx", TextUtils.wrap("xxxxxxxx", 4, "\n", false));
    Assertions.assertEquals("xxxx\nxxxx", TextUtils.wrap("xxxxxxxx", 4, "\n", true));
  }

  @Test
  void testPleural() {
    Assertions.assertEquals("0 bugs", TextUtils.pleural(0, "bug"));
    Assertions.assertEquals("1 bug", TextUtils.pleural(1, "bug"));
    Assertions.assertEquals("10 bugs", TextUtils.pleural(10, "bug"));
  }

  @Test
  void testPleuralise() {
    Assertions.assertEquals("mice", TextUtils.pleuralise(0, "mouse", "mice"));
    Assertions.assertEquals("mouse", TextUtils.pleuralise(1, "mouse", "mice"));
    Assertions.assertEquals("mice", TextUtils.pleuralise(10, "mouse", "mice"));
  }

  @Test
  void canConvertMillisToString() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtils.millisToString(-1));
    // Milliseconds reported exactly
    Assertions.assertEquals("0ms", TextUtils.millisToString(0));
    Assertions.assertEquals("999ms", TextUtils.millisToString(999));
    // Seconds to 3 s.f.
    Assertions.assertEquals("1s", TextUtils.millisToString(1000));
    Assertions.assertEquals("1.00s", TextUtils.millisToString(1001));
    Assertions.assertEquals("1.01s", TextUtils.millisToString(1005));
    Assertions.assertEquals("1.1s", TextUtils.millisToString(1100));
    Assertions.assertEquals("9.99s", TextUtils.millisToString(9990));
    Assertions.assertEquals("10.0s", TextUtils.millisToString(9995));
    Assertions.assertEquals("10s", TextUtils.millisToString(10000));
    Assertions.assertEquals("10.0s", TextUtils.millisToString(10010));
    Assertions.assertEquals("10.1s", TextUtils.millisToString(10050));
    Assertions.assertEquals("10.1s", TextUtils.millisToString(10100));
    Assertions.assertEquals("59.9s", TextUtils.millisToString(59900));
    // Minutes. Seconds should be 1 decimal place
    Assertions.assertEquals("1m00.0s", TextUtils.millisToString(59950));
    Assertions.assertEquals("1m00s", TextUtils.millisToString(60000));
    Assertions.assertEquals("1m00.0s", TextUtils.millisToString(60001));
    Assertions.assertEquals("1m00.0s", TextUtils.millisToString(60010));
    Assertions.assertEquals("1m00.1s", TextUtils.millisToString(60050));
    Assertions.assertEquals("1m00.1s", TextUtils.millisToString(60100));
    Assertions.assertEquals("1m01s", TextUtils.millisToString(61000));
    Assertions.assertEquals("59m59.9s", TextUtils.millisToString(3599900));
    // Hours. Seconds should be 1 decimal place
    Assertions.assertEquals("1h00m00.0s", TextUtils.millisToString(3599950));
    Assertions.assertEquals("1h00m00s", TextUtils.millisToString(3600000));
    Assertions.assertEquals("1h00m00.0s", TextUtils.millisToString(3600001));
    Assertions.assertEquals("1h00m00.0s", TextUtils.millisToString(3600010));
    Assertions.assertEquals("1h00m00.1s", TextUtils.millisToString(3600050));
    Assertions.assertEquals("1h00m00.1s", TextUtils.millisToString(3600100));
    Assertions.assertEquals("1h00m01s", TextUtils.millisToString(3601000));
    Assertions.assertEquals("1h00m10s", TextUtils.millisToString(3610000));
    Assertions.assertEquals("1h01m00s", TextUtils.millisToString(3660000));
    Assertions.assertEquals("1h59m59.9s", TextUtils.millisToString(7199900));
    Assertions.assertEquals("2h00m00.0s", TextUtils.millisToString(7199950));
    Assertions.assertEquals("2h00m00s", TextUtils.millisToString(7200000));
  }

  @Test
  void canConvertNanosToString() {
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
  void canConvertDecisToString() {
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

  @Test
  void canConvertBytesToString() {
    Assertions.assertEquals("0 B", TextUtils.bytesToString(0));
    Assertions.assertEquals("27 B", TextUtils.bytesToString(27));
    Assertions.assertEquals("999 B", TextUtils.bytesToString(999));
    Assertions.assertEquals("1.0 kB", TextUtils.bytesToString(1000));
    Assertions.assertEquals("1.0 kB", TextUtils.bytesToString(1023));
    Assertions.assertEquals("1.0 kB", TextUtils.bytesToString(1024));
    Assertions.assertEquals("1.7 kB", TextUtils.bytesToString(1728));
    Assertions.assertEquals("110.6 kB", TextUtils.bytesToString(110592));
    Assertions.assertEquals("7.1 MB", TextUtils.bytesToString(7077888));
    Assertions.assertEquals("453.0 MB", TextUtils.bytesToString(452984832));
    Assertions.assertEquals("29.0 GB", TextUtils.bytesToString(28991029248L));
    Assertions.assertEquals("1.9 TB", TextUtils.bytesToString(1855425871872L));
    Assertions.assertEquals("9.2 EB", TextUtils.bytesToString(9223372036854775807L));
  }

  @Test
  void canConvertBytesToStringWithBinaryUnits() {
    final Locale locale = Locale.UK;
    Assertions.assertEquals("0 B", TextUtils.bytesToString(0, false, locale));
    Assertions.assertEquals("27 B", TextUtils.bytesToString(27, false, locale));
    Assertions.assertEquals("999 B", TextUtils.bytesToString(999, false, locale));
    Assertions.assertEquals("1000 B", TextUtils.bytesToString(1000, false, locale));
    Assertions.assertEquals("1023 B", TextUtils.bytesToString(1023, false, locale));
    Assertions.assertEquals("1.0 KiB", TextUtils.bytesToString(1024, false, locale));
    Assertions.assertEquals("1.7 KiB", TextUtils.bytesToString(1728, false, locale));
    Assertions.assertEquals("108.0 KiB", TextUtils.bytesToString(110592, false, locale));
    Assertions.assertEquals("6.8 MiB", TextUtils.bytesToString(7077888, false, locale));
    Assertions.assertEquals("432.0 MiB", TextUtils.bytesToString(452984832, false, locale));
    Assertions.assertEquals("27.0 GiB", TextUtils.bytesToString(28991029248L, false, locale));
    Assertions.assertEquals("1.7 TiB", TextUtils.bytesToString(1855425871872L, false, locale));
    Assertions.assertEquals("8.0 EiB",
        TextUtils.bytesToString(9223372036854775807L, false, locale));
  }

  @Test
  void canCheckIsNullOrEmptyString() {
    Assertions.assertTrue(TextUtils.isNullOrEmpty(null));
    Assertions.assertTrue(TextUtils.isNullOrEmpty(""));
    Assertions.assertFalse(TextUtils.isNullOrEmpty(" "));
    Assertions.assertFalse(TextUtils.isNullOrEmpty("a"));
  }

  @Test
  void canCheckIsNotEmptyString() {
    Assertions.assertFalse(TextUtils.isNotEmpty(null));
    Assertions.assertFalse(TextUtils.isNotEmpty(""));
    Assertions.assertTrue(TextUtils.isNotEmpty(" "));
    Assertions.assertTrue(TextUtils.isNotEmpty("a"));
  }

  @Test
  void canFormatToAppendable() {
    final String format = "%s=%d";
    final Object[] args = {"text", 1};
    final String expected = String.format(format, args);
    final StringBuilder sb = new StringBuilder();
    TextUtils.formatTo(sb, format, args);
    final String actual = sb.toString();
    Assertions.assertEquals(expected, actual);
  }
}
