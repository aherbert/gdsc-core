package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MutableFractionalAssignment}.
 */
@SuppressWarnings({"javadoc"})
public class MutableFractionalAssignmentTest {
  @Test
  public void canCreate() {
    final int targetId = 1454945;
    final int predictedId = 7686;
    final double distance = 2342.78998;
    final double score = 4553.789987;
    final MutableFractionalAssignment data =
        new MutableFractionalAssignment(targetId, predictedId, distance, score);
    Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
    Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
    Assertions.assertEquals(distance, data.getDistance(), "Distance");
    Assertions.assertEquals(score, data.getScore(), "Score");
  }

  @Test
  public void canUpdate() {
    int targetId = 1454945;
    int predictedId = 7686;
    double distance = 2342.78998;
    double score = 4553.789987;
    final MutableFractionalAssignment data =
        new MutableFractionalAssignment(targetId, predictedId, distance, score);

    for (final int add : new int[] {-45, 2, 34}) {
      targetId = add + data.getTargetId();
      predictedId = add + data.getPredictedId();
      distance = add + data.getDistance();
      score = add + data.getScore();
      data.setTargetId(targetId);
      data.setPredictedId(predictedId);
      data.setDistance(distance);
      data.setScore(score);
      Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
      Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
      Assertions.assertEquals(distance, data.getDistance(), "Distance");
      Assertions.assertEquals(score, data.getScore(), "Score");
    }
  }
}
