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

import org.apache.commons.rng.JumpableUniformRandomProvider;
import org.apache.commons.rng.LongJumpableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Class to construct a {@link UniformRandomProvider}.
 *
 * <p>This class provides a simple entry path to create all-purpose providers for most random
 * requirements.
 */
public final class UniformRandomProviders {

  /** No construction. */
  private UniformRandomProviders() {}

  /**
   * Creates a new {@link UniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * @return the provider
   */
  public static UniformRandomProvider create() {
    return new XoRoShiRo128PP(RandomSource.createLong(), RandomSource.createLong());
  }

  /**
   * Creates a new {@link UniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * <p>The seed can be used to ensure a reproducible generator is created. Otherwise use
   * {@link #create()}.
   *
   * @param seed the seed
   * @return the provider
   */
  public static UniformRandomProvider create(long seed) {
    return new XoRoShiRo128PP(seed);
  }

  /**
   * Creates a new {@link SplittableUniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * @return the provider
   */
  public static SplittableUniformRandomProvider createSplittable() {
    return new XoRoShiRo128PP(RandomSource.createLong(), RandomSource.createLong());
  }

  /**
   * Creates a new {@link SplittableUniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * <p>The seed can be used to ensure a reproducible generator is created. Otherwise use
   * {@link #createSplittable()}.
   *
   * @param seed the seed
   * @return the provider
   */
  public static SplittableUniformRandomProvider createSplittable(long seed) {
    return new XoRoShiRo128PP(seed);
  }

  /**
   * Creates a new {@link JumpableUniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * <p>The jump size is the equivalent of 2<sup>64</sup> calls to
   * {@link UniformRandomProvider#nextLong() nextLong()}. It can provide up to 2<sup>64</sup>
   * non-overlapping subsequences.</p>
   *
   * @return the provider
   */
  public static JumpableUniformRandomProvider createJumpable() {
    return new XoRoShiRo128PP(RandomSource.createLong(), RandomSource.createLong());
  }

  /**
   * Creates a new {@link JumpableUniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * <p>The jump size is the equivalent of 2<sup>64</sup> calls to
   * {@link UniformRandomProvider#nextLong() nextLong()}. It can provide up to 2<sup>64</sup>
   * non-overlapping subsequences.</p>
   *
   * <p>The seed can be used to ensure a reproducible generator is created. Otherwise use
   * {@link #createJumpable()}.
   *
   * @param seed the seed
   * @return the provider
   */
  public static JumpableUniformRandomProvider createJumpable(long seed) {
    return new XoRoShiRo128PP(seed);
  }

  /**
   * Creates a new {@link JumpableUniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * <p>The jump size is the equivalent of 2<sup>96</sup> calls to
   * {@link UniformRandomProvider#nextLong() nextLong()}. It can provide up to 2<sup>32</sup>
   * non-overlapping subsequences of length 2<sup>96</sup>; each subsequence can provide up to
   * 2<sup>32</sup> non-overlapping subsequences of length 2<sup>64</sup> using the
   * {@link JumpableUniformRandomProvider#jump()} method.</p>
   *
   * @return the provider
   */
  public static LongJumpableUniformRandomProvider createLongJumpable() {
    return new XoRoShiRo128PP(RandomSource.createLong(), RandomSource.createLong());
  }

  /**
   * Creates a new {@link JumpableUniformRandomProvider}.
   *
   * <p>The period of the generator is 2<sup>128</sup> - 1.
   *
   * <p>The jump size is the equivalent of 2<sup>96</sup> calls to
   * {@link UniformRandomProvider#nextLong() nextLong()}. It can provide up to 2<sup>32</sup>
   * non-overlapping subsequences of length 2<sup>96</sup>; each subsequence can provide up to
   * 2<sup>32</sup> non-overlapping subsequences of length 2<sup>64</sup> using the
   * {@link JumpableUniformRandomProvider#jump()} method.</p>
   *
   * <p>The seed can be used to ensure a reproducible generator is created. Otherwise use
   * {@link #createLongJumpable()}.
   *
   * @param seed the seed
   * @return the provider
   */
  public static LongJumpableUniformRandomProvider createLongJumpable(long seed) {
    return new XoRoShiRo128PP(seed);
  }
}
