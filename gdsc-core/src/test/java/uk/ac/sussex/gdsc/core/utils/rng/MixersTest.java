/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
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

package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class MixersTest {
  @SeededTest
  void testXor(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int i = 0; i < 500; i++) {
      final int x = rng.nextInt();
      final int y = rng.nextInt();
      final int z = x ^ y;
      Assertions.assertEquals(x, y ^ z);
      Assertions.assertEquals(y, x ^ z);
    }
  }

  @SeededTest
  void testReverseXorRightShift(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int shift = 1; shift < 64; shift++) {
      final int fshift = shift;
      for (int i = 0; i < 500; i++) {
        final long x = rng.nextLong();
        final long y = x ^ (x >>> shift);
        final long x2 = Mixers.reverseXorRightShift(y, shift);
        Assertions.assertEquals(x, x2, () -> "shift = " + fshift);
      }
    }
  }

  @SeededTest
  void testReverseXorLeftShift(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int shift = 1; shift < 64; shift++) {
      final int fshift = shift;
      for (int i = 0; i < 500; i++) {
        final long x = rng.nextLong();
        final long y = x ^ (x << shift);
        final long x2 = Mixers.reverseXorLeftShift(y, shift);
        Assertions.assertEquals(x, x2, () -> "shift = " + fshift);
      }
    }
  }

  @SeededTest
  void testRxsmxs(RandomSeed seed) {
    assertUnmixer(seed, Mixers::rxsmxs, Mixers::rxsmxsUnmix);
  }

  @SeededTest
  void testRrmxmx(RandomSeed seed) {
    assertUnmixer(seed, Mixers::rrmxmx, Mixers::rrmxmxUnmix);
  }

  private static void assertUnmixer(RandomSeed seed, LongUnaryOperator mix,
      LongUnaryOperator unmix) {
    for (final long x : new long[] {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE}) {
      final long y = mix.applyAsLong(x);
      final long x2 = unmix.applyAsLong(y);
      Assertions.assertEquals(x, x2);
    }
    for (int i = 0; i < 64; i++) {
      final long x = 1L << i;
      final long y = mix.applyAsLong(x);
      final long x2 = unmix.applyAsLong(y);
      Assertions.assertEquals(x, x2);
    }
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int i = 0; i < 500; i++) {
      final long x = rng.nextLong();
      final long y = mix.applyAsLong(x);
      final long x2 = unmix.applyAsLong(y);
      Assertions.assertEquals(x, x2);
    }
  }

  @Test
  void testRxsmxsOutput() {
    // Code generated using the reference c code obtained from:
    // https://mostlymangling.blogspot.com/2018/07/on-mixing-functions-in-fast-splittable.html
    final long[] values =
        {0x4909bf4228b09f5dL, 0x62f1175aa2ac2becL, 0x0fff75f3a0f6eaa1L, 0x440055bc9b89eaf0L,
            0x8d3954796546094bL, 0x541dc47bccef0e39L, 0xbcf2cf7ed5e3db25L, 0x3902cbf791fbac96L,
            0x144101ff31d0bccdL, 0x5da6aec2faa5adceL, 0xbfff757b69be2784L, 0x6b3e67846edd0fd5L,
            0x68192de4e987bdc4L, 0xb3fe34cee77a79a8L, 0xdea80e3b85df836dL, 0xd6276bdcf3d6d342L,
            0x0b6cba29f4d2ad13L, 0xb6bfe1d2d013aa12L, 0xf08e347e079ba78cL, 0x0150ac7471e862fcL,
            0x16457cc24205be12L, 0x1b3c87d7fc416c26L, 0xa7d1a4e56bbf95a9L, 0x44d605b26c0bbdaaL,
            0xfe6167e96c66a310L, 0x09ecd862a1ef544bL, 0x8d83e2956e0da35dL, 0x268734a1a4d94cfdL,
            0x104b3ca0ce4b772bL, 0x488689436f671ba8L, 0x3ae5b9b467cc827aL, 0xac0e0ba50f83e589L,
            0xfd8d4041fb3350c6L, 0x2aebfa9bf4afe825L, 0xf7f840be98599b68L, 0x2c65c37d25d3f0ccL,
            0xbf49fcbcef6acb81L, 0x0552891572d190a4L, 0x8292c9877a7a73dbL, 0x6377a3a02614593eL};
    assertMixer(Mixers::rrmxmx, values, 0x012de1babb3c4104L, 0xc8161b4202294965L);
  }

  @Test
  void testRrxmrrxmsx0Output() {
    // Code generated using the reference c code obtained from:
    // http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html
    final long[] values =
        {0x9ccbd09c5dc10ca7L, 0x3c8947159899882bL, 0xd893601ad724cbe5L, 0x72f26c4df3673ee6L,
            0xfaf48ced65d211cfL, 0x8a44f467531be62aL, 0x09e0c1313a1f5d0aL, 0xf6187ebbdc0757f5L,
            0xcb46619de4f97746L, 0x22144f07f748af25L, 0x80b16f5e7b47ea31L, 0x32b8afbb80754fc5L,
            0xa3b5c4410d509a03L, 0xd168c5f50cce6993L, 0x0f82c25b1fe2b83cL, 0xaa9939e780d46a94L,
            0xb243a1b70ccfd29fL, 0x8339e40046415384L, 0x330356935c8bd63eL, 0x409d8934b393c9ffL,
            0x79e4e7213ca3b57eL, 0xb41093551559a3bcL, 0x6617421a35827962L, 0x07e578716190d472L,
            0x658d3b2eac566bdcL, 0xf1d12ab9abe02ac8L, 0xd342993d81c80796L, 0x7db078f0750ff480L,
            0xb293aa0fe53fe1a3L, 0x6e48ce5dbf834da5L, 0x4c53364beb947791L, 0xc6dbd1f80ce8ff08L,
            0x35d26dadbb5a7380L, 0xa672343acf8264c8L, 0xac015d5d9bef0924L, 0x39b36602bbd90446L,
            0x759517ff4a1ef0fbL, 0x0ef3fdcdbf413a46L, 0x991d24c8b6c9f04fL, 0x7640f103903507ecL};
    assertMixer(Mixers::rrxmrrxmsx0, values, 0x012de1babb3c4104L, 0xc8161b4202294965L);
  }

  @Test
  void testMurmur3IntOutput() {
    // Code generated using the original c code:
    // https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp
    final int[] values = {0x44d89774, 0xe7488b4e, 0x6c1465b4, 0xef03ae52, 0x5b2fd1f4, 0x184498c9,
        0x9a667364, 0x087ae15c, 0xcf91862a, 0x4bd74e5e, 0x709fbc40, 0x57d623de, 0x32a51e70,
        0x35a5495b, 0xc2648d76, 0xcca068c5, 0xcf3774e5, 0xedc88ea5, 0x37d08241, 0x7470b7b8,
        0xab52458a, 0x2d25df71, 0xa3349e19, 0x083a64a6, 0xa23389d2, 0xf4d447c9, 0xc9514d1e,
        0xe92325c8, 0x8d7a890b, 0x135d4731, 0x1a3b17b6, 0x958f6b3b, 0xc054db4b, 0xbe6ecbca,
        0x4530fdb7, 0xcaa56899, 0xe1c959d6, 0xe67d6e9a, 0xb7f13547, 0xa5e9f292};
    assertMixer(Mixers::murmur3, values, 0x012de1ba, 0xc8161b42);
  }

  @Test
  void testMurmur3LongOutput() {
    // Code generated using the reference c code provided by David Stafford:
    // http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html
    final long[] values =
        {0xdb3e232c1003b576L, 0x4da53e5b7c84173eL, 0xe2c498d0956e2a67L, 0x3a1a865a2c08ea34L,
            0xb297e624901fea79L, 0x241a17faf3ee04bdL, 0x32b88f7c345c3c8aL, 0xbed326c79cde3d54L,
            0x1173b9a40603f93fL, 0xc690ca776314d65cL, 0xe3753c2a2602094eL, 0x40d36f700f646e45L,
            0xf03851d528a213e2L, 0x53acf12d51b641b4L, 0x91f382b63e0faee1L, 0x659dda2c787f7c9aL,
            0xeddedb24f7d1f43eL, 0xe8a2dc137d7a84aeL, 0xc8cfa41cc2af0a4fL, 0xd2337b326e5c306eL,
            0x88f0cf730266cf9bL, 0xdce208c02c2aaa04L, 0xbf2e5961057a35f8L, 0x5e136f9d03f7ccb0L,
            0x6b64306b15c5fc39L, 0x04c6252c23bf5144L, 0x854ab124a4663d80L, 0xdb3746bcfc35ebceL,
            0xb1f3fcc933315abbL, 0xc2cb15b0127ff726L, 0x6a5ebc92086ac3f2L, 0x9dfe71c717ac71aaL,
            0x4d7856a84b1d4ee4L, 0x9fc002527767f050L, 0xc784f29e480b2aa2L, 0x1400dc5cbb74621eL,
            0x63afda06ad81baeeL, 0x93ac5025caf335e4L, 0x988cbf34e715eec9L, 0x14318afff8984560L};
    assertMixer(Mixers::murmur3, values, 0x012de1babb3c4104L, 0xc8161b4202294965L);
  }

  @Test
  void testStafford1Output() {
    // Code generated using the reference c code provided by David Stafford:
    // http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html
    final long[] values =
        {0xe00854e38d780911L, 0xbb0ac46041e42918L, 0x10e69689ca4a9a11L, 0x17c6893b3cd5037fL,
            0x52c12c13aea13d04L, 0x5c7eda3b1354483cL, 0x3da00e6700b4bd8dL, 0x989c64f349b85574L,
            0xe27908f80f5754dbL, 0xd89e3d09f9bdfc73L, 0xec0859bf6f61c638L, 0x0a248688fedb698eL,
            0x898dc39bd789089aL, 0x53f1dda084300687L, 0xccf410496922c3e9L, 0xc6bb4639b2759264L,
            0x058cb1a89134d055L, 0xd85d9b39de7669beL, 0xbe4804c7ada87e09L, 0xc05408fb8d0a4b8cL,
            0x4864108702020a36L, 0x6bc93bbbf6f1ef7dL, 0x74a538e6d9a238f7L, 0xb9204a971ef323fbL,
            0xd1e4d8e08809efe8L, 0x669dc2e2c05fc928L, 0xdc5d71277d80e453L, 0x301282108a494599L,
            0x11bb69f547d202e3L, 0xe4f5e136de914632L, 0xae25ab4c28031282L, 0xc1bbfc67c9ed36c1L,
            0x37ef297a41970b69L, 0x2cf273e9261aa2e2L, 0xe50ec4c9e9c46763L, 0x3dae4f49c7b9803aL,
            0x527d10e48f1804e2L, 0x4ddd2787e6088b0bL, 0x3c80ef127e4d4e8eL, 0xa5c012fe3273f5b5L};
    assertMixer(Mixers::stafford1, values, 0x012de1babb3c4104L, 0xc8161b4202294965L);
  }

  @Test
  void testStafford13Output() {
    // Code generated using the reference c code provided by David Stafford:
    // http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html
    final long[] values =
        {0xb6e5bb4394c07618L, 0xc22a2f0fedefdbc5L, 0xc8c692032976fb0dL, 0x5eeafdee96357649L,
            0xac07551c38d612edL, 0x4c02325af6a2d1c6L, 0x419a0e82ffcb9bacL, 0x0429b1de8b800604L,
            0x087870444a51ef1dL, 0x909c0bbbb26527d6L, 0x6127dc6b5184f343L, 0x8e1a3ce1a932ff2cL,
            0x6111ce001b3d0c3cL, 0xcd6de6af46e8bb99L, 0xc9456a240b1475a7L, 0x4d3d1bfc44594264L,
            0xe3030ad6a2ede854L, 0xfccb1eea6d681fa9L, 0x9ac3b08ef7a65e65L, 0x14548ff7c51b277dL,
            0x80638702bc9817abL, 0xaf44c7426c48f1a5L, 0xe588effe5e90d5bdL, 0xf9debeb061581d31L,
            0xe5783c539c916096L, 0xe70372dfeabf1da8L, 0x049ccefce30b5e55L, 0xb2fdd15229d06a64L,
            0xe15967acded0e841L, 0x3ab056912a9c7f44L, 0x800f750d6f92ac6bL, 0x828beb2c6b65fa10L,
            0x2c7ebbc82cdee193L, 0x5f5bd29b1fb5b4e2L, 0xba69ecf78275b12eL, 0x82aea2bebbd0caa5L,
            0xf5ebd97835f7d5abL, 0x3513e0fc1af2b448L, 0x151b8e21cbf81789L, 0xdb17e9cfacf6b51eL};
    assertMixer(Mixers::stafford13, values, 0x012de1babb3c4104L, 0xc8161b4202294965L);
  }

  @Test
  void testLea64Output() {
    // Code generated using the reference java code provided by Steele and Vigna:
    // https://doi.org/10.1145/3485525
    final long[] values =
        {0x45b8512f9ff46f10L, 0xd6ce3db0dd63efc3L, 0x47bf6058710f2a88L, 0x85b8c74e40981596L,
            0xd77442e45944235eL, 0x3ea4255636bfb1c3L, 0x296ec3c9d3e0addcL, 0x6c285eb9694f6eb2L,
            0x8121aeca2ba15b66L, 0x2b6d5c2848c4fdc4L, 0xcc99bc57f5e3e024L, 0xc00f59a3ad3666cbL,
            0x74e5285467c20ae7L, 0xf4d51701e3ea9555L, 0x3aeb92e31a9b1a0eL, 0x5a1a0ce875c7dcaL,
            0xb9a561fb7d82d0f3L, 0x97095f0ab633bf2fL, 0xfe74b5290c07c1d1L, 0x9dfd354727d45838L,
            0xf6279a8801201eddL, 0x2db471b1d42860eeL, 0x4ee66ceb27bd34ecL, 0x2005875ad25bd11aL,
            0x92eac4d1446a0204L, 0xa46087d5dd5fa38eL, 0x7967530c43faabe1L, 0xc53e1dd74fd9bd15L,
            0x259001ab97cca8bcL, 0x5edf024ee6cb1d8bL, 0x3fc021bba7d0d7e6L, 0xf82cae56e00245dbL,
            0xf1dc30974b524d02L, 0xe1f2f1db0af7ace9L, 0x853d5892ebccb9f6L, 0xe266f36a3121da55L,
            0x3b034a81bad01622L, 0x852b53c14569ada2L, 0xee902ddc658c86c9L, 0xd9e926b766013254L};
    assertMixer(Mixers::lea64, values, 0x012de1babb3c4104L, 0xc8161b4202294965L);
  }

  private static void assertMixer(IntUnaryOperator mix, int[] expected, int state, int increment) {
    for (int i = 0; i < expected.length; i++) {
      Assertions.assertEquals(expected[i], mix.applyAsInt(state += increment));
    }
  }

  private static void assertMixer(LongUnaryOperator mix, long[] expected, long state,
      long increment) {
    for (int i = 0; i < expected.length; i++) {
      Assertions.assertEquals(expected[i], mix.applyAsLong(state += increment));
    }
  }

  /**
   * This is an example test to demonstrate that the MurmurHash3-style mix functions are bijections
   * that can be reversed.
   */
  @Test
  void canUnmixStafford13() {
    final long u1 = NumberUtils.computeInverse(0xbf58476d1ce4e5b9L);
    final long u2 = NumberUtils.computeInverse(0x94d049bb133111ebL);
    System.out.println(u1);
    System.out.println(u2);
    final UniformRandomProvider rng = RngFactory.createWithFixedSeed();
    for (int i = 0; i < 200; i++) {
      final long x = rng.nextLong();
      final long y = Mixers.stafford13(x);
      long z = Mixers.reverseXorRightShift(y, 31);
      z *= u2;
      z = Mixers.reverseXorRightShift(z, 27);
      z *= u1;
      z = Mixers.reverseXorRightShift(z, 30);
      Assertions.assertEquals(x, z);
      Assertions.assertEquals(x, unmixStafford13(y));
    }
  }

  private static long unmixStafford13(long x) {
    // Hard-coded inverse constants
    final long u1 = -7575587736534282103L;
    final long u2 = 3573116690164977347L;
    x = x ^ (x >>> 31);
    x ^= (x >>> 62);
    x *= u2;
    x = x ^ (x >>> 27);
    x ^= (x >>> 54);
    x *= u1;
    x = x ^ (x >>> 30);
    x ^= (x >>> 60);
    return x;
  }
}
