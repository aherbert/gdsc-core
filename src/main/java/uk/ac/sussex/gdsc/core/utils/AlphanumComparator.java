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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import java.util.Comparator;

/**
 * Provides number sensitive sorting for character sequences. Leading zeros are ignored from
 * numbers.
 *
 * <pre>
 * Traditional  Alphanum
 * z0200.html   z2.html
 * z100.html    z100.html
 * z2.html      z0200.html
 * </pre>
 *
 * <p>This is is based on ideas in the Alphanum algorithm by David Koelle.
 *
 * <p>This implementation supports:
 *
 * <ul>
 *
 * <li>Buffer caches for more efficient memory consumption
 *
 * <li>{@link CharSequence} comparison
 *
 * <li>Numbers with leading zeros
 *
 * <li>Sorting {@code null} with a defined order
 *
 * </ul>
 *
 * <p>Warning: The algorithm is not thread-safe so should not be used in a parallel sort.
 *
 * @see <a href="http://www.DaveKoelle.com">Alphanum Algorithm</a>
 */
public class AlphanumComparator implements Comparator<CharSequence> {
  /** The null is less flag. */
  private final boolean nullIsLess;
  /** Working buffer 1. */
  private char[] buf1 = new char[0];
  /** Working buffer 2. */
  private char[] buf2 = new char[0];

  /**
   * Create a new instance.
   *
   * @param nullIsLess Set to {@code true} if {@code null} value is less than non-{@code null} value
   */
  public AlphanumComparator(boolean nullIsLess) {
    this.nullIsLess = nullIsLess;
  }

  @Override
  public int compare(CharSequence s1, CharSequence s2) {
    if (s1 == s2) {
      return 0;
    }
    if (s1 == null) {
      return nullIsLess ? -1 : 1;
    }
    if (s2 == null) {
      return nullIsLess ? 1 : -1;
    }

    int pos1 = 0;
    int pos2 = 0;
    final int length1 = s1.length();
    final int length2 = s2.length();
    buf1 = allocate(buf1, length1);
    buf2 = allocate(buf2, length1);

    while (pos1 < length1 && pos2 < length2) {
      int len1 = nextSubSequence(s1, pos1, length1, buf1);
      int len2 = nextSubSequence(s2, pos2, length2, buf2);

      // If both sub-sequences contain numeric characters, sort them numerically
      int result = 0;
      if (isDigit(buf1[0]) && isDigit(buf2[0])) {
        result = compareNumerically(buf1, len1, buf2, len2);
      } else {
        result = compareLexicographically(buf1, len1, buf2, len2);
      }

      if (result != 0) {
        return result;
      }

      pos1 += len1;
      pos2 += len2;
    }

    return length1 - length2;
  }

  private static char[] allocate(char[] buf, int length) {
    return (buf.length < length) ? new char[length] : buf;
  }

  /**
   * Get the next sub-sequence of either digits or non-digit characters starting from the start
   * position into the provided buffer. Leading zeros are ignored.
   *
   * @param seq the character sequence
   * @param start the start position
   * @param length the sequence length
   * @param buf the sub-sequence character buffer
   * @return the length of the sub-sequence
   */
  private static final int nextSubSequence(CharSequence seq, int start, int length, char[] buf) {
    int pos = start;
    char ch = seq.charAt(pos++);
    int len = 0;
    buf[len++] = ch;

    if (isDigit(ch)) {
      while (pos < length) {
        ch = seq.charAt(pos);
        if (!isDigit(ch)) {
          break;
        }
        buf[len++] = ch;
        pos++;
      }
    } else {
      while (pos < length) {
        ch = seq.charAt(pos);
        if (isDigit(ch)) {
          break;
        }
        buf[len++] = ch;
        pos++;
      }
    }
    return len;
  }

  /**
   * Checks if the character is a digit.
   *
   * @param ch the character
   * @return true if a digit
   */
  private static final boolean isDigit(char ch) {
    return ((ch >= 48) && (ch <= 57));
  }

  /**
   * Compares two sequences numerically. Ignores leading zeros. Assumes all characters are digits.
   *
   * @param buf1 the first sequence
   * @param len1 the length of the first sequence
   * @param buf2 the second sequence
   * @param len2 the length of the second sequence
   * @return the value {@code 0} if the sequences are equal; a value less than {@code 0} if sequence
   *         1 is numerically less than sequence 2; and a value greater than {@code 0} if sequence 1
   *         is numerically greater than sequence 2.
   */
  private static int compareNumerically(char[] buf1, int len1, char[] buf2, int len2) {
    // Ignore leading zeros in numbers
    final int start1 = advancePastLeadingZeros(buf1, len1);
    final int start2 = advancePastLeadingZeros(buf2, len2);

    // Simple comparison by length
    final int result = (len1 - start1) - (len2 - start2);
    // If equal, the first different number counts.
    if (result == 0) {
      int i2 = start2;
      for (int i1 = start1; i1 < len1; i1++) {
        final char c1 = buf1[i1];
        final char c2 = buf2[i2++];
        if (c1 != c2) {
          return c1 - c2;
        }
      }
    }
    return result;
  }

  /**
   * Advances past leading zeros. Returns the index of the start character of the number.
   *
   * @param buf the sequence
   * @param len the length of the sequence
   * @return the start index
   */
  private static int advancePastLeadingZeros(char[] buf, int len) {
    if (buf[0] == '0') {
      int start = 0;
      // Ignore zeros only when there are further characters
      while (start < len - 1 && buf[start] == '0') {
        start++;
      }
      return start;
    }
    return 0;
  }

  /**
   * Compares two sequences lexicographically.
   *
   * @param buf1 the first sequence
   * @param len1 the length of the first sequence
   * @param buf2 the second sequence
   * @param len2 the length of the second sequence
   * @return the value {@code 0} if the sequences are equal; a value less than {@code 0} if sequence
   *         1 is lexicographically less than sequence 2; and a value greater than {@code 0} if
   *         sequence 1 is lexicographically greater than sequence 2.
   */
  private static int compareLexicographically(char[] buf1, int len1, char[] buf2, int len2) {
    final int lim = Math.min(len1, len2);

    for (int k = 0; k < lim; k++) {
      final char c1 = buf1[k];
      final char c2 = buf2[k];
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return len1 - len2;
  }
}
