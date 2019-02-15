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

package uk.ac.sussex.gdsc.core.match;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares the {@link Pulse} class using only time.
 */
public class PulseTimeComparator implements Comparator<Pulse>, Serializable {
  /**
   * The serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /** An instance. */
  private static final PulseTimeComparator INSTANCE = new PulseTimeComparator();

  /**
   * Gets an instance.
   *
   * @return an instance
   */
  public static PulseTimeComparator getInstance() {
    return INSTANCE;
  }

  @Override
  public int compare(Pulse o1, Pulse o2) {
    if (o1.getStart() == o2.getStart()) {
      return Integer.compare(o1.getEnd(), o2.getEnd());
    }
    return (o1.getStart() < o2.getStart()) ? -1 : 1;
  }
}
