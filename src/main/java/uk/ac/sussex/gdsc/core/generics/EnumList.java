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

package uk.ac.sussex.gdsc.core.generics;

import uk.ac.sussex.gdsc.core.utils.ArgumentUtils;

/**
 * A helper for working with all elements of an enum in their declared order.
 *
 * @param <E> the enum element type
 * @since 2.0
 */
public class EnumList<E extends Enum<E>> {

  /** The values of the enum, in the order they are declared. */
  private final E[] values;

  /**
   * Instantiates a new enum helper.
   *
   * @param values the values
   */
  private EnumList(E[] values) {
    this.values = values;
  }

  /**
   * Creates a new Enum helper for the given enumeration.
   *
   * @param elementType the element type
   * @return the Enum helper
   */
  public EnumList<E> forEnum(Class<E> elementType) {
    ArgumentUtils.checkNotNull(elementType, "Enum type must not be null");
    return new EnumList<>(elementType.getEnumConstants());
  }

  /**
   * Creates a new Enum helper for the enumeration of {@code element}.
   *
   * @param element the element
   * @return the Enum helper
   */
  public EnumList<E> forEnum(E element) {
    ArgumentUtils.checkNotNull(element, "Enum must not be null");
    return new EnumList<>(element.getDeclaringClass().getEnumConstants());
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
    ArgumentUtils.checkCondition(ordinal >= 0, "Ordinal %d must be positive", ordinal);
    ArgumentUtils.checkCondition(ordinal < values.length, "Ordinal %d must be less than the max %d",
        ordinal, values.length);
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
   * @param ordinal the ordinal
   * @return the enumeration value
   */
  public E getOrFirst(int ordinal) {
    return getOrDefault(ordinal, values[0]);
  }
}
