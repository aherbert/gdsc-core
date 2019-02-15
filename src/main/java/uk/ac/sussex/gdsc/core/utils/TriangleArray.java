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
 * Copyright (C) 2011 - 2019 Alex Herbert
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
 * Specify data in a 2D upper triangular array of size n in 1 dimension.
 *
 * <p>The amount of data will be n*(n-1)/2. The data can be iterated for i in 0:n-1 and for j in
 * i+1:n-1. No index is computed for i==j.
 *
 * <p>The following syntax is valid:
 *
 * <pre>
 * int n;
 * TriangleArray a = new TriangleArray(n);
 *
 * // fast iteration over the data
 * for (int i = 0; i &lt; n; i++)
 *   for (int j = i + 1, index = a.toIndex(i, j); j &lt; n; j++, index++) {
 *   }
 *
 * // Iterate over all NxN values
 * for (int i = 0; i &lt; n; i++) {
 *   for (int j = 0, precursor = a.toPrecursorIndex(i); j &lt; i; j++) {
 *     int k = a.toSafeIndex(i, j);
 *     int index = a.precursorToIndex(precursor, j);
 *     // k == index
 *   }
 *   for (int j = i + 1, index = a.toIndex(i, j); j &lt; n; j++, index++) {
 *     int k = a.toSafeIndex(i, j);
 *     // k == index
 *   }
 * }
 *
 * // Comparing any index j to index i
 * a.setup(i);
 * for (int j = 0; j &lt; n; j++) {
 *   if (i == j)
 *     continue;
 *   int k = a.toSafeIndex(i, j);
 *   int index = a.toIndex(j);
 *   // k == index
 * }
 * </pre>
 */
public class TriangleArray {
  // This class uses i,j as legitimate method parameter names.
  // CHECKSTYLE.OFF: ParameterName
  // CHECKSTYLE.OFF: MemberName

  /**
   * The size of the array in 1 dimension.
   */
  private final int n;

  /**
   * Conversion constant used on the toIndex methods: {@code (n * (n - 1) / 2) -1}.
   */
  private final int toIndex1;
  /**
   * Conversion constant used on the fromIndex methods: {@code 4 * n * (n - 1) - 7}.
   */
  private final int fromIndex1;
  /**
   * Conversion constant used on the fromIndex methods: {@code n - 2}.
   */
  private final int fromIndex2;

  /** The target index for j used when computing the toIndex using only index i. */
  private int targetJ;
  /** Conversion constant used when computing the toIndex using only index i. */
  private int precursor;
  /** Conversion constant used when computing the toIndex using only index i. */
  private int rootIndex;

  /**
   * Instantiates a new 2D upper triangle array.
   *
   * @param n the size of the array in 1 dimension
   */
  public TriangleArray(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("n must be positive");
    }

    this.n = n;

    // Pre-compute conversion constants
    toIndex1 = getLength() - 1;
    fromIndex1 = 4 * n * (n - 1) - 7;
    fromIndex2 = n - 2;
  }

  /**
   * Gets the size of the array in 1 dimension.
   *
   * @return the size of the array in 1 dimension
   */
  public int getSize() {
    return n;
  }

  /**
   * The length of the array data (n * (n - 1) / 2).
   *
   * @param n the size of the array in 1 dimension
   * @return the length
   */
  public static int getLength(int n) {
    return (n * (n - 1) / 2);
  }

  /**
   * The length of the array data (n * (n - 1) / 2).
   *
   * @return the length
   */
  public final int getLength() {
    return getLength(n);
  }

  /**
   * Convert from ij to linear index.
   *
   * <p>Behaviour is undefined if i==j.
   *
   * @param n the size of the array in 1 dimension
   * @param i the index i
   * @param j the index j
   * @return the linear index
   */
  public static int toIndex(int n, int i, int j) {
    return (n * (n - 1) / 2) - (n - i) * ((n - i) - 1) / 2 + j - i - 1;
  }

  /**
   * Convert from ij to linear index. Index j must be greater than i.
   *
   * <p>Behaviour is undefined if i==j.
   *
   * @param i the index i
   * @param j the index j
   * @return the linear index
   */
  public int toIndex(int i, int j) {
    return toIndex1 - (n - i) * ((n - i) - 1) / 2 + j - i;
  }

  /**
   * Generate the linear index for any index i and target index j (initialised with
   * {@link #setup(int)}).
   *
   * @param i the index i
   * @return the linear index
   * @throws IllegalArgumentException if i==j
   */
  public int toIndex(int i) {
    if (targetJ > i) {
      return precursorToIndex(precursor, i);
    }
    if (targetJ < i) {
      return rootIndex + i;
    }
    throw new IllegalArgumentException("i cannot equal j");
  }

  /**
   * Setup to generate the linear index for any index i and target index j.
   *
   * @param j the index j
   */
  public void setup(int j) {
    this.targetJ = j;
    precursor = toPrecursorIndex(j);
    rootIndex = toIndex(j, 0);
  }

  /**
   * Convert from j to a precursor for the linear index. Index j must be greater than target i.
   * Behaviour is undefined if i==j.
   *
   * <p>Package scope for testing.
   *
   * @param j the index j
   * @return the precursor to the linear index
   */
  int toPrecursorIndex(int j) {
    return toIndex1 + j;
  }

  /**
   * Convert from precursor j to linear index. Precursor for j must be computed with index j greater
   * than i. Behaviour is undefined if i==j.
   *
   * <p>Package scope for testing.
   *
   * @param precusor the precursor to the linear index
   * @param i the index i
   * @return the linear index
   */
  int precursorToIndex(int precusor, int i) {
    return precusor - (n - i) * ((n - i) - 1) / 2 - i;
  }

  /**
   * Convert from ij to linear index. If j is less than i then the pair are reversed.
   *
   * <p>Behaviour is undefined if i==j.
   *
   * @param n the size of the array in 1 dimension
   * @param i the index i
   * @param j the index j
   * @return the linear index
   * @see #toIndex(int, int, int)
   */
  public static int toSafeIndex(int n, int i, int j) {
    return (j > i) ? toIndex(n, i, j) : toIndex(n, j, i);
  }

  /**
   * Convert from ij to linear index. If j is less than i then the pair are reversed. Behaviour is
   * undefined if i==j.
   *
   * @param i the index i
   * @param j the index j
   * @return the linear index
   */
  public int toSafeIndex(int i, int j) {
    return (j > i) ? toIndex(i, j) : toIndex(j, i);
  }

  /**
   * Convert from linear index to ij.
   *
   * @param n the size of the array in 1 dimension
   * @param k the linear index (Must be with the bound 0:k-1)
   * @return the ij data
   */
  public static int[] fromIndex(int n, int k) {
    final int i = n - 2 - (int) Math.floor(Math.sqrt(-8.0 * k + 4 * n * (n - 1) - 7) / 2.0 - 0.5);
    final int j = k + i + 1 - (n * (n - 1) / 2) + (n - i) * ((n - i) - 1) / 2;
    return new int[] {i, j};
  }

  /**
   * Convert from linear index to ij.
   *
   * @param k the linear index (Must be with the bound 0:length-1)
   * @return the ij data
   */
  public int[] fromIndex(int k) {
    final int i = fromIndex2 - (int) Math.floor(Math.sqrt(-8.0 * k + fromIndex1) / 2.0 - 0.5);
    final int j = k + i - toIndex1 + (n - i) * ((n - i) - 1) / 2;
    return new int[] {i, j};
  }

  /**
   * Convert from linear index to ij.
   *
   * @param n the size of the array in 1 dimension
   * @param k the linear index (Must be with the bound 0:k-1)
   * @param ij the ij data (Must be size 2 or greater)
   */
  public static void fromIndex(int n, int k, int[] ij) {
    final int i = n - 2 - (int) Math.floor(Math.sqrt(-8.0 * k + 4 * n * (n - 1) - 7) / 2.0 - 0.5);
    ij[0] = i;
    ij[1] = k + i + 1 - (n * (n - 1) / 2) + (n - i) * ((n - i) - 1) / 2;
  }

  /**
   * Convert from linear index to ij.
   *
   * @param k the linear index (Must be with the bound 0:length-1)
   * @param ij the ij data (Must be size 2 or greater)
   */
  public void fromIndex(int k, int[] ij) {
    final int i = fromIndex2 - (int) Math.floor(Math.sqrt(-8.0 * k + fromIndex1) / 2.0 - 0.5);
    ij[0] = i;
    ij[1] = k + i - toIndex1 + (n - i) * ((n - i) - 1) / 2;
  }

  // CHECKSTYLE.ON: ParameterName
  // CHECKSTYLE.ON: MemberName
}
