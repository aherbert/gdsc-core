/*-
 * %%Ignore-License
 *
 * GDSC Software
 *
 * This is an extension of the
 * org.apache.commons.math3.analysis.interpolation.TricubicFunction
 *
 * Modifications have been made to allow computation of gradients and computation
 * with pre-computed x,y,z powers.
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

/**
 * 3D-spline function using single precision float values to store the coefficients. This reduces the memory required to
 * store the function.
 * <p>
 * Not all computations use exclusively float precision. The computations using the power table use float computation
 * and should show the largest speed benefit over the double precision counter part.
 */
public class FloatCustomTricubicFunction extends CustomTricubicFunction
{
	/** Coefficients */
	private final float[] a;

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#getA()
	 */
	@Override
	public double[] getA()
	{
		return toDouble(a);
	}

	@Override
	public double get(int i)
	{
		return a[i];
	}

	@Override
	public float getf(int i)
	{
		return a[i];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#scale(double)
	 */
	@Override
	public void scale(double scale)
	{
		for (int i = 0; i < 64; i++)
			a[i] *= scale;
	}

	/**
	 * Instantiates a new float custom tricubic function.
	 *
	 * @param aV
	 *            List of spline coefficients.
	 */
	FloatCustomTricubicFunction(double[] aV)
	{
		a = toFloat(aV);
	}

	/**
	 * Instantiates a new float custom tricubic function.
	 *
	 * @param aV
	 *            List of spline coefficients.
	 */
	FloatCustomTricubicFunction(float[] aV)
	{
		a = aV;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#isSinglePrecision()
	 */
	@Override
	public boolean isSinglePrecision()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#toSinglePrecision()
	 */
	@Override
	public FloatCustomTricubicFunction toSinglePrecision()
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#toDoublePrecision()
	 */
	@Override
	public CustomTricubicFunction toDoublePrecision()
	{
		return new DoubleCustomTricubicFunction(a);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#copy()
	 */
	@Override
	public CustomTricubicFunction copy()
	{
		return new FloatCustomTricubicFunction(a.clone());
	}

	// XXX - Copy from DoubleCustomeTricubicFunction after here

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value000()
	 */
	@Override
	public double value000()
	{
		return a[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value0(double[], double[], double[])
	 */
	@Override
	protected double value0(final double[] pX, final double[] pY, final double[] pZ)
	{
		double pZpY;
		double result = 0;

		result += a[0];
		result += pX[0] * a[1];
		result += pX[1] * a[2];
		result += pX[2] * a[3];
		result += pY[0] * a[4];
		result += pY[0] * pX[0] * a[5];
		result += pY[0] * pX[1] * a[6];
		result += pY[0] * pX[2] * a[7];
		result += pY[1] * a[8];
		result += pY[1] * pX[0] * a[9];
		result += pY[1] * pX[1] * a[10];
		result += pY[1] * pX[2] * a[11];
		result += pY[2] * a[12];
		result += pY[2] * pX[0] * a[13];
		result += pY[2] * pX[1] * a[14];
		result += pY[2] * pX[2] * a[15];
		result += pZ[0] * a[16];
		result += pZ[0] * pX[0] * a[17];
		result += pZ[0] * pX[1] * a[18];
		result += pZ[0] * pX[2] * a[19];
		pZpY = pZ[0] * pY[0];
		result += pZpY * a[20];
		result += pZpY * pX[0] * a[21];
		result += pZpY * pX[1] * a[22];
		result += pZpY * pX[2] * a[23];
		pZpY = pZ[0] * pY[1];
		result += pZpY * a[24];
		result += pZpY * pX[0] * a[25];
		result += pZpY * pX[1] * a[26];
		result += pZpY * pX[2] * a[27];
		pZpY = pZ[0] * pY[2];
		result += pZpY * a[28];
		result += pZpY * pX[0] * a[29];
		result += pZpY * pX[1] * a[30];
		result += pZpY * pX[2] * a[31];
		result += pZ[1] * a[32];
		result += pZ[1] * pX[0] * a[33];
		result += pZ[1] * pX[1] * a[34];
		result += pZ[1] * pX[2] * a[35];
		pZpY = pZ[1] * pY[0];
		result += pZpY * a[36];
		result += pZpY * pX[0] * a[37];
		result += pZpY * pX[1] * a[38];
		result += pZpY * pX[2] * a[39];
		pZpY = pZ[1] * pY[1];
		result += pZpY * a[40];
		result += pZpY * pX[0] * a[41];
		result += pZpY * pX[1] * a[42];
		result += pZpY * pX[2] * a[43];
		pZpY = pZ[1] * pY[2];
		result += pZpY * a[44];
		result += pZpY * pX[0] * a[45];
		result += pZpY * pX[1] * a[46];
		result += pZpY * pX[2] * a[47];
		result += pZ[2] * a[48];
		result += pZ[2] * pX[0] * a[49];
		result += pZ[2] * pX[1] * a[50];
		result += pZ[2] * pX[2] * a[51];
		pZpY = pZ[2] * pY[0];
		result += pZpY * a[52];
		result += pZpY * pX[0] * a[53];
		result += pZpY * pX[1] * a[54];
		result += pZpY * pX[2] * a[55];
		pZpY = pZ[2] * pY[1];
		result += pZpY * a[56];
		result += pZpY * pX[0] * a[57];
		result += pZpY * pX[1] * a[58];
		result += pZpY * pX[2] * a[59];
		pZpY = pZ[2] * pY[2];
		result += pZpY * a[60];
		result += pZpY * pX[0] * a[61];
		result += pZpY * pX[1] * a[62];
		result += pZpY * pX[2] * a[63];

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[])
	 */
	@Override
	public double value(double[] table)
	{
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[])
	 */
	@Override
	public double value(float[] table)
	{
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value000(double[])
	 */
	@Override
	public double value000(double[] df_da)
	{
		df_da[0] = a[1];
		df_da[1] = a[4];
		df_da[2] = a[16];
		return a[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value1(double[], double[], double[], double[])
	 */
	@Override
	protected double value1(final double[] pX, final double[] pY, final double[] pZ, final double[] df_da)
	{
		double pZpY;
		double pZpYpX;
		double result = 0;
		df_da[0] = 0;
		df_da[1] = 0;
		df_da[2] = 0;

		result += a[0];
		df_da[0] += a[1];
		df_da[1] += a[4];
		df_da[2] += a[16];
		result += pX[0] * a[1];
		df_da[0] += 2 * pX[0] * a[2];
		df_da[1] += pX[0] * a[5];
		df_da[2] += pX[0] * a[17];
		result += pX[1] * a[2];
		df_da[0] += 3 * pX[1] * a[3];
		df_da[1] += pX[1] * a[6];
		df_da[2] += pX[1] * a[18];
		result += pX[2] * a[3];
		df_da[1] += pX[2] * a[7];
		df_da[2] += pX[2] * a[19];
		result += pY[0] * a[4];
		df_da[0] += pY[0] * a[5];
		df_da[1] += 2 * pY[0] * a[8];
		df_da[2] += pY[0] * a[20];
		pZpYpX = pY[0] * pX[0];
		result += pZpYpX * a[5];
		df_da[0] += 2 * pZpYpX * a[6];
		df_da[1] += 2 * pZpYpX * a[9];
		df_da[2] += pZpYpX * a[21];
		pZpYpX = pY[0] * pX[1];
		result += pZpYpX * a[6];
		df_da[0] += 3 * pZpYpX * a[7];
		df_da[1] += 2 * pZpYpX * a[10];
		df_da[2] += pZpYpX * a[22];
		pZpYpX = pY[0] * pX[2];
		result += pZpYpX * a[7];
		df_da[1] += 2 * pZpYpX * a[11];
		df_da[2] += pZpYpX * a[23];
		result += pY[1] * a[8];
		df_da[0] += pY[1] * a[9];
		df_da[1] += 3 * pY[1] * a[12];
		df_da[2] += pY[1] * a[24];
		pZpYpX = pY[1] * pX[0];
		result += pZpYpX * a[9];
		df_da[0] += 2 * pZpYpX * a[10];
		df_da[1] += 3 * pZpYpX * a[13];
		df_da[2] += pZpYpX * a[25];
		pZpYpX = pY[1] * pX[1];
		result += pZpYpX * a[10];
		df_da[0] += 3 * pZpYpX * a[11];
		df_da[1] += 3 * pZpYpX * a[14];
		df_da[2] += pZpYpX * a[26];
		pZpYpX = pY[1] * pX[2];
		result += pZpYpX * a[11];
		df_da[1] += 3 * pZpYpX * a[15];
		df_da[2] += pZpYpX * a[27];
		result += pY[2] * a[12];
		df_da[0] += pY[2] * a[13];
		df_da[2] += pY[2] * a[28];
		pZpYpX = pY[2] * pX[0];
		result += pZpYpX * a[13];
		df_da[0] += 2 * pZpYpX * a[14];
		df_da[2] += pZpYpX * a[29];
		pZpYpX = pY[2] * pX[1];
		result += pZpYpX * a[14];
		df_da[0] += 3 * pZpYpX * a[15];
		df_da[2] += pZpYpX * a[30];
		pZpYpX = pY[2] * pX[2];
		result += pZpYpX * a[15];
		df_da[2] += pZpYpX * a[31];
		result += pZ[0] * a[16];
		df_da[0] += pZ[0] * a[17];
		df_da[1] += pZ[0] * a[20];
		df_da[2] += 2 * pZ[0] * a[32];
		pZpYpX = pZ[0] * pX[0];
		result += pZpYpX * a[17];
		df_da[0] += 2 * pZpYpX * a[18];
		df_da[1] += pZpYpX * a[21];
		df_da[2] += 2 * pZpYpX * a[33];
		pZpYpX = pZ[0] * pX[1];
		result += pZpYpX * a[18];
		df_da[0] += 3 * pZpYpX * a[19];
		df_da[1] += pZpYpX * a[22];
		df_da[2] += 2 * pZpYpX * a[34];
		pZpYpX = pZ[0] * pX[2];
		result += pZpYpX * a[19];
		df_da[1] += pZpYpX * a[23];
		df_da[2] += 2 * pZpYpX * a[35];
		pZpY = pZ[0] * pY[0];
		result += pZpY * a[20];
		df_da[0] += pZpY * a[21];
		df_da[1] += 2 * pZpY * a[24];
		df_da[2] += 2 * pZpY * a[36];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[21];
		df_da[0] += 2 * pZpYpX * a[22];
		df_da[1] += 2 * pZpYpX * a[25];
		df_da[2] += 2 * pZpYpX * a[37];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[22];
		df_da[0] += 3 * pZpYpX * a[23];
		df_da[1] += 2 * pZpYpX * a[26];
		df_da[2] += 2 * pZpYpX * a[38];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[23];
		df_da[1] += 2 * pZpYpX * a[27];
		df_da[2] += 2 * pZpYpX * a[39];
		pZpY = pZ[0] * pY[1];
		result += pZpY * a[24];
		df_da[0] += pZpY * a[25];
		df_da[1] += 3 * pZpY * a[28];
		df_da[2] += 2 * pZpY * a[40];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[25];
		df_da[0] += 2 * pZpYpX * a[26];
		df_da[1] += 3 * pZpYpX * a[29];
		df_da[2] += 2 * pZpYpX * a[41];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[26];
		df_da[0] += 3 * pZpYpX * a[27];
		df_da[1] += 3 * pZpYpX * a[30];
		df_da[2] += 2 * pZpYpX * a[42];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[27];
		df_da[1] += 3 * pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[43];
		pZpY = pZ[0] * pY[2];
		result += pZpY * a[28];
		df_da[0] += pZpY * a[29];
		df_da[2] += 2 * pZpY * a[44];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[29];
		df_da[0] += 2 * pZpYpX * a[30];
		df_da[2] += 2 * pZpYpX * a[45];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[30];
		df_da[0] += 3 * pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[46];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[47];
		result += pZ[1] * a[32];
		df_da[0] += pZ[1] * a[33];
		df_da[1] += pZ[1] * a[36];
		df_da[2] += 3 * pZ[1] * a[48];
		pZpYpX = pZ[1] * pX[0];
		result += pZpYpX * a[33];
		df_da[0] += 2 * pZpYpX * a[34];
		df_da[1] += pZpYpX * a[37];
		df_da[2] += 3 * pZpYpX * a[49];
		pZpYpX = pZ[1] * pX[1];
		result += pZpYpX * a[34];
		df_da[0] += 3 * pZpYpX * a[35];
		df_da[1] += pZpYpX * a[38];
		df_da[2] += 3 * pZpYpX * a[50];
		pZpYpX = pZ[1] * pX[2];
		result += pZpYpX * a[35];
		df_da[1] += pZpYpX * a[39];
		df_da[2] += 3 * pZpYpX * a[51];
		pZpY = pZ[1] * pY[0];
		result += pZpY * a[36];
		df_da[0] += pZpY * a[37];
		df_da[1] += 2 * pZpY * a[40];
		df_da[2] += 3 * pZpY * a[52];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[37];
		df_da[0] += 2 * pZpYpX * a[38];
		df_da[1] += 2 * pZpYpX * a[41];
		df_da[2] += 3 * pZpYpX * a[53];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[38];
		df_da[0] += 3 * pZpYpX * a[39];
		df_da[1] += 2 * pZpYpX * a[42];
		df_da[2] += 3 * pZpYpX * a[54];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[39];
		df_da[1] += 2 * pZpYpX * a[43];
		df_da[2] += 3 * pZpYpX * a[55];
		pZpY = pZ[1] * pY[1];
		result += pZpY * a[40];
		df_da[0] += pZpY * a[41];
		df_da[1] += 3 * pZpY * a[44];
		df_da[2] += 3 * pZpY * a[56];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[41];
		df_da[0] += 2 * pZpYpX * a[42];
		df_da[1] += 3 * pZpYpX * a[45];
		df_da[2] += 3 * pZpYpX * a[57];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[42];
		df_da[0] += 3 * pZpYpX * a[43];
		df_da[1] += 3 * pZpYpX * a[46];
		df_da[2] += 3 * pZpYpX * a[58];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[43];
		df_da[1] += 3 * pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[59];
		pZpY = pZ[1] * pY[2];
		result += pZpY * a[44];
		df_da[0] += pZpY * a[45];
		df_da[2] += 3 * pZpY * a[60];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[45];
		df_da[0] += 2 * pZpYpX * a[46];
		df_da[2] += 3 * pZpYpX * a[61];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[46];
		df_da[0] += 3 * pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[63];
		result += pZ[2] * a[48];
		df_da[0] += pZ[2] * a[49];
		df_da[1] += pZ[2] * a[52];
		pZpYpX = pZ[2] * pX[0];
		result += pZpYpX * a[49];
		df_da[0] += 2 * pZpYpX * a[50];
		df_da[1] += pZpYpX * a[53];
		pZpYpX = pZ[2] * pX[1];
		result += pZpYpX * a[50];
		df_da[0] += 3 * pZpYpX * a[51];
		df_da[1] += pZpYpX * a[54];
		pZpYpX = pZ[2] * pX[2];
		result += pZpYpX * a[51];
		df_da[1] += pZpYpX * a[55];
		pZpY = pZ[2] * pY[0];
		result += pZpY * a[52];
		df_da[0] += pZpY * a[53];
		df_da[1] += 2 * pZpY * a[56];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[53];
		df_da[0] += 2 * pZpYpX * a[54];
		df_da[1] += 2 * pZpYpX * a[57];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[54];
		df_da[0] += 3 * pZpYpX * a[55];
		df_da[1] += 2 * pZpYpX * a[58];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[55];
		df_da[1] += 2 * pZpYpX * a[59];
		pZpY = pZ[2] * pY[1];
		result += pZpY * a[56];
		df_da[0] += pZpY * a[57];
		df_da[1] += 3 * pZpY * a[60];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[57];
		df_da[0] += 2 * pZpYpX * a[58];
		df_da[1] += 3 * pZpYpX * a[61];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[58];
		df_da[0] += 3 * pZpYpX * a[59];
		df_da[1] += 3 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[59];
		df_da[1] += 3 * pZpYpX * a[63];
		pZpY = pZ[2] * pY[2];
		result += pZpY * a[60];
		df_da[0] += pZpY * a[61];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[61];
		df_da[0] += 2 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[62];
		df_da[0] += 3 * pZpYpX * a[63];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[63];

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[], double[])
	 */
	@Override
	public double value(double[] table, double[] df_da)
	{
		df_da[0] = table[0] * a[1] + 2 * table[1] * a[2] + 3 * table[2] * a[3] + table[4] * a[5] + 2 * table[5] * a[6] +
				3 * table[6] * a[7] + table[8] * a[9] + 2 * table[9] * a[10] + 3 * table[10] * a[11] +
				table[12] * a[13] + 2 * table[13] * a[14] + 3 * table[14] * a[15] + table[16] * a[17] +
				2 * table[17] * a[18] + 3 * table[18] * a[19] + table[20] * a[21] + 2 * table[21] * a[22] +
				3 * table[22] * a[23] + table[24] * a[25] + 2 * table[25] * a[26] + 3 * table[26] * a[27] +
				table[28] * a[29] + 2 * table[29] * a[30] + 3 * table[30] * a[31] + table[32] * a[33] +
				2 * table[33] * a[34] + 3 * table[34] * a[35] + table[36] * a[37] + 2 * table[37] * a[38] +
				3 * table[38] * a[39] + table[40] * a[41] + 2 * table[41] * a[42] + 3 * table[42] * a[43] +
				table[44] * a[45] + 2 * table[45] * a[46] + 3 * table[46] * a[47] + table[48] * a[49] +
				2 * table[49] * a[50] + 3 * table[50] * a[51] + table[52] * a[53] + 2 * table[53] * a[54] +
				3 * table[54] * a[55] + table[56] * a[57] + 2 * table[57] * a[58] + 3 * table[58] * a[59] +
				table[60] * a[61] + 2 * table[61] * a[62] + 3 * table[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + 2 * table[4] * a[8] +
				2 * table[5] * a[9] + 2 * table[6] * a[10] + 2 * table[7] * a[11] + 3 * table[8] * a[12] +
				3 * table[9] * a[13] + 3 * table[10] * a[14] + 3 * table[11] * a[15] + table[16] * a[20] +
				table[17] * a[21] + table[18] * a[22] + table[19] * a[23] + 2 * table[20] * a[24] +
				2 * table[21] * a[25] + 2 * table[22] * a[26] + 2 * table[23] * a[27] + 3 * table[24] * a[28] +
				3 * table[25] * a[29] + 3 * table[26] * a[30] + 3 * table[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + 2 * table[36] * a[40] +
				2 * table[37] * a[41] + 2 * table[38] * a[42] + 2 * table[39] * a[43] + 3 * table[40] * a[44] +
				3 * table[41] * a[45] + 3 * table[42] * a[46] + 3 * table[43] * a[47] + table[48] * a[52] +
				table[49] * a[53] + table[50] * a[54] + table[51] * a[55] + 2 * table[52] * a[56] +
				2 * table[53] * a[57] + 2 * table[54] * a[58] + 2 * table[55] * a[59] + 3 * table[56] * a[60] +
				3 * table[57] * a[61] + 3 * table[58] * a[62] + 3 * table[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + 2 * table[16] * a[32] + 2 * table[17] * a[33] + 2 * table[18] * a[34] +
				2 * table[19] * a[35] + 2 * table[20] * a[36] + 2 * table[21] * a[37] + 2 * table[22] * a[38] +
				2 * table[23] * a[39] + 2 * table[24] * a[40] + 2 * table[25] * a[41] + 2 * table[26] * a[42] +
				2 * table[27] * a[43] + 2 * table[28] * a[44] + 2 * table[29] * a[45] + 2 * table[30] * a[46] +
				2 * table[31] * a[47] + 3 * table[32] * a[48] + 3 * table[33] * a[49] + 3 * table[34] * a[50] +
				3 * table[35] * a[51] + 3 * table[36] * a[52] + 3 * table[37] * a[53] + 3 * table[38] * a[54] +
				3 * table[39] * a[55] + 3 * table[40] * a[56] + 3 * table[41] * a[57] + 3 * table[42] * a[58] +
				3 * table[43] * a[59] + 3 * table[44] * a[60] + 3 * table[45] * a[61] + 3 * table[46] * a[62] +
				3 * table[47] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#gradient(double[], double[])
	 */
	@Override
	public void gradient(double[] table, double[] df_da)
	{
		df_da[0] = table[0] * a[1] + 2 * table[1] * a[2] + 3 * table[2] * a[3] + table[4] * a[5] + 2 * table[5] * a[6] +
				3 * table[6] * a[7] + table[8] * a[9] + 2 * table[9] * a[10] + 3 * table[10] * a[11] +
				table[12] * a[13] + 2 * table[13] * a[14] + 3 * table[14] * a[15] + table[16] * a[17] +
				2 * table[17] * a[18] + 3 * table[18] * a[19] + table[20] * a[21] + 2 * table[21] * a[22] +
				3 * table[22] * a[23] + table[24] * a[25] + 2 * table[25] * a[26] + 3 * table[26] * a[27] +
				table[28] * a[29] + 2 * table[29] * a[30] + 3 * table[30] * a[31] + table[32] * a[33] +
				2 * table[33] * a[34] + 3 * table[34] * a[35] + table[36] * a[37] + 2 * table[37] * a[38] +
				3 * table[38] * a[39] + table[40] * a[41] + 2 * table[41] * a[42] + 3 * table[42] * a[43] +
				table[44] * a[45] + 2 * table[45] * a[46] + 3 * table[46] * a[47] + table[48] * a[49] +
				2 * table[49] * a[50] + 3 * table[50] * a[51] + table[52] * a[53] + 2 * table[53] * a[54] +
				3 * table[54] * a[55] + table[56] * a[57] + 2 * table[57] * a[58] + 3 * table[58] * a[59] +
				table[60] * a[61] + 2 * table[61] * a[62] + 3 * table[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + 2 * table[4] * a[8] +
				2 * table[5] * a[9] + 2 * table[6] * a[10] + 2 * table[7] * a[11] + 3 * table[8] * a[12] +
				3 * table[9] * a[13] + 3 * table[10] * a[14] + 3 * table[11] * a[15] + table[16] * a[20] +
				table[17] * a[21] + table[18] * a[22] + table[19] * a[23] + 2 * table[20] * a[24] +
				2 * table[21] * a[25] + 2 * table[22] * a[26] + 2 * table[23] * a[27] + 3 * table[24] * a[28] +
				3 * table[25] * a[29] + 3 * table[26] * a[30] + 3 * table[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + 2 * table[36] * a[40] +
				2 * table[37] * a[41] + 2 * table[38] * a[42] + 2 * table[39] * a[43] + 3 * table[40] * a[44] +
				3 * table[41] * a[45] + 3 * table[42] * a[46] + 3 * table[43] * a[47] + table[48] * a[52] +
				table[49] * a[53] + table[50] * a[54] + table[51] * a[55] + 2 * table[52] * a[56] +
				2 * table[53] * a[57] + 2 * table[54] * a[58] + 2 * table[55] * a[59] + 3 * table[56] * a[60] +
				3 * table[57] * a[61] + 3 * table[58] * a[62] + 3 * table[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + 2 * table[16] * a[32] + 2 * table[17] * a[33] + 2 * table[18] * a[34] +
				2 * table[19] * a[35] + 2 * table[20] * a[36] + 2 * table[21] * a[37] + 2 * table[22] * a[38] +
				2 * table[23] * a[39] + 2 * table[24] * a[40] + 2 * table[25] * a[41] + 2 * table[26] * a[42] +
				2 * table[27] * a[43] + 2 * table[28] * a[44] + 2 * table[29] * a[45] + 2 * table[30] * a[46] +
				2 * table[31] * a[47] + 3 * table[32] * a[48] + 3 * table[33] * a[49] + 3 * table[34] * a[50] +
				3 * table[35] * a[51] + 3 * table[36] * a[52] + 3 * table[37] * a[53] + 3 * table[38] * a[54] +
				3 * table[39] * a[55] + 3 * table[40] * a[56] + 3 * table[41] * a[57] + 3 * table[42] * a[58] +
				3 * table[43] * a[59] + 3 * table[44] * a[60] + 3 * table[45] * a[61] + 3 * table[46] * a[62] +
				3 * table[47] * a[63];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], double[])
	 */
	@Override
	public double value(float[] table, double[] df_da)
	{
		df_da[0] = table[0] * a[1] + 2 * table[1] * a[2] + 3 * table[2] * a[3] + table[4] * a[5] + 2 * table[5] * a[6] +
				3 * table[6] * a[7] + table[8] * a[9] + 2 * table[9] * a[10] + 3 * table[10] * a[11] +
				table[12] * a[13] + 2 * table[13] * a[14] + 3 * table[14] * a[15] + table[16] * a[17] +
				2 * table[17] * a[18] + 3 * table[18] * a[19] + table[20] * a[21] + 2 * table[21] * a[22] +
				3 * table[22] * a[23] + table[24] * a[25] + 2 * table[25] * a[26] + 3 * table[26] * a[27] +
				table[28] * a[29] + 2 * table[29] * a[30] + 3 * table[30] * a[31] + table[32] * a[33] +
				2 * table[33] * a[34] + 3 * table[34] * a[35] + table[36] * a[37] + 2 * table[37] * a[38] +
				3 * table[38] * a[39] + table[40] * a[41] + 2 * table[41] * a[42] + 3 * table[42] * a[43] +
				table[44] * a[45] + 2 * table[45] * a[46] + 3 * table[46] * a[47] + table[48] * a[49] +
				2 * table[49] * a[50] + 3 * table[50] * a[51] + table[52] * a[53] + 2 * table[53] * a[54] +
				3 * table[54] * a[55] + table[56] * a[57] + 2 * table[57] * a[58] + 3 * table[58] * a[59] +
				table[60] * a[61] + 2 * table[61] * a[62] + 3 * table[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + 2 * table[4] * a[8] +
				2 * table[5] * a[9] + 2 * table[6] * a[10] + 2 * table[7] * a[11] + 3 * table[8] * a[12] +
				3 * table[9] * a[13] + 3 * table[10] * a[14] + 3 * table[11] * a[15] + table[16] * a[20] +
				table[17] * a[21] + table[18] * a[22] + table[19] * a[23] + 2 * table[20] * a[24] +
				2 * table[21] * a[25] + 2 * table[22] * a[26] + 2 * table[23] * a[27] + 3 * table[24] * a[28] +
				3 * table[25] * a[29] + 3 * table[26] * a[30] + 3 * table[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + 2 * table[36] * a[40] +
				2 * table[37] * a[41] + 2 * table[38] * a[42] + 2 * table[39] * a[43] + 3 * table[40] * a[44] +
				3 * table[41] * a[45] + 3 * table[42] * a[46] + 3 * table[43] * a[47] + table[48] * a[52] +
				table[49] * a[53] + table[50] * a[54] + table[51] * a[55] + 2 * table[52] * a[56] +
				2 * table[53] * a[57] + 2 * table[54] * a[58] + 2 * table[55] * a[59] + 3 * table[56] * a[60] +
				3 * table[57] * a[61] + 3 * table[58] * a[62] + 3 * table[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + 2 * table[16] * a[32] + 2 * table[17] * a[33] + 2 * table[18] * a[34] +
				2 * table[19] * a[35] + 2 * table[20] * a[36] + 2 * table[21] * a[37] + 2 * table[22] * a[38] +
				2 * table[23] * a[39] + 2 * table[24] * a[40] + 2 * table[25] * a[41] + 2 * table[26] * a[42] +
				2 * table[27] * a[43] + 2 * table[28] * a[44] + 2 * table[29] * a[45] + 2 * table[30] * a[46] +
				2 * table[31] * a[47] + 3 * table[32] * a[48] + 3 * table[33] * a[49] + 3 * table[34] * a[50] +
				3 * table[35] * a[51] + 3 * table[36] * a[52] + 3 * table[37] * a[53] + 3 * table[38] * a[54] +
				3 * table[39] * a[55] + 3 * table[40] * a[56] + 3 * table[41] * a[57] + 3 * table[42] * a[58] +
				3 * table[43] * a[59] + 3 * table[44] * a[60] + 3 * table[45] * a[61] + 3 * table[46] * a[62] +
				3 * table[47] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#gradient(float[], double[])
	 */
	@Override
	public void gradient(float[] table, double[] df_da)
	{
		df_da[0] = table[0] * a[1] + 2 * table[1] * a[2] + 3 * table[2] * a[3] + table[4] * a[5] + 2 * table[5] * a[6] +
				3 * table[6] * a[7] + table[8] * a[9] + 2 * table[9] * a[10] + 3 * table[10] * a[11] +
				table[12] * a[13] + 2 * table[13] * a[14] + 3 * table[14] * a[15] + table[16] * a[17] +
				2 * table[17] * a[18] + 3 * table[18] * a[19] + table[20] * a[21] + 2 * table[21] * a[22] +
				3 * table[22] * a[23] + table[24] * a[25] + 2 * table[25] * a[26] + 3 * table[26] * a[27] +
				table[28] * a[29] + 2 * table[29] * a[30] + 3 * table[30] * a[31] + table[32] * a[33] +
				2 * table[33] * a[34] + 3 * table[34] * a[35] + table[36] * a[37] + 2 * table[37] * a[38] +
				3 * table[38] * a[39] + table[40] * a[41] + 2 * table[41] * a[42] + 3 * table[42] * a[43] +
				table[44] * a[45] + 2 * table[45] * a[46] + 3 * table[46] * a[47] + table[48] * a[49] +
				2 * table[49] * a[50] + 3 * table[50] * a[51] + table[52] * a[53] + 2 * table[53] * a[54] +
				3 * table[54] * a[55] + table[56] * a[57] + 2 * table[57] * a[58] + 3 * table[58] * a[59] +
				table[60] * a[61] + 2 * table[61] * a[62] + 3 * table[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + 2 * table[4] * a[8] +
				2 * table[5] * a[9] + 2 * table[6] * a[10] + 2 * table[7] * a[11] + 3 * table[8] * a[12] +
				3 * table[9] * a[13] + 3 * table[10] * a[14] + 3 * table[11] * a[15] + table[16] * a[20] +
				table[17] * a[21] + table[18] * a[22] + table[19] * a[23] + 2 * table[20] * a[24] +
				2 * table[21] * a[25] + 2 * table[22] * a[26] + 2 * table[23] * a[27] + 3 * table[24] * a[28] +
				3 * table[25] * a[29] + 3 * table[26] * a[30] + 3 * table[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + 2 * table[36] * a[40] +
				2 * table[37] * a[41] + 2 * table[38] * a[42] + 2 * table[39] * a[43] + 3 * table[40] * a[44] +
				3 * table[41] * a[45] + 3 * table[42] * a[46] + 3 * table[43] * a[47] + table[48] * a[52] +
				table[49] * a[53] + table[50] * a[54] + table[51] * a[55] + 2 * table[52] * a[56] +
				2 * table[53] * a[57] + 2 * table[54] * a[58] + 2 * table[55] * a[59] + 3 * table[56] * a[60] +
				3 * table[57] * a[61] + 3 * table[58] * a[62] + 3 * table[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + 2 * table[16] * a[32] + 2 * table[17] * a[33] + 2 * table[18] * a[34] +
				2 * table[19] * a[35] + 2 * table[20] * a[36] + 2 * table[21] * a[37] + 2 * table[22] * a[38] +
				2 * table[23] * a[39] + 2 * table[24] * a[40] + 2 * table[25] * a[41] + 2 * table[26] * a[42] +
				2 * table[27] * a[43] + 2 * table[28] * a[44] + 2 * table[29] * a[45] + 2 * table[30] * a[46] +
				2 * table[31] * a[47] + 3 * table[32] * a[48] + 3 * table[33] * a[49] + 3 * table[34] * a[50] +
				3 * table[35] * a[51] + 3 * table[36] * a[52] + 3 * table[37] * a[53] + 3 * table[38] * a[54] +
				3 * table[39] * a[55] + 3 * table[40] * a[56] + 3 * table[41] * a[57] + 3 * table[42] * a[58] +
				3 * table[43] * a[59] + 3 * table[44] * a[60] + 3 * table[45] * a[61] + 3 * table[46] * a[62] +
				3 * table[47] * a[63];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[], double[], double[], double[])
	 */
	@Override
	public double value(double[] table, double[] table2, double[] table3, double[] df_da)
	{
		df_da[0] = table[0] * a[1] + table2[1] * a[2] + table3[2] * a[3] + table[4] * a[5] + table2[5] * a[6] +
				table3[6] * a[7] + table[8] * a[9] + table2[9] * a[10] + table3[10] * a[11] + table[12] * a[13] +
				table2[13] * a[14] + table3[14] * a[15] + table[16] * a[17] + table2[17] * a[18] + table3[18] * a[19] +
				table[20] * a[21] + table2[21] * a[22] + table3[22] * a[23] + table[24] * a[25] + table2[25] * a[26] +
				table3[26] * a[27] + table[28] * a[29] + table2[29] * a[30] + table3[30] * a[31] + table[32] * a[33] +
				table2[33] * a[34] + table3[34] * a[35] + table[36] * a[37] + table2[37] * a[38] + table3[38] * a[39] +
				table[40] * a[41] + table2[41] * a[42] + table3[42] * a[43] + table[44] * a[45] + table2[45] * a[46] +
				table3[46] * a[47] + table[48] * a[49] + table2[49] * a[50] + table3[50] * a[51] + table[52] * a[53] +
				table2[53] * a[54] + table3[54] * a[55] + table[56] * a[57] + table2[57] * a[58] + table3[58] * a[59] +
				table[60] * a[61] + table2[61] * a[62] + table3[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + table2[4] * a[8] +
				table2[5] * a[9] + table2[6] * a[10] + table2[7] * a[11] + table3[8] * a[12] + table3[9] * a[13] +
				table3[10] * a[14] + table3[11] * a[15] + table[16] * a[20] + table[17] * a[21] + table[18] * a[22] +
				table[19] * a[23] + table2[20] * a[24] + table2[21] * a[25] + table2[22] * a[26] + table2[23] * a[27] +
				table3[24] * a[28] + table3[25] * a[29] + table3[26] * a[30] + table3[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + table2[36] * a[40] + table2[37] * a[41] +
				table2[38] * a[42] + table2[39] * a[43] + table3[40] * a[44] + table3[41] * a[45] + table3[42] * a[46] +
				table3[43] * a[47] + table[48] * a[52] + table[49] * a[53] + table[50] * a[54] + table[51] * a[55] +
				table2[52] * a[56] + table2[53] * a[57] + table2[54] * a[58] + table2[55] * a[59] + table3[56] * a[60] +
				table3[57] * a[61] + table3[58] * a[62] + table3[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + table2[16] * a[32] + table2[17] * a[33] + table2[18] * a[34] + table2[19] * a[35] +
				table2[20] * a[36] + table2[21] * a[37] + table2[22] * a[38] + table2[23] * a[39] + table2[24] * a[40] +
				table2[25] * a[41] + table2[26] * a[42] + table2[27] * a[43] + table2[28] * a[44] + table2[29] * a[45] +
				table2[30] * a[46] + table2[31] * a[47] + table3[32] * a[48] + table3[33] * a[49] + table3[34] * a[50] +
				table3[35] * a[51] + table3[36] * a[52] + table3[37] * a[53] + table3[38] * a[54] + table3[39] * a[55] +
				table3[40] * a[56] + table3[41] * a[57] + table3[42] * a[58] + table3[43] * a[59] + table3[44] * a[60] +
				table3[45] * a[61] + table3[46] * a[62] + table3[47] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], float[], float[], double[])
	 */
	@Override
	public double value(float[] table, float[] table2, float[] table3, double[] df_da)
	{
		df_da[0] = table[0] * a[1] + table2[1] * a[2] + table3[2] * a[3] + table[4] * a[5] + table2[5] * a[6] +
				table3[6] * a[7] + table[8] * a[9] + table2[9] * a[10] + table3[10] * a[11] + table[12] * a[13] +
				table2[13] * a[14] + table3[14] * a[15] + table[16] * a[17] + table2[17] * a[18] + table3[18] * a[19] +
				table[20] * a[21] + table2[21] * a[22] + table3[22] * a[23] + table[24] * a[25] + table2[25] * a[26] +
				table3[26] * a[27] + table[28] * a[29] + table2[29] * a[30] + table3[30] * a[31] + table[32] * a[33] +
				table2[33] * a[34] + table3[34] * a[35] + table[36] * a[37] + table2[37] * a[38] + table3[38] * a[39] +
				table[40] * a[41] + table2[41] * a[42] + table3[42] * a[43] + table[44] * a[45] + table2[45] * a[46] +
				table3[46] * a[47] + table[48] * a[49] + table2[49] * a[50] + table3[50] * a[51] + table[52] * a[53] +
				table2[53] * a[54] + table3[54] * a[55] + table[56] * a[57] + table2[57] * a[58] + table3[58] * a[59] +
				table[60] * a[61] + table2[61] * a[62] + table3[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + table2[4] * a[8] +
				table2[5] * a[9] + table2[6] * a[10] + table2[7] * a[11] + table3[8] * a[12] + table3[9] * a[13] +
				table3[10] * a[14] + table3[11] * a[15] + table[16] * a[20] + table[17] * a[21] + table[18] * a[22] +
				table[19] * a[23] + table2[20] * a[24] + table2[21] * a[25] + table2[22] * a[26] + table2[23] * a[27] +
				table3[24] * a[28] + table3[25] * a[29] + table3[26] * a[30] + table3[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + table2[36] * a[40] + table2[37] * a[41] +
				table2[38] * a[42] + table2[39] * a[43] + table3[40] * a[44] + table3[41] * a[45] + table3[42] * a[46] +
				table3[43] * a[47] + table[48] * a[52] + table[49] * a[53] + table[50] * a[54] + table[51] * a[55] +
				table2[52] * a[56] + table2[53] * a[57] + table2[54] * a[58] + table2[55] * a[59] + table3[56] * a[60] +
				table3[57] * a[61] + table3[58] * a[62] + table3[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + table2[16] * a[32] + table2[17] * a[33] + table2[18] * a[34] + table2[19] * a[35] +
				table2[20] * a[36] + table2[21] * a[37] + table2[22] * a[38] + table2[23] * a[39] + table2[24] * a[40] +
				table2[25] * a[41] + table2[26] * a[42] + table2[27] * a[43] + table2[28] * a[44] + table2[29] * a[45] +
				table2[30] * a[46] + table2[31] * a[47] + table3[32] * a[48] + table3[33] * a[49] + table3[34] * a[50] +
				table3[35] * a[51] + table3[36] * a[52] + table3[37] * a[53] + table3[38] * a[54] + table3[39] * a[55] +
				table3[40] * a[56] + table3[41] * a[57] + table3[42] * a[58] + table3[43] * a[59] + table3[44] * a[60] +
				table3[45] * a[61] + table3[46] * a[62] + table3[47] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value000(double[], double[])
	 */
	@Override
	public double value000(double[] df_da, double[] d2f_da2)
	{
		df_da[0] = a[1];
		df_da[1] = a[4];
		df_da[2] = a[16];
		d2f_da2[0] = 2 * a[2];
		d2f_da2[1] = 2 * a[8];
		d2f_da2[2] = 2 * a[32];
		return a[0];
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
		double pZpY;
		double pZpYpX;
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
		result += pX[0] * a[1];
		df_da[0] += 2 * pX[0] * a[2];
		d2f_da2[0] += 6 * pX[0] * a[3];
		df_da[1] += pX[0] * a[5];
		d2f_da2[1] += 2 * pX[0] * a[9];
		df_da[2] += pX[0] * a[17];
		d2f_da2[2] += 2 * pX[0] * a[33];
		result += pX[1] * a[2];
		df_da[0] += 3 * pX[1] * a[3];
		df_da[1] += pX[1] * a[6];
		d2f_da2[1] += 2 * pX[1] * a[10];
		df_da[2] += pX[1] * a[18];
		d2f_da2[2] += 2 * pX[1] * a[34];
		result += pX[2] * a[3];
		df_da[1] += pX[2] * a[7];
		d2f_da2[1] += 2 * pX[2] * a[11];
		df_da[2] += pX[2] * a[19];
		d2f_da2[2] += 2 * pX[2] * a[35];
		result += pY[0] * a[4];
		df_da[0] += pY[0] * a[5];
		d2f_da2[0] += 2 * pY[0] * a[6];
		df_da[1] += 2 * pY[0] * a[8];
		d2f_da2[1] += 6 * pY[0] * a[12];
		df_da[2] += pY[0] * a[20];
		d2f_da2[2] += 2 * pY[0] * a[36];
		pZpYpX = pY[0] * pX[0];
		result += pZpYpX * a[5];
		df_da[0] += 2 * pZpYpX * a[6];
		d2f_da2[0] += 6 * pZpYpX * a[7];
		df_da[1] += 2 * pZpYpX * a[9];
		d2f_da2[1] += 6 * pZpYpX * a[13];
		df_da[2] += pZpYpX * a[21];
		d2f_da2[2] += 2 * pZpYpX * a[37];
		pZpYpX = pY[0] * pX[1];
		result += pZpYpX * a[6];
		df_da[0] += 3 * pZpYpX * a[7];
		df_da[1] += 2 * pZpYpX * a[10];
		d2f_da2[1] += 6 * pZpYpX * a[14];
		df_da[2] += pZpYpX * a[22];
		d2f_da2[2] += 2 * pZpYpX * a[38];
		pZpYpX = pY[0] * pX[2];
		result += pZpYpX * a[7];
		df_da[1] += 2 * pZpYpX * a[11];
		d2f_da2[1] += 6 * pZpYpX * a[15];
		df_da[2] += pZpYpX * a[23];
		d2f_da2[2] += 2 * pZpYpX * a[39];
		result += pY[1] * a[8];
		df_da[0] += pY[1] * a[9];
		d2f_da2[0] += 2 * pY[1] * a[10];
		df_da[1] += 3 * pY[1] * a[12];
		df_da[2] += pY[1] * a[24];
		d2f_da2[2] += 2 * pY[1] * a[40];
		pZpYpX = pY[1] * pX[0];
		result += pZpYpX * a[9];
		df_da[0] += 2 * pZpYpX * a[10];
		d2f_da2[0] += 6 * pZpYpX * a[11];
		df_da[1] += 3 * pZpYpX * a[13];
		df_da[2] += pZpYpX * a[25];
		d2f_da2[2] += 2 * pZpYpX * a[41];
		pZpYpX = pY[1] * pX[1];
		result += pZpYpX * a[10];
		df_da[0] += 3 * pZpYpX * a[11];
		df_da[1] += 3 * pZpYpX * a[14];
		df_da[2] += pZpYpX * a[26];
		d2f_da2[2] += 2 * pZpYpX * a[42];
		pZpYpX = pY[1] * pX[2];
		result += pZpYpX * a[11];
		df_da[1] += 3 * pZpYpX * a[15];
		df_da[2] += pZpYpX * a[27];
		d2f_da2[2] += 2 * pZpYpX * a[43];
		result += pY[2] * a[12];
		df_da[0] += pY[2] * a[13];
		d2f_da2[0] += 2 * pY[2] * a[14];
		df_da[2] += pY[2] * a[28];
		d2f_da2[2] += 2 * pY[2] * a[44];
		pZpYpX = pY[2] * pX[0];
		result += pZpYpX * a[13];
		df_da[0] += 2 * pZpYpX * a[14];
		d2f_da2[0] += 6 * pZpYpX * a[15];
		df_da[2] += pZpYpX * a[29];
		d2f_da2[2] += 2 * pZpYpX * a[45];
		pZpYpX = pY[2] * pX[1];
		result += pZpYpX * a[14];
		df_da[0] += 3 * pZpYpX * a[15];
		df_da[2] += pZpYpX * a[30];
		d2f_da2[2] += 2 * pZpYpX * a[46];
		pZpYpX = pY[2] * pX[2];
		result += pZpYpX * a[15];
		df_da[2] += pZpYpX * a[31];
		d2f_da2[2] += 2 * pZpYpX * a[47];
		result += pZ[0] * a[16];
		df_da[0] += pZ[0] * a[17];
		d2f_da2[0] += 2 * pZ[0] * a[18];
		df_da[1] += pZ[0] * a[20];
		d2f_da2[1] += 2 * pZ[0] * a[24];
		df_da[2] += 2 * pZ[0] * a[32];
		d2f_da2[2] += 6 * pZ[0] * a[48];
		pZpYpX = pZ[0] * pX[0];
		result += pZpYpX * a[17];
		df_da[0] += 2 * pZpYpX * a[18];
		d2f_da2[0] += 6 * pZpYpX * a[19];
		df_da[1] += pZpYpX * a[21];
		d2f_da2[1] += 2 * pZpYpX * a[25];
		df_da[2] += 2 * pZpYpX * a[33];
		d2f_da2[2] += 6 * pZpYpX * a[49];
		pZpYpX = pZ[0] * pX[1];
		result += pZpYpX * a[18];
		df_da[0] += 3 * pZpYpX * a[19];
		df_da[1] += pZpYpX * a[22];
		d2f_da2[1] += 2 * pZpYpX * a[26];
		df_da[2] += 2 * pZpYpX * a[34];
		d2f_da2[2] += 6 * pZpYpX * a[50];
		pZpYpX = pZ[0] * pX[2];
		result += pZpYpX * a[19];
		df_da[1] += pZpYpX * a[23];
		d2f_da2[1] += 2 * pZpYpX * a[27];
		df_da[2] += 2 * pZpYpX * a[35];
		d2f_da2[2] += 6 * pZpYpX * a[51];
		pZpY = pZ[0] * pY[0];
		result += pZpY * a[20];
		df_da[0] += pZpY * a[21];
		d2f_da2[0] += 2 * pZpY * a[22];
		df_da[1] += 2 * pZpY * a[24];
		d2f_da2[1] += 6 * pZpY * a[28];
		df_da[2] += 2 * pZpY * a[36];
		d2f_da2[2] += 6 * pZpY * a[52];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[21];
		df_da[0] += 2 * pZpYpX * a[22];
		d2f_da2[0] += 6 * pZpYpX * a[23];
		df_da[1] += 2 * pZpYpX * a[25];
		d2f_da2[1] += 6 * pZpYpX * a[29];
		df_da[2] += 2 * pZpYpX * a[37];
		d2f_da2[2] += 6 * pZpYpX * a[53];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[22];
		df_da[0] += 3 * pZpYpX * a[23];
		df_da[1] += 2 * pZpYpX * a[26];
		d2f_da2[1] += 6 * pZpYpX * a[30];
		df_da[2] += 2 * pZpYpX * a[38];
		d2f_da2[2] += 6 * pZpYpX * a[54];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[23];
		df_da[1] += 2 * pZpYpX * a[27];
		d2f_da2[1] += 6 * pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[39];
		d2f_da2[2] += 6 * pZpYpX * a[55];
		pZpY = pZ[0] * pY[1];
		result += pZpY * a[24];
		df_da[0] += pZpY * a[25];
		d2f_da2[0] += 2 * pZpY * a[26];
		df_da[1] += 3 * pZpY * a[28];
		df_da[2] += 2 * pZpY * a[40];
		d2f_da2[2] += 6 * pZpY * a[56];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[25];
		df_da[0] += 2 * pZpYpX * a[26];
		d2f_da2[0] += 6 * pZpYpX * a[27];
		df_da[1] += 3 * pZpYpX * a[29];
		df_da[2] += 2 * pZpYpX * a[41];
		d2f_da2[2] += 6 * pZpYpX * a[57];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[26];
		df_da[0] += 3 * pZpYpX * a[27];
		df_da[1] += 3 * pZpYpX * a[30];
		df_da[2] += 2 * pZpYpX * a[42];
		d2f_da2[2] += 6 * pZpYpX * a[58];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[27];
		df_da[1] += 3 * pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[43];
		d2f_da2[2] += 6 * pZpYpX * a[59];
		pZpY = pZ[0] * pY[2];
		result += pZpY * a[28];
		df_da[0] += pZpY * a[29];
		d2f_da2[0] += 2 * pZpY * a[30];
		df_da[2] += 2 * pZpY * a[44];
		d2f_da2[2] += 6 * pZpY * a[60];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[29];
		df_da[0] += 2 * pZpYpX * a[30];
		d2f_da2[0] += 6 * pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[45];
		d2f_da2[2] += 6 * pZpYpX * a[61];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[30];
		df_da[0] += 3 * pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[46];
		d2f_da2[2] += 6 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[31];
		df_da[2] += 2 * pZpYpX * a[47];
		d2f_da2[2] += 6 * pZpYpX * a[63];
		result += pZ[1] * a[32];
		df_da[0] += pZ[1] * a[33];
		d2f_da2[0] += 2 * pZ[1] * a[34];
		df_da[1] += pZ[1] * a[36];
		d2f_da2[1] += 2 * pZ[1] * a[40];
		df_da[2] += 3 * pZ[1] * a[48];
		pZpYpX = pZ[1] * pX[0];
		result += pZpYpX * a[33];
		df_da[0] += 2 * pZpYpX * a[34];
		d2f_da2[0] += 6 * pZpYpX * a[35];
		df_da[1] += pZpYpX * a[37];
		d2f_da2[1] += 2 * pZpYpX * a[41];
		df_da[2] += 3 * pZpYpX * a[49];
		pZpYpX = pZ[1] * pX[1];
		result += pZpYpX * a[34];
		df_da[0] += 3 * pZpYpX * a[35];
		df_da[1] += pZpYpX * a[38];
		d2f_da2[1] += 2 * pZpYpX * a[42];
		df_da[2] += 3 * pZpYpX * a[50];
		pZpYpX = pZ[1] * pX[2];
		result += pZpYpX * a[35];
		df_da[1] += pZpYpX * a[39];
		d2f_da2[1] += 2 * pZpYpX * a[43];
		df_da[2] += 3 * pZpYpX * a[51];
		pZpY = pZ[1] * pY[0];
		result += pZpY * a[36];
		df_da[0] += pZpY * a[37];
		d2f_da2[0] += 2 * pZpY * a[38];
		df_da[1] += 2 * pZpY * a[40];
		d2f_da2[1] += 6 * pZpY * a[44];
		df_da[2] += 3 * pZpY * a[52];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[37];
		df_da[0] += 2 * pZpYpX * a[38];
		d2f_da2[0] += 6 * pZpYpX * a[39];
		df_da[1] += 2 * pZpYpX * a[41];
		d2f_da2[1] += 6 * pZpYpX * a[45];
		df_da[2] += 3 * pZpYpX * a[53];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[38];
		df_da[0] += 3 * pZpYpX * a[39];
		df_da[1] += 2 * pZpYpX * a[42];
		d2f_da2[1] += 6 * pZpYpX * a[46];
		df_da[2] += 3 * pZpYpX * a[54];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[39];
		df_da[1] += 2 * pZpYpX * a[43];
		d2f_da2[1] += 6 * pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[55];
		pZpY = pZ[1] * pY[1];
		result += pZpY * a[40];
		df_da[0] += pZpY * a[41];
		d2f_da2[0] += 2 * pZpY * a[42];
		df_da[1] += 3 * pZpY * a[44];
		df_da[2] += 3 * pZpY * a[56];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[41];
		df_da[0] += 2 * pZpYpX * a[42];
		d2f_da2[0] += 6 * pZpYpX * a[43];
		df_da[1] += 3 * pZpYpX * a[45];
		df_da[2] += 3 * pZpYpX * a[57];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[42];
		df_da[0] += 3 * pZpYpX * a[43];
		df_da[1] += 3 * pZpYpX * a[46];
		df_da[2] += 3 * pZpYpX * a[58];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[43];
		df_da[1] += 3 * pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[59];
		pZpY = pZ[1] * pY[2];
		result += pZpY * a[44];
		df_da[0] += pZpY * a[45];
		d2f_da2[0] += 2 * pZpY * a[46];
		df_da[2] += 3 * pZpY * a[60];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[45];
		df_da[0] += 2 * pZpYpX * a[46];
		d2f_da2[0] += 6 * pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[61];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[46];
		df_da[0] += 3 * pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[47];
		df_da[2] += 3 * pZpYpX * a[63];
		result += pZ[2] * a[48];
		df_da[0] += pZ[2] * a[49];
		d2f_da2[0] += 2 * pZ[2] * a[50];
		df_da[1] += pZ[2] * a[52];
		d2f_da2[1] += 2 * pZ[2] * a[56];
		pZpYpX = pZ[2] * pX[0];
		result += pZpYpX * a[49];
		df_da[0] += 2 * pZpYpX * a[50];
		d2f_da2[0] += 6 * pZpYpX * a[51];
		df_da[1] += pZpYpX * a[53];
		d2f_da2[1] += 2 * pZpYpX * a[57];
		pZpYpX = pZ[2] * pX[1];
		result += pZpYpX * a[50];
		df_da[0] += 3 * pZpYpX * a[51];
		df_da[1] += pZpYpX * a[54];
		d2f_da2[1] += 2 * pZpYpX * a[58];
		pZpYpX = pZ[2] * pX[2];
		result += pZpYpX * a[51];
		df_da[1] += pZpYpX * a[55];
		d2f_da2[1] += 2 * pZpYpX * a[59];
		pZpY = pZ[2] * pY[0];
		result += pZpY * a[52];
		df_da[0] += pZpY * a[53];
		d2f_da2[0] += 2 * pZpY * a[54];
		df_da[1] += 2 * pZpY * a[56];
		d2f_da2[1] += 6 * pZpY * a[60];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[53];
		df_da[0] += 2 * pZpYpX * a[54];
		d2f_da2[0] += 6 * pZpYpX * a[55];
		df_da[1] += 2 * pZpYpX * a[57];
		d2f_da2[1] += 6 * pZpYpX * a[61];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[54];
		df_da[0] += 3 * pZpYpX * a[55];
		df_da[1] += 2 * pZpYpX * a[58];
		d2f_da2[1] += 6 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[55];
		df_da[1] += 2 * pZpYpX * a[59];
		d2f_da2[1] += 6 * pZpYpX * a[63];
		pZpY = pZ[2] * pY[1];
		result += pZpY * a[56];
		df_da[0] += pZpY * a[57];
		d2f_da2[0] += 2 * pZpY * a[58];
		df_da[1] += 3 * pZpY * a[60];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[57];
		df_da[0] += 2 * pZpYpX * a[58];
		d2f_da2[0] += 6 * pZpYpX * a[59];
		df_da[1] += 3 * pZpYpX * a[61];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[58];
		df_da[0] += 3 * pZpYpX * a[59];
		df_da[1] += 3 * pZpYpX * a[62];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[59];
		df_da[1] += 3 * pZpYpX * a[63];
		pZpY = pZ[2] * pY[2];
		result += pZpY * a[60];
		df_da[0] += pZpY * a[61];
		d2f_da2[0] += 2 * pZpY * a[62];
		pZpYpX = pZpY * pX[0];
		result += pZpYpX * a[61];
		df_da[0] += 2 * pZpYpX * a[62];
		d2f_da2[0] += 6 * pZpYpX * a[63];
		pZpYpX = pZpY * pX[1];
		result += pZpYpX * a[62];
		df_da[0] += 3 * pZpYpX * a[63];
		pZpYpX = pZpY * pX[2];
		result += pZpYpX * a[63];

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[], double[], double[])
	 */
	@Override
	public double value(double[] table, double[] df_da, double[] d2f_da2)
	{
		df_da[0] = table[0] * a[1] + 2 * table[1] * a[2] + 3 * table[2] * a[3] + table[4] * a[5] + 2 * table[5] * a[6] +
				3 * table[6] * a[7] + table[8] * a[9] + 2 * table[9] * a[10] + 3 * table[10] * a[11] +
				table[12] * a[13] + 2 * table[13] * a[14] + 3 * table[14] * a[15] + table[16] * a[17] +
				2 * table[17] * a[18] + 3 * table[18] * a[19] + table[20] * a[21] + 2 * table[21] * a[22] +
				3 * table[22] * a[23] + table[24] * a[25] + 2 * table[25] * a[26] + 3 * table[26] * a[27] +
				table[28] * a[29] + 2 * table[29] * a[30] + 3 * table[30] * a[31] + table[32] * a[33] +
				2 * table[33] * a[34] + 3 * table[34] * a[35] + table[36] * a[37] + 2 * table[37] * a[38] +
				3 * table[38] * a[39] + table[40] * a[41] + 2 * table[41] * a[42] + 3 * table[42] * a[43] +
				table[44] * a[45] + 2 * table[45] * a[46] + 3 * table[46] * a[47] + table[48] * a[49] +
				2 * table[49] * a[50] + 3 * table[50] * a[51] + table[52] * a[53] + 2 * table[53] * a[54] +
				3 * table[54] * a[55] + table[56] * a[57] + 2 * table[57] * a[58] + 3 * table[58] * a[59] +
				table[60] * a[61] + 2 * table[61] * a[62] + 3 * table[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + 2 * table[4] * a[8] +
				2 * table[5] * a[9] + 2 * table[6] * a[10] + 2 * table[7] * a[11] + 3 * table[8] * a[12] +
				3 * table[9] * a[13] + 3 * table[10] * a[14] + 3 * table[11] * a[15] + table[16] * a[20] +
				table[17] * a[21] + table[18] * a[22] + table[19] * a[23] + 2 * table[20] * a[24] +
				2 * table[21] * a[25] + 2 * table[22] * a[26] + 2 * table[23] * a[27] + 3 * table[24] * a[28] +
				3 * table[25] * a[29] + 3 * table[26] * a[30] + 3 * table[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + 2 * table[36] * a[40] +
				2 * table[37] * a[41] + 2 * table[38] * a[42] + 2 * table[39] * a[43] + 3 * table[40] * a[44] +
				3 * table[41] * a[45] + 3 * table[42] * a[46] + 3 * table[43] * a[47] + table[48] * a[52] +
				table[49] * a[53] + table[50] * a[54] + table[51] * a[55] + 2 * table[52] * a[56] +
				2 * table[53] * a[57] + 2 * table[54] * a[58] + 2 * table[55] * a[59] + 3 * table[56] * a[60] +
				3 * table[57] * a[61] + 3 * table[58] * a[62] + 3 * table[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + 2 * table[16] * a[32] + 2 * table[17] * a[33] + 2 * table[18] * a[34] +
				2 * table[19] * a[35] + 2 * table[20] * a[36] + 2 * table[21] * a[37] + 2 * table[22] * a[38] +
				2 * table[23] * a[39] + 2 * table[24] * a[40] + 2 * table[25] * a[41] + 2 * table[26] * a[42] +
				2 * table[27] * a[43] + 2 * table[28] * a[44] + 2 * table[29] * a[45] + 2 * table[30] * a[46] +
				2 * table[31] * a[47] + 3 * table[32] * a[48] + 3 * table[33] * a[49] + 3 * table[34] * a[50] +
				3 * table[35] * a[51] + 3 * table[36] * a[52] + 3 * table[37] * a[53] + 3 * table[38] * a[54] +
				3 * table[39] * a[55] + 3 * table[40] * a[56] + 3 * table[41] * a[57] + 3 * table[42] * a[58] +
				3 * table[43] * a[59] + 3 * table[44] * a[60] + 3 * table[45] * a[61] + 3 * table[46] * a[62] +
				3 * table[47] * a[63];
		d2f_da2[0] = 2 * table[0] * a[2] + 6 * table[1] * a[3] + 2 * table[4] * a[6] + 6 * table[5] * a[7] +
				2 * table[8] * a[10] + 6 * table[9] * a[11] + 2 * table[12] * a[14] + 6 * table[13] * a[15] +
				2 * table[16] * a[18] + 6 * table[17] * a[19] + 2 * table[20] * a[22] + 6 * table[21] * a[23] +
				2 * table[24] * a[26] + 6 * table[25] * a[27] + 2 * table[28] * a[30] + 6 * table[29] * a[31] +
				2 * table[32] * a[34] + 6 * table[33] * a[35] + 2 * table[36] * a[38] + 6 * table[37] * a[39] +
				2 * table[40] * a[42] + 6 * table[41] * a[43] + 2 * table[44] * a[46] + 6 * table[45] * a[47] +
				2 * table[48] * a[50] + 6 * table[49] * a[51] + 2 * table[52] * a[54] + 6 * table[53] * a[55] +
				2 * table[56] * a[58] + 6 * table[57] * a[59] + 2 * table[60] * a[62] + 6 * table[61] * a[63];
		d2f_da2[1] = 2 * table[0] * a[8] + 2 * table[1] * a[9] + 2 * table[2] * a[10] + 2 * table[3] * a[11] +
				6 * table[4] * a[12] + 6 * table[5] * a[13] + 6 * table[6] * a[14] + 6 * table[7] * a[15] +
				2 * table[16] * a[24] + 2 * table[17] * a[25] + 2 * table[18] * a[26] + 2 * table[19] * a[27] +
				6 * table[20] * a[28] + 6 * table[21] * a[29] + 6 * table[22] * a[30] + 6 * table[23] * a[31] +
				2 * table[32] * a[40] + 2 * table[33] * a[41] + 2 * table[34] * a[42] + 2 * table[35] * a[43] +
				6 * table[36] * a[44] + 6 * table[37] * a[45] + 6 * table[38] * a[46] + 6 * table[39] * a[47] +
				2 * table[48] * a[56] + 2 * table[49] * a[57] + 2 * table[50] * a[58] + 2 * table[51] * a[59] +
				6 * table[52] * a[60] + 6 * table[53] * a[61] + 6 * table[54] * a[62] + 6 * table[55] * a[63];
		d2f_da2[2] = 2 * table[0] * a[32] + 2 * table[1] * a[33] + 2 * table[2] * a[34] + 2 * table[3] * a[35] +
				2 * table[4] * a[36] + 2 * table[5] * a[37] + 2 * table[6] * a[38] + 2 * table[7] * a[39] +
				2 * table[8] * a[40] + 2 * table[9] * a[41] + 2 * table[10] * a[42] + 2 * table[11] * a[43] +
				2 * table[12] * a[44] + 2 * table[13] * a[45] + 2 * table[14] * a[46] + 2 * table[15] * a[47] +
				6 * table[16] * a[48] + 6 * table[17] * a[49] + 6 * table[18] * a[50] + 6 * table[19] * a[51] +
				6 * table[20] * a[52] + 6 * table[21] * a[53] + 6 * table[22] * a[54] + 6 * table[23] * a[55] +
				6 * table[24] * a[56] + 6 * table[25] * a[57] + 6 * table[26] * a[58] + 6 * table[27] * a[59] +
				6 * table[28] * a[60] + 6 * table[29] * a[61] + 6 * table[30] * a[62] + 6 * table[31] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], double[], double[])
	 */
	@Override
	public double value(float[] table, double[] df_da, double[] d2f_da2)
	{
		df_da[0] = table[0] * a[1] + 2 * table[1] * a[2] + 3 * table[2] * a[3] + table[4] * a[5] + 2 * table[5] * a[6] +
				3 * table[6] * a[7] + table[8] * a[9] + 2 * table[9] * a[10] + 3 * table[10] * a[11] +
				table[12] * a[13] + 2 * table[13] * a[14] + 3 * table[14] * a[15] + table[16] * a[17] +
				2 * table[17] * a[18] + 3 * table[18] * a[19] + table[20] * a[21] + 2 * table[21] * a[22] +
				3 * table[22] * a[23] + table[24] * a[25] + 2 * table[25] * a[26] + 3 * table[26] * a[27] +
				table[28] * a[29] + 2 * table[29] * a[30] + 3 * table[30] * a[31] + table[32] * a[33] +
				2 * table[33] * a[34] + 3 * table[34] * a[35] + table[36] * a[37] + 2 * table[37] * a[38] +
				3 * table[38] * a[39] + table[40] * a[41] + 2 * table[41] * a[42] + 3 * table[42] * a[43] +
				table[44] * a[45] + 2 * table[45] * a[46] + 3 * table[46] * a[47] + table[48] * a[49] +
				2 * table[49] * a[50] + 3 * table[50] * a[51] + table[52] * a[53] + 2 * table[53] * a[54] +
				3 * table[54] * a[55] + table[56] * a[57] + 2 * table[57] * a[58] + 3 * table[58] * a[59] +
				table[60] * a[61] + 2 * table[61] * a[62] + 3 * table[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + 2 * table[4] * a[8] +
				2 * table[5] * a[9] + 2 * table[6] * a[10] + 2 * table[7] * a[11] + 3 * table[8] * a[12] +
				3 * table[9] * a[13] + 3 * table[10] * a[14] + 3 * table[11] * a[15] + table[16] * a[20] +
				table[17] * a[21] + table[18] * a[22] + table[19] * a[23] + 2 * table[20] * a[24] +
				2 * table[21] * a[25] + 2 * table[22] * a[26] + 2 * table[23] * a[27] + 3 * table[24] * a[28] +
				3 * table[25] * a[29] + 3 * table[26] * a[30] + 3 * table[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + 2 * table[36] * a[40] +
				2 * table[37] * a[41] + 2 * table[38] * a[42] + 2 * table[39] * a[43] + 3 * table[40] * a[44] +
				3 * table[41] * a[45] + 3 * table[42] * a[46] + 3 * table[43] * a[47] + table[48] * a[52] +
				table[49] * a[53] + table[50] * a[54] + table[51] * a[55] + 2 * table[52] * a[56] +
				2 * table[53] * a[57] + 2 * table[54] * a[58] + 2 * table[55] * a[59] + 3 * table[56] * a[60] +
				3 * table[57] * a[61] + 3 * table[58] * a[62] + 3 * table[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + 2 * table[16] * a[32] + 2 * table[17] * a[33] + 2 * table[18] * a[34] +
				2 * table[19] * a[35] + 2 * table[20] * a[36] + 2 * table[21] * a[37] + 2 * table[22] * a[38] +
				2 * table[23] * a[39] + 2 * table[24] * a[40] + 2 * table[25] * a[41] + 2 * table[26] * a[42] +
				2 * table[27] * a[43] + 2 * table[28] * a[44] + 2 * table[29] * a[45] + 2 * table[30] * a[46] +
				2 * table[31] * a[47] + 3 * table[32] * a[48] + 3 * table[33] * a[49] + 3 * table[34] * a[50] +
				3 * table[35] * a[51] + 3 * table[36] * a[52] + 3 * table[37] * a[53] + 3 * table[38] * a[54] +
				3 * table[39] * a[55] + 3 * table[40] * a[56] + 3 * table[41] * a[57] + 3 * table[42] * a[58] +
				3 * table[43] * a[59] + 3 * table[44] * a[60] + 3 * table[45] * a[61] + 3 * table[46] * a[62] +
				3 * table[47] * a[63];
		d2f_da2[0] = 2 * table[0] * a[2] + 6 * table[1] * a[3] + 2 * table[4] * a[6] + 6 * table[5] * a[7] +
				2 * table[8] * a[10] + 6 * table[9] * a[11] + 2 * table[12] * a[14] + 6 * table[13] * a[15] +
				2 * table[16] * a[18] + 6 * table[17] * a[19] + 2 * table[20] * a[22] + 6 * table[21] * a[23] +
				2 * table[24] * a[26] + 6 * table[25] * a[27] + 2 * table[28] * a[30] + 6 * table[29] * a[31] +
				2 * table[32] * a[34] + 6 * table[33] * a[35] + 2 * table[36] * a[38] + 6 * table[37] * a[39] +
				2 * table[40] * a[42] + 6 * table[41] * a[43] + 2 * table[44] * a[46] + 6 * table[45] * a[47] +
				2 * table[48] * a[50] + 6 * table[49] * a[51] + 2 * table[52] * a[54] + 6 * table[53] * a[55] +
				2 * table[56] * a[58] + 6 * table[57] * a[59] + 2 * table[60] * a[62] + 6 * table[61] * a[63];
		d2f_da2[1] = 2 * table[0] * a[8] + 2 * table[1] * a[9] + 2 * table[2] * a[10] + 2 * table[3] * a[11] +
				6 * table[4] * a[12] + 6 * table[5] * a[13] + 6 * table[6] * a[14] + 6 * table[7] * a[15] +
				2 * table[16] * a[24] + 2 * table[17] * a[25] + 2 * table[18] * a[26] + 2 * table[19] * a[27] +
				6 * table[20] * a[28] + 6 * table[21] * a[29] + 6 * table[22] * a[30] + 6 * table[23] * a[31] +
				2 * table[32] * a[40] + 2 * table[33] * a[41] + 2 * table[34] * a[42] + 2 * table[35] * a[43] +
				6 * table[36] * a[44] + 6 * table[37] * a[45] + 6 * table[38] * a[46] + 6 * table[39] * a[47] +
				2 * table[48] * a[56] + 2 * table[49] * a[57] + 2 * table[50] * a[58] + 2 * table[51] * a[59] +
				6 * table[52] * a[60] + 6 * table[53] * a[61] + 6 * table[54] * a[62] + 6 * table[55] * a[63];
		d2f_da2[2] = 2 * table[0] * a[32] + 2 * table[1] * a[33] + 2 * table[2] * a[34] + 2 * table[3] * a[35] +
				2 * table[4] * a[36] + 2 * table[5] * a[37] + 2 * table[6] * a[38] + 2 * table[7] * a[39] +
				2 * table[8] * a[40] + 2 * table[9] * a[41] + 2 * table[10] * a[42] + 2 * table[11] * a[43] +
				2 * table[12] * a[44] + 2 * table[13] * a[45] + 2 * table[14] * a[46] + 2 * table[15] * a[47] +
				6 * table[16] * a[48] + 6 * table[17] * a[49] + 6 * table[18] * a[50] + 6 * table[19] * a[51] +
				6 * table[20] * a[52] + 6 * table[21] * a[53] + 6 * table[22] * a[54] + 6 * table[23] * a[55] +
				6 * table[24] * a[56] + 6 * table[25] * a[57] + 6 * table[26] * a[58] + 6 * table[27] * a[59] +
				6 * table[28] * a[60] + 6 * table[29] * a[61] + 6 * table[30] * a[62] + 6 * table[31] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[], double[], double[], double[], double[],
	 * double[])
	 */
	@Override
	public double value(double[] table, double[] table2, double[] table3, double[] table6, double[] df_da,
			double[] d2f_da2)
	{
		df_da[0] = table[0] * a[1] + table2[1] * a[2] + table3[2] * a[3] + table[4] * a[5] + table2[5] * a[6] +
				table3[6] * a[7] + table[8] * a[9] + table2[9] * a[10] + table3[10] * a[11] + table[12] * a[13] +
				table2[13] * a[14] + table3[14] * a[15] + table[16] * a[17] + table2[17] * a[18] + table3[18] * a[19] +
				table[20] * a[21] + table2[21] * a[22] + table3[22] * a[23] + table[24] * a[25] + table2[25] * a[26] +
				table3[26] * a[27] + table[28] * a[29] + table2[29] * a[30] + table3[30] * a[31] + table[32] * a[33] +
				table2[33] * a[34] + table3[34] * a[35] + table[36] * a[37] + table2[37] * a[38] + table3[38] * a[39] +
				table[40] * a[41] + table2[41] * a[42] + table3[42] * a[43] + table[44] * a[45] + table2[45] * a[46] +
				table3[46] * a[47] + table[48] * a[49] + table2[49] * a[50] + table3[50] * a[51] + table[52] * a[53] +
				table2[53] * a[54] + table3[54] * a[55] + table[56] * a[57] + table2[57] * a[58] + table3[58] * a[59] +
				table[60] * a[61] + table2[61] * a[62] + table3[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + table2[4] * a[8] +
				table2[5] * a[9] + table2[6] * a[10] + table2[7] * a[11] + table3[8] * a[12] + table3[9] * a[13] +
				table3[10] * a[14] + table3[11] * a[15] + table[16] * a[20] + table[17] * a[21] + table[18] * a[22] +
				table[19] * a[23] + table2[20] * a[24] + table2[21] * a[25] + table2[22] * a[26] + table2[23] * a[27] +
				table3[24] * a[28] + table3[25] * a[29] + table3[26] * a[30] + table3[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + table2[36] * a[40] + table2[37] * a[41] +
				table2[38] * a[42] + table2[39] * a[43] + table3[40] * a[44] + table3[41] * a[45] + table3[42] * a[46] +
				table3[43] * a[47] + table[48] * a[52] + table[49] * a[53] + table[50] * a[54] + table[51] * a[55] +
				table2[52] * a[56] + table2[53] * a[57] + table2[54] * a[58] + table2[55] * a[59] + table3[56] * a[60] +
				table3[57] * a[61] + table3[58] * a[62] + table3[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + table2[16] * a[32] + table2[17] * a[33] + table2[18] * a[34] + table2[19] * a[35] +
				table2[20] * a[36] + table2[21] * a[37] + table2[22] * a[38] + table2[23] * a[39] + table2[24] * a[40] +
				table2[25] * a[41] + table2[26] * a[42] + table2[27] * a[43] + table2[28] * a[44] + table2[29] * a[45] +
				table2[30] * a[46] + table2[31] * a[47] + table3[32] * a[48] + table3[33] * a[49] + table3[34] * a[50] +
				table3[35] * a[51] + table3[36] * a[52] + table3[37] * a[53] + table3[38] * a[54] + table3[39] * a[55] +
				table3[40] * a[56] + table3[41] * a[57] + table3[42] * a[58] + table3[43] * a[59] + table3[44] * a[60] +
				table3[45] * a[61] + table3[46] * a[62] + table3[47] * a[63];
		d2f_da2[0] = table2[0] * a[2] + table6[1] * a[3] + table2[4] * a[6] + table6[5] * a[7] + table2[8] * a[10] +
				table6[9] * a[11] + table2[12] * a[14] + table6[13] * a[15] + table2[16] * a[18] + table6[17] * a[19] +
				table2[20] * a[22] + table6[21] * a[23] + table2[24] * a[26] + table6[25] * a[27] + table2[28] * a[30] +
				table6[29] * a[31] + table2[32] * a[34] + table6[33] * a[35] + table2[36] * a[38] + table6[37] * a[39] +
				table2[40] * a[42] + table6[41] * a[43] + table2[44] * a[46] + table6[45] * a[47] + table2[48] * a[50] +
				table6[49] * a[51] + table2[52] * a[54] + table6[53] * a[55] + table2[56] * a[58] + table6[57] * a[59] +
				table2[60] * a[62] + table6[61] * a[63];
		d2f_da2[1] = table2[0] * a[8] + table2[1] * a[9] + table2[2] * a[10] + table2[3] * a[11] + table6[4] * a[12] +
				table6[5] * a[13] + table6[6] * a[14] + table6[7] * a[15] + table2[16] * a[24] + table2[17] * a[25] +
				table2[18] * a[26] + table2[19] * a[27] + table6[20] * a[28] + table6[21] * a[29] + table6[22] * a[30] +
				table6[23] * a[31] + table2[32] * a[40] + table2[33] * a[41] + table2[34] * a[42] + table2[35] * a[43] +
				table6[36] * a[44] + table6[37] * a[45] + table6[38] * a[46] + table6[39] * a[47] + table2[48] * a[56] +
				table2[49] * a[57] + table2[50] * a[58] + table2[51] * a[59] + table6[52] * a[60] + table6[53] * a[61] +
				table6[54] * a[62] + table6[55] * a[63];
		d2f_da2[2] = table2[0] * a[32] + table2[1] * a[33] + table2[2] * a[34] + table2[3] * a[35] + table2[4] * a[36] +
				table2[5] * a[37] + table2[6] * a[38] + table2[7] * a[39] + table2[8] * a[40] + table2[9] * a[41] +
				table2[10] * a[42] + table2[11] * a[43] + table2[12] * a[44] + table2[13] * a[45] + table2[14] * a[46] +
				table2[15] * a[47] + table6[16] * a[48] + table6[17] * a[49] + table6[18] * a[50] + table6[19] * a[51] +
				table6[20] * a[52] + table6[21] * a[53] + table6[22] * a[54] + table6[23] * a[55] + table6[24] * a[56] +
				table6[25] * a[57] + table6[26] * a[58] + table6[27] * a[59] + table6[28] * a[60] + table6[29] * a[61] +
				table6[30] * a[62] + table6[31] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], float[], float[], float[], double[],
	 * double[])
	 */
	@Override
	public double value(float[] table, float[] table2, float[] table3, float[] table6, double[] df_da, double[] d2f_da2)
	{
		df_da[0] = table[0] * a[1] + table2[1] * a[2] + table3[2] * a[3] + table[4] * a[5] + table2[5] * a[6] +
				table3[6] * a[7] + table[8] * a[9] + table2[9] * a[10] + table3[10] * a[11] + table[12] * a[13] +
				table2[13] * a[14] + table3[14] * a[15] + table[16] * a[17] + table2[17] * a[18] + table3[18] * a[19] +
				table[20] * a[21] + table2[21] * a[22] + table3[22] * a[23] + table[24] * a[25] + table2[25] * a[26] +
				table3[26] * a[27] + table[28] * a[29] + table2[29] * a[30] + table3[30] * a[31] + table[32] * a[33] +
				table2[33] * a[34] + table3[34] * a[35] + table[36] * a[37] + table2[37] * a[38] + table3[38] * a[39] +
				table[40] * a[41] + table2[41] * a[42] + table3[42] * a[43] + table[44] * a[45] + table2[45] * a[46] +
				table3[46] * a[47] + table[48] * a[49] + table2[49] * a[50] + table3[50] * a[51] + table[52] * a[53] +
				table2[53] * a[54] + table3[54] * a[55] + table[56] * a[57] + table2[57] * a[58] + table3[58] * a[59] +
				table[60] * a[61] + table2[61] * a[62] + table3[62] * a[63];
		df_da[1] = table[0] * a[4] + table[1] * a[5] + table[2] * a[6] + table[3] * a[7] + table2[4] * a[8] +
				table2[5] * a[9] + table2[6] * a[10] + table2[7] * a[11] + table3[8] * a[12] + table3[9] * a[13] +
				table3[10] * a[14] + table3[11] * a[15] + table[16] * a[20] + table[17] * a[21] + table[18] * a[22] +
				table[19] * a[23] + table2[20] * a[24] + table2[21] * a[25] + table2[22] * a[26] + table2[23] * a[27] +
				table3[24] * a[28] + table3[25] * a[29] + table3[26] * a[30] + table3[27] * a[31] + table[32] * a[36] +
				table[33] * a[37] + table[34] * a[38] + table[35] * a[39] + table2[36] * a[40] + table2[37] * a[41] +
				table2[38] * a[42] + table2[39] * a[43] + table3[40] * a[44] + table3[41] * a[45] + table3[42] * a[46] +
				table3[43] * a[47] + table[48] * a[52] + table[49] * a[53] + table[50] * a[54] + table[51] * a[55] +
				table2[52] * a[56] + table2[53] * a[57] + table2[54] * a[58] + table2[55] * a[59] + table3[56] * a[60] +
				table3[57] * a[61] + table3[58] * a[62] + table3[59] * a[63];
		df_da[2] = table[0] * a[16] + table[1] * a[17] + table[2] * a[18] + table[3] * a[19] + table[4] * a[20] +
				table[5] * a[21] + table[6] * a[22] + table[7] * a[23] + table[8] * a[24] + table[9] * a[25] +
				table[10] * a[26] + table[11] * a[27] + table[12] * a[28] + table[13] * a[29] + table[14] * a[30] +
				table[15] * a[31] + table2[16] * a[32] + table2[17] * a[33] + table2[18] * a[34] + table2[19] * a[35] +
				table2[20] * a[36] + table2[21] * a[37] + table2[22] * a[38] + table2[23] * a[39] + table2[24] * a[40] +
				table2[25] * a[41] + table2[26] * a[42] + table2[27] * a[43] + table2[28] * a[44] + table2[29] * a[45] +
				table2[30] * a[46] + table2[31] * a[47] + table3[32] * a[48] + table3[33] * a[49] + table3[34] * a[50] +
				table3[35] * a[51] + table3[36] * a[52] + table3[37] * a[53] + table3[38] * a[54] + table3[39] * a[55] +
				table3[40] * a[56] + table3[41] * a[57] + table3[42] * a[58] + table3[43] * a[59] + table3[44] * a[60] +
				table3[45] * a[61] + table3[46] * a[62] + table3[47] * a[63];
		d2f_da2[0] = table2[0] * a[2] + table6[1] * a[3] + table2[4] * a[6] + table6[5] * a[7] + table2[8] * a[10] +
				table6[9] * a[11] + table2[12] * a[14] + table6[13] * a[15] + table2[16] * a[18] + table6[17] * a[19] +
				table2[20] * a[22] + table6[21] * a[23] + table2[24] * a[26] + table6[25] * a[27] + table2[28] * a[30] +
				table6[29] * a[31] + table2[32] * a[34] + table6[33] * a[35] + table2[36] * a[38] + table6[37] * a[39] +
				table2[40] * a[42] + table6[41] * a[43] + table2[44] * a[46] + table6[45] * a[47] + table2[48] * a[50] +
				table6[49] * a[51] + table2[52] * a[54] + table6[53] * a[55] + table2[56] * a[58] + table6[57] * a[59] +
				table2[60] * a[62] + table6[61] * a[63];
		d2f_da2[1] = table2[0] * a[8] + table2[1] * a[9] + table2[2] * a[10] + table2[3] * a[11] + table6[4] * a[12] +
				table6[5] * a[13] + table6[6] * a[14] + table6[7] * a[15] + table2[16] * a[24] + table2[17] * a[25] +
				table2[18] * a[26] + table2[19] * a[27] + table6[20] * a[28] + table6[21] * a[29] + table6[22] * a[30] +
				table6[23] * a[31] + table2[32] * a[40] + table2[33] * a[41] + table2[34] * a[42] + table2[35] * a[43] +
				table6[36] * a[44] + table6[37] * a[45] + table6[38] * a[46] + table6[39] * a[47] + table2[48] * a[56] +
				table2[49] * a[57] + table2[50] * a[58] + table2[51] * a[59] + table6[52] * a[60] + table6[53] * a[61] +
				table6[54] * a[62] + table6[55] * a[63];
		d2f_da2[2] = table2[0] * a[32] + table2[1] * a[33] + table2[2] * a[34] + table2[3] * a[35] + table2[4] * a[36] +
				table2[5] * a[37] + table2[6] * a[38] + table2[7] * a[39] + table2[8] * a[40] + table2[9] * a[41] +
				table2[10] * a[42] + table2[11] * a[43] + table2[12] * a[44] + table2[13] * a[45] + table2[14] * a[46] +
				table2[15] * a[47] + table6[16] * a[48] + table6[17] * a[49] + table6[18] * a[50] + table6[19] * a[51] +
				table6[20] * a[52] + table6[21] * a[53] + table6[22] * a[54] + table6[23] * a[55] + table6[24] * a[56] +
				table6[25] * a[57] + table6[26] * a[58] + table6[27] * a[59] + table6[28] * a[60] + table6[29] * a[61] +
				table6[30] * a[62] + table6[31] * a[63];
		return table[0] * a[0] + table[1] * a[1] + table[2] * a[2] + table[3] * a[3] + table[4] * a[4] +
				table[5] * a[5] + table[6] * a[6] + table[7] * a[7] + table[8] * a[8] + table[9] * a[9] +
				table[10] * a[10] + table[11] * a[11] + table[12] * a[12] + table[13] * a[13] + table[14] * a[14] +
				table[15] * a[15] + table[16] * a[16] + table[17] * a[17] + table[18] * a[18] + table[19] * a[19] +
				table[20] * a[20] + table[21] * a[21] + table[22] * a[22] + table[23] * a[23] + table[24] * a[24] +
				table[25] * a[25] + table[26] * a[26] + table[27] * a[27] + table[28] * a[28] + table[29] * a[29] +
				table[30] * a[30] + table[31] * a[31] + table[32] * a[32] + table[33] * a[33] + table[34] * a[34] +
				table[35] * a[35] + table[36] * a[36] + table[37] * a[37] + table[38] * a[38] + table[39] * a[39] +
				table[40] * a[40] + table[41] * a[41] + table[42] * a[42] + table[43] * a[43] + table[44] * a[44] +
				table[45] * a[45] + table[46] * a[46] + table[47] * a[47] + table[48] * a[48] + table[49] * a[49] +
				table[50] * a[50] + table[51] * a[51] + table[52] * a[52] + table[53] * a[53] + table[54] * a[54] +
				table[55] * a[55] + table[56] * a[56] + table[57] * a[57] + table[58] * a[58] + table[59] * a[59] +
				table[60] * a[60] + table[61] * a[61] + table[62] * a[62] + table[63] * a[63];
	}
}
