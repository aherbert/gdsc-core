package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link AssignmentComparator}.
 */
@SuppressWarnings({"javadoc"})
public class AssignmentComparatorTest {
  @SeededTest
  public void canSort(RandomSeed seed) {
    final Assignment[] data = IntStream.rangeClosed(1, 10)
        .mapToObj(d -> new ImmutableAssignment(0, 0, d)).toArray(Assignment[]::new);
    RandomUtils.shuffle(data, RngUtils.create(seed.getSeed()));
    final List<Assignment> list = Arrays.asList(data.clone());
    AssignmentComparator.sort(data);
    AssignmentComparator.sort(list);
    for (int i = 0; i < data.length; i++) {
      final double expected = i + 1.0;
      Assertions.assertEquals(expected, data[i].getDistance(), "Array sort");
      Assertions.assertEquals(expected, list.get(i).getDistance(), "List sort");
    }
  }
}
