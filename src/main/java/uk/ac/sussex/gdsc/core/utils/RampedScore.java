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

/**
 * Provide a score function that ramps smoothly between the configured limits.
 */
public class RampedScore {
  /** The lower end of the range. */
  public final double lower;

  /** The upper end of the range. */
  public final double upper;
  private final double range;

  /**
   * Create the score function with the specified limits.
   *
   * @param lower The lower end of the range
   * @param upper The upper end of the range
   */
  public RampedScore(double lower, double upper) {
    ValidationUtils.checkArgument(lower <= upper, "Lower (%f) must be below upper (%f)", lower,
        upper);
    this.upper = upper;
    this.lower = lower;
    this.range = upper - lower;
  }

  /**
   * Instantiates a new ramped score.
   *
   * @param source the source
   */
  private RampedScore(RampedScore source) {
    this.upper = source.upper;
    this.lower = source.lower;
    this.range = source.range;
  }

  /**
   * Provide a score between 0 and 1 for the value. Return 1 if below the lower limit, 0 if above
   * the upper limit, otherwise ramp smoothly from 1 to 0.
   *
   * @param value the value
   * @return the score
   */
  public double score(double value) {
    if (value > upper) {
      return 0.0;
    }
    if (value <= lower) {
      return 1.0;
    }
    // Interpolate from the minimum to the maximum match distance:
    // Cosine
    return (0.5 * (1 + Math.cos(((value - lower) / range) * Math.PI)));
  }

  /**
   * Provide a score between 0 and 1 for the value. Return 1 if below the lower limit, 0 if above
   * the upper limit, otherwise ramp smoothly from 1 to 0. Flatten the score to a new score that
   * will have a maximum number of steps between 0 and 1.
   *
   * @param value the value
   * @param steps the steps
   * @return the score
   */
  public double scoreAndFlatten(double value, int steps) {
    return flatten(score(value), steps);
  }

  /**
   * Flatten the score to a new score that will have a maximum number of steps between 0 and 1.
   *
   * @param score the score
   * @param steps the steps
   * @return The new score
   */
  public static double flatten(double score, int steps) {
    return (Math.round(score * steps)) / (double) steps;
  }

  /**
   * Flatten the score to a new score that will have a maximum number of steps between 0 and 1.
   *
   * @param score the score
   * @param steps the steps
   * @return The new score
   */
  public static float flatten(float score, int steps) {
    return (Math.round(score * steps)) / (float) steps;
  }

  /**
   * Create a copy.
   *
   * @return the copy
   */
  public RampedScore copy() {
    return new RampedScore(this);
  }
}
