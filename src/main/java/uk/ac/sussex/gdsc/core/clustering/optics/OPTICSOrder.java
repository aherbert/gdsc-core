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
package uk.ac.sussex.gdsc.core.clustering.optics;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


/**
 * Contains the ordered result of the OPTICS algorithm
 */
public class OPTICSOrder {
  /**
   * The Id of the parent point object used when generating this result. Can be used to identify the
   * coordinates from the original input data.
   */
  public final int parent;
  /**
   * The Id of the point that set the reachability distance.
   */
  public final int predecessor;
  /**
   * The cluster identifier. This may be modified if clustering is re-performed by OPTICS.
   */
  int clusterId;

  /**
   * Gets the cluster id.
   *
   * @return the cluster id
   */
  public int getClusterId() {
    return clusterId;
  }

  /**
   * The core distance. Set to positive infinity if not a core point.
   */
  public final double coreDistance;
  /**
   * The reachability distance. Set to positive infinity if not a reachable point, or the first core
   * point of a new grouping.
   */
  public final double reachabilityDistance;

  /**
   * Instantiates a new OPTICS order result.
   *
   * @param parent the parent
   * @param predecessor the predecessor
   * @param coreDistance the core distance
   * @param reachabilityDistance the reachability distance
   */
  public OPTICSOrder(int parent, int predecessor, double coreDistance,
      double reachabilityDistance) {
    this.parent = parent;
    this.predecessor = predecessor;
    this.coreDistance = coreDistance;
    this.reachabilityDistance = reachabilityDistance;
  }

  /**
   * Checks if is core point.
   *
   * @return true, if is core point
   */
  public boolean isCorePoint() {
    return coreDistance != Double.POSITIVE_INFINITY;
  }

  /**
   * Checks if is a reachable point. <p> Note that the first core point of a new grouping will not
   * be labelled as a reachable point as it has no reachability distance.
   *
   * @return true, if is reachable point
   */
  public boolean isReachablePoint() {
    return reachabilityDistance != Double.POSITIVE_INFINITY;
  }
}
