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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.statistics.distribution.ContinuousDistribution;
import org.apache.commons.statistics.distribution.ContinuousDistribution.Sampler;
import org.apache.commons.statistics.distribution.ExponentialDistribution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.api.Predicates;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.rng.RngFactory;

/**
 * Test for {@link GeometryUtils}.
 */
@SuppressWarnings({"javadoc"})
class DistributionsTest {
  @ParameterizedTest
  @ValueSource(doubles = {0.5, 1, 4})
  void canComputeExponentialDistribution(double mean) {
    final double rate = 1 / mean;
    final ExponentialDistribution ed = ExponentialDistribution.of(mean);
    final ContinuousDistribution d = Distributions.exponential(rate);
    final DoubleDoubleBiPredicate test = Predicates.doublesAreUlpClose(5);
    Assertions.assertEquals(ed.getSupportLowerBound(), d.getSupportLowerBound(), "lower bound");
    Assertions.assertEquals(ed.getSupportUpperBound(), d.getSupportUpperBound(), "upper bound");
    TestAssertions.assertTest(ed.getMean(), d.getMean(), test, "mean");
    TestAssertions.assertTest(ed.getVariance(), d.getVariance(), test, "variance");

    final double[] x = SimpleArrayUtils.newArray(10, mean / 5, mean / 2);
    for (int i = 0; i < x.length; i++) {
      TestAssertions.assertTest(ed.density(x[i]), d.density(x[i]), test, "density");
      TestAssertions.assertTest(ed.logDensity(x[i]), d.logDensity(x[i]), test, "logDensity");
      if (i + 1 < x.length) {
        TestAssertions.assertTest(ed.probability(x[i], x[i + 1]), d.probability(x[i], x[i + 1]),
            test, "probability");
      }
      TestAssertions.assertTest(ed.cumulativeProbability(x[i]), d.cumulativeProbability(x[i]), test,
          "cumulativeProbability");
      TestAssertions.assertTest(ed.survivalProbability(x[i]), d.survivalProbability(x[i]), test,
          "survivalProbability");
    }

    final double[] p = SimpleArrayUtils.newArray(9, 0, 1 / 8.0);
    for (int i = 0; i < p.length; i++) {
      TestAssertions.assertTest(ed.inverseCumulativeProbability(p[i]),
          d.inverseCumulativeProbability(p[i]), test, "inverseCumulativeProbability");
      TestAssertions.assertTest(ed.inverseSurvivalProbability(p[i]),
          d.inverseSurvivalProbability(p[i]), test, "inverseSurvivalProbability");
    }

    // Requires rate to be exactly invertible
    if (mean == 1 / rate) {
      final Sampler s1 = ed.createSampler(RngFactory.createWithFixedSeed());
      final Sampler s2 = d.createSampler(RngFactory.createWithFixedSeed());
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(s1.sample(), s2.sample());
      }
    }
  }

  @Test
  void canComputeExponentialDistributionEdgeCases() {
    final ExponentialDistribution ed = ExponentialDistribution.of(1);
    final ContinuousDistribution d = Distributions.exponential(1);
    for (final double x : new double[] {-1, 0}) {
      Assertions.assertEquals(ed.density(x), d.density(x), "density");
      // Allow -0.0 == 0.0
      Assertions.assertEquals(ed.logDensity(x), d.logDensity(x), 0.0, "logDensity");
      Assertions.assertEquals(ed.cumulativeProbability(x), d.cumulativeProbability(x),
          "cumulativeProbability");
      Assertions.assertEquals(ed.survivalProbability(x), d.survivalProbability(x),
          "survivalProbability");
    }
    Assertions.assertThrows(IllegalArgumentException.class, () -> Distributions.exponential(0));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Distributions.exponential(-1));
    Assertions.assertThrows(IllegalArgumentException.class, () -> d.probability(1, 0.5));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> d.inverseCumulativeProbability(-1));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> d.inverseCumulativeProbability(1.5));
    Assertions.assertThrows(IllegalArgumentException.class, () -> d.inverseSurvivalProbability(-1));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> d.inverseSurvivalProbability(1.5));
  }

  @Test
  void canComputeExponentialDistributionSamplesWithInfiniteMean() {
    final double rate = Double.MIN_NORMAL / 4;
    final ContinuousDistribution d = Distributions.exponential(rate);
    Assertions.assertEquals(Double.POSITIVE_INFINITY, d.getMean(), "mean");
    final Sampler s1 =
        ExponentialDistribution.of(1).createSampler(RngFactory.createWithFixedSeed());
    final Sampler s2 = d.createSampler(RngFactory.createWithFixedSeed());
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(s1.sample() / rate, s2.sample());
    }
  }
}
