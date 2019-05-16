/*-
 * %%Ignore-License
 *
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com.
 *
 * Released under the MIT License - https://opensource.org/licenses/MIT
 *
 * Copyright 2007-2017 David Koelle
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
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
 * <p>This is an updated version with enhancements made by Daniel Migowski, Andre Bogus, and David
 * Koelle. Updated by David Koelle in 2017.
 *
 * <p>Modification have been made by Alex Herbert to:
 *
 * <ul>
 *
 * <li>Support {@link CharSequence}
 *
 * <li>Handle numbers with leading zeros
 *
 * <li>Sort {@code null} with a defined order
 *
 * </ul>
 *
 * @see <a href="http://www.DaveKoelle.com">Alphanum Algorithm</a>
 */
public class AlphanumComparator implements Comparator<CharSequence> {
  /**
   * The instance where {@code null} is considered less than a non-{@code null} value.
   */
  public static final AlphanumComparator NULL_IS_LESS_INSTANCE = new AlphanumComparator(true);
  /**
   * The instance where {@code null} is considered greater than a non-{@code null} value.
   */
  public static final AlphanumComparator NULL_IS_MORE_INSTANCE = new AlphanumComparator(false);

  /** The null is less flag. */
  final boolean nullIsLess;

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

    int thisMarker = 0;
    int thatMarker = 0;
    final int s1Length = s1.length();
    final int s2Length = s2.length();

    while (thisMarker < s1Length && thatMarker < s2Length) {
      final String thisChunk = getChunk(s1, s1Length, thisMarker);
      final String thatChunk = getChunk(s2, s2Length, thatMarker);

      // If both chunks contain numeric characters, sort them numerically
      int result = 0;
      if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
        // Simple chunk comparison by length.
        final int thisChunkLength = thisChunk.length();
        result = thisChunkLength - thatChunk.length();
        // If equal, the first different number counts
        if (result == 0) {
          for (int i = 0; i < thisChunkLength; i++) {
            result = thisChunk.charAt(i) - thatChunk.charAt(i);
            if (result != 0) {
              return result;
            }
          }
        }
      } else {
        result = thisChunk.compareTo(thatChunk);
      }

      if (result != 0) {
        return result;
      }

      thisMarker += thisChunk.length();
      thatMarker += thatChunk.length();
    }

    return s1Length - s2Length;
  }

  /**
   * Get the next chunk of either digits or non-digit characters starting from the marker. Leading
   * zeros are ignored. Length of string is passed in for improved efficiency (only need to
   * calculate it once).
   *
   * @param string the string
   * @param length the string length
   * @param marker the marker
   * @return the chunk
   */
  private static final String getChunk(CharSequence string, int length, int marker) {
    final char[] chunk = new char[length - marker];
    int count = 0;
    char ch = string.charAt(marker++);
    chunk[count++] = ch;

    if (isDigit(ch)) {
      while (marker < length) {
        ch = string.charAt(marker);
        if (!isDigit(ch)) {
          break;
        }
        chunk[count++] = ch;
        marker++;
      }
      // Ignore leading zeros in numbers
      if (chunk[0] == '0') {
        int offset = 0;
        // Ignore zeros only when there are further characters
        while (offset < count - 1 && chunk[offset] == '0') {
          offset++;
        }
        return new String(chunk, offset, count - offset);
      }
    } else {
      while (marker < length) {
        ch = string.charAt(marker);
        if (isDigit(ch)) {
          break;
        }
        chunk[count++] = ch;
        marker++;
      }
    }
    return new String(chunk, 0, count);
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
}
