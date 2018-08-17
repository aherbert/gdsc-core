package uk.ac.sussex.gdsc.core.math.interpolation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc" })
public class IndexedCubicSplinePositionTest {
    // Note: Avoids testing the super-class methods again. Only those new to this
    // class.

    @Test
    public void testConstructor() {
        final int index = 0;
        final double x = 0.5;
        Assertions.assertNotNull(new IndexedCubicSplinePosition(index, x));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            final IndexedCubicSplinePosition p = new IndexedCubicSplinePosition(-1, x);
        });
    }

    @Test
    public void testProperties() {
        final double x = 0.5;
        for (int i = 0; i <= 5; i++) {
            final IndexedCubicSplinePosition p = new IndexedCubicSplinePosition(i, x);
            Assertions.assertNotNull(p);
            Assertions.assertEquals(i, p.index);
        }
    }
}
