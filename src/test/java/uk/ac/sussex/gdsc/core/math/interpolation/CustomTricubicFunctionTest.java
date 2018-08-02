package uk.ac.sussex.gdsc.core.math.interpolation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;

/**
 * This class is used to in-line the computation for the CustomTricubicFunction.
 * <p>
 * The ordering of the computation is set to multiply by the power ZYX and the cubic coefficient last.
 * <p>
 * This allows the power table to be precomputed and the result should match the
 * non-precomputed version. This includes scaling the power table by 2,3,6 for
 * computation of the gradients.
 */
@SuppressWarnings({ "javadoc" })
public class CustomTricubicFunctionTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(CustomTricubicFunctionTest.class.getName());
    }

    @AfterAll
    public static void afterAll()
    {
        logger = null;
    }

    /** Number of points. */
    private final short N = 4;
    /** Number of points - 1. */
    private final short N_1 = 3;
    /** Number of points - 2. */
    private final short N_2 = 2;

    static int getIndex(int i, int j, int k)
    {
        return CustomTricubicFunction.getIndex(i, j, k);
    }

    /**
     * Used to create the inline value function
     *
     * @return the function text.
     */
    String inlineValue()
    {
        String _pZpY;
        final StringBuilder sb = new StringBuilder();

        for (int k = 0, ai = 0; k < N; k++)
            for (int j = 0; j < N; j++)
            {
                _pZpY = append_pZpY(sb, k, j);

                for (int i = 0; i < N; i++, ai++)
                    sb.append(String.format("result += %s * pX[%d] * a[%d];\n", _pZpY, i, ai));
            }

        return finaliseInlineFunction(sb);
    }

    static String append_pZpY(StringBuilder sb, int k, int j)
    {
        String _pZpY;
        if (k == 0)
        {
            if (j == 0)
                _pZpY = "1";
            else
                _pZpY = String.format("pY[%d]", j);
        }
        else if (j == 0)
            _pZpY = String.format("pZ[%d]", k);
        else
        {
            sb.append(String.format("pZpY = pZ[%d] * pY[%d];\n", k, j));
            _pZpY = "pZpY";
        }
        return _pZpY;
    }

    static String finaliseInlineFunction(StringBuilder sb)
    {
        String result = sb.toString();
        // Replace the use of 1 in multiplications
        result = result.replace("pX[0]", "1");
        result = result.replace(" * 1", "");
        result = result.replace(" 1 *", "");
        // We optimise out the need to store 1.0 in the array at pN[0]
        // The power must all be shifted
        for (int i = 0; i < 3; i++)
        {
            final String was = String.format("[%d]", i + 1);
            final String now = String.format("[%d]", i);
            result = result.replace("pX" + was, "pX" + now);
            result = result.replace("pY" + was, "pY" + now);
            result = result.replace("pZ" + was, "pZ" + now);
        }

        return result;
    }

    /**
     * Used to create the inline value function for first-order gradients with power table
     *
     * @return the function text.
     */
    String inlineValueWithPowerTable()
    {
        final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);

        final StringBuilder sb = new StringBuilder();

        sb.append("return ");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    appendPower(map, sb, i, j, k, i, j, k);
        sb.append(";\n");

        // Each entry should be unique indicating that the result is optimal
        map.forEachEntry(new TObjectIntProcedure<String>()
        {
            @Override
            public boolean execute(String a, int b)
            {
                if (b > 1)
                {
                    TestLog.info(logger, "%s = %d\n", a, b);
                    return false;
                }
                return true;
            }
        });

        return finaliseInlinePowerTableFunction(sb);
    }

    /**
     * Used to create the inline power table function
     *
     * @return the function text.
     */
    String inlineComputePowerTable()
    {
        String table0jk;
        final StringBuilder sb = new StringBuilder();

        for (int k = 0, ai = 0; k < N; k++)
            for (int j = 0; j < N; j++)
            {
                table0jk = appendTableijk(sb, k, j, 0, ai++);

                for (int i = 1; i < N; i++, ai++)
                    sb.append(String.format("table[%d] = %s * pX[%d];\n", ai, table0jk, i));
            }

        return finaliseInlineFunction(sb);
    }

    static String appendTableijk(StringBuilder sb, int k, int j, int i, int ai)
    {
        String pZpY;
        boolean compound = true;
        if (k == 0)
        {
            compound = false;
            if (j == 0)
                pZpY = "1";
            else
                pZpY = String.format("pY[%d]", j);
        }
        else if (j == 0)
        {
            compound = false;
            pZpY = String.format("pZ[%d]", k);
        }
        else
            pZpY = String.format("pZ[%d] * pY[%d]", k, j);

        final String tableijk = String.format("table[%d]", ai);
        sb.append(String.format("%s = %s * pX[%d];\n", tableijk, pZpY, i));
        return (compound) ? tableijk : pZpY;
    }

    /**
     * Used to create the inline value function for first-order gradients
     *
     * @return the function text.
     */
    String inlineValue1()
    {
        String _pZpY;
        String _pZpYpX;
        final StringBuilder sb = new StringBuilder();

        // Gradients are described in:
        // Babcock & Zhuang (2017)
        // Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
        // Scientific Reports 7, Article number: 552
        for (int k = 0, ai = 0; k < N; k++)
            for (int j = 0; j < N; j++)
            {
                _pZpY = append_pZpY(sb, k, j);

                for (int i = 0; i < N; i++, ai++)
                {
                    _pZpYpX = append_pZpYpX(sb, _pZpY, i);

                    //@formatter:off
					sb.append(String.format("result += %s * a[%d];\n", _pZpYpX, ai));
					if (i < N_1)
						sb.append(String.format("df_da[0] += %d * %s * a[%d];\n", i+1, _pZpYpX, getIndex(i+1, j, k)));
					if (j < N_1)
						sb.append(String.format("df_da[1] += %d * %s * a[%d];\n", j+1, _pZpYpX, getIndex(i, j+1, k)));
					if (k < N_1)
						sb.append(String.format("df_da[2] += %d * %s * a[%d];\n", k+1, _pZpYpX, getIndex(i, j, k+1)));
					//@formatter:on

                    // Formal computation
                    //pZpYpX = pZ[k] * pY[j] * pX[i];
                    //result += pZpYpX * a[ai];
                    //if (i < N_1)
                    //	df_da[0] += (i+1) * pZpYpX * a[getIndex(i+1, j, k)];
                    //if (j < N_1)
                    //	df_da[1] += (j+1) * pZpYpX * a[getIndex(i, j+1, k)];
                    //if (k < N_1)
                    //	df_da[2] += (k+1) * pZpYpX * a[getIndex(i, j, k+1)];
                }
            }

        return finaliseInlineFunction(sb);
    }

    static String append_pZpYpX(StringBuilder sb, String _pZpY, int i)
    {
        String _pZpYpX;
        if (i == 0)
            _pZpYpX = _pZpY;
        else if (_pZpY.equals("1"))
            _pZpYpX = String.format("pX[%d]", i);
        else
        {
            sb.append(String.format("pZpYpX = %s * pX[%d];\n", _pZpY, i));
            _pZpYpX = "pZpYpX";
        }
        return _pZpYpX;
    }

    /**
     * Used to create the inline value function for first-order gradients with power table
     *
     * @return the function text.
     */
    String inlineValue1WithPowerTable()
    {
        final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);

        final StringBuilder sb = new StringBuilder();
        // Inline each gradient array in order.
        // Maybe it will help the optimiser?
        sb.append("df_da[0] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (i < N_1)
                        appendPower(map, sb, i + 1, j, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[1] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (j < N_1)
                        appendPower(map, sb, i, j + 1, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[2] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (k < N_1)
                        appendPower(map, sb, i, j, k + 1, i, j, k);
        sb.append(";\n");
        sb.append("return ");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    appendPower(map, sb, i, j, k, i, j, k);
        sb.append(";\n");

        // Each entry should be unique indicating that the result is optimal
        map.forEachEntry(new TObjectIntProcedure<String>()
        {
            @Override
            public boolean execute(String a, int b)
            {
                if (b > 1)
                {
                    TestLog.info(logger, "%s = %d\n", a, b);
                    return false;
                }
                return true;
            }
        });

        return finaliseInlinePowerTableFunction(sb);
    }

    static void appendPower(TObjectIntHashMap<String> map, StringBuilder sb, int i1, int j1, int k1, int i2, int j2,
            int k2)
    {
        final int after = getIndex(i2, j2, k2);
        final int before = getIndex(i1, j1, k1);
        int nh, nl;
        if (i1 != i2)
        {
            nh = i1;
            nl = i2;
        }
        else if (j1 != j2)
        {
            nh = j1;
            nl = j2;
        }
        else
        {
            nh = k1;
            nl = k2;
        }
        int n = 1;
        while (nh > nl)
        {
            n *= nh;
            nh--;
        }
        final String sum = String.format("%d * table[%d] * a[%d]\n", n, after, before);
        map.adjustOrPutValue(sum, 1, 1);
        sb.append("+ ").append(sum);
    }

    /**
     * Used to create the inline value function for first-order gradients with power table
     *
     * @return the function text.
     */
    String inlineValue1WithPowerTableN()
    {
        final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);

        final StringBuilder sb = new StringBuilder();
        // Inline each gradient array in order.
        // Maybe it will help the optimiser?
        sb.append("df_da[0] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (i < N_1)
                        appendPowerN(map, sb, i + 1, j, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[1] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (j < N_1)
                        appendPowerN(map, sb, i, j + 1, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[2] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (k < N_1)
                        appendPowerN(map, sb, i, j, k + 1, i, j, k);
        sb.append(";\n");
        sb.append("return ");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    appendPowerN(map, sb, i, j, k, i, j, k);
        sb.append(";\n");

        // Each entry should be unique indicating that the result is optimal
        map.forEachEntry(new TObjectIntProcedure<String>()
        {
            @Override
            public boolean execute(String a, int b)
            {
                if (b > 1)
                {
                    TestLog.info(logger, "%s = %d", a, b);
                    return false;
                }
                return true;
            }
        });

        return finaliseInlinePowerTableFunction(sb);
    }

    static void appendPowerN(TObjectIntHashMap<String> map, StringBuilder sb, int i1, int j1, int k1, int i2, int j2,
            int k2)
    {
        final int after = getIndex(i2, j2, k2);
        final int before = getIndex(i1, j1, k1);
        int nh, nl;
        if (i1 != i2)
        {
            nh = i1;
            nl = i2;
        }
        else if (j1 != j2)
        {
            nh = j1;
            nl = j2;
        }
        else
        {
            nh = k1;
            nl = k2;
        }
        int n = 1;
        while (nh > nl)
        {
            n *= nh;
            nh--;
        }
        final String sum = String.format("table%d[%d] * a[%d]\n", n, after, before);
        map.adjustOrPutValue(sum, 1, 1);
        sb.append("+ ").append(sum);
    }

    static String finaliseInlinePowerTableFunction(StringBuilder sb)
    {
        String result = sb.toString();
        result = result.replace("return +", "return ");
        result = result.replace("=+", "=");
        result = result.replace("1 * ", "");
        result = result.replace("table1", "table");
        return result;
    }

    /**
     * Used to create the inline value function for second-order gradients
     *
     * @return the function text.
     */
    String inlineValue2()
    {
        String _pZpY;
        String _pZpYpX;
        final StringBuilder sb = new StringBuilder();

        // Gradients are described in:
        // Babcock & Zhuang (2017)
        // Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
        // Scientific Reports 7, Article number: 552
        for (int k = 0, ai = 0; k < N; k++)
            for (int j = 0; j < N; j++)
            {
                _pZpY = append_pZpY(sb, k, j);

                for (int i = 0; i < N; i++, ai++)
                {
                    _pZpYpX = append_pZpYpX(sb, _pZpY, i);

                    //@formatter:off
					sb.append(String.format("result += %s * a[%d];\n", _pZpYpX, ai));
					if (i < N_1)
					{
						sb.append(String.format("df_da[0] += %d * %s * a[%d];\n", i+1, _pZpYpX, getIndex(i+1, j, k)));
						if (i < N_2)
							sb.append(String.format("d2f_da2[0] += %d * %s * a[%d];\n", (i+1)*(i+2), _pZpYpX, getIndex(i+2, j, k)));
					}
					if (j < N_1)
					{
						sb.append(String.format("df_da[1] += %d * %s * a[%d];\n", j+1, _pZpYpX, getIndex(i, j+1, k)));
						if (j < N_2)
							sb.append(String.format("d2f_da2[1] += %d * %s * a[%d];\n", (j+1)*(j+2), _pZpYpX, getIndex(i, j+2, k)));
					}
					if (k < N_1)
					{
						sb.append(String.format("df_da[2] += %d * %s * a[%d];\n", k+1, _pZpYpX, getIndex(i, j, k+1)));
						if (k < N_2)
							sb.append(String.format("d2f_da2[2] += %d * %s * a[%d];\n", (k+1)*(k+2), _pZpYpX, getIndex(i, j, k+2)));
					}
					//@formatter:on

                    //// Formal computation
                    //pZpYpX = pZpY * pX[i];
                    //result += pZpYpX * a[ai];
                    //if (i < N_1)
                    //{
                    //	df_da[0] += (i+1) * pZpYpX * a[getIndex(i+1, j, k)];
                    //	if (i < N_2)
                    //		d2f_da2[0] += (i+1) * (i + 2) * pZpYpX * a[getIndex(i + 2, j, k)];
                    //}
                    //if (j < N_1)
                    //{
                    //	df_da[1] += (j+1) * pZpYpX * a[getIndex(i, j+1, k)];
                    //	if (j < N_2)
                    //		d2f_da2[1] += (j+1) * (j + 2) * pZpYpX * a[getIndex(i, j + 2, k)];
                    //}
                    //if (k < N_1)
                    //{
                    //	df_da[2] += (k+1) * pZpYpX * a[getIndex(i, j, k+1)];
                    //	if (k < N_2)
                    //		d2f_da2[2] += (k+1) * (k + 2) * pZpYpX * a[getIndex(i, j, k + 2)];
                    //}
                }
            }

        return finaliseInlineFunction(sb);
    }

    /**
     * Used to create the inline value function for second-order gradients with power table
     *
     * @return the function text.
     */
    String inlineValue2WithPowerTable()
    {
        final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);
        final StringBuilder sb = new StringBuilder();
        // Inline each gradient array in order.
        // Maybe it will help the optimiser?
        sb.append("df_da[0] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (i < N_1)
                        appendPower(map, sb, i + 1, j, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[1] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (j < N_1)
                        appendPower(map, sb, i, j + 1, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[2] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (k < N_1)
                        appendPower(map, sb, i, j, k + 1, i, j, k);
        sb.append(";\n");
        sb.append("d2f_da2[0] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (i < N_2)
                        appendPower(map, sb, i + 2, j, k, i, j, k);
        sb.append(";\n");
        sb.append("d2f_da2[1] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (j < N_2)
                        appendPower(map, sb, i, j + 2, k, i, j, k);
        sb.append(";\n");
        sb.append("d2f_da2[2] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (k < N_2)
                        appendPower(map, sb, i, j, k + 2, i, j, k);
        sb.append(";\n");
        sb.append("return ");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    appendPower(map, sb, i, j, k, i, j, k);
        sb.append(";\n");

        // Each entry should be unique indicating that the result is optimal
        map.forEachEntry(new TObjectIntProcedure<String>()
        {
            @Override
            public boolean execute(String a, int b)
            {
                if (b > 1)
                {
                    TestLog.info(logger, "%s = %d", a, b);
                    return false;
                }
                return true;
            }
        });

        return finaliseInlinePowerTableFunction(sb);
    }

    /**
     * Used to create the inline value function for second-order gradients with power table
     *
     * @return the function text.
     */
    String inlineValue2WithPowerTableN()
    {
        final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);
        final StringBuilder sb = new StringBuilder();
        // Inline each gradient array in order.
        // Maybe it will help the optimiser?
        sb.append("df_da[0] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (i < N_1)
                        appendPowerN(map, sb, i + 1, j, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[1] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (j < N_1)
                        appendPowerN(map, sb, i, j + 1, k, i, j, k);
        sb.append(";\n");
        sb.append("df_da[2] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (k < N_1)
                        appendPowerN(map, sb, i, j, k + 1, i, j, k);
        sb.append(";\n");
        sb.append("d2f_da2[0] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (i < N_2)
                        appendPowerN(map, sb, i + 2, j, k, i, j, k);
        sb.append(";\n");
        sb.append("d2f_da2[1] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (j < N_2)
                        appendPowerN(map, sb, i, j + 2, k, i, j, k);
        sb.append(";\n");
        sb.append("d2f_da2[2] =");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    if (k < N_2)
                        appendPowerN(map, sb, i, j, k + 2, i, j, k);
        sb.append(";\n");
        sb.append("return ");
        for (int k = 0; k < N; k++)
            for (int j = 0; j < N; j++)
                for (int i = 0; i < N; i++)
                    appendPowerN(map, sb, i, j, k, i, j, k);
        sb.append(";\n");

        // Each entry should be unique indicating that the result is optimal
        map.forEachEntry(new TObjectIntProcedure<String>()
        {
            @Override
            public boolean execute(String a, int b)
            {
                if (b > 1)
                {
                    TestLog.info(logger, "%s = %d", a, b);
                    return false;
                }
                return true;
            }
        });

        return finaliseInlinePowerTableFunction(sb);
    }

    private final Level level = Level.FINEST;

    @Test
    public void canConstructInlineValue()
    {
        // DoubleCustomTricubicFunction#value0(double[], double[], double[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue());
    }

    @Test
    public void canConstructInlineValueWithPowerTable()
    {
        // DoubleCustomTricubicFunction#value(double[])
        // DoubleCustomTricubicFunction#value(float[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValueWithPowerTable());
    }

    @Test
    public void canConstructInlineComputePowerTable()
    {
        // CustomTricubicFunction.computePowerTable
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineComputePowerTable());
    }

    @Test
    public void canConstructInlineValue1()
    {
        // DoubleCustomTricubicFunction#value1(double[], double[], double[], double[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue1());
    }

    @Test
    public void canConstructInlineValue1WithPowerTable()
    {
        // DoubleCustomTricubicFunction#value(double[], double[])
        // DoubleCustomTricubicFunction#gradient(double[], double[])
        // DoubleCustomTricubicFunction#value(float[], double[])
        // DoubleCustomTricubicFunction#gradient(float[], double[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue1WithPowerTable());
    }

    @Test
    public void canConstructInlineValue1WithPowerTableN()
    {
        // DoubleCustomTricubicFunction#value(double[], double[], double[], double[])
        // DoubleCustomTricubicFunction#value(float[], float[], float[], double[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue1WithPowerTableN());
    }

    @Test
    public void canConstructInlineValue2()
    {
        // DoubleCustomTricubicFunction#value2(double[], double[], double[], double[], double[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue2());
    }

    @Test
    public void canConstructInlineValue2WithPowerTable()
    {
        // DoubleCustomTricubicFunction#value(double[], double[], double[])
        // DoubleCustomTricubicFunction#value(float[], double[], double[])
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue2WithPowerTable());
    }

    @Test
    public void canConstructInlineValue2WithPowerTableN()
    {
        // DoubleCustomTricubicFunction#value(double[], double[], double[], double[], double[],
        // DoubleCustomTricubicFunction#value(float[], float[], float[], float[], double[],
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineValue2WithPowerTableN());
    }
}
