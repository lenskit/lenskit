/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.predict.ordrec;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.transform.quantize.Quantizer;

/**
 * This is a helper class contains all parameters the Ordrec need:
 * The thresholds t1 and beta. The rating value set and the number of
 * different rating values.
 *
 */
class OrdRecModel {
    private int levelCount;
    private double t1;
    private ArrayRealVector beta;
    private RealVector qtzValues;

    /**
     * The constructor of OrdRecParameter.
     * It use the quantized values of rating to initialize t1 and beta.
     * Each threshold is initialized as the mean of two contiguous rating values.
     * Since the index of quantizer is always an successive non-negative integer
     * begin from 0, so t1 will initialize as 0.5, and the interval between two
     * thresholds will be 1.
     * @param qtz The quantizer for ratings
     */
    OrdRecModel(Quantizer qtz) {
        qtzValues = qtz.getValues();
        levelCount = qtzValues.getDimension();
        t1 = (qtzValues.getEntry(0) + qtzValues.getEntry(1))/2;
        beta = new ArrayRealVector(levelCount - 2);

        double tr = t1;
        for (int i = 1; i <= beta.getDimension(); i++) {
            double trnext = (qtzValues.getEntry(i) + qtzValues.getEntry(i + 1)) * 0.5;
            beta.setEntry(i - 1, Math.log(trnext - tr));
            tr = trnext;
        }
    }

    /**
     * Get the first threshold t1
     *
     * @return the first threshold t1.
     */
    public double getT1() {
        return t1;
    }

    /**
     * Get beta set.
     *
     * @return beta set.
     */
    public RealVector getBeta() {
        return beta;
    }

    /**
     * Get the count of rating levelCount.
     *
     * @return s The number of different ratings.
     */
    public int getLevelCount() {
        return levelCount;
    }

    /**
     * Get the rth threshold.
     *
     * @param thresholdIndex The index of the threshold
     * @return the rth threshold.
     */
    public double getThreshold(int thresholdIndex) {
        double tr = t1;
        if(thresholdIndex < 0) {
            return Double.NEGATIVE_INFINITY;
        } else if(thresholdIndex == 0){
            return tr;
        } else if(thresholdIndex > beta.getDimension()) {
            return Double.POSITIVE_INFINITY;
        } else {
            for(int k = 0; k < thresholdIndex; k++) {
                tr += Math.exp(beta.getEntry(k));
            }
            return tr;
        }
    }

    /**
     * Get the probability of P(rui&lt;=r|Theta)
     *
     * @param score The score of user uid and item iid.
     * @param r The index of rth threshold.
     * @return The probability
     */
    public double getProbLE(double score, int r) {

        return 1/(1 + Math.exp(score - getThreshold(r)));
    }

    /**
     * Get the probability of P(rui=r|Theta)
     *
     * @param score The score of user uid and item iid.
     * @param r The index of rth threshold.
     * @return The probability
     */
    public double getProbEQ(double score, int r) {
        return getProbLE(score, r) - getProbLE(score, r-1);
    }

    /**
     * Get the probability distribution according to score and thresholds
     * @param score The score
     * @param vec The MutableVec to be filled in.
     */
    public void getProbDistribution(double score, RealVector vec) {
        double pre = getProbLE(score, 0);
        vec.setEntry(0, pre);
        for(int i = 1; i < getLevelCount(); i++) {
            double pro = getProbLE(score, i);
            vec.setEntry(i, pro - pre);
            pre = pro;
        }

    }

    public void update(double dt1, RealVector dbeta) {
        t1 += dt1;
        beta.combineToSelf(1, 1, dbeta);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OrdRecParams(t1=")
          .append(t1)
          .append(", beta=")
          .append(beta)
          .append(")");
        return sb.toString();
    }
}
