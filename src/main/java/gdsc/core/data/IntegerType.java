package gdsc.core.data;

/**
 * Contains pre-computed reference data for integers.
 */
public enum IntegerType
{
	//@formatter:off
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
	/** A signed 32-bit integer */
	SIGNED_32 { 
	@Override public String getName() { return "Signed 32-bit integer"; }
    @Override public long getMin() { return Integer.MIN_VALUE; }
    @Override public long getMax() { return Integer.MAX_VALUE; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 32; }
	},
	/** A signed 64-bit integer (long) */
	SIGNED_64 { 
	@Override public String getName() { return "Signed 64-bit integer"; }
    @Override public long getMin() { return Long.MIN_VALUE; }
    @Override public long getMax() { return Long.MAX_VALUE; }
    @Override public boolean isSigned() { return true; }
    @Override public int getBitDepth() { return 64; }
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
	/** An unsigned 32-bit integer */
	UNSIGNED_32 { 
	@Override public String getName() { return "Unsigned 32-bit integer"; }
    @Override public long getMin() { return 0L; }
    @Override public long getMax() { return 4294967295L; }
    @Override public boolean isSigned() { return false; }
    @Override public int getBitDepth() { return 32; }
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
	 * Gets the largest absolute integer that can be returned. A signed integer can return a larger absolute value for
	 * its min value than for its max (as a single bit is used to hold the sign). For an unsigned integer this will be
	 * the max value.
	 *
	 * @return the absolute max
	 */
	public long getAbsoluteMax()
	{
		return (isSigned()) ? -getMin() : getMax();
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
	public static IntegerType forOrdinal(int ordinal) throws IllegalArgumentException
	{
		if (ordinal < 0)
			throw new IllegalArgumentException("Negative ordinal");
		IntegerType[] values = IntegerType.values();
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
	public static IntegerType forOrdinal(int ordinal, IntegerType defaultValue)
	{
		IntegerType[] values = IntegerType.values();
		if (ordinal < 0 || ordinal >= values.length)
			return (defaultValue == null) ? values[0] : defaultValue;
		return values[ordinal];
	}
}