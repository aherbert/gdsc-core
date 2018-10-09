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
package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD;

import java.util.Arrays;

/**
 * A KD-tree node.
 *
 * @param <T> the generic type
 */
abstract class KdNode<T> {
  // All types

  /** The bucket capacity. */
  protected int bucketCapacity;
  /** The size. */
  protected int size;

  // Leaf only

  /** The points. */
  protected double[][] points;
  /** The data. */
  protected Object[] data;

  // Stem only

  /** The left. */
  protected KdNode<T> left;
  /** The right. */
  protected KdNode<T> right;
  /** The split dimension. */
  protected int splitDimension;
  /** The split value. */
  protected double splitValue;

  // Bounds

  /** The min bound. */
  protected double[] minBound;
  /** The max bound. */
  protected double[] maxBound;

  /** The single point. */
  protected boolean singlePoint;

  /**
   * Instantiates a new kd node.
   *
   * @param bucketCapacity the bucket capacity
   */
  protected KdNode(int bucketCapacity) {
    // Init base
    this.bucketCapacity = bucketCapacity;
    this.size = 0;
    this.singlePoint = true;

    // Init leaf elements
    this.points = new double[bucketCapacity + 1][];
    this.data = new Object[bucketCapacity + 1];
  }

  /* -------- SIMPLE GETTERS -------- */

  /**
   * Gets the dimensions.
   *
   * @return the dimensions
   */
  public abstract int getDimensions();

  /**
   * Get the size.
   *
   * @return the size
   */
  public int size() {
    return size;
  }

  /**
   * Checks if is leaf.
   *
   * @return true, if is leaf
   */
  public boolean isLeaf() {
    return points != null;
  }

  /* -------- OPERATIONS -------- */

  /**
   * Adds the point.
   *
   * @param point the point
   * @param value the value
   */
  public void addPoint(double[] point, T value) {
    KdNode<T> cursor = this;
    while (!cursor.isLeaf()) {
      cursor.extendBounds(point);
      cursor.size++;
      if (point[cursor.splitDimension] > cursor.splitValue) {
        cursor = cursor.right;
      } else {
        cursor = cursor.left;
      }
    }
    cursor.addLeafPoint(point, value);
  }

  /* -------- INTERNAL OPERATIONS -------- */

  /**
   * Adds the leaf point.
   *
   * @param point the point
   * @param value the value
   */
  public void addLeafPoint(double[] point, T value) {
    // Add the data point
    points[size] = point;
    data[size] = value;
    extendBounds(point);
    size++;

    if (size == points.length - 1) {
      // If the node is getting too large
      if (calculateSplit()) {
        // If the node successfully had it's split value calculated, split node
        splitLeafNode();
      } else {
        // If the node could not be split, enlarge node
        increaseLeafCapacity();
      }
    }
  }

  @SuppressWarnings("unused")
  private boolean checkBounds(double[] point) {
    for (int i = getDimensions(); i-- > 0;) {
      if (point[i] > maxBound[i]) {
        return false;
      }
      if (point[i] < minBound[i]) {
        return false;
      }
    }
    return true;
  }

  private void extendBounds(double[] point) {
    if (minBound == null) {
      minBound = Arrays.copyOf(point, getDimensions());
      maxBound = Arrays.copyOf(point, getDimensions());
      return;
    }

    for (int i = getDimensions(); i-- > 0;) {
      if (Double.isNaN(point[i])) {
        if (!Double.isNaN(minBound[i]) || !Double.isNaN(maxBound[i])) {
          singlePoint = false;
        }
        minBound[i] = Double.NaN;
        maxBound[i] = Double.NaN;
      } else if (minBound[i] > point[i]) {
        minBound[i] = point[i];
        singlePoint = false;
      } else if (maxBound[i] < point[i]) {
        maxBound[i] = point[i];
        singlePoint = false;
      }
    }
  }

  private void increaseLeafCapacity() {
    points = Arrays.copyOf(points, points.length * 2);
    data = Arrays.copyOf(data, data.length * 2);
  }

  private boolean calculateSplit() {
    if (singlePoint) {
      return false;
    }

    double width = 0;
    for (int i = getDimensions(); i-- > 0;) {
      double dwidth = (maxBound[i] - minBound[i]);
      if (Double.isNaN(dwidth)) {
        dwidth = 0;
      }
      if (dwidth > width) {
        splitDimension = i;
        width = dwidth;
      }
    }

    if (width == 0) {
      return false;
    }

    // Start the split in the middle of the variance
    splitValue = (minBound[splitDimension] + maxBound[splitDimension]) * 0.5;

    // Never split on infinity or NaN
    if (splitValue == Double.POSITIVE_INFINITY) {
      splitValue = Double.MAX_VALUE;
    } else if (splitValue == Double.NEGATIVE_INFINITY) {
      splitValue = -Double.MAX_VALUE;
    }

    // Don't let the split value be the same as the upper value as
    // can happen due to rounding errors!
    if (splitValue == maxBound[splitDimension]) {
      splitValue = minBound[splitDimension];
    }

    // Success
    return true;
  }

  @SuppressWarnings("unchecked")
  private void splitLeafNode() {
    right = newInstance();
    left = newInstance();

    // Move locations into children
    for (int i = 0; i < size; i++) {
      final double[] oldLocation = points[i];
      final Object oldData = data[i];
      if (oldLocation[splitDimension] > splitValue) {
        right.addLeafPoint(oldLocation, (T) oldData);
      } else {
        left.addLeafPoint(oldLocation, (T) oldData);
      }
    }

    points = null;
    data = null;
  }

  /**
   * Create a new instance.
   *
   * @return the kd node
   */
  protected abstract KdNode<T> newInstance();
}
