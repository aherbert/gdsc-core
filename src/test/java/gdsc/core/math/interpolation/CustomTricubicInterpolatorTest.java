package gdsc.core.math.interpolation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import gdsc.core.data.DoubleArrayTrivalueProvider;
import gdsc.core.data.DoubleArrayValueProvider;
import gdsc.core.data.procedures.StandardTrivalueProcedure;
import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;
import gdsc.core.utils.DoubleEquality;
import gdsc.core.utils.Maths;
import gdsc.core.utils.SimpleArrayUtils;
import gdsc.core.utils.Statistics;

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

			Assert.assertTrue(f1.isUniform());
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
		Assert.assertTrue(f1.isUniform());
		double[] bad = xval.clone();
		bad[1] *= 1.001;
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform());
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform());
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform());
		double[] good = xval.clone();
		// The tolerance is relative but we have steps of size 1 so use as an absolute
		good[1] += CustomTricubicInterpolatingFunction.UNIFORM_TOLERANCE / 2;
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(good, yval, zval, fval).isUniform());
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, good, zval, fval).isUniform());
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, good, fval).isUniform());

		// Check scale. This can be used to map an interpolation point x to the 
		// range 0-1 for power tables 
		double[] scale = f1.getScale();
		Assert.assertEquals(xscale, scale[0], 0);
		Assert.assertEquals(yscale, scale[1], 0);
		Assert.assertEquals(zscale, scale[2], 0);
	}

	@Test
	public void canDetectIfInteger()
	{
		int x = 3, y = 3, z = 3;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 4.2345, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 17.5, 1.0);
		double[][][] fval = new double[x][y][z];
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		Assert.assertTrue(f1.isUniform());
		Assert.assertTrue(f1.isInteger());
		double[] bad = SimpleArrayUtils.newArray(x, 0, 1.0 + CustomTricubicInterpolatingFunction.INTEGER_TOLERANCE);
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform());
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform());
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform());
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isInteger());
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isInteger());
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isInteger());

		// Check scale. This can be used to map an interpolation point x to the 
		// range 0-1 for power tables 
		double[] scale = f1.getScale();
		Assert.assertEquals(1, scale[0], 0);
		Assert.assertEquals(1, scale[1], 0);
		Assert.assertEquals(1, scale[2], 0);
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

	@Test
	public void canInterpolateSingleNode()
	{
		canInterpolateSingleNode(0.5, 1, 2);
	}

	@Test
	public void canInterpolateSingleNodeWithNoScale()
	{
		canInterpolateSingleNode(1, 1, 1);
	}

	private void canInterpolateSingleNode(double xscale, double yscale, double zscale)
	{
		int x = 4, y = 4, z = 4;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		// If the scales are uniform then the version with the scale is identical to the
		// version without as it just packs and then unpacks the gradients.
		boolean noScale = xscale == 1 && yscale == 1 && zscale == 1;
		if (!noScale)
		{
			// Create non-linear scale
			for (int i = 0, n = 2; i < 4; i++, n *= 2)
			{
				xval[i] *= n;
				yval[i] *= n;
				zval[i] *= n;
			}
		}
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

		double[] e = f1.getSplineNode(1, 1, 1).getA();

		double[] o;
		if (noScale)
			o = CustomTricubicInterpolator.create(new DoubleArrayTrivalueProvider(fval)).getA();
		else
			o = CustomTricubicInterpolator
					.create(new DoubleArrayValueProvider(xval), new DoubleArrayValueProvider(yval),
							new DoubleArrayValueProvider(zval), new DoubleArrayTrivalueProvider(fval))
					.getA();

		Assert.assertArrayEquals(e, o, 0);
	}

	@Test
	public void canInterpolateIndividualNode()
	{
		canInterpolateIndividualNode(0.5, 1, 2);
	}

	@Test
	public void canInterpolateIndividualNodeWithNoScale()
	{
		canInterpolateIndividualNode(1, 1, 1);
	}

	private void canInterpolateIndividualNode(double xscale, double yscale, double zscale)
	{
		int x = 6, y = 6, z = 6;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		// If the scales are uniform then the version with the scale is identical to the
		// version without as it just packs and then unpacks the gradients.
		boolean noScale = xscale == 1 && yscale == 1 && zscale == 1;
		if (!noScale)
		{
			// Create non-linear scale
			for (int i = 0, n = 2; i < x; i++, n *= 2)
			{
				xval[i] *= n;
				yval[i] *= n;
				zval[i] *= n;
			}
		}
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

		check(f1, xval, yval, zval, fval, noScale, 0, 0, 0);
		check(f1, xval, yval, zval, fval, noScale, 0, 1, 0);
		check(f1, xval, yval, zval, fval, noScale, 1, 1, 1);
		check(f1, xval, yval, zval, fval, noScale, 2, 1, 1);
		check(f1, xval, yval, zval, fval, noScale, 2, 3, 2);
		check(f1, xval, yval, zval, fval, noScale, 4, 4, 4);
	}

	private void check(CustomTricubicInterpolatingFunction f1, double[] xval, double[] yval, double[] zval,
			double[][][] fval, boolean noScale, int i, int j, int k)
	{
		double[] e = f1.getSplineNode(i, j, k).getA();

		double[] o;
		if (noScale)
			o = CustomTricubicInterpolator.create(new DoubleArrayTrivalueProvider(fval), i, j, k).getA();
		else
			o = CustomTricubicInterpolator
					.create(new DoubleArrayValueProvider(xval), new DoubleArrayValueProvider(yval),
							new DoubleArrayValueProvider(zval), new DoubleArrayTrivalueProvider(fval), i, j, k)
					.getA();

		Assert.assertArrayEquals(e, o, 0);
	}

	double[][][] createData(int x, int y, int z, RandomGenerator r)
	{
		// Create a 2D Gaussian
		double s = 1.0;
		double cx = x / 2.0;
		double cy = y / 2.0;
		double cz = z / 2.0;
		if (r != null)
		{
			s += r.nextDouble() - 0.5;
			cx += r.nextDouble() - 0.5;
			cy += r.nextDouble() - 0.5;
			cz += r.nextDouble() - 0.5;
		}
		else
		{
			// Prevent symmetry which breaks the evaluation of gradients
			cx += 0.01;
			cy += 0.01;
			cz += 0.01;
		}
		return createData(x, y, z, cx, cy, cz, s);

		//      double[][][] fval = new double[x][y][z];
		//		double[] otherx = new double[x];
		//		for (int zz = 0; zz < z; zz++)
		//		{
		//			double s2 = 2 * s * s;
		//			for (int xx = 0; xx < x; xx++)
		//				otherx[xx] = Maths.pow2(xx - cx) / s2;
		//			for (int yy = 0; yy < y; yy++)
		//			{
		//				double othery = Maths.pow2(yy - cy) / s2;
		//				for (int xx = 0; xx < x; xx++)
		//				{
		//					fval[xx][yy][zz] = Math.exp(otherx[xx] + othery);
		//				}
		//			}
		//			// Move Gaussian
		//			s += 0.1;
		//			cx += 0.1;
		//			cy -= 0.05;
		//		}
		//		return fval;
	}

	double amplitude;

	double[][][] createData(int x, int y, int z, double cx, double cy, double cz, double s)
	{
		double[][][] fval = new double[x][y][z];
		// Create a 2D Gaussian with astigmatism
		double[] otherx = new double[x];
		double zDepth = cz / 2;
		double gamma = 1;

		// Compute the maximum amplitude 
		double sx = s * (1.0 + Maths.pow2((gamma) / zDepth) * 0.5);
		double sy = s * (1.0 + Maths.pow2((-gamma) / zDepth) * 0.5);
		amplitude = 1.0 / (2 * Math.PI * sx * sy);

		//ImageStack stack = new ImageStack(x, y);
		for (int zz = 0; zz < z; zz++)
		{
			//float[] pixels = new float[x * y];
			//int i=0;

			// Astigmatism based on cz.
			// Width will be 1.5 at zDepth.
			double dz = cz - zz;
			sx = s * (1.0 + Maths.pow2((dz + gamma) / zDepth) * 0.5);
			sy = s * (1.0 + Maths.pow2((dz - gamma) / zDepth) * 0.5);

			//System.out.printf("%d = %f,%f\n", zz, sx, sy);

			double norm = 1.0 / (2 * Math.PI * sx * sy);

			double sx2 = 2 * sx * sx;
			double sy2 = 2 * sy * sy;
			for (int xx = 0; xx < x; xx++)
				otherx[xx] = -Maths.pow2(xx - cx) / sx2;
			for (int yy = 0; yy < y; yy++)
			{
				double othery = Maths.pow2(yy - cy) / sy2;
				for (int xx = 0; xx < x; xx++)
				{
					double value = norm * FastMath.exp(otherx[xx] - othery);
					fval[xx][yy][zz] = value;
					//pixels[i++] = (float) value;
				}
			}
			//stack.addSlice(null, pixels);
		}
		//ImagePlus imp = Utils.display("Test", stack);
		//for (int i = 9; i-- > 0;)
		//	imp.getCanvas().zoomIn(0, 0);
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

		// Gradients on the node points are evaluated using different polynomials 
		// as the function switches to a new cubic polynomial.
		// First-order gradients should be OK across nodes.
		// Second-order gradients will be incorrect.

		double[] testx = SimpleArrayUtils.newArray(9, xval[1], (xval[2] - xval[1]) / 5);
		double[] testy = SimpleArrayUtils.newArray(9, yval[1], (yval[2] - yval[1]) / 5);
		double[] testz = SimpleArrayUtils.newArray(9, zval[1], (zval[2] - zval[1]) / 5);
		double[] df_daH = new double[3];
		double[] df_daL = new double[3];
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		double[] d2f_da2A = new double[3];
		double[] d2f_da2B = new double[3];
		DoubleEquality eq = new DoubleEquality(1e-6, 1e-3);
		for (int i = 0; i < 3; i++)
		{
			double[][][] fval = createData(x, y, z, (i == 0) ? null : r);
			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);
			for (double zz : testz)
			{
				boolean onNode = Arrays.binarySearch(zval, zz) >= 0;
				IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);

				for (double yy : testy)
				{
					onNode = onNode || Arrays.binarySearch(yval, yy) >= 0;
					IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

					for (double xx : testx)
					{
						onNode = onNode || Arrays.binarySearch(xval, xx) >= 0;

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
							//double df_da = (high - e) / h;
							double df_da = (high - low) / (2 * h);
							boolean signOK = (df_da * df_daA[j]) >= 0;
							boolean ok = eq.almostEqualRelativeOrAbsolute(df_da, df_daA[j]);
							Assert.assertTrue(df_da + " sign != " + df_daA[j], signOK);
							//System.out.printf("[%.2f,%.2f,%.2f] %f == [%d] %f  ok=%b\n", xx, yy, zz, df_da2, j,
							//		df_daA[j], ok);
							//if (!ok)
							//{
							//	System.out.printf("[%.1f,%.1f,%.1f] %f == [%d] %f?\n", xx, yy, zz, df_da2, j, df_daA[j]);
							//}
							Assert.assertTrue(df_da + " != " + df_daA[j], ok);

							double d2f_da2 = (df_daH[j] - df_daL[j]) / (2 * h);
							if (!onNode)
							{
								Assert.assertTrue(d2f_da2 + " sign != " + d2f_da2A[j], (d2f_da2 * d2f_da2A[j]) >= 0);
								//boolean ok = eq.almostEqualRelativeOrAbsolute(d2f_da2, d2f_da2A[j]);
								//System.out.printf("%d [%.2f,%.2f,%.2f] %f == [%d] %f  ok=%b\n", j, xx, yy, zz, d2f_da2,
								//		j, d2f_da2A[j], ok);
								//if (!ok)
								//{
								//System.out.printf("%d [%.1f,%.1f,%.1f] %f == [%d] %f?\n", j, xx, yy, zz, d2f_da2, j,
								//		d2f_da2A[j]);
								//}
								Assert.assertTrue(d2f_da2 + " != " + d2f_da2A[j],
										eq.almostEqualRelativeOrAbsolute(d2f_da2, d2f_da2A[j]));
							}
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
			double[] table2 = CustomTricubicFunction.scalePowerTable(table, 2);
			double[] table3 = CustomTricubicFunction.scalePowerTable(table, 3);
			double[] table6 = CustomTricubicFunction.scalePowerTable(table, 6);

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

						o = f1.value(xi, yi, zi, table, table2, table3, df_daB);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 1e-10);

						o = f1.value(a[0], a[1], a[2], df_daB, d2f_da2A);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 1e-10);

						o = f1.value(xi, yi, zi, table, df_daB, d2f_da2B);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						Assert.assertArrayEquals(df_daA, df_daB, 1e-10);
						Assert.assertArrayEquals(d2f_da2A, d2f_da2B, 1e-10);

						o = f1.value(xi, yi, zi, table, table2, table3, table6, df_daB, d2f_da2B);
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
					nodes[i] = f1.getSplineNodeReference(zi, yi, xi);
					fnodes[i] = nodes[i].toSinglePrecision();
				}

		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			double[] table = CustomTricubicFunction.computePowerTable(xx, yy, zz);
			float[] ftable = CustomTricubicFunction.computeFloatPowerTable(xx, yy, zz);
			float[] ftable2 = CustomTricubicFunction.scalePowerTable(ftable, 2);
			float[] ftable3 = CustomTricubicFunction.scalePowerTable(ftable, 3);
			float[] ftable6 = CustomTricubicFunction.scalePowerTable(ftable, 6);

			for (int ii = 0; ii < nodes.length; ii++)
			{
				CustomTricubicFunction n1 = nodes[ii];
				CustomTricubicFunction n2 = fnodes[ii];

				// Just check relative to the double-table version
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

				o = n2.value(ftable, ftable2, ftable3, df_daB);
				assertEquals(e, o, valueTolerance);
				for (int j = 0; j < 3; j++)
					assertEquals(df_daA[j], df_daB[j], gradientTolerance);

				o2 = n2.value(ftable, ftable2, ftable3, ftable6, df_daB2, d2f_da2B);
				// Should be the same as the first-order gradient 
				Assert.assertEquals(o, o2, 0);
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

	@Test
	public void canComputeNoInterpolation()
	{
		int x = 4, y = 4, z = 4;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		double[] d2f_da2A = new double[3];
		double[] d2f_da2B = new double[3];
		double e, o;
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

		// Extract node for testing
		CustomTricubicFunction n1 = f1.getSplineNodeReference(1, 1, 1);
		CustomTricubicFunction n2 = n1.toSinglePrecision();

		double[] table = CustomTricubicFunction.computePowerTable(0, 0, 0);
		float[] ftable = CustomTricubicFunction.computeFloatPowerTable(0, 0, 0);

		// Check no interpolation is correct
		e = n1.value(table);
		o = n1.value000();
		Assert.assertEquals(e, o, 0);

		e = n1.value(table, df_daA);
		o = n1.value000(df_daB);
		Assert.assertEquals(e, o, 0);
		Assert.assertArrayEquals(df_daA, df_daB, 0);

		e = n1.value(table, df_daA, d2f_da2A);
		o = n1.value000(df_daB, d2f_da2B);
		Assert.assertEquals(e, o, 0);
		Assert.assertArrayEquals(df_daA, df_daB, 0);
		Assert.assertArrayEquals(d2f_da2A, d2f_da2B, 0);

		// Check no interpolation is correct
		e = n2.value(ftable);
		o = n2.value000();
		Assert.assertEquals(e, o, 0);

		e = n2.value(ftable, df_daA);
		o = n2.value000(df_daB);
		Assert.assertEquals(e, o, 0);
		Assert.assertArrayEquals(df_daA, df_daB, 0);

		e = n2.value(ftable, df_daA, d2f_da2A);
		o = n2.value000(df_daB, d2f_da2B);
		Assert.assertEquals(e, o, 0);
		Assert.assertArrayEquals(df_daA, df_daB, 0);
		Assert.assertArrayEquals(d2f_da2A, d2f_da2B, 0);
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
		Assume.assumeTrue(false);

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
					nodes[i] = f1.getSplineNodeReference(zi, yi, xi);
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

			tables[i] = CustomTricubicFunction.computePowerTable(xx, yy, zz);
			ftables[i] = CustomTricubicFunction.computeFloatPowerTable(xx, yy, zz);
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

	@Test
	public void canComputeWithExecutorService()
	{
		canComputeWithExecutorService(1, 0.5, 2.0);
	}

	@Test
	public void canComputeIntegerGridWithExecutorService()
	{
		canComputeWithExecutorService(1, 1, 1);
	}

	private void canComputeWithExecutorService(double xscale, double yscale, double zscale)
	{
		int x = 6, y = 5, z = 4;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[][][] fval = createData(x, y, z, null);

		CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);
		ExecutorService es = Executors.newFixedThreadPool(4);
		interpolator.setExecutorService(es);
		interpolator.setTaskSize(5);
		CustomTricubicInterpolatingFunction f2 = interpolator.interpolate(xval, yval, zval, fval);
		es.shutdown();

		// Compare all nodes
		for (int i = 0; i < f1.getMaxXSplinePosition(); i++)
			for (int j = 0; j < f1.getMaxYSplinePosition(); j++)
				for (int k = 0; k < f1.getMaxZSplinePosition(); k++)
				{
					DoubleCustomTricubicFunction n1 = (DoubleCustomTricubicFunction) f1.getSplineNodeReference(i, j, k);
					DoubleCustomTricubicFunction n2 = (DoubleCustomTricubicFunction) f2.getSplineNodeReference(i, j, k);
					Assert.assertArrayEquals(n1.getA(), n2.getA(), 0);
				}
	}

	@Test
	public void canSampleInterpolatedFunctionWithN1()
	{
		canSampleInterpolatedFunction(1);
	}

	@Test
	public void canSampleInterpolatedFunctionWithN2()
	{
		canSampleInterpolatedFunction(2);
	}

	@Test
	public void canSampleInterpolatedFunctionWithN3()
	{
		canSampleInterpolatedFunction(3);
	}

	private void canSampleInterpolatedFunction(int n)
	{
		int x = 6, y = 5, z = 4;
		// Make it easy to have exact matching
		double xscale = 2.0, yscale = 2.0, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[][][] fval = createData(x, y, z, null);

		CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

		StandardTrivalueProcedure p = new StandardTrivalueProcedure();
		f1.sample(n, p);

		Assert.assertArrayEquals(SimpleArrayUtils.newArray((x - 1) * n + 1, 0, xscale / n), p.x, 1e-6);
		Assert.assertArrayEquals(SimpleArrayUtils.newArray((y - 1) * n + 1, 0, yscale / n), p.y, 1e-6);
		Assert.assertArrayEquals(SimpleArrayUtils.newArray((z - 1) * n + 1, 0, zscale / n), p.z, 1e-6);

		for (int i = 0; i < p.x.length; i++)
			for (int j = 0; j < p.y.length; j++)
				for (int k = 0; k < p.z.length; k++)
				{
					// Test original function interpolated value against the sample
					assertEquals(f1.value(p.x[i], p.y[j], p.z[k]), p.value[i][j][k], 1e-8);
				}
	}

	@Test
	public void canDynamicallySampleFunctionWithN2()
	{
		canDynamicallySampleFunction(2);
	}

	@Test
	public void canDynamicallySampleFunctionWithN3()
	{
		canDynamicallySampleFunction(3);
	}

	private void canDynamicallySampleFunction(int n)
	{
		// This assumes that the sample method of the CustomTricubicInterpolatingFunction works!

		int x = 6, y = 5, z = 4;
		// No scale for this test
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = createData(x, y, z, null);

		CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(fval);
		CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(new DoubleArrayValueProvider(xval),
				new DoubleArrayValueProvider(yval), new DoubleArrayValueProvider(zval), f);

		StandardTrivalueProcedure p = new StandardTrivalueProcedure();
		f1.sample(n, p);

		StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
		CustomTricubicInterpolator.sample(f, n, p2);

		Assert.assertArrayEquals(p.x, p2.x, 1e-10);
		Assert.assertArrayEquals(p.y, p2.y, 1e-10);
		Assert.assertArrayEquals(p.z, p2.z, 1e-10);

		for (int i = 0; i < p.x.length; i++)
			for (int j = 0; j < p.y.length; j++)
			{
				Assert.assertArrayEquals(p.value[i][j], p2.value[i][j], 0);
			}
	}

	@Test
	public void canExternaliseDoubleFunction() throws IOException, ClassNotFoundException
	{
		canExternaliseFunction(false);
	}

	@Test
	public void canExternaliseFloatFunction() throws IOException, ClassNotFoundException
	{
		canExternaliseFunction(true);
	}

	private void canExternaliseFunction(boolean singlePrecision) throws IOException, ClassNotFoundException
	{
		int x = 6, y = 5, z = 4;
		double xscale = 1, yscale = 0.5, zscale = 2.0;
		double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		double[][][] fval = createData(x, y, z, null);

		CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

		if (singlePrecision)
			f1.toSinglePrecision();

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		f1.write(b);

		byte[] bytes = b.toByteArray();
		//System.out.printf("Single precision = %b, size = %d, memory estimate = %d\n", singlePrecision, bytes.length,
		//		CustomTricubicInterpolatingFunction.estimateSize(new int[] { x, y, z })
		//				.getMemoryFootprint(singlePrecision));
		CustomTricubicInterpolatingFunction f2 = CustomTricubicInterpolatingFunction
				.read(new ByteArrayInputStream(bytes));

		int n = 2;
		StandardTrivalueProcedure p1 = new StandardTrivalueProcedure();
		f1.sample(n, p1);
		StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
		f2.sample(n, p2);

		Assert.assertArrayEquals(p1.x, p2.x, 0);
		Assert.assertArrayEquals(p1.y, p2.y, 0);
		Assert.assertArrayEquals(p1.z, p2.z, 0);

		for (int i = 0; i < p1.x.length; i++)
			for (int j = 0; j < p1.y.length; j++)
				for (int k = 0; k < p1.z.length; k++)
				{
					Assert.assertEquals(f1.value(p1.x[i], p1.y[j], p1.z[k]), f2.value(p1.x[i], p1.y[j], p1.z[k]), 0);
				}
	}

	@Test
	public void canInterpolateAcrossNodesForValueAndGradient1()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 4, y = 4, z = 4;
		// Difference scales
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		for (int ii = 0; ii < 3; ii++)
		{
			double[][][] fval = createData(x, y, z, (ii == 0) ? null : r);
			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);
			for (int zz = f1.getMaxZSplinePosition(); zz > 0; zz--)
			{
				for (int yy = f1.getMaxYSplinePosition(); yy > 0; yy--)
				{
					for (int xx = f1.getMaxXSplinePosition(); xx > 0; xx--)
					{
						CustomTricubicFunction next = f1.getSplineNodeReference(xx, yy, zz);

						// Test that interpolating at x=1 equals x=0 for the next node
						for (int k = 0; k < 2; k++)
						{
							int zzz = zz - k;
							for (int j = 0; j < 2; j++)
							{
								int yyy = yy - j;
								for (int i = 0; i < 2; i++)
								{
									int xxx = xx - i;
									if (i + j + k == 0)
										continue;

									CustomTricubicFunction previous = f1.getSplineNodeReference(xxx, yyy, zzz);

									double e = next.value(0, 0, 0, df_daA);
									double o = previous.value(i, j, k, df_daB);
									Assert.assertEquals(e, o, Math.abs(e * 1e-8));

									for (int c = 0; c < 3; c++)
									{
										//Assert.assertEquals(df_daA[c], df_daB[c], Math.abs(df_daA[c] * 1e-8));
										Assert.assertTrue(DoubleEquality.almostEqualRelativeOrAbsolute(df_daA[c],
												df_daB[c], 1e-8, 1e-12));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void cannotInterpolateAcrossNodesForGradient2()
	{
		RandomGenerator r = new Well19937c(30051977);
		int x = 4, y = 4, z = 4;
		// Difference scales
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[] df_daA = new double[3];
		double[] df_daB = new double[3];
		double[] d2f_da2A = new double[3];
		double[] d2f_da2B = new double[3];
		for (int ii = 0; ii < 3; ii++)
		{
			Statistics[] value = new Statistics[3];
			for (int i = 0; i < value.length; i++)
				value[i] = new Statistics();

			double[][][] fval = createData(x, y, z, (ii == 0) ? null : r);
			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);
			for (int zz = f1.getMaxZSplinePosition(); zz > 0; zz--)
			{
				for (int yy = f1.getMaxYSplinePosition(); yy > 0; yy--)
				{
					for (int xx = f1.getMaxXSplinePosition(); xx > 0; xx--)
					{
						CustomTricubicFunction next = f1.getSplineNodeReference(xx, yy, zz);

						// Test that interpolating at x=1 equals x=0 for the next node
						for (int k = 0; k < 2; k++)
						{
							int zzz = zz - k;
							for (int j = 0; j < 2; j++)
							{
								int yyy = yy - j;
								for (int i = 0; i < 2; i++)
								{
									int xxx = xx - i;
									if (i + j + k == 0)
										continue;

									CustomTricubicFunction previous = f1.getSplineNodeReference(xxx, yyy, zzz);

									next.value(0, 0, 0, df_daA, d2f_da2A);
									previous.value(i, j, k, df_daB, d2f_da2B);

									for (int c = 0; c < 3; c++)
									{
										// The function may change direction so check the 2nd derivative magnitude is similar
										//System.out.printf("[%d] %f vs %f\n", c, d2f_da2A[c], d2f_da2B[c], DoubleEquality.relativeError(d2f_da2A[c], d2f_da2B[c]));
										d2f_da2A[c] = Math.abs(d2f_da2A[c]);
										d2f_da2B[c] = Math.abs(d2f_da2B[c]);
										value[c].add(DoubleEquality.relativeError(d2f_da2A[c], d2f_da2B[c]));
									}
								}
							}
						}
					}
				}
			}

			boolean same = true;
			for (int c = 0; c < 3; c++)
			{
				// The second gradients are so different that this should fail
				same = same && value[c].getMean() < 0.01;
				//System.out.printf("d2yda2[%d] Error = %f +/- %f\n", c, value[c].getMean(),
				//		value[c].getStandardDeviation());
			}
			Assert.assertFalse(same);
		}
	}

	@Test
	public void searchSplineImprovesFunctionValue()
	{
		// Skip this as it is for testing the binary search works
		Assume.assumeTrue(false);

		RandomGenerator r = new Well19937c(30051977);
		// Bigger depth of field to capture astigmatism centre
		int x = 10, y = 10, z = 10;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		for (int ii = 0; ii < 3; ii++)
		{
			double cx = (x - 1) / 2.0 + r.nextDouble() / 2;
			double cy = (y - 1) / 2.0 + r.nextDouble() / 2;
			double cz = (z - 1) / 2.0 + r.nextDouble() / 2;
			double[][][] fval = createData(x, y, z, cx, cy, cz, 2);

			CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
			CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

			// Check the search approaches the actual function value
			double[] last = null;
			for (int i = 0; i <= 10; i++)
			{
				double[] optimum = f1.search(true, i, 0, 0);
				//double d = Maths.distance(cx, cy, cz, optimum[0], optimum[1], optimum[2]);
				//System.out.printf("[%d] %f,%f,%f %d = %s : dist = %f : error = %f\n", ii, cx, cy, cz, i,
				//		Arrays.toString(optimum), d, DoubleEquality.relativeError(amplitude, optimum[3]));

				// Skip 0 to 1 as it moves from an exact node value to interpolation
				// which may use a different node depending on the gradient
				if (i > 1)
				{
					double d = Maths.distance(last[0], last[1], last[2], optimum[0], optimum[1], optimum[2]);
					System.out.printf("[%d] %f,%f,%f %d = %s : dist = %f : change = %g\n", ii, cx, cy, cz, i,
							Arrays.toString(optimum), d, DoubleEquality.relativeError(last[3], optimum[3]));
					Assert.assertTrue(optimum[3] >= last[3]);
				}
				last = optimum;
			}
		}
	}

	@Test
	public void canFindOptimum()
	{
		RandomGenerator r = new Well19937c(30051977);
		// Bigger depth of field to capture astigmatism centre
		int x = 10, y = 10, z = 10;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		for (int ii = 0; ii < 10; ii++)
		{
			double cx = (x - 1) / 2.0 + r.nextDouble() / 2;
			double cy = (y - 1) / 2.0 + r.nextDouble() / 2;
			double cz = (z - 1) / 2.0 + r.nextDouble() / 2;
			double[][][] fval = createData(x, y, z, cx, cy, cz, 2);

			// Test max and min search
			boolean maximum = (ii % 2 == 1);
			if (!maximum)
			{
				// Invert
				for (int xx = 0; xx < x; xx++)
					for (int yy = 0; yy < y; yy++)
						for (int zz = 0; zz < z; zz++)
							fval[xx][yy][zz] = -fval[xx][yy][zz];
				amplitude = -amplitude;
			}

			CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
			CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

			double[] last = f1.search(maximum, 10, 1e-6, 0);

			// Since the cubic function is not the same as the input we cannot be too precise here
			Assert.assertEquals(cx, last[0], 1e-1);
			Assert.assertEquals(cy, last[1], 1e-1);
			Assert.assertEquals(cz, last[2], 1e-1);
			Assert.assertEquals(amplitude, last[3], Math.abs(amplitude) * 1e-2);
		}
	}
}
