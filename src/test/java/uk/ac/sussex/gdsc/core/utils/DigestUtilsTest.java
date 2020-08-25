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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.rng.RadixStringSampler;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class DigestUtilsTest {
  @SeededTest
  void canComputeMD5Hex(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());

    final byte[] testBytes = new byte[50];

    final RadixStringSampler sampler = new RadixStringSampler(r, 50, 16);

    for (int i = 0; i < 10; i++) {
      final String testString = sampler.sample();
      Assertions.assertEquals(org.apache.commons.codec.digest.DigestUtils.md5Hex(testString),
          DigestUtils.md5Hex(testString));
      r.nextBytes(testBytes);
      Assertions.assertEquals(org.apache.commons.codec.digest.DigestUtils.md5Hex(testBytes),
          DigestUtils.md5Hex(testBytes));
      Assertions.assertEquals(
          org.apache.commons.codec.digest.DigestUtils.md5Hex(new ByteArrayInputStream(testBytes)),
          DigestUtils.md5Hex(new ByteArrayInputStream(testBytes)));
    }
  }

  @Test
  void canConvertToHexString() {
    final byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, (byte) 255};
    final String expected = "000102030405060708090a0b0c0d0e0f10ff";
    Assertions.assertEquals(expected, DigestUtils.toHex(data));
    Assertions.assertEquals(expected.toUpperCase(Locale.getDefault()),
        DigestUtils.toHex(data, false));
  }

  @Test
  void getDigestWithBadAlgorithmThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DigestUtils.getDigest("this is nonsense"));
  }
}
