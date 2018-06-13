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

import gdsc.core.test.TestSettings;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

/**
 * This class is used to in-line the computation for the CustomTricubicFunction
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

	static String append_pYpZ(StringBuilder sb, int k, int j)
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
					sb.append(String.format("table[%d] = pX[%d] * %s;\n", ai, i, table0jk));
				}
			}
		}

		return finaliseInlineFunction(sb);
	}

	static String appendTableijk(StringBuilder sb, int k, int j, int i, int ai)
	{
		String pYpZ;
		boolean compound = true;
		if (k == 0)
		{
			compound = false;
			if (j == 0)
			{
				pYpZ = "1";
			}
			else
			{
				pYpZ = String.format("pY[%d]", j);
			}
		}
		else if (j == 0)
		{
			compound = false;
			pYpZ = String.format("pZ[%d]", k);
		}
		else
		{
			pYpZ = String.format("pY[%d] * pZ[%d]", j, k);
		}

		String tableijk = String.format("table[%d]", ai);
		sb.append(String.format("%s = pX[%d] * %s;\n", tableijk, i, pYpZ));
		return (compound) ? tableijk : pYpZ;
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
						sb.append(String.format("df_da[0] += %d * a[%d] * %s;\n", i+1, getIndex(i+1, j, k), _pXpYpZ));
					if (j < N_1)
						sb.append(String.format("df_da[1] += %d * a[%d] * %s;\n", j+1, getIndex(i, j+1, k), _pXpYpZ));
					if (k < N_1)
						sb.append(String.format("df_da[2] += %d * a[%d] * %s;\n", k+1, getIndex(i, j, k+1), _pXpYpZ));
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

	static String append_pXpYpZ(StringBuilder sb, String _pYpZ, int i)
	{
		String _pXpYpZ;
		if (i == 0)
		{
			_pXpYpZ = _pYpZ;
		}
		else if (_pYpZ.equals("1"))
		{
			_pXpYpZ = String.format("pX[%d]", i);
		}
		else
		{
			sb.append(String.format("pXpYpZ = pX[%d] * %s;\n", i, _pYpZ));
			_pXpYpZ = "pXpYpZ";
		}
		return _pXpYpZ;
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
		String sum = String.format("%d * a[%d] * table[%d]\n", n, before, after);
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
		String sum = String.format("a[%d] * table%d[%d]\n", before, n, after);
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
						sb.append(String.format("df_da[0] += %d * a[%d] * %s;\n", i+1, getIndex(i+1, j, k), _pXpYpZ));
						if (i < N_2)
							sb.append(String.format("d2f_da2[0] += %d * d[%d] * %s;\n", (i+1)*(i+2), getIndex(i+2, j, k), _pXpYpZ));
					}
					if (j < N_1)
					{
						sb.append(String.format("df_da[1] += %d * a[%d] * %s;\n", j+1, getIndex(i, j+1, k), _pXpYpZ));
						if (j < N_2)
							sb.append(String.format("d2f_da2[1] += %d * a[%d] * %s;\n", (j+1)*(j+2), getIndex(i, j+2, k), _pXpYpZ));
					}						
					if (k < N_1)
					{
						sb.append(String.format("df_da[2] += %d * a[%d] * %s;\n", k+1, getIndex(i, j, k+1), _pXpYpZ));
						if (k < N_2)
							sb.append(String.format("d2f_da2[2] += %d * a[%d] * %s;\n", (k+1)*(k+2), getIndex(i, j, k+2), _pXpYpZ));
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

	@Test
	public void canConstructInlineValue()
	{
		Assume.assumeTrue(false);
		TestSettings.infoln(inlineValue());
	}

	@Test
	public void canConstructInlineComputePowerTable()
	{
		Assume.assumeTrue(false);
		TestSettings.infoln(inlineComputePowerTable());
	}

	@Test
	public void canConstructInlineValue1()
	{
		Assume.assumeTrue(true);
		TestSettings.infoln(inlineValue1());
	}

	@Test
	public void canConstructInlineValue1WithPowerTable()
	{
		Assume.assumeTrue(true);
		TestSettings.infoln(inlineValue1WithPowerTable());
	}

	@Test
	public void canConstructInlineValue1WithPowerTableN()
	{
		Assume.assumeTrue(true);
		TestSettings.infoln(inlineValue1WithPowerTableN());
	}

	@Test
	public void canConstructInlineValue2()
	{
		Assume.assumeTrue(true);
		TestSettings.infoln(inlineValue2());
	}

	@Test
	public void canConstructInlineValue2WithPowerTable()
	{
		Assume.assumeTrue(true);
		TestSettings.infoln(inlineValue2WithPowerTable());
	}

	@Test
	public void canConstructInlineValue2WithPowerTableN()
	{
		Assume.assumeTrue(true);
		TestSettings.infoln(inlineValue2WithPowerTableN());
	}
}
