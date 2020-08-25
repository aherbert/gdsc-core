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

import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TimingService;

@SuppressWarnings({"javadoc"})
class SortConversionSpeedTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(SortConversionSpeedTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private abstract class FloatConversionTimingTask extends BaseTimingTask {
    final int size;

    public FloatConversionTimingTask(String name, int size) {
      super(name + " size=" + size);
      this.size = size;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public Object getData(int index) {
      return null;
    }

    @Override
    public Object run(Object data) {
      for (int index = size; index-- > 0;) {
        if (convertBack(convert(index)) != index) {
          throw new RuntimeException();
        }
      }
      return null;
    }

    abstract float convert(int index);

    abstract int convertBack(float value);
  }

  private class FloatCastConversion extends FloatConversionTimingTask {
    public FloatCastConversion(int size) {
      super("float cast", size);
    }

    @Override
    float convert(int index) {
      return index;
    }

    @Override
    int convertBack(float value) {
      return (int) value;
    }
  }

  private class FloatBitConversion extends FloatConversionTimingTask {
    public FloatBitConversion(int size) {
      super("float bit", size);
    }

    @Override
    float convert(int index) {
      return Float.intBitsToFloat(index);
    }

    @Override
    int convertBack(float value) {
      return Float.floatToRawIntBits(value);
    }
  }

  @Test
  void canTestFloatBitConversionSpeed() {
    Assumptions.assumeTrue(false);

    // Q. Is it faster to use:
    // int index;
    // float value = index;
    // index = (int) value;
    // OR
    // float value = Float.intBitsToFloat(index)
    // index = Float.floatToRawIntBits(index);

    // Note that is the number of indices is above the max value that can be
    // stored in a float mantissa then the casting case is invalid.

    final int[] size = new int[] {100, 10000, 1000000};
    final int maxn = size[size.length - 1];

    for (int index = 0; index < size.length; index++) {
      final TimingService ts = new TimingService(10 * maxn / size[index]);
      ts.execute(new FloatCastConversion(size[index]));
      ts.execute(new FloatBitConversion(size[index]));

      final int resultsSize = ts.getSize();
      ts.repeat(resultsSize);
      logger.info(ts.getReport(resultsSize));
    }
  }

  private abstract class DoubleConversionTimingTask extends BaseTimingTask {
    final int size;

    public DoubleConversionTimingTask(String name, int size) {
      super(name + " size=" + size);
      this.size = size;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public Object getData(int index) {
      return null;
    }

    @Override
    public Object run(Object data) {
      for (int i = size; i-- > 0;) {
        if (convertBack(convert(i)) != i) {
          throw new RuntimeException();
        }
      }
      return null;
    }

    abstract double convert(int index);

    abstract int convertBack(double value);
  }

  private class DoubleCastConversion extends DoubleConversionTimingTask {
    public DoubleCastConversion(int size) {
      super("double cast", size);
    }

    @Override
    double convert(int index) {
      return index;
    }

    @Override
    int convertBack(double value) {
      return (int) value;
    }
  }

  private class DoubleBitConversion extends DoubleConversionTimingTask {
    public DoubleBitConversion(int size) {
      super("double bit", size);
    }

    @Override
    double convert(int index) {
      return Double.longBitsToDouble(index);
    }

    @Override
    int convertBack(double value) {
      return (int) Double.doubleToRawLongBits(value);
    }
  }

  @Test
  void canTestDoubleBitConversionSpeed() {
    Assumptions.assumeTrue(false);

    // Q. Is it faster to use:
    // int i;
    // double f = i;
    // i = (int) f;
    // OR
    // double f = Double.longBitsToDouble(i)
    // i = (int) Double.doubleToRawLongBits(i);

    // Note that is the number of indices is above the max value that can be
    // stored in a float mantissa then the casting case is invalid.

    // 1 << 30 takes too long to run
    final int[] size = new int[] {100, 10000, 1000000, 1 << 25};
    final int maxn = size[size.length - 1];

    for (int i = 0; i < size.length; i++) {
      final TimingService ts = new TimingService(maxn / size[i]);
      ts.execute(new DoubleCastConversion(size[i]));
      ts.execute(new DoubleBitConversion(size[i]));

      final int resultsSize = ts.getSize();
      ts.repeat(resultsSize);
      logger.info(ts.getReport(resultsSize));
    }
  }
}
