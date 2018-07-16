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
package gdsc.core.data;

import gdsc.core.utils.Maths;

/**
 * Contains the 20 SI prefixes used to form decimal multiples and submultiples of SI units in powers of 10.
 */
public enum SIPrefix
{
	//@formatter:off
    /** Yotta */
    YOTTA {
    @Override public double getFactor() { return 1e24; }
    @Override public String getName() { return "yotta"; }
    @Override public String getSymbol() { return "Y"; }
    },
    /** Zetta */
    ZETTA {
    @Override public double getFactor() { return 1e21; }
    @Override public String getName() { return "zetta"; }
    @Override public String getSymbol() { return "Z"; }
    },
    /** Exa */
    EXA {
    @Override public double getFactor() { return 1e18; }
    @Override public String getName() { return "exa"; }
    @Override public String getSymbol() { return "E"; }
    },
    /** Peta */
    PETA {
    @Override public double getFactor() { return 1e15; }
    @Override public String getName() { return "peta"; }
    @Override public String getSymbol() { return "P"; }
    },
    /** Tera */
    TERA {
    @Override public double getFactor() { return 1e12; }
    @Override public String getName() { return "tera"; }
    @Override public String getSymbol() { return "T"; }
    },
    /** Giga */
    GIGA {
    @Override public double getFactor() { return 1e9; }
    @Override public String getName() { return "giga"; }
    @Override public String getSymbol() { return "G"; }
    },
    /** Mega */
    MEGA {
    @Override public double getFactor() { return 1e6; }
    @Override public String getName() { return "mega"; }
    @Override public String getSymbol() { return "M"; }
    },
    /** Kilo */
    KILO {
    @Override public double getFactor() { return 1e3; }
    @Override public String getName() { return "kilo"; }
    @Override public String getSymbol() { return "k"; }
    },
    /** Hecto */
    HECTO {
    @Override public double getFactor() { return 1e2; }
    @Override public String getName() { return "hecto"; }
    @Override public String getSymbol() { return "h"; }
    },
    /** Deka */
    DEKA {
    @Override public double getFactor() { return 1e1; }
    @Override public String getName() { return "deka"; }
    @Override public String getSymbol() { return "da"; }
    },
    /** None */
    NONE {
    @Override public double getFactor() { return 1e0; }
    @Override public String getName() { return ""; }
    @Override public String getSymbol() { return ""; }
    },
    /** Deci */
    DECI {
    @Override public double getFactor() { return 1e-1; }
    @Override public String getName() { return "deci"; }
    @Override public String getSymbol() { return "d"; }
    },
    /** Centi */
    CENTI {
    @Override public double getFactor() { return 1e-2; }
    @Override public String getName() { return "centi"; }
    @Override public String getSymbol() { return "c"; }
    },
    /** Milli */
    MILLI {
    @Override public double getFactor() { return 1e-3; }
    @Override public String getName() { return "milli"; }
    @Override public String getSymbol() { return "m"; }
    },
    /** Micro */
    MICRO {
    @Override public double getFactor() { return 1e-6; }
    @Override public String getName() { return "micro"; }
    @Override public String getSymbol() { return "µ"; }
    },
    /** Nano */
    NANO {
    @Override public double getFactor() { return 1e-9; }
    @Override public String getName() { return "nano"; }
    @Override public String getSymbol() { return "n"; }
    },
    /** Pico */
    PICO {
    @Override public double getFactor() { return 1e-12; }
    @Override public String getName() { return "pico"; }
    @Override public String getSymbol() { return "p"; }
    },
    /** Femto */
    FEMTO {
    @Override public double getFactor() { return 1e-15; }
    @Override public String getName() { return "femto"; }
    @Override public String getSymbol() { return "f"; }
    },
    /** Atto */
    ATTO {
    @Override public double getFactor() { return 1e-18; }
    @Override public String getName() { return "atto"; }
    @Override public String getSymbol() { return "a"; }
    },
    /** Zepto */
    ZEPTO {
    @Override public double getFactor() { return 1e-21; }
    @Override public String getName() { return "zepto"; }
    @Override public String getSymbol() { return "z"; }
    },
    /** Yocto */
    YOCTO {
    @Override public double getFactor() { return 1e-24; }
    @Override public String getName() { return "yocto"; }
    @Override public String getSymbol() { return "y"; }
    },
    ;
	//@formatter:on

	/**
	 * Gets the factor.
	 *
	 * @return the factor
	 */
	public abstract double getFactor();

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * Gets the symbol.
	 *
	 * @return the symbol
	 */
	public abstract String getSymbol();

	/**
	 * Convert the value.
	 *
	 * @param value
	 *            the value
	 * @return the converted value
	 */
	public double convert(double value)
	{
		return value / getFactor();
	}

	/**
	 * Convert the value from the given prefix.
	 *
	 * @param value
	 *            the value
	 * @param prefix
	 *            the prefix
	 * @return the converted value
	 */
	public double convert(double value, SIPrefix prefix)
	{
		return value * (prefix.getFactor() / getFactor());
	}

	/**
	 * Gets the value for the ordinal.
	 *
	 * @param ordinal
	 *            the ordinal
	 * @return the integer type
	 * @throws IllegalArgumentException
	 *             If the ordinal is invalid
	 */
	public static SIPrefix forOrdinal(int ordinal) throws IllegalArgumentException
	{
		if (ordinal < 0)
			throw new IllegalArgumentException("Negative ordinal");
		final SIPrefix[] values = SIPrefix.values();
		if (ordinal >= values.length)
			throw new IllegalArgumentException("Ordinal too high");
		return values[ordinal];
	}

	/**
	 * Gets the value for the ordinal, or a default. If the given default is null then the value with ordinal 0 is
	 * returned.
	 *
	 * @param ordinal
	 *            the ordinal
	 * @param defaultValue
	 *            the default value (if the ordinal is invalid)
	 * @return the integer type
	 */
	public static SIPrefix forOrdinal(int ordinal, SIPrefix defaultValue)
	{
		final SIPrefix[] values = SIPrefix.values();
		if (ordinal < 0 || ordinal >= values.length)
			return (defaultValue == null) ? values[0] : defaultValue;
		return values[ordinal];
	}

	/**
	 * Gets the appropriate prefix for the value so that the significant digits before the decimal point are minimised
	 * but at least one digit is before the decimal point.
	 * <p>
	 * The string representation for value of [unit] for example can be:
	 *
	 * <pre>
	 * double value = ...;
	 * SIPrefix p = SIPrefix.getPrefix(value);
	 * String text = String.format("%s %s[unit]", p.convert(value), p.getName());
	 * </pre>
	 *
	 * @param value
	 *            the value
	 * @return the prefix
	 */
	public static SIPrefix getPrefix(double value)
	{
		// Edge case
		if (value == 0 || !Maths.isFinite(value))
			return SIPrefix.NONE;

		value = Math.abs(value);
		final SIPrefix[] p = SIPrefix.values();
		final int p_1 = p.length - 1;
		int i = 0;
		while (i < p_1 && value < p[i].getFactor())
			i++;
		return p[i];
	}
}
