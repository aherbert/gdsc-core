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

package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.DataException;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link QuadraticUtils}.
 */
@SuppressWarnings({"javadoc"})
class QuadraticUtilsTest {

  // It is fine to use a,b,c for a quadratic ax^2 + bx + c
  // CHECKSTYLE.OFF: ParameterName

  @SeededTest
  void canGetDeterminant3x3(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final DoubleDoubleBiPredicate areClose = TestHelper.doublesAreClose(1e-6, 0);
    for (int i = 0; i < 5; i++) {
      final double[] m = new double[9];
      for (int j = 0; j < 9; j++) {
        m[j] = -5 + r.nextDouble() * 10;
      }

      final double e = QuadraticUtils.getDeterminant3x3(m, 1);
      for (int j = 0; j < 3; j++) {
        final double scale = r.nextDouble() * 100;
        final double o = QuadraticUtils.getDeterminant3x3(m, scale);
        TestAssertions.assertTest(e, o, areClose);
      }
    }
  }

  @SeededTest
  void canSolveQuadratic(RandomSeed seed) {
    final double a = 3;
    final double b = -2;
    final double c = -4;
    final double[] exp = new double[] {a, b, c};

    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 5; i++) {
      // Avoid identical points
      final double x1 = -5 + r.nextDouble() * 10;
      double x2 = -5 + r.nextDouble() * 10;
      while (x2 == x1) {
        x2 = -5 + r.nextDouble() * 10;
      }
      double x3 = -5 + r.nextDouble() * 10;
      while (x3 == x1 || x3 == x2) {
        x3 = -5 + r.nextDouble() * 10;
      }

      // Order invariant
      final DoubleDoubleBiPredicate areClose = TestHelper.doublesAreClose(1e-6, 0);
      canSolveQuadratic(a, b, c, exp, x1, x2, x3, areClose);
      canSolveQuadratic(a, b, c, exp, x1, x3, x2, areClose);
      canSolveQuadratic(a, b, c, exp, x2, x1, x3, areClose);
      canSolveQuadratic(a, b, c, exp, x2, x3, x1, areClose);
      canSolveQuadratic(a, b, c, exp, x3, x1, x2, areClose);
      canSolveQuadratic(a, b, c, exp, x3, x2, x1, areClose);
    }
  }

  private static void canSolveQuadratic(double a, double b, double c, double[] exp, double x1,
      double x2, double x3, DoubleDoubleBiPredicate areClose) {
    final double[] o = solveQuadratic(a, b, c, x1, x2, x3);
    Assertions.assertNotNull(o);
    TestAssertions.assertArrayTest(exp, o, areClose);
  }

  private static double[] solveQuadratic(double a, double b, double c, double x1, double x2,
      double x3) {
    final double y1 = a * x1 * x1 + b * x1 + c;
    final double y2 = a * x2 * x2 + b * x2 + c;
    final double y3 = a * x3 * x3 + b * x3 + c;
    return QuadraticUtils.solve(x1, y1, x2, y2, x3, y3);
  }

  @Test
  void solveUsingColocatedPointsReturnsNull() {
    final double a = 3;
    final double b = -2;
    final double c = -4;
    Assertions.assertNull(solveQuadratic(a, b, c, -1, 0, 0));
    Assertions.assertNull(solveQuadratic(a, b, c, -1, -1, 0));
    Assertions.assertNull(solveQuadratic(a, b, c, 0, -1, 0));
  }

  @Test
  void canFindMinMaxQuadratic() {
    final DoubleDoubleBiPredicate areClose = TestHelper.doublesAreClose(1e-6, 0);
    TestAssertions.assertTest(0, findMinMaxQuadratic(1, 0, 0, -1, 0, 1), areClose);
    TestAssertions.assertTest(0, findMinMaxQuadratic(1, 0, -10, -1, 0, 1), areClose);
    TestAssertions.assertTest(-1, findMinMaxQuadratic(1, 2, 0, -1, 0, 1), areClose);
    TestAssertions.assertTest(-1, findMinMaxQuadratic(1, 2, -10, -1, 0, 1), areClose);
  }

  private static double findMinMaxQuadratic(double a, double b, double c, double x1, double x2,
      double x3) {
    final double y1 = a * x1 * x1 + b * x1 + c;
    final double y2 = a * x2 * x2 + b * x2 + c;
    final double y3 = a * x3 * x3 + b * x3 + c;
    return QuadraticUtils.findMinMax(x1, y1, x2, y2, x3, y3);
  }

  @Test
  void findMinMaxUsingColocatedPointsThrows() {
    final double a = 3;
    final double b = -2;
    final double c = -4;
    Assertions.assertThrows(DataException.class, () -> {
      findMinMaxQuadratic(a, b, c, -1, 0, 0);
    });
  }

  @Test
  void findMinMaxUsingColinearPointsThrows() {
    final double a = 0;
    final double b = 1;
    final double c = 0;
    Assertions.assertThrows(DataException.class, () -> {
      findMinMaxQuadratic(a, b, c, -1, 0, 1);
    });
  }
}
