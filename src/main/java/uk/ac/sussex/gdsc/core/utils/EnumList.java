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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A helper for working with all elements of an enum in their declared order.
 *
 * @param <E> the enum element type
 * @since 2.0
 */
public final class EnumList<E extends Enum<E>> implements Iterable<E> {

  /** The values of the enum, in the order they are declared. */
  private final E[] values;

  /**
   * Instantiates a new enum helper.
   *
   * @param elementType the element type
   */
  private EnumList(Class<E> elementType) {
    this.values = elementType.getEnumConstants();
  }

  /**
   * Creates a new Enum helper for the given enumeration.
   *
   * @param <E> the enum element type
   * @param elementType the element type
   * @return the Enum helper
   */
  public static <E extends Enum<E>> EnumList<E> forEnum(Class<E> elementType) {
    ValidationUtils.checkNotNull(elementType, "Enum type must not be null");
    return new EnumList<>(elementType);
  }

  /**
   * Creates a new Enum helper for the enumeration of {@code element}.
   *
   * @param <E> the enum element type
   * @param element the element
   * @return the Enum helper
   */
  public static <E extends Enum<E>> EnumList<E> forEnum(E element) {
    ValidationUtils.checkNotNull(element, "Enum must not be null");
    return new EnumList<>(element.getDeclaringClass());
  }

  /**
   * Get the number of values in the enum.
   *
   * @return the size
   */
  public int size() {
    return values.length;
  }

  /**
   * Get the values in the enum as an array.
   *
   * @return the values
   */
  public E[] toArray() {
    return values.clone();
  }

  /**
   * Get the enumeration value for the ordinal.
   *
   * @param ordinal the ordinal
   * @return the enumeration value
   * @throws IllegalArgumentException If the ordinal is out of the range of the enum
   */
  public E get(int ordinal) {
    ValidationUtils.checkArgument(ordinal >= 0, "Ordinal %d must be positive", ordinal);
    ValidationUtils.checkArgument(ordinal < values.length,
        "Ordinal %d must be less than the max %d", ordinal, values.length);
    return values[ordinal];
  }

  /**
   * Get the enumeration value for the ordinal or a default.
   *
   * <p>If the ordinal is out of the range of the enum the default value is returned.
   *
   * @param ordinal the ordinal
   * @param defaultValue the default value (can be null)
   * @return the enumeration value
   */
  public E getOrDefault(int ordinal, E defaultValue) {
    return (ordinal < 0 || ordinal >= values.length) ? defaultValue : values[ordinal];
  }

  /**
   * Get the enumeration value for the ordinal.
   *
   * <p>If the ordinal is out of the range of the enum the first declared value in the enum is
   * returned.
   *
   * <p>If the enum has no declared values then there is no valid return value and null is returned.
   *
   * @param ordinal the ordinal
   * @return the enumeration value
   */
  public E getOrFirst(int ordinal) {
    if ((ordinal < 0 || ordinal >= values.length)) {
      return (size() == 0) ? null : values[0];
    }
    return values[ordinal];
  }

  /**
   * {@inheritDoc}
   *
   * <p>Since the list is unmodifiable the iterator will throw an
   * {@link UnsupportedOperationException} on {@link Iterator#remove()}.
   */
  @Override
  public Iterator<E> iterator() {
    return new Itr();
  }

  /**
   * A simple iterator over the values.
   */
  private class Itr implements Iterator<E> {
    /** index of next element to return. */
    int cursor;

    @Override
    public boolean hasNext() {
      return cursor != size();
    }

    @Override
    public E next() {
      final int i = cursor;
      if (i >= size()) {
        throw new NoSuchElementException();
      }
      cursor = i + 1;
      return values[i];
    }

    /**
     * This is not supported as the list is unmodifiable.
     *
     * @throws UnsupportedOperationException This is not supported
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("List is not modifiable");
    }
  }
}
