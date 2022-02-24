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

package uk.ac.sussex.gdsc.core.clustering;

import java.awt.geom.Rectangle2D;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils.TestLevel;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
class DensityManagerTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(DensityManagerTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  int size = 256;
  float[] radii = new float[] {2, 4, 8, 16};
  int[] ns = new int[] {1000, 2000, 4000};

  @SeededTest
  void testCalculateSquareDensity(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final DensityManager dm = createDensityManager(rng, size, 1000);

    // Check arguments are validated
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> dm.calculateSquareDensity(1, 0, false));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> dm.calculateSquareDensity(0, 1, false));

    final float maxXCoord = dm.getMaximumX();
    final float maxYCoord = dm.getMaximumY();
    final float[][] coordData = dm.getData();
    final float[] xcoord = coordData[0];
    final float[] ycoord = coordData[1];
    final int[] expected = new int[xcoord.length];
    final int[] expected2 = new int[xcoord.length];
    for (final float radius : new float[] {8, 16}) {
      for (final int resolution : new int[] {1, 2}) {
        // Allocate molecules to the cells
        final float cellSize = radius / resolution;
        final int maxx = (int) (maxXCoord / cellSize) + 1;
        final int maxy = (int) (maxYCoord / cellSize) + 1;
        final int[] data = new int[maxx * maxy];
        for (int i = 0; i < xcoord.length; i++) {
          final int x = (int) (xcoord[i] / cellSize);
          final int y = (int) (ycoord[i] / cellSize);
          data[y * maxx + x]++;
        }

        // For each molecule compute the density count from neighbouring cells
        final float area = MathUtils.pow2(2 * resolution + 1);
        for (int i = 0; i < xcoord.length; i++) {
          final int u = (int) (xcoord[i] / cellSize);
          final int v = (int) (ycoord[i] / cellSize);
          final int minU = Math.max(0, u - resolution);
          final int maxU = Math.min(maxx - 1, u + resolution);
          final int minV = Math.max(0, v - resolution);
          final int maxV = Math.min(maxy - 1, v + resolution);
          int sum = 0;
          int blocks = 0;
          for (int y = minV; y <= maxV; y++) {
            for (int x = minU; x <= maxU; x++) {
              sum += data[y * maxx + x];
              blocks++;
            }
          }
          expected[i] = sum - 1;
          // Adjust
          expected2[i] = (int) (expected[i] * (area / blocks));
        }

        // Validate
        Assertions.assertArrayEquals(expected,
            dm.calculateSquareDensity(radius, resolution, false));
        Assertions.assertArrayEquals(expected2,
            dm.calculateSquareDensity(radius, resolution, true));
      }
    }
  }

  @SeededTest
  void densityWithTriangleMatchesDensity(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        final int[] d1 = dm.calculateDensity(radius);
        final int[] d2 = dm.calculateDensityTriangle(radius);

        Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
      }
    }
  }

  @SeededTest
  void densityWithGridMatchesDensity(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        final int[] d1 = dm.calculateDensity(radius);
        final int[] d2 = dm.calculateDensityGrid(radius);

        Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
      }
    }
  }

  @SeededTest
  void densityWithAdjustMatchesDensity(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    // Size must be above and below the threshold to choose algorithm
    for (final int n : new int[] {100, 1000}) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        final int[] d1 = dm.calculateDensity(radius);
        final int[] d2 = dm.calculateDensity(radius, false);

        Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
      }
    }
  }

  @SeededTest
  void densityWithAdjustIncreases(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final int n = 2000;
    final DensityManager dm = createDensityManager(rng, size, n);
    final float[][] coordData = dm.getData();
    final float[] xcoord = coordData[0];
    final float[] ycoord = coordData[1];
    final float maxXCoord = dm.getMaximumX();
    final float maxYCoord = dm.getMaximumY();

    for (final float radius : radii) {
      final int[] d1 = dm.calculateDensity(radius, false);
      final int[] d2 = dm.calculateDensity(radius, true);
      final Rectangle2D.Double border =
          new Rectangle2D.Double(radius, radius, maxXCoord - 2 * radius, maxYCoord - 2 * radius);

      for (int i = 0; i < xcoord.length; i++) {
        // Check if at boundary
        if (border.contains(xcoord[i], ycoord[i])) {
          Assertions.assertEquals(d1[i], d2[i]);
        } else {
          Assertions.assertTrue(d2[i] >= d1[i]);
          Assertions.assertTrue(d2[i] <= d1[i] * 4);
        }
      }

      Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
    }
  }

  @SeededTest
  void densityWithGridFasterThanDensityTriangle(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        long start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateDensityTriangle(radius);
        }
        final long t1 = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateDensityGrid(radius);
        }
        final long t2 = System.nanoTime() - start;

        final Supplier<String> msg = FunctionUtils
            .getSupplier("Grid vs Triangle. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
        logger.log(TestLevel.TEST_INFO, msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SeededTest
  void densityWithGridFasterThanDensity(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        long start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateDensity(radius);
        }
        final long t1 = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateDensityGrid(radius);
        }
        final long t2 = System.nanoTime() - start;

        final Supplier<String> msg = FunctionUtils
            .getSupplier("Grid vs Standard. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
        logger.log(TestLevel.TEST_INFO, msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SeededTest
  void sumWithGridMatchesSum(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        final int s1 = dm.calculateSum(radius);
        final int s2 = dm.calculateSumGrid(radius);

        Assertions.assertEquals(s1, s2, () -> String.format("N=%d, R=%f", n, radius));
      }
    }
  }

  @SeededTest
  void sumWithGridFasterThanSum(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        long start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateSum(radius);
        }
        final long t1 = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateSumGrid(radius);
        }
        final long t2 = System.nanoTime() - start;

        final Supplier<String> msg = FunctionUtils.getSupplier(
            "Sum Grid vs Standard. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
        logger.log(TestLevel.TEST_INFO, msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SeededTest
  void blockDensityMatchesBlockDensity2(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        final int[] d1 = dm.calculateBlockDensity(radius);
        final int[] d2 = dm.calculateBlockDensity2(radius);

        Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
      }
    }
  }

  @SeededTest
  void blockDensity2MatchesBlockDensity3(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        final int[] d1 = dm.calculateBlockDensity2(radius);
        final int[] d2 = dm.calculateBlockDensity3(radius);

        Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
      }
    }
  }

  @Disabled("This is not always true. The two are comparable in speed.")
  @SeededTest
  void blockDensityFasterThanBlockDensity2(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        long start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateBlockDensity(radius);
        }
        final long t1 = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateBlockDensity2(radius);
        }
        final long t2 = System.nanoTime() - start;

        final Supplier<String> msg = FunctionUtils.getSupplier(
            "calculateBlockDensity2 vs calculateBlockDensity. N=%d, R=%f : %fx faster", n, radius,
            (double) t1 / t2);
        logger.log(TestLevel.TEST_INFO, msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SpeedTag
  @SeededTest
  void blockDensity2FasterThanBlockDensity3(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int n : ns) {
      final DensityManager dm = createDensityManager(rng, size, n);

      for (final float radius : radii) {
        long start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateBlockDensity3(radius);
        }
        final long t1 = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 10; i-- > 0;) {
          dm.calculateBlockDensity2(radius);
        }
        final long t2 = System.nanoTime() - start;

        final Supplier<String> msg = FunctionUtils.getSupplier(
            "calculateBlockDensity2 vs calculateBlockDensity3. N=%d, R=%f : %fx faster", n, radius,
            (double) t1 / t2);
        // This is not always faster
        // TestLog.info(logger,msg);
        // Assertions.assertTrue(t2 < t1, msg);
        logger.log(TestLogUtils.getResultRecord(t2 < t1, msg));
      }
    }
  }

  @SeededTest
  void testRipleysFunctions(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final int n = 2000;
    final DensityManager dm = createDensityManager(rng, size, n);
    final float radius = 8;

    final int[] density = dm.calculateDensity(radius, false);

    Assertions.assertThrows(IllegalArgumentException.class, () -> dm.ripleysKFunction(-1));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> dm.ripleysKFunction(new int[0], radius));

    final double k1 = dm.ripleysKFunction(radius);
    final double k2 = dm.ripleysKFunction(density, radius);
    Assertions.assertEquals(k1, k2);

    // Check
    final double avgDensity = n / dm.area;
    final double expected = ((double) MathUtils.sum(density) / n) / avgDensity;
    Assertions.assertEquals(expected, k1, expected * 1e-6);

    final double l = Math.sqrt(k1 / Math.PI);
    Assertions.assertEquals(l, dm.ripleysLFunction(radius));
    Assertions.assertEquals(l, dm.ripleysLFunction(density, radius));
  }

  private static DensityManager createDensityManager(UniformRandomProvider rng, int size, int n) {
    final float[] xcoord = new float[n];
    final float[] ycoord = new float[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      xcoord[i] = rng.nextFloat() * size;
      ycoord[i] = rng.nextFloat() * size;
    }
    return new DensityManager(xcoord, ycoord, size * size);
  }
}
