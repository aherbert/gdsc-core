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

package uk.ac.sussex.gdsc.core.threshold;

/**
 * Contains a histogram.
 *
 * <p>The histogram is implemented in this class using integer bin values starting from an offset.
 * The offset does not have to be an integer.
 */
public class IntHistogram extends Histogram {
  /** The offset. */
  final double offset;

  /**
   * Instantiates a new int histogram.
   *
   * @param histogramCounts the histogram counts
   * @param minBin the min bin
   * @param maxBin the max bin
   * @param offset the offset
   */
  protected IntHistogram(int[] histogramCounts, int minBin, int maxBin, double offset) {
    super(histogramCounts, minBin, maxBin);
    this.offset = offset;
  }

  /**
   * Instantiates a new int histogram.
   *
   * @param source the source
   */
  protected IntHistogram(IntHistogram source) {
    super(source);
    this.offset = source.offset;
  }

  /**
   * Instantiates a new int histogram.
   *
   * @param histogram the histogram counts
   * @param offset the offset
   */
  public IntHistogram(int[] histogram, int offset) {
    super(histogram);
    this.offset = offset;
  }

  @Override
  public float getValue(int bin) {
    return (float) (offset + bin);
  }

  @Override
  public IntHistogram copy() {
    return new IntHistogram(this);
  }
}
