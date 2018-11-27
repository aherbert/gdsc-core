package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@SuppressWarnings({"javadoc"})
public class SortUtilsTest {
  @Test
  public void canSortDescendingIndicesUsingIntValues() {
    int[] indices = {0, 1, 2};
    int[] values = {2, 1, 3};
    SortUtils.sort(indices, values);
    int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesAndValuesUsingIntValues() {
    int[] indices = {0, 1, 2};
    int[] values = {2, 1, 3};
    int[] expectedValues = values.clone();
    SortUtils.sort(indices, values, true);
    int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingIndicesUsingFloatValues() {
    int[] indices = {0, 1, 2};
    float[] values = {2, 1, 3};
    SortUtils.sort(indices, values);
    int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesAndValuesUsingFloatValues() {
    int[] indices = {0, 1, 2};
    float[] values = {2, 1, 3};
    float[] expectedValues = values.clone();
    SortUtils.sort(indices, values, true);
    int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingIndicesUsingDoubleValues() {
    int[] indices = {0, 1, 2};
    double[] values = {2, 1, 3};
    SortUtils.sort(indices, values);
    int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesAndValuesUsingDoubleValues() {
    int[] indices = {0, 1, 2};
    double[] values = {2, 1, 3};
    double[] expectedValues = values.clone();
    SortUtils.sort(indices, values, true);
    int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortAscendingIndicesUsingIntValues() {
    int[] indices = {0, 1, 2};
    int[] values = {2, 1, 3};
    SortUtils.sortAscending(indices, values);
    int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesAndValuesUsingIntValues() {
    int[] indices = {0, 1, 2};
    int[] values = {2, 1, 3};
    int[] expectedValues = values.clone();
    SortUtils.sortAscending(indices, values, true);
    int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
    Arrays.sort(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingIndicesUsingFloatValues() {
    int[] indices = {0, 1, 2};
    float[] values = {2, 1, 3};
    SortUtils.sortAscending(indices, values);
    int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesAndValuesUsingFloatValues() {
    int[] indices = {0, 1, 2};
    float[] values = {2, 1, 3};
    float[] expectedValues = values.clone();
    SortUtils.sortAscending(indices, values, true);
    int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
    Arrays.sort(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingIndicesUsingDoubleValues() {
    int[] indices = {0, 1, 2};
    double[] values = {2, 1, 3};
    SortUtils.sortAscending(indices, values);
    int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesAndValuesUsingDoubleValues() {
    int[] indices = {0, 1, 2};
    double[] values = {2, 1, 3};
    double[] expectedValues = values.clone();
    SortUtils.sortAscending(indices, values, true);
    int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
    Arrays.sort(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }
}
