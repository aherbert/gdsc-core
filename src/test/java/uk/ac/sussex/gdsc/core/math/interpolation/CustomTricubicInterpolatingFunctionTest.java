package uk.ac.sussex.gdsc.core.math.interpolation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import uk.ac.sussex.gdsc.core.utils.Sort;
import uk.ac.sussex.gdsc.test.BaseTimingTask;
import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.TimingService;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

/**
 * This class is used to in-line the computation for the CustomTricubicInterpolatingFunction
 */
@SuppressWarnings({ "javadoc" })
public class CustomTricubicInterpolatingFunctionTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(CustomTricubicInterpolatingFunctionTest.class.getName());
    }

    @AfterAll
    public static void afterAll()
    {
        logger = null;
    }

    static String inlineComputeCoefficients()
    {
        final StringBuilder sb = new StringBuilder();

        final int sz = 64;

        sb.append(String.format("final double[] a = new double[%d];\n", sz));

        for (int i = 0; i < sz; i++)
        {
            sb.append(String.format("a[%d]=", i));

            final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
            for (int j = 0; j < sz; j++)
            {
                final double d = row[j];
                if (d != 0)
                {
                    if (d > 0)
                        sb.append('+');
                    final int di = (int) Math.floor(d);
                    if (di == d)
                        sb.append(String.format("%d*beta[%d]", di, j));
                    else
                        sb.append(String.format("%f*beta[%d]", d, j));
                }
            }
            sb.append(String.format(";\n", i));
        }
        sb.append("return a;\n");

        return finialise(sb);
    }

    static String inlineComputeCoefficientsCollectTerms()
    {
        final StringBuilder sb = new StringBuilder();

        final int sz = 64;

        // Require integer coefficients
        int max = 0;
        for (int i = 0; i < sz; i++)
        {
            final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
            for (int j = 0; j < sz; j++)
            {
                final double d = row[j];
                if (d != 0)
                {
                    final int di = (int) Math.floor(d);
                    if (di != d)
                        return null;
                    if (max < Math.abs(di))
                        max = Math.abs(di);
                }
            }
        }

        final TIntObjectHashMap<TIntArrayList> map = new TIntObjectHashMap<>(max + 1);

        sb.append(String.format("final double[] a = new double[%d];\n", sz));

        for (int i = 0; i < sz; i++)
        {
            map.clear();
            final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
            for (int j = 0; j < sz; j++)
            {
                final double d = row[j];
                if (d != 0)
                {
                    final int di = (int) Math.floor(d);
                    final int key = Math.abs(di);
                    // Check if contains either positive or negative key
                    TIntArrayList value = map.get(key);
                    if (value == null)
                    {
                        value = new TIntArrayList();
                        map.put(key, value);
                    }
                    // Store the index and the sign.
                    // We use 1-based index so we can store -0
                    value.add(((di < 0) ? -1 : 1) * (j + 1));
                }
            }

            sb.append(String.format("a[%d]=", i));

            // Collect terms
            map.forEachEntry(new TIntObjectProcedure<TIntArrayList>()
            {
                @Override
                public boolean execute(int key, TIntArrayList value)
                {
                    final int[] js = value.toArray(); // Signed j
                    final int[] j = js.clone(); // Unsigned j
                    for (int i = 0; i < j.length; i++)
                        j[i] = Math.abs(j[i]);

                    Sort.sortArrays(js, j, true);

                    // Check if starting with negative
                    char add = '+';
                    char sub = '-';

                    if (js[0] < 0)
                    {
                        // Subtract the set
                        sb.append('-');
                        if (key > 1)
                            sb.append(key).append('*');
                        // Swap signs
                        add = sub;
                        sub = '+';
                    }
                    else
                    {
                        // Some positive so add the set
                        sb.append('+');
                        if (key > 1)
                            sb.append(key).append('*');
                    }

                    if (js.length != 1)
                        sb.append('(');
                    for (int i = 0; i < js.length; i++)
                    {
                        if (i != 0)
                            if (js[i] < 0)
                                sb.append(sub);
                            else
                                sb.append(add);
                        // Convert 1-based index back to 0-based
                        sb.append("beta[").append(Math.abs(js[i]) - 1).append(']');
                    }
                    if (js.length != 1)
                        sb.append(')');
                    return true;
                }
            });

            sb.append(String.format(";\n", i));
        }
        sb.append("return a;\n");

        return finialise(sb);
    }

    private static String finialise(final StringBuilder sb)
    {
        String result = sb.toString();
        result = result.replaceAll("\\+1\\*", "+");
        result = result.replaceAll("-1\\*", "-");
        result = result.replaceAll("=\\+", "=");
        result = result.replaceAll("=\\-", "=-");
        return result;
    }

    private final Level level = Level.FINEST;

    @Test
    public void canConstructInlineComputeCoefficients()
    {
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineComputeCoefficients());
    }

    @Test
    public void canConstructInlineComputeCoefficientsCollectTerms()
    {
        ExtraAssumptions.assume(logger, level);
        TestLog.log(logger, level, inlineComputeCoefficientsCollectTerms());
    }

    private abstract class MyTimingTask extends BaseTimingTask
    {
        double[][] a;

        public MyTimingTask(String name, double[][] a)
        {
            super(name);
            this.a = a;
        }

        @Override
        public int getSize()
        {
            return 1;
        }

        @Override
        public Object getData(int i)
        {
            return null;
        }

        @Override
        public void check(int i, Object result)
        {
            final double[][] b = (double[][]) result;
            ExtraAssertions.assertArrayEqualsRelative(a, b, 1e-6, getName());
        }
    }

    @SeededTest
    public void inlineComputeCoefficientsIsFaster(RandomSeed seed)
    {
        ExtraAssumptions.assume(TestComplexity.MEDIUM);

        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());

        final int N = 3000;
        final double[][] tables = new double[N][];
        final double[][] a = new double[N][];
        for (int i = 0; i < tables.length; i++)
        {
            final double[] table = new double[64];
            for (int j = 0; j < 64; j++)
                table[j] = r.nextDouble();
            tables[i] = table;
            a[i] = CustomTricubicInterpolatingFunction.computeCoefficients(table);
        }

        final TimingService ts = new TimingService();

        ts.execute(new MyTimingTask("Standard", a)
        {
            @Override
            public Object run(Object data)
            {
                final double[][] a = new double[N][];
                for (int i = 0; i < N; i++)
                    a[i] = CustomTricubicInterpolatingFunction.computeCoefficients(tables[i]);
                return a;
            }
        });
        ts.execute(new MyTimingTask("Inline", a)
        {
            @Override
            public Object run(Object data)
            {
                final double[][] a = new double[N][];
                for (int i = 0; i < N; i++)
                    a[i] = CustomTricubicInterpolatingFunction.computeCoefficientsInline(tables[i]);
                return a;
            }
        });
        ts.execute(new MyTimingTask("InlineCollectTerms", a)
        {
            @Override
            public Object run(Object data)
            {
                final double[][] a = new double[N][];
                for (int i = 0; i < N; i++)
                    a[i] = CustomTricubicInterpolatingFunction.computeCoefficientsInlineCollectTerms(tables[i]);
                return a;
            }
        });

        final int n = ts.getSize();
        ts.check();
        ts.repeat();
        if (logger.isLoggable(Level.INFO))
            //ts.report(logger);
            ts.report(logger, n);

        Assertions.assertTrue(ts.get(-1).getMean() < ts.get(-n).getMean(),
                () -> String.format("%f vs %f", ts.get(-1).getMean(), ts.get(-n).getMean()));
    }
}
