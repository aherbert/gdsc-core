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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


/**
 * Compares primitives for equality
 */
public class Equality {
  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(boolean value1, boolean value2) {
    return value1 == value2;
  }

  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(byte value1, byte value2) {
    return value1 == value2;
  }

  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(short value1, short value2) {
    return value1 == value2;
  }

  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(int value1, int value2) {
    return value1 == value2;
  }

  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(long value1, long value2) {
    return value1 == value2;
  }

  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(float value1, float value2) {
    if (value1 == value2) {
      return true;
    }
    // Possible NaN
    if (value1 != value1) {
      return (value2 != value2);
    }
    return false;
  }

  /**
   * Test if the two values are equal.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if equal
   */
  public static boolean areEqual(double value1, double value2) {
    if (value1 == value2) {
      return true;
    }
    // Possible NaN
    if (value1 != value1) {
      return (value2 != value2);
    }
    return false;
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(boolean value1, boolean value2) {
    return (value1 == value2) ? 0 : (value1 ? 1 : -1);
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(byte value1, byte value2) {
    return value1 - value2;
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(short value1, short value2) {
    return value1 - value2;
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(int value1, int value2) {
    return (value1 < value2) ? -1 : ((value1 == value2) ? 0 : 1);
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(long value1, long value2) {
    return (value1 < value2) ? -1 : ((value1 == value2) ? 0 : 1);
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(float value1, float value2) {
    return Float.compare(value1, value2);
  }

  /**
   * Compares two values numerically.
   *
   * @param value1 the first value to compare
   * @param value2 the second value to compare
   * @return the value {@code 0} if {@code f1} is numerically equal to {@code f2}; a value less than
   *         {@code 0} if {@code f1} is numerically less than {@code f2}; and a value greater than
   *         {@code 0} if {@code f1} is numerically greater than {@code f2}.
   */
  public static int compare(double value1, double value2) {
    return Double.compare(value1, value2);
  }
}
