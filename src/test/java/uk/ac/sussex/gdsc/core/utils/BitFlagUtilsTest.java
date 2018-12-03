package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class BitFlagUtilsTest {

  private static final int FLAG1 = 0x01;
  private static final int FLAG2 = 0x02;
  private static final int FLAGS = FLAG1 | FLAG2;

  @Test
  public void canSetAndTestFlags() {
    int flags = 0;
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertFalse(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertTrue(BitFlagUtils.anyNotSet(flags, FLAGS));

    flags = BitFlagUtils.set(flags, FLAG1);
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertTrue(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertTrue(BitFlagUtils.anyNotSet(flags, FLAGS));

    flags = BitFlagUtils.set(flags, FLAG2);
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertTrue(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertFalse(BitFlagUtils.anyNotSet(flags, FLAGS));

    flags = BitFlagUtils.unset(flags, FLAG1);
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertTrue(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertTrue(BitFlagUtils.anyNotSet(flags, FLAGS));
  }
}
