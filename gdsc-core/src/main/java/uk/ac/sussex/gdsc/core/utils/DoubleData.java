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

import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * Specify double data.
 */
public interface DoubleData {
  /**
   * The number of values.
   *
   * @return the number of values
   */
  int size();

  /**
   * Get the values.
   *
   * @return the values
   */
  double[] values();

  /**
   * Performs the given action for each value of the data until all elements have been processed or
   * the action throws an exception. Unless otherwise specified by the implementing class, actions
   * are performed in the order of values as defined by the {@link #values()} method. Exceptions
   * thrown by the action are relayed to the caller.
   *
   * @param action The action to be performed for each element
   */
  void forEach(DoubleConsumer action);

  /**
   * Wrap the values to create an instance.
   *
   * @param values the values
   * @return the double data
   */
  static DoubleData wrap(double[] values) {
    Objects.requireNonNull(values);
    return new DoubleData() {
      @Override
      public double[] values() {
        return values;
      }

      @Override
      public int size() {
        return values.length;
      }

      @Override
      public void forEach(DoubleConsumer action) {
          for (final double d : values) {
          action.accept(d);
        }
      }
    };
  }
}
