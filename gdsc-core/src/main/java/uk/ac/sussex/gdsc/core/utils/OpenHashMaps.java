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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.ObjIntConsumer;
import uk.ac.sussex.gdsc.core.utils.function.IntIntConsumer;
import uk.ac.sussex.gdsc.core.utils.function.IntObjConsumer;
import uk.ac.sussex.gdsc.core.utils.function.LongIntConsumer;

/**
 * Provide extension classes for the OpenHashMap implementation in the <a
 * href="https://github.com/vigna/fastutil">fastutil collections library</a>.
 *
 * <p>The extensions provide zero-allocation forEach iteration of the entries in the map.
 *
 * @deprecated Functionality is now available in the fastutil OpenHashMap implementations.
 */
@Deprecated
public final class OpenHashMaps {
  /**
   * Customisation of Long2IntOpenHashMap.
   * 
   * @deprecated Functionality is now available in the fastutil OpenHashMap implementations.
   */
  @Deprecated
  public static class CustomLong2IntOpenHashMap extends Long2IntOpenHashMap {
    private static final long serialVersionUID = 1L;

    /**
     * Create an instance.
     */
    public CustomLong2IntOpenHashMap() {
      super();
    }

    /**
     * Create an instance.
     *
     * @param expected the expected number of entries
     */
    public CustomLong2IntOpenHashMap(int expected) {
      super(expected);
    }

    /**
     * Perform the action on each entry in the map.
     *
     * @param action the action
     */
    public void forEach(LongIntConsumer action) {
      // Delegate
      super.forEach(action::accept);
    }
  }

  /**
   * Customisation of Int2IntOpenHashMap.
   * 
   * @deprecated Functionality is now available in the fastutil OpenHashMap implementations.
   */
  @Deprecated
  public static class CustomInt2IntOpenHashMap extends Int2IntOpenHashMap {
    private static final long serialVersionUID = 1L;

    /**
     * Create an instance.
     */
    public CustomInt2IntOpenHashMap() {
      super();
    }

    /**
     * Create an instance.
     *
     * @param expected the expected number of entries
     */
    public CustomInt2IntOpenHashMap(int expected) {
      super(expected);
    }

    /**
     * Perform the action on each entry in the map.
     *
     * @param action the action
     */
    public void forEach(IntIntConsumer action) {
      // Delegate
      super.forEach(action::accept);
    }
  }

  /**
   * Customisation of Int2ObjectOpenHashMap.
   *
   * @param <T> the element type
   * @deprecated Functionality is now available in the fastutil OpenHashMap implementations.
   */
  @Deprecated
  public static class CustomInt2ObjectOpenHashMap<T> extends Int2ObjectOpenHashMap<T> {
    private static final long serialVersionUID = 1L;

    /**
     * Create an instance.
     */
    public CustomInt2ObjectOpenHashMap() {
      super();
    }

    /**
     * Create an instance.
     *
     * @param expected the expected number of entries
     */
    public CustomInt2ObjectOpenHashMap(int expected) {
      super(expected);
    }

    /**
     * Perform the action on each entry in the map.
     *
     * @param action the action
     */
    public void forEach(IntObjConsumer<T> action) {
      // Delegate
      super.forEach(action::accept);
    }
  }

  /**
   * Customisation of Object2IntOpenHashMap.
   *
   * @param <T> the element type
   * @deprecated Functionality is now available in the fastutil OpenHashMap implementations.
   */
  @Deprecated
  public static class CustomObject2IntOpenHashMap<T> extends Object2IntOpenHashMap<T> {
    private static final long serialVersionUID = 1L;

    /**
     * Create an instance.
     */
    public CustomObject2IntOpenHashMap() {
      super();
    }

    /**
     * Create an instance.
     *
     * @param expected the expected number of entries
     */
    public CustomObject2IntOpenHashMap(int expected) {
      super(expected);
    }

    /**
     * Perform the action on each entry in the map.
     *
     * @param action the action
     */
    public void forEach(ObjIntConsumer<T> action) {
      // Delegate
      super.forEach(action::accept);
    }
  }

  /** No public construction. */
  private OpenHashMaps() {}
}
