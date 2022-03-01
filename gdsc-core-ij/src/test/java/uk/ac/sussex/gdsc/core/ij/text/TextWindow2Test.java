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

package uk.ac.sussex.gdsc.core.ij.text;

import ij.text.TextPanel;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class TextWindow2Test {
  @Test
  void testConstructor() {
    String title = "title";
    String text = "hello";
    int width = 300;
    int height = 400;
    TextWindow2 tw = new TextWindow2(title, text, width, height);
    Assertions.assertEquals(title, tw.getTitle());
    Assertions.assertEquals("", tw.getTextPanel().getColumnHeadings());
    Assertions.assertTrue(tw.getTextPanel().getText().contains(text));
    Assertions.assertEquals(width, tw.getWidth());
    Assertions.assertEquals(height, tw.getHeight());

    String headings = "c1";
    tw = new TextWindow2(title, headings, text, width, height);
    Assertions.assertEquals(title, tw.getTitle());
    Assertions.assertEquals(headings, tw.getTextPanel().getColumnHeadings());
    Assertions.assertTrue(tw.getTextPanel().getText().contains(text));
    Assertions.assertEquals(width, tw.getWidth());
    Assertions.assertEquals(height, tw.getHeight());

    tw.setVisible(true);
    tw.setVisible(false);
  }

  @Test
  void testListConstructor() {
    String title = "title";
    String headings = "c1";
    ArrayList<String> text = new ArrayList<>(Arrays.asList("hello", "world"));
    int width = 300;
    int height = 400;
    TextWindow2 tw = new TextWindow2(title, headings, text, width, height);
    Assertions.assertEquals(title, tw.getTitle());
    Assertions.assertEquals(width, tw.getWidth());
    Assertions.assertEquals(height, tw.getHeight());
    TextPanel tp = tw.getTextPanel();
    Assertions.assertEquals(headings, tp.getColumnHeadings());
    Assertions.assertEquals(text.size(), tp.getLineCount());
    Assertions.assertEquals(text.get(0), tp.getLine(0));
    Assertions.assertEquals(text.get(1), tp.getLine(1));
  }
}
