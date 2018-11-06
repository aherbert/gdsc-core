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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

  /** No public constructor. */
  private TextUtils() {}

  // -----------------------------------------------------------------------
  // Wrapping - Taken from apache.commons.lang.WordUtils
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
  public static String wrap(String str, int wrapLength, String newLineStr, boolean wrapLongWords) {
    if (str == null) {
      return null;
    }
    if (newLineStr == null) {
      newLineStr = System.getProperty("line.separator");
    }
    if (wrapLength < 1) {
      wrapLength = 1;
    }
    final int inputLineLength = str.length();
    int offset = 0;
    final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

    while ((inputLineLength - offset) > wrapLength) {
      if (str.charAt(offset) == ' ') {
        offset++;
        continue;
      }
      int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);

      if (spaceToWrapAt >= offset) {
        // normal case
        wrappedLine.append(str.substring(offset, spaceToWrapAt));
        wrappedLine.append(newLineStr);
        offset = spaceToWrapAt + 1;

        // really long word or URL
      } else if (wrapLongWords) {
        // wrap really long word one line at a time
        wrappedLine.append(str.substring(offset, wrapLength + offset));
        wrappedLine.append(newLineStr);
        offset += wrapLength;
      } else {
        // do not wrap really long word, just extend beyond limit
        spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
        if (spaceToWrapAt >= 0) {
          wrappedLine.append(str.substring(offset, spaceToWrapAt));
          wrappedLine.append(newLineStr);
          offset = spaceToWrapAt + 1;
        } else {
          wrappedLine.append(str.substring(offset));
          offset = inputLineLength;
        }
      }
    }

    // Whatever is left in line is short enough to just pass through
    wrappedLine.append(str.substring(offset));

    return wrappedLine.toString();
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
   * @return "s" or empty string
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
   * @param string the string
   * @return true if the string is null or length zero
   */
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
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
}
