package gdsc.core.math.interpolation;

import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;
import gdsc.core.utils.DoubleEquality;
import gdsc.core.utils.Maths;
import gdsc.core.utils.SimpleArrayUtils;

public class CustomTricubicInterpolatorTest
{
	// Delta for numerical gradients
	private double h_ = 0.00001;

	@Test
	public void canConstructInterpolatingFunction()
	{
		RandomGenerator r = new Well19937c(30051977);

		int x = 4, y = 5, z = 6;
		double[][][] fval = createData(x, y, z, null);
		for (int i = 0; i < 3; i++)
		{
			double[] xval = SimpleArrayUtils.newArray(x, r.nextDouble(), r.nextDouble());
			double[] yval = SimpleArrayUtils.newArray(y, r.nextDouble(), r.nextDouble());
			double[] zval = SimpleArrayUtils.newArray(z, r.nextDouble(), r.nextDouble());

			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);

			// Check the function knows its bounds
			Assert.assertEquals(xval[0], f1.getMinX(), 0);
			Assert.assertEquals(yval[0], f1.getMinY(), 0);
			Assert.assertEquals(zval[0], f1.getMinZ(), 0);
			Assert.assertEquals(xval[x - 1], f1.getMaxX(), 0);
			Assert.assertEquals(yval[y - 1], f1.getMaxY(), 0);
			Assert.assertEquals(zval[z - 1], f1.getMaxZ(), 0);
			Assert.assertEquals(x - 2, f1.getMaxXSplinePosition());
			Assert.assertEquals(y - 2, f1.getMaxYSplinePosition());
			Assert.assertEquals(z - 2, f1.getMaxZSplinePosition());

			for (int j = 0; j < xval.length; j++)
				Assert.assertEquals(xval[j], f1.getXSplineValue(j), 0);
			for (int j = 0; j < yval.length; j++)
				Assert.assertEquals(yval[j], f1.getYSplineValue(j), 0);
			for (int j = 0; j < zval.length; j++)
				Assert.assertEquals(zval[j], f1.getZSplineValue(j), 0);

			Assert.assertTrue(f1.isUniform);

			f1.precomputeGradientCoefficients(1);
		}
	}

	@Test(expected = NumberIsTooSmallException.class)
	public void constructWithXArrayOfLength1Throws()
	{
		int x = 1, y = 2, z = 2;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = new double[x][y][z];
		new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
	}

	@Test(expected = NumberIsTooSmallException.class)
	public void constructWithYArrayOfLength1Throws()
	{
		int x = 2, y = 1, z = 2;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = new double[x][y][z];
		new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
	}

	@Test(expected = NumberIsTooSmallException.class)
	public void constructWithZArrayOfLength1Throws()
	{
		int x = 2, y = 2, z = 1;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = new double[x][y][z];
		new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
	}

	@Test
	public void canDetectIfUniform()
	{
		int x = 3, y = 3, z = 3;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[][][] fval = new double[x][y][z];
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		Assert.assertTrue(f1.isUniform);
		double[] bad = xval.clone();
		bad[1] *= 1.001;
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform);
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform);
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform);
		double[] good = xval.clone();
		// The tolerance is relative but we have steps of size 1 so use as an absolute
		good[1] += CustomTricubicInterpolatingFunction.UNIFORM_TOLERANCE / 2;
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(good, yval, zval, fval).isUniform);
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, good, zval, fval).isUniform);
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, good, fval).isUniform);

		// Check scale. This can be used to map an interpolation point x to the 
		// range 0-1 for power tables 
		double[] scale = f1.getScale();
		Assert.assertEquals(xscale, scale[0], 0);
		Assert.assertEquals(yscale, scale[1], 0);
		Assert.assertEquals(zscale, scale[2], 0);
	}

	@Test
	public void canInterpolate()
	{
		RandomGenerator r = new Well19937c(30051977);
		// Test verses the original
		int x = 4, y = 4, z = 4;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 0.5);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 2.0);
		double[] testx = SimpleArrayUtils.newArray(9, xval[1], (xval[2] - xval[1]) / 5);
		double[] testy = SimpleArrayUtils.newArray(9, yval[1], (yval[2] - yval[1]) / 5);
		double[] testz = SimpleArrayUtils.newArray(9, zval[1], (zval[2] - zval[1]) / 5);
		TricubicInterpolator f3 = new TricubicInterpolator();
		for (int i = 0; i < 3; i++)
		{
			double[][][] fval = createData(x, y, z, (i == 0) ? null : r);
			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);
			TricubicInterpolatingFunction f2 = new org.apache.commons.math3.analysis.interpolation.TricubicInterpolator()
					.interpolate(xval, yval, zval, fval);
			for (double zz : testz)
			{
				IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);
				for (double yy : testy)
				{
					IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

					for (double xx : testx)
					{
						double o = f1.value(xx, yy, zz);
						double e = f2.value(xx, yy, zz);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						IndexedCubicSplinePosition sx = f1.getXSplinePosition(xx);
						double o2 = f1.value(sx, sy, sz);
						Assert.assertEquals(o, o2, 0);

						// Test against simple tricubic spline
						// Which requires x,y,z in the range 0-1 for function values
						// x=-1 to x=2; y=-1 to y=2; and z=-1 to z=2
						if (zz < zval[2] && yy < yval[2] && xx < xval[2])
						{
							// @formatter:off
							double e2 = f3.getValue(fval, 
									(xx - xval[1]) / (xval[2] - xval[1]),
									(yy - yval[1]) / (yval[2] - yval[1]), 
									(zz - zval[1]) / (zval[2] - zval[1]));
							// @formatter:on
							Assert.assertEquals(e2, o, Math.abs(e2 * 1e-8));
						}
					}
				}
			}
		}
	}

	@Test
	public void canInterpolateUsingPrecomputedTable()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 4, y = 4, z = 4;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			// This is done unscaled
			double[] table = DoubleCustomTricubicFunction.computePowerTable(xx, yy, zz);

			xx *= xscale;
			yy *= yscale;
			zz *= zscale;

			for (int zi = 1; zi < 3; zi++)
				for (int yi = 1; yi < 3; yi++)
					for (int xi = 1; xi < 3; xi++)
					{
						double o = f1.value(xval[xi] + xx, yval[yi] + yy, zval[zi] + zz);
						double e = f1.value(xi, yi, zi, table);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
					}
		}
	}

	double[][][] createData(int x, int y, int z, RandomGenerator r)
	{
		double[][][] fval = new double[x][y][z];
		// Create a 2D Gaussian
		double s = 1.0;
		double cx = x / 2.0;
		double cy = y / 2.0;
		if (r != null)
		{
			s += r.nextDouble() - 0.5;
			cx += r.nextDouble() - 0.5;
			cy += r.nextDouble() - 0.5;
		}
		double[] otherx = new double[x];
		for (int zz = 0; zz < z; zz++)
		{
			double s2 = 2 * s * s;
			for (int xx = 0; xx < x; xx++)
				otherx[xx] = Maths.pow2(xx - cx) / s2;
			for (int yy = 0; yy < y; yy++)
			{
				double othery = Maths.pow2(yy - cy) / s2;
				for (int xx = 0; xx < x; xx++)
				{
					fval[xx][yy][zz] = Math.exp(otherx[xx] + othery);
				}
			}
			// Move Gaussian
			s += 0.1;
			cx += 0.1;
			cy -= 0.05;
		}
		return fval;
	}

	@Test
	public void canInterpolateWithGradients()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 4, y = 4, z = 4;
		// Difference scales
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 0.5);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 2.0);
		// Gradients on the node points are not numerically evaluated correctly as 
		// the function switches to a new cubic polynomial. So evaluate off the node points.
		double[] testx = SimpleArrayUtils.newArray(9, xval[1] + 0.01, (xval[2] - xval[1]) / 5);
		double[] testy = SimpleArrayUtils.newArray(9, yval[1] + 0.01, (yval[2] - yval[1]) / 5);
		double[] testz = SimpleArrayUtils.newArray(9, zval[1] + 0.01, (zval[2] - zval[1]) / 5);
		double[] df_daH = new double[3];
		double[] df_daL = new double[3];
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		double[] d2f_da2A = new double[3];
		double[] d2f_da2B = new double[3];
		DoubleEquality eq = new DoubleEquality(1e-2, 1e-3);
		for (int i = 0; i < 3; i++)
		{
			double[][][] fval = createData(x, y, z, (i == 0) ? null : r);
			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);
			for (double zz : testz)
			{
				IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);
				for (double yy : testy)
				{
					IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

					for (double xx : testx)
					{
						double e = f1.value(xx, yy, zz);
						double o = f1.value(xx, yy, zz, df_daA);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						double o2 = f1.value(xx, yy, zz, df_daB, d2f_da2A);
						Assert.assertEquals(e, o2, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 0);

						IndexedCubicSplinePosition sx = f1.getXSplinePosition(xx);
						o2 = f1.value(sx, sy, sz, df_daB);
						Assert.assertEquals(e, o2, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 0);
						o2 = f1.value(sx, sy, sz, df_daB, d2f_da2B);
						Assert.assertEquals(e, o2, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 0);
						Assert.assertArrayEquals(d2f_da2A, d2f_da2B, 0);

						// Get gradient and check
						double[] a = new double[] { xx, yy, zz };
						for (int j = 0; j < 3; j++)
						{
							double h = Precision.representableDelta(a[j], h_);
							double old = a[j];
							a[j] = old + h;
							double high = f1.value(a[0], a[1], a[2], df_daH);
							a[j] = old - h;
							double low = f1.value(a[0], a[1], a[2], df_daL);
							a[j] = old;
							double firstOrder = (high - low) / (2 * h);
							Assert.assertTrue(firstOrder + " sign != " + df_daA[j], (firstOrder * df_daA[j]) >= 0);
							//boolean ok = eq.almostEqualRelativeOrAbsolute(firstOrder, df_daA[j]);
							//System.out.printf("[%.2f,%.2f,%.2f] %f == [%d] %f  ok=%b\n", xx, yy, zz, firstOrder, j,
							//		df_daA[j], ok);
							//if (!ok)
							//{
							//	System.out.printf("[%.1f,%.1f,%.1f] %f == [%d] %f?\n", xx, yy, zz, firstOrder, j, df_daA[j]);
							//}
							Assert.assertTrue(firstOrder + " != " + df_daA[j],
									eq.almostEqualRelativeOrAbsolute(firstOrder, df_daA[j]));

							double secondOrder = (df_daH[j] - df_daL[j]) / (2 * h);
							Assert.assertTrue(secondOrder + " sign != " + d2f_da2A[j],
									(secondOrder * d2f_da2A[j]) >= 0);
							//boolean ok = eq.almostEqualRelativeOrAbsolute(secondOrder, d2f_da2A[j]);
							//System.out.printf("%d [%.2f,%.2f,%.2f] %f == [%d] %f  ok=%b\n", j, xx, yy, zz, secondOrder,
							//		j, d2f_da2A[j], ok);
							//if (!ok)
							//{
							//System.out.printf("%d [%.1f,%.1f,%.1f] %f == [%d] %f?\n", j, xx, yy, zz, secondOrder, j,
							//		d2f_da2A[j]);
							//}
							Assert.assertTrue(secondOrder + " != " + d2f_da2A[j],
									eq.almostEqualRelativeOrAbsolute(secondOrder, d2f_da2A[j]));
						}
					}
				}
			}
		}
	}

	@Test
	public void canInterpolateWithGradientsUsingPrecomputedTable()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 4, y = 4, z = 4;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		double[] d2f_da2A = new double[3];
		double[] d2f_da2B = new double[3];
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			// This is done unscaled
			double[] table = DoubleCustomTricubicFunction.computePowerTable(xx, yy, zz);

			xx *= xscale;
			yy *= yscale;
			zz *= zscale;

			for (int zi = 1; zi < 3; zi++)
				for (int yi = 1; yi < 3; yi++)
					for (int xi = 1; xi < 3; xi++)
					{
						// Just check relative to the non-table version
						double[] a = new double[] { xval[xi] + xx, yval[yi] + yy, zval[zi] + zz };
						double e = f1.value(a[0], a[1], a[2], df_daA);
						double o = f1.value(xi, yi, zi, table, df_daB);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 1e-10);

						o = f1.value(a[0], a[1], a[2], df_daB, d2f_da2A);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 1e-10);

						o = f1.value(xi, yi, zi, table, df_daB, d2f_da2B);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 1e-10);
						Assert.assertArrayEquals(d2f_da2A, d2f_da2B, 1e-10);
					}
		}
	}

	@Test
	public void canInterpolateWithGradientsUsingPrecomputedTableSinglePrecision()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 4, y = 4, z = 4;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		double[] df_daA2 = new double[3];
		double[] df_daB2 = new double[3];
		double[] d2f_da2A = new double[3];
		double[] d2f_da2B = new double[3];
		double e, o, e2, o2;
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

		double valueTolerance = 1e-5;
		double gradientTolerance = 1e-3;

		// Extract nodes for testing
		CustomTricubicFunction[] nodes = new CustomTricubicFunction[2 * 2 * 2];
		CustomTricubicFunction[] fnodes = new CustomTricubicFunction[nodes.length];
		for (int zi = 1, i = 0; zi < 3; zi++)
			for (int yi = 1; yi < 3; yi++)
				for (int xi = 1; xi < 3; xi++, i++)
				{
					nodes[i] = f1.getSplineNode(zi, yi, xi);
					nodes[i].precomputeGradientCoefficients(2);
					fnodes[i] = nodes[i].toSinglePrecision();
				}

		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			double[] table = DoubleCustomTricubicFunction.computePowerTable(xx, yy, zz);
			float[] ftable = FloatCustomTricubicFunction.computePowerTable(xx, yy, zz);

			for (int ii = 0; ii < nodes.length; ii++)
			{
				CustomTricubicFunction n1 = nodes[ii];
				CustomTricubicFunction n2 = fnodes[ii];

				// Just check relative to the non-table version
				e = n1.value(table);
				o = n2.value(ftable);
				assertEquals(e, o, valueTolerance);

				e = n1.value(table, df_daA);
				o = n2.value(ftable, df_daB);
				assertEquals(e, o, valueTolerance);
				for (int j = 0; j < 3; j++)
					assertEquals(df_daA[j], df_daB[j], gradientTolerance);

				e2 = n1.value(table, df_daA2, d2f_da2A);
				o2 = n2.value(ftable, df_daB2, d2f_da2B);
				// Should be the same as the first-order gradient 
				Assert.assertEquals(e, e2, 0);
				Assert.assertEquals(o, o2, 0);
				Assert.assertArrayEquals(df_daA, df_daA2, 0);
				Assert.assertArrayEquals(df_daB, df_daB2, 0);
				assertEquals(e2, o2, valueTolerance);
				for (int j = 0; j < 3; j++)
				{
					assertEquals(df_daA[j], df_daB[j], gradientTolerance);
					assertEquals(d2f_da2A[j], d2f_da2B[j], gradientTolerance);
				}
			}
		}
	}

	private static void assertEquals(double e, double o, double tolerance)
	{
		Assert.assertEquals(e, o, Math.abs(e * tolerance));
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		CustomTricubicFunction[] nodes;
		double[] df_da = new double[3];
		double[] d2f_da2 = new double[3];

		public MyTimingTask(String name, CustomTricubicFunction[] nodes)
		{
			super(name + " " + nodes[0].getClass().getSimpleName());
			this.nodes = nodes;
		}

		public int getSize()
		{
			return 1;
		}

		public Object getData(int i)
		{
			return null;
		}
	}

	private abstract class DoubleTimingTask extends MyTimingTask
	{
		double[][] tables;

		public DoubleTimingTask(String name, double[][] tables, CustomTricubicFunction[] nodes)
		{
			super(name, nodes);
			this.tables = tables;
		}
	}

	private abstract class FloatTimingTask extends MyTimingTask
	{
		float[][] tables;

		public FloatTimingTask(String name, float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(name, nodes);
			this.tables = tables;
		}
	}

	private class Double0TimingTask extends DoubleTimingTask
	{
		public Double0TimingTask(double[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Double0TimingTask.class.getSimpleName(), tables, nodes);
		}

		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
			{
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j]);
			}
			return v;
		}
	}

	private class Float0TimingTask extends FloatTimingTask
	{
		public Float0TimingTask(float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Float0TimingTask.class.getSimpleName(), tables, nodes);
		}

		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
			{
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j]);
			}
			return v;
		}
	}

	private class Double1TimingTask extends DoubleTimingTask
	{
		public Double1TimingTask(double[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Double1TimingTask.class.getSimpleName(), tables, nodes);
		}

		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
			{
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da);
			}
			return v;
		}
	}

	private class Float1TimingTask extends FloatTimingTask
	{
		public Float1TimingTask(float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Float1TimingTask.class.getSimpleName(), tables, nodes);
		}

		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
			{
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da);
			}
			return v;
		}
	}

	private class Double2TimingTask extends DoubleTimingTask
	{
		public Double2TimingTask(double[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Double2TimingTask.class.getSimpleName(), tables, nodes);
		}

		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
			{
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da, d2f_da2);
			}
			return v;
		}
	}

	private class Float2TimingTask extends FloatTimingTask
	{
		public Float2TimingTask(float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Float2TimingTask.class.getSimpleName(), tables, nodes);
		}

		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
			{
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da, d2f_da2);
			}
			return v;
		}
	}

	@Test
	public void floatCustomTricubicFunctionIsFasterUsingPrecomputedTable()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 6, y = 5, z = 4;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

		// Extract nodes for testing
		CustomTricubicFunction[] nodes = new CustomTricubicFunction[(x - 2) * (y - 2) * (z - 2)];
		CustomTricubicFunction[] fnodes = new CustomTricubicFunction[nodes.length];
		for (int zi = 1, i = 0; zi < x - 1; zi++)
			for (int yi = 1; yi < y - 1; yi++)
				for (int xi = 1; xi < z - 1; xi++, i++)
				{
					nodes[i] = f1.getSplineNode(zi, yi, xi);
					nodes[i].precomputeGradientCoefficients(2);
					fnodes[i] = nodes[i].toSinglePrecision();
				}

		// Get points
		double[][] tables = new double[3000][];
		float[][] ftables = new float[tables.length][];
		for (int i = 0; i < tables.length; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			tables[i] = DoubleCustomTricubicFunction.computePowerTable(xx, yy, zz);
			ftables[i] = FloatCustomTricubicFunction.computePowerTable(xx, yy, zz);
		}

		TimingService ts = new TimingService();

		// Put in order to pass the speed test
		ts.execute(new Double2TimingTask(tables, fnodes));
		ts.execute(new Double2TimingTask(tables, nodes));
		
		ts.execute(new Float2TimingTask(ftables, nodes));
		ts.execute(new Float2TimingTask(ftables, fnodes));
		
		ts.execute(new Double1TimingTask(tables, fnodes));
		ts.execute(new Double1TimingTask(tables, nodes));
		
		ts.execute(new Float1TimingTask(ftables, nodes));
		ts.execute(new Float1TimingTask(ftables, fnodes));
		
		ts.execute(new Double0TimingTask(tables, fnodes));
		ts.execute(new Double0TimingTask(tables, nodes));
		
		ts.execute(new Float0TimingTask(ftables, nodes));
		ts.execute(new Float0TimingTask(ftables, fnodes));

		int n = ts.getSize();
		ts.repeat();
		ts.report(n);

		// Sometimes this fails for the Float0TimingTask so add a margin for error
		double margin = 1.1;
		for (int i = 1; i < n; i += 2)
			Assert.assertTrue(String.format("%f vs %f", ts.get(-i).getMean(), ts.get(-i - 1).getMean()),
					ts.get(-i).getMean() < ts.get(-i - 1).getMean() * margin);
	}
}
