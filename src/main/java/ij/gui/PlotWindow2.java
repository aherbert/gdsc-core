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
package ij.gui;

import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;

/**
 * Extension of the ij.gui.PlotWindow class to add functionality
 */
public class PlotWindow2 extends PlotWindow
{
	private static final long serialVersionUID = 5935603633626914545L;

	private static int precision = Analyzer.getPrecision();
	private static boolean scientific;
	static
	{
		int measurements = Analyzer.getMeasurements();
		scientific = (measurements & Measurements.SCIENTIFIC_NOTATION) != 0;
	}
	private static boolean update = false;

	/**
	 * Construct a plot window.
	 * This method throws an IllegalAccessError on some platforms since the super constructor is package private.
	 * 
	 * @param plot
	 */
	PlotWindow2(Plot plot)
	{
		super(plot);
	}

	/**
	 * Instantiates a new plot window 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 * @deprecated
	 * 			replaced by the Plot2 class.
	 */
	@Deprecated
	public PlotWindow2(String title, String xLabel, String yLabel, double[] xValues, double[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	/**
	 * Instantiates a new plot window 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 * @deprecated replaced by the Plot2 class.
	 */
	@Deprecated
	public PlotWindow2(String title, String xLabel, String yLabel, float[] xValues, float[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	private int currentPrecision;
	private boolean currentScientific;
	private boolean reset;

	@Override
	public void saveAsText()
	{
		askForPrecision();
		super.saveAsText();
		reset();
	}

	@Override
	public void showList()
	{
		askForPrecision();
		super.showList();
		reset();
	}

	@Override
	public void copyToClipboard(boolean writeAllColumns)
	{
		askForPrecision();
		super.copyToClipboard(writeAllColumns);
		reset();
	}

	private synchronized void askForPrecision()
	{
		currentPrecision = Analyzer.getPrecision();
		int measurements = Analyzer.getMeasurements();
		currentScientific = (measurements & Measurements.SCIENTIFIC_NOTATION) != 0;

		GenericDialog gd = new GenericDialog("Plot precision");
		gd.addSlider("Plot_precision", 0, 9, precision);
		gd.addCheckbox("Scientific_notation", scientific);
		gd.addCheckbox("Update_preferences", update);
		gd.showDialog();
		if (!gd.wasCanceled())
		{
			int p = (int) gd.getNextNumber();
			scientific = gd.getNextBoolean();
			update = gd.getNextBoolean();
			if (!gd.invalidNumber())
			{
				precision = Math.max(0, Math.min(p, 9));

				Analyzer.setPrecision(p);
				Analyzer.setMeasurement(Measurements.SCIENTIFIC_NOTATION, scientific);

				// Reset the global settings if an update was not requested
				reset = !update;
			}
		}
	}

	private void reset()
	{
		if (reset)
		{
			Analyzer.setPrecision(currentPrecision);
			Analyzer.setMeasurement(Measurements.SCIENTIFIC_NOTATION, currentScientific);
		}
	}
}
