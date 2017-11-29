package gdsc.core.data;

import org.junit.Assert;
import org.junit.Test;

public class IntegerTypeTest
{
	@Test
	public void canProvideIntegerTypeData()
	{
		for (IntegerType type : IntegerType.values())
		{
			int bd = type.getBitDepth();
			Assert.assertTrue(type.getName().contains(Integer.toString(bd) + "-bit"));
			Assert.assertEquals(type, IntegerType.forOrdinal(type.ordinal()));
			
			if (type.isSigned())
			{
				// Signed
				Assert.assertTrue(type.getName().contains("Signed"));
				Assert.assertEquals(type.getName(), minSigned(bd), type.getMin());
				Assert.assertEquals(type.getName(), maxSigned(bd), type.getMax());
				Assert.assertEquals(type.getName(), -minSigned(bd), type.getAbsoluteMax());
			}
			else
			{
				// Unsigned
				Assert.assertTrue(type.getName().contains("Unsigned"));
				Assert.assertEquals(type.getName(), 0l, type.getMin());
				Assert.assertEquals(type.getName(), maxUnsigned(bd), type.getMax());
				Assert.assertEquals(type.getName(), maxUnsigned(bd), type.getAbsoluteMax());
			}
		}
	}

	private long maxUnsigned(int bd)
	{
		long max = 1;
		while (bd-- > 0)
			max *= 2l;
		return max - 1;
	}

	private long maxSigned(int bd)
	{
		long max = 1;
		while (bd-- > 1)
			max *= 2l;
		return max - 1;
	}

	private long minSigned(int bd)
	{
		long max = 1;
		while (bd-- > 1)
			max *= 2l;
		return -max;
	}
}
