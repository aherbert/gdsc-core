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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import uk.ac.sussex.gdsc.core.clustering.DensityCounter.SimpleMolecule;
import uk.ac.sussex.gdsc.core.utils.rng.SamplerUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingService;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

/**
 * Test the DensityCounter.
 */
@SuppressWarnings({"javadoc"})
class DensityCounterTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(DensityCounterTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  boolean skipSpeedTest = true;

  int size = 256;
  float[] radii = new float[] {2, 4, 8};
  int[] ns = new int[] {1000, 2000, 4000};
  int channels = 3;
  int speedTestSize = 5;

  @SeededTest
  void countAllWithSimpleMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, radius, channels - 1);
        final int[][] d2 = c.countAllSimple(channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  void countAllWithSingleThreadMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, radius, channels - 1);
        final int[][] d2 = c.countAll(channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  void countAllWithMultiThreadSycnMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(4);
        c.setMultiThreadMode(DensityCounter.MODE_SYNC);

        final int[][] d1 = DensityCounter.countAll(molecules, radius, channels - 1);
        final int[][] d2 = c.countAll(channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  void countAllWithMultiThreadNonSyncMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(4);
        c.setMultiThreadMode(DensityCounter.MODE_NON_SYNC);

        final int[][] d1 = DensityCounter.countAll(molecules, radius, channels - 1);
        final int[][] d2 = c.countAll(channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  void countAllAroundMoleculesWithSimpleMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n / 2);
      final SimpleMolecule[] molecules2 = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, channels - 1);
        final int[][] d2 = c.countAllSimple(molecules2, channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  void countAllAroundMoleculesWithSingleThreadMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n / 2);
      final SimpleMolecule[] molecules2 = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, channels - 1);
        final int[][] d2 = c.countAll(molecules2, channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  void countAllAroundMoleculesWithMultiThreadMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n / 2);
      final SimpleMolecule[] molecules2 = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(4);

        final int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, channels - 1);
        final int[][] d2 = c.countAll(molecules2, channels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  private static void check(final int n, final float radius, final int[][] d1, final int[][] d2) {
    Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
    Assertions.assertEquals(d1.length, n, () -> String.format("N=%d, R=%f", n, radius));
  }

  private abstract class MyTimingTask extends BaseTimingTask {
    public MyTimingTask(String name) {
      super(name);
    }

    @Override
    public Object getData(int index) {
      return index;
    }

    @Override
    public int getSize() {
      return speedTestSize;
    }
  }

  @SeededTest
  void countAllSpeedTest(RandomSeed seed) {
    // Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    // Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider r = RngUtils.create(seed.getSeed());

    // The single thread mode is faster when the radius is small.
    // The multi-thread mode is faster when the radius is large (>4).
    // The non-synchronised multi-thread mode is faster than the synchronised mode.

    // However both will be fast enough when the radius is small so it is
    // probably OK to leave it multi-threading by default.

    final float radius = 0.35f;
    final int nThreads = 16;

    // TODO - Repeat this at different number of molecules to to determine if multi-threading is
    // worth it

    final SimpleMolecule[][] molecules = new SimpleMolecule[speedTestSize][];
    final DensityCounter[] c = new DensityCounter[molecules.length];
    for (int i = 0; i < molecules.length; i++) {
      molecules[i] = createMolecules(r, size, 20000);
      c[i] = new DensityCounter(molecules[i], radius, true);
    }

    // How many distance comparison are we expected to make?
    // Compute mean density per grid cell (d):
    // single/sync multi = nCells * (5 * d * d) // For simplicity the n*(n-1)/2 for the main cell is
    // ignored
    // non-sync multi = nCells * (9 * d * d)
    final double d = molecules[0].length * radius * radius / (size * size);
    final double nCells = (size / radius) * (size / radius);
    logger.info(FunctionUtils.getSupplier("Expected Comparisons : Single = %f, Multi non-sync = %f",
        nCells * 5 * d * d, nCells * 9 * d * d));

    final TimingService ts = new TimingService();
    // ts.execute(new MyTimingTask("countAllSimple")
    // {
    // public Object run(Object data) { int i = (Integer) data; return c[i].countAllSimple(nChannels
    // - 1); }
    // });
    // ts.execute(new MyTimingTask("countAllSimple static")
    // {
    // public Object run(Object data) { int i = (Integer) data; return
    // DensityCounter.countAll(molecules[i], radius, nChannels - 1); }
    // });
    ts.execute(new MyTimingTask("countAll single thread") {
      @Override
      public Object run(Object data) {
        final int i = (Integer) data;
        c[i].setNumberOfThreads(1);
        return c[i].countAll(channels - 1);
      }
    });
    ts.execute(new MyTimingTask("countAll single thread + constructor") {
      @Override
      public Object run(Object data) {
        final int i = (Integer) data;
        final DensityCounter c = new DensityCounter(molecules[i], radius, true);
        c.setNumberOfThreads(1);
        return c.countAll(channels - 1);
      }
    });
    ts.execute(new MyTimingTask("countAll multi thread") {
      @Override
      public Object run(Object data) {
        final int i = (Integer) data;
        c[i].setNumberOfThreads(nThreads);
        // c[i].gridPriority = null;
        c[i].setMultiThreadMode(DensityCounter.MODE_SYNC);
        return c[i].countAll(channels - 1);
      }
    });
    ts.execute(new MyTimingTask("countAll multi thread + constructor") {
      @Override
      public Object run(Object data) {
        final int i = (Integer) data;
        final DensityCounter c = new DensityCounter(molecules[i], radius, true);
        c.setNumberOfThreads(nThreads);
        c.gridPriority = null;
        c.setMultiThreadMode(DensityCounter.MODE_SYNC);
        return c.countAll(channels - 1);
      }
    });
    ts.execute(new MyTimingTask("countAll multi thread non-sync") {
      @Override
      public Object run(Object data) {
        final int i = (Integer) data;
        c[i].setNumberOfThreads(nThreads);
        // c[i].gridPriority = null;
        c[i].setMultiThreadMode(DensityCounter.MODE_NON_SYNC);
        return c[i].countAll(channels - 1);
      }
    });
    ts.execute(new MyTimingTask("countAll multi thread non-sync + constructor") {
      @Override
      public Object run(Object data) {
        final int i = (Integer) data;
        final DensityCounter c = new DensityCounter(molecules[i], radius, true);
        c.setNumberOfThreads(nThreads);
        c.gridPriority = null;
        c.setMultiThreadMode(DensityCounter.MODE_NON_SYNC);
        return c.countAll(channels - 1);
      }
    });

    @SuppressWarnings("unused")
    final int size = ts.repeat();
    // ts.repeat(size);

    logger.info(ts.getReport());
  }

  @SeededTest
  void countAllAroundMoleculesSpeedTest(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());

    // The multi-thread mode is faster when the number of molecules is large.

    // However both will be fast enough when the data size is small so it is
    // probably OK to leave it multi-threading by default.

    final float radius = 0.35f;
    final int numberOfThreads = Runtime.getRuntime().availableProcessors();

    // TODO - Repeat this at different number of molecules to to determine if multi-threading is
    // worth it

    final SimpleMolecule[][] molecules = new SimpleMolecule[speedTestSize][];
    final SimpleMolecule[][] molecules2 = new SimpleMolecule[speedTestSize][];
    final DensityCounter[] c = new DensityCounter[molecules.length];
    for (int i = 0; i < molecules.length; i++) {
      molecules[i] = createMolecules(r, size, 20000);
      molecules2[i] = createMolecules(r, size, 20000);
      c[i] = new DensityCounter(molecules[i], radius, true);
    }

    // How many distance comparison are we expected to make?
    // Compute mean density per grid cell (d) = nMolecules * 9 * d.
    final double d = molecules[0].length * radius * radius / (size * size);
    logger.info(
        FunctionUtils.getSupplier("Expected Comparisons = %f", molecules2[0].length * 9.0 * d));

    //@formatter:off
    final TimingService ts = new TimingService();
    //ts.execute(new MyTimingTask("countAllSimple") {
    //  public Object run(Object data) {
    //    int i = (Integer) data;
    //    return c[i].countAllSimple(molecules[i], nChannels - 1);
    //  }
    //});
    //ts.execute(new MyTimingTask("countAllSimple static") {
    //  public Object run(Object data) {
    //    int i = (Integer) data;
    //    return DensityCounter.countAll(molecules[i], molecules2[i], radius, nChannels - 1);
    //  }
    //});
    ts.execute(new MyTimingTask("countAllAroundMolecules single thread") {
      @Override
      public Object run(Object data) { final int i = (Integer) data;
        c[i].setNumberOfThreads(1);
        return c[i].countAll(molecules2[i], channels - 1); }
    });
    ts.execute(new MyTimingTask("countAllAroundMolecules single thread + constructor") {
      @Override
      public Object run(Object data) { final int i = (Integer) data;
          final DensityCounter c = new DensityCounter(molecules[i], radius, true);
          c.setNumberOfThreads(1);
          return c.countAll(molecules2[i], channels - 1); }
    });
    if (numberOfThreads > 1) {
      ts.execute(new MyTimingTask("countAllAroundMolecules multi thread") {
        @Override
        public Object run(Object data) { final int i = (Integer) data;
          c[i].setNumberOfThreads(numberOfThreads);
          return c[i].countAll(molecules2[i], channels - 1); }
      });
      ts.execute(new MyTimingTask("countAllAroundMolecules multi thread + constructor") {
        @Override
        public Object run(Object data) { final int i = (Integer) data;
            final DensityCounter c = new DensityCounter(molecules[i], radius, true);
            c.setNumberOfThreads(numberOfThreads);
            return c.countAll(molecules2[i], channels - 1); }
      });
    }
    //@formatter:on

    @SuppressWarnings("unused")
    final int size = ts.repeat();
    // ts.repeat(size);

    logger.info(ts.getReport());
  }

  /**
   * Creates the molecules. Creates clusters of molecules.
   *
   * @param rng the random number generator
   * @param size the size
   * @param numberOfMolecules the number of molecules
   * @return the simple molecule[]
   */
  private SimpleMolecule[] createMolecules(UniformRandomProvider rng, int size,
      int numberOfMolecules) {
    final double precision = 0.1; // pixels
    final int meanClusterSize = 5;
    final PoissonSampler p = new PoissonSampler(rng, meanClusterSize);

    final SimpleMolecule[] molecules = new SimpleMolecule[numberOfMolecules];
    for (int i = 0; i < numberOfMolecules;) {
      final float x = rng.nextFloat() * size;
      final float y = rng.nextFloat() * size;
      final int id = rng.nextInt(channels);
      final SharedStateContinuousSampler gx = SamplerUtils.createGaussianSampler(rng, x, precision);
      final SharedStateContinuousSampler gy = SamplerUtils.createGaussianSampler(rng, y, precision);

      int count = p.sample();
      while (i < numberOfMolecules && count-- > 0) {
        molecules[i++] = new SimpleMolecule((float) gx.sample(), (float) gy.sample(), id);
      }
    }
    return molecules;
  }
}
