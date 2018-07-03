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
package gdsc.core.clustering;

import java.awt.Rectangle;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;
import gdsc.test.TestSettings.TestComplexity;

public class DensityManagerTest
{
	int size = 256;
	float[] radii = new float[] { 2, 4, 8, 16 };
	int[] N = new int[] { 1000, 2000, 4000 };

	@Test
	public void densityWithTriangleMatchesDensity()
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateDensity(radius);
				int[] d2 = dm.calculateDensityTriangle(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	@Test
	public void densityWithGridMatchesDensity()
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateDensity(radius);
				int[] d2 = dm.calculateDensityGrid(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	@Test
	public void densityWithGridFasterThanDensityTriangle()
	{
		TestSettings.assume(TestComplexity.MEDIUM);

		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityTriangle(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityGrid(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("Grid vs Triangle. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
				TestSettings.info(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void densityWithGridFasterThanDensity()
	{
		TestSettings.assume(TestComplexity.MEDIUM);

		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensity(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityGrid(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("Grid vs Standard. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
				TestSettings.info(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void sumWithGridMatchesSum()
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				int s1 = dm.calculateSum(radius);
				int s2 = dm.calculateSumGrid(radius);

				Assert.assertEquals(String.format("N=%d, R=%f", n, radius), s1, s2);
			}
		}
	}

	@Test
	public void sumWithGridFasterThanSum()
	{
		TestSettings.assume(TestComplexity.MEDIUM);

		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateSum(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateSumGrid(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("Sum Grid vs Standard. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				TestSettings.info(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void blockDensityMatchesBlockDensity2()
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateBlockDensity(radius);
				int[] d2 = dm.calculateBlockDensity2(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	@Test
	public void blockDensity2MatchesBlockDensity3()
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateBlockDensity2(radius);
				int[] d2 = dm.calculateBlockDensity3(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	// This is not always true. The two are comparable in speed.
	//@Test
	public void blockDensityFasterThanBlockDensity2()
	{
		TestSettings.assume(TestComplexity.MEDIUM);

		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity2(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("calculateBlockDensity2 vs calculateBlockDensity. N=%d, R=%f : %fx faster",
						n, radius, (double) t1 / t2);
				TestSettings.info(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void blockDensity2FasterThanBlockDensity3()
	{
		TestSettings.assume(TestComplexity.MEDIUM);

		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(r, size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity3(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity2(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("calculateBlockDensity2 vs calculateBlockDensity3. N=%d, R=%f : %fx faster",
						n, radius, (double) t1 / t2);
				TestSettings.info(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	private DensityManager createDensityManager(RandomGenerator r, int size, int n)
	{
		float[] xcoord = new float[n];
		float[] ycoord = new float[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] = r.nextFloat() * size;
			ycoord[i] = r.nextFloat() * size;
		}
		DensityManager dm = new DensityManager(xcoord, ycoord, new Rectangle(size, size));
		return dm;
	}
}
