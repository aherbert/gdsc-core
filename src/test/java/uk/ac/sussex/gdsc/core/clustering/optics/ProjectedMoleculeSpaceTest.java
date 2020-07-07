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

package uk.ac.sussex.gdsc.core.clustering.optics;

import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.rng.UniformRandomProviders;

@SuppressWarnings({"javadoc"})
public class ProjectedMoleculeSpaceTest {
  @Test
  public void testComputeWithSize1() {
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 1);
    final UniformRandomProvider rg = UniformRandomProviders.create(123);
    final ProjectedMoleculeSpace space = new ProjectedMoleculeSpace(om, 0, rg);
    space.generate();
    space.computeSets(100);
    final int[][] neighbours = space.computeAverageDistInSetAndNeighbours();
    Assertions.assertEquals(1, neighbours.length);
    // No neighbours
    Assertions.assertEquals(0, neighbours[0].length);
    final Molecule object = space.setOfObjects[0];
    space.findNeighbours(2, object, 0);
    Assertions.assertEquals(0, space.neighbours.size);
  }

  @Test
  public void testComputeWithSize2() {
    final float[] x = {0, 0};
    final OpticsManager om = new OpticsManager(x, x, 1);
    final UniformRandomProvider rg = UniformRandomProviders.create(123);
    final ProjectedMoleculeSpace space = new ProjectedMoleculeSpace(om, 0, rg);
    space.generate();
    space.computeSets(100);
    final int[][] neighbours = space.computeAverageDistInSetAndNeighbours();
    Assertions.assertEquals(2, neighbours.length);
    Assertions.assertEquals(1, neighbours[0].length);
    Assertions.assertEquals(1, neighbours[1].length);
    Molecule object = space.setOfObjects[0];
    space.findNeighbours(1, object, 0);
    Assertions.assertEquals(1, space.neighbours.size);
    Assertions.assertSame(space.setOfObjects[1], space.neighbours.get(0));
    object = space.setOfObjects[1];
    space.findNeighbours(1, object, 0);
    Assertions.assertEquals(1, space.neighbours.size);
    Assertions.assertSame(space.setOfObjects[0], space.neighbours.get(0));
  }

  @Test
  public void testComputeWithMinSplitSizeOf1() {
    final float[] x = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    final OpticsManager om = new OpticsManager(x, x, 10);
    final UniformRandomProvider rg = UniformRandomProviders.create(123);
    final ProjectedMoleculeSpace space = new ProjectedMoleculeSpace(om, 0, rg);
    space.generate();
    space.computeSets(1);
    final int[][] neighbours = space.computeAverageDistInSetAndNeighbours();
    Assertions.assertEquals(10, neighbours.length);
    // No neighbours
    for (int i = 0; i < x.length; i++) {
      final int[] n = neighbours[i];
      Assertions.assertEquals(0, n.length);
      final Molecule object = space.setOfObjects[i];
      space.findNeighbours(2, object, 0);
      Assertions.assertEquals(0, space.neighbours.size);
    }
  }

  @Test
  public void testSplitRandomly() {
    final int[] ind = {0, 1, 2, 3, 4, 5};
    final int begin = 1;
    final int end = 5;
    float[] tpro = {0, 5, 3, 6, 4, 0};
    for (int i = 0; i < 4; i++) {
      final int nextInt = i;
      final UniformRandomProvider rg = new SplitMix64(0L) {
        @Override
        public int nextInt(int n) {
          return nextInt;
        }
      };
      final int[] indices = ind.clone();
      final int index = ProjectedMoleculeSpace.splitRandomly(indices, begin, end, tpro, rg);
      float max = tpro[indices[begin]];
      for (int j = begin; j <= index; j++) {
        max = Math.max(max, tpro[indices[j]]);
      }
      // All after should be higher
      for (int j = index + 1; j < end; j++) {
        Assertions.assertTrue(tpro[indices[j]] > max);
      }
      // We know the actual split element
      for (int j = begin; j <= index; j++) {
        Assertions.assertTrue(tpro[indices[j]] <= tpro[ind[begin + i]]);
      }
      for (int j = index + 1; j < end; j++) {
        Assertions.assertTrue(tpro[indices[j]] > tpro[ind[begin + i]]);
      }
    }
    // Split in middle when distances are the same
    tpro = new float[ind.length];
    int index = ProjectedMoleculeSpace.splitRandomly(ind, begin, end, tpro,
        UniformRandomProviders.create(123));
    Assertions.assertEquals((end - begin - 1) / 2 + begin, index);
    // Edge case when minIndex == maxIndex and no swap occurs
    tpro = new float[] {0, 0, 0, 1, 1};
    index = ProjectedMoleculeSpace.splitRandomly(ind, 0, 5, tpro, new SplitMix64(0L) {
      @Override
      public int nextInt(int n) {
        return 0;
      }
    });
    Assertions.assertEquals(2, index);
  }

  @Test
  public void testSplitByDistance() {
    final int[] ind = {0, 1, 2, 3, 4, 5};
    final int begin = 1;
    final int end = 5;
    float[] tpro = {0, 5, 3, 6, 4, 0};
    for (int i = 0; i < 4; i++) {
      final float nextFloat = i / 3.0f;
      final UniformRandomProvider rg = new SplitMix64(0L) {
        @Override
        public float nextFloat() {
          return nextFloat;
        }
      };
      final int[] indices = ind.clone();
      final int index = ProjectedMoleculeSpace.splitByDistance(indices, begin, end, tpro, rg);
      float max = tpro[indices[begin]];
      for (int j = begin; j <= index; j++) {
        max = Math.max(max, tpro[indices[j]]);
      }
      // All after should be higher
      for (int j = index + 1; j < end; j++) {
        Assertions.assertTrue(tpro[indices[j]] > max);
      }
    }
    // Split in middle when distances are the same
    tpro = new float[ind.length];
    int index = ProjectedMoleculeSpace.splitByDistance(ind, begin, end, tpro,
        UniformRandomProviders.create(123));
    Assertions.assertEquals((end - begin - 1) / 2 + begin, index);
    // Edge case when minIndex == maxIndex and no swap occurs
    tpro = new float[] {0, 0, 0, 1, 1};
    index = ProjectedMoleculeSpace.splitByDistance(ind, 0, 5, tpro, new SplitMix64(0L) {
      @Override
      public float nextFloat() {
        return 0f;
      }
    });
    Assertions.assertEquals(2, index);
  }

  @Test
  public void testFindNeighbours() {
    final float[] x = {0, 1, 3};
    final OpticsManager om = new OpticsManager(x, x, 10);
    final UniformRandomProvider rg = UniformRandomProviders.create(123);
    final ProjectedMoleculeSpace space = new ProjectedMoleculeSpace(om, 0, rg);
    space.generate();
    space.computeSets(2);
    final int[][] neighbours = space.computeAverageDistInSetAndNeighbours();
    Assertions.assertEquals(3, neighbours.length);
    final Molecule object = space.setOfObjects[1];
    space.findNeighbours(1, object, 0);
    Assertions.assertEquals(2, space.neighbours.size);
    Assertions.assertEquals(2, neighbours[1].length);
    for (int i = 0; i < 2; i++) {
      Assertions.assertEquals(0f, space.neighbours.get(i).getD());
    }
    space.findNeighboursAndDistances(1, object, 0);
    Assertions.assertEquals(2, space.neighbours.size);
    Assertions.assertEquals(2, neighbours[1].length);
    final float[] d = new float[2];
    for (int i = 0; i < 2; i++) {
      d[i] = space.neighbours.get(i).getD();
    }
    Arrays.sort(d);
    // Note: Distances are squared: 1^2+1^2, 2^2+2^2
    Assertions.assertArrayEquals(new float[] {2, 8}, d);
  }
}
