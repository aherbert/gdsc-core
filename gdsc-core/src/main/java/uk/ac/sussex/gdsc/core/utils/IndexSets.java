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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Creates index sets.
 */
public final class IndexSets {

  /**
   * An index set backed by a {@link BitSet}.
   */
  private static class BitSetIndexSet implements IndexSet {
    /** The set. */
    private final BitSet set;

    /**
     * Create an instance.
     *
     * @param capacity the capacity
     */
    BitSetIndexSet(int capacity) {
      set = new BitSet(capacity);
    }

    @Override
    public boolean add(int index) {
      if (contains(index)) {
        return false;
      }
      set.set(index);
      return true;
    }

    @Override
    public boolean contains(int index) {
      return set.get(index);
    }

    @Override
    public int size() {
      return set.cardinality();
    }

    @Override
    public void clear() {
      set.clear();
    }

    @Override
    public IntStream stream() {
      return set.stream();
    }
  }

  /**
   * An index set backed by a open-addressed hash table using linear hashing.
   * Table size is a power of 2.
   */
  private static class HashIndexSet implements IndexSet {
    /** The set. */
    private final int[] set;
    /** The size. */
    private int size;

    /**
     * Create an instance.
     *
     * @param capacity the capacity
     */
    HashIndexSet(int capacity) {
      // This will generate a load factor of 0.25 to 0.5
      set = new int[MathUtils.nextPow2(capacity * 2)];
    }

    @Override
    public boolean add(int index) {
      return true;
    }

    @Override
    public boolean contains(int index) {
      return false;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public IntStream stream() {
      return Arrays.stream(set).filter(i -> i < 0).map(i -> ~i);
    }

    @Override
    public void clear() {
      size = 0;
      Arrays.fill(set, 0);
    }
  }

  /** No instances. */
  private IndexSets() {}

  /**
   * Creates an index set with the specified initial capacity.
   *
   * <p>Note that a {@link BitSet} can store all indices using 2<sup>25</sup> long values, or 32MiB
   * of storage. This is equal to a capacity of 2<sup>26</sup> integers. Implementations may be
   * optimised for smaller capacities for example by using a hash set.
   *
   * @param capacity the capacity
   * @return the index set
   * @throws IllegalArgumentException if capacity is negative
   */
  public static IndexSet create(int capacity) {
    ValidationUtils.checkPositive(capacity, "capacity");
    if (capacity >= (1 << 26)) {
      return new BitSetIndexSet(capacity);
    }
    return new BitSetIndexSet(capacity);
  }
}
