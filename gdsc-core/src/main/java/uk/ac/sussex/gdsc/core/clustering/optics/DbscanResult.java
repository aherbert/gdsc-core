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

package uk.ac.sussex.gdsc.core.clustering.optics;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import uk.ac.sussex.gdsc.core.math.hull.Hull;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;

/**
 * Contains the result of the DBSCAN algorithm.
 */
public class DbscanResult implements ClusteringResult {

  /**
   * A result not part of any cluster.
   */
  public static final int NOISE = 0;

  /**
   * Used to provide access to the raw coordinates.
   */
  private final OpticsManager opticsManager;

  /**
   * The minimum points for a core object.
   */
  private final int minPoints;
  /**
   * The generating distance for a core object.
   */
  private final float generatingDistance;

  /**
   * The order results.
   */
  final DbscanOrder[] results;

  /**
   * Clusters assigned by extractClusters(...)
   */
  private int[] clusters;

  /**
   * Convex hulls assigned by computeConvexHulls().
   */
  private Hull[] hulls;

  /**
   * Bounds assigned by computeConvexHulls().
   */
  private float[][] bounds;

  /**
   * Instantiates a new DBSCAN result.
   *
   * @param opticsManager the optics manager
   * @param minPoints the min points
   * @param generatingDistance the generating distance
   * @param dbscanResults the DBSCAN results
   */
  DbscanResult(OpticsManager opticsManager, int minPoints, float generatingDistance,
      DbscanOrder[] dbscanResults) {
    this.opticsManager = opticsManager;
    this.minPoints = minPoints;
    this.generatingDistance = generatingDistance;
    this.results = dbscanResults;
  }

  /**
   * Get the number of results.
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
  public DbscanOrder get(int index) {
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

  @Override
  public void scrambleClusters(UniformRandomProvider rng) {
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
    RandomUtils.shuffle(map, rng);

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
   * {@inheritDoc}
   *
   * <p>This can be set by {@link #extractClusters(boolean)}. Otherwise it is null.
   *
   * @see uk.ac.sussex.gdsc.core.clustering.optics.ClusteringResult#getClusters()
   */
  @Override
  public int[] getClusters() {
    return ArrayUtils.clone(clusters);
  }

  /**
   * Gets the cluster Id for each parent object.
   *
   * @param core Set to true to get the clusters using only the core points
   * @return the clusters
   */
  public int[] getClusters(boolean core) {
    final int[] newClusters = new int[size()];
    if (core) {
      for (int i = size(); i-- > 0;) {
        if (results[i].numberOfPoints >= getMinPoints()) {
          final int id = results[i].parent;
          newClusters[id] = results[i].clusterId;
        }
      }
    } else {
      for (int i = size(); i-- > 0;) {
        final int id = results[i].parent;
        newClusters[id] = results[i].clusterId;
      }
    }
    return newClusters;
  }

  @Override
  public boolean hasHulls() {
    return hulls != null;
  }

  @Override
  public void computeHulls(Hull.Builder builder) {
    if (clusters == null) {
      return;
    }

    // Get the number of clusters
    final int nClusters = MathUtils.max(clusters);
    hulls = new Hull[nClusters];
    bounds = new float[nClusters][];

    // Special 2D/3D processing
    IntConsumer processor;
    Supplier<float[]> boundsGenerator;
    if (opticsManager.is3d()) {
      final MinMax3d mm = new MinMax3d();
      final float[] z = opticsManager.getZData();
      processor = i -> {
        final float x = opticsManager.getOriginalX(results[i].parent);
        final float y = opticsManager.getOriginalY(results[i].parent);
        builder.add(x, y, z[i]);
        mm.add(x, y, z[i]);
      };
      boundsGenerator = () -> {
        final float[] bounds = mm.getBounds();
        mm.clear();
        return bounds;
      };
    } else {
      final MinMax2d mm = new MinMax2d();
      processor = i -> {
        final float x = opticsManager.getOriginalX(results[i].parent);
        final float y = opticsManager.getOriginalY(results[i].parent);
        builder.add(x, y);
        mm.add(x, y);
      };
      boundsGenerator = () -> {
        final float[] bounds = mm.getBounds();
        mm.clear();
        return bounds;
      };
    }

    for (int clusterId = 1; clusterId <= nClusters; clusterId++) {
      builder.clear();
      computeHull(processor, clusterId);
      hulls[clusterId - 1] = builder.build();
      bounds[clusterId - 1] = boundsGenerator.get();
    }
  }

  private void computeHull(IntConsumer processor, int clusterId) {
    for (int i = size(); i-- > 0;) {
      if (clusterId == clusters[i]) {
        processor.accept(i);
      }
    }
  }

  @Override
  public Hull getHull(int clusterId) {
    if (hulls == null || clusterId <= 0 || clusterId > hulls.length) {
      return null;
    }
    return hulls[clusterId - 1];
  }

  @Override
  public float[] getBounds(int clusterId) {
    if (bounds == null || clusterId <= 0 || clusterId > bounds.length) {
      return null;
    }
    return bounds[clusterId - 1];
  }

  @Override
  public int[] getParents(int[] clusterIds) {
    if (clusterIds == null) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
    final TIntArrayList parents = new TIntArrayList();

    if (clusterIds.length == 1) {
      getParentsFromSingleCluster(clusterIds[0], parents);
    } else {
      getParentsFromMultipleClusters(clusterIds, parents);
    }

    return parents.toArray();
  }

  private void getParentsFromSingleCluster(int clusterId, final TIntArrayList parents) {
    if (clusterId > 0) {
      for (int i = size(); i-- > 0;) {
        if (clusterId == clusters[i]) {
          parents.add(results[i].parent);
        }
      }
    }
  }

  private void getParentsFromMultipleClusters(int[] clusterIds, final TIntArrayList parents) {
    // Multiple clusters selected. Prevent double counting by
    // using a hash set to store each cluster we have processed.
    final int nClusters = MathUtils.max(clusters);
    final TIntHashSet ids = new TIntHashSet(clusterIds.length);

    for (final int clusterId : clusterIds) {
      if (clusterId > 0 && clusterId <= nClusters && ids.add(clusterId)) {
        // Stupid implementation processes each new cluster in turn.
        for (int i = size(); i-- > 0;) {
          if (clusterId == clusters[i]) {
            parents.add(results[i].parent);
          }
        }
      }
    }
  }

  /**
   * Gets the minimum points for a core object.
   *
   * @return the minimum points for a core object.
   */
  public int getMinPoints() {
    return minPoints;
  }

  /**
   * Gets the generating distance for a core object.
   *
   * @return the generating distance for a core object.
   */
  public float getGeneratingDistance() {
    return generatingDistance;
  }
}
