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

import org.apache.commons.lang3.ArrayUtils;
import uk.ac.sussex.gdsc.core.utils.DoubleEquality;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Class to compute the assignment problem in polynomial time using the Jonker-Volgenant (LAPJV)
 * assignment algorithm, modified for rectangular matrices ({@code n x m}).
 *
 * <p>Complexity is {@code O(n^3)} for a square matrix of size {@code n}.
 *
 * <p>This is the same algorithm as the {@link JonkerVolgenantAssignment} adapted for floating-point
 * cost values. The algorithm requires adjusting the cost matrix using additions and subtractions.
 * This may accumulate errors for large matrices. A similar assignment can be computed by mapping
 * the input cost matrix to integer costs and using the {@link JonkerVolgenantAssignment} class.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Assignment_problem">Assignment problem</a>
 * @see <a href="https://doi.org/10.1007/BF02278710">Jonker and Volgenant (1987) A Shortest
 *      Augmenting Path Algorithm for Dense and Sparse Linear Assignment Problems. Computing 38,
 *      325-340.</a>
 * @see JonkerVolgenantAssignment
 */
public class DoubleJonkerVolgenantAssignment {
  /** A suitably large number. */
  private static final double INF = Double.MAX_VALUE;
  /** The default relative error. */
  private static final double RELATIVE_ERROR = 1e-9;
  /** The default absolute error. */
  private static final double ABSOLUTE_ERROR = 1e-16;

  /** The cost matrix. */
  private final double[][] cost;

  /**
   * Set to true if the cost matrix has been transposed.
   *
   * <p>The algorithm is faster when padding with empty rows rather than empty columns. An input
   * non-square matrix with more rows than columns should be transposed then padded with an empty
   * rows. The solution will return the mapping from columns to rows rather than rows to columns.
   */
  private final boolean transposed;

  /** The maximum relative error for cost equality. */
  private final double maxRelativeError;
  /** The maximum absolute error for cost equality. */
  private final double maxAbsoluteError;

  /**
   * Create a new instance. The cost matrix is assumed to be rectangular with
   * {@code rows <= columns}. If the matrix is transposed then the assignment result from
   * {@link #compute()} will be returned for rows assigned to each column; otherwise it is columns
   * assigned to each row.
   *
   * @param cost the cost of an assignment between row and column
   * @param transposed true if the matrix is transposed
   * @param maxRelativeError the maximum relative error for cost equality
   * @param maxAbsoluteError the maximum absolute error for cost equality
   */
  private DoubleJonkerVolgenantAssignment(double[][] cost, boolean transposed,
      double maxRelativeError, double maxAbsoluteError) {
    this.cost = cost;
    this.transposed = transposed;
    this.maxRelativeError = maxRelativeError;
    this.maxAbsoluteError = maxAbsoluteError;
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
   */
  public static int[] compute(double[][] cost) {
    return compute(cost, RELATIVE_ERROR, ABSOLUTE_ERROR);
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
   * @param maxRelativeError the maximum relative error for cost equality
   * @param maxAbsoluteError the maximum absolute error for cost equality
   * @return the assignments
   * @throws IllegalArgumentException if the array is null, empty or not rectangular
   */
  public static int[] compute(double[][] cost, double maxRelativeError, double maxAbsoluteError) {
    // Check the data
    final int rows = ArrayUtils.getLength(cost);
    ValidationUtils.checkStrictlyPositive(rows, "No rows");
    final int cols = ArrayUtils.getLength(cost[0]);
    ValidationUtils.checkStrictlyPositive(cols, "No columns");
    for (int i = 1; i < rows; i++) {
      ValidationUtils.checkArgument(ArrayUtils.getLength(cost[i]) == cols,
          "Irregular size on row[%d]", i);
    }
    // Create square matrix using zero padding
    final int n = Math.max(rows, cols);
    double[][] c;
    final boolean transposed = cols < n;
    if (transposed) {
      c = new double[cols][];
      for (int i = 0; i < cols; i++) {
        c[i] = new double[n];
        for (int j = 0; j < n; j++) {
          c[i][j] = cost[j][i];
        }
      }
    } else {
      c = cost;
    }
    return new DoubleJonkerVolgenantAssignment(c, transposed, maxRelativeError, maxAbsoluteError)
        .compute();
  }

  /**
   * Compute the assignments of rows to columns.
   *
   * <p>Given the {@code n x m} matrix, find a set of {@code k} independent elements
   * {@code k = min(n, m)} so that the sum of these elements is minimum.
   *
   * <p>The returned array will have length equal to {@code rows}. A value of -1 is used for no
   * assignment when there are more rows than columns.
   *
   * @param cost the cost of an assignment between row and column (as
   *        {@code cost(i,j) = [i * cols + j]}).
   * @param rows the rows
   * @param cols the columns
   * @return the assignments
   * @throws IllegalArgumentException if the array is null, empty or not equal to rows * columns
   */
  public static int[] compute(double[] cost, int rows, int cols) {
    return compute(cost, rows, cols, RELATIVE_ERROR, ABSOLUTE_ERROR);
  }

  /**
   * Compute the assignments of rows to columns.
   *
   * <p>Given the {@code n x m} matrix, find a set of {@code k} independent elements
   * {@code k = min(n, m)} so that the sum of these elements is minimum.
   *
   * <p>The returned array will have length equal to {@code rows}. A value of -1 is used for no
   * assignment when there are more rows than columns.
   *
   * @param cost the cost of an assignment between row and column (as
   *        {@code cost(i,j) = [i * cols + j]}).
   * @param rows the rows
   * @param cols the columns
   * @param maxRelativeError the maximum relative error for cost equality
   * @param maxAbsoluteError the maximum absolute error for cost equality
   * @return the assignments
   * @throws IllegalArgumentException if the array is null, empty or not equal to rows * columns
   */
  public static int[] compute(double[] cost, int rows, int cols, double maxRelativeError,
      double maxAbsoluteError) {
    SimpleArrayUtils.hasData2D(rows, cols, cost);
    // Convert to square matrix form using zero padding
    final int n = Math.max(rows, cols);
    final double[][] c;
    final boolean transposed = cols < n;
    if (transposed) {
      c = new double[cols][];
      for (int i = 0; i < cols; i++) {
        c[i] = new double[n];
        for (int j = 0; j < n; j++) {
          c[i][j] = cost[j * cols + i];
        }
      }
    } else {
      c = new double[rows][];
      for (int i = 0; i < rows; i++) {
        final double[] tmp = new double[n];
        System.arraycopy(cost, i * cols, tmp, 0, cols);
        c[i] = tmp;
      }
    }
    return new DoubleJonkerVolgenantAssignment(c, transposed, maxRelativeError, maxAbsoluteError)
        .compute();
  }

  /**
   * Compute the assignments of agents to tasks.
   *
   * <p>A value of -1 is used for no assignment.
   *
   * <p>If the matrix was transposed then the assignment result will be returned for rows assigned
   * to each column; otherwise it is columns assigned to each row.
   *
   * @return the assignments
   */
  private int[] compute() {
    // Note:
    // This is a port from the Pascal code listed in Jonker & Volgenant (1987).
    // The code uses row first naming convention: cost[row][col] OR cost[i][j]
    // The assignment required is created in 'x'.
    // It has been modified for the rectangular matrix where rows < columns.
    // See Volgenant (1996).

    // A modification has been made to use a floating-point equality check
    // for comparison of minimum and sub-minimum (u1 and u2). If the two
    // are approximately equal then the algorithm can stop augmenting reduction.

    // Assumes a matrix rows <= columns
    final int rows = cost.length;
    final int n = cost[0].length;

    // x: columns (j) assigned to rows (i)
    // y: rows (i) assigned to columns (j)
    // The original Fortran code uses 1-based indexing. So we replicate the
    // algorithm by storing indices plus 1. A value of zero is unassigned.
    // Thus the maximum supported matrix size is Integer.MAX_VALUE - 1.
    final int[] x = new int[rows];
    final int[] y = new int[n];

    // array of columns:
    // scanned (k=0 ... low-2),
    // labelled and unscanned (k=low-1 ... up-2),
    // unlabelled (k=up-1 ... n-1),
    final int[] col = new int[n];

    int f = 0;
    final int[] free = new int[rows];
    final double[] v = new double[n];
    if (rows < n) {
      // Modification for rows < columns: see Volgenant (1996).
      // No column reduction. The pad rows will have zero cost and the minimum
      // will always be zero. No reduction transfer as all rows are unassigned.
      // Initialise the columns
      for (int j = n - 1; j != 0; j--) {
        col[j] = j;
      }
      // All rows unassigned
      for (int i = rows - 1; i != 0; i--) {
        free[i] = i;
      }
      f = rows;
    } else {
      // Square matrix

      // COLUMN REDUCTION
      for (int j = n - 1; j >= 0; j--) {
        col[j] = j;
        double h = cost[0][j];
        int i1 = 0;
        for (int i = 1; i < n; i++) {
          if (cost[i][j] < h) {
            h = cost[i][j];
            i1 = i;
          }
        }
        v[j] = h;
        if (x[i1] == 0) {
          x[i1] = j + 1;
          y[j] = i1 + 1;
        } else {
          // x[i1] = -abs(x[i1])
          if (x[i1] > 0) {
            x[i1] = -x[i1];
          }
          // y is initialised to zero
          // y[j] = 0;
        }
      }

      // REDUCTION TRANSFER
      for (int i = 0; i < n; i++) {
        if (x[i] == 0) {
          // unassigned row in free-array
          free[f++] = i;
        } else if (x[i] < 0) {
          // no reduction transfer possible
          x[i] = -x[i];
        } else {
          // reduction transfer from assigned row
          final int j1 = x[i] - 1;
          double min = INF;
          for (int j = 0; j < n; j++) {
            if (j != j1) {
              if (cost[i][j] - v[j] < min) {
                min = cost[i][j] - v[j];
              }
            }
          }
          v[j1] -= min;
        }
      }

      if (f == 0) {
        // No unassigned rows
        return finaliseAssignments(x, y);
      }
    }

    // AUGMENTING ROW REDUCTION
    // routine applied twice
    for (int cnt = 0; cnt < 2; cnt++) {
      int k = 0;
      final int f0 = f;
      f = 0;
      while (k < f0) {
        final int i = free[k++];
        double u1 = cost[i][0] - v[0];
        int j1 = 0;
        int j2 = -1;
        double u2 = INF;
        for (int j = 1; j < n; j++) {
          final double h = cost[i][j] - v[j];
          if (h < u2) {
            if (h > u1) {
              u2 = h;
              j2 = j;
            } else {
              u2 = u1;
              u1 = h;
              j2 = j1;
              j1 = j;
            }
          }
        }
        int i1 = y[j1] - 1;
        // if (u1 < u2)
        // u1 = minimum cost; u2 = second minimum cost.
        // Allow some margin for floating-point relative error.
        final boolean better = !DoubleEquality.almostEqualRelativeOrAbsolute(u1, u2,
            maxRelativeError, maxAbsoluteError);
        if (better) {
          v[j1] = v[j1] - u2 + u1;
        } else if (i1 >= 0) {
          j1 = j2;
          i1 = y[j1] - 1;
        }
        if (i1 >= 0) {
          if (better) {
            free[--k] = i1;
          } else {
            free[f++] = i1;
          }
        }
        x[i] = j1 + 1;
        y[j1] = i + 1;
      }
    }

    // AUGMENTATION
    final int f0 = f;
    final double[] d = new double[n];
    final int[] pred = new int[n];
    for (f = 0; f < f0; f++) {
      final int i1 = free[f];
      int low = 0;
      int up = 0;
      // initialize d- and pred-array
      for (int j = 0; j < n; j++) {
        d[j] = cost[i1][j] - v[j];
        pred[j] = i1;
      }
      int last;
      int i;
      int j = -1;
      double min;
      // repeat: ends with goto AUGMENT
      OUTER: for (;;) {
        // This condition is always true due to the use of the do-while loop below
        // if (up == low) {
        // find columns with new value for minimum d
        last = low; // - 1
        min = d[col[up]];
        up++;
        for (int k = up; k < n; k++) {
          j = col[k];
          final double h = d[j];
          if (h <= min) {
            if (h < min) {
              up = low;
              min = h;
            }
            col[k] = col[up];
            col[up] = j;
            up++;
          }
        }
        for (int h = low; h < up; h++) {
          j = col[h];
          if (y[j] == 0) {
            // goto AUGMENT
            break OUTER;
          }
        }
        // } end if (up == low)

        // scan a row
        do {
          final int j1 = col[low++];
          i = y[j1] - 1;
          final double u1 = cost[i][j1] - v[j1] - min;
          for (int k = up; k < n; k++) {
            j = col[k];
            final double h = cost[i][j] - v[j] - u1;
            if (h < d[j]) {
              d[j] = h;
              pred[j] = i;
              if (h == min) {
                if (y[j] == 0) {
                  // goto AUGMENT
                  break OUTER;
                }
                col[k] = col[up];
                col[up++] = j;
              }
            }
          }
        } while (up != low);
      }

      // AUGMENT:
      // updating of column prices
      // k := 1 to last (here last is not (low - 1) but (low) so use '<' not '<='
      for (int k = 0; k < last; k++) {
        final int j0 = col[k];
        v[j0] += d[j0] - min;
      }

      // augmentation
      do {
        i = pred[j];
        y[j] = i + 1;
        final int k = j;
        j = x[i] - 1;
        x[i] = k + 1;
      } while (i1 != i);
    }

    return finaliseAssignments(x, y);
  }

  /**
   * Finalise the assignments. The indices in the assignments are offset by 1.
   *
   * @param x the columns assigned to rows
   * @param y the rows assigned to columns
   * @return the assignments
   */
  private int[] finaliseAssignments(final int[] x, final int[] y) {
    return finaliseAssignments(transposed ? y : x);
  }

  /**
   * Finalise the assignments. The indices in the assignments are offset by 1.
   *
   * @param n the number of rows
   * @param x the columns assigned to rows
   * @return the assignments
   */
  private static int[] finaliseAssignments(final int[] x) {
    // The assignment of i -> j is in x offset by 1
    for (int i = 0; i < x.length; i++) {
      x[i]--;
    }
    return x;
  }
}
