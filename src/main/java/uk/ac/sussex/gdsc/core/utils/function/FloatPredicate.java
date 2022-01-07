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
 * Represents a predicate (boolean-valued function) of one {@code float}-valued argument. This is
 * the {@code float}-consuming primitive type specialization of
 * {@link java.util.function.Predicate}.
 *
 * <p>This is a functional interface whose functional method is {@link #test(float)}.
 *
 * <p>This is based on {@link java.util.function.DoublePredicate DoublePredicate} for manipulation
 * of {@code float} values.
 *
 * @see java.util.function.Predicate
 * @since 2.0
 */
@FunctionalInterface
public interface FloatPredicate {

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param value the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  boolean test(float value);

  /**
   * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
   * and another. When evaluating the composed predicate, if this predicate is {@code false}, then
   * the {@code other} predicate is not evaluated.
   *
   * <p>Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
   * evaluation of this predicate throws an exception, the {@code other} predicate will not be
   * evaluated.
   *
   * @param other a predicate that will be logically-ANDed with this predicate
   * @return a composed predicate that represents the short-circuiting logical AND of this predicate
   *         and the {@code other} predicate
   * @throws NullPointerException if other is null
   */
  default FloatPredicate and(FloatPredicate other) {
    Objects.requireNonNull(other);
    return (value) -> test(value) && other.test(value);
  }

  /**
   * Returns a predicate that represents the logical negation of this predicate.
   *
   * @return a predicate that represents the logical negation of this predicate
   */
  default FloatPredicate negate() {
    return (value) -> !test(value);
  }

  /**
   * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
   * and another. When evaluating the composed predicate, if this predicate is {@code true}, then
   * the {@code other} predicate is not evaluated.
   *
   * <p>Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
   * evaluation of this predicate throws an exception, the {@code other} predicate will not be
   * evaluated.
   *
   * @param other a predicate that will be logically-ORed with this predicate
   * @return a composed predicate that represents the short-circuiting logical OR of this predicate
   *         and the {@code other} predicate
   * @throws NullPointerException if other is null
   */
  default FloatPredicate or(FloatPredicate other) {
    Objects.requireNonNull(other);
    return (value) -> test(value) || other.test(value);
  }
}
