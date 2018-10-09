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


import java.awt.geom.Rectangle2D;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import uk.ac.sussex.gdsc.core.utils.ConvexHull;
import uk.ac.sussex.gdsc.core.utils.Maths;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Contains the result of the DBSCAN algorithm
 */
public class DBSCANResult implements ClusteringResult {
  /**
   * Used to provide access to the raw coordinates
   */
  private final OPTICSManager opticsManager;

  /**
   * A result not part of any cluster
   */
  public static final int NOISE = 0;

  /**
   * The min points for a core object
   */
  public final int minPts;
  /**
   * The generating distance for a core object
   */
  public final float generatingDistance;

  /**
   * The order results
   */
  final DBSCANOrder[] results;

  /**
   * Clusters assigned by extractClusters(...)
   */
  private int[] clusters = null;

  /**
   * Convex hulls assigned by computeConvexHulls()
   */
  private ConvexHull[] hulls = null;

  /**
   * Bounds assigned by computeConvexHulls()
   */
  private Rectangle2D[] bounds = null;

  /**
   * Instantiates a new DBSCAN result.
   *
   * @param opticsManager the optics manager
   * @param minPts the min points
   * @param generatingDistance the generating distance
   * @param dbscanResults the DBSCAN results
   */
  DBSCANResult(OPTICSManager opticsManager, int minPts, float generatingDistance,
      DBSCANOrder[] dbscanResults) {
    this.opticsManager = opticsManager;
    this.minPts = minPts;
    this.generatingDistance = generatingDistance;
    this.results = dbscanResults;
  }

  /**
   * Get the number of results
   *
   * @return the number of results
   */
  public int size() {
    return results.length;
  }

  /**
   * Get the result.
   *
   * @param index the index
   * @return the DBSCAN result
   */
  public DBSCANOrder get(int index) {
    return results[index];
  }

  /**
   * Gets the DBSCAN order of the original input points.
   *
   * @return the order
   */
  public int[] getOrder() {
    final int[] data = new int[size()];
    for (int i = size(); i-- > 0;) {
      data[results[i].parent] = i + 1;
    }
    return data;
  }

  /** {@inheritDoc} */
  @Override
  public void scrambleClusters(RandomGenerator rng) {
    clusters = null;
    hulls = null;
    bounds = null;

    int max = 0;
    for (int i = size(); i-- > 0;) {
      if (max < results[i].clusterId) {
        max = results[i].clusterId;
      }
    }
    if (max == 0) {
      return;
    }

    final int[] map = SimpleArrayUtils.newArray(max, 1, 1);
    MathArrays.shuffle(map, rng);

    for (int i = size(); i-- > 0;) {
      if (results[i].clusterId > 0) {
        results[i].clusterId = map[results[i].clusterId - 1];
      }
    }
  }

  /**
   * Extract the clusters and store a reference to them for return by {@link #getClusters()}.
   * Deletes the cached convex hulls for previous clusters.
   *
   * @param core the core
   */
  public void extractClusters(boolean core) {
    clusters = getClusters(core);
    hulls = null;
    bounds = null;
  }

  /**
   * This can be set by {@link #extractClusters(boolean)}. <p> {@inheritDoc}
   *
   * @see uk.ac.sussex.gdsc.core.clustering.optics.ClusteringResult#getClusters()
   */
  @Override
  public int[] getClusters() {
    return clusters;
  }

  /**
   * Gets the cluster Id for each parent object.
   *
   * @param core Set to true to get the clusters using only the core points
   * @return the clusters
   */
  public int[] getClusters(boolean core) {
    final int[] clusters = new int[size()];
    if (core) {
      for (int i = size(); i-- > 0;) {
        if (results[i].nPts >= minPts) {
          final int id = results[i].parent;
          clusters[id] = results[i].clusterId;
        }
      }
    } else {
      for (int i = size(); i-- > 0;) {
        final int id = results[i].parent;
        clusters[id] = results[i].clusterId;
      }
    }
    return clusters;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasConvexHulls() {
    return hulls != null;
  }

  /** {@inheritDoc} */
  @Override
  public void computeConvexHulls() {
    if (hasConvexHulls()) {
      return;
    }

    if (clusters == null) {
      return;
    }

    // Get the number of clusters
    final int nClusters = Maths.max(clusters);
    hulls = new ConvexHull[nClusters];
    bounds = new Rectangle2D[nClusters];

    // Descend the hierarchy and compute the hulls, smallest first
    final ScratchSpace scratch = new ScratchSpace(100);
    for (int clusterId = 1; clusterId <= nClusters; clusterId++) {
      computeConvexHull(clusterId, scratch);
    }
  }

  private void computeConvexHull(int clusterId, ScratchSpace scratch) {
    scratch.n = 0;
    for (int i = size(); i-- > 0;) {
      if (clusterId == clusters[i]) {
        scratch.safeAdd(opticsManager.getOriginalX(results[i].parent),
            opticsManager.getOriginalY(results[i].parent));
      }
    }

    bounds[clusterId - 1] = scratch.getBounds();

    // Compute the hull
    final ConvexHull h = scratch.getConvexHull();
    if (h != null) {
      hulls[clusterId - 1] = h;
    } else {
      // System.out.printf("No hull: n=%d\n", scratch.n);
      // for (int i = 0; i < scratch.n; i++)
      // System.out.printf("%d: %f,%f\n", i, scratch.x[i], scratch.y[i]);
    }
  }

  /** {@inheritDoc} */
  @Override
  public ConvexHull getConvexHull(int clusterId) {
    if (hulls == null || clusterId <= 0 || clusterId > hulls.length) {
      return null;
    }
    return hulls[clusterId - 1];
  }

  /** {@inheritDoc} */
  @Override
  public Rectangle2D getBounds(int clusterId) {
    if (bounds == null || clusterId <= 0 || clusterId > bounds.length) {
      return null;
    }
    return bounds[clusterId - 1];
  }

  /** {@inheritDoc} */
  @Override
  public int[] getParents(int[] clusterIds) {
    if (clusterIds == null) {
      return new int[0];
    }
    final TIntArrayList parents = new TIntArrayList();

    // Stupid implementation processes each cluster in turn.
    if (clusterIds.length == 1) {
      final int clusterId = clusterIds[0];
      for (int i = size(); i-- > 0;) {
        if (clusterId == clusters[i]) {
          parents.add(results[i].parent);
        }
      }
    } else {
      // Multiple clusters selected. Prevent double counting by
      // using a hash set to store each cluster we have processed
      final int nClusters = Maths.max(clusters);
      final TIntHashSet ids = new TIntHashSet(clusterIds.length);

      for (final int clusterId : clusterIds) {
        if (clusterId > 0 && clusterId <= nClusters) {
          if (ids.add(clusterId)) {
            for (int i = size(); i-- > 0;) {
              if (clusterId == clusters[i]) {
                parents.add(results[i].parent);
              }
            }
          }
        }
      }
    }

    return parents.toArray();
  }
}
