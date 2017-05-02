package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Plugins Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Class for manipulating bit flags
 * 
 * @author Alex Herbert
 */
public class BitFlags
{
	/**
	 * Check if all of the given bits are set in the flags
	 * 
	 * @param flags
	 * @param bits
	 * @return True if all are set
	 */
	public static boolean areSet(final int flags, final int bits)
	{
		return (flags & bits) == bits;
	}

	/**
	 * Check if any of the given bits are set in the flags
	 * 
	 * @param flags
	 * @param bits
	 * @return True if any are set
	 */
	public static boolean anySet(final int flags, final int bits)
	{
		return (flags & bits) != 0;
	}

	/**
	 * Check if any of the given bits are not set in the flags
	 * 
	 * @param flags
	 * @param bits
	 * @return True if any are not set
	 */
	public static boolean anyNotSet(final int flags, final int bits)
	{
		return !areSet(flags, bits);
	}

	/**
	 * Set the given bits in the flags.
	 *
	 * @param flags
	 *            the flags
	 * @param bits
	 *            the bits
	 * @return the new flags
	 */
	public static int set(final int flags, final int bits)
	{
		return flags | bits;
	}

	/**
	 * Unset the given bits in the flags.
	 *
	 * @param flags
	 *            the flags
	 * @param bits
	 *            the bits
	 * @return the new flags
	 */
	public static int unset(final int flags, final int bits)
	{
		return flags & ~bits;
	}
}