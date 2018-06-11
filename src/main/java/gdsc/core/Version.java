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
package gdsc.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Show the version information contained in the gdsc/core/Version.txt file.
 * <p>
 * Uses Semantic Versioning.
 * 
 * @see http://semver.org/
 */
public class Version
{
	public static final String UNKNOWN = "unknown";
	private static String version = null;
	private static String buildDate = null;

	static
	{
		// Locate the version file
		Class<Version> resourceClass = Version.class;
		InputStream propertiesStream = resourceClass.getResourceAsStream("/gdsc/core/Version.txt");

		try
		{
			// Read the version properties
			Properties props = new Properties();
			props.load(propertiesStream);
			version = props.getProperty("version");
			buildDate = props.getProperty("build.date");
		}
		catch (IOException e)
		{
			// Ignore
		}

		if (version == null || version.length() == 0)
			version = UNKNOWN;
		if (buildDate == null || buildDate.length() == 0)
			buildDate = UNKNOWN;
	}

	public static void main(String[] args)
	{
		StringBuilder msg = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		msg.append("Version : ").append(version).append(newLine);
		msg.append("Build Date : ").append(buildDate).append(newLine);
		System.out.print(msg.toString());
	}

	/**
	 * Get the GDSC Core version
	 * 
	 * @return The gdsc.core package version
	 */
	public static String getVersion()
	{
		return version;
	}

	/**
	 * Get the GDSC Core package build date
	 * 
	 * @return The gdsc.core package build date
	 */
	public static String getBuildDate()
	{
		return buildDate;
	}

	/**
	 * Get the major version
	 * 
	 * @return The major version (or 0 if unknown)
	 */
	public static int getMajorVersion()
	{
		Pattern p = Pattern.compile("^\\d+");
		Matcher m = p.matcher(version);
		if (m.find())
			return Integer.parseInt(m.group());
		return 0;
	}

	/**
	 * Get the minor version
	 * 
	 * @return The minor version (or 0 if unknown)
	 */
	public static int getMinorVersion()
	{
		Pattern p = Pattern.compile("^\\d+\\.(\\d+)");
		Matcher m = p.matcher(version);
		if (m.find())
			return Integer.parseInt(m.group(1));
		return 0;
	}

	/**
	 * Get the patch version
	 * 
	 * @return The patch version (or 0 if unknown)
	 */
	public static int getPatchVersion()
	{
		Pattern p = Pattern.compile("^\\d+\\.\\d+\\.(\\d+)");
		Matcher m = p.matcher(version);
		if (m.find())
			return Integer.parseInt(m.group(1));
		return 0;
	}

	/**
	 * Get a string with the major, minor and patch versions
	 * 
	 * @return Major.Minor.Patch
	 */
	public static String getMajorMinorPatch()
	{
		Pattern p = Pattern.compile("^\\d+\\.\\d+\\.\\d+");
		Matcher m = p.matcher(version);
		if (m.find())
			return m.group();
		return "";
	}
}
