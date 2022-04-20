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

package uk.ac.sussex.gdsc.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Show the version information contained in the source jar manifest.
 */
public final class VersionUtils {
  /** Constant for the string "unknown". */
  private static final String UNKNOWN = "unknown";

  /** The version number. */
  private static String versionNumber;

  /** The build date. */
  private static String buildDate;

  /** The build number. */
  private static String buildNumber;

  static {
    final Manifest manifest = loadManifest(VersionUtils.class);
    if (manifest != null) {
      final Attributes attributes = manifest.getMainAttributes();
      versionNumber = attributes.getValue("Implementation-Version");
      buildDate = attributes.getValue("Implementation-Date");
      buildNumber = attributes.getValue("Implementation-Build");
    }

    versionNumber = StringUtils.getIfBlank(versionNumber, () -> UNKNOWN);
    buildDate = StringUtils.getIfBlank(buildDate, () -> UNKNOWN);
    buildNumber = StringUtils.getIfBlank(buildNumber, () -> UNKNOWN);
  }

  /** No public construction. */
  private VersionUtils() {}

  /**
   * The main method. Output the version and build date.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    final String newLine = System.lineSeparator();
    //@formatter:off
    final StringBuilder msg = new StringBuilder(100)
        .append("Version : ").append(versionNumber).append(newLine)
        .append("Build Date : ").append(buildDate).append(newLine)
        .append("Build Number : ").append(buildNumber).append(newLine);
    //@formatter:on
    System.out.print(msg);
  }

  /**
   * Get the GDSC Core version.
   *
   * <p>This uses the 'Implementation-Version' entry in the manifest. It will have the full project
   * version including any suffix, for example SNAPSHOT, RC1, etc.
   *
   * @return The uk.ac.sussex.gdsc.core package version
   */
  public static String getVersion() {
    return versionNumber;
  }

  /**
   * Get the GDSC Core package build date.
   *
   * @return The uk.ac.sussex.gdsc.core package build date
   */
  public static String getBuildDate() {
    return buildDate;
  }

  /**
   * Get the GDSC Core package build number.
   *
   * @return The uk.ac.sussex.gdsc.core package build number
   */
  public static String getBuildNumber() {
    return buildNumber;
  }

  /**
   * Get the major version.
   *
   * @return The major version (or 0 if unknown)
   */
  public static int getMajorVersion() {
    return getMajorVersion(versionNumber);
  }

  /**
   * Get the major version.
   *
   * @param versionNumber the version number text
   * @return The major version (or 0 if unknown)
   */
  static int getMajorVersion(String versionNumber) {
    final Pattern p = Pattern.compile("^\\d+");
    final Matcher m = p.matcher(versionNumber);
    if (m.find()) {
      return Integer.parseInt(m.group());
    }
    return 0;
  }

  /**
   * Get the minor version.
   *
   * @return The minor version (or 0 if unknown)
   */
  public static int getMinorVersion() {
    return getMinorVersion(versionNumber);
  }

  /**
   * Get the minor version.
   *
   * @param versionNumber the version number text
   * @return The minor version (or 0 if unknown)
   */
  static int getMinorVersion(String versionNumber) {
    final Pattern p = Pattern.compile("^\\d+\\.(\\d+)");
    final Matcher m = p.matcher(versionNumber);
    if (m.find()) {
      return Integer.parseInt(m.group(1));
    }
    return 0;
  }

  /**
   * Get the patch version.
   *
   * @return The patch version (or 0 if unknown)
   */
  public static int getPatchVersion() {
    return getPatchVersion(versionNumber);
  }

  /**
   * Get the patch version.
   *
   * @param versionNumber the version number text
   * @return The patch version (or 0 if unknown)
   */
  static int getPatchVersion(String versionNumber) {
    final Pattern p = Pattern.compile("^\\d+\\.\\d+\\.(\\d+)");
    final Matcher m = p.matcher(versionNumber);
    if (m.find()) {
      return Integer.parseInt(m.group(1));
    }
    return 0;
  }

  /**
   * Get a string with the major, minor and patch versions.
   *
   * @return Major.Minor.Patch
   */
  public static String getMajorMinorPatch() {
    return getMajorMinorPatch(versionNumber);
  }

  /**
   * Get a string with the major, minor and patch versions.
   *
   * @param versionNumber the version number text
   * @return Major.Minor.Patch
   */
  static String getMajorMinorPatch(String versionNumber) {
    final Pattern p = Pattern.compile("^\\d+\\.\\d+\\.\\d+");
    final Matcher m = p.matcher(versionNumber);
    if (m.find()) {
      return m.group();
    }
    return "";
  }

  /**
   * Load the jar manifest for the given class.
   *
   * <p>If not from a jar or an IO exception occurs return null.
   *
   * @param clazz the class
   * @return the manifest (or null)
   */
  public static Manifest loadManifest(Class<?> clazz) {
    final String resource = "/" + clazz.getName().replace(".", "/") + ".class";
    final String classPath = clazz.getResource(resource).toString();
    if (!classPath.startsWith("jar")) {
      // Class not from JAR
      return null;
    }
    final String manifestPath =
        classPath.substring(0, classPath.lastIndexOf('!') + 1) + "/META-INF/MANIFEST.MF";
    try {
      try (InputStream in = new URL(manifestPath).openStream()) {
        return new Manifest(in);
      }
    } catch (final IOException ex) {
      Logger.getLogger(VersionUtils.class.getName()).log(Level.WARNING, ex,
          () -> "Failed to load manifest for class: " + clazz.getName());
    }
    return null;
  }
}
