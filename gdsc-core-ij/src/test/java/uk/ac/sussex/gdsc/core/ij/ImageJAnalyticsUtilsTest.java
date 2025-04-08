/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
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

package uk.ac.sussex.gdsc.core.ij;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ImageJAnalyticsUtilsTest {

  @Test
  void testPageView() {
    final boolean isDisabled = ImageJAnalyticsUtils.isDisabled();
    try {
      final String documentPath = "/GDSC/Core/Tests/AnalyticsTest";
      final String documentTitle = "AnalyticsTest";
      ImageJAnalyticsUtils.setDisabled(true);
      ImageJAnalyticsUtils.pageview(documentPath, documentTitle);
      ImageJAnalyticsUtils.setDisabled(false);
      ImageJAnalyticsUtils.pageview(documentPath, documentTitle);
    } finally {
      ImageJAnalyticsUtils.setDisabled(isDisabled);
    }
  }

  @Test
  void testSantisePath() {
    Assertions.assertEquals("/", ImageJAnalyticsUtils.sanitisePath(null));
    Assertions.assertEquals("/", ImageJAnalyticsUtils.sanitisePath(""));
    Assertions.assertEquals("/", ImageJAnalyticsUtils.sanitisePath(""));
    Assertions.assertEquals("/tmp", ImageJAnalyticsUtils.sanitisePath("tmp"));
    Assertions.assertEquals("/tmp", ImageJAnalyticsUtils.sanitisePath("/tmp"));
  }

  @Test
  void testBuildPluginMap() {
    final Charset charset = StandardCharsets.UTF_8;
    final String propertiesContent =
        "#Some comments\nPlugins>GDSC>Test, \"Plugin1\", gdsc.test.Plugin_1(\"args\")\n\n"
            + "Plugins>GDSC>Test, \"Plugin2\", gdsc.test.Plugin_2\n"
            + "Plugins>GDSC>Test, \"BadPlugin\"";
    ByteArrayInputStream pluginsStream =
        new ByteArrayInputStream(propertiesContent.getBytes(charset));
    final HashMap<String, String[]> map = new HashMap<>();
    ImageJAnalyticsUtils.buildPluginMap(map, pluginsStream, charset);
    Assertions.assertEquals(2, map.size());
    Assertions.assertArrayEquals(new String[] {"/Plugins/GDSC/Test/Plugin1", "Plugin1"},
        map.get("gdsc.test.Plugin_1.args"));
    Assertions.assertArrayEquals(new String[] {"/Plugins/GDSC/Test/Plugin2", "Plugin2"},
        map.get("gdsc.test.Plugin_2"));

    // IOExceptions are swallowed
    pluginsStream = new ByteArrayInputStream(propertiesContent.getBytes(charset)) {
      @Override
      public void close() throws IOException {
        throw new EOFException("Dummy close exception");
      }
    };
    final Logger logger = Logger.getLogger(ImageJAnalyticsUtils.class.getName());
    final Level level = logger.getLevel();
    logger.setLevel(Level.OFF);
    ImageJAnalyticsUtils.buildPluginMap(map, pluginsStream, charset);
    logger.setLevel(level);
  }

  @Test
  void testGetKey() {
    Assertions.assertEquals("name", ImageJAnalyticsUtils.getKey("name", null));
    Assertions.assertEquals("name", ImageJAnalyticsUtils.getKey("name", ""));
    Assertions.assertEquals("name.arg", ImageJAnalyticsUtils.getKey("name", "arg"));
  }

  @Test
  void testDisabledProperty() {
    // Note:
    // This exercises the setDisabled logic but isDisabled is always true
    // as no analytics provider is currently used.

    final boolean isDisabled = ImageJAnalyticsUtils.isDisabled();
    try {
      for (final boolean value : new boolean[] {!isDisabled, isDisabled}) {
        ImageJAnalyticsUtils.setDisabled(value);
        Assertions.assertTrue(ImageJAnalyticsUtils.isDisabled());
        // Repeat setting the same value
        ImageJAnalyticsUtils.setDisabled(value);
        Assertions.assertTrue(ImageJAnalyticsUtils.isDisabled());
      }
    } finally {
      ImageJAnalyticsUtils.setDisabled(isDisabled);
    }
  }

  @Test
  void testUnknownStatus() {
    // No assertions.
    // We cannot control the state flag read at initialisation.
    ImageJAnalyticsUtils.unknownStatus();
  }
}
