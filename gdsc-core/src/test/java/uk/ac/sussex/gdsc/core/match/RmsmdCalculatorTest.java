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
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.trees.DoubleDistanceFunctions;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

/**
 * Test for {@link RmsmdCalculator}.
 */
@SuppressWarnings({"javadoc"})
class RmsmdCalculatorTest {
  @Test
  void testBadArguments() {
    final List<double[]> c2 = Arrays.asList(new double[2]);
    final List<double[]> empty = Collections.emptyList();
    final List<double[]> c3 = Arrays.asList(new double[3]);
    // Check OK with the same size
    Assertions.assertEquals(0, RmsmdCalculator.rmsmd(c2, c2));
    Assertions.assertEquals(0, RmsmdCalculator.rmsmd(c3, c3));
    // Empty is not allowed
    Assertions.assertThrows(IllegalArgumentException.class, () -> RmsmdCalculator.rmsmd(c2, empty));
    Assertions.assertThrows(IllegalArgumentException.class, () -> RmsmdCalculator.rmsmd(empty, c2));
    // Dimension mismatch
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> RmsmdCalculator.rmsmd(c2, c3));
  }

  @Test
  void testRmsmd() {
    // From the RMSMD paper
    //@formatter:off
    final List<double[]> s = Arrays.asList(
        new double[] {200, 460},
        new double[] {750, 660},
        new double[] {1190, 600},
        new double[] {1200, 200}
    );
    final List<double[]> x = Arrays.asList(
        new double[] {300, 400},
        new double[] {260, 760},
        new double[] {550, 800},
        new double[] {820, 560},
        new double[] {950, 800},
        new double[] {1100, 100}
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740), RmsmdCalculator.rmsmd(s, x));
  }

  @Test
  void testRmsmdWithObjects() {
    // From the RMSMD paper
    //@formatter:off
    final List<BasePoint> s = Arrays.asList(
        new BasePoint(200, 460),
        new BasePoint(750, 660),
        new BasePoint(1190, 600),
        new BasePoint(1200, 200)
    );
    final List<BasePoint> x = Arrays.asList(
        new BasePoint(300, 400),
        new BasePoint(260, 760),
        new BasePoint(550, 800),
        new BasePoint(820, 560),
        new BasePoint(950, 800),
        new BasePoint(1100, 100)
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740),
        RmsmdCalculator.rmsmd(s, x, p -> new double[] {p.getX(), p.getY()}));
  }

  @Test
  void testRmsmdWithCustomDistanceFunction() {
    // From the RMSMD paper
    //@formatter:off
    final List<double[]> s = Arrays.asList(
        new double[] {200, 460},
        new double[] {750, 660},
        new double[] {1190, 600},
        new double[] {1200, 200}
    );
    final List<double[]> x = Arrays.asList(
        new double[] {300, 400},
        new double[] {260, 760},
        new double[] {550, 800},
        new double[] {820, 560},
        new double[] {950, 800},
        new double[] {1100, 100}
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740),
        RmsmdCalculator.rmsmd(s, x, DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D::distance));
  }

  @Test
  void testRmsmd3d() {
    //@formatter:off
    final List<double[]> s = Arrays.asList(
        new double[] {0, 200, 460},
        new double[] {0, 750, 660},
        new double[] {0, 1190, 600},
        new double[] {0, 1200, 200}
    );
    final List<double[]> x = Arrays.asList(
        new double[] {0, 300, 400},
        new double[] {0, 260, 760},
        new double[] {0, 550, 800},
        new double[] {0, 820, 560},
        new double[] {0, 950, 800},
        new double[] {0, 1100, 100}
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740), RmsmdCalculator.rmsmd(s, x));
  }

  @Test
  void testRmsmdNd() {
    //@formatter:off
    final List<double[]> s = Arrays.asList(
        new double[] {0, 0, 200, 460},
        new double[] {0, 0, 750, 660},
        new double[] {0, 0, 1190, 600},
        new double[] {0, 0, 1200, 200}
    );
    final List<double[]> x = Arrays.asList(
        new double[] {0, 0, 300, 400},
        new double[] {0, 0, 260, 760},
        new double[] {0, 0, 550, 800},
        new double[] {0, 0, 820, 560},
        new double[] {0, 0, 950, 800},
        new double[] {0, 0, 1100, 100}
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740), RmsmdCalculator.rmsmd(s, x));
  }

  @Test
  void testSumMinimumDistance() {
    // From the RMSMD paper
    //@formatter:off
    final double[][] s = new double[][] {
        new double[] {200, 460},
        new double[] {750, 660},
        new double[] {1190, 600},
        new double[] {1200, 200}
    };
    final double[][] x = new double[][] {
        new double[] {300, 400},
        new double[] {260, 760},
        new double[] {550, 800},
        new double[] {820, 560},
        new double[] {950, 800},
        new double[] {1100, 100}
    };
    //@formatter:on
    // Re-use an existing distance function
    final ToDoubleBiFunction<double[], double[]> distanceFunction =
        DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D::distance;
    final double expected = 40740 * (s.length + x.length);
    // Check implementations are correct
    Assertions.assertEquals(expected,
        RmsmdCalculator.sumMinimumDistancesAllVsAll(s, x, distanceFunction)
            + RmsmdCalculator.sumMinimumDistancesAllVsAll(x, s, distanceFunction));
    Assertions.assertEquals(expected,
        RmsmdCalculator.sumMinimumDistancesKdTree(s, x,
            DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D)
            + RmsmdCalculator.sumMinimumDistancesKdTree(x, s,
                DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D));
  }

  @SeededTest
  void testRmsmd1dWithKdTree(RandomSeed seed) {
    assertRmsmdNdWithKdTree(seed, 1);
  }

  @SeededTest
  void testRmsmd2dWithKdTree(RandomSeed seed) {
    assertRmsmdNdWithKdTree(seed, 2);
  }

  @SeededTest
  void testRmsmd3dWithKdTree(RandomSeed seed) {
    assertRmsmdNdWithKdTree(seed, 3);
  }

  private static void assertRmsmdNdWithKdTree(RandomSeed seed, int dimensions) {
    final UniformRandomProvider rg = RngUtils.create(seed.get());
    // Create enough data to trigger use of the tree
    final double[][] p0 = createData(dimensions, rg, 4);
    final double[][] p1 = createData(dimensions, rg, 64);
    final double[][] p2 = createData(dimensions, rg, 512);
    final ToDoubleBiFunction<double[], double[]> distanceFunction =
        DoubleDistanceFunctions.SQUARED_EUCLIDEAN_ND::distance;
    // Hit edge cases for use of the tree using a custom distance function
    Assertions.assertEquals(RmsmdCalculator.sumMinimumDistancesAllVsAll(p0, p2, distanceFunction),
        RmsmdCalculator.sumMinimumDistances(p0, p2, distanceFunction));
    Assertions.assertEquals(RmsmdCalculator.sumMinimumDistancesAllVsAll(p1, p2, distanceFunction),
        RmsmdCalculator.sumMinimumDistances(p1, p2, distanceFunction));

    // With default distance function
    Assertions.assertEquals(
        Math.sqrt((RmsmdCalculator.sumMinimumDistancesAllVsAll(p1, p2, distanceFunction)
            + RmsmdCalculator.sumMinimumDistancesAllVsAll(p2, p1, distanceFunction))
            / (p1.length + p2.length)),
        RmsmdCalculator.rmsmd(Arrays.asList(p1), Arrays.asList(p2)));
  }

  /**
   * Creates the ND data.
   *
   * @param rg the random generator
   * @param size the size
   * @return the data
   */
  private static double[][] createData(int dimensions, UniformRandomProvider rg, int size) {
    return IntStream.range(0, size).mapToObj(i -> {
      final double[] tmp = new double[dimensions];
      for (int j = 0; j < dimensions; j++) {
        tmp[j] = rg.nextDouble();
      }
      return tmp;
    }).toArray(double[][]::new);
  }
}
