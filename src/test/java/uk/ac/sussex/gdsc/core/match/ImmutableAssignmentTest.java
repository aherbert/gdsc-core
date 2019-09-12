package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ImmutableAssignment}.
 */
@SuppressWarnings({"javadoc"})
public class ImmutableAssignmentTest {
  @Test
  public void canCreate() {
    final int targetId = 1454945;
    final int predictedId = 7686;
    final double distance = 2342.78998;
    final ImmutableAssignment data = new ImmutableAssignment(targetId, predictedId, distance);
    Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
    Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
    Assertions.assertEquals(distance, data.getDistance(), "Distance");
  }
}
