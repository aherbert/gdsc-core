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

package uk.ac.sussex.gdsc.core.utils.function;

/**
 * Represents an operation that accepts an {@code int}-valued and an {@code object}-valued argument,
 * and returns no result. This is the {@code (int, reference)} specialization of
 * {@link java.util.function.BiConsumer BiConsumer}. Unlike most other functional interfaces,
 * {@code IntObjConsumer} is expected to operate via side-effects.
 *
 * @param <T> the type of the object argument to the operation
 */
@FunctionalInterface
public interface IntObjConsumer<T> {
  /**
   * Performs this operation on the given arguments.
   *
   * @param a the first input argument
   * @param b the second input argument
   */
  void accept(int a, T b);
}
