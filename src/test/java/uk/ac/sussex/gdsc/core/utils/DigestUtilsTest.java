package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.core.utils.rng.RadixStringSampler;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

@SuppressWarnings({"javadoc"})
public class DigestUtilsTest {
  @SeededTest
  public void canComputeMD5Hex(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());

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
  public void canConvertToHexString() {
    byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, (byte) 255};
    String expected = "000102030405060708090a0b0c0d0e0f10ff";
    Assertions.assertEquals(expected, DigestUtils.toHex(data));
    Assertions.assertEquals(expected.toUpperCase(Locale.getDefault()),
        DigestUtils.toHex(data, false));
  }

  @Test
  public void getDigestWithBadAlgorithmThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> DigestUtils.getDigest("this is nonsense"));
  }
}
