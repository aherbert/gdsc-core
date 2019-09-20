package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MutableAssignment}.
 */
@SuppressWarnings({"javadoc"})
public class MutableAssignmentTest {
  @Test
  public void canCreate() {
    final int targetId = 1454945;
    final int predictedId = 7686;
    final double distance = 2342.78998;
    final MutableAssignment data = new MutableAssignment(targetId, predictedId, distance);
    Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
    Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
    Assertions.assertEquals(distance, data.getDistance(), "Distance");
  }

  @Test
  public void canUpdate() {
    int targetId = 1454945;
    int predictedId = 7686;
    double distance = 2342.78998;
    final MutableAssignment data = new MutableAssignment(targetId, predictedId, distance);

    for (final int add : new int[] {-45, 2, 34}) {
      targetId = add + data.getTargetId();
      predictedId = add + data.getPredictedId();
      distance = add + data.getDistance();
      data.setTargetId(targetId);
      data.setPredictedId(predictedId);
      data.setDistance(distance);
      Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
      Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
      Assertions.assertEquals(distance, data.getDistance(), "Distance");
    }
  }
}
