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

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for a Linear Assignment Problem (LAP) algorithm.
 */
@SuppressWarnings({"javadoc"})
class LinearAssignmentProblemTest {

  /**
   * Define the assignment algorithm.
   */
  public interface AssignmentAlgorithm {
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
     */
    int[] compute(int[][] cost);
  }

  /**
   * Base class for an assignment algorithm.
   */
  private abstract static class BaseAssignmentAlgorithm implements AssignmentAlgorithm {
    String name;

    BaseAssignmentAlgorithm(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Class for an int cost matrix assignment algorithm.
   */
  private static class IntAssignmentAlgorithm extends BaseAssignmentAlgorithm {
    private final Function<int[][], int[]> algorithm;

    IntAssignmentAlgorithm(String name, Function<int[][], int[]> algorithm) {
      super(name);
      this.algorithm = algorithm;
    }

    @Override
    public int[] compute(int[][] cost) {
      return algorithm.apply(cost);
    }
  }

  /**
   * Class to convert the cost matrix to a double for an assignment algorithm.
   */
  private static class DoubleAssignmentAlgorithm extends BaseAssignmentAlgorithm {
    private final Function<double[][], int[]> algorithm;

    DoubleAssignmentAlgorithm(String name, Function<double[][], int[]> algorithm) {
      super(name);
      this.algorithm = algorithm;
    }

    @Override
    public int[] compute(int[][] cost) {
      final int rows = cost.length;
      final double[][] c = new double[rows][];
      for (int i = 0; i < rows; i++) {
        c[i] = SimpleArrayUtils.toDouble(cost[i]);
      }
      return algorithm.apply(c);
    }
  }

  static class AlgorithmFactoryParams implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(new IntAssignmentAlgorithm("Kuhn-Munkres", KuhnMunkresAssignment::compute)),
          Arguments.of(new DoubleAssignmentAlgorithm("Double Kuhn-Munkres",
              DoubleKuhnMunkresAssignment::compute)),
          Arguments.of(
              new IntAssignmentAlgorithm("Jonker-Volgenant", JonkerVolgenantAssignment::compute)),
          Arguments.of(new DoubleAssignmentAlgorithm("Double Jonker-Volgenant",
              DoubleJonkerVolgenantAssignment::compute)));
    }
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment3x3Zero(AssignmentAlgorithm algorithm) {
    final int[][] cost = new int[3][3];
    final int[] assignments = algorithm.compute(cost);
    Assertions.assertEquals(3, assignments.length);
    for (int i = 0; i < 3; i++) {
      Assertions.assertNotEquals(-1, ArrayUtils.indexOf(assignments, i));
    }
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment3x3(AssignmentAlgorithm algorithm) {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final int[][] cost = {
        { 7, 5, 11 },
        { 5, 4, 1 },
        { 9, 3, 2 },
    };
    //@formatter:on
    // 7 + 1 + 3 == 11
    final int[] expected = {0, 2, 1};
    assertAssignment(algorithm, cost, expected);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment3x3Rotated(AssignmentAlgorithm algorithm) {
    // As above but rotated
    //@formatter:off
    final int[][] cost = {
        { 9, 5, 7 },
        { 3, 4, 5 },
        { 2, 1, 11 },
    };
    //@formatter:on
    // 7 + 3 + 1 == 11
    final int[] expected = {2, 0, 1};
    assertAssignment(algorithm, cost, expected);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment2x3(AssignmentAlgorithm algorithm) {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final int[][] cost = {
        { 7, 5, 11 },
        { 5, 4, 1 },
    };
    //@formatter:on
    // 5 + 1 == 6
    final int[] expected = {1, 2};
    assertAssignment(algorithm, cost, expected);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment3x2(AssignmentAlgorithm algorithm) {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final int[][] cost = {
        { 5, 7 },
        { 4, 5 },
        { 1, 11 },
    };
    //@formatter:on
    // 5 + 1 == 6
    final int[] expected = {-1, 1, 0};
    assertAssignment(algorithm, cost, expected);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment3x3b(AssignmentAlgorithm algorithm) {
    //@formatter:off
    final int[][] cost = {
        { 1, 2, 3 },
        { 2, 4, 6 },
        { 3, 6, 9 },
    };
    //@formatter:on
    // 3 + 4 + 3 == 10
    final int[] expected = {2, 1, 0};
    assertAssignment(algorithm, cost, expected);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testAssignment3x3c(AssignmentAlgorithm algorithm) {
    //@formatter:off
    final int[][] cost = {
        { 1, 10, 10 },
        { 10, 10, 0 },
        { 10, 2, 10 },
    };
    //@formatter:on
    // 1 + 0 + 2 == 3
    final int[] expected = {0, 2, 1};
    assertAssignment(algorithm, cost, expected);
  }

  // Permutations from
  // https://www.mathsisfun.com/combinatorics/combinations-permutations-calculator.html

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testRandomAssignment4x4(AssignmentAlgorithm algorithm) {
    assertRandomAssignment(algorithm,
        new int[][] {{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 2, 3, 1}, {0, 3, 1, 2},
            {0, 3, 2, 1}, {1, 0, 2, 3}, {1, 0, 3, 2}, {1, 2, 0, 3}, {1, 2, 3, 0}, {1, 3, 0, 2},
            {1, 3, 2, 0}, {2, 0, 1, 3}, {2, 0, 3, 1}, {2, 1, 0, 3}, {2, 1, 3, 0}, {2, 3, 0, 1},
            {2, 3, 1, 0}, {3, 0, 1, 2}, {3, 0, 2, 1}, {3, 1, 0, 2}, {3, 1, 2, 0}, {3, 2, 0, 1},
            {3, 2, 1, 0}});
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testRandomAssignment4x4WithBinaryCost(AssignmentAlgorithm algorithm) {
    final int costLimit = 2;
    assertRandomAssignment(algorithm, costLimit,
        new int[][] {{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 2, 3, 1}, {0, 3, 1, 2},
            {0, 3, 2, 1}, {1, 0, 2, 3}, {1, 0, 3, 2}, {1, 2, 0, 3}, {1, 2, 3, 0}, {1, 3, 0, 2},
            {1, 3, 2, 0}, {2, 0, 1, 3}, {2, 0, 3, 1}, {2, 1, 0, 3}, {2, 1, 3, 0}, {2, 3, 0, 1},
            {2, 3, 1, 0}, {3, 0, 1, 2}, {3, 0, 2, 1}, {3, 1, 0, 2}, {3, 1, 2, 0}, {3, 2, 0, 1},
            {3, 2, 1, 0}});
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testRandomAssignment4x3(AssignmentAlgorithm algorithm) {
    // As above but switch 3 to -1 for no assignment
    assertRandomAssignment(algorithm,
        new int[][] {{0, 1, 2, -1}, {0, 1, -1, 2}, {0, 2, 1, -1}, {0, 2, -1, 1}, {0, -1, 1, 2},
            {0, -1, 2, 1}, {1, 0, 2, -1}, {1, 0, -1, 2}, {1, 2, 0, -1}, {1, 2, -1, 0},
            {1, -1, 0, 2}, {1, -1, 2, 0}, {2, 0, 1, -1}, {2, 0, -1, 1}, {2, 1, 0, -1},
            {2, 1, -1, 0}, {2, -1, 0, 1}, {2, -1, 1, 0}, {-1, 0, 1, 2}, {-1, 0, 2, 1},
            {-1, 1, 0, 2}, {-1, 1, 2, 0}, {-1, 2, 0, 1}, {-1, 2, 1, 0}});
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testRandomAssignment3x4(AssignmentAlgorithm algorithm) {
    assertRandomAssignment(algorithm,
        new int[][] {{0, 1, 2}, {0, 1, 3}, {0, 2, 1}, {0, 2, 3}, {0, 3, 1}, {0, 3, 2}, {1, 0, 2},
            {1, 0, 3}, {1, 2, 0}, {1, 2, 3}, {1, 3, 0}, {1, 3, 2}, {2, 0, 1}, {2, 0, 3}, {2, 1, 0},
            {2, 1, 3}, {2, 3, 0}, {2, 3, 1}, {3, 0, 1}, {3, 0, 2}, {3, 1, 0}, {3, 1, 2}, {3, 2, 0},
            {3, 2, 1}});
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(AlgorithmFactoryParams.class)
  void testRandomAssignment6x6(AssignmentAlgorithm algorithm) {
    assertRandomAssignment(algorithm,
        new int[][] {{0, 1, 2, 3, 4, 5}, {0, 1, 2, 3, 5, 4}, {0, 1, 2, 4, 3, 5}, {0, 1, 2, 4, 5, 3},
            {0, 1, 2, 5, 3, 4}, {0, 1, 2, 5, 4, 3}, {0, 1, 3, 2, 4, 5}, {0, 1, 3, 2, 5, 4},
            {0, 1, 3, 4, 2, 5}, {0, 1, 3, 4, 5, 2}, {0, 1, 3, 5, 2, 4}, {0, 1, 3, 5, 4, 2},
            {0, 1, 4, 2, 3, 5}, {0, 1, 4, 2, 5, 3}, {0, 1, 4, 3, 2, 5}, {0, 1, 4, 3, 5, 2},
            {0, 1, 4, 5, 2, 3}, {0, 1, 4, 5, 3, 2}, {0, 1, 5, 2, 3, 4}, {0, 1, 5, 2, 4, 3},
            {0, 1, 5, 3, 2, 4}, {0, 1, 5, 3, 4, 2}, {0, 1, 5, 4, 2, 3}, {0, 1, 5, 4, 3, 2},
            {0, 2, 1, 3, 4, 5}, {0, 2, 1, 3, 5, 4}, {0, 2, 1, 4, 3, 5}, {0, 2, 1, 4, 5, 3},
            {0, 2, 1, 5, 3, 4}, {0, 2, 1, 5, 4, 3}, {0, 2, 3, 1, 4, 5}, {0, 2, 3, 1, 5, 4},
            {0, 2, 3, 4, 1, 5}, {0, 2, 3, 4, 5, 1}, {0, 2, 3, 5, 1, 4}, {0, 2, 3, 5, 4, 1},
            {0, 2, 4, 1, 3, 5}, {0, 2, 4, 1, 5, 3}, {0, 2, 4, 3, 1, 5}, {0, 2, 4, 3, 5, 1},
            {0, 2, 4, 5, 1, 3}, {0, 2, 4, 5, 3, 1}, {0, 2, 5, 1, 3, 4}, {0, 2, 5, 1, 4, 3},
            {0, 2, 5, 3, 1, 4}, {0, 2, 5, 3, 4, 1}, {0, 2, 5, 4, 1, 3}, {0, 2, 5, 4, 3, 1},
            {0, 3, 1, 2, 4, 5}, {0, 3, 1, 2, 5, 4}, {0, 3, 1, 4, 2, 5}, {0, 3, 1, 4, 5, 2},
            {0, 3, 1, 5, 2, 4}, {0, 3, 1, 5, 4, 2}, {0, 3, 2, 1, 4, 5}, {0, 3, 2, 1, 5, 4},
            {0, 3, 2, 4, 1, 5}, {0, 3, 2, 4, 5, 1}, {0, 3, 2, 5, 1, 4}, {0, 3, 2, 5, 4, 1},
            {0, 3, 4, 1, 2, 5}, {0, 3, 4, 1, 5, 2}, {0, 3, 4, 2, 1, 5}, {0, 3, 4, 2, 5, 1},
            {0, 3, 4, 5, 1, 2}, {0, 3, 4, 5, 2, 1}, {0, 3, 5, 1, 2, 4}, {0, 3, 5, 1, 4, 2},
            {0, 3, 5, 2, 1, 4}, {0, 3, 5, 2, 4, 1}, {0, 3, 5, 4, 1, 2}, {0, 3, 5, 4, 2, 1},
            {0, 4, 1, 2, 3, 5}, {0, 4, 1, 2, 5, 3}, {0, 4, 1, 3, 2, 5}, {0, 4, 1, 3, 5, 2},
            {0, 4, 1, 5, 2, 3}, {0, 4, 1, 5, 3, 2}, {0, 4, 2, 1, 3, 5}, {0, 4, 2, 1, 5, 3},
            {0, 4, 2, 3, 1, 5}, {0, 4, 2, 3, 5, 1}, {0, 4, 2, 5, 1, 3}, {0, 4, 2, 5, 3, 1},
            {0, 4, 3, 1, 2, 5}, {0, 4, 3, 1, 5, 2}, {0, 4, 3, 2, 1, 5}, {0, 4, 3, 2, 5, 1},
            {0, 4, 3, 5, 1, 2}, {0, 4, 3, 5, 2, 1}, {0, 4, 5, 1, 2, 3}, {0, 4, 5, 1, 3, 2},
            {0, 4, 5, 2, 1, 3}, {0, 4, 5, 2, 3, 1}, {0, 4, 5, 3, 1, 2}, {0, 4, 5, 3, 2, 1},
            {0, 5, 1, 2, 3, 4}, {0, 5, 1, 2, 4, 3}, {0, 5, 1, 3, 2, 4}, {0, 5, 1, 3, 4, 2},
            {0, 5, 1, 4, 2, 3}, {0, 5, 1, 4, 3, 2}, {0, 5, 2, 1, 3, 4}, {0, 5, 2, 1, 4, 3},
            {0, 5, 2, 3, 1, 4}, {0, 5, 2, 3, 4, 1}, {0, 5, 2, 4, 1, 3}, {0, 5, 2, 4, 3, 1},
            {0, 5, 3, 1, 2, 4}, {0, 5, 3, 1, 4, 2}, {0, 5, 3, 2, 1, 4}, {0, 5, 3, 2, 4, 1},
            {0, 5, 3, 4, 1, 2}, {0, 5, 3, 4, 2, 1}, {0, 5, 4, 1, 2, 3}, {0, 5, 4, 1, 3, 2},
            {0, 5, 4, 2, 1, 3}, {0, 5, 4, 2, 3, 1}, {0, 5, 4, 3, 1, 2}, {0, 5, 4, 3, 2, 1},
            {1, 0, 2, 3, 4, 5}, {1, 0, 2, 3, 5, 4}, {1, 0, 2, 4, 3, 5}, {1, 0, 2, 4, 5, 3},
            {1, 0, 2, 5, 3, 4}, {1, 0, 2, 5, 4, 3}, {1, 0, 3, 2, 4, 5}, {1, 0, 3, 2, 5, 4},
            {1, 0, 3, 4, 2, 5}, {1, 0, 3, 4, 5, 2}, {1, 0, 3, 5, 2, 4}, {1, 0, 3, 5, 4, 2},
            {1, 0, 4, 2, 3, 5}, {1, 0, 4, 2, 5, 3}, {1, 0, 4, 3, 2, 5}, {1, 0, 4, 3, 5, 2},
            {1, 0, 4, 5, 2, 3}, {1, 0, 4, 5, 3, 2}, {1, 0, 5, 2, 3, 4}, {1, 0, 5, 2, 4, 3},
            {1, 0, 5, 3, 2, 4}, {1, 0, 5, 3, 4, 2}, {1, 0, 5, 4, 2, 3}, {1, 0, 5, 4, 3, 2},
            {1, 2, 0, 3, 4, 5}, {1, 2, 0, 3, 5, 4}, {1, 2, 0, 4, 3, 5}, {1, 2, 0, 4, 5, 3},
            {1, 2, 0, 5, 3, 4}, {1, 2, 0, 5, 4, 3}, {1, 2, 3, 0, 4, 5}, {1, 2, 3, 0, 5, 4},
            {1, 2, 3, 4, 0, 5}, {1, 2, 3, 4, 5, 0}, {1, 2, 3, 5, 0, 4}, {1, 2, 3, 5, 4, 0},
            {1, 2, 4, 0, 3, 5}, {1, 2, 4, 0, 5, 3}, {1, 2, 4, 3, 0, 5}, {1, 2, 4, 3, 5, 0},
            {1, 2, 4, 5, 0, 3}, {1, 2, 4, 5, 3, 0}, {1, 2, 5, 0, 3, 4}, {1, 2, 5, 0, 4, 3},
            {1, 2, 5, 3, 0, 4}, {1, 2, 5, 3, 4, 0}, {1, 2, 5, 4, 0, 3}, {1, 2, 5, 4, 3, 0},
            {1, 3, 0, 2, 4, 5}, {1, 3, 0, 2, 5, 4}, {1, 3, 0, 4, 2, 5}, {1, 3, 0, 4, 5, 2},
            {1, 3, 0, 5, 2, 4}, {1, 3, 0, 5, 4, 2}, {1, 3, 2, 0, 4, 5}, {1, 3, 2, 0, 5, 4},
            {1, 3, 2, 4, 0, 5}, {1, 3, 2, 4, 5, 0}, {1, 3, 2, 5, 0, 4}, {1, 3, 2, 5, 4, 0},
            {1, 3, 4, 0, 2, 5}, {1, 3, 4, 0, 5, 2}, {1, 3, 4, 2, 0, 5}, {1, 3, 4, 2, 5, 0},
            {1, 3, 4, 5, 0, 2}, {1, 3, 4, 5, 2, 0}, {1, 3, 5, 0, 2, 4}, {1, 3, 5, 0, 4, 2},
            {1, 3, 5, 2, 0, 4}, {1, 3, 5, 2, 4, 0}, {1, 3, 5, 4, 0, 2}, {1, 3, 5, 4, 2, 0},
            {1, 4, 0, 2, 3, 5}, {1, 4, 0, 2, 5, 3}, {1, 4, 0, 3, 2, 5}, {1, 4, 0, 3, 5, 2},
            {1, 4, 0, 5, 2, 3}, {1, 4, 0, 5, 3, 2}, {1, 4, 2, 0, 3, 5}, {1, 4, 2, 0, 5, 3},
            {1, 4, 2, 3, 0, 5}, {1, 4, 2, 3, 5, 0}, {1, 4, 2, 5, 0, 3}, {1, 4, 2, 5, 3, 0},
            {1, 4, 3, 0, 2, 5}, {1, 4, 3, 0, 5, 2}, {1, 4, 3, 2, 0, 5}, {1, 4, 3, 2, 5, 0},
            {1, 4, 3, 5, 0, 2}, {1, 4, 3, 5, 2, 0}, {1, 4, 5, 0, 2, 3}, {1, 4, 5, 0, 3, 2},
            {1, 4, 5, 2, 0, 3}, {1, 4, 5, 2, 3, 0}, {1, 4, 5, 3, 0, 2}, {1, 4, 5, 3, 2, 0},
            {1, 5, 0, 2, 3, 4}, {1, 5, 0, 2, 4, 3}, {1, 5, 0, 3, 2, 4}, {1, 5, 0, 3, 4, 2},
            {1, 5, 0, 4, 2, 3}, {1, 5, 0, 4, 3, 2}, {1, 5, 2, 0, 3, 4}, {1, 5, 2, 0, 4, 3},
            {1, 5, 2, 3, 0, 4}, {1, 5, 2, 3, 4, 0}, {1, 5, 2, 4, 0, 3}, {1, 5, 2, 4, 3, 0},
            {1, 5, 3, 0, 2, 4}, {1, 5, 3, 0, 4, 2}, {1, 5, 3, 2, 0, 4}, {1, 5, 3, 2, 4, 0},
            {1, 5, 3, 4, 0, 2}, {1, 5, 3, 4, 2, 0}, {1, 5, 4, 0, 2, 3}, {1, 5, 4, 0, 3, 2},
            {1, 5, 4, 2, 0, 3}, {1, 5, 4, 2, 3, 0}, {1, 5, 4, 3, 0, 2}, {1, 5, 4, 3, 2, 0},
            {2, 0, 1, 3, 4, 5}, {2, 0, 1, 3, 5, 4}, {2, 0, 1, 4, 3, 5}, {2, 0, 1, 4, 5, 3},
            {2, 0, 1, 5, 3, 4}, {2, 0, 1, 5, 4, 3}, {2, 0, 3, 1, 4, 5}, {2, 0, 3, 1, 5, 4},
            {2, 0, 3, 4, 1, 5}, {2, 0, 3, 4, 5, 1}, {2, 0, 3, 5, 1, 4}, {2, 0, 3, 5, 4, 1},
            {2, 0, 4, 1, 3, 5}, {2, 0, 4, 1, 5, 3}, {2, 0, 4, 3, 1, 5}, {2, 0, 4, 3, 5, 1},
            {2, 0, 4, 5, 1, 3}, {2, 0, 4, 5, 3, 1}, {2, 0, 5, 1, 3, 4}, {2, 0, 5, 1, 4, 3},
            {2, 0, 5, 3, 1, 4}, {2, 0, 5, 3, 4, 1}, {2, 0, 5, 4, 1, 3}, {2, 0, 5, 4, 3, 1},
            {2, 1, 0, 3, 4, 5}, {2, 1, 0, 3, 5, 4}, {2, 1, 0, 4, 3, 5}, {2, 1, 0, 4, 5, 3},
            {2, 1, 0, 5, 3, 4}, {2, 1, 0, 5, 4, 3}, {2, 1, 3, 0, 4, 5}, {2, 1, 3, 0, 5, 4},
            {2, 1, 3, 4, 0, 5}, {2, 1, 3, 4, 5, 0}, {2, 1, 3, 5, 0, 4}, {2, 1, 3, 5, 4, 0},
            {2, 1, 4, 0, 3, 5}, {2, 1, 4, 0, 5, 3}, {2, 1, 4, 3, 0, 5}, {2, 1, 4, 3, 5, 0},
            {2, 1, 4, 5, 0, 3}, {2, 1, 4, 5, 3, 0}, {2, 1, 5, 0, 3, 4}, {2, 1, 5, 0, 4, 3},
            {2, 1, 5, 3, 0, 4}, {2, 1, 5, 3, 4, 0}, {2, 1, 5, 4, 0, 3}, {2, 1, 5, 4, 3, 0},
            {2, 3, 0, 1, 4, 5}, {2, 3, 0, 1, 5, 4}, {2, 3, 0, 4, 1, 5}, {2, 3, 0, 4, 5, 1},
            {2, 3, 0, 5, 1, 4}, {2, 3, 0, 5, 4, 1}, {2, 3, 1, 0, 4, 5}, {2, 3, 1, 0, 5, 4},
            {2, 3, 1, 4, 0, 5}, {2, 3, 1, 4, 5, 0}, {2, 3, 1, 5, 0, 4}, {2, 3, 1, 5, 4, 0},
            {2, 3, 4, 0, 1, 5}, {2, 3, 4, 0, 5, 1}, {2, 3, 4, 1, 0, 5}, {2, 3, 4, 1, 5, 0},
            {2, 3, 4, 5, 0, 1}, {2, 3, 4, 5, 1, 0}, {2, 3, 5, 0, 1, 4}, {2, 3, 5, 0, 4, 1},
            {2, 3, 5, 1, 0, 4}, {2, 3, 5, 1, 4, 0}, {2, 3, 5, 4, 0, 1}, {2, 3, 5, 4, 1, 0},
            {2, 4, 0, 1, 3, 5}, {2, 4, 0, 1, 5, 3}, {2, 4, 0, 3, 1, 5}, {2, 4, 0, 3, 5, 1},
            {2, 4, 0, 5, 1, 3}, {2, 4, 0, 5, 3, 1}, {2, 4, 1, 0, 3, 5}, {2, 4, 1, 0, 5, 3},
            {2, 4, 1, 3, 0, 5}, {2, 4, 1, 3, 5, 0}, {2, 4, 1, 5, 0, 3}, {2, 4, 1, 5, 3, 0},
            {2, 4, 3, 0, 1, 5}, {2, 4, 3, 0, 5, 1}, {2, 4, 3, 1, 0, 5}, {2, 4, 3, 1, 5, 0},
            {2, 4, 3, 5, 0, 1}, {2, 4, 3, 5, 1, 0}, {2, 4, 5, 0, 1, 3}, {2, 4, 5, 0, 3, 1},
            {2, 4, 5, 1, 0, 3}, {2, 4, 5, 1, 3, 0}, {2, 4, 5, 3, 0, 1}, {2, 4, 5, 3, 1, 0},
            {2, 5, 0, 1, 3, 4}, {2, 5, 0, 1, 4, 3}, {2, 5, 0, 3, 1, 4}, {2, 5, 0, 3, 4, 1},
            {2, 5, 0, 4, 1, 3}, {2, 5, 0, 4, 3, 1}, {2, 5, 1, 0, 3, 4}, {2, 5, 1, 0, 4, 3},
            {2, 5, 1, 3, 0, 4}, {2, 5, 1, 3, 4, 0}, {2, 5, 1, 4, 0, 3}, {2, 5, 1, 4, 3, 0},
            {2, 5, 3, 0, 1, 4}, {2, 5, 3, 0, 4, 1}, {2, 5, 3, 1, 0, 4}, {2, 5, 3, 1, 4, 0},
            {2, 5, 3, 4, 0, 1}, {2, 5, 3, 4, 1, 0}, {2, 5, 4, 0, 1, 3}, {2, 5, 4, 0, 3, 1},
            {2, 5, 4, 1, 0, 3}, {2, 5, 4, 1, 3, 0}, {2, 5, 4, 3, 0, 1}, {2, 5, 4, 3, 1, 0},
            {3, 0, 1, 2, 4, 5}, {3, 0, 1, 2, 5, 4}, {3, 0, 1, 4, 2, 5}, {3, 0, 1, 4, 5, 2},
            {3, 0, 1, 5, 2, 4}, {3, 0, 1, 5, 4, 2}, {3, 0, 2, 1, 4, 5}, {3, 0, 2, 1, 5, 4},
            {3, 0, 2, 4, 1, 5}, {3, 0, 2, 4, 5, 1}, {3, 0, 2, 5, 1, 4}, {3, 0, 2, 5, 4, 1},
            {3, 0, 4, 1, 2, 5}, {3, 0, 4, 1, 5, 2}, {3, 0, 4, 2, 1, 5}, {3, 0, 4, 2, 5, 1},
            {3, 0, 4, 5, 1, 2}, {3, 0, 4, 5, 2, 1}, {3, 0, 5, 1, 2, 4}, {3, 0, 5, 1, 4, 2},
            {3, 0, 5, 2, 1, 4}, {3, 0, 5, 2, 4, 1}, {3, 0, 5, 4, 1, 2}, {3, 0, 5, 4, 2, 1},
            {3, 1, 0, 2, 4, 5}, {3, 1, 0, 2, 5, 4}, {3, 1, 0, 4, 2, 5}, {3, 1, 0, 4, 5, 2},
            {3, 1, 0, 5, 2, 4}, {3, 1, 0, 5, 4, 2}, {3, 1, 2, 0, 4, 5}, {3, 1, 2, 0, 5, 4},
            {3, 1, 2, 4, 0, 5}, {3, 1, 2, 4, 5, 0}, {3, 1, 2, 5, 0, 4}, {3, 1, 2, 5, 4, 0},
            {3, 1, 4, 0, 2, 5}, {3, 1, 4, 0, 5, 2}, {3, 1, 4, 2, 0, 5}, {3, 1, 4, 2, 5, 0},
            {3, 1, 4, 5, 0, 2}, {3, 1, 4, 5, 2, 0}, {3, 1, 5, 0, 2, 4}, {3, 1, 5, 0, 4, 2},
            {3, 1, 5, 2, 0, 4}, {3, 1, 5, 2, 4, 0}, {3, 1, 5, 4, 0, 2}, {3, 1, 5, 4, 2, 0},
            {3, 2, 0, 1, 4, 5}, {3, 2, 0, 1, 5, 4}, {3, 2, 0, 4, 1, 5}, {3, 2, 0, 4, 5, 1},
            {3, 2, 0, 5, 1, 4}, {3, 2, 0, 5, 4, 1}, {3, 2, 1, 0, 4, 5}, {3, 2, 1, 0, 5, 4},
            {3, 2, 1, 4, 0, 5}, {3, 2, 1, 4, 5, 0}, {3, 2, 1, 5, 0, 4}, {3, 2, 1, 5, 4, 0},
            {3, 2, 4, 0, 1, 5}, {3, 2, 4, 0, 5, 1}, {3, 2, 4, 1, 0, 5}, {3, 2, 4, 1, 5, 0},
            {3, 2, 4, 5, 0, 1}, {3, 2, 4, 5, 1, 0}, {3, 2, 5, 0, 1, 4}, {3, 2, 5, 0, 4, 1},
            {3, 2, 5, 1, 0, 4}, {3, 2, 5, 1, 4, 0}, {3, 2, 5, 4, 0, 1}, {3, 2, 5, 4, 1, 0},
            {3, 4, 0, 1, 2, 5}, {3, 4, 0, 1, 5, 2}, {3, 4, 0, 2, 1, 5}, {3, 4, 0, 2, 5, 1},
            {3, 4, 0, 5, 1, 2}, {3, 4, 0, 5, 2, 1}, {3, 4, 1, 0, 2, 5}, {3, 4, 1, 0, 5, 2},
            {3, 4, 1, 2, 0, 5}, {3, 4, 1, 2, 5, 0}, {3, 4, 1, 5, 0, 2}, {3, 4, 1, 5, 2, 0},
            {3, 4, 2, 0, 1, 5}, {3, 4, 2, 0, 5, 1}, {3, 4, 2, 1, 0, 5}, {3, 4, 2, 1, 5, 0},
            {3, 4, 2, 5, 0, 1}, {3, 4, 2, 5, 1, 0}, {3, 4, 5, 0, 1, 2}, {3, 4, 5, 0, 2, 1},
            {3, 4, 5, 1, 0, 2}, {3, 4, 5, 1, 2, 0}, {3, 4, 5, 2, 0, 1}, {3, 4, 5, 2, 1, 0},
            {3, 5, 0, 1, 2, 4}, {3, 5, 0, 1, 4, 2}, {3, 5, 0, 2, 1, 4}, {3, 5, 0, 2, 4, 1},
            {3, 5, 0, 4, 1, 2}, {3, 5, 0, 4, 2, 1}, {3, 5, 1, 0, 2, 4}, {3, 5, 1, 0, 4, 2},
            {3, 5, 1, 2, 0, 4}, {3, 5, 1, 2, 4, 0}, {3, 5, 1, 4, 0, 2}, {3, 5, 1, 4, 2, 0},
            {3, 5, 2, 0, 1, 4}, {3, 5, 2, 0, 4, 1}, {3, 5, 2, 1, 0, 4}, {3, 5, 2, 1, 4, 0},
            {3, 5, 2, 4, 0, 1}, {3, 5, 2, 4, 1, 0}, {3, 5, 4, 0, 1, 2}, {3, 5, 4, 0, 2, 1},
            {3, 5, 4, 1, 0, 2}, {3, 5, 4, 1, 2, 0}, {3, 5, 4, 2, 0, 1}, {3, 5, 4, 2, 1, 0},
            {4, 0, 1, 2, 3, 5}, {4, 0, 1, 2, 5, 3}, {4, 0, 1, 3, 2, 5}, {4, 0, 1, 3, 5, 2},
            {4, 0, 1, 5, 2, 3}, {4, 0, 1, 5, 3, 2}, {4, 0, 2, 1, 3, 5}, {4, 0, 2, 1, 5, 3},
            {4, 0, 2, 3, 1, 5}, {4, 0, 2, 3, 5, 1}, {4, 0, 2, 5, 1, 3}, {4, 0, 2, 5, 3, 1},
            {4, 0, 3, 1, 2, 5}, {4, 0, 3, 1, 5, 2}, {4, 0, 3, 2, 1, 5}, {4, 0, 3, 2, 5, 1},
            {4, 0, 3, 5, 1, 2}, {4, 0, 3, 5, 2, 1}, {4, 0, 5, 1, 2, 3}, {4, 0, 5, 1, 3, 2},
            {4, 0, 5, 2, 1, 3}, {4, 0, 5, 2, 3, 1}, {4, 0, 5, 3, 1, 2}, {4, 0, 5, 3, 2, 1},
            {4, 1, 0, 2, 3, 5}, {4, 1, 0, 2, 5, 3}, {4, 1, 0, 3, 2, 5}, {4, 1, 0, 3, 5, 2},
            {4, 1, 0, 5, 2, 3}, {4, 1, 0, 5, 3, 2}, {4, 1, 2, 0, 3, 5}, {4, 1, 2, 0, 5, 3},
            {4, 1, 2, 3, 0, 5}, {4, 1, 2, 3, 5, 0}, {4, 1, 2, 5, 0, 3}, {4, 1, 2, 5, 3, 0},
            {4, 1, 3, 0, 2, 5}, {4, 1, 3, 0, 5, 2}, {4, 1, 3, 2, 0, 5}, {4, 1, 3, 2, 5, 0},
            {4, 1, 3, 5, 0, 2}, {4, 1, 3, 5, 2, 0}, {4, 1, 5, 0, 2, 3}, {4, 1, 5, 0, 3, 2},
            {4, 1, 5, 2, 0, 3}, {4, 1, 5, 2, 3, 0}, {4, 1, 5, 3, 0, 2}, {4, 1, 5, 3, 2, 0},
            {4, 2, 0, 1, 3, 5}, {4, 2, 0, 1, 5, 3}, {4, 2, 0, 3, 1, 5}, {4, 2, 0, 3, 5, 1},
            {4, 2, 0, 5, 1, 3}, {4, 2, 0, 5, 3, 1}, {4, 2, 1, 0, 3, 5}, {4, 2, 1, 0, 5, 3},
            {4, 2, 1, 3, 0, 5}, {4, 2, 1, 3, 5, 0}, {4, 2, 1, 5, 0, 3}, {4, 2, 1, 5, 3, 0},
            {4, 2, 3, 0, 1, 5}, {4, 2, 3, 0, 5, 1}, {4, 2, 3, 1, 0, 5}, {4, 2, 3, 1, 5, 0},
            {4, 2, 3, 5, 0, 1}, {4, 2, 3, 5, 1, 0}, {4, 2, 5, 0, 1, 3}, {4, 2, 5, 0, 3, 1},
            {4, 2, 5, 1, 0, 3}, {4, 2, 5, 1, 3, 0}, {4, 2, 5, 3, 0, 1}, {4, 2, 5, 3, 1, 0},
            {4, 3, 0, 1, 2, 5}, {4, 3, 0, 1, 5, 2}, {4, 3, 0, 2, 1, 5}, {4, 3, 0, 2, 5, 1},
            {4, 3, 0, 5, 1, 2}, {4, 3, 0, 5, 2, 1}, {4, 3, 1, 0, 2, 5}, {4, 3, 1, 0, 5, 2},
            {4, 3, 1, 2, 0, 5}, {4, 3, 1, 2, 5, 0}, {4, 3, 1, 5, 0, 2}, {4, 3, 1, 5, 2, 0},
            {4, 3, 2, 0, 1, 5}, {4, 3, 2, 0, 5, 1}, {4, 3, 2, 1, 0, 5}, {4, 3, 2, 1, 5, 0},
            {4, 3, 2, 5, 0, 1}, {4, 3, 2, 5, 1, 0}, {4, 3, 5, 0, 1, 2}, {4, 3, 5, 0, 2, 1},
            {4, 3, 5, 1, 0, 2}, {4, 3, 5, 1, 2, 0}, {4, 3, 5, 2, 0, 1}, {4, 3, 5, 2, 1, 0},
            {4, 5, 0, 1, 2, 3}, {4, 5, 0, 1, 3, 2}, {4, 5, 0, 2, 1, 3}, {4, 5, 0, 2, 3, 1},
            {4, 5, 0, 3, 1, 2}, {4, 5, 0, 3, 2, 1}, {4, 5, 1, 0, 2, 3}, {4, 5, 1, 0, 3, 2},
            {4, 5, 1, 2, 0, 3}, {4, 5, 1, 2, 3, 0}, {4, 5, 1, 3, 0, 2}, {4, 5, 1, 3, 2, 0},
            {4, 5, 2, 0, 1, 3}, {4, 5, 2, 0, 3, 1}, {4, 5, 2, 1, 0, 3}, {4, 5, 2, 1, 3, 0},
            {4, 5, 2, 3, 0, 1}, {4, 5, 2, 3, 1, 0}, {4, 5, 3, 0, 1, 2}, {4, 5, 3, 0, 2, 1},
            {4, 5, 3, 1, 0, 2}, {4, 5, 3, 1, 2, 0}, {4, 5, 3, 2, 0, 1}, {4, 5, 3, 2, 1, 0},
            {5, 0, 1, 2, 3, 4}, {5, 0, 1, 2, 4, 3}, {5, 0, 1, 3, 2, 4}, {5, 0, 1, 3, 4, 2},
            {5, 0, 1, 4, 2, 3}, {5, 0, 1, 4, 3, 2}, {5, 0, 2, 1, 3, 4}, {5, 0, 2, 1, 4, 3},
            {5, 0, 2, 3, 1, 4}, {5, 0, 2, 3, 4, 1}, {5, 0, 2, 4, 1, 3}, {5, 0, 2, 4, 3, 1},
            {5, 0, 3, 1, 2, 4}, {5, 0, 3, 1, 4, 2}, {5, 0, 3, 2, 1, 4}, {5, 0, 3, 2, 4, 1},
            {5, 0, 3, 4, 1, 2}, {5, 0, 3, 4, 2, 1}, {5, 0, 4, 1, 2, 3}, {5, 0, 4, 1, 3, 2},
            {5, 0, 4, 2, 1, 3}, {5, 0, 4, 2, 3, 1}, {5, 0, 4, 3, 1, 2}, {5, 0, 4, 3, 2, 1},
            {5, 1, 0, 2, 3, 4}, {5, 1, 0, 2, 4, 3}, {5, 1, 0, 3, 2, 4}, {5, 1, 0, 3, 4, 2},
            {5, 1, 0, 4, 2, 3}, {5, 1, 0, 4, 3, 2}, {5, 1, 2, 0, 3, 4}, {5, 1, 2, 0, 4, 3},
            {5, 1, 2, 3, 0, 4}, {5, 1, 2, 3, 4, 0}, {5, 1, 2, 4, 0, 3}, {5, 1, 2, 4, 3, 0},
            {5, 1, 3, 0, 2, 4}, {5, 1, 3, 0, 4, 2}, {5, 1, 3, 2, 0, 4}, {5, 1, 3, 2, 4, 0},
            {5, 1, 3, 4, 0, 2}, {5, 1, 3, 4, 2, 0}, {5, 1, 4, 0, 2, 3}, {5, 1, 4, 0, 3, 2},
            {5, 1, 4, 2, 0, 3}, {5, 1, 4, 2, 3, 0}, {5, 1, 4, 3, 0, 2}, {5, 1, 4, 3, 2, 0},
            {5, 2, 0, 1, 3, 4}, {5, 2, 0, 1, 4, 3}, {5, 2, 0, 3, 1, 4}, {5, 2, 0, 3, 4, 1},
            {5, 2, 0, 4, 1, 3}, {5, 2, 0, 4, 3, 1}, {5, 2, 1, 0, 3, 4}, {5, 2, 1, 0, 4, 3},
            {5, 2, 1, 3, 0, 4}, {5, 2, 1, 3, 4, 0}, {5, 2, 1, 4, 0, 3}, {5, 2, 1, 4, 3, 0},
            {5, 2, 3, 0, 1, 4}, {5, 2, 3, 0, 4, 1}, {5, 2, 3, 1, 0, 4}, {5, 2, 3, 1, 4, 0},
            {5, 2, 3, 4, 0, 1}, {5, 2, 3, 4, 1, 0}, {5, 2, 4, 0, 1, 3}, {5, 2, 4, 0, 3, 1},
            {5, 2, 4, 1, 0, 3}, {5, 2, 4, 1, 3, 0}, {5, 2, 4, 3, 0, 1}, {5, 2, 4, 3, 1, 0},
            {5, 3, 0, 1, 2, 4}, {5, 3, 0, 1, 4, 2}, {5, 3, 0, 2, 1, 4}, {5, 3, 0, 2, 4, 1},
            {5, 3, 0, 4, 1, 2}, {5, 3, 0, 4, 2, 1}, {5, 3, 1, 0, 2, 4}, {5, 3, 1, 0, 4, 2},
            {5, 3, 1, 2, 0, 4}, {5, 3, 1, 2, 4, 0}, {5, 3, 1, 4, 0, 2}, {5, 3, 1, 4, 2, 0},
            {5, 3, 2, 0, 1, 4}, {5, 3, 2, 0, 4, 1}, {5, 3, 2, 1, 0, 4}, {5, 3, 2, 1, 4, 0},
            {5, 3, 2, 4, 0, 1}, {5, 3, 2, 4, 1, 0}, {5, 3, 4, 0, 1, 2}, {5, 3, 4, 0, 2, 1},
            {5, 3, 4, 1, 0, 2}, {5, 3, 4, 1, 2, 0}, {5, 3, 4, 2, 0, 1}, {5, 3, 4, 2, 1, 0},
            {5, 4, 0, 1, 2, 3}, {5, 4, 0, 1, 3, 2}, {5, 4, 0, 2, 1, 3}, {5, 4, 0, 2, 3, 1},
            {5, 4, 0, 3, 1, 2}, {5, 4, 0, 3, 2, 1}, {5, 4, 1, 0, 2, 3}, {5, 4, 1, 0, 3, 2},
            {5, 4, 1, 2, 0, 3}, {5, 4, 1, 2, 3, 0}, {5, 4, 1, 3, 0, 2}, {5, 4, 1, 3, 2, 0},
            {5, 4, 2, 0, 1, 3}, {5, 4, 2, 0, 3, 1}, {5, 4, 2, 1, 0, 3}, {5, 4, 2, 1, 3, 0},
            {5, 4, 2, 3, 0, 1}, {5, 4, 2, 3, 1, 0}, {5, 4, 3, 0, 1, 2}, {5, 4, 3, 0, 2, 1},
            {5, 4, 3, 1, 0, 2}, {5, 4, 3, 1, 2, 0}, {5, 4, 3, 2, 0, 1}, {5, 4, 3, 2, 1, 0}});
  }

  /**
   * Assert the assignments from a random cost matrix. The input is all possible permutations for
   * the agents to the tasks. Use -1 for no assignment.
   *
   * @param algorithm the algorithm
   * @param permutations the permutations
   */
  private static void assertRandomAssignment(AssignmentAlgorithm algorithm, int[][] permutations) {
    assertRandomAssignment(algorithm, 0, permutations);
  }

  /**
   * Assert the assignments from a random cost matrix. The input is all possible permutations for
   * the agents to the tasks. Use -1 for no assignment.
   *
   * @param algorithm the algorithm
   * @param costLimit the cost limit (maximum cost)
   * @param permutations the permutations
   */
  private static void assertRandomAssignment(AssignmentAlgorithm algorithm, int costLimit,
      int[][] permutations) {
    final int agents = permutations[0].length;
    final int tasks = Arrays.stream(permutations).mapToInt(MathUtils::max).reduce(0, Math::max) + 1;

    // Test random cost matrices
    final int trials = 20;
    final int limit = costLimit == 0 ? tasks : costLimit;
    final UniformRandomProvider rng = RngUtils.create(67681623912L);
    final int[][] cost = new int[agents][tasks];
    for (int trial = 0; trial < trials; trial++) {
      for (int i = 0; i < agents; i++) {
        for (int j = 0; j < tasks; j++) {
          cost[i][j] = rng.nextInt(limit);
        }
      }

      // Compute expected
      int expected = Integer.MAX_VALUE;
      for (final int[] assignments : permutations) {
        expected = Math.min(expected, score(cost, assignments));
      }

      // Compute actual
      final int actual = score(cost, algorithm.compute(cost));

      Assertions.assertEquals(expected, actual, "Did not find min cost");
    }
  }

  private static int score(int[][] cost, int[] assignments) {
    int sum = 0;
    for (int i = 0; i < assignments.length; i++) {
      final int index = assignments[i];
      if (index >= 0) {
        sum += cost[i][index];
      }
    }
    return sum;
  }

  private static double score(double[][] cost, int[] assignments) {
    double sum = 0;
    for (int i = 0; i < assignments.length; i++) {
      final int index = assignments[i];
      if (index >= 0) {
        sum += cost[i][index];
      }
    }
    return sum;
  }

  private static void assertAssignment(AssignmentAlgorithm algorithm, int[][] cost,
      int[] expected) {
    final int[] assignments = algorithm.compute(cost);
    Assertions.assertArrayEquals(expected, assignments);
  }

  // The following tests on large data show the algorithms are consistent

  @Test
  void testIntAssignmentNxN() {
    testIntAssignment(100, 100);
  }

  @Test
  void testIntAssignmentNxM() {
    testIntAssignment(20, 100);
  }

  @Test
  void testIntAssignmentMxN() {
    testIntAssignment(100, 20);
  }

  private static void testIntAssignment(int agents, int tasks) {
    final int limit = 1000;
    final UniformRandomProvider rng = RngUtils.create(67681623912L);
    final int[][] cost = new int[agents][tasks];
    for (int i = 0; i < agents; i++) {
      for (int j = 0; j < tasks; j++) {
        cost[i][j] = rng.nextInt(limit);
      }
    }
    final int[] a1 = KuhnMunkresAssignment.compute(cost);
    final int[] a2 = JonkerVolgenantAssignment.compute(cost);
    Assertions.assertEquals(score(cost, a1), score(cost, a2));
  }

  @Test
  void testDoubleAssignmentNxN() {
    testDoubleAssignment(100, 100);
  }

  @Test
  void testDoubleAssignmentNxM() {
    testDoubleAssignment(20, 100);
  }

  @Test
  void testDoubleAssignmentMxN() {
    testDoubleAssignment(100, 20);
  }

  private static void testDoubleAssignment(int agents, int tasks) {
    final int limit = 1000;
    final UniformRandomProvider rng = RngUtils.create(67681623912L);
    final double[][] cost = new double[agents][tasks];
    for (int i = 0; i < agents; i++) {
      for (int j = 0; j < tasks; j++) {
        cost[i][j] = rng.nextInt(limit);
      }
    }
    final int[] a1 = DoubleKuhnMunkresAssignment.compute(cost);
    final int[] a2 = DoubleJonkerVolgenantAssignment.compute(cost);
    Assertions.assertEquals(score(cost, a1), score(cost, a2));
  }
}
