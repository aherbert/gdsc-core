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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

/**
 * Class for manipulating bit flags.
 */
public final class BitFlagUtils {

  /** No public construction. */
  private BitFlagUtils() {}

  /**
   * Check if all of the given bits are set in the flags.
   *
   * @param flags the flags
   * @param bits the bits
   * @return True if all are set
   */
  public static boolean areSet(final int flags, final int bits) {
    return (flags & bits) == bits;
  }

  /**
   * Check if any of the given bits are set in the flags.
   *
   * @param flags the flags
   * @param bits the bits
   * @return True if any are set
   */
  public static boolean anySet(final int flags, final int bits) {
    return (flags & bits) != 0;
  }

  /**
   * Check if any of the given bits are not set in the flags.
   *
   * @param flags the flags
   * @param bits the bits
   * @return True if any are not set
   */
  public static boolean anyNotSet(final int flags, final int bits) {
    return !areSet(flags, bits);
  }

  /**
   * Set the given bits in the flags.
   *
   * @param flags the flags
   * @param bits the bits
   * @return the new flags
   */
  public static int set(final int flags, final int bits) {
    return flags | bits;
  }

  /**
   * Unset the given bits in the flags.
   *
   * @param flags the flags
   * @param bits the bits
   * @return the new flags
   */
  public static int unset(final int flags, final int bits) {
    return flags & ~bits;
  }
}
