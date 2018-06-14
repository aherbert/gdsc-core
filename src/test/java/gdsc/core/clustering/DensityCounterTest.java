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

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.clustering.DensityCounter.SimpleMolecule;
import gdsc.test.BaseTimingTask;
import gdsc.test.TestSettings;
import gdsc.test.TimingService;

/**
 * Test the DensityCounter.
 */
public class DensityCounterTest
{
	boolean skipSpeedTest = true;

	int size = 256;
	float[] radii = new float[] { 2, 4, 8 };
	int[] N = new int[] { 1000, 2000, 4000 };
	int nChannels = 3;
	int speedTestSize = 5;

	@Test
	public void countAllWithSimpleMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(1);

				int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
				int[][] d2 = c.countAllSimple(nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	@Test
	public void countAllWithSingleThreadMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(1);

				int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
				int[][] d2 = c.countAll(nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	@Test
	public void countAllWithMultiThreadSycnMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(4);
				c.multiThreadMode = DensityCounter.MODE_SYNC;

				int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
				int[][] d2 = c.countAll(nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	@Test
	public void countAllWithMultiThreadNonSyncMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(4);
				c.multiThreadMode = DensityCounter.MODE_NON_SYNC;

				int[][] d1 = DensityCounter.countAll(molecules, radius, nChannels - 1);
				int[][] d2 = c.countAll(nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	@Test
	public void countAllAroundMoleculesWithSimpleMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n / 2);
			SimpleMolecule[] molecules2 = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(1);

				int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, nChannels - 1);
				int[][] d2 = c.countAllSimple(molecules2, nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	@Test
	public void countAllAroundMoleculesWithSingleThreadMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n / 2);
			SimpleMolecule[] molecules2 = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(1);

				int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, nChannels - 1);
				int[][] d2 = c.countAll(molecules2, nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	@Test
	public void countAllAroundMoleculesWithMultiThreadMatches()
	{
		for (int n : N)
		{
			SimpleMolecule[] molecules = createMolecules(size, n / 2);
			SimpleMolecule[] molecules2 = createMolecules(size, n);

			for (float radius : radii)
			{
				DensityCounter c = new DensityCounter(molecules, radius, true);
				c.setNumberOfThreads(4);

				int[][] d1 = DensityCounter.countAll(molecules, molecules2, radius, nChannels - 1);
				int[][] d2 = c.countAll(molecules2, nChannels - 1);

				String name = String.format("N=%d, R=%f", n, radius);
				Assert.assertNotNull(name, d1);
				Assert.assertNotNull(name, d2);
				Assert.assertEquals(name, d1.length, n);
				Assert.assertEquals(name, d2.length, n);
				for (int i = 0; i < n; i++)
					Assert.assertArrayEquals(name, d1[i], d2[i]);
			}
		}
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		public MyTimingTask(String name)
		{
			super(name);
		}

		@Override
		public Object getData(int i)
		{
			return i;
		}

		@Override
		public int getSize()
		{
			return speedTestSize;
		}
	}

	@Test
	public void countAllSpeedTest()
	{
		// The single thread mode is faster when the radius is small.
		// The multi-thread mode is faster when the radius is large (>4).
		// The non-synchronised multi-thread mode is faster than the synchronised mode.

		// However both will be fast enough when the radius is small so it is 
		// probably OK to leave it multi-threading by default.

		final float radius = 0.35f;
		final int nThreads = 16;

		// TODO - Repeat this at different number of molecules to to determine if multi-threading is worth it

		final SimpleMolecule[][] molecules = new SimpleMolecule[speedTestSize][];
		final DensityCounter[] c = new DensityCounter[molecules.length];
		for (int i = 0; i < molecules.length; i++)
		{
			molecules[i] = createMolecules(size, 20000);
			c[i] = new DensityCounter(molecules[i], radius, true);
		}

		// How many distance comparison are we expected to make?
		// Compute mean density per grid cell (d):
		// single/sync multi = nCells * (5 * d * d) // For simplicity the n*(n-1)/2 for the main cell is ignored
		// non-sync multi = nCells * (9 * d * d)
		double d = molecules[0].length * radius * radius / (size * size);
		double nCells = (size / radius) * (size / radius);
		TestSettings.info("Expected Comparisons : Single = %f, Multi non-sync = %f\n", nCells * 5 * d * d,
				nCells * 9 * d * d);

		//@formatter:off
		TimingService ts = new TimingService();
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
			public Object run(Object data) { int i = (Integer) data;
				c[i].setNumberOfThreads(1);
				return c[i].countAll(nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAll single thread + constructor")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
    			DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(1);
    			return c.countAll(nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAll multi thread")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
				c[i].setNumberOfThreads(nThreads);
				//c[i].gridPriority = null;
    			c[i].multiThreadMode = DensityCounter.MODE_SYNC;
				return c[i].countAll(nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAll multi thread + constructor")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
    			DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(nThreads);
				c.gridPriority = null;
    			c.multiThreadMode = DensityCounter.MODE_SYNC;
    			return c.countAll(nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAll multi thread non-sync")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
				c[i].setNumberOfThreads(nThreads);
				//c[i].gridPriority = null;
    			c[i].multiThreadMode = DensityCounter.MODE_NON_SYNC;
				return c[i].countAll(nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAll multi thread non-sync + constructor")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
    			DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(nThreads);
				c.gridPriority = null;
    			c.multiThreadMode = DensityCounter.MODE_NON_SYNC;
    			return c.countAll(nChannels - 1); }
		});		

		//@formatter:on

		@SuppressWarnings("unused")
		int size = ts.repeat();
		//ts.repeat(size);

		ts.report();
	}

	@Test
	public void countAllAroundMoleculesSpeedTest()
	{
		// The multi-thread mode is faster when the number of molecules is large.

		// However both will be fast enough when the data size is small so it is 
		// probably OK to leave it multi-threading by default.

		final float radius = 0.35f;
		final int nThreads = 16;

		// TODO - Repeat this at different number of molecules to to determine if multi-threading is worth it

		final SimpleMolecule[][] molecules = new SimpleMolecule[speedTestSize][];
		final SimpleMolecule[][] molecules2 = new SimpleMolecule[speedTestSize][];
		final DensityCounter[] c = new DensityCounter[molecules.length];
		for (int i = 0; i < molecules.length; i++)
		{
			molecules[i] = createMolecules(size, 20000);
			molecules2[i] = createMolecules(size, 20000);
			c[i] = new DensityCounter(molecules[i], radius, true);
		}

		// How many distance comparison are we expected to make?
		// Compute mean density per grid cell (d) = nMolecules * 9 * d.
		double d = molecules[0].length * radius * radius / (size * size);
		TestSettings.info("Expected Comparisons = %f\n", molecules2[0].length * 9.0 * d);

		//@formatter:off
		TimingService ts = new TimingService();
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
			public Object run(Object data) { int i = (Integer) data;
				c[i].setNumberOfThreads(1);
				return c[i].countAll(molecules2[i], nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAllAroundMolecules single thread + constructor")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
    			DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(1);
    			return c.countAll(molecules2[i], nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAllAroundMolecules multi thread")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
				c[i].setNumberOfThreads(nThreads);
				return c[i].countAll(molecules2[i], nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAllAroundMolecules multi thread + constructor")
		{
			@Override
			public Object run(Object data) { int i = (Integer) data;
    			DensityCounter c = new DensityCounter(molecules[i], radius, true);
    			c.setNumberOfThreads(nThreads);
    			return c.countAll(molecules2[i], nChannels - 1); }
		});		

		//@formatter:on

		@SuppressWarnings("unused")
		int size = ts.repeat();
		//ts.repeat(size);

		ts.report();
	}

	/**
	 * Creates the molecules. Creates clusters of molecules.
	 *
	 * @param size
	 *            the size
	 * @param n
	 *            the number of molecules
	 * @return the simple molecule[]
	 */
	private SimpleMolecule[] createMolecules(int size, int n)
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		RandomDataGenerator rdg = new RandomDataGenerator(r);

		float precision = 0.1f; // pixels
		int meanClusterSize = 5;

		SimpleMolecule[] molecules = new SimpleMolecule[n];
		for (int i = 0; i < n;)
		{
			float x = r.nextFloat() * size;
			float y = r.nextFloat() * size;
			int id = r.nextInt(nChannels);

			int c = (int) rdg.nextPoisson(meanClusterSize);
			while (i < n && c-- > 0)
			{
				molecules[i++] = new SimpleMolecule((float) rdg.nextGaussian(x, precision),
						(float) rdg.nextGaussian(y, precision), id);
			}
		}
		return molecules;
	}
}
