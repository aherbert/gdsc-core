package uk.ac.sussex.gdsc.core.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class GeometryTest {
  @Test
  public void canComputeArea() {
    // Area is signed
    canComputeArea(0.5, true, 0, 0, 1, 0, 1, 1);
    canComputeArea(-0.5, true, 0, 0, 1, 1, 1, 0);
    canComputeArea(0.5, false, 0, 0, 1, 1, 1, 0);

    canComputeArea(1, true, 0, 0, 1, 0, 1, 1, 0, 1);
  }

  private static void canComputeArea(double exp, boolean signed, double... vertices) {
    final double[] x = new double[vertices.length / 2];
    final double[] y = new double[x.length];
    for (int i = 0, j = 0; i < vertices.length; i += 2, j++) {
      x[j] = vertices[i];
      y[j] = vertices[i + 1];
    }
    double obs = Geometry.getArea(x, y);
    if (!signed) {
      obs = Math.abs(obs);
    }
    Assertions.assertEquals(exp, obs, 1e-10);
  }

  @Test
  public void canComputeIntersection() {
    canComputeIntersection(null, 0, 0, 1, 0, 0, 1, 1, 1);
    canComputeIntersection(new double[] {0.5, 0.5}, 0, 0, 1, 1, 1, 0, 0, 1);
    canComputeIntersection(new double[] {0, 0}, 0, 0, 1, 1, 0, 0, 0, 1);
  }

  private static void canComputeIntersection(double[] exp, double x1, double y1, double x2,
      double y2, double x3, double y3, double x4, double y4) {
    final double[] obs = new double[2];
    final boolean result = Geometry.getIntersection(x1, y1, x2, y2, x3, y3, x4, y4, obs);
    if (exp == null) {
      Assertions.assertFalse(result);
    } else {
      Assertions.assertTrue(result);
      Assertions.assertArrayEquals(exp, obs, 1e-10);
    }
  }
}
