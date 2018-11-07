package uk.ac.sussex.gdsc.core.ij;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class ImageJUtilsTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(ImageJUtilsTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void cannotIterateOverNullList() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      for (final int i : getIDList()) {
        // This will not run as an exception should be generated
        logger.info("Window ID = " + i);
      }
    });
  }

  private static int[] getIDList() {
    return null;
  }

  @Test
  public void cantIterateOver_getIDList() {
    for (final int i : ImageJUtils.getIdList()) {
      // This will not run as the ID list should be empty
      logger.info("Window ID = " + i);
    }
  }
}
