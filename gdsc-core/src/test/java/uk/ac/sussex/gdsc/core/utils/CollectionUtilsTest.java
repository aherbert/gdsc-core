/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.EnumSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class CollectionUtilsTest {

  enum SimpleEnum {
    A, B, C, D, E, F, G, H, I, J, K, L, M
  }

  // For smaller test code
  private static final SimpleEnum A = SimpleEnum.A;
  private static final SimpleEnum B = SimpleEnum.B;
  private static final SimpleEnum C = SimpleEnum.C;
  private static final SimpleEnum D = SimpleEnum.D;
  private static final SimpleEnum E = SimpleEnum.E;
  private static final SimpleEnum F = SimpleEnum.F;
  private static final SimpleEnum G = SimpleEnum.G;

  @Test
  void testContainsAny() {
    // 2 args
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(A), A, B));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(B), A, B));
    Assertions.assertFalse(CollectionUtils.containsAny(EnumSet.of(G), A, B));

    // 3 args
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(A), A, B, C));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(B), A, B, C));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(C), A, B, C));
    Assertions.assertFalse(CollectionUtils.containsAny(EnumSet.of(G), A, B, C));

    // 4 args
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(A), A, B, C, D));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(B), A, B, C, D));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(C), A, B, C, D));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(D), A, B, C, D));
    Assertions.assertFalse(CollectionUtils.containsAny(EnumSet.of(G), A, B, C, D));

    // 5 args
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(A), A, B, C, D, E));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(B), A, B, C, D, E));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(C), A, B, C, D, E));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(D), A, B, C, D, E));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(E), A, B, C, D, E));
    Assertions.assertFalse(CollectionUtils.containsAny(EnumSet.of(G), A, B, C, D, E));

    // 6 args
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(A), A, B, C, D, E, F));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(B), A, B, C, D, E, F));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(C), A, B, C, D, E, F));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(D), A, B, C, D, E, F));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(E), A, B, C, D, E, F));
    Assertions.assertTrue(CollectionUtils.containsAny(EnumSet.of(F), A, B, C, D, E, F));
    Assertions.assertFalse(CollectionUtils.containsAny(EnumSet.of(G), A, B, C, D, E, F));
  }

  @Test
  void testContainsAll() {
    // 2 args
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), A, B));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(B), A, B));
    Assertions.assertTrue(CollectionUtils.containsAll(EnumSet.of(A, B), A, B));

    // 3 args
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), B, C, D));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), A, B, C));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B), A, B, C));
    Assertions.assertTrue(CollectionUtils.containsAll(EnumSet.of(A, B, C), A, B, C));

    // 4 args
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), B, C, D, E));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), A, B, C, D));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B), A, B, C, D));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B, C), A, B, C, D));
    Assertions.assertTrue(CollectionUtils.containsAll(EnumSet.of(A, B, C, D), A, B, C, D));

    // 5 args
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), B, C, D, E, F));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), A, B, C, D, E));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B), A, B, C, D, E));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B, C), A, B, C, D, E));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B, C, D), A, B, C, D, E));
    Assertions.assertTrue(CollectionUtils.containsAll(EnumSet.of(A, B, C, D, E), A, B, C, D, E));

    // 6 args
    // @formatter:off
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), B));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), A, B));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A), A, B, C, D, E, F));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B), A, B, C, D, E, F));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B, C), A, B, C, D, E, F));
    Assertions.assertFalse(CollectionUtils.containsAll(EnumSet.of(A, B, C, D), A, B, C, D, E, F));
    Assertions.assertFalse(CollectionUtils.containsAll(
        EnumSet.of(A, B, C, D, E), A, B, C, D, E, F));
    Assertions.assertTrue(CollectionUtils.containsAll(
        EnumSet.of(A, B, C, D, E, F), A, B, C, D, E, F));
    // @formatter:on
  }

  @Test
  void canGetSize() {
    Assertions.assertEquals(0, CollectionUtils.getSize(null));
    Assertions.assertEquals(3, CollectionUtils.getSize(Arrays.asList(1, 2, 3)));
  }
}
