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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import java.util.Collection;

/**
 * Utility class for working with {@link Collection}.
 */
public final class CollectionUtils {

  /** No public construction. */
  private CollectionUtils() {}

  /**
   * Check if the {@link Collection#contains(Object)} method is true for any of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @return true if any of the elements are in the collection
   */
  public static <T> boolean containsAny(Collection<T> collection, T element1, T element2) {
    return collection.contains(element1) || collection.contains(element2);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for any of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @param element3 the third element
   * @return true if any of the elements are in the collection
   */
  public static <T> boolean containsAny(Collection<T> collection, T element1, T element2,
      T element3) {
    return collection.contains(element1) || collection.contains(element2)
        || collection.contains(element3);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for any of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @param element3 the third element
   * @param element4 the fourth element
   * @return true if any of the elements are in the collection
   */
  public static <T> boolean containsAny(Collection<T> collection, T element1, T element2,
      T element3, T element4) {
    return collection.contains(element1) || collection.contains(element2)
        || collection.contains(element3) || collection.contains(element4);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for any of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @param element3 the third element
   * @param element4 the fourth element
   * @param element5 the fifth element
   * @return true if any of the elements are in the collection
   */
  public static <T> boolean containsAny(Collection<T> collection, T element1, T element2,
      T element3, T element4, T element5) {
    return collection.contains(element1) || collection.contains(element2)
        || collection.contains(element3) || collection.contains(element4)
        || collection.contains(element5);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for any of the elements.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param first the first element
   * @param rest the rest
   * @return true if any of the elements are in the collection
   */
  @SafeVarargs
  public static <T> boolean containsAny(Collection<T> collection, T first, T... rest) {
    if (collection.contains(first)) {
      return true;
    }
    for (T element : rest) {
      if (collection.contains(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for all of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @return true if all of the elements are in the collection
   */
  public static <T> boolean containsAll(Collection<T> collection, T element1, T element2) {
    return collection.contains(element1) && collection.contains(element2);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for all of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @param element3 the third element
   * @return true if all of the elements are in the collection
   */
  public static <T> boolean containsAll(Collection<T> collection, T element1, T element2,
      T element3) {
    return collection.contains(element1) && collection.contains(element2)
        && collection.contains(element3);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for all of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @param element3 the third element
   * @param element4 the fourth element
   * @return true if all of the elements are in the collection
   */
  public static <T> boolean containsAll(Collection<T> collection, T element1, T element2,
      T element3, T element4) {
    return collection.contains(element1) && collection.contains(element2)
        && collection.contains(element3) && collection.contains(element4);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for all of the arguments.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param element1 the first element
   * @param element2 the second element
   * @param element3 the third element
   * @param element4 the fourth element
   * @param element5 the fifth element
   * @return true if all of the elements are in the collection
   */
  public static <T> boolean containsAll(Collection<T> collection, T element1, T element2,
      T element3, T element4, T element5) {
    return collection.contains(element1) && collection.contains(element2)
        && collection.contains(element3) && collection.contains(element4)
        && collection.contains(element5);
  }

  /**
   * Check if the {@link Collection#contains(Object)} method is true for all of the elements.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @param first the first element
   * @param rest the rest of the elements
   * @return true if all of the elements are in the collection
   */
  @SafeVarargs
  public static <T> boolean containsAll(Collection<T> collection, T first, T... rest) {
    if (collection.contains(first)) {
      for (T element : rest) {
        if (!collection.contains(element)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Get the size of the collection.
   *
   * <p>Returns {@code 0} if the collection is null.
   *
   * @param collection the collection
   * @return the size
   * @see Collection#size()
   */
  public static int getSize(Collection<?> collection) {
    return (collection != null) ? collection.size() : 0;
  }
}
