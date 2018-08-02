package uk.ac.sussex.gdsc.core.clustering;

import java.awt.Rectangle;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class DensityManagerTest
{
	private static Logger logger;

	@BeforeAll
	public static void beforeAll()
	{
		logger = Logger.getLogger(DensityManagerTest.class.getName());
	}

	@AfterAll
	public static void afterAll()
	{
		logger = null;
	}

	int size = 256;
	float[] radii = new float[] { 2, 4, 8, 16 };
	int[] N = new int[] { 1000, 2000, 4000 };

	@SeededTest
	public void densityWithTriangleMatchesDensity(RandomSeed seed)
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				final int[] d1 = dm.calculateDensity(radius);
				final int[] d2 = dm.calculateDensityTriangle(radius);

				Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
			}
		}
	}

	@SeededTest
	public void densityWithGridMatchesDensity(RandomSeed seed)
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				final int[] d1 = dm.calculateDensity(radius);
				final int[] d2 = dm.calculateDensityGrid(radius);

				Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
			}
		}
	}

	@SeededTest
	public void densityWithGridFasterThanDensityTriangle(RandomSeed seed)
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);

		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityTriangle(radius);
				final long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityGrid(radius);
				final long t2 = System.nanoTime() - start;

				final String msg = String.format("Grid vs Triangle. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				TestLog.info(logger, msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@SeededTest
	public void densityWithGridFasterThanDensity(RandomSeed seed)
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);

		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensity(radius);
				final long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityGrid(radius);
				final long t2 = System.nanoTime() - start;

				final String msg = String.format("Grid vs Standard. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				TestLog.info(logger, msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@SeededTest
	public void sumWithGridMatchesSum(RandomSeed seed)
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				final int s1 = dm.calculateSum(radius);
				final int s2 = dm.calculateSumGrid(radius);

				Assertions.assertEquals(s1, s2, () -> String.format("N=%d, R=%f", n, radius));
			}
		}
	}

	@SeededTest
	public void sumWithGridFasterThanSum(RandomSeed seed)
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);

		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateSum(radius);
				final long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateSumGrid(radius);
				final long t2 = System.nanoTime() - start;

				final String msg = String.format("Sum Grid vs Standard. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				TestLog.info(logger, msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@SeededTest
	public void blockDensityMatchesBlockDensity2(RandomSeed seed)
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				final int[] d1 = dm.calculateBlockDensity(radius);
				final int[] d2 = dm.calculateBlockDensity2(radius);

				Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
			}
		}
	}

	@SeededTest
	public void blockDensity2MatchesBlockDensity3(RandomSeed seed)
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				final int[] d1 = dm.calculateBlockDensity2(radius);
				final int[] d2 = dm.calculateBlockDensity3(radius);

				Assertions.assertArrayEquals(d1, d2, () -> String.format("N=%d, R=%f", n, radius));
			}
		}
	}

	// This is not always true. The two are comparable in speed.
	//@SeededTest
	public void blockDensityFasterThanBlockDensity2(RandomSeed seed)
	{
		ExtraAssumptions.assumeSpeedTest();

		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity(radius);
				final long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity2(radius);
				final long t2 = System.nanoTime() - start;

				final String msg = String.format(
						"calculateBlockDensity2 vs calculateBlockDensity. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				TestLog.info(logger, msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@SpeedTag
	@SeededTest
	public void blockDensity2FasterThanBlockDensity3(RandomSeed seed)
	{
		ExtraAssumptions.assumeSpeedTest();

		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (final int n : N)
		{
			final DensityManager dm = createDensityManager(r, size, n);

			for (final float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity3(radius);
				final long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity2(radius);
				final long t2 = System.nanoTime() - start;

				final String msg = String.format(
						"calculateBlockDensity2 vs calculateBlockDensity3. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				// This is not always faster
				//TestLog.info(logger,msg);
				//Assertions.assertTrue(t2 < t1, msg);
				TestLog.logTestResult(logger, t2 < t1, msg);
			}
		}
	}

	private static DensityManager createDensityManager(UniformRandomProvider r, int size, int n)
	{
		final float[] xcoord = new float[n];
		final float[] ycoord = new float[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] = r.nextFloat() * size;
			ycoord[i] = r.nextFloat() * size;
		}
		final DensityManager dm = new DensityManager(xcoord, ycoord, new Rectangle(size, size));
		return dm;
	}
}
