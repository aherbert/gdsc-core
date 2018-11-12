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
 * Class for validating arguments.
 */
public final class ArgumentUtils {

  /** No public construction. */
  private ArgumentUtils() {}

  /**
   * Check the {@code result} is {@code true}.
   *
   * @param result the result
   * @throws IllegalArgumentException If not {@code true}
   */
  public static void checkCondition(final boolean result) {
    if (!result) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using {@link String#valueOf(Object)}.
   *
   * @param result the result
   * @param message the object used to form the exception message
   * @throws IllegalArgumentException If not {@code true}
   */
  public static void checkCondition(final boolean result, Object message) {
    if (!result) {
      throw new IllegalArgumentException(String.valueOf(message));
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param result the result
   * @param format the format of the exception message
   * @param args the arguments of the exception message
   * @throws IllegalArgumentException If not {@code true}
   */
  public static void checkCondition(final boolean result, String format, Object... args) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, args));
    }
  }

  /**
   * Check the {@code state} is {@code true}.
   *
   * @param state the state
   * @throws IllegalStateException If not {@code true}
   */
  public static void checkState(final boolean state) {
    if (!state) {
      throw new IllegalStateException();
    }
  }

  /**
   * Check the {@code state} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using {@link String#valueOf(Object)}.
   *
   * @param state the state
   * @param message the object used to form the exception message
   * @throws IllegalStateException If not {@code true}
   */
  public static void checkState(final boolean state, Object message) {
    if (!state) {
      throw new IllegalStateException(String.valueOf(message));
    }
  }

  /**
   * Check the {@code state} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param state the state
   * @param format the format of the exception message
   * @param args the arguments of the exception message
   * @throws IllegalStateException If not {@code true}
   */
  public static void checkState(final boolean state, String format, Object... args) {
    if (!state) {
      throw new IllegalStateException(String.format(format, args));
    }
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not true the exception message is formed using {@link String#valueOf(Object)}.
   *
   * @param obj the object reference to check for nullity
   * @param <T> the type of the reference
   * @return {@code obj} if not {@code null}
   * @throws NullPointerException if {@code obj} is {@code null}
   */
  public static <T> T checkNotNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * @param <T> the type of the reference
   * @param obj the object reference to check for nullity
   * @param message the object used to form the exception message
   * @return {@code obj} if not {@code null}
   * @throws NullPointerException if {@code obj} is {@code null}
   */
  public static <T> T checkNotNull(T obj, Object message) {
    if (obj == null) {
      throw new NullPointerException(String.valueOf(message));
    }
    return obj;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not true the exception message is formed using {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param obj the object reference to check for nullity
   * @param format the format of the exception message
   * @param args the arguments of the exception message
   * @return {@code obj} if not {@code null}
   * @throws NullPointerException if {@code obj} is {@code null}
   */
  public static <T> T checkNotNull(T obj, String format, Object... args) {
    if (obj == null) {
      throw new NullPointerException(String.format(format, args));
    }
    return obj;
  }
}
