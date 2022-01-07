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

package uk.ac.sussex.gdsc.core.match;

/**
 * Stores a 2D/3D point with a start and end time. Allows scoring the match between two fluorophore
 * pulses.
 */
public class Pulse extends BasePoint {
  /** The start. */
  private final int start;
  /** The end. */
  private final int end;

  /**
   * Instantiates a new pulse.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   * @param start the start
   * @param end the end
   */
  public Pulse(float x, float y, float z, int start, int end) {
    super(x, y, z);
    this.start = start;
    this.end = end;
  }

  /**
   * Instantiates a new pulse.
   *
   * @param x the x
   * @param y the y
   * @param start the start
   * @param end the end
   */
  public Pulse(float x, float y, int start, int end) {
    super(x, y);
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    // Must be the same class, allowing subtypes their own implementation
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    // cast to native object is now safe
    final Pulse that = (Pulse) object;

    return x == that.x && y == that.y && z == that.z && start == that.start && end == that.end;
  }

  @Override
  public int hashCode() {
    // Note: floatToRawIntBits does not unify all possible NaN values
    // However since the equals() will fail for NaN values we are not
    // breaking the java contract.
    return (41 * (41 * (41 * (41 * (41 + Float.floatToRawIntBits(x)) + Float.floatToRawIntBits(y))
        + Float.floatToRawIntBits(z)) + start) + end);
  }

  /**
   * Calculate the score for the match to the other pulse. The score computes the number of matching
   * on frames (the overlap) and multiplies it by the distance weighted score (from 1 to zero). The
   * threshold distance is the squared distance at which the score will be 0.5;
   *
   * @param other the other
   * @param d2 The squared distance between the two coordinates
   * @param dt The squared distance threshold
   * @return the score
   */
  public double score(final Pulse other, final double d2, final double dt) {
    final int overlap = calculateOverlap(other);
    return overlap * (1 / (1 + d2 / dt));
  }

  /**
   * Calculate the score for the match to the other pulse. The score computes the number of matching
   * on frames (the overlap) and multiplies it by the distance weighted score (from 1 to zero). The
   * threshold distance is the squared distance at which the score will be 0.5;
   *
   * @param other the other
   * @param dt The squared distance threshold
   * @return the score
   */
  public double score(final Pulse other, final double dt) {
    final double d2 = distanceXyzSquared(other);
    return score(other, d2, dt);
  }

  /**
   * Calculate the number of overlapping frames using the start and end times.
   *
   * @param that The other pulse
   * @return the number of frames
   */
  public int calculateOverlap(final Pulse that) {
    //@formatter:off
    // --------------
    //                ===========
    // or
    // ============
    //               ------------
    //@formatter:on
    if (this.end < that.start || that.end < this.start) {
      return 0;
    }

    //@formatter:off
    // ---------------------
    //         ==================
    // or
    // --------------------------------
    //         ==================
    // or
    //         ------------------
    // =================================
    // or
    //         ---------------------
    // ==================
    //@formatter:on
    final int overlapStart = (this.start < that.start) ? that.start : this.start;
    final int overlapEnd = (this.end < that.end) ? this.end : that.end;
    return overlapEnd - overlapStart + 1;
  }

  /**
   * Gets the start frame.
   *
   * @return the start frame.
   */
  public int getStart() {
    return start;
  }

  /**
   * Gets the end frame.
   *
   * @return the end frame.
   */
  public int getEnd() {
    return end;
  }
}
