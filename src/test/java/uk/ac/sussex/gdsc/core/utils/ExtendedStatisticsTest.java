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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class ExtendedStatisticsTest {
  @SeededTest
  public void canComputeStatistics(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    DescriptiveStatistics expected;
    ExtendedStatistics observed;
    for (int i = 0; i < 10; i++) {
      expected = new DescriptiveStatistics();
      observed = new ExtendedStatistics();
      for (int j = 0; j < 100; j++) {
        final double d = r.nextDouble();
        expected.addValue(d);
        observed.add(d);
        check(expected, observed);
      }
    }

    expected = new DescriptiveStatistics();
    observed = new ExtendedStatistics();
    final int[] idata = SimpleArrayUtils.natural(100);
    PermutationSampler.shuffle(r, idata);
    for (final double v : idata) {
      expected.addValue(v);
    }
    observed.add(idata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    observed = new ExtendedStatistics();
    final double[] ddata = new double[idata.length];
    for (int i = 0; i < idata.length; i++) {
      ddata[i] = idata[i];
      expected.addValue(ddata[i]);
    }
    observed.add(ddata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    observed = new ExtendedStatistics();
    final float[] fdata = new float[idata.length];
    for (int i = 0; i < idata.length; i++) {
      fdata[i] = idata[i];
      expected.addValue(fdata[i]);
    }
    observed.add(fdata);
    check(expected, observed);
  }

  private static void check(DescriptiveStatistics expected, ExtendedStatistics observed) {
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), 1e-10, "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), 1e-10, "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), 1e-10,
        "SD");
    Assertions.assertEquals(expected.getMin(), observed.getMin(), "Min");
    Assertions.assertEquals(expected.getMax(), observed.getMax(), "Max");
  }

  @Test
  public void canAddStatistics() {
    final int[] d1 = SimpleArrayUtils.natural(100);
    final int[] d2 = SimpleArrayUtils.newArray(100, 4, 1);
    final ExtendedStatistics o = new ExtendedStatistics();
    o.add(d1);
    final ExtendedStatistics o2 = new ExtendedStatistics();
    o2.add(d2);
    final ExtendedStatistics o3 = new ExtendedStatistics();
    o3.add(o);
    o3.add(o2);
    final ExtendedStatistics o4 = new ExtendedStatistics();
    o4.add(d1);
    o4.add(d2);

    Assertions.assertEquals(o3.getN(), o4.getN(), "N");
    Assertions.assertEquals(o3.getMean(), o4.getMean(), "Mean");
    Assertions.assertEquals(o3.getVariance(), o4.getVariance(), "Variance");
    Assertions.assertEquals(o3.getStandardDeviation(), o4.getStandardDeviation(), "SD");
    Assertions.assertEquals(o3.getMin(), o4.getMin(), "Min");
    Assertions.assertEquals(o3.getMax(), o4.getMax(), "Max");
  }
}
