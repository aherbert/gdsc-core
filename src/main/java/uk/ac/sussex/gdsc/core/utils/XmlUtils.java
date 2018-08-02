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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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

/**
 * XML Utilities.
 */
public class XmlUtils
{
    private static XmlFormatter formatter = new XmlFormatter(2, 80);

    /**
     * Pretty print format XML. Assumes XML is valid.
     *
     * @param xml
     *            the xml
     * @return pretty-print formatted XML
     */
    public static String formatXml(String xml)
    {
        return formatter.format(xml, 0);
    }

    /**
     * Pretty print format XML. Assumes XML is valid.
     *
     * @param xml
     *            the xml
     * @param initialIndent
     *            the initial indent
     * @return pretty-print formatted XML
     */
    public static String formatXml(String xml, int initialIndent)
    {
        return formatter.format(xml, initialIndent);
    }

    /**
     * XML utility for formatting XML as a pretty-print output.
     * Assumes the XML is valid since no DOM conversion is performed.
     *
     * Taken from http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
     */
    private static class XmlFormatter
    {
        private final int indentNumChars;
        private final int lineLength;
        private boolean singleLine;

        public XmlFormatter(int indentNumChars, int lineLength)
        {
            this.indentNumChars = indentNumChars;
            this.lineLength = lineLength;
        }

        public synchronized String format(String s, int initialIndent)
        {
            int indent = initialIndent;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++)
            {
                final char currentChar = s.charAt(i);
                if (currentChar == '<')
                {
                    final char nextChar = s.charAt(i + 1);
                    if (nextChar == '/')
                        indent -= indentNumChars;
                    if (!singleLine) // Don't indent before closing element if we're creating opening and closing elements on a single line.
                        sb.append(buildWhitespace(indent));
                    if (nextChar != '?' && nextChar != '!' && nextChar != '/')
                        indent += indentNumChars;
                    singleLine = false; // Reset flag.
                }
                sb.append(currentChar);
                if (currentChar == '>')
                    if (s.charAt(i - 1) == '/')
                    {
                        indent -= indentNumChars;
                        sb.append("\n");
                    }
                    else
                    {
                        final int nextStartElementPos = s.indexOf('<', i);
                        if (nextStartElementPos > i + 1)
                        {
                            final String textBetweenElements = s.substring(i + 1, nextStartElementPos);

                            // If the space between elements is solely newlines, let them through to preserve additional newlines in source document.
                            if (textBetweenElements.replaceAll("\n", "").length() == 0)
                                sb.append(textBetweenElements + "\n");
                            else if (textBetweenElements.length() <= lineLength * 0.5)
                            {
                                sb.append(textBetweenElements);
                                singleLine = true;
                            }
                            // For larger amounts of text, wrap lines to a maximum line length.
                            else
                                sb.append("\n" + lineWrap(textBetweenElements, lineLength, indent, null) + "\n");
                            i = nextStartElementPos - 1;
                        }
                        else
                            sb.append("\n");
                    }
            }
            return sb.toString();
        }
    }

    private static String buildWhitespace(int numChars)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numChars; i++)
            sb.append(" ");
        return sb.toString();
    }

    /**
     * Wraps the supplied text to the specified line length.
     *
     * @param s
     *            the s
     * @param lineLength
     *            the maximum length of each line in the returned string (not including indent if specified)
     * @param indent
     *            optional number of whitespace characters to prepend to each line before the text
     * @param linePrefix
     *            optional string to append to the indent (before the text)
     * @return the supplied text wrapped so that no line exceeds the specified line length + indent, optionally with
     *         indent and prefix applied to each line.
     */
    public static String lineWrap(String s, int lineLength, Integer indent, String linePrefix)
    {
        if (s == null)
            return null;

        final StringBuilder sb = new StringBuilder();
        int lineStartPos = 0;
        int lineEndPos;
        boolean firstLine = true;
        while (lineStartPos < s.length())
        {
            if (!firstLine)
                sb.append("\n");
            else
                firstLine = false;

            if (lineStartPos + lineLength > s.length())
                lineEndPos = s.length() - 1;
            else
            {
                lineEndPos = lineStartPos + lineLength - 1;
                while (lineEndPos > lineStartPos && (s.charAt(lineEndPos) != ' ' && s.charAt(lineEndPos) != '\t'))
                    lineEndPos--;
            }
            sb.append(buildWhitespace(indent));
            if (linePrefix != null)
                sb.append(linePrefix);

            sb.append(s.substring(lineStartPos, lineEndPos + 1));
            lineStartPos = lineEndPos + 1;
        }
        return sb.toString();
    }

    /**
     * Formats an XML string for pretty printing. Requires Java 1.6.
     *
     * Taken from http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
     *
     * @param xml
     *            the xml
     * @return pretty-print formatted XML
     */
    public static String prettyPrintXml(String xml)
    {
        try
        {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
                    .getDocumentElement();
            final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

            //May need this: System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.

            return writer.writeToString(document);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the contents of the node as a string. Any exceptions are ignored and the method returns null.
     *
     * @param node
     *            the node
     * @param indent
     *            Indent the XML when formatting
     * @return The node contents
     */
    public static String getString(Node node, boolean indent)
    {
        Transformer transformer;
        try
        {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, (indent) ? "yes" : "no");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(node);
            transformer.transform(source, result);

            return result.getWriter().toString();
        }
        catch (final TransformerConfigurationException e)
        {
            //e.printStackTrace();
        }
        catch (final TransformerFactoryConfigurationError e)
        {
            //e.printStackTrace();
        }
        catch (final TransformerException e)
        {
            //e.printStackTrace();
        }
        return "";
    }

    /**
     * Convert double quotes to single quotes.
     * <p>
     * This method is a simple replace function call. It does not check if the xml contains a mixture of single and
     * double quotes. In that instance the returned XML will break.
     *
     * @param xml
     *            the xml
     * @return the converted xml
     */
    public static String convertQuotes(String xml)
    {
        return xml.replace('"', '\'');
    }
}
