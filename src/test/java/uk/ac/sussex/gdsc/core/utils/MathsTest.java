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
