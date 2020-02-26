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

import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * Class to store the result of a binary (two-class) classification analysis. This class allows
 * fractional counts.
 */
public class FractionClassificationResult {
  /** The true positives. */
  private final double truePositives;
  /** The false positives. */
  private final double falsePositives;
  /** The true negatives. */
  private final double trueNegatives;
  /** The false negatives. */
  private final double falseNegatives;
  /** The number of positives. */
  private final int numberOfPositives;
  /** The number of negatives. */
  private final int numberOfNegatives;
  /** The precision. */
  private final double precision;
  /** The recall. */
  private final double recall;
  /** The jaccard. */
  private final double jaccard;

  /**
   * Instantiates a new fraction classification result.
   *
   * @param truePositives The number of true positives
   * @param falsePositives The number of false positives
   * @param trueNegatives The number of true negatives
   * @param falseNegatives The number of false negatives
   */
  public FractionClassificationResult(double truePositives, double falsePositives,
      double trueNegatives, double falseNegatives) {
    this(truePositives, falsePositives, trueNegatives, falseNegatives, 0, 0);
  }

  /**
   * Instantiates a new fraction classification result.
   *
   * @param truePositives The number of true positives
   * @param falsePositives The number of false positives
   * @param trueNegatives The number of true negatives
   * @param falseNegatives The number of false negatives
   * @param positives The number of positives (can be used when truePositives+falsePositives is not
   *        the number of items that were accepted)
   * @param negatives The number of negatives (can be used when trueNegatives+falseNegatives is not
   *        the number of items that were rejected)
   */
  public FractionClassificationResult(double truePositives, double falsePositives,
      double trueNegatives, double falseNegatives, int positives, int negatives) {
    this.truePositives = truePositives;
    this.falsePositives = falsePositives;
    this.trueNegatives = trueNegatives;
    this.falseNegatives = falseNegatives;
    this.numberOfPositives = positives;
    this.numberOfNegatives = negatives;

    precision = MathUtils.div0(truePositives, truePositives + falsePositives);
    recall = MathUtils.div0(truePositives, truePositives + falseNegatives);
    jaccard = MathUtils.div0(truePositives, truePositives + falsePositives + falseNegatives);
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
  public double getTruePositives() {
    return truePositives;
  }

  /**
   * Gets the true negatives.
   *
   * @return the true negatives (correct rejection).
   */
  public double getTrueNegatives() {
    return trueNegatives;
  }

  /**
   * Gets the false positives.
   *
   * @return the false positives (false alarm, Type 1 error).
   */
  public double getFalsePositives() {
    return falsePositives;
  }

  /**
   * Gets the false negatives.
   *
   * @return the false negatives (miss, Type 2 error).
   */
  public double getFalseNegatives() {
    return falseNegatives;
  }

  /**
   * Gets the total.
   *
   * @return the total number of predictions.
   */
  public double getTotal() {
    return truePositives + falsePositives + trueNegatives + falseNegatives;
  }

  /**
   * Gets the positives.
   *
   * @return the number of positives (TP + FP).
   * @see #getNumberOfPositives()
   */
  public double getPositives() {
    return truePositives + falsePositives;
  }

  /**
   * Gets the negatives.
   *
   * @return the number of negatives (TN + FN).
   * @see #getNumberOfNegatives()
   */
  public double getNegatives() {
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
    return MathUtils.div0(truePositives + trueNegatives,
        truePositives + falsePositives + trueNegatives + falseNegatives);
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
    final double distance = (truePositives + falsePositives) * (truePositives + falseNegatives)
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

  /**
   * Get the number of positives. Note that this may be different from truePositives+falsePositives.
   * Note this is set in the constructor, otherwise zero.
   *
   * @return The number of positives
   * @see #getPositives()
   */
  public int getNumberOfPositives() {
    return numberOfPositives;
  }

  /**
   * Get the number of negatives. Note this may be different from trueNegatives+falseNegatives. Note
   * this is set in the constructor, otherwise zero.
   *
   * @return The number of negatives
   * @see #getNegatives()
   */
  public int getNumberOfNegatives() {
    return numberOfNegatives;
  }
}
