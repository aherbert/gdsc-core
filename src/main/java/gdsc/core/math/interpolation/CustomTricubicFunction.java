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
		final double[] pX = { 1, x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { 1, y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { 1, z, z2, z3 };

		return value(pX, pY, pZ);
	}

	/**
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

		//for (int k = 0, ai = 0; k < N; k++)
		//{
		//	for (int j = 0; j < N; j++)
		//	{
		//		System.out.printf("pYpZ = pY[%d] * pZ[%d];\n", j, k);
		//		pYpZ = pY[j] * pZ[k];
		//		for (int i = 0; i < N; i++)
		//		{
		//			// Used for inlining the computation
		//			System.out.printf("result += a[%d] * pX[%d] * pYpZ;\n", ai, i);
		//			result += a[ai++] * pX[i] * pYpZ;
		//		}
		//	}
		//}

		// In-line
		pYpZ = pY[0] * pZ[0];
		result += a[0] * pX[0] * pYpZ;
		result += a[1] * pX[1] * pYpZ;
		result += a[2] * pX[2] * pYpZ;
		result += a[3] * pX[3] * pYpZ;
		pYpZ = pY[1] * pZ[0];
		result += a[4] * pX[0] * pYpZ;
		result += a[5] * pX[1] * pYpZ;
		result += a[6] * pX[2] * pYpZ;
		result += a[7] * pX[3] * pYpZ;
		pYpZ = pY[2] * pZ[0];
		result += a[8] * pX[0] * pYpZ;
		result += a[9] * pX[1] * pYpZ;
		result += a[10] * pX[2] * pYpZ;
		result += a[11] * pX[3] * pYpZ;
		pYpZ = pY[3] * pZ[0];
		result += a[12] * pX[0] * pYpZ;
		result += a[13] * pX[1] * pYpZ;
		result += a[14] * pX[2] * pYpZ;
		result += a[15] * pX[3] * pYpZ;
		pYpZ = pY[0] * pZ[1];
		result += a[16] * pX[0] * pYpZ;
		result += a[17] * pX[1] * pYpZ;
		result += a[18] * pX[2] * pYpZ;
		result += a[19] * pX[3] * pYpZ;
		pYpZ = pY[1] * pZ[1];
		result += a[20] * pX[0] * pYpZ;
		result += a[21] * pX[1] * pYpZ;
		result += a[22] * pX[2] * pYpZ;
		result += a[23] * pX[3] * pYpZ;
		pYpZ = pY[2] * pZ[1];
		result += a[24] * pX[0] * pYpZ;
		result += a[25] * pX[1] * pYpZ;
		result += a[26] * pX[2] * pYpZ;
		result += a[27] * pX[3] * pYpZ;
		pYpZ = pY[3] * pZ[1];
		result += a[28] * pX[0] * pYpZ;
		result += a[29] * pX[1] * pYpZ;
		result += a[30] * pX[2] * pYpZ;
		result += a[31] * pX[3] * pYpZ;
		pYpZ = pY[0] * pZ[2];
		result += a[32] * pX[0] * pYpZ;
		result += a[33] * pX[1] * pYpZ;
		result += a[34] * pX[2] * pYpZ;
		result += a[35] * pX[3] * pYpZ;
		pYpZ = pY[1] * pZ[2];
		result += a[36] * pX[0] * pYpZ;
		result += a[37] * pX[1] * pYpZ;
		result += a[38] * pX[2] * pYpZ;
		result += a[39] * pX[3] * pYpZ;
		pYpZ = pY[2] * pZ[2];
		result += a[40] * pX[0] * pYpZ;
		result += a[41] * pX[1] * pYpZ;
		result += a[42] * pX[2] * pYpZ;
		result += a[43] * pX[3] * pYpZ;
		pYpZ = pY[3] * pZ[2];
		result += a[44] * pX[0] * pYpZ;
		result += a[45] * pX[1] * pYpZ;
		result += a[46] * pX[2] * pYpZ;
		result += a[47] * pX[3] * pYpZ;
		pYpZ = pY[0] * pZ[3];
		result += a[48] * pX[0] * pYpZ;
		result += a[49] * pX[1] * pYpZ;
		result += a[50] * pX[2] * pYpZ;
		result += a[51] * pX[3] * pYpZ;
		pYpZ = pY[1] * pZ[3];
		result += a[52] * pX[0] * pYpZ;
		result += a[53] * pX[1] * pYpZ;
		result += a[54] * pX[2] * pYpZ;
		result += a[55] * pX[3] * pYpZ;
		pYpZ = pY[2] * pZ[3];
		result += a[56] * pX[0] * pYpZ;
		result += a[57] * pX[1] * pYpZ;
		result += a[58] * pX[2] * pYpZ;
		result += a[59] * pX[3] * pYpZ;
		pYpZ = pY[3] * pZ[3];
		result += a[60] * pX[0] * pYpZ;
		result += a[61] * pX[1] * pYpZ;
		result += a[62] * pX[2] * pYpZ;
		result += a[63] * pX[3] * pYpZ;

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
		final double[] pX = { 1, x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { 1, y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { 1, z, z2, z3 };

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
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				pYpZ = pY[j] * pZ[k];
				for (int i = 0; i < N; i++)
				{
					table[ai++] = pX[i] * pYpZ;
				}
			}
		}
		return table;
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
		final double[] pX = { 1, x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { 1, y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { 1, z, z2, z3 };

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

		// Gradients are described in:
		// Babcock & Zhuang (2017) 
		// Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
		// Scientific Reports 7, Article number: 552

		//		for (int k = 0, ai = 0; k < N; k++)
		//		{
		//			for (int j = 0; j < N; j++)
		//			{
		//				System.out.printf("pYpZ = pY[%d] * pZ[%d];\n", j, k);
		//				for (int i = 0; i < N; i++, ai++)
		//				{
		//					// Used for inlining the computation
		//					System.out.printf("pXpYpZ = pX[%d] * pYpZ;\n", i);
		//					System.out.printf("result += a[%d] * pXpYpZ;\n", ai);
		//					if (i == 0)
		//						System.out.printf("df_da[0] += pXpYpZ * a[%d];\n", getIndex(1, j, k));
		//					else if (i < N_1)
		//						System.out.printf("df_da[0] += %d * pXpYpZ * a[%d];\n", i + 1, getIndex(i + 1, j, k));
		//					if (j == 0)
		//						System.out.printf("df_da[1] += pXpYpZ * a[%d];\n", getIndex(i, 1, k));
		//					else if (j < N_1)
		//						System.out.printf("df_da[1] += %d * pXpYpZ * a[%d];\n", j + 1, getIndex(i, j + 1, k));
		//					if (k == 0)
		//						System.out.printf("df_da[2] += a[%d] * pXpYpZ;\n", getIndex(i, j, 1));
		//					else if (k < N_1)
		//						System.out.printf("df_da[2] += %d * pXpYpZ * a[%d];\n", k + 1, getIndex(i, j, k + 1));
		//
		//					// Formal computation
		//					//pXpYpZ = pX[i] * pY[j] * pZ[k];
		//					//result += a[ai] * pXpYpZ;
		//					//if (i < N_1)
		//					//	df_da[0] += (i + 1) * a[getIndex(i + 1, j, k)] * pXpYpZ;
		//					//if (j < N_1)
		//					//	df_da[1] += (j + 1) * a[getIndex(i, j + 1, k)] * pXpYpZ;
		//					//if (k < N_1)
		//					//	df_da[2] += (k + 1) * a[getIndex(i, j, k + 1)] * pXpYpZ;
		//				}
		//			}
		//		}

		pYpZ = pY[0] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		result += a[0] * pXpYpZ;
		df_da[0] += pXpYpZ * a[1];
		df_da[1] += pXpYpZ * a[4];
		df_da[2] += a[16] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[1] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[2];
		df_da[1] += pXpYpZ * a[5];
		df_da[2] += a[17] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[2] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[3];
		df_da[1] += pXpYpZ * a[6];
		df_da[2] += a[18] * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		result += a[3] * pXpYpZ;
		df_da[1] += pXpYpZ * a[7];
		df_da[2] += a[19] * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		result += a[4] * pXpYpZ;
		df_da[0] += pXpYpZ * a[5];
		df_da[1] += 2 * pXpYpZ * a[8];
		df_da[2] += a[20] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[5] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[6];
		df_da[1] += 2 * pXpYpZ * a[9];
		df_da[2] += a[21] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[6] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[7];
		df_da[1] += 2 * pXpYpZ * a[10];
		df_da[2] += a[22] * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		result += a[7] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[11];
		df_da[2] += a[23] * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		result += a[8] * pXpYpZ;
		df_da[0] += pXpYpZ * a[9];
		df_da[1] += 3 * pXpYpZ * a[12];
		df_da[2] += a[24] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[9] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[10];
		df_da[1] += 3 * pXpYpZ * a[13];
		df_da[2] += a[25] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[10] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[11];
		df_da[1] += 3 * pXpYpZ * a[14];
		df_da[2] += a[26] * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		result += a[11] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[15];
		df_da[2] += a[27] * pXpYpZ;
		pYpZ = pY[3] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		result += a[12] * pXpYpZ;
		df_da[0] += pXpYpZ * a[13];
		df_da[2] += a[28] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[13] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[14];
		df_da[2] += a[29] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[14] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[15];
		df_da[2] += a[30] * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		result += a[15] * pXpYpZ;
		df_da[2] += a[31] * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		result += a[16] * pXpYpZ;
		df_da[0] += pXpYpZ * a[17];
		df_da[1] += pXpYpZ * a[20];
		df_da[2] += 2 * pXpYpZ * a[32];
		pXpYpZ = pX[1] * pYpZ;
		result += a[17] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[18];
		df_da[1] += pXpYpZ * a[21];
		df_da[2] += 2 * pXpYpZ * a[33];
		pXpYpZ = pX[2] * pYpZ;
		result += a[18] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[19];
		df_da[1] += pXpYpZ * a[22];
		df_da[2] += 2 * pXpYpZ * a[34];
		pXpYpZ = pX[3] * pYpZ;
		result += a[19] * pXpYpZ;
		df_da[1] += pXpYpZ * a[23];
		df_da[2] += 2 * pXpYpZ * a[35];
		pYpZ = pY[1] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		result += a[20] * pXpYpZ;
		df_da[0] += pXpYpZ * a[21];
		df_da[1] += 2 * pXpYpZ * a[24];
		df_da[2] += 2 * pXpYpZ * a[36];
		pXpYpZ = pX[1] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[22];
		df_da[1] += 2 * pXpYpZ * a[25];
		df_da[2] += 2 * pXpYpZ * a[37];
		pXpYpZ = pX[2] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[23];
		df_da[1] += 2 * pXpYpZ * a[26];
		df_da[2] += 2 * pXpYpZ * a[38];
		pXpYpZ = pX[3] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[27];
		df_da[2] += 2 * pXpYpZ * a[39];
		pYpZ = pY[2] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		result += a[24] * pXpYpZ;
		df_da[0] += pXpYpZ * a[25];
		df_da[1] += 3 * pXpYpZ * a[28];
		df_da[2] += 2 * pXpYpZ * a[40];
		pXpYpZ = pX[1] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[26];
		df_da[1] += 3 * pXpYpZ * a[29];
		df_da[2] += 2 * pXpYpZ * a[41];
		pXpYpZ = pX[2] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[27];
		df_da[1] += 3 * pXpYpZ * a[30];
		df_da[2] += 2 * pXpYpZ * a[42];
		pXpYpZ = pX[3] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[43];
		pYpZ = pY[3] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		result += a[28] * pXpYpZ;
		df_da[0] += pXpYpZ * a[29];
		df_da[2] += 2 * pXpYpZ * a[44];
		pXpYpZ = pX[1] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[30];
		df_da[2] += 2 * pXpYpZ * a[45];
		pXpYpZ = pX[2] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[31];
		df_da[2] += 2 * pXpYpZ * a[46];
		pXpYpZ = pX[3] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += 2 * pXpYpZ * a[47];
		pYpZ = pY[0] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		result += a[32] * pXpYpZ;
		df_da[0] += pXpYpZ * a[33];
		df_da[1] += pXpYpZ * a[36];
		df_da[2] += 3 * pXpYpZ * a[48];
		pXpYpZ = pX[1] * pYpZ;
		result += a[33] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[34];
		df_da[1] += pXpYpZ * a[37];
		df_da[2] += 3 * pXpYpZ * a[49];
		pXpYpZ = pX[2] * pYpZ;
		result += a[34] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[35];
		df_da[1] += pXpYpZ * a[38];
		df_da[2] += 3 * pXpYpZ * a[50];
		pXpYpZ = pX[3] * pYpZ;
		result += a[35] * pXpYpZ;
		df_da[1] += pXpYpZ * a[39];
		df_da[2] += 3 * pXpYpZ * a[51];
		pYpZ = pY[1] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		result += a[36] * pXpYpZ;
		df_da[0] += pXpYpZ * a[37];
		df_da[1] += 2 * pXpYpZ * a[40];
		df_da[2] += 3 * pXpYpZ * a[52];
		pXpYpZ = pX[1] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[38];
		df_da[1] += 2 * pXpYpZ * a[41];
		df_da[2] += 3 * pXpYpZ * a[53];
		pXpYpZ = pX[2] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[39];
		df_da[1] += 2 * pXpYpZ * a[42];
		df_da[2] += 3 * pXpYpZ * a[54];
		pXpYpZ = pX[3] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[43];
		df_da[2] += 3 * pXpYpZ * a[55];
		pYpZ = pY[2] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		result += a[40] * pXpYpZ;
		df_da[0] += pXpYpZ * a[41];
		df_da[1] += 3 * pXpYpZ * a[44];
		df_da[2] += 3 * pXpYpZ * a[56];
		pXpYpZ = pX[1] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[42];
		df_da[1] += 3 * pXpYpZ * a[45];
		df_da[2] += 3 * pXpYpZ * a[57];
		pXpYpZ = pX[2] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[43];
		df_da[1] += 3 * pXpYpZ * a[46];
		df_da[2] += 3 * pXpYpZ * a[58];
		pXpYpZ = pX[3] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[59];
		pYpZ = pY[3] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		result += a[44] * pXpYpZ;
		df_da[0] += pXpYpZ * a[45];
		df_da[2] += 3 * pXpYpZ * a[60];
		pXpYpZ = pX[1] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[46];
		df_da[2] += 3 * pXpYpZ * a[61];
		pXpYpZ = pX[2] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[47];
		df_da[2] += 3 * pXpYpZ * a[62];
		pXpYpZ = pX[3] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += 3 * pXpYpZ * a[63];
		pYpZ = pY[0] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		result += a[48] * pXpYpZ;
		df_da[0] += pXpYpZ * a[49];
		df_da[1] += pXpYpZ * a[52];
		pXpYpZ = pX[1] * pYpZ;
		result += a[49] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[50];
		df_da[1] += pXpYpZ * a[53];
		pXpYpZ = pX[2] * pYpZ;
		result += a[50] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[51];
		df_da[1] += pXpYpZ * a[54];
		pXpYpZ = pX[3] * pYpZ;
		result += a[51] * pXpYpZ;
		df_da[1] += pXpYpZ * a[55];
		pYpZ = pY[1] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		result += a[52] * pXpYpZ;
		df_da[0] += pXpYpZ * a[53];
		df_da[1] += 2 * pXpYpZ * a[56];
		pXpYpZ = pX[1] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[54];
		df_da[1] += 2 * pXpYpZ * a[57];
		pXpYpZ = pX[2] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[55];
		df_da[1] += 2 * pXpYpZ * a[58];
		pXpYpZ = pX[3] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += 2 * pXpYpZ * a[59];
		pYpZ = pY[2] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		result += a[56] * pXpYpZ;
		df_da[0] += pXpYpZ * a[57];
		df_da[1] += 3 * pXpYpZ * a[60];
		pXpYpZ = pX[1] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[58];
		df_da[1] += 3 * pXpYpZ * a[61];
		pXpYpZ = pX[2] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[59];
		df_da[1] += 3 * pXpYpZ * a[62];
		pXpYpZ = pX[3] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += 3 * pXpYpZ * a[63];
		pYpZ = pY[3] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		result += a[60] * pXpYpZ;
		df_da[0] += pXpYpZ * a[61];
		pXpYpZ = pX[1] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += 2 * pXpYpZ * a[62];
		pXpYpZ = pX[2] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += 3 * pXpYpZ * a[63];
		result += a[63] * pX[3] * pYpZ;

		return result;
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
		final double[] pX = { 1, x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { 1, y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { 1, z, z2, z3 };

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

		//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
		//		{
		//			for (int j = 0; j < N; j++)
		//			{
		//				System.out.printf("pYpZ = pY[%d] * pZ[%d];\n", j, k);
		//				pYpZ = pY[j] * pZ[k];
		//				for (int i = 0; i < N; i++, ai++)
		//				{
		//					// Used for inlining the computation
		//					System.out.printf("pXpYpZ = pX[%d] * pYpZ;\n", i);
		//					System.out.printf("table[%d] = pXpYpZ;\n", ai);
		//					if (i == 0)
		//						System.out.printf("table_df_dx[%d] = pXpYpZ;\n", x1);
		//					else if (i < N_1)
		//						System.out.printf("table_df_dx[%d] = %d * pXpYpZ;\n", x1, i + 1);
		//					if (j == 0)
		//						System.out.printf("table_df_dy[%d] = pXpYpZ;\n", y1);
		//					else if (j < N_1)
		//						System.out.printf("table_df_dy[%d] = %d * pXpYpZ;\n", y1, j + 1);
		//					if (k == 0)
		//						System.out.printf("table_df_dz[%d] = pXpYpZ;\n", z1);
		//					else if (k < N_1)
		//						System.out.printf("table_df_dz[%d] = %d * pXpYpZ;\n", z1, k + 1);
		//
		//					pXpYpZ = pX[i] * pYpZ;
		//					table[ai] = pXpYpZ;
		//					if (i < N_1)
		//					{
		//						table_df_dx[x1++] = (i + 1) * pXpYpZ;
		//					}
		//					if (j < N_1)
		//					{
		//						table_df_dy[y1++] = (j + 1) * pXpYpZ;
		//					}
		//					if (k < N_1)
		//					{
		//						table_df_dz[z1++] = (k + 1) * pXpYpZ;
		//					}
		//				}
		//			}
		//		}

		pYpZ = pY[0] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		table[0] = pXpYpZ;
		table_df_dx[0] = pXpYpZ;
		table_df_dy[0] = pXpYpZ;
		table_df_dz[0] = pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[1] = pXpYpZ;
		table_df_dx[1] = 2 * pXpYpZ;
		table_df_dy[1] = pXpYpZ;
		table_df_dz[1] = pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[2] = pXpYpZ;
		table_df_dx[2] = 3 * pXpYpZ;
		table_df_dy[2] = pXpYpZ;
		table_df_dz[2] = pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[3] = pXpYpZ;
		table_df_dy[3] = pXpYpZ;
		table_df_dz[3] = pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		table[4] = pXpYpZ;
		table_df_dx[3] = pXpYpZ;
		table_df_dy[4] = 2 * pXpYpZ;
		table_df_dz[4] = pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[5] = pXpYpZ;
		table_df_dx[4] = 2 * pXpYpZ;
		table_df_dy[5] = 2 * pXpYpZ;
		table_df_dz[5] = pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[6] = pXpYpZ;
		table_df_dx[5] = 3 * pXpYpZ;
		table_df_dy[6] = 2 * pXpYpZ;
		table_df_dz[6] = pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[7] = pXpYpZ;
		table_df_dy[7] = 2 * pXpYpZ;
		table_df_dz[7] = pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		table[8] = pXpYpZ;
		table_df_dx[6] = pXpYpZ;
		table_df_dy[8] = 3 * pXpYpZ;
		table_df_dz[8] = pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[9] = pXpYpZ;
		table_df_dx[7] = 2 * pXpYpZ;
		table_df_dy[9] = 3 * pXpYpZ;
		table_df_dz[9] = pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[10] = pXpYpZ;
		table_df_dx[8] = 3 * pXpYpZ;
		table_df_dy[10] = 3 * pXpYpZ;
		table_df_dz[10] = pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[11] = pXpYpZ;
		table_df_dy[11] = 3 * pXpYpZ;
		table_df_dz[11] = pXpYpZ;
		pYpZ = pY[3] * pZ[0];
		pXpYpZ = pX[0] * pYpZ;
		table[12] = pXpYpZ;
		table_df_dx[9] = pXpYpZ;
		table_df_dz[12] = pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[13] = pXpYpZ;
		table_df_dx[10] = 2 * pXpYpZ;
		table_df_dz[13] = pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[14] = pXpYpZ;
		table_df_dx[11] = 3 * pXpYpZ;
		table_df_dz[14] = pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[15] = pXpYpZ;
		table_df_dz[15] = pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		table[16] = pXpYpZ;
		table_df_dx[12] = pXpYpZ;
		table_df_dy[12] = pXpYpZ;
		table_df_dz[16] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[17] = pXpYpZ;
		table_df_dx[13] = 2 * pXpYpZ;
		table_df_dy[13] = pXpYpZ;
		table_df_dz[17] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[18] = pXpYpZ;
		table_df_dx[14] = 3 * pXpYpZ;
		table_df_dy[14] = pXpYpZ;
		table_df_dz[18] = 2 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[19] = pXpYpZ;
		table_df_dy[15] = pXpYpZ;
		table_df_dz[19] = 2 * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		table[20] = pXpYpZ;
		table_df_dx[15] = pXpYpZ;
		table_df_dy[16] = 2 * pXpYpZ;
		table_df_dz[20] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[21] = pXpYpZ;
		table_df_dx[16] = 2 * pXpYpZ;
		table_df_dy[17] = 2 * pXpYpZ;
		table_df_dz[21] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[22] = pXpYpZ;
		table_df_dx[17] = 3 * pXpYpZ;
		table_df_dy[18] = 2 * pXpYpZ;
		table_df_dz[22] = 2 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[23] = pXpYpZ;
		table_df_dy[19] = 2 * pXpYpZ;
		table_df_dz[23] = 2 * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		table[24] = pXpYpZ;
		table_df_dx[18] = pXpYpZ;
		table_df_dy[20] = 3 * pXpYpZ;
		table_df_dz[24] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[25] = pXpYpZ;
		table_df_dx[19] = 2 * pXpYpZ;
		table_df_dy[21] = 3 * pXpYpZ;
		table_df_dz[25] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[26] = pXpYpZ;
		table_df_dx[20] = 3 * pXpYpZ;
		table_df_dy[22] = 3 * pXpYpZ;
		table_df_dz[26] = 2 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[27] = pXpYpZ;
		table_df_dy[23] = 3 * pXpYpZ;
		table_df_dz[27] = 2 * pXpYpZ;
		pYpZ = pY[3] * pZ[1];
		pXpYpZ = pX[0] * pYpZ;
		table[28] = pXpYpZ;
		table_df_dx[21] = pXpYpZ;
		table_df_dz[28] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[29] = pXpYpZ;
		table_df_dx[22] = 2 * pXpYpZ;
		table_df_dz[29] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[30] = pXpYpZ;
		table_df_dx[23] = 3 * pXpYpZ;
		table_df_dz[30] = 2 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[31] = pXpYpZ;
		table_df_dz[31] = 2 * pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		table[32] = pXpYpZ;
		table_df_dx[24] = pXpYpZ;
		table_df_dy[24] = pXpYpZ;
		table_df_dz[32] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[33] = pXpYpZ;
		table_df_dx[25] = 2 * pXpYpZ;
		table_df_dy[25] = pXpYpZ;
		table_df_dz[33] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[34] = pXpYpZ;
		table_df_dx[26] = 3 * pXpYpZ;
		table_df_dy[26] = pXpYpZ;
		table_df_dz[34] = 3 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[35] = pXpYpZ;
		table_df_dy[27] = pXpYpZ;
		table_df_dz[35] = 3 * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		table[36] = pXpYpZ;
		table_df_dx[27] = pXpYpZ;
		table_df_dy[28] = 2 * pXpYpZ;
		table_df_dz[36] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[37] = pXpYpZ;
		table_df_dx[28] = 2 * pXpYpZ;
		table_df_dy[29] = 2 * pXpYpZ;
		table_df_dz[37] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[38] = pXpYpZ;
		table_df_dx[29] = 3 * pXpYpZ;
		table_df_dy[30] = 2 * pXpYpZ;
		table_df_dz[38] = 3 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[39] = pXpYpZ;
		table_df_dy[31] = 2 * pXpYpZ;
		table_df_dz[39] = 3 * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		table[40] = pXpYpZ;
		table_df_dx[30] = pXpYpZ;
		table_df_dy[32] = 3 * pXpYpZ;
		table_df_dz[40] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[41] = pXpYpZ;
		table_df_dx[31] = 2 * pXpYpZ;
		table_df_dy[33] = 3 * pXpYpZ;
		table_df_dz[41] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[42] = pXpYpZ;
		table_df_dx[32] = 3 * pXpYpZ;
		table_df_dy[34] = 3 * pXpYpZ;
		table_df_dz[42] = 3 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[43] = pXpYpZ;
		table_df_dy[35] = 3 * pXpYpZ;
		table_df_dz[43] = 3 * pXpYpZ;
		pYpZ = pY[3] * pZ[2];
		pXpYpZ = pX[0] * pYpZ;
		table[44] = pXpYpZ;
		table_df_dx[33] = pXpYpZ;
		table_df_dz[44] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[45] = pXpYpZ;
		table_df_dx[34] = 2 * pXpYpZ;
		table_df_dz[45] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[46] = pXpYpZ;
		table_df_dx[35] = 3 * pXpYpZ;
		table_df_dz[46] = 3 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[47] = pXpYpZ;
		table_df_dz[47] = 3 * pXpYpZ;
		pYpZ = pY[0] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		table[48] = pXpYpZ;
		table_df_dx[36] = pXpYpZ;
		table_df_dy[36] = pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[49] = pXpYpZ;
		table_df_dx[37] = 2 * pXpYpZ;
		table_df_dy[37] = pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[50] = pXpYpZ;
		table_df_dx[38] = 3 * pXpYpZ;
		table_df_dy[38] = pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[51] = pXpYpZ;
		table_df_dy[39] = pXpYpZ;
		pYpZ = pY[1] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		table[52] = pXpYpZ;
		table_df_dx[39] = pXpYpZ;
		table_df_dy[40] = 2 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[53] = pXpYpZ;
		table_df_dx[40] = 2 * pXpYpZ;
		table_df_dy[41] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[54] = pXpYpZ;
		table_df_dx[41] = 3 * pXpYpZ;
		table_df_dy[42] = 2 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[55] = pXpYpZ;
		table_df_dy[43] = 2 * pXpYpZ;
		pYpZ = pY[2] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		table[56] = pXpYpZ;
		table_df_dx[42] = pXpYpZ;
		table_df_dy[44] = 3 * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[57] = pXpYpZ;
		table_df_dx[43] = 2 * pXpYpZ;
		table_df_dy[45] = 3 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[58] = pXpYpZ;
		table_df_dx[44] = 3 * pXpYpZ;
		table_df_dy[46] = 3 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[59] = pXpYpZ;
		table_df_dy[47] = 3 * pXpYpZ;
		pYpZ = pY[3] * pZ[3];
		pXpYpZ = pX[0] * pYpZ;
		table[60] = pXpYpZ;
		table_df_dx[45] = pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		table[61] = pXpYpZ;
		table_df_dx[46] = 2 * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		table[62] = pXpYpZ;
		table_df_dx[47] = 3 * pXpYpZ;
		pXpYpZ = pX[3] * pYpZ;
		table[63] = pXpYpZ;

		return new double[][] { table, table_df_dx, table_df_dy, table_df_dz };
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

		//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
		//		{
		//			for (int j = 0; j < N; j++)
		//			{
		//				for (int i = 0; i < N; i++, ai++)
		//				{
		//					System.out.printf("result += a[%d] * table[%d];\n", ai, ai);
		//					if (i < N_1)
		//					{
		//						System.out.printf("df_da[0] += a[%d] * table_df_dx[%d];\n", getIndex(i + 1, j, k), x1);
		//					}
		//					if (j < N_1)
		//					{
		//						System.out.printf("df_da[1] += a[%d] * table_df_dy[%d];\n", getIndex(i, j + 1, k), y1);
		//					}
		//					if (k < N_1)
		//					{
		//						System.out.printf("df_da[2] += a[%d] * table_df_dz[%d];\n", getIndex(i, j, k + 1), z1);
		//					}
		//
		//					result += a[ai] * table[ai];
		//					if (i < N_1)
		//					{
		//						df_da[0] += a[getIndex(i + 1, j, k)] * table_df_dx[x1++];
		//					}
		//					if (j < N_1)
		//					{
		//						df_da[1] += a[getIndex(i, j + 1, k)] * table_df_dy[y1++];
		//					}
		//					if (k < N_1)
		//					{
		//						df_da[2] += a[getIndex(i, j, k + 1)] * table_df_dz[z1++];
		//					}
		//				}
		//			}
		//		}

		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		// @formatter:off
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					System.out.printf("result += a[%d /* [%d][%d][%d] */] * table[%d /* [%d][%d][%d] */];\n", ai, i, j, k, ai, i, j, k);
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					if (i < N_1)
//					{
//						System.out.printf("df_da[0] += a[%d /* [%d][%d][%d] */] * table_df_dx[%d /* [%d][%d][%d] */];\n", getIndex(i + 1, j, k), i+1, j, k, x1++, i, j, k);
//					}
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					if (j < N_1)
//					{
//						System.out.printf("df_da[1] += a[%d /* [%d][%d][%d] */] * table_df_dy[%d /* [%d][%d][%d] */];\n", getIndex(i, j + 1, k), i, j+1, k, y1++, i, j, k);
//					}
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					if (k < N_1)
//					{
//						System.out.printf("df_da[2] += a[%d /* [%d][%d][%d] */] * table_df_dz[%d /* [%d][%d][%d] */];\n", getIndex(i, j, k + 1), i, j, k+1, z1++, i, j, k);
//					}
		// @formatter:on

		result += a[0 /* [0][0][0] */] * table[0 /* [0][0][0] */];
		result += a[1 /* [1][0][0] */] * table[1 /* [1][0][0] */];
		result += a[2 /* [2][0][0] */] * table[2 /* [2][0][0] */];
		result += a[3 /* [3][0][0] */] * table[3 /* [3][0][0] */];
		result += a[4 /* [0][1][0] */] * table[4 /* [0][1][0] */];
		result += a[5 /* [1][1][0] */] * table[5 /* [1][1][0] */];
		result += a[6 /* [2][1][0] */] * table[6 /* [2][1][0] */];
		result += a[7 /* [3][1][0] */] * table[7 /* [3][1][0] */];
		result += a[8 /* [0][2][0] */] * table[8 /* [0][2][0] */];
		result += a[9 /* [1][2][0] */] * table[9 /* [1][2][0] */];
		result += a[10 /* [2][2][0] */] * table[10 /* [2][2][0] */];
		result += a[11 /* [3][2][0] */] * table[11 /* [3][2][0] */];
		result += a[12 /* [0][3][0] */] * table[12 /* [0][3][0] */];
		result += a[13 /* [1][3][0] */] * table[13 /* [1][3][0] */];
		result += a[14 /* [2][3][0] */] * table[14 /* [2][3][0] */];
		result += a[15 /* [3][3][0] */] * table[15 /* [3][3][0] */];
		result += a[16 /* [0][0][1] */] * table[16 /* [0][0][1] */];
		result += a[17 /* [1][0][1] */] * table[17 /* [1][0][1] */];
		result += a[18 /* [2][0][1] */] * table[18 /* [2][0][1] */];
		result += a[19 /* [3][0][1] */] * table[19 /* [3][0][1] */];
		result += a[20 /* [0][1][1] */] * table[20 /* [0][1][1] */];
		result += a[21 /* [1][1][1] */] * table[21 /* [1][1][1] */];
		result += a[22 /* [2][1][1] */] * table[22 /* [2][1][1] */];
		result += a[23 /* [3][1][1] */] * table[23 /* [3][1][1] */];
		result += a[24 /* [0][2][1] */] * table[24 /* [0][2][1] */];
		result += a[25 /* [1][2][1] */] * table[25 /* [1][2][1] */];
		result += a[26 /* [2][2][1] */] * table[26 /* [2][2][1] */];
		result += a[27 /* [3][2][1] */] * table[27 /* [3][2][1] */];
		result += a[28 /* [0][3][1] */] * table[28 /* [0][3][1] */];
		result += a[29 /* [1][3][1] */] * table[29 /* [1][3][1] */];
		result += a[30 /* [2][3][1] */] * table[30 /* [2][3][1] */];
		result += a[31 /* [3][3][1] */] * table[31 /* [3][3][1] */];
		result += a[32 /* [0][0][2] */] * table[32 /* [0][0][2] */];
		result += a[33 /* [1][0][2] */] * table[33 /* [1][0][2] */];
		result += a[34 /* [2][0][2] */] * table[34 /* [2][0][2] */];
		result += a[35 /* [3][0][2] */] * table[35 /* [3][0][2] */];
		result += a[36 /* [0][1][2] */] * table[36 /* [0][1][2] */];
		result += a[37 /* [1][1][2] */] * table[37 /* [1][1][2] */];
		result += a[38 /* [2][1][2] */] * table[38 /* [2][1][2] */];
		result += a[39 /* [3][1][2] */] * table[39 /* [3][1][2] */];
		result += a[40 /* [0][2][2] */] * table[40 /* [0][2][2] */];
		result += a[41 /* [1][2][2] */] * table[41 /* [1][2][2] */];
		result += a[42 /* [2][2][2] */] * table[42 /* [2][2][2] */];
		result += a[43 /* [3][2][2] */] * table[43 /* [3][2][2] */];
		result += a[44 /* [0][3][2] */] * table[44 /* [0][3][2] */];
		result += a[45 /* [1][3][2] */] * table[45 /* [1][3][2] */];
		result += a[46 /* [2][3][2] */] * table[46 /* [2][3][2] */];
		result += a[47 /* [3][3][2] */] * table[47 /* [3][3][2] */];
		result += a[48 /* [0][0][3] */] * table[48 /* [0][0][3] */];
		result += a[49 /* [1][0][3] */] * table[49 /* [1][0][3] */];
		result += a[50 /* [2][0][3] */] * table[50 /* [2][0][3] */];
		result += a[51 /* [3][0][3] */] * table[51 /* [3][0][3] */];
		result += a[52 /* [0][1][3] */] * table[52 /* [0][1][3] */];
		result += a[53 /* [1][1][3] */] * table[53 /* [1][1][3] */];
		result += a[54 /* [2][1][3] */] * table[54 /* [2][1][3] */];
		result += a[55 /* [3][1][3] */] * table[55 /* [3][1][3] */];
		result += a[56 /* [0][2][3] */] * table[56 /* [0][2][3] */];
		result += a[57 /* [1][2][3] */] * table[57 /* [1][2][3] */];
		result += a[58 /* [2][2][3] */] * table[58 /* [2][2][3] */];
		result += a[59 /* [3][2][3] */] * table[59 /* [3][2][3] */];
		result += a[60 /* [0][3][3] */] * table[60 /* [0][3][3] */];
		result += a[61 /* [1][3][3] */] * table[61 /* [1][3][3] */];
		result += a[62 /* [2][3][3] */] * table[62 /* [2][3][3] */];
		result += a[63 /* [3][3][3] */] * table[63 /* [3][3][3] */];
		df_da[0] += a[1 /* [1][0][0] */] * table_df_dx[0 /* [0][0][0] */];
		df_da[0] += a[2 /* [2][0][0] */] * table_df_dx[1 /* [1][0][0] */];
		df_da[0] += a[3 /* [3][0][0] */] * table_df_dx[2 /* [2][0][0] */];
		df_da[0] += a[5 /* [1][1][0] */] * table_df_dx[3 /* [0][1][0] */];
		df_da[0] += a[6 /* [2][1][0] */] * table_df_dx[4 /* [1][1][0] */];
		df_da[0] += a[7 /* [3][1][0] */] * table_df_dx[5 /* [2][1][0] */];
		df_da[0] += a[9 /* [1][2][0] */] * table_df_dx[6 /* [0][2][0] */];
		df_da[0] += a[10 /* [2][2][0] */] * table_df_dx[7 /* [1][2][0] */];
		df_da[0] += a[11 /* [3][2][0] */] * table_df_dx[8 /* [2][2][0] */];
		df_da[0] += a[13 /* [1][3][0] */] * table_df_dx[9 /* [0][3][0] */];
		df_da[0] += a[14 /* [2][3][0] */] * table_df_dx[10 /* [1][3][0] */];
		df_da[0] += a[15 /* [3][3][0] */] * table_df_dx[11 /* [2][3][0] */];
		df_da[0] += a[17 /* [1][0][1] */] * table_df_dx[12 /* [0][0][1] */];
		df_da[0] += a[18 /* [2][0][1] */] * table_df_dx[13 /* [1][0][1] */];
		df_da[0] += a[19 /* [3][0][1] */] * table_df_dx[14 /* [2][0][1] */];
		df_da[0] += a[21 /* [1][1][1] */] * table_df_dx[15 /* [0][1][1] */];
		df_da[0] += a[22 /* [2][1][1] */] * table_df_dx[16 /* [1][1][1] */];
		df_da[0] += a[23 /* [3][1][1] */] * table_df_dx[17 /* [2][1][1] */];
		df_da[0] += a[25 /* [1][2][1] */] * table_df_dx[18 /* [0][2][1] */];
		df_da[0] += a[26 /* [2][2][1] */] * table_df_dx[19 /* [1][2][1] */];
		df_da[0] += a[27 /* [3][2][1] */] * table_df_dx[20 /* [2][2][1] */];
		df_da[0] += a[29 /* [1][3][1] */] * table_df_dx[21 /* [0][3][1] */];
		df_da[0] += a[30 /* [2][3][1] */] * table_df_dx[22 /* [1][3][1] */];
		df_da[0] += a[31 /* [3][3][1] */] * table_df_dx[23 /* [2][3][1] */];
		df_da[0] += a[33 /* [1][0][2] */] * table_df_dx[24 /* [0][0][2] */];
		df_da[0] += a[34 /* [2][0][2] */] * table_df_dx[25 /* [1][0][2] */];
		df_da[0] += a[35 /* [3][0][2] */] * table_df_dx[26 /* [2][0][2] */];
		df_da[0] += a[37 /* [1][1][2] */] * table_df_dx[27 /* [0][1][2] */];
		df_da[0] += a[38 /* [2][1][2] */] * table_df_dx[28 /* [1][1][2] */];
		df_da[0] += a[39 /* [3][1][2] */] * table_df_dx[29 /* [2][1][2] */];
		df_da[0] += a[41 /* [1][2][2] */] * table_df_dx[30 /* [0][2][2] */];
		df_da[0] += a[42 /* [2][2][2] */] * table_df_dx[31 /* [1][2][2] */];
		df_da[0] += a[43 /* [3][2][2] */] * table_df_dx[32 /* [2][2][2] */];
		df_da[0] += a[45 /* [1][3][2] */] * table_df_dx[33 /* [0][3][2] */];
		df_da[0] += a[46 /* [2][3][2] */] * table_df_dx[34 /* [1][3][2] */];
		df_da[0] += a[47 /* [3][3][2] */] * table_df_dx[35 /* [2][3][2] */];
		df_da[0] += a[49 /* [1][0][3] */] * table_df_dx[36 /* [0][0][3] */];
		df_da[0] += a[50 /* [2][0][3] */] * table_df_dx[37 /* [1][0][3] */];
		df_da[0] += a[51 /* [3][0][3] */] * table_df_dx[38 /* [2][0][3] */];
		df_da[0] += a[53 /* [1][1][3] */] * table_df_dx[39 /* [0][1][3] */];
		df_da[0] += a[54 /* [2][1][3] */] * table_df_dx[40 /* [1][1][3] */];
		df_da[0] += a[55 /* [3][1][3] */] * table_df_dx[41 /* [2][1][3] */];
		df_da[0] += a[57 /* [1][2][3] */] * table_df_dx[42 /* [0][2][3] */];
		df_da[0] += a[58 /* [2][2][3] */] * table_df_dx[43 /* [1][2][3] */];
		df_da[0] += a[59 /* [3][2][3] */] * table_df_dx[44 /* [2][2][3] */];
		df_da[0] += a[61 /* [1][3][3] */] * table_df_dx[45 /* [0][3][3] */];
		df_da[0] += a[62 /* [2][3][3] */] * table_df_dx[46 /* [1][3][3] */];
		df_da[0] += a[63 /* [3][3][3] */] * table_df_dx[47 /* [2][3][3] */];
		df_da[1] += a[4 /* [0][1][0] */] * table_df_dy[0 /* [0][0][0] */];
		df_da[1] += a[5 /* [1][1][0] */] * table_df_dy[1 /* [1][0][0] */];
		df_da[1] += a[6 /* [2][1][0] */] * table_df_dy[2 /* [2][0][0] */];
		df_da[1] += a[7 /* [3][1][0] */] * table_df_dy[3 /* [3][0][0] */];
		df_da[1] += a[8 /* [0][2][0] */] * table_df_dy[4 /* [0][1][0] */];
		df_da[1] += a[9 /* [1][2][0] */] * table_df_dy[5 /* [1][1][0] */];
		df_da[1] += a[10 /* [2][2][0] */] * table_df_dy[6 /* [2][1][0] */];
		df_da[1] += a[11 /* [3][2][0] */] * table_df_dy[7 /* [3][1][0] */];
		df_da[1] += a[12 /* [0][3][0] */] * table_df_dy[8 /* [0][2][0] */];
		df_da[1] += a[13 /* [1][3][0] */] * table_df_dy[9 /* [1][2][0] */];
		df_da[1] += a[14 /* [2][3][0] */] * table_df_dy[10 /* [2][2][0] */];
		df_da[1] += a[15 /* [3][3][0] */] * table_df_dy[11 /* [3][2][0] */];
		df_da[1] += a[20 /* [0][1][1] */] * table_df_dy[12 /* [0][0][1] */];
		df_da[1] += a[21 /* [1][1][1] */] * table_df_dy[13 /* [1][0][1] */];
		df_da[1] += a[22 /* [2][1][1] */] * table_df_dy[14 /* [2][0][1] */];
		df_da[1] += a[23 /* [3][1][1] */] * table_df_dy[15 /* [3][0][1] */];
		df_da[1] += a[24 /* [0][2][1] */] * table_df_dy[16 /* [0][1][1] */];
		df_da[1] += a[25 /* [1][2][1] */] * table_df_dy[17 /* [1][1][1] */];
		df_da[1] += a[26 /* [2][2][1] */] * table_df_dy[18 /* [2][1][1] */];
		df_da[1] += a[27 /* [3][2][1] */] * table_df_dy[19 /* [3][1][1] */];
		df_da[1] += a[28 /* [0][3][1] */] * table_df_dy[20 /* [0][2][1] */];
		df_da[1] += a[29 /* [1][3][1] */] * table_df_dy[21 /* [1][2][1] */];
		df_da[1] += a[30 /* [2][3][1] */] * table_df_dy[22 /* [2][2][1] */];
		df_da[1] += a[31 /* [3][3][1] */] * table_df_dy[23 /* [3][2][1] */];
		df_da[1] += a[36 /* [0][1][2] */] * table_df_dy[24 /* [0][0][2] */];
		df_da[1] += a[37 /* [1][1][2] */] * table_df_dy[25 /* [1][0][2] */];
		df_da[1] += a[38 /* [2][1][2] */] * table_df_dy[26 /* [2][0][2] */];
		df_da[1] += a[39 /* [3][1][2] */] * table_df_dy[27 /* [3][0][2] */];
		df_da[1] += a[40 /* [0][2][2] */] * table_df_dy[28 /* [0][1][2] */];
		df_da[1] += a[41 /* [1][2][2] */] * table_df_dy[29 /* [1][1][2] */];
		df_da[1] += a[42 /* [2][2][2] */] * table_df_dy[30 /* [2][1][2] */];
		df_da[1] += a[43 /* [3][2][2] */] * table_df_dy[31 /* [3][1][2] */];
		df_da[1] += a[44 /* [0][3][2] */] * table_df_dy[32 /* [0][2][2] */];
		df_da[1] += a[45 /* [1][3][2] */] * table_df_dy[33 /* [1][2][2] */];
		df_da[1] += a[46 /* [2][3][2] */] * table_df_dy[34 /* [2][2][2] */];
		df_da[1] += a[47 /* [3][3][2] */] * table_df_dy[35 /* [3][2][2] */];
		df_da[1] += a[52 /* [0][1][3] */] * table_df_dy[36 /* [0][0][3] */];
		df_da[1] += a[53 /* [1][1][3] */] * table_df_dy[37 /* [1][0][3] */];
		df_da[1] += a[54 /* [2][1][3] */] * table_df_dy[38 /* [2][0][3] */];
		df_da[1] += a[55 /* [3][1][3] */] * table_df_dy[39 /* [3][0][3] */];
		df_da[1] += a[56 /* [0][2][3] */] * table_df_dy[40 /* [0][1][3] */];
		df_da[1] += a[57 /* [1][2][3] */] * table_df_dy[41 /* [1][1][3] */];
		df_da[1] += a[58 /* [2][2][3] */] * table_df_dy[42 /* [2][1][3] */];
		df_da[1] += a[59 /* [3][2][3] */] * table_df_dy[43 /* [3][1][3] */];
		df_da[1] += a[60 /* [0][3][3] */] * table_df_dy[44 /* [0][2][3] */];
		df_da[1] += a[61 /* [1][3][3] */] * table_df_dy[45 /* [1][2][3] */];
		df_da[1] += a[62 /* [2][3][3] */] * table_df_dy[46 /* [2][2][3] */];
		df_da[1] += a[63 /* [3][3][3] */] * table_df_dy[47 /* [3][2][3] */];
		df_da[2] += a[16 /* [0][0][1] */] * table_df_dz[0 /* [0][0][0] */];
		df_da[2] += a[17 /* [1][0][1] */] * table_df_dz[1 /* [1][0][0] */];
		df_da[2] += a[18 /* [2][0][1] */] * table_df_dz[2 /* [2][0][0] */];
		df_da[2] += a[19 /* [3][0][1] */] * table_df_dz[3 /* [3][0][0] */];
		df_da[2] += a[20 /* [0][1][1] */] * table_df_dz[4 /* [0][1][0] */];
		df_da[2] += a[21 /* [1][1][1] */] * table_df_dz[5 /* [1][1][0] */];
		df_da[2] += a[22 /* [2][1][1] */] * table_df_dz[6 /* [2][1][0] */];
		df_da[2] += a[23 /* [3][1][1] */] * table_df_dz[7 /* [3][1][0] */];
		df_da[2] += a[24 /* [0][2][1] */] * table_df_dz[8 /* [0][2][0] */];
		df_da[2] += a[25 /* [1][2][1] */] * table_df_dz[9 /* [1][2][0] */];
		df_da[2] += a[26 /* [2][2][1] */] * table_df_dz[10 /* [2][2][0] */];
		df_da[2] += a[27 /* [3][2][1] */] * table_df_dz[11 /* [3][2][0] */];
		df_da[2] += a[28 /* [0][3][1] */] * table_df_dz[12 /* [0][3][0] */];
		df_da[2] += a[29 /* [1][3][1] */] * table_df_dz[13 /* [1][3][0] */];
		df_da[2] += a[30 /* [2][3][1] */] * table_df_dz[14 /* [2][3][0] */];
		df_da[2] += a[31 /* [3][3][1] */] * table_df_dz[15 /* [3][3][0] */];
		df_da[2] += a[32 /* [0][0][2] */] * table_df_dz[16 /* [0][0][1] */];
		df_da[2] += a[33 /* [1][0][2] */] * table_df_dz[17 /* [1][0][1] */];
		df_da[2] += a[34 /* [2][0][2] */] * table_df_dz[18 /* [2][0][1] */];
		df_da[2] += a[35 /* [3][0][2] */] * table_df_dz[19 /* [3][0][1] */];
		df_da[2] += a[36 /* [0][1][2] */] * table_df_dz[20 /* [0][1][1] */];
		df_da[2] += a[37 /* [1][1][2] */] * table_df_dz[21 /* [1][1][1] */];
		df_da[2] += a[38 /* [2][1][2] */] * table_df_dz[22 /* [2][1][1] */];
		df_da[2] += a[39 /* [3][1][2] */] * table_df_dz[23 /* [3][1][1] */];
		df_da[2] += a[40 /* [0][2][2] */] * table_df_dz[24 /* [0][2][1] */];
		df_da[2] += a[41 /* [1][2][2] */] * table_df_dz[25 /* [1][2][1] */];
		df_da[2] += a[42 /* [2][2][2] */] * table_df_dz[26 /* [2][2][1] */];
		df_da[2] += a[43 /* [3][2][2] */] * table_df_dz[27 /* [3][2][1] */];
		df_da[2] += a[44 /* [0][3][2] */] * table_df_dz[28 /* [0][3][1] */];
		df_da[2] += a[45 /* [1][3][2] */] * table_df_dz[29 /* [1][3][1] */];
		df_da[2] += a[46 /* [2][3][2] */] * table_df_dz[30 /* [2][3][1] */];
		df_da[2] += a[47 /* [3][3][2] */] * table_df_dz[31 /* [3][3][1] */];
		df_da[2] += a[48 /* [0][0][3] */] * table_df_dz[32 /* [0][0][2] */];
		df_da[2] += a[49 /* [1][0][3] */] * table_df_dz[33 /* [1][0][2] */];
		df_da[2] += a[50 /* [2][0][3] */] * table_df_dz[34 /* [2][0][2] */];
		df_da[2] += a[51 /* [3][0][3] */] * table_df_dz[35 /* [3][0][2] */];
		df_da[2] += a[52 /* [0][1][3] */] * table_df_dz[36 /* [0][1][2] */];
		df_da[2] += a[53 /* [1][1][3] */] * table_df_dz[37 /* [1][1][2] */];
		df_da[2] += a[54 /* [2][1][3] */] * table_df_dz[38 /* [2][1][2] */];
		df_da[2] += a[55 /* [3][1][3] */] * table_df_dz[39 /* [3][1][2] */];
		df_da[2] += a[56 /* [0][2][3] */] * table_df_dz[40 /* [0][2][2] */];
		df_da[2] += a[57 /* [1][2][3] */] * table_df_dz[41 /* [1][2][2] */];
		df_da[2] += a[58 /* [2][2][3] */] * table_df_dz[42 /* [2][2][2] */];
		df_da[2] += a[59 /* [3][2][3] */] * table_df_dz[43 /* [3][2][2] */];
		df_da[2] += a[60 /* [0][3][3] */] * table_df_dz[44 /* [0][3][2] */];
		df_da[2] += a[61 /* [1][3][3] */] * table_df_dz[45 /* [1][3][2] */];
		df_da[2] += a[62 /* [2][3][3] */] * table_df_dz[46 /* [2][3][2] */];
		df_da[2] += a[63 /* [3][3][3] */] * table_df_dz[47 /* [3][3][2] */];

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
		final double[] pX = { 1, x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { 1, y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { 1, z, z2, z3 };

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

		// Gradients are described in:
		// Babcock & Zhuang (2017) 
		// Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
		// Scientific Reports 7, Article number: 552

		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				//						System.out.printf("pYpZ = pY[%d] * pZ[%d];\n", j, k);
				pYpZ = pY[j] * pZ[k];
				for (int i = 0; i < N; i++, ai++)
				{
					//							// Used for inlining the computation
					//							System.out.printf("pXpYpZ = pX[%d] * pYpZ;\n", i);
					//							System.out.printf("result += a[%d] * pXpYpZ;\n", ai);
					//							if (i == 0)
					//								System.out.printf("df_da[0] += pXpYpZ * a[%d];\n", getIndex(1, j, k));
					//							else if (i < N_1)
					//								System.out.printf("df_da[0] += %d * pXpYpZ * a[%d];\n", i + 1, getIndex(i + 1, j, k));
					//							if (j == 0)
					//								System.out.printf("df_da[1] += pXpYpZ * a[%d];\n", getIndex(i, 1, k));
					//							else if (j < N_1)
					//								System.out.printf("df_da[1] += %d * pXpYpZ * a[%d];\n", j + 1, getIndex(i, j + 1, k));
					//							if (k == 0)
					//								System.out.printf("df_da[2] += a[%d] * pXpYpZ;\n", getIndex(i, j, 1));
					//							else if (k < N_1)
					//								System.out.printf("df_da[2] += %d * pXpYpZ * a[%d];\n", k + 1, getIndex(i, j, k + 1));

					// Formal computation
					pXpYpZ = pX[i] * pYpZ;
					result += a[ai] * pXpYpZ;
					if (i < N_1)
					{
						df_da[0] += (i + 1) * a[getIndex(i + 1, j, k)] * pXpYpZ;
						if (i < N_2)
							d2f_da2[0] += (i + 1) * (i + 2) * a[getIndex(i + 2, j, k)] * pXpYpZ;
					}
					if (j < N_1)
					{
						df_da[1] += (j + 1) * a[getIndex(i, j + 1, k)] * pXpYpZ;
						if (j < N_2)
							d2f_da2[1] += (j + 1) * (j + 2) * a[getIndex(i, j + 2, k)] * pXpYpZ;
					}
					if (k < N_1)
					{
						df_da[2] += (k + 1) * a[getIndex(i, j, k + 1)] * pXpYpZ;
						if (k < N_2)
							d2f_da2[2] += (k + 1) * (k + 2) * a[getIndex(i, j, k + 2)] * pXpYpZ;
					}
				}
			}
		}

		return result;
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
		final double[] pX = { 1, x, x2, x3 };

		final double y2 = y * y;
		final double y3 = y2 * y;
		final double[] pY = { 1, y, y2, y3 };

		final double z2 = z * z;
		final double z3 = z2 * z;
		final double[] pZ = { 1, z, z2, z3 };

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

		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				//System.out.printf("pYpZ = pY[%d] * pZ[%d];\n", j, k);
				pYpZ = pY[j] * pZ[k];
				for (int i = 0; i < N; i++, ai++)
				{
					//							// Used for inlining the computation
					//							System.out.printf("pXpYpZ = pX[%d] * pYpZ;\n", i);
					//							System.out.printf("table[%d] = pXpYpZ;\n", ai);
					//							if (i == 0)
					//								System.out.printf("table_df_dx[%d] = pXpYpZ;\n", x1);
					//							else if (i < N_1)
					//								System.out.printf("table_df_dx[%d] = %d * pXpYpZ;\n", x1, i + 1);
					//							if (j == 0)
					//								System.out.printf("table_df_dy[%d] = pXpYpZ;\n", y1);
					//							else if (j < N_1)
					//								System.out.printf("table_df_dy[%d] = %d * pXpYpZ;\n", y1, j + 1);
					//							if (k == 0)
					//								System.out.printf("table_df_dz[%d] = pXpYpZ;\n", z1);
					//							else if (k < N_1)
					//								System.out.printf("table_df_dz[%d] = %d * pXpYpZ;\n", z1, k + 1);

					pXpYpZ = pX[i] * pYpZ;
					table[ai] = pXpYpZ;
					if (i < N_1)
					{
						table_df_dx[x1++] = (i + 1) * pXpYpZ;
						if (i < N_2)
							table_d2f_dx2[x2++] = (i + 1) * (i + 2) * pXpYpZ;
					}
					if (j < N_1)
					{
						table_df_dy[y1++] = (j + 1) * pXpYpZ;
						if (j < N_2)
							table_d2f_dy2[y2++] = (j + 1) * (j + 2) * pXpYpZ;
					}
					if (k < N_1)
					{
						table_df_dz[z1++] = (k + 1) * pXpYpZ;
						if (k < N_2)
							table_d2f_dz2[z2++] = (k + 1) * (k + 2) * pXpYpZ;
					}
				}
			}
		}

		return new double[][] { table, table_df_dx, table_df_dy, table_df_dz, table_d2f_dx2, table_d2f_dy2,
				table_d2f_dz2 };
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

		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				for (int i = 0; i < N; i++, ai++)
				{
					//							System.out.printf("result += a[%d] * table[%d];\n", ai, ai);
					//							if (i < N_1)
					//							{
					//								System.out.printf("df_da[0] += a[%d] * table_df_dx[%d];\n", getIndex(i + 1, j, k), x1);
					//							}
					//							if (j < N_1)
					//							{
					//								System.out.printf("df_da[1] += a[%d] * table_df_dy[%d];\n", getIndex(i, j + 1, k), y1);
					//							}
					//							if (k < N_1)
					//							{
					//								System.out.printf("df_da[2] += a[%d] * table_df_dz[%d];\n", getIndex(i, j, k + 1), z1);
					//							}

					result += a[ai] * table[ai];
					if (i < N_1)
					{
						df_da[0] += a[getIndex(i + 1, j, k)] * table_df_dx[x1++];
						if (i < N_2)
							d2f_da2[0] += (i + 1) * (i + 2) * a[getIndex(i + 2, j, k)] * table_d2f_dx2[x2++];
					}
					if (j < N_1)
					{
						df_da[1] += a[getIndex(i, j + 1, k)] * table_df_dy[y1++];
						if (j < N_2)
							d2f_da2[1] += (j + 1) * (j + 2) * a[getIndex(i, j + 2, k)] * table_d2f_dy2[y2++];
					}
					if (k < N_1)
					{
						df_da[2] += a[getIndex(i, j, k + 1)] * table_df_dz[z1++];
						if (k < N_2)
							d2f_da2[2] += (k + 1) * (k + 2) * a[getIndex(i, j, k + 2)] * table_d2f_dz2[z2++];
					}
				}
			}
		}

		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		// @formatter:off
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					System.out.printf("result += a[%d /* [%d][%d][%d] */] * table[%d /* [%d][%d][%d] */];\n", ai, i, j, k, ai, i, j, k);
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					if (i < N_1)
//					{
//						System.out.printf("df_da[0] += a[%d /* [%d][%d][%d] */] * table_df_dx[%d /* [%d][%d][%d] */];\n", getIndex(i + 1, j, k), i+1, j, k, x1++, i, j, k);
//					}
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					if (j < N_1)
//					{
//						System.out.printf("df_da[1] += a[%d /* [%d][%d][%d] */] * table_df_dy[%d /* [%d][%d][%d] */];\n", getIndex(i, j + 1, k), i, j+1, k, y1++, i, j, k);
//					}
//		for (int k = 0, ai = 0, x1 = 0, y1 = 0, z1 = 0; k < N; k++)
//			for (int j = 0; j < N; j++)
//				for (int i = 0; i < N; i++, ai++)
//					if (k < N_1)
//					{
//						System.out.printf("df_da[2] += a[%d /* [%d][%d][%d] */] * table_df_dz[%d /* [%d][%d][%d] */];\n", getIndex(i, j, k + 1), i, j, k+1, z1++, i, j, k);
//					}
		// @formatter:on

		return result;
	}
}
