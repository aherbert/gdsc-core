package gdsc.core.utils;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.junit.Assert;
import org.junit.Test;

public class RollingMomentTest
{
	@Test
	public void canComputeMomentDouble()
	{
		canComputeMoment("Single", new double[] { Math.PI });

		RandomGenerator rand = new Well19937c();
		double[] d = new double[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();
		canComputeMoment("Uniform", d);

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextGaussian();
		canComputeMoment("Gaussian", d);

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d);
	}

	private void canComputeMoment(String title, double[] d)
	{
		Statistics m1 = new Statistics();
		m1.add(d);
		SecondMoment m2 = new SecondMoment();
		m2.incrementAll(d);
		RollingMoment r2 = new RollingMoment();
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		assertEquals(title + " Mean", m1.getMean(), r2.getFirstMoment()[0], 1e-6);
		assertEquals(title + " 2nd Moment", m2.getResult(), r2.getSecondMoment()[0], 0);
		assertEquals(title + " Variance", m1.getVariance(), r2.getVariance()[0], 1e-6);
	}

	@Test
	public void canComputeMomentFloat()
	{
		canComputeMoment("Single", new float[] { (float) Math.PI });

		RandomGenerator rand = new Well19937c();
		float[] d = new float[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextFloat();
		canComputeMoment("Uniform", d);

		for (int i = 0; i < d.length; i++)
			d[i] = (float) rand.nextGaussian();
		canComputeMoment("Gaussian", d);

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d);
	}

	private void canComputeMoment(String title, float[] d)
	{
		Statistics m1 = new Statistics();
		m1.add(d);
		SecondMoment m2 = new SecondMoment();
		m2.incrementAll(toDouble(d));
		RollingMoment r2 = new RollingMoment();
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		assertEquals(title + " Mean", m1.getMean(), r2.getFirstMoment()[0], 1e-6);
		assertEquals(title + " 2nd Moment", m2.getResult(), r2.getSecondMoment()[0], 0);
		assertEquals(title + " Variance", m1.getVariance(), r2.getVariance()[0], 1e-6);
	}

	private double[] toDouble(float[] in)
	{
		double[] d = new double[in.length];
		for (int i = 0; i < d.length; i++)
			d[i] = in[i];
		return d;
	}

	@Test
	public void canComputeMomentInt()
	{
		canComputeMoment("Single", new int[] { 42 });

		RandomGenerator rand = new Well19937c();
		int[] d = new int[1000];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextInt();
		canComputeMoment("Uniform", d);

		for (int i = 0; i < d.length; i++)
			d[i] = i;
		canComputeMoment("Series", d);
	}

	private void canComputeMoment(String title, int[] d)
	{
		Statistics m1 = new Statistics();
		m1.add(d);
		SecondMoment m2 = new SecondMoment();
		m2.incrementAll(toDouble(d));
		RollingMoment r2 = new RollingMoment();
		for (int i = 0; i < d.length; i++)
			r2.add(new double[] { d[i] });
		assertEquals(title + " Mean", m1.getMean(), r2.getFirstMoment()[0], 1e-6);
		assertEquals(title + " 2nd Moment", m2.getResult(), r2.getSecondMoment()[0], 0);
		assertEquals(title + " Variance", m1.getVariance(), r2.getVariance()[0], 1e-6);
	}

	private double[] toDouble(int[] in)
	{
		double[] d = new double[in.length];
		for (int i = 0; i < d.length; i++)
			d[i] = in[i];
		return d;
	}

	@Test
	public void canComputeArrayMomentDouble()
	{
		RandomGenerator rand = new Well19937c();
		double[][] d = new double[3][];

		for (int i = d.length; i-- > 0;)
			d[i] = new double[] { rand.nextDouble() };
		canComputeArrayMoment("Single", d);

		int n = 1000;
		for (int i = d.length; i-- > 0;)
			d[i] = uniform(rand, n);
		canComputeArrayMoment("Uniform", d);
	}

	private double[] uniform(RandomGenerator rand, int n)
	{
		double[] d = new double[n];
		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();
		return d;
	}

	private void canComputeArrayMoment(String title, double[][] d)
	{
		RollingMoment r2 = new RollingMoment();
		for (int i = 0; i < d.length; i++)
			r2.add(d[i]);
		double[] om1 = r2.getFirstMoment();
		double[] om2 = r2.getSecondMoment();
		double[] ov = r2.getVariance();

		for (int n = d[0].length; n-- > 0;)
		{
			Statistics m1 = new Statistics();
			SecondMoment m2 = new SecondMoment();
			for (int i = 0; i < d.length; i++)
			{
				m1.add(d[i][n]);
				m2.increment(d[i][n]);
			}
			assertEquals(title + " Mean", m1.getMean(), om1[n], 1e-6);
			assertEquals(title + " 2nd Moment", m2.getResult(), om2[n], 0);
			assertEquals(title + " Variance", m1.getVariance(), ov[n], 1e-6);
		}
	}

	private void assertEquals(String msg, double e, double o, double delta)
	{
		Assert.assertEquals(msg, e, o, Math.abs(e * delta));
	}
}
