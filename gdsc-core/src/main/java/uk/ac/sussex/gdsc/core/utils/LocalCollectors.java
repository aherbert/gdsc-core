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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Provides implementations of the {@link Collector} interface.
 */
public final class LocalCollectors {
  /** No public instances. */
  private LocalCollectors() {}

  /**
   * Returns a {@code Collector} that accumulates the input elements into a new {@code LocalList}.
   *
   * @param <T> the type of the input elements
   * @return a {@code Collector} which collects all the input elements into a {@code LocalList}, in
   *         encounter order
   */
  public static <T> Collector<T, ?, LocalList<T>> toLocalList() {
    return LocalListCollector.instance();
  }

  /**
   * Implementation of a Collector that uses a LocalList.
   *
   * @param <T> the generic type
   */
  private static class LocalListCollector<T> implements Collector<T, LocalList<T>, LocalList<T>> {
    private static final Set<Collector.Characteristics> CHARACTERISTICS =
        Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    @SuppressWarnings("rawtypes")
    private static final LocalListCollector INSTANCE = new LocalListCollector();

    @Override
    public Supplier<LocalList<T>> supplier() {
      return LocalList::new;
    }

    @Override
    public BiConsumer<LocalList<T>, T> accumulator() {
      return LocalList::add;
    }

    @Override
    public BinaryOperator<LocalList<T>> combiner() {
      return (a, b) -> {
        // Use specialised method for combining two local lists.
        // Use the list with enough capacity if possible.
        if (a.getCapacity() - a.size() >= b.size()) {
          a.addAll(b);
          return a;
        }
        // Possible new memory allocation
        b.addAll(a);
        return b;
      };
    }

    @Override
    public Function<LocalList<T>, LocalList<T>> finisher() {
      // Nothing to do
      return a -> a;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return CHARACTERISTICS;
    }

    /**
     * Obtain an instance.
     *
     * @param <T> the generic type
     * @return the collector
     */
    static <T> LocalListCollector<T> instance() {
      return INSTANCE;
    }
  }
}
