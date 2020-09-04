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

package uk.ac.sussex.gdsc.core.utils;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings({"javadoc"})
class XmlUtilsTest {

  @Test
  void testFormat() {
    Assertions.assertEquals("<root>\n  <node attribute='hello'/>\n</root>\n",
        XmlUtils.formatXml("<root><node attribute='hello'/></root>"));
    Assertions.assertEquals("<root>\n  <node attribute='hello'/>\n  <node>\n  </node>\n</root>\n",
        XmlUtils.formatXml("<root><node attribute='hello'/><node></node></root>"));
    Assertions.assertEquals("<root>\n  <node/>\n</root>\n",
        XmlUtils.formatXml("<root><node/></root>"));
    // Cannot handle padding after '/>'
    Assertions.assertEquals("<root>\n  <node/>\n   </root>\n",
        XmlUtils.formatXml("<root><node/>   </root>"));
    // Cannot handle padding after '>'
    Assertions.assertEquals("<root>\n  <node>\n  </node>   </root>\n",
        XmlUtils.formatXml("<root><node></node>   </root>"));
    // Can add newline inbetween elements
    Assertions.assertEquals("<root>\n  <node>\n  </node>\n\n\n\n</root>\n",
        XmlUtils.formatXml("<root><node></node>\n\n\n</root>"));
    // Can ignore comments
    Assertions.assertEquals("<root>\n  <!-- comment -->\n</root>\n",
        XmlUtils.formatXml("<root><!-- comment --></root>"));
    // Can ignore processing instructions
    Assertions.assertEquals("<root>\n  <?my-application do this ?>\n</root>\n",
        XmlUtils.formatXml("<root><?my-application do this ?></root>"));
    // Can wrap text within nodes
    Assertions.assertEquals(
        "<root>\n  <node>\n  "
            + "  01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "  </node>\n</root>\n",
        XmlUtils.formatXml("<root><node>"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789"
            + "</node></root>"));
    Assertions.assertEquals("<root>\n  <node>\n"
        + "    012345678901234567890123456789012345678901234567890123456789012345678901234567890\n"
        + "    23456789\n" + "  </node>\n</root>\n",
        XmlUtils.formatXml(
            "<root><node>" + "012345678901234567890123456789012345678901234567890123456789"
                + "012345678901234567890 23456789" + "</node></root>"));
  }

  @Test
  void testLineWrap() {
    Assertions.assertNull(XmlUtils.lineWrap(null, 80, 0));
    Assertions.assertEquals("0123456789", XmlUtils.lineWrap("0123456789", 80, 0));
    Assertions.assertEquals("    0123456789", XmlUtils.lineWrap("0123456789", 80, 4));
    Assertions.assertEquals("    0123456789", XmlUtils.lineWrap("0123456789", 5, 4));
    Assertions.assertEquals("    01234\n    56789", XmlUtils.lineWrap("01234 56789", 5, 4));
    Assertions.assertEquals("    01234\n    56789", XmlUtils.lineWrap("01234 56789", 4, 4));
    Assertions.assertEquals("01234\n56789", XmlUtils.lineWrap("01234 56789", 3, 0));
    Assertions.assertEquals("01234\n56789", XmlUtils.lineWrap("01234 56789", 2, 0));
    Assertions.assertEquals("01234\n56789", XmlUtils.lineWrap("01234 56789", 1, 0));
    Assertions.assertEquals("01234\n56789", XmlUtils.lineWrap("01234 56789", 6, 0));
    Assertions.assertEquals("01 34\n56 89", XmlUtils.lineWrap("01 34 56 89", 6, 0));
    Assertions.assertEquals("01 34 56\n89", XmlUtils.lineWrap("01 34 56 89", 8, 0));
    Assertions.assertEquals("01 34 56\n89", XmlUtils.lineWrap("01 34 56    89", 8, 0));
    Assertions.assertEquals("01 34 56\n89", XmlUtils.lineWrap("01 34 56         89", 8, 0));
    Assertions.assertEquals("01 34 56\n89", XmlUtils.lineWrap("01 34 56    89    ", 8, 0));
    Assertions.assertEquals("01234\n56789", XmlUtils.lineWrap("01234 56789   ", 1, 0));
    Assertions.assertEquals("01234\n56789", XmlUtils.lineWrap("01234\t\t56789", 1, 0));
    Assertions.assertEquals("0123\n5678\n9", XmlUtils.lineWrap("0123 5678 9", 4, 0));
  }

  @Test
  void testPrettyPrintXml() {
    for (final String xml : new String[] {"bad", "<root><node/>"}) {
      Assertions.assertEquals(xml, XmlUtils.prettyPrintXml(xml));
    }

    Assertions.assertEquals("<root>\n    <node attribute=\"hello\"/>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><node attribute='hello'/></root>"));
    // Will self-close empty nodes
    Assertions.assertEquals("<root>\n    <node attribute=\"hello\"/>\n    <node/>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><node attribute='hello'/><node></node></root>"));
    Assertions.assertEquals("<root>\n    <node/>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><node/></root>"));
    // Can handle padding after '/>'
    Assertions.assertEquals("<root>\n    <node/>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><node/>   </root>"));
    // Can handle padding after '>'
    Assertions.assertEquals("<root>\n    <node/>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><node></node>   </root>"));
    // Will remove newline inbetween elements
    Assertions.assertEquals("<root>\n    <node/>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><node></node>\n\n\n</root>"));
    // Can ignore comments
    Assertions.assertEquals("<root>\n    <!-- comment -->\n</root>\n",
        XmlUtils.prettyPrintXml("<root><!-- comment --></root>"));
    // Can ignore processing instructions
    Assertions.assertEquals("<root><?my-application do this ?>\n</root>\n",
        XmlUtils.prettyPrintXml("<root><?my-application do this ?></root>"));
  }

  @Test
  void testGetString() throws SAXException, IOException, ParserConfigurationException {
    final String xml = "<root><node/></root>";
    assertGetString(xml, xml, false);
    assertGetString("<root>\n<node/>\n</root>\n", xml, true);
    assertGetString("<root><node/></root>", "<root><node></node></root>", false);
  }

  private static void assertGetString(String expected, String xml, boolean indent)
      throws SAXException, IOException, ParserConfigurationException {
    final InputSource src = new InputSource(new StringReader(xml));
    final Node document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
    Assertions.assertEquals(expected, XmlUtils.getString(document, indent));
  }

  @Test
  void testConvertQuotes() {
    Assertions.assertEquals("<node attribute='hello'/>",
        XmlUtils.convertQuotes("<node attribute=\"hello\"/>"));
  }
}
