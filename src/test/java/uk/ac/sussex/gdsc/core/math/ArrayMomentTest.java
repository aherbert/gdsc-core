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
package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.core.data.IntegerType;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;

@SuppressWarnings({ "javadoc" })
public class ArrayMomentTest
{
	final double DELTA = 1e-8;
	final int MAX_INT = 65335; // Unsigned 16-bit int

	@Test
	public void canComputeRollingMomentDouble()
	{
		canComputeMoment("Single", new double[] { Math.PI }, new RollingArrayMoment());

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final double[] d = new double[1000];

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

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final float[] d = new float[1000];

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

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[] d = new int[1000];

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
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final double[][] d = new double[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new double[] { rand.nextDouble() };
		canComputeArrayMoment("Single", d, new RollingArrayMoment());

		final int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformDouble(rand, n);
		canComputeArrayMoment("Uniform", d, new RollingArrayMoment());
	}

	@Test
	public void canCombineRollingArrayMomentDouble()
	{
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final double[][] d = new double[50][];

		final int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformDouble(rand, n);

		final RollingArrayMoment r1 = new RollingArrayMoment();
		final int size = 6;
		final RollingArrayMoment[] r2 = new RollingArrayMoment[size];
		for (int i = 0; i < size; i++)
			r2[i] = new RollingArrayMoment();
		for (int i = 0; i < d.length; i++)
		{
			r1.add(d[i]);
			r2[i % size].add(d[i]);
		}

		final double[] em1 = r1.getFirstMoment();
		final double[] em2 = r1.getSecondMoment();
		final double[] ev = r1.getVariance();
		final double[] esd = r1.getStandardDeviation();

		for (int i = 1; i < size; i++)
			r2[0].add(r2[i]);

		final double[] om1 = r2[0].getFirstMoment();
		final double[] om2 = r2[0].getSecondMoment();
		final double[] ov = r2[0].getVariance();
		final double[] osd = r2[0].getStandardDeviation();

		ExtraAssertions.assertArrayEqualsRelative(em1, om1, DELTA, "Mean");
		ExtraAssertions.assertArrayEqualsRelative(em2, om2, DELTA, "2nd Moment");
		ExtraAssertions.assertArrayEqualsRelative(ev, ov, DELTA, "Variance");
		ExtraAssertions.assertArrayEqualsRelative(esd, osd, DELTA, "SD");
	}

	// Copy to here

	@Test
	public void canComputeSimpleMomentDouble()
	{
		canComputeMoment("Single", new double[] { Math.PI }, new SimpleArrayMoment());

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final double[] d = new double[1000];

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

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final float[] d = new float[1000];

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

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[] d = new int[1000];

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
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[][] d = new int[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new int[] { rand.nextInt(MAX_INT) };
		canComputeArrayMoment("Single", d, new SimpleArrayMoment());

		final int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);
		canComputeArrayMoment("Uniform", d, new SimpleArrayMoment());
	}

	@Test
	public void canCombineSimpleArrayMomentInt()
	{
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[][] d = new int[50][];

		final int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);

		final SimpleArrayMoment r1 = new SimpleArrayMoment();
		final int size = 6;
		final SimpleArrayMoment[] r2 = new SimpleArrayMoment[size];
		for (int i = 0; i < size; i++)
			r2[i] = new SimpleArrayMoment();
		for (int i = 0; i < d.length; i++)
		{
			r1.add(d[i]);
			r2[i % size].add(d[i]);
		}

		final double[] em1 = r1.getFirstMoment();
		final double[] em2 = r1.getSecondMoment();
		final double[] ev = r1.getVariance();
		final double[] esd = r1.getStandardDeviation();

		for (int i = 1; i < size; i++)
			r2[0].add(r2[i]);

		final double[] om1 = r2[0].getFirstMoment();
		final double[] om2 = r2[0].getSecondMoment();
		final double[] ov = r2[0].getVariance();
		final double[] osd = r2[0].getStandardDeviation();

		ExtraAssertions.assertArrayEqualsRelative(em1, om1, DELTA, "Mean");
		ExtraAssertions.assertArrayEqualsRelative(em2, om2, DELTA, "2nd Moment");
		ExtraAssertions.assertArrayEqualsRelative(ev, ov, DELTA, "Variance");
		ExtraAssertions.assertArrayEqualsRelative(esd, osd, DELTA, "SD");
	}

	@Test
	public void canComputeIntegerMomentInt()
	{
		canComputeMoment("Single", new int[] { 42 }, new IntegerArrayMoment());

		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[] d = new int[1000];

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
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[][] d = new int[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new int[] { rand.nextInt(MAX_INT) };
		canComputeArrayMoment("Single", d, new IntegerArrayMoment());

		final int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);
		canComputeArrayMoment("Uniform", d, new IntegerArrayMoment());
	}

	@Test
	public void canCombineIntegerArrayMomentInt()
	{
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final int[][] d = new int[50][];

		final int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniformInt(rand, n);

		final IntegerArrayMoment r1 = new IntegerArrayMoment();
		final int size = 6;
		final IntegerArrayMoment[] r2 = new IntegerArrayMoment[size];
		for (int i = 0; i < size; i++)
			r2[i] = new IntegerArrayMoment();
		for (int i = 0; i < d.length; i++)
		{
			r1.add(d[i]);
			r2[i % size].add(d[i]);
		}

		final double[] em1 = r1.getFirstMoment();
		final double[] em2 = r1.getSecondMoment();
		final double[] ev = r1.getVariance();
		final double[] esd = r1.getStandardDeviation();

		for (int i = 1; i < size; i++)
			r2[0].add(r2[i]);

		final double[] om1 = r2[0].getFirstMoment();
		final double[] om2 = r2[0].getSecondMoment();
		final double[] ov = r2[0].getVariance();
		final double[] osd = r2[0].getStandardDeviation();

		// No delta as integer math should be exact
		Assertions.assertArrayEquals(em1, om1, "Mean");
		Assertions.assertArrayEquals(em2, om2, "2nd Moment");
		Assertions.assertArrayEquals(ev, ov, "Variance");
		Assertions.assertArrayEquals(esd, osd, "SD");
	}

	@Test
	public void canTestIfValidIntegerData()
	{
		// 2^16^2 * 2^31-1 ~ 2^63 : This is OK
		Assertions.assertTrue(IntegerArrayMoment.isValid(IntegerType.UNSIGNED_16, Integer.MAX_VALUE));

		// (2^31-1)^2 ~ 2^62 : We should be able to 2 of these but not 3
		Assertions.assertTrue(IntegerArrayMoment.isValid(IntegerType.SIGNED_32, 1));
		Assertions.assertTrue(IntegerArrayMoment.isValid(IntegerType.SIGNED_32, 2));
		Assertions.assertFalse(IntegerArrayMoment.isValid(IntegerType.SIGNED_32, 3));

		// 2^32^2 == 2^64 : We cannot do this as
		Assertions.assertFalse(IntegerArrayMoment.isValid(IntegerType.UNSIGNED_32, 1));
	}

	private void canComputeMoment(String title, double[] d, ArrayMoment r2)
	{
		final Statistics m1 = new Statistics();
		m1.add(d);
		final SecondMoment m2 = new SecondMoment();
		m2.incrementAll(d);
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		ExtraAssertions.assertEqualsRelative(m1.getMean(), r2.getFirstMoment()[0], DELTA, "% Mean", title);
		ExtraAssertions.assertEqualsRelative(m2.getResult(), r2.getSecondMoment()[0], DELTA, "% 2nd Moment", title);
		ExtraAssertions.assertEqualsRelative(m1.getVariance(), r2.getVariance()[0], DELTA, "% Variance", title);
		ExtraAssertions.assertEqualsRelative(m1.getStandardDeviation(), r2.getStandardDeviation()[0], DELTA, "% SD",
				title);
	}

	private void canComputeMoment(String title, float[] d, ArrayMoment r2)
	{
		final Statistics m1 = new Statistics();
		m1.add(d);
		final SecondMoment m2 = new SecondMoment();
		m2.incrementAll(toDouble(d));
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		ExtraAssertions.assertEqualsRelative(m1.getMean(), r2.getFirstMoment()[0], DELTA, "% Mean", title);
		ExtraAssertions.assertEqualsRelative(m2.getResult(), r2.getSecondMoment()[0], DELTA, "% 2nd Moment", title);
		ExtraAssertions.assertEqualsRelative(m1.getVariance(), r2.getVariance()[0], DELTA, "% Variance", title);
		ExtraAssertions.assertEqualsRelative(m1.getStandardDeviation(), r2.getStandardDeviation()[0], DELTA, "% SD",
				title);
	}

	private static double[] toDouble(float[] in)
	{
		final double[] d = new double[in.length];
		for (int i = 0; i < d.length; i++)
			d[i] = in[i];
		return d;
	}

	private void canComputeMoment(String title, int[] d, ArrayMoment r2)
	{
		final Statistics m1 = new Statistics();
		m1.add(d);
		final SecondMoment m2 = new SecondMoment();
		m2.incrementAll(toDouble(d));
		for (int i = 0; i < d.length; i++)
			r2.add(new int[] { d[i] });
		ExtraAssertions.assertEqualsRelative(m1.getMean(), r2.getFirstMoment()[0], DELTA, "% Mean", title);
		ExtraAssertions.assertEqualsRelative(m2.getResult(), r2.getSecondMoment()[0], DELTA, "% 2nd Moment", title);
		ExtraAssertions.assertEqualsRelative(m1.getVariance(), r2.getVariance()[0], DELTA, "% Variance", title);
		ExtraAssertions.assertEqualsRelative(m1.getStandardDeviation(), r2.getStandardDeviation()[0], DELTA, "% SD",
				title);
	}

	private static double[] toDouble(int[] in)
	{
		final double[] d = new double[in.length];
		for (int i = 0; i < d.length; i++)
			d[i] = in[i];
		return d;
	}

	private static double[] uniformDouble(RandomGenerator rand, int n)
	{
		final double[] d = new double[n];
		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();
		return d;
	}

	private int[] uniformInt(RandomGenerator rand, int n)
	{
		final int[] d = new int[n];
		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextInt(MAX_INT);
		return d;
	}

	private void canComputeArrayMoment(String title, double[][] d, ArrayMoment r2)
	{
		for (int i = 0; i < d.length; i++)
			r2.add(d[i]);
		final double[] om1 = r2.getFirstMoment();
		final double[] om2 = r2.getSecondMoment();
		final double[] ov = r2.getVariance();
		final double[] osd = r2.getStandardDeviation();

		for (int n = d[0].length; n-- > 0;)
		{
			final Statistics m1 = new Statistics();
			final SecondMoment m2 = new SecondMoment();
			for (int i = 0; i < d.length; i++)
			{
				m1.add(d[i][n]);
				m2.increment(d[i][n]);
			}
			ExtraAssertions.assertEqualsRelative(m1.getMean(), om1[n], DELTA, "% Mean", title);
			ExtraAssertions.assertEqualsRelative(m2.getResult(), om2[n], DELTA, "% 2nd Moment", title);
			ExtraAssertions.assertEqualsRelative(m1.getVariance(), ov[n], DELTA, "% Variance", title);
			ExtraAssertions.assertEqualsRelative(m1.getStandardDeviation(), osd[n], DELTA, "% SD", title);
		}
	}

	private void canComputeArrayMoment(String title, int[][] d, ArrayMoment r2)
	{
		for (int i = 0; i < d.length; i++)
			r2.add(d[i]);
		final double[] om1 = r2.getFirstMoment();
		final double[] om2 = r2.getSecondMoment();
		final double[] ov = r2.getVariance();
		final double[] osd = r2.getStandardDeviation();

		for (int n = d[0].length; n-- > 0;)
		{
			final Statistics m1 = new Statistics();
			final SecondMoment m2 = new SecondMoment();
			for (int i = 0; i < d.length; i++)
			{
				m1.add(d[i][n]);
				m2.increment(d[i][n]);
			}
			ExtraAssertions.assertEqualsRelative(m1.getMean(), om1[n], DELTA, "% Mean", title);
			ExtraAssertions.assertEqualsRelative(m2.getResult(), om2[n], DELTA, "% 2nd Moment", title);
			ExtraAssertions.assertEqualsRelative(m1.getVariance(), ov[n], DELTA, "% Variance", title);
			ExtraAssertions.assertEqualsRelative(m1.getStandardDeviation(), osd[n], DELTA, "% SD", title);
		}
	}

	@Test
	public void canComputeMomentForLargeSeries()
	{
		ExtraAssumptions.assume(TestComplexity.MEDIUM);
		
		final RandomGenerator rand = TestSettings.getRandomGenerator();

		final SimpleArrayMoment m1 = new SimpleArrayMoment();
		final SecondMoment m2 = new SecondMoment();
		final RollingArrayMoment r2 = new RollingArrayMoment();

		// Test if the standard Statistics object is good enough for
		// computing the mean and variance of sCMOS data from 60,000 frames. It seems it is.
		for (int i = 600000; i-- > 0;)
		{
			final double d = 100.345 + rand.nextGaussian() * Math.PI;
			m1.add(d);
			m2.increment(d);
			r2.add(d);
		}
		TestLog.info("Mean %s vs %s, SD %s vs %s\n", Double.toString(m1.getFirstMoment()[0]),
				Double.toString(r2.getFirstMoment()[0]), Double.toString(m1.getStandardDeviation()[0]),
				Double.toString(r2.getStandardDeviation()[0]));
		ExtraAssertions.assertEqualsRelative(m1.getFirstMoment()[0], r2.getFirstMoment()[0], DELTA, "Mean");
		Assertions.assertEquals(m2.getResult(), r2.getSecondMoment()[0], "2nd Moment");
	}
}
