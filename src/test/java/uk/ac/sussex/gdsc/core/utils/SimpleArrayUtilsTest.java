package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gnu.trove.set.hash.TIntHashSet;
import uk.ac.sussex.gdsc.test.BaseTimingTask;
import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.TimingService;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

@SuppressWarnings({ "javadoc" })
public class SimpleArrayUtilsTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(SimpleArrayUtilsTest.class.getName());
    }

    @AfterAll
    public static void afterAll()
    {
        logger = null;
    }

    @SeededTest
    public void canFlatten(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        final TIntHashSet set = new TIntHashSet();
        testFlatten(set, new int[0]);
        testFlatten(set, new int[10]);
        for (int i = 0; i < 10; i++)
        {
            testFlatten(set, next(r, 1, 10));
            testFlatten(set, next(r, 10, 10));
            testFlatten(set, next(r, 100, 10));
        }
    }

    private static void testFlatten(TIntHashSet set, int[] s1)
    {
        set.clear();
        set.addAll(s1);
        final int[] e = set.toArray();
        Arrays.sort(e);

        final int[] o = SimpleArrayUtils.flatten(s1);
        //TestLog.debug(logger,"%s =? %s", Arrays.toString(e), Arrays.toString(o));
        Assertions.assertArrayEquals(e, o);
    }

    @SeededTest
    public void canSortMerge(RandomSeed seed)
    {
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
        final TIntHashSet set = new TIntHashSet();
        testSortMerge(set, new int[0], new int[10]);
        testSortMerge(set, new int[10], new int[10]);
        testSortMerge(set, new int[10], next(r, 10, 10));
        for (int i = 0; i < 10; i++)
        {
            testSortMerge(set, next(r, 1, 10), next(r, 1, 10));
            testSortMerge(set, next(r, 10, 10), next(r, 1, 10));
            testSortMerge(set, next(r, 100, 10), next(r, 1, 10));
            testSortMerge(set, next(r, 1, 10), next(r, 10, 10));
            testSortMerge(set, next(r, 10, 10), next(r, 10, 10));
            testSortMerge(set, next(r, 100, 10), next(r, 10, 10));
            testSortMerge(set, next(r, 1, 10), next(r, 100, 10));
            testSortMerge(set, next(r, 10, 10), next(r, 100, 10));
            testSortMerge(set, next(r, 100, 10), next(r, 100, 10));
        }
    }

    @SuppressWarnings("deprecation")
    private static void testSortMerge(TIntHashSet set, int[] s1, int[] s2)
    {
        set.clear();
        set.addAll(s1);
        set.addAll(s2);
        final int[] e = set.toArray();
        Arrays.sort(e);

        final int[] o = SimpleArrayUtils.sortMerge(s1, s2);
        //TestLog.debug(logger,"%s =? %s", Arrays.toString(e), Arrays.toString(o));
        Assertions.assertArrayEquals(e, o);
    }

    private static int[] next(UniformRandomProvider r, int size, int max)
    {
        final int[] a = new int[size];
        for (int i = 0; i < size; i++)
            a[i] = r.nextInt(max);
        return a;
    }

    private abstract class MyTimingTask extends BaseTimingTask
    {
        int[][][] data;

        public MyTimingTask(String name, int[][][] data)
        {
            super(name);
            this.data = data;
        }

        @Override
        public int getSize()
        {
            return data.length;
        }

        @Override
        public Object getData(int i)
        {
            return new int[][] { data[i][0].clone(), data[i][1].clone() };
        }
    }

    @SeededTest
    public void testMergeOnIndexData(RandomSeed seed)
    {
        ExtraAssumptions.assume(logger, Level.INFO);
        ExtraAssumptions.assume(TestComplexity.MEDIUM);
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());

        final double[] f = new double[] { 0.1, 0.5, 0.75 };
        for (final int size : new int[] { 100, 1000, 10000 })
            for (int i = 0; i < f.length; i++)
                for (int j = i; j < f.length; j++)
                    testMergeOnIndexData(r, 100, size, (int) (size * f[i]), (int) (size * f[j]));
    }

    private void testMergeOnIndexData(UniformRandomProvider r, int length, final int size, final int n1, final int n2)
    {
        final int[][][] data = new int[length][2][];
        final int[] s1 = SimpleArrayUtils.newArray(size, 0, 1);
        for (int i = 0; i < length; i++)
        {
            PermutationSampler.shuffle(r, s1);
            data[i][0] = Arrays.copyOf(s1, n1);
            PermutationSampler.shuffle(r, s1);
            data[i][1] = Arrays.copyOf(s1, n2);
        }
        final String msg = String.format("[%d] %d vs %d", size, n1, n2);

        final TimingService ts = new TimingService();
        ts.execute(new MyTimingTask("SortMerge" + msg, data)
        {
            @Override
            @SuppressWarnings("deprecation")
            public Object run(Object data)
            {
                final int[][] d = (int[][]) data;
                return SimpleArrayUtils.sortMerge(d[0], d[1]);
            }
        });
        ts.execute(new MyTimingTask("merge+sort" + msg, data)
        {
            @Override
            public Object run(Object data)
            {
                final int[][] d = (int[][]) data;
                final int[] a = SimpleArrayUtils.merge(d[0], d[1]);
                Arrays.sort(a);
                return a;
            }
        });
        ts.execute(new MyTimingTask("merge+sort unique" + msg, data)
        {
            @Override
            public Object run(Object data)
            {
                final int[][] d = (int[][]) data;
                final int[] a = SimpleArrayUtils.merge(d[0], d[1], true);
                Arrays.sort(a);
                return a;
            }
        });

        ts.repeat(ts.getSize());
        ts.report(logger);
    }

    @SeededTest
    public void testMergeOnRedundantData(RandomSeed seed)
    {
        ExtraAssumptions.assume(logger, Level.INFO);
        ExtraAssumptions.assume(TestComplexity.MEDIUM);
        final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());

        final int[] n = new int[] { 2, 4, 8 };
        final int[] size = new int[] { 100, 1000, 10000 };

        for (int i = 0; i < n.length; i++)
            for (int j = i; j < n.length; j++)
                for (int ii = 0; ii < size.length; ii++)
                    for (int jj = ii; jj < size.length; jj++)
                        testMergeOnRedundantData(r, 50, size[ii], n[i], size[jj], n[j]);
    }

    public void testMergeOnRedundantData(UniformRandomProvider r, int length, final int n1, final int r1, final int n2,
            final int r2)
    {
        final int[][][] data = new int[length][2][];
        final int[] s1 = new int[n1];
        for (int i = 0; i < n1; i++)
            s1[i] = i % r1;
        final int[] s2 = new int[n2];
        for (int i = 0; i < n2; i++)
            s2[i] = i % r2;
        for (int i = 0; i < length; i++)
        {
            PermutationSampler.shuffle(r, s1);
            data[i][0] = s1.clone();
            PermutationSampler.shuffle(r, s2);
            data[i][1] = s2.clone();
        }
        final String msg = String.format("%d%%%d vs %d%%%d", n1, r1, n2, r2);

        final TimingService ts = new TimingService();
        ts.execute(new MyTimingTask("SortMerge" + msg, data)
        {
            @Override
            @SuppressWarnings("deprecation")
            public Object run(Object data)
            {
                final int[][] d = (int[][]) data;
                return SimpleArrayUtils.sortMerge(d[0], d[1]);
            }
        });
        ts.execute(new MyTimingTask("merge+sort" + msg, data)
        {
            @Override
            public Object run(Object data)
            {
                final int[][] d = (int[][]) data;
                final int[] a = SimpleArrayUtils.merge(d[0], d[1]);
                Arrays.sort(a);
                return a;
            }
        });

        ts.repeat(ts.getSize());
        ts.report(logger);
    }

    @Test
    public void canGetRanges()
    {
        testGetRanges(null, new int[0]);
        testGetRanges(new int[0], new int[0]);
        testGetRanges(new int[] { 0 }, new int[] { 0, 0 });
        testGetRanges(new int[] { 1 }, new int[] { 1, 1 });
        testGetRanges(new int[] { 0, 1 }, new int[] { 0, 1 });
        testGetRanges(new int[] { 0, 1, 2, 3 }, new int[] { 0, 3 });
        testGetRanges(new int[] { 0, 1, 3, 4, 5, 7 }, new int[] { 0, 1, 3, 5, 7, 7 });
        testGetRanges(new int[] { 0, 3, 5, 7 }, new int[] { 0, 0, 3, 3, 5, 5, 7, 7 });
        testGetRanges(new int[] { -1, 0, 1 }, new int[] { -1, 1 });
        testGetRanges(new int[] { -2, -1, 1 }, new int[] { -2, -1, 1, 1 });

        // With duplicates
        testGetRanges(new int[] { 0 }, new int[] { 0, 0 });
        testGetRanges(new int[] { 1 }, new int[] { 1, 1 });
        testGetRanges(new int[] { 0, 1 }, new int[] { 0, 1 });
        testGetRanges(new int[] { 0, 1, 2, 3 }, new int[] { 0, 3 });
        testGetRanges(new int[] { 0, 1, 3, 4, 5, 7 }, new int[] { 0, 1, 3, 5, 7, 7 });
        testGetRanges(new int[] { 0, 3, 5, 7 }, new int[] { 0, 0, 3, 3, 5, 5, 7, 7 });
        testGetRanges(new int[] { -1, 0, 1 }, new int[] { -1, 1 });
        testGetRanges(new int[] { -2, -1, 1 }, new int[] { -2, -1, 1, 1 });
    }

    @Test
    public void canGetRangesWithDuplicates()
    {
        testGetRanges(new int[] { 0, 0, 0 }, new int[] { 0, 0 });
        testGetRanges(new int[] { 1, 1 }, new int[] { 1, 1 });
        testGetRanges(new int[] { 0, 1, 1 }, new int[] { 0, 1 });
        testGetRanges(new int[] { 0, 1, 2, 2, 2, 3, 3 }, new int[] { 0, 3 });
        testGetRanges(new int[] { 0, 1, 1, 3, 3, 4, 5, 7, 7 }, new int[] { 0, 1, 3, 5, 7, 7 });
        testGetRanges(new int[] { 0, 3, 5, 5, 5, 7 }, new int[] { 0, 0, 3, 3, 5, 5, 7, 7 });
        testGetRanges(new int[] { -1, 0, 0, 0, 1, 1 }, new int[] { -1, 1 });
        testGetRanges(new int[] { -2, -2, -1, 1 }, new int[] { -2, -1, 1, 1 });
    }

    private static void testGetRanges(int[] in, int[] e)
    {
        final int[] o = SimpleArrayUtils.getRanges(in);
        //TestLog.debug(logger,"%s =? %s", Arrays.toString(e), Arrays.toString(o));
        Assertions.assertArrayEquals(e, o);
    }

    @Test
    public void canToString()
    {
        Assertions.assertEquals("null", SimpleArrayUtils.toString(null));

        Assertions.assertEquals("[0.5, 1.0]", SimpleArrayUtils.toString(new float[] { 0.5f, 1f }));
        Assertions.assertEquals("[0.5, 1.0]", SimpleArrayUtils.toString(new double[] { 0.5, 1 }));

        Assertions.assertEquals("[c, a]", SimpleArrayUtils.toString(new char[] { 'c', 'a' }));

        Assertions.assertEquals("[true, false]", SimpleArrayUtils.toString(new boolean[] { true, false }));

        Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new byte[] { 2, 1 }));
        Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new short[] { 2, 1 }));
        Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new int[] { 2, 1 }));
        Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new long[] { 2, 1 }));

        // Check objects
        Assertions.assertEquals("[2, 1]", SimpleArrayUtils.toString(new Object[] { 2, 1 }));
        Assertions.assertEquals("[foo, bar]", SimpleArrayUtils.toString(new Object[] { "foo", "bar" }));
        Assertions.assertEquals("[foo, 1]", SimpleArrayUtils.toString(new Object[] { "foo", 1 }));

        // Check recursion
        Assertions.assertEquals("[[2, 1], [3, 4]]", SimpleArrayUtils.toString(new int[][] { { 2, 1 }, { 3, 4 } }));
    }
}
