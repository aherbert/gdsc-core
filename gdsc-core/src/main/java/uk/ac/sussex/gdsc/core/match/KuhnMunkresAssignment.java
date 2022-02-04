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

import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.IntFixedList;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Class to compute the assignment problem in polynomial time using the Kuhn-Munkres (Hungarian)
 * assignment algorithm, modified for rectangular matrices ({@code n x m}).
 *
 * <p>Complexity is {@code O(kkl)} where {@code k = min(n, m)} and {@code l = max(n, m)}. For very
 * non-square matrices, i.e. {@code l > 2k}, the procedure can give an improvement factor as large
 * as 10 over the standard algorithm using a squared {@code l x l} matrix.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Hungarian_algorithm">Hungarian algorithm</a>
 * @see <a href="https://dl.acm.org/citation.cfm?id=362945">Bourgeois and Lassalle (1971) An
 *      extension of the Munkres algorithm for the assignment problem to rectangular matrices.
 *      Communications of the ACM Volume 14, Issue 12, 802-804.</a>
 */
public class KuhnMunkresAssignment {
  /** The constant for no assignment. */
  private static final int NO_ASSIGNMENT = -1;
  /** The constant for a normal zero. */
  private static final byte NORMAL = 0;
  /**
   * The constant for a starred zero. Starred zeros form an independent set of zeros.
   */
  private static final byte STAR = 1;
  /**
   * The constant for a primed zero. Primed zeros are possible candidates for the independent set of
   * zeros.
   */
  private static final byte PRIME = 2;

  /** The cost matrix. */
  private final int[] cost;

  /** The rows in the matrix. */
  private final int rows;

  /** The columns in the matrix. */
  private final int cols;

  /** The uncovered zero (z0) that has been primed in algorithm step 3. */
  private int z0;

  /**
   * Create a new instance.
   *
   * @param cost the cost of an assignment between row and column (packed as i * cols + j)
   * @param rows the rows
   * @param cols the columns
   */
  private KuhnMunkresAssignment(int[] cost, int rows, int cols) {
    this.cost = cost;
    this.rows = rows;
    this.cols = cols;
  }

  /**
   * Create a new instance.
   *
   * @param cost the cost of an assignment between row and column (as {@code cost(i,j) = [i][j]}).
   * @return the instance
   * @throws IllegalArgumentException if the array is null, empty or not rectangular
   */
  @VisibleForTesting
  static KuhnMunkresAssignment create(int[][] cost) {
    // Check the data
    final int rows = ArrayUtils.getLength(cost);
    ValidationUtils.checkStrictlyPositive(rows, "No rows");
    final int cols = ArrayUtils.getLength(cost[0]);
    ValidationUtils.checkStrictlyPositive(cols, "No columns");
    for (int i = 1; i < rows; i++) {
      ValidationUtils.checkArgument(ArrayUtils.getLength(cost[i]) == cols,
          "Irregular size on row[%d]", i);
    }

    final int[] data = new int[rows * cols];
    for (int row = 0; row < rows; row++) {
      System.arraycopy(cost[row], 0, data, row * cols, cols);
    }
    return new KuhnMunkresAssignment(data, rows, cols);
  }

  /**
   * Compute the assignments of rows to columns.
   *
   * <p>Given the {@code n x m} matrix, find a set of {@code k} independent elements
   * {@code k = min(n, m)} so that the sum of these elements is minimum.
   *
   * <p>A value of -1 is used for no assignment.
   *
   * @param cost the cost of an assignment between row and column (as {@code cost(i,j) = [i][j]}).
   * @return the assignments
   * @throws IllegalArgumentException if the array is null, empty or not rectangular
   * @throws ArithmeticException if there is an overflow in the cost matrix
   */
  public static int[] compute(int[][] cost) {
    return create(cost).compute();
  }

  /**
   * Compute the assignments of rows to columns.
   *
   * <p>Given the {@code n x m} matrix, find a set of {@code k} independent elements
   * {@code k = min(n, m)} so that the sum of these elements is minimum.
   *
   * <p>A value of -1 is used for no assignment.
   *
   * @param cost the cost of an assignment between row and column (as
   *        {@code cost(i,j) = [i * cols + j]}).
   * @param rows the rows
   * @param cols the columns
   * @return the assignments
   * @throws IllegalArgumentException if the array is null, empty or not equal to rows * columns
   * @throws ArithmeticException if there is an overflow in the cost matrix
   */
  public static int[] compute(int[] cost, int rows, int cols) {
    SimpleArrayUtils.hasData2D(rows, cols, cost);
    return new KuhnMunkresAssignment(cost, rows, cols).compute();
  }

  /**
   * Compute the assignments of agents to tasks.
   *
   * <p>A value of -1 is used for no assignment.
   *
   * @return the assignments
   * @throws ArithmeticException if there is an overflow in the cost matrix
   */
  private int[] compute() {
    // Preliminaries:
    // (a) k = min (n, m), no lines are covered, no zeros are starred or primed.
    final byte[] zeroMask = new byte[cost.length];
    final boolean[] rowMask = new boolean[rows];
    final boolean[] colMask = new boolean[cols];
    final IntFixedList sequence = new IntFixedList(rows + cols);

    // Subtract smallest element from each row and column.
    // This is done conditionally.
    // (b) If the number of rows is greater than the number of columns, go at once to step O.
    if (rows > cols) {
      // Step 0(c)
      subtractSmallestFromEachColumn();
    } else {
      // Step 0(b): For each row, subtract the smallest element from each element in the row
      subtractSmallestFromEachRow();
      // If the number of columns is greater than the number of rows, go at once to step 1.
      if (rows == cols) {
        // Step 0(c): for each column, subtract the smallest element from each element in the column
        subtractSmallestFromEachColumn();
      }
    }

    // Perform the steps
    int step = step1(zeroMask, colMask);
    while (step != 0) {
      switch (step) {
        case 2:
          step = step2(zeroMask, colMask);
          // step in [0, 3]
          break;
        case 3:
          step = step3(zeroMask, rowMask, colMask);
          // step in [3, 4, 5]
          break;
        case 4:
          step = step4(zeroMask, rowMask, colMask, sequence);
          // step == 2
          break;
        case 5:
        default:
          step = step5(rowMask, colMask);
          // step == 3
          break;
      }
    }

    // Starred zeros form the desired independent set
    final int[] assignment = new int[rows];
    Arrays.fill(assignment, NO_ASSIGNMENT);
    for (int row = 0; row < rows; row++) {
      final int start = row * cols;
      final int end = start + cols;
      for (int i = start; i < end; i++) {
        if (zeroMask[i] == STAR) {
          assignment[row] = i - start;
          break;
        }
      }
    }
    return assignment;
  }

  /**
   * Subtract smallest from each row. This is preliminary step 0(b).
   *
   * @throws ArithmeticException if there is an overflow
   */
  private void subtractSmallestFromEachRow() {
    for (int row = 0; row < rows; row++) {
      final int start = row * cols;
      final int end = start + cols;
      int min = cost[start];
      for (int i = start + 1; i < end; i++) {
        if (min > cost[i]) {
          min = cost[i];
        }
      }
      min = Math.negateExact(min);
      for (int i = start; i < end; i++) {
        cost[i] = Math.addExact(cost[i], min);
      }
    }
  }

  /**
   * Subtract smallest from each column. This is preliminary step 0(c).
   *
   * @throws ArithmeticException if there is an overflow
   */
  private void subtractSmallestFromEachColumn() {
    for (int col = 0; col < cols; col++) {
      final int start = col;
      final int end = cost.length;
      int min = cost[start];
      for (int i = start + cols; i < end; i += cols) {
        if (min > cost[i]) {
          min = cost[i];
        }
      }
      min = Math.negateExact(min);
      for (int i = start; i < end; i += cols) {
        cost[i] = Math.addExact(cost[i], min);
      }
    }
  }

  /**
   * Perform step 1.
   *
   * <pre>
   * Find a zero, Z, of the matrix. If there is no starred zero in its row nor its
   * column, star Z. Repeat for each zero of the matrix. Go to step 2.
   * </pre>
   *
   * @param zeroMask the zero mask
   * @param colMask the column mask
   * @return the next step
   */
  private int step1(byte[] zeroMask, boolean[] colMask) {
    // Note: This is done once so we can assume no starred items.
    for (int row = 0; row < rows; row++) {
      final int start = row * cols;
      final int end = start + cols;
      for (int i = start; i < end; i++) {
        // Find a zero, Z, of the matrix.
        if (cost[i] == 0
            // If there is no starred zero
            // in its row nor its column, star Z. Repeat for each zero of the matrix.
            // Since we sweep the row there cannot be a star already. Just check the column above.
            && !starredColumn(zeroMask, i)) {
          zeroMask[i] = STAR;
          break;
        }
      }
    }
    // Go to step 2. Do this directly instead of returning 2.
    // This allows a possible fast exit if the solution is found by step 1 + 2
    // avoiding the main while loop. It will not cause recursion as step 2 returns direct.
    return step2(zeroMask, colMask);
  }

  /**
   * Check if the column contains a starred zero above the current index in the column.
   *
   * @param zeroMask the zero mask
   * @param index the current index
   * @return true, if successful
   */
  private boolean starredColumn(byte[] zeroMask, int index) {
    // Sweep back up the current column
    for (int i = index - cols; i >= 0; i -= cols) {
      if (zeroMask[i] == STAR) {
        return true;
      }
    }
    return false;
  }

  /**
   * Perform step 2.
   *
   * <pre>
   * Cover every column containing a 0*. If k columns are
   * covered, the starred zeros form the desired independent set; Exit.
   * Otherwise, go to step 3.
   * </pre>
   *
   * @param zeroMask the zero mask
   * @param colMask the column mask
   * @return the next step
   */
  private int step2(byte[] zeroMask, boolean[] colMask) {
    // k = min(n,m)
    int remaining = Math.min(rows, cols);
    for (int row = 0; row < rows; row++) {
      final int start = row * cols;
      final int end = start + cols;
      for (int i = start; i < end; i++) {
        if (zeroMask[i] == STAR) {
          if (--remaining == 0) {
            // Found the independent set
            return 0;
          }
          colMask[i - start] = true;
          // Each row has only 1 starred zero so skip the rest of the row
          break;
        }
      }
    }
    // go to step 3.
    return 3;
  }

  /**
   * Perform step 3.
   *
   * <pre>
   * Choose a noncovered zero and prime it; then consider
   * the row containing it. If there is no starred zero Z in this
   * row, go to step 4. If there is a starred zero Z in this row, cover this
   * row and uncover the column of Z. Repeat until all zeros are covered.
   * Go to step 5.
   * </pre>
   *
   * @param zeroMask the zero mask
   * @param rowMask the row mask
   * @param colMask the column mask
   * @return the next step
   */
  private int step3(byte[] zeroMask, boolean[] rowMask, boolean[] colMask) {
    // Choose a noncovered zero
    z0 = findNonCoveredZero(rowMask, colMask);
    if (z0 == NO_ASSIGNMENT) {
      // All zeros are covered. Go to step 5.
      return 5;
    }
    // Prime it
    zeroMask[z0] = PRIME;
    // Consider the row
    final int row = z0 / cols;
    final int start = row * cols;
    final int end = start + cols;
    for (int i = start; i < end; i++) {
      if (zeroMask[i] == STAR) {
        // Cover the row and uncover column of Z
        rowMask[row] = true;
        colMask[i - start] = false;
        // Repeat
        return 3;
      }
    }
    // no starred zero Z in this row, go to step 4
    return 4;
  }

  /**
   * Find a non covered zero.
   *
   * @param rowMask the row mask
   * @param colMask the column mask
   * @return the non-covered zero (or -1)
   */
  private int findNonCoveredZero(boolean[] rowMask, boolean[] colMask) {
    for (int row = 0; row < rows; row++) {
      if (!rowMask[row]) {
        for (int col = 0; col < cols; col++) {
          if (!colMask[col] && cost[row * cols + col] == 0) {
            return row * cols + col;
          }
        }
      }
    }
    return NO_ASSIGNMENT;
  }

  /**
   * Perform step 4.
   *
   * <pre>
   * There is a sequence of alternating starred and primed
   * zeros constructed as follows: let Z0 denote the uncovered 0'. Let
   * Z1 denote the 0* in Z0's column (if any). Let Z2 denote the 0' in
   * Z1's row. Continue in a similar way until the sequence stops at a
   * 0', Z{2k}, which has no 0* in its column. Unstar each starred zero
   * of the sequence, and star each primed zero of the sequence. Erase
   * all primes and uncover every line. Return to step 2.
   * </pre>
   *
   * @param zeroMask the zero mask
   * @param rowMask the row mask
   * @param colMask the column mask
   * @param sequence the sequence
   * @return the next step
   */
  private int step4(byte[] zeroMask, boolean[] rowMask, boolean[] colMask, IntFixedList sequence) {
    // Start at the uncovered 0'
    sequence.clear();
    sequence.add(z0);

    int col = z0 % cols;
    // Find 0* (if any)
    int row = findStarInColumn(zeroMask, col, sequence);

    while (row != NO_ASSIGNMENT) {
      // Find 0': Every starred zero will also have a primed zero in the row
      col = findPrimeInRow(zeroMask, row, sequence);
      // Find 0* (if any)
      row = findStarInColumn(zeroMask, col, sequence);
    }

    // Unstar each starred zero of the sequence
    for (int i = 1; i < sequence.size(); i += 2) {
      zeroMask[sequence.get(i)] = NORMAL;
    }
    // and star each primed zero of the sequence.
    for (int i = 0; i < sequence.size(); i += 2) {
      zeroMask[sequence.get(i)] = STAR;
    }

    // Erase all primes
    for (int i = 0; i < zeroMask.length; i++) {
      // This will leave starred untouched and reset primes
      zeroMask[i] = (byte) (zeroMask[i] & STAR);
    }
    // and uncover every line
    Arrays.fill(rowMask, false);
    Arrays.fill(colMask, false);

    // Return to step 2.
    return 2;
  }

  /**
   * Find the starred zero (0*) in the column (if any) and add it to the sequence.
   *
   * @param zeroMask the zero mask
   * @param col the column
   * @param sequence the sequence
   * @return the row (or -1)
   */
  private int findStarInColumn(byte[] zeroMask, int col, IntFixedList sequence) {
    for (int i = col; i < zeroMask.length; i += cols) {
      if (zeroMask[i] == STAR) {
        sequence.add(i);
        return i / cols;
      }
    }
    return NO_ASSIGNMENT;
  }

  /**
   * Find the primed zero (0') in the row and add it to the sequence.
   *
   * @param zeroMask the zero mask
   * @param row the row
   * @param sequence the sequence
   * @return the column
   */
  private int findPrimeInRow(byte[] zeroMask, int row, IntFixedList sequence) {
    final int start = row * cols;
    final int end = start + cols;
    for (int i = start; i < end; i++) {
      if (zeroMask[i] == PRIME) {
        sequence.add(i);
        return i - start;
      }
    }
    // Should not reach here.
    throw new AssertionError("Every starred zero will also have a primed zero in the row");
  }

  /**
   * Perform step 5.
   *
   * <pre>
   * Let h denote the smallest noncovered element of the
   * matrix; it will be positive. Add h to each covered row; then subtract
   * h from each uncovered column. Return to step 3 without altering
   * any asterisks, primes, or covered lines.
   * </pre>
   *
   * @param rowMask the row mask
   * @param colMask the column mask
   * @return the next step
   * @throws ArithmeticException if there is an overflow in the cost matrix
   */
  private int step5(boolean[] rowMask, boolean[] colMask) {
    final int h = findSmallestNonCovered(rowMask, colMask);

    // Add h to each covered row
    for (int row = 0; row < rows; row++) {
      if (rowMask[row]) {
        final int start = row * cols;
        final int end = start + cols;
        for (int i = start; i < end; i++) {
          cost[i] = addWithoutOverflow(cost[i], h);
        }
      }
    }

    // then subtract h from each uncovered column
    for (int col = 0; col < cols; col++) {
      if (!colMask[col]) {
        final int start = col;
        final int end = cost.length;
        for (int i = start; i < end; i += cols) {
          cost[i] -= h;
        }
      }
    }

    // Return to step 3
    return 3;
  }

  /**
   * Find the smallest non-covered element of the matrix; it will be positive.
   *
   * @param rowMask the row mask
   * @param colMask the column mask
   * @return the smallest element value
   */
  private int findSmallestNonCovered(boolean[] rowMask, boolean[] colMask) {
    int min = Integer.MAX_VALUE;
    for (int row = 0; row < rows; row++) {
      if (!rowMask[row]) {
        for (int col = 0; col < cols; col++) {
          if (!colMask[col]) {
            final int value = cost[row * cols + col];
            // Here the value cannot be zero so no check is made because
            // step 3 repeats until all zeros are covered, then step 5.
            if (min > value) {
              min = value;
            }
          }
        }
      }
    }
    return min;
  }

  /**
   * Adds the values without overflow.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @return the sum
   * @throws ArithmeticException if there is an overflow
   */
  @VisibleForTesting
  static int addWithoutOverflow(int value1, int value2) {
    final int result = value1 + value2;
    if (result < 0) {
      throw new ArithmeticException("Overflow in cost matrix");
    }
    return result;
  }
}
