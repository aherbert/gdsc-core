package uk.ac.sussex.gdsc.core.ij.process;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;

import ij.plugin.filter.EDM;
import ij.process.ByteProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class Fht2Test {
  @Test
  public void canCheckPowerOf2() {
    Assertions.assertFalse(Fht.isPowerOf2(1), "1");
    Assertions.assertFalse(Fht.isPowerOf2(Integer.MAX_VALUE), "" + Integer.MAX_VALUE);
    int i = 2;
    while (i > 0) // Until overflow
    {
      Assertions.assertTrue(Fht.isPowerOf2(i), Integer.toString(i));
      Assertions.assertFalse(Fht.isPowerOf2(i - 1), Integer.toString(i - 1));
      Assertions.assertFalse(Fht.isPowerOf2(i + 1), Integer.toString(i + 1));
      i *= 2;
    }
  }

  @Test
  public void canConjugateMultiply() {
    canCompute(0, false);
  }

  @Test
  public void canMultiply() {
    canCompute(1, false);
  }

  @Test
  public void canDivide() {
    canCompute(2, false);
  }

  @Test
  public void canFastConjugateMultiply() {
    canCompute(0, true);
  }

  @Test
  public void canFastMultiply() {
    canCompute(1, true);
  }

  @Test
  public void canFastDivide() {
    canCompute(2, true);
  }

  private static void canCompute(int mode, boolean fast) {
    final int size = 16;
    final int ex = 5, ey = 7;
    final int ox = 1, oy = 2;
    final FloatProcessor fp1 = createProcessor(size, ex, ey, 4, 4);
    final FloatProcessor fp2 = createProcessor(size, size / 2 + ox, size / 2 + oy, 4, 4);

    // These will duplicate in the constructor
    final FHT fhtA = new FHT(fp1);
    final FHT fhtB = new FHT(fp2);
    fhtA.transform();
    fhtB.transform();

    final Fht fht1 = new Fht(fp1);
    final Fht fht2 = new Fht(fp2);
    fht1.transform();
    fht2.transform();

    FHT fhtE;
    Fht fhtO;
    switch (mode) {
      case 2:
        fhtE = fhtA.divide(fhtB);
        if (fast) {
          fht2.initialiseFastOperations();
        }
        fhtO = fht1.divide(fht2);
        break;
      case 1:
        fhtE = fhtA.multiply(fhtB);
        if (fast) {
          fht2.initialiseFastMultiply();
        }
        fhtO = fht1.multiply(fht2);
        break;
      default:
        fhtE = fhtA.conjugateMultiply(fhtB);
        if (fast) {
          fht2.initialiseFastMultiply();
        }
        fhtO = fht1.conjugateMultiply(fht2);
        break;
    }
    fhtE.inverseTransform();
    fhtO.inverseTransform();

    final float[] e = (float[]) fhtE.getPixels();
    final float[] o = (float[]) fhtO.getPixels();

    // This is not exact for the divide since the FHT2 magnitude is computed
    // using double*double + double*double rather than float*float + float*float,
    // i.e. the float are converted to double before multiplication.
    if (mode == 2) {
      TestAssertions.assertArrayTest(e, o, TestHelper.floatsAreClose(1e-5, 0));
    } else {
      Assertions.assertArrayEquals(e, o);
    }
  }

  private static FloatProcessor createProcessor(int size, int x, int y, int w, int h) {
    final ByteProcessor bp = new ByteProcessor(size, size);
    bp.setColor(255);
    bp.fillOval(x, y, w, h);
    final EDM e = new EDM();
    return e.makeFloatEDM(bp, 0, true);
  }

  @Test
  public void canSwapQuadrants() {
    final int size = 16;
    final FloatProcessor fp1 =
        new FloatProcessor(size, size, SimpleArrayUtils.newArray(size * size, 0, 1f));
    final FloatProcessor fp2 = (FloatProcessor) fp1.duplicate();

    final FHT fht1 = new FHT(fp1);
    final Fht fht2 = new Fht(fp2);

    fht1.swapQuadrants();
    fht2.swapQuadrants();

    Assertions.assertArrayEquals((float[]) fp1.getPixels(), (float[]) fp2.getPixels());
  }
}
