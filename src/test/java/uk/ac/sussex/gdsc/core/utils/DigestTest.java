package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

@SuppressWarnings({"javadoc"})
public class DigestTest {
  @SeededTest
  public void canComputeMD5Hex(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngFactory.create(seed.getSeedAsLong());

    final byte[] testBytes = new byte[50];

    for (int i = 0; i < 10; i++) {
      final String testString = nextHexString(r, 50);
      Assertions.assertEquals(DigestUtils.md5Hex(testString), Digest.md5Hex(testString));
      r.nextBytes(testBytes);
      Assertions.assertEquals(DigestUtils.md5Hex(testBytes), Digest.md5Hex(testBytes));
      Assertions.assertEquals(DigestUtils.md5Hex(new ByteArrayInputStream(testBytes)),
          Digest.md5Hex(new ByteArrayInputStream(testBytes)));
    }
  }

  /**
   * Generates a random string of hex characters of length len. <p> Adapted from
   * org.apache.commons.math3.random.RandomDataGenerator.
   *
   * @param ran the random provider
   * @param len the len
   * @return the string
   * @throws NotStrictlyPositiveException the not strictly positive exception
   */
  private static String nextHexString(UniformRandomProvider ran, int len)
      throws NotStrictlyPositiveException {
    if (len <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
    }

    // Initialize output buffer
    final StringBuilder outBuffer = new StringBuilder();

    // Get int(len/2)+1 random bytes
    final byte[] randomBytes = new byte[(len / 2) + 1];
    ran.nextBytes(randomBytes);

    // Convert each byte to 2 hex digits
    for (int i = 0; i < randomBytes.length; i++) {
      final String hex = Integer.toHexString(randomBytes[i] & 0xFF);

      // Make sure we add 2 hex digits for each byte
      if (hex.length() == 1) {
        outBuffer.append('0');
        outBuffer.append(hex.charAt(0));
      } else {
        outBuffer.append(hex);
      }
    }
    return outBuffer.toString().substring(0, len);
  }
}
