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

package uk.ac.sussex.gdsc.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class VersionUtilsTest {

  @Test
  public void testVersionUtils() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream test = new PrintStream(baos);
    final PrintStream out = System.out;
    System.setOut(test);
    VersionUtils.main(null);
    System.setOut(out);
    final String text = baos.toString();
    Assertions.assertTrue(text.contains(VersionUtils.getVersion()));
    Assertions.assertTrue(text.contains(VersionUtils.getBuildDate()));
    Assertions.assertTrue(text.contains(VersionUtils.getBuildNumber()));
    VersionUtils.getMajorVersion();
    VersionUtils.getMinorVersion();
    VersionUtils.getPatchVersion();
    Assertions.assertNotNull(VersionUtils.getMajorMinorPatch());
  }

  @Test
  public void testGetSemVern() {
    Assertions.assertEquals(0, VersionUtils.getMajorVersion(""));
    Assertions.assertEquals(1, VersionUtils.getMajorVersion("1.2.3"));
    Assertions.assertEquals(0, VersionUtils.getMinorVersion(""));
    Assertions.assertEquals(2, VersionUtils.getMinorVersion("1.2.3"));
    Assertions.assertEquals(0, VersionUtils.getPatchVersion(""));
    Assertions.assertEquals(3, VersionUtils.getPatchVersion("1.2.3"));
    Assertions.assertEquals("", VersionUtils.getMajorMinorPatch(""));
    Assertions.assertEquals("1.2.3", VersionUtils.getMajorMinorPatch("1.2.3"));
    Assertions.assertEquals("", VersionUtils.getMajorMinorPatch("something-1.2.3"));
  }
}
