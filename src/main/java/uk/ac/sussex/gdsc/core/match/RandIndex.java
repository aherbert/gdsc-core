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

package uk.ac.sussex.gdsc.core.match;

import uk.ac.sussex.gdsc.core.utils.MathUtils;

import java.math.BigInteger;

/**
 * Compute the Rand index for two classifications of a set of data.
 *
 * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
 * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
 *
 * <p>A problem with the Rand index is that the expected value between random partitions is not
 * constant. The adjusted Rand index assumes the generalized hyper-geometric distribution as the
 * model of randomness. It has the maximum value 1, and its expected value is 0 in the case of
 * random clusters.
 *
 * <p>W. M. Rand (1971). "Objective criteria for the evaluation of clustering methods". Journal of
 * the American Statistical Association. American Statistical Association. 66 (336): 846–850.
 * doi:10.2307/2284239. JSTOR 2284239.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
 */
public class RandIndex {

  private int numberOfElements = -1;
  private long truePositives;
  private long truePositivesPlusFalsePositives;
  private long truePositivesPlusFalseNegatives;

  /**
   * Returns an exact representation of the the number of 2-element subsets that can be selected
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
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
   * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
   *
   * <p>Uses a simple method of comparing all possible pairs and counting identical classifications.
   *
   * @param set1 the first set of clusters for the objects
   * @param set2 the second set of clusters for the objects
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws IllegalArgumentException if the sets are different lengths
   */
  public static double simpleRandIndex(int[] set1, int[] set2) {
    if (set1 == null) {
      throw new NullPointerException("set1");
    }
    if (set2 == null) {
      throw new NullPointerException("set2");
    }
    if (set1.length != set2.length) {
      throw new IllegalArgumentException("Sets must be the same size");
    }

    final int n = set1.length;

    if (n < 2) {
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
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
   * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
   *
   * <p>Compute using a contingency table.
   *
   * @param set1 the first set of clusters for the objects
   * @param set2 the second set of clusters for the objects
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws IllegalArgumentException if the sets are different lengths
   */
  public static double randIndex(int[] set1, int[] set2) {
    return new RandIndex().getRandIndex(set1, set2);
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
   * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
   *
   * <p>Compute using a contingency table. Each set should use integers from 0 to n-1 for n
   * clusters.
   *
   * <p>Warning: No checks are made on the input!
   *
   * @param set1 the first set of clusters for the objects
   * @param n1 the number of clusters (max cluster number + 1) in set 1
   * @param set2 the second set of clusters for the objects
   * @param n2 the number of clusters (max cluster number + 1) in set 2
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public static double randIndex(int[] set1, int n1, int[] set2, int n2) {
    return new RandIndex().getRandIndex(set1, n1, set2, n2);
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * @param set1 the first set of clusters for the objects
   * @param set2 the second set of clusters for the objects
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws IllegalArgumentException if the sets are different lengths
   */
  public static double adjustedRandIndex(int[] set1, int[] set2) {
    return new RandIndex().getAdjustedRandIndex(set1, set2);
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * <p>Each set should use integers from 0 to n-1 for n clusters. .
   *
   * <p>Warning: No checks are made on the input!
   *
   * @param set1 the first set of clusters for the objects
   * @param n1 the number of clusters (max cluster number + 1) in set 1
   * @param set2 the second set of clusters for the objects
   * @param n2 the number of clusters (max cluster number + 1) in set 2
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public static double adjustedRandIndex(int[] set1, int n1, int[] set2, int n2) {
    return new RandIndex().getAdjustedRandIndex(set1, n1, set2, n2);
  }

  /**
   * Compact the set so that it contains cluster assignments from 0 to n-1 where n is the number of
   * clusters (max cluster number + 1).
   *
   * @param set the set (modified in place)
   * @return the number of clusters (max cluster number + 1) (n)
   */
  public static int compact(int[] set) {
    // Edge cases
    if (set == null || set.length == 0) {
      return 0;
    }
    if (set.length == 1) {
      set[0] = 0;
      return 1;
    }

    final int[] newSet = new int[set.length];
    final int clusterNumber = compact(set, newSet);

    // Copy back
    System.arraycopy(newSet, 0, set, 0, set.length);

    return clusterNumber;
  }

  /**
   * Compact the set so that it contains cluster assignments from 0 to n-1 where n is the number of
   * clusters (max cluster number + 1).
   *
   * @param set the set
   * @param newSet the new set
   * @return the number of clusters (max cluster number + 1) (n)
   */
  private static int compact(int[] set, int[] newSet) {
    // No checks on input arguments as this method is internal

    final boolean[] skip = new boolean[set.length];

    int clusterNumber = 0;
    for (int i = 0; i < set.length; i++) {
      if (skip[i]) {
        continue;
      }
      final int value = set[i];
      for (int j = i; j < set.length; j++) {
        if (value == set[j]) {
          skip[j] = true;
          newSet[j] = clusterNumber;
        }
      }
      clusterNumber++;
    }

    return clusterNumber;
  }

  /**
   * Instantiates a new RandIndex object.
   */
  public RandIndex() {
    reset();
  }

  /**
   * Reset the computation.
   */
  private void reset() {
    numberOfElements = -1;
    truePositives = truePositivesPlusFalsePositives = truePositivesPlusFalseNegatives = 0;
  }

  /**
   * Compute the contingency table for two classifications of a set of data and generate the values
   * required to produce the Rand index.
   *
   * @param set1 the first set of clusters for the objects
   * @param set2 the second set of clusters for the objects
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws IllegalArgumentException if the sets are different lengths
   * @throws RuntimeException if the sums are larger than Long.MAX_VALUE
   */
  public void compute(int[] set1, int[] set2) {
    reset();

    if (set1 == null) {
      throw new NullPointerException("set1");
    }
    if (set2 == null) {
      throw new NullPointerException("set2");
    }
    if (set1.length != set2.length) {
      throw new IllegalArgumentException("Sets must be the same size");
    }

    final int length = set1.length;
    if (length < 2) {
      this.numberOfElements = length;
      return;
    }

    final int[] set1a;
    final int[] set2a;
    final int max1;
    final int max2;

    // Compute using a contingency table.
    // Each set should optimally use integers from 0 to n-1 for n clusters.
    // Check if we need to compact the sets
    final int[] limits1 = MathUtils.limits(set1);
    if (limits1[0] < 0 || limits1[1] == Integer.MAX_VALUE) {
      set1a = new int[length];
      max1 = compact(set1, set1a);
    } else {
      set1a = set1;
      max1 = limits1[1] + 1;
    }

    final int[] limits2 = MathUtils.limits(set2);
    if (limits2[0] < 0 || limits2[1] == Integer.MAX_VALUE) {
      set2a = new int[length];
      max2 = compact(set2, set2a);
    } else {
      set2a = set2;
      max2 = limits2[1] + 1;
    }

    compute(set1a, max1, set2a, max2);
  }

  /**
   * Compute the contingency table for two classifications of a set of data and generate the values
   * required to produce the Rand index.
   *
   * <p>Each set should use integers from 0 to n-1 for n clusters.
   *
   * <p>Warning: No checks are made on the input! However if clusters numbers below zero or above n
   * clusters are used then an {@link ArrayIndexOutOfBoundsException} can occur. This is handled by
   * compacting the sets and re-computing.
   *
   * @param set1 the first set of clusters for the objects
   * @param n1 the number of clusters (max cluster number + 1) in set 1
   * @param set2 the second set of clusters for the objects
   * @param n2 the number of clusters (max cluster number + 1) in set 2
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws RuntimeException if the sums are larger than Long.MAX_VALUE
   */
  public void compute(int[] set1, int n1, int[] set2, int n2) {
    reset();

    final int n = set1.length;
    if (n < 2) {
      this.numberOfElements = n;
      return;
    }

    // TP will only overflow after TP+FP
    long tp = 0;
    // Note: The following could overflow.
    // This will happen if the number of clusters is very large (approaching Integer.MAX_VALUE),
    // i.e. non-clustered data. Any reasonable clustering comparison will have clustered the data
    // better than that so we just fail with an exception.
    long tpPlusFp = 0;
    long tpPlusFn = 0;

    // Note: Using a single array we have an upper limit on the array size of: 2^31 - 1 * 4 bytes ~
    // 8Gb
    // This should be enough. Otherwise we use int[][] table.
    final long lSize = (long) n1 * n2;
    if (lSize > Integer.MAX_VALUE) {
      final int[][] table = new int[n1][n2];

      try {
        for (int i = 0; i < n; i++) {
          table[set1[i]][set2[i]]++;
        }
      } catch (final ArrayIndexOutOfBoundsException ex) {
        // Probably because the input was not checked ...
        // This should not cause infinite recursion as the next time all the indices will be OK.
        compute(set1, set2);
        return;
      }

      for (int i = 0; i < n1; i++) {
        // Note: When we sum the columns or rows we are summing the number of counts
        // of members of the input array. This can never exceed Integer.MAX_VALUE since
        // Java uses ints for array allocation.
        int sum = 0;
        for (int j = 0; j < n2; j++) {
          final int v = table[i][j];
          sum += v;
          tp += binomialCoefficient2(v);
        }
        tpPlusFp += binomialCoefficient2(sum);
        if (tpPlusFp < 0) {
          throw new RuntimeException("TP+FP overflow");
        }
      }

      for (int j = 0; j < n2; j++) {
        int sum = 0;
        for (int i = 0; i < n1; i++) {
          sum += table[i][j];
        }
        tpPlusFn += binomialCoefficient2(sum);
        if (tpPlusFn < 0) {
          throw new RuntimeException("TP+FN overflow");
        }
      }
    } else {
      final int size = n1 * n2;
      final int[] table = new int[size];

      try {
        for (int i = 0; i < n; i++) {
          table[set1[i] * n2 + set2[i]]++;
        }
      } catch (final ArrayIndexOutOfBoundsException ex) {
        // Probably because the input was not checked ...
        // This should not cause infinite recursion as the next time all the indices will be OK.
        compute(set1, set2);
        return;
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
        tpPlusFp += binomialCoefficient2(sum);
        if (tpPlusFp < 0) {
          throw new RuntimeException("TP+FP overflow");
        }
      }

      for (int j = 0; j < n2; j++) {
        int sum = 0;
        for (int index = j; index < size; index += n2) {
          sum += table[index];
        }
        tpPlusFn += binomialCoefficient2(sum);
        if (tpPlusFn < 0) {
          throw new RuntimeException("TP+FN overflow");
        }
      }
    }

    // Store after no exceptions are raised
    this.numberOfElements = n;
    this.truePositives = tp;
    this.truePositivesPlusFalsePositives = tpPlusFp;
    this.truePositivesPlusFalseNegatives = tpPlusFn;
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
   * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
   *
   * <p>Compute using a contingency table.
   *
   * @param set1 the first set of clusters for the objects
   * @param set2 the second set of clusters for the objects
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws IllegalArgumentException if the sets are different lengths
   */
  public double getRandIndex(int[] set1, int[] set2) {
    compute(set1, set2);
    return getRandIndex();
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
   * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
   *
   * <p>Compute using a contingency table. Each set should use integers from 0 to n-1 for n
   * clusters.
   *
   * <p>Warning: No checks are made on the input!
   *
   * @param set1 the first set of clusters for the objects
   * @param n1 the number of clusters (max cluster number + 1) in set 1
   * @param set2 the second set of clusters for the objects
   * @param n2 the number of clusters (max cluster number + 1) in set 2
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public double getRandIndex(int[] set1, int n1, int[] set2, int n2) {
    compute(set1, n1, set2, n2);
    return getRandIndex();
  }

  /**
   * Compute the Rand index for two classifications of a set of data.
   *
   * <p>The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do
   * not agree on any pair of points and 1 indicating that the data clusters are exactly the same.
   *
   * <p>Compute using a contingency table.
   *
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public double getRandIndex() {
    if (numberOfElements < 2) {
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

    final long ab = truePositives + tn;
    if (ab > 0) {
      return (double) ab / binomialCoefficient2(numberOfElements);
    }

    // Use big integer
    return BigInteger.valueOf(truePositives).add(BigInteger.valueOf(tn)).doubleValue()
        / binomialCoefficient2(numberOfElements);
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * @param set1 the first set of clusters for the objects
   * @param set2 the second set of clusters for the objects
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   * @throws IllegalArgumentException if the sets are different lengths
   */
  public double getAdjustedRandIndex(int[] set1, int[] set2) {
    compute(set1, set2);
    return getAdjustedRandIndex();
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * <p>Each set should use integers from 0 to n-1 for n clusters. .
   *
   * <p>Warning: No checks are made on the input!
   *
   * @param set1 the first set of clusters for the objects
   * @param n1 the number of clusters (max cluster number + 1) in set 1
   * @param set2 the second set of clusters for the objects
   * @param n2 the number of clusters (max cluster number + 1) in set 2
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public double getAdjustedRandIndex(int[] set1, int n1, int[] set2, int n2) {
    compute(set1, n1, set2, n2);
    return getAdjustedRandIndex();
  }

  /**
   * Compute the adjusted Rand index for two classifications of a set of data.
   *
   * <p>The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the
   * Rand Index may only yield a value between 0 and +1, the adjusted Rand index can yield negative
   * values if the index is less than the expected index.
   *
   * <p>Compute using a contingency table.
   *
   * @return the Rand index
   * @see <a href="https://en.wikipedia.org/wiki/Rand_index">Rand Index</a>
   */
  public double getAdjustedRandIndex() {
    if (numberOfElements < 2) {
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
   * @return the true positives
   */
  public long getTruePositives() {
    return truePositives;
  }

  /**
   * Gets the true negatives from the last call to compute().
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
   * @return the false positives
   */
  public long getFalsePositives() {
    return truePositivesPlusFalsePositives - truePositives;
  }

  /**
   * Gets the false negatives from the last call to compute().
   *
   * @return the false negatives
   */
  public long getFalseNegatives() {
    return truePositivesPlusFalseNegatives - truePositives;
  }
}
