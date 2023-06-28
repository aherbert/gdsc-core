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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import java.util.function.IntToDoubleFunction;

/**
 * Utility class for creating KD-trees.
 */
public final class KdTrees {

  /** No public construction. */
  private KdTrees() {}

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
    return new ObjDoubleNdTree<>(dimensions, dimensionWeight);
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
    return new IntDoubleNdTree(dimensions, dimensionWeight);
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
    return new DoubleNdTree(dimensions, dimensionWeight);
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
    return new ObjFloatNdTree<>(dimensions, dimensionWeight);
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
    return new IntFloatNdTree(dimensions, dimensionWeight);
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
    return new FloatNdTree(dimensions, dimensionWeight);
  }
}
