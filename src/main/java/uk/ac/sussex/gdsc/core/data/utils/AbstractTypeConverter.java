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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.data.utils;

/**
 * Base class for converters.
 *
 * @param <T> the generic type
 */
public abstract class AbstractTypeConverter<T> implements TypeConverter<T> {

  /** The unit to convert from. */
  private final T fromUnit;

  /** The unit to convert to. */
  private final T toUnit;

  /**
   * Instantiates a new abstract unit converter.
   *
   * @param from unit to convert from
   * @param to unit to convert to
   * @throws ConversionException If the input units are null
   */
  public AbstractTypeConverter(T from, T to) {
    if (from == null) {
      throw new ConversionException("From unit is null");
    }
    if (to == null) {
      throw new ConversionException("To unit is null");
    }
    this.fromUnit = from;
    this.toUnit = to;
  }

  /**
   * Instantiates a new abstract unit converter.
   *
   * @param from unit to convert from
   * @param to unit to convert to
   * @param suppressExceptions the suppress exceptions flag
   * @throws ConversionException If the input units are null (and exception are not suppressed)
   */
  AbstractTypeConverter(T from, T to, boolean suppressExceptions) {
    if (from == null && !suppressExceptions) {
      throw new ConversionException("From unit is null");
    }
    if (to == null && !suppressExceptions) {
      throw new ConversionException("To unit is null");
    }
    this.fromUnit = from;
    this.toUnit = to;
  }

  @Override
  public float convert(float value) {
    return (float) convert((double) value);
  }

  @Override
  public float convertBack(float value) {
    return (float) convertBack((double) value);
  }

  @Override
  public T from() {
    return fromUnit;
  }

  @Override
  public T to() {
    return toUnit;
  }

  @Override
  public String toString() {
    return to() + " = f(x=" + from() + ") = " + getFunction();
  }
}
