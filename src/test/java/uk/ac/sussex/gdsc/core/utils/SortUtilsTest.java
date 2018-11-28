package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
