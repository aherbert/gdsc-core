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

package uk.ac.sussex.gdsc.core.utils.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class FloatPredicateTest {

  @Test
  void canTest() {
    final FloatPredicate fun = f -> f == 3.1f;
    Assertions.assertTrue(fun.test(3.1f));
    Assertions.assertFalse(fun.test(3f));
  }

  @Test
  void testNegate() {
    FloatPredicate fun = f -> f == 3.1f;
    fun = fun.negate();
    Assertions.assertFalse(fun.test(3.1f));
    Assertions.assertTrue(fun.test(3f));
  }

  @Test
  void testAnd() {
    final float v1 = 3.1f;
    final float v2 = 6.7f;
    final FloatPredicate fun1 = f -> f == v1;
    final FloatPredicate fun2 = f -> f <= v2;
    final FloatPredicate fun = fun1.and(fun2);
    final float[] values = {1, v1, v2, 99};
    for (final float value : values) {
      Assertions.assertEquals(value == v1 && value <= v2, fun.test(value));
    }
  }

  @Test
  void testOr() {
    final float v1 = 3.1f;
    final float v2 = 6.7f;
    final FloatPredicate fun1 = f -> f == v1;
    final FloatPredicate fun2 = f -> f <= v2;
    final FloatPredicate fun = fun1.or(fun2);
    final float[] values = {1, v1, v2, 99};
    for (final float value : values) {
      Assertions.assertEquals(value == v1 || value <= v2, fun.test(value));
    }
  }
}
