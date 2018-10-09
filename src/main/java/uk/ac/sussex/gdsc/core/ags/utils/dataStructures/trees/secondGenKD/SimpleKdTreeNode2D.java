/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller memory footprint and
 * optimised 2D processing for use with image data as part of the Genome Damage and Stability Centre
 * ImageJ Core Package.
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event will the
 * authors be held liable for any damages arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose, including commercial
 * applications, and to alter it and redistribute it freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 *
 * 2. This notice may not be removed or altered from any source distribution.
 */
package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD;

/**
 * An efficient well-optimized kd-tree <p> This is a basic copy of the KdTree class but limited to 2
 * dimensions and has no ability to store object data.
 *
 * @author Alex Herbert
 */
abstract class SimpleKdTreeNode2D {
  /** The bucket size. */
  static final int bucketSize = 24;

  // All types

  /** The parent. */
  final SimpleKdTreeNode2D parent;

  // Leaf only

  /** The locations. */
  double[][] locations;
  /** The location count. */
  int locationCount;

  // Stem only

  /** The left. */
  SimpleKdTreeNode2D left;
  /** The right. */
  SimpleKdTreeNode2D right;
  /** The split dimension. */
  int splitDimension;
  /** The split value. */
  double splitValue;

  // Bounds

  /** The min limit. */
  double[] minLimit;
  /** The max limit. */
  double[] maxLimit;

  /** The singularity. */
  boolean singularity;

  // Temporary

  /** The status. */
  Status status;

  /**
   * Construct a RTree with 2 dimensions
   */
  SimpleKdTreeNode2D() {
    // Init as leaf
    this.locations = new double[bucketSize][];
    this.locationCount = 0;
    this.singularity = true;

    // Init as root
    this.parent = null;
  }

  /**
   * Constructor for child nodes. Internal use only.
   *
   * @param parent the parent
   */
  SimpleKdTreeNode2D(SimpleKdTreeNode2D parent) {
    // Init as leaf
    this.locations = new double[Math.max(bucketSize, parent.locationCount)][];
    this.locationCount = 0;
    this.singularity = true;

    // Init as non-root
    this.parent = parent;
  }

  /**
   * Extends the bounds of this node do include a new location.
   *
   * @param location the location
   */
  final void extendBounds(double[] location) {
    if (minLimit == null) {
      minLimit = new double[] {location[0], location[1]};
      maxLimit = new double[] {location[0], location[1]};
      return;
    }

    for (int i = 2; i-- > 0;) {
      if (Double.isNaN(location[i])) {
        minLimit[i] = Double.NaN;
        maxLimit[i] = Double.NaN;
        singularity = false;
      } else if (minLimit[i] > location[i]) {
        minLimit[i] = location[i];
        singularity = false;
      } else if (maxLimit[i] < location[i]) {
        maxLimit[i] = location[i];
        singularity = false;
      }
    }
  }

  /**
   * Find the widest axis of the bounds of this node.
   *
   * @return the axis
   */
  final int findWidestAxis() {
    double width = (maxLimit[0] - minLimit[0]);
    if (Double.isNaN(width)) {
      width = 0;
    }

    double nwidth = (maxLimit[1] - minLimit[1]);
    if (Double.isNaN(nwidth)) {
      nwidth = 0;
    }
    if (nwidth > width) {
      return 1;
    }

    return 0;
  }

  // Override in subclasses

  /**
   * Compute the point distance.
   *
   * @param p1 the p 1
   * @param p2 the p 2
   * @return the distance
   */
  protected abstract double pointDist(double[] p1, double[] p2);

  /**
   * Compute the point region distance.
   *
   * @param point the point
   * @param min the min of the region
   * @param max the max of the region
   * @return the distance
   */
  protected abstract double pointRegionDist(double[] point, double[] min, double[] max);
}
