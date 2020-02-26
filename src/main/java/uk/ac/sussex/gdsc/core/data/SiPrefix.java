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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

/**
 * Contains the 20 SI prefixes used to form decimal multiples and submultiples of SI units in powers
 * of 10.
 */
public enum SiPrefix {
  /** Yotta. */
  YOTTA(1e24, "yotta", "Y"),
  /** Zetta. */
  ZETTA(1e21, "zetta", "Z"),
  /** Exa. */
  EXA(1e18, "exa", "E"),
  /** Peta. */
  PETA(1e15, "peta", "P"),
  /** Tera. */
  TERA(1e12, "tera", "T"),
  /** Giga. */
  GIGA(1e9, "giga", "G"),
  /** Mega. */
  MEGA(1e6, "mega", "M"),
  /** Kilo. */
  KILO(1e3, "kilo", "k"),
  /** Hecto. */
  HECTO(1e2, "hecto", "h"),
  /** Deka. */
  DEKA(1e1, "deka", "da"),
  /** None. */
  NONE(1e0, "", ""),
  /** Deci. */
  DECI(1e-1, "deci", "d"),
  /** Centi. */
  CENTI(1e-2, "centi", "c"),
  /** Milli. */
  MILLI(1e-3, "milli", "m"),
  /** Micro. */
  MICRO(1e-6, "micro", "µ"),
  /** Nano. */
  NANO(1e-9, "nano", "n"),
  /** Pico. */
  PICO(1e-12, "pico", "p"),
  /** Femto. */
  FEMTO(1e-15, "femto", "f"),
  /** Atto. */
  ATTO(1e-18, "atto", "a"),
  /** Zepto. */
  ZEPTO(1e-21, "zepto", "z"),
  /** Yocto. */
  YOCTO(1e-24, "yocto", "y");

  /** The values. */
  private static final SiPrefix[] values = SiPrefix.values();

  /** The factor. */
  final double factor;

  /** The prefix. */
  final String prefix;

  /** The symbol. */
  final String symbol;

  /**
   * Instantiates a new SI prefix.
   *
   * @param factor the factor
   * @param prefix the prefix
   * @param symbol the symbol
   */
  SiPrefix(double factor, String prefix, String symbol) {
    this.factor = factor;
    this.prefix = prefix;
    this.symbol = symbol;
  }


  /**
   * Gets the factor.
   *
   * @return the factor
   */
  public double getFactor() {
    return factor;
  }

  /**
   * Gets the prefix.
   *
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Gets the symbol.
   *
   * @return the symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Convert the value.
   *
   * @param value the value
   * @return the converted value
   */
  public double convert(double value) {
    return value / getFactor();
  }

  /**
   * Convert the value from the given prefix.
   *
   * @param value the value
   * @param prefix the prefix
   * @return the converted value
   */
  public double convert(double value, SiPrefix prefix) {
    return value * (prefix.getFactor() / getFactor());
  }

  /**
   * Gets the value for the ordinal.
   *
   * @param ordinal the ordinal
   * @return the integer type
   * @throws IllegalArgumentException If the ordinal is invalid
   */
  public static SiPrefix forOrdinal(int ordinal) {
    if (ordinal < 0) {
      throw new IllegalArgumentException("Negative ordinal");
    }
    if (ordinal >= values.length) {
      throw new IllegalArgumentException("Ordinal too high");
    }
    return values[ordinal];
  }

  /**
   * Gets the value for the ordinal, or a default. If the given default is null then the value with
   * ordinal 0 is returned.
   *
   * @param ordinal the ordinal
   * @param defaultValue the default value (if the ordinal is invalid)
   * @return the integer type
   */
  public static SiPrefix forOrdinal(int ordinal, SiPrefix defaultValue) {
    if (ordinal < 0 || ordinal >= values.length) {
      return (defaultValue == null) ? values[0] : defaultValue;
    }
    return values[ordinal];
  }

  /**
   * Gets the appropriate prefix for the value so that the significant digits before the decimal
   * point are minimised but at least one digit is before the decimal point.
   *
   * <p>The string representation for value of [unit] for example can be:
   *
   * <pre>
   * double value = ...;
   * SiPrefix p = SiPrefix.getSiPrefix(value);
   * String text = String.format("%s %s[unit]", p.convert(value), p.getPrefix());
   * </pre>
   *
   * @param value the value
   * @return the prefix
   */
  public static SiPrefix getSiPrefix(double value) {
    // Edge case
    if (value == 0 || !Double.isFinite(value)) {
      return SiPrefix.NONE;
    }

    final double absValue = Math.abs(value);
    final int upperLimit = values.length - 1;
    int index = 0;
    while (index < upperLimit && absValue < values[index].getFactor()) {
      index++;
    }
    return values[index];
  }
}
