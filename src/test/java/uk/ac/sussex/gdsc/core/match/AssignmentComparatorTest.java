package uk.ac.sussex.gdsc.core.match;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TimingService;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class AssignmentComparatorTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(AssignmentComparatorTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private static class IntegerSortData implements Comparable<IntegerSortData> {
    final int data;

    IntegerSortData(int data) {
      this.data = data;
    }

    @Override
    public int compareTo(IntegerSortData other) {
      return Integer.compare(data, other.data);
    }
  }

  private static class DoubleSortData implements Comparable<DoubleSortData> {
    final double data;

    DoubleSortData(double data) {
      this.data = data;
    }

    @Override
    public int compareTo(DoubleSortData other) {
      return Double.compare(data, other.data);
    }
  }

  private static class AssignmentComparatorTestData {
    int[][] intData;
    double[][] doubleData;
    int[][] intExp;
    double[][] doubleExp;
    IntegerSortData[][] intSortData;
    DoubleSortData[][] doubleSortData;
    Assignment[][] assignmentData;

    void clear() {
      if (intData == null) {
        return;
      }
      for (int i = 0; i < intData.length; i++) {
        intData[i] = null;
        doubleData[i] = null;
        intExp[i] = null;
        doubleExp[i] = null;
        intSortData[i] = null;
        doubleSortData[i] = null;
        assignmentData[i] = null;
      }
      intData = null;
      doubleData = null;
      intExp = null;
      doubleExp = null;
      intSortData = null;
      doubleSortData = null;
      assignmentData = null;
    }
  }

  private static AssignmentComparatorTestData getData(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    final int size = 100;
    // The assignment data will be concatenated blocks of sorted arrays
    final int blocks = 50;
    final int blockSize = 10;
    final int length = blocks * blockSize;

    final AssignmentComparatorTestData data = new AssignmentComparatorTestData();
    data.intData = new int[size][];
    data.doubleData = new double[size][];
    data.intExp = new int[size][];
    data.doubleExp = new double[size][];
    data.intSortData = new IntegerSortData[size][];
    data.doubleSortData = new DoubleSortData[size][];
    data.assignmentData = new Assignment[size][];
    final int upper = 65536;
    for (int i = size; i-- > 0;) {
      int[] idata = new int[length];
      double[] ddata = new double[length];
      final IntegerSortData[] sdata = new IntegerSortData[length];
      final DoubleSortData[] dsdata = new DoubleSortData[length];
      final Assignment[] adata = new Assignment[length];
      data.intData[i] = idata;
      data.doubleData[i] = ddata;
      data.intSortData[i] = sdata;
      data.doubleSortData[i] = dsdata;
      data.assignmentData[i] = adata;

      // Build the data of sorted blocks
      for (int b = 0; b < blocks; b++) {
        final int[] block = new int[blockSize];
        for (int j = 0; j < block.length; j++) {
          block[j] = r.nextInt(upper);
        }
        Arrays.sort(block);
        System.arraycopy(block, 0, idata, b * blockSize, blockSize);
      }

      // Copy
      for (int j = length; j-- > 0;) {
        final int k = idata[j];
        final double d = k / upper;
        ddata[j] = d;
        sdata[j] = new IntegerSortData(k);
        dsdata[j] = new DoubleSortData(d);
        adata[j] = new ImmutableAssignment(0, 0, d);
      }

      idata = idata.clone();
      Arrays.sort(idata);
      data.intExp[i] = idata;
      ddata = ddata.clone();
      Arrays.sort(ddata);
      data.doubleExp[i] = ddata;
    }

    return data;
  }

  private abstract class MyTimingTask extends BaseTimingTask {
    int[][] intData;
    double[][] doubleData;
    int[][] intExp;
    double[][] doubleExp;
    IntegerSortData[][] intSortData;
    DoubleSortData[][] doubleSortData;
    Assignment[][] assignmentData;

    public MyTimingTask(String name, AssignmentComparatorTestData data) {
      super(name);
      intData = data.intData;
      doubleData = data.doubleData;
      intExp = data.intExp;
      doubleExp = data.doubleExp;
      intSortData = data.intSortData;
      doubleSortData = data.doubleSortData;
      assignmentData = data.assignmentData;
    }

    @Override
    public int getSize() {
      return intData.length;
    }
  }

  private abstract class AssignmentTimingTask extends MyTimingTask {
    public AssignmentTimingTask(String name, AssignmentComparatorTestData data) {
      super(name, data);
    }

    @Override
    public void check(int index, Object result) {
      final double[] exp = doubleExp[index];
      final Assignment[] obs = (Assignment[]) result;
      for (int j = 0; j < exp.length; j++) {
        if (exp[j] != obs[j].getDistance()) {
          throw new AssertionError(getName());
        }
      }
    }
  }

  @SeededTest
  public void canComputeSortSpeed(RandomSeed seed) {
    final int n = logger.isLoggable(Level.INFO) ? 5 : 1;

    final AssignmentComparatorTestData data = getData(seed);

    final TimingService ts = new TimingService(n);
    ts.execute(new MyTimingTask("int[]", data) {
      @Override
      public Object getData(int index) {
        return intData[index].clone();
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((int[]) data);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final int[] exp = intExp[index];
        final int[] obs = (int[]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j]) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    ts.execute(new MyTimingTask("double[]", data) {
      @Override
      public Object getData(int index) {
        return doubleData[index].clone();
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((double[]) data);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final double[] exp = doubleExp[index];
        final double[] obs = (double[]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j]) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    ts.execute(new MyTimingTask("long[]", data) {
      @Override
      public Object getData(int index) {
        final long[] data = new long[intData[index].length];
        for (int j = data.length; j-- > 0;) {
          data[j] = intData[index][j];
        }
        return data;
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((long[]) data);
        return data;
      }
    });
    final Comparator<int[]> c1 = new Comparator<int[]>() {
      @Override
      public int compare(int[] o1, int[] o2) {
        if (o1[0] < o2[0]) {
          return -1;
        }
        if (o1[0] > o2[0]) {
          return 1;
        }
        return 0;
      }
    };
    ts.execute(new MyTimingTask("int[][]", data) {
      @Override
      public Object getData(int index) {
        final int[] d = intData[index];
        final int[][] data = new int[d.length][];
        for (int j = d.length; j-- > 0;) {
          data[j] = new int[] {d[j]};
        }
        return data;
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((int[][]) data, c1);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final int[] exp = intExp[index];
        final int[][] obs = (int[][]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j][0]) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    final Comparator<double[]> c2 = new Comparator<double[]>() {
      @Override
      public int compare(double[] o1, double[] o2) {
        if (o1[0] < o2[0]) {
          return -1;
        }
        if (o1[0] > o2[0]) {
          return 1;
        }
        return 0;
      }
    };
    ts.execute(new MyTimingTask("double[][]", data) {
      @Override
      public Object getData(int index) {
        final double[] d = doubleData[index];
        final double[][] data = new double[d.length][];
        for (int j = d.length; j-- > 0;) {
          data[j] = new double[] {d[j]};
        }
        return data;
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((double[][]) data, c2);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final double[] exp = doubleExp[index];
        final double[][] obs = (double[][]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j][0]) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    final Comparator<int[]> c3 = new Comparator<int[]>() {
      @Override
      public int compare(int[] o1, int[] o2) {
        return o1[0] - o2[0];
      }
    };
    ts.execute(new MyTimingTask("int[][] subtract", data) {
      @Override
      public Object getData(int index) {
        final int[] d = intData[index];
        final int[][] data = new int[d.length][];
        for (int j = d.length; j-- > 0;) {
          data[j] = new int[] {d[j]};
        }
        return data;
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((int[][]) data, c3);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final int[] exp = intExp[index];
        final int[][] obs = (int[][]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j][0]) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    final Comparator<long[]> c4 = new Comparator<long[]>() {
      @Override
      public int compare(long[] o1, long[] o2) {
        if (o1[0] < o2[0]) {
          return -1;
        }
        if (o1[0] > o2[0]) {
          return 1;
        }
        return 0;
      }
    };
    ts.execute(new MyTimingTask("long[][]", data) {
      @Override
      public Object getData(int index) {
        final int[] d = intData[index];
        final long[][] data = new long[d.length][];
        for (int j = d.length; j-- > 0;) {
          data[j] = new long[] {d[j]};
        }
        return data;
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((long[][]) data, c4);
        return data;
      }
    });
    ts.execute(new MyTimingTask("IntegerSortData[]", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(intSortData[index], intSortData[index].length);
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((IntegerSortData[]) data);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final int[] exp = intExp[index];
        final IntegerSortData[] obs = (IntegerSortData[]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j].data) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    ts.execute(new MyTimingTask("DoubleSortData[]", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(doubleSortData[index], doubleSortData[index].length);
      }

      @Override
      public Object run(Object data) {
        Arrays.sort((DoubleSortData[]) data);
        return data;
      }

      @Override
      public void check(int index, Object result) {
        final double[] exp = doubleExp[index];
        final DoubleSortData[] obs = (DoubleSortData[]) result;
        for (int j = 0; j < exp.length; j++) {
          if (exp[j] != obs[j].data) {
            throw new AssertionError(getName());
          }
        }
      }
    });
    ts.execute(new AssignmentTimingTask("Assignment[] sort1", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(assignmentData[index], assignmentData[index].length);
      }

      @Override
      public Object run(Object data) {
        AssignmentComparator.sort1((Assignment[]) data);
        return data;
      }
    });
    ts.execute(new AssignmentTimingTask("Assignment[] sort2", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(assignmentData[index], assignmentData[index].length);
      }

      @Override
      public Object run(Object data) {
        AssignmentComparator.sort2((Assignment[]) data);
        return data;
      }
    });
    ts.execute(new AssignmentTimingTask("Assignment[] sort3", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(assignmentData[index], assignmentData[index].length);
      }

      @Override
      public Object run(Object data) {
        AssignmentComparator.sort3((Assignment[]) data);
        return data;
      }
    });
    ts.execute(new AssignmentTimingTask("Assignment[] sort4", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(assignmentData[index], assignmentData[index].length);
      }

      @Override
      public Object run(Object data) {
        AssignmentComparator.sort4((Assignment[]) data);
        return data;
      }
    });
    ts.execute(new AssignmentTimingTask("Assignment[] sort", data) {
      @Override
      public Object getData(int index) {
        return Arrays.copyOf(assignmentData[index], assignmentData[index].length);
      }

      @Override
      public Object run(Object data) {
        AssignmentComparator.sort((Assignment[]) data);
        return data;
      }
    });

    ts.check();

    if (n == 1) {
      return;
    }

    final int size = ts.repeat();
    ts.repeat(size);

    logger.info(ts.getReport(size));

    data.clear();
  }
}
