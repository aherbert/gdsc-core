package uk.ac.sussex.gdsc.core.clustering;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

@SuppressWarnings({"javadoc"})
public class TimeClusterComparatorTest {

  @Test
  public void canSortClusters() {
    // Sort order is size, X, Y, sumW, startTime, endTime.
    // Build a list of differing size, then x, then y, then weight, then startTime, then endTime.
    final ArrayList<TimeCluster> list = new ArrayList<>();
    final TimeCluster c1 = new TimeCluster(new ClusterPoint(0, 0, 0, 1, 0, 1));
    final TimeCluster c2 = new TimeCluster(new ClusterPoint(0, 1, 0, 1, 0, 1)); // new x
    final TimeCluster c3 = new TimeCluster(new ClusterPoint(0, 1, 1, 1, 0, 1)); // new y
    final TimeCluster c4 = new TimeCluster(new ClusterPoint(0, 1, 1, 2, 0, 1)); // new weight
    final TimeCluster c5 = new TimeCluster(new ClusterPoint(0, 1, 1, 2, 1, 2)); // new start time
    final TimeCluster c6 = new TimeCluster(new ClusterPoint(0, 1, 1, 2, 1, 3)); // new end time
    final TimeCluster c7 = new TimeCluster(new ClusterPoint(0, 0, 0)); // new size
    c7.add(new ClusterPoint(0, 0, 0));

    list.add(c1);
    list.add(c2);
    list.add(c3);
    list.add(c4);
    list.add(c5);
    list.add(c6);
    list.add(c7);

    final TimeClusterComparator comparator = TimeClusterComparator.getInstance();

    // Test the comparison is reversible
    for (final TimeCluster first : list) {
      for (final TimeCluster second : list) {
        final int r1 = comparator.compare(first, second);
        final int r2 = comparator.compare(second, first);
        Assertions.assertEquals(r1, -r2, "Comparator not reversible");
      }
    }

    // Check order
    Assertions.assertEquals(0, comparator.compare(c1, c1), "Same cluster");
    Assertions.assertEquals(-1, comparator.compare(c1, c2), "Lower x not before");
    Assertions.assertEquals(-1, comparator.compare(c2, c3), "Lower y not before");
    Assertions.assertEquals(-1, comparator.compare(c3, c4), "Lower weight not before");
    Assertions.assertEquals(-1, comparator.compare(c4, c5), "Lower start time not before");
    Assertions.assertEquals(-1, comparator.compare(c5, c6), "Lower end time not before");
    Assertions.assertEquals(-1, comparator.compare(c6, c7), "Lower size not before");
  }
}
