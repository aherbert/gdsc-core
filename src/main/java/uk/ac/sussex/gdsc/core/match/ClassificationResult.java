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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

/**
 * Class to store the result of a binary scoring analysis when true and false positive and negatives are available.
 *
 * Can calculate the F-score statistic with a given beta weighting between the precision and recall.
 *
 * @see "http://en.wikipedia.org/wiki/Precision_and_recall#F-measure"
 */
public class ClassificationResult
{
    private final int tp, fp, tn, fn;
    private final double precision;
    private final double recall;
    private final double jaccard;

    /**
     * @param tp
     *            The number of true positives
     * @param fp
     *            The number of false positives
     * @param tn
     *            The number of true negatives
     * @param fn
     *            The number of false negatives
     */
    public ClassificationResult(int tp, int fp, int tn, int fn)
    {
        this.tp = tp;
        this.fp = fp;
        this.tn = tn;
        this.fn = fn;

        precision = divide(tp, tp + fp);
        recall = divide(tp, tp + fn);
        jaccard = divide(tp, tp + fp + fn);
    }

    private static double divide(final double numerator, final int denominator)
    {
        if (denominator == 0)
            return 0;
        return numerator / denominator;
    }

    /**
     * Return the F-Score statistic, a weighted combination of the precision and recall.
     *
     * @param precision
     *            the precision
     * @param recall
     *            the recall
     * @param beta
     *            The weight
     * @return The F-Score
     */
    public static double calculateFScore(double precision, double recall, double beta)
    {
        final double b2 = beta * beta;
        final double f = ((1.0 + b2) * precision * recall) / (b2 * precision + recall);
        return (Double.isNaN(f) ? 0 : f);
    }

    /**
     * Return the F-Score statistic, a weighted combination of the precision and recall
     *
     * @param beta
     *            The weight
     * @return The F-Score
     */
    public double getFScore(double beta)
    {
        return calculateFScore(precision, recall, beta);
    }

    /**
     * Return the F1-Score statistic, a equal weighted combination of the precision and recall
     *
     * @return The F1-Score
     */
    public double getF1Score()
    {
        final double f = (2 * precision * recall) / (precision + recall);
        return (Double.isNaN(f) ? 0 : f);
    }

    /**
     * @return the precision
     */
    public double getPrecision()
    {
        return precision;
    }

    /**
     * @return the recall
     */
    public double getRecall()
    {
        return recall;
    }

    /**
     * @return the Jaccard index (defined as the size of the intersection divided by the size of the union of the sample
     *         sets)
     */
    public double getJaccard()
    {
        return jaccard;
    }

    // Taken from http://en.wikipedia.org/wiki/Sensitivity_and_specificity

    /**
     * @return the true positives (hit)
     */
    public int getTP()
    {
        return tp;
    }

    /**
     * @return the true negatives (correct rejection)
     */
    public int getTN()
    {
        return tn;
    }

    /**
     * @return the false positives (false alarm, Type 1 error)
     */
    public int getFP()
    {
        return fp;
    }

    /**
     * @return the false negatives (miss, Type 2 error)
     */
    public int getFN()
    {
        return fn;
    }

    /**
     * @return the total number of predictions
     */
    public int getTotal()
    {
        return tp + fp + tn + fn;
    }

    /**
     * @return the number of positives
     */
    public int getP()
    {
        return tp + fn;
    }

    /**
     * @return the number of negatives
     */
    public int getN()
    {
        return fp + tn;
    }

    /**
     * @return The true positive rate (recall, sensitivity, hit rate) = tp / (tp + fn)
     */
    public double getTPR()
    {
        return recall;
    }

    /**
     * @return The true negative rate (specificity) = tn / (fp + tn)
     */
    public double getTNR()
    {
        return divide(tn, fp + tn);
    }

    /**
     * @return The positive predictive value (precision) = tp / (tp + fp)
     */
    public double getPPV()
    {
        return precision;
    }

    /**
     * @return The negative predictive value = tn / (tn + fn)
     */
    public double getNPV()
    {
        return divide(tn, tn + fn);
    }

    /**
     * @return The false positive rate (fall-out) = fp / (fp + tn)
     */
    public double getFPR()
    {
        return divide(fp, fp + tn);
    }

    /**
     * @return The false negative rate = fn / (fn + tp)
     */
    public double getFNR()
    {
        return divide(fn, fn + tp);
    }

    /**
     * @return The false discovery rate (1 - precision) = fp / (tp + fp)
     */
    public double getFDR()
    {
        return 1 - precision; // divide(fp, tp + fp);
    }

    /**
     * @return The accuracy = (tp + tn) / (tp + fp + tn + fn)
     */
    public double getAccuracy()
    {
        return divide(tp + tn, getP() + getN());
    }

    /**
     * The Matthews correlation coefficient is used in machine learning as a measure of the quality of binary
     * (two-class) classifications, introduced by biochemist Brian W. Matthews in 1975. It takes into account true and
     * false positives and negatives and is generally regarded as a balanced measure which can be used even if the
     * classes are of very different sizes. The MCC is in essence a correlation coefficient between the observed and
     * predicted binary classifications; it returns a value between −1 and +1. A coefficient of +1 represents a perfect
     * prediction, 0 no better than random prediction and −1 indicates total disagreement between prediction and
     * observation. The statistic is also known as the phi coefficient.
     *
     * @return The Matthews correlation coefficient (MCC)
     */
    public double getMCC()
    {
        final double d = (double) (tp + fp) * (double) (tp + fn) * (tn + fp) * (tn + fn);
        double mcc = 0;
        if (d != 0)
            mcc = ((double) (tp * tn) - (double) (fp * fn)) / Math.sqrt(d);
        return Math.max(-1, Math.min(1, mcc));
    }

    /**
     * @return The informedness (TPR + TNR - 1)
     */
    public double getInformedness()
    {
        return getTPR() + getTNR() - 1;
    }

    /**
     * @return The markedness (PPV + NPV - 1)
     */
    public double getMarkedness()
    {
        return getPPV() + getNPV() - 1;
    }
}
