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

package uk.ac.sussex.gdsc.core.ij;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class ImageJHashUtilsTest {

  int size = 50;

  @SeededTest
  public void canDigestByteProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final byte[] data = new byte[size];
    r.nextBytes(data);

    final String d1 = ImageJHashUtils.digest(new ByteProcessor(size, 1, data));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new ByteProcessor(size, 1, data));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  @SeededTest
  public void canDigestShortProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final short[] data = new short[size];
    for (int i = 0; i < size; i++) {
      data[i] = (short) ((r.nextDouble() - 0.5) * 2 * Short.MAX_VALUE);
    }

    final String d1 = ImageJHashUtils.digest(new ShortProcessor(size, 1, data, null));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new ShortProcessor(size, 1, data, null));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  @SeededTest
  public void canDigestFloatProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final float[] data = new float[size];
    for (int i = 0; i < size; i++) {
      data[i] = (r.nextFloat() - 0.5f) * 2f;
    }

    final String d1 = ImageJHashUtils.digest(new FloatProcessor(size, 1, data));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new FloatProcessor(size, 1, data));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  @SeededTest
  public void canDigestColorProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final int[] data = new int[size];
    for (int i = 0; i < size; i++) {
      data[i] = r.nextInt();
    }

    final String d1 = ImageJHashUtils.digest(new ColorProcessor(size, 1, data));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new ColorProcessor(size, 1, data));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  private void digestStack(final Object pixels, final String expected) {
    final ImageStack stack = new ImageStack(size, 1);
    stack.addSlice(null, pixels);
    final String observed = ImageJHashUtils.digest(stack);
    Assertions.assertEquals(expected, observed, "Stack digest");
  }
}
