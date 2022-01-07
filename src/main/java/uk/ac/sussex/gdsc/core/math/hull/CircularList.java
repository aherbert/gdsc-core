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

package uk.ac.sussex.gdsc.core.math.hull;

import java.util.function.IntConsumer;

/**
 * A class to maintain a circular list of {@code int} values. This list can never be empty.
 *
 * @since 2.0
 */
final class CircularList {
  /**
   * A node in the circular list.
   */
  private class Node {
    /** The value. */
    int value;
    /** The next node. */
    Node next;
    /** The previous node. */
    Node prev;

    /**
     * Create an instance.
     *
     * @param value the value
     */
    Node(int value) {
      this.value = value;
    }
  }

  /**
   * Represents an operation that accepts a two {@code int}-valued arguments and returns no result.
   * This is the primitive type specialization of {@link java.util.function.BiConsumer} for
   * {@code int}. Unlike most other functional interfaces, {@code IntIntConsumer} is expected to
   * operate via side-effects.
   *
   * @see java.util.function.BiConsumer BiConsumer
   */
  @FunctionalInterface
  interface IntIntConsumer {
    /**
     * Performs this operation on the given argument.
     *
     * @param value1 the first input argument
     * @param value2 the second input argument
     */
    void accept(int value1, int value2);
  }

  /** The current node. */
  private Node current;

  /** The saved node. */
  private Node mark;

  /** The size. */
  private int size;

  /**
   * Create an instance.
   *
   * @param value the value
   */
  CircularList(int value) {
    current = new Node(value);
    current.next = current.prev = current;
    size = 1;
    mark();
  }

  /**
   * Get the current value.
   *
   * @return the current value.
   */
  int current() {
    return current.value;
  }

  /**
   * Advance to the next value in the list. The returned value is the new current value, or the
   * value following the old current value.
   *
   * @return the new current value
   */
  int next() {
    current = current.next;
    return current.value;
  }

  /**
   * Advance to the previous value in the list. The returned value is the new current value, or the
   * value preceding the old current value.
   *
   * @return the new current value.
   */
  int previous() {
    current = current.prev;
    return current.value;
  }

  /**
   * Peek at the value offset from the current value. The offset is positive to peek at future
   * values; negative to peek at previous values; or zero for the current value.
   *
   * @param offset the offset
   * @return the value
   */
  int peek(int offset) {
    Node node = current;
    int steps = offset;
    if (offset < 0) {
      while (steps++ != 0) {
        node = node.prev;
      }
    } else {
      while (steps-- != 0) {
        node = node.next;
      }
    }
    return node.value;
  }

  /**
   * Advance to the first occurrence of the specified value. If unsuccessful the current value is
   * unchanged.
   *
   * @param value the value
   * @return true if the value was found
   */
  boolean advanceTo(int value) {
    final Node head = current;
    Node node = head;
    do {
      if (node.value == value) {
        current = node;
        return true;
      }
      node = node.next;
    } while (node != head);
    return false;
  }

  /**
   * Insert the value after the current value. The newly inserted value is set to the current value.
   *
   * @param value the value
   */
  void insertAfter(int value) {
    final Node node = new Node(value);
    node.next = current.next;
    node.prev = current;
    current.next = node;
    node.next.prev = node;
    current = node;
    size++;
  }

  /**
   * Mark the current position in the list. A subsequent call to {@link #reset()} will return to
   * this position.
   */
  void mark() {
    mark = current;
  }

  /**
   * Reset the list to the position set by the last call to {@link #mark()}. If {@link #mark()} has
   * not been called then the position will be the initial position in the list.
   */
  void reset() {
    current = mark;
  }

  /**
   * Gets the size.
   *
   * @return the size
   */
  int size() {
    return size;
  }

  /**
   * Perform the action for each value in the list starting from the current value.
   *
   * @param action the action
   */
  void forEach(IntConsumer action) {
    // List is never empty so current is not null.
    final Node head = current;
    Node node = head;
    do {
      action.accept(node.value);
      node = node.next;
    } while (node != head);
  }

  /**
   * Perform the action for each value and its successor in the list starting from the current value
   * (i.e. current and next).
   *
   * @param action the action
   */
  void forEach(IntIntConsumer action) {
    // List is never empty so current is not null.
    final Node head = current;
    Node node = head;
    do {
      action.accept(node.value, node.next.value);
      node = node.next;
    } while (node != head);
  }
}
