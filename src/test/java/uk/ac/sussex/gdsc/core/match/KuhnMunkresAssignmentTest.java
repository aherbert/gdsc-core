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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link KuhnMunkresAssignment}.
 */
@SuppressWarnings({"javadoc"})
public class KuhnMunkresAssignmentTest {
  @Test
  public void testWithoutOverflow() {
    Assertions.assertThrows(ArithmeticException.class,
        () -> KuhnMunkresAssignment.addWithoutOverflow(Integer.MAX_VALUE, 1));
  }

  @Test
  public void testCreateThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> KuhnMunkresAssignment.create(null), "null input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> KuhnMunkresAssignment.create(new int[1][]), "null second input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> KuhnMunkresAssignment.create(new int[0][0]), "zero length input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> KuhnMunkresAssignment.create(new int[1][0]), "zero length second input");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> KuhnMunkresAssignment.create(new int[][] {{1, 2, 3}, {1, 2}}),
        "Non rectangular input");
  }

  @Test
  public void testComputeThrows() {
    Assertions.assertThrows(ArithmeticException.class,
        () -> KuhnMunkresAssignment
            .compute(new int[][] {{Integer.MIN_VALUE, Integer.MAX_VALUE}, {0, 0}}),
        "Expected overflow when zeroing rows");
    // Force the columns computation first using rows > columns
    Assertions.assertThrows(ArithmeticException.class,
        () -> KuhnMunkresAssignment
            .compute(new int[][] {{Integer.MIN_VALUE, 0}, {Integer.MAX_VALUE, 0}, {0, 0}}),
        "Expected overflow when zeroing columns");
  }

  @Test
  public void testAssignment2x3Linear() {
    // Data from Bourgeois and Lassalle (1971)
    // Communications of the ACM Volume 14, Issue 12, 802-804.
    //@formatter:off
    final int[] cost = {
        5, 7,
        4, 5,
        1, 11,
    };
    //@formatter:on
    // 7 + 1 + 3 == 11
    final int[] expected = {-1, 1, 0};
    final int[] assignments = KuhnMunkresAssignment.compute(cost, 3, 2);
    Assertions.assertArrayEquals(expected, assignments);
  }
}
