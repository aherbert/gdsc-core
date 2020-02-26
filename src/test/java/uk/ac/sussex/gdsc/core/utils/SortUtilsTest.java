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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class SortUtilsTest {
  @Test
  public void canSortAscendingIndicesUsingIntValues() {
    final int[] indices = {0, 1, 2, 1};
    final int[] values = {44, 0, 1};
    final int[] expectedIndices = {1, 1, 2, 0};
    SortUtils.sortIndices(indices, values, false);
    Assertions.assertArrayEquals(expectedIndices, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesUsingFloatValues() {
    final int[] indices = {0, 1, 2, 1};
    final float[] values = {44, -0F, 0F};
    final int[] expectedIndices = {1, 1, 2, 0};
    SortUtils.sortIndices(indices, values, false);
    Assertions.assertArrayEquals(expectedIndices, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesUsingDoubleValues() {
    final int[] indices = {0, 1, 2, 1};
    final double[] values = {44, -0D, 0D};
    final int[] expectedIndices = {1, 1, 2, 0};
    SortUtils.sortIndices(indices, values, false);
    Assertions.assertArrayEquals(expectedIndices, indices, "Indices not ascending order");
  }

  @Test
  public void canSortDescendingIndicesUsingIntValues() {
    final int[] indices = {0, 1, 2, 1};
    final int[] values = {44, 0, 1};
    final int[] expectedIndices = {0, 2, 1, 1};
    SortUtils.sortIndices(indices, values, true);
    Assertions.assertArrayEquals(expectedIndices, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesUsingFloatValues() {
    final int[] indices = {0, 1, 2, 1};
    final float[] values = {44, -0F, 0F};
    final int[] expectedIndices = {0, 2, 1, 1};
    SortUtils.sortIndices(indices, values, true);
    Assertions.assertArrayEquals(expectedIndices, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesUsingDoubleValues() {
    final int[] indices = {0, 1, 2, 1};
    final double[] values = {44, -0D, 0D};
    final int[] expectedIndices = {0, 2, 1, 1};
    SortUtils.sortIndices(indices, values, true);
    Assertions.assertArrayEquals(expectedIndices, indices, "Indices not descending order");
  }

  @Test
  public void canSortAscendingDataUsingIntDataIntValues() {
    final int[] data = {70, 80, 90};
    final int[] values = {44, 0, 1};
    final int[] expectedData = {80, 90, 70};
    final int[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortAscendingDataUsingFloatDataFloatValues() {
    final float[] data = {70, 80, 90};
    final float[] values = {44, -0F, 0F};
    final float[] expectedData = {80, 90, 70};
    final float[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortAscendingDataUsingDoubleDataDoubleValues() {
    final double[] data = {70, 80, 90};
    final double[] values = {44, -0D, 0D};
    final double[] expectedData = {80, 90, 70};
    final double[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortAscendingDataUsingIntDataDoubleValues() {
    final int[] data = {70, 80, 90};
    final double[] values = {44, -0D, 0D};
    final int[] expectedData = {80, 90, 70};
    final double[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortAscendingDataAndValuesUsingIntDataIntValues() {
    final int[] data = {70, 80, 90};
    final int[] values = {44, 0, 1};
    final int[] expectedData = {80, 90, 70};
    final int[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SortUtils.sortData(data, values, true, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingDataAndValuesUsingFloatDataFloatValues() {
    final float[] data = {70, 80, 90};
    final float[] values = {44, 0, 1};
    final float[] expectedData = {80, 90, 70};
    final float[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SortUtils.sortData(data, values, true, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingDataAndValuesUsingDoubleDataDoubleValues() {
    final double[] data = {70, 80, 90};
    final double[] values = {44, 0, 1};
    final double[] expectedData = {80, 90, 70};
    final double[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SortUtils.sortData(data, values, true, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingDataAndValuesUsingIntDataDoubleValues() {
    final int[] data = {70, 80, 90};
    final double[] values = {44, 0, 1};
    final int[] expectedData = {80, 90, 70};
    final double[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SortUtils.sortData(data, values, true, false);
    Assertions.assertArrayEquals(expectedData, data, "Data not ascending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortDescendingDataUsingIntDataIntValues() {
    final int[] data = {70, 80, 90};
    final int[] values = {44, 0, 1};
    final int[] expectedData = {70, 90, 80};
    final int[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortDescendingDataUsingFloatDataFloatValues() {
    final float[] data = {70, 80, 90};
    final float[] values = {44, -0F, 0F};
    final float[] expectedData = {70, 90, 80};
    final float[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortDescendingDataUsingDoubleDataDoubleValues() {
    final double[] data = {70, 80, 90};
    final double[] values = {44, -0D, 0D};
    final double[] expectedData = {70, 90, 80};
    final double[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortDescendingDataUsingIntDataDoubleValues() {
    final int[] data = {70, 80, 90};
    final double[] values = {44, -0D, 0D};
    final int[] expectedData = {70, 90, 80};
    final double[] expectedValues = values.clone();
    SortUtils.sortData(data, values, false, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values have changed");
  }

  @Test
  public void canSortDescendingDataAndValuesUsingIntDataIntValues() {
    final int[] data = {70, 80, 90};
    final int[] values = {44, 0, 1};
    final int[] expectedData = {70, 90, 80};
    final int[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    SortUtils.sortData(data, values, true, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingDataAndValuesUsingFloatDataFloatValues() {
    final float[] data = {70, 80, 90};
    final float[] values = {44, 0, 1};
    final float[] expectedData = {70, 90, 80};
    final float[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    SortUtils.sortData(data, values, true, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingDataAndValuesUsingDoubleDataDoubleValues() {
    final double[] data = {70, 80, 90};
    final double[] values = {44, 0, 1};
    final double[] expectedData = {70, 90, 80};
    final double[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    SortUtils.sortData(data, values, true, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingDataAndValuesUsingIntDataDoubleValues() {
    final int[] data = {70, 80, 90};
    final double[] values = {44, 0, 1};
    final int[] expectedData = {70, 90, 80};
    final double[] expectedValues = values.clone();
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    SortUtils.sortData(data, values, true, true);
    Assertions.assertArrayEquals(expectedData, data, "Data not descending order");
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }
}
