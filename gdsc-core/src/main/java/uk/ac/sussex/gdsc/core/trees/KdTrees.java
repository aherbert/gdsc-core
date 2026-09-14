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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import java.util.Objects;
import java.util.function.IntToDoubleFunction;

/**
 * Utility class for creating KD-trees.
 */
public final class KdTrees {

  /**
   * A builder for a KD-tree.
   */
  public static final class Builder {
    /** The dimensions. */
    private int dimensions;
    /** The dimension weight. */
    private IntToDoubleFunction dimensionWeight = DimensionWeightFunctions.ONE;
    /** The split strategy. */
    private SplitStrategy splitStrategy = SplitStrategy.MIDDLE;

    /**
     * Create an instance.
     *
     * @param dimensions the dimensions
     * @throws IllegalAccessException if {@code dimension < 1}
     */
    Builder(int dimensions) {
      if (dimensions < 1) {
        throw new IllegalArgumentException("Dimensions must be strictly positive: " + dimensions);
      }
      this.dimensions = dimensions;
    }

    /**
     * Sets the dimension weight.
     *
     * @param dimensionWeight the dimension weight
     * @return the builder
     * @throws NullPointerException if the weight is {@code null}
     */
    public Builder setDimensionWeight(IntToDoubleFunction dimensionWeight) {
      this.dimensionWeight = Objects.requireNonNull(dimensionWeight);
      return this;
    }

    /**
     * Sets the split stratgey.
     *
     * @param splitStrategy the split strategy
     * @return the builder
     * @throws NullPointerException if the strategy is {@code null}
     */
    Builder setSplitStratgey(SplitStrategy splitStrategy) {
      this.splitStrategy = Objects.requireNonNull(splitStrategy);
      return this;
    }

    /**
     * Builds a KD-tree that stores an item with each {@code double}-valued location.
     *
     * @param <T> the type of object
     * @return the tree
     */
    public <T> ObjDoubleKdTree<T> buildObjDouble() {
      return new ObjDoubleNdTree<>(dimensions, dimensionWeight, splitStrategy);
    }

    /**
     * Builds a KD-tree that stores an {@code int} with each {@code double}-valued location.
     *
     * @return the tree
     */
    public IntDoubleKdTree buildIntDouble() {
      return new IntDoubleNdTree(dimensions, dimensionWeight, splitStrategy);
    }

    /**
     * Builds a KD-tree using a {@code double}-valued location.
     *
     * @return the tree
     */
    public DoubleKdTree buildDouble() {
      return new DoubleNdTree(dimensions, dimensionWeight, splitStrategy);
    }

    /**
     * Builds a KD-tree that stores an item with each {@code float}-valued location.
     *
     * @param <T> the type of object
     * @return the tree
     */
    public <T> ObjFloatKdTree<T> buildObjFloat() {
      return new ObjFloatNdTree<>(dimensions, dimensionWeight, splitStrategy);
    }

    /**
     * Builds a KD-tree that stores an {@code int} with each {@code float}-valued location.
     *
     * @return the tree
     */
    public IntFloatKdTree buildIntFloat() {
      return new IntFloatNdTree(dimensions, dimensionWeight, splitStrategy);
    }

    /**
     * Builds a KD-tree using a {@code float}-valued location.
     *
     * @return the tree
     */
    public FloatKdTree buildFloat() {
      return new FloatNdTree(dimensions, dimensionWeight, splitStrategy);
    }
  }

  /** No public construction. */
  private KdTrees() {}

  /**
   * Builder.
   *
   * @param dimensions the dimensions
   * @return the builder
   */
  public static Builder builder(int dimensions) {
    return new Builder(dimensions);
  }

  // Note: The following methods are retained for backwards compatibility.
  // The Builder should be used to create trees with customised functionality.

  /**
   * Creates a KD-tree.
   *
   * @param <T> the generic type
   * @param dimensions the dimensions
   * @return the KD-tree
   */
  public static <T> ObjDoubleKdTree<T> newObjDoubleKdTree(int dimensions) {
    return newObjDoubleKdTree(dimensions, DimensionWeightFunctions.ONE);
  }

  /**
   * Creates a KD-tree.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param <T> the generic type
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   * @return the KD-tree
   */
  public static <T> ObjDoubleKdTree<T> newObjDoubleKdTree(int dimensions,
      IntToDoubleFunction dimensionWeight) {
    return builder(dimensions).setDimensionWeight(dimensionWeight).buildObjDouble();
  }

  /**
   * Creates a KD-tree.
   *
   * @param dimensions the dimensions
   * @return the KD-tree
   */
  public static IntDoubleKdTree newIntDoubleKdTree(int dimensions) {
    return newIntDoubleKdTree(dimensions, DimensionWeightFunctions.ONE);
  }

  /**
   * Creates a KD-tree.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   * @return the KD-tree
   */
  public static IntDoubleKdTree newIntDoubleKdTree(int dimensions,
      IntToDoubleFunction dimensionWeight) {
    return builder(dimensions).setDimensionWeight(dimensionWeight).buildIntDouble();
  }

  /**
   * Creates a KD-tree.
   *
   * @param dimensions the dimensions
   * @return the KD-tree
   */
  public static DoubleKdTree newDoubleKdTree(int dimensions) {
    return newDoubleKdTree(dimensions, DimensionWeightFunctions.ONE);
  }

  /**
   * Creates a KD-tree.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   * @return the KD-tree
   */
  public static DoubleKdTree newDoubleKdTree(int dimensions, IntToDoubleFunction dimensionWeight) {
    return builder(dimensions).setDimensionWeight(dimensionWeight).buildDouble();
  }

  /**
   * Creates a KD-tree.
   *
   * @param <T> the generic type
   * @param dimensions the dimensions
   * @return the KD-tree
   */
  public static <T> ObjFloatKdTree<T> newObjFloatKdTree(int dimensions) {
    return newObjFloatKdTree(dimensions, DimensionWeightFunctions.ONE);
  }

  /**
   * Creates a KD-tree.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param <T> the generic type
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   * @return the KD-tree
   */
  public static <T> ObjFloatKdTree<T> newObjFloatKdTree(int dimensions,
      IntToDoubleFunction dimensionWeight) {
    return builder(dimensions).setDimensionWeight(dimensionWeight).buildObjFloat();
  }

  /**
   * Creates a KD-tree.
   *
   * @param dimensions the dimensions
   * @return the KD-tree
   */
  public static IntFloatKdTree newIntFloatKdTree(int dimensions) {
    return newIntFloatKdTree(dimensions, DimensionWeightFunctions.ONE);
  }

  /**
   * Creates a KD-tree.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   * @return the KD-tree
   */
  public static IntFloatKdTree newIntFloatKdTree(int dimensions,
      IntToDoubleFunction dimensionWeight) {
    return builder(dimensions).setDimensionWeight(dimensionWeight).buildIntFloat();
  }

  /**
   * Creates a KD-tree.
   *
   * @param dimensions the dimensions
   * @return the KD-tree
   */
  public static FloatKdTree newFloatKdTree(int dimensions) {
    return newFloatKdTree(dimensions, DimensionWeightFunctions.ONE);
  }

  /**
   * Creates a KD-tree.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   * @return the KD-tree
   */
  public static FloatKdTree newFloatKdTree(int dimensions, IntToDoubleFunction dimensionWeight) {
    return builder(dimensions).setDimensionWeight(dimensionWeight).buildFloat();
  }
}
