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

import gdsc.core.utils.SimpleArrayUtils;

/**
 * 3D-spline function using double precision float values to store the coefficients.
 */
public class DoubleCustomTricubicFunction extends CustomTricubicFunction
{
	/** Coefficients */
	private final double[] a;
	/** Coefficients multiplied by 2 */
	private double[] a2 = null;
	/** Coefficients multiplied by 3 */
	private double[] a3 = null;
	/** Coefficients multiplied by 6 */
	private double[] a6 = null;

	private double[] getA2()
	{
		double[] data = a2;
		if (data == null)
		{
			a2 = data = scaleA(2);
		}
		return data;
	}

	private double[] getA3()
	{
		double[] data = a3;
		if (data == null)
		{
			a3 = data = scaleA(3);
		}
		return data;
	}

	private double[] getA6()
	{
		double[] data = a6;
		if (data == null)
		{
			a6 = data = scaleA(6);
		}
		return data;
	}

	private double[] scaleA(int n)
	{
		final double[] s = new double[64];
		for (int i = 0; i < 64; i++)
			s[i] = a[i] * n;
		return s;
	}

	/**
	 * @param aV
	 *            List of spline coefficients.
	 */
	DoubleCustomTricubicFunction(double[] aV)
	{
		// Use the table directly
		a = aV;
	}

	DoubleCustomTricubicFunction(float[] aV)
	{
		a = SimpleArrayUtils.toDouble(aV);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#precomputeGradientCoefficients(int)
	 */
	void precomputeGradientCoefficients(int order)
	{
		// Use a switch statement to allow fall through
		switch (order)
		{
			case 2: // Second-order
				getA6();
			case 1: // First-order
				getA3();
				getA2();
			default:
				return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#toSinglePrecision()
	 */
	@Override
	public FloatCustomTricubicFunction toSinglePrecision()
	{
		return new FloatCustomTricubicFunction(a, a2, a3, a6);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#toDoublePrecision()
	 */
	@Override
	public CustomTricubicFunction toDoublePrecision()
	{
		return this;
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
	protected double value0(final double[] pX, final double[] pY, final double[] pZ)
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
	 * Get the value using a pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @return the interpolated value.
	 */
	public double value(double[] table)
	{
		return a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
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
	}

	@Override
	public double value(float[] table)
	{
		return a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value1(double[], double[], double[], double[])
	 */
	@Override
	protected double value1(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da)
	{
		double pYpZ;
		double pXpYpZ;
		double result = 0;
		df_da[0] = 0;
		df_da[1] = 0;
		df_da[2] = 0;

		final double[] a2 = getA2();
		final double[] a3 = getA3();
		result += a[0];
		df_da[0] += a[1];
		df_da[1] += a[4];
		df_da[2] += a[16];
		pXpYpZ = pX[0];
		result += a[1] * pXpYpZ;
		df_da[0] += a2[2] * pXpYpZ;
		df_da[1] += a[5] * pXpYpZ;
		df_da[2] += a[17] * pXpYpZ;
		pXpYpZ = pX[1];
		result += a[2] * pXpYpZ;
		df_da[0] += a3[3] * pXpYpZ;
		df_da[1] += a[6] * pXpYpZ;
		df_da[2] += a[18] * pXpYpZ;
		pXpYpZ = pX[2];
		result += a[3] * pXpYpZ;
		df_da[1] += a[7] * pXpYpZ;
		df_da[2] += a[19] * pXpYpZ;
		result += a[4] * pY[0];
		df_da[0] += a[5] * pY[0];
		df_da[1] += a2[8] * pY[0];
		df_da[2] += a[20] * pY[0];
		pXpYpZ = pX[0] * pY[0];
		result += a[5] * pXpYpZ;
		df_da[0] += a2[6] * pXpYpZ;
		df_da[1] += a2[9] * pXpYpZ;
		df_da[2] += a[21] * pXpYpZ;
		pXpYpZ = pX[1] * pY[0];
		result += a[6] * pXpYpZ;
		df_da[0] += a3[7] * pXpYpZ;
		df_da[1] += a2[10] * pXpYpZ;
		df_da[2] += a[22] * pXpYpZ;
		pXpYpZ = pX[2] * pY[0];
		result += a[7] * pXpYpZ;
		df_da[1] += a2[11] * pXpYpZ;
		df_da[2] += a[23] * pXpYpZ;
		result += a[8] * pY[1];
		df_da[0] += a[9] * pY[1];
		df_da[1] += a3[12] * pY[1];
		df_da[2] += a[24] * pY[1];
		pXpYpZ = pX[0] * pY[1];
		result += a[9] * pXpYpZ;
		df_da[0] += a2[10] * pXpYpZ;
		df_da[1] += a3[13] * pXpYpZ;
		df_da[2] += a[25] * pXpYpZ;
		pXpYpZ = pX[1] * pY[1];
		result += a[10] * pXpYpZ;
		df_da[0] += a3[11] * pXpYpZ;
		df_da[1] += a3[14] * pXpYpZ;
		df_da[2] += a[26] * pXpYpZ;
		pXpYpZ = pX[2] * pY[1];
		result += a[11] * pXpYpZ;
		df_da[1] += a3[15] * pXpYpZ;
		df_da[2] += a[27] * pXpYpZ;
		result += a[12] * pY[2];
		df_da[0] += a[13] * pY[2];
		df_da[2] += a[28] * pY[2];
		pXpYpZ = pX[0] * pY[2];
		result += a[13] * pXpYpZ;
		df_da[0] += a2[14] * pXpYpZ;
		df_da[2] += a[29] * pXpYpZ;
		pXpYpZ = pX[1] * pY[2];
		result += a[14] * pXpYpZ;
		df_da[0] += a3[15] * pXpYpZ;
		df_da[2] += a[30] * pXpYpZ;
		pXpYpZ = pX[2] * pY[2];
		result += a[15] * pXpYpZ;
		df_da[2] += a[31] * pXpYpZ;
		result += a[16] * pZ[0];
		df_da[0] += a[17] * pZ[0];
		df_da[1] += a[20] * pZ[0];
		df_da[2] += a2[32] * pZ[0];
		pXpYpZ = pX[0] * pZ[0];
		result += a[17] * pXpYpZ;
		df_da[0] += a2[18] * pXpYpZ;
		df_da[1] += a[21] * pXpYpZ;
		df_da[2] += a2[33] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[0];
		result += a[18] * pXpYpZ;
		df_da[0] += a3[19] * pXpYpZ;
		df_da[1] += a[22] * pXpYpZ;
		df_da[2] += a2[34] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[0];
		result += a[19] * pXpYpZ;
		df_da[1] += a[23] * pXpYpZ;
		df_da[2] += a2[35] * pXpYpZ;
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		df_da[0] += a[21] * pYpZ;
		df_da[1] += a2[24] * pYpZ;
		df_da[2] += a2[36] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += a2[22] * pXpYpZ;
		df_da[1] += a2[25] * pXpYpZ;
		df_da[2] += a2[37] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += a3[23] * pXpYpZ;
		df_da[1] += a2[26] * pXpYpZ;
		df_da[2] += a2[38] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += a2[27] * pXpYpZ;
		df_da[2] += a2[39] * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		df_da[0] += a[25] * pYpZ;
		df_da[1] += a3[28] * pYpZ;
		df_da[2] += a2[40] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += a2[26] * pXpYpZ;
		df_da[1] += a3[29] * pXpYpZ;
		df_da[2] += a2[41] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += a3[27] * pXpYpZ;
		df_da[1] += a3[30] * pXpYpZ;
		df_da[2] += a2[42] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += a3[31] * pXpYpZ;
		df_da[2] += a2[43] * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		df_da[0] += a[29] * pYpZ;
		df_da[2] += a2[44] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += a2[30] * pXpYpZ;
		df_da[2] += a2[45] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += a3[31] * pXpYpZ;
		df_da[2] += a2[46] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += a2[47] * pXpYpZ;
		result += a[32] * pZ[1];
		df_da[0] += a[33] * pZ[1];
		df_da[1] += a[36] * pZ[1];
		df_da[2] += a3[48] * pZ[1];
		pXpYpZ = pX[0] * pZ[1];
		result += a[33] * pXpYpZ;
		df_da[0] += a2[34] * pXpYpZ;
		df_da[1] += a[37] * pXpYpZ;
		df_da[2] += a3[49] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[1];
		result += a[34] * pXpYpZ;
		df_da[0] += a3[35] * pXpYpZ;
		df_da[1] += a[38] * pXpYpZ;
		df_da[2] += a3[50] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[1];
		result += a[35] * pXpYpZ;
		df_da[1] += a[39] * pXpYpZ;
		df_da[2] += a3[51] * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		df_da[0] += a[37] * pYpZ;
		df_da[1] += a2[40] * pYpZ;
		df_da[2] += a3[52] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += a2[38] * pXpYpZ;
		df_da[1] += a2[41] * pXpYpZ;
		df_da[2] += a3[53] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += a3[39] * pXpYpZ;
		df_da[1] += a2[42] * pXpYpZ;
		df_da[2] += a3[54] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += a2[43] * pXpYpZ;
		df_da[2] += a3[55] * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		df_da[0] += a[41] * pYpZ;
		df_da[1] += a3[44] * pYpZ;
		df_da[2] += a3[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += a2[42] * pXpYpZ;
		df_da[1] += a3[45] * pXpYpZ;
		df_da[2] += a3[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += a3[43] * pXpYpZ;
		df_da[1] += a3[46] * pXpYpZ;
		df_da[2] += a3[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += a3[47] * pXpYpZ;
		df_da[2] += a3[59] * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		df_da[0] += a[45] * pYpZ;
		df_da[2] += a3[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += a2[46] * pXpYpZ;
		df_da[2] += a3[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += a3[47] * pXpYpZ;
		df_da[2] += a3[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += a3[63] * pXpYpZ;
		result += a[48] * pZ[2];
		df_da[0] += a[49] * pZ[2];
		df_da[1] += a[52] * pZ[2];
		pXpYpZ = pX[0] * pZ[2];
		result += a[49] * pXpYpZ;
		df_da[0] += a2[50] * pXpYpZ;
		df_da[1] += a[53] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[2];
		result += a[50] * pXpYpZ;
		df_da[0] += a3[51] * pXpYpZ;
		df_da[1] += a[54] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[2];
		result += a[51] * pXpYpZ;
		df_da[1] += a[55] * pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		df_da[0] += a[53] * pYpZ;
		df_da[1] += a2[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += a2[54] * pXpYpZ;
		df_da[1] += a2[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += a3[55] * pXpYpZ;
		df_da[1] += a2[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += a2[59] * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		df_da[0] += a[57] * pYpZ;
		df_da[1] += a3[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += a2[58] * pXpYpZ;
		df_da[1] += a3[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += a3[59] * pXpYpZ;
		df_da[1] += a3[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += a3[63] * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		df_da[0] += a[61] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += a2[62] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += a3[63] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[63] * pXpYpZ;

		return result;
	}

	/**
	 * Compute the value and partial first-order derivatives using pre-computed power table.
	 *
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the interpolated value.
	 */
	public double value(double[] table, double[] df_da)
	{
		final double[] a2 = getA2();
		final double[] a3 = getA3();
		df_da[0] = a[1] * table[0] + a2[2] * table[1] + a3[3] * table[2] + a[5] * table[4] + a2[6] * table[5] +
				a3[7] * table[6] + a[9] * table[8] + a2[10] * table[9] + a3[11] * table[10] + a[13] * table[12] +
				a2[14] * table[13] + a3[15] * table[14] + a[17] * table[16] + a2[18] * table[17] + a3[19] * table[18] +
				a[21] * table[20] + a2[22] * table[21] + a3[23] * table[22] + a[25] * table[24] + a2[26] * table[25] +
				a3[27] * table[26] + a[29] * table[28] + a2[30] * table[29] + a3[31] * table[30] + a[33] * table[32] +
				a2[34] * table[33] + a3[35] * table[34] + a[37] * table[36] + a2[38] * table[37] + a3[39] * table[38] +
				a[41] * table[40] + a2[42] * table[41] + a3[43] * table[42] + a[45] * table[44] + a2[46] * table[45] +
				a3[47] * table[46] + a[49] * table[48] + a2[50] * table[49] + a3[51] * table[50] + a[53] * table[52] +
				a2[54] * table[53] + a3[55] * table[54] + a[57] * table[56] + a2[58] * table[57] + a3[59] * table[58] +
				a[61] * table[60] + a2[62] * table[61] + a3[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a2[8] * table[4] +
				a2[9] * table[5] + a2[10] * table[6] + a2[11] * table[7] + a3[12] * table[8] + a3[13] * table[9] +
				a3[14] * table[10] + a3[15] * table[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a2[24] * table[20] + a2[25] * table[21] + a2[26] * table[22] + a2[27] * table[23] +
				a3[28] * table[24] + a3[29] * table[25] + a3[30] * table[26] + a3[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a2[40] * table[36] + a2[41] * table[37] +
				a2[42] * table[38] + a2[43] * table[39] + a3[44] * table[40] + a3[45] * table[41] + a3[46] * table[42] +
				a3[47] * table[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a2[56] * table[52] + a2[57] * table[53] + a2[58] * table[54] + a2[59] * table[55] + a3[60] * table[56] +
				a3[61] * table[57] + a3[62] * table[58] + a3[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a2[32] * table[16] + a2[33] * table[17] + a2[34] * table[18] + a2[35] * table[19] +
				a2[36] * table[20] + a2[37] * table[21] + a2[38] * table[22] + a2[39] * table[23] + a2[40] * table[24] +
				a2[41] * table[25] + a2[42] * table[26] + a2[43] * table[27] + a2[44] * table[28] + a2[45] * table[29] +
				a2[46] * table[30] + a2[47] * table[31] + a3[48] * table[32] + a3[49] * table[33] + a3[50] * table[34] +
				a3[51] * table[35] + a3[52] * table[36] + a3[53] * table[37] + a3[54] * table[38] + a3[55] * table[39] +
				a3[56] * table[40] + a3[57] * table[41] + a3[58] * table[42] + a3[59] * table[43] + a3[60] * table[44] +
				a3[61] * table[45] + a3[62] * table[46] + a3[63] * table[47];
		return a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
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
	}

	@Override
	public double value(float[] table, double[] df_da)
	{
		final double[] a2 = getA2();
		final double[] a3 = getA3();
		df_da[0] = a[1] * table[0] + a2[2] * table[1] + a3[3] * table[2] + a[5] * table[4] + a2[6] * table[5] +
				a3[7] * table[6] + a[9] * table[8] + a2[10] * table[9] + a3[11] * table[10] + a[13] * table[12] +
				a2[14] * table[13] + a3[15] * table[14] + a[17] * table[16] + a2[18] * table[17] + a3[19] * table[18] +
				a[21] * table[20] + a2[22] * table[21] + a3[23] * table[22] + a[25] * table[24] + a2[26] * table[25] +
				a3[27] * table[26] + a[29] * table[28] + a2[30] * table[29] + a3[31] * table[30] + a[33] * table[32] +
				a2[34] * table[33] + a3[35] * table[34] + a[37] * table[36] + a2[38] * table[37] + a3[39] * table[38] +
				a[41] * table[40] + a2[42] * table[41] + a3[43] * table[42] + a[45] * table[44] + a2[46] * table[45] +
				a3[47] * table[46] + a[49] * table[48] + a2[50] * table[49] + a3[51] * table[50] + a[53] * table[52] +
				a2[54] * table[53] + a3[55] * table[54] + a[57] * table[56] + a2[58] * table[57] + a3[59] * table[58] +
				a[61] * table[60] + a2[62] * table[61] + a3[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a2[8] * table[4] +
				a2[9] * table[5] + a2[10] * table[6] + a2[11] * table[7] + a3[12] * table[8] + a3[13] * table[9] +
				a3[14] * table[10] + a3[15] * table[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a2[24] * table[20] + a2[25] * table[21] + a2[26] * table[22] + a2[27] * table[23] +
				a3[28] * table[24] + a3[29] * table[25] + a3[30] * table[26] + a3[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a2[40] * table[36] + a2[41] * table[37] +
				a2[42] * table[38] + a2[43] * table[39] + a3[44] * table[40] + a3[45] * table[41] + a3[46] * table[42] +
				a3[47] * table[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a2[56] * table[52] + a2[57] * table[53] + a2[58] * table[54] + a2[59] * table[55] + a3[60] * table[56] +
				a3[61] * table[57] + a3[62] * table[58] + a3[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a2[32] * table[16] + a2[33] * table[17] + a2[34] * table[18] + a2[35] * table[19] +
				a2[36] * table[20] + a2[37] * table[21] + a2[38] * table[22] + a2[39] * table[23] + a2[40] * table[24] +
				a2[41] * table[25] + a2[42] * table[26] + a2[43] * table[27] + a2[44] * table[28] + a2[45] * table[29] +
				a2[46] * table[30] + a2[47] * table[31] + a3[48] * table[32] + a3[49] * table[33] + a3[50] * table[34] +
				a3[51] * table[35] + a3[52] * table[36] + a3[53] * table[37] + a3[54] * table[38] + a3[55] * table[39] +
				a3[56] * table[40] + a3[57] * table[41] + a3[58] * table[42] + a3[59] * table[43] + a3[60] * table[44] +
				a3[61] * table[45] + a3[62] * table[46] + a3[63] * table[47];
		return a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value2(double[], double[], double[], double[], double[])
	 */
	@Override
	protected double value2(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da,
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

		final double[] a2 = getA2();
		final double[] a3 = getA3();
		final double[] a6 = getA6();
		result += a[0];
		df_da[0] += a[1];
		d2f_da2[0] += a2[2];
		df_da[1] += a[4];
		d2f_da2[1] += a2[8];
		df_da[2] += a[16];
		d2f_da2[2] += a2[32];
		pXpYpZ = pX[0];
		result += a[1] * pXpYpZ;
		df_da[0] += a2[2] * pXpYpZ;
		d2f_da2[0] += a6[3] * pXpYpZ;
		df_da[1] += a[5] * pXpYpZ;
		d2f_da2[1] += a2[9] * pXpYpZ;
		df_da[2] += a[17] * pXpYpZ;
		d2f_da2[2] += a2[33] * pXpYpZ;
		pXpYpZ = pX[1];
		result += a[2] * pXpYpZ;
		df_da[0] += a3[3] * pXpYpZ;
		df_da[1] += a[6] * pXpYpZ;
		d2f_da2[1] += a2[10] * pXpYpZ;
		df_da[2] += a[18] * pXpYpZ;
		d2f_da2[2] += a2[34] * pXpYpZ;
		pXpYpZ = pX[2];
		result += a[3] * pXpYpZ;
		df_da[1] += a[7] * pXpYpZ;
		d2f_da2[1] += a2[11] * pXpYpZ;
		df_da[2] += a[19] * pXpYpZ;
		d2f_da2[2] += a2[35] * pXpYpZ;
		result += a[4] * pY[0];
		df_da[0] += a[5] * pY[0];
		d2f_da2[0] += a2[6] * pY[0];
		df_da[1] += a2[8] * pY[0];
		d2f_da2[1] += a6[12] * pY[0];
		df_da[2] += a[20] * pY[0];
		d2f_da2[2] += a2[36] * pY[0];
		pXpYpZ = pX[0] * pY[0];
		result += a[5] * pXpYpZ;
		df_da[0] += a2[6] * pXpYpZ;
		d2f_da2[0] += a6[7] * pXpYpZ;
		df_da[1] += a2[9] * pXpYpZ;
		d2f_da2[1] += a6[13] * pXpYpZ;
		df_da[2] += a[21] * pXpYpZ;
		d2f_da2[2] += a2[37] * pXpYpZ;
		pXpYpZ = pX[1] * pY[0];
		result += a[6] * pXpYpZ;
		df_da[0] += a3[7] * pXpYpZ;
		df_da[1] += a2[10] * pXpYpZ;
		d2f_da2[1] += a6[14] * pXpYpZ;
		df_da[2] += a[22] * pXpYpZ;
		d2f_da2[2] += a2[38] * pXpYpZ;
		pXpYpZ = pX[2] * pY[0];
		result += a[7] * pXpYpZ;
		df_da[1] += a2[11] * pXpYpZ;
		d2f_da2[1] += a6[15] * pXpYpZ;
		df_da[2] += a[23] * pXpYpZ;
		d2f_da2[2] += a2[39] * pXpYpZ;
		result += a[8] * pY[1];
		df_da[0] += a[9] * pY[1];
		d2f_da2[0] += a2[10] * pY[1];
		df_da[1] += a3[12] * pY[1];
		df_da[2] += a[24] * pY[1];
		d2f_da2[2] += a2[40] * pY[1];
		pXpYpZ = pX[0] * pY[1];
		result += a[9] * pXpYpZ;
		df_da[0] += a2[10] * pXpYpZ;
		d2f_da2[0] += a6[11] * pXpYpZ;
		df_da[1] += a3[13] * pXpYpZ;
		df_da[2] += a[25] * pXpYpZ;
		d2f_da2[2] += a2[41] * pXpYpZ;
		pXpYpZ = pX[1] * pY[1];
		result += a[10] * pXpYpZ;
		df_da[0] += a3[11] * pXpYpZ;
		df_da[1] += a3[14] * pXpYpZ;
		df_da[2] += a[26] * pXpYpZ;
		d2f_da2[2] += a2[42] * pXpYpZ;
		pXpYpZ = pX[2] * pY[1];
		result += a[11] * pXpYpZ;
		df_da[1] += a3[15] * pXpYpZ;
		df_da[2] += a[27] * pXpYpZ;
		d2f_da2[2] += a2[43] * pXpYpZ;
		result += a[12] * pY[2];
		df_da[0] += a[13] * pY[2];
		d2f_da2[0] += a2[14] * pY[2];
		df_da[2] += a[28] * pY[2];
		d2f_da2[2] += a2[44] * pY[2];
		pXpYpZ = pX[0] * pY[2];
		result += a[13] * pXpYpZ;
		df_da[0] += a2[14] * pXpYpZ;
		d2f_da2[0] += a6[15] * pXpYpZ;
		df_da[2] += a[29] * pXpYpZ;
		d2f_da2[2] += a2[45] * pXpYpZ;
		pXpYpZ = pX[1] * pY[2];
		result += a[14] * pXpYpZ;
		df_da[0] += a3[15] * pXpYpZ;
		df_da[2] += a[30] * pXpYpZ;
		d2f_da2[2] += a2[46] * pXpYpZ;
		pXpYpZ = pX[2] * pY[2];
		result += a[15] * pXpYpZ;
		df_da[2] += a[31] * pXpYpZ;
		d2f_da2[2] += a2[47] * pXpYpZ;
		result += a[16] * pZ[0];
		df_da[0] += a[17] * pZ[0];
		d2f_da2[0] += a2[18] * pZ[0];
		df_da[1] += a[20] * pZ[0];
		d2f_da2[1] += a2[24] * pZ[0];
		df_da[2] += a2[32] * pZ[0];
		d2f_da2[2] += a6[48] * pZ[0];
		pXpYpZ = pX[0] * pZ[0];
		result += a[17] * pXpYpZ;
		df_da[0] += a2[18] * pXpYpZ;
		d2f_da2[0] += a6[19] * pXpYpZ;
		df_da[1] += a[21] * pXpYpZ;
		d2f_da2[1] += a2[25] * pXpYpZ;
		df_da[2] += a2[33] * pXpYpZ;
		d2f_da2[2] += a6[49] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[0];
		result += a[18] * pXpYpZ;
		df_da[0] += a3[19] * pXpYpZ;
		df_da[1] += a[22] * pXpYpZ;
		d2f_da2[1] += a2[26] * pXpYpZ;
		df_da[2] += a2[34] * pXpYpZ;
		d2f_da2[2] += a6[50] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[0];
		result += a[19] * pXpYpZ;
		df_da[1] += a[23] * pXpYpZ;
		d2f_da2[1] += a2[27] * pXpYpZ;
		df_da[2] += a2[35] * pXpYpZ;
		d2f_da2[2] += a6[51] * pXpYpZ;
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		df_da[0] += a[21] * pYpZ;
		d2f_da2[0] += a2[22] * pYpZ;
		df_da[1] += a2[24] * pYpZ;
		d2f_da2[1] += a6[28] * pYpZ;
		df_da[2] += a2[36] * pYpZ;
		d2f_da2[2] += a6[52] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += a2[22] * pXpYpZ;
		d2f_da2[0] += a6[23] * pXpYpZ;
		df_da[1] += a2[25] * pXpYpZ;
		d2f_da2[1] += a6[29] * pXpYpZ;
		df_da[2] += a2[37] * pXpYpZ;
		d2f_da2[2] += a6[53] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += a3[23] * pXpYpZ;
		df_da[1] += a2[26] * pXpYpZ;
		d2f_da2[1] += a6[30] * pXpYpZ;
		df_da[2] += a2[38] * pXpYpZ;
		d2f_da2[2] += a6[54] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += a2[27] * pXpYpZ;
		d2f_da2[1] += a6[31] * pXpYpZ;
		df_da[2] += a2[39] * pXpYpZ;
		d2f_da2[2] += a6[55] * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		df_da[0] += a[25] * pYpZ;
		d2f_da2[0] += a2[26] * pYpZ;
		df_da[1] += a3[28] * pYpZ;
		df_da[2] += a2[40] * pYpZ;
		d2f_da2[2] += a6[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += a2[26] * pXpYpZ;
		d2f_da2[0] += a6[27] * pXpYpZ;
		df_da[1] += a3[29] * pXpYpZ;
		df_da[2] += a2[41] * pXpYpZ;
		d2f_da2[2] += a6[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += a3[27] * pXpYpZ;
		df_da[1] += a3[30] * pXpYpZ;
		df_da[2] += a2[42] * pXpYpZ;
		d2f_da2[2] += a6[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += a3[31] * pXpYpZ;
		df_da[2] += a2[43] * pXpYpZ;
		d2f_da2[2] += a6[59] * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		df_da[0] += a[29] * pYpZ;
		d2f_da2[0] += a2[30] * pYpZ;
		df_da[2] += a2[44] * pYpZ;
		d2f_da2[2] += a6[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += a2[30] * pXpYpZ;
		d2f_da2[0] += a6[31] * pXpYpZ;
		df_da[2] += a2[45] * pXpYpZ;
		d2f_da2[2] += a6[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += a3[31] * pXpYpZ;
		df_da[2] += a2[46] * pXpYpZ;
		d2f_da2[2] += a6[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += a2[47] * pXpYpZ;
		d2f_da2[2] += a6[63] * pXpYpZ;
		result += a[32] * pZ[1];
		df_da[0] += a[33] * pZ[1];
		d2f_da2[0] += a2[34] * pZ[1];
		df_da[1] += a[36] * pZ[1];
		d2f_da2[1] += a2[40] * pZ[1];
		df_da[2] += a3[48] * pZ[1];
		pXpYpZ = pX[0] * pZ[1];
		result += a[33] * pXpYpZ;
		df_da[0] += a2[34] * pXpYpZ;
		d2f_da2[0] += a6[35] * pXpYpZ;
		df_da[1] += a[37] * pXpYpZ;
		d2f_da2[1] += a2[41] * pXpYpZ;
		df_da[2] += a3[49] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[1];
		result += a[34] * pXpYpZ;
		df_da[0] += a3[35] * pXpYpZ;
		df_da[1] += a[38] * pXpYpZ;
		d2f_da2[1] += a2[42] * pXpYpZ;
		df_da[2] += a3[50] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[1];
		result += a[35] * pXpYpZ;
		df_da[1] += a[39] * pXpYpZ;
		d2f_da2[1] += a2[43] * pXpYpZ;
		df_da[2] += a3[51] * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		df_da[0] += a[37] * pYpZ;
		d2f_da2[0] += a2[38] * pYpZ;
		df_da[1] += a2[40] * pYpZ;
		d2f_da2[1] += a6[44] * pYpZ;
		df_da[2] += a3[52] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += a2[38] * pXpYpZ;
		d2f_da2[0] += a6[39] * pXpYpZ;
		df_da[1] += a2[41] * pXpYpZ;
		d2f_da2[1] += a6[45] * pXpYpZ;
		df_da[2] += a3[53] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += a3[39] * pXpYpZ;
		df_da[1] += a2[42] * pXpYpZ;
		d2f_da2[1] += a6[46] * pXpYpZ;
		df_da[2] += a3[54] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += a2[43] * pXpYpZ;
		d2f_da2[1] += a6[47] * pXpYpZ;
		df_da[2] += a3[55] * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		df_da[0] += a[41] * pYpZ;
		d2f_da2[0] += a2[42] * pYpZ;
		df_da[1] += a3[44] * pYpZ;
		df_da[2] += a3[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += a2[42] * pXpYpZ;
		d2f_da2[0] += a6[43] * pXpYpZ;
		df_da[1] += a3[45] * pXpYpZ;
		df_da[2] += a3[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += a3[43] * pXpYpZ;
		df_da[1] += a3[46] * pXpYpZ;
		df_da[2] += a3[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += a3[47] * pXpYpZ;
		df_da[2] += a3[59] * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		df_da[0] += a[45] * pYpZ;
		d2f_da2[0] += a2[46] * pYpZ;
		df_da[2] += a3[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += a2[46] * pXpYpZ;
		d2f_da2[0] += a6[47] * pXpYpZ;
		df_da[2] += a3[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += a3[47] * pXpYpZ;
		df_da[2] += a3[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += a3[63] * pXpYpZ;
		result += a[48] * pZ[2];
		df_da[0] += a[49] * pZ[2];
		d2f_da2[0] += a2[50] * pZ[2];
		df_da[1] += a[52] * pZ[2];
		d2f_da2[1] += a2[56] * pZ[2];
		pXpYpZ = pX[0] * pZ[2];
		result += a[49] * pXpYpZ;
		df_da[0] += a2[50] * pXpYpZ;
		d2f_da2[0] += a6[51] * pXpYpZ;
		df_da[1] += a[53] * pXpYpZ;
		d2f_da2[1] += a2[57] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[2];
		result += a[50] * pXpYpZ;
		df_da[0] += a3[51] * pXpYpZ;
		df_da[1] += a[54] * pXpYpZ;
		d2f_da2[1] += a2[58] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[2];
		result += a[51] * pXpYpZ;
		df_da[1] += a[55] * pXpYpZ;
		d2f_da2[1] += a2[59] * pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		df_da[0] += a[53] * pYpZ;
		d2f_da2[0] += a2[54] * pYpZ;
		df_da[1] += a2[56] * pYpZ;
		d2f_da2[1] += a6[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += a2[54] * pXpYpZ;
		d2f_da2[0] += a6[55] * pXpYpZ;
		df_da[1] += a2[57] * pXpYpZ;
		d2f_da2[1] += a6[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += a3[55] * pXpYpZ;
		df_da[1] += a2[58] * pXpYpZ;
		d2f_da2[1] += a6[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += a2[59] * pXpYpZ;
		d2f_da2[1] += a6[63] * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		df_da[0] += a[57] * pYpZ;
		d2f_da2[0] += a2[58] * pYpZ;
		df_da[1] += a3[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += a2[58] * pXpYpZ;
		d2f_da2[0] += a6[59] * pXpYpZ;
		df_da[1] += a3[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += a3[59] * pXpYpZ;
		df_da[1] += a3[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += a3[63] * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		df_da[0] += a[61] * pYpZ;
		d2f_da2[0] += a2[62] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += a2[62] * pXpYpZ;
		d2f_da2[0] += a6[63] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += a3[63] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[63] * pXpYpZ;

		return result;
	}

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
	public double value(double[] table, double[] df_da, double[] d2f_da2)
	{
		final double[] a2 = getA2();
		final double[] a3 = getA3();
		final double[] a6 = getA6();
		df_da[0] = a[1] * table[0] + a2[2] * table[1] + a3[3] * table[2] + a[5] * table[4] + a2[6] * table[5] +
				a3[7] * table[6] + a[9] * table[8] + a2[10] * table[9] + a3[11] * table[10] + a[13] * table[12] +
				a2[14] * table[13] + a3[15] * table[14] + a[17] * table[16] + a2[18] * table[17] + a3[19] * table[18] +
				a[21] * table[20] + a2[22] * table[21] + a3[23] * table[22] + a[25] * table[24] + a2[26] * table[25] +
				a3[27] * table[26] + a[29] * table[28] + a2[30] * table[29] + a3[31] * table[30] + a[33] * table[32] +
				a2[34] * table[33] + a3[35] * table[34] + a[37] * table[36] + a2[38] * table[37] + a3[39] * table[38] +
				a[41] * table[40] + a2[42] * table[41] + a3[43] * table[42] + a[45] * table[44] + a2[46] * table[45] +
				a3[47] * table[46] + a[49] * table[48] + a2[50] * table[49] + a3[51] * table[50] + a[53] * table[52] +
				a2[54] * table[53] + a3[55] * table[54] + a[57] * table[56] + a2[58] * table[57] + a3[59] * table[58] +
				a[61] * table[60] + a2[62] * table[61] + a3[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a2[8] * table[4] +
				a2[9] * table[5] + a2[10] * table[6] + a2[11] * table[7] + a3[12] * table[8] + a3[13] * table[9] +
				a3[14] * table[10] + a3[15] * table[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a2[24] * table[20] + a2[25] * table[21] + a2[26] * table[22] + a2[27] * table[23] +
				a3[28] * table[24] + a3[29] * table[25] + a3[30] * table[26] + a3[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a2[40] * table[36] + a2[41] * table[37] +
				a2[42] * table[38] + a2[43] * table[39] + a3[44] * table[40] + a3[45] * table[41] + a3[46] * table[42] +
				a3[47] * table[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a2[56] * table[52] + a2[57] * table[53] + a2[58] * table[54] + a2[59] * table[55] + a3[60] * table[56] +
				a3[61] * table[57] + a3[62] * table[58] + a3[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a2[32] * table[16] + a2[33] * table[17] + a2[34] * table[18] + a2[35] * table[19] +
				a2[36] * table[20] + a2[37] * table[21] + a2[38] * table[22] + a2[39] * table[23] + a2[40] * table[24] +
				a2[41] * table[25] + a2[42] * table[26] + a2[43] * table[27] + a2[44] * table[28] + a2[45] * table[29] +
				a2[46] * table[30] + a2[47] * table[31] + a3[48] * table[32] + a3[49] * table[33] + a3[50] * table[34] +
				a3[51] * table[35] + a3[52] * table[36] + a3[53] * table[37] + a3[54] * table[38] + a3[55] * table[39] +
				a3[56] * table[40] + a3[57] * table[41] + a3[58] * table[42] + a3[59] * table[43] + a3[60] * table[44] +
				a3[61] * table[45] + a3[62] * table[46] + a3[63] * table[47];
		d2f_da2[0] = a2[2] * table[0] + a6[3] * table[1] + a2[6] * table[4] + a6[7] * table[5] + a2[10] * table[8] +
				a6[11] * table[9] + a2[14] * table[12] + a6[15] * table[13] + a2[18] * table[16] + a6[19] * table[17] +
				a2[22] * table[20] + a6[23] * table[21] + a2[26] * table[24] + a6[27] * table[25] + a2[30] * table[28] +
				a6[31] * table[29] + a2[34] * table[32] + a6[35] * table[33] + a2[38] * table[36] + a6[39] * table[37] +
				a2[42] * table[40] + a6[43] * table[41] + a2[46] * table[44] + a6[47] * table[45] + a2[50] * table[48] +
				a6[51] * table[49] + a2[54] * table[52] + a6[55] * table[53] + a2[58] * table[56] + a6[59] * table[57] +
				a2[62] * table[60] + a6[63] * table[61];
		d2f_da2[1] = a2[8] * table[0] + a2[9] * table[1] + a2[10] * table[2] + a2[11] * table[3] + a6[12] * table[4] +
				a6[13] * table[5] + a6[14] * table[6] + a6[15] * table[7] + a2[24] * table[16] + a2[25] * table[17] +
				a2[26] * table[18] + a2[27] * table[19] + a6[28] * table[20] + a6[29] * table[21] + a6[30] * table[22] +
				a6[31] * table[23] + a2[40] * table[32] + a2[41] * table[33] + a2[42] * table[34] + a2[43] * table[35] +
				a6[44] * table[36] + a6[45] * table[37] + a6[46] * table[38] + a6[47] * table[39] + a2[56] * table[48] +
				a2[57] * table[49] + a2[58] * table[50] + a2[59] * table[51] + a6[60] * table[52] + a6[61] * table[53] +
				a6[62] * table[54] + a6[63] * table[55];
		d2f_da2[2] = a2[32] * table[0] + a2[33] * table[1] + a2[34] * table[2] + a2[35] * table[3] + a2[36] * table[4] +
				a2[37] * table[5] + a2[38] * table[6] + a2[39] * table[7] + a2[40] * table[8] + a2[41] * table[9] +
				a2[42] * table[10] + a2[43] * table[11] + a2[44] * table[12] + a2[45] * table[13] + a2[46] * table[14] +
				a2[47] * table[15] + a6[48] * table[16] + a6[49] * table[17] + a6[50] * table[18] + a6[51] * table[19] +
				a6[52] * table[20] + a6[53] * table[21] + a6[54] * table[22] + a6[55] * table[23] + a6[56] * table[24] +
				a6[57] * table[25] + a6[58] * table[26] + a6[59] * table[27] + a6[60] * table[28] + a6[61] * table[29] +
				a6[62] * table[30] + a6[63] * table[31];
		return a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
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
	}

	@Override
	public double value(float[] table, double[] df_da, double[] d2f_da2)
	{
		final double[] a2 = getA2();
		final double[] a3 = getA3();
		final double[] a6 = getA6();
		df_da[0] = a[1] * table[0] + a2[2] * table[1] + a3[3] * table[2] + a[5] * table[4] + a2[6] * table[5] +
				a3[7] * table[6] + a[9] * table[8] + a2[10] * table[9] + a3[11] * table[10] + a[13] * table[12] +
				a2[14] * table[13] + a3[15] * table[14] + a[17] * table[16] + a2[18] * table[17] + a3[19] * table[18] +
				a[21] * table[20] + a2[22] * table[21] + a3[23] * table[22] + a[25] * table[24] + a2[26] * table[25] +
				a3[27] * table[26] + a[29] * table[28] + a2[30] * table[29] + a3[31] * table[30] + a[33] * table[32] +
				a2[34] * table[33] + a3[35] * table[34] + a[37] * table[36] + a2[38] * table[37] + a3[39] * table[38] +
				a[41] * table[40] + a2[42] * table[41] + a3[43] * table[42] + a[45] * table[44] + a2[46] * table[45] +
				a3[47] * table[46] + a[49] * table[48] + a2[50] * table[49] + a3[51] * table[50] + a[53] * table[52] +
				a2[54] * table[53] + a3[55] * table[54] + a[57] * table[56] + a2[58] * table[57] + a3[59] * table[58] +
				a[61] * table[60] + a2[62] * table[61] + a3[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a2[8] * table[4] +
				a2[9] * table[5] + a2[10] * table[6] + a2[11] * table[7] + a3[12] * table[8] + a3[13] * table[9] +
				a3[14] * table[10] + a3[15] * table[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a2[24] * table[20] + a2[25] * table[21] + a2[26] * table[22] + a2[27] * table[23] +
				a3[28] * table[24] + a3[29] * table[25] + a3[30] * table[26] + a3[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a2[40] * table[36] + a2[41] * table[37] +
				a2[42] * table[38] + a2[43] * table[39] + a3[44] * table[40] + a3[45] * table[41] + a3[46] * table[42] +
				a3[47] * table[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a2[56] * table[52] + a2[57] * table[53] + a2[58] * table[54] + a2[59] * table[55] + a3[60] * table[56] +
				a3[61] * table[57] + a3[62] * table[58] + a3[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a2[32] * table[16] + a2[33] * table[17] + a2[34] * table[18] + a2[35] * table[19] +
				a2[36] * table[20] + a2[37] * table[21] + a2[38] * table[22] + a2[39] * table[23] + a2[40] * table[24] +
				a2[41] * table[25] + a2[42] * table[26] + a2[43] * table[27] + a2[44] * table[28] + a2[45] * table[29] +
				a2[46] * table[30] + a2[47] * table[31] + a3[48] * table[32] + a3[49] * table[33] + a3[50] * table[34] +
				a3[51] * table[35] + a3[52] * table[36] + a3[53] * table[37] + a3[54] * table[38] + a3[55] * table[39] +
				a3[56] * table[40] + a3[57] * table[41] + a3[58] * table[42] + a3[59] * table[43] + a3[60] * table[44] +
				a3[61] * table[45] + a3[62] * table[46] + a3[63] * table[47];
		d2f_da2[0] = a2[2] * table[0] + a6[3] * table[1] + a2[6] * table[4] + a6[7] * table[5] + a2[10] * table[8] +
				a6[11] * table[9] + a2[14] * table[12] + a6[15] * table[13] + a2[18] * table[16] + a6[19] * table[17] +
				a2[22] * table[20] + a6[23] * table[21] + a2[26] * table[24] + a6[27] * table[25] + a2[30] * table[28] +
				a6[31] * table[29] + a2[34] * table[32] + a6[35] * table[33] + a2[38] * table[36] + a6[39] * table[37] +
				a2[42] * table[40] + a6[43] * table[41] + a2[46] * table[44] + a6[47] * table[45] + a2[50] * table[48] +
				a6[51] * table[49] + a2[54] * table[52] + a6[55] * table[53] + a2[58] * table[56] + a6[59] * table[57] +
				a2[62] * table[60] + a6[63] * table[61];
		d2f_da2[1] = a2[8] * table[0] + a2[9] * table[1] + a2[10] * table[2] + a2[11] * table[3] + a6[12] * table[4] +
				a6[13] * table[5] + a6[14] * table[6] + a6[15] * table[7] + a2[24] * table[16] + a2[25] * table[17] +
				a2[26] * table[18] + a2[27] * table[19] + a6[28] * table[20] + a6[29] * table[21] + a6[30] * table[22] +
				a6[31] * table[23] + a2[40] * table[32] + a2[41] * table[33] + a2[42] * table[34] + a2[43] * table[35] +
				a6[44] * table[36] + a6[45] * table[37] + a6[46] * table[38] + a6[47] * table[39] + a2[56] * table[48] +
				a2[57] * table[49] + a2[58] * table[50] + a2[59] * table[51] + a6[60] * table[52] + a6[61] * table[53] +
				a6[62] * table[54] + a6[63] * table[55];
		d2f_da2[2] = a2[32] * table[0] + a2[33] * table[1] + a2[34] * table[2] + a2[35] * table[3] + a2[36] * table[4] +
				a2[37] * table[5] + a2[38] * table[6] + a2[39] * table[7] + a2[40] * table[8] + a2[41] * table[9] +
				a2[42] * table[10] + a2[43] * table[11] + a2[44] * table[12] + a2[45] * table[13] + a2[46] * table[14] +
				a2[47] * table[15] + a6[48] * table[16] + a6[49] * table[17] + a6[50] * table[18] + a6[51] * table[19] +
				a6[52] * table[20] + a6[53] * table[21] + a6[54] * table[22] + a6[55] * table[23] + a6[56] * table[24] +
				a6[57] * table[25] + a6[58] * table[26] + a6[59] * table[27] + a6[60] * table[28] + a6[61] * table[29] +
				a6[62] * table[30] + a6[63] * table[31];
		return a[0] * table[0] + a[1] * table[1] + a[2] * table[2] + a[3] * table[3] + a[4] * table[4] +
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
	}
}
