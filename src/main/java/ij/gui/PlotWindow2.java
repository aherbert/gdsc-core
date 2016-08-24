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

	@Deprecated
	public PlotWindow2(String title, String xLabel, String yLabel, double[] xValues, double[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

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
				Analyzer.setMeasurement(Analyzer.SCIENTIFIC_NOTATION, scientific);

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
			Analyzer.setMeasurement(Analyzer.SCIENTIFIC_NOTATION, currentScientific);
		}
	}
}
