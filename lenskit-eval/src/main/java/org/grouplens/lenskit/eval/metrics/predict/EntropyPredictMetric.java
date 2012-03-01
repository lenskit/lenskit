/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.metrics.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;
import org.grouplens.lenskit.norm.Quantizer;
import org.grouplens.lenskit.vectors.SparseVector;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluate a recommender's prediction accuracy by computing the mutual 
 * information between the ratings and the prediction. This tells us the amount
 * of information our predictions can tell the user about our ratings. 
 * @author Daniel Kluver <kluver@cs.umn.edu>
 *
 */
public class EntropyPredictMetric implements PredictEvalMetric {
    private static final Logger logger = LoggerFactory.getLogger(EntropyPredictMetric.class);
    private static final String[] COLUMNS = {"Entropy.ofRating.ByUser","Entropy.ofPredictions.byUser", "Information.ByUser"};

    @Override
    public Accumulator makeAccumulator(TTDataSet ds) {
        return new Accum(ds.getPreferenceDomain());
    }

    @Override
    public String[] getColumnLabels() {
        return COLUMNS;
    }

    class Accum implements Accumulator {
        private Quantizer quantizer;
                
        private double informationSum = 0.0;
        private double ratingEntropySum = 0.0;
        private double predictionEntropySum = 0.0;
        private int nusers = 0;
        
        public Accum(PreferenceDomain preferenceDomain) {
            quantizer = new PreferenceDomainQuantizer(preferenceDomain);
        }

        @Override
        public void evaluatePredictions(long user, SparseVector ratings,
                                        SparseVector predictions) {

            double n = 0.0;
            // indexed by prediction then rating.
            double[][] jointDistribution = new double[quantizer.getCount()][quantizer.getCount()];
            
            for (Long2DoubleMap.Entry e: predictions.fast()) {
                if (Double.isNaN(e.getDoubleValue())) continue;
                int pred = quantizer.apply(e.getDoubleValue());
                int rating = quantizer.apply(ratings.get(e.getLongKey()));
                jointDistribution[pred][rating]++;
                n++;
            }
            
            if (n > 0) {
                double[] ratingDistribution = new double[quantizer.getCount()];
                double[] predDistribution = new double[quantizer.getCount()];
                
                for (int pred = 0; pred < quantizer.getCount(); pred++) {
                    for (int rating = 0; rating < quantizer.getCount(); rating++) {
                        // divide by n to get joint probability from our frequency counts.
                        jointDistribution[pred][rating] /= n;
                        ratingDistribution[rating] += jointDistribution[pred][rating];
                        predDistribution[pred] += jointDistribution[pred][rating];
                    }
                }
                
                informationSum += mutualInfo(ratingDistribution, predDistribution, jointDistribution);
                ratingEntropySum += entropy(ratingDistribution);
                predictionEntropySum += entropy(predDistribution);
                nusers ++;
            }
        }

        private double entropy(double[] distribution) {
            double result = 0.0;
            for(int i = 0; i < quantizer.getCount(); i++) {
                if(distribution[i] != 0.0) {
                    result += distribution[i]*Math.log(distribution[i])/Math.log(2);
                }
            }
            return -result;
        }

        private double mutualInfo(double[] ratingDistribution, double[] predDistribution, double[][] jointDistribution) {
            double info = 0.0;
            for (int pred = 0; pred < quantizer.getCount(); pred++) {
                for (int rating = 0; rating < quantizer.getCount() ; rating++) {
                    double joint = jointDistribution[pred][rating];
                    if(joint != 0) {
                        info += joint * Math.log(joint/(ratingDistribution[rating]*predDistribution[pred]))/Math.log(2);
                    }
                }
            }
            return info;
        }

        @Override
        public String[] results() {
            logger.info("H(rating|user): {}", ratingEntropySum / nusers);
            logger.info("H(prediction|user): {}", predictionEntropySum / nusers);
            logger.info("I(rating;prediction): {}", informationSum / nusers);
            return new String[] {
                    Double.toString(ratingEntropySum / nusers),
                    Double.toString(predictionEntropySum / nusers),
                    Double.toString(informationSum / nusers)
            };
        }

    }
}
