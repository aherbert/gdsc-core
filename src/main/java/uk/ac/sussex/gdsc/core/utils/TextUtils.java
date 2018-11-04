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

/**
 * Text utilities.
 */
public final class TextUtils {

  // Constants for time conversion
  private static final long THOUSAND = 1000;
  private static final long MILLION = 1000000;
  private static final long SIXTY = 60;

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
   * Convert time in milliseconds into a nice string.
   *
   * @param time the time
   * @return The string
   * @deprecated Time should be measured using a long so use {@link #timeToString(long)}
   */
  @Deprecated
  public static String timeToString(double time) {
    String units = " ms";
    if (time > 1000) {
      // 1 second
      time /= 1000;
      units = " s";

      if (time > 180) {
        // 3 minutes
        time /= 60;
        units = " min";
      }
    }
    return MathUtils.rounded(time, 4) + units;
  }

  /**
   * Convert time in milliseconds into a nice string. Uses the format:
   *
   * <pre>
   * 0-999ms
   * 0.000s
   * 0m00.000s
   * 0h00m0.000s
   * </pre>
   * 
   * Note: The full information to the millisecond is always preserved.
   *
   * @param milliseconds the duration in milliseconds
   * @return The string
   * @throws IllegalArgumentException If the milliseconds is not positive
   */
  public static String timeToString(long milliseconds) {
    // TODO change this to millisToString
    checkPositive(milliseconds);
    if (milliseconds < THOUSAND) {
      return milliseconds + "ms";
    }
    // No rounding as the modulus preserves the remainder
    long seconds = milliseconds / THOUSAND;
    final long millis = milliseconds % THOUSAND;
    if (seconds < SIXTY) {
      return String.format("%d.%03ds", seconds, millis);
    }
    long minutes = seconds / SIXTY;
    seconds = seconds % SIXTY;
    if (minutes < SIXTY) {
      return String.format("%dm%02d.%03ds", minutes, seconds, millis);
    }
    final long hours = minutes / SIXTY;
    minutes = minutes % SIXTY;
    return String.format("%dh%02dm%02d.%03ds", hours, minutes, seconds, millis);
  }

  /**
   * Convert time in nanoseconds into a nice string. Uses the format:
   *
   * <pre>
   * 0-999ns
   * 0-999µs
   * 0-999ms
   * 0.000s
   * 0m00.000s
   * 0h00m0.000s
   * </pre>
   * 
   * Durations are rounded to 3 significant figures when converting the time unit, e.g. 9999ns
   * becomes 10µs, 999999ns becomes 1.000s.
   *
   * @param nanoseconds the duration in nanoseconds
   * @return the string
   * @throws IllegalArgumentException If the nanoseconds is not positive
   */
  public static String nanosToString(long nanoseconds) {
    checkPositive(nanoseconds);
    if (nanoseconds < THOUSAND) {
      return nanoseconds + "ns";
    }
    // Rounding to microseconds - overflow safe
    final long microseconds = divideAndRound(nanoseconds, THOUSAND);
    if (microseconds < THOUSAND) {
      return microseconds + "µs";
    }
    // Rounding to milliseconds - overflow safe
    final long milliseconds = divideAndRound(nanoseconds, MILLION);
    return timeToString(milliseconds);
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
   * Divide the value by the divisor and round up/down to the nearest integer.
   *
   * @param value the value
   * @param divisor the divisor
   * @return the result
   */
  private static long divideAndRound(long value, long divisor) {
    long floor = value / divisor;
    long remainder = value % divisor;
    // This has the effect of rounding up the floor to the next integer if
    // the remainder is >= half of the divisor.
    return floor + (remainder + divisor / 2) / divisor;
  }
}
