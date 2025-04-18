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
 * Define conversion of a type.
 *
 * @param <T> the generic type
 */
public interface TypeConverter<T> extends Converter {
  /**
   * {@inheritDoc}
   *
   * <p>The input value is in the {@link #from() from} unit and the output is in the {@link #to()
   * to} unit.
   *
   * @param value the value
   * @return the new value
   * @see #from()
   * @see #to()
   */
  @Override
  double convert(double value);

  /**
   * {@inheritDoc}
   *
   * <p>The input value is in the {@link #from() from} unit and the output is in the {@link #to()
   * to} unit.
   *
   * @param value the value
   * @return the new value
   * @see #from()
   * @see #to()
   */
  @Override
  float convert(float value);

  /**
   * {@inheritDoc}
   *
   * <p>The input value is in the {@link #to() to} unit and the output is in the {@link #from()
   * from} unit.
   *
   * @param value the value
   * @return the new value
   * @see #to()
   * @see #from()
   */
  @Override
  double convertBack(double value);

  /**
   * {@inheritDoc}
   *
   * <p>The input value is in the {@link #to() to} unit and the output is in the {@link #from()
   * from} unit.
   *
   * @param value the value
   * @return the new value
   * @see #to()
   * @see #from()
   */
  @Override
  float convertBack(float value);

  /**
   * Specify the source unit to be converted from.
   *
   * @return the source unit
   */
  T from();

  /**
   * Specify the destination unit to be converted to.
   *
   * @return the destination unit
   */
  T to();
}
