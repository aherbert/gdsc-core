package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This class is used to in-line the computation for the CustomTricubicFunction.
 * <p>
 * The ordering of the computation is set to multiply by the power ZYX and the
 * cubic coefficient last.
 * <p>
 * This allows the power table to be precomputed and the result should match the
 * non-precomputed version. This includes scaling the power table by 2,3,6 for
 * computation of the gradients.
 */
@SuppressWarnings({ "javadoc" })
public class CubicSplinePositionTest {
    @Test
    public void testConstructor() {
        Assertions.assertThrows(OutOfRangeException.class, () -> {
            @SuppressWarnings("unused")
            CubicSplinePosition p = new CubicSplinePosition(-1e-6);
        });
        Assertions.assertThrows(OutOfRangeException.class, () -> {
            @SuppressWarnings("unused")
            CubicSplinePosition p = new CubicSplinePosition(1.000001);
        });
        Assertions.assertThrows(OutOfRangeException.class, () -> {
            @SuppressWarnings("unused")
            CubicSplinePosition p = new CubicSplinePosition(Double.NaN);
        });
        CubicSplinePosition p = new CubicSplinePosition(0.5);
        Assertions.assertNotNull(p);
    }

    @Test
    public void testProperties() {
        for (int i = 0; i <= 5; i++) {
            double x = (double) i / 5;
            CubicSplinePosition p = new CubicSplinePosition(x);
            Assertions.assertNotNull(p);
            double x2 = x * x;
            double x3 = x * x2;
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
