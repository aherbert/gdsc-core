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
package uk.ac.sussex.gdsc.core.math.interpolation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.core.data.DoubleArrayTrivalueProvider;
import uk.ac.sussex.gdsc.core.data.DoubleArrayValueProvider;
import uk.ac.sussex.gdsc.core.data.procedures.StandardTrivalueProcedure;
import uk.ac.sussex.gdsc.core.utils.DoubleEquality;
import uk.ac.sussex.gdsc.core.utils.Maths;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.test.BaseTimingTask;
import uk.ac.sussex.gdsc.test.LogLevel;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.TimingResult;
import uk.ac.sussex.gdsc.test.TimingService;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class CustomTricubicInterpolatorTest
{
	// Delta for numerical gradients
	private final double h_ = 0.00001;

	@Test
	public void canConstructInterpolatingFunction()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();

		final int x = 4, y = 5, z = 6;
		final double[][][] fval = createData(x, y, z, null);
		for (int i = 0; i < 3; i++)
		{
			final double[] xval = SimpleArrayUtils.newArray(x, r.nextDouble(), r.nextDouble());
			final double[] yval = SimpleArrayUtils.newArray(y, r.nextDouble(), r.nextDouble());
			final double[] zval = SimpleArrayUtils.newArray(z, r.nextDouble(), r.nextDouble());

			final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval,
					zval, fval);

			// Check the function knows its bounds
			Assertions.assertEquals(xval[0], f1.getMinX());
			Assertions.assertEquals(yval[0], f1.getMinY());
			Assertions.assertEquals(zval[0], f1.getMinZ());
			Assertions.assertEquals(xval[x - 1], f1.getMaxX());
			Assertions.assertEquals(yval[y - 1], f1.getMaxY());
			Assertions.assertEquals(zval[z - 1], f1.getMaxZ());
			Assertions.assertEquals(x - 2, f1.getMaxXSplinePosition());
			Assertions.assertEquals(y - 2, f1.getMaxYSplinePosition());
			Assertions.assertEquals(z - 2, f1.getMaxZSplinePosition());

			for (int j = 0; j < xval.length; j++)
				Assertions.assertEquals(xval[j], f1.getXSplineValue(j));
			for (int j = 0; j < yval.length; j++)
				Assertions.assertEquals(yval[j], f1.getYSplineValue(j));
			for (int j = 0; j < zval.length; j++)
				Assertions.assertEquals(zval[j], f1.getZSplineValue(j));

			Assertions.assertTrue(f1.isUniform());
		}
	}

	@Test
	public void constructWithXArrayOfLength1Throws()
	{
		final int x = 1, y = 2, z = 2;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		final double[][][] fval = new double[x][y][z];
		Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
			new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		});
	}

	@Test
	public void constructWithYArrayOfLength1Throws()
	{
		final int x = 2, y = 1, z = 2;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		final double[][][] fval = new double[x][y][z];
		Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
			new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		});
	}

	@Test
	public void constructWithZArrayOfLength1Throws()
	{
		final int x = 2, y = 2, z = 1;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		final double[][][] fval = new double[x][y][z];
		Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
			new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
		});
	}

	@Test
	public void canDetectIfUniform()
	{
		final int x = 3, y = 3, z = 3;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[][][] fval = new double[x][y][z];
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);
		Assertions.assertTrue(f1.isUniform());
		final double[] bad = xval.clone();
		bad[1] *= 1.001;
		Assertions.assertFalse(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform());
		Assertions.assertFalse(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform());
		Assertions.assertFalse(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform());
		final double[] good = xval.clone();
		// The tolerance is relative but we have steps of size 1 so use as an absolute
		good[1] += CustomTricubicInterpolatingFunction.UNIFORM_TOLERANCE / 2;
		Assertions.assertTrue(new CustomTricubicInterpolator().interpolate(good, yval, zval, fval).isUniform());
		Assertions.assertTrue(new CustomTricubicInterpolator().interpolate(xval, good, zval, fval).isUniform());
		Assertions.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, good, fval).isUniform());

		// Check scale. This can be used to map an interpolation point x to the
		// range 0-1 for power tables
		final double[] scale = f1.getScale();
		Assertions.assertEquals(xscale, scale[0]);
		Assertions.assertEquals(yscale, scale[1]);
		Assertions.assertEquals(zscale, scale[2]);
	}

	@Test
	public void canDetectIfInteger()
	{
		final int x = 3, y = 3, z = 3;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 4.2345, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 17.5, 1.0);
		final double[][][] fval = new double[x][y][z];
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);
		Assertions.assertTrue(f1.isUniform());
		Assertions.assertTrue(f1.isInteger());
		final double[] bad = SimpleArrayUtils.newArray(x, 0,
				1.0 + CustomTricubicInterpolatingFunction.INTEGER_TOLERANCE);
		Assertions.assertTrue(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform());
		Assertions.assertTrue(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform());
		Assertions.assertTrue(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform());
		Assertions.assertFalse(new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isInteger());
		Assertions.assertFalse(new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isInteger());
		Assertions.assertFalse(new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isInteger());

		// Check scale. This can be used to map an interpolation point x to the
		// range 0-1 for power tables
		final double[] scale = f1.getScale();
		Assertions.assertEquals(1, scale[0]);
		Assertions.assertEquals(1, scale[1]);
		Assertions.assertEquals(1, scale[2]);
	}

	@Test
	public void canInterpolate()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 0.5);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 2.0);
		final double[] testx = SimpleArrayUtils.newArray(6, xval[1], (xval[2] - xval[1]) / 5);
		final double[] testy = SimpleArrayUtils.newArray(6, yval[1], (yval[2] - yval[1]) / 5);
		final double[] testz = SimpleArrayUtils.newArray(6, zval[1], (zval[2] - zval[1]) / 5);
		final TricubicInterpolator f3 = new TricubicInterpolator();
		final BicubicInterpolator bi = new BicubicInterpolator();
		double[] face, face2;
		double o, e;
		for (int i = 0; i < 3; i++)
		{
			final double[][][] fval = createData(x, y, z, (i == 0) ? null : r);

			final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval,
					zval, fval);
			// No longer possible to test verses the original as we handle edges differently
			//TricubicInterpolatingFunction f2 = new org.apache.commons.math3.analysis.interpolation.TricubicInterpolator()
			//		.interpolate(xval, yval, zval, fval);
			for (final double zz : testz)
			{
				final IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);
				for (final double yy : testy)
				{
					final IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

					for (final double xx : testx)
					{
						o = f1.value(xx, yy, zz);
						//double e = f2.value(xx, yy, zz);
						//ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
						final IndexedCubicSplinePosition sx = f1.getXSplinePosition(xx);
						final double o2 = f1.value(sx, sy, sz);
						Assertions.assertEquals(o, o2);

						// Test against simple tricubic spline
						// which requires x,y,z in the range 0-1 for function values
						// x=-1 to x=2; y=-1 to y=2; and z=-1 to z=2
						// @formatter:off
							final double e2 = f3.getValue(fval,
									(xx - xval[1]) / (xval[2] - xval[1]),
									(yy - yval[1]) / (yval[2] - yval[1]),
									(zz - zval[1]) / (zval[2] - zval[1]));
							// @formatter:on
						ExtraAssertions.assertEqualsRelative(e2, o, 1e-8);
					}
				}
			}

			// Each face of the cube should interpolate as a Bicubic function
			face = extractXYFace(fval, 0);
			face2 = extractXYFace(fval, z - 1);
			for (final double xx : testx)
				for (final double yy : testy)
				{
					o = f1.value(xx, yy, 0);
					e = bi.getValue(face, (xx - xval[1]) / (xval[2] - xval[1]), (yy - yval[1]) / (yval[2] - yval[1]));
					ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
					o = f1.value(xx, yy, zval[z - 1]);
					e = bi.getValue(face2, (xx - xval[1]) / (xval[2] - xval[1]), (yy - yval[1]) / (yval[2] - yval[1]));
					ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
				}

			face = extractXZFace(fval, 0);
			face2 = extractXZFace(fval, y - 1);
			for (final double xx : testx)
				for (final double zz : testz)
				{
					o = f1.value(xx, 0, zz);
					e = bi.getValue(face, (xx - xval[1]) / (xval[2] - xval[1]), (zz - zval[1]) / (zval[2] - zval[1]));
					ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
					o = f1.value(xx, yval[y - 1], zz);
					e = bi.getValue(face2, (xx - xval[1]) / (xval[2] - xval[1]), (zz - zval[1]) / (zval[2] - zval[1]));
					ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
				}

			face = extractYZFace(fval, 0);
			face2 = extractYZFace(fval, z - 1);
			for (final double yy : testy)
				for (final double zz : testz)
				{
					o = f1.value(0, yy, zz);
					e = bi.getValue(face, (yy - yval[1]) / (yval[2] - yval[1]), (zz - zval[1]) / (zval[2] - zval[1]));
					ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
					o = f1.value(xval[x - 1], yy, zz);
					e = bi.getValue(face2, (yy - yval[1]) / (yval[2] - yval[1]), (zz - zval[1]) / (zval[2] - zval[1]));
					ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
				}
		}
	}

	private static double[] extractXYFace(double[][][] fval, int o)
	{
		final int maxx = fval.length;
		final int maxy = fval[0].length;

		final double[] f = new double[maxx * maxy];
		int i = 0;
		for (int y = 0; y < maxy; y++)
			for (int x = 0; x < maxx; x++)
				f[i++] = fval[x][y][o];
		return f;
	}

	private static double[] extractXZFace(double[][][] fval, int o)
	{
		final int maxx = fval.length;
		final int maxz = fval[0][0].length;

		final double[] f = new double[maxx * maxz];
		int i = 0;
		for (int z = 0; z < maxz; z++)
			for (int x = 0; x < maxx; x++)
				f[i++] = fval[x][o][z];
		return f;
	}

	private static double[] extractYZFace(double[][][] fval, int o)
	{
		final int maxy = fval[0].length;
		final int maxz = fval[0][0].length;

		final double[] f = new double[maxy * maxz];
		int i = 0;
		for (int z = 0; z < maxz; z++)
			for (int y = 0; y < maxy; y++)
				f[i++] = fval[o][y][z];
		return f;
	}

	@Test
	public void canInterpolateUsingPrecomputedTable()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);
		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			// This is done unscaled
			final double[] table = CustomTricubicFunction.computePowerTable(xx, yy, zz);

			xx *= xscale;
			yy *= yscale;
			zz *= zscale;

			for (int zi = 1; zi < 3; zi++)
				for (int yi = 1; yi < 3; yi++)
					for (int xi = 1; xi < 3; xi++)
					{
						final double o = f1.value(xval[xi] + xx, yval[yi] + yy, zval[zi] + zz);
						final double e = f1.value(xi, yi, zi, table);
						ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
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
		final int x = 4, y = 4, z = 4;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		// If the scales are uniform then the version with the scale is identical to the
		// version without as it just packs and then unpacks the gradients.
		final boolean noScale = xscale == 1 && yscale == 1 && zscale == 1;
		if (!noScale)
			// Create non-linear scale
			for (int i = 0, n = 2; i < 4; i++, n *= 2)
			{
				xval[i] *= n;
				yval[i] *= n;
				zval[i] *= n;
			}
		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);

		final double[] e = f1.getSplineNode(1, 1, 1).getA();

		double[] o;
		if (noScale)
			o = CustomTricubicInterpolator.create(new DoubleArrayTrivalueProvider(fval)).getA();
		else
			o = CustomTricubicInterpolator
					.create(new DoubleArrayValueProvider(xval), new DoubleArrayValueProvider(yval),
							new DoubleArrayValueProvider(zval), new DoubleArrayTrivalueProvider(fval))
					.getA();

		Assertions.assertArrayEquals(e, o);
	}

	@Test
	public void canInterpolateSingleNodeWithOffset()
	{
		canInterpolateSingleNodeWithOffset(0.5, 1, 2);
	}

	@Test
	public void canInterpolateSingleNodeWithOffsetWithNoScale()
	{
		canInterpolateSingleNodeWithOffset(1, 1, 1);
	}

	private void canInterpolateSingleNodeWithOffset(double xscale, double yscale, double zscale)
	{
		final int x = 6, y = 6, z = 6;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		// If the scales are uniform then the version with the scale is identical to the
		// version without as it just packs and then unpacks the gradients.
		final boolean noScale = xscale == 1 && yscale == 1 && zscale == 1;
		if (!noScale)
			// Create non-linear scale
			for (int i = 0, n = 2; i < x; i++, n *= 2)
			{
				xval[i] *= n;
				yval[i] *= n;
				zval[i] *= n;
			}
		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);

		check(f1, xval, yval, zval, fval, noScale, 0, 0, 0);
		check(f1, xval, yval, zval, fval, noScale, 0, 1, 0);
		check(f1, xval, yval, zval, fval, noScale, 1, 1, 1);
		check(f1, xval, yval, zval, fval, noScale, 2, 1, 1);
		check(f1, xval, yval, zval, fval, noScale, 2, 3, 2);
		check(f1, xval, yval, zval, fval, noScale, 4, 4, 4);
	}

	private static void check(CustomTricubicInterpolatingFunction f1, double[] xval, double[] yval, double[] zval,
			double[][][] fval, boolean noScale, int i, int j, int k)
	{
		final double[] e = f1.getSplineNode(i, j, k).getA();

		double[] o;
		if (noScale)
			o = CustomTricubicInterpolator.create(new DoubleArrayTrivalueProvider(fval), i, j, k).getA();
		else
			o = CustomTricubicInterpolator
					.create(new DoubleArrayValueProvider(xval), new DoubleArrayValueProvider(yval),
							new DoubleArrayValueProvider(zval), new DoubleArrayTrivalueProvider(fval), i, j, k)
					.getA();

		Assertions.assertArrayEquals(e, o);
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
		final double[][][] fval = new double[x][y][z];
		// Create a 2D Gaussian with astigmatism
		final double[] otherx = new double[x];
		final double zDepth = cz / 2;
		final double gamma = 1;

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
			final double dz = cz - zz;
			sx = s * (1.0 + Maths.pow2((dz + gamma) / zDepth) * 0.5);
			sy = s * (1.0 + Maths.pow2((dz - gamma) / zDepth) * 0.5);

			//TestLog.debug("%d = %f,%f\n", zz, sx, sy);

			final double norm = 1.0 / (2 * Math.PI * sx * sy);

			final double sx2 = 2 * sx * sx;
			final double sy2 = 2 * sy * sy;
			for (int xx = 0; xx < x; xx++)
				otherx[xx] = -Maths.pow2(xx - cx) / sx2;
			for (int yy = 0; yy < y; yy++)
			{
				final double othery = Maths.pow2(yy - cy) / sy2;
				for (int xx = 0; xx < x; xx++)
				{
					final double value = norm * FastMath.exp(otherx[xx] - othery);
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
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		// Difference scales
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 0.5);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 2.0);

		// Gradients on the node points are evaluated using different polynomials
		// as the function switches to a new cubic polynomial.
		// First-order gradients should be OK across nodes.
		// Second-order gradients will be incorrect.

		final double[] testx = SimpleArrayUtils.newArray(9, xval[1], (xval[2] - xval[1]) / 5);
		final double[] testy = SimpleArrayUtils.newArray(9, yval[1], (yval[2] - yval[1]) / 5);
		final double[] testz = SimpleArrayUtils.newArray(9, zval[1], (zval[2] - zval[1]) / 5);
		final double[] df_daH = new double[3];
		final double[] df_daL = new double[3];
		final double[] df_daA = new double[3];
		final double[] df_daB = new double[3];
		final double[] d2f_da2A = new double[3];
		final double[] d2f_da2B = new double[3];
		final DoubleEquality eq = new DoubleEquality(1e-6, 1e-3);
		for (int i = 0; i < 3; i++)
		{
			final double[][][] fval = createData(x, y, z, (i == 0) ? null : r);
			final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval,
					zval, fval);
			for (final double zz : testz)
			{
				boolean onNode = Arrays.binarySearch(zval, zz) >= 0;
				final IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);

				for (final double yy : testy)
				{
					onNode = onNode || Arrays.binarySearch(yval, yy) >= 0;
					final IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

					for (final double xx : testx)
					{
						onNode = onNode || Arrays.binarySearch(xval, xx) >= 0;

						final double e = f1.value(xx, yy, zz);
						final double o = f1.value(xx, yy, zz, df_daA);
						ExtraAssertions.assertEqualsRelative(e, o, 1e-8);

						double o2 = f1.value(xx, yy, zz, df_daB, d2f_da2A);
						Assertions.assertEquals(o, o2);
						Assertions.assertArrayEquals(df_daA, df_daB);

						final IndexedCubicSplinePosition sx = f1.getXSplinePosition(xx);
						o2 = f1.value(sx, sy, sz, df_daB);
						Assertions.assertEquals(o, o2);
						Assertions.assertArrayEquals(df_daA, df_daB);

						o2 = f1.value(sx, sy, sz, df_daB, d2f_da2B);
						Assertions.assertEquals(o, o2);
						Assertions.assertArrayEquals(df_daA, df_daB);
						Assertions.assertArrayEquals(d2f_da2A, d2f_da2B);

						// Get gradient and check
						final double[] a = new double[] { xx, yy, zz };
						for (int j = 0; j < 3; j++)
						{
							final double h = Precision.representableDelta(a[j], h_);
							final double old = a[j];
							a[j] = old + h;
							final double high = f1.value(a[0], a[1], a[2], df_daH);
							a[j] = old - h;
							final double low = f1.value(a[0], a[1], a[2], df_daL);
							a[j] = old;
							//double df_da = (high - e) / h;
							final double df_da = (high - low) / (2 * h);
							final boolean signOK = (df_da * df_daA[j]) >= 0;
							final boolean ok = eq.almostEqualRelativeOrAbsolute(df_da, df_daA[j]);
							if (!signOK)
								Assertions.fail(df_da + " sign != " + df_daA[j]);
							//TestLog.debug("[%.2f,%.2f,%.2f] %f == [%d] %f  ok=%b\n", xx, yy, zz, df_da2, j,
							//		df_daA[j], ok);
							//if (!ok)
							//{
							//	TestLog.info("[%.1f,%.1f,%.1f] %f == [%d] %f?\n", xx, yy, zz, df_da2, j, df_daA[j]);
							//}
							if (!ok)
								Assertions.fail(df_da + " != " + df_daA[j]);

							final double d2f_da2 = (df_daH[j] - df_daL[j]) / (2 * h);
							if (!onNode)
							{
								if (!((d2f_da2 * d2f_da2A[j]) >= 0))
									Assertions.fail(d2f_da2 + " sign != " + d2f_da2A[j]);
								//boolean ok = eq.almostEqualRelativeOrAbsolute(d2f_da2, d2f_da2A[j]);
								//TestLog.debug("%d [%.2f,%.2f,%.2f] %f == [%d] %f  ok=%b\n", j, xx, yy, zz, d2f_da2,
								//		j, d2f_da2A[j], ok);
								//if (!ok)
								//{
								//TestLog.debug("%d [%.1f,%.1f,%.1f] %f == [%d] %f?\n", j, xx, yy, zz, d2f_da2, j,
								//		d2f_da2A[j]);
								//}
								ExtraAssertions.assertTrue(eq.almostEqualRelativeOrAbsolute(d2f_da2, d2f_da2A[j]),
										"%s != %s", d2f_da2, d2f_da2A[j]);
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
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[] df_daA = new double[3];
		final double[] df_daB = new double[3];
		final double[] d2f_da2A = new double[3];
		final double[] d2f_da2B = new double[3];
		double e, o, o2;
		double[] e1A, e1B, e2B;

		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);
		for (int i = 0; i < 3; i++)
		{
			double xx = r.nextDouble();
			double yy = r.nextDouble();
			double zz = r.nextDouble();

			// This is done unscaled
			final double[] table = CustomTricubicFunction.computePowerTable(xx, yy, zz);
			final double[] table2 = CustomTricubicFunction.scalePowerTable(table, 2);
			final double[] table3 = CustomTricubicFunction.scalePowerTable(table, 3);
			final double[] table6 = CustomTricubicFunction.scalePowerTable(table, 6);

			xx *= xscale;
			yy *= yscale;
			zz *= zscale;

			for (int zi = 1; zi < 3; zi++)
				for (int yi = 1; yi < 3; yi++)
					for (int xi = 1; xi < 3; xi++)
					{
						// Just check relative to the non-table version
						final double[] a = new double[] { xval[xi] + xx, yval[yi] + yy, zval[zi] + zz };

						e = f1.value(a[0], a[1], a[2]);
						o = f1.value(xi, yi, zi, table);
						ExtraAssertions.assertEqualsRelative(e, o, 1e-8);

						// 1st order gradient

						e = f1.value(a[0], a[1], a[2], df_daA);
						o = f1.value(xi, yi, zi, table, df_daB);
						ExtraAssertions.assertEqualsRelative(e, o, 1e-8);
						ExtraAssertions.assertArrayEqualsRelative(df_daA, df_daB, 1e-8);

						// Store result
						e1A = df_daA.clone();
						e1B = df_daB.clone();

						// Pre-scaled table should be the same
						o2 = f1.value(xi, yi, zi, table, table2, table3, df_daB);

						Assertions.assertEquals(o, o2);
						Assertions.assertArrayEquals(e1B, df_daB);

						// 2nd order gradient

						o2 = f1.value(a[0], a[1], a[2], df_daA, d2f_da2A);
						Assertions.assertEquals(e, o2);
						Assertions.assertArrayEquals(e1A, df_daA);

						o2 = f1.value(xi, yi, zi, table, df_daB, d2f_da2B);
						Assertions.assertEquals(o, o2);
						Assertions.assertArrayEquals(e1B, df_daB);
						ExtraAssertions.assertArrayEqualsRelative(d2f_da2A, d2f_da2B, 1e-8);

						// Store result
						e2B = d2f_da2B.clone();

						// Pre-scaled table should be the same
						o2 = f1.value(xi, yi, zi, table, table2, table3, table6, df_daB, d2f_da2B);
						Assertions.assertEquals(o, o2);
						Assertions.assertArrayEquals(e1B, df_daB);
						Assertions.assertArrayEquals(e2B, d2f_da2B);
					}
		}
	}

	@Test
	public void canInterpolateWithGradientsUsingPrecomputedTableSinglePrecision()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[] df_daA = new double[3];
		final double[] df_daB = new double[3];
		final double[] d2f_da2A = new double[3];
		final double[] d2f_da2B = new double[3];
		double e, o, o2;
		double[] e1B, e2B;
		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);

		final double valueTolerance = 1e-5;
		// The gradients are computed using float and the tolerance is low
		final double gradientTolerance = 5e-3;
		final double gradientTolerance2 = 1e-2;

		// Extract nodes for testing
		final CustomTricubicFunction[] nodes = new CustomTricubicFunction[2 * 2 * 2];
		final CustomTricubicFunction[] fnodes = new CustomTricubicFunction[nodes.length];
		for (int zi = 1, i = 0; zi < 3; zi++)
			for (int yi = 1; yi < 3; yi++)
				for (int xi = 1; xi < 3; xi++, i++)
				{
					nodes[i] = f1.getSplineNodeReference(zi, yi, xi);
					fnodes[i] = nodes[i].toSinglePrecision();
				}

		for (int i = 0; i < 3; i++)
		{
			final double xx = r.nextDouble();
			final double yy = r.nextDouble();
			final double zz = r.nextDouble();

			final double[] table = CustomTricubicFunction.computePowerTable(xx, yy, zz);
			final float[] ftable = CustomTricubicFunction.computeFloatPowerTable(xx, yy, zz);
			final float[] ftable2 = CustomTricubicFunction.scalePowerTable(ftable, 2);
			final float[] ftable3 = CustomTricubicFunction.scalePowerTable(ftable, 3);
			final float[] ftable6 = CustomTricubicFunction.scalePowerTable(ftable, 6);

			for (int ii = 0; ii < nodes.length; ii++)
			{
				final CustomTricubicFunction n1 = nodes[ii];
				final CustomTricubicFunction n2 = fnodes[ii];

				// Just check relative to the double-table version
				e = n1.value(table);
				o = n2.value(ftable);
				ExtraAssertions.assertEqualsRelative(e, o, valueTolerance);

				// 1st order gradient

				n1.value(table, df_daA);
				o2 = n2.value(ftable, df_daB);
				Assertions.assertEquals(o, o2);
				ExtraAssertions.assertArrayEqualsRelative(df_daA, df_daB, gradientTolerance);

				// Store result
				e1B = df_daB.clone();

				// 2nd order gradient

				n1.value(table, df_daA, d2f_da2A);
				o2 = n2.value(ftable, df_daB, d2f_da2B);
				// Should be the same as the first-order gradient (which has already passed)
				Assertions.assertEquals(o, o2);
				Assertions.assertArrayEquals(e1B, df_daB);
				// Check 2nd order gradient
				ExtraAssertions.assertArrayEqualsRelative(d2f_da2A, d2f_da2B, gradientTolerance2);

				// Store result
				e2B = d2f_da2B.clone();

				// Pre-scaled table should be the same
				o2 = n2.value(ftable, ftable2, ftable3, df_daB);
				Assertions.assertEquals(o, o2);
				Assertions.assertArrayEquals(e1B, df_daB);

				o2 = n2.value(ftable, ftable2, ftable3, ftable6, df_daB, d2f_da2B);
				Assertions.assertEquals(o, o2);
				Assertions.assertArrayEquals(e1B, df_daB);
				Assertions.assertArrayEquals(e2B, d2f_da2B);
			}
		}
	}

	@Test
	public void canComputeNoInterpolation()
	{
		final int x = 4, y = 4, z = 4;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[] df_daA = new double[3];
		final double[] df_daB = new double[3];
		final double[] d2f_da2A = new double[3];
		final double[] d2f_da2B = new double[3];
		double e, o;
		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);

		// Extract node for testing
		final CustomTricubicFunction n1 = f1.getSplineNodeReference(1, 1, 1);
		final CustomTricubicFunction n2 = n1.toSinglePrecision();

		final double[] table = CustomTricubicFunction.computePowerTable(0, 0, 0);
		final float[] ftable = CustomTricubicFunction.computeFloatPowerTable(0, 0, 0);

		// Check no interpolation is correct
		e = n1.value(table);
		o = n1.value000();
		Assertions.assertEquals(e, o);

		e = n1.value(table, df_daA);
		o = n1.value000(df_daB);
		Assertions.assertEquals(e, o);
		Assertions.assertArrayEquals(df_daA, df_daB);

		e = n1.value(table, df_daA, d2f_da2A);
		o = n1.value000(df_daB, d2f_da2B);
		Assertions.assertEquals(e, o);
		Assertions.assertArrayEquals(df_daA, df_daB);
		Assertions.assertArrayEquals(d2f_da2A, d2f_da2B);

		// Check no interpolation is correct
		e = n2.value(ftable);
		o = n2.value000();
		Assertions.assertEquals(e, o);

		e = n2.value(ftable, df_daA);
		o = n2.value000(df_daB);
		Assertions.assertEquals(e, o);
		Assertions.assertArrayEquals(df_daA, df_daB);

		e = n2.value(ftable, df_daA, d2f_da2A);
		o = n2.value000(df_daB, d2f_da2B);
		Assertions.assertEquals(e, o);
		Assertions.assertArrayEquals(df_daA, df_daB);
		Assertions.assertArrayEquals(d2f_da2A, d2f_da2B);
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

		@Override
		public int getSize()
		{
			return 1;
		}

		@Override
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

		@Override
		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j]);
			return v;
		}
	}

	private class Float0TimingTask extends FloatTimingTask
	{
		public Float0TimingTask(float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Float0TimingTask.class.getSimpleName(), tables, nodes);
		}

		@Override
		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j]);
			return v;
		}
	}

	private class Double1TimingTask extends DoubleTimingTask
	{
		public Double1TimingTask(double[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Double1TimingTask.class.getSimpleName(), tables, nodes);
		}

		@Override
		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da);
			return v;
		}
	}

	private class Float1TimingTask extends FloatTimingTask
	{
		public Float1TimingTask(float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Float1TimingTask.class.getSimpleName(), tables, nodes);
		}

		@Override
		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da);
			return v;
		}
	}

	private class Double2TimingTask extends DoubleTimingTask
	{
		public Double2TimingTask(double[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Double2TimingTask.class.getSimpleName(), tables, nodes);
		}

		@Override
		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da, d2f_da2);
			return v;
		}
	}

	private class Float2TimingTask extends FloatTimingTask
	{
		public Float2TimingTask(float[][] tables, CustomTricubicFunction[] nodes)
		{
			super(Float2TimingTask.class.getSimpleName(), tables, nodes);
		}

		@Override
		public Object run(Object data)
		{
			double v = 0;
			for (int i = 0; i < nodes.length; i++)
				for (int j = 0; j < tables.length; j++)
					v += nodes[i].value(tables[j], df_da, d2f_da2);
			return v;
		}
	}

	@SpeedTag
	@Test
	public void floatCustomTricubicFunctionIsFasterUsingPrecomputedTable()
	{
		ExtraAssumptions.assumeSpeedTest();

		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 6, y = 5, z = 4;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[][][] fval = createData(x, y, z, null);
		final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval, zval,
				fval);

		// Extract nodes for testing
		final CustomTricubicFunction[] nodes = new CustomTricubicFunction[(x - 2) * (y - 2) * (z - 2)];
		final CustomTricubicFunction[] fnodes = new CustomTricubicFunction[nodes.length];
		for (int zi = 1, i = 0; zi < x - 1; zi++)
			for (int yi = 1; yi < y - 1; yi++)
				for (int xi = 1; xi < z - 1; xi++, i++)
				{
					nodes[i] = f1.getSplineNodeReference(zi, yi, xi);
					fnodes[i] = nodes[i].toSinglePrecision();
				}

		// Get points
		final double[][] tables = new double[3000][];
		final float[][] ftables = new float[tables.length][];
		for (int i = 0; i < tables.length; i++)
		{
			final double xx = r.nextDouble();
			final double yy = r.nextDouble();
			final double zz = r.nextDouble();

			tables[i] = CustomTricubicFunction.computePowerTable(xx, yy, zz);
			ftables[i] = CustomTricubicFunction.computeFloatPowerTable(xx, yy, zz);
		}

		final TimingService ts = new TimingService();

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

		final int n = ts.getSize();
		ts.repeat();
		if (TestSettings.allow(LogLevel.INFO))
			ts.report(n);

		for (int i = 1; i < n; i += 2)
		{
			final TimingResult r1 = ts.get(-i);
			final TimingResult r2 = ts.get(-i - 1);
			final double t1 = r1.getMean();
			final double t2 = r2.getMean();
			TestLog.logSpeedTestResult(t1 < t2, "%s  %f vs %s  %f : %.2fx", r1.getTask().getName(), t1,
					r2.getTask().getName(), t2, t2 / t1);
		}
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
		final int x = 6, y = 5, z = 4;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[][][] fval = createData(x, y, z, null);

		final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);
		final ExecutorService es = Executors.newFixedThreadPool(4);
		interpolator.setExecutorService(es);
		interpolator.setTaskSize(5);
		final CustomTricubicInterpolatingFunction f2 = interpolator.interpolate(xval, yval, zval, fval);
		es.shutdown();

		// Compare all nodes
		for (int i = 0; i < f1.getMaxXSplinePosition(); i++)
			for (int j = 0; j < f1.getMaxYSplinePosition(); j++)
				for (int k = 0; k < f1.getMaxZSplinePosition(); k++)
				{
					final DoubleCustomTricubicFunction n1 = (DoubleCustomTricubicFunction) f1.getSplineNodeReference(i,
							j, k);
					final DoubleCustomTricubicFunction n2 = (DoubleCustomTricubicFunction) f2.getSplineNodeReference(i,
							j, k);
					Assertions.assertArrayEquals(n1.getA(), n2.getA());
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
		final int x = 6, y = 5, z = 4;
		// Make it easy to have exact matching
		final double xscale = 2.0, yscale = 2.0, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[][][] fval = createData(x, y, z, null);

		final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

		final StandardTrivalueProcedure p = new StandardTrivalueProcedure();
		f1.sample(n, p);

		Assertions.assertArrayEquals(SimpleArrayUtils.newArray((x - 1) * n + 1, 0, xscale / n), p.x, 1e-6);
		Assertions.assertArrayEquals(SimpleArrayUtils.newArray((y - 1) * n + 1, 0, yscale / n), p.y, 1e-6);
		Assertions.assertArrayEquals(SimpleArrayUtils.newArray((z - 1) * n + 1, 0, zscale / n), p.z, 1e-6);

		for (int i = 0; i < p.x.length; i++)
			for (int j = 0; j < p.y.length; j++)
				for (int k = 0; k < p.z.length; k++)
					// Test original function interpolated value against the sample
					ExtraAssertions.assertEqualsRelative(f1.value(p.x[i], p.y[j], p.z[k]), p.value[i][j][k], 1e-8);
	}

	@Test
	public void canDynamicallySampleFunctionWithN2()
	{
		canDynamicallySampleFunction(2, false);
	}

	@Test
	public void canDynamicallySampleFunctionWithN3()
	{
		canDynamicallySampleFunction(3, false);
	}

	@Test
	public void canDynamicallySampleFunctionWithN2WithExecutorService()
	{
		canDynamicallySampleFunction(2, true);
	}

	@Test
	public void canDynamicallySampleFunctionWithN3WithExecutorService()
	{
		canDynamicallySampleFunction(3, true);
	}

	@SuppressWarnings("null")
	private void canDynamicallySampleFunction(int n, boolean threaded)
	{
		// This assumes that the sample method of the CustomTricubicInterpolatingFunction works!

		final int x = 6, y = 5, z = 4;
		// No scale for this test
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		final double[][][] fval = createData(x, y, z, null);

		final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		final DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(fval);
		final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(new DoubleArrayValueProvider(xval),
				new DoubleArrayValueProvider(yval), new DoubleArrayValueProvider(zval), f);

		final StandardTrivalueProcedure p = new StandardTrivalueProcedure();
		f1.sample(n, p);

		ExecutorService es = null;
		if (threaded)
		{
			es = Executors.newFixedThreadPool(4);
			interpolator.setExecutorService(es);
			interpolator.setTaskSize(5);
		}

		final StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
		interpolator.sample(f, n, p2);

		if (threaded)
			es.shutdown();

		Assertions.assertArrayEquals(p.x, p2.x, 1e-10);
		Assertions.assertArrayEquals(p.y, p2.y, 1e-10);
		Assertions.assertArrayEquals(p.z, p2.z, 1e-10);

		for (int i = 0; i < p.x.length; i++)
			for (int j = 0; j < p.y.length; j++)
				Assertions.assertArrayEquals(p.value[i][j], p2.value[i][j]);
	}

	@Test
	public void canExternaliseDoubleFunction() throws IOException
	{
		canExternaliseFunction(false);
	}

	@Test
	public void canExternaliseFloatFunction() throws IOException
	{
		canExternaliseFunction(true);
	}

	private void canExternaliseFunction(boolean singlePrecision) throws IOException
	{
		final int x = 6, y = 5, z = 4;
		final double xscale = 1, yscale = 0.5, zscale = 2.0;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
		final double[][][] fval = createData(x, y, z, null);

		final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
		final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

		if (singlePrecision)
			f1.toSinglePrecision();

		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		f1.write(b);

		final byte[] bytes = b.toByteArray();
		//TestLog.debug("Single precision = %b, size = %d, memory estimate = %d\n", singlePrecision, bytes.length,
		//		CustomTricubicInterpolatingFunction.estimateSize(new int[] { x, y, z })
		//				.getMemoryFootprint(singlePrecision));
		final CustomTricubicInterpolatingFunction f2 = CustomTricubicInterpolatingFunction
				.read(new ByteArrayInputStream(bytes));

		final int n = 2;
		final StandardTrivalueProcedure p1 = new StandardTrivalueProcedure();
		f1.sample(n, p1);
		final StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
		f2.sample(n, p2);

		Assertions.assertArrayEquals(p1.x, p2.x);
		Assertions.assertArrayEquals(p1.y, p2.y);
		Assertions.assertArrayEquals(p1.z, p2.z);

		for (int i = 0; i < p1.x.length; i++)
			for (int j = 0; j < p1.y.length; j++)
				for (int k = 0; k < p1.z.length; k++)
					Assertions.assertEquals(f1.value(p1.x[i], p1.y[j], p1.z[k]), f2.value(p1.x[i], p1.y[j], p1.z[k]));
	}

	@Test
	public void canInterpolateAcrossNodesForValueAndGradient1()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		// Difference scales
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		final double[] df_daA = new double[3];
		final double[] df_daB = new double[3];
		for (int ii = 0; ii < 3; ii++)
		{
			final double[][][] fval = createData(x, y, z, (ii == 0) ? null : r);
			final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval,
					zval, fval);
			for (int zz = f1.getMaxZSplinePosition(); zz > 0; zz--)
				for (int yy = f1.getMaxYSplinePosition(); yy > 0; yy--)
					for (int xx = f1.getMaxXSplinePosition(); xx > 0; xx--)
					{
						final CustomTricubicFunction next = f1.getSplineNodeReference(xx, yy, zz);

						// Test that interpolating at x=1 equals x=0 for the next node
						for (int k = 0; k < 2; k++)
						{
							final int zzz = zz - k;
							for (int j = 0; j < 2; j++)
							{
								final int yyy = yy - j;
								for (int i = 0; i < 2; i++)
								{
									final int xxx = xx - i;
									if (i + j + k == 0)
										continue;

									final CustomTricubicFunction previous = f1.getSplineNodeReference(xxx, yyy, zzz);

									final double e = next.value(0, 0, 0, df_daA);
									final double o = previous.value(i, j, k, df_daB);
									ExtraAssertions.assertEqualsRelative(e, o, 1e-8);

									for (int c = 0; c < 3; c++)
										ExtraAssertions.assertEqualsRelative(df_daA[c], df_daB[c], 1e-8);
									//Assertions.assertTrue(DoubleEquality.almostEqualRelativeOrAbsolute(df_daA[c],
									//		df_daB[c], 1e-8, 1e-12));
								}
							}
						}
					}
		}
	}

	@Test
	public void cannotInterpolateAcrossNodesForGradient2()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int x = 4, y = 4, z = 4;
		// Difference scales
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		final double[] df_daA = new double[3];
		final double[] df_daB = new double[3];
		final double[] d2f_da2A = new double[3];
		final double[] d2f_da2B = new double[3];
		for (int ii = 0; ii < 3; ii++)
		{
			final Statistics[] value = new Statistics[3];
			for (int i = 0; i < value.length; i++)
				value[i] = new Statistics();

			final double[][][] fval = createData(x, y, z, (ii == 0) ? null : r);
			final CustomTricubicInterpolatingFunction f1 = new CustomTricubicInterpolator().interpolate(xval, yval,
					zval, fval);
			for (int zz = f1.getMaxZSplinePosition(); zz > 0; zz--)
				for (int yy = f1.getMaxYSplinePosition(); yy > 0; yy--)
					for (int xx = f1.getMaxXSplinePosition(); xx > 0; xx--)
					{
						final CustomTricubicFunction next = f1.getSplineNodeReference(xx, yy, zz);

						// Test that interpolating at x=1 equals x=0 for the next node
						for (int k = 0; k < 2; k++)
						{
							final int zzz = zz - k;
							for (int j = 0; j < 2; j++)
							{
								final int yyy = yy - j;
								for (int i = 0; i < 2; i++)
								{
									final int xxx = xx - i;
									if (i + j + k == 0)
										continue;

									final CustomTricubicFunction previous = f1.getSplineNodeReference(xxx, yyy, zzz);

									next.value(0, 0, 0, df_daA, d2f_da2A);
									previous.value(i, j, k, df_daB, d2f_da2B);

									for (int c = 0; c < 3; c++)
									{
										// The function may change direction so check the 2nd derivative magnitude is similar
										//TestLog.debug("[%d] %f vs %f\n", c, d2f_da2A[c], d2f_da2B[c], DoubleEquality.relativeError(d2f_da2A[c], d2f_da2B[c]));
										d2f_da2A[c] = Math.abs(d2f_da2A[c]);
										d2f_da2B[c] = Math.abs(d2f_da2B[c]);
										value[c].add(DoubleEquality.relativeError(d2f_da2A[c], d2f_da2B[c]));
									}
								}
							}
						}
					}

			boolean same = true;
			for (int c = 0; c < 3; c++)
				// The second gradients are so different that this should fail
				same = same && value[c].getMean() < 0.01;
			//TestLog.debug("d2yda2[%d] Error = %f +/- %f\n", c, value[c].getMean(),
			//		value[c].getStandardDeviation());
			Assertions.assertFalse(same);
		}
	}

	@Test
	public void searchSplineImprovesFunctionValue()
	{
		// Skip this as it is for testing the binary search works
		Assumptions.assumeTrue(false);

		final RandomGenerator r = TestSettings.getRandomGenerator();
		// Bigger depth of field to capture astigmatism centre
		final int x = 10, y = 10, z = 10;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		for (int ii = 0; ii < 3; ii++)
		{
			final double cx = (x - 1) / 2.0 + r.nextDouble() / 2;
			final double cy = (y - 1) / 2.0 + r.nextDouble() / 2;
			final double cz = (z - 1) / 2.0 + r.nextDouble() / 2;
			final double[][][] fval = createData(x, y, z, cx, cy, cz, 2);

			final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
			final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

			// Check the search approaches the actual function value
			double[] last = null;
			for (int i = 0; i <= 10; i++)
			{
				final double[] optimum = f1.search(true, i, 0, 0);
				//double d = Maths.distance(cx, cy, cz, optimum[0], optimum[1], optimum[2]);
				//TestLog.debug("[%d] %f,%f,%f %d = %s : dist = %f : error = %f\n", ii, cx, cy, cz, i,
				//		Arrays.toString(optimum), d, DoubleEquality.relativeError(amplitude, optimum[3]));

				// Skip 0 to 1 as it moves from an exact node value to interpolation
				// which may use a different node depending on the gradient
				if (i > 1)
				{
					@SuppressWarnings("null")
					final double d = Maths.distance(last[0], last[1], last[2], optimum[0], optimum[1], optimum[2]);
					TestLog.info("[%d] %f,%f,%f %d = %s : dist = %f : change = %g\n", ii, cx, cy, cz, i,
							Arrays.toString(optimum), d, DoubleEquality.relativeError(last[3], optimum[3]));
					Assertions.assertTrue(optimum[3] >= last[3]);
				}
				last = optimum;
			}
		}
	}

	@Test
	public void canFindOptimum()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		// Bigger depth of field to capture astigmatism centre
		final int x = 10, y = 10, z = 10;
		final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
		final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
		final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
		for (int ii = 0; ii < 10; ii++)
		{
			final double cx = (x - 1) / 2.0 + r.nextDouble() / 2;
			final double cy = (y - 1) / 2.0 + r.nextDouble() / 2;
			final double cz = (z - 1) / 2.0 + r.nextDouble() / 2;
			final double[][][] fval = createData(x, y, z, cx, cy, cz, 2);

			// Test max and min search
			final boolean maximum = (ii % 2 == 1);
			if (!maximum)
			{
				// Invert
				for (int xx = 0; xx < x; xx++)
					for (int yy = 0; yy < y; yy++)
						for (int zz = 0; zz < z; zz++)
							fval[xx][yy][zz] = -fval[xx][yy][zz];
				amplitude = -amplitude;
			}

			final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
			final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

			final double[] last = f1.search(maximum, 10, 1e-6, 0);

			// Since the cubic function is not the same as the input we cannot be too precise here
			ExtraAssertions.assertEqualsRelative(cx, last[0], 5e-2);
			ExtraAssertions.assertEqualsRelative(cy, last[1], 5e-2);
			ExtraAssertions.assertEqualsRelative(cz, last[2], 5e-2);
			ExtraAssertions.assertEqualsRelative(amplitude, last[3], 5e-2);
		}
	}
}
