package ij.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

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

import ij.IJ;
import ij.WindowManager;

/**
 * This is an extension of ExtendedGenericDialog that is non-model.
 * <p>
 * The implementation is a copy of ij.gui.NonBlockingDialog.
 */
public class NonBlockingExtendedGenericDialog extends ExtendedGenericDialog
{
	private static final long serialVersionUID = 8535959215385211516L;

	public NonBlockingExtendedGenericDialog(String title)
	{
		super(title, null);
		setModal(false);
	}

	public synchronized void showDialog()
	{
		super.showDialog();
		if (isMacro())
			return;
		if (!IJ.macroRunning())
		{ // add to Window menu on event dispatch thread
			final NonBlockingExtendedGenericDialog thisDialog = this;
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					WindowManager.addWindow(thisDialog);
				}
			});
		}
		try
		{
			wait();
		}
		catch (InterruptedException e)
		{
		}
	}

	public synchronized void actionPerformed(ActionEvent e)
	{
		super.actionPerformed(e);
		if (!isVisible())
			notify();
	}

	public synchronized void keyPressed(KeyEvent e)
	{
		super.keyPressed(e);
		if (wasOKed() || wasCanceled())
			notify();
	}

	public synchronized void windowClosing(WindowEvent e)
	{
		super.windowClosing(e);
		if (wasOKed() || wasCanceled())
			notify();
	}

	public void dispose()
	{
		super.dispose();
		WindowManager.removeWindow(this);
	}
}
