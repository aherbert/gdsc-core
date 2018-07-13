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
package ij.text;

import java.awt.AWTEvent;
import java.awt.CheckboxMenuItem;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.YesNoCancelDialog;
import ij.macro.Interpreter;

/**
 * Uses a TextPanel to displays text in a window.
 * <p>
 * Copied from ij.text.TextWindow. Add functionality to allow the window to be configured before display.
 *
 * @see TextPanel
 */
public class TextWindow2 extends Frame implements ActionListener, FocusListener, ItemListener
{
	private static final long serialVersionUID = -6164933921278234275L;

	static final String FONT_SIZE = "tw.font.size";
	static final String FONT_ANTI = "tw.font.anti";
	TextPanel textPanel;
	CheckboxMenuItem antialiased;
	int[] sizes = { 9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 36, 48, 60, 72 };
	int fontSize = (int) Prefs.get(FONT_SIZE, 5);
	MenuBar mb;

	/**
	 * Opens a new single-column text window.
	 *
	 * @param title
	 *            the title of the window
	 * @param text
	 *            the text initially displayed in the window
	 * @param width
	 *            the width of the window in pixels
	 * @param height
	 *            the height of the window in pixels
	 */
	public TextWindow2(String title, String text, int width, int height)
	{
		this(title, "", text, width, height);
	}

	/**
	 * Opens a new multi-column text window.
	 *
	 * @param title
	 *            title of the window
	 * @param headings
	 *            the tab-delimited column headings
	 * @param text
	 *            text initially displayed in the window
	 * @param width
	 *            width of the window in pixels
	 * @param height
	 *            height of the window in pixels
	 */
	public TextWindow2(String title, String headings, String text, int width, int height)
	{
		super(title);
		textPanel = new TextPanel(title);
		textPanel.setColumnHeadings(headings);
		if (text != null && !text.equals(""))
			textPanel.append(text);
		create(textPanel, width, height);
	}

	/**
	 * Opens a new multi-column text window.
	 *
	 * @param title
	 *            title of the window
	 * @param headings
	 *            tab-delimited column headings
	 * @param text
	 *            ArrayList containing the text to be displayed in the window
	 * @param width
	 *            width of the window in pixels
	 * @param height
	 *            height of the window in pixels
	 */
	public TextWindow2(String title, String headings, ArrayList<String> text, int width, int height)
	{
		super(title);
		textPanel = new TextPanel(title);
		textPanel.setColumnHeadings(headings);
		if (text != null)
			textPanel.append(text);
		create(textPanel, width, height);
	}

	private void create(TextPanel textPanel, int width, int height)
	{
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		if (IJ.isLinux())
			setBackground(ImageJ.backgroundColor);
		add("Center", textPanel);
		addKeyListener(textPanel);
		final ImageJ ij = IJ.getInstance();
		if (ij != null)
		{
			textPanel.addKeyListener(ij);
			if (!IJ.isMacOSX())
			{
				final Image img = ij.getIconImage();
				if (img != null)
					try
					{
						setIconImage(img);
					}
					catch (final Exception e)
					{ // Ignore
					}
			}
		}
		addFocusListener(this);
		addMenuBar();
		setFont();
		WindowManager.addWindow(this);
		setSize(width, height);
		if (!IJ.debugMode)
			GUI.center(this);
		// Do not show upon construction
		//show();
	}

	void addMenuBar()
	{
		mb = new MenuBar();
		if (Menus.getFontSize() != 0)
			mb.setFont(Menus.getFont());
		Menu m = new Menu("File");
		m.add(new MenuItem("Save As...", new MenuShortcut(KeyEvent.VK_S)));
		if (getTitle().equals("Results"))
		{
			m.add(new MenuItem("Rename..."));
			m.add(new MenuItem("Duplicate..."));
		}
		m.addActionListener(this);
		mb.add(m);
		textPanel.fileMenu = m;
		m = new Menu("Edit");
		m.add(new MenuItem("Cut", new MenuShortcut(KeyEvent.VK_X)));
		m.add(new MenuItem("Copy", new MenuShortcut(KeyEvent.VK_C)));
		m.add(new MenuItem("Clear"));
		m.add(new MenuItem("Select All", new MenuShortcut(KeyEvent.VK_A)));
		m.addSeparator();
		m.add(new MenuItem("Find...", new MenuShortcut(KeyEvent.VK_F)));
		m.add(new MenuItem("Find Next", new MenuShortcut(KeyEvent.VK_G)));
		m.addActionListener(this);
		mb.add(m);
		m = new Menu("Font");
		m.add(new MenuItem("Make Text Smaller"));
		m.add(new MenuItem("Make Text Larger"));
		m.addSeparator();
		antialiased = new CheckboxMenuItem("Antialiased", Prefs.get(FONT_ANTI, IJ.isMacOSX() ? true : false));
		antialiased.addItemListener(this);
		m.add(antialiased);
		m.add(new MenuItem("Save Settings"));
		m.addActionListener(this);
		mb.add(m);
		setMenuBar(mb);
	}

	/**
	 * Adds one or lines of text to the window.
	 *
	 * @param text
	 *            The text to be appended. Multiple
	 *            lines should be separated by \n.
	 */
	public void append(String text)
	{
		textPanel.append(text);
	}

	void setFont()
	{
		textPanel.setFont(new Font("SanSerif", Font.PLAIN, sizes[fontSize]), antialiased.getState());
	}

	/**
	 * Returns a reference to this TextWindow's TextPanel.
	 *
	 * @return the text panel
	 */
	public TextPanel getTextPanel()
	{
		return textPanel;
	}

	/**
	 * Appends the text in the specified file to the end of this TextWindow.
	 *
	 * @param in
	 *            the in
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void load(BufferedReader in) throws IOException
	{
		while (true)
		{
			final String s = in.readLine();
			if (s == null)
				break;
			textPanel.appendLine(s);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		final String cmd = evt.getActionCommand();
		if (cmd.equals("Make Text Larger"))
			changeFontSize(true);
		else if (cmd.equals("Make Text Smaller"))
			changeFontSize(false);
		else if (cmd.equals("Save Settings"))
			saveSettings();
		else
			textPanel.doCommand(cmd);
	}

	@Override
	public void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		final int id = e.getID();
		if (id == WindowEvent.WINDOW_CLOSING)
			close();
		else if (id == WindowEvent.WINDOW_ACTIVATED)
			WindowManager.setWindow(this);
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		setFont();
	}

	/**
	 * Closes this TextWindow. Display a "save changes" dialog
	 * if this is the "Results" window.
	 */
	public void close()
	{
		close(true);
	}

	/**
	 * Closes this TextWindow. Display a "save changes" dialog
	 * if this is the "Results" window and 'showDialog' is true.
	 *
	 * @param showDialog
	 *            the show dialog flag
	 */
	public void close(boolean showDialog)
	{
		if (textPanel != null && textPanel.rt != null)
			if (!saveContents())
				return;
		//setVisible(false);
		dispose();
		WindowManager.removeWindow(this);
		textPanel.flush();
	}

	/**
	 * Rename the window.
	 *
	 * @param title
	 *            the title
	 */
	public void rename(String title)
	{
		textPanel.rename(title);
	}

	boolean saveContents()
	{
		int lineCount = textPanel.getLineCount();
		if (!textPanel.unsavedLines)
			lineCount = 0;
		final ImageJ ij = IJ.getInstance();
		final boolean macro = IJ.macroRunning() || Interpreter.isBatchMode();
		final boolean isResults = getTitle().contains("Results");
		if (lineCount > 0 && !macro && ij != null && !ij.quitting() && isResults)
		{
			final YesNoCancelDialog d = new YesNoCancelDialog(this, getTitle(), "Save " + lineCount + " measurements?");
			if (d.cancelPressed())
				return false;
			else if (d.yesPressed())
				if (!textPanel.saveAs(""))
					return false;
		}
		textPanel.rt.reset();
		return true;
	}

	void changeFontSize(boolean larger)
	{
		if (larger)
		{
			fontSize++;
			if (fontSize == sizes.length)
				fontSize = sizes.length - 1;
		}
		else
		{
			fontSize--;
			if (fontSize < 0)
				fontSize = 0;
		}
		IJ.showStatus(sizes[fontSize] + " point");
		setFont();
	}

	void saveSettings()
	{
		Prefs.set(FONT_SIZE, fontSize);
		Prefs.set(FONT_ANTI, antialiased.getState());
		IJ.showStatus("Font settings saved (size=" + sizes[fontSize] + ", antialiased=" + antialiased.getState() + ")");
	}

	@Override
	public void focusGained(FocusEvent e)
	{
		WindowManager.setWindow(this);
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		// Do nothing		
	}
}
