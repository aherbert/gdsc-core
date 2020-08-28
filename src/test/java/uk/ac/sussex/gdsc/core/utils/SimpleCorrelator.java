package uk.ac.sussex.gdsc.core.utils;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.Arrays;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * A test class to compute a correlation.
 */
class SimpleCorrelator {

  /** The x data. */
  private final TDoubleArrayList x = new TDoubleArrayList();

  /** The y data. */
  private final TDoubleArrayList y = new TDoubleArrayList();

  /**
   * Adds the values.
   *
   * @param v1 value 1
   * @param v2 value 2
   */
  void add(int v1, int v2) {
    x.add(v1);
    y.add(v2);
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  int[] getX() {
    return Arrays.stream(x.toArray()).mapToInt(v -> (int) v).toArray();
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  int[] getY() {
    return Arrays.stream(y.toArray()).mapToInt(v -> (int) v).toArray();
  }

  /**
   * Gets the sum X.
   *
   * @return the sum X
   */
  long getSumX() {
    return (long) x.sum();
  }

  /**
   * Gets the sum Y.
   *
   * @return the sum Y
   */
  long getSumY() {
    return (long) y.sum();
  }

  /**
   * Gets the number of data points.
   *
   * @return the n
   */
  int getN() {
    return x.size();
  }

  /**
   * Gets the sum of squared X.
   *
   * @return the sum squared X
   */
  long getSumXX() {
    return sumSquare(x);
  }

  /**
   * Gets the sum of squared Y.
   *
   * @return the sum squared Y
   */
  long getSumYY() {
    return sumSquare(y);
  }

  private static long sumSquare(TDoubleArrayList data) {
    long sum = 0;
    for (int i = 0; i < data.size(); i++) {
      final long v = (long) data.getQuick(i);
      sum += v * v;
    }
    return sum;
  }

  /**
   * Gets the sum of {@code X*Y}.
   *
   * @return the sum XY
   */
  long getSumXY() {
    long sum = 0;
    for (int i = 0; i < x.size(); i++) {
      final long v1 = (long) x.getQuick(i);
      final long v2 = (long) y.getQuick(i);
      sum += v1 * v2;
    }
    return sum;
  }

  /**
   * Gets the correlation.
   *
   * @return the correlation
   */
  double getCorrelation() {
    if (x.size() < 2) {
      // Q. Should this return NaN or 0 for size 1? Currently return 0.
      return x.isEmpty() ? Double.NaN : 0.0;
    }
    final PearsonsCorrelation c = new PearsonsCorrelation();
    return c.correlation(x.toArray(), y.toArray());
  }
}
