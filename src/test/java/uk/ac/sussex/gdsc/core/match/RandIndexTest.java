package uk.ac.sussex.gdsc.core.match;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

@SuppressWarnings({ "javadoc" })
public class RandIndexTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(RandIndexTest.class.getName());
    }

    @AfterAll
    public static void afterAll()
    {
        logger = null;
    }

    @Test
    public void canComputeSimpleRandIndexWithNoData()
    {
        for (final int size : new int[] { 0, 1, 2 })
        {
            final double e = (size == 0) ? 0 : 1;
            final int[] clusters = new int[size];
            final double r = RandIndex.simpleRandIndex(clusters, clusters);
            Assertions.assertEquals(e, r);
        }
    }

    @Test
    public void canComputeRandIndexWithNoData()
    {
        for (final int size : new int[] { 0, 1, 2 })
        {
            final double e = (size == 0) ? 0 : 1;
            final int[] clusters = new int[size];
            final double r = RandIndex.randIndex(clusters, clusters);
            Assertions.assertEquals(e, r);
        }
    }

    @Test
    public void canComputeRandIndex2WithNoData()
    {
        for (final int size : new int[] { 0, 1, 2 })
        {
            final double e = (size == 0) ? 0 : 1;
            final int[] clusters = new int[size];
            final double r = RandIndex.randIndex(clusters, 1, clusters, 1);
            Assertions.assertEquals(e, r);
        }
    }

    @Test
    public void canComputeAdjustedRandIndexWithNoData()
    {
        for (final int size : new int[] { 0, 1, 2 })
        {
            final double e = (size == 0) ? 0 : 1;
            final int[] clusters = new int[size];
            final double r = RandIndex.adjustedRandIndex(clusters, 1, clusters, 1);
            Assertions.assertEquals(e, r);
        }
    }

    @Test
    public void canComputeAdjustedRandIndexWhenNoRandomness()
    {
        // Q. should this be zero?
        final double e = 1;

        final int[] clusters = new int[2];
        clusters[1] = 1;
        final double r = RandIndex.adjustedRandIndex(clusters, 2, clusters, 2);
        Assertions.assertEquals(e, r);
    }

    // The example data and answer are from:
    // http://stats.stackexchange.com/questions/89030/rand-index-calculation

    @Test
    public void canComputeSimpleRandIndex()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
        final double r = RandIndex.simpleRandIndex(clusters, classes);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndex()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
        final double r = RandIndex.randIndex(clusters, classes);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndexWithArbitraryClusterNumbers()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
        final double r = RandIndex.simpleRandIndex(clusters, classes);

        //@formatter:off
		final int[][] maps = new int[][] {
			{ 0,1,2 },
			{ 0,2,1 },
			{ 1,0,2 },
			{ 1,2,0 },
			{ 2,0,1 },
			{ 2,1,0 },
		};
		//@formatter:on
        final RandIndex ri = new RandIndex();
        for (final int[] map : maps)
        {
            final int[] c2 = new int[classes.length];
            for (int i = 0; i < c2.length; i++)
                c2[i] = map[classes[i]];
            Assertions.assertEquals(r, ri.getRandIndex(clusters, 3, c2, 3));
        }
    }

    @Test
    public void canComputeRandIndex2()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
        final double r = RandIndex.randIndex(clusters, 3, classes, 3);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeSimpleRandIndexWithNegativeData()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
        final double r = RandIndex.simpleRandIndex(clusters, classes);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndexWithNegativeData()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
        final double r = RandIndex.randIndex(clusters, classes);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndex2WithNegativeData()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
        final double r = RandIndex.randIndex(clusters, 3, classes, 3);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndexWhenInvalidNClusters()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
        final double r = RandIndex.randIndex(clusters, 2, classes, 3);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeAdjustedRandIndexWhenInvalidNClusters()
    {
        final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
        final double e = RandIndex.adjustedRandIndex(clusters, 3, classes, 3);
        final double o = RandIndex.adjustedRandIndex(clusters, 2, classes, 3);
        Assertions.assertEquals(e, o);
    }

    @Test
    public void canComputeSimpleRandIndexWithSparseData()
    {
        final int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
        final double r = RandIndex.simpleRandIndex(clusters, classes);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndexWithSparseData()
    {
        final int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
        final double r = RandIndex.randIndex(clusters, classes);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void canComputeRandIndex2WithSparseData()
    {
        final int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
        final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
        final double r = RandIndex.randIndex(clusters, 7, classes, 10);
        Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    }

    @Test
    public void getRandIndexThrowsWhenNotComputed()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            final RandIndex ri = new RandIndex();
            ri.getRandIndex();
        });
    }

    @Test
    public void getAdjustedRandIndexThrowsWhenNotComputed()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            final RandIndex ri = new RandIndex();
            ri.getAdjustedRandIndex();
        });
    }

    @SeededTest
    public void canComputeRandIndexWithSimpleData(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int size = 100;
        for (final int n1 : new int[] { 1, 2, 3, 4, 5 })
            for (final int n2 : new int[] { 1, 2, 3, 4, 5 })
                canComputeRandIndexWithData(rg, size, n1, n2);
    }

    @SeededTest
    public void canComputeRandIndexWithBigData(RandomSeed seed)
    {
        ExtraAssumptions.assumeLowComplexity();
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int size = 10000;
        for (final int i : new int[] { 3, 5, 10 })
        {
            final int n1 = size / i;
            final int n2 = size / i;
            canComputeRandIndexWithData(rg, size, n1, n2);
        }
        for (final int i : new int[] { 3, 5, 10 })
        {
            final int n1 = size / i;
            final int n2 = i;
            canComputeRandIndexWithData(rg, size, n1, n2);
        }
        for (final int i : new int[] { 3, 5, 10 })
        {
            final int n1 = i;
            final int n2 = i;
            canComputeRandIndexWithData(rg, size, n1, n2);
        }
    }

    private static void canComputeRandIndexWithData(UniformRandomProvider rg, int size, int n1, int n2)
    {
        final int n = size;
        final int[] c1 = new int[size];
        final int[] c2 = new int[size];
        while (size-- > 0)
        {
            c1[size] = size % n1;
            c2[size] = size % n2;
        }
        PermutationSampler.shuffle(rg, c1);

        final long t1 = System.nanoTime();
        final double e = RandIndex.simpleRandIndex(c1, c2);
        final long t2 = System.nanoTime();
        final double o1 = RandIndex.randIndex(c1, c2);
        final long t3 = System.nanoTime();
        final double o2 = RandIndex.randIndex(c1, n1, c2, n2);
        final long t4 = System.nanoTime();

        final long simple = t2 - t1;
        final long table1 = t3 - t2;
        final long table2 = t4 - t3;

        logger.log(TestLog.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), table1=%d (%f), %f", n, n1, n2, simple, e, table1, o1,
                simple / (double) table1));
        logger.log(TestLog.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), table2=%d (%f), %f", n, n1, n2, simple, e, table2, o2,
                simple / (double) table2));

        ExtraAssertions.assertEqualsRelative(e, o1, 1e-10);
        Assertions.assertEquals(o2, o1);
    }

    @SeededTest
    public void adjustedRandIndexIsZeroForRandomData(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int size = 100;
        for (final int n1 : new int[] { 2, 5, 10 })
            for (final int n2 : new int[] { 2, 5 })
                adjustedRandIndexIsZeroForRandomData(rg, size, n1, n2, 10);
    }

    private static void adjustedRandIndexIsZeroForRandomData(UniformRandomProvider rg, int size, int n1, int n2,
            int loops)
    {
        final int n = size;
        final int[] c1 = new int[size];
        final int[] c2 = new int[size];
        while (size-- > 0)
        {
            c1[size] = size % n1;
            c2[size] = size % n2;
        }
        final RandIndex ri = new RandIndex();

        double sum = 0;
        for (int i = loops; i-- > 0;)
        {
            PermutationSampler.shuffle(rg, c1);
            sum += ri.getAdjustedRandIndex(c1, n1, c2, n2);
        }

        sum /= loops;
        logger.log(TestLog.getRecord(Level.FINE, "[%d,%d,%d,%d] %f", n, n1, n2, loops, sum));

        final double delta = 0.1;
        Assertions.assertTrue(sum < delta && sum > -delta);
    }

    @SeededTest
    public void canComputeAdjustedRandIndexWithSimpleData(RandomSeed seed)
    {
        final int size = 100;
        for (final int n1 : new int[] { 1, 2, 3, 4, 5 })
            for (final int n2 : new int[] { 1, 2, 3, 4, 5 })
                canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
    }

    // Speed test on large data
    @SeededTest
    public void canComputeAdjustedRandIndexWithBigData(RandomSeed seed)
    {
        final int size = 10000;
        for (final int i : new int[] { 3, 5, 10 })
        {
            final int n1 = size / i;
            final int n2 = size / i;
            canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
        }
        for (final int i : new int[] { 3, 5, 10 })
        {
            final int n1 = size / i;
            final int n2 = i;
            canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
        }
        for (final int i : new int[] { 3, 5, 10 })
        {
            final int n1 = i;
            final int n2 = i;
            canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
        }
    }

    private static void canComputeAdjustedRandIndexWithData(RandomSeed seed, int size, int n1, int n2)
    {
        final int n = size;
        final int[] c1 = new int[size];
        final int[] c2 = new int[size];
        while (size-- > 0)
        {
            c1[size] = size % n1;
            c2[size] = size % n2;
        }
        final UniformRandomProvider rand = TestSettings.getRandomGenerator(seed.getSeed());
        PermutationSampler.shuffle(rand, c1);

        final RandIndex ri = new RandIndex();

        final long t1 = System.nanoTime();
        final double o1 = ri.getAdjustedRandIndex(c1, c2);
        final long t2 = System.nanoTime();
        final double o2 = ri.getAdjustedRandIndex(c1, n1, c2, n2);
        final long t3 = System.nanoTime();

        final double r = ri.getRandIndex();

        final long table1 = t2 - t1;
        final long table2 = t3 - t2;

        logger.log(TestLog.getRecord(Level.FINE, "[%d,%d,%d] table1=%d (%f [%f]), table2=%d (%f), %f", n, n1, n2, table1, o1, r, table2, o2,
                table1 / (double) table2));

        Assertions.assertEquals(o2, o1);
    }
}
