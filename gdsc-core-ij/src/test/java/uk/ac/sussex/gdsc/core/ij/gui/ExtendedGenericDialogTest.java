/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
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

package uk.ac.sussex.gdsc.core.ij.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.gui.ExtendedGenericDialog.OptionCollectedEvent;
import uk.ac.sussex.gdsc.test.junit5.DisabledIfHeadless;

@SuppressWarnings({"javadoc"})
class ExtendedGenericDialogTest {

  enum TestEnum {
    A, B, C, D
  }

  @Test
  @DisabledIfHeadless
  void checkNotifyOptionCollectedListeners() {
    final ExtendedGenericDialog gd = new ExtendedGenericDialog("Test");
    final OptionCollectedEvent event = new OptionCollectedEvent("field_name");
    gd.notifyOptionCollectedListeners(event);
    final int[] count = {0};
    gd.addOptionCollectedListener(e -> {
      count[0]++;
      Assertions.assertSame(e, e);
    });
    gd.addOptionCollectedListener(e -> {
      count[0]++;
      Assertions.assertSame(e, e);
    });
    gd.notifyOptionCollectedListeners(event);
    Assertions.assertEquals(2, count[0]);
    gd.notifyOptionCollectedListeners(null);
    Assertions.assertEquals(2, count[0]);
  }

  @Test
  @DisabledIfHeadless
  void checkAddColorField() {
    Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
      // Run without showing the dialog.
      // This requires manipulation of the thread name and setting of macro options.
      Thread.currentThread().setName("Run$_ test");
      Macro.setOptions("something");

      ExtendedGenericDialog gd = new ExtendedGenericDialog("Test");
      final Color c1 = Color.RED;
      final Color c2 = null;
      gd.addColorField("Color_1", c1);
      gd.addColorField("Color_2", c2);
      gd.showDialog();
      Assertions.assertEquals(c1, gd.getNextColor());
      Assertions.assertEquals(c2, gd.getNextColor());

      final Color c3 = Color.MAGENTA;
      final Color c4 = Color.YELLOW;
      Macro.setOptions(String.format("color_1=#%6x color_2=#%6x", c3.getRGB() & 0xffffff,
          c4.getRGB() & 0xffffff));

      gd = new ExtendedGenericDialog("Test");
      gd.addColorField("Color_1", c1);
      gd.addColorField("Color_2", c2);
      gd.showDialog();
      Assertions.assertEquals(c3, gd.getNextColor());
      Assertions.assertEquals(c4, gd.getNextColor());
    });
  }

  @Test
  void checkParseHexLong() {
    Assertions.assertEquals(0L, ExtendedGenericDialog.parseHexLong(""));
    Assertions.assertEquals(0L, ExtendedGenericDialog.parseHexLong("0"));
    Assertions.assertEquals(0L, ExtendedGenericDialog.parseHexLong("wrong"));
    Assertions.assertEquals(-1L, ExtendedGenericDialog.parseHexLong("ffffffffffffffff"));
    Assertions.assertEquals(-1L, ExtendedGenericDialog.parseHexLong("ffffffffffffffffff"));
    Assertions.assertEquals(-16L, ExtendedGenericDialog.parseHexLong("ffffffffffffffff0"));
  }

  @Test
  @DisabledIfHeadless
  void checkAddHexField() {
    Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
      // Run without showing the dialog.
      // This requires manipulation of the thread name and setting of macro options.
      Thread.currentThread().setName("Run$_ test");
      Macro.setOptions("something");

      final ExtendedGenericDialog gd = new ExtendedGenericDialog("Test");
      final long l1 = 26378462834L;
      final long l2 = 13L;
      final byte[] b1 = {1, 3, -42};
      final byte[] b2 = {};
      gd.addHexField("Hex_1", l1);
      gd.addHexField("Hex_2", l2);
      gd.addHexField("Hex_3", b1);
      gd.addHexField("Hex_4", b2);
      gd.showDialog();
      Assertions.assertEquals(l1, gd.getNextHexLong());
      Assertions.assertEquals(l2, gd.getNextHexLong());
      Assertions.assertArrayEquals(b1, gd.getNextHexBytes());
      Assertions.assertArrayEquals(b2, gd.getNextHexBytes());
    });
  }

  /**
   * Check {@link GenericDialog#addImageChoice(String, String)} is supported without any overrides.
   */
  @Test
  @DisabledIfHeadless
  void checkAddImageChoice() {
    Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
      // Run without showing the dialog.
      // This requires manipulation of the thread name and setting of macro options.
      Thread.currentThread().setName("Run$_ test");
      Macro.setOptions("something");

      final ExtendedGenericDialog gd = new ExtendedGenericDialog("Test");
      final String name1 = "test1";
      final ImagePlus imp1 = IJ.createImage(name1, 3, 4, 1, 8);
      imp1.show();
      gd.addImageChoice("image1", name1);
      gd.showDialog();
      Assertions.assertEquals(imp1, gd.getNextImage());
      imp1.close();
    });
  }

  /**
   * Check {@link GenericDialog#addEnumChoice(String, Enum)} is supported without any overrides.
   */
  @Test
  @DisabledIfHeadless
  void checkAddEnumChoice() {
    Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
      // Run without showing the dialog.
      // This requires manipulation of the thread name and setting of macro options.
      Thread.currentThread().setName("Run$_ test");
      Macro.setOptions("something");

      final ExtendedGenericDialog gd = new ExtendedGenericDialog("Test");
      final TestEnum ll1 = TestEnum.B;
      final TestEnum ll2 = TestEnum.C;
      gd.addEnumChoice("enum1", ll1);
      gd.addEnumChoice("enum2", new TestEnum[] {TestEnum.A, ll2}, ll2);
      gd.showDialog();
      Assertions.assertEquals(ll1, gd.getNextEnumChoice(TestEnum.class));
      Assertions.assertEquals(ll2, gd.getNextEnumChoice(ll2.getDeclaringClass()));
    });
  }
}
