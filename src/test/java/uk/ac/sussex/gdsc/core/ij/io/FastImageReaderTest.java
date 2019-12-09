package uk.ac.sussex.gdsc.core.ij.io;

import ij.io.FileInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings({"javadoc"})
public class FastImageReaderTest {
  @Test
  public void testBadFileTypeThrows() {
    try (ByteArraySeekableStream data = ByteArraySeekableStream.wrap(new byte[10])) {
      final FileInfo fi = new FileInfo();
      fi.fileType = -1;
      final FastImageReader writer = new FastImageReader(fi);
      Assertions.assertThrows(IOException.class, () -> writer.readPixels(data));
    }
  }
}
