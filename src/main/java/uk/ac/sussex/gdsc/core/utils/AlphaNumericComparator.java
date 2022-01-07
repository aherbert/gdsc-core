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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Provides number sensitive sorting for character sequences.
 *
 * <p>Extracts sub-sequences of either numeric ({@code [0, 9]}) or non-numeric characters and
 * compares them numerically or lexicographically. Leading zeros are ignored from numbers. Negative
 * numbers are not supported.
 *
 * <pre>
 * Traditional  AlphaNumeric
 * z0200.html   z2.html
 * z100.html    z100.html
 * z2.html      z0200.html
 * </pre>
 *
 * <p>This is based on ideas in the Alphanum algorithm by David Koelle.
 *
 * <p>This implementation supports:
 *
 * <ul>
 *
 * <li>{@link CharSequence} comparison
 *
 * <li>Numbers with leading zeros
 *
 * <li>Direct use of input sequences for minimal memory consumption
 *
 * <li>Sorting {@code null} with a defined order
 *
 * </ul>
 *
 * <p>Note: The comparator is thread-safe so can be used in a parallel sort.
 *
 * @see <a href="http://www.DaveKoelle.com">Alphanum Algorithm</a>
 */
public class AlphaNumericComparator implements Comparator<CharSequence>, Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The instance where {@code null} is considered less than a non-{@code null} value.
   */
  public static final AlphaNumericComparator NULL_IS_LESS_INSTANCE =
      new AlphaNumericComparator(true);
  /**
   * The instance where {@code null} is considered greater than a non-{@code null} value.
   */
  public static final AlphaNumericComparator NULL_IS_MORE_INSTANCE =
      new AlphaNumericComparator(false);

  /** The null is less flag. */
  private final boolean nullIsLess;

  /**
   * Create a new instance.
   *
   * @param nullIsLess Set to {@code true} if {@code null} value is less than non-{@code null} value
   */
  public AlphaNumericComparator(boolean nullIsLess) {
    this.nullIsLess = nullIsLess;
  }

  @Override
  public int compare(CharSequence seq1, CharSequence seq2) {
    if (seq1 == seq2) {
      return 0;
    }
    if (seq1 == null) {
      return nullIsLess ? -1 : 1;
    }
    if (seq2 == null) {
      return nullIsLess ? 1 : -1;
    }

    int pos1 = 0;
    int pos2 = 0;
    final int length1 = seq1.length();
    final int length2 = seq2.length();

    while (pos1 < length1 && pos2 < length2) {
      final int end1 = nextSubSequenceEnd(seq1, pos1, length1);
      final int end2 = nextSubSequenceEnd(seq2, pos2, length2);

      // If both sub-sequences contain numeric characters, sort them numerically
      int result = 0;
      if (isDigit(seq1.charAt(pos1)) && isDigit(seq2.charAt(pos2))) {
        result = compareNumerically(seq1, pos1, end1, seq2, pos2, end2);
      } else {
        result = compareLexicographically(seq1, pos1, end1, seq2, pos2, end2);
      }

      if (result != 0) {
        return result;
      }

      pos1 = end1;
      pos2 = end2;
    }

    return length1 - length2;
  }

  /**
   * Get the end position of the next sub-sequence of either digits or non-digit characters starting
   * from the start position.
   *
   * <p>The end position is exclusive such that the sub-sequence is the interval
   * {@code [start, end)}.
   *
   * @param seq the character sequence
   * @param start the start position
   * @param length the sequence length
   * @return the sub-sequence end position (exclusive)
   */
  private static int nextSubSequenceEnd(CharSequence seq, int start, int length) {
    int pos = start;
    // Set the sub-sequence type (digits or non-digits)
    final boolean seqType = isDigit(seq.charAt(pos++));
    while (pos < length && seqType == isDigit(seq.charAt(pos))) {
      // Extend the sub-sequence
      pos++;
    }
    return pos;
  }

  /**
   * Checks if the character is a digit.
   *
   * @param ch the character
   * @return true if a digit
   */
  private static boolean isDigit(char ch) {
    return ch >= 48 && ch <= 57;
  }

  /**
   * Compares two sub-sequences numerically. Ignores leading zeros. Assumes all characters are
   * digits.
   *
   * @param seq1 the first sequence
   * @param start1 the start of the first sub-sequence
   * @param end1 the end of the first sub-sequence
   * @param seq2 the second sequence
   * @param start2 the start of the second sub-sequence
   * @param end2 the end of the second sub-sequence sequence
   * @return the value {@code 0} if the sub-sequences are equal; a value less than {@code 0} if
   *         sub-sequence 1 is numerically less than sub-sequence 2; and a value greater than
   *         {@code 0} if sub-sequence 1 is numerically greater than sub-sequence 2.
   */
  private static int compareNumerically(CharSequence seq1, int start1, int end1, CharSequence seq2,
      int start2, int end2) {
    // Ignore leading zeros in numbers
    int pos1 = advancePastLeadingZeros(seq1, start1, end1);
    int pos2 = advancePastLeadingZeros(seq2, start2, end2);

    // Simple comparison by length
    final int result = (end1 - pos1) - (end2 - pos2);
    // If equal, the first different number counts.
    if (result == 0) {
      while (pos1 < end1) {
        final char c1 = seq1.charAt(pos1++);
        final char c2 = seq2.charAt(pos2++);
        if (c1 != c2) {
          return c1 - c2;
        }
      }
    }
    return result;
  }

  /**
   * Advances past leading zeros in the sub-sequence. Returns the index of the start character of
   * the number.
   *
   * @param seq the sequence
   * @param start the start of the sub-sequence
   * @param end the end of the sub-sequence
   * @return the start index of the number
   */
  private static int advancePastLeadingZeros(CharSequence seq, int start, int end) {
    int pos = start;
    // Ignore zeros only when there are further characters
    while (pos < end - 1 && seq.charAt(pos) == '0') {
      pos++;
    }
    return pos;
  }

  /**
   * Compares two sub-sequences lexicographically. This matches the compare function in
   * {@link String} using extracted sub-sequences.
   *
   * @param seq1 the first sequence
   * @param start1 the start of the first sub-sequence
   * @param end1 the end of the first sub-sequence
   * @param seq2 the second sequence
   * @param start2 the start of the second sub-sequence
   * @param end2 the end of the second sub-sequence sequence
   * @return the value {@code 0} if the sub-sequences are equal; a value less than {@code 0} if
   *         sub-sequence 1 is lexicographically less than sub-sequence 2; and a value greater than
   *         {@code 0} if sub-sequence 1 is lexicographically greater than sub-sequence 2.
   * @see String#compareTo(String)
   */
  private static int compareLexicographically(CharSequence seq1, int start1, int end1,
      CharSequence seq2, int start2, int end2) {
    final int len1 = end1 - start1;
    final int len2 = end2 - start2;
    final int limit = Math.min(len1, len2);

    for (int i = 0; i < limit; i++) {
      final char c1 = seq1.charAt(i + start1);
      final char c2 = seq2.charAt(i + start2);
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return len1 - len2;
  }
}
