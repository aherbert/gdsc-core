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
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Creates index sets.
 */
public final class IndexSets {

  /**
   * An index set backed by a {@link BitSet}.
   */
  static class BitSetIndexSet implements IndexSet {
    /** The set. */
    private final BitSet set;

    /**
     * Create an instance with initial size to store indices up to the specified maximum.
     *
     * @param maximum the maximum
     */
    BitSetIndexSet(int maximum) {
      // maximum is inclusive, nbits is exclusive so add 1 if possible.
      set = new BitSet(maximum < Integer.MAX_VALUE ? maximum + 1 : maximum);
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
    public void put(int index) {
      set.set(index);
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
    public IntStream intStream() {
      return set.stream();
    }

    @Override
    public OfInt spliterator() {
      return intStream().spliterator();
    }

    @Override
    public void forEach(IntConsumer action) {
      for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
        action.accept(i);
        if (i == Integer.MAX_VALUE) {
          // or (i+1) would overflow
          break;
        }
      }
    }
  }

  /**
   * An index set backed by a open-addressed hash table using linear hashing. Table size is a power
   * of 2 and has a maximum capacity of 2^29 with a load factor of 0.5.
   *
   * <p>Values are stored using bit inversion. Any positive index will have a negative
   * representation when stored. An empty slot is indicated by a zero.
   */
  static class HashIndexSet implements IndexSet {
    /** The maximum capacity of the set. */
    private static final int MAX_CAPACITY = 1 << 29;
    /** The minimum size of the backing array. */
    private static final int MIN_SIZE = 16;
    /** The maximum size of the backing array. */
    private static final int MAX_SIZE = 1 << 30;
    /**
     * Unsigned 32-bit integer numerator of the golden ratio (0.618) with an assumed denominator of
     * 2^32.
     *
     * <pre>
     * 2654435769 = round(2^32 * (sqrt(5) - 1) / 2)
     * Long.toHexString((long)(0x1p32 * (Math.sqrt(5.0) - 1) / 2))
     * </pre>
     */
    private static final int PHI = 0x9e3779b9;

    /** The set. */
    private int[] set;
    /** The size. */
    private int size;

    /**
     * Create an instance with initial size to store up to the specified capacity.
     *
     * @param capacity the initial capacity
     */
    HashIndexSet(int capacity) {
      if (capacity > MAX_CAPACITY) {
        throw new IllegalArgumentException("Unsupported capacity: " + capacity);
      }
      // This will generate a load factor at capacity in the range (0.25, 0.5]
      set = new int[MathUtils.nextPow2(Math.max(MIN_SIZE, capacity * 2))];
    }

    @Override
    public boolean add(int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
      final int[] keys = set;
      final int key = ~index;
      final int mask = keys.length - 1;
      int pos = mix(index) & mask;
      int curr = keys[pos];
      if (curr < 0) {
        if (curr == key) {
          // Already present
          return false;
        }
        // Probe
        while ((curr = keys[pos = (pos + 1) & mask]) < 0) {
          if (curr == key) {
            // Already present
            return false;
          }
        }
      }
      // Insert
      keys[pos] = key;
      // Here the load factor is 0.5: size > keys.length * 0.5
      if (++size > (mask + 1) >>> 1) {
        grow(pos);
      }
      return true;
    }

    /**
     * Grow the table by rehashing all the current elements. The growth factor is 2.
     *
     * <p>If the maximum capacity is exceeded then the last inserted element is removed. This leaves
     * the table in the state of its maximum capacity and prevents insertion into the over-filled
     * table. This prevents the table becoming filled in every slot which will result in an infinite
     * loop when adding a previously unseen index.
     *
     * @param lastInserted the last inserted position
     */
    private void grow(int lastInserted) {
      final int[] keys = set;
      final int len = keys.length;
      if (len == MAX_SIZE) {
        keys[lastInserted] = 0;
        size--;
        throw new IllegalStateException("Capacity exceeded");
      }
      final int[] newKeys = new int[len << 1];
      final int mask = newKeys.length - 1;
      int pos;
      int key;
      for (int i = keys.length - 1; i >= 0; i--) {
        if ((key = keys[i]) < 0) {
          pos = mix(~key) & mask;
          if (newKeys[pos] < 0) {
            // Probe
            while ((newKeys[pos = (pos + 1) & mask]) < 0) {
              // search for empty
            }
          }
          newKeys[pos] = key;
        }
      }
      set = newKeys;
    }

    @Override
    public boolean contains(int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("Invalid index: " + index);
      }
      final int[] keys = set;
      final int key = ~index;
      final int mask = keys.length - 1;
      int pos = mix(index) & mask;
      int curr = keys[pos];
      if (curr == 0) {
        return false;
      }
      if (curr == key) {
        return true;
      }
      // Probe
      while (true) {
        pos = (pos + 1) & mask;
        curr = keys[pos];
        if (curr == 0) {
          // No more entries
          return false;
        }
        if (curr == key) {
          return true;
        }
      }
    }

    /**
     * Mix the bits of an integer.
     *
     * <p>This is the fast hash function used in the linear hash implementation in the <a
     * href="https://github.com/leventov/Koloboke">Koloboke Collections</a>.
     *
     * @param x the bits
     * @return the mixed bits
     */
    private static int mix(int x) {
      final int h = x * PHI;
      return h ^ (h >>> 16);
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public void clear() {
      size = 0;
      Arrays.fill(set, 0);
    }

    @Override
    public IntStream intStream() {
      return StreamSupport.intStream(spliterator(), false);
    }

    @Override
    public OfInt spliterator() {
      return new SetSpliterator();
    }

    @Override
    public void forEach(IntConsumer action) {
      final int[] keys = set;
      int key;
      for (int i = keys.length; i-- != 0;) {
        if ((key = keys[i]) < 0) {
          action.accept(~key);
        }
      }
    }

    @Override
    public int[] toArray(int[] destination) {
      final int len = size;
      final int[] a = destination == null || destination.length < len ? new int[len] : destination;
      final int[] keys = set;
      int key;
      int c = 0;
      for (int i = keys.length; i-- != 0;) {
        if ((key = keys[i]) < 0) {
          a[c++] = ~key;
        }
      }
      return a;
    }

    /**
     * A spliterator over the set. If the set is resized after creation of the spliterator then the
     * behaviour is undefined.
     */
    private class SetSpliterator implements Spliterator.OfInt {
      /** The index of the next slot. */
      private int index;
      /** The upper bound of slots. */
      private final int bound;
      /** The count of elements returned. */
      private int count;
      /** Flag to indicate if a split has occurred to cover a subset. */
      private boolean subSet;

      /** Create an instance to iterate the entire set. */
      SetSpliterator() {
        bound = set.length;
      }

      /**
       * Create an instance to iterate part of the set.
       *
       * @param index the index
       * @param bound the bound
       */
      SetSpliterator(int index, int bound) {
        this.index = index;
        this.bound = bound;
        subSet = true;
      }

      @Override
      public long estimateSize() {
        if (subSet) {
          // Load factor of 0.5 through the range.
          // The upper limit is size - count.
          return Math.min(size - count, (bound - index) / 2);
        }
        return size - count;
      }

      @Override
      public int characteristics() {
        if (subSet) {
          return Spliterator.NONNULL | Spliterator.DISTINCT;
        }
        return Spliterator.SIZED | Spliterator.NONNULL | Spliterator.DISTINCT;
      }

      @Override
      public OfInt trySplit() {
        final int upper = bound;
        final int pos = index;
        if (pos >= upper) {
          return null;
        }
        final int len = (upper - pos) >> 1;
        if (len <= 1) {
          return null;
        }
        index = pos + len;
        subSet = true;
        return new SetSpliterator(pos, index);
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
        final int[] keys = set;
        int key;
        final int upper = bound;
        while (index < upper) {
          if ((key = keys[index++]) < 0) {
            count++;
            action.accept(~key);
            return true;
          }
        }
        return false;
      }

      @Override
      public void forEachRemaining(IntConsumer action) {
        Objects.requireNonNull(action, "action");
        final int[] keys = set;
        final int limit = bound;
        if (index < limit) {
          int key;
          do {
            if ((key = keys[index++]) < 0) {
              count++;
              action.accept(~key);
            }
          } while (index < limit);
        }
      }
    }
  }

  /** No instances. */
  private IndexSets() {}

  /**
   * Creates an index set to hold the expected number of unique indices.
   *
   * <p>Warning: The returned implementation will support expansion beyond the expected number of
   * indices but there is no guarantee that the returned implementation will be able to hold all
   * positive indices. Note that a {@link BitSet} can store all indices using 2<sup>25</sup> long
   * values, or 32MiB of storage. An expected size above 2<sup>25</sup> will use a BitSet backed
   * implementation. Smaller sizes use a hash table with a backing array limited to 2<sup>30</sup>
   * for the table size and a load factor of 0.5 for a maximum number of indices of 2<sup>29</sup>.
   *
   * @param expected the expected number of indices
   * @return the index set
   * @throws IllegalArgumentException if expected is negative
   * @see #create(int, int)
   */
  public static IndexSet create(int expected) {
    ValidationUtils.checkPositive(expected, "expected");
    // The HashIndexSet has a load factor of 2. Above 2^25 the BitSet has optimal memory usage.
    if (expected >= (1 << 25)) {
      // Optimal memory
      return new BitSetIndexSet(expected);
    }
    return new HashIndexSet(expected);
  }

  /**
   * Creates an index set to hold the expected number of unique indices within the provided maximum
   * value.
   *
   * <p>Warning: The returned implementation will support expansion beyond the expected number of
   * indices and maximum value but there is no guarantee that the returned implementation will be
   * able to hold all positive indices.
   *
   * @param expected the expected number of indices
   * @param max the maximum index (inclusive)
   * @return the index set
   * @throws IllegalArgumentException if expected or max are negative
   * @see #create(int)
   */
  public static IndexSet create(int expected, int max) {
    ValidationUtils.checkPositive(expected, "expected");
    ValidationUtils.checkPositive(max, "max");
    // Optimise based on saturation.
    // Once the BitSet has 2 non-zero bits in each long in the entire array
    // then it is optimal for storage.
    // if expected / 2 > (number of longs)
    // where (number of longs) = 1 + max / 64
    if (expected >= (1 << 25) || (expected >> 1) >= (1 + (max >> 6))) {
      // The capacity is the maximum index
      return new BitSetIndexSet(Math.max(expected, max));
    }
    return new HashIndexSet(expected);
  }
}
