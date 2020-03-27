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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RmsmdCalculator}.
 */
@SuppressWarnings({"javadoc"})
public class RmsmdCalculatorTest {
  @Test
  public void testBadArguments() {
    List<double[]> c2 = Arrays.asList(new double[2]);
    List<double[]> empty = Collections.emptyList();
    List<double[]> c3 = Arrays.asList(new double[3]);
    // Check OK with the same size
    Assertions.assertEquals(0, RmsmdCalculator.rmsmd(c2, c2));
    Assertions.assertEquals(0, RmsmdCalculator.rmsmd(c3, c3));
    // Empty is not allowed
    Assertions.assertThrows(IllegalArgumentException.class, () -> RmsmdCalculator.rmsmd(c2, empty));
    Assertions.assertThrows(IllegalArgumentException.class, () -> RmsmdCalculator.rmsmd(empty, c2));
    // Dimension mismatch
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> RmsmdCalculator.rmsmd(c2, c3));
  }

  @Test
  public void testRmsmd() {
    // From the RMSMD paper
    //@formatter:off
    List<double[]> s = Arrays.asList(
        new double[] {200, 460},
        new double[] {750, 660},
        new double[] {1190, 600},
        new double[] {1200, 200}
    );
    List<double[]> x = Arrays.asList(
        new double[] {300, 400},
        new double[] {260, 760},
        new double[] {550, 800},
        new double[] {820, 560},
        new double[] {950, 800},
        new double[] {1100, 100}
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740), RmsmdCalculator.rmsmd(s, x));
  }

  @Test
  public void testRmsmdWithObjects() {
    // From the RMSMD paper
    //@formatter:off
    List<BasePoint> s = Arrays.asList(
        new BasePoint(200, 460),
        new BasePoint(750, 660),
        new BasePoint(1190, 600),
        new BasePoint(1200, 200)
    );
    List<BasePoint> x = Arrays.asList(
        new BasePoint(300, 400),
        new BasePoint(260, 760),
        new BasePoint(550, 800),
        new BasePoint(820, 560),
        new BasePoint(950, 800),
        new BasePoint(1100, 100)
    );
    //@formatter:on
    Assertions.assertEquals(Math.sqrt(40740),
        RmsmdCalculator.rmsmd(s, x, p -> new double[] {p.getX(), p.getY()}));
  }
}
