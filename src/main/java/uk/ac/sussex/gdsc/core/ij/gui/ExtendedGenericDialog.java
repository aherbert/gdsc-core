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

package uk.ac.sussex.gdsc.core.ij.gui;

import uk.ac.sussex.gdsc.core.ij.RecorderUtils;
import uk.ac.sussex.gdsc.core.ij.Utils;
import uk.ac.sussex.gdsc.core.utils.TurboList;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;

import java.awt.BorderLayout;
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
import java.awt.MenuComponent;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Extension of the {@link ij.gui.GenericDialog} class to add functionality.
 *
 * <p>This dialog adds all components to a panel that is presented in a {@link java.awt.ScrollPane}.
 * This allows display of large dialogs on small screens.
 */
public class ExtendedGenericDialog extends GenericDialog {
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2405780565152258007L;

  private static final String OPTION_LISTENER_NULL = "Option listener must not be null";

  private static Color buttonBackgroundColor;
  private static int buttonRightBorder;

  static {
    // Try and control the option button appearance across platforms
    buttonBackgroundColor = new Color(240, 240, 240);
    // For some reason an extra 2 pixels looks correct on a mac
    buttonRightBorder = (IJ.isMacOSX()) ? 3 : 1;
  }

  private Component positionComponent = null;

  private TurboList<OptionListener<?>> listeners;

  /** The labels. Used to reset the recorder. */
  private final TurboList<String> labels = new TurboList<>();

  // We capture all components added to the dialog and put them on a single panel
  private GridBagLayout grid;
  private final JPanel panel = new JPanel();

  // Max unscrolled width
  private int maxWidth = 0;
  // Max unscrolled height
  private int maxHeight = 0;

  private JButton lastOptionButton = null;

  /**
   * The silent flag.
   *
   * <p>The call to {@link #setVisible(boolean)} will be ignored.
   */
  private boolean silent;

  /**
   * Instantiates a new extended generic dialog.
   *
   * @param title the title
   * @param parent the parent
   */
  public ExtendedGenericDialog(String title, Frame parent) {
    super(title, parent);
    initialise();
  }

  /**
   * Instantiates a new extended generic dialog.
   *
   * @param title the title
   */
  public ExtendedGenericDialog(String title) {
    super(title);
    initialise();
  }

  private void initialise() {
    grid = (GridBagLayout) getLayout();
    panel.setLayout(grid);

    this.setLayout(new BorderLayout());

    // Use a scroll pane as required
    if (Utils.isShowGenericDialog()) {
      // JScrollPane allows the border to be set to null.
      // However it does not repaint the contents inside the
      // window unless we use a JPanel. Even then some of the
      // java.awt.Panel components are not always redrawn.
      // Given the GenericDialog uses a lot of java.awt components
      // we stick to use a java.awt.ScrollPane.
      // JScrollPane scroll = new JScrollPane(panel)
      // scroll.setBorder(BorderFactory.createEmptyBorder())
      // scroll.getVerticalScrollBar().setUnitIncrement(8)
      // scroll.getHorizontalScrollBar().setUnitIncrement(8)
      // scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE)

      // The ScrollPanel must be sized from the default of 100x100 when
      // displayed. This is done in setup()
      final int policy = ScrollPane.SCROLLBARS_AS_NEEDED;
      final ScrollPane scroll = new ScrollPane(policy);
      scroll.getHAdjustable().setUnitIncrement(16);
      scroll.getVAdjustable().setUnitIncrement(16);
      scroll.add(panel);

      super.add(scroll, BorderLayout.CENTER);
    } else {
      // No need for a scroll pane
      super.add(panel);
    }
  }

  // Record all the field names

  @Override
  public void addCheckbox(String label, boolean defaultValue) {
    labels.add(label);
    super.addCheckbox(label, defaultValue);
  }


  /**
   * Adds the choice.
   *
   * @param label the label
   * @param defaultValue the default value
   * @param optionListener the option listener
   * @throws NullPointerException if the option lister is null
   */
  public void addCheckbox(final String label, boolean defaultValue,
      final OptionListener<Boolean> optionListener) {
    Objects.requireNonNull(optionListener, OPTION_LISTENER_NULL);

    final Checkbox cb = addAndGetCheckbox(label, defaultValue);
    final GridBagConstraints c = grid.getConstraints(cb);
    remove(cb);

    addOptionListener(optionListener);

    final JButton button = createOptionButton();
    button.addActionListener(event -> {
      if (optionListener.collectOptions(cb.getState())) {
        notifyOptionCollectedListeners(label);
      }
    });

    final Panel newPanel = new Panel();
    newPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    newPanel.add(cb);
    newPanel.add(button);
    grid.setConstraints(newPanel, c);
    add(newPanel);
  }

  @Override
  public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues) {
    for (final String label : labels) {
      this.labels.add(label);
    }
    super.addCheckboxGroup(rows, columns, labels, defaultValues);
  }

  @Override
  public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues,
      String[] headings) {
    for (final String label : labels) {
      this.labels.add(label);
    }
    super.addCheckboxGroup(rows, columns, labels, defaultValues, headings);
  }

  @Override
  public void addChoice(String label, String[] items, String defaultItem) {
    labels.add(label);
    super.addChoice(label, items, defaultItem);
  }

  /**
   * Adds the choice.
   *
   * @param label the label
   * @param items the items
   * @param defaultIndex the default index
   * @param optionListener the option listener
   * @throws NullPointerException if the option lister is null
   */
  public void addChoice(String label, String[] items, int defaultIndex,
      final OptionListener<Integer> optionListener) {
    if (defaultIndex < 0 || defaultIndex >= items.length) {
      defaultIndex = 0;
    }
    final String defaultItem = items[defaultIndex];
    addChoice(label, items, defaultItem, optionListener);
  }

  /**
   * Adds the choice.
   *
   * @param label the label
   * @param items the items
   * @param defaultItem the default item
   * @param optionListener the option listener
   * @throws NullPointerException if the option lister is null
   */
  public void addChoice(final String label, String[] items, String defaultItem,
      final OptionListener<Integer> optionListener) {
    Objects.requireNonNull(optionListener, OPTION_LISTENER_NULL);

    final Choice choice = addAndGetChoice(label, items, defaultItem);
    final GridBagConstraints c = grid.getConstraints(choice);
    remove(choice);

    addOptionListener(optionListener);

    final JButton button = createOptionButton();
    button.addActionListener(event -> {
      if (optionListener.collectOptions(choice.getSelectedIndex())) {
        notifyOptionCollectedListeners(label);
      }
    });

    final Panel newPanel = new Panel();
    newPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    newPanel.add(choice);
    newPanel.add(button);
    grid.setConstraints(newPanel, c);
    add(newPanel);
  }

  /**
   * Adds the choice.
   *
   * @param label the label
   * @param items the items
   * @param defaultIndex the default index
   */
  public void addChoice(String label, String[] items, int defaultIndex) {
    if (defaultIndex < 0 || defaultIndex >= items.length) {
      defaultIndex = 0;
    }
    final String defaultItem = items[defaultIndex];
    addChoice(label, items, defaultItem);
  }

  @Override
  public void addNumericField(String label, double defaultValue, int digits) {
    labels.add(label);
    super.addNumericField(label, defaultValue, digits);
  }

  @Override
  public void addNumericField(String label, double defaultValue, int digits, int columns,
      String units) {
    labels.add(label);
    super.addNumericField(label, defaultValue, digits, columns, units);
  }

  /**
   * Adds a numeric field. The first word of the label must be unique or command recording will not
   * work.
   *
   * @param label the label
   * @param defaultValue value to be initially displayed
   * @param digits number of digits to right of decimal point
   * @param optionListener the option listener
   */
  public void addNumericField(String label, double defaultValue, int digits,
      final OptionListener<Double> optionListener) {
    Objects.requireNonNull(optionListener, OPTION_LISTENER_NULL);

    final TextField tf = addAndGetNumericField(label, defaultValue, digits);

    addNumericFieldListener(tf, label, defaultValue, optionListener);
  }

  /**
   * Adds a numeric field. The first word of the label must be unique or command recording will not
   * work.
   *
   * @param label the label
   * @param defaultValue value to be initially displayed
   * @param digits number of digits to right of decimal point
   * @param columns width of field in characters
   * @param units a string displayed to the right of the field
   * @param optionListener the option listener
   */
  public void addNumericField(String label, double defaultValue, int digits, int columns,
      String units, final OptionListener<Double> optionListener) {
    Objects.requireNonNull(optionListener, OPTION_LISTENER_NULL);

    final TextField tf = addAndGetNumericField(label, defaultValue, digits, columns, units);

    addNumericFieldListener(tf, label, defaultValue, optionListener);
  }

  /**
   * Adds a numeric field. The first word of the label must be unique or command recording will not
   * work.
   *
   * @param tf the tf
   * @param label the label
   * @param originalValue value to be initially displayed
   * @param optionListener the option listener
   */
  private void addNumericFieldListener(final TextField tf, final String label,
      final double originalValue, final OptionListener<Double> optionListener) {
    // Numeric fields may have a text field and units in a panel
    final Component lastAdded = getContents().getComponent(getContents().getComponentCount() - 1);

    final GridBagConstraints c = grid.getConstraints(lastAdded);
    remove(lastAdded);

    final String originalText = tf.getText();

    addOptionListener(optionListener);

    final JButton button = createOptionButton();
    button.addActionListener(event -> {
      final String theText = tf.getText();
      Double value;
      if (theText.equals(originalText)) {
        value = originalValue;
      } else {
        value = getValue(theText);
        if (value == null) {
          value = Double.NaN;
        }
      }
      if (optionListener.collectOptions(value)) {
        notifyOptionCollectedListeners(label);
      }
    });

    final Panel newPanel = new Panel();
    newPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    newPanel.add(lastAdded);
    newPanel.add(button);
    grid.setConstraints(newPanel, c);
    add(newPanel);
  }

  @Override
  public void addSlider(String label, double minValue, double maxValue, double defaultValue) {
    labels.add(label);
    super.addSlider(label, minValue, maxValue, defaultValue);
  }

  /**
   * Adds a slider (scroll bar) to the dialog box. Floating point values will be used if
   * (maxValue-minValue)&lt;=5.0 and either minValue or maxValue are non-integer.
   *
   * @param label the label
   * @param minValue the minimum value of the slider
   * @param maxValue the maximum value of the slider
   * @param defaultValue the initial value of the slider
   * @param optionListener the option listener
   */
  public void addSlider(final String label, double minValue, double maxValue, double defaultValue,
      final OptionListener<Double> optionListener) {
    Objects.requireNonNull(optionListener, OPTION_LISTENER_NULL);

    if (defaultValue < minValue) {
      defaultValue = minValue;
    }
    if (defaultValue > maxValue) {
      defaultValue = maxValue;
    }

    addSlider(label, minValue, maxValue, defaultValue);
    final Panel p = getLastPanel();

    final TextField tf = new ComponentFinder<>(p, TextField.class).getLast();
    final String originalText = tf.getText();
    final double originalValue = defaultValue;

    addOptionListener(optionListener);

    final JButton button = createOptionButton();
    button.addActionListener(event -> {
      final String theText = tf.getText();
      Double value;
      if (theText.equals(originalText)) {
        value = originalValue;
      } else {
        value = convertToDouble(theText);
      }
      if (optionListener.collectOptions(value)) {
        notifyOptionCollectedListeners(label);
      }
    });

    final GridBagConstraints pc = new GridBagConstraints();
    pc.gridy = 0;
    pc.gridx = 2;
    pc.insets = new Insets(5, 5, 0, 0);
    pc.anchor = GridBagConstraints.EAST;
    p.add(button, pc);
  }

  /**
   * Adds a slider (scroll bar) to the dialog box. Floating point values will be used if
   * (maxValue-minValue)&lt;=5.0 and either minValue or maxValue are non-integer.
   *
   * <p>Update the min or max value to include the default value if it is outside the slider range.
   *
   * @param label the label
   * @param minValue the minimum value of the slider
   * @param maxValue the maximum value of the slider
   * @param defaultValue the initial value of the slider
   */
  public void addSliderIncludeDefault(String label, double minValue, double maxValue,
      double defaultValue) {
    labels.add(label);
    if (Double.isFinite(defaultValue)) {
      if (defaultValue < minValue) {
        minValue = defaultValue;
      }
      if (defaultValue > maxValue) {
        maxValue = defaultValue;
      }
    }
    super.addSlider(label, minValue, maxValue, defaultValue);
  }

  @Override
  public void addStringField(String label, String defaultText) {
    labels.add(label);
    super.addStringField(label, defaultText);
  }

  @Override
  public void addStringField(String label, String defaultText, int columns) {
    labels.add(label);
    super.addStringField(label, defaultText, columns);
  }

  /**
   * Adds the string field.
   *
   * @param label the label
   * @param defaultText the default text
   * @param optionListener the option listener
   * @throws NullPointerException if the option lister is null
   */
  public void addStringField(String label, String defaultText,
      final OptionListener<String> optionListener) {
    addStringField(label, defaultText, 8, optionListener);
  }

  /**
   * Adds the string field.
   *
   * @param label the label
   * @param defaultText the default text
   * @param columns the columns
   * @param optionListener the option listener
   * @throws NullPointerException if the option lister is null
   */
  public void addStringField(final String label, String defaultText, int columns,
      final OptionListener<String> optionListener) {
    Objects.requireNonNull(optionListener, OPTION_LISTENER_NULL);

    final TextField tf = addAndGetStringField(label, defaultText, columns);
    final GridBagConstraints c = grid.getConstraints(tf);
    remove(tf);

    addOptionListener(optionListener);

    final JButton button = createOptionButton();
    button.addActionListener(event -> {
      if (optionListener.collectOptions(tf.getText())) {
        notifyOptionCollectedListeners(label);
      }
    });

    final Panel newPanel = new Panel();
    newPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    newPanel.add(tf);
    newPanel.add(button);
    grid.setConstraints(newPanel, c);
    add(newPanel);
  }

  @Override
  public void addTextAreas(String text1, String text2, int rows, int columns) {
    labels.add("text1");
    if (text2 != null) {
      labels.add("text2");
    }
    super.addTextAreas(text1, text2, rows, columns);
  }

  /**
   * Gets the last label added to the dialog.
   *
   * @return the last label
   */
  public Label getLastLabel() {
    return new ComponentFinder<>(getContents(), Label.class).getLast();
  }

  /**
   * Gets the last getContents() added to the dialog.
   *
   * @return the last getContents()
   */
  public Panel getLastPanel() {
    return new ComponentFinder<>(getContents(), Panel.class).getLast();
  }

  /**
   * Gets the last choice added to the dialog.
   *
   * @return the last choice
   */
  public Choice getLastChoice() {
    return new ComponentFinder<>(getContents(), Choice.class).getLast();
  }

  /**
   * Gets the last text field added to the dialog.
   *
   * @return the last text field
   */
  public TextField getLastTextField() {
    return new ComponentFinder<>(getContents(), TextField.class).getLast();
  }

  /**
   * Gets the last checkbox added to the dialog.
   *
   * @return the last checkbox
   */
  public Checkbox getLastCheckbox() {
    return new ComponentFinder<>(getContents(), Checkbox.class).getLast();
  }

  /**
   * Gets the last scrollbar added to the dialog.
   *
   * @return the last scrollbar
   */
  public Scrollbar getLastScrollbar() {
    return new ComponentFinder<>(getContents(), Scrollbar.class).getLast();
  }

  private Container getContents() {
    // We may not want to put all contents into a single panel
    return panel;
  }

  /**
   * Simple generic component finder to traverse the dialog looking for the last component of a
   * given type.
   *
   * @param <T> the generic type
   */
  private static class ComponentFinder<T> {
    Container container;
    Class<T> type;

    ComponentFinder(Container container, Class<T> type) {
      this.container = container;
      this.type = type;
    }

    T getLast() {
      return getLast(container);
    }

    T getLast(Container container) {
      int count = container.getComponentCount();
      while (count-- > 0) {
        final Component component = container.getComponent(count);
        if (type.isInstance(component)) {
          return type.cast(component);
        }
        if (component instanceof Container) {
          // Traverse containers
          final T object = getLast((Container) component);
          if (object != null) {
            return object;
          }
        }
      }
      return null;
    }
  }

  /**
   * Adds and then gets a checkbox.
   *
   * @param label the label
   * @param defaultValue the default value
   * @return the checkbox
   */
  public Checkbox addAndGetCheckbox(String label, boolean defaultValue) {
    addCheckbox(label, defaultValue);
    return (Checkbox) tail(getCheckboxes());
  }

  /**
   * Get the last object in the vector.
   *
   * @param vector the vector
   * @return the object
   */
  private static Object tail(Vector<?> vector) {
    return vector.get(vector.size() - 1);
  }

  /**
   * Adds and then gets a choice.
   *
   * @param label the label
   * @param items the items
   * @param defaultItem the default item
   * @return the choice
   */
  public Choice addAndGetChoice(String label, String[] items, String defaultItem) {
    addChoice(label, items, defaultItem);
    return (Choice) tail(getChoices());
  }

  /**
   * Adds and then gets a choice.
   *
   * @param label the label
   * @param items the items
   * @param defaultIndex the default index
   * @return the choice
   */
  public Choice addAndGetChoice(String label, String[] items, int defaultIndex) {
    addChoice(label, items, defaultIndex);
    return (Choice) tail(getChoices());
  }

  /**
   * Adds and then gets a string field.
   *
   * @param label the label
   * @param defaultText the default text
   * @return the text field
   */
  public TextField addAndGetStringField(String label, String defaultText) {
    addStringField(label, defaultText);
    return (TextField) tail(getStringFields());
  }

  /**
   * Adds and then gets a string field.
   *
   * @param label the label
   * @param defaultText the default text
   * @param columns the columns
   * @return the text field
   */
  public TextField addAndGetStringField(String label, String defaultText, int columns) {
    addStringField(label, defaultText, columns);
    return (TextField) tail(getStringFields());
  }

  /**
   * Adds and then gets a numeric field.
   *
   * @param label the label
   * @param defaultValue the default value
   * @param digits the digits
   * @param columns the columns
   * @param units the units
   * @return the text field
   */
  public TextField addAndGetNumericField(String label, double defaultValue, int digits, int columns,
      String units) {
    addNumericField(label, defaultValue, digits, columns, units);
    return (TextField) tail(getNumericFields());
  }

  /**
   * Adds and then gets a numeric field.
   *
   * @param label the label
   * @param defaultValue the default value
   * @param digits the digits
   * @return the text field
   */
  public TextField addAndGetNumericField(String label, double defaultValue, int digits) {
    addNumericField(label, defaultValue, digits);
    return (TextField) tail(getNumericFields());
  }

  /**
   * Adds and then gets the text field associated with the slider.
   *
   * @param label the label
   * @param minValue the min value
   * @param maxValue the max value
   * @param defaultValue the default value
   * @return the text field
   */
  public TextField addAndGetSlider(String label, double minValue, double maxValue,
      double defaultValue) {
    addSlider(label, minValue, maxValue, defaultValue);
    return (TextField) tail(getNumericFields());
  }

  /**
   * Adds the button.
   *
   * @param label the label
   * @param actionListener the action listener (must not be null)
   * @return the button
   * @throws NullPointerException if the action listener is null
   */
  public Button addAndGetButton(String label, final ActionListener actionListener) {
    if (actionListener == null) {
      throw new NullPointerException("Action listener is missing for the button");
    }

    // To make room for the button we add a message and then remove that from the dialog
    addMessage("-- Empty --");

    // Get the message and 'steal' the constraints so we get the current row
    final Label msg = (Label) getMessage();
    final GridBagConstraints c = grid.getConstraints(msg);

    // Remove the dummy message
    remove(msg);

    // Add a button
    final Panel buttons = new Panel();
    buttons.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    final Button button = new Button(label);
    button.addActionListener(actionListener);
    buttons.add(button);
    c.gridx = 1;
    // c.gridy = y; // Same as the message label we removed
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
   *
   * @param <T> the generic type
   */
  public interface OptionListener<T> {
    /**
     * Gets the options using the current value of the field.
     *
     * @param value the field value
     * @return true, if new options were collected
     */
    public boolean collectOptions(T value);

    /**
     * Gets the options using the previously read value of the field.
     *
     * <p>This will be called when the parent field is read using the appropriate getNext(...)
     * method. It allows macros to be supported by either recording the options in the Recorder or
     * reading the options from the Macro options. The simple implementation is to construct an
     * ExtendedGenericDialog to collect the options and set the silent flag to true. The dialog will
     * not be presented in the showDialog() method and the method can proceed direct to reading the
     * fields.
     *
     * @return true, if new options were collected
     */
    public boolean collectOptions();
  }

  /**
   * An event generated when options are collected.
   */
  public class OptionCollectedEvent {
    private final String label;

    /**
     * Instantiates a new option collected event.
     *
     * @param label the label
     */
    public OptionCollectedEvent(String label) {
      this.label = label;
    }

    /**
     * Gets the label of the field for which options were collected.
     *
     * @return the label
     */
    public String getLabel() {
      return label;
    }
  }

  /**
   * The listener interface for receiving optionCollected events. The class that is interested in
   * processing a optionCollected event implements this interface, and the object created with that
   * class is registered with a component using the component's
   * <code>addOptionCollectedListener</code> method. When the optionCollected event occurs, that
   * object's appropriate method is invoked.
   *
   * @see OptionCollectedEvent
   */
  public interface OptionCollectedListener {

    /**
     * Called if options were collected.
     *
     * @param event the event
     */
    public void optionCollected(OptionCollectedEvent event);
  }

  private TurboList<OptionCollectedListener> optionCollectedListeners = null;

  /**
   * Adds the option collected listener.
   *
   * @param listener the listener
   */
  public void addOptionCollectedListener(OptionCollectedListener listener) {
    if (optionCollectedListeners == null) {
      optionCollectedListeners = new TurboList<>();
    }
    optionCollectedListeners.add(listener);
  }

  private void notifyOptionCollectedListeners(String label) {
    if (optionCollectedListeners == null) {
      return;
    }
    final OptionCollectedEvent e = new OptionCollectedEvent(label);
    for (int i = 0; i < optionCollectedListeners.size(); i++) {
      optionCollectedListeners.getf(i).optionCollected(e);
    }
  }

  private JButton createOptionButton(String tooltip) {
    lastOptionButton = new JButton("...");
    lastOptionButton.setBackground(buttonBackgroundColor);
    lastOptionButton.setToolTipText(tooltip);
    lastOptionButton
        .setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(1),
            BorderFactory.createEmptyBorder(1, 1, 1, buttonRightBorder)));
    return lastOptionButton;
  }

  private JButton createOptionButton() {
    return createOptionButton("Extra options");
  }

  /**
   * Gets a reference to the most recently added option button.
   *
   * @return the last option button
   */
  public JButton getLastOptionButton() {
    return lastOptionButton;
  }

  private int addOptionListener(OptionListener<?> optionListener) {
    if (listeners == null) {
      listeners = new TurboList<>();
    }
    final int id = listeners.size();
    listeners.add(optionListener);
    return id;
  }

  /**
   * Adds the filename field.
   *
   * <p>Note that if the filename is empty then the open dialog within the action listener will use
   * ij.io.OpenDialog.getDefaultDirectory(). This can be changed using
   * ij.io.OpenDialog.setDefaultDirectory(String).
   *
   * @param label the label
   * @param defaultText the default filename
   * @return the text field
   * @throws NullPointerException if the option lister is null
   */
  public TextField addFilenameField(String label, String defaultText) {
    return addFilenameField(label, defaultText, 30);
  }

  /**
   * Adds the filename field.
   *
   * <p>Note that if the filename is empty then the open dialog within the action listener will use
   * ij.io.OpenDialog.getDefaultDirectory(). This can be changed using
   * ij.io.OpenDialog.setDefaultDirectory(String).
   *
   * @param label the label
   * @param defaultText the default filename
   * @param columns the columns
   * @return the text field
   * @throws NullPointerException if the option lister is null
   */
  public TextField addFilenameField(final String label, String defaultText, int columns) {
    final TextField tf = addAndGetStringField(label, defaultText, columns);
    final GridBagConstraints c = grid.getConstraints(tf);
    remove(tf);

    final JButton button = createOptionButton("Select a file");
    button.addActionListener(event -> {
      final boolean record = Recorder.record;
      final String filename = Utils.getFilename(label, tf.getText());
      Recorder.record = record;
      if (filename != null) {
        tf.setText(filename);
      }
    });

    final Panel newPanel = new Panel();
    newPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    newPanel.add(tf);
    newPanel.add(button);
    grid.setConstraints(newPanel, c);
    add(newPanel);

    return tf;
  }

  /**
   * Adds the directory field.
   *
   * @param label the label
   * @param defaultText the default directory
   * @return the text field
   * @throws NullPointerException if the option lister is null
   */
  public TextField addDirectoryField(String label, String defaultText) {
    return addDirectoryField(label, defaultText, 30);
  }

  /**
   * Adds the directory field.
   *
   * @param label the label
   * @param defaultText the default directory
   * @param columns the columns
   * @return the text field
   * @throws NullPointerException if the option lister is null
   */
  public TextField addDirectoryField(final String label, String defaultText, int columns) {
    final TextField tf = addAndGetStringField(label, defaultText, columns);
    final GridBagConstraints c = grid.getConstraints(tf);
    remove(tf);

    final JButton button = createOptionButton("Select a directory");
    button.addActionListener(event -> {
      final boolean record = Recorder.record;
      final String filename = Utils.getDirectory(label, tf.getText());
      Recorder.record = record;
      if (filename != null) {
        tf.setText(filename);
      }
    });

    final Panel newPanel = new Panel();
    newPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    newPanel.add(tf);
    newPanel.add(button);
    grid.setConstraints(newPanel, c);
    add(newPanel);

    return tf;
  }

  private Double convertToDouble(String theText) {
    // This catches any number format exceptions
    final Double value = getValue(theText);
    if (value == null) {
      // return NaN and let the downstream code handle this
      return Double.NaN;
    }
    return value;
  }


  /**
   * Reset the recorder for all the named fields that have been added to the dialog. This should be
   * called if the dialog is to be reused as repeat calls to getNext(...) for fields with the same
   * name will cause ImageJ to show a duplicate field error.
   */
  public void resetRecorder() {
    RecorderUtils.resetRecorder(labels.toArray(new String[labels.size()]));
  }

  /**
   * Show the dialog.
   *
   * @param resetRecorder Set to true to reset the recorder for all the named fields that have been
   *        added to the dialog. Ignored if the Recorder is not enabled.
   * @see ij.gui.GenericDialog#showDialog()
   */
  public void showDialog(boolean resetRecorder) {
    if (resetRecorder && Recorder.record) {
      resetRecorder();
    }
    showDialog();
  }

  /**
   * Show the dialog.
   *
   * @param resetRecorder Set to true to reset the recorder for all the named fields that have been
   *        added to the dialog. Ignored if the Recorder is not enabled.
   * @param positionComponent Sets the component that will be used to position this dialog
   * @see ij.gui.GenericDialog#showDialog()
   */
  public void showDialog(boolean resetRecorder, Component positionComponent) {
    setPositionComponent(positionComponent);
    showDialog(resetRecorder);
  }

  /**
   * Sets the component that will be used to position this dialog. See
   * {@link #setLocationRelativeTo(Component)}.
   *
   * @param component the new position component
   */
  public void setPositionComponent(Component component) {
    positionComponent = component;
  }

  @Override
  public void setVisible(boolean value) {
    if (silent) {
      return;
    }

    // Allow positioning relative to a parent component
    if (positionComponent != null && positionComponent.isVisible()) {
      setLocationRelativeTo(positionComponent);
    }

    super.setVisible(value);
  }

  /**
   * Collect the options from all the option listeners silently. Calls all the listeners since the
   * value may have been changed since they were last called interactively.
   *
   * <p>This should be called after all the fields have been read. This allows the fields to be read
   * correctly from Macro option arguments. It also allows the options to be recorded to the
   * Recorder.
   *
   * <p>This method does nothing if the Recorder is disabled or this is not running in a macro, i.e.
   * there is no point collecting options again.
   */
  public void collectOptions() {
    if (listeners == null) {
      return;
    }
    if (!(Recorder.record || Utils.isMacro())) {
      return;
    }
    for (int i = 0; i < listeners.size(); i++) {
      listeners.getf(i).collectOptions();
    }
  }

  /**
   * Gets the max width before a scroll pane is used.
   *
   * @return the max width
   */
  public int getMaxUnscrolledWidth() {
    return maxWidth;
  }

  /**
   * Sets the max width before a scroll pane is used.
   *
   * @param maxWidth the new max width
   */
  public void setMaxUnscrolledWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  /**
   * Gets the max height before a scroll pane is used.
   *
   * @return the max height
   */
  public int getMaxUnscrolledHeight() {
    return maxHeight;
  }

  /**
   * Sets the max height before a scroll pane is used.
   *
   * @param maxHeight the new max height
   */
  public void setMaxUnscrolledHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  /**
   * Sets the max size before a scroll pane is used.
   *
   * @param maxWidth the max width
   * @param maxHeight the max height
   */
  public void setMaxUnscrolledSize(int maxWidth, int maxHeight) {
    setMaxUnscrolledWidth(maxWidth);
    setMaxUnscrolledHeight(maxHeight);
  }

  // Methods used within GenericDialog to add components to the container.
  // These are overridden so that we can add components to a single panel.

  @Override
  public Component add(Component comp) {
    return panel.add(comp);
  }

  @Override
  public void add(Component comp, Object constraints) {
    panel.add(comp, constraints);
  }

  @Override
  public Component add(Component comp, int index) {
    return panel.add(comp, index);
  }

  @Override
  public Component add(String name, Component comp) {
    return panel.add(name, comp);
  }

  @Override
  public void add(Component comp, Object constraints, int index) {
    panel.add(comp, constraints, index);
  }

  @Override
  public void add(PopupMenu popup) {
    panel.add(popup);
  }

  @Override
  public void remove(Component comp) {
    panel.remove(comp);
  }

  @Override
  public void remove(int index) {
    panel.remove(index);
  }

  @Override
  public void remove(MenuComponent popup) {
    panel.remove(popup);
  }

  /**
   * This method is called just before the dialog is set visible. Determine the preferred size of
   * the panel contents and appropriately size the scroll pane to fit.
   *
   * @see ij.gui.GenericDialog#setup()
   */
  @Override
  protected void setup() {
    if (!Utils.isShowGenericDialog()) {
      return;
    }

    // Appropriately size the scrollpane
    final ScreenDimensionHelper helper = new ScreenDimensionHelper();
    if (maxWidth > 0) {
      helper.setMaxWidth(maxWidth);
    }
    if (maxHeight > 0) {
      helper.setMaxHeight(maxHeight);
    }

    final Dimension d = panel.getPreferredSize();
    final ScrollPane scroll = (ScrollPane) getComponent(0);

    helper.setup(scroll, d);

    pack();
  }

  /**
   * Checks for fields.
   *
   * @return true, if successful
   */
  public boolean hasFields() {
    if (getNumericFields() != null) {
      return true;
    }
    if (getStringFields() != null) {
      return true;
    }
    if (getCheckboxes() != null) {
      return true;
    }
    if (getChoices() != null) {
      return true;
    }
    if (getSliders() != null) {
      return true;
    }
    if (getRadioButtonGroups() != null) {
      return true;
    }
    if (getTextArea1() != null) {
      return true;
    }
    if (getTextArea2() != null) {
      return true;
    }
    return (getMessage() != null);
  }

  @Override
  public void setLocation(Point location) {
    if (location != null) {
      setLocation(location.x, location.y);
    }
  }

  /**
   * Sets the silent flag. The call to {@link #setVisible(boolean)} will be ignored.
   *
   * <p>Use this to not show the dialog in a call to {@link #showDialog()}. However the dialog can
   * still be read and will record in macros if necessary.
   *
   * @param silent the new silent flag
   */
  public void setSilent(boolean silent) {
    this.silent = silent;
  }
}
