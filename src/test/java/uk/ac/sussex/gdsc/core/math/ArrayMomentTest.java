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

package uk.ac.sussex.gdsc.core.math;

import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.IntegerType;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.core.utils.rng.SamplerUtils;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

/**
 * Test for {@link ArrayMoment}.
 */
@SuppressWarnings({"javadoc"})
class ArrayMomentTest {
  static final int MAX_INT = 65335; // Unsigned 16-bit int
  static final int MAX_INT_2 = MAX_INT / 2;

  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(ArrayMomentTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-8, 0);

  // XXX RollingArrayMoment

  @Test
  void testRollingArrayMomentSingleDouble() {
    final RollingArrayMoment m = new RollingArrayMoment();
    m.add(new double[] {1});
    final double[] zero = new double[1];
    Assertions.assertEquals(1, m.getN());
    Assertions.assertArrayEquals(new double[] {1}, m.getMean());
    Assertions.assertArrayEquals(zero, m.getSumOfSquares());
    Assertions.assertArrayEquals(zero, m.getVariance());
    Assertions.assertArrayEquals(zero, m.getVariance(false));
    Assertions.assertArrayEquals(zero, m.getStandardDeviation());
    Assertions.assertArrayEquals(zero, m.getStandardDeviation(false));
    m.add(new double[] {2});
    Assertions.assertEquals(2, m.getN());
    Assertions.assertArrayEquals(new double[] {1.5}, m.getMean());
    Assertions.assertArrayEquals(new double[] {0.5}, m.getSumOfSquares());
    Assertions.assertArrayEquals(new double[] {0.5}, m.getVariance());
    Assertions.assertArrayEquals(new double[] {0.25}, m.getVariance(false));
    Assertions.assertArrayEquals(new double[] {Math.sqrt(0.5)}, m.getStandardDeviation());
    Assertions.assertArrayEquals(new double[] {Math.sqrt(0.25)}, m.getStandardDeviation(false));
  }

  @Test
  void testRollingMomentSmallResults() {
    assertSmallResults(new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentDouble(RandomSeed seed) {
    canComputeMoment("Single", new double[] {Math.PI}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[] d = new double[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextDouble();
    }
    canComputeMoment("Uniform", d, new RollingArrayMoment());

    final NormalizedGaussianSampler g = ZigguratSampler.NormalizedGaussian.of(rng);
    for (int i = 0; i < d.length; i++) {
      d[i] = (float) g.sample();
    }
    canComputeMoment("Gaussian", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i;
    }
    canComputeMoment("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentFloat(RandomSeed seed) {
    canComputeMoment("Single", new float[] {(float) Math.PI}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final float[] d = new float[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextFloat();
    }
    canComputeMoment("Uniform", d, new RollingArrayMoment());

    final SharedStateContinuousSampler g = SamplerUtils.createGaussianSampler(rng, 0, 1);
    for (int i = 0; i < d.length; i++) {
      d[i] = (float) g.sample();
    }
    canComputeMoment("Gaussian", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i;
    }
    canComputeMoment("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentInt(RandomSeed seed) {
    canComputeMoment("Single", new int[] {-42}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[] d = new int[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextInt(MAX_INT) - MAX_INT_2;
    }
    canComputeMoment("Uniform", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i - 500;
    }
    canComputeMoment("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentShort(RandomSeed seed) {
    canComputeMoment("Single", new short[] {-42}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final short[] d = new short[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMoment("Uniform", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (i - 500);
    }
    canComputeMoment("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentByte(RandomSeed seed) {
    canComputeMoment("Single", new byte[] {-42}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] d = new byte[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMoment("Uniform", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (i - 500);
    }
    canComputeMoment("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentShortUnsigned(RandomSeed seed) {
    canComputeMomentUnsigned("Single", new short[] {-42}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final short[] d = new short[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMomentUnsigned("Uniform", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (i - 500);
    }
    canComputeMomentUnsigned("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingMomentByteUnsigned(RandomSeed seed) {
    canComputeMomentUnsigned("Single", new byte[] {-42}, new RollingArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] d = new byte[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMomentUnsigned("Uniform", d, new RollingArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (i - 500);
    }
    canComputeMomentUnsigned("Series", d, new RollingArrayMoment());
  }

  @SeededTest
  void canComputeRollingArrayMomentDouble(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[][] d = new double[3][];

    for (int i = d.length; i-- > 0;) {
      d[i] = new double[] {rng.nextDouble()};
    }
    canComputeArrayMoment("Single", d, new RollingArrayMoment());

    final int n = 1000;
    for (int i = d.length; i-- > 0;) {
      d[i] = uniformDouble(rng, n);
    }
    canComputeArrayMoment("Uniform", d, new RollingArrayMoment());
  }

  @SeededTest
  void canCombineRollingArrayMomentDouble(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[][] d = new double[50][];

    final int n = 1000;
    for (int i = d.length; i-- > 0;) {
      d[i] = uniformDouble(rng, n);
    }

    final RollingArrayMoment r1 = new RollingArrayMoment();
    Assertions.assertThrows(IllegalArgumentException.class, () -> r1.add(new IntegerArrayMoment()));

    final int size = 6;
    final RollingArrayMoment[] r2 = new RollingArrayMoment[size];
    for (int i = 0; i < size; i++) {
      r2[i] = new RollingArrayMoment();
    }
    for (int i = 0; i < d.length; i++) {
      r1.add(d[i]);
      r2[i % size].add(d[i]);
    }

    final double[] em1 = r1.getMean();
    final double[] em2 = r1.getSumOfSquares();
    final double[] ev = r1.getVariance();
    final double[] esd = r1.getStandardDeviation();

    for (int i = 1; i < size; i++) {
      r2[0].add((ArrayMoment) r2[i]);
    }

    final double[] om1 = r2[0].getMean();
    final double[] om2 = r2[0].getSumOfSquares();
    final double[] ov = r2[0].getVariance();
    final double[] osd = r2[0].getStandardDeviation();

    TestAssertions.assertArrayTest(em1, om1, equality, "Mean");
    TestAssertions.assertArrayTest(em2, om2, equality, "2nd Moment");
    TestAssertions.assertArrayTest(ev, ov, equality, "Variance");
    TestAssertions.assertArrayTest(esd, osd, equality, "SD");
  }

  // XXX SimpleArrayMoment

  @Test
  void testSimpleMomentSmallResults() {
    assertSmallResults(new SimpleArrayMoment());
  }

  @Test
  void testSimpleArrayMomentSingleDouble() {
    final SimpleArrayMoment m = new SimpleArrayMoment();
    m.add(new double[] {1});
    final double[] zero = new double[1];
    Assertions.assertEquals(1, m.getN());
    Assertions.assertArrayEquals(new double[] {1}, m.getMean());
    Assertions.assertArrayEquals(zero, m.getSumOfSquares());
    Assertions.assertArrayEquals(zero, m.getVariance());
    Assertions.assertArrayEquals(zero, m.getVariance(false));
    Assertions.assertArrayEquals(zero, m.getStandardDeviation());
    Assertions.assertArrayEquals(zero, m.getStandardDeviation(false));
    m.add(new double[] {2});
    Assertions.assertEquals(2, m.getN());
    Assertions.assertArrayEquals(new double[] {1.5}, m.getMean());
    Assertions.assertArrayEquals(new double[] {0.5}, m.getSumOfSquares());
    Assertions.assertArrayEquals(new double[] {0.5}, m.getVariance());
    Assertions.assertArrayEquals(new double[] {0.25}, m.getVariance(false));
    Assertions.assertArrayEquals(new double[] {Math.sqrt(0.5)}, m.getStandardDeviation());
    Assertions.assertArrayEquals(new double[] {Math.sqrt(0.25)}, m.getStandardDeviation(false));
  }

  @SeededTest
  void canComputeSimpleMomentDouble(RandomSeed seed) {
    canComputeMoment("Single", new double[] {Math.PI}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[] d = new double[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextDouble();
    }
    canComputeMoment("Uniform", d, new SimpleArrayMoment());

    final SharedStateContinuousSampler g = SamplerUtils.createGaussianSampler(rng, 0, 1);
    for (int i = 0; i < d.length; i++) {
      d[i] = (float) g.sample();
    }
    canComputeMoment("Gaussian", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i;
    }
    canComputeMoment("Series", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canComputeSimpleMomentFloat(RandomSeed seed) {
    canComputeMoment("Single", new float[] {(float) Math.PI}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final float[] d = new float[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextFloat();
    }
    canComputeMoment("Uniform", d, new SimpleArrayMoment());

    final SharedStateContinuousSampler g = SamplerUtils.createGaussianSampler(rng, 0, 1);
    for (int i = 0; i < d.length; i++) {
      d[i] = (float) g.sample();
    }
    canComputeMoment("Gaussian", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i;
    }
    canComputeMoment("Series", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canComputeSimpleMomentInt(RandomSeed seed) {
    canComputeMoment("Single", new int[] {-42}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[] d = new int[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextInt(MAX_INT) - MAX_INT_2;
    }
    canComputeMoment("Uniform", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i - 500;
    }
    canComputeMoment("Series", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canComputeSimpleMomentShort(RandomSeed seed) {
    canComputeMoment("Single", new short[] {-42}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final short[] d = new short[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMoment("Uniform", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (i - 500);
    }
    canComputeMoment("Series", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canComputeSimpleMomentByte(RandomSeed seed) {
    canComputeMoment("Single", new byte[] {-42}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] d = new byte[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMoment("Uniform", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (i - 500);
    }
    canComputeMoment("Series", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canComputeSimpleMomentShortUnsigned(RandomSeed seed) {
    canComputeMomentUnsigned("Single", new short[] {-42}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final short[] d = new short[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMomentUnsigned("Uniform", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (i - 500);
    }
    canComputeMomentUnsigned("Series", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canComputeSimpleMomentByteUnsigned(RandomSeed seed) {
    canComputeMomentUnsigned("Single", new byte[] {-42}, new SimpleArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] d = new byte[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMomentUnsigned("Uniform", d, new SimpleArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (i - 500);
    }
    canComputeMomentUnsigned("Series", d, new SimpleArrayMoment());
  }


  @SeededTest
  void canComputeSimpleArrayMomentInt(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[][] d = new int[3][];

    for (int i = d.length; i-- > 0;) {
      d[i] = new int[] {rng.nextInt(MAX_INT)};
    }
    canComputeArrayMoment("Single", d, new SimpleArrayMoment());

    final int n = 1000;
    for (int i = d.length; i-- > 0;) {
      d[i] = uniformInt(rng, n);
    }
    canComputeArrayMoment("Uniform", d, new SimpleArrayMoment());
  }

  @SeededTest
  void canCombineSimpleArrayMomentInt(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[][] d = new int[50][];

    final int n = 1000;
    for (int i = d.length; i-- > 0;) {
      d[i] = uniformInt(rng, n);
    }

    final SimpleArrayMoment r1 = new SimpleArrayMoment();
    Assertions.assertThrows(IllegalArgumentException.class, () -> r1.add(new RollingArrayMoment()));

    final int size = 6;
    final SimpleArrayMoment[] r2 = new SimpleArrayMoment[size];
    for (int i = 0; i < size; i++) {
      r2[i] = new SimpleArrayMoment();
    }
    for (int i = 0; i < d.length; i++) {
      r1.add(d[i]);
      r2[i % size].add(d[i]);
    }

    final double[] em1 = r1.getMean();
    final double[] em2 = r1.getSumOfSquares();
    final double[] ev = r1.getVariance();
    final double[] esd = r1.getStandardDeviation();

    for (int i = 1; i < size; i++) {
      r2[0].add((ArrayMoment) r2[i]);
    }

    final double[] om1 = r2[0].getMean();
    final double[] om2 = r2[0].getSumOfSquares();
    final double[] ov = r2[0].getVariance();
    final double[] osd = r2[0].getStandardDeviation();

    TestAssertions.assertArrayTest(em1, om1, equality, "Mean");
    TestAssertions.assertArrayTest(em2, om2, equality, "2nd Moment");
    TestAssertions.assertArrayTest(ev, ov, equality, "Variance");
    TestAssertions.assertArrayTest(esd, osd, equality, "SD");
  }

  // XXX IntegerArrayMoment

  @Test
  void testIntegerMomentSmallResults() {
    assertSmallResults(new IntegerArrayMoment());
  }

  @Test
  void testIntegerArrayMomentThrows() {
    final IntegerArrayMoment m = new IntegerArrayMoment();
    Assertions.assertThrows(NotImplementedException.class, () -> m.add(new double[] {1.23}));
    Assertions.assertThrows(NotImplementedException.class, () -> m.add(new float[] {1.5f}));
  }

  @SeededTest
  void canComputeIntegerMomentInt(RandomSeed seed) {
    canComputeMoment("Single", new int[] {-42}, new IntegerArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[] d = new int[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextInt(MAX_INT) - MAX_INT_2;
    }
    canComputeMoment("Uniform", d, new IntegerArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = i - 500;
    }
    canComputeMoment("Series", d, new IntegerArrayMoment());
  }

  @SeededTest
  void canComputeIntegerMomentShort(RandomSeed seed) {
    canComputeMoment("Single", new short[] {-42}, new IntegerArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final short[] d = new short[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMoment("Uniform", d, new IntegerArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (i - 500);
    }
    canComputeMoment("Series", d, new IntegerArrayMoment());
  }

  @SeededTest
  void canComputeIntegerMomentByte(RandomSeed seed) {
    canComputeMoment("Single", new byte[] {-42}, new IntegerArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] d = new byte[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMoment("Uniform", d, new IntegerArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (i - 500);
    }
    canComputeMoment("Series", d, new IntegerArrayMoment());
  }

  @SeededTest
  void canComputeIntegerMomentShortUnsigned(RandomSeed seed) {
    canComputeMomentUnsigned("Single", new short[] {-42}, new IntegerArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final short[] d = new short[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMomentUnsigned("Uniform", d, new IntegerArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (short) (i - 500);
    }
    canComputeMomentUnsigned("Series", d, new IntegerArrayMoment());
  }

  @SeededTest
  void canComputeIntegerMomentByteUnsigned(RandomSeed seed) {
    canComputeMomentUnsigned("Single", new byte[] {-42}, new IntegerArrayMoment());

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] d = new byte[1000];

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (rng.nextInt(MAX_INT) - MAX_INT_2);
    }
    canComputeMomentUnsigned("Uniform", d, new IntegerArrayMoment());

    for (int i = 0; i < d.length; i++) {
      d[i] = (byte) (i - 500);
    }
    canComputeMomentUnsigned("Series", d, new IntegerArrayMoment());
  }


  @SeededTest
  void canComputeIntegerArrayMomentInt(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[][] d = new int[3][];

    for (int i = d.length; i-- > 0;) {
      d[i] = new int[] {rng.nextInt(MAX_INT)};
    }
    canComputeArrayMoment("Single", d, new IntegerArrayMoment());

    final int n = 1000;
    for (int i = d.length; i-- > 0;) {
      d[i] = uniformInt(rng, n);
    }
    canComputeArrayMoment("Uniform", d, new IntegerArrayMoment());
  }

  @SeededTest
  void canCombineIntegerArrayMomentInt(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final int[][] d = new int[50][];

    final int n = 1000;
    for (int i = d.length; i-- > 0;) {
      d[i] = uniformInt(rng, n);
    }

    final IntegerArrayMoment r1 = new IntegerArrayMoment();
    Assertions.assertThrows(IllegalArgumentException.class, () -> r1.add(new RollingArrayMoment()));

    final int size = 6;
    final IntegerArrayMoment[] r2 = new IntegerArrayMoment[size];
    for (int i = 0; i < size; i++) {
      r2[i] = new IntegerArrayMoment();
    }
    for (int i = 0; i < d.length; i++) {
      r1.add(d[i]);
      r2[i % size].add(d[i]);
    }

    final double[] em1 = r1.getMean();
    final double[] em2 = r1.getSumOfSquares();
    final double[] ev = r1.getVariance();
    final double[] esd = r1.getStandardDeviation();

    for (int i = 1; i < size; i++) {
      r2[0].add((ArrayMoment) r2[i]);
    }

    final double[] om1 = r2[0].getMean();
    final double[] om2 = r2[0].getSumOfSquares();
    final double[] ov = r2[0].getVariance();
    final double[] osd = r2[0].getStandardDeviation();

    // No delta as integer math should be exact
    Assertions.assertArrayEquals(em1, om1, "Mean");
    Assertions.assertArrayEquals(em2, om2, "2nd Moment");
    Assertions.assertArrayEquals(ev, ov, "Variance");
    Assertions.assertArrayEquals(esd, osd, "SD");
  }

  @Test
  void canTestIfValidIntegerData() {
    // If the sum of squares is above Long.MAX_VALUE (2^63 - 1) then expected false.

    // 2^16^2 * 2^31-1 = 2^32 * 2^31-1 < 2^63 : This is OK
    canTestIfValidIntegerData(true, IntegerType.UNSIGNED_16, Integer.MAX_VALUE);

    // (2^30)^2 = 2^60 : We should be able to do lots of these up to size = 2^3 = 8
    for (int size = 1; size < 8; size++) {
      canTestIfValidIntegerData(true, IntegerType.SIGNED_31, size);
    }
    canTestIfValidIntegerData(false, IntegerType.SIGNED_31, 8);

    // (2^31)^2 = 2^62 : We should be able to 1 of these but not 2 as is = 2^63
    canTestIfValidIntegerData(true, IntegerType.SIGNED_32, 1);
    canTestIfValidIntegerData(false, IntegerType.SIGNED_32, 2);

    // 2^32^2 == 2^64 : We cannot do this as
    canTestIfValidIntegerData(false, IntegerType.UNSIGNED_32, 1);
  }

  private static void canTestIfValidIntegerData(boolean expected, IntegerType integerType,
      int size) {
    Assertions.assertEquals(expected, IntegerArrayMoment.isValid(integerType, size),
        () -> String.format("%s * %d", integerType.getTypeName(), size));
  }

  private void canComputeMoment(String title, double[] data, ArrayMoment r2) {
    final Statistics m1 = new Statistics();
    m1.add(data);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(data);
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(new double[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private void canComputeMoment(String title, float[] data, ArrayMoment r2) {
    final Statistics m1 = new Statistics();
    m1.add(data);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(toDouble(data));
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(new float[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private void canComputeMoment(String title, int[] data, ArrayMoment r2) {
    final Statistics m1 = new Statistics();
    m1.add(data);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(toDouble(data));
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(new int[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private void canComputeMoment(String title, short[] data, ArrayMoment r2) {
    final double[] data2 = toDouble(data);
    final Statistics m1 = new Statistics();
    m1.add(data2);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(data2);
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(new short[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private void canComputeMoment(String title, byte[] data, ArrayMoment r2) {
    final double[] data2 = toDouble(data);
    final Statistics m1 = new Statistics();
    m1.add(data2);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(data2);
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(new byte[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private void canComputeMomentUnsigned(String title, short[] data, ArrayMoment r2) {
    final double[] data2 = toDoubleUnsigned(data);
    final Statistics m1 = new Statistics();
    m1.add(data2);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(data2);
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.addUnsigned(new short[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private void canComputeMomentUnsigned(String title, byte[] data, ArrayMoment r2) {
    final double[] data2 = toDoubleUnsigned(data);
    final Statistics m1 = new Statistics();
    m1.add(data2);
    final SecondMoment m2 = new SecondMoment();
    m2.incrementAll(data2);
    final long n = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.addUnsigned(new byte[] {data[i]});
    }
    Assertions.assertEquals(n + data.length, r2.getN());
    TestAssertions.assertTest(m1.getMean(), r2.getMean()[0], equality, () -> title + " Mean");
    TestAssertions.assertTest(m2.getResult(), r2.getSumOfSquares()[0], equality,
        () -> title + " 2nd Moment");
    TestAssertions.assertTest(m1.getVariance(), r2.getVariance()[0], equality,
        () -> title + " Variance");
    TestAssertions.assertTest(m1.getStandardDeviation(), r2.getStandardDeviation()[0], equality,
        () -> title + " SD");
  }

  private static double[] toDouble(float[] in) {
    final double[] d = new double[in.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = in[i];
    }
    return d;
  }

  private static double[] toDouble(int[] in) {
    final double[] d = new double[in.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = in[i];
    }
    return d;
  }

  private static double[] toDouble(short[] in) {
    final double[] d = new double[in.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = in[i];
    }
    return d;
  }

  private static double[] toDouble(byte[] in) {
    final double[] d = new double[in.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = in[i];
    }
    return d;
  }

  private static double[] toDoubleUnsigned(short[] in) {
    final double[] d = new double[in.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = in[i] & 0xffff;
    }
    return d;
  }

  private static double[] toDoubleUnsigned(byte[] in) {
    final double[] d = new double[in.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = in[i] & 0xff;
    }
    return d;
  }

  private static double[] uniformDouble(UniformRandomProvider rng, int n) {
    final double[] d = new double[n];
    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextDouble();
    }
    return d;
  }

  private static int[] uniformInt(UniformRandomProvider rng, int n) {
    final int[] d = new int[n];
    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextInt(MAX_INT);
    }
    return d;
  }

  /**
   * Assert small results.
   *
   * @param m the moment (must be empty)
   */
  private static void assertSmallResults(ArrayMoment m) {
    final double[] empty = new double[0];
    Assertions.assertEquals(0, m.getN());
    Assertions.assertArrayEquals(empty, m.getMean());
    Assertions.assertArrayEquals(empty, m.getSumOfSquares());
    Assertions.assertArrayEquals(empty, m.getVariance());
    Assertions.assertArrayEquals(empty, m.getVariance(false));
    Assertions.assertArrayEquals(empty, m.getStandardDeviation());
    Assertions.assertArrayEquals(empty, m.getStandardDeviation(false));

    m.add(new int[] {0, 1, 2});
    final double[] zero = new double[3];
    Assertions.assertEquals(1, m.getN());
    Assertions.assertArrayEquals(new double[] {0, 1, 2}, m.getMean());
    Assertions.assertArrayEquals(zero, m.getSumOfSquares());
    Assertions.assertArrayEquals(zero, m.getVariance());
    Assertions.assertArrayEquals(zero, m.getVariance(false));
    Assertions.assertArrayEquals(zero, m.getStandardDeviation());
    Assertions.assertArrayEquals(zero, m.getStandardDeviation(false));

    m.add(new int[] {0, 2, 4});
    Assertions.assertEquals(2, m.getN());
    Assertions.assertArrayEquals(new double[] {0, 1.5, 3}, m.getMean());
    Assertions.assertArrayEquals(new double[] {0, 0.5, 2}, m.getSumOfSquares());
    Assertions.assertArrayEquals(new double[] {0, 0.5, 2}, m.getVariance());
    Assertions.assertArrayEquals(new double[] {0, 0.25, 1}, m.getVariance(false));
    Assertions.assertArrayEquals(new double[] {0, Math.sqrt(0.5), Math.sqrt(2)},
        m.getStandardDeviation());
    Assertions.assertArrayEquals(new double[] {0, Math.sqrt(0.25), 1},
        m.getStandardDeviation(false));

    final ArrayMoment m2 = m.newInstance();
    Assertions.assertEquals(0, m2.getN());
    Assertions.assertEquals(m.getClass(), m2.getClass());

    m2.add(m);
    Assertions.assertEquals(m.getN(), m2.getN());
    Assertions.assertArrayEquals(m.getMean(), m2.getMean());
    Assertions.assertArrayEquals(m.getSumOfSquares(), m2.getSumOfSquares());
    Assertions.assertArrayEquals(m.getVariance(), m2.getVariance());
    Assertions.assertArrayEquals(m.getStandardDeviation(), m2.getStandardDeviation());

    // Add empty
    final ArrayMoment m3 = m.newInstance();
    m2.add(m3);
    Assertions.assertEquals(m.getN(), m2.getN());
    Assertions.assertArrayEquals(m.getMean(), m2.getMean());
    Assertions.assertArrayEquals(m.getSumOfSquares(), m2.getSumOfSquares());
    Assertions.assertArrayEquals(m.getVariance(), m2.getVariance());
    Assertions.assertArrayEquals(m.getStandardDeviation(), m2.getStandardDeviation());

    // Wrong size
    final ArrayMoment m4 = m.newInstance();
    m4.add(new int[] {99});
    Assertions.assertThrows(IllegalArgumentException.class, () -> m2.add(m4));
  }

  private void canComputeArrayMoment(String title, double[][] data, ArrayMoment r2) {
    final long size = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(data[i]);
    }
    Assertions.assertEquals(size + data.length, r2.getN());
    final double[] om1 = r2.getMean();
    final double[] om2 = r2.getSumOfSquares();
    final double[] ov = r2.getVariance();
    final double[] osd = r2.getStandardDeviation();

    for (int n = data[0].length; n-- > 0;) {
      final Statistics m1 = new Statistics();
      final SecondMoment m2 = new SecondMoment();
      for (int i = 0; i < data.length; i++) {
        m1.add(data[i][n]);
        m2.increment(data[i][n]);
      }
      TestAssertions.assertTest(m1.getMean(), om1[n], equality, () -> title + " Mean");
      TestAssertions.assertTest(m2.getResult(), om2[n], equality, () -> title + " 2nd Moment");
      TestAssertions.assertTest(m1.getVariance(), ov[n], equality, () -> title + " Variance");
      TestAssertions.assertTest(m1.getStandardDeviation(), osd[n], equality, () -> title + " SD");
    }
  }

  private void canComputeArrayMoment(String title, int[][] data, ArrayMoment r2) {
    final long size = r2.getN();
    for (int i = 0; i < data.length; i++) {
      r2.add(data[i]);
    }
    Assertions.assertEquals(size + data.length, r2.getN());
    final double[] om1 = r2.getMean();
    final double[] om2 = r2.getSumOfSquares();
    final double[] ov = r2.getVariance();
    final double[] osd = r2.getStandardDeviation();

    for (int n = data[0].length; n-- > 0;) {
      final Statistics m1 = new Statistics();
      final SecondMoment m2 = new SecondMoment();
      for (int i = 0; i < data.length; i++) {
        m1.add(data[i][n]);
        m2.increment(data[i][n]);
      }
      TestAssertions.assertTest(m1.getMean(), om1[n], equality, () -> title + " Mean");
      TestAssertions.assertTest(m2.getResult(), om2[n], equality, () -> title + " 2nd Moment");
      TestAssertions.assertTest(m1.getVariance(), ov[n], equality, () -> title + " Variance");
      TestAssertions.assertTest(m1.getStandardDeviation(), osd[n], equality, () -> title + " SD");
    }
  }

  @SeededTest
  void canComputeMomentForLargeSeries(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());

    final SimpleArrayMoment m1 = new SimpleArrayMoment();
    final SecondMoment m2 = new SecondMoment();
    final RollingArrayMoment r2 = new RollingArrayMoment();

    // Test if the standard Statistics object is good enough for
    // computing the mean and variance of sCMOS data from 60,000 frames. It seems it is.
    final SharedStateContinuousSampler g =
        SamplerUtils.createGaussianSampler(rng, 100.345, Math.PI);
    for (int i = 600000; i-- > 0;) {
      final double d = g.sample();
      final double[] data = new double[] {d};
      m1.add(data);
      m2.increment(d);
      r2.add(data);
    }
    logger.info(
        FunctionUtils.getSupplier("Mean %s vs %s, SD %s vs %s", Double.toString(m1.getMean()[0]),
            Double.toString(r2.getMean()[0]), Double.toString(m1.getStandardDeviation()[0]),
            Double.toString(r2.getStandardDeviation()[0])));
    TestAssertions.assertTest(m1.getMean()[0], r2.getMean()[0], equality, "Mean");
    Assertions.assertEquals(m2.getResult(), r2.getSumOfSquares()[0], "2nd Moment");
  }
}
