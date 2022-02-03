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

import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * Class to store the result of a binary (two-class) classification analysis.
 */
public class ClassificationResult {
  /** The true positives. */
  private final int truePositives;
  /** The false positives. */
  private final int falsePositives;
  /** The true negatives. */
  private final int trueNegatives;
  /** The false negatives. */
  private final int falseNegatives;
  /** The precision. */
  private final double precision;
  /** The recall. */
  private final double recall;
  /** The jaccard. */
  private final double jaccard;

  /**
   * Instantiates a new classification result.
   *
   * @param truePositives The number of true positives
   * @param falsePositives The number of false positives
   * @param trueNegatives The number of true negatives
   * @param falseNegatives The number of false negatives
   */
  public ClassificationResult(int truePositives, int falsePositives, int trueNegatives,
      int falseNegatives) {
    this.truePositives = truePositives;
    this.falsePositives = falsePositives;
    this.trueNegatives = trueNegatives;
    this.falseNegatives = falseNegatives;

    precision = MatchScores.calculatePrecision(truePositives, falsePositives);
    recall = MatchScores.calculateRecall(truePositives, falseNegatives);
    jaccard = MatchScores.calculateJaccard(truePositives, falsePositives, falseNegatives);
  }

  /**
   * Return the F-Score statistic, a weighted combination of the precision and recall.
   *
   * @param beta The weight
   * @return The F-Score
   */
  public double getFScore(double beta) {
    return MatchScores.calculateFBetaScore(getPrecision(), getRecall(), beta);
  }

  /**
   * Return the F1-Score statistic, a equal weighted combination of the precision and recall.
   *
   * @return The F1-Score
   */
  public double getF1Score() {
    return MatchScores.calculateF1Score(getPrecision(), getRecall());
  }

  /**
   * Gets the precision.
   *
   * @return the precision.
   */
  public double getPrecision() {
    return precision;
  }

  /**
   * Gets the recall.
   *
   * @return the recall.
   */
  public double getRecall() {
    return recall;
  }

  /**
   * Gets the jaccard.
   *
   * @return the Jaccard index (defined as the size of the intersection divided by the size of the
   *         union of the sample sets)
   */
  public double getJaccard() {
    return jaccard;
  }

  // Taken from http://en.wikipedia.org/wiki/Sensitivity_and_specificity

  /**
   * Gets the true positives.
   *
   * @return the true positives (hit).
   */
  public int getTruePositives() {
    return truePositives;
  }

  /**
   * Gets the true negatives.
   *
   * @return the true negatives (correct rejection).
   */
  public int getTrueNegatives() {
    return trueNegatives;
  }

  /**
   * Gets the false positives.
   *
   * @return the false positives (false alarm, Type 1 error).
   */
  public int getFalsePositives() {
    return falsePositives;
  }

  /**
   * Gets the false negatives.
   *
   * @return the false negatives (miss, Type 2 error).
   */
  public int getFalseNegatives() {
    return falseNegatives;
  }

  /**
   * Gets the total.
   *
   * @return the total number of predictions.
   */
  public int getTotal() {
    return truePositives + falsePositives + trueNegatives + falseNegatives;
  }

  /**
   * Gets the positives.
   *
   * @return the number of positives (TP + FP).
   */
  public int getPositives() {
    return truePositives + falsePositives;
  }

  /**
   * Gets the negatives.
   *
   * @return the number of negatives (TN + FN).
   */
  public int getNegatives() {
    return trueNegatives + falseNegatives;
  }

  /**
   * Gets the true positive rate.
   *
   * @return The true positive rate (recall, sensitivity, hit rate) = truePositives / (truePositives
   *         + falseNegatives)
   */
  public double getTruePositiveRate() {
    return getRecall();
  }

  /**
   * Gets the true negative rate.
   *
   * @return The true negative rate (specificity) = trueNegatives / (falsePositives + trueNegatives)
   */
  public double getTrueNegativeRate() {
    return MathUtils.div0(trueNegatives, falsePositives + trueNegatives);
  }

  /**
   * Gets the positive predictive value.
   *
   * @return The positive predictive value (precision) = truePositives / (truePositives +
   *         falsePositives)
   */
  public double getPositivePredictiveValue() {
    return getPrecision();
  }

  /**
   * Gets the negative predictive value.
   *
   * @return The negative predictive value = trueNegatives / (trueNegatives + falseNegatives)
   */
  public double getNegativePredictiveValue() {
    return MathUtils.div0(trueNegatives, trueNegatives + falseNegatives);
  }

  /**
   * Gets the false positive rate.
   *
   * @return The false positive rate (fall-out) = falsePositives / (falsePositives + trueNegatives)
   */
  public double getFalsePositiveRate() {
    return MathUtils.div0(falsePositives, falsePositives + trueNegatives);
  }

  /**
   * Gets the false negative rate.
   *
   * @return The false negative rate = falseNegatives / (falseNegatives + truePositives)
   */
  public double getFalseNegativeRate() {
    return MathUtils.div0(falseNegatives, falseNegatives + truePositives);
  }

  /**
   * Gets the false discovery rate.
   *
   * @return The false discovery rate (1 - precision) = falsePositives / (truePositives +
   *         falsePositives)
   */
  public double getFalseDiscoveryRate() {
    return MathUtils.div0(falsePositives, truePositives + falsePositives);
  }

  /**
   * Gets the accuracy.
   *
   * @return The accuracy = (truePositives + trueNegatives) / (truePositives + falsePositives +
   *         trueNegatives + falseNegatives)
   */
  public double getAccuracy() {
    return MathUtils.div0((long) truePositives + trueNegatives,
        (long) truePositives + falsePositives + trueNegatives + falseNegatives);
  }

  /**
   * The Matthews correlation coefficient is used in machine learning as a measure of the quality of
   * binary (two-class) classifications, introduced by biochemist Brian W. Matthews in 1975. It
   * takes into account true and false positives and negatives and is generally regarded as a
   * balanced measure which can be used even if the classes are of very different sizes. The MCC is
   * in essence a correlation coefficient between the observed and predicted binary classifications;
   * it returns a value between −1 and +1. A coefficient of +1 represents a perfect prediction, 0 no
   * better than random prediction and −1 indicates total disagreement between prediction and
   * observation. The statistic is also known as the phi coefficient.
   *
   * @return The Matthews correlation coefficient (MCC)
   */
  public double getMatthewsCorrelationCoefficient() {
    final double distance =
        (double) (truePositives + falsePositives) * (truePositives + falseNegatives)
            * (trueNegatives + falsePositives) * (trueNegatives + falseNegatives);
    double mcc = 0;
    if (distance != 0) {
      mcc = (truePositives * trueNegatives - falsePositives * falseNegatives) / Math.sqrt(distance);
    }
    return MathUtils.clip(-1, 1, mcc);
  }

  /**
   * Gets the informedness.
   *
   * @return The informedness (TPR + TNR - 1)
   */
  public double getInformedness() {
    return getTruePositiveRate() + getTrueNegativeRate() - 1;
  }

  /**
   * Gets the markedness.
   *
   * @return The markedness (PPV + NPV - 1)
   */
  public double getMarkedness() {
    return getPositivePredictiveValue() + getNegativePredictiveValue() - 1;
  }
}
