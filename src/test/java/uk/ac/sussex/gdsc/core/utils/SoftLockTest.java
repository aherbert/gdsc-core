package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class SoftLockTest {
  @Test
  public void canSoftLock() {
    SoftLock lock = new SoftLock();
    Assertions.assertFalse(lock.isLocked(), "Initial state");
    Assertions.assertTrue(lock.acquire(), "Aquire when unlocked");
    Assertions.assertTrue(lock.isLocked(), "Locked state");
    Assertions.assertFalse(lock.acquire(), "Aquire when locked");
    Assertions.assertTrue(lock.isLocked(), "Locked state after second aquire");
    lock.release();
    Assertions.assertFalse(lock.isLocked(), "Locked state after release");
  }
}
