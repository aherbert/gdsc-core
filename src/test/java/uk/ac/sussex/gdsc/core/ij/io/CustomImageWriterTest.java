package uk.ac.sussex.gdsc.core.ij.io;

import ij.io.FileInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SuppressWarnings({"javadoc"})
public class CustomImageWriterTest {
  @Test
  public void testBadPixelsThrows() {
    final FileInfo fi = new FileInfo();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    final CustomImageWriter writer = new CustomImageWriter(fi);
    fi.pixels = null;
    fi.virtualStack = null;
    Assertions.assertThrows(IOException.class, () -> writer.write(data), "Null pixels");

    fi.pixels = new byte[1];
    fi.nImages = 2;
    Assertions.assertThrows(IOException.class, () -> writer.write(data), "Not a pixel array stack");
  }

  @Test
  public void testBadFileTypeThrows() {
    final FileInfo fi = new FileInfo();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    final CustomImageWriter writer = new CustomImageWriter(fi);
    fi.pixels = new byte[1];
    fi.fileType = FileInfo.RGB48_PLANAR;
    Assertions.assertThrows(IOException.class, () -> writer.write(data));
  }
}
