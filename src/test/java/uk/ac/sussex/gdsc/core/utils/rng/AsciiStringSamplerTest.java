package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.test.utils.functions.IntArrayFormatSupplier;

import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("javadoc")
public class AsciiStringSamplerTest {

  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(AsciiStringSamplerTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void testConstructor() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final AsciiStringSampler s = new AsciiStringSampler(rng);
    Assertions.assertNotNull(s);
  }

  @SuppressWarnings("unused")
  @Test
  public void testConstructorThrows() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new AsciiStringSampler(null);
    });
  }

  @Test
  public void testNextWithZeroLength() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final AsciiStringSampler s = new AsciiStringSampler(rng);
    Assertions.assertEquals("", s.nextAscii(0));
    Assertions.assertEquals("", s.nextUpper(0));
  }

  @Test
  public void testNextWithNegativeLengthThrows() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final AsciiStringSampler s = new AsciiStringSampler(rng);
    Assertions.assertThrows(IllegalArgumentException.class, () -> s.nextAscii(-1));
    Assertions.assertThrows(IllegalArgumentException.class, () -> s.nextUpper(-1));
  }

  private final int digit0 = '0';
  private final int digit9 = '9';
  private final int lettera = 'a';
  private final int letterz = 'z';
  private final int letterA = 'A';
  private final int letterF = 'F';
  private final int letterZ = 'Z';

  private final int[] rangeAtoZ = {letterA, letterZ};
  private final int[] rangeatoz = {lettera, letterz};
  private final int[] rangeAtoF = {letterA, letterF};
  private final int[] range0to9 = {digit0, digit9};
  private final int[] rangeAscii = {32, 126};
  private final int[] rangePrint = {32, 125};
  private final int[] rangeGraph = {33, 125};

  private static boolean isAscii(int ch) {
    return ch >= 32 && ch <= 126;
  }

  private boolean isNumeric(int ch) {
    return ch >= digit0 && ch <= digit9;
  }

  private boolean isLower(int ch) {
    return ch >= lettera && ch <= letterz;
  }

  private boolean isUpper(int ch) {
    return ch >= letterA && ch <= letterZ;
  }

  private boolean isAlphabetic(int ch) {
    return isLower(ch) || isUpper(ch);
  }

  private boolean isAlphanumeric(int ch) {
    return isAlphabetic(ch) || isNumeric(ch);
  }

  private static boolean isPrint(int ch) {
    return ch >= 32 && ch <= 125;
  }

  private static boolean isGraph(int ch) {
    return ch >= 33 && ch <= 125;
  }

  private boolean isHex(int ch) {
    return isNumeric(ch) || ch >= letterA && ch <= letterF;
  }

  private boolean isBase64(int ch) {
    return isAlphanumeric(ch) || ch == '+' || ch == '/';
  }

  private boolean isCharacter(int ch) {
    return isUpper(ch) || isNumeric(ch);
  }

  @Test
  public void testAll() {
    final Level level = Level.INFO;
    Assumptions.assumeTrue(logger.isLoggable(level));
    final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
    final AsciiStringSampler s = new AsciiStringSampler(rng1);
    final int count = 200;
    logger.log(level, () -> "Alphabetic:        " + s.nextAlphabetic(count));
    logger.log(level, () -> "Alphanumeric:      " + s.nextAlphanumeric(count));
    logger.log(level, () -> "Ascii:             " + s.nextAscii(count));
    logger.log(level, () -> "Base64:            " + s.nextBase64(count));
    logger.log(level, () -> "Character:         " + s.nextCharacter(count));
    logger.log(level, () -> "Graph:             " + s.nextGraph(count));
    logger.log(level, () -> "Hex:               " + s.nextHex(count));
    logger.log(level, () -> "Lower:             " + s.nextLower(count));
    logger.log(level, () -> "Numeric:           " + s.nextNumeric(count));
    logger.log(level, () -> "Print:             " + s.nextPrint(count));
    logger.log(level, () -> "Upper:             " + s.nextUpper(count));

    // For reference with unicode
    final RandomStringGenerator rss =
        new RandomStringGenerator.Builder().usingRandom(rng1::nextInt).build();
    logger.log(level, () -> "Unicode:           " + rss.generate(count));
  }

  @Test
  public void testAlphabetic() {
    testSamples((s, l) -> s.nextAlphabetic(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isAlphabetic(c)) {
          return false;
        }
      }
      return true;
    }, rangeAtoZ, rangeatoz);
  }

  @Test
  public void testAlphanumeric() {
    testSamples((s, l) -> s.nextAlphanumeric(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isAlphanumeric(c)) {
          return false;
        }
      }
      return true;
    }, rangeAtoZ, rangeatoz, range0to9);
  }

  @Test
  public void testAscii() {
    testSamples((s, l) -> s.nextAscii(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isAscii(c)) {
          return false;
        }
      }
      return true;
    }, rangeAscii);
  }

  @Test
  public void testBase64() {
    testSamples((s, l) -> s.nextBase64(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isBase64(c)) {
          return false;
        }
      }
      return true;
    }, rangeAtoZ);
  }

  @Test
  public void testCharacter() {
    testSamples((s, l) -> s.nextCharacter(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isCharacter(c)) {
          return false;
        }
      }
      return true;
    }, rangeAtoZ);
  }

  @Test
  public void testGraph() {
    testSamples((s, l) -> s.nextGraph(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isGraph(c)) {
          return false;
        }
      }
      return true;
    }, rangeGraph);
  }

  @Test
  public void testHex() {
    testSamples((s, l) -> s.nextHex(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isHex(c)) {
          return false;
        }
      }
      return true;
    }, range0to9, rangeAtoF);
  }

  @Test
  public void testLower() {
    testSamples((s, l) -> s.nextLower(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isLower(c)) {
          return false;
        }
      }
      return true;
    }, rangeatoz);
  }

  @Test
  public void testNumeric() {
    testSamples((s, l) -> s.nextNumeric(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isNumeric(c)) {
          return false;
        }
      }
      return true;
    }, range0to9);
  }

  @Test
  public void testPrint() {
    testSamples((s, l) -> s.nextPrint(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isPrint(c)) {
          return false;
        }
      }
      return true;
    }, rangePrint);
  }

  @Test
  public void testUpper() {
    testSamples((s, l) -> s.nextUpper(l), (string) -> {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (!isUpper(c)) {
          return false;
        }
      }
      return true;
    }, rangeAtoZ);
  }

  private static void testSamples(BiFunction<AsciiStringSampler, Integer, String> fun,
      Function<String, Boolean> test, int[]... range) {
    final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
    final AsciiStringSampler s = new AsciiStringSampler(rng1);
    // Test short enough strings that the algorithm edge cases are hit
    final int[] lengths = new int[] {1, 2, 3, 4, 5, 10, 1000};
    final int[] h = new int[128]; // Lower half of ASCII histogram table
    for (final int length : lengths) {
      for (int i = 0; i < 10; i++) {
        final String string = fun.apply(s, length);
        Assertions.assertNotNull(string);
        Assertions.assertEquals(length, string.length());
        Assertions.assertTrue(test.apply(string), string);
        for (int j = 0; j < length; j++) {
          h[string.charAt(j)]++;
        }
      }
    }
    // Check the histogram contains samples at all code points
    logger.finer(() -> Arrays.toString(h));
    final IntArrayFormatSupplier msg = new IntArrayFormatSupplier("Missing %c", 1);
    for (final int[] check : range) {
      for (int i = check[0]; i <= check[1]; i++) {
        Assertions.assertTrue(h[i] != 0, msg.set(0, i));
      }
    }
  }
}
