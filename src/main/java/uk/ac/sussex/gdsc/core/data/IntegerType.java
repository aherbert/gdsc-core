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

/**
 * Contains pre-computed reference data for integers.
 */
public enum IntegerType {
  //@formatter:off
    /** A signed 1-bit integer */
    SIGNED_1 {
    @Override public String getName() { return "Signed 1-bit integer"; }
    @Override public long getMin() { return -1L; }
    @Override public long getMax() { return 0L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 1; }
    },
    /** A signed 2-bit integer */
    SIGNED_2 {
    @Override public String getName() { return "Signed 2-bit integer"; }
    @Override public long getMin() { return -2L; }
    @Override public long getMax() { return 1L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 2; }
    },
    /** A signed 3-bit integer */
    SIGNED_3 {
    @Override public String getName() { return "Signed 3-bit integer"; }
    @Override public long getMin() { return -4L; }
    @Override public long getMax() { return 3L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 3; }
    },
    /** A signed 4-bit integer */
    SIGNED_4 {
    @Override public String getName() { return "Signed 4-bit integer"; }
    @Override public long getMin() { return -8L; }
    @Override public long getMax() { return 7L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 4; }
    },
    /** A signed 5-bit integer */
    SIGNED_5 {
    @Override public String getName() { return "Signed 5-bit integer"; }
    @Override public long getMin() { return -16L; }
    @Override public long getMax() { return 15L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 5; }
    },
    /** A signed 6-bit integer */
    SIGNED_6 {
    @Override public String getName() { return "Signed 6-bit integer"; }
    @Override public long getMin() { return -32L; }
    @Override public long getMax() { return 31L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 6; }
    },
    /** A signed 7-bit integer */
    SIGNED_7 {
    @Override public String getName() { return "Signed 7-bit integer"; }
    @Override public long getMin() { return -64L; }
    @Override public long getMax() { return 63L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 7; }
    },
    /** A signed 8-bit integer */
    SIGNED_8 {
    @Override public String getName() { return "Signed 8-bit integer"; }
    @Override public long getMin() { return -128L; }
    @Override public long getMax() { return 127L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 8; }
    },
    /** A signed 9-bit integer */
    SIGNED_9 {
    @Override public String getName() { return "Signed 9-bit integer"; }
    @Override public long getMin() { return -256L; }
    @Override public long getMax() { return 255L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 9; }
    },
    /** A signed 10-bit integer */
    SIGNED_10 {
    @Override public String getName() { return "Signed 10-bit integer"; }
    @Override public long getMin() { return -512L; }
    @Override public long getMax() { return 511L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 10; }
    },
    /** A signed 11-bit integer */
    SIGNED_11 {
    @Override public String getName() { return "Signed 11-bit integer"; }
    @Override public long getMin() { return -1024L; }
    @Override public long getMax() { return 1023L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 11; }
    },
    /** A signed 12-bit integer */
    SIGNED_12 {
    @Override public String getName() { return "Signed 12-bit integer"; }
    @Override public long getMin() { return -2048L; }
    @Override public long getMax() { return 2047L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 12; }
    },
    /** A signed 13-bit integer */
    SIGNED_13 {
    @Override public String getName() { return "Signed 13-bit integer"; }
    @Override public long getMin() { return -4096L; }
    @Override public long getMax() { return 4095L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 13; }
    },
    /** A signed 14-bit integer */
    SIGNED_14 {
    @Override public String getName() { return "Signed 14-bit integer"; }
    @Override public long getMin() { return -8192L; }
    @Override public long getMax() { return 8191L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 14; }
    },
    /** A signed 15-bit integer */
    SIGNED_15 {
    @Override public String getName() { return "Signed 15-bit integer"; }
    @Override public long getMin() { return -16384L; }
    @Override public long getMax() { return 16383L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 15; }
    },
    /** A signed 16-bit integer */
    SIGNED_16 {
    @Override public String getName() { return "Signed 16-bit integer"; }
    @Override public long getMin() { return -32768L; }
    @Override public long getMax() { return 32767L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 16; }
    },
    /** A signed 17-bit integer */
    SIGNED_17 {
    @Override public String getName() { return "Signed 17-bit integer"; }
    @Override public long getMin() { return -65536L; }
    @Override public long getMax() { return 65535L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 17; }
    },
    /** A signed 18-bit integer */
    SIGNED_18 {
    @Override public String getName() { return "Signed 18-bit integer"; }
    @Override public long getMin() { return -131072L; }
    @Override public long getMax() { return 131071L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 18; }
    },
    /** A signed 19-bit integer */
    SIGNED_19 {
    @Override public String getName() { return "Signed 19-bit integer"; }
    @Override public long getMin() { return -262144L; }
    @Override public long getMax() { return 262143L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 19; }
    },
    /** A signed 20-bit integer */
    SIGNED_20 {
    @Override public String getName() { return "Signed 20-bit integer"; }
    @Override public long getMin() { return -524288L; }
    @Override public long getMax() { return 524287L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 20; }
    },
    /** A signed 21-bit integer */
    SIGNED_21 {
    @Override public String getName() { return "Signed 21-bit integer"; }
    @Override public long getMin() { return -1048576L; }
    @Override public long getMax() { return 1048575L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 21; }
    },
    /** A signed 22-bit integer */
    SIGNED_22 {
    @Override public String getName() { return "Signed 22-bit integer"; }
    @Override public long getMin() { return -2097152L; }
    @Override public long getMax() { return 2097151L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 22; }
    },
    /** A signed 23-bit integer */
    SIGNED_23 {
    @Override public String getName() { return "Signed 23-bit integer"; }
    @Override public long getMin() { return -4194304L; }
    @Override public long getMax() { return 4194303L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 23; }
    },
    /** A signed 24-bit integer */
    SIGNED_24 {
    @Override public String getName() { return "Signed 24-bit integer"; }
    @Override public long getMin() { return -8388608L; }
    @Override public long getMax() { return 8388607L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 24; }
    },
    /** A signed 25-bit integer */
    SIGNED_25 {
    @Override public String getName() { return "Signed 25-bit integer"; }
    @Override public long getMin() { return -16777216L; }
    @Override public long getMax() { return 16777215L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 25; }
    },
    /** A signed 26-bit integer */
    SIGNED_26 {
    @Override public String getName() { return "Signed 26-bit integer"; }
    @Override public long getMin() { return -33554432L; }
    @Override public long getMax() { return 33554431L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 26; }
    },
    /** A signed 27-bit integer */
    SIGNED_27 {
    @Override public String getName() { return "Signed 27-bit integer"; }
    @Override public long getMin() { return -67108864L; }
    @Override public long getMax() { return 67108863L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 27; }
    },
    /** A signed 28-bit integer */
    SIGNED_28 {
    @Override public String getName() { return "Signed 28-bit integer"; }
    @Override public long getMin() { return -134217728L; }
    @Override public long getMax() { return 134217727L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 28; }
    },
    /** A signed 29-bit integer */
    SIGNED_29 {
    @Override public String getName() { return "Signed 29-bit integer"; }
    @Override public long getMin() { return -268435456L; }
    @Override public long getMax() { return 268435455L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 29; }
    },
    /** A signed 30-bit integer */
    SIGNED_30 {
    @Override public String getName() { return "Signed 30-bit integer"; }
    @Override public long getMin() { return -536870912L; }
    @Override public long getMax() { return 536870911L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 30; }
    },
    /** A signed 31-bit integer */
    SIGNED_31 {
    @Override public String getName() { return "Signed 31-bit integer"; }
    @Override public long getMin() { return -1073741824L; }
    @Override public long getMax() { return 1073741823L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 31; }
    },
    /** A signed 32-bit integer */
    SIGNED_32 {
    @Override public String getName() { return "Signed 32-bit integer"; }
    @Override public long getMin() { return -2147483648L; }
    @Override public long getMax() { return 2147483647L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 32; }
    },
    /** A signed 33-bit integer */
    SIGNED_33 {
    @Override public String getName() { return "Signed 33-bit integer"; }
    @Override public long getMin() { return -4294967296L; }
    @Override public long getMax() { return 4294967295L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 33; }
    },
    /** A signed 34-bit integer */
    SIGNED_34 {
    @Override public String getName() { return "Signed 34-bit integer"; }
    @Override public long getMin() { return -8589934592L; }
    @Override public long getMax() { return 8589934591L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 34; }
    },
    /** A signed 35-bit integer */
    SIGNED_35 {
    @Override public String getName() { return "Signed 35-bit integer"; }
    @Override public long getMin() { return -17179869184L; }
    @Override public long getMax() { return 17179869183L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 35; }
    },
    /** A signed 36-bit integer */
    SIGNED_36 {
    @Override public String getName() { return "Signed 36-bit integer"; }
    @Override public long getMin() { return -34359738368L; }
    @Override public long getMax() { return 34359738367L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 36; }
    },
    /** A signed 37-bit integer */
    SIGNED_37 {
    @Override public String getName() { return "Signed 37-bit integer"; }
    @Override public long getMin() { return -68719476736L; }
    @Override public long getMax() { return 68719476735L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 37; }
    },
    /** A signed 38-bit integer */
    SIGNED_38 {
    @Override public String getName() { return "Signed 38-bit integer"; }
    @Override public long getMin() { return -137438953472L; }
    @Override public long getMax() { return 137438953471L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 38; }
    },
    /** A signed 39-bit integer */
    SIGNED_39 {
    @Override public String getName() { return "Signed 39-bit integer"; }
    @Override public long getMin() { return -274877906944L; }
    @Override public long getMax() { return 274877906943L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 39; }
    },
    /** A signed 40-bit integer */
    SIGNED_40 {
    @Override public String getName() { return "Signed 40-bit integer"; }
    @Override public long getMin() { return -549755813888L; }
    @Override public long getMax() { return 549755813887L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 40; }
    },
    /** A signed 41-bit integer */
    SIGNED_41 {
    @Override public String getName() { return "Signed 41-bit integer"; }
    @Override public long getMin() { return -1099511627776L; }
    @Override public long getMax() { return 1099511627775L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 41; }
    },
    /** A signed 42-bit integer */
    SIGNED_42 {
    @Override public String getName() { return "Signed 42-bit integer"; }
    @Override public long getMin() { return -2199023255552L; }
    @Override public long getMax() { return 2199023255551L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 42; }
    },
    /** A signed 43-bit integer */
    SIGNED_43 {
    @Override public String getName() { return "Signed 43-bit integer"; }
    @Override public long getMin() { return -4398046511104L; }
    @Override public long getMax() { return 4398046511103L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 43; }
    },
    /** A signed 44-bit integer */
    SIGNED_44 {
    @Override public String getName() { return "Signed 44-bit integer"; }
    @Override public long getMin() { return -8796093022208L; }
    @Override public long getMax() { return 8796093022207L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 44; }
    },
    /** A signed 45-bit integer */
    SIGNED_45 {
    @Override public String getName() { return "Signed 45-bit integer"; }
    @Override public long getMin() { return -17592186044416L; }
    @Override public long getMax() { return 17592186044415L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 45; }
    },
    /** A signed 46-bit integer */
    SIGNED_46 {
    @Override public String getName() { return "Signed 46-bit integer"; }
    @Override public long getMin() { return -35184372088832L; }
    @Override public long getMax() { return 35184372088831L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 46; }
    },
    /** A signed 47-bit integer */
    SIGNED_47 {
    @Override public String getName() { return "Signed 47-bit integer"; }
    @Override public long getMin() { return -70368744177664L; }
    @Override public long getMax() { return 70368744177663L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 47; }
    },
    /** A signed 48-bit integer */
    SIGNED_48 {
    @Override public String getName() { return "Signed 48-bit integer"; }
    @Override public long getMin() { return -140737488355328L; }
    @Override public long getMax() { return 140737488355327L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 48; }
    },
    /** A signed 49-bit integer */
    SIGNED_49 {
    @Override public String getName() { return "Signed 49-bit integer"; }
    @Override public long getMin() { return -281474976710656L; }
    @Override public long getMax() { return 281474976710655L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 49; }
    },
    /** A signed 50-bit integer */
    SIGNED_50 {
    @Override public String getName() { return "Signed 50-bit integer"; }
    @Override public long getMin() { return -562949953421312L; }
    @Override public long getMax() { return 562949953421311L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 50; }
    },
    /** A signed 51-bit integer */
    SIGNED_51 {
    @Override public String getName() { return "Signed 51-bit integer"; }
    @Override public long getMin() { return -1125899906842624L; }
    @Override public long getMax() { return 1125899906842623L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 51; }
    },
    /** A signed 52-bit integer */
    SIGNED_52 {
    @Override public String getName() { return "Signed 52-bit integer"; }
    @Override public long getMin() { return -2251799813685248L; }
    @Override public long getMax() { return 2251799813685247L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 52; }
    },
    /** A signed 53-bit integer */
    SIGNED_53 {
    @Override public String getName() { return "Signed 53-bit integer"; }
    @Override public long getMin() { return -4503599627370496L; }
    @Override public long getMax() { return 4503599627370495L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 53; }
    },
    /** A signed 54-bit integer */
    SIGNED_54 {
    @Override public String getName() { return "Signed 54-bit integer"; }
    @Override public long getMin() { return -9007199254740992L; }
    @Override public long getMax() { return 9007199254740991L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 54; }
    },
    /** A signed 55-bit integer */
    SIGNED_55 {
    @Override public String getName() { return "Signed 55-bit integer"; }
    @Override public long getMin() { return -18014398509481984L; }
    @Override public long getMax() { return 18014398509481983L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 55; }
    },
    /** A signed 56-bit integer */
    SIGNED_56 {
    @Override public String getName() { return "Signed 56-bit integer"; }
    @Override public long getMin() { return -36028797018963968L; }
    @Override public long getMax() { return 36028797018963967L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 56; }
    },
    /** A signed 57-bit integer */
    SIGNED_57 {
    @Override public String getName() { return "Signed 57-bit integer"; }
    @Override public long getMin() { return -72057594037927936L; }
    @Override public long getMax() { return 72057594037927935L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 57; }
    },
    /** A signed 58-bit integer */
    SIGNED_58 {
    @Override public String getName() { return "Signed 58-bit integer"; }
    @Override public long getMin() { return -144115188075855872L; }
    @Override public long getMax() { return 144115188075855871L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 58; }
    },
    /** A signed 59-bit integer */
    SIGNED_59 {
    @Override public String getName() { return "Signed 59-bit integer"; }
    @Override public long getMin() { return -288230376151711744L; }
    @Override public long getMax() { return 288230376151711743L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 59; }
    },
    /** A signed 60-bit integer */
    SIGNED_60 {
    @Override public String getName() { return "Signed 60-bit integer"; }
    @Override public long getMin() { return -576460752303423488L; }
    @Override public long getMax() { return 576460752303423487L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 60; }
    },
    /** A signed 61-bit integer */
    SIGNED_61 {
    @Override public String getName() { return "Signed 61-bit integer"; }
    @Override public long getMin() { return -1152921504606846976L; }
    @Override public long getMax() { return 1152921504606846975L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 61; }
    },
    /** A signed 62-bit integer */
    SIGNED_62 {
    @Override public String getName() { return "Signed 62-bit integer"; }
    @Override public long getMin() { return -2305843009213693952L; }
    @Override public long getMax() { return 2305843009213693951L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 62; }
    },
    /** A signed 63-bit integer */
    SIGNED_63 {
    @Override public String getName() { return "Signed 63-bit integer"; }
    @Override public long getMin() { return -4611686018427387904L; }
    @Override public long getMax() { return 4611686018427387903L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 63; }
    },
    /** A signed 64-bit integer */
    SIGNED_64 {
    @Override public String getName() { return "Signed 64-bit integer"; }
    @Override public long getMin() { return -9223372036854775808L; }
    @Override public long getMax() { return 9223372036854775807L; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 64; }
    },
    /** An unsigned 1-bit integer */
    UNSIGNED_1 {
    @Override public String getName() { return "Unsigned 1-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 1; }
    },
    /** An unsigned 2-bit integer */
    UNSIGNED_2 {
    @Override public String getName() { return "Unsigned 2-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 3L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 2; }
    },
    /** An unsigned 3-bit integer */
    UNSIGNED_3 {
    @Override public String getName() { return "Unsigned 3-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 7L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 3; }
    },
    /** An unsigned 4-bit integer */
    UNSIGNED_4 {
    @Override public String getName() { return "Unsigned 4-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 15L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 4; }
    },
    /** An unsigned 5-bit integer */
    UNSIGNED_5 {
    @Override public String getName() { return "Unsigned 5-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 31L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 5; }
    },
    /** An unsigned 6-bit integer */
    UNSIGNED_6 {
    @Override public String getName() { return "Unsigned 6-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 63L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 6; }
    },
    /** An unsigned 7-bit integer */
    UNSIGNED_7 {
    @Override public String getName() { return "Unsigned 7-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 127L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 7; }
    },
    /** An unsigned 8-bit integer */
    UNSIGNED_8 {
    @Override public String getName() { return "Unsigned 8-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 255L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 8; }
    },
    /** An unsigned 9-bit integer */
    UNSIGNED_9 {
    @Override public String getName() { return "Unsigned 9-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 511L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 9; }
    },
    /** An unsigned 10-bit integer */
    UNSIGNED_10 {
    @Override public String getName() { return "Unsigned 10-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1023L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 10; }
    },
    /** An unsigned 11-bit integer */
    UNSIGNED_11 {
    @Override public String getName() { return "Unsigned 11-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 2047L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 11; }
    },
    /** An unsigned 12-bit integer */
    UNSIGNED_12 {
    @Override public String getName() { return "Unsigned 12-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4095L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 12; }
    },
    /** An unsigned 13-bit integer */
    UNSIGNED_13 {
    @Override public String getName() { return "Unsigned 13-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 8191L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 13; }
    },
    /** An unsigned 14-bit integer */
    UNSIGNED_14 {
    @Override public String getName() { return "Unsigned 14-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 16383L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 14; }
    },
    /** An unsigned 15-bit integer */
    UNSIGNED_15 {
    @Override public String getName() { return "Unsigned 15-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 32767L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 15; }
    },
    /** An unsigned 16-bit integer */
    UNSIGNED_16 {
    @Override public String getName() { return "Unsigned 16-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 65535L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 16; }
    },
    /** An unsigned 17-bit integer */
    UNSIGNED_17 {
    @Override public String getName() { return "Unsigned 17-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 131071L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 17; }
    },
    /** An unsigned 18-bit integer */
    UNSIGNED_18 {
    @Override public String getName() { return "Unsigned 18-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 262143L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 18; }
    },
    /** An unsigned 19-bit integer */
    UNSIGNED_19 {
    @Override public String getName() { return "Unsigned 19-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 524287L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 19; }
    },
    /** An unsigned 20-bit integer */
    UNSIGNED_20 {
    @Override public String getName() { return "Unsigned 20-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1048575L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 20; }
    },
    /** An unsigned 21-bit integer */
    UNSIGNED_21 {
    @Override public String getName() { return "Unsigned 21-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 2097151L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 21; }
    },
    /** An unsigned 22-bit integer */
    UNSIGNED_22 {
    @Override public String getName() { return "Unsigned 22-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4194303L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 22; }
    },
    /** An unsigned 23-bit integer */
    UNSIGNED_23 {
    @Override public String getName() { return "Unsigned 23-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 8388607L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 23; }
    },
    /** An unsigned 24-bit integer */
    UNSIGNED_24 {
    @Override public String getName() { return "Unsigned 24-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 16777215L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 24; }
    },
    /** An unsigned 25-bit integer */
    UNSIGNED_25 {
    @Override public String getName() { return "Unsigned 25-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 33554431L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 25; }
    },
    /** An unsigned 26-bit integer */
    UNSIGNED_26 {
    @Override public String getName() { return "Unsigned 26-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 67108863L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 26; }
    },
    /** An unsigned 27-bit integer */
    UNSIGNED_27 {
    @Override public String getName() { return "Unsigned 27-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 134217727L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 27; }
    },
    /** An unsigned 28-bit integer */
    UNSIGNED_28 {
    @Override public String getName() { return "Unsigned 28-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 268435455L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 28; }
    },
    /** An unsigned 29-bit integer */
    UNSIGNED_29 {
    @Override public String getName() { return "Unsigned 29-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 536870911L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 29; }
    },
    /** An unsigned 30-bit integer */
    UNSIGNED_30 {
    @Override public String getName() { return "Unsigned 30-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1073741823L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 30; }
    },
    /** An unsigned 31-bit integer */
    UNSIGNED_31 {
    @Override public String getName() { return "Unsigned 31-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 2147483647L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 31; }
    },
    /** An unsigned 32-bit integer */
    UNSIGNED_32 {
    @Override public String getName() { return "Unsigned 32-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4294967295L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 32; }
    },
    /** An unsigned 33-bit integer */
    UNSIGNED_33 {
    @Override public String getName() { return "Unsigned 33-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 8589934591L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 33; }
    },
    /** An unsigned 34-bit integer */
    UNSIGNED_34 {
    @Override public String getName() { return "Unsigned 34-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 17179869183L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 34; }
    },
    /** An unsigned 35-bit integer */
    UNSIGNED_35 {
    @Override public String getName() { return "Unsigned 35-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 34359738367L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 35; }
    },
    /** An unsigned 36-bit integer */
    UNSIGNED_36 {
    @Override public String getName() { return "Unsigned 36-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 68719476735L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 36; }
    },
    /** An unsigned 37-bit integer */
    UNSIGNED_37 {
    @Override public String getName() { return "Unsigned 37-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 137438953471L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 37; }
    },
    /** An unsigned 38-bit integer */
    UNSIGNED_38 {
    @Override public String getName() { return "Unsigned 38-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 274877906943L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 38; }
    },
    /** An unsigned 39-bit integer */
    UNSIGNED_39 {
    @Override public String getName() { return "Unsigned 39-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 549755813887L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 39; }
    },
    /** An unsigned 40-bit integer */
    UNSIGNED_40 {
    @Override public String getName() { return "Unsigned 40-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1099511627775L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 40; }
    },
    /** An unsigned 41-bit integer */
    UNSIGNED_41 {
    @Override public String getName() { return "Unsigned 41-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 2199023255551L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 41; }
    },
    /** An unsigned 42-bit integer */
    UNSIGNED_42 {
    @Override public String getName() { return "Unsigned 42-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4398046511103L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 42; }
    },
    /** An unsigned 43-bit integer */
    UNSIGNED_43 {
    @Override public String getName() { return "Unsigned 43-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 8796093022207L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 43; }
    },
    /** An unsigned 44-bit integer */
    UNSIGNED_44 {
    @Override public String getName() { return "Unsigned 44-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 17592186044415L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 44; }
    },
    /** An unsigned 45-bit integer */
    UNSIGNED_45 {
    @Override public String getName() { return "Unsigned 45-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 35184372088831L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 45; }
    },
    /** An unsigned 46-bit integer */
    UNSIGNED_46 {
    @Override public String getName() { return "Unsigned 46-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 70368744177663L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 46; }
    },
    /** An unsigned 47-bit integer */
    UNSIGNED_47 {
    @Override public String getName() { return "Unsigned 47-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 140737488355327L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 47; }
    },
    /** An unsigned 48-bit integer */
    UNSIGNED_48 {
    @Override public String getName() { return "Unsigned 48-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 281474976710655L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 48; }
    },
    /** An unsigned 49-bit integer */
    UNSIGNED_49 {
    @Override public String getName() { return "Unsigned 49-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 562949953421311L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 49; }
    },
    /** An unsigned 50-bit integer */
    UNSIGNED_50 {
    @Override public String getName() { return "Unsigned 50-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1125899906842623L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 50; }
    },
    /** An unsigned 51-bit integer */
    UNSIGNED_51 {
    @Override public String getName() { return "Unsigned 51-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 2251799813685247L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 51; }
    },
    /** An unsigned 52-bit integer */
    UNSIGNED_52 {
    @Override public String getName() { return "Unsigned 52-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4503599627370495L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 52; }
    },
    /** An unsigned 53-bit integer */
    UNSIGNED_53 {
    @Override public String getName() { return "Unsigned 53-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 9007199254740991L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 53; }
    },
    /** An unsigned 54-bit integer */
    UNSIGNED_54 {
    @Override public String getName() { return "Unsigned 54-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 18014398509481983L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 54; }
    },
    /** An unsigned 55-bit integer */
    UNSIGNED_55 {
    @Override public String getName() { return "Unsigned 55-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 36028797018963967L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 55; }
    },
    /** An unsigned 56-bit integer */
    UNSIGNED_56 {
    @Override public String getName() { return "Unsigned 56-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 72057594037927935L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 56; }
    },
    /** An unsigned 57-bit integer */
    UNSIGNED_57 {
    @Override public String getName() { return "Unsigned 57-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 144115188075855871L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 57; }
    },
    /** An unsigned 58-bit integer */
    UNSIGNED_58 {
    @Override public String getName() { return "Unsigned 58-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 288230376151711743L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 58; }
    },
    /** An unsigned 59-bit integer */
    UNSIGNED_59 {
    @Override public String getName() { return "Unsigned 59-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 576460752303423487L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 59; }
    },
    /** An unsigned 60-bit integer */
    UNSIGNED_60 {
    @Override public String getName() { return "Unsigned 60-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 1152921504606846975L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 60; }
    },
    /** An unsigned 61-bit integer */
    UNSIGNED_61 {
    @Override public String getName() { return "Unsigned 61-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 2305843009213693951L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 61; }
    },
    /** An unsigned 62-bit integer */
    UNSIGNED_62 {
    @Override public String getName() { return "Unsigned 62-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4611686018427387903L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 62; }
    },
    /** An unsigned 63-bit integer */
    UNSIGNED_63 {
    @Override public String getName() { return "Unsigned 63-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 9223372036854775807L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 63; }
    };
	//@formatter:on

  /**
   * Gets the name.
   *
   * @return the name
   */
  public abstract String getName();

  /**
   * Gets the min value.
   *
   * @return the min value
   */
  public abstract long getMin();

  /**
   * Gets the max value.
   *
   * @return the max value
   */
  public abstract long getMax();

  /**
   * Checks if is signed.
   *
   * @return true, if is signed
   */
  public abstract boolean isSigned();

  /**
   * Gets the bit depth.
   *
   * @return the bit depth
   */
  public abstract int getBitDepth();

  /**
   * Gets the largest absolute integer that can be returned. A signed integer can return a larger
   * absolute value for its min value than for its max (as a single bit is used to hold the sign).
   * For an unsigned integer this will be the max value.
   *
   * @return the absolute max
   */
  public long getAbsoluteMax() {
    return (isSigned()) ? -getMin() : getMax();
  }

  /**
   * Gets the value for the ordinal.
   *
   * @param ordinal the ordinal
   * @return the integer type
   * @throws IllegalArgumentException If the ordinal is invalid
   */
  public static IntegerType forOrdinal(int ordinal) throws IllegalArgumentException {
    if (ordinal < 0) {
      throw new IllegalArgumentException("Negative ordinal");
    }
    final IntegerType[] values = IntegerType.values();
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
  public static IntegerType forOrdinal(int ordinal, IntegerType defaultValue) {
    final IntegerType[] values = IntegerType.values();
    if (ordinal < 0 || ordinal >= values.length) {
      return (defaultValue == null) ? values[0] : defaultValue;
    }
    return values[ordinal];
  }

  /**
   * Get the max value of an unsigned integer of the given bit depth.
   *
   * @param bitDepth the bit depth (range 1-63)
   * @return the max value
   * @throws IllegalArgumentException If the bit-depth is invalid
   */
  public static long maxUnsigned(int bitDepth) throws IllegalArgumentException {
    if (bitDepth < 0 || bitDepth > 63) {
      throw new IllegalArgumentException("Invalid bit depth: " + bitDepth);
    }
    long max = 1;
    while (bitDepth-- > 0) {
      max = max << 1;
    }
    return max - 1;
  }

  /**
   * Get the max value of a signed integer of the given bit depth.
   *
   * @param bitDepth the bit depth (range 1-64)
   * @return the max value
   * @throws IllegalArgumentException If the bit-depth is invalid
   */
  public static long maxSigned(int bitDepth) {
    if (bitDepth < 0 || bitDepth > 64) {
      throw new IllegalArgumentException("Invalid bit depth: " + bitDepth);
    }
    long max = 1;
    while (bitDepth-- > 1) {
      max = max << 1;
    }
    return max - 1;
  }

  /**
   * Get the min value of a signed integer of the given bit depth.
   *
   * @param bitDepth the bit depth (range 1-64)
   * @return the min value
   * @throws IllegalArgumentException If the bit-depth is invalid
   */
  public static long minSigned(int bitDepth) {
    if (bitDepth < 0 || bitDepth > 64) {
      throw new IllegalArgumentException("Invalid bit depth: " + bitDepth);
    }
    long max = 1;
    while (bitDepth-- > 1) {
      max = max << 1;
    }
    return -max;
  }
}
