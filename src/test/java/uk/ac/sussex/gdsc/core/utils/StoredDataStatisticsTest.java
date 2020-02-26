package uk.ac.sussex.gdsc.core.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
public class StoredDataStatisticsTest extends StatisticsTest {
  private static Logger logger;
  private static Map<RandomSeed, StoredDataStatistics> dataCache;

  @BeforeAll
  static void beforeAll() {
    logger = Logger.getLogger(StoredDataStatisticsTest.class.getName());
    dataCache = new ConcurrentHashMap<>();
  }

  @AfterAll
  static void afterAll() {
    dataCache.clear();
    dataCache = null;
    logger = null;
  }

  static final int STATISTICS_SIZE = 10000;
  final int loops = 100;

  private static StoredDataStatistics createStatistics(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final StoredDataStatistics stats = new StoredDataStatistics(STATISTICS_SIZE);
    for (int i = 0; i < STATISTICS_SIZE; i++) {
      stats.add(r.nextDouble());
    }
    return stats;
  }

  @SeededTest
  public void getValuesEqualsIterator(RandomSeed seed) {
    final StoredDataStatistics stats =
        dataCache.computeIfAbsent(seed, StoredDataStatisticsTest::createStatistics);

    final double[] values = stats.getValues();
    int index = 0;
    for (final double d : stats) {
      Assertions.assertEquals(d, values[index++]);
    }
  }

  // These speed tests are weak. A JMH benchmark would be better.

  @SpeedTag
  @SeededTest
  public void forLoopIsSlowerThanValuesIterator(RandomSeed seed) {
    // This fails. Perhaps change the test to use the TimingService for repeat testing.
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final StoredDataStatistics stats =
        dataCache.computeIfAbsent(seed, StoredDataStatisticsTest::createStatistics);

    double total = 0;
    long start1 = System.nanoTime();
    for (int i = 0; i < loops; i++) {
      total = 0;
      final double[] values = stats.getValues();
      for (int j = 0; j < values.length; j++) {
        total += values[j];
      }
    }
    start1 = System.nanoTime() - start1;
    logger.finest(FunctionUtils.getSupplier("Total = %s", total));

    long start2 = System.nanoTime();
    for (int i = 0; i < loops; i++) {
      total = 0;
      for (final double d : stats.getValues()) {
        total += d;
      }
    }
    start2 = System.nanoTime() - start2;
    logger.finest(FunctionUtils.getSupplier("Total = %s", total));

    logger.log(TestLogUtils.getTimingRecord("for (double d : stats.getValues())", start2,
        "for (int j = 0; j < values.length; j++)", start1));
  }

  @SpeedTag
  @SeededTest
  public void iteratorIsSlowerUsingdouble(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final StoredDataStatistics stats =
        dataCache.computeIfAbsent(seed, StoredDataStatisticsTest::createStatistics);

    double total = 0;
    long start1 = System.nanoTime();
    for (int i = 0; i < loops; i++) {
      total = 0;
      for (final double d : stats.getValues()) {
        total += d;
      }
    }
    start1 = System.nanoTime() - start1;
    logger.finest(FunctionUtils.getSupplier("Total = %s", total));

    long start2 = System.nanoTime();
    for (int i = 0; i < loops; i++) {
      total = 0;
      for (final double d : stats) {
        total += d;
      }
    }
    start2 = System.nanoTime() - start2;
    logger.finest(FunctionUtils.getSupplier("Total = %s", total));

    logger.log(TestLogUtils.getTimingRecord("for (double d : stats)", start2,
        "for (double d : stats.getValues)", start1));
  }

  @SpeedTag
  @SeededTest
  public void iteratorIsSlowerUsingDouble(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final StoredDataStatistics stats =
        dataCache.computeIfAbsent(seed, StoredDataStatisticsTest::createStatistics);

    double total = 0;
    long start1 = System.nanoTime();
    for (int i = 0; i < loops; i++) {
      total = 0;
      for (final double d : stats.getValues()) {
        total += d;
      }
    }
    start1 = System.nanoTime() - start1;
    logger.finest(FunctionUtils.getSupplier("Total = %s", total));

    long start2 = System.nanoTime();
    for (int i = 0; i < loops; i++) {
      total = 0;
      for (final Double d : stats) {
        total += d;
      }
    }
    start2 = System.nanoTime() - start2;
    logger.finest(FunctionUtils.getSupplier("Total = %s", total));

    logger.log(TestLogUtils.getTimingRecord("for (Double d : stats)", start2,
        "for (double d : stats.getValues)", start1));
  }

  @Test
  public void canConstructWithData() {
    // This requires that the constructor correctly initialises the storage
    StoredDataStatistics stats;
    stats = StoredDataStatistics.create(new double[] {1, 2, 3});
    stats.add(1d);
    stats = StoredDataStatistics.create(new float[] {1, 2, 3});
    stats.add(1f);
    stats = StoredDataStatistics.create(new int[] {1, 2, 3});
    stats.add(1);
  }
}
