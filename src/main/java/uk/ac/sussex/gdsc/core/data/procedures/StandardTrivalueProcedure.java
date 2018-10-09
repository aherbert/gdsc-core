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
package uk.ac.sussex.gdsc.core.data.procedures;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


/**
 * Standard implementation for accessing a value in three dimensions.
 */
public class StandardTrivalueProcedure implements TrivalueProcedure {
  /** The x axis values. */
  public double[] x;

  /** The y axis values. */
  public double[] y;

  /** The z axis values. */
  public double[] z;

  /** The value. */
  public double[][][] value;

  /** {@inheritDoc} */
  @Override
  public boolean setDimensions(int maxx, int maxy, int maxz) {
    x = new double[maxx];
    y = new double[maxy];
    z = new double[maxz];
    value = new double[maxx][maxy][maxz];
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void setX(int i, double value) {
    x[i] = value;
  }

  /** {@inheritDoc} */
  @Override
  public void setY(int j, double value) {
    y[j] = value;
  }

  /** {@inheritDoc} */
  @Override
  public void setZ(int k, double value) {
    z[k] = value;
  }

  /** {@inheritDoc} */
  @Override
  public void setValue(int i, int j, int k, double value) {
    this.value[i][j][k] = value;
  }
}
