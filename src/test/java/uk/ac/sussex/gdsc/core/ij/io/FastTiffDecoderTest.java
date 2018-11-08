package uk.ac.sussex.gdsc.core.ij.io;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

@SuppressWarnings({"javadoc"})
public class FastTiffDecoderTest {

  @SeededTest
  public void canGetOrigin(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());

    for (int i = 0; i < 5; i++) {
      int x = rng.nextInt(100);
      int y = rng.nextInt(100);
      int width = rng.nextInt(100);
      int height = rng.nextInt(100);

      ExtendedFileInfo fi = new ExtendedFileInfo();

      // "ROI": [x,y,w,h]
      String text =
          String.format("SummaryMetaData \"ROI\": [%d,%d,%d,%d] asdfasd", x, y, width, height);
      fi.setSummaryMetaData(text);
      checkOrigin(fi, x, y, width, height, text);
      fi.setSummaryMetaData(null);

      // "ROI": "x-y-w-h"
      text = String.format("info \"ROI\": \"%d-%d-%d-%d\" asdfasd", x, y, width, height);
      fi.info = text;
      checkOrigin(fi, x, y, width, height, text);
      fi.info = null;

      // "ROI": "x-y-w-h"
      text =
          String.format("extendedMetaData \"ROI\": \"%d-%d-%d-%d\" asdfasd", x, y, width, height);
      fi.setExtendedMetaData(text);
      checkOrigin(fi, x, y, width, height, text);
    }
  }

  private static void checkOrigin(ExtendedFileInfo fi, int x, int y, int width, int height,
      String text) {
    Rectangle origin = FastTiffDecoder.getOrigin(fi);
    Assertions.assertNotNull(origin, text);
    Assertions.assertEquals(x, origin.x, () -> "X missing: " + text);
    Assertions.assertEquals(y, origin.y, () -> "Y missing: " + text);
    Assertions.assertEquals(width, origin.width, () -> "Width missing: " + text);
    Assertions.assertEquals(height, origin.height, () -> "Height missing: " + text);
  }

  @Test
  public void canGetOriginSkipsBadPatterns() {
    char start = '[';
    char delimiter = ',';
    char end = ']';
    for (String pattern : new String[] {
        //@formatter:off
        null, "",
        "No ROI tag",
        "\"ROI\" without colon after",
        "\"ROI\": with no start character after",
        "\"ROI\": bad chars before start character[",
        "\"ROI\": [ no end character",
        "\"ROI\": []",
        "\"ROI\": [abc]",
        "\"ROI\": [0]",
        "\"ROI\": [0,0]",
        "\"ROI\": [0,0,0]",
        "\"ROI\": [0,0,,0]",
        "\"ROI\": [0,0,0,0,0]",
        //@formatter:on
        }) {
      Assertions.assertNull(FastTiffDecoder.getOrigin(pattern, start, delimiter, end), pattern);
    }
  }
}
