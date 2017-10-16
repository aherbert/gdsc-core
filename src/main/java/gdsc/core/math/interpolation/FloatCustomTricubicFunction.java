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
	public double[] getA()
	{
		return toDouble(a);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[])
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[])
	 */
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
		result += a[1] * pX[0];
		df_da[0] += 2 * a[2] * pX[0];
		df_da[1] += a[5] * pX[0];
		df_da[2] += a[17] * pX[0];
		result += a[2] * pX[1];
		df_da[0] += 3 * a[3] * pX[1];
		df_da[1] += a[6] * pX[1];
		df_da[2] += a[18] * pX[1];
		result += a[3] * pX[2];
		df_da[1] += a[7] * pX[2];
		df_da[2] += a[19] * pX[2];
		result += a[4] * pY[0];
		df_da[0] += a[5] * pY[0];
		df_da[1] += 2 * a[8] * pY[0];
		df_da[2] += a[20] * pY[0];
		pXpYpZ = pX[0] * pY[0];
		result += a[5] * pXpYpZ;
		df_da[0] += 2 * a[6] * pXpYpZ;
		df_da[1] += 2 * a[9] * pXpYpZ;
		df_da[2] += a[21] * pXpYpZ;
		pXpYpZ = pX[1] * pY[0];
		result += a[6] * pXpYpZ;
		df_da[0] += 3 * a[7] * pXpYpZ;
		df_da[1] += 2 * a[10] * pXpYpZ;
		df_da[2] += a[22] * pXpYpZ;
		pXpYpZ = pX[2] * pY[0];
		result += a[7] * pXpYpZ;
		df_da[1] += 2 * a[11] * pXpYpZ;
		df_da[2] += a[23] * pXpYpZ;
		result += a[8] * pY[1];
		df_da[0] += a[9] * pY[1];
		df_da[1] += 3 * a[12] * pY[1];
		df_da[2] += a[24] * pY[1];
		pXpYpZ = pX[0] * pY[1];
		result += a[9] * pXpYpZ;
		df_da[0] += 2 * a[10] * pXpYpZ;
		df_da[1] += 3 * a[13] * pXpYpZ;
		df_da[2] += a[25] * pXpYpZ;
		pXpYpZ = pX[1] * pY[1];
		result += a[10] * pXpYpZ;
		df_da[0] += 3 * a[11] * pXpYpZ;
		df_da[1] += 3 * a[14] * pXpYpZ;
		df_da[2] += a[26] * pXpYpZ;
		pXpYpZ = pX[2] * pY[1];
		result += a[11] * pXpYpZ;
		df_da[1] += 3 * a[15] * pXpYpZ;
		df_da[2] += a[27] * pXpYpZ;
		result += a[12] * pY[2];
		df_da[0] += a[13] * pY[2];
		df_da[2] += a[28] * pY[2];
		pXpYpZ = pX[0] * pY[2];
		result += a[13] * pXpYpZ;
		df_da[0] += 2 * a[14] * pXpYpZ;
		df_da[2] += a[29] * pXpYpZ;
		pXpYpZ = pX[1] * pY[2];
		result += a[14] * pXpYpZ;
		df_da[0] += 3 * a[15] * pXpYpZ;
		df_da[2] += a[30] * pXpYpZ;
		pXpYpZ = pX[2] * pY[2];
		result += a[15] * pXpYpZ;
		df_da[2] += a[31] * pXpYpZ;
		result += a[16] * pZ[0];
		df_da[0] += a[17] * pZ[0];
		df_da[1] += a[20] * pZ[0];
		df_da[2] += 2 * a[32] * pZ[0];
		pXpYpZ = pX[0] * pZ[0];
		result += a[17] * pXpYpZ;
		df_da[0] += 2 * a[18] * pXpYpZ;
		df_da[1] += a[21] * pXpYpZ;
		df_da[2] += 2 * a[33] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[0];
		result += a[18] * pXpYpZ;
		df_da[0] += 3 * a[19] * pXpYpZ;
		df_da[1] += a[22] * pXpYpZ;
		df_da[2] += 2 * a[34] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[0];
		result += a[19] * pXpYpZ;
		df_da[1] += a[23] * pXpYpZ;
		df_da[2] += 2 * a[35] * pXpYpZ;
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		df_da[0] += a[21] * pYpZ;
		df_da[1] += 2 * a[24] * pYpZ;
		df_da[2] += 2 * a[36] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += 2 * a[22] * pXpYpZ;
		df_da[1] += 2 * a[25] * pXpYpZ;
		df_da[2] += 2 * a[37] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += 3 * a[23] * pXpYpZ;
		df_da[1] += 2 * a[26] * pXpYpZ;
		df_da[2] += 2 * a[38] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += 2 * a[27] * pXpYpZ;
		df_da[2] += 2 * a[39] * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		df_da[0] += a[25] * pYpZ;
		df_da[1] += 3 * a[28] * pYpZ;
		df_da[2] += 2 * a[40] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += 2 * a[26] * pXpYpZ;
		df_da[1] += 3 * a[29] * pXpYpZ;
		df_da[2] += 2 * a[41] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += 3 * a[27] * pXpYpZ;
		df_da[1] += 3 * a[30] * pXpYpZ;
		df_da[2] += 2 * a[42] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += 3 * a[31] * pXpYpZ;
		df_da[2] += 2 * a[43] * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		df_da[0] += a[29] * pYpZ;
		df_da[2] += 2 * a[44] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += 2 * a[30] * pXpYpZ;
		df_da[2] += 2 * a[45] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += 3 * a[31] * pXpYpZ;
		df_da[2] += 2 * a[46] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += 2 * a[47] * pXpYpZ;
		result += a[32] * pZ[1];
		df_da[0] += a[33] * pZ[1];
		df_da[1] += a[36] * pZ[1];
		df_da[2] += 3 * a[48] * pZ[1];
		pXpYpZ = pX[0] * pZ[1];
		result += a[33] * pXpYpZ;
		df_da[0] += 2 * a[34] * pXpYpZ;
		df_da[1] += a[37] * pXpYpZ;
		df_da[2] += 3 * a[49] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[1];
		result += a[34] * pXpYpZ;
		df_da[0] += 3 * a[35] * pXpYpZ;
		df_da[1] += a[38] * pXpYpZ;
		df_da[2] += 3 * a[50] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[1];
		result += a[35] * pXpYpZ;
		df_da[1] += a[39] * pXpYpZ;
		df_da[2] += 3 * a[51] * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		df_da[0] += a[37] * pYpZ;
		df_da[1] += 2 * a[40] * pYpZ;
		df_da[2] += 3 * a[52] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += 2 * a[38] * pXpYpZ;
		df_da[1] += 2 * a[41] * pXpYpZ;
		df_da[2] += 3 * a[53] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += 3 * a[39] * pXpYpZ;
		df_da[1] += 2 * a[42] * pXpYpZ;
		df_da[2] += 3 * a[54] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += 2 * a[43] * pXpYpZ;
		df_da[2] += 3 * a[55] * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		df_da[0] += a[41] * pYpZ;
		df_da[1] += 3 * a[44] * pYpZ;
		df_da[2] += 3 * a[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += 2 * a[42] * pXpYpZ;
		df_da[1] += 3 * a[45] * pXpYpZ;
		df_da[2] += 3 * a[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += 3 * a[43] * pXpYpZ;
		df_da[1] += 3 * a[46] * pXpYpZ;
		df_da[2] += 3 * a[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += 3 * a[47] * pXpYpZ;
		df_da[2] += 3 * a[59] * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		df_da[0] += a[45] * pYpZ;
		df_da[2] += 3 * a[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += 2 * a[46] * pXpYpZ;
		df_da[2] += 3 * a[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += 3 * a[47] * pXpYpZ;
		df_da[2] += 3 * a[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += 3 * a[63] * pXpYpZ;
		result += a[48] * pZ[2];
		df_da[0] += a[49] * pZ[2];
		df_da[1] += a[52] * pZ[2];
		pXpYpZ = pX[0] * pZ[2];
		result += a[49] * pXpYpZ;
		df_da[0] += 2 * a[50] * pXpYpZ;
		df_da[1] += a[53] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[2];
		result += a[50] * pXpYpZ;
		df_da[0] += 3 * a[51] * pXpYpZ;
		df_da[1] += a[54] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[2];
		result += a[51] * pXpYpZ;
		df_da[1] += a[55] * pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		df_da[0] += a[53] * pYpZ;
		df_da[1] += 2 * a[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += 2 * a[54] * pXpYpZ;
		df_da[1] += 2 * a[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += 3 * a[55] * pXpYpZ;
		df_da[1] += 2 * a[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += 2 * a[59] * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		df_da[0] += a[57] * pYpZ;
		df_da[1] += 3 * a[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += 2 * a[58] * pXpYpZ;
		df_da[1] += 3 * a[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += 3 * a[59] * pXpYpZ;
		df_da[1] += 3 * a[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += 3 * a[63] * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		df_da[0] += a[61] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += 2 * a[62] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += 3 * a[63] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[63] * pXpYpZ;

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
		df_da[0] = a[1] * table[0] + 2 * a[2] * table[1] + 3 * a[3] * table[2] + a[5] * table[4] + 2 * a[6] * table[5] +
				3 * a[7] * table[6] + a[9] * table[8] + 2 * a[10] * table[9] + 3 * a[11] * table[10] +
				a[13] * table[12] + 2 * a[14] * table[13] + 3 * a[15] * table[14] + a[17] * table[16] +
				2 * a[18] * table[17] + 3 * a[19] * table[18] + a[21] * table[20] + 2 * a[22] * table[21] +
				3 * a[23] * table[22] + a[25] * table[24] + 2 * a[26] * table[25] + 3 * a[27] * table[26] +
				a[29] * table[28] + 2 * a[30] * table[29] + 3 * a[31] * table[30] + a[33] * table[32] +
				2 * a[34] * table[33] + 3 * a[35] * table[34] + a[37] * table[36] + 2 * a[38] * table[37] +
				3 * a[39] * table[38] + a[41] * table[40] + 2 * a[42] * table[41] + 3 * a[43] * table[42] +
				a[45] * table[44] + 2 * a[46] * table[45] + 3 * a[47] * table[46] + a[49] * table[48] +
				2 * a[50] * table[49] + 3 * a[51] * table[50] + a[53] * table[52] + 2 * a[54] * table[53] +
				3 * a[55] * table[54] + a[57] * table[56] + 2 * a[58] * table[57] + 3 * a[59] * table[58] +
				a[61] * table[60] + 2 * a[62] * table[61] + 3 * a[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + 2 * a[8] * table[4] +
				2 * a[9] * table[5] + 2 * a[10] * table[6] + 2 * a[11] * table[7] + 3 * a[12] * table[8] +
				3 * a[13] * table[9] + 3 * a[14] * table[10] + 3 * a[15] * table[11] + a[20] * table[16] +
				a[21] * table[17] + a[22] * table[18] + a[23] * table[19] + 2 * a[24] * table[20] +
				2 * a[25] * table[21] + 2 * a[26] * table[22] + 2 * a[27] * table[23] + 3 * a[28] * table[24] +
				3 * a[29] * table[25] + 3 * a[30] * table[26] + 3 * a[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + 2 * a[40] * table[36] +
				2 * a[41] * table[37] + 2 * a[42] * table[38] + 2 * a[43] * table[39] + 3 * a[44] * table[40] +
				3 * a[45] * table[41] + 3 * a[46] * table[42] + 3 * a[47] * table[43] + a[52] * table[48] +
				a[53] * table[49] + a[54] * table[50] + a[55] * table[51] + 2 * a[56] * table[52] +
				2 * a[57] * table[53] + 2 * a[58] * table[54] + 2 * a[59] * table[55] + 3 * a[60] * table[56] +
				3 * a[61] * table[57] + 3 * a[62] * table[58] + 3 * a[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + 2 * a[32] * table[16] + 2 * a[33] * table[17] + 2 * a[34] * table[18] +
				2 * a[35] * table[19] + 2 * a[36] * table[20] + 2 * a[37] * table[21] + 2 * a[38] * table[22] +
				2 * a[39] * table[23] + 2 * a[40] * table[24] + 2 * a[41] * table[25] + 2 * a[42] * table[26] +
				2 * a[43] * table[27] + 2 * a[44] * table[28] + 2 * a[45] * table[29] + 2 * a[46] * table[30] +
				2 * a[47] * table[31] + 3 * a[48] * table[32] + 3 * a[49] * table[33] + 3 * a[50] * table[34] +
				3 * a[51] * table[35] + 3 * a[52] * table[36] + 3 * a[53] * table[37] + 3 * a[54] * table[38] +
				3 * a[55] * table[39] + 3 * a[56] * table[40] + 3 * a[57] * table[41] + 3 * a[58] * table[42] +
				3 * a[59] * table[43] + 3 * a[60] * table[44] + 3 * a[61] * table[45] + 3 * a[62] * table[46] +
				3 * a[63] * table[47];
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
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], double[])
	 */
	@Override
	public double value(float[] table, double[] df_da)
	{
		df_da[0] = a[1] * table[0] + 2 * a[2] * table[1] + 3 * a[3] * table[2] + a[5] * table[4] + 2 * a[6] * table[5] +
				3 * a[7] * table[6] + a[9] * table[8] + 2 * a[10] * table[9] + 3 * a[11] * table[10] +
				a[13] * table[12] + 2 * a[14] * table[13] + 3 * a[15] * table[14] + a[17] * table[16] +
				2 * a[18] * table[17] + 3 * a[19] * table[18] + a[21] * table[20] + 2 * a[22] * table[21] +
				3 * a[23] * table[22] + a[25] * table[24] + 2 * a[26] * table[25] + 3 * a[27] * table[26] +
				a[29] * table[28] + 2 * a[30] * table[29] + 3 * a[31] * table[30] + a[33] * table[32] +
				2 * a[34] * table[33] + 3 * a[35] * table[34] + a[37] * table[36] + 2 * a[38] * table[37] +
				3 * a[39] * table[38] + a[41] * table[40] + 2 * a[42] * table[41] + 3 * a[43] * table[42] +
				a[45] * table[44] + 2 * a[46] * table[45] + 3 * a[47] * table[46] + a[49] * table[48] +
				2 * a[50] * table[49] + 3 * a[51] * table[50] + a[53] * table[52] + 2 * a[54] * table[53] +
				3 * a[55] * table[54] + a[57] * table[56] + 2 * a[58] * table[57] + 3 * a[59] * table[58] +
				a[61] * table[60] + 2 * a[62] * table[61] + 3 * a[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + 2 * a[8] * table[4] +
				2 * a[9] * table[5] + 2 * a[10] * table[6] + 2 * a[11] * table[7] + 3 * a[12] * table[8] +
				3 * a[13] * table[9] + 3 * a[14] * table[10] + 3 * a[15] * table[11] + a[20] * table[16] +
				a[21] * table[17] + a[22] * table[18] + a[23] * table[19] + 2 * a[24] * table[20] +
				2 * a[25] * table[21] + 2 * a[26] * table[22] + 2 * a[27] * table[23] + 3 * a[28] * table[24] +
				3 * a[29] * table[25] + 3 * a[30] * table[26] + 3 * a[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + 2 * a[40] * table[36] +
				2 * a[41] * table[37] + 2 * a[42] * table[38] + 2 * a[43] * table[39] + 3 * a[44] * table[40] +
				3 * a[45] * table[41] + 3 * a[46] * table[42] + 3 * a[47] * table[43] + a[52] * table[48] +
				a[53] * table[49] + a[54] * table[50] + a[55] * table[51] + 2 * a[56] * table[52] +
				2 * a[57] * table[53] + 2 * a[58] * table[54] + 2 * a[59] * table[55] + 3 * a[60] * table[56] +
				3 * a[61] * table[57] + 3 * a[62] * table[58] + 3 * a[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + 2 * a[32] * table[16] + 2 * a[33] * table[17] + 2 * a[34] * table[18] +
				2 * a[35] * table[19] + 2 * a[36] * table[20] + 2 * a[37] * table[21] + 2 * a[38] * table[22] +
				2 * a[39] * table[23] + 2 * a[40] * table[24] + 2 * a[41] * table[25] + 2 * a[42] * table[26] +
				2 * a[43] * table[27] + 2 * a[44] * table[28] + 2 * a[45] * table[29] + 2 * a[46] * table[30] +
				2 * a[47] * table[31] + 3 * a[48] * table[32] + 3 * a[49] * table[33] + 3 * a[50] * table[34] +
				3 * a[51] * table[35] + 3 * a[52] * table[36] + 3 * a[53] * table[37] + 3 * a[54] * table[38] +
				3 * a[55] * table[39] + 3 * a[56] * table[40] + 3 * a[57] * table[41] + 3 * a[58] * table[42] +
				3 * a[59] * table[43] + 3 * a[60] * table[44] + 3 * a[61] * table[45] + 3 * a[62] * table[46] +
				3 * a[63] * table[47];
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
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[], double[], double[], double[])
	 */
	@Override
	public double value(double[] table, double[] table2, double[] table3, double[] df_da)
	{
		df_da[0] = a[1] * table[0] + a[2] * table2[1] + a[3] * table3[2] + a[5] * table[4] + a[6] * table2[5] +
				a[7] * table3[6] + a[9] * table[8] + a[10] * table2[9] + a[11] * table3[10] + a[13] * table[12] +
				a[14] * table2[13] + a[15] * table3[14] + a[17] * table[16] + a[18] * table2[17] + a[19] * table3[18] +
				a[21] * table[20] + a[22] * table2[21] + a[23] * table3[22] + a[25] * table[24] + a[26] * table2[25] +
				a[27] * table3[26] + a[29] * table[28] + a[30] * table2[29] + a[31] * table3[30] + a[33] * table[32] +
				a[34] * table2[33] + a[35] * table3[34] + a[37] * table[36] + a[38] * table2[37] + a[39] * table3[38] +
				a[41] * table[40] + a[42] * table2[41] + a[43] * table3[42] + a[45] * table[44] + a[46] * table2[45] +
				a[47] * table3[46] + a[49] * table[48] + a[50] * table2[49] + a[51] * table3[50] + a[53] * table[52] +
				a[54] * table2[53] + a[55] * table3[54] + a[57] * table[56] + a[58] * table2[57] + a[59] * table3[58] +
				a[61] * table[60] + a[62] * table2[61] + a[63] * table3[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a[8] * table2[4] +
				a[9] * table2[5] + a[10] * table2[6] + a[11] * table2[7] + a[12] * table3[8] + a[13] * table3[9] +
				a[14] * table3[10] + a[15] * table3[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a[24] * table2[20] + a[25] * table2[21] + a[26] * table2[22] + a[27] * table2[23] +
				a[28] * table3[24] + a[29] * table3[25] + a[30] * table3[26] + a[31] * table3[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a[40] * table2[36] + a[41] * table2[37] +
				a[42] * table2[38] + a[43] * table2[39] + a[44] * table3[40] + a[45] * table3[41] + a[46] * table3[42] +
				a[47] * table3[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a[56] * table2[52] + a[57] * table2[53] + a[58] * table2[54] + a[59] * table2[55] + a[60] * table3[56] +
				a[61] * table3[57] + a[62] * table3[58] + a[63] * table3[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a[32] * table2[16] + a[33] * table2[17] + a[34] * table2[18] + a[35] * table2[19] +
				a[36] * table2[20] + a[37] * table2[21] + a[38] * table2[22] + a[39] * table2[23] + a[40] * table2[24] +
				a[41] * table2[25] + a[42] * table2[26] + a[43] * table2[27] + a[44] * table2[28] + a[45] * table2[29] +
				a[46] * table2[30] + a[47] * table2[31] + a[48] * table3[32] + a[49] * table3[33] + a[50] * table3[34] +
				a[51] * table3[35] + a[52] * table3[36] + a[53] * table3[37] + a[54] * table3[38] + a[55] * table3[39] +
				a[56] * table3[40] + a[57] * table3[41] + a[58] * table3[42] + a[59] * table3[43] + a[60] * table3[44] +
				a[61] * table3[45] + a[62] * table3[46] + a[63] * table3[47];
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
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], float[], float[], double[])
	 */
	@Override
	public double value(float[] table, float[] table2, float[] table3, double[] df_da)
	{
		df_da[0] = a[1] * table[0] + a[2] * table2[1] + a[3] * table3[2] + a[5] * table[4] + a[6] * table2[5] +
				a[7] * table3[6] + a[9] * table[8] + a[10] * table2[9] + a[11] * table3[10] + a[13] * table[12] +
				a[14] * table2[13] + a[15] * table3[14] + a[17] * table[16] + a[18] * table2[17] + a[19] * table3[18] +
				a[21] * table[20] + a[22] * table2[21] + a[23] * table3[22] + a[25] * table[24] + a[26] * table2[25] +
				a[27] * table3[26] + a[29] * table[28] + a[30] * table2[29] + a[31] * table3[30] + a[33] * table[32] +
				a[34] * table2[33] + a[35] * table3[34] + a[37] * table[36] + a[38] * table2[37] + a[39] * table3[38] +
				a[41] * table[40] + a[42] * table2[41] + a[43] * table3[42] + a[45] * table[44] + a[46] * table2[45] +
				a[47] * table3[46] + a[49] * table[48] + a[50] * table2[49] + a[51] * table3[50] + a[53] * table[52] +
				a[54] * table2[53] + a[55] * table3[54] + a[57] * table[56] + a[58] * table2[57] + a[59] * table3[58] +
				a[61] * table[60] + a[62] * table2[61] + a[63] * table3[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a[8] * table2[4] +
				a[9] * table2[5] + a[10] * table2[6] + a[11] * table2[7] + a[12] * table3[8] + a[13] * table3[9] +
				a[14] * table3[10] + a[15] * table3[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a[24] * table2[20] + a[25] * table2[21] + a[26] * table2[22] + a[27] * table2[23] +
				a[28] * table3[24] + a[29] * table3[25] + a[30] * table3[26] + a[31] * table3[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a[40] * table2[36] + a[41] * table2[37] +
				a[42] * table2[38] + a[43] * table2[39] + a[44] * table3[40] + a[45] * table3[41] + a[46] * table3[42] +
				a[47] * table3[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a[56] * table2[52] + a[57] * table2[53] + a[58] * table2[54] + a[59] * table2[55] + a[60] * table3[56] +
				a[61] * table3[57] + a[62] * table3[58] + a[63] * table3[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a[32] * table2[16] + a[33] * table2[17] + a[34] * table2[18] + a[35] * table2[19] +
				a[36] * table2[20] + a[37] * table2[21] + a[38] * table2[22] + a[39] * table2[23] + a[40] * table2[24] +
				a[41] * table2[25] + a[42] * table2[26] + a[43] * table2[27] + a[44] * table2[28] + a[45] * table2[29] +
				a[46] * table2[30] + a[47] * table2[31] + a[48] * table3[32] + a[49] * table3[33] + a[50] * table3[34] +
				a[51] * table3[35] + a[52] * table3[36] + a[53] * table3[37] + a[54] * table3[38] + a[55] * table3[39] +
				a[56] * table3[40] + a[57] * table3[41] + a[58] * table3[42] + a[59] * table3[43] + a[60] * table3[44] +
				a[61] * table3[45] + a[62] * table3[46] + a[63] * table3[47];
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
		result += a[1] * pX[0];
		df_da[0] += 2 * a[2] * pX[0];
		d2f_da2[0] += 6 * a[3] * pX[0];
		df_da[1] += a[5] * pX[0];
		d2f_da2[1] += 2 * a[9] * pX[0];
		df_da[2] += a[17] * pX[0];
		d2f_da2[2] += 2 * a[33] * pX[0];
		result += a[2] * pX[1];
		df_da[0] += 3 * a[3] * pX[1];
		df_da[1] += a[6] * pX[1];
		d2f_da2[1] += 2 * a[10] * pX[1];
		df_da[2] += a[18] * pX[1];
		d2f_da2[2] += 2 * a[34] * pX[1];
		result += a[3] * pX[2];
		df_da[1] += a[7] * pX[2];
		d2f_da2[1] += 2 * a[11] * pX[2];
		df_da[2] += a[19] * pX[2];
		d2f_da2[2] += 2 * a[35] * pX[2];
		result += a[4] * pY[0];
		df_da[0] += a[5] * pY[0];
		d2f_da2[0] += 2 * a[6] * pY[0];
		df_da[1] += 2 * a[8] * pY[0];
		d2f_da2[1] += 6 * a[12] * pY[0];
		df_da[2] += a[20] * pY[0];
		d2f_da2[2] += 2 * a[36] * pY[0];
		pXpYpZ = pX[0] * pY[0];
		result += a[5] * pXpYpZ;
		df_da[0] += 2 * a[6] * pXpYpZ;
		d2f_da2[0] += 6 * a[7] * pXpYpZ;
		df_da[1] += 2 * a[9] * pXpYpZ;
		d2f_da2[1] += 6 * a[13] * pXpYpZ;
		df_da[2] += a[21] * pXpYpZ;
		d2f_da2[2] += 2 * a[37] * pXpYpZ;
		pXpYpZ = pX[1] * pY[0];
		result += a[6] * pXpYpZ;
		df_da[0] += 3 * a[7] * pXpYpZ;
		df_da[1] += 2 * a[10] * pXpYpZ;
		d2f_da2[1] += 6 * a[14] * pXpYpZ;
		df_da[2] += a[22] * pXpYpZ;
		d2f_da2[2] += 2 * a[38] * pXpYpZ;
		pXpYpZ = pX[2] * pY[0];
		result += a[7] * pXpYpZ;
		df_da[1] += 2 * a[11] * pXpYpZ;
		d2f_da2[1] += 6 * a[15] * pXpYpZ;
		df_da[2] += a[23] * pXpYpZ;
		d2f_da2[2] += 2 * a[39] * pXpYpZ;
		result += a[8] * pY[1];
		df_da[0] += a[9] * pY[1];
		d2f_da2[0] += 2 * a[10] * pY[1];
		df_da[1] += 3 * a[12] * pY[1];
		df_da[2] += a[24] * pY[1];
		d2f_da2[2] += 2 * a[40] * pY[1];
		pXpYpZ = pX[0] * pY[1];
		result += a[9] * pXpYpZ;
		df_da[0] += 2 * a[10] * pXpYpZ;
		d2f_da2[0] += 6 * a[11] * pXpYpZ;
		df_da[1] += 3 * a[13] * pXpYpZ;
		df_da[2] += a[25] * pXpYpZ;
		d2f_da2[2] += 2 * a[41] * pXpYpZ;
		pXpYpZ = pX[1] * pY[1];
		result += a[10] * pXpYpZ;
		df_da[0] += 3 * a[11] * pXpYpZ;
		df_da[1] += 3 * a[14] * pXpYpZ;
		df_da[2] += a[26] * pXpYpZ;
		d2f_da2[2] += 2 * a[42] * pXpYpZ;
		pXpYpZ = pX[2] * pY[1];
		result += a[11] * pXpYpZ;
		df_da[1] += 3 * a[15] * pXpYpZ;
		df_da[2] += a[27] * pXpYpZ;
		d2f_da2[2] += 2 * a[43] * pXpYpZ;
		result += a[12] * pY[2];
		df_da[0] += a[13] * pY[2];
		d2f_da2[0] += 2 * a[14] * pY[2];
		df_da[2] += a[28] * pY[2];
		d2f_da2[2] += 2 * a[44] * pY[2];
		pXpYpZ = pX[0] * pY[2];
		result += a[13] * pXpYpZ;
		df_da[0] += 2 * a[14] * pXpYpZ;
		d2f_da2[0] += 6 * a[15] * pXpYpZ;
		df_da[2] += a[29] * pXpYpZ;
		d2f_da2[2] += 2 * a[45] * pXpYpZ;
		pXpYpZ = pX[1] * pY[2];
		result += a[14] * pXpYpZ;
		df_da[0] += 3 * a[15] * pXpYpZ;
		df_da[2] += a[30] * pXpYpZ;
		d2f_da2[2] += 2 * a[46] * pXpYpZ;
		pXpYpZ = pX[2] * pY[2];
		result += a[15] * pXpYpZ;
		df_da[2] += a[31] * pXpYpZ;
		d2f_da2[2] += 2 * a[47] * pXpYpZ;
		result += a[16] * pZ[0];
		df_da[0] += a[17] * pZ[0];
		d2f_da2[0] += 2 * a[18] * pZ[0];
		df_da[1] += a[20] * pZ[0];
		d2f_da2[1] += 2 * a[24] * pZ[0];
		df_da[2] += 2 * a[32] * pZ[0];
		d2f_da2[2] += 6 * a[48] * pZ[0];
		pXpYpZ = pX[0] * pZ[0];
		result += a[17] * pXpYpZ;
		df_da[0] += 2 * a[18] * pXpYpZ;
		d2f_da2[0] += 6 * a[19] * pXpYpZ;
		df_da[1] += a[21] * pXpYpZ;
		d2f_da2[1] += 2 * a[25] * pXpYpZ;
		df_da[2] += 2 * a[33] * pXpYpZ;
		d2f_da2[2] += 6 * a[49] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[0];
		result += a[18] * pXpYpZ;
		df_da[0] += 3 * a[19] * pXpYpZ;
		df_da[1] += a[22] * pXpYpZ;
		d2f_da2[1] += 2 * a[26] * pXpYpZ;
		df_da[2] += 2 * a[34] * pXpYpZ;
		d2f_da2[2] += 6 * a[50] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[0];
		result += a[19] * pXpYpZ;
		df_da[1] += a[23] * pXpYpZ;
		d2f_da2[1] += 2 * a[27] * pXpYpZ;
		df_da[2] += 2 * a[35] * pXpYpZ;
		d2f_da2[2] += 6 * a[51] * pXpYpZ;
		pYpZ = pY[0] * pZ[0];
		result += a[20] * pYpZ;
		df_da[0] += a[21] * pYpZ;
		d2f_da2[0] += 2 * a[22] * pYpZ;
		df_da[1] += 2 * a[24] * pYpZ;
		d2f_da2[1] += 6 * a[28] * pYpZ;
		df_da[2] += 2 * a[36] * pYpZ;
		d2f_da2[2] += 6 * a[52] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[21] * pXpYpZ;
		df_da[0] += 2 * a[22] * pXpYpZ;
		d2f_da2[0] += 6 * a[23] * pXpYpZ;
		df_da[1] += 2 * a[25] * pXpYpZ;
		d2f_da2[1] += 6 * a[29] * pXpYpZ;
		df_da[2] += 2 * a[37] * pXpYpZ;
		d2f_da2[2] += 6 * a[53] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[22] * pXpYpZ;
		df_da[0] += 3 * a[23] * pXpYpZ;
		df_da[1] += 2 * a[26] * pXpYpZ;
		d2f_da2[1] += 6 * a[30] * pXpYpZ;
		df_da[2] += 2 * a[38] * pXpYpZ;
		d2f_da2[2] += 6 * a[54] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[23] * pXpYpZ;
		df_da[1] += 2 * a[27] * pXpYpZ;
		d2f_da2[1] += 6 * a[31] * pXpYpZ;
		df_da[2] += 2 * a[39] * pXpYpZ;
		d2f_da2[2] += 6 * a[55] * pXpYpZ;
		pYpZ = pY[1] * pZ[0];
		result += a[24] * pYpZ;
		df_da[0] += a[25] * pYpZ;
		d2f_da2[0] += 2 * a[26] * pYpZ;
		df_da[1] += 3 * a[28] * pYpZ;
		df_da[2] += 2 * a[40] * pYpZ;
		d2f_da2[2] += 6 * a[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[25] * pXpYpZ;
		df_da[0] += 2 * a[26] * pXpYpZ;
		d2f_da2[0] += 6 * a[27] * pXpYpZ;
		df_da[1] += 3 * a[29] * pXpYpZ;
		df_da[2] += 2 * a[41] * pXpYpZ;
		d2f_da2[2] += 6 * a[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[26] * pXpYpZ;
		df_da[0] += 3 * a[27] * pXpYpZ;
		df_da[1] += 3 * a[30] * pXpYpZ;
		df_da[2] += 2 * a[42] * pXpYpZ;
		d2f_da2[2] += 6 * a[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[27] * pXpYpZ;
		df_da[1] += 3 * a[31] * pXpYpZ;
		df_da[2] += 2 * a[43] * pXpYpZ;
		d2f_da2[2] += 6 * a[59] * pXpYpZ;
		pYpZ = pY[2] * pZ[0];
		result += a[28] * pYpZ;
		df_da[0] += a[29] * pYpZ;
		d2f_da2[0] += 2 * a[30] * pYpZ;
		df_da[2] += 2 * a[44] * pYpZ;
		d2f_da2[2] += 6 * a[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[29] * pXpYpZ;
		df_da[0] += 2 * a[30] * pXpYpZ;
		d2f_da2[0] += 6 * a[31] * pXpYpZ;
		df_da[2] += 2 * a[45] * pXpYpZ;
		d2f_da2[2] += 6 * a[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[30] * pXpYpZ;
		df_da[0] += 3 * a[31] * pXpYpZ;
		df_da[2] += 2 * a[46] * pXpYpZ;
		d2f_da2[2] += 6 * a[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[31] * pXpYpZ;
		df_da[2] += 2 * a[47] * pXpYpZ;
		d2f_da2[2] += 6 * a[63] * pXpYpZ;
		result += a[32] * pZ[1];
		df_da[0] += a[33] * pZ[1];
		d2f_da2[0] += 2 * a[34] * pZ[1];
		df_da[1] += a[36] * pZ[1];
		d2f_da2[1] += 2 * a[40] * pZ[1];
		df_da[2] += 3 * a[48] * pZ[1];
		pXpYpZ = pX[0] * pZ[1];
		result += a[33] * pXpYpZ;
		df_da[0] += 2 * a[34] * pXpYpZ;
		d2f_da2[0] += 6 * a[35] * pXpYpZ;
		df_da[1] += a[37] * pXpYpZ;
		d2f_da2[1] += 2 * a[41] * pXpYpZ;
		df_da[2] += 3 * a[49] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[1];
		result += a[34] * pXpYpZ;
		df_da[0] += 3 * a[35] * pXpYpZ;
		df_da[1] += a[38] * pXpYpZ;
		d2f_da2[1] += 2 * a[42] * pXpYpZ;
		df_da[2] += 3 * a[50] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[1];
		result += a[35] * pXpYpZ;
		df_da[1] += a[39] * pXpYpZ;
		d2f_da2[1] += 2 * a[43] * pXpYpZ;
		df_da[2] += 3 * a[51] * pXpYpZ;
		pYpZ = pY[0] * pZ[1];
		result += a[36] * pYpZ;
		df_da[0] += a[37] * pYpZ;
		d2f_da2[0] += 2 * a[38] * pYpZ;
		df_da[1] += 2 * a[40] * pYpZ;
		d2f_da2[1] += 6 * a[44] * pYpZ;
		df_da[2] += 3 * a[52] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[37] * pXpYpZ;
		df_da[0] += 2 * a[38] * pXpYpZ;
		d2f_da2[0] += 6 * a[39] * pXpYpZ;
		df_da[1] += 2 * a[41] * pXpYpZ;
		d2f_da2[1] += 6 * a[45] * pXpYpZ;
		df_da[2] += 3 * a[53] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[38] * pXpYpZ;
		df_da[0] += 3 * a[39] * pXpYpZ;
		df_da[1] += 2 * a[42] * pXpYpZ;
		d2f_da2[1] += 6 * a[46] * pXpYpZ;
		df_da[2] += 3 * a[54] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[39] * pXpYpZ;
		df_da[1] += 2 * a[43] * pXpYpZ;
		d2f_da2[1] += 6 * a[47] * pXpYpZ;
		df_da[2] += 3 * a[55] * pXpYpZ;
		pYpZ = pY[1] * pZ[1];
		result += a[40] * pYpZ;
		df_da[0] += a[41] * pYpZ;
		d2f_da2[0] += 2 * a[42] * pYpZ;
		df_da[1] += 3 * a[44] * pYpZ;
		df_da[2] += 3 * a[56] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[41] * pXpYpZ;
		df_da[0] += 2 * a[42] * pXpYpZ;
		d2f_da2[0] += 6 * a[43] * pXpYpZ;
		df_da[1] += 3 * a[45] * pXpYpZ;
		df_da[2] += 3 * a[57] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[42] * pXpYpZ;
		df_da[0] += 3 * a[43] * pXpYpZ;
		df_da[1] += 3 * a[46] * pXpYpZ;
		df_da[2] += 3 * a[58] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[43] * pXpYpZ;
		df_da[1] += 3 * a[47] * pXpYpZ;
		df_da[2] += 3 * a[59] * pXpYpZ;
		pYpZ = pY[2] * pZ[1];
		result += a[44] * pYpZ;
		df_da[0] += a[45] * pYpZ;
		d2f_da2[0] += 2 * a[46] * pYpZ;
		df_da[2] += 3 * a[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[45] * pXpYpZ;
		df_da[0] += 2 * a[46] * pXpYpZ;
		d2f_da2[0] += 6 * a[47] * pXpYpZ;
		df_da[2] += 3 * a[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[46] * pXpYpZ;
		df_da[0] += 3 * a[47] * pXpYpZ;
		df_da[2] += 3 * a[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[47] * pXpYpZ;
		df_da[2] += 3 * a[63] * pXpYpZ;
		result += a[48] * pZ[2];
		df_da[0] += a[49] * pZ[2];
		d2f_da2[0] += 2 * a[50] * pZ[2];
		df_da[1] += a[52] * pZ[2];
		d2f_da2[1] += 2 * a[56] * pZ[2];
		pXpYpZ = pX[0] * pZ[2];
		result += a[49] * pXpYpZ;
		df_da[0] += 2 * a[50] * pXpYpZ;
		d2f_da2[0] += 6 * a[51] * pXpYpZ;
		df_da[1] += a[53] * pXpYpZ;
		d2f_da2[1] += 2 * a[57] * pXpYpZ;
		pXpYpZ = pX[1] * pZ[2];
		result += a[50] * pXpYpZ;
		df_da[0] += 3 * a[51] * pXpYpZ;
		df_da[1] += a[54] * pXpYpZ;
		d2f_da2[1] += 2 * a[58] * pXpYpZ;
		pXpYpZ = pX[2] * pZ[2];
		result += a[51] * pXpYpZ;
		df_da[1] += a[55] * pXpYpZ;
		d2f_da2[1] += 2 * a[59] * pXpYpZ;
		pYpZ = pY[0] * pZ[2];
		result += a[52] * pYpZ;
		df_da[0] += a[53] * pYpZ;
		d2f_da2[0] += 2 * a[54] * pYpZ;
		df_da[1] += 2 * a[56] * pYpZ;
		d2f_da2[1] += 6 * a[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[53] * pXpYpZ;
		df_da[0] += 2 * a[54] * pXpYpZ;
		d2f_da2[0] += 6 * a[55] * pXpYpZ;
		df_da[1] += 2 * a[57] * pXpYpZ;
		d2f_da2[1] += 6 * a[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[54] * pXpYpZ;
		df_da[0] += 3 * a[55] * pXpYpZ;
		df_da[1] += 2 * a[58] * pXpYpZ;
		d2f_da2[1] += 6 * a[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[55] * pXpYpZ;
		df_da[1] += 2 * a[59] * pXpYpZ;
		d2f_da2[1] += 6 * a[63] * pXpYpZ;
		pYpZ = pY[1] * pZ[2];
		result += a[56] * pYpZ;
		df_da[0] += a[57] * pYpZ;
		d2f_da2[0] += 2 * a[58] * pYpZ;
		df_da[1] += 3 * a[60] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[57] * pXpYpZ;
		df_da[0] += 2 * a[58] * pXpYpZ;
		d2f_da2[0] += 6 * a[59] * pXpYpZ;
		df_da[1] += 3 * a[61] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[58] * pXpYpZ;
		df_da[0] += 3 * a[59] * pXpYpZ;
		df_da[1] += 3 * a[62] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[59] * pXpYpZ;
		df_da[1] += 3 * a[63] * pXpYpZ;
		pYpZ = pY[2] * pZ[2];
		result += a[60] * pYpZ;
		df_da[0] += a[61] * pYpZ;
		d2f_da2[0] += 2 * a[62] * pYpZ;
		pXpYpZ = pX[0] * pYpZ;
		result += a[61] * pXpYpZ;
		df_da[0] += 2 * a[62] * pXpYpZ;
		d2f_da2[0] += 6 * a[63] * pXpYpZ;
		pXpYpZ = pX[1] * pYpZ;
		result += a[62] * pXpYpZ;
		df_da[0] += 3 * a[63] * pXpYpZ;
		pXpYpZ = pX[2] * pYpZ;
		result += a[63] * pXpYpZ;

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
		df_da[0] = a[1] * table[0] + 2 * a[2] * table[1] + 3 * a[3] * table[2] + a[5] * table[4] + 2 * a[6] * table[5] +
				3 * a[7] * table[6] + a[9] * table[8] + 2 * a[10] * table[9] + 3 * a[11] * table[10] +
				a[13] * table[12] + 2 * a[14] * table[13] + 3 * a[15] * table[14] + a[17] * table[16] +
				2 * a[18] * table[17] + 3 * a[19] * table[18] + a[21] * table[20] + 2 * a[22] * table[21] +
				3 * a[23] * table[22] + a[25] * table[24] + 2 * a[26] * table[25] + 3 * a[27] * table[26] +
				a[29] * table[28] + 2 * a[30] * table[29] + 3 * a[31] * table[30] + a[33] * table[32] +
				2 * a[34] * table[33] + 3 * a[35] * table[34] + a[37] * table[36] + 2 * a[38] * table[37] +
				3 * a[39] * table[38] + a[41] * table[40] + 2 * a[42] * table[41] + 3 * a[43] * table[42] +
				a[45] * table[44] + 2 * a[46] * table[45] + 3 * a[47] * table[46] + a[49] * table[48] +
				2 * a[50] * table[49] + 3 * a[51] * table[50] + a[53] * table[52] + 2 * a[54] * table[53] +
				3 * a[55] * table[54] + a[57] * table[56] + 2 * a[58] * table[57] + 3 * a[59] * table[58] +
				a[61] * table[60] + 2 * a[62] * table[61] + 3 * a[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + 2 * a[8] * table[4] +
				2 * a[9] * table[5] + 2 * a[10] * table[6] + 2 * a[11] * table[7] + 3 * a[12] * table[8] +
				3 * a[13] * table[9] + 3 * a[14] * table[10] + 3 * a[15] * table[11] + a[20] * table[16] +
				a[21] * table[17] + a[22] * table[18] + a[23] * table[19] + 2 * a[24] * table[20] +
				2 * a[25] * table[21] + 2 * a[26] * table[22] + 2 * a[27] * table[23] + 3 * a[28] * table[24] +
				3 * a[29] * table[25] + 3 * a[30] * table[26] + 3 * a[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + 2 * a[40] * table[36] +
				2 * a[41] * table[37] + 2 * a[42] * table[38] + 2 * a[43] * table[39] + 3 * a[44] * table[40] +
				3 * a[45] * table[41] + 3 * a[46] * table[42] + 3 * a[47] * table[43] + a[52] * table[48] +
				a[53] * table[49] + a[54] * table[50] + a[55] * table[51] + 2 * a[56] * table[52] +
				2 * a[57] * table[53] + 2 * a[58] * table[54] + 2 * a[59] * table[55] + 3 * a[60] * table[56] +
				3 * a[61] * table[57] + 3 * a[62] * table[58] + 3 * a[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + 2 * a[32] * table[16] + 2 * a[33] * table[17] + 2 * a[34] * table[18] +
				2 * a[35] * table[19] + 2 * a[36] * table[20] + 2 * a[37] * table[21] + 2 * a[38] * table[22] +
				2 * a[39] * table[23] + 2 * a[40] * table[24] + 2 * a[41] * table[25] + 2 * a[42] * table[26] +
				2 * a[43] * table[27] + 2 * a[44] * table[28] + 2 * a[45] * table[29] + 2 * a[46] * table[30] +
				2 * a[47] * table[31] + 3 * a[48] * table[32] + 3 * a[49] * table[33] + 3 * a[50] * table[34] +
				3 * a[51] * table[35] + 3 * a[52] * table[36] + 3 * a[53] * table[37] + 3 * a[54] * table[38] +
				3 * a[55] * table[39] + 3 * a[56] * table[40] + 3 * a[57] * table[41] + 3 * a[58] * table[42] +
				3 * a[59] * table[43] + 3 * a[60] * table[44] + 3 * a[61] * table[45] + 3 * a[62] * table[46] +
				3 * a[63] * table[47];
		d2f_da2[0] = 2 * a[2] * table[0] + 6 * a[3] * table[1] + 2 * a[6] * table[4] + 6 * a[7] * table[5] +
				2 * a[10] * table[8] + 6 * a[11] * table[9] + 2 * a[14] * table[12] + 6 * a[15] * table[13] +
				2 * a[18] * table[16] + 6 * a[19] * table[17] + 2 * a[22] * table[20] + 6 * a[23] * table[21] +
				2 * a[26] * table[24] + 6 * a[27] * table[25] + 2 * a[30] * table[28] + 6 * a[31] * table[29] +
				2 * a[34] * table[32] + 6 * a[35] * table[33] + 2 * a[38] * table[36] + 6 * a[39] * table[37] +
				2 * a[42] * table[40] + 6 * a[43] * table[41] + 2 * a[46] * table[44] + 6 * a[47] * table[45] +
				2 * a[50] * table[48] + 6 * a[51] * table[49] + 2 * a[54] * table[52] + 6 * a[55] * table[53] +
				2 * a[58] * table[56] + 6 * a[59] * table[57] + 2 * a[62] * table[60] + 6 * a[63] * table[61];
		d2f_da2[1] = 2 * a[8] * table[0] + 2 * a[9] * table[1] + 2 * a[10] * table[2] + 2 * a[11] * table[3] +
				6 * a[12] * table[4] + 6 * a[13] * table[5] + 6 * a[14] * table[6] + 6 * a[15] * table[7] +
				2 * a[24] * table[16] + 2 * a[25] * table[17] + 2 * a[26] * table[18] + 2 * a[27] * table[19] +
				6 * a[28] * table[20] + 6 * a[29] * table[21] + 6 * a[30] * table[22] + 6 * a[31] * table[23] +
				2 * a[40] * table[32] + 2 * a[41] * table[33] + 2 * a[42] * table[34] + 2 * a[43] * table[35] +
				6 * a[44] * table[36] + 6 * a[45] * table[37] + 6 * a[46] * table[38] + 6 * a[47] * table[39] +
				2 * a[56] * table[48] + 2 * a[57] * table[49] + 2 * a[58] * table[50] + 2 * a[59] * table[51] +
				6 * a[60] * table[52] + 6 * a[61] * table[53] + 6 * a[62] * table[54] + 6 * a[63] * table[55];
		d2f_da2[2] = 2 * a[32] * table[0] + 2 * a[33] * table[1] + 2 * a[34] * table[2] + 2 * a[35] * table[3] +
				2 * a[36] * table[4] + 2 * a[37] * table[5] + 2 * a[38] * table[6] + 2 * a[39] * table[7] +
				2 * a[40] * table[8] + 2 * a[41] * table[9] + 2 * a[42] * table[10] + 2 * a[43] * table[11] +
				2 * a[44] * table[12] + 2 * a[45] * table[13] + 2 * a[46] * table[14] + 2 * a[47] * table[15] +
				6 * a[48] * table[16] + 6 * a[49] * table[17] + 6 * a[50] * table[18] + 6 * a[51] * table[19] +
				6 * a[52] * table[20] + 6 * a[53] * table[21] + 6 * a[54] * table[22] + 6 * a[55] * table[23] +
				6 * a[56] * table[24] + 6 * a[57] * table[25] + 6 * a[58] * table[26] + 6 * a[59] * table[27] +
				6 * a[60] * table[28] + 6 * a[61] * table[29] + 6 * a[62] * table[30] + 6 * a[63] * table[31];
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
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], double[], double[])
	 */
	@Override
	public double value(float[] table, double[] df_da, double[] d2f_da2)
	{
		df_da[0] = a[1] * table[0] + 2 * a[2] * table[1] + 3 * a[3] * table[2] + a[5] * table[4] + 2 * a[6] * table[5] +
				3 * a[7] * table[6] + a[9] * table[8] + 2 * a[10] * table[9] + 3 * a[11] * table[10] +
				a[13] * table[12] + 2 * a[14] * table[13] + 3 * a[15] * table[14] + a[17] * table[16] +
				2 * a[18] * table[17] + 3 * a[19] * table[18] + a[21] * table[20] + 2 * a[22] * table[21] +
				3 * a[23] * table[22] + a[25] * table[24] + 2 * a[26] * table[25] + 3 * a[27] * table[26] +
				a[29] * table[28] + 2 * a[30] * table[29] + 3 * a[31] * table[30] + a[33] * table[32] +
				2 * a[34] * table[33] + 3 * a[35] * table[34] + a[37] * table[36] + 2 * a[38] * table[37] +
				3 * a[39] * table[38] + a[41] * table[40] + 2 * a[42] * table[41] + 3 * a[43] * table[42] +
				a[45] * table[44] + 2 * a[46] * table[45] + 3 * a[47] * table[46] + a[49] * table[48] +
				2 * a[50] * table[49] + 3 * a[51] * table[50] + a[53] * table[52] + 2 * a[54] * table[53] +
				3 * a[55] * table[54] + a[57] * table[56] + 2 * a[58] * table[57] + 3 * a[59] * table[58] +
				a[61] * table[60] + 2 * a[62] * table[61] + 3 * a[63] * table[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + 2 * a[8] * table[4] +
				2 * a[9] * table[5] + 2 * a[10] * table[6] + 2 * a[11] * table[7] + 3 * a[12] * table[8] +
				3 * a[13] * table[9] + 3 * a[14] * table[10] + 3 * a[15] * table[11] + a[20] * table[16] +
				a[21] * table[17] + a[22] * table[18] + a[23] * table[19] + 2 * a[24] * table[20] +
				2 * a[25] * table[21] + 2 * a[26] * table[22] + 2 * a[27] * table[23] + 3 * a[28] * table[24] +
				3 * a[29] * table[25] + 3 * a[30] * table[26] + 3 * a[31] * table[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + 2 * a[40] * table[36] +
				2 * a[41] * table[37] + 2 * a[42] * table[38] + 2 * a[43] * table[39] + 3 * a[44] * table[40] +
				3 * a[45] * table[41] + 3 * a[46] * table[42] + 3 * a[47] * table[43] + a[52] * table[48] +
				a[53] * table[49] + a[54] * table[50] + a[55] * table[51] + 2 * a[56] * table[52] +
				2 * a[57] * table[53] + 2 * a[58] * table[54] + 2 * a[59] * table[55] + 3 * a[60] * table[56] +
				3 * a[61] * table[57] + 3 * a[62] * table[58] + 3 * a[63] * table[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + 2 * a[32] * table[16] + 2 * a[33] * table[17] + 2 * a[34] * table[18] +
				2 * a[35] * table[19] + 2 * a[36] * table[20] + 2 * a[37] * table[21] + 2 * a[38] * table[22] +
				2 * a[39] * table[23] + 2 * a[40] * table[24] + 2 * a[41] * table[25] + 2 * a[42] * table[26] +
				2 * a[43] * table[27] + 2 * a[44] * table[28] + 2 * a[45] * table[29] + 2 * a[46] * table[30] +
				2 * a[47] * table[31] + 3 * a[48] * table[32] + 3 * a[49] * table[33] + 3 * a[50] * table[34] +
				3 * a[51] * table[35] + 3 * a[52] * table[36] + 3 * a[53] * table[37] + 3 * a[54] * table[38] +
				3 * a[55] * table[39] + 3 * a[56] * table[40] + 3 * a[57] * table[41] + 3 * a[58] * table[42] +
				3 * a[59] * table[43] + 3 * a[60] * table[44] + 3 * a[61] * table[45] + 3 * a[62] * table[46] +
				3 * a[63] * table[47];
		d2f_da2[0] = 2 * a[2] * table[0] + 6 * a[3] * table[1] + 2 * a[6] * table[4] + 6 * a[7] * table[5] +
				2 * a[10] * table[8] + 6 * a[11] * table[9] + 2 * a[14] * table[12] + 6 * a[15] * table[13] +
				2 * a[18] * table[16] + 6 * a[19] * table[17] + 2 * a[22] * table[20] + 6 * a[23] * table[21] +
				2 * a[26] * table[24] + 6 * a[27] * table[25] + 2 * a[30] * table[28] + 6 * a[31] * table[29] +
				2 * a[34] * table[32] + 6 * a[35] * table[33] + 2 * a[38] * table[36] + 6 * a[39] * table[37] +
				2 * a[42] * table[40] + 6 * a[43] * table[41] + 2 * a[46] * table[44] + 6 * a[47] * table[45] +
				2 * a[50] * table[48] + 6 * a[51] * table[49] + 2 * a[54] * table[52] + 6 * a[55] * table[53] +
				2 * a[58] * table[56] + 6 * a[59] * table[57] + 2 * a[62] * table[60] + 6 * a[63] * table[61];
		d2f_da2[1] = 2 * a[8] * table[0] + 2 * a[9] * table[1] + 2 * a[10] * table[2] + 2 * a[11] * table[3] +
				6 * a[12] * table[4] + 6 * a[13] * table[5] + 6 * a[14] * table[6] + 6 * a[15] * table[7] +
				2 * a[24] * table[16] + 2 * a[25] * table[17] + 2 * a[26] * table[18] + 2 * a[27] * table[19] +
				6 * a[28] * table[20] + 6 * a[29] * table[21] + 6 * a[30] * table[22] + 6 * a[31] * table[23] +
				2 * a[40] * table[32] + 2 * a[41] * table[33] + 2 * a[42] * table[34] + 2 * a[43] * table[35] +
				6 * a[44] * table[36] + 6 * a[45] * table[37] + 6 * a[46] * table[38] + 6 * a[47] * table[39] +
				2 * a[56] * table[48] + 2 * a[57] * table[49] + 2 * a[58] * table[50] + 2 * a[59] * table[51] +
				6 * a[60] * table[52] + 6 * a[61] * table[53] + 6 * a[62] * table[54] + 6 * a[63] * table[55];
		d2f_da2[2] = 2 * a[32] * table[0] + 2 * a[33] * table[1] + 2 * a[34] * table[2] + 2 * a[35] * table[3] +
				2 * a[36] * table[4] + 2 * a[37] * table[5] + 2 * a[38] * table[6] + 2 * a[39] * table[7] +
				2 * a[40] * table[8] + 2 * a[41] * table[9] + 2 * a[42] * table[10] + 2 * a[43] * table[11] +
				2 * a[44] * table[12] + 2 * a[45] * table[13] + 2 * a[46] * table[14] + 2 * a[47] * table[15] +
				6 * a[48] * table[16] + 6 * a[49] * table[17] + 6 * a[50] * table[18] + 6 * a[51] * table[19] +
				6 * a[52] * table[20] + 6 * a[53] * table[21] + 6 * a[54] * table[22] + 6 * a[55] * table[23] +
				6 * a[56] * table[24] + 6 * a[57] * table[25] + 6 * a[58] * table[26] + 6 * a[59] * table[27] +
				6 * a[60] * table[28] + 6 * a[61] * table[29] + 6 * a[62] * table[30] + 6 * a[63] * table[31];
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
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(double[], double[], double[], double[], double[],
	 * double[])
	 */
	@Override
	public double value(double[] table, double[] table2, double[] table3, double[] table6, double[] df_da,
			double[] d2f_da2)
	{
		df_da[0] = a[1] * table[0] + a[2] * table2[1] + a[3] * table3[2] + a[5] * table[4] + a[6] * table2[5] +
				a[7] * table3[6] + a[9] * table[8] + a[10] * table2[9] + a[11] * table3[10] + a[13] * table[12] +
				a[14] * table2[13] + a[15] * table3[14] + a[17] * table[16] + a[18] * table2[17] + a[19] * table3[18] +
				a[21] * table[20] + a[22] * table2[21] + a[23] * table3[22] + a[25] * table[24] + a[26] * table2[25] +
				a[27] * table3[26] + a[29] * table[28] + a[30] * table2[29] + a[31] * table3[30] + a[33] * table[32] +
				a[34] * table2[33] + a[35] * table3[34] + a[37] * table[36] + a[38] * table2[37] + a[39] * table3[38] +
				a[41] * table[40] + a[42] * table2[41] + a[43] * table3[42] + a[45] * table[44] + a[46] * table2[45] +
				a[47] * table3[46] + a[49] * table[48] + a[50] * table2[49] + a[51] * table3[50] + a[53] * table[52] +
				a[54] * table2[53] + a[55] * table3[54] + a[57] * table[56] + a[58] * table2[57] + a[59] * table3[58] +
				a[61] * table[60] + a[62] * table2[61] + a[63] * table3[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a[8] * table2[4] +
				a[9] * table2[5] + a[10] * table2[6] + a[11] * table2[7] + a[12] * table3[8] + a[13] * table3[9] +
				a[14] * table3[10] + a[15] * table3[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a[24] * table2[20] + a[25] * table2[21] + a[26] * table2[22] + a[27] * table2[23] +
				a[28] * table3[24] + a[29] * table3[25] + a[30] * table3[26] + a[31] * table3[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a[40] * table2[36] + a[41] * table2[37] +
				a[42] * table2[38] + a[43] * table2[39] + a[44] * table3[40] + a[45] * table3[41] + a[46] * table3[42] +
				a[47] * table3[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a[56] * table2[52] + a[57] * table2[53] + a[58] * table2[54] + a[59] * table2[55] + a[60] * table3[56] +
				a[61] * table3[57] + a[62] * table3[58] + a[63] * table3[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a[32] * table2[16] + a[33] * table2[17] + a[34] * table2[18] + a[35] * table2[19] +
				a[36] * table2[20] + a[37] * table2[21] + a[38] * table2[22] + a[39] * table2[23] + a[40] * table2[24] +
				a[41] * table2[25] + a[42] * table2[26] + a[43] * table2[27] + a[44] * table2[28] + a[45] * table2[29] +
				a[46] * table2[30] + a[47] * table2[31] + a[48] * table3[32] + a[49] * table3[33] + a[50] * table3[34] +
				a[51] * table3[35] + a[52] * table3[36] + a[53] * table3[37] + a[54] * table3[38] + a[55] * table3[39] +
				a[56] * table3[40] + a[57] * table3[41] + a[58] * table3[42] + a[59] * table3[43] + a[60] * table3[44] +
				a[61] * table3[45] + a[62] * table3[46] + a[63] * table3[47];
		d2f_da2[0] = a[2] * table2[0] + a[3] * table6[1] + a[6] * table2[4] + a[7] * table6[5] + a[10] * table2[8] +
				a[11] * table6[9] + a[14] * table2[12] + a[15] * table6[13] + a[18] * table2[16] + a[19] * table6[17] +
				a[22] * table2[20] + a[23] * table6[21] + a[26] * table2[24] + a[27] * table6[25] + a[30] * table2[28] +
				a[31] * table6[29] + a[34] * table2[32] + a[35] * table6[33] + a[38] * table2[36] + a[39] * table6[37] +
				a[42] * table2[40] + a[43] * table6[41] + a[46] * table2[44] + a[47] * table6[45] + a[50] * table2[48] +
				a[51] * table6[49] + a[54] * table2[52] + a[55] * table6[53] + a[58] * table2[56] + a[59] * table6[57] +
				a[62] * table2[60] + a[63] * table6[61];
		d2f_da2[1] = a[8] * table2[0] + a[9] * table2[1] + a[10] * table2[2] + a[11] * table2[3] + a[12] * table6[4] +
				a[13] * table6[5] + a[14] * table6[6] + a[15] * table6[7] + a[24] * table2[16] + a[25] * table2[17] +
				a[26] * table2[18] + a[27] * table2[19] + a[28] * table6[20] + a[29] * table6[21] + a[30] * table6[22] +
				a[31] * table6[23] + a[40] * table2[32] + a[41] * table2[33] + a[42] * table2[34] + a[43] * table2[35] +
				a[44] * table6[36] + a[45] * table6[37] + a[46] * table6[38] + a[47] * table6[39] + a[56] * table2[48] +
				a[57] * table2[49] + a[58] * table2[50] + a[59] * table2[51] + a[60] * table6[52] + a[61] * table6[53] +
				a[62] * table6[54] + a[63] * table6[55];
		d2f_da2[2] = a[32] * table2[0] + a[33] * table2[1] + a[34] * table2[2] + a[35] * table2[3] + a[36] * table2[4] +
				a[37] * table2[5] + a[38] * table2[6] + a[39] * table2[7] + a[40] * table2[8] + a[41] * table2[9] +
				a[42] * table2[10] + a[43] * table2[11] + a[44] * table2[12] + a[45] * table2[13] + a[46] * table2[14] +
				a[47] * table2[15] + a[48] * table6[16] + a[49] * table6[17] + a[50] * table6[18] + a[51] * table6[19] +
				a[52] * table6[20] + a[53] * table6[21] + a[54] * table6[22] + a[55] * table6[23] + a[56] * table6[24] +
				a[57] * table6[25] + a[58] * table6[26] + a[59] * table6[27] + a[60] * table6[28] + a[61] * table6[29] +
				a[62] * table6[30] + a[63] * table6[31];
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
	 * @see gdsc.core.math.interpolation.CustomTricubicFunction#value(float[], float[], float[], float[], double[],
	 * double[])
	 */
	@Override
	public double value(float[] table, float[] table2, float[] table3, float[] table6, double[] df_da, double[] d2f_da2)
	{
		df_da[0] = a[1] * table[0] + a[2] * table2[1] + a[3] * table3[2] + a[5] * table[4] + a[6] * table2[5] +
				a[7] * table3[6] + a[9] * table[8] + a[10] * table2[9] + a[11] * table3[10] + a[13] * table[12] +
				a[14] * table2[13] + a[15] * table3[14] + a[17] * table[16] + a[18] * table2[17] + a[19] * table3[18] +
				a[21] * table[20] + a[22] * table2[21] + a[23] * table3[22] + a[25] * table[24] + a[26] * table2[25] +
				a[27] * table3[26] + a[29] * table[28] + a[30] * table2[29] + a[31] * table3[30] + a[33] * table[32] +
				a[34] * table2[33] + a[35] * table3[34] + a[37] * table[36] + a[38] * table2[37] + a[39] * table3[38] +
				a[41] * table[40] + a[42] * table2[41] + a[43] * table3[42] + a[45] * table[44] + a[46] * table2[45] +
				a[47] * table3[46] + a[49] * table[48] + a[50] * table2[49] + a[51] * table3[50] + a[53] * table[52] +
				a[54] * table2[53] + a[55] * table3[54] + a[57] * table[56] + a[58] * table2[57] + a[59] * table3[58] +
				a[61] * table[60] + a[62] * table2[61] + a[63] * table3[62];
		df_da[1] = a[4] * table[0] + a[5] * table[1] + a[6] * table[2] + a[7] * table[3] + a[8] * table2[4] +
				a[9] * table2[5] + a[10] * table2[6] + a[11] * table2[7] + a[12] * table3[8] + a[13] * table3[9] +
				a[14] * table3[10] + a[15] * table3[11] + a[20] * table[16] + a[21] * table[17] + a[22] * table[18] +
				a[23] * table[19] + a[24] * table2[20] + a[25] * table2[21] + a[26] * table2[22] + a[27] * table2[23] +
				a[28] * table3[24] + a[29] * table3[25] + a[30] * table3[26] + a[31] * table3[27] + a[36] * table[32] +
				a[37] * table[33] + a[38] * table[34] + a[39] * table[35] + a[40] * table2[36] + a[41] * table2[37] +
				a[42] * table2[38] + a[43] * table2[39] + a[44] * table3[40] + a[45] * table3[41] + a[46] * table3[42] +
				a[47] * table3[43] + a[52] * table[48] + a[53] * table[49] + a[54] * table[50] + a[55] * table[51] +
				a[56] * table2[52] + a[57] * table2[53] + a[58] * table2[54] + a[59] * table2[55] + a[60] * table3[56] +
				a[61] * table3[57] + a[62] * table3[58] + a[63] * table3[59];
		df_da[2] = a[16] * table[0] + a[17] * table[1] + a[18] * table[2] + a[19] * table[3] + a[20] * table[4] +
				a[21] * table[5] + a[22] * table[6] + a[23] * table[7] + a[24] * table[8] + a[25] * table[9] +
				a[26] * table[10] + a[27] * table[11] + a[28] * table[12] + a[29] * table[13] + a[30] * table[14] +
				a[31] * table[15] + a[32] * table2[16] + a[33] * table2[17] + a[34] * table2[18] + a[35] * table2[19] +
				a[36] * table2[20] + a[37] * table2[21] + a[38] * table2[22] + a[39] * table2[23] + a[40] * table2[24] +
				a[41] * table2[25] + a[42] * table2[26] + a[43] * table2[27] + a[44] * table2[28] + a[45] * table2[29] +
				a[46] * table2[30] + a[47] * table2[31] + a[48] * table3[32] + a[49] * table3[33] + a[50] * table3[34] +
				a[51] * table3[35] + a[52] * table3[36] + a[53] * table3[37] + a[54] * table3[38] + a[55] * table3[39] +
				a[56] * table3[40] + a[57] * table3[41] + a[58] * table3[42] + a[59] * table3[43] + a[60] * table3[44] +
				a[61] * table3[45] + a[62] * table3[46] + a[63] * table3[47];
		d2f_da2[0] = a[2] * table2[0] + a[3] * table6[1] + a[6] * table2[4] + a[7] * table6[5] + a[10] * table2[8] +
				a[11] * table6[9] + a[14] * table2[12] + a[15] * table6[13] + a[18] * table2[16] + a[19] * table6[17] +
				a[22] * table2[20] + a[23] * table6[21] + a[26] * table2[24] + a[27] * table6[25] + a[30] * table2[28] +
				a[31] * table6[29] + a[34] * table2[32] + a[35] * table6[33] + a[38] * table2[36] + a[39] * table6[37] +
				a[42] * table2[40] + a[43] * table6[41] + a[46] * table2[44] + a[47] * table6[45] + a[50] * table2[48] +
				a[51] * table6[49] + a[54] * table2[52] + a[55] * table6[53] + a[58] * table2[56] + a[59] * table6[57] +
				a[62] * table2[60] + a[63] * table6[61];
		d2f_da2[1] = a[8] * table2[0] + a[9] * table2[1] + a[10] * table2[2] + a[11] * table2[3] + a[12] * table6[4] +
				a[13] * table6[5] + a[14] * table6[6] + a[15] * table6[7] + a[24] * table2[16] + a[25] * table2[17] +
				a[26] * table2[18] + a[27] * table2[19] + a[28] * table6[20] + a[29] * table6[21] + a[30] * table6[22] +
				a[31] * table6[23] + a[40] * table2[32] + a[41] * table2[33] + a[42] * table2[34] + a[43] * table2[35] +
				a[44] * table6[36] + a[45] * table6[37] + a[46] * table6[38] + a[47] * table6[39] + a[56] * table2[48] +
				a[57] * table2[49] + a[58] * table2[50] + a[59] * table2[51] + a[60] * table6[52] + a[61] * table6[53] +
				a[62] * table6[54] + a[63] * table6[55];
		d2f_da2[2] = a[32] * table2[0] + a[33] * table2[1] + a[34] * table2[2] + a[35] * table2[3] + a[36] * table2[4] +
				a[37] * table2[5] + a[38] * table2[6] + a[39] * table2[7] + a[40] * table2[8] + a[41] * table2[9] +
				a[42] * table2[10] + a[43] * table2[11] + a[44] * table2[12] + a[45] * table2[13] + a[46] * table2[14] +
				a[47] * table2[15] + a[48] * table6[16] + a[49] * table6[17] + a[50] * table6[18] + a[51] * table6[19] +
				a[52] * table6[20] + a[53] * table6[21] + a[54] * table6[22] + a[55] * table6[23] + a[56] * table6[24] +
				a[57] * table6[25] + a[58] * table6[26] + a[59] * table6[27] + a[60] * table6[28] + a[61] * table6[29] +
				a[62] * table6[30] + a[63] * table6[31];
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
