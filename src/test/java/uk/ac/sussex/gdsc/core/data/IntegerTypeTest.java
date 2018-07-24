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
package uk.ac.sussex.gdsc.core.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestLog;

@SuppressWarnings({ "javadoc" })
public class IntegerTypeTest
{
	@Test
	public void canGenerateIntegerType()
	{
		Assumptions.assumeTrue(false);
		final StringBuilder sb = new StringBuilder();
		for (int bitDepth = 1; bitDepth <= 64; bitDepth++)
			add(sb, true, bitDepth);
		for (int bitDepth = 1; bitDepth <= 63; bitDepth++)
			add(sb, false, bitDepth);
		TestLog.infoln(sb.toString());
	}

	private static void add(StringBuilder sb, boolean signed, int bitDepth)
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
		for (final IntegerType type : IntegerType.values())
		{
			final int bd = type.getBitDepth();
			Assertions.assertTrue(type.getName().contains(Integer.toString(bd) + "-bit"));
			Assertions.assertEquals(type, IntegerType.forOrdinal(type.ordinal()));

			if (type.isSigned())
			{
				// Signed
				Assertions.assertTrue(type.getName().contains("Signed"));
				Assertions.assertEquals(minSigned(bd), type.getMin(), type.getName());
				Assertions.assertEquals(maxSigned(bd), type.getMax(), type.getName());
				Assertions.assertEquals(-minSigned(bd), type.getAbsoluteMax(), type.getName());
			}
			else
			{
				// Unsigned
				Assertions.assertTrue(type.getName().contains("Unsigned"));
				Assertions.assertEquals(0l, type.getMin(), type.getName());
				Assertions.assertEquals(maxUnsigned(bd), type.getMax(), type.getName());
				Assertions.assertEquals(maxUnsigned(bd), type.getAbsoluteMax(), type.getName());
			}
		}
	}

	private static long maxUnsigned(int bd)
	{
		long max = 1;
		while (bd-- > 0)
			max *= 2l;
		return max - 1;
	}

	private static long maxSigned(int bd)
	{
		long max = 1;
		while (bd-- > 1)
			max *= 2l;
		return max - 1;
	}

	private static long minSigned(int bd)
	{
		long max = 1;
		while (bd-- > 1)
			max *= 2l;
		return -max;
	}
}
