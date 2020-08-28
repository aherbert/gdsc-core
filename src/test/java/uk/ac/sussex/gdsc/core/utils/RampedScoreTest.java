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

import gnu.trove.set.hash.TDoubleHashSet;
import gnu.trove.set.hash.TFloatHashSet;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class RampedScoreTest {
  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new RampedScore(2, 1));
  }

  @Test
  void testScoreNoRamp() {
    final RampedScore score = new RampedScore(3, 3);
    Assertions.assertEquals(1, score.score(2));
    Assertions.assertEquals(1, score.score(Math.nextDown(3)));
    Assertions.assertEquals(1, score.score(3));
    Assertions.assertEquals(0, score.score(Math.nextUp(3)));
    Assertions.assertEquals(0, score.score(4));
  }

  @Test
  void testScore() {
    final RampedScore score = new RampedScore(3, 5);
    Assertions.assertEquals(1, score.score(2));
    Assertions.assertEquals(1, score.score(3));
    Assertions.assertEquals(0, score.score(5));
    Assertions.assertEquals(0, score.score(6));
    double last = score.score(3);
    for (int i = 1; i <= 100; i++) {
      final double current = score.score(3 + i / 50.0);
      Assertions.assertTrue(current < last);
      last = current;
    }
  }

  @Test
  void testScoreAndFlatten() {
    final RampedScore score = new RampedScore(3, 5);
    final int steps = 256;
    for (int i = 1; i <= 100; i++) {
      final double value = 3 + i / 50.0;
      final double current = score.score(value);
      Assertions.assertEquals(RampedScore.flatten(current, steps),
          score.scoreAndFlatten(value, steps));
    }
  }

  @Test
  void testFlattenDouble() {
    final int steps = 20;
    final TDoubleHashSet set = new TDoubleHashSet(101);
    for (int i = 0; i <= 100; i++) {
      final double value = i / 100.0;
      final double score = RampedScore.flatten(value, steps);
      Assertions.assertEquals(value, score, 0.025);
      set.add(score);
    }
    Assertions.assertEquals(steps + 1, set.size());
  }

  @Test
  void testFlattenFloat() {
    final int steps = 20;
    final TFloatHashSet set = new TFloatHashSet(101);
    for (int i = 0; i <= 100; i++) {
      final float value = i / 100.0f;
      final float score = RampedScore.flatten(value, steps);
      Assertions.assertEquals(value, score, 0.025f);
      set.add(score);
    }
    Assertions.assertEquals(steps + 1, set.size());
  }

  @SeededTest
  void testCopy(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final RampedScore score1 = new RampedScore(0.25, 0.75);
    final RampedScore score2 = score1.copy();
    Assertions.assertNotSame(score1, score2);
    for (int i = 0; i < 10; i++) {
      final double value = rng.nextDouble();
      Assertions.assertEquals(score1.score(value), score2.score(value));
      Assertions.assertEquals(score1.scoreAndFlatten(value, 256),
          score2.scoreAndFlatten(value, 256));
    }
  }
}
