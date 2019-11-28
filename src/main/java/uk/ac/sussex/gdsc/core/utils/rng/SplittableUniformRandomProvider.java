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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import org.apache.commons.rng.UniformRandomProvider;

/**
 * Applies to generators of random number sequences that follow a uniform distribution and can be
 * split to create a new independent instance.
 *
 * <h2>Overlap Computations</h2>
 *
 * <p>The probability of overlap among different streams of generators can be computed using an
 * approximation. The following is extracted from L&#39;Ecuyer, et al. (2017).
 *
 * <p>If the RNG has period {@code p} and we take {@code s} different streams of length {@code l},
 * with random starting points in the sequence, and assuming that {@code sl/p} is very small, the
 * probability that there is some overlap is approximately {@code s}<sup>2</sup>{@code l/p}.
 *
 * <p>For example if {@code s=l=}2<sup>20</sup> the overlap probability
 * 2<sup>60</sup>{@code /p} is near 2<sup>-68</sup> for
 * {@code p=}2<sup>128</sup> and near 2<sup>-964</sup> for {@code p=}2<sup>1024</sup>.
 *
 * @since 2.0
 * @see <a href="https://doi.org/10.1016/j.matcom.2016.05.005">L&#39;Ecuyer, et al. Random numbers
 *      for parallel computers: Requirements and methods, with emphasis on GPUs. (2017), Mathematics
 *      and Computers in Simulation 135, pp. 3-17</a>
 */
public interface SplittableUniformRandomProvider extends UniformRandomProvider {
  /**
   * Create and return a new instance which shares no state with this instance.
   *
   * <p>The state of the current instance is advanced allowing repeat split invocations on the same
   * instance to create different generators.
   *
   * <p>The output from the two generators should have the same statistical properties as the same
   * quantity of output from the original generator. This relationship is expected to hold in the
   * event of recursive splitting.
   *
   * @return the new generator
   */
  SplittableUniformRandomProvider split();
}
