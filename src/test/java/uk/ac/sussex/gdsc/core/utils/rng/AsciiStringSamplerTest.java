package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.functions.IntArrayFormatSupplier;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;

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
        final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
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

    private final int digit0 = '0';
    private final int digit9 = '9';
    private final int lettera = 'a';
    private final int letterf = 'f';
    private final int letterz = 'z';
    private final int letterA = 'A';
    private final int letterZ = 'Z';

    private final int[] rangeAZ = { letterA, letterZ };
    private final int[] rangeaz = { lettera, letterz };
    private final int[] rangeaf = { lettera, letterf };
    private final int[] range09 = { digit0, digit9 };
    private final int[] rangeAscii = { 32, 126 };
    private final int[] rangePrint = { 32, 125 };
    private final int[] rangeGraph = { 33, 125 };

    private static boolean isAscii(int c) {
        return c >= 32 && c <= 126;
    }

    private boolean isNumeric(int c) {
        return c >= digit0 && c <= digit9;
    }

    private boolean isLower(int c) {
        return c >= lettera && c <= letterz;
    }

    private boolean isUpper(int c) {
        return c >= letterA && c <= letterZ;
    }

    private boolean isAlphabetic(int c) {
        return isLower(c) || isUpper(c);
    }

    private boolean isAlphanumeric(int c) {
        return isAlphabetic(c) || isNumeric(c);
    }

    private boolean isLowerAlphanumeric(int c) {
        return isLower(c) || isNumeric(c);
    }

    private static boolean isPrint(int c) {
        return c >= 32 && c <= 125;
    }

    private static boolean isGraph(int c) {
        return c >= 33 && c <= 125;
    }

    private boolean isHex(int c) {
        return isNumeric(c) || c >= lettera && c <= letterf;
    }

    @Test
    public void testAll() {
        final Level level = Level.FINE;
        ExtraAssumptions.assume(logger, level);
        final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
        final AsciiStringSampler s = new AsciiStringSampler(rng1);
        final int count = 200;
        logger.log(level, s.nextAlphabetic(count));
        logger.log(level, s.nextAlphanumeric(count));
        logger.log(level, s.nextAscii(count));
        logger.log(level, s.nextGraph(count));
        logger.log(level, s.nextHex(count));
        logger.log(level, s.nextLower(count));
        logger.log(level, s.nextLowerAlphanumeric(count));
        logger.log(level, s.nextNumeric(count));
        logger.log(level, s.nextPrint(count));
        logger.log(level, s.nextUpper(count));
    }

    @Test
    public void testAlphabetic() {
        testSamples((s, l) -> s.nextAlphabetic(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isAlphabetic(c))
                    return false;
            }
            return true;
        }, rangeAZ, rangeaz);
    }

    @Test
    public void testAlphanumeric() {
        testSamples((s, l) -> s.nextAlphanumeric(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isAlphanumeric(c))
                    return false;
            }
            return true;
        }, rangeAZ, rangeaz, range09);
    }

    @Test
    public void testAscii() {
        testSamples((s, l) -> s.nextAscii(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isAscii(c))
                    return false;
            }
            return true;
        }, rangeAscii);
    }

    @Test
    public void testGraph() {
        testSamples((s, l) -> s.nextGraph(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isGraph(c))
                    return false;
            }
            return true;
        }, rangeGraph);
    }

    @Test
    public void testHex() {
        testSamples((s, l) -> s.nextHex(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isHex(c))
                    return false;
            }
            return true;
        }, range09, rangeaf);
    }

    @Test
    public void testLower() {
        testSamples((s, l) -> s.nextLower(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isLower(c))
                    return false;
            }
            return true;
        }, rangeaz);
    }

    @Test
    public void testLowerAlphanumeric() {
        testSamples((s, l) -> s.nextLowerAlphanumeric(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isLowerAlphanumeric(c))
                    return false;
            }
            return true;
        }, rangeaz, range09);
    }

    @Test
    public void testNumeric() {
        testSamples((s, l) -> s.nextNumeric(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isNumeric(c))
                    return false;
            }
            return true;
        }, range09);
    }

    @Test
    public void testPrint() {
        testSamples((s, l) -> s.nextPrint(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isPrint(c))
                    return false;
            }
            return true;
        }, rangePrint);
    }

    @Test
    public void testUpper() {
        testSamples((s, l) -> s.nextUpper(l), (string) -> {
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isUpper(c))
                    return false;
            }
            return true;
        }, rangeAZ);
    }

    private static void testSamples(BiFunction<AsciiStringSampler, Integer, String> fun, Function<String, Boolean> test,
            int[]... range) {
        final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
        final AsciiStringSampler s = new AsciiStringSampler(rng1);
        // Test short enough strings that the algorithm edge cases are hit
        final int[] lengths = new int[] { 1, 2, 3, 4, 5, 10, 1000 };
        final int[] h = new int[128]; // Lower half of ASCII histogram table
        for (final int length : lengths) {
            for (int i = 0; i < 10; i++) {
                final String string = fun.apply(s, length);
                Assertions.assertNotNull(string);
                Assertions.assertEquals(length, string.length());
                Assertions.assertTrue(test.apply(string), string);
                for (int j = 0; j < length; j++)
                    h[string.charAt(j)]++;
            }
        }
        // Check the histogram contains samples at all code points
        logger.finer(() -> Arrays.toString(h));
        final IntArrayFormatSupplier msg = new IntArrayFormatSupplier("Missing %c", 1);
        for (int[] check : range) {
            for (int i = check[0]; i <= check[1]; i++) {
                Assertions.assertTrue(h[i] != 0, msg.set(0, i));
            }
        }
    }
}
