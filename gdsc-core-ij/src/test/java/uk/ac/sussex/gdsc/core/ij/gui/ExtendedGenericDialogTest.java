/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import ij.Macro;
import java.awt.Color;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.gui.ExtendedGenericDialog.OptionCollectedEvent;
import uk.ac.sussex.gdsc.test.junit5.DisabledIfHeadless;

@SuppressWarnings({"javadoc"})
class ExtendedGenericDialogTest {

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
  void checkAddColorField() {
    Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
      // Run without showing the dialog.
      // This requires manipulation of the thread name and setting of macro options.
      Thread.currentThread().setName("Run$_ test");
      Macro.setOptions("something");

      ExtendedGenericDialog gd = new ExtendedGenericDialog("Test");
      Color c1 = Color.RED;
      Color c2 = null;
      gd.addColorField("Color_1", c1);
      gd.addColorField("Color_2", c2);
      gd.showDialog();
      Assertions.assertEquals(c1, gd.getNextColor());
      Assertions.assertEquals(c2, gd.getNextColor());

      Color c3 = Color.MAGENTA;
      Color c4 = Color.YELLOW;
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
}
