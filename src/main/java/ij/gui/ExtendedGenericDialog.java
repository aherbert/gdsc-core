/*
 * 
 */
package ij.gui;

import java.awt.BorderLayout;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import gdsc.core.ij.RecorderUtils;
import gdsc.core.ij.Utils;
import gdsc.core.utils.TurboList;
import ij.IJ;
import ij.plugin.frame.Recorder;

/**
 * Extension of the ij.gui.GenericDialog class to add functionality.
 * <p>
 * This dialog adds all components to a panel that is presented in a java.awt.ScrollPane. This allows display of large
 * dialogs on small screens.
 */
public class ExtendedGenericDialog extends GenericDialog
{
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2405780565152258007L;
	private Component positionComponent = null;

	private TurboList<OptionListener<?>> listeners;

	/** The labels. Used to reset the recorder. */
	private TurboList<String> labels = new TurboList<String>();

	// We capture all components added to the dialog and put them on a single panel
	private GridBagLayout grid;
	private JPanel panel = new JPanel();

	// Store the screen dimension
	private static Dimension screenDimension;
	static
	{
		screenDimension = IJ.getScreenSize();
	}

	// Max unscrolled width - Set to a reasonable value for current screen resolution.
	private int maxWidth = screenDimension.width - 100;
	// Max unscrolled height - Set to a reasonable value for current screen resolution.
	private int maxHeight = screenDimension.height - 150;

	/**
	 * Instantiates a new extended generic dialog.
	 *
	 * @param title
	 *            the title
	 * @param parent
	 *            the parent
	 */
	public ExtendedGenericDialog(String title, Frame parent)
	{
		super(title, parent);
		initialise();
	}

	/**
	 * Instantiates a new extended generic dialog.
	 *
	 * @param title
	 *            the title
	 */
	public ExtendedGenericDialog(String title)
	{
		super(title);
		initialise();
	}

	private void initialise()
	{
		grid = (GridBagLayout) getLayout();
		panel.setLayout(grid);

		this.setLayout(new BorderLayout());

		// Use a scroll pane as required
		if (Utils.isShowGenericDialog())
		{
			// JScrollPane allows the border to be set to null.
			// However it does not repaint the contents inside the 
			// window unless we use a JPanel. Even then some of the 
			// java.awt.Panel components are not always redrawn.
			// Given the GenericDialog uses a lot of java.awt components 
			// we stick to use a java.awt.ScrollPane. 
			//JScrollPane scroll = new JScrollPane(panel);
			//scroll.setBorder(BorderFactory.createEmptyBorder());
			//scroll.getVerticalScrollBar().setUnitIncrement(8);
			//scroll.getHorizontalScrollBar().setUnitIncrement(8);
			//scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

			// The ScrollPanel must be sized from the default of 100x100 when
			// displayed. This is done in setup()
			ScrollPane scroll = new ScrollPane();
			scroll.getHAdjustable().setUnitIncrement(16);
			scroll.getVAdjustable().setUnitIncrement(16);
			scroll.add(panel);

			super.add(scroll, BorderLayout.CENTER);
		}
		else
		{
			// No need for a scroll pane
			super.add(panel);
		}
	}

	// Record all the field names

	@Override
	public void addCheckbox(String label, boolean defaultValue)
	{
		labels.add(label);
		super.addCheckbox(label, defaultValue);
	}

	@Override
	public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues)
	{
		for (String label : labels)
			this.labels.add(label);
		super.addCheckboxGroup(rows, columns, labels, defaultValues);
	}

	@Override
	public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues, String[] headings)
	{
		for (String label : labels)
			this.labels.add(label);
		super.addCheckboxGroup(rows, columns, labels, defaultValues, headings);
	}

	@Override
	public void addChoice(String label, String[] items, String defaultItem)
	{
		labels.add(label);
		super.addChoice(label, items, defaultItem);
	}

	/**
	 * Adds the choice.
	 *
	 * @param label
	 *            the label
	 * @param items
	 *            the items
	 * @param defaultIndex
	 *            the default index
	 */
	public void addChoice(String label, String[] items, int defaultIndex)
	{
		if (defaultIndex < 0 || defaultIndex >= items.length)
			defaultIndex = 0;
		String defaultItem = items[defaultIndex];
		addChoice(label, items, defaultItem);
	}

	@Override
	public void addNumericField(String label, double defaultValue, int digits)
	{
		labels.add(label);
		super.addNumericField(label, defaultValue, digits);
	}

	@Override
	public void addNumericField(String label, double defaultValue, int digits, int columns, String units)
	{
		labels.add(label);
		super.addNumericField(label, defaultValue, digits, columns, units);
	}

	@Override
	public void addSlider(String label, double minValue, double maxValue, double defaultValue)
	{
		labels.add(label);
		super.addSlider(label, minValue, maxValue, defaultValue);
	}

	@Override
	public void addStringField(String label, String defaultText)
	{
		labels.add(label);
		super.addStringField(label, defaultText);
	}

	@Override
	public void addStringField(String label, String defaultText, int columns)
	{
		labels.add(label);
		super.addStringField(label, defaultText, columns);
	}

	@Override
	public void addTextAreas(String text1, String text2, int rows, int columns)
	{
		labels.add("text1");
		if (text2 != null)
			labels.add("text2");
		super.addTextAreas(text1, text2, rows, columns);
	}

	/**
	 * Gets the last label added to the dialog.
	 *
	 * @return the last label
	 */
	public Label getLastLabel()
	{
		return new ComponentFinder<Label>(getContents(), Label.class).getLast();
	}

	/**
	 * Gets the last getContents() added to the dialog.
	 *
	 * @return the last getContents()
	 */
	public Panel getLastPanel()
	{
		return new ComponentFinder<Panel>(getContents(), Panel.class).getLast();
	}

	/**
	 * Gets the last choice added to the dialog.
	 *
	 * @return the last choice
	 */
	public Choice getLastChoice()
	{
		return new ComponentFinder<Choice>(getContents(), Choice.class).getLast();
	}

	/**
	 * Gets the last text field added to the dialog.
	 *
	 * @return the last text field
	 */
	public TextField getLastTextField()
	{
		return new ComponentFinder<TextField>(getContents(), TextField.class).getLast();
	}

	/**
	 * Gets the last checkbox added to the dialog.
	 *
	 * @return the last checkbox
	 */
	public Checkbox getLastCheckbox()
	{
		return new ComponentFinder<Checkbox>(getContents(), Checkbox.class).getLast();
	}

	/**
	 * Gets the last scrollbar added to the dialog.
	 *
	 * @return the last scrollbar
	 */
	public Scrollbar getLastScrollbar()
	{
		return new ComponentFinder<Scrollbar>(getContents(), Scrollbar.class).getLast();
	}

	private Container getContents()
	{
		// We may not want to put all contents into a single panel
		return panel;
	}

	/**
	 * Simple generic component finder to traverse the dialog looking for the last component of a given type.
	 *
	 * @param <T>
	 *            the generic type
	 */
	private static class ComponentFinder<T>
	{
		Container container;
		Class<T> type;

		ComponentFinder(Container container, Class<T> type)
		{
			this.container = container;
			this.type = type;
		}

		T getLast()
		{
			return getLast(container);
		}

		T getLast(Container container)
		{
			int n = container.getComponentCount();
			while (n-- > 0)
			{
				Component c = container.getComponent(n);
				if (type.isInstance(c))
					return type.cast(c);
				if (c instanceof Container)
				{
					// Traverse containers
					T t = getLast((Container) c);
					if (t != null)
						return t;
				}
			}
			return null;
		}
	}

	/**
	 * Adds and then gets a checkbox.
	 *
	 * @param label
	 *            the label
	 * @param defaultValue
	 *            the default value
	 * @return the checkbox
	 */
	public Checkbox addAndGetCheckbox(String label, boolean defaultValue)
	{
		addCheckbox(label, defaultValue);
		return (Checkbox) tail(getCheckboxes());
	}

	/**
	 * Tail.
	 *
	 * @param v
	 *            the v
	 * @return the object
	 */
	private Object tail(Vector<?> v)
	{
		return v.get(v.size() - 1);
	}

	/**
	 * Adds and then gets a choice.
	 *
	 * @param label
	 *            the label
	 * @param items
	 *            the items
	 * @param defaultItem
	 *            the default item
	 * @return the choice
	 */
	public Choice addAndGetChoice(String label, String[] items, String defaultItem)
	{
		addChoice(label, items, defaultItem);
		return (Choice) tail(getChoices());
	}

	/**
	 * Adds and then gets a string field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default text
	 * @return the text field
	 */
	public TextField addAndGetStringField(String label, String defaultText)
	{
		addStringField(label, defaultText);
		return (TextField) tail(getStringFields());
	}

	/**
	 * Adds and then gets a string field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default text
	 * @param columns
	 *            the columns
	 * @return the text field
	 */
	public TextField addAndGetStringField(String label, String defaultText, int columns)
	{
		addStringField(label, defaultText, columns);
		return (TextField) tail(getStringFields());
	}

	/**
	 * Adds and then gets a numeric field.
	 *
	 * @param label
	 *            the label
	 * @param defaultValue
	 *            the default value
	 * @param digits
	 *            the digits
	 * @param columns
	 *            the columns
	 * @param units
	 *            the units
	 * @return the text field
	 */
	public TextField addAndGetNumericField(String label, double defaultValue, int digits, int columns, String units)
	{
		addNumericField(label, defaultValue, digits, columns, units);
		return (TextField) tail(getNumericFields());
	}

	/**
	 * Adds and then gets a numeric field.
	 *
	 * @param label
	 *            the label
	 * @param defaultValue
	 *            the default value
	 * @param digits
	 *            the digits
	 * @return the text field
	 */
	public TextField addAndGetNumericField(String label, double defaultValue, int digits)
	{
		addNumericField(label, defaultValue, digits);
		return (TextField) tail(getNumericFields());
	}

	/**
	 * Adds and then gets a slider.
	 *
	 * @param label
	 *            the label
	 * @param minValue
	 *            the min value
	 * @param maxValue
	 *            the max value
	 * @param defaultValue
	 *            the default value
	 * @return the scrollbar
	 */
	public Scrollbar addAndGetSlider(String label, double minValue, double maxValue, double defaultValue)
	{
		addSlider(label, minValue, maxValue, defaultValue);
		return (Scrollbar) tail(getSliders());
	}

	/**
	 * Adds the button.
	 *
	 * @param label
	 *            the label
	 * @param actionListener
	 *            the action listener (must not be null)
	 * @return the button
	 * @throws NullPointerException
	 *             if the action listener is null
	 */
	public Button addAndGetButton(String label, final ActionListener actionListener)
	{
		if (actionListener == null)
			throw new NullPointerException("Action listener is missing for the button");

		// To make room for the button we add a message and then remove that from the dialog
		addMessage("-- Empty --");

		// Get the message and 'steal' the constraints so we get the current row
		Label msg = (Label) getMessage();
		GridBagConstraints c = grid.getConstraints(msg);

		// Remove the dummy message
		remove(msg);

		// Add a button		
		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		Button button = new Button(label);
		button.addActionListener(actionListener);
		buttons.add(button);
		c.gridx = 1;
		//c.gridy = y; // Same as the message label we removed
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 0, 0);
		grid.setConstraints(buttons, c);
		add(buttons);

		return button;
	}

	// Add fields as per the normal add methods but have a custom OptionListener interface
	// to call when an options button is pressed. Add a ... button at the end of the row
	// with a mouse over tooltip that calls the option listener

	/**
	 * Invoked to collect options for the field value.
	 */
	public interface OptionListener<T>
	{
		/**
		 * Gets the options using the current value of the field.
		 *
		 * @param value
		 *            the field value
		 * @return true, if options were collected successful
		 */
		public boolean collectOptions(T value);

		/**
		 * Gets the options using the previously read value of the field.
		 * <p>
		 * This will be called when the parent field is read using the appropriate getNext(...) method. It allows macros
		 * to be supported by either recording the options in the Recorder or reading the options from the Macro
		 * options. The simple implementation is to construct an ExtendedGenericDialog to collect the options but do not
		 * present the dialog using the showDialog() method, just proceed direct to reading the fields..
		 * 
		 * @return true, if options were collected successful
		 */
		public boolean collectOptions();
	}

	/**
	 * An event generated when options are collected
	 */
	public class OptionCollectedEvent
	{
		private final String label;

		public OptionCollectedEvent(String label)
		{
			this.label = label;
		}

		/**
		 * Gets the label of the field for which options were collected.
		 *
		 * @return the label
		 */
		public String getLabel()
		{
			return label;
		}
	}

	/**
	 * The listener interface for receiving optionCollected events.
	 * The class that is interested in processing a optionCollected
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOptionCollectedListener<code> method. When
	 * the optionCollected event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see OptionCollectedEvent
	 */
	public interface OptionCollectedListener
	{
		/**
		 * Called if options were collected.
		 *
		 * @param e
		 *            the event
		 */
		public void optionCollected(OptionCollectedEvent e);
	}

	private TurboList<OptionCollectedListener> optionCollectedListeners = null;

	public void addOptionCollectedListener(OptionCollectedListener listener)
	{
		if (optionCollectedListeners == null)
			optionCollectedListeners = new TurboList<OptionCollectedListener>();
		optionCollectedListeners.add(listener);
	}

	private void notifyOptionCollectedListeners(String label)
	{
		if (optionCollectedListeners == null)
			return;
		OptionCollectedEvent e = new OptionCollectedEvent(label);
		for (int i = 0; i < optionCollectedListeners.size(); i++)
			optionCollectedListeners.getf(i).optionCollected(e);
	}

	/**
	 * Adds the string field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default text
	 * @param optionListener
	 *            the option listener
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public void addStringField(String label, String defaultText, final OptionListener<String> optionListener)
	{
		addStringField(label, defaultText, 8, optionListener);
	}

	/**
	 * Adds the string field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default text
	 * @param columns
	 *            the columns
	 * @param optionListener
	 *            the option listener
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public void addStringField(final String label, String defaultText, int columns,
			final OptionListener<String> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		final TextField tf = addAndGetStringField(label, defaultText, columns);
		GridBagConstraints c = grid.getConstraints(tf);
		remove(tf);

		add(optionListener);

		JButton button = createOptionButton();
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (optionListener.collectOptions(tf.getText()))
					notifyOptionCollectedListeners(label);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.add(tf);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
	}

	private JButton lastOptionButton = null;

	private JButton createOptionButton()
	{
		return createOptionButton("Extra options");
	}

	// Try and control the button appearance across platforms
	private static Color buttonBackgroundColor;
	private static int buttonRightBorder;
	static
	{
		buttonBackgroundColor = new Color(240, 240, 240);
		// For some reason an extra 2 pixels looks correct on a mac
		buttonRightBorder = (IJ.isMacOSX()) ? 3 : 1;
	}

	private JButton createOptionButton(String tooltip)
	{
		lastOptionButton = new JButton("...");
		lastOptionButton.setBackground(buttonBackgroundColor);
		lastOptionButton.setToolTipText(tooltip);
		lastOptionButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(1),
				BorderFactory.createEmptyBorder(1, 1, 1, buttonRightBorder)));
		return lastOptionButton;
	}

	/**
	 * Gets a reference to the most recently added option button.
	 *
	 * @return the last option button
	 */
	public JButton getLastOptionButton()
	{
		return lastOptionButton;
	}

	private int add(OptionListener<?> optionListener)
	{
		if (listeners == null)
		{
			listeners = new TurboList<OptionListener<?>>();
		}
		int id = listeners.size();
		listeners.add(optionListener);
		return id;
	}

	/**
	 * Adds the filename field.
	 * <p>
	 * Note that if the filename is empty then the open dialog within the action listener
	 * will use ij.io.OpenDialog.getDefaultDirectory(). This can be changed using
	 * ij.io.OpenDialog.setDefaultDirectory(String).
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default filename
	 * @return the text field
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public TextField addFilenameField(String label, String defaultText)
	{
		return addFilenameField(label, defaultText, 30);
	}

	/**
	 * Adds the filename field.
	 * <p>
	 * Note that if the filename is empty then the open dialog within the action listener
	 * will use ij.io.OpenDialog.getDefaultDirectory(). This can be changed using
	 * ij.io.OpenDialog.setDefaultDirectory(String).
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default filename
	 * @param columns
	 *            the columns
	 * @return the text field
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public TextField addFilenameField(final String label, String defaultText, int columns)
	{
		final TextField tf = addAndGetStringField(label, defaultText, columns);
		GridBagConstraints c = grid.getConstraints(tf);
		remove(tf);

		JButton button = createOptionButton("Select a file");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean record = Recorder.record;
				String filename = Utils.getFilename(label, tf.getText());
				Recorder.record = record;
				if (filename != null)
					tf.setText(filename);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.add(tf);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);

		return tf;
	}

	/**
	 * Adds the directory field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default directory
	 * @return the text field
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public TextField addDirectoryField(String label, String defaultText)
	{
		return addDirectoryField(label, defaultText, 30);
	}

	/**
	 * Adds the directory field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default directory
	 * @param columns
	 *            the columns
	 * @return the text field
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public TextField addDirectoryField(final String label, String defaultText, int columns)
	{
		final TextField tf = addAndGetStringField(label, defaultText, columns);
		GridBagConstraints c = grid.getConstraints(tf);
		remove(tf);

		JButton button = createOptionButton("Select a directory");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean record = Recorder.record;
				String filename = Utils.getDirectory(label, tf.getText());
				Recorder.record = record;
				if (filename != null)
					tf.setText(filename);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.add(tf);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);

		return tf;
	}

	/**
	 * Adds the choice.
	 *
	 * @param label
	 *            the label
	 * @param items
	 *            the items
	 * @param defaultIndex
	 *            the default index
	 * @param optionListener
	 *            the option listener
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public void addChoice(String label, String[] items, int defaultIndex, final OptionListener<Integer> optionListener)
	{
		if (defaultIndex < 0 || defaultIndex >= items.length)
			defaultIndex = 0;
		String defaultItem = items[defaultIndex];
		addChoice(label, items, defaultItem, optionListener);
	}

	/**
	 * Adds the choice.
	 *
	 * @param label
	 *            the label
	 * @param items
	 *            the items
	 * @param defaultItem
	 *            the default item
	 * @param optionListener
	 *            the option listener
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public void addChoice(final String label, String[] items, String defaultItem,
			final OptionListener<Integer> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		final Choice choice = addAndGetChoice(label, items, defaultItem);
		GridBagConstraints c = grid.getConstraints(choice);
		remove(choice);

		add(optionListener);

		JButton button = createOptionButton();
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (optionListener.collectOptions(choice.getSelectedIndex()))
					notifyOptionCollectedListeners(label);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.add(choice);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
	}

	/**
	 * Adds the choice.
	 *
	 * @param label
	 *            the label
	 * @param defaultValue
	 *            the default value
	 * @param optionListener
	 *            the option listener
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public void addCheckbox(final String label, boolean defaultValue, final OptionListener<Boolean> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		final Checkbox cb = addAndGetCheckbox(label, defaultValue);
		GridBagConstraints c = grid.getConstraints(cb);
		remove(cb);

		add(optionListener);

		JButton button = createOptionButton();
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (optionListener.collectOptions(cb.getState()))
					notifyOptionCollectedListeners(label);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.add(cb);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
	}

	/**
	 * Adds a slider (scroll bar) to the dialog box.
	 * Floating point values will be used if (maxValue-minValue)<=5.0
	 * and either minValue or maxValue are non-integer.
	 * 
	 * @param label
	 *            the label
	 * @param minValue
	 *            the minimum value of the slider
	 * @param maxValue
	 *            the maximum value of the slider
	 * @param defaultValue
	 *            the initial value of the slider
	 */
	public void addSlider(final String label, double minValue, double maxValue, double defaultValue,
			final OptionListener<Double> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		if (defaultValue < minValue)
			defaultValue = minValue;
		if (defaultValue > maxValue)
			defaultValue = maxValue;

		addSlider(label, minValue, maxValue, defaultValue);
		Panel p = getLastPanel();

		final TextField tf = new ComponentFinder<TextField>(p, TextField.class).getLast();
		final String originalText = tf.getText();
		final double originalValue = defaultValue;

		add(optionListener);

		JButton button = createOptionButton();
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String theText = tf.getText();
				Double value;
				if (theText.equals(originalText))
				{
					value = originalValue;
				}
				else
				{
					value = convertToDouble(theText);
				}
				if (optionListener.collectOptions(value))
					notifyOptionCollectedListeners(label);
			}
		});

		GridBagConstraints pc = new GridBagConstraints();
		pc.gridy = 0;
		pc.gridx = 2;
		pc.insets = new Insets(5, 5, 0, 0);
		pc.anchor = GridBagConstraints.EAST;
		p.add(button, pc);
	}

	private Double convertToDouble(String theText)
	{
		// This catches any number format exceptions
		Double value = getValue(theText);
		if (value == null)
			// return NaN and let the downstream code handle this
			return Double.NaN;
		return value;
	}

	/**
	 * Adds a numeric field. The first word of the label must be
	 * unique or command recording will not work.
	 *
	 * @param label
	 *            the label
	 * @param defaultValue
	 *            value to be initially displayed
	 * @param digits
	 *            number of digits to right of decimal point
	 * @param optionListener
	 *            the option listener
	 */
	public void addNumericField(String label, double defaultValue, int digits,
			final OptionListener<Double> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		TextField tf = addAndGetNumericField(label, defaultValue, digits);

		addNumericFieldListener(tf, label, defaultValue, optionListener);
	}

	/**
	 * Adds a numeric field. The first word of the label must be
	 * unique or command recording will not work.
	 *
	 * @param label
	 *            the label
	 * @param defaultValue
	 *            value to be initially displayed
	 * @param digits
	 *            number of digits to right of decimal point
	 * @param columns
	 *            width of field in characters
	 * @param units
	 *            a string displayed to the right of the field
	 * @param optionListener
	 *            the option listener
	 */
	public void addNumericField(String label, double defaultValue, int digits, int columns, String units,
			final OptionListener<Double> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		TextField tf = addAndGetNumericField(label, defaultValue, digits, columns, units);

		addNumericFieldListener(tf, label, defaultValue, optionListener);
	}

	/**
	 * Adds a numeric field. The first word of the label must be
	 * unique or command recording will not work.
	 *
	 * @param tf
	 *            the tf
	 * @param label
	 *            the label
	 * @param originalValue
	 *            value to be initially displayed
	 * @param optionListener
	 *            the option listener
	 */
	private void addNumericFieldListener(final TextField tf, final String label, final double originalValue,
			final OptionListener<Double> optionListener)
	{
		// Numeric fields may have a text field and units in a panel
		Component lastAdded = getContents().getComponent(getContents().getComponentCount() - 1);

		GridBagConstraints c = grid.getConstraints(lastAdded);
		remove(lastAdded);

		final String originalText = tf.getText();

		add(optionListener);

		JButton button = createOptionButton();
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String theText = tf.getText();
				Double value;
				if (theText.equals(originalText))
				{
					value = originalValue;
				}
				else
				{
					value = getValue(theText);
					if (value == null)
						value = Double.NaN;
				}
				if (optionListener.collectOptions(value))
					notifyOptionCollectedListeners(label);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		panel.add(lastAdded);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
	}

	/**
	 * Reset the recorder for all the named fields that have been added to the dialog. This should be called if the
	 * dialog is to be reused as repeat calls to getNext(...) for fields with the same name will cause ImageJ to show a
	 * duplicate field error.
	 */
	public void resetRecorder()
	{
		RecorderUtils.resetRecorder(labels.toArray(new String[labels.size()]));
	}

	/**
	 * Show the dialog.
	 *
	 * @param resetRecorder
	 *            Set to true to reset the recorder for all the named fields that have been added to the dialog. Ignored
	 *            if the Recorder is not enabled.
	 * @see ij.gui.GenericDialog#showDialog()
	 */
	public void showDialog(boolean resetRecorder)
	{
		if (resetRecorder && Recorder.record)
			resetRecorder();
		super.showDialog();
	}

	/**
	 * Show the dialog.
	 *
	 * @param resetRecorder
	 *            Set to true to reset the recorder for all the named fields that have been added to the dialog. Ignored
	 *            if the Recorder is not enabled.
	 * @param positionComponent
	 *            Sets the component that will be used to position this dialog
	 * @see ij.gui.GenericDialog#showDialog()
	 */
	public void showDialog(boolean resetRecorder, Component positionComponent)
	{
		setPositionComponent(positionComponent);
		showDialog(resetRecorder);
	}

	/**
	 * Sets the component that will be used to position this dialog. See
	 * {@link #setLocationRelativeTo(Component)}.
	 *
	 * @param c
	 *            the new position component
	 */
	public void setPositionComponent(Component c)
	{
		positionComponent = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Dialog#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b)
	{
		// Allow positioning relative to a parent component
		if (positionComponent != null && positionComponent.isVisible())
			setLocationRelativeTo(positionComponent);

		super.setVisible(b);
	}

	/**
	 * Collect the options from all the option listeners silently. Calls all the listeners since the value may have been
	 * changed since they were last called interactively.
	 * <p>
	 * This should be called after all the fields have been read. This allows the fields to be read correctly from Macro
	 * option arguments. It also allows the options to be recorded to the Recorder.
	 * <p>
	 * This method does nothing if the Recorder is disabled or this is not running in a macro, i.e. there is no point
	 * collecting options again.
	 */
	public void collectOptions()
	{
		if (listeners == null)
			return;
		if (!(Recorder.record || Utils.isMacro()))
			return;
		for (int i = 0; i < listeners.size(); i++)
		{
			listeners.getf(i).collectOptions();
		}
	}

	/**
	 * Gets the max width before a scroll pane is used.
	 *
	 * @return the max width
	 */
	public int getMaxUnscrolledWidth()
	{
		return maxWidth;
	}

	/**
	 * Sets the max width before a scroll pane is used.
	 *
	 * @param maxWidth
	 *            the new max width
	 */
	public void setMaxUnscrolledWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}

	/**
	 * Gets the max height before a scroll pane is used.
	 *
	 * @return the max height
	 */
	public int getMaxUnscrolledHeight()
	{
		return maxHeight;
	}

	/**
	 * Sets the max height before a scroll pane is used.
	 *
	 * @param maxHeight
	 *            the new max height
	 */
	public void setMaxUnscrolledHeight(int maxHeight)
	{
		this.maxHeight = maxHeight;
	}

	/**
	 * Sets the max size before a scroll pane is used.
	 *
	 * @param maxWidth
	 *            the max width
	 * @param maxHeight
	 *            the max height
	 */
	public void setMaxUnscrolledSize(int maxWidth, int maxHeight)
	{
		setMaxUnscrolledWidth(maxWidth);
		setMaxUnscrolledHeight(maxHeight);
	}

	// Methods used within GenericDialog to add components to the container.
	// These are overridden so that we can add components to a single panel.

	@Override
	public Component add(Component comp)
	{
		return panel.add(comp);
	}

	@Override
	public void remove(Component comp)
	{
		panel.remove(comp);
	}

	/**
	 * This method is called just before the dialog is set visible. Determine the preferred size of the panel
	 * contents and appropriately size the scroll pane to fit.
	 * 
	 * @see ij.gui.GenericDialog#setup()
	 */
	@Override
	protected void setup()
	{
		if (!Utils.isShowGenericDialog())
			return;

		// This is how to set the preferred size on the JScrollPane.
		// This is not used as the java.awt components are not repainted.
		//		if (maxWidth <= 0 && maxHeight <= 0)
		//			return;
		//		JScrollPane scroll = (JScrollPane) getComponent(0);
		//		JViewport viewport = scroll.getViewport();
		//		Dimension d = viewport.getViewSize();
		//		if (maxWidth > 0)
		//			d.width = Math.min(d.width, maxWidth);
		//		if (maxHeight > 0)
		//			d.height = Math.min(d.height, maxHeight);
		//		viewport.setPreferredSize(d);

		// Appropriately size the scrollpane
		Dimension d = panel.getPreferredSize();

		ScrollPane scroll = (ScrollPane) getComponent(0);

		if (maxWidth > 0)
			d.width = Math.min(d.width, maxWidth);
		if (maxHeight > 0)
			d.height = Math.min(d.height, maxHeight);

		Insets insets = scroll.getInsets();
		d.width += insets.left + insets.right;
		d.height += insets.top + insets.bottom;

		if (IJ.isMacintosh())
		{
			// This is needed as the OSX scroll pane adds scrollbars when the panel 
			// is close in size to the scroll pane
			int padding = 15;
			d.width += padding;
			d.height += padding;
		}

		scroll.setPreferredSize(d);
		scroll.setSize(d);
		pack();
	}
}
