/*-
 * %%Ignore-License
 *
 * GDSC Software
 *
 * This is an extension of the
 * org.apache.commons.math3.analysis.interpolation.TricubicFunction
 *
 * Modifications have been made to allow computation of gradients and computation
 * with pre-computed x,y,z powers using single/floating precision.
 *
 * The code is released under the original Apache licence:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gdsc.core.math.interpolation;

import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

import gdsc.core.utils.SimpleArrayUtils;

/**
 * 3D-spline function.
 */
public abstract class CustomTricubicFunction implements TrivariateFunction
{
	/**
	 * Get a copy of the 64 coefficients for the tricubic function.
	 *
	 * @return the coefficients
	 */
	abstract public double[] getA();

	/**
	 * Get coefficient at index i for the tricubic function.
	 *
	 * @param i
	 *            the index
	 * @return the coefficient
	 */
	abstract public double get(int i);

	/**
	 * Get coefficient at index i for the tricubic function.
	 *
	 * @param i
	 *            the index
	 * @return the coefficient
	 */
	abstract public float getf(int i);

	/**
	 * Checks if is single precision.
	 *
	 * @return true, if is single precision
	 */
	abstract public boolean isSinglePrecision();

	/**
	 * Convert this instance to single precision.
	 *
	 * @return the custom tricubic function
	 */
	abstract public CustomTricubicFunction toSinglePrecision();

	/**
	 * Convert this instance to double precision.
	 *
	 * @return the custom tricubic function
	 */
	abstract public CustomTricubicFunction toDoublePrecision();

	/**
	 * Copy the function.
	 *
	 * @return the custom tricubic function
	 */
	abstract public CustomTricubicFunction copy();

	/**
	 * Gets the index in the table for the specified position.
	 *
	 * @param i
	 *            the x index
	 * @param j
	 *            the x index
	 * @param k
	 *            the z index
	 * @return the index
	 */
	public static int getIndex(int i, int j, int k)
	{
		return i + 4 * (j + 4 * k);
	}

	/**
	 * Get the interpolated value
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the interpolated value.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	@Override
	public double value(double x, double y, double z) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
			throw new OutOfRangeException(x, 0, 1);
		if (y < 0 || y > 1)
			throw new OutOfRangeException(y, 0, 1);
		if (z < 0 || z > 1)
			throw new OutOfRangeException(z, 0, 1);

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return value0(pX, pY, pZ);
	}

	/**
	 * Get the interpolated value
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the interpolated value.
	 */
	public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z)
	{
		return value0(x.p, y.p, z.p);
	}

	/**
	 * Get the interpolated value
	 *
	 * @param pX
	 *            x-coordinate powers of the interpolation point.
	 * @param pY
	 *            y-coordinate powers of the interpolation point.
	 * @param pZ
	 *            z-coordinate powers of the interpolation point.
	 * @return the interpolated value.
	 */
	abstract protected double value0(final double[] pX, final double[] pY, final double[] pZ);

	/**
	 * Compute the power table.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power table.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public static double[] computePowerTable(double x, double y, double z) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
			throw new OutOfRangeException(x, 0, 1);
		if (y < 0 || y > 1)
			throw new OutOfRangeException(y, 0, 1);
		if (z < 0 || z > 1)
			throw new OutOfRangeException(z, 0, 1);

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return computePowerTable(pX, pY, pZ);
	}

	/**
	 * Compute the power table.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power table.
	 */
	public static double[] computePowerTable(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z)
	{
		return computePowerTable(x.p, y.p, z.p);
	}

	/**
	 * Compute the power table.
	 *
	 * @param pX
	 *            x-coordinate powers of the interpolation point.
	 * @param pY
	 *            y-coordinate powers of the interpolation point.
	 * @param pZ
	 *            z-coordinate powers of the interpolation point.
	 * @return the power table.
	 */
	private static double[] computePowerTable(final double[] pX, final double[] pY, final double[] pZ)
	{
		final double[] table = new double[64];

		table[0] = 1;
		table[1] = pX[0];
		table[2] = pX[1];
		table[3] = pX[2];
		table[4] = pY[0];
		table[5] = pY[0] * pX[0];
		table[6] = pY[0] * pX[1];
		table[7] = pY[0] * pX[2];
		table[8] = pY[1];
		table[9] = pY[1] * pX[0];
		table[10] = pY[1] * pX[1];
		table[11] = pY[1] * pX[2];
		table[12] = pY[2];
		table[13] = pY[2] * pX[0];
		table[14] = pY[2] * pX[1];
		table[15] = pY[2] * pX[2];
		table[16] = pZ[0];
		table[17] = pZ[0] * pX[0];
		table[18] = pZ[0] * pX[1];
		table[19] = pZ[0] * pX[2];
		table[20] = pZ[0] * pY[0];
		table[21] = table[20] * pX[0];
		table[22] = table[20] * pX[1];
		table[23] = table[20] * pX[2];
		table[24] = pZ[0] * pY[1];
		table[25] = table[24] * pX[0];
		table[26] = table[24] * pX[1];
		table[27] = table[24] * pX[2];
		table[28] = pZ[0] * pY[2];
		table[29] = table[28] * pX[0];
		table[30] = table[28] * pX[1];
		table[31] = table[28] * pX[2];
		table[32] = pZ[1];
		table[33] = pZ[1] * pX[0];
		table[34] = pZ[1] * pX[1];
		table[35] = pZ[1] * pX[2];
		table[36] = pZ[1] * pY[0];
		table[37] = table[36] * pX[0];
		table[38] = table[36] * pX[1];
		table[39] = table[36] * pX[2];
		table[40] = pZ[1] * pY[1];
		table[41] = table[40] * pX[0];
		table[42] = table[40] * pX[1];
		table[43] = table[40] * pX[2];
		table[44] = pZ[1] * pY[2];
		table[45] = table[44] * pX[0];
		table[46] = table[44] * pX[1];
		table[47] = table[44] * pX[2];
		table[48] = pZ[2];
		table[49] = pZ[2] * pX[0];
		table[50] = pZ[2] * pX[1];
		table[51] = pZ[2] * pX[2];
		table[52] = pZ[2] * pY[0];
		table[53] = table[52] * pX[0];
		table[54] = table[52] * pX[1];
		table[55] = table[52] * pX[2];
		table[56] = pZ[2] * pY[1];
		table[57] = table[56] * pX[0];
		table[58] = table[56] * pX[1];
		table[59] = table[56] * pX[2];
		table[60] = pZ[2] * pY[2];
		table[61] = table[60] * pX[0];
		table[62] = table[60] * pX[1];
		table[63] = table[60] * pX[2];

		return table;
	}

	/**
	 * Checks if the power table is at the boundary of the interpolation range for the given dimension, i.e. 0 or 1
	 *
	 * @param dimension
	 *            the dimension [x=0,y=1,z=2]
	 * @param table
	 *            the table
	 * @return true, if at the boundary
	 */
	public static boolean isBoundary(int dimension, double[] table)
	{
		switch (dimension)
		{
			case 0:
				return isBoundary(table[1]);
			case 1:
				return isBoundary(table[4]);
			case 2:
				return isBoundary(table[16]);
			default:
				return false;
		}
	}

	/**
	 * Checks if the power table is at the boundary of the interpolation range for the given dimension, i.e. 0 or 1
	 *
	 * @param dimension
	 *            the dimension [x=0,y=1,z=2]
	 * @param table
	 *            the table
	 * @return true, if at the boundary
	 */
	public static boolean isBoundary(int dimension, float[] table)
	{
		switch (dimension)
		{
			case 0:
				return isBoundary(table[1]);
			case 1:
				return isBoundary(table[4]);
			case 2:
				return isBoundary(table[16]);
			default:
				return false;
		}
	}

	/**
	 * Checks if the value is 0 or 1
	 *
	 * @param d
	 *            the value
	 * @return true, if 0 or 1
	 */
	private static boolean isBoundary(double d)
	{
		return d == 0 || d == 1;
	}

	/**
	 * Compute the power table.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power table.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public static float[] computeFloatPowerTable(double x, double y, double z) throws OutOfRangeException
	{
		// Compute as a double for precision
		return toFloat(computePowerTable(x, y, z));
	}

	/**
	 * Compute the power table.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power table.
	 */
	public static float[] computeFloatPowerTable(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z)
	{
		// Compute as a double for precision
		return toFloat(computePowerTable(x, y, z));
	}

	/**
	 * Convert a length 64 array to a float
	 *
	 * @param d
	 *            the array
	 * @return the float array
	 */
	protected static float[] toFloat(double[] d)
	{
		final float[] f = new float[64];
		for (int i = 0; i < 64; i++)
			f[i] = (float) d[i];
		return f;
	}

	/**
	 * Convert a length 64 array to a double
	 *
	 * @param f
	 *            the array
	 * @return the double array
	 */
	protected static double[] toDouble(float[] f)
	{
		final double[] d = new double[64];
		for (int i = 0; i < 64; i++)
			d[i] = f[i];
		return d;
	}

	/**
	 * Scale the power table. The scaled table can be used for fast computation of the gradients.
	 *
	 * @param table
	 *            the table
	 * @param n
	 *            the scale
	 * @return the scaled table
	 */
	public static double[] scalePowerTable(double[] table, int n)
	{
		final double[] tableN = new double[64];
		for (int i = 0; i < 64; i++)
			tableN[i] = n * table[i];
		return tableN;
	}

	/**
	 * Scale the power table. The scaled table can be used for fast computation of the gradients.
	 *
	 * @param table
	 *            the table
	 * @param n
	 *            the scale
	 * @return the scaled table
	 */
	public static float[] scalePowerTable(float[] table, int n)
	{
		final float[] tableN = new float[64];
		for (int i = 0; i < 64; i++)
			tableN[i] = n * table[i];
		return tableN;
	}

	/**
	 * Get the value using a pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @return the interpolated value.
	 */
	abstract public double value(double[] table);

	/**
	 * Get the value using a pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @return the interpolated value.
	 */
	abstract public double value(float[] table);

	/**
	 * Compute the value and partial first-order derivatives
	 * <p>
	 * WARNING: The gradients will be unscaled.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public double value(double x, double y, double z, double[] df_da) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
			throw new OutOfRangeException(x, 0, 1);
		if (y < 0 || y > 1)
			throw new OutOfRangeException(y, 0, 1);
		if (z < 0 || z > 1)
			throw new OutOfRangeException(z, 0, 1);

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return value1(pX, pY, pZ, df_da);
	}

	/**
	 * Compute the value and partial first-order derivatives
	 * <p>
	 * The gradients are scaled
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z, double[] df_da)
	{
		final double value = value1(x.p, y.p, z.p, df_da);
		df_da[0] = x.scaleGradient(df_da[0]);
		df_da[1] = y.scaleGradient(df_da[1]);
		df_da[2] = z.scaleGradient(df_da[2]);
		return value;
	}

	/**
	 * Compute the value and partial first-order derivatives
	 *
	 * @param pX
	 *            x-coordinate powers of the interpolation point.
	 * @param pY
	 *            y-coordinate powers of the interpolation point.
	 * @param pZ
	 *            z-coordinate powers of the interpolation point.
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract protected double value1(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da);

	/**
	 * Compute the value and partial first-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(double[] table, double[] df_da);

	/**
	 * Compute the partial first-order derivatives using pre-computed power table. Provides separability between
	 * computing the value and the derivative.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 */
	abstract public void gradient(double[] table, double[] df_da);

	/**
	 * Compute the value and partial first-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(float[] table, double[] df_da);

	/**
	 * Compute the partial first-order derivatives using pre-computed power table. Provides separability between
	 * computing the value and the derivative.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 */
	abstract public void gradient(float[] table, double[] df_da);

	/**
	 * Compute the value and partial first-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param table2
	 *            the power table multiplied by 2
	 * @param table3
	 *            the power table multiplied by 3
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(double[] table, double[] table2, double[] table3, double[] df_da);

	/**
	 * Compute the value and partial first-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param table2
	 *            the power table multiplied by 2
	 * @param table3
	 *            the power table multiplied by 3
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(float[] table, float[] table2, float[] table3, double[] df_da);

	/**
	 * Compute the value and partial first-order and second-order derivatives
	 * <p>
	 * WARNING: The gradients will be unscaled.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public double value(double x, double y, double z, double[] df_da, double[] d2f_da2) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
			throw new OutOfRangeException(x, 0, 1);
		if (y < 0 || y > 1)
			throw new OutOfRangeException(y, 0, 1);
		if (z < 0 || z > 1)
			throw new OutOfRangeException(z, 0, 1);

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return value2(pX, pY, pZ, df_da, d2f_da2);
	}

	/**
	 * Compute the value and partial first-order and second-order derivatives
	 * <p>
	 * The gradients are scaled.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z, double[] df_da,
			double[] d2f_da2)
	{
		final double value = value2(x.p, y.p, z.p, df_da, d2f_da2);
		df_da[0] = x.scaleGradient(df_da[0]);
		df_da[1] = y.scaleGradient(df_da[1]);
		df_da[2] = z.scaleGradient(df_da[2]);
		d2f_da2[0] = x.scaleGradient2(d2f_da2[0]);
		d2f_da2[1] = y.scaleGradient2(d2f_da2[1]);
		d2f_da2[2] = z.scaleGradient2(d2f_da2[2]);
		return value;
	}

	/**
	 * Compute the value and partial first-order and second-order derivatives
	 *
	 * @param pX
	 *            x-coordinate powers of the interpolation point.
	 * @param pY
	 *            y-coordinate powers of the interpolation point.
	 * @param pZ
	 *            z-coordinate powers of the interpolation point.
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract protected double value2(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da,
			double[] d2f_da2);

	/**
	 * Compute the value and partial first-order and second-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(double[] table, double[] df_da, double[] d2f_da2);

	/**
	 * Compute the value and partial first-order and second-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(float[] table, double[] df_da, double[] d2f_da2);

	/**
	 * Compute the value and partial first-order and second-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param table2
	 *            the power table multiplied by 2
	 * @param table3
	 *            the power table multiplied by 3
	 * @param table6
	 *            the power table multiplied by 6
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(double[] table, double[] table2, double[] table3, double[] table6, double[] df_da,
			double[] d2f_da2);

	/**
	 * Compute the value and partial first-order and second-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param table2
	 *            the power table multiplied by 2
	 * @param table3
	 *            the power table multiplied by 3
	 * @param table6
	 *            the power table multiplied by 6
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value(float[] table, float[] table2, float[] table3, float[] table6, double[] df_da,
			double[] d2f_da2);

	/**
	 * Compute the value with no interpolation (i.e. x=0,y=0,z=0).
	 *
	 * @return the interpolated value.
	 */
	abstract public double value000();

	/**
	 * Compute the value and partial first-order derivatives with no interpolation (i.e. x=0,y=0,z=0).
	 *
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value000(double[] df_da);

	/**
	 * Compute the value and partial first-order and second-order derivatives with no interpolation (i.e. x=0,y=0,z=0).
	 *
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	abstract public double value000(double[] df_da, double[] d2f_da2);

	/**
	 * Creates the tricubic function.
	 *
	 * @param a
	 *            the 64 coefficients
	 * @return the custom tricubic function
	 */
	public static CustomTricubicFunction create(double[] a)
	{
		if (a == null || a.length != 64)
			throw new IllegalArgumentException("Require 64 coefficients");
		return new DoubleCustomTricubicFunction(a);
	}

	/**
	 * Creates the tricubic function. The function will store single precision coefficients.
	 *
	 * @param a
	 *            the 64 coefficients
	 * @return the custom tricubic function
	 */
	public static CustomTricubicFunction create(float[] a)
	{
		if (a == null || a.length != 64)
			throw new IllegalArgumentException("Require 64 coefficients");
		return new FloatCustomTricubicFunction(a);
	}

	/**
	 * Scale the coefficients by the given value.
	 *
	 * @param scale
	 *            the scale
	 */
	abstract public void scale(double scale);

	/**
	 * Perform n refinements of a binary search to find the optimum value. 8 vertices of a cube are evaluated per
	 * refinement and the optimum value selected. The bounds of the cube are then reduced by 2.
	 * <p>
	 * The search starts with the bounds at 0,1 for each dimension. This search works because the function is a cubic
	 * polynomial and so the peak at the optimum is closest-in-distance to the closest-in-value bounding point.
	 * <p>
	 * The optimum will be found within error +/- 1/(2^refinements), e.g. 5 refinements will have an error of +/- 1/32.
	 * <p>
	 * An optional tolerance for improvement can be specified. This is applied only if the optimum vertex has changed,
	 * otherwise the value would be the same. If it has changed then the maximum error will be greater than if the
	 * maximum refinements was achieved.
	 *
	 * @param maximum
	 *            Set to true to find the maximum
	 * @param refinements
	 *            the refinements (this is set to 1 if below 1)
	 * @param relativeError
	 *            relative tolerance threshold (set to negative to ignore)
	 * @param absoluteError
	 *            absolute tolerance threshold (set to negative to ignore)
	 * @return [x, y, z, value]
	 */
	public double[] search(boolean maximum, int refinements, double relativeError, double absoluteError)
	{
		if (refinements < 1)
			refinements = 1;

		final boolean checkValue = relativeError > 0 || absoluteError > 0;

		final CubicSplinePosition[] sx = new CubicSplinePosition[] { new CubicSplinePosition(0), new CubicSplinePosition(1) };
		final CubicSplinePosition[] sy = sx.clone();
		final CubicSplinePosition[] sz = sx.clone();
		// 8 cube vertices packed as z*4 + y*2 + x
		final double[] values = new double[8];
		// We can initialise the default node value
		int lastI = 0;
		double lastValue = value000();
		for (;;)
		{
			// Evaluate the 8 flanking positions
			for (int z = 0, i = 0; z < 2; z++)
				for (int y = 0; y < 2; y++)
					for (int x = 0; x < 2; x++, i++)
						// We can skip the value we know
						values[i] = (i == lastI) ? lastValue : value(sx[x], sy[y], sz[z]);

			final int i = (maximum) ? SimpleArrayUtils.findMaxIndex(values) : SimpleArrayUtils.findMinIndex(values);
			final int z = i / 4;
			final int j = i % 4;
			final int y = j / 2;
			final int x = j % 2;

			final double value = values[i];

			boolean converged = (--refinements == 0);
			if (!converged && checkValue && lastI != i)
				// Check convergence on value if the cube vertex has changed.
				// If it hasn't changed then the value will be the same and we continue
				// reducing the cube size.
				converged = areEqual(lastValue, value, absoluteError, relativeError);

			if (converged)
				// Terminate
				return new double[] { sx[x].getX(), sy[y].getX(), sz[z].getX(), value };

			lastI = i;
			lastValue = value;

			// Update bounds
			update(sx, x);
			update(sy, y);
			update(sz, z);
		}
	}

	/**
	 * Check if the pair of values are equal.
	 *
	 * @param p
	 *            Previous
	 * @param c
	 *            Current
	 * @param relativeError
	 *            relative tolerance threshold (set to negative to ignore)
	 * @param absoluteError
	 *            absolute tolerance threshold (set to negative to ignore)
	 * @return True if equal
	 */
	public static boolean areEqual(final double p, final double c, double relativeError, double absoluteError)
	{
		final double difference = Math.abs(p - c);
		if (difference <= absoluteError)
			return true;
		final double size = max(Math.abs(p), Math.abs(c));
		return (difference <= size * relativeError);
	}

	private static double max(final double a, final double b)
	{
		// Ignore NaN
		return (a > b) ? a : b;
	}

	/**
	 * Update the bounds by fixing the last spline position that was optimum and moving the other position to the
	 * midpoint.
	 *
	 * @param s
	 *            the pair of spline positions defining the bounds
	 * @param i
	 *            the index of the optimum
	 */
	private static void update(CubicSplinePosition[] s, int i)
	{
		final double mid = (s[0].getX() + s[1].getX()) / 2;
		// Move opposite bound
		s[(i + 1) % 2] = new CubicSplinePosition(mid);
	}
}
