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

package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

@SuppressWarnings({"javadoc"})
public class TriangleArrayTest {
  final int[] testN = new int[] {0, 1, 2, 5};

  @Test
  public void canComputeIndex() {
    for (final int n : testN) {
      final TriangleArray a = new TriangleArray(n);

      final int[] count = new int[a.getLength()];
      int[] ij = new int[2];

      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          final int k = a.toIndex(i, j);
          count[k]++;

          ij = a.fromIndex(k);
          Assertions.assertEquals(i, ij[0], () -> "fromIndex(int) " + k);
          Assertions.assertEquals(j, ij[1], () -> "fromIndex(int) " + k);

          a.fromIndex(k, ij);
          Assertions.assertEquals(i, ij[0], () -> "fromIndex(int,int[]) " + k);
          Assertions.assertEquals(j, ij[1], () -> "fromIndex(int,int[]) " + k);

          ij = TriangleArray.fromIndex(n, k);
          Assertions.assertEquals(i, ij[0], () -> "static fromIndex(int) " + k);
          Assertions.assertEquals(j, ij[1], () -> "static fromIndex(int) " + k);

          TriangleArray.fromIndex(n, k, ij);
          Assertions.assertEquals(i, ij[0], () -> "static fromIndex(int,int[]) " + k);
          Assertions.assertEquals(j, ij[1], () -> "static fromIndex(int,int[]) " + k);
        }
      }
      for (int i = count.length; i-- > 0;) {
        Assertions.assertEquals(1, count[i], "count");
      }
    }
  }

  @Test
  public void indexNotReversible() {
    final int n = 10;
    final TriangleArray a = new TriangleArray(n);

    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        final int k = a.toIndex(i, j);
        final int k2 = a.toIndex(j, i);
        if (k == k2) {
          continue;
        }
        return;
      }
    }

    Assertions.fail();
  }

  @Test
  public void safeIndexIsReversible() {
    final int n = 10;
    final TriangleArray a = new TriangleArray(n);

    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        final int k = a.toIndex(i, j);
        final int k2 = a.toSafeIndex(j, i);
        Assertions.assertEquals(k, k2);
      }
    }
  }

  @Test
  public void canFastComputePostIndex() {
    final IndexSupplier msg = new IndexSupplier(2);
    for (final int n : testN) {
      final TriangleArray a = new TriangleArray(n);

      for (int i = 0; i < n; i++) {
        msg.set(0, i);
        for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++) {
          final int k = a.toIndex(i, j);
          Assertions.assertEquals(k, index, msg.set(1, j));
        }
      }
    }
  }

  @Test
  public void canFastComputePreIndex() {
    final IndexSupplier msg = new IndexSupplier(2);
    for (final int n : testN) {
      final TriangleArray a = new TriangleArray(n);

      for (int j = n; j-- > 0;) {
        msg.set(1, j);
        for (int i = j, index = a.toPrecursorIndex(j); i-- > 0;) {
          final int k = a.toIndex(i, j);
          final int k2 = a.precursorToIndex(index, i);
          Assertions.assertEquals(k, k2, msg.set(0, i));
        }
      }
    }
  }

  @Test
  public void canFastIterateNxN() {
    for (final int n : testN) {
      final TriangleArray a = new TriangleArray(n);

      for (int i = 0; i < n; i++) {
        for (int j = 0, precursor = a.toPrecursorIndex(i); j < i; j++) {
          final int k = a.toSafeIndex(i, j);
          final int k2 = a.precursorToIndex(precursor, j);
          Assertions.assertEquals(k, k2);
        }
        for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++) {
          final int k = a.toSafeIndex(i, j);
          Assertions.assertEquals(k, index);
        }
      }
    }
  }

  @Test
  public void canCompareItoAnyJ() {
    for (final int n : testN) {
      final TriangleArray a = new TriangleArray(n);

      for (int i = 0; i < n; i++) {
        a.setup(i);
        for (int j = 0; j < n; j++) {
          if (i == j) {
            continue;
          }
          final int k = a.toSafeIndex(i, j);
          final int k2 = a.toIndex(j);
          Assertions.assertEquals(k, k2);
        }
      }
    }
  }
}
