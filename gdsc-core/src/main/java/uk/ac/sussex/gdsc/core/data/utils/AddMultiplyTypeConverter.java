/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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
 * Perform conversion by addition then multiplication.
 *
 * @param <T> the generic type
 */
public class AddMultiplyTypeConverter<T> extends MultiplyTypeConverter<T> {
  /** The addition. */
  private final double addition;

  /**
   * Instantiates a new add then multiplication unit converter.
   *
   * @param from unit to convert from
   * @param to unit to convert to
   * @param addition the value to add before multiplication
   * @param multiplication the multiplication
   * @throws ConversionException If the input units are null
   * @throws ConversionException If the multiplication is not finite
   * @throws ConversionException If the addition is not finite
   */
  public AddMultiplyTypeConverter(T from, T to, double addition, double multiplication) {
    super(from, to, multiplication);
    if (!Double.isFinite(addition)) {
      throw new ConversionException("addition must be finite");
    }
    this.addition = addition;
  }

  @Override
  public double convert(double value) {
    return (value + addition) * multiplication;
  }

  @Override
  public double convertBack(double value) {
    return (value / multiplication) - addition;
  }

  @Override
  public String getFunction() {
    return "(x + " + addition + ") * " + multiplication;
  }
}
