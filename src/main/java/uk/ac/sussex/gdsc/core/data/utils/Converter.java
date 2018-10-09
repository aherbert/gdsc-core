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
package uk.ac.sussex.gdsc.core.data.utils;

/**
 * Define conversion of a value
 */
public interface Converter {
  /**
   * Convert the value.
   *
   * @param value the value
   * @return the new value
   */
  public double convert(double value);

  /**
   * Convert the value.
   *
   * @param value the value
   * @return the new value
   */
  public float convert(float value);

  /**
   * Convert the value back. This performs the opposite of {@link #convert(double)}.
   *
   * @param value the value
   * @return the new value
   */
  public double convertBack(double value);

  /**
   * Convert the value back. This performs the opposite of {@link #convert(float)}.
   *
   * @param value the value
   * @return the new value
   */
  public float convertBack(float value);

  /**
   * Gets the conversion function, f(x). The function should represent what conversion is performed
   * on the function input value x.
   *
   * @return the function
   */
  public String getFunction();
}
