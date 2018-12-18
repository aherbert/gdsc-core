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

package uk.ac.sussex.gdsc.core.ij;

import ij.ImageListener;
import ij.ImagePlus;

/**
 * An abstract adapter class for receiving image events. The methods in this class are empty. This
 * class exists as a convenience for creating listener objects.
 *
 * <p>Extend this class to create an <code>ImagePlus</code> listener and override the methods for
 * the events of interest. (If you implement the <code>ImageListener</code> interface, you have to
 * define all of the methods in it. This abstract class defines null methods for them all, so you
 * only have to define methods for events you care about.)
 *
 * <p>Create a listener object using the extended class and then register it using
 * {@link ImagePlus#addImageListener(ImageListener)}.
 *
 * @see ImageListener
 *
 * @since 2.0
 */
public abstract class ImageAdapter implements ImageListener {

  @Override
  public void imageOpened(ImagePlus imp) {}

  @Override
  public void imageClosed(ImagePlus imp) {}

  @Override
  public void imageUpdated(ImagePlus imp) {}
}
