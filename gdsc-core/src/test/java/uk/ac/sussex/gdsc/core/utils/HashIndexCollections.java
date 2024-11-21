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

import com.koloboke.collect.map.hash.HashIntByteMapFactory;
import com.koloboke.collect.map.hash.HashIntByteMaps;
import com.koloboke.collect.map.hash.HashIntCharMapFactory;
import com.koloboke.collect.map.hash.HashIntCharMaps;
import com.koloboke.collect.map.hash.HashIntDoubleMapFactory;
import com.koloboke.collect.map.hash.HashIntDoubleMaps;
import com.koloboke.collect.map.hash.HashIntFloatMapFactory;
import com.koloboke.collect.map.hash.HashIntFloatMaps;
import com.koloboke.collect.map.hash.HashIntIntMapFactory;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import com.koloboke.collect.map.hash.HashIntLongMapFactory;
import com.koloboke.collect.map.hash.HashIntLongMaps;
import com.koloboke.collect.map.hash.HashIntObjMapFactory;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import com.koloboke.collect.map.hash.HashIntShortMapFactory;
import com.koloboke.collect.map.hash.HashIntShortMaps;
import com.koloboke.collect.set.hash.HashIntSetFactory;
import com.koloboke.collect.set.hash.HashIntSets;

/**
 * Provides factories for the hash collections in the <a
 * href="https://github.com/leventov/Koloboke">Koloboke</a> collections library with a keys domain
 * optimised to store only index values. A valid index is any positive integer, including zero.
 *
 * <p>Note: This class exists as the Koloboke default implementation uses zero to store a not
 * present key value. If zero is entered into the table then the not present key must be changed
 * which incurs a performance overhead. The collection can be initialised with a keys domain for
 * positive integers and a suitable not present key that will avoid the rebuilding overhead.
 *
 * <p>The factories returned by this class are initialised with a key domain of [0,
 * Integer.MAX_VALUE]. The following are equivalent:
 *
 * <pre>
 * HashIntIntMap m1 = HashIndexCollections.getHashIntIntMapFactory().newUpdatableMap();
 * HashIntIntMap m2 =
 *     HashIntIntMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE).newUpdatableMap();
 * </pre>
 *
 * <p><strong>Note</strong>
 *
 * <p>The keys domain is a performance hint. It is not expected that the returned maps will prevent
 * insertion of negative valued keys. The class is tested to ensure that the default factory
 * collections in the dependency tree of the GDSC core library allow negative key insertion. However
 * the factory is loaded from the classpath using a service loader. If the default classpath
 * contains an implementation that validates the key domain then the returned map may exhibit this
 * behaviour.
 */
public final class HashIndexCollections {

  /** Lazy load the HashIntSetFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntSetFactoryHolder {
    /** Factory. */
    static final HashIntSetFactory FACTORY =
        HashIntSets.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntByteMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntByteMapFactoryHolder {
    /** Factory. */
    static final HashIntByteMapFactory FACTORY =
        HashIntByteMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntCharMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntCharMapFactoryHolder {
    /** Factory. */
    static final HashIntCharMapFactory FACTORY =
        HashIntCharMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntDoubleMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntDoubleMapFactoryHolder {
    /** Factory. */
    static final HashIntDoubleMapFactory FACTORY =
        HashIntDoubleMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntFloatMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntFloatMapFactoryHolder {
    /** Factory. */
    static final HashIntFloatMapFactory FACTORY =
        HashIntFloatMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntIntMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntIntMapFactoryHolder {
    /** Factory. */
    static final HashIntIntMapFactory FACTORY =
        HashIntIntMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntLongMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntLongMapFactoryHolder {
    /** Factory. */
    static final HashIntLongMapFactory FACTORY =
        HashIntLongMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /** Lazy load the HashIntIntMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntObjMapFactoryHolder {
    /** Factory. */
    @SuppressWarnings("rawtypes")
    static final HashIntObjMapFactory FACTORY =
        HashIntObjMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);

    /**
     * Get an instance.
     *
     * @param <V> the value type
     * @return the factory
     */
    @SuppressWarnings({"unchecked"})
    static <V> HashIntObjMapFactory<V> instance() {
      return FACTORY;
    }
  }

  /** Lazy load the HashIntShortMapFactory for the domain [0, Integer.MAX_VALUE]. */
  static final class HashIntShortMapFactoryHolder {
    /** Factory. */
    static final HashIntShortMapFactory FACTORY =
        HashIntShortMaps.getDefaultFactory().withKeysDomain(0, Integer.MAX_VALUE);
  }

  /**
   * No public construction.
   */
  private HashIndexCollections() {}

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntSetFactory getHashIntSetFactory() {
    return HashIntSetFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntByteMapFactory getHashIntByteMapFactory() {
    return HashIntByteMapFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntCharMapFactory getHashIntCharMapFactory() {
    return HashIntCharMapFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntDoubleMapFactory getHashIntDoubleMapFactory() {
    return HashIntDoubleMapFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntFloatMapFactory getHashIntFloatMapFactory() {
    return HashIntFloatMapFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntIntMapFactory getHashIntIntMapFactory() {
    return HashIntIntMapFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntLongMapFactory getHashIntLongMapFactory() {
    return HashIntLongMapFactoryHolder.FACTORY;
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @param <V> the most general value type of the maps that could be constructed by the returned
   *        factory
   * @return the factory
   */
  public static <V> HashIntObjMapFactory<V> getHashIntObjMapFactory() {
    return HashIntObjMapFactoryHolder.instance();
  }

  /**
   * Gets a factory optimised for positive integer keys. The keys domain is [0, Integer.MAX_VALUE].
   *
   * @return the factory
   */
  public static HashIntShortMapFactory getHashIntShortMapFactory() {
    return HashIntShortMapFactoryHolder.FACTORY;
  }
}
