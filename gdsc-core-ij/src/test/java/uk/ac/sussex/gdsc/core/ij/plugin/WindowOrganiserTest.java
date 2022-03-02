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

package uk.ac.sussex.gdsc.core.ij.plugin;

import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.process.ByteProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.DisabledIfHeadless;

@SuppressWarnings({"javadoc"})
class WindowOrganiserTest {
  @Test
  void testProperties() {
    final WindowOrganiser wo = new WindowOrganiser();
    Assertions.assertTrue(wo.isEmpty());

    Assertions.assertFalse(wo.isIgnore());
    wo.setIgnore(true);
    Assertions.assertTrue(wo.isIgnore());
    wo.setIgnore(false);

    Assertions.assertTrue(wo.isUnfreeze());
    wo.setUnfreeze(false);
    Assertions.assertFalse(wo.isUnfreeze());
  }

  @Test
  void testAdd() {
    final WindowOrganiser wo = new WindowOrganiser();
    Assertions.assertTrue(wo.isEmpty());
    Assertions.assertFalse(wo.isNotEmpty());
    final ImagePlus imp = new ImagePlus(null, new ByteProcessor(3, 4));
    wo.setIgnore(true);
    wo.add(42);
    wo.add(imp);
    Assertions.assertTrue(wo.isEmpty());
    Assertions.assertFalse(wo.isNotEmpty());
    wo.setIgnore(false);
    wo.add(-10);
    wo.add((ImagePlus) null);
    wo.add(imp);
    Assertions.assertFalse(wo.isEmpty());
    Assertions.assertTrue(wo.isNotEmpty());
    Assertions.assertEquals(3, wo.size());
    Assertions.assertArrayEquals(new int[] {-10, imp.getID()}, wo.getWindowIds());

    final WindowOrganiser wo2 = new WindowOrganiser();
    wo2.add(-13);
    wo2.add(wo);
    Assertions.assertArrayEquals(new int[] {-13, -10, imp.getID()}, wo2.getWindowIds());

    wo.clear();
    Assertions.assertEquals(0, wo.size());
  }

  @Test
  @DisabledIfHeadless
  void testAdd2() {
    final WindowOrganiser wo = new WindowOrganiser();
    final PlotWindow pw = new Plot("dummy", "x", "y").show();
    wo.setIgnore(true);
    wo.add(pw);
    Assertions.assertTrue(wo.isEmpty());
    Assertions.assertFalse(wo.isNotEmpty());
    wo.setIgnore(false);
    wo.add(pw);
    Assertions.assertFalse(wo.isEmpty());
    Assertions.assertTrue(wo.isNotEmpty());
    Assertions.assertEquals(1, wo.size());
    Assertions.assertArrayEquals(new int[] {pw.getImagePlus().getID()}, wo.getWindowIds());

    final WindowOrganiser wo2 = new WindowOrganiser();
    wo2.add(-13);
    wo2.add(wo);
    Assertions.assertArrayEquals(new int[] {-13, pw.getImagePlus().getID()}, wo2.getWindowIds());

    wo.clear();
    Assertions.assertEquals(0, wo.size());
  }

  @Test
  void testEmpty() {
    final WindowOrganiser wo = new WindowOrganiser();
    wo.tile();
    wo.cascade();
  }

  @Test
  void testTileEmpty() {
    WindowOrganiser.tileWindows(new int[0]);
    WindowOrganiser.tileWindows(new int[0], false);
  }

  @Test
  void testCascadeEmpty() {
    WindowOrganiser.cascadeWindows(new int[0]);
  }
}
