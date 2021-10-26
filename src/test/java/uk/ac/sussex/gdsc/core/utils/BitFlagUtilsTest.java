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

package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class BitFlagUtilsTest {

  private static final int FLAG1 = 0x01;
  private static final int FLAG2 = 0x02;
  private static final int FLAGS = FLAG1 | FLAG2;

  @Test
  void canSetAndTestFlags() {
    int flags = 0;
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertFalse(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertTrue(BitFlagUtils.anyNotSet(flags, FLAGS));

    flags = BitFlagUtils.set(flags, FLAG1);
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertTrue(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertTrue(BitFlagUtils.anyNotSet(flags, FLAGS));

    flags = BitFlagUtils.set(flags, FLAG2);
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertTrue(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertFalse(BitFlagUtils.anyNotSet(flags, FLAGS));

    flags = BitFlagUtils.unset(flags, FLAG1);
    Assertions.assertFalse(BitFlagUtils.areSet(flags, FLAG1));
    Assertions.assertTrue(BitFlagUtils.areSet(flags, FLAG2));
    Assertions.assertTrue(BitFlagUtils.anySet(flags, FLAGS));
    Assertions.assertTrue(BitFlagUtils.anyNotSet(flags, FLAGS));
  }
}
