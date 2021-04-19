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
 *
 * <p>Uses the smoother step function with zero 1st- and 2nd-order derivatives at x=0 and x=1.
 *
 * <pre>
 * x = (value - edge0) / (edge1 - edge0)
 *
 * smotherstep(x) = 0                     when x &leq; 0
 *                  6x^5 - 15x^4 + 10x^3  when 0 &lt; x &lt; 1
 *                  1                     when x &geq; 1
 * </pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Smoothstep">Smoother step (Wikipedia)</a>
 */
public class RampedScore {
  /** The edge of the range assigned a score of zero. */
  public final double edge0;

  /** The edge of the range assigned a score of one. */
  public final double edge1;

  /** The range between edge0 and edge1. */
  private final double range;

  private static class Low1ThresholdScore extends RampedScore {
    Low1ThresholdScore(double threshold) {
      super(threshold, threshold);
    }

    @Override
    public double score(double value) {
      return value <= edge0 ? 1.0 : 0.0;
    }
  }

  private static class Low0ThresholdScore extends RampedScore {
    Low0ThresholdScore(double threshold) {
      super(threshold, threshold);
    }

    @Override
    public double score(double value) {
      return value <= edge0 ? 0.0 : 1.0;
    }
  }

  /**
   * Create the score function with the specified limits. Note: The range can be low to high or high
   * to low.
   *
   * @param edge0 The edge of the range assigned a score of zero
   * @param edge1 The edge of the range assigned a score of one
   */
  private RampedScore(double edge0, double edge1) {
    this.edge1 = edge1;
    this.edge0 = edge0;
    this.range = edge1 - edge0;
  }

  /**
   * Instantiates a new ramped score.
   *
   * @param source the source
   */
  private RampedScore(RampedScore source) {
    this.edge1 = source.edge1;
    this.edge0 = source.edge0;
    this.range = source.range;
  }

  /**
   * Create the score function with the specified limits. Note: The range can be low to high or high
   * to low.
   *
   * <p>If there is no range then an exception is thrown.
   *
   * @param edge0 The edge of the range assigned a score of zero
   * @param edge1 The edge of the range assigned a score of one
   * @return the ramped score
   * @throws IllegalArgumentException if there is no range
   */
  public static RampedScore of(double edge0, double edge1) {
    ValidationUtils.checkArgument(edge0 != edge1, "Edge0 (%f) must not equal edge1 (%f)", edge0,
        edge1);
    return new RampedScore(edge0, edge1);
  }

  /**
   * Create the score function with the specified limits. Note: The range can be low to high or high
   * to low.
   *
   * <p>If there is no range then the ramp does not exist. This function will safely return an
   * object that sets the score as 0 or 1 if the value is less than or equal to the edge0 range. The
   * behaviour is configured based on the {@code noRangeLowIsZero} parameter.
   *
   * @param edge0 The edge of the range assigned a score of zero
   * @param edge1 The edge of the range assigned a score of one
   * @param noRangeLowIsZero Set to true to return 0 for any value below {@code edge0} when
   *        {@code edge0 == edge1}, otherwise return 1
   * @return the ramped score
   */
  public static RampedScore of(double edge0, double edge1, boolean noRangeLowIsZero) {
    if (edge0 == edge1) {
      return noRangeLowIsZero ? new Low0ThresholdScore(edge0) : new Low1ThresholdScore(edge0);
    }
    return new RampedScore(edge0, edge1);
  }

  /**
   * Provide a score between 0 and 1 for the value. Return 0 if at or outside edge zero, 1 if at or
   * outside edge one, otherwise ramp smoothly from 0 to 1.
   *
   * @param value the value
   * @return the score
   */
  public double score(double value) {
    // Smoother step
    // Scale to interval 0..1
    final double x = (value - edge0) / range;
    if (x <= 0.0) {
      return 0.0;
    } else if (x >= 1) {
      return 1.0;
    }
    // Evaluate polynomial
    return x * x * x * (x * (x * 6 - 15) + 10);
  }

  /**
   * Provide a score between 0 and 1 for the value. Return 0 if at or outside edge zero, 1 if at or
   * outside edge one, otherwise ramp smoothly from 0 to 1. Flatten the score to a new score that
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
