package gdsc.core.math.interpolation;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * This is an extension of the 
 * org.apache.commons.math3.analysis.interpolation.TricubicFunction
 * 
 * Modifications have been made to allow computation of gradients and computation
 * with pre-computated x,y,z powers.
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

import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * 3D-spline function.
 */
public class CustomTricubicFunction implements TrivariateFunction
{
	/** Number of points. */
	private static final short N = 4;
	/** Number of points - 1. */
	private static final short N_1 = 3;
	/** Number of points - 2. */
	private static final short N_2 = 2;
	/** Coefficients */
	private final double[] a;

	/**
	 * @param aV
	 *            List of spline coefficients.
	 */
	CustomTricubicFunction(double[] aV)
	{
		// Use the table directly
		a = aV;

		//// Copy the table
		//a = new double[64];
		//for (int k = 0, ai = 0; k < N; k++)
		//{
		//	for (int j = 0; j < N; j++)
		//	{
		//		for (int i = 0; i < N; i++)
		//		{
		//			int ii = getIndex(i, j, k);
		//			a[ai++] = aV[ii];
		//		}
		//	}
		//}
	}

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
		return i + N * (j + N * k);
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
	public double value(double x, double y, double z) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
		{
			throw new OutOfRangeException(x, 0, 1);
		}
		if (y < 0 || y > 1)
		{
			throw new OutOfRangeException(y, 0, 1);
		}
		if (z < 0 || z > 1)
		{
			throw new OutOfRangeException(z, 0, 1);
		}

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return value(pX, pY, pZ);
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
		return value(x.p, y.p, z.p);
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
	private double value(final double[] pX, final double[] pY, final double[] pZ)
	{
		double pYpZ;
		double result = 0;

		result += a[0];
		result += a[1] * pX[0];
		result += a[2] * pX[1];
		result += a[3] * pX[2];
		result += a[4] * pY[0];
		result += a[5] * pX[0] * pY[0];
		result += a[6] * pX[1] * pY[0];
		result += a[7] * pX[2] * pY[0];
		result += a[8] * pY[1];
		result += a[9] * pX[0] * pY[1];
		result += a[10] * pX[1] * pY[1];
		result += a[11] * pX[2] * pY[1];
		result += a[12] * pY[2];
		result += a[13] * pX[0] * pY[2];
		result += a[14] * pX[1] * pY[2];
		result += a[15] * pX[2] * pY[2];
		result += a[16] * pZ[0];
		result += a[17] * pX[0] * pZ[0];
		result += a[18] * pX[1] * pZ[0];
		result += a[19] * pX[2] * pZ[0];
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		result += a[21] * pX[0] * pYpZ;
		result += a[22] * pX[1] * pYpZ;
		result += a[23] * pX[2] * pYpZ;
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		result += a[25] * pX[0] * pYpZ;
		result += a[26] * pX[1] * pYpZ;
		result += a[27] * pX[2] * pYpZ;
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		result += a[29] * pX[0] * pYpZ;
		result += a[30] * pX[1] * pYpZ;
		result += a[31] * pX[2] * pYpZ;
		result += a[32] * pZ[1];
		result += a[33] * pX[0] * pZ[1];
		result += a[34] * pX[1] * pZ[1];
		result += a[35] * pX[2] * pZ[1];
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		result += a[37] * pX[0] * pYpZ;
		result += a[38] * pX[1] * pYpZ;
		result += a[39] * pX[2] * pYpZ;
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		result += a[41] * pX[0] * pYpZ;
		result += a[42] * pX[1] * pYpZ;
		result += a[43] * pX[2] * pYpZ;
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		result += a[45] * pX[0] * pYpZ;
		result += a[46] * pX[1] * pYpZ;
		result += a[47] * pX[2] * pYpZ;
		result += a[48] * pZ[2];
		result += a[49] * pX[0] * pZ[2];
		result += a[50] * pX[1] * pZ[2];
		result += a[51] * pX[2] * pZ[2];
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		result += a[53] * pX[0] * pYpZ;
		result += a[54] * pX[1] * pYpZ;
		result += a[55] * pX[2] * pYpZ;
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		result += a[57] * pX[0] * pYpZ;
		result += a[58] * pX[1] * pYpZ;
		result += a[59] * pX[2] * pYpZ;
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		result += a[61] * pX[0] * pYpZ;
		result += a[62] * pX[1] * pYpZ;
		result += a[63] * pX[2] * pYpZ;

		return result;
	}

	/**
	 * Used to create the inline value function
	 * 
	 * @return the function text.
	 */
	static String inlineValue()
	{
		String _pYpZ;
		StringBuilder sb = new StringBuilder();

		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pYpZ = append_pYpZ(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					sb.append(String.format("result += a[%d] * pX[%d] * %s;\n", ai, i, _pYpZ));
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	private static String append_pYpZ(StringBuilder sb, int k, int j)
	{
		String _pYpZ;
		if (k == 0)
		{
			if (j == 0)
			{
				_pYpZ = "1";
			}
			else
			{
				_pYpZ = String.format("pY[%d]", j);
			}
		}
		else if (j == 0)
		{
			_pYpZ = String.format("pZ[%d]", k);
		}
		else
		{
			sb.append(String.format("pYpZ = pY[%d] * pZ[%d];\n", j, k));
			_pYpZ = "pYpZ";
		}
		return _pYpZ;
	}

	private static String finaliseInlineFunction(StringBuilder sb)
	{
		String result = sb.toString();
		// Replace the use of 1 in multiplications
		result = result.replace("pX[0]", "1");
		result = result.replace(" * 1", "");
		result = result.replace(" 1 *", "");
		// We optimise out the need to store 1.0 in the array at pN[0]
		// The power must all be shifted
		for (int i = 0; i < 3; i++)
		{
			String was = String.format("[%d]", i + 1);
			String now = String.format("[%d]", i);
			result = result.replace("pX" + was, "pX" + now);
			result = result.replace("pY" + was, "pY" + now);
			result = result.replace("pZ" + was, "pZ" + now);
		}
		// Simplify compound multiplications
		result = result.replace("2 * 3", "6");
		result = result.replace("3 * 2", "6");
		return result;
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
	public static double[] computePowerTable(double x, double y, double z) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
		{
			throw new OutOfRangeException(x, 0, 1);
		}
		if (y < 0 || y > 1)
		{
			throw new OutOfRangeException(y, 0, 1);
		}
		if (z < 0 || z > 1)
		{
			throw new OutOfRangeException(z, 0, 1);
		}

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
		double pYpZ;
		double[] table = new double[64];

		table[0] = 1;
		table[1] = pX[0];
		table[2] = pX[1];
		table[3] = pX[2];
		table[4] = pY[0];
		table[5] = pX[0] * pY[0];
		table[6] = pX[1] * pY[0];
		table[7] = pX[2] * pY[0];
		table[8] = pY[1];
		table[9] = pX[0] * pY[1];
		table[10] = pX[1] * pY[1];
		table[11] = pX[2] * pY[1];
		table[12] = pY[2];
		table[13] = pX[0] * pY[2];
		table[14] = pX[1] * pY[2];
		table[15] = pX[2] * pY[2];
		table[16] = pZ[0];
		table[17] = pX[0] * pZ[0];
		table[18] = pX[1] * pZ[0];
		table[19] = pX[2] * pZ[0];
		pYpZ = pY[0] * pZ[0];
		table[20] = pYpZ;
		table[21] = pX[0] * pYpZ;
		table[22] = pX[1] * pYpZ;
		table[23] = pX[2] * pYpZ;
		pYpZ = pY[1] * pZ[0];
		table[24] = pYpZ;
		table[25] = pX[0] * pYpZ;
		table[26] = pX[1] * pYpZ;
		table[27] = pX[2] * pYpZ;
		pYpZ = pY[2] * pZ[0];
		table[28] = pYpZ;
		table[29] = pX[0] * pYpZ;
		table[30] = pX[1] * pYpZ;
		table[31] = pX[2] * pYpZ;
		table[32] = pZ[1];
		table[33] = pX[0] * pZ[1];
		table[34] = pX[1] * pZ[1];
		table[35] = pX[2] * pZ[1];
		pYpZ = pY[0] * pZ[1];
		table[36] = pYpZ;
		table[37] = pX[0] * pYpZ;
		table[38] = pX[1] * pYpZ;
		table[39] = pX[2] * pYpZ;
		pYpZ = pY[1] * pZ[1];
		table[40] = pYpZ;
		table[41] = pX[0] * pYpZ;
		table[42] = pX[1] * pYpZ;
		table[43] = pX[2] * pYpZ;
		pYpZ = pY[2] * pZ[1];
		table[44] = pYpZ;
		table[45] = pX[0] * pYpZ;
		table[46] = pX[1] * pYpZ;
		table[47] = pX[2] * pYpZ;
		table[48] = pZ[2];
		table[49] = pX[0] * pZ[2];
		table[50] = pX[1] * pZ[2];
		table[51] = pX[2] * pZ[2];
		pYpZ = pY[0] * pZ[2];
		table[52] = pYpZ;
		table[53] = pX[0] * pYpZ;
		table[54] = pX[1] * pYpZ;
		table[55] = pX[2] * pYpZ;
		pYpZ = pY[1] * pZ[2];
		table[56] = pYpZ;
		table[57] = pX[0] * pYpZ;
		table[58] = pX[1] * pYpZ;
		table[59] = pX[2] * pYpZ;
		pYpZ = pY[2] * pZ[2];
		table[60] = pYpZ;
		table[61] = pX[0] * pYpZ;
		table[62] = pX[1] * pYpZ;
		table[63] = pX[2] * pYpZ;

		return table;
	}

	/**
	 * Used to create the inline power table function
	 * 
	 * @return the function text.
	 */
	static String inlineComputePowerTable()
	{
		String _pYpZ;
		StringBuilder sb = new StringBuilder();

		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pYpZ = append_pYpZ(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					sb.append(String.format("table[%d] = pX[%d] * %s;\n", ai, i, _pYpZ));
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	/**
	 * Get the value using a pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @return the interpolated value.
	 */
	public double value(double[] table)
	{
		double result = 0;
		for (int ai = 0; ai < 64; ai++)
		{
			result += a[ai] * table[ai];
		}
		return result;
	}

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
		{
			throw new OutOfRangeException(x, 0, 1);
		}
		if (y < 0 || y > 1)
		{
			throw new OutOfRangeException(y, 0, 1);
		}
		if (z < 0 || z > 1)
		{
			throw new OutOfRangeException(z, 0, 1);
		}

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return value(pX, pY, pZ, df_da);
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
		double value = value(x.p, y.p, z.p, df_da);
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
	private double value(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da)
	{
		double pYpZ;
		double pXpYpZ;
		double result = 0;
		df_da[0] = 0;
		df_da[1] = 0;
		df_da[2] = 0;

		result += a[0];
		df_da[0] += a[1];
		df_da[1] += a[4];
		df_da[2] += a[16];
		pXpYpZ = pX[0];
		result += a[1] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[2];
		df_da[1] += pXpYpZ * a[5];
		df_da[2] += pXpYpZ * a[17];
		pXpYpZ = pX[1];
		result += a[2] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[3];
		df_da[1] += pXpYpZ * a[6];
		df_da[2] += pXpYpZ * a[18];
		pXpYpZ = pX[2];
		result += a[3] * pXpYpZ;
		df_da[1] += pXpYpZ * a[7];
		df_da[2] += pXpYpZ * a[19];
		result += a[4] * pY[0];
		df_da[0] += pY[0] * a[5];
		df_da[1] += 2 * pY[0] * a[8];
		df_da[2] += pY[0] * a[20];
		pXpYpZ = pX[0] * pY[0];
		result += a[5] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[6];
		df_da[1] += 2 * pXpYpZ * a[9];
		df_da[2] += pXpYpZ * a[21];
		pXpYpZ = pX[1] * pY[0];
		result += a[6] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[7];
		df_da[1] += 2 * pXpYpZ * a[10];
		df_da[2] += pXpYpZ * a[22];
		pXpYpZ = pX[2] * pY[0];
		result += a[7] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[11];
		df_da[2] += pXpYpZ * a[23];
		result += a[8] * pY[1];
		df_da[0] += pY[1] * a[9];
		df_da[1] += 3 * pY[1] * a[12];
		df_da[2] += pY[1] * a[24];
		pXpYpZ = pX[0] * pY[1];
		result += a[9] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[10];
		df_da[1] += 3 * pXpYpZ * a[13];
		df_da[2] += pXpYpZ * a[25];
		pXpYpZ = pX[1] * pY[1];
		result += a[10] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[11];
		df_da[1] += 3 * pXpYpZ * a[14];
		df_da[2] += pXpYpZ * a[26];
		pXpYpZ = pX[2] * pY[1];
		result += a[11] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[15];
		df_da[2] += pXpYpZ * a[27];
		result += a[12] * pY[2];
		df_da[0] += pY[2] * a[13];
		df_da[2] += pY[2] * a[28];
		pXpYpZ = pX[0] * pY[2];
		result += a[13] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[14];
		df_da[2] += pXpYpZ * a[29];
		pXpYpZ = pX[1] * pY[2];
		result += a[14] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[15];
		df_da[2] += pXpYpZ * a[30];
		pXpYpZ = pX[2] * pY[2];
		result += a[15] * pXpYpZ;
		df_da[2] += pXpYpZ * a[31];
		result += a[16] * pZ[0];
		df_da[0] += pZ[0] * a[17];
		df_da[1] += pZ[0] * a[20];
		df_da[2] += 2 * pZ[0] * a[32];
		pXpYpZ = pX[0] * pZ[0];
		result += a[17] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[18];
		df_da[1] += pXpYpZ * a[21];
		df_da[2] += 2 * pXpYpZ * a[33];
		pXpYpZ = pX[1] * pZ[0];
		result += a[18] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[19];
		df_da[1] += pXpYpZ * a[22];
		df_da[2] += 2 * pXpYpZ * a[34];
		pXpYpZ = pX[2] * pZ[0];
		result += a[19] * pXpYpZ;
		df_da[1] += pXpYpZ * a[23];
		df_da[2] += 2 * pXpYpZ * a[35];
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		df_da[0] += pYpZ * a[21];
		df_da[1] += 2 * pYpZ * a[24];
		df_da[2] += 2 * pYpZ * a[36];
		pXpYpZ = pX[0] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[22];
		df_da[1] += 2 * pXpYpZ * a[25];
		df_da[2] += 2 * pXpYpZ * a[37];
		pXpYpZ = pX[1] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[23];
		df_da[1] += 2 * pXpYpZ * a[26];
		df_da[2] += 2 * pXpYpZ * a[38];
		pXpYpZ = pX[2] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[27];
		df_da[2] += 2 * pXpYpZ * a[39];
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		df_da[0] += pYpZ * a[25];
		df_da[1] += 3 * pYpZ * a[28];
		df_da[2] += 2 * pYpZ * a[40];
		pXpYpZ = pX[0] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[26];
		df_da[1] += 3 * pXpYpZ * a[29];
		df_da[2] += 2 * pXpYpZ * a[41];
		pXpYpZ = pX[1] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[27];
		df_da[1] += 3 * pXpYpZ * a[30];
		df_da[2] += 2 * pXpYpZ * a[42];
		pXpYpZ = pX[2] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[43];
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		df_da[0] += pYpZ * a[29];
		df_da[2] += 2 * pYpZ * a[44];
		pXpYpZ = pX[0] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[30];
		df_da[2] += 2 * pXpYpZ * a[45];
		pXpYpZ = pX[1] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[46];
		pXpYpZ = pX[2] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += 2 * pXpYpZ * a[47];
		result += a[32] * pZ[1];
		df_da[0] += pZ[1] * a[33];
		df_da[1] += pZ[1] * a[36];
		df_da[2] += 3 * pZ[1] * a[48];
		pXpYpZ = pX[0] * pZ[1];
		result += a[33] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[34];
		df_da[1] += pXpYpZ * a[37];
		df_da[2] += 3 * pXpYpZ * a[49];
		pXpYpZ = pX[1] * pZ[1];
		result += a[34] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[35];
		df_da[1] += pXpYpZ * a[38];
		df_da[2] += 3 * pXpYpZ * a[50];
		pXpYpZ = pX[2] * pZ[1];
		result += a[35] * pXpYpZ;
		df_da[1] += pXpYpZ * a[39];
		df_da[2] += 3 * pXpYpZ * a[51];
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		df_da[0] += pYpZ * a[37];
		df_da[1] += 2 * pYpZ * a[40];
		df_da[2] += 3 * pYpZ * a[52];
		pXpYpZ = pX[0] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[38];
		df_da[1] += 2 * pXpYpZ * a[41];
		df_da[2] += 3 * pXpYpZ * a[53];
		pXpYpZ = pX[1] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[39];
		df_da[1] += 2 * pXpYpZ * a[42];
		df_da[2] += 3 * pXpYpZ * a[54];
		pXpYpZ = pX[2] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[43];
		df_da[2] += 3 * pXpYpZ * a[55];
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		df_da[0] += pYpZ * a[41];
		df_da[1] += 3 * pYpZ * a[44];
		df_da[2] += 3 * pYpZ * a[56];
		pXpYpZ = pX[0] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[42];
		df_da[1] += 3 * pXpYpZ * a[45];
		df_da[2] += 3 * pXpYpZ * a[57];
		pXpYpZ = pX[1] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[43];
		df_da[1] += 3 * pXpYpZ * a[46];
		df_da[2] += 3 * pXpYpZ * a[58];
		pXpYpZ = pX[2] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[59];
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		df_da[0] += pYpZ * a[45];
		df_da[2] += 3 * pYpZ * a[60];
		pXpYpZ = pX[0] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[46];
		df_da[2] += 3 * pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += 3 * pXpYpZ * a[63];
		result += a[48] * pZ[2];
		df_da[0] += pZ[2] * a[49];
		df_da[1] += pZ[2] * a[52];
		pXpYpZ = pX[0] * pZ[2];
		result += a[49] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[50];
		df_da[1] += pXpYpZ * a[53];
		pXpYpZ = pX[1] * pZ[2];
		result += a[50] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[51];
		df_da[1] += pXpYpZ * a[54];
		pXpYpZ = pX[2] * pZ[2];
		result += a[51] * pXpYpZ;
		df_da[1] += pXpYpZ * a[55];
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		df_da[0] += pYpZ * a[53];
		df_da[1] += 2 * pYpZ * a[56];
		pXpYpZ = pX[0] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[54];
		df_da[1] += 2 * pXpYpZ * a[57];
		pXpYpZ = pX[1] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[55];
		df_da[1] += 2 * pXpYpZ * a[58];
		pXpYpZ = pX[2] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[59];
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		df_da[0] += pYpZ * a[57];
		df_da[1] += 3 * pYpZ * a[60];
		pXpYpZ = pX[0] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[58];
		df_da[1] += 3 * pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[59];
		df_da[1] += 3 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[63];
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		df_da[0] += pYpZ * a[61];
		pXpYpZ = pX[0] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[62];
		pXpYpZ = pX[1] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[63];
		pXpYpZ = pX[2] * pYpZ;
		result += a[63] * pXpYpZ;

		return result;
	}

	/**
	 * Used to create the inline value function for first-order gradients
	 * 
	 * @return the function text.
	 */
	static String inlineValue1()
	{
		String _pYpZ;
		String _pXpYpZ;
		StringBuilder sb = new StringBuilder();

		// Gradients are described in:
		// Babcock & Zhuang (2017) 
		// Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
		// Scientific Reports 7, Article number: 552
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pYpZ = append_pYpZ(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					_pXpYpZ = append_pXpYpZ(sb, _pYpZ, i);

					//@formatter:off
					sb.append(String.format("result += a[%d] * %s;\n", ai, _pXpYpZ));
					if (i < N_1)
						sb.append(String.format("df_da[0] += %d * %s * a[%d];\n", i+1, _pXpYpZ, getIndex(i+1, j, k)));
					if (j < N_1)
						sb.append(String.format("df_da[1] += %d * %s * a[%d];\n", j+1, _pXpYpZ, getIndex(i, j+1, k)));
					if (k < N_1)
						sb.append(String.format("df_da[2] += %d * %s * a[%d];\n", k+1, _pXpYpZ, getIndex(i, j, k+1)));
					//@formatter:on

					// Formal computation
					//pXpYpZ = pX[i] * pY[j] * pZ[k];
					//result += a[ai] * pXpYpZ;
					//if (i < N_1)
					//	df_da[0] += (i+1) * a[getIndex(i+1, j, k)] * pXpYpZ;
					//if (j < N_1)
					//	df_da[1] += (j+1) * a[getIndex(i, j+1, k)] * pXpYpZ;
					//if (k < N_1)
					//	df_da[2] += (k+1) * a[getIndex(i, j, k+1)] * pXpYpZ;
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	private static String append_pXpYpZ(StringBuilder sb, String _pYpZ, int i)
	{
		String _pXpYpZ;
		if (i == 0)
		{
			_pXpYpZ = _pYpZ;
		}
		else
		{
			sb.append(String.format("pXpYpZ = pX[%d] * %s;\n", i, _pYpZ));
			_pXpYpZ = "pXpYpZ";
		}
		return _pXpYpZ;
	}

	/**
	 * Compute the power table for computation of first order derivatives.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power tables.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public static double[][] computeFirstOrderPowerTables(double x, double y, double z) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
		{
			throw new OutOfRangeException(x, 0, 1);
		}
		if (y < 0 || y > 1)
		{
			throw new OutOfRangeException(y, 0, 1);
		}
		if (z < 0 || z > 1)
		{
			throw new OutOfRangeException(z, 0, 1);
		}

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return computeFirstOrderPowerTables(pX, pY, pZ);
	}

	/**
	 * Compute the power table for computation of first order derivatives.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power tables.
	 */
	public static double[][] computeFirstOrderPowerTables(CubicSplinePosition x, CubicSplinePosition y,
			CubicSplinePosition z)
	{
		return computeFirstOrderPowerTables(x.p, y.p, z.p);
	}

	/**
	 * Compute the power table for computation of first order derivatives.
	 *
	 * @param pX
	 *            x-coordinate powers of the interpolation point.
	 * @param pY
	 *            y-coordinate powers of the interpolation point.
	 * @param pZ
	 *            z-coordinate powers of the interpolation point.
	 * @return the power tables.
	 */
	private static double[][] computeFirstOrderPowerTables(final double[] pX, final double[] pY, final double[] pZ)
	{
		double pYpZ;
		double pXpYpZ;
		final double[] table = new double[64];
		final double[] table_df_dx = new double[64];
		final double[] table_df_dy = new double[48];
		final double[] table_df_dz = new double[48];

		table[0] = 1;
		table_df_dx[0] = 1;
		table_df_dy[0] = 1;
		table_df_dz[0] = 1;
		pXpYpZ = pX[0];
		table[1] = pXpYpZ;
		table_df_dx[1] = 2 * pXpYpZ;
		table_df_dy[1] = pXpYpZ;
		table_df_dz[1] = pXpYpZ;
		pXpYpZ = pX[1];
		table[2] = pXpYpZ;
		table_df_dx[2] = 3 * pXpYpZ;
		table_df_dy[2] = pXpYpZ;
		table_df_dz[2] = pXpYpZ;
		pXpYpZ = pX[2];
		table[3] = pXpYpZ;
		table_df_dy[3] = pXpYpZ;
		table_df_dz[3] = pXpYpZ;
		table[4] = pY[0];
		table_df_dx[3] = pY[0];
		table_df_dy[4] = 2 * pY[0];
		table_df_dz[4] = pY[0];
		pXpYpZ = pX[0] * pY[0];
		table[5] = pXpYpZ;
		table_df_dx[4] = 2 * pXpYpZ;
		table_df_dy[5] = 2 * pXpYpZ;
		table_df_dz[5] = pXpYpZ;
		pXpYpZ = pX[1] * pY[0];
		table[6] = pXpYpZ;
		table_df_dx[5] = 3 * pXpYpZ;
		table_df_dy[6] = 2 * pXpYpZ;
		table_df_dz[6] = pXpYpZ;
		pXpYpZ = pX[2] * pY[0];
		table[7] = pXpYpZ;
		table_df_dy[7] = 2 * pXpYpZ;
		table_df_dz[7] = pXpYpZ;
		table[8] = pY[1];
		table_df_dx[6] = pY[1];
		table_df_dy[8] = 3 * pY[1];
		table_df_dz[8] = pY[1];
		pXpYpZ = pX[0] * pY[1];
		table[9] = pXpYpZ;
		table_df_dx[7] = 2 * pXpYpZ;
		table_df_dy[9] = 3 * pXpYpZ;
		table_df_dz[9] = pXpYpZ;
		pXpYpZ = pX[1] * pY[1];
		table[10] = pXpYpZ;
		table_df_dx[8] = 3 * pXpYpZ;
		table_df_dy[10] = 3 * pXpYpZ;
		table_df_dz[10] = pXpYpZ;
		pXpYpZ = pX[2] * pY[1];
		table[11] = pXpYpZ;
		table_df_dy[11] = 3 * pXpYpZ;
		table_df_dz[11] = pXpYpZ;
		table[12] = pY[2];
		table_df_dx[9] = pY[2];
		table_df_dz[12] = pY[2];
		pXpYpZ = pX[0] * pY[2];
		table[13] = pXpYpZ;
		table_df_dx[10] = 2 * pXpYpZ;
		table_df_dz[13] = pXpYpZ;
		pXpYpZ = pX[1] * pY[2];
		table[14] = pXpYpZ;
		table_df_dx[11] = 3 * pXpYpZ;
		table_df_dz[14] = pXpYpZ;
		pXpYpZ = pX[2] * pY[2];
		table[15] = pXpYpZ;
		table_df_dz[15] = pXpYpZ;
		table[16] = pZ[0];
		table_df_dx[12] = pZ[0];
		table_df_dy[12] = pZ[0];
		table_df_dz[16] = 2 * pZ[0];
		pXpYpZ = pX[0] * pZ[0];
		table[17] = pXpYpZ;
		table_df_dx[13] = 2 * pXpYpZ;
		table_df_dy[13] = pXpYpZ;
		table_df_dz[17] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pZ[0];
		table[18] = pXpYpZ;
		table_df_dx[14] = 3 * pXpYpZ;
		table_df_dy[14] = pXpYpZ;
		table_df_dz[18] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pZ[0];
		table[19] = pXpYpZ;
		table_df_dy[15] = pXpYpZ;
		table_df_dz[19] = 2 * pXpYpZ;
		pYpZ = pY[0] * pZ[0];
		table[20] = pYpZ;
		table_df_dx[15] = pYpZ;
		table_df_dy[16] = 2 * pYpZ;
		table_df_dz[20] = 2 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[21] = pXpYpZ;
		table_df_dx[16] = 2 * pXpYpZ;
		table_df_dy[17] = 2 * pXpYpZ;
		table_df_dz[21] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[22] = pXpYpZ;
		table_df_dx[17] = 3 * pXpYpZ;
		table_df_dy[18] = 2 * pXpYpZ;
		table_df_dz[22] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[23] = pXpYpZ;
		table_df_dy[19] = 2 * pXpYpZ;
		table_df_dz[23] = 2 * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		table[24] = pYpZ;
		table_df_dx[18] = pYpZ;
		table_df_dy[20] = 3 * pYpZ;
		table_df_dz[24] = 2 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[25] = pXpYpZ;
		table_df_dx[19] = 2 * pXpYpZ;
		table_df_dy[21] = 3 * pXpYpZ;
		table_df_dz[25] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[26] = pXpYpZ;
		table_df_dx[20] = 3 * pXpYpZ;
		table_df_dy[22] = 3 * pXpYpZ;
		table_df_dz[26] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[27] = pXpYpZ;
		table_df_dy[23] = 3 * pXpYpZ;
		table_df_dz[27] = 2 * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		table[28] = pYpZ;
		table_df_dx[21] = pYpZ;
		table_df_dz[28] = 2 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[29] = pXpYpZ;
		table_df_dx[22] = 2 * pXpYpZ;
		table_df_dz[29] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[30] = pXpYpZ;
		table_df_dx[23] = 3 * pXpYpZ;
		table_df_dz[30] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[31] = pXpYpZ;
		table_df_dz[31] = 2 * pXpYpZ;
		table[32] = pZ[1];
		table_df_dx[24] = pZ[1];
		table_df_dy[24] = pZ[1];
		table_df_dz[32] = 3 * pZ[1];
		pXpYpZ = pX[0] * pZ[1];
		table[33] = pXpYpZ;
		table_df_dx[25] = 2 * pXpYpZ;
		table_df_dy[25] = pXpYpZ;
		table_df_dz[33] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pZ[1];
		table[34] = pXpYpZ;
		table_df_dx[26] = 3 * pXpYpZ;
		table_df_dy[26] = pXpYpZ;
		table_df_dz[34] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pZ[1];
		table[35] = pXpYpZ;
		table_df_dy[27] = pXpYpZ;
		table_df_dz[35] = 3 * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		table[36] = pYpZ;
		table_df_dx[27] = pYpZ;
		table_df_dy[28] = 2 * pYpZ;
		table_df_dz[36] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[37] = pXpYpZ;
		table_df_dx[28] = 2 * pXpYpZ;
		table_df_dy[29] = 2 * pXpYpZ;
		table_df_dz[37] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[38] = pXpYpZ;
		table_df_dx[29] = 3 * pXpYpZ;
		table_df_dy[30] = 2 * pXpYpZ;
		table_df_dz[38] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[39] = pXpYpZ;
		table_df_dy[31] = 2 * pXpYpZ;
		table_df_dz[39] = 3 * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		table[40] = pYpZ;
		table_df_dx[30] = pYpZ;
		table_df_dy[32] = 3 * pYpZ;
		table_df_dz[40] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[41] = pXpYpZ;
		table_df_dx[31] = 2 * pXpYpZ;
		table_df_dy[33] = 3 * pXpYpZ;
		table_df_dz[41] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[42] = pXpYpZ;
		table_df_dx[32] = 3 * pXpYpZ;
		table_df_dy[34] = 3 * pXpYpZ;
		table_df_dz[42] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[43] = pXpYpZ;
		table_df_dy[35] = 3 * pXpYpZ;
		table_df_dz[43] = 3 * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		table[44] = pYpZ;
		table_df_dx[33] = pYpZ;
		table_df_dz[44] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[45] = pXpYpZ;
		table_df_dx[34] = 2 * pXpYpZ;
		table_df_dz[45] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[46] = pXpYpZ;
		table_df_dx[35] = 3 * pXpYpZ;
		table_df_dz[46] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[47] = pXpYpZ;
		table_df_dz[47] = 3 * pXpYpZ;
		table[48] = pZ[2];
		table_df_dx[36] = pZ[2];
		table_df_dy[36] = pZ[2];
		pXpYpZ = pX[0] * pZ[2];
		table[49] = pXpYpZ;
		table_df_dx[37] = 2 * pXpYpZ;
		table_df_dy[37] = pXpYpZ;
		pXpYpZ = pX[1] * pZ[2];
		table[50] = pXpYpZ;
		table_df_dx[38] = 3 * pXpYpZ;
		table_df_dy[38] = pXpYpZ;
		pXpYpZ = pX[2] * pZ[2];
		table[51] = pXpYpZ;
		table_df_dy[39] = pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		table[52] = pYpZ;
		table_df_dx[39] = pYpZ;
		table_df_dy[40] = 2 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[53] = pXpYpZ;
		table_df_dx[40] = 2 * pXpYpZ;
		table_df_dy[41] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[54] = pXpYpZ;
		table_df_dx[41] = 3 * pXpYpZ;
		table_df_dy[42] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[55] = pXpYpZ;
		table_df_dy[43] = 2 * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		table[56] = pYpZ;
		table_df_dx[42] = pYpZ;
		table_df_dy[44] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[57] = pXpYpZ;
		table_df_dx[43] = 2 * pXpYpZ;
		table_df_dy[45] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[58] = pXpYpZ;
		table_df_dx[44] = 3 * pXpYpZ;
		table_df_dy[46] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[59] = pXpYpZ;
		table_df_dy[47] = 3 * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		table[60] = pYpZ;
		table_df_dx[45] = pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[61] = pXpYpZ;
		table_df_dx[46] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[62] = pXpYpZ;
		table_df_dx[47] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[63] = pXpYpZ;

		return new double[][] { table, table_df_dx, table_df_dy, table_df_dz };
	}

	/**
	 * Used to create the inline power table function for first-order gradients
	 * 
	 * @return the function text.
	 */
	static String inlineComputePowerTable1()
	{
		String _pYpZ, _pXpYpZ;
		StringBuilder sb = new StringBuilder();

		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pYpZ = append_pYpZ(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					_pXpYpZ = append_pXpYpZ(sb, _pYpZ, i);

					sb.append(String.format("table[%d] = %s;\n", ai, _pXpYpZ));
					if (i < N_1)
						sb.append(String.format("table_df_dx[%d] = %d * %s;\n", x1++, i + 1, _pXpYpZ));
					if (j < N_1)
						sb.append(String.format("table_df_dy[%d] = %d * %s;\n", y1++, j + 1, _pXpYpZ));
					if (k < N_1)
						sb.append(String.format("table_df_dz[%d] = %d * %s;\n", z1++, k + 1, _pXpYpZ));
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	/**
	 * Compute the value and partial first-order derivatives using pre-computed power tables.
	 *
	 * @param tables
	 *            the power tables
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	public double value(double[][] tables, double[] df_da)
	{
		final double[] table = tables[0];
		final double[] table_df_dx = tables[1];
		final double[] table_df_dy = tables[2];
		final double[] table_df_dz = tables[3];

		double result = 0;
		df_da[0] = 0;
		df_da[1] = 0;
		df_da[2] = 0;

		result = a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
				a[5] * table[5] + a[6] * table[6] + a[7] * table[7] + a[8] * table[8] + a[9] * table[9] +
				a[10] * table[10] + a[11] * table[11] + a[12] * table[12] + a[13] * table[13] + a[14] * table[14] +
				a[15] * table[15] + a[16] * table[16] + a[17] * table[17] + a[18] * table[18] + a[19] * table[19] +
				a[20] * table[20] + a[21] * table[21] + a[22] * table[22] + a[23] * table[23] + a[24] * table[24] +
				a[25] * table[25] + a[26] * table[26] + a[27] * table[27] + a[28] * table[28] + a[29] * table[29] +
				a[30] * table[30] + a[31] * table[31] + a[32] * table[32] + a[33] * table[33] + a[34] * table[34] +
				a[35] * table[35] + a[36] * table[36] + a[37] * table[37] + a[38] * table[38] + a[39] * table[39] +
				a[40] * table[40] + a[41] * table[41] + a[42] * table[42] + a[43] * table[43] + a[44] * table[44] +
				a[45] * table[45] + a[46] * table[46] + a[47] * table[47] + a[48] * table[48] + a[49] * table[49] +
				a[50] * table[50] + a[51] * table[51] + a[52] * table[52] + a[53] * table[53] + a[54] * table[54] +
				a[55] * table[55] + a[56] * table[56] + a[57] * table[57] + a[58] * table[58] + a[59] * table[59] +
				a[60] * table[60] + a[61] * table[61] + a[62] * table[62] + a[63] * table[63];
		df_da[0] = a[1] * table_df_dx[0] + a[2] * table_df_dx[1] + a[3] * table_df_dx[2] + a[5] * table_df_dx[3] +
				a[6] * table_df_dx[4] + a[7] * table_df_dx[5] + a[9] * table_df_dx[6] + a[10] * table_df_dx[7] +
				a[11] * table_df_dx[8] + a[13] * table_df_dx[9] + a[14] * table_df_dx[10] + a[15] * table_df_dx[11] +
				a[17] * table_df_dx[12] + a[18] * table_df_dx[13] + a[19] * table_df_dx[14] + a[21] * table_df_dx[15] +
				a[22] * table_df_dx[16] + a[23] * table_df_dx[17] + a[25] * table_df_dx[18] + a[26] * table_df_dx[19] +
				a[27] * table_df_dx[20] + a[29] * table_df_dx[21] + a[30] * table_df_dx[22] + a[31] * table_df_dx[23] +
				a[33] * table_df_dx[24] + a[34] * table_df_dx[25] + a[35] * table_df_dx[26] + a[37] * table_df_dx[27] +
				a[38] * table_df_dx[28] + a[39] * table_df_dx[29] + a[41] * table_df_dx[30] + a[42] * table_df_dx[31] +
				a[43] * table_df_dx[32] + a[45] * table_df_dx[33] + a[46] * table_df_dx[34] + a[47] * table_df_dx[35] +
				a[49] * table_df_dx[36] + a[50] * table_df_dx[37] + a[51] * table_df_dx[38] + a[53] * table_df_dx[39] +
				a[54] * table_df_dx[40] + a[55] * table_df_dx[41] + a[57] * table_df_dx[42] + a[58] * table_df_dx[43] +
				a[59] * table_df_dx[44] + a[61] * table_df_dx[45] + a[62] * table_df_dx[46] + a[63] * table_df_dx[47];
		df_da[1] = a[4] * table_df_dy[0] + a[5] * table_df_dy[1] + a[6] * table_df_dy[2] + a[7] * table_df_dy[3] +
				a[8] * table_df_dy[4] + a[9] * table_df_dy[5] + a[10] * table_df_dy[6] + a[11] * table_df_dy[7] +
				a[12] * table_df_dy[8] + a[13] * table_df_dy[9] + a[14] * table_df_dy[10] + a[15] * table_df_dy[11] +
				a[20] * table_df_dy[12] + a[21] * table_df_dy[13] + a[22] * table_df_dy[14] + a[23] * table_df_dy[15] +
				a[24] * table_df_dy[16] + a[25] * table_df_dy[17] + a[26] * table_df_dy[18] + a[27] * table_df_dy[19] +
				a[28] * table_df_dy[20] + a[29] * table_df_dy[21] + a[30] * table_df_dy[22] + a[31] * table_df_dy[23] +
				a[36] * table_df_dy[24] + a[37] * table_df_dy[25] + a[38] * table_df_dy[26] + a[39] * table_df_dy[27] +
				a[40] * table_df_dy[28] + a[41] * table_df_dy[29] + a[42] * table_df_dy[30] + a[43] * table_df_dy[31] +
				a[44] * table_df_dy[32] + a[45] * table_df_dy[33] + a[46] * table_df_dy[34] + a[47] * table_df_dy[35] +
				a[52] * table_df_dy[36] + a[53] * table_df_dy[37] + a[54] * table_df_dy[38] + a[55] * table_df_dy[39] +
				a[56] * table_df_dy[40] + a[57] * table_df_dy[41] + a[58] * table_df_dy[42] + a[59] * table_df_dy[43] +
				a[60] * table_df_dy[44] + a[61] * table_df_dy[45] + a[62] * table_df_dy[46] + a[63] * table_df_dy[47];
		df_da[2] = a[16] * table_df_dz[0] + a[17] * table_df_dz[1] + a[18] * table_df_dz[2] + a[19] * table_df_dz[3] +
				a[20] * table_df_dz[4] + a[21] * table_df_dz[5] + a[22] * table_df_dz[6] + a[23] * table_df_dz[7] +
				a[24] * table_df_dz[8] + a[25] * table_df_dz[9] + a[26] * table_df_dz[10] + a[27] * table_df_dz[11] +
				a[28] * table_df_dz[12] + a[29] * table_df_dz[13] + a[30] * table_df_dz[14] + a[31] * table_df_dz[15] +
				a[32] * table_df_dz[16] + a[33] * table_df_dz[17] + a[34] * table_df_dz[18] + a[35] * table_df_dz[19] +
				a[36] * table_df_dz[20] + a[37] * table_df_dz[21] + a[38] * table_df_dz[22] + a[39] * table_df_dz[23] +
				a[40] * table_df_dz[24] + a[41] * table_df_dz[25] + a[42] * table_df_dz[26] + a[43] * table_df_dz[27] +
				a[44] * table_df_dz[28] + a[45] * table_df_dz[29] + a[46] * table_df_dz[30] + a[47] * table_df_dz[31] +
				a[48] * table_df_dz[32] + a[49] * table_df_dz[33] + a[50] * table_df_dz[34] + a[51] * table_df_dz[35] +
				a[52] * table_df_dz[36] + a[53] * table_df_dz[37] + a[54] * table_df_dz[38] + a[55] * table_df_dz[39] +
				a[56] * table_df_dz[40] + a[57] * table_df_dz[41] + a[58] * table_df_dz[42] + a[59] * table_df_dz[43] +
				a[60] * table_df_dz[44] + a[61] * table_df_dz[45] + a[62] * table_df_dz[46] + a[63] * table_df_dz[47];

		return result;
	}

	/**
	 * Used to create the inline value function for first-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValue1WithPowerTable()
	{
		StringBuilder sb = new StringBuilder();
		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		// @formatter:off
		sb.append("result =");
		for (int k = 0, ai = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++, ai++)
					sb.append(String.format("+ a[%d] * table[%d]\n", ai, ai));
		sb.append(";\n");
		sb.append("df_da[0] =");
		for (int k = 0, x1 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_1)
						sb.append(String.format("+ a[%d] * table_df_dx[%d]\n", getIndex(i+1, j, k), x1++));
		sb.append(";\n");
		sb.append("df_da[1] =");
		for (int k = 0, y1 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_1)
						sb.append(String.format("+ a[%d] * table_df_dy[%d]\n", getIndex(i, j+1, k), y1++));
		sb.append(";\n");
		sb.append("df_da[2] =");
		for (int k = 0, z1 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_1)
						sb.append(String.format("+ a[%d] * table_df_dz[%d]\n", getIndex(i, j, k+1), z1++));
		sb.append(";\n");
		// @formatter:on	
		return finaliseInlinePowerTableFunction(sb);
	}

	private static String finaliseInlinePowerTableFunction(StringBuilder sb)
	{
		String result = sb.toString();
		result = result.replace("=+", "=");
		return result;
	}

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
	 * @return the interpolated value.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public double value(double x, double y, double z, double[] df_da, double[] d2f_da2) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
		{
			throw new OutOfRangeException(x, 0, 1);
		}
		if (y < 0 || y > 1)
		{
			throw new OutOfRangeException(y, 0, 1);
		}
		if (z < 0 || z > 1)
		{
			throw new OutOfRangeException(z, 0, 1);
		}

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return value(pX, pY, pZ, df_da, d2f_da2);
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
		double value = value(x.p, y.p, z.p, df_da, d2f_da2);
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
	private double value(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da,
			double[] d2f_da2)
	{
		double pYpZ;
		double pXpYpZ;
		double result = 0;
		df_da[0] = 0;
		df_da[1] = 0;
		df_da[2] = 0;
		d2f_da2[0] = 0;
		d2f_da2[1] = 0;
		d2f_da2[2] = 0;

		result += a[0];
		df_da[0] += a[1];
		d2f_da2[0] += 2 * a[2];
		df_da[1] += a[4];
		d2f_da2[1] += 2 * a[8];
		df_da[2] += a[16];
		d2f_da2[2] += 2 * a[32];
		pXpYpZ = pX[0];
		result += a[1] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[2];
		d2f_da2[0] += 6 * pXpYpZ * a[3];
		df_da[1] += pXpYpZ * a[5];
		d2f_da2[1] += 2 * pXpYpZ * a[9];
		df_da[2] += pXpYpZ * a[17];
		d2f_da2[2] += 2 * pXpYpZ * a[33];
		pXpYpZ = pX[1];
		result += a[2] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[3];
		df_da[1] += pXpYpZ * a[6];
		d2f_da2[1] += 2 * pXpYpZ * a[10];
		df_da[2] += pXpYpZ * a[18];
		d2f_da2[2] += 2 * pXpYpZ * a[34];
		pXpYpZ = pX[2];
		result += a[3] * pXpYpZ;
		df_da[1] += pXpYpZ * a[7];
		d2f_da2[1] += 2 * pXpYpZ * a[11];
		df_da[2] += pXpYpZ * a[19];
		d2f_da2[2] += 2 * pXpYpZ * a[35];
		result += a[4] * pY[0];
		df_da[0] += pY[0] * a[5];
		d2f_da2[0] += 2 * pY[0] * a[6];
		df_da[1] += 2 * pY[0] * a[8];
		d2f_da2[1] += 6 * pY[0] * a[12];
		df_da[2] += pY[0] * a[20];
		d2f_da2[2] += 2 * pY[0] * a[36];
		pXpYpZ = pX[0] * pY[0];
		result += a[5] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[6];
		d2f_da2[0] += 6 * pXpYpZ * a[7];
		df_da[1] += 2 * pXpYpZ * a[9];
		d2f_da2[1] += 6 * pXpYpZ * a[13];
		df_da[2] += pXpYpZ * a[21];
		d2f_da2[2] += 2 * pXpYpZ * a[37];
		pXpYpZ = pX[1] * pY[0];
		result += a[6] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[7];
		df_da[1] += 2 * pXpYpZ * a[10];
		d2f_da2[1] += 6 * pXpYpZ * a[14];
		df_da[2] += pXpYpZ * a[22];
		d2f_da2[2] += 2 * pXpYpZ * a[38];
		pXpYpZ = pX[2] * pY[0];
		result += a[7] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[11];
		d2f_da2[1] += 6 * pXpYpZ * a[15];
		df_da[2] += pXpYpZ * a[23];
		d2f_da2[2] += 2 * pXpYpZ * a[39];
		result += a[8] * pY[1];
		df_da[0] += pY[1] * a[9];
		d2f_da2[0] += 2 * pY[1] * a[10];
		df_da[1] += 3 * pY[1] * a[12];
		df_da[2] += pY[1] * a[24];
		d2f_da2[2] += 2 * pY[1] * a[40];
		pXpYpZ = pX[0] * pY[1];
		result += a[9] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[10];
		d2f_da2[0] += 6 * pXpYpZ * a[11];
		df_da[1] += 3 * pXpYpZ * a[13];
		df_da[2] += pXpYpZ * a[25];
		d2f_da2[2] += 2 * pXpYpZ * a[41];
		pXpYpZ = pX[1] * pY[1];
		result += a[10] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[11];
		df_da[1] += 3 * pXpYpZ * a[14];
		df_da[2] += pXpYpZ * a[26];
		d2f_da2[2] += 2 * pXpYpZ * a[42];
		pXpYpZ = pX[2] * pY[1];
		result += a[11] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[15];
		df_da[2] += pXpYpZ * a[27];
		d2f_da2[2] += 2 * pXpYpZ * a[43];
		result += a[12] * pY[2];
		df_da[0] += pY[2] * a[13];
		d2f_da2[0] += 2 * pY[2] * a[14];
		df_da[2] += pY[2] * a[28];
		d2f_da2[2] += 2 * pY[2] * a[44];
		pXpYpZ = pX[0] * pY[2];
		result += a[13] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[14];
		d2f_da2[0] += 6 * pXpYpZ * a[15];
		df_da[2] += pXpYpZ * a[29];
		d2f_da2[2] += 2 * pXpYpZ * a[45];
		pXpYpZ = pX[1] * pY[2];
		result += a[14] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[15];
		df_da[2] += pXpYpZ * a[30];
		d2f_da2[2] += 2 * pXpYpZ * a[46];
		pXpYpZ = pX[2] * pY[2];
		result += a[15] * pXpYpZ;
		df_da[2] += pXpYpZ * a[31];
		d2f_da2[2] += 2 * pXpYpZ * a[47];
		result += a[16] * pZ[0];
		df_da[0] += pZ[0] * a[17];
		d2f_da2[0] += 2 * pZ[0] * a[18];
		df_da[1] += pZ[0] * a[20];
		d2f_da2[1] += 2 * pZ[0] * a[24];
		df_da[2] += 2 * pZ[0] * a[32];
		d2f_da2[2] += 6 * pZ[0] * a[48];
		pXpYpZ = pX[0] * pZ[0];
		result += a[17] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[18];
		d2f_da2[0] += 6 * pXpYpZ * a[19];
		df_da[1] += pXpYpZ * a[21];
		d2f_da2[1] += 2 * pXpYpZ * a[25];
		df_da[2] += 2 * pXpYpZ * a[33];
		d2f_da2[2] += 6 * pXpYpZ * a[49];
		pXpYpZ = pX[1] * pZ[0];
		result += a[18] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[19];
		df_da[1] += pXpYpZ * a[22];
		d2f_da2[1] += 2 * pXpYpZ * a[26];
		df_da[2] += 2 * pXpYpZ * a[34];
		d2f_da2[2] += 6 * pXpYpZ * a[50];
		pXpYpZ = pX[2] * pZ[0];
		result += a[19] * pXpYpZ;
		df_da[1] += pXpYpZ * a[23];
		d2f_da2[1] += 2 * pXpYpZ * a[27];
		df_da[2] += 2 * pXpYpZ * a[35];
		d2f_da2[2] += 6 * pXpYpZ * a[51];
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		df_da[0] += pYpZ * a[21];
		d2f_da2[0] += 2 * pYpZ * a[22];
		df_da[1] += 2 * pYpZ * a[24];
		d2f_da2[1] += 6 * pYpZ * a[28];
		df_da[2] += 2 * pYpZ * a[36];
		d2f_da2[2] += 6 * pYpZ * a[52];
		pXpYpZ = pX[0] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[22];
		d2f_da2[0] += 6 * pXpYpZ * a[23];
		df_da[1] += 2 * pXpYpZ * a[25];
		d2f_da2[1] += 6 * pXpYpZ * a[29];
		df_da[2] += 2 * pXpYpZ * a[37];
		d2f_da2[2] += 6 * pXpYpZ * a[53];
		pXpYpZ = pX[1] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[23];
		df_da[1] += 2 * pXpYpZ * a[26];
		d2f_da2[1] += 6 * pXpYpZ * a[30];
		df_da[2] += 2 * pXpYpZ * a[38];
		d2f_da2[2] += 6 * pXpYpZ * a[54];
		pXpYpZ = pX[2] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[27];
		d2f_da2[1] += 6 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[39];
		d2f_da2[2] += 6 * pXpYpZ * a[55];
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		df_da[0] += pYpZ * a[25];
		d2f_da2[0] += 2 * pYpZ * a[26];
		df_da[1] += 3 * pYpZ * a[28];
		df_da[2] += 2 * pYpZ * a[40];
		d2f_da2[2] += 6 * pYpZ * a[56];
		pXpYpZ = pX[0] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[26];
		d2f_da2[0] += 6 * pXpYpZ * a[27];
		df_da[1] += 3 * pXpYpZ * a[29];
		df_da[2] += 2 * pXpYpZ * a[41];
		d2f_da2[2] += 6 * pXpYpZ * a[57];
		pXpYpZ = pX[1] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[27];
		df_da[1] += 3 * pXpYpZ * a[30];
		df_da[2] += 2 * pXpYpZ * a[42];
		d2f_da2[2] += 6 * pXpYpZ * a[58];
		pXpYpZ = pX[2] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[43];
		d2f_da2[2] += 6 * pXpYpZ * a[59];
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		df_da[0] += pYpZ * a[29];
		d2f_da2[0] += 2 * pYpZ * a[30];
		df_da[2] += 2 * pYpZ * a[44];
		d2f_da2[2] += 6 * pYpZ * a[60];
		pXpYpZ = pX[0] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[30];
		d2f_da2[0] += 6 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[45];
		d2f_da2[2] += 6 * pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[46];
		d2f_da2[2] += 6 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += 2 * pXpYpZ * a[47];
		d2f_da2[2] += 6 * pXpYpZ * a[63];
		result += a[32] * pZ[1];
		df_da[0] += pZ[1] * a[33];
		d2f_da2[0] += 2 * pZ[1] * a[34];
		df_da[1] += pZ[1] * a[36];
		d2f_da2[1] += 2 * pZ[1] * a[40];
		df_da[2] += 3 * pZ[1] * a[48];
		pXpYpZ = pX[0] * pZ[1];
		result += a[33] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[34];
		d2f_da2[0] += 6 * pXpYpZ * a[35];
		df_da[1] += pXpYpZ * a[37];
		d2f_da2[1] += 2 * pXpYpZ * a[41];
		df_da[2] += 3 * pXpYpZ * a[49];
		pXpYpZ = pX[1] * pZ[1];
		result += a[34] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[35];
		df_da[1] += pXpYpZ * a[38];
		d2f_da2[1] += 2 * pXpYpZ * a[42];
		df_da[2] += 3 * pXpYpZ * a[50];
		pXpYpZ = pX[2] * pZ[1];
		result += a[35] * pXpYpZ;
		df_da[1] += pXpYpZ * a[39];
		d2f_da2[1] += 2 * pXpYpZ * a[43];
		df_da[2] += 3 * pXpYpZ * a[51];
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		df_da[0] += pYpZ * a[37];
		d2f_da2[0] += 2 * pYpZ * a[38];
		df_da[1] += 2 * pYpZ * a[40];
		d2f_da2[1] += 6 * pYpZ * a[44];
		df_da[2] += 3 * pYpZ * a[52];
		pXpYpZ = pX[0] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[38];
		d2f_da2[0] += 6 * pXpYpZ * a[39];
		df_da[1] += 2 * pXpYpZ * a[41];
		d2f_da2[1] += 6 * pXpYpZ * a[45];
		df_da[2] += 3 * pXpYpZ * a[53];
		pXpYpZ = pX[1] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[39];
		df_da[1] += 2 * pXpYpZ * a[42];
		d2f_da2[1] += 6 * pXpYpZ * a[46];
		df_da[2] += 3 * pXpYpZ * a[54];
		pXpYpZ = pX[2] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[43];
		d2f_da2[1] += 6 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[55];
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		df_da[0] += pYpZ * a[41];
		d2f_da2[0] += 2 * pYpZ * a[42];
		df_da[1] += 3 * pYpZ * a[44];
		df_da[2] += 3 * pYpZ * a[56];
		pXpYpZ = pX[0] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[42];
		d2f_da2[0] += 6 * pXpYpZ * a[43];
		df_da[1] += 3 * pXpYpZ * a[45];
		df_da[2] += 3 * pXpYpZ * a[57];
		pXpYpZ = pX[1] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[43];
		df_da[1] += 3 * pXpYpZ * a[46];
		df_da[2] += 3 * pXpYpZ * a[58];
		pXpYpZ = pX[2] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[59];
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		df_da[0] += pYpZ * a[45];
		d2f_da2[0] += 2 * pYpZ * a[46];
		df_da[2] += 3 * pYpZ * a[60];
		pXpYpZ = pX[0] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[46];
		d2f_da2[0] += 6 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += 3 * pXpYpZ * a[63];
		result += a[48] * pZ[2];
		df_da[0] += pZ[2] * a[49];
		d2f_da2[0] += 2 * pZ[2] * a[50];
		df_da[1] += pZ[2] * a[52];
		d2f_da2[1] += 2 * pZ[2] * a[56];
		pXpYpZ = pX[0] * pZ[2];
		result += a[49] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[50];
		d2f_da2[0] += 6 * pXpYpZ * a[51];
		df_da[1] += pXpYpZ * a[53];
		d2f_da2[1] += 2 * pXpYpZ * a[57];
		pXpYpZ = pX[1] * pZ[2];
		result += a[50] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[51];
		df_da[1] += pXpYpZ * a[54];
		d2f_da2[1] += 2 * pXpYpZ * a[58];
		pXpYpZ = pX[2] * pZ[2];
		result += a[51] * pXpYpZ;
		df_da[1] += pXpYpZ * a[55];
		d2f_da2[1] += 2 * pXpYpZ * a[59];
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		df_da[0] += pYpZ * a[53];
		d2f_da2[0] += 2 * pYpZ * a[54];
		df_da[1] += 2 * pYpZ * a[56];
		d2f_da2[1] += 6 * pYpZ * a[60];
		pXpYpZ = pX[0] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[54];
		d2f_da2[0] += 6 * pXpYpZ * a[55];
		df_da[1] += 2 * pXpYpZ * a[57];
		d2f_da2[1] += 6 * pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[55];
		df_da[1] += 2 * pXpYpZ * a[58];
		d2f_da2[1] += 6 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[59];
		d2f_da2[1] += 6 * pXpYpZ * a[63];
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		df_da[0] += pYpZ * a[57];
		d2f_da2[0] += 2 * pYpZ * a[58];
		df_da[1] += 3 * pYpZ * a[60];
		pXpYpZ = pX[0] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[58];
		d2f_da2[0] += 6 * pXpYpZ * a[59];
		df_da[1] += 3 * pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[59];
		df_da[1] += 3 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[63];
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		df_da[0] += pYpZ * a[61];
		d2f_da2[0] += 2 * pYpZ * a[62];
		pXpYpZ = pX[0] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[62];
		d2f_da2[0] += 6 * pXpYpZ * a[63];
		pXpYpZ = pX[1] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[63];
		pXpYpZ = pX[2] * pYpZ;
		result += a[63] * pXpYpZ;

		return result;
	}

	/**
	 * Used to create the inline value function for second-order gradients
	 * 
	 * @return the function text.
	 */
	static String inlineValue2()
	{
		String _pYpZ;
		String _pXpYpZ;
		StringBuilder sb = new StringBuilder();

		// Gradients are described in:
		// Babcock & Zhuang (2017) 
		// Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
		// Scientific Reports 7, Article number: 552
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pYpZ = append_pYpZ(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					_pXpYpZ = append_pXpYpZ(sb, _pYpZ, i);

					//@formatter:off
					sb.append(String.format("result += a[%d] * %s;\n", ai, _pXpYpZ));
					if (i < N_1)
					{
						sb.append(String.format("df_da[0] += %d * %s * a[%d];\n", i+1, _pXpYpZ, getIndex(i+1, j, k)));
						if (i < N_2)
							sb.append(String.format("d2f_da2[0] += %d * %d * %s * a[%d];\n", i+1, i+2, _pXpYpZ, getIndex(i+2, j, k)));
					}
					if (j < N_1)
					{
						sb.append(String.format("df_da[1] += %d * %s * a[%d];\n", j+1, _pXpYpZ, getIndex(i, j+1, k)));
						if (j < N_2)
							sb.append(String.format("d2f_da2[1] += %d * %d * %s * a[%d];\n", j+1, j+2, _pXpYpZ, getIndex(i, j+2, k)));
					}
					if (k < N_1)
					{
						sb.append(String.format("df_da[2] += %d * %s * a[%d];\n", k+1, _pXpYpZ, getIndex(i, j, k+1)));
						if (k < N_2)
							sb.append(String.format("d2f_da2[2] += %d * %d * %s * a[%d];\n", k+1, k+2, _pXpYpZ, getIndex(i, j, k+2)));
					}
					//@formatter:on

					//// Formal computation
					//pXpYpZ = pX[i] * pYpZ;
					//result += a[ai] * pXpYpZ;
					//if (i < N_1)
					//{
					//	df_da[0] += (i+1) * a[getIndex(i+1, j, k)] * pXpYpZ;
					//	if (i < N_2)
					//		d2f_da2[0] += (i+1) * (i + 2) * a[getIndex(i + 2, j, k)] * pXpYpZ;
					//}
					//if (j < N_1)
					//{
					//	df_da[1] += (j+1) * a[getIndex(i, j+1, k)] * pXpYpZ;
					//	if (j < N_2)
					//		d2f_da2[1] += (j+1) * (j + 2) * a[getIndex(i, j + 2, k)] * pXpYpZ;
					//}
					//if (k < N_1)
					//{
					//	df_da[2] += (k+1) * a[getIndex(i, j, k+1)] * pXpYpZ;
					//	if (k < N_2)
					//		d2f_da2[2] += (k+1) * (k + 2) * a[getIndex(i, j, k + 2)] * pXpYpZ;
					//}
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	/**
	 * Compute the power table for computation of second order derivatives.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power tables.
	 * @throws OutOfRangeException
	 *             if {@code x}, {@code y} or
	 *             {@code z} are not in the interval {@code [0, 1]}.
	 */
	public static double[][] computeSecondOrderPowerTables(double x, double y, double z) throws OutOfRangeException
	{
		if (x < 0 || x > 1)
		{
			throw new OutOfRangeException(x, 0, 1);
		}
		if (y < 0 || y > 1)
		{
			throw new OutOfRangeException(y, 0, 1);
		}
		if (z < 0 || z > 1)
		{
			throw new OutOfRangeException(z, 0, 1);
		}

		final double x2 = x * x;
		final double x3 = x2 * x;
		final double[] pX = { /* 1, optimised out */ x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { /* 1, optimised out */ y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { /* 1, optimised out */ z, z2, z3 };

		return computeSecondOrderPowerTables(pX, pY, pZ);
	}

	/**
	 * Compute the power table for computation of second order derivatives.
	 *
	 * @param x
	 *            x-coordinate of the interpolation point.
	 * @param y
	 *            y-coordinate of the interpolation point.
	 * @param z
	 *            z-coordinate of the interpolation point.
	 * @return the power tables.
	 */
	public static double[][] computeSecondOrderPowerTables(CubicSplinePosition x, CubicSplinePosition y,
			CubicSplinePosition z)
	{
		return computeSecondOrderPowerTables(x.p, y.p, z.p);
	}

	/**
	 * Compute the power table for computation of second order derivatives.
	 *
	 * @param pX
	 *            x-coordinate powers of the interpolation point.
	 * @param pY
	 *            y-coordinate powers of the interpolation point.
	 * @param pZ
	 *            z-coordinate powers of the interpolation point.
	 * @return the power tables.
	 */
	private static double[][] computeSecondOrderPowerTables(final double[] pX, final double[] pY, final double[] pZ)
	{
		double pYpZ;
		double pXpYpZ;
		final double[] table = new double[64];
		final double[] table_df_dx = new double[48];
		final double[] table_df_dy = new double[48];
		final double[] table_df_dz = new double[48];
		final double[] table_d2f_dx2 = new double[32];
		final double[] table_d2f_dy2 = new double[32];
		final double[] table_d2f_dz2 = new double[32];

		table[0] = 1;
		table_df_dx[0] = 1;
		table_d2f_dx2[0] = 2;
		table_df_dy[0] = 1;
		table_d2f_dy2[0] = 2;
		table_df_dz[0] = 1;
		table_d2f_dz2[0] = 2;
		pXpYpZ = pX[0];
		table[1] = pXpYpZ;
		table_df_dx[1] = 2 * pXpYpZ;
		table_d2f_dx2[1] = 6 * pXpYpZ;
		table_df_dy[1] = pXpYpZ;
		table_d2f_dy2[1] = 2 * pXpYpZ;
		table_df_dz[1] = pXpYpZ;
		table_d2f_dz2[1] = 2 * pXpYpZ;
		pXpYpZ = pX[1];
		table[2] = pXpYpZ;
		table_df_dx[2] = 3 * pXpYpZ;
		table_df_dy[2] = pXpYpZ;
		table_d2f_dy2[2] = 2 * pXpYpZ;
		table_df_dz[2] = pXpYpZ;
		table_d2f_dz2[2] = 2 * pXpYpZ;
		pXpYpZ = pX[2];
		table[3] = pXpYpZ;
		table_df_dy[3] = pXpYpZ;
		table_d2f_dy2[3] = 2 * pXpYpZ;
		table_df_dz[3] = pXpYpZ;
		table_d2f_dz2[3] = 2 * pXpYpZ;
		table[4] = pY[0];
		table_df_dx[3] = pY[0];
		table_d2f_dx2[2] = 2 * pY[0];
		table_df_dy[4] = 2 * pY[0];
		table_d2f_dy2[4] = 6 * pY[0];
		table_df_dz[4] = pY[0];
		table_d2f_dz2[4] = 2 * pY[0];
		pXpYpZ = pX[0] * pY[0];
		table[5] = pXpYpZ;
		table_df_dx[4] = 2 * pXpYpZ;
		table_d2f_dx2[3] = 6 * pXpYpZ;
		table_df_dy[5] = 2 * pXpYpZ;
		table_d2f_dy2[5] = 6 * pXpYpZ;
		table_df_dz[5] = pXpYpZ;
		table_d2f_dz2[5] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pY[0];
		table[6] = pXpYpZ;
		table_df_dx[5] = 3 * pXpYpZ;
		table_df_dy[6] = 2 * pXpYpZ;
		table_d2f_dy2[6] = 6 * pXpYpZ;
		table_df_dz[6] = pXpYpZ;
		table_d2f_dz2[6] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pY[0];
		table[7] = pXpYpZ;
		table_df_dy[7] = 2 * pXpYpZ;
		table_d2f_dy2[7] = 6 * pXpYpZ;
		table_df_dz[7] = pXpYpZ;
		table_d2f_dz2[7] = 2 * pXpYpZ;
		table[8] = pY[1];
		table_df_dx[6] = pY[1];
		table_d2f_dx2[4] = 2 * pY[1];
		table_df_dy[8] = 3 * pY[1];
		table_df_dz[8] = pY[1];
		table_d2f_dz2[8] = 2 * pY[1];
		pXpYpZ = pX[0] * pY[1];
		table[9] = pXpYpZ;
		table_df_dx[7] = 2 * pXpYpZ;
		table_d2f_dx2[5] = 6 * pXpYpZ;
		table_df_dy[9] = 3 * pXpYpZ;
		table_df_dz[9] = pXpYpZ;
		table_d2f_dz2[9] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pY[1];
		table[10] = pXpYpZ;
		table_df_dx[8] = 3 * pXpYpZ;
		table_df_dy[10] = 3 * pXpYpZ;
		table_df_dz[10] = pXpYpZ;
		table_d2f_dz2[10] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pY[1];
		table[11] = pXpYpZ;
		table_df_dy[11] = 3 * pXpYpZ;
		table_df_dz[11] = pXpYpZ;
		table_d2f_dz2[11] = 2 * pXpYpZ;
		table[12] = pY[2];
		table_df_dx[9] = pY[2];
		table_d2f_dx2[6] = 2 * pY[2];
		table_df_dz[12] = pY[2];
		table_d2f_dz2[12] = 2 * pY[2];
		pXpYpZ = pX[0] * pY[2];
		table[13] = pXpYpZ;
		table_df_dx[10] = 2 * pXpYpZ;
		table_d2f_dx2[7] = 6 * pXpYpZ;
		table_df_dz[13] = pXpYpZ;
		table_d2f_dz2[13] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pY[2];
		table[14] = pXpYpZ;
		table_df_dx[11] = 3 * pXpYpZ;
		table_df_dz[14] = pXpYpZ;
		table_d2f_dz2[14] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pY[2];
		table[15] = pXpYpZ;
		table_df_dz[15] = pXpYpZ;
		table_d2f_dz2[15] = 2 * pXpYpZ;
		table[16] = pZ[0];
		table_df_dx[12] = pZ[0];
		table_d2f_dx2[8] = 2 * pZ[0];
		table_df_dy[12] = pZ[0];
		table_d2f_dy2[8] = 2 * pZ[0];
		table_df_dz[16] = 2 * pZ[0];
		table_d2f_dz2[16] = 6 * pZ[0];
		pXpYpZ = pX[0] * pZ[0];
		table[17] = pXpYpZ;
		table_df_dx[13] = 2 * pXpYpZ;
		table_d2f_dx2[9] = 6 * pXpYpZ;
		table_df_dy[13] = pXpYpZ;
		table_d2f_dy2[9] = 2 * pXpYpZ;
		table_df_dz[17] = 2 * pXpYpZ;
		table_d2f_dz2[17] = 6 * pXpYpZ;
		pXpYpZ = pX[1] * pZ[0];
		table[18] = pXpYpZ;
		table_df_dx[14] = 3 * pXpYpZ;
		table_df_dy[14] = pXpYpZ;
		table_d2f_dy2[10] = 2 * pXpYpZ;
		table_df_dz[18] = 2 * pXpYpZ;
		table_d2f_dz2[18] = 6 * pXpYpZ;
		pXpYpZ = pX[2] * pZ[0];
		table[19] = pXpYpZ;
		table_df_dy[15] = pXpYpZ;
		table_d2f_dy2[11] = 2 * pXpYpZ;
		table_df_dz[19] = 2 * pXpYpZ;
		table_d2f_dz2[19] = 6 * pXpYpZ;
		pYpZ = pY[0] * pZ[0];
		table[20] = pYpZ;
		table_df_dx[15] = pYpZ;
		table_d2f_dx2[10] = 2 * pYpZ;
		table_df_dy[16] = 2 * pYpZ;
		table_d2f_dy2[12] = 6 * pYpZ;
		table_df_dz[20] = 2 * pYpZ;
		table_d2f_dz2[20] = 6 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[21] = pXpYpZ;
		table_df_dx[16] = 2 * pXpYpZ;
		table_d2f_dx2[11] = 6 * pXpYpZ;
		table_df_dy[17] = 2 * pXpYpZ;
		table_d2f_dy2[13] = 6 * pXpYpZ;
		table_df_dz[21] = 2 * pXpYpZ;
		table_d2f_dz2[21] = 6 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[22] = pXpYpZ;
		table_df_dx[17] = 3 * pXpYpZ;
		table_df_dy[18] = 2 * pXpYpZ;
		table_d2f_dy2[14] = 6 * pXpYpZ;
		table_df_dz[22] = 2 * pXpYpZ;
		table_d2f_dz2[22] = 6 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[23] = pXpYpZ;
		table_df_dy[19] = 2 * pXpYpZ;
		table_d2f_dy2[15] = 6 * pXpYpZ;
		table_df_dz[23] = 2 * pXpYpZ;
		table_d2f_dz2[23] = 6 * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		table[24] = pYpZ;
		table_df_dx[18] = pYpZ;
		table_d2f_dx2[12] = 2 * pYpZ;
		table_df_dy[20] = 3 * pYpZ;
		table_df_dz[24] = 2 * pYpZ;
		table_d2f_dz2[24] = 6 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[25] = pXpYpZ;
		table_df_dx[19] = 2 * pXpYpZ;
		table_d2f_dx2[13] = 6 * pXpYpZ;
		table_df_dy[21] = 3 * pXpYpZ;
		table_df_dz[25] = 2 * pXpYpZ;
		table_d2f_dz2[25] = 6 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[26] = pXpYpZ;
		table_df_dx[20] = 3 * pXpYpZ;
		table_df_dy[22] = 3 * pXpYpZ;
		table_df_dz[26] = 2 * pXpYpZ;
		table_d2f_dz2[26] = 6 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[27] = pXpYpZ;
		table_df_dy[23] = 3 * pXpYpZ;
		table_df_dz[27] = 2 * pXpYpZ;
		table_d2f_dz2[27] = 6 * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		table[28] = pYpZ;
		table_df_dx[21] = pYpZ;
		table_d2f_dx2[14] = 2 * pYpZ;
		table_df_dz[28] = 2 * pYpZ;
		table_d2f_dz2[28] = 6 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[29] = pXpYpZ;
		table_df_dx[22] = 2 * pXpYpZ;
		table_d2f_dx2[15] = 6 * pXpYpZ;
		table_df_dz[29] = 2 * pXpYpZ;
		table_d2f_dz2[29] = 6 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[30] = pXpYpZ;
		table_df_dx[23] = 3 * pXpYpZ;
		table_df_dz[30] = 2 * pXpYpZ;
		table_d2f_dz2[30] = 6 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[31] = pXpYpZ;
		table_df_dz[31] = 2 * pXpYpZ;
		table_d2f_dz2[31] = 6 * pXpYpZ;
		table[32] = pZ[1];
		table_df_dx[24] = pZ[1];
		table_d2f_dx2[16] = 2 * pZ[1];
		table_df_dy[24] = pZ[1];
		table_d2f_dy2[16] = 2 * pZ[1];
		table_df_dz[32] = 3 * pZ[1];
		pXpYpZ = pX[0] * pZ[1];
		table[33] = pXpYpZ;
		table_df_dx[25] = 2 * pXpYpZ;
		table_d2f_dx2[17] = 6 * pXpYpZ;
		table_df_dy[25] = pXpYpZ;
		table_d2f_dy2[17] = 2 * pXpYpZ;
		table_df_dz[33] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pZ[1];
		table[34] = pXpYpZ;
		table_df_dx[26] = 3 * pXpYpZ;
		table_df_dy[26] = pXpYpZ;
		table_d2f_dy2[18] = 2 * pXpYpZ;
		table_df_dz[34] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pZ[1];
		table[35] = pXpYpZ;
		table_df_dy[27] = pXpYpZ;
		table_d2f_dy2[19] = 2 * pXpYpZ;
		table_df_dz[35] = 3 * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		table[36] = pYpZ;
		table_df_dx[27] = pYpZ;
		table_d2f_dx2[18] = 2 * pYpZ;
		table_df_dy[28] = 2 * pYpZ;
		table_d2f_dy2[20] = 6 * pYpZ;
		table_df_dz[36] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[37] = pXpYpZ;
		table_df_dx[28] = 2 * pXpYpZ;
		table_d2f_dx2[19] = 6 * pXpYpZ;
		table_df_dy[29] = 2 * pXpYpZ;
		table_d2f_dy2[21] = 6 * pXpYpZ;
		table_df_dz[37] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[38] = pXpYpZ;
		table_df_dx[29] = 3 * pXpYpZ;
		table_df_dy[30] = 2 * pXpYpZ;
		table_d2f_dy2[22] = 6 * pXpYpZ;
		table_df_dz[38] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[39] = pXpYpZ;
		table_df_dy[31] = 2 * pXpYpZ;
		table_d2f_dy2[23] = 6 * pXpYpZ;
		table_df_dz[39] = 3 * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		table[40] = pYpZ;
		table_df_dx[30] = pYpZ;
		table_d2f_dx2[20] = 2 * pYpZ;
		table_df_dy[32] = 3 * pYpZ;
		table_df_dz[40] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[41] = pXpYpZ;
		table_df_dx[31] = 2 * pXpYpZ;
		table_d2f_dx2[21] = 6 * pXpYpZ;
		table_df_dy[33] = 3 * pXpYpZ;
		table_df_dz[41] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[42] = pXpYpZ;
		table_df_dx[32] = 3 * pXpYpZ;
		table_df_dy[34] = 3 * pXpYpZ;
		table_df_dz[42] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[43] = pXpYpZ;
		table_df_dy[35] = 3 * pXpYpZ;
		table_df_dz[43] = 3 * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		table[44] = pYpZ;
		table_df_dx[33] = pYpZ;
		table_d2f_dx2[22] = 2 * pYpZ;
		table_df_dz[44] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[45] = pXpYpZ;
		table_df_dx[34] = 2 * pXpYpZ;
		table_d2f_dx2[23] = 6 * pXpYpZ;
		table_df_dz[45] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[46] = pXpYpZ;
		table_df_dx[35] = 3 * pXpYpZ;
		table_df_dz[46] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[47] = pXpYpZ;
		table_df_dz[47] = 3 * pXpYpZ;
		table[48] = pZ[2];
		table_df_dx[36] = pZ[2];
		table_d2f_dx2[24] = 2 * pZ[2];
		table_df_dy[36] = pZ[2];
		table_d2f_dy2[24] = 2 * pZ[2];
		pXpYpZ = pX[0] * pZ[2];
		table[49] = pXpYpZ;
		table_df_dx[37] = 2 * pXpYpZ;
		table_d2f_dx2[25] = 6 * pXpYpZ;
		table_df_dy[37] = pXpYpZ;
		table_d2f_dy2[25] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pZ[2];
		table[50] = pXpYpZ;
		table_df_dx[38] = 3 * pXpYpZ;
		table_df_dy[38] = pXpYpZ;
		table_d2f_dy2[26] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pZ[2];
		table[51] = pXpYpZ;
		table_df_dy[39] = pXpYpZ;
		table_d2f_dy2[27] = 2 * pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		table[52] = pYpZ;
		table_df_dx[39] = pYpZ;
		table_d2f_dx2[26] = 2 * pYpZ;
		table_df_dy[40] = 2 * pYpZ;
		table_d2f_dy2[28] = 6 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[53] = pXpYpZ;
		table_df_dx[40] = 2 * pXpYpZ;
		table_d2f_dx2[27] = 6 * pXpYpZ;
		table_df_dy[41] = 2 * pXpYpZ;
		table_d2f_dy2[29] = 6 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[54] = pXpYpZ;
		table_df_dx[41] = 3 * pXpYpZ;
		table_df_dy[42] = 2 * pXpYpZ;
		table_d2f_dy2[30] = 6 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[55] = pXpYpZ;
		table_df_dy[43] = 2 * pXpYpZ;
		table_d2f_dy2[31] = 6 * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		table[56] = pYpZ;
		table_df_dx[42] = pYpZ;
		table_d2f_dx2[28] = 2 * pYpZ;
		table_df_dy[44] = 3 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[57] = pXpYpZ;
		table_df_dx[43] = 2 * pXpYpZ;
		table_d2f_dx2[29] = 6 * pXpYpZ;
		table_df_dy[45] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[58] = pXpYpZ;
		table_df_dx[44] = 3 * pXpYpZ;
		table_df_dy[46] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[59] = pXpYpZ;
		table_df_dy[47] = 3 * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		table[60] = pYpZ;
		table_df_dx[45] = pYpZ;
		table_d2f_dx2[30] = 2 * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		table[61] = pXpYpZ;
		table_df_dx[46] = 2 * pXpYpZ;
		table_d2f_dx2[31] = 6 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[62] = pXpYpZ;
		table_df_dx[47] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[63] = pXpYpZ;

		return new double[][] { table, table_df_dx, table_df_dy, table_df_dz, table_d2f_dx2, table_d2f_dy2,
				table_d2f_dz2 };
	}

	/**
	 * Used to create the inline power table function for second-order gradients
	 * 
	 * @return the function text.
	 */
	static String inlineComputePowerTable2()
	{
		String _pYpZ, _pXpYpZ;
		StringBuilder sb = new StringBuilder();

		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pYpZ = append_pYpZ(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					_pXpYpZ = append_pXpYpZ(sb, _pYpZ, i);

					//@formatter:off
					sb.append(String.format("table[%d] = %s;\n", ai, _pXpYpZ));
					if (i < N_1)
					{
						sb.append(String.format("table_df_dx[%d] = %d * %s;\n", x1++, i+1, _pXpYpZ));
						if (i < N_2)
							sb.append(String.format("table_d2f_dx2[%d] = %d * %d * %s;\n", x2++, i+1, i+2, _pXpYpZ));
					}
					if (j < N_1)
					{
						sb.append(String.format("table_df_dy[%d] = %d * %s;\n", y1++, j+1, _pXpYpZ));
						if (j < N_2)
							sb.append(String.format("table_d2f_dy2[%d] = %d * %d * %s;\n", y2++, j+1, j+2, _pXpYpZ));
					}
					if (k < N_1)
					{
						sb.append(String.format("table_df_dz[%d] = %d * %s;\n", z1++, k+1, _pXpYpZ));
						if (k < N_2)
							sb.append(String.format("table_d2f_dz2[%d] = %d * %d * %s;\n", z2++, k+1, k+2, _pXpYpZ));
					}
					//@formatter:on
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	/**
	 * Compute the value and partial first-order and second-order derivatives using pre-computed power tables.
	 *
	 * @param tables
	 *            the power tables
	 * @param df_da
	 *            the partial second order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	public double value(double[][] tables, double[] df_da, double[] d2f_da2)
	{
		final double[] table = tables[0];
		final double[] table_df_dx = tables[1];
		final double[] table_df_dy = tables[2];
		final double[] table_df_dz = tables[3];
		final double[] table_d2f_dx2 = tables[4];
		final double[] table_d2f_dy2 = tables[5];
		final double[] table_d2f_dz2 = tables[6];

		double result = 0;
		df_da[0] = 0;
		df_da[1] = 0;
		df_da[2] = 0;
		d2f_da2[0] = 0;
		d2f_da2[1] = 0;
		d2f_da2[2] = 0;

		result = a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
				a[5] * table[5] + a[6] * table[6] + a[7] * table[7] + a[8] * table[8] + a[9] * table[9] +
				a[10] * table[10] + a[11] * table[11] + a[12] * table[12] + a[13] * table[13] + a[14] * table[14] +
				a[15] * table[15] + a[16] * table[16] + a[17] * table[17] + a[18] * table[18] + a[19] * table[19] +
				a[20] * table[20] + a[21] * table[21] + a[22] * table[22] + a[23] * table[23] + a[24] * table[24] +
				a[25] * table[25] + a[26] * table[26] + a[27] * table[27] + a[28] * table[28] + a[29] * table[29] +
				a[30] * table[30] + a[31] * table[31] + a[32] * table[32] + a[33] * table[33] + a[34] * table[34] +
				a[35] * table[35] + a[36] * table[36] + a[37] * table[37] + a[38] * table[38] + a[39] * table[39] +
				a[40] * table[40] + a[41] * table[41] + a[42] * table[42] + a[43] * table[43] + a[44] * table[44] +
				a[45] * table[45] + a[46] * table[46] + a[47] * table[47] + a[48] * table[48] + a[49] * table[49] +
				a[50] * table[50] + a[51] * table[51] + a[52] * table[52] + a[53] * table[53] + a[54] * table[54] +
				a[55] * table[55] + a[56] * table[56] + a[57] * table[57] + a[58] * table[58] + a[59] * table[59] +
				a[60] * table[60] + a[61] * table[61] + a[62] * table[62] + a[63] * table[63];
		df_da[0] = a[1] * table_df_dx[0] + a[2] * table_df_dx[1] + a[3] * table_df_dx[2] + a[5] * table_df_dx[3] +
				a[6] * table_df_dx[4] + a[7] * table_df_dx[5] + a[9] * table_df_dx[6] + a[10] * table_df_dx[7] +
				a[11] * table_df_dx[8] + a[13] * table_df_dx[9] + a[14] * table_df_dx[10] + a[15] * table_df_dx[11] +
				a[17] * table_df_dx[12] + a[18] * table_df_dx[13] + a[19] * table_df_dx[14] + a[21] * table_df_dx[15] +
				a[22] * table_df_dx[16] + a[23] * table_df_dx[17] + a[25] * table_df_dx[18] + a[26] * table_df_dx[19] +
				a[27] * table_df_dx[20] + a[29] * table_df_dx[21] + a[30] * table_df_dx[22] + a[31] * table_df_dx[23] +
				a[33] * table_df_dx[24] + a[34] * table_df_dx[25] + a[35] * table_df_dx[26] + a[37] * table_df_dx[27] +
				a[38] * table_df_dx[28] + a[39] * table_df_dx[29] + a[41] * table_df_dx[30] + a[42] * table_df_dx[31] +
				a[43] * table_df_dx[32] + a[45] * table_df_dx[33] + a[46] * table_df_dx[34] + a[47] * table_df_dx[35] +
				a[49] * table_df_dx[36] + a[50] * table_df_dx[37] + a[51] * table_df_dx[38] + a[53] * table_df_dx[39] +
				a[54] * table_df_dx[40] + a[55] * table_df_dx[41] + a[57] * table_df_dx[42] + a[58] * table_df_dx[43] +
				a[59] * table_df_dx[44] + a[61] * table_df_dx[45] + a[62] * table_df_dx[46] + a[63] * table_df_dx[47];
		df_da[1] = a[4] * table_df_dy[0] + a[5] * table_df_dy[1] + a[6] * table_df_dy[2] + a[7] * table_df_dy[3] +
				a[8] * table_df_dy[4] + a[9] * table_df_dy[5] + a[10] * table_df_dy[6] + a[11] * table_df_dy[7] +
				a[12] * table_df_dy[8] + a[13] * table_df_dy[9] + a[14] * table_df_dy[10] + a[15] * table_df_dy[11] +
				a[20] * table_df_dy[12] + a[21] * table_df_dy[13] + a[22] * table_df_dy[14] + a[23] * table_df_dy[15] +
				a[24] * table_df_dy[16] + a[25] * table_df_dy[17] + a[26] * table_df_dy[18] + a[27] * table_df_dy[19] +
				a[28] * table_df_dy[20] + a[29] * table_df_dy[21] + a[30] * table_df_dy[22] + a[31] * table_df_dy[23] +
				a[36] * table_df_dy[24] + a[37] * table_df_dy[25] + a[38] * table_df_dy[26] + a[39] * table_df_dy[27] +
				a[40] * table_df_dy[28] + a[41] * table_df_dy[29] + a[42] * table_df_dy[30] + a[43] * table_df_dy[31] +
				a[44] * table_df_dy[32] + a[45] * table_df_dy[33] + a[46] * table_df_dy[34] + a[47] * table_df_dy[35] +
				a[52] * table_df_dy[36] + a[53] * table_df_dy[37] + a[54] * table_df_dy[38] + a[55] * table_df_dy[39] +
				a[56] * table_df_dy[40] + a[57] * table_df_dy[41] + a[58] * table_df_dy[42] + a[59] * table_df_dy[43] +
				a[60] * table_df_dy[44] + a[61] * table_df_dy[45] + a[62] * table_df_dy[46] + a[63] * table_df_dy[47];
		df_da[2] = a[16] * table_df_dz[0] + a[17] * table_df_dz[1] + a[18] * table_df_dz[2] + a[19] * table_df_dz[3] +
				a[20] * table_df_dz[4] + a[21] * table_df_dz[5] + a[22] * table_df_dz[6] + a[23] * table_df_dz[7] +
				a[24] * table_df_dz[8] + a[25] * table_df_dz[9] + a[26] * table_df_dz[10] + a[27] * table_df_dz[11] +
				a[28] * table_df_dz[12] + a[29] * table_df_dz[13] + a[30] * table_df_dz[14] + a[31] * table_df_dz[15] +
				a[32] * table_df_dz[16] + a[33] * table_df_dz[17] + a[34] * table_df_dz[18] + a[35] * table_df_dz[19] +
				a[36] * table_df_dz[20] + a[37] * table_df_dz[21] + a[38] * table_df_dz[22] + a[39] * table_df_dz[23] +
				a[40] * table_df_dz[24] + a[41] * table_df_dz[25] + a[42] * table_df_dz[26] + a[43] * table_df_dz[27] +
				a[44] * table_df_dz[28] + a[45] * table_df_dz[29] + a[46] * table_df_dz[30] + a[47] * table_df_dz[31] +
				a[48] * table_df_dz[32] + a[49] * table_df_dz[33] + a[50] * table_df_dz[34] + a[51] * table_df_dz[35] +
				a[52] * table_df_dz[36] + a[53] * table_df_dz[37] + a[54] * table_df_dz[38] + a[55] * table_df_dz[39] +
				a[56] * table_df_dz[40] + a[57] * table_df_dz[41] + a[58] * table_df_dz[42] + a[59] * table_df_dz[43] +
				a[60] * table_df_dz[44] + a[61] * table_df_dz[45] + a[62] * table_df_dz[46] + a[63] * table_df_dz[47];
		d2f_da2[0] = a[2] * table_d2f_dx2[0] + a[3] * table_d2f_dx2[1] + a[6] * table_d2f_dx2[2] +
				a[7] * table_d2f_dx2[3] + a[10] * table_d2f_dx2[4] + a[11] * table_d2f_dx2[5] +
				a[14] * table_d2f_dx2[6] + a[15] * table_d2f_dx2[7] + a[18] * table_d2f_dx2[8] +
				a[19] * table_d2f_dx2[9] + a[22] * table_d2f_dx2[10] + a[23] * table_d2f_dx2[11] +
				a[26] * table_d2f_dx2[12] + a[27] * table_d2f_dx2[13] + a[30] * table_d2f_dx2[14] +
				a[31] * table_d2f_dx2[15] + a[34] * table_d2f_dx2[16] + a[35] * table_d2f_dx2[17] +
				a[38] * table_d2f_dx2[18] + a[39] * table_d2f_dx2[19] + a[42] * table_d2f_dx2[20] +
				a[43] * table_d2f_dx2[21] + a[46] * table_d2f_dx2[22] + a[47] * table_d2f_dx2[23] +
				a[50] * table_d2f_dx2[24] + a[51] * table_d2f_dx2[25] + a[54] * table_d2f_dx2[26] +
				a[55] * table_d2f_dx2[27] + a[58] * table_d2f_dx2[28] + a[59] * table_d2f_dx2[29] +
				a[62] * table_d2f_dx2[30] + a[63] * table_d2f_dx2[31];
		d2f_da2[1] = a[8] * table_d2f_dy2[0] + a[9] * table_d2f_dy2[1] + a[10] * table_d2f_dy2[2] +
				a[11] * table_d2f_dy2[3] + a[12] * table_d2f_dy2[4] + a[13] * table_d2f_dy2[5] +
				a[14] * table_d2f_dy2[6] + a[15] * table_d2f_dy2[7] + a[24] * table_d2f_dy2[8] +
				a[25] * table_d2f_dy2[9] + a[26] * table_d2f_dy2[10] + a[27] * table_d2f_dy2[11] +
				a[28] * table_d2f_dy2[12] + a[29] * table_d2f_dy2[13] + a[30] * table_d2f_dy2[14] +
				a[31] * table_d2f_dy2[15] + a[40] * table_d2f_dy2[16] + a[41] * table_d2f_dy2[17] +
				a[42] * table_d2f_dy2[18] + a[43] * table_d2f_dy2[19] + a[44] * table_d2f_dy2[20] +
				a[45] * table_d2f_dy2[21] + a[46] * table_d2f_dy2[22] + a[47] * table_d2f_dy2[23] +
				a[56] * table_d2f_dy2[24] + a[57] * table_d2f_dy2[25] + a[58] * table_d2f_dy2[26] +
				a[59] * table_d2f_dy2[27] + a[60] * table_d2f_dy2[28] + a[61] * table_d2f_dy2[29] +
				a[62] * table_d2f_dy2[30] + a[63] * table_d2f_dy2[31];
		d2f_da2[2] = a[32] * table_d2f_dz2[0] + a[33] * table_d2f_dz2[1] + a[34] * table_d2f_dz2[2] +
				a[35] * table_d2f_dz2[3] + a[36] * table_d2f_dz2[4] + a[37] * table_d2f_dz2[5] +
				a[38] * table_d2f_dz2[6] + a[39] * table_d2f_dz2[7] + a[40] * table_d2f_dz2[8] +
				a[41] * table_d2f_dz2[9] + a[42] * table_d2f_dz2[10] + a[43] * table_d2f_dz2[11] +
				a[44] * table_d2f_dz2[12] + a[45] * table_d2f_dz2[13] + a[46] * table_d2f_dz2[14] +
				a[47] * table_d2f_dz2[15] + a[48] * table_d2f_dz2[16] + a[49] * table_d2f_dz2[17] +
				a[50] * table_d2f_dz2[18] + a[51] * table_d2f_dz2[19] + a[52] * table_d2f_dz2[20] +
				a[53] * table_d2f_dz2[21] + a[54] * table_d2f_dz2[22] + a[55] * table_d2f_dz2[23] +
				a[56] * table_d2f_dz2[24] + a[57] * table_d2f_dz2[25] + a[58] * table_d2f_dz2[26] +
				a[59] * table_d2f_dz2[27] + a[60] * table_d2f_dz2[28] + a[61] * table_d2f_dz2[29] +
				a[62] * table_d2f_dz2[30] + a[63] * table_d2f_dz2[31];

		return result;
	}

	/**
	 * Used to create the inline value function for second-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValue2WithPowerTable()
	{
		StringBuilder sb = new StringBuilder();
		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		// @formatter:off
		sb.append("result =");
		for (int k = 0, ai = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++, ai++)
					sb.append(String.format("+ a[%d] * table[%d]\n", ai, ai));
		sb.append(";\n");
		sb.append("df_da[0] =");
		for (int k = 0, x1 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_1)
						sb.append(String.format("+ a[%d] * table_df_dx[%d]\n", getIndex(i+1, j, k), x1++));
		sb.append(";\n");
		sb.append("df_da[1] =");
		for (int k = 0, y1 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_1)
						sb.append(String.format("+ a[%d] * table_df_dy[%d]\n", getIndex(i, j+1, k), y1++));
		sb.append(";\n");
		sb.append("df_da[2] =");
		for (int k = 0, z1 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_1)
						sb.append(String.format("+ a[%d] * table_df_dz[%d]\n", getIndex(i, j, k+1), z1++));
		sb.append(";\n");
		sb.append("d2f_da2[0] =");
		for (int k = 0, x2 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_2)
						sb.append(String.format("+ a[%d] * table_d2f_dx2[%d]\n", getIndex(i+2, j, k), x2++));
		sb.append(";\n");
		sb.append("d2f_da2[1] =");
		for (int k = 0, y2 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_2)
						sb.append(String.format("+ a[%d] * table_d2f_dy2[%d]\n", getIndex(i, j+2, k), y2++));
		sb.append(";\n");
		sb.append("d2f_da2[2] =");
		for (int k = 0, z2 = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_2)
						sb.append(String.format("+ a[%d] * table_d2f_dz2[%d]\n", getIndex(i, j, k+2), z2++));
		sb.append(";\n");
		// @formatter:on	
		return finaliseInlinePowerTableFunction(sb);
	}
}
