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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class StandardTrivalueProcedureTest {
  @Test
  void testProcedure() {
    final StandardTrivalueProcedure procedure = new StandardTrivalueProcedure();
    Assertions.assertNull(procedure.getXAxis());
    Assertions.assertNull(procedure.getYAxis());
    Assertions.assertNull(procedure.getZAxis());
    Assertions.assertNull(procedure.getValue());
    final int maxx = 3;
    final int maxy = 4;
    final int maxz = 5;
    procedure.setDimensions(maxx, maxy, maxz);
    Assertions.assertArrayEquals(new double[maxx], procedure.getXAxis());
    Assertions.assertArrayEquals(new double[maxy], procedure.getYAxis());
    Assertions.assertArrayEquals(new double[maxz], procedure.getZAxis());
    Assertions.assertArrayEquals(new double[maxx][maxy][maxz], procedure.getValue());
    procedure.setX(2, 3);
    procedure.setY(1, 4);
    procedure.setZ(0, 5);
    procedure.setValue(1, 2, 3, 42);
    Assertions.assertArrayEquals(new double[] {0, 0, 3}, procedure.getXAxis());
    Assertions.assertArrayEquals(new double[] {0, 4, 0, 0}, procedure.getYAxis());
    Assertions.assertArrayEquals(new double[] {5, 0, 0, 0, 0}, procedure.getZAxis());
    // @formatter:off
    Assertions.assertArrayEquals(new double[][][] {
      {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}},
      {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,42,0},{0,0,0,0,0}},
      {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}},
    }, procedure.getValue());
    // @formatter:on
  }
}
