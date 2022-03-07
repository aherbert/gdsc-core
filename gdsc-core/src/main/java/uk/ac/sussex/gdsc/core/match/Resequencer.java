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

package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.OpenHashMaps.CustomInt2IntOpenHashMap;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;
import uk.ac.sussex.gdsc.core.utils.function.IntIntConsumer;

/**
 * Provide methods to re-number a sequence of identifiers.
 */
public class Resequencer {

  /** The value for no entry in a map of sequence ids. */
  static final int NO_ENTRY = -1;

  /** Used to clear the cached map. */
  private static final IntMap NO_MAP = null;

  /** The default switch point to use a dynamic set. */
  private static final int DEFAULT_SWITCH_POINT = 65535;

  /** The switch point to use a dynamic set. */
  private int switchPoint = DEFAULT_SWITCH_POINT;

  /** The int map used for the last renumber operation. */
  private IntMap intMap;

  /** The cache map flag. */
  private boolean cacheMap;

  /**
   * Simple interface for a map of integers.
   */
  private interface IntMap {
    /**
     * Compute the value if absent.
     *
     * @param key the key
     * @param mappingFunction the mapping function
     * @return the current (existing or computed) value
     */
    int computeIfAbsent(int key, IntUnaryOperator mappingFunction);

    /**
     * Checks if the map has the capacity to handle the given range.
     *
     * @param min the min
     * @param range the range
     * @return true, if successful
     */
    boolean resetForRange(int min, long range);

    /**
     * Gets the mapped key-value pairs.
     *
     * @param action the action
     */
    void forEach(IntIntConsumer action);
  }

  /**
   * A fixed size map of values.
   *
   * <p>This is intended to be extended with an offset.
   */
  private static class FixedIntMap implements IntMap {
    /** The map of observed values. */
    final int[] observed;

    /** The size. */
    int size;

    /**
     * Instantiates a new fixed int map.
     *
     * @param size the size
     */
    FixedIntMap(int size) {
      observed = new int[size];
      this.size = size;
      Arrays.fill(observed, NO_ENTRY);
    }

    @Override
    public int computeIfAbsent(int key, IntUnaryOperator mappingFunction) {
      // This deliberately does not use the offset from getOffset()
      // for efficiency. The method is overridden in the subclass.
      int current = observed[key];
      if (current == NO_ENTRY) {
        observed[key] = current = mappingFunction.applyAsInt(key);
      }
      return current;
    }

    @Override
    public boolean resetForRange(int min, long range) {
      if (observed.length >= range) {
        this.size = (int) range;
        setOffset(min);
        Arrays.fill(observed, 0, size, NO_ENTRY);
      }
      return false;
    }

    @Override
    public void forEach(IntIntConsumer action) {
      final int offset = getOffset();
      for (int i = 0; i < size; i++) {
        if (observed[i] != NO_ENTRY) {
          action.accept(i + offset, observed[i]);
        }
      }
    }

    /**
     * sets the offset to convert the key to an index.
     *
     * @param offset the new offset
     */
    protected void setOffset(int offset) {
      // This method is intended to be overridden
    }

    /**
     * Gets the offset to convert the index to a key.
     *
     * @return the offset
     */
    protected int getOffset() {
      // This method is intended to be overridden.
      // It is a convenience method for outputting the key-value pairs from the map.
      return 0;
    }
  }

  /**
   * A fixed size map with an offset for each value.
   */
  private static class OffsetIntMap extends FixedIntMap {

    /** The offset. */
    int offset;

    /**
     * Instantiates a new offset int map.
     *
     * @param size the size
     * @param offset the offset
     */
    OffsetIntMap(int size, int offset) {
      super(size);
      this.offset = offset;
    }

    @Override
    public int computeIfAbsent(int key, IntUnaryOperator mappingFunction) {
      return super.computeIfAbsent(key - offset, mappingFunction);
    }

    @Override
    protected void setOffset(int offset) {
      this.offset = offset;
    }

    @Override
    protected int getOffset() {
      return offset;
    }
  }

  /**
   * A dynamic set.
   */
  private static class DynamicIntMap implements IntMap {
    /** The set of observed values. */
    final CustomInt2IntOpenHashMap observed;

    /**
     * Instantiates a new dynamic int set.
     *
     * @param size the size
     */
    DynamicIntMap(int size) {
      observed = new CustomInt2IntOpenHashMap(size);
      observed.defaultReturnValue(NO_ENTRY);
    }

    @Override
    public int computeIfAbsent(int key, IntUnaryOperator mappingFunction) {
      return observed.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public boolean resetForRange(int min, long range) {
      observed.clear();
      return true;
    }

    @Override
    public void forEach(IntIntConsumer action) {
      observed.forEach(action);
    }
  }

  /**
   * Renumber the set of identifiers so that it contains numbers from 0 to n-1 where n is the number
   * of distinct identifiers in the set. Matched identifiers will be preserved, for example:
   *
   * <pre>
   * {@code
   * [0,1,1,2]    => [0,1,1,2]
   * [1,1,0,2]    => [0,0,1,2]
   * [-8,-8,4,16] => [0,0,1,2]
   * }
   * </pre>
   *
   * <p>This method can be used to compact identifiers into a minimum representation that can be
   * safely used as array indexes.
   *
   * <p>For more details see {@link #renumber(int[], int[])}.
   *
   * @param set the set (modified in place)
   * @return the number of distinct identifiers (n)
   * @throws IllegalArgumentException If the number of clusters does not fit in the range of an
   *         integer
   */
  public int renumber(int[] set) {
    return renumber(set, set);
  }

  /**
   * Renumber the set of identifiers so that it contains numbers from 0 to n-1 where n is the number
   * of distinct identifiers in the set. Matched identifiers will be preserved, for example:
   *
   * <pre>
   * {@code
   * [0,1,1,2]    => [0,1,1,2]
   * [1,1,0,2]    => [0,0,1,2]
   * [-8,-8,4,16] => [0,0,1,2]
   * }
   * </pre>
   *
   * <p>This method can be used to compact identifiers into a minimum representation that can be
   * safely used as array indexes.
   *
   * <p>The algorithm uses a single pass over the input with a map to store the new identifiers. The
   * map chosen depends on the range of identifiers. If this is above the configured switch point
   * then a dynamic map is used.
   *
   * <p>The map can be retrieved if explicitly set to be cached.
   *
   * @param set the set
   * @param outputSet the output set
   * @return the number of distinct identifiers (n)
   * @throws IllegalArgumentException If input and output set are not the same length
   * @see #setSwitchPoint(int)
   * @see #setCacheMap(boolean)
   * @see #forEach(IntIntConsumer)
   */
  public int renumber(int[] set, int[] outputSet) {
    ValidationUtils.checkArgument(set.length == outputSet.length,
        "Input and Output set must have the same length");

    // Edge cases
    if (set.length == 0) {
      return renumber0();
    }
    if (set.length == 1) {
      return renumber1(set, outputSet);
    }
    if (set.length == 2) {
      return renumber2(set, outputSet);
    }

    final IntMap observed = createMap(set);

    int[] nextId = {0};
    for (int i = 0; i < set.length; i++) {
      outputSet[i] = observed.computeIfAbsent(set[i], j -> nextId[0]++);
    }

    if (isCacheMap()) {
      intMap = observed;
    }

    return nextId[0];
  }

  /**
   * Renumber a set of length 0.
   *
   * @return the number of distinct identifiers (0)
   */
  private int renumber0() {
    if (isCacheMap()) {
      intMap = createMap(0, 0);
    }
    return 0;
  }

  /**
   * Renumber a set of length 1.
   *
   * @param set the set
   * @param outputSet the output set
   * @return the number of distinct identifiers (1)
   */
  private int renumber1(int[] set, int[] outputSet) {
    if (isCacheMap()) {
      final int min = set[0];
      intMap = createMap(min, 1);
      intMap.computeIfAbsent(min, i -> 0);
    }
    outputSet[0] = 0;
    return 1;
  }

  /**
   * Renumber a set of length 2.
   *
   * @param set the set
   * @param outputSet the output set
   * @return the number of distinct identifiers (1 or 2)
   */
  private int renumber2(int[] set, int[] outputSet) {
    // If the Ids are currently different then use 2 Ids.
    final int id1 = (set[0] == set[1]) ? 0 : 1;
    if (isCacheMap()) {
      final int min = Math.min(set[0], set[1]);
      intMap = createMap(min, 2);
      intMap.computeIfAbsent(set[0], i -> 0);
      intMap.computeIfAbsent(set[1], i -> id1);
    }
    outputSet[0] = 0;
    outputSet[1] = id1;
    return id1 + 1;
  }

  /**
   * Creates the map to store the range of identifiers.
   *
   * @param min the min
   * @param range the range
   * @return the int map
   */
  private IntMap createMap(int min, long range) {
    if (intMap != null && intMap.resetForRange(min, range)) {
      return intMap;
    }

    if (range < getSwitchPoint()) {
      final int size = (int) range;
      return (min == 0) ? new FixedIntMap(size) : new OffsetIntMap(size, min);
    }
    // The input range may be very large. Let the set expand dynamically.
    return new DynamicIntMap((int) Math.min(range, DEFAULT_SWITCH_POINT));
  }

  /**
   * Creates the map.
   *
   * @param set the set of identifiers
   * @return the int map
   */
  private IntMap createMap(int[] set) {
    // Overflow safe range computation
    final int[] limits = MathUtils.limits(set);
    final int min = limits[0];
    final int max = limits[1];
    final long range = (long) max - min + 1;
    return createMap(min, range);
  }

  /**
   * Apply the given action to the mapped key-value pairs from the last call to
   * {@link #renumber(int[], int[])}.
   *
   * <p>{@code key} is the original identifier. {@code value} is the new identifier.
   *
   * <p>This requires that the map has been cached (which is disabled by default).
   * If the map is not available then the method returns immediately and the action is not
   * used.
   *
   * @param action the action
   * @see #setCacheMap(boolean)
   */
  void forEach(IntIntConsumer action) {
    if (intMap != null) {
      intMap.forEach(action);
    }
  }

  /**
   * Gets the switch point for the algorithm to use a dynamic hash map algorithm.
   *
   * @return the switch point
   */
  public int getSwitchPoint() {
    return switchPoint;
  }

  /**
   * Sets the switch point for the algorithm to use a dynamic hash map algorithm.
   *
   * <p>Small ranges of sequence identifiers can be handled using a fixed size hash map. This option
   * is used to control the switch point to a dynamic hash map.
   *
   * @param switchPoint the new switch point
   */
  public void setSwitchPoint(int switchPoint) {
    this.switchPoint = Math.min(0, switchPoint);
  }

  /**
   * Checks the map of the identifiers will be cached. The default is false.
   *
   * @return true to cache the map
   */
  public boolean isCacheMap() {
    return cacheMap;
  }

  /**
   * Set if the map of the identifiers will be cached. The default is false.
   *
   * <p>The map can be reused to improve efficiency at the expense of memory overhead. It is also
   * required to allow the map to be retrieved from the method {@link #forEach(IntIntConsumer)}.
   *
   * @param cacheMap the new cache map flag
   */
  public void setCacheMap(boolean cacheMap) {
    if (!cacheMap) {
      intMap = NO_MAP;
    }
    this.cacheMap = cacheMap;
  }
}
