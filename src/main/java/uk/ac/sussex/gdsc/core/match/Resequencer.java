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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provide methods to re-number a sequence of identifiers.
 */
public class Resequencer {

  /** The value for no entry in a map of sequence ids. */
  private static final int NO_ENTRY = -1;

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
     * Put the value into the map if absent.
     *
     * @param key the key
     * @param value the value
     * @return The previous value associated with key, or -1 if none was found
     */
    int putIfAbsent(int key, int value);

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
     * @return the map
     */
    List<int[]> getMap();
  }

  /**
   * A fixed size map of values.
   *
   * <p>This is intended to be extended with an offset and methods exist for
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
    public int putIfAbsent(int key, int value) {
      // This deliberately does not use the offset from getOffset()
      // for efficiency. The method is overridden in the subclass.
      final int current = observed[key];
      if (current == NO_ENTRY) {
        observed[key] = value;
        return NO_ENTRY;
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
    public List<int[]> getMap() {
      final ArrayList<int[]> list = new ArrayList<>();
      final int offset = getOffset();
      for (int i = 0; i < size; i++) {
        if (observed[i] != NO_ENTRY) {
          list.add(new int[] {i + offset, observed[i]});
        }
      }
      return list;
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
      // It is a convenience method for the outputting the key-value pairs from the map.
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
    public int putIfAbsent(int key, int value) {
      return super.putIfAbsent(key - offset, value);
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
    final TIntIntHashMap observed;

    /**
     * Instantiates a new dynamic int set.
     *
     * @param size the size
     */
    DynamicIntMap(int size) {
      observed = new TIntIntHashMap(size, 0.5f, 0, NO_ENTRY);
    }

    @Override
    public int putIfAbsent(int key, int value) {
      return observed.putIfAbsent(key, value);
    }

    @Override
    public boolean resetForRange(int min, long range) {
      observed.clear();
      return true;
    }

    @Override
    public List<int[]> getMap() {
      final ArrayList<int[]> list = new ArrayList<>();
      observed.forEachEntry((key, value) -> {
        list.add(new int[] {key, value});
        return true;
      });
      return list;
    }
  }

  /**
   * Renumber the set of identifiers so that it contains numbers from 0 to n-1 where n is the number
   * of distinct identifiers in the set. Matched identifiers will be preserved, for example:
   *
   * <pre>
   * [0,1,1,2]    => [0,1,1,2]
   * [1,1,0,2]    => [0,0,1,2]
   * [-8,-8,4,16] => [0,0,1,2]
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
   * [0,1,1,2]    => [0,1,1,2]
   * [1,1,0,2]    => [0,0,1,2]
   * [-8,-8,4,16] => [0,0,1,2]
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
   * @see #getRenumberMap()
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

    int nextId = 0;
    for (int i = 0; i < set.length; i++) {
      final int id = observed.putIfAbsent(set[i], nextId);
      if (id == NO_ENTRY) {
        // Was unmapped so consume the id
        outputSet[i] = nextId;
        // Can't overflow as the array can only go up to size Integer.MAX_VALUE
        nextId++;
      } else {
        outputSet[i] = id;
      }
    }

    if (isCacheMap()) {
      intMap = observed;
    }

    return nextId;
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
      int min = set[0];
      intMap = createMap(min, 1);
      intMap.putIfAbsent(min, 0);
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
    final int id1 = (set[0] != set[1]) ? 1 : 0;
    if (isCacheMap()) {
      int min = Math.min(set[0], set[1]);
      intMap = createMap(min, 2);
      intMap.putIfAbsent(set[0], 0);
      intMap.putIfAbsent(set[1], id1);
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
      int size = (int) range;
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
   * Gets the mapped key-value pairs from the last call to {@link #renumber(int[], int[])}.
   *
   * <p>{@code key} is the original identifier. {@code value} is the new identifier.
   *
   * <p>This requires that the map has been cached (which is disabled by default).
   *
   * @return the map (or null)
   * @see #setCacheMap(boolean)
   */
  public List<int[]> getRenumberMap() {
    // This is documented to return null when no map is available
    return (intMap == null) ? null : intMap.getMap();
  }

  /**
   * Gets the mapped value-key pairs from the last call to {@link #renumber(int[], int[])}.
   *
   * <p>{@code value} is the new identifier. {@code key} is the original identifier. Since the the
   * values are ascending from zero the map is returned using a single array where the index
   * corresponds to {@code value}.
   *
   * <p>This requires that the map has been cached (which is disabled by default).
   *
   * @return the map (or null)
   * @see #setCacheMap(boolean)
   */
  public int[] getRenumberInverseMap() {
    final List<int[]> pairs = getRenumberMap();
    if (pairs != null) {
      // Sort by the values
      Collections.sort(pairs, (p1, p2) -> Integer.compare(p1[1], p2[1]));
      // Extract keys
      int[] inverseMap = new int[pairs.size()];
      for (int j = 0; j < inverseMap.length; j++) {
        int[] pair = pairs.get(j);
        inverseMap[pair[1]] = pair[0];
      }
      return inverseMap;
    }
    // This is documented to return null when no map is available
    return null;
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
   * @param cacheMap the new cache map flag
   */
  public void setCacheMap(boolean cacheMap) {
    if (!cacheMap) {
      intMap = NO_MAP;
    }
    this.cacheMap = cacheMap;
  }
}
