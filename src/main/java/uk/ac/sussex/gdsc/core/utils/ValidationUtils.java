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

import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Class for validating conditions.
 *
 * <p>Overloaded methods are provided for common argument types to avoid the performance overhead of
 * autoboxing and varargs array creation.
 */
public final class ValidationUtils {

  private static final String VALUE = "Value";
  private static final String MSG_IS_NOT_POSITIVE = " is not positive: ";
  private static final String MSG_IS_NOT_STRICTLY_POSITIVE = " is not strictly positive: ";

  /** No public construction. */
  private ValidationUtils() {}

  /**
   * Return the specified object if it is not {@code null}, otherwise the default value.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param defaultValue the default value (must not be null)
   * @return {@code object} if not {@code null}, otherwise the default value
   * @throws NullPointerException if {@code defaultValue} is {@code null}
   */
  public static <T> T defaultIfNull(T object, T defaultValue) {
    return (object == null) ? checkNotNull(defaultValue, "Default value must not be null") : object;
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * @param result the result
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result) {
    if (!result) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using {@link Supplier#get()}.
   *
   * @param result the result
   * @param message the supplier of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, Supplier<String> message) {
    if (!result) {
      throw new IllegalArgumentException(message.get());
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using {@link String#valueOf(Object)}.
   *
   * @param result the result
   * @param message the object used to form the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, Object message) {
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
   * @param p1 the first argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, byte p1) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, int p1) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, long p1) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, float p1) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, double p1) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, Object p1) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, byte p1, byte p2) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, int p1, int p2) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, long p1, long p2) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, float p1, float p2) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, double p1, double p2) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalArgumentException if not {@code true}
   */
  public static void checkArgument(boolean result, String format, Object p1, Object p2) {
    if (!result) {
      throw new IllegalArgumentException(String.format(format, p1, p2));
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * @param result the result
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result) {
    if (!result) {
      throw new IllegalStateException();
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using {@link Supplier#get()}.
   *
   * @param result the result
   * @param message the supplier of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, Supplier<String> message) {
    if (!result) {
      throw new IllegalStateException(message.get());
    }
  }

  /**
   * Check the {@code result} is {@code true}.
   *
   * <p>If not {@code true} the exception message is formed using {@link String#valueOf(Object)}.
   *
   * @param result the result
   * @param message the object used to form the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, Object message) {
    if (!result) {
      throw new IllegalStateException(String.valueOf(message));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, byte p1) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, int p1) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, long p1) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, float p1) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, double p1) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, Object p1) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, byte p1, byte p2) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, int p1, int p2) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, long p1, long p2) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, float p1, float p2) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, double p1, double p2) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1, p2));
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
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @throws IllegalStateException if not {@code true}
   */
  public static void checkState(boolean result, String format, Object p1, Object p2) {
    if (!result) {
      throw new IllegalStateException(String.format(format, p1, p2));
    }
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object) {
    if (object == null) {
      throw new NullPointerException();
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using {@link Supplier#get()}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param message the supplier of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, Supplier<String> message) {
    if (object == null) {
      throw new NullPointerException(message.get());
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using {@link String#valueOf(Object)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param message the object used to form the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, Object message) {
    if (object == null) {
      throw new NullPointerException(String.valueOf(message));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, byte p1) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, int p1) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, long p1) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, float p1) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, double p1) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, Object p1) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, byte p1, byte p2) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1, p2));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, int p1, int p2) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1, p2));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, long p1, long p2) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1, p2));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, float p1, float p2) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1, p2));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, double p1, double p2) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1, p2));
    }
    return object;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   *
   * <p>If not {@code true} the exception message is formed using
   * {@link String#format(String, Object...)}.
   *
   * @param <T> the type of the reference
   * @param object the object reference to check for nullity
   * @param format the format of the exception message
   * @param p1 the first argument of the exception message
   * @param p2 the second argument of the exception message
   * @return {@code object} if not {@code null}
   * @throws NullPointerException if {@code object} is {@code null}
   */
  public static <T> T checkNotNull(T object, String format, Object p1, Object p2) {
    if (object == null) {
      throw new NullPointerException(String.format(format, p1, p2));
    }
    return object;
  }



  /**
   * Check the specified value is positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static int checkPositive(int value) {
    return checkPositive(value, VALUE);
  }

  /**
   * Check the specified value is positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static int checkPositive(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static long checkPositive(long value) {
    return checkPositive(value, VALUE);
  }

  /**
   * Check the specified value is positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static long checkPositive(long value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is positive. The value is allowed to be infinite but not NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static float checkPositive(float value) {
    return checkPositive(value, VALUE);
  }

  /**
   * Check the specified value is positive. The value is allowed to be infinite but not NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static float checkPositive(float value, String name) {
    if (value < 0 || Float.isNaN(value)) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is positive. The value is allowed to be infinite but not NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static double checkPositive(double value) {
    return checkPositive(value, VALUE);
  }

  /**
   * Check the specified value is positive. The value is allowed to be infinite but not NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >=0}
   */
  public static double checkPositive(double value, String name) {
    if (value < 0 || Double.isNaN(value)) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is strictly positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static int checkStrictlyPositive(int value) {
    return checkStrictlyPositive(value, VALUE);
  }

  /**
   * Check the specified value is strictly positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static int checkStrictlyPositive(int value, String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_STRICTLY_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is strictly positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static long checkStrictlyPositive(long value) {
    return checkStrictlyPositive(value, VALUE);
  }

  /**
   * Check the specified value is strictly positive.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static long checkStrictlyPositive(long value, String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_STRICTLY_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is strictly positive. The value is allowed to be infinite but not
   * NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static float checkStrictlyPositive(float value) {
    return checkStrictlyPositive(value, VALUE);
  }

  /**
   * Check the specified value is strictly positive. The value is allowed to be infinite but not
   * NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static float checkStrictlyPositive(float value, String name) {
    if (value <= 0 || Float.isNaN(value)) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_STRICTLY_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified value is strictly positive. The value is allowed to be infinite but not
   * NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Value is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static double checkStrictlyPositive(double value) {
    return checkStrictlyPositive(value, VALUE);
  }

  /**
   * Check the specified value is strictly positive. The value is allowed to be infinite but not
   * NaN.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " is not strictly positive: " + value
   * </pre>
   *
   * @param value the value
   * @param name the name of the value
   * @return the value
   * @throws IllegalArgumentException if {@code value} is not {@code >0}
   */
  public static double checkStrictlyPositive(double value, String name) {
    if (value <= 0 || Double.isNaN(value)) {
      throw new IllegalArgumentException(name + MSG_IS_NOT_STRICTLY_POSITIVE + value);
    }
    return value;
  }

  /**
   * Check the specified array has a non-zero length.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * "Array length is zero"
   * </pre>
   *
   * @param array the array
   * @throws IllegalArgumentException if the object is not an array, is null or zero length
   */
  public static void checkArrayLength(Object array) {
    checkArrayLength(array, "Array");
  }

  /**
   * Check the specified array has a non-zero length.
   *
   * <p>An error message is constructed using:
   *
   * <pre>
   * name + " length is zero"
   * </pre>
   *
   * @param array the array
   * @param name the name of the array
   * @throws IllegalArgumentException if the object is not an array, is null or zero length
   */
  public static void checkArrayLength(Object array, String name) {
    if (ArrayUtils.getLength(array) == 0) {
      throw new IllegalArgumentException(name + " length is zero");
    }
  }

  /**
   * Check the index is valid for the array. The index must be within the range zero, inclusive, to
   * {@code size}, exclusive.
   *
   * @param <T> the array type
   * @param index the index
   * @param array the array
   * @return the index
   * @throws NullPointerException if the array is {@code null}
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public static <T> int checkIndex(int index, T[] array) {
    return checkIndex(index, array, "index");
  }

  /**
   * Check the index is valid for the array. The index must be within the range zero, inclusive, to
   * {@code size}, exclusive.
   *
   * @param <T> the array type
   * @param index the index
   * @param array the array
   * @param name the name of the index used in the error message
   * @return the index
   * @throws NullPointerException if the array is {@code null}
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public static <T> int checkIndex(int index, T[] array, String name) {
    return checkIndex(index, checkNotNull(array, "Validated array is null").length, name);
  }

  /**
   * Check the index is valid for the array size. The index must be within the range zero,
   * inclusive, to {@code size}, exclusive.
   *
   * @param index the index
   * @param size the size
   * @return the index
   * @throws IndexOutOfBoundsException if the index is invalid
   * @throws IllegalArgumentException if the size is negative
   */
  public static int checkIndex(int index, int size) {
    return checkIndex(index, size, "index");
  }

  /**
   * Check the index is valid for the array size. The index must be within the range zero,
   * inclusive, to {@code size}, exclusive.
   *
   * @param index the index
   * @param size the size
   * @param name the name of the index used in the error message
   * @return the index
   * @throws IndexOutOfBoundsException if the index is invalid
   * @throws IllegalArgumentException if the size is negative
   */
  public static int checkIndex(int index, int size, String name) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(getIndexMessage(index, size, name));
    }
    return index;
  }

  /**
   * Gets the message for a bad array index. This method assumes that the following is true:
   * {@code index < 0 || index >= size}.
   *
   * @param index the index
   * @param size the size
   * @param name the name
   * @return the index message
   */
  private static String getIndexMessage(int index, int size, String name) {
    if (index < 0) {
      return String.format("%s (%d) must not be negative", name, index);
    }
    if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    }
    // index >= size
    return String.format("%s (%d) must be less than size (%d)", name, index, size);
  }
}
