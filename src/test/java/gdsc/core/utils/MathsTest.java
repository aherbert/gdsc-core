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
package gdsc.core.utils;

import java.math.BigDecimal;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;

@SuppressWarnings({"javadoc"})
public class MathsTest
{
	@Test
	public void canRoundToDecimalPlaces()
	{
		// 0.1 cannot be an exact double (see constructor documentation for BigDecimal)
		double d = 0.1;
		BigDecimal bd = new BigDecimal(d);
		Assert.assertNotEquals("0.1", bd.toPlainString());
		Assert.assertEquals("0.1", Maths.roundUsingDecimalPlacesToBigDecimal(d, 1).toPlainString());

		// Random test that rounding does the same as String.format
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int i = 0; i < 10; i++)
		{
			String format = "%." + i + "f";
			for (int j = 0; j < 10; j++)
			{
				d = r.nextDouble();
				String e = String.format(format, d);
				bd = Maths.roundUsingDecimalPlacesToBigDecimal(d, i);
				Assert.assertEquals(e, bd.toPlainString());
			}
		}
	}

	@Test
	public void canRoundToNegativeDecimalPlaces()
	{
		Assert.assertEquals("123", Maths.roundUsingDecimalPlacesToBigDecimal(123, 1).toPlainString());
		Assert.assertEquals("123", Maths.roundUsingDecimalPlacesToBigDecimal(123, 0).toPlainString());
		Assert.assertEquals("120", Maths.roundUsingDecimalPlacesToBigDecimal(123, -1).toPlainString());
		Assert.assertEquals("100", Maths.roundUsingDecimalPlacesToBigDecimal(123, -2).toPlainString());
		Assert.assertEquals("0", Maths.roundUsingDecimalPlacesToBigDecimal(123, -3).toPlainString());
		Assert.assertEquals("0", Maths.roundUsingDecimalPlacesToBigDecimal(123, -4).toPlainString());
		Assert.assertEquals("1000", Maths.roundUsingDecimalPlacesToBigDecimal(523, -3).toPlainString());
	}
}
