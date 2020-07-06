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

package uk.ac.sussex.gdsc.core.clustering.optics;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.ConvexHull;

@SuppressWarnings({"javadoc"})
public class ScratchSpaceTest {
  @Test
  public void testAdd() {
    final ScratchSpace ss = new ScratchSpace(4);
    ss.add(10, 100);
    ss.clear();
    ss.add(0, 0);
    ss.add(0, 2);
    ss.add(2, 2);
    ss.add(2, 0);
    final Rectangle2D bounds = ss.getBounds();
    Assertions.assertEquals(new Rectangle(0, 0, 2, 2), bounds);
  }

  @Test
  public void testSafeAdd() {
    final ScratchSpace ss = new ScratchSpace(1);
    ss.resize(2);
    ss.resize(2);
    ss.safeAdd(0, 0);
    ss.safeAdd(0, 2);
    ss.safeAdd(2, 2);
    ss.safeAdd(2, 0);
    final Rectangle2D bounds = ss.getBounds();
    Assertions.assertEquals(new Rectangle(0, 0, 2, 2), bounds);
  }

  @Test
  public void testArrayAdd() {
    final ScratchSpace ss = new ScratchSpace(2);
    ss.add(10, 100);
    ss.resize(10);
    final float[] x = {0, 1, 3, 5};
    final float[] y = {1, 4, 2, 4};
    ss.add(x, y);
    final ConvexHull ch1 = ConvexHull.create(x, y);
    final ConvexHull ch2 = ss.getConvexHull();
    Assertions.assertEquals(ch1.getArea(), ch2.getArea());
    Assertions.assertEquals(ch1.getLength(), ch2.getLength());
  }

  @Test
  public void testGetBounds() {
    final ScratchSpace ss = new ScratchSpace(4);
    Assertions.assertNull(ss.getBounds());
    ss.add(2, 2);
    ss.add(0, 2);
    ss.add(0, 0);
    ss.add(2, 0);
    final Rectangle2D bounds = ss.getBounds();
    Assertions.assertEquals(new Rectangle(0, 0, 2, 2), bounds);
  }
}
