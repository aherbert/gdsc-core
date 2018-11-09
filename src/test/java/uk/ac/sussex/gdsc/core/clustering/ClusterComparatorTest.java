package uk.ac.sussex.gdsc.core.clustering;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

@SuppressWarnings({"javadoc"})
public class ClusterComparatorTest {

  @Test
  public void canSortClusters() {
    // Sort order is size, X, Y, sumW.
    // Build a list of differing size, then x, then y, then weight
    final ArrayList<Cluster> list = new ArrayList<>();
    final Cluster c1 = new Cluster(new ClusterPoint(0, 0, 0));
    final Cluster c2 = new Cluster(new ClusterPoint(0, 1, 0)); // new x
    final Cluster c3 = new Cluster(new ClusterPoint(0, 1, 1)); // new y
    final Cluster c4 = new Cluster(new ClusterPoint(0, 1, 1, 2)); // new weight
    final Cluster c5 = new Cluster(new ClusterPoint(0, 0, 0)); // new size
    c5.add(new ClusterPoint(0, 0, 0));
    list.add(c1);
    list.add(c2);
    list.add(c3);
    list.add(c4);
    list.add(c5);

    final ClusterComparator comparator = ClusterComparator.getInstance();

    // Test the comparison is reversible
    for (final Cluster first : list) {
      for (final Cluster second : list) {
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
    Assertions.assertEquals(-1, comparator.compare(c4, c5), "Lower size not before");
  }
}
