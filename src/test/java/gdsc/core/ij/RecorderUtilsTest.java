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
package gdsc.core.ij;

import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;
import ij.plugin.frame.Recorder;

@SuppressWarnings({ "javadoc" })
public class RecorderUtilsTest
{
	// We need an instance otherwise the static method calls to
	// Recorder.setCommand() are ignored.
	static Recorder recorder = null;

	private synchronized static void createRecorder()
	{
		// This test is slow as creating the recorder involves spinning up a lot
		// ImageJ and Java AWT classes. So only run if asked for.
		TestSettings.assumeLowComplexity();
		if (recorder == null)
			recorder = new Recorder(false);
	}

	@Test
	public void canResetRecorder()
	{
		createRecorder();
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", ""), null);
	}

	@Test
	public void canResetRecorderWithQuotedValues()
	{
		createRecorder();
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("3 3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("3 3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("3 3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("", ""), null);

		canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3 3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3 3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3 3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("", ""), null);

		canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3 3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3 3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3 3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("", ""), null);

		canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3 3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3 3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3 3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("", ""), null);

		canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3 3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3 3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3 3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("", ""), null);

		canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3 3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3 3", "4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3 3", ""), null);
		canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("", "4 4"), null);
		canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("", ""), null);
	}

	@Test
	public void resetRecorderIgnoresInvalidKeys()
	{
		createRecorder();
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"), toArray("e", "f"));
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"), toArray("e", "f"));
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", ""), toArray("e", "f"));
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"), toArray("e", "f"));
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", "4"), toArray("e", "f"));
		canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", ""), toArray("e", "f"));
	}

	private static String[] toArray(String... values)
	{
		return values;
	}

	private static void canResetRecorder(String[] keys1, String[] values1, String[] keys2, String[] values2,
			String[] badKeys)
	{
		clearRecorder();
		record(keys1, values1);
		final String e1 = Recorder.getCommandOptions();
		clearRecorder();
		record(keys2, values2);
		final String e2 = Recorder.getCommandOptions();
		clearRecorder();
		record(keys1, values1);
		record(keys2, values2);
		final String e3 = Recorder.getCommandOptions();
		RecorderUtils.resetRecorder(keys2);
		final String o1 = Recorder.getCommandOptions();
		Assert.assertNotEquals("-keys2 did not change", e3, o1);
		Assert.assertEquals("-keys2", e1, o1);
		RecorderUtils.resetRecorder(badKeys);
		final String o1b = Recorder.getCommandOptions();
		Assert.assertEquals("-badkeys2", o1, o1b);
		clearRecorder();
		record(keys1, values1);
		record(keys2, values2);
		RecorderUtils.resetRecorder(keys1);
		final String o2 = Recorder.getCommandOptions();
		Assert.assertNotEquals("-keys1 did not change", e3, o2);
		Assert.assertEquals("-keys1", e2, o2);
		RecorderUtils.resetRecorder(badKeys);
		final String o2b = Recorder.getCommandOptions();
		Assert.assertEquals("-badkeys1", o2, o2b);
	}

	private static void clearRecorder()
	{
		Recorder.saveCommand();
		Recorder.setCommand("Test");
	}

	private static void record(String[] keys1, String[] values1)
	{
		for (int i = 0; i < keys1.length; i++)
			if (values1[i] != "")
				Recorder.recordOption(keys1[i], values1[i]);
			else
				Recorder.recordOption(keys1[i]);
	}
}
