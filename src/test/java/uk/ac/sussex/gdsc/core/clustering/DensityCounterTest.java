package uk.ac.sussex.gdsc.core.clustering;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.GaussianSampler;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import uk.ac.sussex.gdsc.test.junit5.*;import uk.ac.sussex.gdsc.test.rng.RngFactory;import uk.ac.sussex.gdsc.core.clustering.DensityCounter.SimpleMolecule;
import uk.ac.sussex.gdsc.core.utils.rng.GaussianSamplerFactory;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TimingService;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

/**
 * Test the DensityCounter.
 */
@SuppressWarnings({"javadoc"})
public class DensityCounterTest {
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
  int[] N = new int[] {1000, 2000, 4000};
  int nChannels = 3;
  int speedTestSize = 5;

  @SeededTest
  public void countAllWithSimpleMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
        final int[][] d2 = c.countAllSimple(nChannels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  public void countAllWithSingleThreadMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
        final int[][] d2 = c.countAll(nChannels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  public void countAllWithMultiThreadSycnMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(4);
        c.multiThreadMode = DensityCounter.MODE_SYNC;

        final int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
        final int[][] d2 = c.countAll(nChannels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  public void countAllWithMultiThreadNonSyncMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(4);
        c.multiThreadMode = DensityCounter.MODE_NON_SYNC;

        final int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
        final int[][] d2 = c.countAll(nChannels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  public void countAllAroundMoleculesWithSimpleMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n / 2);
      final SimpleMolecule[] molecules2 = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, nChannels - 1);
        final int[][] d2 = c.countAllSimple(molecules2, nChannels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  public void countAllAroundMoleculesWithSingleThreadMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n / 2);
      final SimpleMolecule[] molecules2 = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(1);

        final int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, nChannels - 1);
        final int[][] d2 = c.countAll(molecules2, nChannels - 1);

        check(n, radius, d1, d2);
      }
    }
  }

  @SeededTest
  public void countAllAroundMoleculesWithMultiThreadMatches(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());
    for (final int n : N) {
      final SimpleMolecule[] molecules = createMolecules(r, size, n / 2);
      final SimpleMolecule[] molecules2 = createMolecules(r, size, n);

      for (final float radius : radii) {
        final DensityCounter c = new DensityCounter(molecules, radius, true);
        c.setNumberOfThreads(4);

        final int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, nChannels - 1);
        final int[][] d2 = c.countAll(molecules2, nChannels - 1);

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
    public Object getData(int i) {
      return i;
    }

    @Override
    public int getSize() {
      return speedTestSize;
    }
  }

  @SeededTest
  public void countAllSpeedTest(RandomSeed seed) {
    // Assumptions.assumeTrue(logger.isLoggable(Level.INFO)); Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());

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

    //@formatter:off
		final TimingService ts = new TimingService();
//		ts.execute(new MyTimingTask("countAllSimple")
//		{
//			public Object run(Object data) { int i = (Integer) data; return c[i].countAllSimple(nChannels - 1); }
//		});
//		ts.execute(new MyTimingTask("countAllSimple static")
//		{
//			public Object run(Object data) { int i = (Integer) data; return DensityCounter.countAll(molecules[i], radius, nChannels - 1); }
//		});
		ts.execute(new MyTimingTask("countAll single thread")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
				c[i].setNumberOfThreads(1);
				return c[i].countAll(nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAll single thread + constructor")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
    			final DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(1);
    			return c.countAll(nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAll multi thread")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
				c[i].setNumberOfThreads(nThreads);
				//c[i].gridPriority = null;
    			c[i].multiThreadMode = DensityCounter.MODE_SYNC;
				return c[i].countAll(nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAll multi thread + constructor")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
    			final DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(nThreads);
				c.gridPriority = null;
    			c.multiThreadMode = DensityCounter.MODE_SYNC;
    			return c.countAll(nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAll multi thread non-sync")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
				c[i].setNumberOfThreads(nThreads);
				//c[i].gridPriority = null;
    			c[i].multiThreadMode = DensityCounter.MODE_NON_SYNC;
				return c[i].countAll(nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAll multi thread non-sync + constructor")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
    			final DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(nThreads);
				c.gridPriority = null;
    			c.multiThreadMode = DensityCounter.MODE_NON_SYNC;
    			return c.countAll(nChannels - 1); }
		});

		//@formatter:on

    @SuppressWarnings("unused")
    final int size = ts.repeat();
    // ts.repeat(size);

    logger.info(ts.getReport());
  }

  @SeededTest
  public void countAllAroundMoleculesSpeedTest(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());

    // The multi-thread mode is faster when the number of molecules is large.

    // However both will be fast enough when the data size is small so it is
    // probably OK to leave it multi-threading by default.

    final float radius = 0.35f;
    final int nThreads = 16;

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
//		ts.execute(new MyTimingTask("countAllSimple")
//		{
//			public Object run(Object data) { int i = (Integer) data; return c[i].countAllSimple(molecules[i], nChannels - 1); }
//		});
//		ts.execute(new MyTimingTask("countAllSimple static")
//		{
//			public Object run(Object data) { int i = (Integer) data; return DensityCounter.countAll(molecules[i], molecules2[i], radius, nChannels - 1); }
//		});
		ts.execute(new MyTimingTask("countAllAroundMolecules single thread")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
				c[i].setNumberOfThreads(1);
				return c[i].countAll(molecules2[i], nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAllAroundMolecules single thread + constructor")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
    			final DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(1);
    			return c.countAll(molecules2[i], nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAllAroundMolecules multi thread")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
				c[i].setNumberOfThreads(nThreads);
				return c[i].countAll(molecules2[i], nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAllAroundMolecules multi thread + constructor")
		{
			@Override
			public Object run(Object data) { final int i = (Integer) data;
    			final DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(nThreads);
    			return c.countAll(molecules2[i], nChannels - 1); }
		});

		//@formatter:on

    @SuppressWarnings("unused")
    final int size = ts.repeat();
    // ts.repeat(size);

    logger.info(ts.getReport());
  }

  /**
   * Creates the molecules. Creates clusters of molecules.
   *
   * @param size the size
   * @param n the number of molecules
   * @return the simple molecule[]
   */
  private SimpleMolecule[] createMolecules(UniformRandomProvider r, int size, int n) {
    final double precision = 0.1; // pixels
    final int meanClusterSize = 5;
    final PoissonSampler p = new PoissonSampler(r, meanClusterSize);

    final SimpleMolecule[] molecules = new SimpleMolecule[n];
    for (int i = 0; i < n;) {
      final float x = r.nextFloat() * size;
      final float y = r.nextFloat() * size;
      final int id = r.nextInt(nChannels);
      final GaussianSampler gx = GaussianSamplerFactory.createGaussianSampler(r, x, precision);
      final GaussianSampler gy = GaussianSamplerFactory.createGaussianSampler(r, y, precision);

      int c = p.sample();
      while (i < n && c-- > 0) {
        molecules[i++] = new SimpleMolecule((float) gx.sample(), (float) gy.sample(), id);
      }
    }
    return molecules;
  }
}
