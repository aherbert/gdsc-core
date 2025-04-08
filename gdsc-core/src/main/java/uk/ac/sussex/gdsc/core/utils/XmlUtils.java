/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML Utilities.
 */
public final class XmlUtils {

  /** The XML formatter. */
  private static final XmlFormatter FORMATTER = new XmlFormatter(2, 80);

  private static final char NEW_LINE = '\n';

  /** No public construction. */
  private XmlUtils() {}

  /**
   * Pretty print format XML. Assumes XML is valid.
   *
   * @param xml the xml
   * @return pretty-print formatted XML
   */
  public static String formatXml(String xml) {
    return formatXml(xml, 0);
  }

  /**
   * Pretty print format XML. Assumes XML is valid.
   *
   * @param xml the xml
   * @param initialIndent the initial indent
   * @return pretty-print formatted XML
   */
  public static String formatXml(String xml, int initialIndent) {
    return FORMATTER.format(xml, initialIndent);
  }

  /**
   * XML utility for formatting XML as a pretty-print output. Assumes the XML is valid since no DOM
   * conversion is performed.
   *
   * @see <a href="http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java">
   *      StackOverflow: How to pretty print XML from Java?</a>
   */
  private static class XmlFormatter {
    private static final char LESS_THAN = '<';
    private static final char GREATER_THAN = '>';
    private static final char FORWARD_SLASH = '/';
    private static final char QUESTION_MARK = '?';
    private static final char EXCLAMATION_MARK = '!';
    private static final char NEW_LINE = '\n';

    private final int indentNumChars;
    private final int lineLength;

    XmlFormatter(int indentNumChars, int lineLength) {
      this.indentNumChars = indentNumChars;
      this.lineLength = lineLength;
    }

    String format(String xml, int initialIndent) {
      boolean singleLine = false;
      int indent = initialIndent;
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < xml.length(); i++) {
        final char currentChar = xml.charAt(i);
        if (currentChar == LESS_THAN) {
          final char nextChar = xml.charAt(i + 1);
          if (nextChar == FORWARD_SLASH) {
            indent -= indentNumChars;
          }
          if (!singleLine) {
            appendWhitespace(sb, indent);
          }
          if (nextChar != QUESTION_MARK && nextChar != EXCLAMATION_MARK
              && nextChar != FORWARD_SLASH) {
            indent += indentNumChars;
          }
          singleLine = false; // Reset flag.
        }
        sb.append(currentChar);
        if (currentChar == GREATER_THAN) {
          if (xml.charAt(i - 1) == FORWARD_SLASH) {
            indent -= indentNumChars;
            sb.append(NEW_LINE);
          } else {
            final int nextStartElementPos = xml.indexOf(LESS_THAN, i);
            if (nextStartElementPos > i + 1) {
              final String textBetweenElements = xml.substring(i + 1, nextStartElementPos);

              // If the space between elements is solely newlines, let them through to preserve
              // additional newlines in source document.
              if (isNewLines(textBetweenElements)) {
                sb.append(textBetweenElements).append(NEW_LINE);
              } else if (textBetweenElements.length() <= lineLength * 0.5) {
                sb.append(textBetweenElements);
                singleLine = true;
              } else {
                // For larger amounts of text, wrap lines to a maximum line length.
                sb.append(NEW_LINE).append(lineWrap(textBetweenElements, lineLength, indent))
                    .append(NEW_LINE);
              }
              i = nextStartElementPos - 1;
            } else {
              sb.append(NEW_LINE);
            }
          }
        }
      }
      return sb.toString();
    }

    private static boolean isNewLines(String textBetweenElements) {
      for (int i = 0; i < textBetweenElements.length(); i++) {
        if (textBetweenElements.charAt(i) != NEW_LINE) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Append whitespace to the string builder.
   *
   * @param sb the string builder
   * @param numChars the number of characters
   */
  static void appendWhitespace(StringBuilder sb, int numChars) {
    for (int i = 0; i < numChars; i++) {
      sb.append(' ');
    }
  }

  /**
   * Wraps the supplied text to the specified line length.
   *
   * @param text the text
   * @param lineLength the maximum length of each line in the returned string (not including indent
   *        if specified)
   * @param indent optional number of whitespace characters to prepend to each line before the text
   * @return the supplied text wrapped so that no line exceeds the specified line length + indent,
   *         optionally with indent and prefix applied to each line.
   */
  static String lineWrap(String text, int lineLength, int indent) {
    if (text == null) {
      return null;
    }

    final int length = text.length();
    final StringBuilder sb = new StringBuilder(length);
    final int maxLineLength = Math.max(0, lineLength);
    // Scan for whitespace. These are allowed cut points.
    int lineStartPos = 0;
    int lineEndPos;
    while (lineStartPos < length) {
      // Trim
      while (lineStartPos < length && isWhitespace(text.charAt(lineStartPos))) {
        lineStartPos++;
      }
      if (lineStartPos == length) {
        break;
      }

      lineEndPos = lineStartPos + maxLineLength;
      if (lineEndPos >= length) {
        lineEndPos = length;
      } else {
        // End position is inside the string.
        // Scan back for a cut.
        while (lineEndPos > lineStartPos && !isWhitespace(text.charAt(lineEndPos))) {
          lineEndPos--;
        }

        if (lineEndPos == lineStartPos) {
          // No whitespace for a cut. Scan forward. This may scan to the end of the string.
          lineEndPos = lineStartPos + maxLineLength + 1;
          while (lineEndPos < length && !isWhitespace(text.charAt(lineEndPos))) {
            lineEndPos++;
          }
        }
      }

      if (sb.length() != 0) {
        // Not the first line
        sb.append(NEW_LINE);
      }
      appendWhitespace(sb, indent);

      trimAndAppend(sb, text, lineStartPos, lineEndPos);
      lineStartPos = lineEndPos + 1;
    }
    return sb.toString();
  }

  /**
   * Checks if the character is whitespace.
   *
   * @param ch the character
   * @return true if is whitespace
   */
  private static boolean isWhitespace(char ch) {
    // Simple whitespace detection
    return ch == ' ' || ch == '\t';
  }

  /**
   * Append the substring to the string builder. Performs a right trim before the append.
   *
   * <p>Warning: This assumes that {@code start < end} and the character at the start position is
   * not whitespace.
   *
   * @param sb the string builder
   * @param text the text
   * @param start the start
   * @param end the end
   */
  private static void trimAndAppend(StringBuilder sb, String text, int start, int end) {
    while (isWhitespace(text.charAt(end - 1))) {
      end--;
    }
    sb.append(text.substring(start, end));
  }

  /**
   * Formats an XML string for pretty printing.
   *
   * <p>Any XML parser exceptions are ignored and the method returns the input string.
   *
   * @param xml the xml
   * @return pretty-print formatted XML
   * @see <a href="http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java">
   *      StackOverflow: How to pretty print XML from Java?</a>
   */
  public static String prettyPrintXml(String xml) {
    try {
      final InputSource src = new InputSource(new StringReader(xml));
      final Node document =
          DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
      final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

      final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
      final LSSerializer writer = impl.createLSSerializer();

      // Set this to true if the output needs to be beautified.
      writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      // Set this to true if the declaration is needed to be output.
      writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

      return writer.writeToString(document);
    } catch (SAXException | IOException | ParserConfigurationException | ClassNotFoundException
        | InstantiationException | IllegalAccessException | ClassCastException ex) {
      // Ignore and return the input string
    }
    return xml;
  }

  /**
   * Return the contents of the node as a string.
   *
   * <p>Any exceptions are ignored and the method returns an empty string.
   *
   * @param node the node
   * @param indent Indent the XML when formatting
   * @return The node contents
   */
  public static String getString(Node node, boolean indent) {
    try {
      final TransformerFactory factory = TransformerFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      final Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, (indent) ? "yes" : "no");

      final StringWriter writer = new StringWriter();
      final StreamResult result = new StreamResult(writer);
      final DOMSource source = new DOMSource(node);
      transformer.transform(source, result);

      return writer.toString();
    } catch (final TransformerFactoryConfigurationError | TransformerException ex) {
      // Ignore exceptions and return empty string
    }
    return "";
  }

  /**
   * Convert double quotes to single quotes.
   *
   * <p>This method is a simple replace function call. It does not check if the xml contains a
   * mixture of single and double quotes. In that instance the returned XML will break.
   *
   * @param xml the xml
   * @return the converted xml
   */
  public static String convertQuotes(String xml) {
    return xml.replace('"', '\'');
  }
}
