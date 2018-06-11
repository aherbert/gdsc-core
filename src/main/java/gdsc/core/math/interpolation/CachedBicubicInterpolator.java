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
package gdsc.core.math.interpolation;

/**
 * Cached Bicubic Interpolator using the Catmull-Rom spline.
 * <p>
 * Taken from http://www.paulinternet.nl/?page=bicubic.
 */
public class CachedBicubicInterpolator
{
	private double a00, a01, a02, a03;
	private double a10, a11, a12, a13;
	private double a20, a21, a22, a23;
	private double a30, a31, a32, a33;

	/**
	 * Update coefficients.
	 * <p>
	 * Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0. This is because the cubic
	 * interpolation uses the points to construct the gradient at x=0 as ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will
	 * just zero the gradient at x=0. Likewise for the gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
	 *
	 * @param p
	 *            the value of the function at x=-1 to x=2 and y=-1 to y=2
	 * @return the value
	 */
	public void updateCoefficients(double[][] p)
	{
		//@formatter:off
		a00 = p[1][1];
		a01 = -.5*p[1][0] + .5*p[1][2];
		a02 = p[1][0] - 2.5*p[1][1] + 2*p[1][2] - .5*p[1][3];
		a03 = -.5*p[1][0] + 1.5*p[1][1] - 1.5*p[1][2] + .5*p[1][3];
		a10 = -.5*p[0][1] + .5*p[2][1];
		a11 = .25*p[0][0] - .25*p[0][2] - .25*p[2][0] + .25*p[2][2];
		a12 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + .5*p[2][0] - 1.25*p[2][1] + p[2][2] - .25*p[2][3];
		a13 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .25*p[2][0] + .75*p[2][1] - .75*p[2][2] + .25*p[2][3];
		a20 = p[0][1] - 2.5*p[1][1] + 2*p[2][1] - .5*p[3][1];
		a21 = -.5*p[0][0] + .5*p[0][2] + 1.25*p[1][0] - 1.25*p[1][2] - p[2][0] + p[2][2] + .25*p[3][0] - .25*p[3][2];
		a22 = p[0][0] - 2.5*p[0][1] + 2*p[0][2] - .5*p[0][3] - 2.5*p[1][0] + 6.25*p[1][1] - 5*p[1][2] + 1.25*p[1][3] + 2*p[2][0] - 5*p[2][1] + 4*p[2][2] - p[2][3] - .5*p[3][0] + 1.25*p[3][1] - p[3][2] + .25*p[3][3];
		a23 = -.5*p[0][0] + 1.5*p[0][1] - 1.5*p[0][2] + .5*p[0][3] + 1.25*p[1][0] - 3.75*p[1][1] + 3.75*p[1][2] - 1.25*p[1][3] - p[2][0] + 3*p[2][1] - 3*p[2][2] + p[2][3] + .25*p[3][0] - .75*p[3][1] + .75*p[3][2] - .25*p[3][3];
		a30 = -.5*p[0][1] + 1.5*p[1][1] - 1.5*p[2][1] + .5*p[3][1];
		a31 = .25*p[0][0] - .25*p[0][2] - .75*p[1][0] + .75*p[1][2] + .75*p[2][0] - .75*p[2][2] - .25*p[3][0] + .25*p[3][2];
		a32 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + 1.5*p[1][0] - 3.75*p[1][1] + 3*p[1][2] - .75*p[1][3] - 1.5*p[2][0] + 3.75*p[2][1] - 3*p[2][2] + .75*p[2][3] + .5*p[3][0] - 1.25*p[3][1] + p[3][2] - .25*p[3][3];
		a33 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .75*p[1][0] + 2.25*p[1][1] - 2.25*p[1][2] + .75*p[1][3] + .75*p[2][0] - 2.25*p[2][1] + 2.25*p[2][2] - .75*p[2][3] - .25*p[3][0] + .75*p[3][1] - .75*p[3][2] + .25*p[3][3];
		//@formatter:on
	}

	/**
	 * Update coefficients.
	 * <p>
	 * Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0. This is because the cubic
	 * interpolation uses the points to construct the gradient at x=0 as ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will
	 * just zero the gradient at x=0. Likewise for the gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
	 *
	 * @param p
	 *            the value of the function at x=-1 to x=2 and y=-1 to y=2
	 * @return the value
	 */
	public void updateCoefficients(float[][] p)
	{
		//@formatter:off
		a00 = p[1][1];
		a01 = -.5*p[1][0] + .5*p[1][2];
		a02 = p[1][0] - 2.5*p[1][1] + 2*p[1][2] - .5*p[1][3];
		a03 = -.5*p[1][0] + 1.5*p[1][1] - 1.5*p[1][2] + .5*p[1][3];
		a10 = -.5*p[0][1] + .5*p[2][1];
		a11 = .25*p[0][0] - .25*p[0][2] - .25*p[2][0] + .25*p[2][2];
		a12 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + .5*p[2][0] - 1.25*p[2][1] + p[2][2] - .25*p[2][3];
		a13 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .25*p[2][0] + .75*p[2][1] - .75*p[2][2] + .25*p[2][3];
		a20 = p[0][1] - 2.5*p[1][1] + 2*p[2][1] - .5*p[3][1];
		a21 = -.5*p[0][0] + .5*p[0][2] + 1.25*p[1][0] - 1.25*p[1][2] - p[2][0] + p[2][2] + .25*p[3][0] - .25*p[3][2];
		a22 = p[0][0] - 2.5*p[0][1] + 2*p[0][2] - .5*p[0][3] - 2.5*p[1][0] + 6.25*p[1][1] - 5*p[1][2] + 1.25*p[1][3] + 2*p[2][0] - 5*p[2][1] + 4*p[2][2] - p[2][3] - .5*p[3][0] + 1.25*p[3][1] - p[3][2] + .25*p[3][3];
		a23 = -.5*p[0][0] + 1.5*p[0][1] - 1.5*p[0][2] + .5*p[0][3] + 1.25*p[1][0] - 3.75*p[1][1] + 3.75*p[1][2] - 1.25*p[1][3] - p[2][0] + 3*p[2][1] - 3*p[2][2] + p[2][3] + .25*p[3][0] - .75*p[3][1] + .75*p[3][2] - .25*p[3][3];
		a30 = -.5*p[0][1] + 1.5*p[1][1] - 1.5*p[2][1] + .5*p[3][1];
		a31 = .25*p[0][0] - .25*p[0][2] - .75*p[1][0] + .75*p[1][2] + .75*p[2][0] - .75*p[2][2] - .25*p[3][0] + .25*p[3][2];
		a32 = -.5*p[0][0] + 1.25*p[0][1] - p[0][2] + .25*p[0][3] + 1.5*p[1][0] - 3.75*p[1][1] + 3*p[1][2] - .75*p[1][3] - 1.5*p[2][0] + 3.75*p[2][1] - 3*p[2][2] + .75*p[2][3] + .5*p[3][0] - 1.25*p[3][1] + p[3][2] - .25*p[3][3];
		a33 = .25*p[0][0] - .75*p[0][1] + .75*p[0][2] - .25*p[0][3] - .75*p[1][0] + 2.25*p[1][1] - 2.25*p[1][2] + .75*p[1][3] + .75*p[2][0] - 2.25*p[2][1] + 2.25*p[2][2] - .75*p[2][3] - .25*p[3][0] + .75*p[3][1] - .75*p[3][2] + .25*p[3][3];
		//@formatter:on
	}

	/**
	 * Update coefficients.
	 * <p>
	 * Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0. This is because the cubic
	 * interpolation uses the points to construct the gradient at x=0 as ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will
	 * just zero the gradient at x=0. Likewise for the gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
	 *
	 * @param p
	 *            the value of the function at x=-1 to x=2 and y=-1 to y=2, packed in yx order.
	 * @return the value
	 */
	public void updateCoefficients(double[] p)
	{
		//@formatter:off
		a00 = p[5];
		a01 = -.5*p[1] + .5*p[9];
		a02 = p[1] - 2.5*p[5] + 2*p[9] - .5*p[13];
		a03 = -.5*p[1] + 1.5*p[5] - 1.5*p[9] + .5*p[13];
		a10 = -.5*p[4] + .5*p[6];
		a11 = .25*p[0] - .25*p[8] - .25*p[2] + .25*p[10];
		a12 = -.5*p[0] + 1.25*p[4] - p[8] + .25*p[12] + .5*p[2] - 1.25*p[6] + p[10] - .25*p[14];
		a13 = .25*p[0] - .75*p[4] + .75*p[8] - .25*p[12] - .25*p[2] + .75*p[6] - .75*p[10] + .25*p[14];
		a20 = p[4] - 2.5*p[5] + 2*p[6] - .5*p[7];
		a21 = -.5*p[0] + .5*p[8] + 1.25*p[1] - 1.25*p[9] - p[2] + p[10] + .25*p[3] - .25*p[11];
		a22 = p[0] - 2.5*p[4] + 2*p[8] - .5*p[12] - 2.5*p[1] + 6.25*p[5] - 5*p[9] + 1.25*p[13] + 2*p[2] - 5*p[6] + 4*p[10] - p[14] - .5*p[3] + 1.25*p[7] - p[11] + .25*p[15];
		a23 = -.5*p[0] + 1.5*p[4] - 1.5*p[8] + .5*p[12] + 1.25*p[1] - 3.75*p[5] + 3.75*p[9] - 1.25*p[13] - p[2] + 3*p[6] - 3*p[10] + p[14] + .25*p[3] - .75*p[7] + .75*p[11] - .25*p[15];
		a30 = -.5*p[4] + 1.5*p[5] - 1.5*p[6] + .5*p[7];
		a31 = .25*p[0] - .25*p[8] - .75*p[1] + .75*p[9] + .75*p[2] - .75*p[10] - .25*p[3] + .25*p[11];
		a32 = -.5*p[0] + 1.25*p[4] - p[8] + .25*p[12] + 1.5*p[1] - 3.75*p[5] + 3*p[9] - .75*p[13] - 1.5*p[2] + 3.75*p[6] - 3*p[10] + .75*p[14] + .5*p[3] - 1.25*p[7] + p[11] - .25*p[15];
		a33 = .25*p[0] - .75*p[4] + .75*p[8] - .25*p[12] - .75*p[1] + 2.25*p[5] - 2.25*p[9] + .75*p[13] + .75*p[2] - 2.25*p[6] + 2.25*p[10] - .75*p[14] - .25*p[3] + .75*p[7] - .75*p[11] + .25*p[15];
		//@formatter:on
	}

	/**
	 * Update coefficients.
	 * <p>
	 * Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0. This is because the cubic
	 * interpolation uses the points to construct the gradient at x=0 as ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will
	 * just zero the gradient at x=0. Likewise for the gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
	 *
	 * @param p
	 *            the value of the function at x=-1 to x=2 and y=-1 to y=2, packed in yx order.
	 * @return the value
	 */
	public void updateCoefficients(float[] p)
	{
		//@formatter:off
		a00 = p[5];
		a01 = -.5*p[1] + .5*p[9];
		a02 = p[1] - 2.5*p[5] + 2*p[9] - .5*p[13];
		a03 = -.5*p[1] + 1.5*p[5] - 1.5*p[9] + .5*p[13];
		a10 = -.5*p[4] + .5*p[6];
		a11 = .25*p[0] - .25*p[8] - .25*p[2] + .25*p[10];
		a12 = -.5*p[0] + 1.25*p[4] - p[8] + .25*p[12] + .5*p[2] - 1.25*p[6] + p[10] - .25*p[14];
		a13 = .25*p[0] - .75*p[4] + .75*p[8] - .25*p[12] - .25*p[2] + .75*p[6] - .75*p[10] + .25*p[14];
		a20 = p[4] - 2.5*p[5] + 2*p[6] - .5*p[7];
		a21 = -.5*p[0] + .5*p[8] + 1.25*p[1] - 1.25*p[9] - p[2] + p[10] + .25*p[3] - .25*p[11];
		a22 = p[0] - 2.5*p[4] + 2*p[8] - .5*p[12] - 2.5*p[1] + 6.25*p[5] - 5*p[9] + 1.25*p[13] + 2*p[2] - 5*p[6] + 4*p[10] - p[14] - .5*p[3] + 1.25*p[7] - p[11] + .25*p[15];
		a23 = -.5*p[0] + 1.5*p[4] - 1.5*p[8] + .5*p[12] + 1.25*p[1] - 3.75*p[5] + 3.75*p[9] - 1.25*p[13] - p[2] + 3*p[6] - 3*p[10] + p[14] + .25*p[3] - .75*p[7] + .75*p[11] - .25*p[15];
		a30 = -.5*p[4] + 1.5*p[5] - 1.5*p[6] + .5*p[7];
		a31 = .25*p[0] - .25*p[8] - .75*p[1] + .75*p[9] + .75*p[2] - .75*p[10] - .25*p[3] + .25*p[11];
		a32 = -.5*p[0] + 1.25*p[4] - p[8] + .25*p[12] + 1.5*p[1] - 3.75*p[5] + 3*p[9] - .75*p[13] - 1.5*p[2] + 3.75*p[6] - 3*p[10] + .75*p[14] + .5*p[3] - 1.25*p[7] + p[11] - .25*p[15];
		a33 = .25*p[0] - .75*p[4] + .75*p[8] - .25*p[12] - .75*p[1] + 2.25*p[5] - 2.25*p[9] + .75*p[13] + .75*p[2] - 2.25*p[6] + 2.25*p[10] - .75*p[14] - .25*p[3] + .75*p[7] - .75*p[11] + .25*p[15];
		//@formatter:on
	}

	/**
	 * Gets the interpolated value.
	 *
	 * @param x
	 *            the x (between 0 and 1)
	 * @param y
	 *            the y (between 0 and 1)
	 * @return the value
	 */
	public double getValue(double x, double y)
	{
		double x2 = x * x;
		double x3 = x2 * x;
		double y2 = y * y;
		double y3 = y2 * y;

		//@formatter:off
		return (a00 + a01 * y + a02 * y2 + a03 * y3) +
		       (a10 + a11 * y + a12 * y2 + a13 * y3) * x +
		       (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 +
		       (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
		//@formatter:on
	}

	/**
	 * Gets the interpolated value. The power must be computed correctly. This function allows you to pre-compute the
	 * powers for efficient sub-sampling.
	 *
	 * @param x
	 *            the x (between 0 and 1)
	 * @param x2
	 *            x^2
	 * @param x3
	 *            x^3
	 * @param y
	 *            the y (between 0 and 1)
	 * @param y2
	 *            y^2
	 * @param y3
	 *            y^3
	 * @return the value
	 */
	public double getValue(double x, double x2, double x3, double y, double y2, double y3)
	{
		//@formatter:off
		return (a00 + a01 * y + a02 * y2 + a03 * y3) +
		       (a10 + a11 * y + a12 * y2 + a13 * y3) * x +
		       (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 +
		       (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
		//@formatter:on
	}
}
