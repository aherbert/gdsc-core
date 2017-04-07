package gdsc.core.clustering;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.clustering.DensityCounter.SimpleMolecule;
import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;

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

	/**
	 * Count all with single thread matches count all.
	 */
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
	
	/**
	 * Count all with single thread matches count all.
	 */
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

	private abstract class MyTimingTask extends BaseTimingTask
	{
		public MyTimingTask(String name)
		{
			super(name);
		}

		public Object getData(int i)
		{
			return i;
		}

		public int getSize()
		{
			return speedTestSize;
		}
	}

	/**
	 * Count all with single thread matches count all.
	 */
	@Test
	public void countAllSpeedTest()
	{
		final float radius = 1;

		final SimpleMolecule[][] molecules = new SimpleMolecule[speedTestSize][];
		final DensityCounter[] c = new DensityCounter[molecules.length];
		for (int i = 0; i < molecules.length; i++)
		{
			molecules[i] = createMolecules(size, 5000);
			c[i] = new DensityCounter(molecules[i], radius, true);
		}

		//@formatter:off
		TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("countAllSimple")
		{
			public Object run(Object data) { int i = (Integer) data; return c[i].countAllSimple(nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAllSimple static")
		{
			public Object run(Object data) { int i = (Integer) data; return DensityCounter.countAll(molecules[i], radius, nChannels - 1); }
		});
		ts.execute(new MyTimingTask("countAll")
		{
			public Object run(Object data) { int i = (Integer) data; return c[i].countAll(nChannels - 1); }
		});		
		ts.execute(new MyTimingTask("countAll + constructor")
		{
			public Object run(Object data) { int i = (Integer) data; 
			return new DensityCounter(molecules[i], radius, true).countAll(nChannels - 1); }
		});		

		//@formatter:on

		//int size = ts.repeat();
		//ts.repeat(size);

		ts.report();
	}

	/**
	 * Creates the molecules.
	 *
	 * @param size
	 *            the size
	 * @param n
	 *            the n
	 * @return the simple molecule[]
	 */
	private SimpleMolecule[] createMolecules(int size, int n)
	{
		RandomGenerator r = new Well19937c();

		SimpleMolecule[] molecules = new SimpleMolecule[n];
		for (int i = 0; i < n; i++)
		{
			molecules[i] = new SimpleMolecule(r.nextFloat() * size, r.nextFloat() * size, r.nextInt(nChannels));
		}
		return molecules;
	}
}
