/*-
 * #%L
 * Genome Damage and Stability Centre SMLM ImageJ Plugins
 *
 * Software for single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2019 Alex Herbert
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
/*
 *
 */

package uk.ac.sussex.gdsc.core.ij.gui;

import gnu.trove.list.array.TIntArrayList;
import ij.IJ;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GUI;
import ij.macro.Interpreter;
import ij.plugin.frame.Recorder;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Shows a list dialog allowing multiple items to be selected.
 */
public class MultiDialog extends Dialog {
  private static final long serialVersionUID = 20190625L;

  /**
   * Maximum number of items to show in the displayed list.
   */
  private static final int MAX_SIZE = 30;

  private List<String> selected;
  private boolean selectAll;

  private Button cancel;
  private Button okay;
  private Button all;
  private Button none;
  private boolean wasCanceled;
  private JList<String> list;
  private final String macroOptions;
  private final boolean macro;

  private final List<String> items;
  private transient Function<String, String> displayConverter = Function.identity();

  /**
   * The modifiers captured in from {@link MouseListener#mouseClicked(MouseEvent)}.
   */
  protected int modifiers;

  /**
   * The last event from {@link ItemEvent#getStateChange()} captured in
   * {@link ItemListener#itemStateChanged(ItemEvent)}.
   */
  protected int lastEvent = -1;

  private transient LocalKeyAdapter keyAdapter = new LocalKeyAdapter();
  private transient LocalWindowAdpater windowAdpater = new LocalWindowAdpater();
  private transient LocalMouseAdpater mouseAdpater = new LocalMouseAdpater();

  private class LocalKeyAdapter extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent event) {
      final int keyCode = event.getKeyCode();
      IJ.setKeyDown(keyCode);
      if (keyCode == KeyEvent.VK_ENTER) {
        final Object source = event.getSource();
        if (source == okay || source == cancel || source == list) {
          wasCanceled = source == cancel;
          dispose();
        } else if (source == all) {
          list.setSelectionInterval(0, items.size() - 1);
        } else if (source == none) {
          list.clearSelection();
        }
      } else if (keyCode == KeyEvent.VK_ESCAPE) {
        wasCanceled = true;
        dispose();
        IJ.resetEscape();
      } else if (keyCode == KeyEvent.VK_W
          && (event.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
        wasCanceled = true;
        dispose();
      }
    }
  }

  private class LocalWindowAdpater extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent event) {
      wasCanceled = true;
      dispose();
    }
  }

  private class LocalMouseAdpater extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent event) {
      modifiers = event.getModifiers();
    }
  }

  /**
   * Instantiates a new multi dialog.
   *
   * @param title the title
   * @param items the items
   */
  public MultiDialog(String title, List<String> items) {
    super(getDialogOwner(), title, true);
    addKeyListener(keyAdapter);
    addWindowListener(windowAdpater);
    macroOptions = Macro.getOptions();
    macro = macroOptions != null;
    this.items = items;
  }

  private static Frame getDialogOwner() {
    if (WindowManager.getCurrentImage() != null) {
      return WindowManager.getCurrentImage().getWindow();
    }
    return IJ.getInstance();
  }

  /**
   * Sets the list of selected items.
   *
   * @param selected the selected
   */
  public void setSelected(List<String> selected) {
    this.selected = selected;
  }

  /**
   * Sets the display converter. This is used to convert the text in the list of items into the text
   * displayed in the dialog. It can be used to decorate the underlying text items.
   *
   * @param displayConverter the display converter
   */
  public void setDisplayConverter(Function<String, String> displayConverter) {
    this.displayConverter = displayConverter;
  }

  /**
   * Checks if select all items in the list.
   *
   * @return true, if is select all
   */
  public boolean isSelectAll() {
    return selectAll;
  }

  /**
   * Sets the select all flag.
   *
   * @param selectAll Set to true to select all items in the list
   */
  public void setSelectAll(boolean selectAll) {
    this.selectAll = selectAll;
  }

  /**
   * Show the dialog.
   */
  public void showDialog() {
    // Detect if running in a macro and just collect the input options
    if (macro) {
      dispose();
    } else {
      add(buildPanel());
      this.addKeyListener(keyAdapter);
      if (IJ.isMacintosh()) {
        setResizable(false);
      }
      pack();
      GUI.center(this);
      setVisible(true);
      IJ.wait(50); // work around for Sun/WinNT bug
    }
  }

  /**
   * Builds the main panel for the dialog.
   *
   * @return the panel
   */
  protected Panel buildPanel() {
    final Panel p = new Panel();
    final BorderLayout layout = new BorderLayout();
    layout.setVgap(3);
    p.setLayout(layout);
    p.add(buildResultsList(), BorderLayout.NORTH, 0);
    p.add(buildButtonPanel(), BorderLayout.CENTER, 1);
    return p;
  }

  /**
   * Builds the results list component for the dialog.
   *
   * @return the component
   */
  protected Component buildResultsList() {
    final int size = items.size();
    list = new JList<>(items.toArray(new String[0]));
    list.setVisibleRowCount(Math.min(size, MAX_SIZE));

    list.setCellRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public void setText(String text) {
        super.setText(MultiDialog.this.mapToDisplay(text));
      }
    });

    // Initial selection
    if (selectAll) {
      list.setSelectionInterval(0, items.size() - 1);
    } else if (selected != null) {
      // Selection must be done all at once.
      final TIntArrayList indices = new TIntArrayList();
      for (final String item : selected) {
        final int index = items.indexOf(item);
        if (index != -1) {
          indices.add(index);
        }
      }
      list.setSelectedIndices(indices.toArray());
    }

    list.addMouseListener(mouseAdpater);
    list.addKeyListener(keyAdapter);

    return list;
  }

  /**
   * Map the text in the list to display text.
   *
   * @param value the value
   * @return the display value
   */
  private String mapToDisplay(String value) {
    return displayConverter.apply(value);
  }

  /**
   * Builds the button panel for the dialog.
   *
   * @return the panel
   */
  protected Panel buildButtonPanel() {
    final Panel buttons = new Panel();
    buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
    all = new Button("All");
    all.addActionListener(this::actionPerformed);
    all.addKeyListener(keyAdapter);
    buttons.add(all);
    none = new Button("None");
    none.addActionListener(this::actionPerformed);
    none.addKeyListener(keyAdapter);
    buttons.add(none);
    okay = new Button("OK");
    okay.addActionListener(this::actionPerformed);
    okay.addKeyListener(keyAdapter);
    buttons.add(okay);
    cancel = new Button("Cancel");
    cancel.addActionListener(this::actionPerformed);
    cancel.addKeyListener(keyAdapter);
    buttons.add(cancel);
    return buttons;
  }

  private void actionPerformed(ActionEvent event) {
    final Object source = event.getSource();
    if (source == okay || source == cancel) {
      wasCanceled = source == cancel;
      dispose();
    } else if (source == all) {
      list.setSelectionInterval(0, items.size() - 1);
    } else if (source == none) {
      list.clearSelection();
    }
  }

  /**
   * Check if the dialog was cancelled.
   *
   * @return true, if cancelled
   */
  public boolean wasCancelled() {
    return wasCanceled;
  }

  /**
   * Gets the selected results from the dialog.
   *
   * @return the selected results
   */
  public List<String> getSelectedResults() {
    ArrayList<String> selectedResults;

    // Get the selected names
    if (macro) {
      selectedResults = new ArrayList<>();
      String name = getValue("input");
      while (name != null) {
        selectedResults.add(name);
        name = getValue("input" + selectedResults.size());
      }
    } else {
      final int[] listIndexes = list.getSelectedIndices();
      selectedResults = new ArrayList<>(listIndexes.length);
      if (listIndexes.length > 0) {
        for (final int index : listIndexes) {
          selectedResults.add(items.get(index));
        }
      }
    }

    // Record for macros
    if (((macro && Recorder.record && Recorder.recordInMacros) || Recorder.record)
        && !selectedResults.isEmpty()) {
      Recorder.recordOption("Input", selectedResults.get(0));
      if (selectedResults.size() > 1) {
        for (int n = 1; n < selectedResults.size(); ++n) {
          Recorder.recordOption("Input" + n, selectedResults.get(n));
        }
      }
    }

    return selectedResults;
  }

  /**
   * Get a value from the macro options. Adapted from ij.gui.GenericDialog.
   *
   * @param label the label
   * @return The value (or null)
   */
  private String getValue(String label) {
    String theText = Macro.getValue(macroOptions, label, null);
    if (theText != null
        && (theText.startsWith("&") || label.toLowerCase(Locale.US).startsWith(theText))) {
      // Is the value a macro variable?
      if (theText.startsWith("&")) {
        theText = theText.substring(1);
      }
      final Interpreter interp = Interpreter.getInstance();
      final String s = interp != null ? interp.getVariableAsString(theText) : null;
      if (s != null) {
        theText = s;
      }
    }
    return theText;
  }

  /**
   * Custom support for Serializable.
   *
   * @param in the object input stream
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    // Create all instance fields
    displayConverter = Function.identity();
    keyAdapter = new LocalKeyAdapter();
    windowAdpater = new LocalWindowAdpater();
    mouseAdpater = new LocalMouseAdpater();
  }
}
