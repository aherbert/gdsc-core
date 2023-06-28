/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
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

package uk.ac.sussex.gdsc.core.ij;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.DigestUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class ImageJDigestTest {
  int size = 50;

  @Test
  void testBadPixelsThrows() {
    final ImageJDigest digest = new ImageJDigest();
    Assertions.assertThrows(IllegalArgumentException.class, () -> digest.getPixelsDigester(null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> digest.getPixelsDigester(new Object()));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> digest.getPixelsDigester(new double[10]));
  }

  @SeededTest
  void canDigestByteProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final byte[] data = new byte[size];
    r.nextBytes(data);

    final String o = new ImageJDigest().digest(new ByteProcessor(size, 1, data));
    final String e = DigestUtils.md5Hex(data);
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  void canDigestShortProcessor(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final short[] data = new short[size];
    for (int i = 0; i < size; i++) {
      data[i] = (short) ((r.nextDouble() - 0.5) * 2 * Short.MAX_VALUE);
    }

    final String o = new ImageJDigest().digest(new ShortProcessor(size, 1, data, null));
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
    final DataOutputStream out = new DataOutputStream(bos);
    for (int i = 0; i < size; i++) {
      out.writeShort(data[i]);
    }
    final String e = DigestUtils.md5Hex(bos.toByteArray());
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  void canDigestFloatProcessor(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final float[] data = new float[size];
    for (int i = 0; i < size; i++) {
      data[i] = (r.nextFloat() - 0.5f) * 2f;
    }

    final String o = new ImageJDigest().digest(new FloatProcessor(size, 1, data, null));
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
    final DataOutputStream out = new DataOutputStream(bos);
    for (int i = 0; i < size; i++) {
      out.writeFloat(data[i]);
    }
    final String e = DigestUtils.md5Hex(bos.toByteArray());
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  void canDigestColorProcessor(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int[] data = new int[size];
    for (int i = 0; i < size; i++) {
      data[i] = r.nextInt();
    }

    final String o = new ImageJDigest().digest(new ColorProcessor(size, 1, data));
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
    final DataOutputStream out = new DataOutputStream(bos);
    for (int i = 0; i < size; i++) {
      out.writeInt(data[i]);
    }
    final String e = DigestUtils.md5Hex(bos.toByteArray());
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  void canDigestStack(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final byte[] data1 = new byte[size];
    final byte[] data2 = new byte[size];
    r.nextBytes(data1);
    r.nextBytes(data2);

    final ImageStack stack = new ImageStack(size, 1, 2);
    stack.setPixels(data1, 1);
    stack.setPixels(data2, 2);
    final String o = new ImageJDigest().digest(stack);
    final byte[] allData = Arrays.copyOf(data1, size * 2);
    System.arraycopy(data2, 0, allData, size, size);
    final String e = DigestUtils.md5Hex(allData);
    Assertions.assertEquals(e, o);
  }
}
