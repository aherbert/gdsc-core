package gdsc.core.utils;

import java.math.BigDecimal;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

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
		RandomGenerator r = new Well19937c(30051977);
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
