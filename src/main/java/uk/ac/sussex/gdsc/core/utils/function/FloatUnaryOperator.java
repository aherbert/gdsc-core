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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
 * Represents an operation on a single {@code float}-valued operand that produces a
 * {@code float}-valued result. This is the primitive type specialization of
 * {@link java.util.function.UnaryOperator UnaryOperator} for {@code float}.
 *
 * <p>This is a functional interface whose functional method is {@link #applyAsFloat(float)}.
 *
 * <p>This is based on {@link java.util.function.DoubleUnaryOperator DoubleUnaryOperator} for
 * manipulation of {@code float} values.
 *
 * @see java.util.function.UnaryOperator
 * @since 2.0
 */
@FunctionalInterface
public interface FloatUnaryOperator {

  /**
   * Applies this operator to the given operand.
   *
   * @param operand the operand
   * @return the operator result
   */
  float applyAsFloat(float operand);

  /**
   * Returns a composed operator that first applies the {@code before} operator to its input, and
   * then applies this operator to the result. If evaluation of either operator throws an exception,
   * it is relayed to the caller of the composed operator.
   *
   * @param before the operator to apply before this operator is applied
   * @return a composed operator that first applies the {@code before} operator and then applies
   *         this operator
   * @throws NullPointerException if before is null
   *
   * @see #andThen(FloatUnaryOperator)
   */
  default FloatUnaryOperator compose(FloatUnaryOperator before) {
    Objects.requireNonNull(before);
    return (float v) -> applyAsFloat(before.applyAsFloat(v));
  }

  /**
   * Returns a composed operator that first applies this operator to its input, and then applies the
   * {@code after} operator to the result. If evaluation of either operator throws an exception, it
   * is relayed to the caller of the composed operator.
   *
   * @param after the operator to apply after this operator is applied
   * @return a composed operator that first applies this operator and then applies the {@code after}
   *         operator
   * @throws NullPointerException if after is null
   *
   * @see #compose(FloatUnaryOperator)
   */
  default FloatUnaryOperator andThen(FloatUnaryOperator after) {
    Objects.requireNonNull(after);
    return (float t) -> after.applyAsFloat(applyAsFloat(t));
  }

  /**
   * Returns a unary operator that always returns its input argument.
   *
   * @return a unary operator that always returns its input argument
   */
  static FloatUnaryOperator identity() {
    return t -> t;
  }
}
