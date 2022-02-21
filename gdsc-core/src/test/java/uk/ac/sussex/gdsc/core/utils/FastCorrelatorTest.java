/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class FastCorrelatorTest {

  @Test
  void testEmptyValues() {
    final FastCorrelator observed = new FastCorrelator();
    final SimpleCorrelator expected = new SimpleCorrelator();
    check(expected, observed);
    final FastCorrelator observed2 = new FastCorrelator();
    check(expected, observed2);
  }

  @Test
  void testSingleValues() {
    final FastCorrelator observed = new FastCorrelator();
    final SimpleCorrelator expected = new SimpleCorrelator();
    observed.add(1, 2);
    expected.add(1, 2);
    check(expected, observed);
  }

  @Test
  void testNullValues() {
    final FastCorrelator observed = new FastCorrelator();
    final int[] data = {1, 2, 3};
    observed.add(null, data);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, null);
    Assertions.assertEquals(0, observed.getN());
    observed.add(null, data, 3);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, null, 3);
    Assertions.assertEquals(0, observed.getN());
  }

  @SeededTest
  void canComputeCorrelation(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    SimpleCorrelator expected;
    FastCorrelator observed;
    for (int i = 0; i < 10; i++) {
      expected = new SimpleCorrelator();
      observed = new FastCorrelator();
      final FastCorrelator observed2 = new FastCorrelator();
      for (int j = 0; j < 100; j++) {
        final int v1 = j + rng.nextInt(20);
        final int v2 = j + rng.nextInt(300);
        expected.add(v1, v2);
        observed.add(v1, v2);
        observed2.add((long) v1, v2);
        // The SimpleCorrelator can fail (NaN) with no variation in x so build up data
        if (j > 10) {
          check(expected, observed);
          check(expected, observed2);
        }
      }
    }

    expected = new SimpleCorrelator();
    final int[] v1 = SimpleArrayUtils.natural(100);
    final int[] v2 = v1.clone();
    PermutationSampler.shuffle(rng, v1);
    PermutationSampler.shuffle(rng, v2);
    for (int i = 0; i < v1.length; i++) {
      expected.add(v1[i], v2[i]);
    }
    observed = new FastCorrelator();
    observed.add(v1, v2);
    check(expected, observed);

    Assertions.assertEquals(expected.getCorrelation(), FastCorrelator.correlation(v1, v2), 1e-10);

    expected = new SimpleCorrelator();
    final int length = v1.length / 2;
    for (int i = 0; i < length; i++) {
      expected.add(v1[i], v2[i]);
    }
    observed = new FastCorrelator();
    observed.add(v1, v2, length);
    check(expected, observed);

    Assertions.assertEquals(expected.getCorrelation(), FastCorrelator.correlation(v1, v2, length),
        1e-10);
  }

  private static void check(SimpleCorrelator expected, FastCorrelator observed) {
    Assertions.assertEquals(expected.getSumX(), observed.getSumX(), "SumX");
    Assertions.assertEquals(expected.getSumY(), observed.getSumY(), "SumY");
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getCorrelation(), observed.getCorrelation(), 1e-10,
        "Correlation");
    Assertions.assertEquals(expected.getSumXX(), observed.getSumSquaredX(), "SumXX");
    Assertions.assertEquals(expected.getSumYY(), observed.getSumSquaredY(), "SumYY");
    Assertions.assertEquals(expected.getSumXY(), observed.getSumXbyY(), "SumXY");
  }

  @Test
  void canClear() {
    final FastCorrelator data = new FastCorrelator();
    Assertions.assertEquals(0, data.getN());
    data.add(1, 2);
    data.add(3, 4);
    Assertions.assertEquals(2, data.getN());
    Assertions.assertEquals(4, data.getSumX());
    Assertions.assertEquals(6, data.getSumY());
    data.clear();
    Assertions.assertEquals(0, data.getN());
    Assertions.assertEquals(0, data.getSumX());
    Assertions.assertEquals(0, data.getSumY());
  }

  @Test
  void testCorrelationWithNoValues() {
    int[] data = {1, 2, 3};
    Assertions.assertEquals(Double.NaN, FastCorrelator.correlation(data, null));
    Assertions.assertEquals(Double.NaN, FastCorrelator.correlation(null, data));
    Assertions.assertEquals(Double.NaN, FastCorrelator.correlation(data, null, 3));
    Assertions.assertEquals(Double.NaN, FastCorrelator.correlation(null, data, 3));
    Assertions.assertEquals(Double.NaN, FastCorrelator.correlation(data, data, 0));
    data = new int[0];
    Assertions.assertEquals(Double.NaN, FastCorrelator.correlation(data, data));
  }
}
