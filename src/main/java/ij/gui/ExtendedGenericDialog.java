package ij.gui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import gdsc.core.ij.Utils;

/**
 * Extension of the ij.gui.GenericDialog class to add functionality
 */
public class ExtendedGenericDialog extends GenericDialog
{
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2405780565152258007L;
	private final GridBagLayout grid;

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
		grid = (GridBagLayout) getLayout();
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
		grid = (GridBagLayout) getLayout();
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
		super.addCheckbox(label, defaultValue);
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
		super.addChoice(label, items, defaultItem);
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
		super.addStringField(label, defaultText);
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
		super.addStringField(label, defaultText, columns);
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
		super.addNumericField(label, defaultValue, digits, columns, units);
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
		super.addNumericField(label, defaultValue, digits);
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
		super.addSlider(label, minValue, maxValue, defaultValue);
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

	// TODO add fields as per the normal add methods but have a custom OptionListener interface
	// to call when an options button is pressed. Add a ... button at the end of the row
	// with a mouse over tooltip that calls the option listener

	/**
	 * Invoked to collect options for the field value.
	 */
	public interface OptionListener<T>
	{
		/**
		 * Gets the options.
		 *
		 * @param field
		 *            the field
		 */
		public void collectOptions(T field);
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
	public void addStringField(String label, String defaultText, final OptionListener<TextField> optionListener)
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
	public void addStringField(String label, String defaultText, int columns,
			final OptionListener<TextField> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		TextField tf = addAndGetStringField(label, defaultText, columns);
		GridBagConstraints c = grid.getConstraints(tf);
		remove(tf);

		Button button = new Button("...");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				optionListener.collectOptions(tf);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(tf);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
	}

	/**
	 * Adds the filename field.
	 *
	 * @param label
	 *            the label
	 * @param defaultText
	 *            the default text
	 * @param columns
	 *            the columns
	 * @throws NullPointerException
	 *             if the option lister is null
	 */
	public void addFilenameField(String label, String defaultText, int columns)
	{
		TextField tf = addAndGetStringField(label, defaultText, columns);
		GridBagConstraints c = grid.getConstraints(tf);
		remove(tf);

		Button button = new Button("...");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String filename = Utils.getFilename("Test_file", tf.getText());
				if (filename != null)
					tf.setText(filename);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(tf);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
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
	public void addChoice(String label, String[] items, String defaultItem, final OptionListener<Choice> optionListener)
	{
		if (optionListener == null)
			throw new NullPointerException("Option listener is null");

		Choice choice = addAndGetChoice(label, items, defaultItem);
		GridBagConstraints c = grid.getConstraints(choice);
		remove(choice);

		Button button = new Button("...");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				optionListener.collectOptions(choice);
			}
		});

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(choice);
		panel.add(button);
		grid.setConstraints(panel, c);
		add(panel);
	}
}
