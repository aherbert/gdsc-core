package uk.ac.sussex.gdsc.core.math.interpolation;

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
public class CustomSplinePositionTest {
    @Test
    public void testConstructor() {
    }
}
