package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class MedianWindowTest
{
    private static Logger logger;

    @BeforeAll
    public static void beforeAll()
    {
        logger = Logger.getLogger(MedianWindowTest.class.getName());
    }

    @AfterAll
    public static void afterAll()
    {
        logger = null;
    }

    int dataSize = 2000;
    int[] radii = new int[] { 0, 1, 2, 4, 8, 16 };
    double[] values = new double[] { 0, -1.1, 2.2 };
    int[] speedRadii = new int[] { 16, 32, 64 };
    int testSpeedRadius = speedRadii[speedRadii.length - 1];
    int[] speedIncrement = new int[] { 1, 2, 4, 8, 16 };

    @SeededTest
    public void testClassCanComputeActualMedian(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());

        // Verify the internal median method using the Apache commons maths library

        double[] data = createRandomData(rg, dataSize);
        for (final int radius : radii)
            for (int i = 0; i < data.length; i++)
            {
                final double median = calculateMedian(data, i, radius);
                final double median2 = calculateMedian2(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);
            }
        data = createRandomData(rg, dataSize + 1);
        for (final int radius : radii)
            for (int i = 0; i < data.length; i++)
            {
                final double median = calculateMedian(data, i, radius);
                final double median2 = calculateMedian2(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);
            }
    }

    @Test
    public void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsAboveMedian()
    {
        final double[] data = new double[] { 1, 2, 3, 4, 5 };

        final MedianWindowDLL mw = new MedianWindowDLL(data);
        double median = mw.getMedian();
        double median2 = calculateMedian(data, 2, 2);
        Assertions.assertEquals(median2, median, 1e-6, "Before insert");

        mw.add(6);
        median = mw.getMedian();
        data[0] = 6;
        median2 = calculateMedian(data, 2, 2);
        Assertions.assertEquals(median2, median, 1e-6, "After insert");
    }

    @SeededTest
    public void canComputeMedianForRandomDataUsingDynamicLinkedList(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final double[] data = createRandomData(rg, dataSize);
        for (final int radius : radii)
        {
            final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
            final MedianWindowDLL mw = new MedianWindowDLL(startData);
            for (int i = radius, j = startData.length; j < data.length; i++, j++)
            {
                final double median = mw.getMedian();
                mw.add(data[j]);
                final double median2 = calculateMedian(data, i, radius);
                TestLog.info(logger, "Position %d, Radius %d : %g vs %g", i, radius, median2, median);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);
            }
        }
    }

    @SeededTest
    public void canComputeMedianForRandomDataUsingSingleIncrement(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        canComputeMedianForDataUsingSingleIncrement(createRandomData(rg, dataSize));
    }

    @SeededTest
    public void canComputeMedianForRandomDataUsingSetPosition(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        canComputeMedianForDataUsingSetPosition(createRandomData(rg, dataSize));
    }

    @SeededTest
    public void canComputeMedianForRandomDataUsingBigIncrement(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        canComputeMedianForDataUsingBigIncrement(createRandomData(rg, dataSize));
    }

    @Test
    public void canComputeMedianForDuplicateDataUsingSingleIncrement()
    {
        for (final double value : values)
            canComputeMedianForDataUsingSingleIncrement(createDuplicateData(dataSize, value));
    }

    @Test
    public void canComputeMedianForDuplicateDataUsingSetPosition()
    {
        for (final double value : values)
            canComputeMedianForDataUsingSetPosition(createDuplicateData(dataSize, value));
    }

    @Test
    public void canComputeMedianForDuplicateDataUsingBigIncrement()
    {
        for (final double value : values)
            canComputeMedianForDataUsingBigIncrement(createDuplicateData(dataSize, value));
    }

    @Test
    public void canComputeMedianForSparseDataUsingSingleIncrement()
    {
        //canComputeMedianForDataUsingSingleIncrement(SimpleArrayUtils.newArray(10, 1.0, 1));

        for (final double value : values)
            canComputeMedianForDataUsingSingleIncrement(createSparseData(dataSize, value));
    }

    @Test
    public void canComputeMedianForSparseDataUsingSetPosition()
    {
        for (final double value : values)
            canComputeMedianForDataUsingSetPosition(createSparseData(dataSize, value));
    }

    @Test
    public void canComputeMedianForSparseDataUsingBigIncrement()
    {
        for (final double value : values)
            canComputeMedianForDataUsingBigIncrement(createSparseData(dataSize, value));
    }

    private void canComputeMedianForDataUsingSingleIncrement(double[] data)
    {
        for (final int radius : radii)
        {
            final MedianWindow mw = new MedianWindow(data, radius);
            for (int i = 0; i < data.length; i++)
            {
                final double median = mw.getMedian();
                mw.increment();
                final double median2 = calculateMedian(data, i, radius);
                //TestLog.debug(logger,"%f vs %f", median, median2);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);
            }
        }
    }

    private void canComputeMedianForDataUsingSetPosition(double[] data)
    {
        for (final int radius : radii)
        {
            final MedianWindow mw = new MedianWindow(data, radius);
            for (int i = 0; i < data.length; i += 10)
            {
                mw.setPosition(i);
                final double median = mw.getMedian();
                final double median2 = calculateMedian(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);
            }
        }
    }

    private void canComputeMedianForDataUsingBigIncrement(double[] data)
    {
        final int increment = 10;
        for (final int radius : radii)
        {
            final MedianWindow mw = new MedianWindow(data, radius);
            for (int i = 0; i < data.length; i += increment)
            {
                final double median = mw.getMedian();
                mw.increment(increment);
                final double median2 = calculateMedian(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);
            }
        }
    }

    @Test
    public void cannotComputeMedianBackToInputArrayUsingSingleIncrement()
    {
        final double[] data = SimpleArrayUtils.newArray(dataSize, 0.0, 1);
        for (final int radius : radii)
        {
            if (radius <= 1)
                continue;

            final double[] in = data.clone();
            final double[] e = new double[in.length];
            MedianWindow mw = new MedianWindow(in, radius);
            for (int i = 0; i < data.length; i++)
            {
                e[i] = mw.getMedian();
                mw.increment();
            }
            // Must create a new window
            mw = new MedianWindow(in, radius);
            for (int i = 0; i < data.length; i++)
            {
                // Write back to the input array
                in[i] = mw.getMedian();
                mw.increment();
            }
            Assertions.assertThrows(AssertionError.class, () -> {
                Assertions.assertArrayEquals(e, in);
            }, () -> String.format("Radius = %s", radius));
        }
    }

    @SeededTest
    public void canIncrementThroughTheDataArray(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final double[] data = createRandomData(rg, 300);
        for (final int radius : radii)
        {
            MedianWindow mw = new MedianWindow(data, radius);
            int i = 0;
            while (mw.isValidPosition())
            {
                final double median = mw.getMedian();
                final double median2 = calculateMedian(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);

                mw.increment();
                i++;
            }
            Assertions.assertEquals(i, data.length, "Not all data interated");

            mw = new MedianWindow(data, radius);
            i = 0;
            do
            {
                final double median = mw.getMedian();
                final double median2 = calculateMedian(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);

                i++;
            } while (mw.increment());
            Assertions.assertEquals(i, data.length, "Not all data interated");
        }
    }

    @SeededTest
    public void canIncrementThroughTheDataArrayUsingBigIncrement(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final double[] data = createRandomData(rg, 300);
        final int increment = 10;
        for (final int radius : radii)
        {
            final MedianWindow mw = new MedianWindow(data, radius);
            int i = 0;
            while (mw.isValidPosition())
            {
                final double median = mw.getMedian();
                final double median2 = calculateMedian(data, i, radius);
                ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", i, radius);

                mw.increment(increment);
                i += increment;
            }
        }
    }

    @SeededTest
    public void returnNaNForInvalidPositions(RandomSeed seed)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final double[] data = createRandomData(rg, 300);
        for (final int radius : radii)
        {
            MedianWindow mw = new MedianWindow(data, radius);
            for (int i = 0; i < data.length; i++)
                mw.increment();
            Assertions.assertEquals(Double.NaN, mw.getMedian(), 1e-6);

            mw = new MedianWindow(data, radius);
            while (mw.isValidPosition())
                mw.increment();
            Assertions.assertEquals(Double.NaN, mw.getMedian(), 1e-6);

            mw = new MedianWindow(data, radius);
            mw.setPosition(data.length + 10);
            Assertions.assertEquals(Double.NaN, mw.getMedian(), 1e-6);
        }
    }

    @SpeedTag
    @SeededTest
    public void isFasterThanLocalSort(RandomSeed seed)
    {
        ExtraAssumptions.assumeLowComplexity();
        final int[] speedRadii2 = (logger.isLoggable(Level.INFO)) ? speedRadii : new int[] { testSpeedRadius };
        for (final int radius : speedRadii2)
            for (final int increment : speedIncrement)
                isFasterThanLocalSort(seed, radius, increment);
    }

    private void isFasterThanLocalSort(RandomSeed seed, int radius, int increment)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int iterations = 20;
        final double[][] data = new double[iterations][];
        for (int i = 0; i < iterations; i++)
            data[i] = createRandomData(rg, dataSize);

        final double[] m1 = new double[dataSize];
        // Initialise class
        MedianWindow mw = new MedianWindow(data[0], radius);
        long t1;
        if (increment == 1)
        {
            do
                mw.getMedian();
            while (mw.increment());

            final long s1 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw = new MedianWindow(data[iter], radius);
                int j = 0;
                do
                    m1[j++] = mw.getMedian();
                while (mw.increment());
            }
            t1 = System.nanoTime() - s1;
        }
        else
        {
            while (mw.isValidPosition())
            {
                mw.getMedian();
                mw.increment(increment);
            }

            final long s1 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw = new MedianWindow(data[iter], radius);
                int j = 0;
                while (mw.isValidPosition())
                {
                    m1[j++] = mw.getMedian();
                    mw.increment(increment);
                }
            }
            t1 = System.nanoTime() - s1;
        }

        final double[] m2 = new double[dataSize];
        // Initialise
        for (int i = 0; i < dataSize; i += increment)
            calculateMedian(data[0], i, radius);
        final long s2 = System.nanoTime();
        for (int iter = 0; iter < iterations; iter++)
            for (int i = 0, j = 0; i < dataSize; i += increment)
                m2[j++] = calculateMedian(data[iter], i, radius);
        final long t2 = System.nanoTime() - s2;

        Assertions.assertArrayEquals(m1, m2, 1e-6);
        TestLog.info(logger, "Radius %d, Increment %d : window %d : standard %d = %fx faster", radius, increment, t1,
                t2, (double) t2 / t1);

        // Only test the largest radii
        if (radius == testSpeedRadius)
            ExtraAssertions.assertTrue(t1 < t2, "Radius %d, Increment %d", radius, increment);
    }

    @SpeedTag
    @SeededTest
    public void floatVersionIsFasterThanDoubleVersion(RandomSeed seed)
    {
        ExtraAssumptions.assumeLowComplexity();
        final int[] speedRadii2 = (logger.isLoggable(Level.INFO)) ? speedRadii : new int[] { testSpeedRadius };
        for (final int radius : speedRadii2)
            for (final int increment : speedIncrement)
                floatVersionIsFasterThanDoubleVersion(seed, radius, increment);
    }

    private void floatVersionIsFasterThanDoubleVersion(RandomSeed seed, int radius, int increment)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int iterations = 20;
        final double[][] data = new double[iterations][];
        final float[][] data2 = new float[iterations][];
        for (int i = 0; i < iterations; i++)
        {
            data[i] = createRandomData(rg, dataSize);
            data2[i] = copyData(data[i]);
        }

        final double[] m1 = new double[dataSize];
        // Initialise class
        MedianWindow mw = new MedianWindow(data[0], radius);
        long t1;
        if (increment == 1)
        {
            do
                mw.getMedian();
            while (mw.increment());

            final long s1 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw = new MedianWindow(data[iter], radius);
                int j = 0;
                do
                    m1[j++] = mw.getMedian();
                while (mw.increment());
            }
            t1 = System.nanoTime() - s1;
        }
        else
        {
            while (mw.isValidPosition())
            {
                mw.getMedian();
                mw.increment(increment);
            }

            final long s1 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw = new MedianWindow(data[iter], radius);
                int j = 0;
                while (mw.isValidPosition())
                {
                    m1[j++] = mw.getMedian();
                    mw.increment(increment);
                }
            }
            t1 = System.nanoTime() - s1;
        }

        final double[] m2 = new double[dataSize];
        // Initialise
        MedianWindowFloat mw2 = new MedianWindowFloat(data2[0], radius);
        long t2;
        if (increment == 1)
        {
            do
                mw2.getMedian();
            while (mw2.increment());

            final long s2 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw2 = new MedianWindowFloat(data2[iter], radius);
                int j = 0;
                do
                    m2[j++] = mw2.getMedian();
                while (mw2.increment());
            }
            t2 = System.nanoTime() - s2;
        }
        else
        {
            while (mw2.isValidPosition())
            {
                mw2.getMedian();
                mw2.increment(increment);
            }

            final long s2 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw2 = new MedianWindowFloat(data2[iter], radius);
                int j = 0;
                while (mw2.isValidPosition())
                {
                    m2[j++] = mw2.getMedian();
                    mw2.increment(increment);
                }
            }
            t2 = System.nanoTime() - s2;
        }

        Assertions.assertArrayEquals(m1, m2, 1e-3);

        // Only test the largest radii
        if (radius == testSpeedRadius)
            // Allow a margin of error
            //Assertions.assertTrue(String.format("Radius %d, Increment %d", radius, increment), t2 < t1 * 1.1);
            TestLog.logTestResult(logger, t2 < t1, "Radius %d, Increment %d : double %d : float %d = %fx faster",
                    radius, increment, t1, t2, (double) t1 / t2);
        else
            TestLog.info(logger, "Radius %d, Increment %d : double %d : float %d = %fx faster", radius, increment, t1,
                    t2, (double) t1 / t2);
    }

    @SpeedTag
    @SeededTest
    public void intVersionIsFasterThanDoubleVersion(RandomSeed seed)
    {
        ExtraAssumptions.assumeSpeedTest(TestComplexity.LOW);
        for (final int radius : speedRadii)
            for (final int increment : speedIncrement)
                intVersionIsFasterThanDoubleVersion(seed, radius, increment);
    }

    private void intVersionIsFasterThanDoubleVersion(RandomSeed seed, int radius, int increment)
    {
        final UniformRandomProvider rg = TestSettings.getRandomGenerator(seed.getSeed());
        final int iterations = 20;
        final double[][] data = new double[iterations][];
        final int[][] data2 = new int[iterations][];
        for (int i = 0; i < iterations; i++)
        {
            data[i] = createRandomData(rg, dataSize);
            data2[i] = copyDataInt(data[i]);
        }

        // Initialise class
        MedianWindow mw = new MedianWindow(data[0], radius);
        long t1;
        if (increment == 1)
        {
            do
                mw.getMedian();
            while (mw.increment());

            final long s1 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw = new MedianWindow(data[iter], radius);
                do
                    mw.getMedian();
                while (mw.increment());
            }
            t1 = System.nanoTime() - s1;
        }
        else
        {
            while (mw.isValidPosition())
            {
                mw.getMedian();
                mw.increment(increment);
            }

            final long s1 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw = new MedianWindow(data[iter], radius);
                while (mw.isValidPosition())
                {
                    mw.getMedian();
                    mw.increment(increment);
                }
            }
            t1 = System.nanoTime() - s1;
        }

        // Initialise
        MedianWindowInt mw2 = new MedianWindowInt(data2[0], radius);
        long t2;
        if (increment == 1)
        {
            do
                mw2.getMedian();
            while (mw2.increment());

            final long s2 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw2 = new MedianWindowInt(data2[iter], radius);
                do
                    mw2.getMedian();
                while (mw2.increment());
            }
            t2 = System.nanoTime() - s2;
        }
        else
        {
            while (mw2.isValidPosition())
            {
                mw2.getMedian();
                mw2.increment(increment);
            }

            final long s2 = System.nanoTime();
            for (int iter = 0; iter < iterations; iter++)
            {
                mw2 = new MedianWindowInt(data2[iter], radius);
                while (mw2.isValidPosition())
                {
                    mw2.getMedian();
                    mw2.increment(increment);
                }
            }
            t2 = System.nanoTime() - s2;
        }

        // Only test the largest radii
        if (radius == testSpeedRadius)
            //Assertions.assertTrue(String.format("Radius %d, Increment %d", radius, increment), t2 < t1);
            TestLog.logTestResult(logger, t2 < t1, "Radius %d, Increment %d : double %d : int %d = %fx faster", radius,
                    increment, t1, t2, (double) t1 / t2);
        else
            TestLog.info(logger, "Radius %d, Increment %d : double %d : int %d = %fx faster", radius, increment, t1, t2,
                    (double) t1 / t2);
    }

    static double calculateMedian(double[] data, int position, int radius)
    {
        final int start = FastMath.max(0, position - radius);
        final int end = FastMath.min(position + radius + 1, data.length);
        final double[] cache = new double[end - start];
        for (int i = start, j = 0; i < end; i++, j++)
            cache[j] = data[i];
        //TestLog.debugln(logger,Arrays.toString(cache));
        Arrays.sort(cache);
        return (cache[(cache.length - 1) / 2] + cache[cache.length / 2]) * 0.5;
    }

    static double calculateMedian2(double[] data, int position, int radius)
    {
        final int start = FastMath.max(0, position - radius);
        final int end = FastMath.min(position + radius + 1, data.length);
        final double[] cache = new double[end - start];
        for (int i = start, j = 0; i < end; i++, j++)
            cache[j] = data[i];
        final DescriptiveStatistics stats = new DescriptiveStatistics(cache);
        return stats.getPercentile(50);
    }

    static double[] createRandomData(UniformRandomProvider random, int size)
    {
        final double[] data = new double[size];
        for (int i = 0; i < data.length; i++)
            data[i] = random.nextDouble() * size;
        return data;
    }

    double[] createDuplicateData(int size, double value)
    {
        final double[] data = new double[size];
        Arrays.fill(data, value);
        return data;
    }

    static double[] createSparseData(int size, double value)
    {
        final double[] data = new double[size];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = value;
            if (i % 32 == 0)
                value++;
        }
        new Random(30051977).shuffle(data);
        return data;
    }

    static float[] copyData(double[] data)
    {
        final float[] data2 = new float[data.length];
        for (int i = 0; i < data.length; i++)
            data2[i] = (float) data[i];
        return data2;
    }

    static int[] copyDataInt(double[] data)
    {
        final int[] data2 = new int[data.length];
        for (int i = 0; i < data.length; i++)
            data2[i] = (int) data[i];
        return data2;
    }
}
