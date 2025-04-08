/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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
 * Contains pre-computed reference data for integers.
 */
public enum IntegerType {
  /** A signed 1-bit integer. */
  SIGNED_1("Signed 1-bit integer", -1L, 0L, true, 1),
  /** A signed 2-bit integer. */
  SIGNED_2("Signed 2-bit integer", -2L, 1L, true, 2),
  /** A signed 3-bit integer. */
  SIGNED_3("Signed 3-bit integer", -4L, 3L, true, 3),
  /** A signed 4-bit integer. */
  SIGNED_4("Signed 4-bit integer", -8L, 7L, true, 4),
  /** A signed 5-bit integer. */
  SIGNED_5("Signed 5-bit integer", -16L, 15L, true, 5),
  /** A signed 6-bit integer. */
  SIGNED_6("Signed 6-bit integer", -32L, 31L, true, 6),
  /** A signed 7-bit integer. */
  SIGNED_7("Signed 7-bit integer", -64L, 63L, true, 7),
  /** A signed 8-bit integer. */
  SIGNED_8("Signed 8-bit integer", -128L, 127L, true, 8),
  /** A signed 9-bit integer. */
  SIGNED_9("Signed 9-bit integer", -256L, 255L, true, 9),
  /** A signed 10-bit integer. */
  SIGNED_10("Signed 10-bit integer", -512L, 511L, true, 10),
  /** A signed 11-bit integer. */
  SIGNED_11("Signed 11-bit integer", -1024L, 1023L, true, 11),
  /** A signed 12-bit integer. */
  SIGNED_12("Signed 12-bit integer", -2048L, 2047L, true, 12),
  /** A signed 13-bit integer. */
  SIGNED_13("Signed 13-bit integer", -4096L, 4095L, true, 13),
  /** A signed 14-bit integer. */
  SIGNED_14("Signed 14-bit integer", -8192L, 8191L, true, 14),
  /** A signed 15-bit integer. */
  SIGNED_15("Signed 15-bit integer", -16384L, 16383L, true, 15),
  /** A signed 16-bit integer. */
  SIGNED_16("Signed 16-bit integer", -32768L, 32767L, true, 16),
  /** A signed 17-bit integer. */
  SIGNED_17("Signed 17-bit integer", -65536L, 65535L, true, 17),
  /** A signed 18-bit integer. */
  SIGNED_18("Signed 18-bit integer", -131072L, 131071L, true, 18),
  /** A signed 19-bit integer. */
  SIGNED_19("Signed 19-bit integer", -262144L, 262143L, true, 19),
  /** A signed 20-bit integer. */
  SIGNED_20("Signed 20-bit integer", -524288L, 524287L, true, 20),
  /** A signed 21-bit integer. */
  SIGNED_21("Signed 21-bit integer", -1048576L, 1048575L, true, 21),
  /** A signed 22-bit integer. */
  SIGNED_22("Signed 22-bit integer", -2097152L, 2097151L, true, 22),
  /** A signed 23-bit integer. */
  SIGNED_23("Signed 23-bit integer", -4194304L, 4194303L, true, 23),
  /** A signed 24-bit integer. */
  SIGNED_24("Signed 24-bit integer", -8388608L, 8388607L, true, 24),
  /** A signed 25-bit integer. */
  SIGNED_25("Signed 25-bit integer", -16777216L, 16777215L, true, 25),
  /** A signed 26-bit integer. */
  SIGNED_26("Signed 26-bit integer", -33554432L, 33554431L, true, 26),
  /** A signed 27-bit integer. */
  SIGNED_27("Signed 27-bit integer", -67108864L, 67108863L, true, 27),
  /** A signed 28-bit integer. */
  SIGNED_28("Signed 28-bit integer", -134217728L, 134217727L, true, 28),
  /** A signed 29-bit integer. */
  SIGNED_29("Signed 29-bit integer", -268435456L, 268435455L, true, 29),
  /** A signed 30-bit integer. */
  SIGNED_30("Signed 30-bit integer", -536870912L, 536870911L, true, 30),
  /** A signed 31-bit integer. */
  SIGNED_31("Signed 31-bit integer", -1073741824L, 1073741823L, true, 31),
  /** A signed 32-bit integer. */
  SIGNED_32("Signed 32-bit integer", -2147483648L, 2147483647L, true, 32),
  /** A signed 33-bit integer. */
  SIGNED_33("Signed 33-bit integer", -4294967296L, 4294967295L, true, 33),
  /** A signed 34-bit integer. */
  SIGNED_34("Signed 34-bit integer", -8589934592L, 8589934591L, true, 34),
  /** A signed 35-bit integer. */
  SIGNED_35("Signed 35-bit integer", -17179869184L, 17179869183L, true, 35),
  /** A signed 36-bit integer. */
  SIGNED_36("Signed 36-bit integer", -34359738368L, 34359738367L, true, 36),
  /** A signed 37-bit integer. */
  SIGNED_37("Signed 37-bit integer", -68719476736L, 68719476735L, true, 37),
  /** A signed 38-bit integer. */
  SIGNED_38("Signed 38-bit integer", -137438953472L, 137438953471L, true, 38),
  /** A signed 39-bit integer. */
  SIGNED_39("Signed 39-bit integer", -274877906944L, 274877906943L, true, 39),
  /** A signed 40-bit integer. */
  SIGNED_40("Signed 40-bit integer", -549755813888L, 549755813887L, true, 40),
  /** A signed 41-bit integer. */
  SIGNED_41("Signed 41-bit integer", -1099511627776L, 1099511627775L, true, 41),
  /** A signed 42-bit integer. */
  SIGNED_42("Signed 42-bit integer", -2199023255552L, 2199023255551L, true, 42),
  /** A signed 43-bit integer. */
  SIGNED_43("Signed 43-bit integer", -4398046511104L, 4398046511103L, true, 43),
  /** A signed 44-bit integer. */
  SIGNED_44("Signed 44-bit integer", -8796093022208L, 8796093022207L, true, 44),
  /** A signed 45-bit integer. */
  SIGNED_45("Signed 45-bit integer", -17592186044416L, 17592186044415L, true, 45),
  /** A signed 46-bit integer. */
  SIGNED_46("Signed 46-bit integer", -35184372088832L, 35184372088831L, true, 46),
  /** A signed 47-bit integer. */
  SIGNED_47("Signed 47-bit integer", -70368744177664L, 70368744177663L, true, 47),
  /** A signed 48-bit integer. */
  SIGNED_48("Signed 48-bit integer", -140737488355328L, 140737488355327L, true, 48),
  /** A signed 49-bit integer. */
  SIGNED_49("Signed 49-bit integer", -281474976710656L, 281474976710655L, true, 49),
  /** A signed 50-bit integer. */
  SIGNED_50("Signed 50-bit integer", -562949953421312L, 562949953421311L, true, 50),
  /** A signed 51-bit integer. */
  SIGNED_51("Signed 51-bit integer", -1125899906842624L, 1125899906842623L, true, 51),
  /** A signed 52-bit integer. */
  SIGNED_52("Signed 52-bit integer", -2251799813685248L, 2251799813685247L, true, 52),
  /** A signed 53-bit integer. */
  SIGNED_53("Signed 53-bit integer", -4503599627370496L, 4503599627370495L, true, 53),
  /** A signed 54-bit integer. */
  SIGNED_54("Signed 54-bit integer", -9007199254740992L, 9007199254740991L, true, 54),
  /** A signed 55-bit integer. */
  SIGNED_55("Signed 55-bit integer", -18014398509481984L, 18014398509481983L, true, 55),
  /** A signed 56-bit integer. */
  SIGNED_56("Signed 56-bit integer", -36028797018963968L, 36028797018963967L, true, 56),
  /** A signed 57-bit integer. */
  SIGNED_57("Signed 57-bit integer", -72057594037927936L, 72057594037927935L, true, 57),
  /** A signed 58-bit integer. */
  SIGNED_58("Signed 58-bit integer", -144115188075855872L, 144115188075855871L, true, 58),
  /** A signed 59-bit integer. */
  SIGNED_59("Signed 59-bit integer", -288230376151711744L, 288230376151711743L, true, 59),
  /** A signed 60-bit integer. */
  SIGNED_60("Signed 60-bit integer", -576460752303423488L, 576460752303423487L, true, 60),
  /** A signed 61-bit integer. */
  SIGNED_61("Signed 61-bit integer", -1152921504606846976L, 1152921504606846975L, true, 61),
  /** A signed 62-bit integer. */
  SIGNED_62("Signed 62-bit integer", -2305843009213693952L, 2305843009213693951L, true, 62),
  /** A signed 63-bit integer. */
  SIGNED_63("Signed 63-bit integer", -4611686018427387904L, 4611686018427387903L, true, 63),
  /** A signed 64-bit integer. */
  SIGNED_64("Signed 64-bit integer", -9223372036854775808L, 9223372036854775807L, true, 64),
  /** An unsigned 1-bit integer. */
  UNSIGNED_1("Unsigned 1-bit integer", 0L, 1L, false, 1),
  /** An unsigned 2-bit integer. */
  UNSIGNED_2("Unsigned 2-bit integer", 0L, 3L, false, 2),
  /** An unsigned 3-bit integer. */
  UNSIGNED_3("Unsigned 3-bit integer", 0L, 7L, false, 3),
  /** An unsigned 4-bit integer. */
  UNSIGNED_4("Unsigned 4-bit integer", 0L, 15L, false, 4),
  /** An unsigned 5-bit integer. */
  UNSIGNED_5("Unsigned 5-bit integer", 0L, 31L, false, 5),
  /** An unsigned 6-bit integer. */
  UNSIGNED_6("Unsigned 6-bit integer", 0L, 63L, false, 6),
  /** An unsigned 7-bit integer. */
  UNSIGNED_7("Unsigned 7-bit integer", 0L, 127L, false, 7),
  /** An unsigned 8-bit integer. */
  UNSIGNED_8("Unsigned 8-bit integer", 0L, 255L, false, 8),
  /** An unsigned 9-bit integer. */
  UNSIGNED_9("Unsigned 9-bit integer", 0L, 511L, false, 9),
  /** An unsigned 10-bit integer. */
  UNSIGNED_10("Unsigned 10-bit integer", 0L, 1023L, false, 10),
  /** An unsigned 11-bit integer. */
  UNSIGNED_11("Unsigned 11-bit integer", 0L, 2047L, false, 11),
  /** An unsigned 12-bit integer. */
  UNSIGNED_12("Unsigned 12-bit integer", 0L, 4095L, false, 12),
  /** An unsigned 13-bit integer. */
  UNSIGNED_13("Unsigned 13-bit integer", 0L, 8191L, false, 13),
  /** An unsigned 14-bit integer. */
  UNSIGNED_14("Unsigned 14-bit integer", 0L, 16383L, false, 14),
  /** An unsigned 15-bit integer. */
  UNSIGNED_15("Unsigned 15-bit integer", 0L, 32767L, false, 15),
  /** An unsigned 16-bit integer. */
  UNSIGNED_16("Unsigned 16-bit integer", 0L, 65535L, false, 16),
  /** An unsigned 17-bit integer. */
  UNSIGNED_17("Unsigned 17-bit integer", 0L, 131071L, false, 17),
  /** An unsigned 18-bit integer. */
  UNSIGNED_18("Unsigned 18-bit integer", 0L, 262143L, false, 18),
  /** An unsigned 19-bit integer. */
  UNSIGNED_19("Unsigned 19-bit integer", 0L, 524287L, false, 19),
  /** An unsigned 20-bit integer. */
  UNSIGNED_20("Unsigned 20-bit integer", 0L, 1048575L, false, 20),
  /** An unsigned 21-bit integer. */
  UNSIGNED_21("Unsigned 21-bit integer", 0L, 2097151L, false, 21),
  /** An unsigned 22-bit integer. */
  UNSIGNED_22("Unsigned 22-bit integer", 0L, 4194303L, false, 22),
  /** An unsigned 23-bit integer. */
  UNSIGNED_23("Unsigned 23-bit integer", 0L, 8388607L, false, 23),
  /** An unsigned 24-bit integer. */
  UNSIGNED_24("Unsigned 24-bit integer", 0L, 16777215L, false, 24),
  /** An unsigned 25-bit integer. */
  UNSIGNED_25("Unsigned 25-bit integer", 0L, 33554431L, false, 25),
  /** An unsigned 26-bit integer. */
  UNSIGNED_26("Unsigned 26-bit integer", 0L, 67108863L, false, 26),
  /** An unsigned 27-bit integer. */
  UNSIGNED_27("Unsigned 27-bit integer", 0L, 134217727L, false, 27),
  /** An unsigned 28-bit integer. */
  UNSIGNED_28("Unsigned 28-bit integer", 0L, 268435455L, false, 28),
  /** An unsigned 29-bit integer. */
  UNSIGNED_29("Unsigned 29-bit integer", 0L, 536870911L, false, 29),
  /** An unsigned 30-bit integer. */
  UNSIGNED_30("Unsigned 30-bit integer", 0L, 1073741823L, false, 30),
  /** An unsigned 31-bit integer. */
  UNSIGNED_31("Unsigned 31-bit integer", 0L, 2147483647L, false, 31),
  /** An unsigned 32-bit integer. */
  UNSIGNED_32("Unsigned 32-bit integer", 0L, 4294967295L, false, 32),
  /** An unsigned 33-bit integer. */
  UNSIGNED_33("Unsigned 33-bit integer", 0L, 8589934591L, false, 33),
  /** An unsigned 34-bit integer. */
  UNSIGNED_34("Unsigned 34-bit integer", 0L, 17179869183L, false, 34),
  /** An unsigned 35-bit integer. */
  UNSIGNED_35("Unsigned 35-bit integer", 0L, 34359738367L, false, 35),
  /** An unsigned 36-bit integer. */
  UNSIGNED_36("Unsigned 36-bit integer", 0L, 68719476735L, false, 36),
  /** An unsigned 37-bit integer. */
  UNSIGNED_37("Unsigned 37-bit integer", 0L, 137438953471L, false, 37),
  /** An unsigned 38-bit integer. */
  UNSIGNED_38("Unsigned 38-bit integer", 0L, 274877906943L, false, 38),
  /** An unsigned 39-bit integer. */
  UNSIGNED_39("Unsigned 39-bit integer", 0L, 549755813887L, false, 39),
  /** An unsigned 40-bit integer. */
  UNSIGNED_40("Unsigned 40-bit integer", 0L, 1099511627775L, false, 40),
  /** An unsigned 41-bit integer. */
  UNSIGNED_41("Unsigned 41-bit integer", 0L, 2199023255551L, false, 41),
  /** An unsigned 42-bit integer. */
  UNSIGNED_42("Unsigned 42-bit integer", 0L, 4398046511103L, false, 42),
  /** An unsigned 43-bit integer. */
  UNSIGNED_43("Unsigned 43-bit integer", 0L, 8796093022207L, false, 43),
  /** An unsigned 44-bit integer. */
  UNSIGNED_44("Unsigned 44-bit integer", 0L, 17592186044415L, false, 44),
  /** An unsigned 45-bit integer. */
  UNSIGNED_45("Unsigned 45-bit integer", 0L, 35184372088831L, false, 45),
  /** An unsigned 46-bit integer. */
  UNSIGNED_46("Unsigned 46-bit integer", 0L, 70368744177663L, false, 46),
  /** An unsigned 47-bit integer. */
  UNSIGNED_47("Unsigned 47-bit integer", 0L, 140737488355327L, false, 47),
  /** An unsigned 48-bit integer. */
  UNSIGNED_48("Unsigned 48-bit integer", 0L, 281474976710655L, false, 48),
  /** An unsigned 49-bit integer. */
  UNSIGNED_49("Unsigned 49-bit integer", 0L, 562949953421311L, false, 49),
  /** An unsigned 50-bit integer. */
  UNSIGNED_50("Unsigned 50-bit integer", 0L, 1125899906842623L, false, 50),
  /** An unsigned 51-bit integer. */
  UNSIGNED_51("Unsigned 51-bit integer", 0L, 2251799813685247L, false, 51),
  /** An unsigned 52-bit integer. */
  UNSIGNED_52("Unsigned 52-bit integer", 0L, 4503599627370495L, false, 52),
  /** An unsigned 53-bit integer. */
  UNSIGNED_53("Unsigned 53-bit integer", 0L, 9007199254740991L, false, 53),
  /** An unsigned 54-bit integer. */
  UNSIGNED_54("Unsigned 54-bit integer", 0L, 18014398509481983L, false, 54),
  /** An unsigned 55-bit integer. */
  UNSIGNED_55("Unsigned 55-bit integer", 0L, 36028797018963967L, false, 55),
  /** An unsigned 56-bit integer. */
  UNSIGNED_56("Unsigned 56-bit integer", 0L, 72057594037927935L, false, 56),
  /** An unsigned 57-bit integer. */
  UNSIGNED_57("Unsigned 57-bit integer", 0L, 144115188075855871L, false, 57),
  /** An unsigned 58-bit integer. */
  UNSIGNED_58("Unsigned 58-bit integer", 0L, 288230376151711743L, false, 58),
  /** An unsigned 59-bit integer. */
  UNSIGNED_59("Unsigned 59-bit integer", 0L, 576460752303423487L, false, 59),
  /** An unsigned 60-bit integer. */
  UNSIGNED_60("Unsigned 60-bit integer", 0L, 1152921504606846975L, false, 60),
  /** An unsigned 61-bit integer. */
  UNSIGNED_61("Unsigned 61-bit integer", 0L, 2305843009213693951L, false, 61),
  /** An unsigned 62-bit integer. */
  UNSIGNED_62("Unsigned 62-bit integer", 0L, 4611686018427387903L, false, 62),
  /** An unsigned 63-bit integer. */
  UNSIGNED_63("Unsigned 63-bit integer", 0L, 9223372036854775807L, false, 63);

  /** The VALUES. */
  private static final IntegerType[] VALUES = IntegerType.values();

  /** The type name. */
  final String typeName;

  /** The min. */
  final long min;

  /** The max. */
  final long max;

  /** The signed flag. */
  final boolean signed;

  /** The bit depth. */
  final int bitDepth;

  IntegerType(String typeName, long min, long max, boolean signed, int bitDepth) {
    this.typeName = typeName;
    this.min = min;
    this.max = max;
    this.signed = signed;
    this.bitDepth = bitDepth;
  }

  /**
   * Gets the type name.
   *
   * @return the type name
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Gets the min value.
   *
   * <p>For an unsigned integer this will be zero.
   *
   * <p>A signed integer can return a larger magnitude for its min value than for its max (as a
   * single bit is used to hold the sign). Beware that using the absolute of the min value is not
   * valid when this is a signed 64-bit integer as {@link Math#abs(long)} returns a negative.
   *
   * @return the min value
   */
  public long getMin() {
    return min;
  }

  /**
   * Gets the max value.
   *
   * @return the max value
   */
  public long getMax() {
    return max;
  }

  /**
   * Checks if is signed.
   *
   * @return true, if is signed
   */
  public boolean isSigned() {
    return signed;
  }

  /**
   * Gets the bit depth.
   *
   * @return the bit depth
   */
  public int getBitDepth() {
    return bitDepth;
  }

  @Override
  public String toString() {
    return getTypeName();
  }

  /**
   * Gets the value for the ordinal.
   *
   * @param ordinal the ordinal
   * @return the integer type
   * @throws IllegalArgumentException If the ordinal is invalid
   */
  public static IntegerType forOrdinal(int ordinal) {
    if (ordinal < 0) {
      throw new IllegalArgumentException("Negative ordinal");
    }
    if (ordinal >= VALUES.length) {
      throw new IllegalArgumentException("Ordinal too high");
    }
    return VALUES[ordinal];
  }

  /**
   * Gets the value for the ordinal, or a default. If the given default is null then the value with
   * ordinal 0 is returned.
   *
   * @param ordinal the ordinal
   * @param defaultValue the default value (if the ordinal is invalid)
   * @return the integer type
   */
  public static IntegerType forOrdinal(int ordinal, IntegerType defaultValue) {
    if (ordinal < 0 || ordinal >= VALUES.length) {
      return (defaultValue == null) ? VALUES[0] : defaultValue;
    }
    return VALUES[ordinal];
  }

  /**
   * Get the max value of an unsigned integer of the given bit depth.
   *
   * @param bitDepth the bit depth (range 1-63)
   * @return the max value
   * @throws IllegalArgumentException If the bit-depth is invalid
   */
  public static long maxUnsigned(int bitDepth) {
    if (bitDepth < 1 || bitDepth > 63) {
      throw new IllegalArgumentException(invalidBitDepthMessage(bitDepth));
    }
    return (1L << bitDepth) - 1;
  }

  /**
   * Get the max value of a signed integer of the given bit depth.
   *
   * @param bitDepth the bit depth (range 1-64)
   * @return the max value
   * @throws IllegalArgumentException If the bit-depth is invalid
   */
  public static long maxSigned(int bitDepth) {
    if (bitDepth < 1 || bitDepth > 64) {
      throw new IllegalArgumentException(invalidBitDepthMessage(bitDepth));
    }
    return (1L << (bitDepth - 1)) - 1;
  }

  /**
   * Get the min value of a signed integer of the given bit depth.
   *
   * @param bitDepth the bit depth (range 1-64)
   * @return the min value
   * @throws IllegalArgumentException If the bit-depth is invalid
   */
  public static long minSigned(int bitDepth) {
    if (bitDepth < 1 || bitDepth > 64) {
      throw new IllegalArgumentException(invalidBitDepthMessage(bitDepth));
    }
    return -(1L << (bitDepth - 1));
  }

  private static String invalidBitDepthMessage(int bitDepth) {
    return "Invalid bit depth: " + bitDepth;
  }
}
