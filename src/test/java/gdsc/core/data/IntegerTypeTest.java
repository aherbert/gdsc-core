package gdsc.core.data;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class IntegerTypeTest
{
	@Test
	public void canGenerateIntegerType()
	{
		Assume.assumeTrue(false);
		StringBuilder sb = new StringBuilder();
		for (int bitDepth = 1; bitDepth <= 64; bitDepth++)
			add(sb, true, bitDepth);
		for (int bitDepth = 1; bitDepth <= 63; bitDepth++)
			add(sb, false, bitDepth);
		System.out.println(sb.toString());
	}

	private void add(StringBuilder sb, boolean signed, int bitDepth)
	{
		if (signed)
		{
			sb.append("    /** A signed ").append(bitDepth).append("-bit integer */\n");
			sb.append("    SIGNED_").append(bitDepth).append(" {\n");
			sb.append("    @Override public String getName() { return \"Signed ").append(bitDepth)
					.append("-bit integer\"; }\n");
			sb.append("    @Override public long getMin() { return ").append(minSigned(bitDepth)).append("L; }\n");
			sb.append("    @Override public long getMax() { return ").append(maxSigned(bitDepth)).append("L; }\n");
			sb.append("    @Override public boolean isSigned() { return true; }\n");
		}
		else
		{
			sb.append("    /** An unsigned ").append(bitDepth).append("-bit integer */\n");
			sb.append("    UNSIGNED_").append(bitDepth).append(" {\n");
			sb.append("    @Override public String getName() { return \"Unsigned ").append(bitDepth)
					.append("-bit integer\"; }\n");
			sb.append("    @Override public long getMin() { return 0L; }\n");
			sb.append("    @Override public long getMax() { return ").append(maxUnsigned(bitDepth)).append("L; }\n");
			sb.append("    @Override public boolean isSigned() { return false; }\n");
		}
		sb.append("    @Override public int getBitDepth() { return ").append(bitDepth).append("; }\n");
		sb.append("    },\n");
	}

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
