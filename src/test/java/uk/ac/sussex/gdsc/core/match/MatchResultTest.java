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

package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MatchResult}.
 */
@SuppressWarnings({"javadoc"})
class MatchResultTest {
  @Test
  void canCreate() {
    final int tp = 5;
    final int fp = 1;
    final int fn = 3;
    final double rmsd = 67.789;
    final MatchResult match = new MatchResult(tp, fp, fn, rmsd);
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(rmsd, match.getRmsd(), "rmsd");
    Assertions.assertEquals(tp + fp, match.getNumberPredicted(), "predicted");
    Assertions.assertEquals(tp + fn, match.getNumberActual(), "actual");
    Assertions.assertEquals(MatchScores.calculateRecall(tp, fn), match.getRecall(), "recall");
    Assertions.assertEquals(MatchScores.calculatePrecision(tp, fp), match.getPrecision(),
        "precision");
    Assertions.assertEquals(MatchScores.calculateJaccard(tp, fp, fn), match.getJaccard(),
        "Jaccard");
    Assertions.assertEquals(MatchScores.calculateF1Score(tp, fp, fn), match.getF1Score(),
        "f1-score");
    Assertions.assertEquals(MatchScores.calculateFBetaScore(tp, fp, fn, 0.5), match.getFScore(0.5),
        "f-score");
  }
}
