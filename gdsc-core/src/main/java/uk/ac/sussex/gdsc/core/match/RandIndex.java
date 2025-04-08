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

package uk.ac.sussex.gdsc.core.match;

import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Compute the Rand index for two classifications of a set of data.
 *
 * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data
 * classifications do not agree on any pair of points and 1 indicating that the data classifications
 * are exactly the same.
 *
 * <p>A problem with the Rand index is that the expected value between random partitions is not
 * constant. The adjusted Rand index assumes the generalized hyper-geometric distribution as the
 * model of randomness. It has the maximum value 1, and its expected value is 0 in the case of
 * random classifications.
 *
 * <p>W. M. Rand (1971). "Objective criteria for the evaluation of clustering methods". Journal of
 * the American Statistical Association. American Statistical Association. 66 (336): 846–850.
 * doi:10.2307/2284239. JSTOR 2284239.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
 */
public class RandIndex {

  /** The constant to reset the number of elements. */
  private static final int RESET_NUMBER_OF_ELEMENTS = -1;

  /** The number of elements. */
  private int numberOfElements = RESET_NUMBER_OF_ELEMENTS;
  /** The true positives. */
  private long truePositives;
  /** The true positives + false positives. */
  private long truePositivesPlusFalsePositives;
  /** The true positives + false negatives. */
  private long truePositivesPlusFalseNegatives;

  /**
   * Returns an exact representation of the number of 2-element subsets that can be selected
   * from an {@code n}-element set.
   *
   * <p>If n==0 or n==1 it will return 0.
   *
   * @param n the size of the set
   * @return {@code n choose 2}
   */
  private static long binomialCoefficient2(final long n) {
    // Unsigned right shift since the number will be positive
    // (it is only called with integers cast up to long).
    // Equivalent to: return (n - 1L) * n / 2L
    return ((n - 1L) * n) >>> 1;
  }

  /**
   * Gets the default Rand index for small datasets (n<2).
   *
   * @param n the n
   * @return the default rand index
   */
  private static double getDefaultRandIndex(int n) {
    checkState(n);
    return (n == 1) ? 1 : 0;
  }

  /**
   * Check state.
   *
   * @param n the n
   */
  private static void checkState(int n) {
    if (n < 0) {
      throw new IllegalStateException("No contigency table has been computed");
    }
  }

  /**
   * Gets the default adjusted Rand index for small datasets (n<2).
   *
   * @param n the n
   * @return the default rand index
   */
  private static double getDefaultAdjustedRandIndex(int n) {
    return getDefaultRandIndex(n); // No difference
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data
   * classifications do not agree on any pair of points and 1 indicating that the data
   * classifications are exactly the same.
   *
   * <p>Uses a simple method of comparing all possible pairs and counting identical classifications.
   *
   * @param set1 the first set of classifications for the objects
   * @param set2 the second set of classifications for the objects
   * @return the Rand index
   * @throws IllegalArgumentException if the sets are different lengths
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public static double simpleRandIndex(int[] set1, int[] set2) {
    checkSetArguments(set1, set2);

    final int n = set1.length;

    if (n <= 1) {
      return getDefaultRandIndex(n);
    }

    // a = the number of pairs of elements in S that are in the same
    // set in X and in the same set in Y
    // b = the number of pairs of elements in S that are in different
    // sets in X and in different sets in Y
    long aplusb = 0; // a+b
    for (int i = 0; i < n; i++) {
      final int s1 = set1[i];
      final int s2 = set2[i];
      for (int j = i + 1; j < n; j++) {
        if (s1 == set1[j]) {
          if (s2 == set2[j]) {
            aplusb++;
          }
        } else if (s2 != set2[j]) {
          aplusb++;
        }
      }
    }

    return (double) aplusb / binomialCoefficient2(n);
  }

  /**
   * Check the two sets are the same size.
   *
   * @param set1 the set 1
   * @param set2 the set 2
   */
  private static void checkSetArguments(int[] set1, int[] set2) {
    ValidationUtils.checkNotNull(set1, "set1");
    ValidationUtils.checkNotNull(set2, "set2");
    ValidationUtils.checkArgument(set1.length == set2.length, "Sets must be the same size");
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data
   * classifications do not agree on any pair of points and 1 indicating that the data
   * classifications are exactly the same.
   *
   * <p>Compute using a contingency table which requires that all identifiers are positive and
   * optimally non-sparse from zero to n-1 for n classifications.
   *
   * @param set1 the first set of classifications for the objects
   * @param set2 the second set of classifications for the objects
   * @return the Rand index
   * @throws IllegalArgumentException if the sets are different lengths or contain non positive
   *         identifiers
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @see #compute(int[], int[])
   */
  public static double randIndex(int[] set1, int[] set2) {
    return new RandIndex().compute(set1, set2).getRandIndex();
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * <p>Compute using a contingency table which requires that all identifiers are positive and
   * optimally non-sparse from zero to n-1 for n classifications.
   *
   * @param set1 the first set of classifications for the objects
   * @param set2 the second set of classifications for the objects
   * @return the Rand index
   * @throws IllegalArgumentException if the sets are different lengths or contain non positive
   *         identifiers
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @see #compute(int[], int[])
   */
  public static double adjustedRandIndex(int[] set1, int[] set2) {
    return new RandIndex().compute(set1, set2).getAdjustedRandIndex();
  }

  /**
   * Reset the computation.
   */
  private void reset() {
    numberOfElements = RESET_NUMBER_OF_ELEMENTS;
    truePositives = 0;
    truePositivesPlusFalsePositives = 0;
    truePositivesPlusFalseNegatives = 0;
  }

  /**
   * Compute the contingency table for two classifications of a set of data and generate the values
   * required to produce the Rand index.
   *
   * <p>Requires that all identifiers are positive. Optimally they should be from 0 to n-1 where n
   * is the number of distinct identifiers in the set. This can be achieved using a
   * {@link Resequencer}.
   *
   * @param set1 the first set of classifications for the objects
   * @param set2 the second set of classifications for the objects
   * @return a reference to this object allowing chaining
   * @throws IllegalArgumentException if the sets are different lengths or contain non positive
   *         identifiers
   * @throws ArithmeticException if the sums are larger than Long.MAX_VALUE
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public RandIndex compute(int[] set1, int[] set2) {
    reset();

    checkSetArguments(set1, set2);

    // Fast result
    final int length = set1.length;
    if (length <= 1) {
      this.numberOfElements = length;
    } else {
      // Each set should use integers from 0 to n-1 for n classifications.
      final int n1 = checkPositive(set1);
      final int n2 = checkPositive(set2);

      computeContingencyTable(set1, n1, set2, n2);
    }
    return this;
  }

  /**
   * Check positive.
   *
   * @param set the set
   * @return the number of identifiers
   */
  private static int checkPositive(int[] set) {
    int max = 0;
    for (final int value : set) {
      if (value < 0) {
        throw new IllegalArgumentException("Identifiers must be positive: " + value);
      }
      if (max < value) {
        max = value;
      }
    }
    return Math.addExact(max, 1);
  }

  /**
   * Compute the contingency table for two classifications of a set of data and generate the values
   * required to produce the Rand index.
   *
   * <p>Each set should use integers from 0 to n-1 for n classifications.
   *
   * <p>Warning: No checks are made on the input!
   *
   * @param set1 the first set of classifications for the objects
   * @param n1 the number of classifications (max classification number + 1) in set 1
   * @param set2 the second set of classifications for the objects
   * @param n2 the number of classifications (max classification number + 1) in set 2
   * @throws ArithmeticException if the sums are larger than Long.MAX_VALUE
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  private void computeContingencyTable(int[] set1, int n1, int[] set2, int n2) {
    // Note: Using a single array we have an upper limit on the array size of:
    // 2^31 - 1 * 4 bytes ~ 8Gb
    // This should be enough. Otherwise we use int[][] table.
    if ((long) n1 * n2 > Integer.MAX_VALUE) {
      computeUsingMatrix(set1, n1, set2, n2);
    } else {
      computeUsingArray(set1, n1, set2, n2);
    }
  }

  /**
   * Compute the contingency table for two classifications of a set of data and generate the values
   * required to produce the Rand index.
   *
   * <p>Each set should use integers from 0 to n-1 for n classifications.
   *
   * <p>Warning: No checks are made on the input!
   *
   * @param set1 the first set of classifications for the objects
   * @param n1 the number of classifications (max classification number + 1) in set 1
   * @param set2 the second set of classifications for the objects
   * @param n2 the number of classifications (max classification number + 1) in set 2
   * @throws ArithmeticException if the sums are larger than Long.MAX_VALUE
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  @VisibleForTesting
  void computeUsingMatrix(int[] set1, int n1, int[] set2, int n2) {
    // TP will only overflow after TP+FP
    long tp = 0;
    // Note: The following could overflow.
    // This will happen if the number of classifications is very large (approaching
    // Integer.MAX_VALUE),
    // i.e. non-clustered data. Any reasonable clustering comparison will have clustered the data
    // better than that so we just fail with an exception.
    long tpPlusFp = 0;
    long tpPlusFn = 0;

    // This only ever gets called when (n1 * n2) is very large.
    // Offset with the min of the range just in case the input can be reduced in size.
    final int min1 = MathUtils.min(set1);
    final int min2 = MathUtils.min(set2);
    final int size1 = n1 - min1 + 1;
    final int size2 = n2 - min2 + 1;

    final int[][] table = new int[size1][size2];

    for (int i = 0; i < set1.length; i++) {
      table[set1[i] - min1][set2[i] - min2]++;
    }

    for (int i = 0; i < size1; i++) {
      // Note: When we sum the columns or rows we are summing the number of counts
      // of members of the input array. This can never exceed Integer.MAX_VALUE since
      // Java uses ints for array allocation.
      int sum = 0;
      for (int j = 0; j < size2; j++) {
        final int v = table[i][j];
        sum += v;
        tp += binomialCoefficient2(v);
      }
      tpPlusFp = Math.addExact(tpPlusFp, binomialCoefficient2(sum));
    }

    for (int j = 0; j < size2; j++) {
      int sum = 0;
      for (int i = 0; i < size1; i++) {
        sum += table[i][j];
      }
      tpPlusFn = Math.addExact(tpPlusFn, binomialCoefficient2(sum));
    }

    // Store after no exceptions are raised
    this.numberOfElements = set1.length;
    this.truePositives = tp;
    this.truePositivesPlusFalsePositives = tpPlusFp;
    this.truePositivesPlusFalseNegatives = tpPlusFn;
  }

  /**
   * Compute the contingency table for two classifications of a set of data and generate the values
   * required to produce the Rand index.
   *
   * <p>Each set should use integers from 0 to n-1 for n classifications.
   *
   * <p>Warning: No checks are made on the input!
   *
   * <p>This method uses a single linear array for the contingency table.
   *
   * @param set1 the first set of classifications for the objects
   * @param n1 the number of classifications (max classification number + 1) in set 1
   * @param set2 the second set of classifications for the objects
   * @param n2 the number of classifications (max classification number + 1) in set 2
   * @throws ArithmeticException if the sums are larger than Long.MAX_VALUE
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  private void computeUsingArray(int[] set1, int n1, int[] set2, int n2) {
    // TP will only overflow after TP+FP
    long tp = 0;
    // Note: The following could overflow.
    // This will happen if the number of classifications is very large (approaching
    // Integer.MAX_VALUE),
    // i.e. non-clustered data. Any reasonable clustering comparison will have clustered the data
    // better than that so we just fail with an exception.
    long tpPlusFp = 0;
    long tpPlusFn = 0;

    final int size = n1 * n2;
    final int[] table = new int[size];

    for (int i = 0; i < set1.length; i++) {
      table[set1[i] * n2 + set2[i]]++;
    }

    for (int i = 0, index = 0; i < n1; i++) {
      // Note: When we sum the columns or rows we are summing the number of counts
      // of members of the input array. This can never exceed Integer.MAX_VALUE since
      // Java uses ints for array allocation.
      int sum = 0;
      for (final int stop = index + n2; index < stop; index++) {
        final int v = table[index];
        sum += v;
        tp += binomialCoefficient2(v);
      }
      tpPlusFp = Math.addExact(tpPlusFp, binomialCoefficient2(sum));
    }

    for (int j = 0; j < n2; j++) {
      int sum = 0;
      for (int index = j; index < size; index += n2) {
        sum += table[index];
      }
      tpPlusFn = Math.addExact(tpPlusFn, binomialCoefficient2(sum));
    }

    // Store after no exceptions are raised
    this.numberOfElements = set1.length;
    this.truePositives = tp;
    this.truePositivesPlusFalsePositives = tpPlusFp;
    this.truePositivesPlusFalseNegatives = tpPlusFn;
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>For a set of {@code n} elements {@code S} and two partitions of {@code S},
   * {@code X = (X1, ..., Xr)} into {@code r} subsets and {@code Y = (Y1, ..., Ys)} into {@code s}
   * subsets define:
   *
   * <ul>
   *
   * <li>{@code a}, the number of pairs of elements in {@code S} that are in the
   * <strong>same</strong> subset in {@code X} and in the <strong>same</strong> subset in {@code Y}.
   *
   * <li>{@code b}, the number of pairs of elements in {@code S} that are in
   * <strong>different</strong> subsets in {@code X} and in <strong>different</strong> subsets in
   * {@code Y}.
   *
   * <li>{@code c}, the number of pairs of elements in {@code S} that are in the
   * <strong>same</strong> subset in {@code X} and in <strong>different</strong> subsets in
   * {@code Y}.
   *
   * <li>{@code d}, the number of pairs of elements in {@code S} that are in
   * <strong>different</strong> subsets in {@code X} and in the <strong>same</strong> subset in
   * {@code Y}.
   *
   * </ul>
   *
   * <pre>
   *         a + b
   * R = -------------
   *     a + b + c + d
   * </pre>
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data
   * classifications do not agree on any pair of points and 1 indicating that the data
   * classifications are exactly the same.
   *
   * <p>Since the denominator is the total number of pairs, the Rand index represents the frequency
   * of occurrence of agreements over the total pairs, or the probability that {@code X} and
   * {@code Y} will agree on a randomly chosen pair.
   *
   * <p>Uses the pre-computed contingency table.
   *
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @see #compute(int[], int[])
   */
  public double getRandIndex() {
    if (numberOfElements <= 1) {
      return getDefaultRandIndex(numberOfElements);
    }

    // Note:
    // Use the definitions here:
    // https://en.wikipedia.org/wiki/Rand_index
    // a = tp
    // b = tn
    // c = fp
    // d = fn
    // R = (a+b) / (a+b+c+d)
    // R = (a+b) / nC2

    if (truePositives == truePositivesPlusFalseNegatives
        && truePositives == truePositivesPlusFalsePositives) {
      // No errors
      return 1;
    }

    final long tn = getTrueNegatives();

    // No check for overflow since the binomial coefficient is computed as a long.
    // If nC2 == (a+b+c+d) then (a+b) must be less than a long.
    final long ab = truePositives + tn;
    return (double) ab / binomialCoefficient2(numberOfElements);
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * <p>Uses the pre-computed contingency table.
   *
   * @return the adjusted Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @see #compute(int[], int[])
   */
  public double getAdjustedRandIndex() {
    if (numberOfElements <= 1) {
      return getDefaultAdjustedRandIndex(numberOfElements);
    }

    // Note:
    // Use the definitions here:
    // https://en.wikipedia.org/wiki/Rand_index
    //
    // Adjusted Rand Index
    // sum(nij C 2) = tp
    // sum(ai C 2) = tp_fp
    // sum(bj C 2) = tp_fn
    // ARI = (sum(nij C 2) - (sum(ai C 2) * sum(bj C 2))/ nC2) / ((0.5*(sum(ai C 2)+sum(bj C 2))) -
    // (sum(ai C 2)*sum(bj C 2)) / nC2))
    // = (Index - ExpectedIndex) / (MaxIndex - ExpectedIndex)

    if (truePositives == truePositivesPlusFalseNegatives
        && truePositives == truePositivesPlusFalsePositives) {
      // No errors
      // Note: It also returns 1 if a sample of n=2 is used with only 1 cluster.
      // Q. Is this correct? Perhaps return 0 in that case (i.e. we are no better than random).
      return 1;
    }

    final long index = truePositives;
    final double expectedIndex = truePositivesPlusFalsePositives
        * (double) truePositivesPlusFalseNegatives / binomialCoefficient2(numberOfElements);
    final double maxIndex =
        0.5 * (truePositivesPlusFalsePositives + truePositivesPlusFalseNegatives);

    return (index - expectedIndex) / (maxIndex - expectedIndex);
  }

  /**
   * Gets the number of elements in the set of data.
   *
   * @return the number of elements
   */
  public int getN() {
    return numberOfElements;
  }

  /**
   * Gets the true positives from the last call to compute().
   *
   * <p>For a set of {@code n} elements {@code S} and two partitions of {@code S},
   * {@code X = (X1, ..., Xr)} into {@code r} subsets and {@code Y = (Y1, ..., Ys)} into {@code s}
   * subsets, return the number of pairs of elements in {@code S} that are in the
   * <strong>same</strong> subset in {@code X} and in the <strong>same</strong> subset in {@code Y}.
   *
   * @return the true positives
   */
  public long getTruePositives() {
    return truePositives;
  }

  /**
   * Gets the true negatives from the last call to compute().
   *
   * <p>For a set of {@code n} elements {@code S} and two partitions of {@code S},
   * {@code X = (X1, ..., Xr)} into {@code r} subsets and {@code Y = (Y1, ..., Ys)} into {@code s}
   * subsets, return the number of pairs of elements in {@code S} that are in
   * <strong>different</strong> subsets in {@code X} and in <strong>different</strong> subsets in
   * {@code Y}.
   *
   * @return the true negatives
   */
  public long getTrueNegatives() {
    return binomialCoefficient2(numberOfElements) - truePositivesPlusFalsePositives
        - truePositivesPlusFalseNegatives + truePositives;
  }

  /**
   * Gets the false positives from the last call to compute().
   *
   * <p>For a set of {@code n} elements {@code S} and two partitions of {@code S},
   * {@code X = (X1, ..., Xr)} into {@code r} subsets and {@code Y = (Y1, ..., Ys)} into {@code s}
   * subsets, return the number of pairs of elements in {@code S} that are in the
   * <strong>same</strong> subset in {@code X} and in <strong>different</strong> subsets in
   * {@code Y}.
   *
   * @return the false positives
   */
  public long getFalsePositives() {
    return truePositivesPlusFalsePositives - truePositives;
  }

  /**
   * Gets the false negatives from the last call to compute().
   *
   * <p>For a set of {@code n} elements {@code S} and two partitions of {@code S},
   * {@code X = (X1, ..., Xr)} into {@code r} subsets and {@code Y = (Y1, ..., Ys)} into {@code s}
   * subsets, return the number of pairs of elements in {@code S} that are in
   * <strong>different</strong> subsets in {@code X} and in the <strong>same</strong> subset in
   * {@code Y}.
   *
   * @return the false negatives
   */
  public long getFalseNegatives() {
    return truePositivesPlusFalseNegatives - truePositives;
  }
}
