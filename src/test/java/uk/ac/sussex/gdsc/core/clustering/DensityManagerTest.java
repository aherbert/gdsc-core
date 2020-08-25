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

package uk.ac.sussex.gdsc.core.clustering;

import java.util.function.Supplier;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
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
  void densityWithTriangleMatchesDensity(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
  void densityWithGridFasterThanDensityTriangle(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
        logger.info(msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SeededTest
  void densityWithGridFasterThanDensity(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
        logger.info(msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SeededTest
  void sumWithGridMatchesSum(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
        logger.info(msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SeededTest
  void blockDensityMatchesBlockDensity2(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
        logger.info(msg);
        Assertions.assertTrue(t2 < t1, msg);
      }
    }
  }

  @SpeedTag
  @SeededTest
  void blockDensity2FasterThanBlockDensity3(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
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
