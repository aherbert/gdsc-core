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
package gdsc.core.math;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.data.IntegerType;
import gdsc.core.utils.Statistics;
import gdsc.test.TestSettings;

public class ArrayMomentTest
{
	final double DELTA = 1e-8;
	final int MAX_INT = 65335; // Unsigned 16-bit int

	@Test
	public void canComputeRollingMomentDouble()
	{
		canComputeMoment("Single", new double[] { Math.PI }, new RollingArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		double[] d = new double[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();
		canComputeMoment("Uniform", d, new RollingArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextGaussian();
		canComputeMoment("Gaussian", d, new RollingArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new RollingArrayMoment());
	}

	@Test
	public void canComputeRollingMomentFloat()
	{
		canComputeMoment("Single", new float[] { (float) Math.PI }, new RollingArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		float[] d = new float[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextFloat();
		canComputeMoment("Uniform", d, new RollingArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = (float) rand.nextGaussian();
		canComputeMoment("Gaussian", d, new RollingArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new RollingArrayMoment());
	}

	@Test
	public void canComputeRollingMomentInt()
	{
		canComputeMoment("Single", new int[] { 42 }, new RollingArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[] d = new int[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextInt(MAX_INT);
		canComputeMoment("Uniform", d, new RollingArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new RollingArrayMoment());
	}

	@Test
	public void canComputeRollingArrayMomentDouble()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();
		double[][] d = new double[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new double[] { rand.nextDouble() };
		canComputeArrayMoment("Single", d, new RollingArrayMoment());

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformDouble(rand, n);
		canComputeArrayMoment("Uniform", d, new RollingArrayMoment());
	}

	@Test
	public void canCombineRollingArrayMomentDouble()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();
		double[][] d = new double[50][];

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformDouble(rand, n);

		RollingArrayMoment r1 = new RollingArrayMoment();
		int size = 6;
		RollingArrayMoment[] r2 = new RollingArrayMoment[size];
		for (int i = 0; i < size; i++)
			r2[i] = new RollingArrayMoment();
		for (int i = 0; i < d.length; i++)
		{
			r1.add(d[i]);
			r2[i % size].add(d[i]);
		}

		double[] em1 = r1.getFirstMoment();
		double[] em2 = r1.getSecondMoment();
		double[] ev = r1.getVariance();
		double[] esd = r1.getStandardDeviation();

		for (int i = 1; i < size; i++)
			r2[0].add(r2[i]);

		double[] om1 = r2[0].getFirstMoment();
		double[] om2 = r2[0].getSecondMoment();
		double[] ov = r2[0].getVariance();
		double[] osd = r2[0].getStandardDeviation();

		TestSettings.assertArrayEquals("Mean", em1, om1, DELTA);
		TestSettings.assertArrayEquals("2nd Moment", em2, om2, DELTA);
		TestSettings.assertArrayEquals("Variance", ev, ov, DELTA);
		TestSettings.assertArrayEquals("SD", esd, osd, DELTA);
	}

	// Copy to here

	@Test
	public void canComputeSimpleMomentDouble()
	{
		canComputeMoment("Single", new double[] { Math.PI }, new SimpleArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		double[] d = new double[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();
		canComputeMoment("Uniform", d, new SimpleArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextGaussian();
		canComputeMoment("Gaussian", d, new SimpleArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new SimpleArrayMoment());
	}

	@Test
	public void canComputeSimpleMomentFloat()
	{
		canComputeMoment("Single", new float[] { (float) Math.PI }, new SimpleArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		float[] d = new float[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextFloat();
		canComputeMoment("Uniform", d, new SimpleArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = (float) rand.nextGaussian();
		canComputeMoment("Gaussian", d, new SimpleArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new SimpleArrayMoment());
	}

	@Test
	public void canComputeSimpleMomentInt()
	{
		canComputeMoment("Single", new int[] { 42 }, new SimpleArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[] d = new int[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextInt(MAX_INT);
		canComputeMoment("Uniform", d, new SimpleArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new SimpleArrayMoment());
	}

	@Test
	public void canComputeSimpleArrayMomentInt()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[][] d = new int[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new int[] { rand.nextInt(MAX_INT) };
		canComputeArrayMoment("Single", d, new SimpleArrayMoment());

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);
		canComputeArrayMoment("Uniform", d, new SimpleArrayMoment());
	}

	@Test
	public void canCombineSimpleArrayMomentInt()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[][] d = new int[50][];

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);

		SimpleArrayMoment r1 = new SimpleArrayMoment();
		int size = 6;
		SimpleArrayMoment[] r2 = new SimpleArrayMoment[size];
		for (int i = 0; i < size; i++)
			r2[i] = new SimpleArrayMoment();
		for (int i = 0; i < d.length; i++)
		{
			r1.add(d[i]);
			r2[i % size].add(d[i]);
		}

		double[] em1 = r1.getFirstMoment();
		double[] em2 = r1.getSecondMoment();
		double[] ev = r1.getVariance();
		double[] esd = r1.getStandardDeviation();

		for (int i = 1; i < size; i++)
			r2[0].add(r2[i]);

		double[] om1 = r2[0].getFirstMoment();
		double[] om2 = r2[0].getSecondMoment();
		double[] ov = r2[0].getVariance();
		double[] osd = r2[0].getStandardDeviation();

		TestSettings.assertArrayEquals("Mean", em1, om1, DELTA);
		TestSettings.assertArrayEquals("2nd Moment", em2, om2, DELTA);
		TestSettings.assertArrayEquals("Variance", ev, ov, DELTA);
		TestSettings.assertArrayEquals("SD", esd, osd, DELTA);
	}

	@Test
	public void canComputeIntegerMomentInt()
	{
		canComputeMoment("Single", new int[] { 42 }, new IntegerArrayMoment());

		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[] d = new int[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextInt(MAX_INT);
		canComputeMoment("Uniform", d, new IntegerArrayMoment());

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d, new IntegerArrayMoment());
	}

	@Test
	public void canComputeIntegerArrayMomentInt()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[][] d = new int[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new int[] { rand.nextInt(MAX_INT) };
		canComputeArrayMoment("Single", d, new IntegerArrayMoment());

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);
		canComputeArrayMoment("Uniform", d, new IntegerArrayMoment());
	}

	@Test
	public void canCombineIntegerArrayMomentInt()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();
		int[][] d = new int[50][];

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);

		IntegerArrayMoment r1 = new IntegerArrayMoment();
		int size = 6;
		IntegerArrayMoment[] r2 = new IntegerArrayMoment[size];
		for (int i = 0; i < size; i++)
			r2[i] = new IntegerArrayMoment();
		for (int i = 0; i < d.length; i++)
		{
			r1.add(d[i]);
			r2[i % size].add(d[i]);
		}

		double[] em1 = r1.getFirstMoment();
		double[] em2 = r1.getSecondMoment();
		double[] ev = r1.getVariance();
		double[] esd = r1.getStandardDeviation();

		for (int i = 1; i < size; i++)
			r2[0].add(r2[i]);

		double[] om1 = r2[0].getFirstMoment();
		double[] om2 = r2[0].getSecondMoment();
		double[] ov = r2[0].getVariance();
		double[] osd = r2[0].getStandardDeviation();

		// No delta as integer math should be exact
		TestSettings.assertArrayEquals("Mean", em1, om1, 0);
		TestSettings.assertArrayEquals("2nd Moment", em2, om2, 0);
		TestSettings.assertArrayEquals("Variance", ev, ov, 0);
		TestSettings.assertArrayEquals("SD", esd, osd, 0);
	}

	@Test
	public void canTestIfValidIntegerData()
	{
		// 2^16^2 * 2^31-1 ~ 2^63 : This is OK
		Assert.assertTrue(IntegerArrayMoment.isValid(IntegerType.UNSIGNED_16, Integer.MAX_VALUE));

		// (2^31-1)^2 ~ 2^62 : We should be able to 2 of these but not 3
		Assert.assertTrue(IntegerArrayMoment.isValid(IntegerType.SIGNED_32, 1));
		Assert.assertTrue(IntegerArrayMoment.isValid(IntegerType.SIGNED_32, 2));
		Assert.assertFalse(IntegerArrayMoment.isValid(IntegerType.SIGNED_32, 3));

		// 2^32^2 == 2^64 : We cannot do this as 
		Assert.assertFalse(IntegerArrayMoment.isValid(IntegerType.UNSIGNED_32, 1));
	}

	private void canComputeMoment(String title, double[] d, ArrayMoment r2)
	{
		Statistics m1 = new Statistics();
		m1.add(d);
		SecondMoment m2 = new SecondMoment();
		m2.incrementAll(d);
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		TestSettings.assertEquals(title + " Mean", m1.getMean(), r2.getFirstMoment()[0], DELTA);
		TestSettings.assertEquals(title + " 2nd Moment", m2.getResult(), r2.getSecondMoment()[0], DELTA);
		TestSettings.assertEquals(title + " Variance", m1.getVariance(), r2.getVariance()[0], DELTA);
		TestSettings.assertEquals(title + " SD", m1.getStandardDeviation(), r2.getStandardDeviation()[0], DELTA);
	}

	private void canComputeMoment(String title, float[] d, ArrayMoment r2)
	{
		Statistics m1 = new Statistics();
		m1.add(d);
		SecondMoment m2 = new SecondMoment();
		m2.incrementAll(toDouble(d));
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		TestSettings.assertEquals(title + " Mean", m1.getMean(), r2.getFirstMoment()[0], DELTA);
		TestSettings.assertEquals(title + " 2nd Moment", m2.getResult(), r2.getSecondMoment()[0], DELTA);
		TestSettings.assertEquals(title + " Variance", m1.getVariance(), r2.getVariance()[0], DELTA);
		TestSettings.assertEquals(title + " SD", m1.getStandardDeviation(), r2.getStandardDeviation()[0], DELTA);
	}

	private double[] toDouble(float[] in)
	{
		double[] d = new double[in.length];
		for (int i = 0; i < d.length; i++)
			d[i] = in[i];
		return d;
	}

	private void canComputeMoment(String title, int[] d, ArrayMoment r2)
	{
		Statistics m1 = new Statistics();
		m1.add(d);
		SecondMoment m2 = new SecondMoment();
		m2.incrementAll(toDouble(d));
		for (int i = 0; i < d.length; i++)
			r2.add(new int[] { d[i] });
		TestSettings.assertEquals(title + " Mean", m1.getMean(), r2.getFirstMoment()[0], DELTA);
		TestSettings.assertEquals(title + " 2nd Moment", m2.getResult(), r2.getSecondMoment()[0], DELTA);
		TestSettings.assertEquals(title + " Variance", m1.getVariance(), r2.getVariance()[0], DELTA);
		TestSettings.assertEquals(title + " SD", m1.getStandardDeviation(), r2.getStandardDeviation()[0], DELTA);
	}

	private double[] toDouble(int[] in)
	{
		double[] d = new double[in.length];
		for (int i = 0; i < d.length; i++)
			d[i] = in[i];
		return d;
	}

	private double[] uniformDouble(RandomGenerator rand, int n)
	{
		double[] d = new double[n];
		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();
		return d;
	}

	private int[] uniformInt(RandomGenerator rand, int n)
	{
		int[] d = new int[n];
		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextInt(MAX_INT);
		return d;
	}

	private void canComputeArrayMoment(String title, double[][] d, ArrayMoment r2)
	{
		for (int i = 0; i < d.length; i++)
			r2.add(d[i]);
		double[] om1 = r2.getFirstMoment();
		double[] om2 = r2.getSecondMoment();
		double[] ov = r2.getVariance();
		double[] osd = r2.getStandardDeviation();

		for (int n = d[0].length; n-- > 0;)
		{
			Statistics m1 = new Statistics();
			SecondMoment m2 = new SecondMoment();
			for (int i = 0; i < d.length; i++)
			{
				m1.add(d[i][n]);
				m2.increment(d[i][n]);
			}
			TestSettings.assertEquals(title + " Mean", m1.getMean(), om1[n], DELTA);
			TestSettings.assertEquals(title + " 2nd Moment", m2.getResult(), om2[n], DELTA);
			TestSettings.assertEquals(title + " Variance", m1.getVariance(), ov[n], DELTA);
			TestSettings.assertEquals(title + " SD", m1.getStandardDeviation(), osd[n], DELTA);
		}
	}

	private void canComputeArrayMoment(String title, int[][] d, ArrayMoment r2)
	{
		for (int i = 0; i < d.length; i++)
			r2.add(d[i]);
		double[] om1 = r2.getFirstMoment();
		double[] om2 = r2.getSecondMoment();
		double[] ov = r2.getVariance();
		double[] osd = r2.getStandardDeviation();

		for (int n = d[0].length; n-- > 0;)
		{
			Statistics m1 = new Statistics();
			SecondMoment m2 = new SecondMoment();
			for (int i = 0; i < d.length; i++)
			{
				m1.add(d[i][n]);
				m2.increment(d[i][n]);
			}
			TestSettings.assertEquals(title + " Mean", m1.getMean(), om1[n], DELTA);
			TestSettings.assertEquals(title + " 2nd Moment", m2.getResult(), om2[n], DELTA);
			TestSettings.assertEquals(title + " Variance", m1.getVariance(), ov[n], DELTA);
			TestSettings.assertEquals(title + " SD", m1.getStandardDeviation(), osd[n], DELTA);
		}
	}

	//@Test
	public void canComputeMomentForLargeSeries()
	{
		RandomGenerator rand = TestSettings.getRandomGenerator();

		SimpleArrayMoment m1 = new SimpleArrayMoment();
		SecondMoment m2 = new SecondMoment();
		RollingArrayMoment r2 = new RollingArrayMoment();

		// Test if the standard Statistics object is good enough for 
		// computing the mean and variance of sCMOS data from 60,000 frames. It seems it is.
		for (int i = 600000; i-- > 0;)
		{
			double d = 100.345 + rand.nextGaussian() * Math.PI;
			m1.add(d);
			m2.increment(d);
			r2.add(d);
		}
		TestSettings.info("Mean %s vs %s, SD %s vs %s\n", Double.toString(m1.getFirstMoment()[0]),
				Double.toString(r2.getFirstMoment()[0]), Double.toString(m1.getStandardDeviation()[0]),
				Double.toString(r2.getStandardDeviation()[0]));
		TestSettings.assertEquals("Mean", m1.getFirstMoment()[0], r2.getFirstMoment()[0], DELTA);
		TestSettings.assertEquals("2nd Moment", m2.getResult(), r2.getSecondMoment()[0], 0);
	}
}
