package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;
import uk.ac.sussex.gdsc.test.utils.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.*;import uk.ac.sussex.gdsc.test.rng.RngFactory;import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TimingService;

@SuppressWarnings({"javadoc"})
public class SortTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(SortTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private abstract class FloatConversionTimingTask extends BaseTimingTask {
    final int n;

    public FloatConversionTimingTask(String name, int n) {
      super(name + " n=" + n);
      this.n = n;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public Object getData(int i) {
      return null;
    }

    @Override
    public Object run(Object data) {
      for (int i = n; i-- > 0;) {
        if (convertBack(convert(i)) != i) {
          throw new RuntimeException();
        }
      }
      return null;
    }

    abstract float convert(int i);

    abstract int convertBack(float f);
  }

  private class FloatCastConversion extends FloatConversionTimingTask {
    public FloatCastConversion(int n) {
      super("float cast", n);
    }

    @Override
    float convert(int i) {
      return i;
    }

    @Override
    int convertBack(float f) {
      return (int) f;
    }
  }

  private class FloatBitConversion extends FloatConversionTimingTask {
    public FloatBitConversion(int n) {
      super("float bit", n);
    }

    @Override
    float convert(int i) {
      return Float.intBitsToFloat(i);
    }

    @Override
    int convertBack(float f) {
      return Float.floatToRawIntBits(f);
    }
  }

  @Test
  public void canTestFloatBitConversionSpeed() {
    Assumptions.assumeTrue(false);

    // Q. Is it faster to use:
    // int i;
    // float f = i;
    // i = (int) f;
    // OR
    // float f = Float.intBitsToFloat(i)
    // i = Float.floatToRawIntBits(i);

    // Note that is the number of indices is above the max value that can be
    // stored in a float mantissa then the casting case is invalid.

    final int[] n = new int[] {100, 10000, 1000000};
    final int maxn = n[n.length - 1];

    for (int i = 0; i < n.length; i++) {
      final TimingService ts = new TimingService(10 * maxn / n[i]);
      ts.execute(new FloatCastConversion(n[i]));
      ts.execute(new FloatBitConversion(n[i]));

      final int size = ts.getSize();
      ts.repeat(size);
      logger.info(ts.getReport(size));
    }
  }

  private abstract class DoubleConversionTimingTask extends BaseTimingTask {
    final int n;

    public DoubleConversionTimingTask(String name, int n) {
      super(name + " n=" + n);
      this.n = n;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public Object getData(int i) {
      return null;
    }

    @Override
    public Object run(Object data) {
      for (int i = n; i-- > 0;) {
        if (convertBack(convert(i)) != i) {
          throw new RuntimeException();
        }
      }
      return null;
    }

    abstract double convert(int i);

    abstract int convertBack(double f);
  }

  private class DoubleCastConversion extends DoubleConversionTimingTask {
    public DoubleCastConversion(int n) {
      super("double cast", n);
    }

    @Override
    double convert(int i) {
      return i;
    }

    @Override
    int convertBack(double f) {
      return (int) f;
    }
  }

  private class DoubleBitConversion extends DoubleConversionTimingTask {
    public DoubleBitConversion(int n) {
      super("double bit", n);
    }

    @Override
    double convert(int i) {
      return Double.longBitsToDouble(i);
    }

    @Override
    int convertBack(double f) {
      return (int) Double.doubleToRawLongBits(f);
    }
  }

  @Test
  public void canTestDoubleBitConversionSpeed() {
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
    final int[] n = new int[] {100, 10000, 1000000, 1 << 25};
    final int maxn = n[n.length - 1];

    for (int i = 0; i < n.length; i++) {
      final TimingService ts = new TimingService(maxn / n[i]);
      ts.execute(new DoubleCastConversion(n[i]));
      ts.execute(new DoubleBitConversion(n[i]));

      final int size = ts.getSize();
      ts.repeat(size);
      logger.info(ts.getReport(size));
    }
  }
}
