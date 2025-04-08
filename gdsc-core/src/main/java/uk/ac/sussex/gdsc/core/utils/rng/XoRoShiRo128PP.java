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

package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.core.source64.XoRoShiRo128PlusPlus;

/**
 * A fast all-purpose 64-bit generator.
 *
 * <p>This is a member of the Xor-Shift-Rotate family of generators. Memory footprint is 128 bits
 * and the period is 2<sup>128</sup>-1.
 *
 * <p>This has been extended from the Commons RNG implementation to add functionality:</p>
 *
 * <ul>
 *
 * <li>Implement {@link SplittableUniformRandomProvider}.
 *
 * <li>Make the copy function public.
 *
 * <li>Allow construction from any seed with checks for an invalid all-zero bit state.
 *
 * </ul>
 *
 * @see <a href="http://xoshiro.di.unimi.it/xoroshiro128plusplus.c">Original source code</a>
 * @see <a href="http://xoshiro.di.unimi.it/">xorshiro / xoroshiro generators</a>
 */
public final class XoRoShiRo128PP extends XoRoShiRo128PlusPlus
    implements SplittableUniformRandomProvider {
  /**
   * The golden ratio, phi, scaled to 64-bits and rounded to odd.
   *
   * <pre>
   * phi = (sqrt(5) - 1) / 2) * 2^64
   *     ~ 0.61803 * 2^64
   *     = 11400714819323198485 (unsigned 64-bit integer)
   * </pre>
   */
  private static final long GOLDEN_RATIO = 0x9e3779b97f4a7c15L;

  /**
   * Create a new instance setting the 128-bit state by expansion and mixing of the provided 64-bit
   * seed.
   *
   * @param seed the seed for the state
   */
  public XoRoShiRo128PP(long seed) {
    super(Mixers.stafford13(seed + GOLDEN_RATIO), Mixers.stafford13(seed + 2 * GOLDEN_RATIO));
  }

  /**
   * Create a new instance setting the 128-bit state using the provided seed.
   *
   * <p>Note: This generator is invalid with all-zero bits in the state. If both seeds are zero the
   * results is the same as using the single argument constructor with a seed of zero where the
   * state is created by expansion and mixing of the single 64-bit seed.
   *
   * @param seed0 the seed for the first state
   * @param seed1 the seed for the second state
   */
  public XoRoShiRo128PP(long seed0, long seed1) {
    super(seed0, seed1);
    // Combine bits and check for zero seed
    if ((seed0 | seed1) == 0) {
      this.state0 = Mixers.stafford13(GOLDEN_RATIO);
      this.state1 = Mixers.stafford13(2 * GOLDEN_RATIO);
    }
  }

  /**
   * Creates a copy instance.
   *
   * @param source Source to copy.
   */
  private XoRoShiRo128PP(XoRoShiRo128PP source) {
    super(source);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This has been made public from the default protected level in Commons RNG. Use with caution.
   */
  @Override
  public XoRoShiRo128PP copy() {
    return new XoRoShiRo128PP(this);
  }

  @Override
  public XoRoShiRo128PP split() {
    // Advance the generator.
    // Allows multiple calls to split to create different generators.
    final long s0 = state0;
    final long s1 = state1;
    final long x = next();

    // Mix the bits of the generator. Using the stafford mixer on each state alone
    // will not cascade the bits across the entire 128-bits. To make the lower
    // influence the upper (and vice versa) the current random output is combined in.
    //
    // Note:
    //
    // This method used to use the Stafford mixer on the separate states. It was later
    // changed to combine in the random bits of current output.
    //
    // This may not be the best method to scramble the bits of the state.
    // It uses 8 xors, 6 shifts, 6 multiplies.
    //
    // The Java implementation by Vigna (co-creator of the RNG method)
    // uses a round of SpookyHash ShortMix which cascades 256-bits.
    // The upper 128-bits are the state plus random constants.
    // That method uses 14 additions, 11 xors, 12 left rotates.
    // See:
    // Artifact: it.unimi.dsi:dsiutils
    // Class: it.unimi.dsi.util.XoRoShiRo128PlusPlusRandomGenerator (v 2.7.0)
    // SpookyHash: http://burtleburtle.net/bob/hash/spooky.html

    final long seed0 = Mixers.stafford13(s0 ^ x);
    final long seed1 = Mixers.stafford13(s1 ^ x);

    // Use the constructor to avoid a zero initial state
    return new XoRoShiRo128PP(seed0, seed1);
  }
}
