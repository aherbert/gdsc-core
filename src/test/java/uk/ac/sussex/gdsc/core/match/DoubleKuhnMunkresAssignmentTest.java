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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link DoubleKuhnMunkresAssignment}.
 */
@SuppressWarnings({"javadoc"})
class DoubleKuhnMunkresAssignmentTest {
  @Test
  void testWithoutOverflow() {
    Assertions.assertThrows(ArithmeticException.class,
        () -> DoubleKuhnMunkresAssignment.addWithoutOverflow(Double.MAX_VALUE, Double.MAX_VALUE));
  }

  @Test
  void testSubtractToZero() {
    Assertions.assertEquals(0f, DoubleKuhnMunkresAssignment.subtractToZero(0.3f, 0.4f));
  }

  @Test
  void testCreateThrows() {
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
  void testComputeThrows() {
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
  void testAssignment3x3Linear() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[] cost = {
        7, 5, 11,
        5, 4, 1,
        9, 3, 2,
    };
    //@formatter:on
    // 7 + 1 + 3 == 11
    final int[] expected = {0, 2, 1};
    final int[] assignments = DoubleKuhnMunkresAssignment.compute(cost, 3, 3);
    Assertions.assertArrayEquals(expected, assignments);
  }

  @Test
  void testAssignment2x3Linear() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[] cost = {
        7, 5, 11,
        5, 4, 1,
    };
    //@formatter:on
    // 5 + 1 == 6
    final int[] expected = {1, 2};
    final int[] assignments = DoubleKuhnMunkresAssignment.compute(cost, 2, 3);
    Assertions.assertArrayEquals(expected, assignments);
  }

  @Test
  void testAssignment3x2Linear() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final double[] cost = {
        5, 7,
        4, 5,
        1, 11,
    };
    //@formatter:on
    // 7 + 1 + 3 == 11
    final int[] expected = {-1, 1, 0};
    final int[] assignments = DoubleKuhnMunkresAssignment.compute(cost, 3, 2);
    Assertions.assertArrayEquals(expected, assignments);
  }
}
