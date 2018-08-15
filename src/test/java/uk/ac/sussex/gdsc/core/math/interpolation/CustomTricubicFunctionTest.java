package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
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
public class CustomTricubicFunctionTest {
    @Test
    public void canAccessCoefficients() {
        UniformRandomProvider rng = RandomSource.create(RandomSource.MWC_256);
        float[] fa = new float[64];
        double[] da = new double[64];
        for (int i = 0; i < fa.length; i++)
            da[i] = fa[i] = rng.nextFloat();
        FloatCustomTricubicFunction ff = (FloatCustomTricubicFunction) CustomTricubicFunction.create(fa.clone());
        DoubleCustomTricubicFunction df = (DoubleCustomTricubicFunction) CustomTricubicFunction.create(da.clone());
        for (int i = 0; i < fa.length; i++) {
            Assertions.assertEquals(da[i], ff.get(i));
            Assertions.assertEquals(da[i], df.get(i));
            Assertions.assertEquals(fa[i], ff.getf(i));
            Assertions.assertEquals(fa[i], df.getf(i));
        }

        Assertions.assertArrayEquals(da, ff.getA());
        Assertions.assertArrayEquals(da, df.getA());

        // test scaling
        ff.scale(2);
        df.scale(2);
        for (int i = 0; i < fa.length; i++) {
            Assertions.assertEquals(da[i] * 2, ff.get(i));
            Assertions.assertEquals(da[i] * 2, df.get(i));
            Assertions.assertEquals(fa[i] * 2, ff.getf(i));
            Assertions.assertEquals(fa[i] * 2, df.getf(i));
        }
    }
    
    @Test
    public void canConvert() {
        UniformRandomProvider rng = RandomSource.create(RandomSource.MWC_256);
        float[] fa = new float[64];
        double[] da = new double[64];
        for (int i = 0; i < fa.length; i++)
            da[i] = fa[i] = rng.nextFloat();
        FloatCustomTricubicFunction ff = (FloatCustomTricubicFunction) CustomTricubicFunction.create(fa.clone());
        DoubleCustomTricubicFunction df = (DoubleCustomTricubicFunction) CustomTricubicFunction.create(da.clone());
        
        Assertions.assertSame(ff, ff.toSinglePrecision());
        Assertions.assertSame(df, df.toDoublePrecision());

        // Convert
        DoubleCustomTricubicFunction df2 = (DoubleCustomTricubicFunction) ff.toDoublePrecision();
        FloatCustomTricubicFunction ff2 = (FloatCustomTricubicFunction) df.toSinglePrecision();
        
        Assertions.assertArrayEquals(da, ff2.getA());
        Assertions.assertArrayEquals(da, df2.getA());
    }
}
