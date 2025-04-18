/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.DoubleConsumer;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Median;
import org.apache.commons.statistics.descriptive.Statistic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class StoredDataStatisticsTest {
  @Test
  void testEmptyValues() {
    final StoredDataStatistics observed = new StoredDataStatistics();
    final StoredDoubleStatistics expected = new StoredDoubleStatistics();
    check(expected, observed);
    final StoredDataStatistics observed2 = new StoredDataStatistics(10);
    check(expected, observed2);
  }

  @Test
  void testSingleValues() {
    final StoredDataStatistics observed = new StoredDataStatistics();
    final StoredDoubleStatistics expected = new StoredDoubleStatistics();
    final Object stats1 = observed.getStatistics();
    observed.add(Math.PI);
    expected.accept(Math.PI);
    check(expected, observed);
    Assertions.assertNotSame(stats1, observed.getStatistics());
  }

  @Test
  void testAddDoubleArrayWithZeroRange() {
    final StoredDataStatistics observed = new StoredDataStatistics();
    final double[] data = new double[] {1, 2, 3};
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, 2, 3);
    final StoredDoubleStatistics expected = new StoredDoubleStatistics();
    expected.accept(data[2]);
    check(expected, observed);
  }

  @Test
  void testAddFloatArrayWithZeroRange() {
    final StoredDataStatistics observed = new StoredDataStatistics();
    final float[] data = new float[] {1, 2, 3};
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, 2, 3);
    final StoredDoubleStatistics expected = new StoredDoubleStatistics();
    expected.accept(data[2]);
    check(expected, observed);
  }

  @Test
  void testAddIntArrayWithZeroRange() {
    final StoredDataStatistics observed = new StoredDataStatistics();
    final int[] data = new int[] {1, 2, 3};
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, 2, 3);
    final StoredDoubleStatistics expected = new StoredDoubleStatistics();
    expected.accept(data[2]);
    check(expected, observed);
  }

  @SeededTest
  void canAddMultipleValues(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final StoredDataStatistics observed = new StoredDataStatistics();
    final StoredDoubleStatistics expected = new StoredDoubleStatistics();
    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(-1, 123));
    observed.add(0, 123);
    for (int i = 0; i < 5; i++) {
      final int n = r.nextInt(10) + 1;
      final double value = r.nextDouble();
      observed.add(n, value);
      for (int j = 0; j < n; j++) {
        expected.accept(value);
      }
    }
    check(expected, observed);
  }

  @SeededTest
  void canComputeStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    StoredDoubleStatistics expected;
    StoredDataStatistics observed;
    final StoredDataStatistics observed2 = new StoredDataStatistics();
    for (int i = 0; i < 10; i++) {
      expected = new StoredDoubleStatistics();
      observed = new StoredDataStatistics();
      for (int j = 0; j < 100; j++) {
        // Use negative data to test for old bug in median
        final double d = -rng.nextDouble();
        expected.accept(d);
        observed.add(d);
        check(expected, observed);
      }
    }

    expected = new StoredDoubleStatistics();
    final int[] idata = SimpleArrayUtils.natural(100);
    PermutationSampler.shuffle(rng, idata);
    for (final double v : idata) {
      expected.accept(v);
    }
    observed = StoredDataStatistics.create(idata);
    check(expected, observed);
    observed2.reset();
    observed2.add(idata, 0, idata.length / 2);
    observed2.add(idata, idata.length / 2, idata.length);
    check(expected, observed2);

    expected = new StoredDoubleStatistics();
    final double[] ddata = new double[idata.length];
    for (int i = 0; i < idata.length; i++) {
      ddata[i] = idata[i];
      expected.accept(ddata[i]);
    }
    observed = StoredDataStatistics.create(ddata);
    check(expected, observed);
    observed2.reset();
    observed2.add(ddata, 0, ddata.length / 2);
    observed2.add(ddata, ddata.length / 2, ddata.length);
    check(expected, observed2);

    expected = new StoredDoubleStatistics();
    final float[] fdata = new float[idata.length];
    for (int i = 0; i < idata.length; i++) {
      fdata[i] = idata[i];
      expected.accept(fdata[i]);
    }
    observed = StoredDataStatistics.create(fdata);
    check(expected, observed);
    observed2.reset();
    observed2.add(fdata, 0, fdata.length / 2);
    observed2.add(fdata, fdata.length / 2, fdata.length);
    check(expected, observed2);
  }

  private static void check(StoredDoubleStatistics data, StoredDataStatistics observed) {
    final DoubleStatistics expected = data.getStats();
    Assertions.assertEquals(expected.getCount(), observed.getN(), "N");
    Assertions.assertEquals(expected.getAsDouble(Statistic.MEAN), observed.getMean(), 1e-10,
        "Mean");
    Assertions.assertEquals(expected.getAsDouble(Statistic.VARIANCE), observed.getVariance(), 1e-10,
        "Variance");
    Assertions.assertEquals(expected.getAsDouble(Statistic.STANDARD_DEVIATION),
        observed.getStandardDeviation(), 1e-10, "SD");
    Assertions.assertEquals(expected.getAsDouble(Statistic.SUM), observed.getSum(), 1e-10, "Sum");
    Assertions.assertEquals(expected.getAsDouble(Statistic.SUM_OF_SQUARES),
        observed.getSumOfSquares(), 1e-10, "SumOfSquare");
    Assertions.assertEquals(
        expected.getAsDouble(Statistic.STANDARD_DEVIATION) / Math.sqrt(expected.getCount()),
        observed.getStandardError(), 1e-10, "StandardError");

    final double[] d1 = data.getValues();
    final double[] d2 = observed.values();
    final float[] f2 = observed.getFloatValues();
    Assertions.assertEquals(d2.length, f2.length);
    for (int i = 0; i < d2.length; i++) {
      Assertions.assertEquals(d2[i], observed.getValue(i));
      Assertions.assertEquals((float) d2[i], f2[i]);
    }
    final DoubleArrayList list = new DoubleArrayList(d2.length);
    observed.forEach(list::add);
    Assertions.assertArrayEquals(d2, list.toDoubleArray());
    Arrays.sort(d1);
    Arrays.sort(d2);
    Assertions.assertArrayEquals(d1, d2);
    Assertions.assertEquals(Median.withDefaults().evaluate(d1), observed.getMedian(), 1e-10,
        "Median");
    Assertions.assertSame(observed.getStatistics(), observed.getStatistics());
  }

  @Test
  void canAddStatistics() {
    final int[] d1 = SimpleArrayUtils.natural(100);
    final int[] d2 = SimpleArrayUtils.newArray(75, 4, 1);
    final int[] d3 = SimpleArrayUtils.newArray(33, 4, -1);
    final StoredDataStatistics o1 = StoredDataStatistics.create(d1);
    final StoredDataStatistics o2 = StoredDataStatistics.create(d2);
    final StoredDataStatistics o3 = StoredDataStatistics.create(d3);
    final StoredDataStatistics expected = new StoredDataStatistics();
    expected.add(d1);
    expected.add(d2);
    expected.add(d3);
    final StoredDataStatistics observed = new StoredDataStatistics();
    observed.add(o1);
    observed.add(o2);
    observed.add(o3);
    observed.add(new StoredDataStatistics());

    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), "SD");

    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(new Statistics()));
  }

  @Test
  void canConstructWithData() {
    // This requires that the constructor correctly initialises the storage
    StoredDataStatistics stats;
    stats = StoredDataStatistics.create(new double[] {1, 2, 3});
    stats.add(1d);
    stats = StoredDataStatistics.create(new float[] {1, 2, 3});
    stats.add(1f);
    stats = StoredDataStatistics.create(new int[] {1, 2, 3});
    stats.add(1);
  }

  private static class StoredDoubleStatistics implements DoubleConsumer {
    private static final EnumSet<Statistic> STATS =
        EnumSet.of(Statistic.MIN, Statistic.MAX, Statistic.MEAN, Statistic.STANDARD_DEVIATION,
            Statistic.SUM_OF_SQUARES, Statistic.VARIANCE, Statistic.SUM);

    private final DoubleStatistics stats = DoubleStatistics.of(STATS);
    private final DoubleArrayList data = new DoubleArrayList();

    @Override
    public void accept(double value) {
      stats.accept(value);
      data.add(value);
    }

    DoubleStatistics getStats() {
      return stats;
    }

    double[] getValues() {
      return data.toDoubleArray();
    }
  }
}
