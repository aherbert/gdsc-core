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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import java.awt.Dimension;
import java.awt.Panel;
import java.awt.ScrollPane;
import javax.swing.JScrollPane;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

@SuppressWarnings({"javadoc"})
class ScreenDimensionHelperTest {
  @Test
  void testProperties() {
    final Dimension d = ScreenDimensionHelper.getScreenSize();
    final ScreenDimensionHelper helper = new ScreenDimensionHelper();
    Assertions.assertTrue(d.width > helper.getMaxWidth());
    Assertions.assertTrue(d.height > helper.getMaxHeight());
    // This is exact but may be changed in the future
    Assertions.assertEquals(d.width - 100, helper.getMaxWidth());
    Assertions.assertEquals(d.height - 150, helper.getMaxHeight());

    Assertions.assertEquals(0, helper.getMinWidth());
    Assertions.assertEquals(0, helper.getMinHeight());

    // Test updates
    helper.setMinSize(13, 14);
    Assertions.assertEquals(13, helper.getMinWidth());
    Assertions.assertEquals(14, helper.getMinHeight());
    helper.setMaxSize(33, 34);
    Assertions.assertEquals(33, helper.getMaxWidth());
    Assertions.assertEquals(34, helper.getMaxHeight());
  }

  @Test
  void testClipDimensions() {
    final ScreenDimensionHelper helper = new ScreenDimensionHelper();
    helper.setMinSize(0, 0);
    helper.setMaxSize(0, 0);
    final Dimension d = new Dimension(50, 100);
    assertClipDimensions(helper, d, 50, 100);
    helper.setMinSize(70, 100);
    assertClipDimensions(helper, d, 70, 100);
    helper.setMinSize(0, 200);
    assertClipDimensions(helper, d, 50, 200);
    helper.setMinSize(0, 0);
    helper.setMaxSize(30, 100);
    assertClipDimensions(helper, d, 30, 100);
    helper.setMaxSize(0, 90);
    assertClipDimensions(helper, d, 50, 90);
  }

  private static void assertClipDimensions(ScreenDimensionHelper helper, Dimension d, int width,
      int height) {
    final Dimension test = new Dimension(d);
    helper.clipDimensions(test);
    Assertions.assertEquals(width, test.width);
    Assertions.assertEquals(height, test.height);
  }

  @Test
  @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
  void testSetupScrollPane() {
    final ScrollPane pane = new ScrollPane();
    final ScreenDimensionHelper helper = new ScreenDimensionHelper();
    helper.setMaxSize(500, 600);
    helper.setup(pane, new Dimension(1000, 1000));
    final Dimension d = pane.getPreferredSize();
    Assertions.assertEquals(500, d.width, 50);
    Assertions.assertEquals(600, d.height, 50);
  }

  @Test
  @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
  void testSetupJScrollPane() {
    final Panel p = new Panel();
    p.setSize(1000, 1000);
    p.setPreferredSize(new Dimension(1000, 1000));
    final JScrollPane pane = new JScrollPane(p);
    final ScreenDimensionHelper helper = new ScreenDimensionHelper();
    helper.setMaxSize(500, 600);
    helper.setup(pane);
    final Dimension d = pane.getViewport().getPreferredSize();
    Assertions.assertEquals(500, d.width, 50);
    Assertions.assertEquals(600, d.height, 50);
  }
}
