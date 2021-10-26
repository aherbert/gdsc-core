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

package uk.ac.sussex.gdsc.core.match;

// @formatter:off
/**
 * Class to store the result of a binary scoring analysis between two sets A and B. Requires the
 * size of the intersect between sets and the size of the unmatched regions to be known.
 *
 * <ul>
 * <li>|A &#8745; B|
 * <li>|A| - |A &#8745; B|
 * <li>|B| - |A &#8745; B|
 * </ul>
 */
//@formatter:on
public class IntersectionResult {

  /** Size of the intersection. */
  private final int intersection;

  /** Size A minus intersection. */
  private final int sizeAMinusIntersection;

  /** Size B minus intersection. */
  private final int sizeBMinusIntersection;

  /**
   * Create a new instance.
   *
   * @param intersection The size of the intersection (|A &#8745; B|)
   * @param sizeAMinusIntersection The size of A excluding the intersection (|A| - |A &#8745; B|)
   * @param sizeBMinusIntersection The size of B excluding the intersection (|B| - |A &#8745; B|)
   */
  public IntersectionResult(int intersection, int sizeAMinusIntersection,
      int sizeBMinusIntersection) {
    this.intersection = intersection;
    this.sizeAMinusIntersection = sizeAMinusIntersection;
    this.sizeBMinusIntersection = sizeBMinusIntersection;
  }

  /**
   * Gets the size |A|.
   *
   * @return the size |A|
   */
  public int getSizeA() {
    return intersection + sizeAMinusIntersection;
  }

  /**
   * Gets the size |B|.
   *
   * @return the size |B|
   */
  public int getSizeB() {
    return intersection + sizeBMinusIntersection;
  }

  /**
   * Gets the intersection (|A &#8745; B|).
   *
   * @return the intersection
   */
  public int getIntersection() {
    return intersection;
  }

  /**
   * The size of A excluding the intersection (|A| - |A &#8745; B|).
   *
   * @return the size A minus intersection
   */
  public int getSizeAMinusIntersection() {
    return sizeAMinusIntersection;
  }

  /**
   * The size of B excluding the intersection (|B| - |A &#8745; B|).
   *
   * @return the size B minus intersection
   */
  public int getSizeBMinusIntersection() {
    return sizeBMinusIntersection;
  }
}
