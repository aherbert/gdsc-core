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

package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link DoubleKuhnMunkresAssignment}.
 */
@SuppressWarnings({"javadoc"})
public class DoubleKuhnMunkresAssignmentTest {
  @Test
  public void testWithoutOverflow() {
    Assertions.assertThrows(ArithmeticException.class,
        () -> DoubleKuhnMunkresAssignment.addWithoutOverflow(Double.MAX_VALUE, Double.MAX_VALUE));
  }

  @Test
  public void testSubtractToZero() {
    Assertions.assertEquals(0f, DoubleKuhnMunkresAssignment.subtractToZero(0.3f, 0.4f));
  }

  @Test
  public void testCreateThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DoubleKuhnMunkresAssignment.create(null), "null input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DoubleKuhnMunkresAssignment.create(new double[1][]), "null second input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DoubleKuhnMunkresAssignment.create(new double[0][0]), "zero length input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DoubleKuhnMunkresAssignment.create(new double[1][0]), "zero length second input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DoubleKuhnMunkresAssignment.create(new double[][] {{1, 2, 3}, {1, 2}}),
        "Non rectangular input");
  }

  @Test
  public void testComputeThrows() {
    Assertions.assertThrows(ArithmeticException.class,
        () -> DoubleKuhnMunkresAssignment
            .compute(new double[][] {{-Double.MAX_VALUE, Double.MAX_VALUE}, {0, 0}}),
        "Expected overflow when zeroing rows");
    // Force the columns computation first using rows > columns
    Assertions.assertThrows(ArithmeticException.class,
        () -> DoubleKuhnMunkresAssignment
            .compute(new double[][] {{-Double.MAX_VALUE, 0}, {Double.MAX_VALUE, 0}, {0, 0}}),
        "Expected overflow when zeroing columns");

    // If the minimum is negative infinity this should be noticed
    Assertions.assertThrows(ArithmeticException.class,
        () -> DoubleKuhnMunkresAssignment
            .compute(new double[][] {{Double.NEGATIVE_INFINITY, -Double.MIN_VALUE}, {0, 0}}),
        "Expected overflow when zeroing rows containing negative infinity");
    Assertions.assertThrows(ArithmeticException.class,
        () -> DoubleKuhnMunkresAssignment
            .compute(new double[][] {{Double.POSITIVE_INFINITY, Double.MIN_VALUE}, {0, 0}}),
        "Expected overflow when zeroing rows containing positive infinity");
  }

  @Test
  public void testAssignment3x3Zero() {
    final double[][] cost = new double[3][3];
    final int[] assignments = DoubleKuhnMunkresAssignment.compute(cost);
    Assertions.assertEquals(3, assignments.length);
    for (int i = 0; i < 3; i++) {
      Assertions.assertNotEquals(-1, ArrayUtils.indexOf(assignments, i));
    }
  }

  @Test
  public void testAssignment3x3() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[][] cost = {
        { 7, 5, 11.2 },
        { 5, 4, 1 },
        { 9.3, 3, 2 },
    };
    //@formatter:on
    // 7 + 1 + 3 == 11
    final int[] expected = {0, 2, 1};
    assertAssignment(cost, expected);
  }

  @Test
  public void testAssignment3x3Rotated() {
    // As above but rotated
    //@formatter:off
    final double[][] cost = {
        { 9.3, 5, 7 },
        { 3, 4, 5 },
        { 2, 1, 11.2 },
    };
    //@formatter:on
    // 7 + 3 + 1 == 11
    final int[] expected = {2, 0, 1};
    assertAssignment(cost, expected);
  }

  @Test
  public void testAssignment2x2() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[][] cost = {
        { 7, 5, 11.2 },
        { 5, 4, 1 },
    };
    //@formatter:on
    // 5 + 1 == 6
    final int[] expected = {1, 2};
    assertAssignment(cost, expected);
  }

  @Test
  public void testAssignment2x2Rotated() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[][] cost = {
        { 5, 7 },
        { 4, 5 },
        { 1, 11.2 },
    };
    //@formatter:on
    // 5 + 1 == 6
    final int[] expected = {-1, 1, 0};
    assertAssignment(cost, expected);
  }

  @Test
  public void testAssignment2x2Linear() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[] cost = {
        5, 7,
        4, 5,
        1, 11.2,
    };
    //@formatter:on
    // 7 + 1 + 3 == 11
    final int[] expected = {-1, 1, 0};
    final int[] assignments = DoubleKuhnMunkresAssignment.compute(cost, 3, 2);
    Assertions.assertArrayEquals(expected, assignments);
  }

  @Test
  public void testAssignment3x3b() {
    //@formatter:off
    final double[][] cost = {
        { 1, 2, 3 },
        { 2, 4, 6 },
        { 3, 6, 9 },
    };
    //@formatter:on
    // 3 + 4 + 3 == 10
    final int[] expected = {2, 1, 0};
    assertAssignment(cost, expected);
  }

  // Permutations from
  // https://www.mathsisfun.com/combinatorics/combinations-permutations-calculator.html

  @Test
  public void testRandomAssignment4x4() {
    assertRandomAssignment(new int[][] {{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 2, 3, 1},
        {0, 3, 1, 2}, {0, 3, 2, 1}, {1, 0, 2, 3}, {1, 0, 3, 2}, {1, 2, 0, 3}, {1, 2, 3, 0},
        {1, 3, 0, 2}, {1, 3, 2, 0}, {2, 0, 1, 3}, {2, 0, 3, 1}, {2, 1, 0, 3}, {2, 1, 3, 0},
        {2, 3, 0, 1}, {2, 3, 1, 0}, {3, 0, 1, 2}, {3, 0, 2, 1}, {3, 1, 0, 2}, {3, 1, 2, 0},
        {3, 2, 0, 1}, {3, 2, 1, 0}});
  }

  @Test
  public void testRandomAssignment4x4WithBinaryCost() {
    final int costLimit = 2;
    assertRandomAssignment(costLimit,
        new int[][] {{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 2, 3, 1}, {0, 3, 1, 2},
            {0, 3, 2, 1}, {1, 0, 2, 3}, {1, 0, 3, 2}, {1, 2, 0, 3}, {1, 2, 3, 0}, {1, 3, 0, 2},
            {1, 3, 2, 0}, {2, 0, 1, 3}, {2, 0, 3, 1}, {2, 1, 0, 3}, {2, 1, 3, 0}, {2, 3, 0, 1},
            {2, 3, 1, 0}, {3, 0, 1, 2}, {3, 0, 2, 1}, {3, 1, 0, 2}, {3, 1, 2, 0}, {3, 2, 0, 1},
            {3, 2, 1, 0}});
  }

  @Test
  public void testRandomAssignment4x3() {
    // As above but switch 3 to -1 for no assignment
    assertRandomAssignment(new int[][] {{0, 1, 2, -1}, {0, 1, -1, 2}, {0, 2, 1, -1}, {0, 2, -1, 1},
        {0, -1, 1, 2}, {0, -1, 2, 1}, {1, 0, 2, -1}, {1, 0, -1, 2}, {1, 2, 0, -1}, {1, 2, -1, 0},
        {1, -1, 0, 2}, {1, -1, 2, 0}, {2, 0, 1, -1}, {2, 0, -1, 1}, {2, 1, 0, -1}, {2, 1, -1, 0},
        {2, -1, 0, 1}, {2, -1, 1, 0}, {-1, 0, 1, 2}, {-1, 0, 2, 1}, {-1, 1, 0, 2}, {-1, 1, 2, 0},
        {-1, 2, 0, 1}, {-1, 2, 1, 0}});
  }

  @Test
  public void testRandomAssignment3x4() {
    assertRandomAssignment(new int[][] {{0, 1, 2}, {0, 1, 3}, {0, 2, 1}, {0, 2, 3}, {0, 3, 1},
        {0, 3, 2}, {1, 0, 2}, {1, 0, 3}, {1, 2, 0}, {1, 2, 3}, {1, 3, 0}, {1, 3, 2}, {2, 0, 1},
        {2, 0, 3}, {2, 1, 0}, {2, 1, 3}, {2, 3, 0}, {2, 3, 1}, {3, 0, 1}, {3, 0, 2}, {3, 1, 0},
        {3, 1, 2}, {3, 2, 0}, {3, 2, 1}});
  }

  @Test
  public void testRandomAssignment6x6() {
    assertRandomAssignment(
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
   * @param costLimit the cost limit (maximum cost)
   * @param permutations the permutations
   */
  private static void assertRandomAssignment(int[][] permutations) {
    assertRandomAssignment(0, permutations);
  }

  /**
   * Assert the assignments from a random cost matrix. The input is all possible permutations for
   * the agents to the tasks. Use -1 for no assignment.
   *
   * @param costLimit the cost limit (maximum cost)
   * @param permutations the permutations
   */
  private static void assertRandomAssignment(int costLimit, int[][] permutations) {
    final int agents = permutations[0].length;
    final int tasks = Arrays.stream(permutations).mapToInt(MathUtils::max).reduce(0, Math::max) + 1;

    // Test random cost matrices
    final int trials = 20;
    final int limit = costLimit == 0 ? tasks : costLimit;
    final UniformRandomProvider rng = RngUtils.create(67681623912L);
    final double[][] cost = new double[agents][tasks];
    for (int trial = 0; trial < trials; trial++) {
      for (int i = 0; i < agents; i++) {
        for (int j = 0; j < tasks; j++) {
          cost[i][j] = rng.nextInt(limit);
        }
      }

      // Compute expected
      double expected = Double.MAX_VALUE;
      for (final int[] assignments : permutations) {
        expected = Math.min(expected, score(cost, assignments));
      }

      // Compute actual
      final double actual = score(cost, DoubleKuhnMunkresAssignment.compute(cost));

      Assertions.assertEquals(expected, actual, "Did not find min cost");
    }
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

  private static void assertAssignment(double[][] cost, int[] expected) {
    final int[] assignments = DoubleKuhnMunkresAssignment.compute(cost);
    Assertions.assertArrayEquals(expected, assignments);
  }
}
