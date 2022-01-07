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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.trees;

/**
 * Constants representing the status of a node during the running of a search.
 *
 * <p>This is deliberately not an enum to minimise memory overhead when storing an array of the
 * status. An enum would be a class instance and may be larger than 32-bits per value depending on
 * the JVM platform.
 *
 * @since 2.0
 */
final class Status {
  /** Status indicating that neither child has been visited. */
  static final byte NONE = 0x0;
  /** Status indicating that the left has been visited. */
  static final byte LEFTVISITED = 0x1;
  /** Status indicating that the right has been visited. */
  static final byte RIGHTVISITED = 0x2;
  /** Status indicating that both the left and the right have been visited. */
  static final byte ALLVISITED = 0x3;

  /** No instances. */
  private Status() {}
}
