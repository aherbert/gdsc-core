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
public class SIPrefixTest
{
	@Test
	public void canGenerateSIPrefix()
	{
		// This is not a test. It generates the Enum.
		Assumptions.assumeTrue(false);

		//@formatter:off
		final String[] data = {
            "24","yotta","Y",
            "21","zetta","Z",
            "18","exa","E",
            "15","peta","P",
            "12","tera","T",
            "9","giga","G",
            "6","mega","M",
            "3","kilo","k",
            "2","hecto","h",
            "1","deka","da",
            "0","","",
            "-1","deci","d",
            "-2","centi","c",
            "-3","milli","m",
            "-6","micro","µ",
            "-9","nano","n",
            "-12","pico","p",
            "-15","femto","f",
            "-18","atto","a",
            "-21","zepto","z",
            "-24","yocto","y"
		};
		//@formatter:on
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i += 3)
			add(sb, data[i], data[i + 1], data[i + 2]);
		System.out.println(sb.toString());
	}

	private static void add(StringBuilder sb, String pow, String name, String symbol)
	{
		final String name2 = (name.length() == 0) ? "none" : name;
		sb.append("    /** ").append(name2.substring(0, 1).toUpperCase()).append(name2.substring(1)).append(" */\n");
		sb.append("    ").append(name2.toUpperCase()).append(" {\n");
		sb.append("    @Override public double getFactor() { return 1e").append(pow).append("; }\n");
		sb.append("    @Override public String getName() { return \"").append(name).append("\"; }\n");
		sb.append("    @Override public String getSymbol() { return \"").append(symbol).append("\"; }\n");
		sb.append("    },\n");
	}

	@Test
	public void canGetPrefix()
	{
		// Edge cases
		canGetPrefix(0, SIPrefix.NONE);
		canGetPrefix(Double.POSITIVE_INFINITY, SIPrefix.NONE);
		canGetPrefix(Double.NEGATIVE_INFINITY, SIPrefix.NONE);
		canGetPrefix(Double.NaN, SIPrefix.NONE);

		for (final int sign : new int[] { -1, 1 })
		{
			// Edge case high
			canGetPrefix(sign, SIPrefix.YOTTA.getFactor() * 10, SIPrefix.YOTTA);

			// Above 1
			final SIPrefix[] values = SIPrefix.values();
			for (int i = 0; i < values.length; i++)
				if (values[i].getFactor() > 1)
				{
					if (i > 0)
						canGetPrefix(sign, (values[i].getFactor() + values[i - 1].getFactor()) / 2, values[i]);
					canGetPrefix(sign, values[i].getFactor(), values[i]);
					if (i + 1 < values.length)
						canGetPrefix(sign, (values[i].getFactor() + values[i + 1].getFactor()) / 2, values[i + 1]);
				}

			// 1
			canGetPrefix(sign, 0.5, SIPrefix.DECI);
			canGetPrefix(sign, 1, SIPrefix.NONE);
			canGetPrefix(sign, 2, SIPrefix.NONE);

			// Below 1
			for (int i = 0; i < values.length; i++)
				if (values[i].getFactor() < 1)
				{
					if (i > 0)
						canGetPrefix(sign, (values[i].getFactor() + values[i - 1].getFactor()) / 2, values[i]);
					canGetPrefix(sign, values[i].getFactor(), values[i]);
					if (i + 1 < values.length)
						canGetPrefix(sign, (values[i].getFactor() + values[i + 1].getFactor()) / 2, values[i + 1]);
				}

			// Edge case low
			canGetPrefix(sign, SIPrefix.YOCTO.getFactor() / 10, SIPrefix.YOCTO);
		}
	}

	private static void canGetPrefix(double value, SIPrefix e)
	{
		canGetPrefix(1, value, e);
	}

	private static void canGetPrefix(int sign, double value, SIPrefix e)
	{
		value *= sign;
		final SIPrefix o = SIPrefix.getPrefix(value);
		TestLog.info("Value %s = %s %s (%s)\n", value, o.convert(value), o.getName(), o.getSymbol());
		Assertions.assertEquals(e, o);
	}
}
