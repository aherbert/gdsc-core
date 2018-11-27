package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@SuppressWarnings({"javadoc"})
public class SortUtilsTest {
  @Test
  public void canSortDescendingIndicesUsingIntValues() {
    final int[] indices = {0, 1, 2};
    final int[] values = {2, 1, 3};
    SortUtils.sort(indices, values);
    final int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesAndValuesUsingIntValues() {
    final int[] indices = {0, 1, 2};
    final int[] values = {2, 1, 3};
    final int[] expectedValues = values.clone();
    SortUtils.sort(indices, values, true);
    final int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingIndicesUsingFloatValues() {
    final int[] indices = {0, 1, 2};
    final float[] values = {0.0f, -0.0f, 1f};
    SortUtils.sort(indices, values);
    final int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesAndValuesUsingFloatValues() {
    final int[] indices = {0, 1, 2};
    final float[] values = {0.0f, -0.0f, 1f};
    final float[] expectedValues = values.clone();
    SortUtils.sort(indices, values, true);
    final int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortDescendingIndicesUsingDoubleValues() {
    final int[] indices = {0, 1, 2};
    final double[] values = {0.0, -0.0, 1};
    SortUtils.sort(indices, values);
    final int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
  }

  @Test
  public void canSortDescendingIndicesAndValuesUsingDoubleValues() {
    final int[] indices = {0, 1, 2};
    final double[] values = {0.0, -0.0, 1};
    final double[] expectedValues = values.clone();
    SortUtils.sort(indices, values, true);
    final int[] expected = {2, 0, 1};
    Assertions.assertArrayEquals(expected, indices, "Indices not descending order");
    Arrays.sort(expectedValues);
    SimpleArrayUtils.reverse(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not descending order");
  }

  @Test
  public void canSortAscendingIndicesUsingIntValues() {
    final int[] indices = {0, 1, 2};
    final int[] values = {2, 1, 3};
    SortUtils.sortAscending(indices, values);
    final int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesAndValuesUsingIntValues() {
    final int[] indices = {0, 1, 2};
    final int[] values = {2, 1, 3};
    final int[] expectedValues = values.clone();
    SortUtils.sortAscending(indices, values, true);
    final int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
    Arrays.sort(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingIndicesUsingFloatValues() {
    final int[] indices = {0, 1, 2};
    final float[] values = {0.0f, -0.0f, 1f};
    SortUtils.sortAscending(indices, values);
    final int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesAndValuesUsingFloatValues() {
    final int[] indices = {0, 1, 2};
    final float[] values = {0.0f, -0.0f, 1f};
    final float[] expectedValues = values.clone();
    SortUtils.sortAscending(indices, values, true);
    final int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
    Arrays.sort(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }

  @Test
  public void canSortAscendingIndicesUsingDoubleValues() {
    final int[] indices = {0, 1, 2};
    final double[] values = {0.0, -0.0, 1};
    SortUtils.sortAscending(indices, values);
    final int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
  }

  @Test
  public void canSortAscendingIndicesAndValuesUsingDoubleValues() {
    final int[] indices = {0, 1, 2};
    final double[] values = {0.0, -0.0, 1};
    final double[] expectedValues = values.clone();
    SortUtils.sortAscending(indices, values, true);
    final int[] expected = {1, 0, 2};
    Assertions.assertArrayEquals(expected, indices, "Indices not ascending order");
    Arrays.sort(expectedValues);
    Assertions.assertArrayEquals(expectedValues, values, "Values not ascending order");
  }
}
