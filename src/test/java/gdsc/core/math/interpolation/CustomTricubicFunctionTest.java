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

import org.junit.Assume;
import org.junit.Test;

import gdsc.test.TestSettings;
import gdsc.test.TestSettings.LogLevel;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

/**
 * This class is used to in-line the computation for the CustomTricubicFunction.
 * <p>
 * The ordering of the computation is set to multiply by the power ZYX and the cubic coefficient last.
 * <p>
 * This allows the power table to be precomputed and the result should match the
 * non-precomputed version. This includes scaling the power table by 2,3,6 for
 * computation of the gradients.
 */
public class CustomTricubicFunctionTest
{
	/** Number of points. */
	private static final short N = 4;
	/** Number of points - 1. */
	private static final short N_1 = 3;
	/** Number of points - 2. */
	private static final short N_2 = 2;

	static int getIndex(int i, int j, int k)
	{
		return CustomTricubicFunction.getIndex(i, j, k);
	}

	/**
	 * Used to create the inline value function
	 * 
	 * @return the function text.
	 */
	static String inlineValue()
	{
		String _pZpY;
		StringBuilder sb = new StringBuilder();

		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pZpY = append_pZpY(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					sb.append(String.format("result += %s * pX[%d] * a[%d];\n", _pZpY, i, ai));
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	static String append_pZpY(StringBuilder sb, int k, int j)
	{
		String _pZpY;
		if (k == 0)
		{
			if (j == 0)
			{
				_pZpY = "1";
			}
			else
			{
				_pZpY = String.format("pY[%d]", j);
			}
		}
		else if (j == 0)
		{
			_pZpY = String.format("pZ[%d]", k);
		}
		else
		{
			sb.append(String.format("pZpY = pZ[%d] * pY[%d];\n", k, j));
			_pZpY = "pZpY";
		}
		return _pZpY;
	}

	static String finaliseInlineFunction(StringBuilder sb)
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

		return result;
	}

	/**
	 * Used to create the inline value function for first-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValueWithPowerTable()
	{
		TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(64);

		StringBuilder sb = new StringBuilder();

		sb.append("return ");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					appendPower(map, sb, i, j, k, i, j, k);
		sb.append(";\n");

		// Each entry should be unique indicating that the result is optimal 
		map.forEachEntry(new TObjectIntProcedure<String>()
		{
			@Override
			public boolean execute(String a, int b)
			{
				if (b > 1)
				{
					TestSettings.info("%s = %d\n", a, b);
					return false;
				}
				return true;
			}
		});

		return finaliseInlinePowerTableFunction(sb);
	}

	/**
	 * Used to create the inline power table function
	 * 
	 * @return the function text.
	 */
	static String inlineComputePowerTable()
	{
		String table0jk;
		StringBuilder sb = new StringBuilder();

		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				table0jk = appendTableijk(sb, k, j, 0, ai++);

				for (int i = 1; i < N; i++, ai++)
				{
					sb.append(String.format("table[%d] = %s * pX[%d];\n", ai, table0jk, i));
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	static String appendTableijk(StringBuilder sb, int k, int j, int i, int ai)
	{
		String pZpY;
		boolean compound = true;
		if (k == 0)
		{
			compound = false;
			if (j == 0)
			{
				pZpY = "1";
			}
			else
			{
				pZpY = String.format("pY[%d]", j);
			}
		}
		else if (j == 0)
		{
			compound = false;
			pZpY = String.format("pZ[%d]", k);
		}
		else
		{
			pZpY = String.format("pZ[%d] * pY[%d]", k, j);
		}

		String tableijk = String.format("table[%d]", ai);
		sb.append(String.format("%s = %s * pX[%d];\n", tableijk, pZpY, i));
		return (compound) ? tableijk : pZpY;
	}

	/**
	 * Used to create the inline value function for first-order gradients
	 * 
	 * @return the function text.
	 */
	static String inlineValue1()
	{
		String _pZpY;
		String _pZpYpX;
		StringBuilder sb = new StringBuilder();

		// Gradients are described in:
		// Babcock & Zhuang (2017) 
		// Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
		// Scientific Reports 7, Article number: 552
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pZpY = append_pZpY(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					_pZpYpX = append_pZpYpX(sb, _pZpY, i);

					//@formatter:off
					sb.append(String.format("result += %s * a[%d];\n", _pZpYpX, ai));
					if (i < N_1)
						sb.append(String.format("df_da[0] += %d * %s * a[%d];\n", i+1, _pZpYpX, getIndex(i+1, j, k)));
					if (j < N_1)
						sb.append(String.format("df_da[1] += %d * %s * a[%d];\n", j+1, _pZpYpX, getIndex(i, j+1, k)));
					if (k < N_1)
						sb.append(String.format("df_da[2] += %d * %s * a[%d];\n", k+1, _pZpYpX, getIndex(i, j, k+1)));
					//@formatter:on

					// Formal computation
					//pZpYpX = pZ[k] * pY[j] * pX[i];
					//result += pZpYpX * a[ai];
					//if (i < N_1)
					//	df_da[0] += (i+1) * pZpYpX * a[getIndex(i+1, j, k)];
					//if (j < N_1)
					//	df_da[1] += (j+1) * pZpYpX * a[getIndex(i, j+1, k)];
					//if (k < N_1)
					//	df_da[2] += (k+1) * pZpYpX * a[getIndex(i, j, k+1)];
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	static String append_pZpYpX(StringBuilder sb, String _pZpY, int i)
	{
		String _pZpYpX;
		if (i == 0)
		{
			_pZpYpX = _pZpY;
		}
		else if (_pZpY.equals("1"))
		{
			_pZpYpX = String.format("pX[%d]", i);
		}
		else
		{
			sb.append(String.format("pZpYpX = %s * pX[%d];\n", _pZpY, i));
			_pZpYpX = "pZpYpX";
		}
		return _pZpYpX;
	}

	/**
	 * Used to create the inline value function for first-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValue1WithPowerTable()
	{
		TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(64);

		StringBuilder sb = new StringBuilder();
		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		sb.append("df_da[0] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_1)
						appendPower(map, sb, i + 1, j, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[1] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_1)
						appendPower(map, sb, i, j + 1, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[2] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_1)
						appendPower(map, sb, i, j, k + 1, i, j, k);
		sb.append(";\n");
		sb.append("return ");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					appendPower(map, sb, i, j, k, i, j, k);
		sb.append(";\n");

		// Each entry should be unique indicating that the result is optimal 
		map.forEachEntry(new TObjectIntProcedure<String>()
		{
			@Override
			public boolean execute(String a, int b)
			{
				if (b > 1)
				{
					TestSettings.info("%s = %d\n", a, b);
					return false;
				}
				return true;
			}
		});

		return finaliseInlinePowerTableFunction(sb);
	}

	static void appendPower(TObjectIntHashMap<String> map, StringBuilder sb, int i1, int j1, int k1, int i2, int j2,
			int k2)
	{
		int after = getIndex(i2, j2, k2);
		int before = getIndex(i1, j1, k1);
		int nh, nl;
		if (i1 != i2)
		{
			nh = i1;
			nl = i2;
		}
		else if (j1 != j2)
		{
			nh = j1;
			nl = j2;
		}
		else
		{
			nh = k1;
			nl = k2;
		}
		int n = 1;
		while (nh > nl)
		{
			n *= nh;
			nh--;
		}
		String sum = String.format("%d * table[%d] * a[%d]\n", n, after, before);
		map.adjustOrPutValue(sum, 1, 1);
		sb.append("+ ").append(sum);
	}

	/**
	 * Used to create the inline value function for first-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValue1WithPowerTableN()
	{
		TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(64);

		StringBuilder sb = new StringBuilder();
		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		sb.append("df_da[0] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_1)
						appendPowerN(map, sb, i + 1, j, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[1] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_1)
						appendPowerN(map, sb, i, j + 1, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[2] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_1)
						appendPowerN(map, sb, i, j, k + 1, i, j, k);
		sb.append(";\n");
		sb.append("return ");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					appendPowerN(map, sb, i, j, k, i, j, k);
		sb.append(";\n");

		// Each entry should be unique indicating that the result is optimal 
		map.forEachEntry(new TObjectIntProcedure<String>()
		{
			@Override
			public boolean execute(String a, int b)
			{
				if (b > 1)
				{
					TestSettings.info("%s = %d\n", a, b);
					return false;
				}
				return true;
			}
		});

		return finaliseInlinePowerTableFunction(sb);
	}

	static void appendPowerN(TObjectIntHashMap<String> map, StringBuilder sb, int i1, int j1, int k1, int i2, int j2,
			int k2)
	{
		int after = getIndex(i2, j2, k2);
		int before = getIndex(i1, j1, k1);
		int nh, nl;
		if (i1 != i2)
		{
			nh = i1;
			nl = i2;
		}
		else if (j1 != j2)
		{
			nh = j1;
			nl = j2;
		}
		else
		{
			nh = k1;
			nl = k2;
		}
		int n = 1;
		while (nh > nl)
		{
			n *= nh;
			nh--;
		}
		String sum = String.format("table%d[%d] * a[%d]\n", n, after, before);
		map.adjustOrPutValue(sum, 1, 1);
		sb.append("+ ").append(sum);
	}

	static String finaliseInlinePowerTableFunction(StringBuilder sb)
	{
		String result = sb.toString();
		result = result.replace("return +", "return ");
		result = result.replace("=+", "=");
		result = result.replace("1 * ", "");
		result = result.replace("table1", "table");
		return result;
	}

	/**
	 * Used to create the inline value function for second-order gradients
	 * 
	 * @return the function text.
	 */
	static String inlineValue2()
	{
		String _pZpY;
		String _pZpYpX;
		StringBuilder sb = new StringBuilder();

		// Gradients are described in:
		// Babcock & Zhuang (2017) 
		// Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
		// Scientific Reports 7, Article number: 552
		for (int k = 0, ai = 0; k < N; k++)
		{
			for (int j = 0; j < N; j++)
			{
				_pZpY = append_pZpY(sb, k, j);

				for (int i = 0; i < N; i++, ai++)
				{
					_pZpYpX = append_pZpYpX(sb, _pZpY, i);

					//@formatter:off
					sb.append(String.format("result += %s * a[%d];\n", _pZpYpX, ai));
					if (i < N_1)
					{
						sb.append(String.format("df_da[0] += %d * %s * a[%d];\n", i+1, _pZpYpX, getIndex(i+1, j, k)));
						if (i < N_2)
							sb.append(String.format("d2f_da2[0] += %d * %s * a[%d];\n", (i+1)*(i+2), _pZpYpX, getIndex(i+2, j, k)));
					}
					if (j < N_1)
					{
						sb.append(String.format("df_da[1] += %d * %s * a[%d];\n", j+1, _pZpYpX, getIndex(i, j+1, k)));
						if (j < N_2)
							sb.append(String.format("d2f_da2[1] += %d * %s * a[%d];\n", (j+1)*(j+2), _pZpYpX, getIndex(i, j+2, k)));
					}						
					if (k < N_1)
					{
						sb.append(String.format("df_da[2] += %d * %s * a[%d];\n", k+1, _pZpYpX, getIndex(i, j, k+1)));
						if (k < N_2)
							sb.append(String.format("d2f_da2[2] += %d * %s * a[%d];\n", (k+1)*(k+2), _pZpYpX, getIndex(i, j, k+2)));
					}
					//@formatter:on

					//// Formal computation
					//pZpYpX = pZpY * pX[i];
					//result += pZpYpX * a[ai];
					//if (i < N_1)
					//{
					//	df_da[0] += (i+1) * pZpYpX * a[getIndex(i+1, j, k)];
					//	if (i < N_2)
					//		d2f_da2[0] += (i+1) * (i + 2) * pZpYpX * a[getIndex(i + 2, j, k)];
					//}
					//if (j < N_1)
					//{
					//	df_da[1] += (j+1) * pZpYpX * a[getIndex(i, j+1, k)];
					//	if (j < N_2)
					//		d2f_da2[1] += (j+1) * (j + 2) * pZpYpX * a[getIndex(i, j + 2, k)];
					//}
					//if (k < N_1)
					//{
					//	df_da[2] += (k+1) * pZpYpX * a[getIndex(i, j, k+1)];
					//	if (k < N_2)
					//		d2f_da2[2] += (k+1) * (k + 2) * pZpYpX * a[getIndex(i, j, k + 2)];
					//}
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	/**
	 * Used to create the inline value function for second-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValue2WithPowerTable()
	{
		TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(64);
		StringBuilder sb = new StringBuilder();
		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		sb.append("df_da[0] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_1)
						appendPower(map, sb, i + 1, j, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[1] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_1)
						appendPower(map, sb, i, j + 1, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[2] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_1)
						appendPower(map, sb, i, j, k + 1, i, j, k);
		sb.append(";\n");
		sb.append("d2f_da2[0] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_2)
						appendPower(map, sb, i + 2, j, k, i, j, k);
		sb.append(";\n");
		sb.append("d2f_da2[1] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_2)
						appendPower(map, sb, i, j + 2, k, i, j, k);
		sb.append(";\n");
		sb.append("d2f_da2[2] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_2)
						appendPower(map, sb, i, j, k + 2, i, j, k);
		sb.append(";\n");
		sb.append("return ");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					appendPower(map, sb, i, j, k, i, j, k);
		sb.append(";\n");

		// Each entry should be unique indicating that the result is optimal 
		map.forEachEntry(new TObjectIntProcedure<String>()
		{
			@Override
			public boolean execute(String a, int b)
			{
				if (b > 1)
				{
					TestSettings.info("%s = %d\n", a, b);
					return false;
				}
				return true;
			}
		});

		return finaliseInlinePowerTableFunction(sb);
	}

	/**
	 * Used to create the inline value function for second-order gradients with power table
	 * 
	 * @return the function text.
	 */
	static String inlineValue2WithPowerTableN()
	{
		TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(64);
		StringBuilder sb = new StringBuilder();
		// Inline each gradient array in order.
		// Maybe it will help the optimiser?
		sb.append("df_da[0] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_1)
						appendPowerN(map, sb, i + 1, j, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[1] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_1)
						appendPowerN(map, sb, i, j + 1, k, i, j, k);
		sb.append(";\n");
		sb.append("df_da[2] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_1)
						appendPowerN(map, sb, i, j, k + 1, i, j, k);
		sb.append(";\n");
		sb.append("d2f_da2[0] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (i < N_2)
						appendPowerN(map, sb, i + 2, j, k, i, j, k);
		sb.append(";\n");
		sb.append("d2f_da2[1] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (j < N_2)
						appendPowerN(map, sb, i, j + 2, k, i, j, k);
		sb.append(";\n");
		sb.append("d2f_da2[2] =");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					if (k < N_2)
						appendPowerN(map, sb, i, j, k + 2, i, j, k);
		sb.append(";\n");
		sb.append("return ");
		for (int k = 0; k < N; k++)
			for (int j = 0; j < N; j++)
				for (int i = 0; i < N; i++)
					appendPowerN(map, sb, i, j, k, i, j, k);
		sb.append(";\n");

		// Each entry should be unique indicating that the result is optimal 
		map.forEachEntry(new TObjectIntProcedure<String>()
		{
			@Override
			public boolean execute(String a, int b)
			{
				if (b > 1)
				{
					TestSettings.info("%s = %d\n", a, b);
					return false;
				}
				return true;
			}
		});

		return finaliseInlinePowerTableFunction(sb);
	}

	private LogLevel level = LogLevel.INFO;

	@Test
	public void canConstructInlineValue()
	{
		// DoubleCustomTricubicFunction#value0(double[], double[], double[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue());
	}

	@Test
	public void canConstructInlineValueWithPowerTable()
	{
		// DoubleCustomTricubicFunction#value(double[])
		// DoubleCustomTricubicFunction#value(float[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValueWithPowerTable());
	}

	@Test
	public void canConstructInlineComputePowerTable()
	{
		// CustomTricubicFunction.computePowerTable		
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineComputePowerTable());
	}

	@Test
	public void canConstructInlineValue1()
	{
		// DoubleCustomTricubicFunction#value1(double[], double[], double[], double[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue1());
	}

	@Test
	public void canConstructInlineValue1WithPowerTable()
	{
		// DoubleCustomTricubicFunction#value(double[], double[])
		// DoubleCustomTricubicFunction#gradient(double[], double[])
		// DoubleCustomTricubicFunction#value(float[], double[])
		// DoubleCustomTricubicFunction#gradient(float[], double[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue1WithPowerTable());
	}

	@Test
	public void canConstructInlineValue1WithPowerTableN()
	{
		// DoubleCustomTricubicFunction#value(double[], double[], double[], double[])
		// DoubleCustomTricubicFunction#value(float[], float[], float[], double[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue1WithPowerTableN());
	}

	@Test
	public void canConstructInlineValue2()
	{
		// DoubleCustomTricubicFunction#value2(double[], double[], double[], double[], double[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue2());
	}

	@Test
	public void canConstructInlineValue2WithPowerTable()
	{
		// DoubleCustomTricubicFunction#value(double[], double[], double[])
		// DoubleCustomTricubicFunction#value(float[], double[], double[])
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue2WithPowerTable());
	}

	@Test
	public void canConstructInlineValue2WithPowerTableN()
	{
		// DoubleCustomTricubicFunction#value(double[], double[], double[], double[], double[],
		// DoubleCustomTricubicFunction#value(float[], float[], float[], float[], double[],
		Assume.assumeTrue(TestSettings.allow(level));
		TestSettings.log(level, inlineValue2WithPowerTableN());
	}
}
