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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.gui.ExtendedGenericDialog.OptionCollectedEvent;

@SuppressWarnings({"javadoc"})
class ExtendedGenericDialogTest {

  @Test
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
}
