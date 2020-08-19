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

package uk.ac.sussex.gdsc.core.ij.process;

import ij.ImageStack;
import ij.plugin.filter.EDM;
import ij.process.ByteProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;

@SuppressWarnings({"javadoc"})
public class FhtTest {
  @Test
  public void canCheckPowerOf2() {
    Assertions.assertFalse(Fht.isPowerOf2(1), "1");
    Assertions.assertFalse(Fht.isPowerOf2(Integer.MAX_VALUE), "" + Integer.MAX_VALUE);
    int value = 2;
    // Until overflow
    while (value > 0) {
      Assertions.assertTrue(Fht.isPowerOf2(value), Integer.toString(value));
      Assertions.assertFalse(Fht.isPowerOf2(value - 1), Integer.toString(value - 1));
      Assertions.assertFalse(Fht.isPowerOf2(value + 1), Integer.toString(value + 1));
      value *= 2;
    }
  }

  @Test
  public void testConstructor() {
    final ImageProcessor ip = new FloatProcessor(4, 3);
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Fht(ip, false));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Fht(new float[9], 3, false));
    final ByteProcessor bp = new ByteProcessor(4, 4);
    final int index = 7;
    bp.set(index, 42);
    final Fht fht = new Fht(bp, false);
    Assertions.assertEquals(42, fht.getf(index));
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
    final int ex = 5;
    final int ey = 7;
    final int ox = 1;
    final int oy = 2;
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

    final float[] exp = (float[]) fhtE.getPixels();
    final float[] oobs = (float[]) fhtO.getPixels();

    // This is not exact for the divide since the FHT2 magnitude is computed
    // using double*double + double*double rather than float*float + float*float,
    // i.e. the float are converted to double before multiplication.
    if (mode == 2) {
      TestAssertions.assertArrayTest(exp, oobs, TestHelper.floatsAreClose(1e-5, 0));
    } else {
      Assertions.assertArrayEquals(exp, oobs);
    }
  }

  private static FloatProcessor createProcessor(int size, int x, int y, int width, int height) {
    final ByteProcessor bp = new ByteProcessor(size, size);
    bp.setColor(255);
    bp.fillOval(x, y, width, height);
    final EDM e = new EDM();
    return e.makeFloatEDM(bp, 0, true);
  }

  @Test
  public void canSwapQuadrants() {
    final int size = 16;
    final FloatProcessor fp1 =
        new FloatProcessor(size, size, SimpleArrayUtils.newArray(size * size, 0, 1f));
    final FloatProcessor fp2 = (FloatProcessor) fp1.duplicate();
    final FloatProcessor fp3 = (FloatProcessor) fp1.duplicate();

    final FHT fht1 = new FHT(fp1);
    final Fht fht2 = new Fht(fp2);

    fht1.swapQuadrants();
    fht2.swapQuadrants();
    Fht.swapQuadrants(fp3);

    Assertions.assertArrayEquals((float[]) fht1.getPixels(), (float[]) fht2.getPixels());
    Assertions.assertArrayEquals((float[]) fht1.getPixels(), (float[]) fp3.getPixels());

    final FloatProcessor fp4 = new FloatProcessor(4, 3);
    Assertions.assertThrows(IllegalArgumentException.class, () -> Fht.swapQuadrants(fp4));
    final FloatProcessor fp5 = new FloatProcessor(3, 4);
    Assertions.assertThrows(IllegalArgumentException.class, () -> Fht.swapQuadrants(fp5));
    final FloatProcessor fp6 = new FloatProcessor(3, 3);
    Assertions.assertThrows(IllegalArgumentException.class, () -> Fht.swapQuadrants(fp6));
  }

  @Test
  public void canGetComplexTransform() {
    final int size = 16;
    final FloatProcessor fp1 = createProcessor(size, 5, 7, 4, 6);
    final FloatProcessor fp2 = (FloatProcessor) fp1.duplicate();

    final FHT fht1 = new FHT(fp1);
    final Fht fht2 = new Fht(fp2);

    Assertions.assertThrows(IllegalArgumentException.class, () -> fht2.getComplexTransform());
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> fht2.getComplexTransformProcessors());

    fht1.transform();
    fht2.transform();

    final ImageStack stack1 = fht1.getComplexTransform();
    final ImageStack stack2 = fht2.getComplexTransform();

    Assertions.assertArrayEquals(stack1.getImageArray(), stack2.getImageArray());

    final FloatProcessor[] processors = fht2.getComplexTransformProcessors();

    for (int i = 0; i < 2; i++) {
      Assertions.assertArrayEquals((float[]) stack1.getPixels(i + 1),
          (float[]) processors[i].getPixels());
    }
  }

  @Test
  public void canCopy() {
    final int size = 16;
    final FloatProcessor fp = createProcessor(size, 5, 7, 4, 6);
    final Fht fht1 = new Fht(fp);
    final Fht fht2 = fht1.getCopy();
    Assertions.assertArrayEquals((float[]) fht1.getPixels(), (float[]) fht2.getPixels());
  }

  @Test
  public void testToString() {
    final int size = 4;
    final FloatProcessor fp = new FloatProcessor(size, size);
    final Fht fht = new Fht(fp);
    String text = fht.toString();
    Assertions.assertTrue(text.contains(Integer.toString(size)));
    Assertions.assertTrue(text.contains(Boolean.FALSE.toString()));
    Assertions.assertFalse(text.contains(Boolean.TRUE.toString()));
    fht.transform();
    text = fht.toString();
    Assertions.assertFalse(text.contains(Boolean.FALSE.toString()));
    Assertions.assertTrue(text.contains(Boolean.TRUE.toString()));
  }
}
