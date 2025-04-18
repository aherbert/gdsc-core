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

import java.util.function.Supplier;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class DoubleMedianWindowTest {

  private static class UpdateableSupplier implements Supplier<String> {
    int pos;
    int radius;

    @Override
    public String get() {
      return String.format("Position %d, Radius %d", pos, radius);
    }

    Supplier<String> update(int pos, int radius) {
      this.pos = pos;
      this.radius = radius;
      return this;
    }
  }

  private final int dataSize = 2000;
  private final int[] radii = new int[] {0, 1, 2, 4, 8, 16};
  private final double[] values = new double[] {0, -1.1, 2.2};
  private final boolean[] sortedScans = {true, false};

  @Test
  void testWrap() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DoubleMedianWindow.wrap(new double[5], -1));
    Assertions.assertThrows(NullPointerException.class, () -> DoubleMedianWindow.wrap(null, 5));
    final double[] data = new double[3];
    for (int radius = 0; radius < data.length * 2; radius++) {
      final DoubleMedianWindow mw = DoubleMedianWindow.wrap(new double[5], radius);
      Assertions.assertEquals(0, mw.getPosition());
      Assertions.assertEquals(radius, mw.getRadius(), "Radius is not clipped to length");
      Assertions.assertFalse(mw.isSortedScan());
    }
  }

  @Test
  void canComputeForArrayLength1() {
    final double[] data = {42};
    for (final int radius : radii) {
      final DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
      Assertions.assertEquals(data[0], mw.getMedian());
    }
  }

  @SeededTest
  void canComputeMedianForRandomDataUsingSingleIncrement(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    canComputeMedianForDataUsingSingleIncrement(
        MedianWindowTest.createRandomDataDouble(rg, dataSize));
  }

  @SeededTest
  void canComputeMedianForRandomDataUsingSetPosition(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    canComputeMedianForDataUsingSetPosition(MedianWindowTest.createRandomDataDouble(rg, dataSize));
  }

  @SeededTest
  void canComputeMedianForRandomDataUsingBigIncrement(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    canComputeMedianForDataUsingBigIncrement(MedianWindowTest.createRandomDataDouble(rg, dataSize));
  }

  @Test
  void canComputeMedianForDuplicateDataUsingSingleIncrement() {
    for (final double value : values) {
      canComputeMedianForDataUsingSingleIncrement(
          MedianWindowTest.createDuplicateDataDouble(dataSize, value));
    }
  }

  @Test
  void canComputeMedianForDuplicateDataUsingSetPosition() {
    for (final double value : values) {
      canComputeMedianForDataUsingSetPosition(
          MedianWindowTest.createDuplicateDataDouble(dataSize, value));
    }
  }

  @Test
  void canComputeMedianForDuplicateDataUsingBigIncrement() {
    for (final double value : values) {
      canComputeMedianForDataUsingBigIncrement(
          MedianWindowTest.createDuplicateDataDouble(dataSize, value));
    }
  }

  @SeededTest
  void canComputeMedianForSparseDataUsingSingleIncrement(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (final double value : values) {
      canComputeMedianForDataUsingSingleIncrement(
          MedianWindowTest.createSparseDataDouble(rng, dataSize, value));
    }
  }

  @SeededTest
  void canComputeMedianForSparseDataUsingSetPosition(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (final double value : values) {
      canComputeMedianForDataUsingSetPosition(
          MedianWindowTest.createSparseDataDouble(rng, dataSize, value));
    }
  }

  @SeededTest
  void canComputeMedianForSparseDataUsingBigIncrement(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (final double value : values) {
      canComputeMedianForDataUsingBigIncrement(
          MedianWindowTest.createSparseDataDouble(rng, dataSize, value));
    }
  }

  @SeededTest
  void canComputeMedianForNaNDataUsingSingleIncrement(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (final int start : new int[] {2, 5, 10, 40}) {
      canComputeMedianForDataUsingSingleIncrement(
          MedianWindowTest.createNaNDataDouble(rng, 50, start));
    }
  }

  @SeededTest
  void canComputeMedianForNaNDataUsingSetPosition(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (final int start : new int[] {2, 5, 10, 40}) {
      canComputeMedianForDataUsingSetPosition(MedianWindowTest.createNaNDataDouble(rng, 50, start));
    }
  }

  @SeededTest
  void canComputeMedianForNaNDataUsingBigIncrement(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (final int start : new int[] {2, 5, 10, 40}) {
      canComputeMedianForDataUsingBigIncrement(
          MedianWindowTest.createNaNDataDouble(rng, 50, start));
    }
  }

  private void canComputeMedianForDataUsingSingleIncrement(double[] data) {
    final UpdateableSupplier msg = new UpdateableSupplier();
    for (final int radius : radii) {
      for (final boolean sortedScan : sortedScans) {
        final DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
        mw.setSortedScan(sortedScan);
        Assertions.assertEquals(sortedScan, mw.isSortedScan());
        for (int i = 0; i < data.length; i++) {
          final double median = mw.getMedian();
          mw.increment();
          final double median2 = MedianWindowTest.calculateMedian(data, i, radius);
          Assertions.assertEquals(median2, median, 1e-6, msg.update(i, radius));
        }
      }
    }
  }

  private void canComputeMedianForDataUsingSetPosition(double[] data) {
    final UpdateableSupplier msg = new UpdateableSupplier();
    for (final int radius : radii) {
      for (final boolean sortedScan : sortedScans) {
        final DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
        mw.setSortedScan(sortedScan);
        Assertions.assertEquals(sortedScan, mw.isSortedScan());
        for (int i = 0; i < data.length; i += 10) {
          mw.setPosition(i);
          Assertions.assertEquals(i, mw.getPosition());
          final double median = mw.getMedian();
          final double median2 = MedianWindowTest.calculateMedian(data, i, radius);
          Assertions.assertEquals(median2, median, 1e-6, msg.update(i, radius));
          mw.setPosition(i);
          Assertions.assertEquals(median, mw.getMedian());
          mw.setPosition(i + 1);
          mw.setPosition(i);
          Assertions.assertEquals(median, mw.getMedian());
        }
        for (int i = data.length - 1; i >= 0; i -= 10) {
          mw.setPosition(i);
          Assertions.assertEquals(i, mw.getPosition());
          final double median = mw.getMedian();
          final double median2 = MedianWindowTest.calculateMedian(data, i, radius);
          Assertions.assertEquals(median2, median, 1e-6, msg.update(i, radius));
          mw.setPosition(i);
          Assertions.assertEquals(median, mw.getMedian());
          mw.setPosition(i - 1);
          mw.setPosition(i);
          Assertions.assertEquals(median, mw.getMedian());
        }
      }
    }
  }

  private void canComputeMedianForDataUsingBigIncrement(double[] data) {
    final UpdateableSupplier msg = new UpdateableSupplier();
    final int increment = 10;
    for (final int radius : radii) {
      for (final boolean sortedScan : sortedScans) {
        final DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
        mw.setSortedScan(sortedScan);
        Assertions.assertEquals(sortedScan, mw.isSortedScan());
        for (int i = 0; i < data.length; i += increment) {
          final double median = mw.getMedian();
          mw.increment(increment);
          final double median2 = MedianWindowTest.calculateMedian(data, i, radius);
          Assertions.assertEquals(median2, median, 1e-6, msg.update(i, radius));
        }
      }
    }
  }

  @Test
  void cannotComputeMedianBackToInputArrayUsingSingleIncrement() {
    final double[] data = SimpleArrayUtils.newArray(dataSize, 0.0, 1);
    for (final int radius : radii) {
      if (radius <= 1) {
        continue;
      }

      final double[] in = data.clone();
      final double[] e = new double[in.length];
      DoubleMedianWindow mw = new DoubleMedianWindow(in, radius);
      for (int i = 0; i < data.length; i++) {
        e[i] = mw.getMedian();
        mw.increment();
      }
      // Must create a new window
      mw = new DoubleMedianWindow(in, radius);
      for (int i = 0; i < data.length; i++) {
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
  void canIncrementThroughTheDataArray(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final double[] data = MedianWindowTest.createRandomDataDouble(rg, 300);
    final UpdateableSupplier msg = new UpdateableSupplier();
    for (final int radius : radii) {
      DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
      int index = 0;
      while (mw.isValidPosition()) {
        final double median = mw.getMedian();
        final double median2 = MedianWindowTest.calculateMedian(data, index, radius);
        Assertions.assertEquals(median2, median, 1e-6, msg.update(index, radius));

        mw.increment();
        index++;
      }
      Assertions.assertEquals(index, data.length, "Not all data interated");

      mw = new DoubleMedianWindow(data, radius);
      index = 0;
      do {
        final double median = mw.getMedian();
        final double median2 = MedianWindowTest.calculateMedian(data, index, radius);
        Assertions.assertEquals(median2, median, 1e-6, msg.update(index, radius));

        index++;
      } while (mw.increment());
      Assertions.assertEquals(index, data.length, "Not all data interated");
    }
  }

  @SeededTest
  void canIncrementThroughTheDataArrayUsingBigIncrement(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final double[] data = MedianWindowTest.createRandomDataDouble(rg, 300);
    final UpdateableSupplier msg = new UpdateableSupplier();
    final int increment = 10;
    for (final int radius : radii) {
      final DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
      int index = 0;
      while (mw.isValidPosition()) {
        final double median = mw.getMedian();
        final double median2 = MedianWindowTest.calculateMedian(data, index, radius);
        Assertions.assertEquals(median2, median, 1e-6, msg.update(index, radius));

        mw.increment(increment);
        index += increment;
      }
    }
  }

  @SeededTest
  void returnNaNForInvalidPositions(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final double[] data = MedianWindowTest.createRandomDataDouble(rg, 300);
    for (final int radius : radii) {
      DoubleMedianWindow mw = new DoubleMedianWindow(data, radius);
      for (int i = 0; i < data.length; i++) {
        mw.increment();
      }
      Assertions.assertEquals(Double.NaN, mw.getMedian(), 1e-6);

      mw = new DoubleMedianWindow(data, radius);
      while (mw.isValidPosition()) {
        mw.increment();
      }
      Assertions.assertEquals(Double.NaN, mw.getMedian(), 1e-6);

      mw = new DoubleMedianWindow(data, radius);
      mw.setPosition(data.length + 10);
      Assertions.assertEquals(Double.NaN, mw.getMedian(), 1e-6);
    }
  }
}
