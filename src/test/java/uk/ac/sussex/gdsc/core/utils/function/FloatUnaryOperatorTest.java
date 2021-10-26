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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class FloatUnaryOperatorTest {

  @Test
  void canApplyAsFloat() {
    final FloatUnaryOperator fun = f -> f + 1;
    Assertions.assertEquals(3, fun.applyAsFloat(2));
    Assertions.assertEquals(3.5f, fun.applyAsFloat(2.5f));
  }

  @Test
  void testIdentity() {
    final FloatUnaryOperator fun = FloatUnaryOperator.identity();
    for (final float f : new float[] {1, 2, 3, 5.67f}) {
      Assertions.assertEquals(f, fun.applyAsFloat(f));
    }
  }

  @Test
  void testCompose() {
    final FloatUnaryOperator multiply = f -> f * 9 / 5;
    final FloatUnaryOperator add = f -> f + 32;
    final FloatUnaryOperator fun = add.compose(multiply);
    Assertions.assertEquals(32, fun.applyAsFloat(0));
    Assertions.assertEquals(212, fun.applyAsFloat(100));
    Assertions.assertEquals(-40, fun.applyAsFloat(-40));
  }

  @Test
  void testAndThen() {
    final FloatUnaryOperator multiply = f -> f * 9 / 5;
    final FloatUnaryOperator add = f -> f + 32;
    final FloatUnaryOperator fun = multiply.andThen(add);
    Assertions.assertEquals(32, fun.applyAsFloat(0));
    Assertions.assertEquals(212, fun.applyAsFloat(100));
    Assertions.assertEquals(-40, fun.applyAsFloat(-40));
  }
}
