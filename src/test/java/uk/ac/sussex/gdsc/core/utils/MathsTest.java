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
package uk.ac.sussex.gdsc.core.utils;

import java.math.BigDecimal;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

@SuppressWarnings({ "javadoc" })
public class MathsTest
{
	@SeededTest
	public void canRoundToDecimalPlaces(RandomSeed seed)
	{
		// 0.1 cannot be an exact double (see constructor documentation for BigDecimal)
		double d = 0.1;
		BigDecimal bd = new BigDecimal(d);
		Assertions.assertNotEquals("0.1", bd.toPlainString());
		Assertions.assertEquals("0.1", Maths.roundUsingDecimalPlacesToBigDecimal(d, 1).toPlainString());

		// Random test that rounding does the same as String.format
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		for (int i = 0; i < 10; i++)
		{
			final String format = "%." + i + "f";
			for (int j = 0; j < 10; j++)
			{
				d = r.nextDouble();
				final String e = String.format(format, d);
				bd = Maths.roundUsingDecimalPlacesToBigDecimal(d, i);
				Assertions.assertEquals(e, bd.toPlainString());
			}
		}
	}

	@Test
	public void canRoundToNegativeDecimalPlaces()
	{
		Assertions.assertEquals("123", Maths.roundUsingDecimalPlacesToBigDecimal(123, 1).toPlainString());
		Assertions.assertEquals("123", Maths.roundUsingDecimalPlacesToBigDecimal(123, 0).toPlainString());
		Assertions.assertEquals("120", Maths.roundUsingDecimalPlacesToBigDecimal(123, -1).toPlainString());
		Assertions.assertEquals("100", Maths.roundUsingDecimalPlacesToBigDecimal(123, -2).toPlainString());
		Assertions.assertEquals("0", Maths.roundUsingDecimalPlacesToBigDecimal(123, -3).toPlainString());
		Assertions.assertEquals("0", Maths.roundUsingDecimalPlacesToBigDecimal(123, -4).toPlainString());
		Assertions.assertEquals("1000", Maths.roundUsingDecimalPlacesToBigDecimal(523, -3).toPlainString());
	}
}
