package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc" })
public class CubicSplinePositionTest {
    @Test
    public void testConstructor() {
        final double x = 0.5;
        Assertions.assertNotNull(new CubicSplinePosition(x));

        Assertions.assertThrows(OutOfRangeException.class, () -> {
            @SuppressWarnings("unused")
            final CubicSplinePosition p = new CubicSplinePosition(-1e-6);
        });
        Assertions.assertThrows(OutOfRangeException.class, () -> {
            @SuppressWarnings("unused")
            final CubicSplinePosition p = new CubicSplinePosition(1.000001);
        });
        Assertions.assertThrows(OutOfRangeException.class, () -> {
            @SuppressWarnings("unused")
            final CubicSplinePosition p = new CubicSplinePosition(Double.NaN);
        });
    }

    @Test
    public void testProperties() {
        for (int i = 0; i <= 5; i++) {
            final double x = (double) i / 5;
            final CubicSplinePosition p = new CubicSplinePosition(x);
            Assertions.assertNotNull(p);
            final double x2 = x * x;
            final double x3 = x * x2;
            Assertions.assertEquals(1, p.getPower(0));
            Assertions.assertEquals(x, p.getPower(1));
            Assertions.assertEquals(x2, p.getPower(2));
            Assertions.assertEquals(x3, p.getPower(3));
            Assertions.assertEquals(x, p.getX());
            Assertions.assertEquals(x2, p.getX2());
            Assertions.assertEquals(x3, p.getX3());
            Assertions.assertEquals(1, p.scale(1));
            Assertions.assertEquals(1, p.scaleGradient(1));
            Assertions.assertEquals(1, p.scaleGradient2(1));
        }
    }
}
