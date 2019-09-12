package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Pulse}.
 */
@SuppressWarnings({"javadoc"})
public class PulseTest {
  @Test
  public void canCreate() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    final int start = 789;
    final int end = 890;
    Pulse data = new Pulse(x, y, z, start, end);
    Assertions.assertEquals(x, data.getX(), "X");
    Assertions.assertEquals(y, data.getY(), "Y");
    Assertions.assertEquals(z, data.getZ(), "Z");
    Assertions.assertEquals(start, data.getStart(), "start");
    Assertions.assertEquals(end, data.getEnd(), "end");

    Assertions.assertEquals((int) x, data.getXint(), "Xint");
    Assertions.assertEquals((int) y, data.getYint(), "Yint");
    Assertions.assertEquals((int) z, data.getZint(), "Zint");

    data = new Pulse(x, y, start, end);
    Assertions.assertEquals(x, data.getX(), "X");
    Assertions.assertEquals(y, data.getY(), "Y");
    Assertions.assertEquals(0.0f, data.getZ(), "Z");
    Assertions.assertEquals(start, data.getStart(), "start");
    Assertions.assertEquals(end, data.getEnd(), "end");
  }

  @Test
  public void testEquals() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    final int start = 789;
    final int end = 890;
    final Pulse data = new Pulse(x, y, z, start, end);
    Assertions.assertTrue(data.equals(data));
    Assertions.assertFalse(data.equals(null));
    Assertions.assertFalse(data.equals(new Object()));

    final int[] shifts = {0, 1};
    for (final float dx : shifts) {
      for (final float dy : shifts) {
        for (final float dz : shifts) {
          for (final int ds : shifts) {
            for (final int de : shifts) {
              if (dx + dy + dz + ds + de != 0) {
                Assertions.assertFalse(
                    data.equals(new Pulse(x + dx, y + dy, z + dz, start + ds, end + de)));
              } else {
                Assertions.assertTrue(
                    data.equals(new Pulse(x + dx, y + dy, z + dz, start + ds, end + de)));
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void testHashCode() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    final int start = 789;
    final int end = 890;
    final Pulse data1 = new Pulse(x, y, z, start, end);
    final Pulse data2 = new Pulse(x, y, z, start, end);
    final Pulse data3 = new Pulse(x, y, start, end);
    Assertions.assertEquals(data1.hashCode(), data2.hashCode());
    Assertions.assertNotEquals(data1.hashCode(), data3.hashCode());
  }

  @Test
  public void testCalculateOverlap() {
    assertOverlap(0, 1, 1, 10, 10);
    assertOverlap(0, 10, 10, 1, 1);
    assertOverlap(1, 1, 1, 1, 1);
    assertOverlap(1, 1, 3, 1, 1);
    assertOverlap(1, 1, 3, 2, 2);
    assertOverlap(1, 1, 3, 3, 3);
    assertOverlap(2, 1, 3, 2, 3);
    assertOverlap(2, 1, 3, 2, 30);
    assertOverlap(3, 1, 3, 0, 30);
  }

  private static void assertOverlap(int overlap, int start1, int end1, int start2, int end2) {
    final Pulse data1 = new Pulse(0f, 0f, start1, end1);
    final Pulse data2 = new Pulse(0f, 0f, start2, end2);
    Assertions.assertEquals(overlap, data1.calculateOverlap(data2));
  }

  @Test
  public void testScore() {
    final float x = 0f;
    final float y = 0f;
    final int start = 1;
    final int end = 10;
    final Pulse data1 = new Pulse(x, y, start, end);

    // Distance threshold does not matter
    final double dt = 4;

    double score = data1.score(data1, dt);
    Assertions.assertEquals(end - start + 1, score,
        "Perfect match should equal the number of frames");

    // Test the score gets smaller when the distance increases
    for (int i = 1; i < 5; i++) {
      final double score2 = data1.score(new Pulse(x + i, y, start, end), dt);
      Assertions.assertTrue(score2 < score, "Distance increase should lower score");
      score = score2;
    }

    // Test the score gets smaller when the overlap decreases
    score = data1.score(data1, dt);
    for (int i = 1; i < 5; i++) {
      final double score2 = data1.score(new Pulse(x, y, start + i, end), dt);
      Assertions.assertTrue(score2 < score, "Overlap decrease should lower score");
      score = score2;
    }
  }
}
