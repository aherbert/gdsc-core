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
	/** Coefficients */
	private final double[] a = new double[64];

	/**
	 * @param aV
	 *            List of spline coefficients.
	 */
	CustomTricubicFunction(double[] aV)
	{
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				for (int i = 0; i < N; i++)
				{
					a[ai++] = aV[i + N * (j + N * k)];
				}
			}
		}
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
		double result = 0;
		
		//		for (int k = 0, ai = 0; k < N; k++)
		//		{
		//			for (int j = 0; j < N; j++)
		//			{
		//				for (int i = 0; i < N; i++)
		//				{
		//					// Used for inlining the computation
		//					System.out.printf("result += a[%d] * pX[%d] * pY[%d] * pZ[%d];\n", ai, i, j, k);
		//					result += a[ai++] * pX[i] * pY[j] * pZ[k];
		//				}
		//			}
		//		}

		// In-line
		result += a[0] * pX[0] * pY[0] * pZ[0];
		result += a[1] * pX[1] * pY[0] * pZ[0];
		result += a[2] * pX[2] * pY[0] * pZ[0];
		result += a[3] * pX[3] * pY[0] * pZ[0];
		result += a[4] * pX[0] * pY[1] * pZ[0];
		result += a[5] * pX[1] * pY[1] * pZ[0];
		result += a[6] * pX[2] * pY[1] * pZ[0];
		result += a[7] * pX[3] * pY[1] * pZ[0];
		result += a[8] * pX[0] * pY[2] * pZ[0];
		result += a[9] * pX[1] * pY[2] * pZ[0];
		result += a[10] * pX[2] * pY[2] * pZ[0];
		result += a[11] * pX[3] * pY[2] * pZ[0];
		result += a[12] * pX[0] * pY[3] * pZ[0];
		result += a[13] * pX[1] * pY[3] * pZ[0];
		result += a[14] * pX[2] * pY[3] * pZ[0];
		result += a[15] * pX[3] * pY[3] * pZ[0];
		result += a[16] * pX[0] * pY[0] * pZ[1];
		result += a[17] * pX[1] * pY[0] * pZ[1];
		result += a[18] * pX[2] * pY[0] * pZ[1];
		result += a[19] * pX[3] * pY[0] * pZ[1];
		result += a[20] * pX[0] * pY[1] * pZ[1];
		result += a[21] * pX[1] * pY[1] * pZ[1];
		result += a[22] * pX[2] * pY[1] * pZ[1];
		result += a[23] * pX[3] * pY[1] * pZ[1];
		result += a[24] * pX[0] * pY[2] * pZ[1];
		result += a[25] * pX[1] * pY[2] * pZ[1];
		result += a[26] * pX[2] * pY[2] * pZ[1];
		result += a[27] * pX[3] * pY[2] * pZ[1];
		result += a[28] * pX[0] * pY[3] * pZ[1];
		result += a[29] * pX[1] * pY[3] * pZ[1];
		result += a[30] * pX[2] * pY[3] * pZ[1];
		result += a[31] * pX[3] * pY[3] * pZ[1];
		result += a[32] * pX[0] * pY[0] * pZ[2];
		result += a[33] * pX[1] * pY[0] * pZ[2];
		result += a[34] * pX[2] * pY[0] * pZ[2];
		result += a[35] * pX[3] * pY[0] * pZ[2];
		result += a[36] * pX[0] * pY[1] * pZ[2];
		result += a[37] * pX[1] * pY[1] * pZ[2];
		result += a[38] * pX[2] * pY[1] * pZ[2];
		result += a[39] * pX[3] * pY[1] * pZ[2];
		result += a[40] * pX[0] * pY[2] * pZ[2];
		result += a[41] * pX[1] * pY[2] * pZ[2];
		result += a[42] * pX[2] * pY[2] * pZ[2];
		result += a[43] * pX[3] * pY[2] * pZ[2];
		result += a[44] * pX[0] * pY[3] * pZ[2];
		result += a[45] * pX[1] * pY[3] * pZ[2];
		result += a[46] * pX[2] * pY[3] * pZ[2];
		result += a[47] * pX[3] * pY[3] * pZ[2];
		result += a[48] * pX[0] * pY[0] * pZ[3];
		result += a[49] * pX[1] * pY[0] * pZ[3];
		result += a[50] * pX[2] * pY[0] * pZ[3];
		result += a[51] * pX[3] * pY[0] * pZ[3];
		result += a[52] * pX[0] * pY[1] * pZ[3];
		result += a[53] * pX[1] * pY[1] * pZ[3];
		result += a[54] * pX[2] * pY[1] * pZ[3];
		result += a[55] * pX[3] * pY[1] * pZ[3];
		result += a[56] * pX[0] * pY[2] * pZ[3];
		result += a[57] * pX[1] * pY[2] * pZ[3];
		result += a[58] * pX[2] * pY[2] * pZ[3];
		result += a[59] * pX[3] * pY[2] * pZ[3];
		result += a[60] * pX[0] * pY[3] * pZ[3];
		result += a[61] * pX[1] * pY[3] * pZ[3];
		result += a[62] * pX[2] * pY[3] * pZ[3];
		result += a[63] * pX[3] * pY[3] * pZ[3];

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
	public double[] computePowerTable(double x, double y, double z) throws OutOfRangeException
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
	public double[] computePowerTable(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z)
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
		double[] table = new double[64];
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				for (int i = 0; i < N; i++)
				{
					table[ai++] = pX[i] * pY[j] * pZ[k];
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
}