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

package uk.ac.sussex.gdsc.core.trees;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class StatusStackTest {
  @Test
  public void testStack() {
    final StatusStack stack = new StatusStack(3);
    stack.push(Status.ALLVISITED);
    stack.push(Status.RIGHTVISITED);
    stack.push(Status.LEFTVISITED);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> stack.push(Status.NONE));
    Assertions.assertEquals(Status.LEFTVISITED, stack.pop());
    Assertions.assertEquals(Status.RIGHTVISITED, stack.pop());
    Assertions.assertEquals(Status.ALLVISITED, stack.pop());
    // Allows pop to be called once when empty
    Assertions.assertEquals(0, stack.pop());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> stack.pop());
  }
}
