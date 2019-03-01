package uk.ac.sussex.gdsc.core.utils.concurrent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({"javadoc"})
public class ConcurrencyUtilsTest {

  @Test
  public void canRefresh() {
    AtomicReference<Integer> reference = new AtomicReference<>();
    Predicate<Integer> test = v -> v.equals(1);
    Supplier<Integer> supplier = () -> Integer.valueOf(1);

    Integer value = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertEquals(Integer.valueOf(1), value, "Did not generate the value when null");

    Integer refreshedValue = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertSame(value, refreshedValue, "Did not return the same value when test pass");

    // Add a bad value
    reference.set(2);

    value = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertEquals(Integer.valueOf(1), value, "Did not generate the value when test fail");
  }
}
