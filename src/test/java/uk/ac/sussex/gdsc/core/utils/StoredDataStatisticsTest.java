package uk.ac.sussex.gdsc.core.utils;

import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RNGFactory;
import uk.ac.sussex.gdsc.test.utils.DataCache;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLog;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({ "javadoc" })
public class StoredDataStatisticsTest extends StatisticsTest implements Function<RandomSeed, StoredDataStatistics>
{
    private static Logger logger;
    private static DataCache<RandomSeed, StoredDataStatistics> dataCache;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(StoredDataStatisticsTest.class.getName());
        dataCache = new DataCache<>();
    }

    @AfterAll
    public static void afterAll()
    {
        dataCache.clear();
        dataCache = null;
        logger = null;
    }

    final int n = 10000;
    final int loops = 100;

    @Override
    public StoredDataStatistics apply(RandomSeed seed)
    {
        final UniformRandomProvider r = RNGFactory.create(seed.getSeed());
        final StoredDataStatistics stats = new StoredDataStatistics(n);
        for (int i = 0; i < n; i++)
            stats.add(r.nextDouble());
        return stats;
    }

    @SeededTest
    public void getValuesEqualsIterator(RandomSeed seed)
    {
        final StoredDataStatistics stats = dataCache.getOrComputeIfAbsent(seed, this);

        final double[] values = stats.getValues();
        int i = 0;
        for (final double d : stats)
            Assertions.assertEquals(d, values[i++]);
    }

    // These speed tests are weak. A JMH benchmark would be better.

    @SpeedTag
    @SeededTest
    public void forLoopIsSlowerThanValuesIterator(RandomSeed seed)
    {
        // This fails. Perhaps change the test to use the TimingService for repeat testing.
        ExtraAssumptions.assume(TestComplexity.MEDIUM);

        final StoredDataStatistics stats = dataCache.getOrComputeIfAbsent(seed, this);

        double total = 0;
        long start1 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            total = 0;
            final double[] values = stats.getValues();
            for (int j = 0; j < values.length; j++)
                total += values[j];
        }
        start1 = System.nanoTime() - start1;
        logger.finest(FunctionUtils.getSupplier("Total = %s", total));

        long start2 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            total = 0;
            for (final double d : stats.getValues())
                total += d;
        }
        start2 = System.nanoTime() - start2;
        logger.finest(FunctionUtils.getSupplier("Total = %s", total));

        logger.log(TestLog.getTimingRecord("for (double d : stats.getValues())", start2,
                "for (int j = 0; j < values.length; j++)", start1));
    }

    @SpeedTag
    @SeededTest
    public void iteratorIsSlowerUsingdouble(RandomSeed seed)
    {
        ExtraAssumptions.assume(TestComplexity.MEDIUM);

        final StoredDataStatistics stats = dataCache.getOrComputeIfAbsent(seed, this);

        double total = 0;
        long start1 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            total = 0;
            for (final double d : stats.getValues())
            {
                total += d;
            }
        }
        start1 = System.nanoTime() - start1;
        logger.finest(FunctionUtils.getSupplier("Total = %s", total));

        long start2 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            total = 0;
            for (final double d : stats)
            {
                total += d;
            }
        }
        start2 = System.nanoTime() - start2;
        logger.finest(FunctionUtils.getSupplier("Total = %s", total));

        logger.log(TestLog.getTimingRecord("for (double d : stats)", start2, "for (double d : stats.getValues)", start1));
    }

    @SpeedTag
    @SeededTest
    public void iteratorIsSlowerUsingDouble(RandomSeed seed)
    {
        ExtraAssumptions.assume(TestComplexity.MEDIUM);

        final StoredDataStatistics stats = dataCache.getOrComputeIfAbsent(seed, this);

        double total = 0;
        long start1 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            total = 0;
            for (final double d : stats.getValues())
            {
                total += d;
            }
        }
        start1 = System.nanoTime() - start1;
        logger.finest(FunctionUtils.getSupplier("Total = %s", total));

        long start2 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            total = 0;
            for (final Double d : stats)
            {
                total += d;
            }
        }
        start2 = System.nanoTime() - start2;
        logger.finest(FunctionUtils.getSupplier("Total = %s", total));

        logger.log(TestLog.getTimingRecord("for (Double d : stats)", start2, "for (double d : stats.getValues)", start1));
    }

    @Test
    public void canConstructWithData()
    {
        // This requires that the constructor correctly initialises the storage
        StoredDataStatistics s;
        s = new StoredDataStatistics(new double[] { 1, 2, 3 });
        s.add(1d);
        s = new StoredDataStatistics(new float[] { 1, 2, 3 });
        s.add(1f);
        s = new StoredDataStatistics(new int[] { 1, 2, 3 });
        s.add(1);
    }
}
