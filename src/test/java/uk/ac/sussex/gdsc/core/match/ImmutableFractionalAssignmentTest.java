package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ImmutableFractionalAssignment}.
 */
@SuppressWarnings({"javadoc"})
public class ImmutableFractionalAssignmentTest {
  @Test
  public void canCreate() {
    final int targetId = 1454945;
    final int predictedId = 7686;
    final double distance = 2342.78998;
    final double score = 4553.789987;
    ImmutableFractionalAssignment data =
        new ImmutableFractionalAssignment(targetId, predictedId, distance, score);
    Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
    Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
    Assertions.assertEquals(distance, data.getDistance(), "Distance");
    Assertions.assertEquals(score, data.getScore(), "Score");

    data = new ImmutableFractionalAssignment(targetId, predictedId, distance);
    Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
    Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
    Assertions.assertEquals(distance, data.getDistance(), "Distance");
    Assertions.assertEquals(1.0, data.getScore(), "Score");
  }
}
