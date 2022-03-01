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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils.function;

import java.util.Objects;

/**
 * Represents an operation that accepts a single {@code double}-valued argument and returns no
 * result. This is the primitive type specialization of {@link java.util.function.Consumer} for
 * {@code float}. Unlike most other functional interfaces, {@code FloatConsumer} is expected to
 * operate via side-effects.
 *
 * <p>This is a functional interface whose functional method is {@link #accept(float)}.
 *
 * <p>This is based on {@link java.util.function.DoubleConsumer DoubleConsumer} for manipulation of
 * {@code float} values.
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface FloatConsumer {

  /**
   * Performs this operation on the given argument.
   *
   * @param value the input argument
   */
  void accept(float value);

  /**
   * Returns a composed {@code FloatConsumer} that performs, in sequence, this operation followed by
   * the {@code after} operation. If performing either operation throws an exception, it is relayed
   * to the caller of the composed operation. If performing this operation throws an exception, the
   * {@code after} operation will not be performed.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code FloatConsumer} that performs in sequence this operation followed by
   *         the {@code after} operation
   * @throws NullPointerException if {@code after} is null
   */
  default FloatConsumer andThen(FloatConsumer after) {
    Objects.requireNonNull(after);
    return (float t) -> {
      accept(t);
      after.accept(t);
    };
  }
}
