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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.ij;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.BufferedTextWindow.Output;
import uk.ac.sussex.gdsc.core.utils.LocalList;

@SuppressWarnings({"javadoc"})
class BufferedTextWindowTest {
  @Test
  void testProperties() {
    @SuppressWarnings("resource")
    final BufferedTextWindow tw = new BufferedTextWindow((Output) null);
    Assertions.assertEquals(10, tw.getIncrement());
    tw.setIncrement(3);
    Assertions.assertEquals(3, tw.getIncrement());
  }

  @Test
  void testBuffer() {
    final LocalList<String> list = new LocalList<>();
    final AtomicInteger count = new AtomicInteger();
    final Output output = new Output() {
      @Override
      public void write(String text) {
        list.add(text);
      }

      @Override
      public void flush() {
        count.incrementAndGet();
      }
    };

    final ArrayList<String> expected = new ArrayList<>();
    try (BufferedTextWindow tw = new BufferedTextWindow(output)) {
      for (int i = 0; i < 15; i++) {
        final String value = Integer.toString(i);
        tw.append(value);
        expected.add(value);
      }
    }
    Assertions.assertEquals(expected, list);
    Assertions.assertEquals(2, count.get());
  }
}
