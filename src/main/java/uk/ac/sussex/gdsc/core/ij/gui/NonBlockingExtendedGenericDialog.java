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

import ij.IJ;
import ij.WindowManager;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * This is an extension of {@link uk.ac.sussex.gdsc.core.ij.gui.ExtendedGenericDialog} that is
 * non-model.
 *
 * <p>The implementation is a copy of {@link ij.gui.NonBlockingGenericDialog}.
 */
public class NonBlockingExtendedGenericDialog extends ExtendedGenericDialog {
  private static final long serialVersionUID = 8535959215385211516L;

  private boolean closed;

  /**
   * Instantiates a new non blocking extended generic dialog.
   *
   * @param title the title
   */
  public NonBlockingExtendedGenericDialog(String title) {
    super(title, null);
    setModal(false);
  }

  @Override
  public synchronized void showDialog() {
    super.showDialog();
    if (isMacro()) {
      return;
    }
    if (!IJ.macroRunning()) { // add to Window menu on event dispatch thread
      final NonBlockingExtendedGenericDialog thisDialog = this;
      EventQueue.invokeLater(() -> WindowManager.addWindow(thisDialog));
    }
    while (!closed) {
      try {
        wait();
      } catch (final InterruptedException ex) {
        // Restore interrupted state...
        Thread.currentThread().interrupt();
        // Ignore exception
      }
    }
  }

  @Override
  public synchronized void actionPerformed(ActionEvent event) {
    super.actionPerformed(event);
    if (!isVisible()) {
      closed = true;
      notifyAll();
    }
  }

  @Override
  public synchronized void keyPressed(KeyEvent event) {
    super.keyPressed(event);
    if (wasOKed() || wasCanceled()) {
      closed = true;
      notifyAll();
    }
  }

  @Override
  public synchronized void windowClosing(WindowEvent event) {
    super.windowClosing(event);
    if (wasOKed() || wasCanceled()) {
      closed = true;
      notifyAll();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    WindowManager.removeWindow(this);
  }
}
