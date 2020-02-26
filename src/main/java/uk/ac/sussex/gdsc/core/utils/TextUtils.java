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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Formatter;
import java.util.Locale;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Text utilities.
 */
public final class TextUtils {

  // Constants for time conversion
  private static final MathContext ROUND_TO_3_SF = new MathContext(3);
  private static final long TEN = 10;
  private static final long SIXTY = 60;
  private static final long HUNDRED = 100;
  private static final long THOUSAND = 1000;
  private static final long HUNDRED_MILLION = HUNDRED * THOUSAND * THOUSAND;
  private static final BigDecimal BD_SIXTY = BigDecimal.valueOf(SIXTY);
  private static final BigDecimal BD_THOUSAND = BigDecimal.valueOf(THOUSAND);
  private static final BigDecimal BD_MILLION = BigDecimal.valueOf(THOUSAND * THOUSAND);
  private static final BigDecimal BD_BILLION = BigDecimal.valueOf(THOUSAND * THOUSAND * THOUSAND);

  // Constants for byte conversion
  private static final String[] SI_UNITS = {"B", "kB", "MB", "GB", "TB", "PB", "EB"};
  private static final String[] BINARY_UNITS = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};

  /** No public constructor. */
  private TextUtils() {}

  // -----------------------------------------------------------------------
  // Wrapping - Delegate to apache.commons.lang.WordUtils
  // -----------------------------------------------------------------------

  /**
   * Wraps a single line of text, identifying words by <code>' '</code>.
   *
   * <p>New lines will be separated by the system property line separator. Very long words, such as
   * URLs will <i>not</i> be wrapped.
   *
   * <p>Leading spaces on a new line are stripped. Trailing spaces are not stripped.
   *
   * <pre>
   * WordUtils.wrap(null, *) = null
   * WordUtils.wrap("", *) = ""
   * </pre>
   *
   * @param str the String to be word wrapped, may be null
   * @param wrapLength the column to wrap the words at, less than 1 is treated as 1
   * @return a line with newlines inserted, <code>null</code> if null input
   */
  public static String wrap(String str, int wrapLength) {
    return wrap(str, wrapLength, null, false);
  }

  /**
   * Wraps a single line of text, identifying words by <code>' '</code>.
   *
   * <p>Leading spaces on a new line are stripped. Trailing spaces are not stripped.
   *
   * <pre>
   * WordUtils.wrap(null, *, *, *) = null
   * WordUtils.wrap("", *, *, *) = ""
   * </pre>
   *
   * @param str the String to be word wrapped, may be null
   * @param wrapLength the column to wrap the words at, less than 1 is treated as 1
   * @param newLineStr the string to insert for a new line, <code>null</code> uses the system
   *        property line separator
   * @param wrapLongWords true if long words (such as URLs) should be wrapped
   * @return a line with newlines inserted, <code>null</code> if null input
   */
  @SuppressWarnings("deprecation")
  public static String wrap(String str, int wrapLength, String newLineStr, boolean wrapLongWords) {
    return WordUtils.wrap(str, wrapLength, newLineStr, wrapLongWords);
  }

  /**
   * Return "s" if the size is not 1 otherwise returns an empty string. This can be used to add an s
   * where necessary to nouns:
   *
   * <pre>
   * System.out.printf(&quot;Created %d thing%s\n&quot;, n, TextUtils.pleural(n));
   * </pre>
   *
   * @param n The number of things
   * @return "s" or empty string
   */
  public static String pleural(int n) {
    return (Math.abs(n) == 1) ? "" : "s";
  }

  /**
   * Create a string of the number and the name. Adds "s" to the name if the size is not 1. This can
   * be used to add an s where necessary to nouns:
   *
   * <pre>
   * System.out.printf(&quot;Created %s\n&quot;, TextUtils.pleural(n, &quot;thing&quot;));
   * </pre>
   *
   * @param n The number of things
   * @param name The name of the thing
   * @return {@code n + " " + name} (name has "s" at the end if size is not 1)
   */
  public static String pleural(int n, String name) {
    return n + " " + name + ((Math.abs(n) == 1) ? "" : "s");
  }

  /**
   * Get the correct pleural form for the count. E.g.
   *
   * <pre>
   * TextUtils.pleural(0, "mouse", "mice") => "mice"
   * TextUtils.pleural(1, "mouse", "mice") => "mouse"
   * TextUtils.pleural(2, "mouse", "mice") => "mice"
   * </pre>
   *
   * @param count the count
   * @param singular the singular form
   * @param pleural the pleural form
   * @return the string
   */
  public static String pleuralise(int count, String singular, String pleural) {
    return (count == 1) ? singular : pleural;
  }

  /**
   * Check if the string is null or length zero. Does not check for a string of whitespace.
   *
   * <p>This produces the opposite result to {@link #isNotEmpty(String)}.
   *
   * @param string the string
   * @return true if the string is null or length zero
   */
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }

  /**
   * Check if the string has characters. Does not check for a string of whitespace.
   *
   * <p>Returns false for a null or empty string, that is it produces the opposite result to
   * {@link #isNullOrEmpty(String)}.
   *
   * @param string the string
   * @return true if the string has characters
   */
  public static boolean isNotEmpty(String string) {
    return string != null && !string.isEmpty();
  }

  /**
   * Get the length of the string.
   *
   * <p>A null string will return zero.
   *
   * @param string the string
   * @return the length
   */
  public static int getLength(String string) {
    return (string != null) ? string.length() : 0;
  }

  /**
   * Convert time in milliseconds into a nice string. Uses the format:
   *
   * <pre>
   *       0-999ms
   *       0.00s
   *    0m00.0s
   * 0h00m00.0s
   * </pre>
   *
   * <p>Durations in seconds are rounded to 3 significant figures.
   *
   * <p>When minutes or hours are present the seconds are rounded to 1 decimal place.
   *
   * <p>Trailing zeros are omitted unless required to signify rounding has occurred.
   *
   * @param milliseconds the duration in milliseconds
   * @return The string
   * @throws IllegalArgumentException If the milliseconds is not positive
   */
  public static String millisToString(long milliseconds) {
    checkPositive(milliseconds);
    if (milliseconds < THOUSAND) {
      // No rounding
      return milliseconds + "ms";
    }
    // Rounding uses BigDecimal to achieve 3 s.f.
    final BigDecimal millis = BigDecimal.valueOf(milliseconds);
    // Round to seconds
    final BigDecimal seconds = millis.divide(BD_THOUSAND, ROUND_TO_3_SF);
    if (seconds.compareTo(BD_SIXTY) < 0) {
      return seconds.toString() + "s";
    }
    // Once in minutes then seconds should be to 1 decimal place.
    // Convert to deciseconds with rounding and output.
    final long deciseconds = divideAndRound(milliseconds, HUNDRED);
    final boolean wasRounded = milliseconds % HUNDRED != 0;
    return decisToString(deciseconds, wasRounded);
  }

  /**
   * Convert time in nanoseconds into a nice string. Uses the format:
   *
   * <pre>
   *       0-999ns
   *       0.00µs
   *       0.00ms
   *       0.00s
   *    0m00.0s
   * 0h00m00.0s
   * </pre>
   *
   * <p>Durations in microseconds, milliseconds and seconds are rounded to 3 significant figures.
   *
   * <p>When minutes or hours are present the seconds are rounded to 1 decimal place.
   *
   * <p>Trailing zeros are omitted unless required to signify rounding has occurred.
   *
   * @param nanoseconds the duration in nanoseconds
   * @return the string
   * @throws IllegalArgumentException If the nanoseconds is not positive
   */
  public static String nanosToString(long nanoseconds) {
    checkPositive(nanoseconds);
    if (nanoseconds < THOUSAND) {
      // No rounding
      return nanoseconds + "ns";
    }
    // Rounding uses BigDecimal to achieve 3 s.f.
    final BigDecimal nanos = BigDecimal.valueOf(nanoseconds);
    // Round to microseconds
    final BigDecimal micros = nanos.divide(BD_THOUSAND, ROUND_TO_3_SF);
    if (micros.compareTo(BD_THOUSAND) < 0) {
      return micros.toString() + "µs";
    }
    // Round to milliseconds
    final BigDecimal millis = nanos.divide(BD_MILLION, ROUND_TO_3_SF);
    if (millis.compareTo(BD_THOUSAND) < 0) {
      return millis.toString() + "ms";
    }
    // Round to seconds
    final BigDecimal seconds = nanos.divide(BD_BILLION, ROUND_TO_3_SF);
    if (seconds.compareTo(BD_SIXTY) < 0) {
      return seconds.toString() + "s";
    }
    // Once in minutes then seconds should be to 1 decimal place.
    // Convert to deciseconds with rounding and output.
    final long deciseconds = divideAndRound(nanoseconds, HUNDRED_MILLION);
    final boolean wasRounded = nanoseconds % HUNDRED_MILLION != 0;
    return decisToString(deciseconds, wasRounded);
  }

  /**
   * Convert time in deciseconds into a nice string. Uses the format:
   *
   * <pre>
   *       0.0s
   *    0m00.0s
   * 0h00m00.0s
   * </pre>
   *
   * <p>No rounding is required.
   *
   * <p>Ommits the trailing .0s unless the {@code wasRounded} flag is set to true.
   *
   * @param deciseconds the duration in deciseconds
   * @param wasRounded true if the deciseconds have been rounded
   * @return the string
   * @throws IllegalArgumentException If the deciseconds is not positive
   */
  public static String decisToString(long deciseconds, boolean wasRounded) {
    checkPositive(deciseconds);
    // No rounding as the modulus preserves the remainder
    long seconds = deciseconds / TEN;
    final long tenths = deciseconds % TEN;
    if (seconds < SIXTY) {
      return String.format("%d%s", seconds, getTenths(tenths, wasRounded));
    }
    long minutes = seconds / SIXTY;
    seconds = seconds % SIXTY;
    if (minutes < SIXTY) {
      return String.format("%dm%02d%s", minutes, seconds, getTenths(tenths, wasRounded));
    }
    final long hours = minutes / SIXTY;
    minutes = minutes % SIXTY;
    return String.format("%dh%02dm%02d%s", hours, minutes, seconds, getTenths(tenths, wasRounded));
  }

  /**
   * Check the duration is positive.
   *
   * @param duration the duration
   * @throws IllegalArgumentException If the duration is not positive
   */
  private static void checkPositive(long duration) {
    if (duration < 0) {
      throw new IllegalArgumentException("Duration must be positive: " + duration);
    }
  }

  /**
   * Divide the value by the divisor and round up/down to the nearest integer. This is overflow
   * safe, e.g. value can be {@link Long#MAX_VALUE}.
   *
   * @param value the value (must be above 0)
   * @param divisor the divisor (must be above 0)
   * @return the result
   */
  private static long divideAndRound(long value, long divisor) {
    final long floor = value / divisor;
    final long remainder = value % divisor;
    // This has the effect of rounding up the floor to the next integer if
    // the remainder is >= half of the divisor.
    return floor + (remainder + divisor / 2) / divisor;
  }

  private static String getTenths(long tenths, boolean wasRounded) {
    return (wasRounded || tenths != 0) ? "." + tenths + "s" : "s";
  }

  /**
   * Convert bytes to a human readable string. Example output:
   *
   * <pre>
   *                              SI     BINARY
   *                   0:        0 B        0 B
   *                  27:       27 B       27 B
   *                 999:      999 B      999 B
   *                1000:     1.0 kB     1000 B
   *                1023:     1.0 kB     1023 B
   *                1024:     1.0 kB    1.0 KiB
   *                1728:     1.7 kB    1.7 KiB
   *              110592:   110.6 kB  108.0 KiB
   *             7077888:     7.1 MB    6.8 MiB
   *           452984832:   453.0 MB  432.0 MiB
   *         28991029248:    29.0 GB   27.0 GiB
   *       1855425871872:     1.9 TB    1.7 TiB
   * 9223372036854775807:     9.2 EB    8.0 EiB   (Long.MAX_VALUE)
   * </pre>
   *
   * @param bytes the bytes
   * @param useSiUnits Set to true to use SI units
   * @param locale the locale
   * @return the string
   * @see <a
   *      href="https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java">How
   *      to convert byte size into human readable format in java?</a>
   */
  public static String bytesToString(final long bytes, final boolean useSiUnits,
      final Locale locale) {
    final String[] units = useSiUnits ? SI_UNITS : BINARY_UNITS;
    final int base = useSiUnits ? 1000 : 1024;

    // When using the smallest unit no decimal point is needed, because it's the exact number.
    if (bytes < base) {
      return bytes + " " + units[0];
    }

    final int exponent = (int) (Math.log(bytes) / Math.log(base));
    final String unit = units[exponent];
    return String.format(locale, "%.1f %s", bytes / Math.pow(base, exponent), unit);
  }

  /**
   * Convert bytes to a human readable string using SI units and the default locale. Example output:
   *
   * <pre>
   *                              SI
   *                   0:        0 B
   *                  27:       27 B
   *                 999:      999 B
   *                1000:     1.0 kB
   *                1023:     1.0 kB
   *                1024:     1.0 kB
   *                1728:     1.7 kB
   *              110592:   110.6 kB
   *             7077888:     7.1 MB
   *           452984832:   453.0 MB
   *         28991029248:    29.0 GB
   *       1855425871872:     1.9 TB
   * 9223372036854775807:     9.2 EB   (Long.MAX_VALUE)
   * </pre>
   *
   * @param bytes the bytes
   * @return the string
   * @see #bytesToString(long, boolean, Locale)
   */
  public static String bytesToString(final long bytes) {
    return bytesToString(bytes, true, Locale.getDefault());
  }

  /**
   * Format a message to an {@link Appendable}. Convenience method for the equivalent of:
   *
   * <pre>
   * return new Formatter(appendable).format(format, args).out()
   * </pre>
   *
   * <p>It is recommended to explicitly create a {@link Formatter} if multiple invocations are
   * expected.
   *
   * @param appendable the appendable
   * @param format the format
   * @param args the arguments
   * @return the appendable
   */
  public static Appendable formatTo(Appendable appendable, String format, Object... args) {
    return formatTo(appendable, Locale.getDefault(Locale.Category.FORMAT), format, args);
  }

  /**
   * Format a message to an {@link Appendable}. Convenience method for the equivalent of:
   *
   * <pre>
   * return new Formatter(appendable).format(locale, format, args).out()
   * </pre>
   *
   * <p>It is recommended to explicitly create a {@link Formatter} if multiple invocations are
   * expected.
   *
   * @param appendable the appendable
   * @param locale the locale
   * @param format the format
   * @param args the arguments
   * @return the appendable
   */
  @SuppressWarnings("resource")
  public static Appendable formatTo(Appendable appendable, Locale locale, String format,
      Object... args) {
    new Formatter(appendable).format(locale, format, args);
    return appendable;
  }
}
