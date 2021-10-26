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

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Class for rounding.
 */
public class MathContextRounder implements Rounder {
  /** The math context. */
  private final MathContext mathContext;

  /**
   * Instantiates a new math context rounder.
   *
   * @param mathContext the math context
   * @throws NullPointerException if the mathContext is null
   */
  public MathContextRounder(MathContext mathContext) {
    if (mathContext == null) {
      throw new NullPointerException("MathContext must not be null");
    }
    this.mathContext = mathContext;
  }

  /**
   * Instantiates a new math context rounder.
   *
   * @param precision The non-negative {@code int} precision setting.
   * @throws IllegalArgumentException if the {@code precision} parameter is less than zero.
   */
  public MathContextRounder(int precision) {
    mathContext = new MathContext(precision);
  }

  @Override
  public double round(double value) {
    if (Double.isFinite(value)) {
      return BigDecimal.valueOf(value).round(mathContext).doubleValue();
    }
    return value; // NaN or infinite
  }

  @Override
  public float round(float value) {
    if (Float.isFinite(value)) {
      return BigDecimal.valueOf(value).round(mathContext).floatValue();
    }
    return value; // NaN or infinite
  }

  @Override
  public String toString(double value) {
    if (Double.isFinite(value)) {
      return BigDecimal.valueOf(value).round(mathContext).toString();
    }
    if (value == Double.POSITIVE_INFINITY) {
      return "Infinity";
    }
    if (value == Double.NEGATIVE_INFINITY) {
      return "-Infinity";
    }
    return "NaN";
  }

  @Override
  public String toString(float value) {
    if (Float.isFinite(value)) {
      return BigDecimal.valueOf(value).round(mathContext).toString();
    }
    if (value == Float.POSITIVE_INFINITY) {
      return "Infinity";
    }
    if (value == Float.NEGATIVE_INFINITY) {
      return "-Infinity";
    }
    return "NaN";
  }
}
