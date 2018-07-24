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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import java.awt.Rectangle;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.SpeedTest;

@SuppressWarnings({ "javadoc" })
public class DensityManagerTest
{
	int size = 256;
	float[] radii = new float[] { 2, 4, 8, 16 };
	int[] N = new int[] { 1000, 2000, 4000 };

	@Test
	public void densityWithTriangleMatchesDensity()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
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

	@Test
	public void densityWithGridMatchesDensity()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
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

	@Test
	public void densityWithGridFasterThanDensityTriangle()
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);

		final RandomGenerator r = TestSettings.getRandomGenerator();
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
				TestLog.info(msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@Test
	public void densityWithGridFasterThanDensity()
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);

		final RandomGenerator r = TestSettings.getRandomGenerator();
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
				TestLog.info(msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@Test
	public void sumWithGridMatchesSum()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
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

	@Test
	public void sumWithGridFasterThanSum()
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);

		final RandomGenerator r = TestSettings.getRandomGenerator();
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
				TestLog.info(msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@Test
	public void blockDensityMatchesBlockDensity2()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
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

	@Test
	public void blockDensity2MatchesBlockDensity3()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
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
	//@Test
	public void blockDensityFasterThanBlockDensity2()
	{
		ExtraAssumptions.assumeSpeedTest();

		final RandomGenerator r = TestSettings.getRandomGenerator();
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
				TestLog.info(msg);
				Assertions.assertTrue(t2 < t1, msg);
			}
		}
	}

	@SpeedTest
	public void blockDensity2FasterThanBlockDensity3()
	{
		ExtraAssumptions.assumeSpeedTest();

		final RandomGenerator r = TestSettings.getRandomGenerator();
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
				//TestLog.info(msg);
				//Assertions.assertTrue(t2 < t1, msg);
				TestLog.logSpeedTestResult(t2 < t1, msg);
			}
		}
	}

	private static DensityManager createDensityManager(RandomGenerator r, int size, int n)
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
