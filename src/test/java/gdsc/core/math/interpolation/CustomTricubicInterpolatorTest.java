package gdsc.core.math.interpolation;

import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.Maths;
import gdsc.core.utils.SimpleArrayUtils;

public class CustomTricubicInterpolatorTest
{
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
			
			Assert.assertTrue(f1.isUniform);
		}
	}

	@Test(expected = NoDataException.class)
	public void constructWithXArrayOfLength1Throws()
	{
		int x = 1, y = 2, z = 2;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = new double[x][y][z];
		new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
	}

	@Test(expected = NoDataException.class)
	public void constructWithYArrayOfLength1Throws()
	{
		int x = 2, y = 1, z = 2;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = new double[x][y][z];
		new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
	}

	@Test(expected = NoDataException.class)
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
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		double[][][] fval = new double[x][y][z];
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval).isUniform);
		double[] bad = xval.clone();
		bad[1] *= 1.001;
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform);
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform);
		Assert.assertFalse(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform);
		double[] good = xval.clone();
		// The tolerance is relative but we habve steps of size 1 so use as an absolute
		good[1] += CustomTricubicInterpolatingFunction.UNIFORM_TOLERANCE / 2;
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(good, yval, zval, fval).isUniform);
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, good, zval, fval).isUniform);
		Assert.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, good, fval).isUniform);
	}
	
	@Test
	public void canInterpolate()
	{
		// Test verses the original
		int x = 4, y = 4, z = 4;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		RandomGenerator r = new Well19937c(30051977);
		double[] test = SimpleArrayUtils.newArray(9, 1, 0.2);
		for (int i = 0; i < 3; i++)
		{
			double[][][] fval = createData(x, y, z, (i == 0) ? null : r);
			CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
					fval);
			TricubicInterpolatingFunction f2 = new org.apache.commons.math3.analysis.interpolation.TricubicInterpolator()
					.interpolate(xval, yval, zval, fval);
			for (double zz : test)
			{
				CubicSplinePosition sz = f1.getZSplinePosition(zz);
				for (double yy : test)
				{
					CubicSplinePosition sy = f1.getYSplinePosition(yy);

					for (double xx : test)
					{
						double o = f1.value(xx, yy, zz);
						double e = f2.value(xx, yy, zz);
						Assert.assertEquals(e, o, Math.abs(e * 1e-8));
						CubicSplinePosition sx = f1.getXSplinePosition(xx);
						double o2 = f1.value(sx, sy, sz);
						Assert.assertEquals(o, o2, 0);
					}
				}
			}
		}
	}

	@Test
	public void canInterpolateUsingPrecomputedTable()
	{
		int x = 4, y = 4, z = 4;
		double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		RandomGenerator r = new Well19937c(30051977);
		double[][][] fval = createData(x, y, z, null);
		CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			double[] table = CustomTricubicFunction.computePowerTable(xx, yy, zz);

			for (int zi = 1; zi < 3; zi++)
				for (int yi = 1; yi < 3; yi++)
					for (int xi = 1; xi < 3; xi++)
					{
						double o = f1.value(xi + xx, yi + yy, zi + zz);
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

}
