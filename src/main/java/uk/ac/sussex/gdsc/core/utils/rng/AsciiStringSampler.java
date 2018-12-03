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

package uk.ac.sussex.gdsc.core.utils.rng;

import gnu.trove.list.array.TCharArrayList;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.Objects;

/**
 * Class for generating random strings from the printable ASCII character set.
 *
 * <p>Characters will be chosen from the set of characters whose ASCII value is between {@code 32}
 * and {@code 126} (inclusive). This is: <ul> <li>{@code 0} to {@code 9} <li>{@code A} to {@code Z}
 * <li>{@code a} to {@code z} <li><code>!"#$%&amp;'()*+,-./:;&lt;=&gt;?@\]^_`{|}</code> <li>space
 * (ASCII {@code 32}) <li>escape (ASCII {@code 126}) </ul>.
 *
 * <p>Methods names are inspired by {@code org.apache.commons.lang3.RandomStringUtils}. For simple
 * ASCII character sets (with no support for range of code points) this class will outperform
 * {@code RandomStringUtils}. For a custom set of characters {@code char[]} it is recommended to use
 * {@code RandomStringUtils} with a wrapped {@link java.util.Random} implementing
 * {@link java.util.Random#nextInt(int)}.
 */
public class AsciiStringSampler {

  /**
   * The printable ASCII characters. This table is arranged into the printable ASCII characters in
   * the order: <ul> <li>0-9 <li>A-Z <li>a-z <li>others except space and escape (code 126) <li>space
   * <li>escape (code 126) </ul>
   */
  private static final char[] ASCII;

  /** The start of the upper case letters. */
  private static final int START_UPPER_CASE;
  /** The start of the lower case letters. */
  private static final int START_LOWER_CASE;
  /** The start of the other letters. */
  private static final int START_OTHER;

  static {
    final TCharArrayList list = new TCharArrayList();
    // Numbers
    for (int i = 48; i <= 57; i++) {
      list.add((char) i);
    }
    // Upper case letters
    START_UPPER_CASE = list.size();
    for (int i = 65; i <= 90; i++) {
      list.add((char) i);
    }
    // Lower case letters
    START_LOWER_CASE = list.size();
    for (int i = 97; i <= 122; i++) {
      list.add((char) i);
    }
    START_OTHER = list.size();
    for (int i = 33; i <= 47; i++) {
      list.add((char) i);
    }
    for (int i = 58; i <= 64; i++) {
      list.add((char) i);
    }
    for (int i = 91; i <= 96; i++) {
      list.add((char) i);
    }
    for (int i = 123; i <= 125; i++) {
      list.add((char) i);
    }
    // Space
    list.add(' ');
    list.add((char) 126);
    ASCII = list.toArray();
  }

  /** The generator of uniformly distributed random numbers. */
  private final UniformRandomProvider rng;

  /**
   * Creates a generator of strings.
   *
   * <p>The sampling works using the {@code UniformRandomProvider#nextInt(int)} method so a native
   * generator of ints is best.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @throws NullPointerException If {@code rng} is null.
   */
  public AsciiStringSampler(UniformRandomProvider rng) {
    this.rng = Objects.requireNonNull(rng, "Random generator must not be null");
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z). </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextAlphabetic(int count) {
    return next(count, START_UPPER_CASE, START_OTHER - START_UPPER_CASE, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z) and the digits
   * 0-9. </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextAlphanumeric(int count) {
    return next(count, START_OTHER, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of characters whose ASCII value is between {@code 32}
   * and {@code 126} (inclusive). </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextAscii(int count) {
    return next(count, ASCII.length, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the MIME Base64 table: A-Z, a-z, 0-9, +, /. </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextBase64(int count) {
    return next(count, 64, RadixStringSampler.TABLE64);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of Characters (A-Z) and the digits 0-9. </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextCharacter(int count) {
    return next(count, 36, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of characters which match the POSIX [:graph:] regular
   * expression character class. This class contains all visible ASCII characters (i.e. anything
   * except spaces and control characters). </p>.
   *
   * <p>Characters will be chosen from the set of characters whose ASCII value is between {@code 33}
   * and {@code 125} (inclusive). </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextGraph(int count) {
    return next(count, ASCII.length - 2, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of characters (A-Z) and the digits 0-9. </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextHex(int count) {
    return next(count, 16, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of lower case characters (a-z). </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextLower(int count) {
    return next(count, START_LOWER_CASE, START_OTHER - START_LOWER_CASE, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of numeric characters. </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextNumeric(int count) {
    return next(count, 10, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of characters which match the POSIX [:print:] regular
   * expression character class. This class includes all visible ASCII characters and spaces (i.e.
   * anything except control characters). </p>.
   *
   * <p>Characters will be chosen from the set of characters whose ASCII value is between {@code 32}
   * and {@code 125} (inclusive). </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextPrint(int count) {
    return next(count, ASCII.length - 1, ASCII);
  }

  /**
   * <p>Creates a random string whose length is the number of characters specified. </p> <p>
   * Characters will be chosen from the set of upper case characters (A-Z). </p>
   *
   * @param count the length of random string to create
   * @return the random string
   */
  public String nextUpper(int count) {
    return next(count, START_UPPER_CASE, START_LOWER_CASE - START_UPPER_CASE, ASCII);
  }

  /**
   * Creates a random string whose length is the number of characters specified from the range of
   * the printable ASCII table.
   *
   * @param count the length of random string to create
   * @param start the start index in the table
   * @param range the range in the table
   * @return the random string
   */
  private String next(int count, int range, char[] table) {
    if (count == 0) {
      return "";
    }
    if (count < 0) {
      throw new IllegalArgumentException(
          "Requested random string length " + count + " is less than 0.");
    }
    final char[] chars = new char[count];
    for (int i = 0; i < count; i++) {
      chars[i] = table[rng.nextInt(range)];
    }
    return new String(chars);
  }

  /**
   * Creates a random string whose length is the number of characters specified from the range of
   * the printable ASCII table.
   *
   * @param count the length of random string to create
   * @param start the start index in the table
   * @param range the range in the table
   * @return the random string
   */
  private String next(int count, int start, int range, char[] table) {
    if (count == 0) {
      return "";
    }
    if (count < 0) {
      throw new IllegalArgumentException(
          "Requested random string length " + count + " is less than 0.");
    }
    final char[] chars = new char[count];
    for (int i = 0; i < count; i++) {
      chars[i] = table[rng.nextInt(range) + start];
    }
    return new String(chars);
  }
}
