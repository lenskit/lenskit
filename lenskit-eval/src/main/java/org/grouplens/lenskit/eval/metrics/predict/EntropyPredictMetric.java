/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.transform.quantize.PreferenceDomainQuantizer;
import org.grouplens.lenskit.transform.quantize.Quantizer;
import org.grouplens.lenskit.util.statistics.MutualInformationAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluate a recommender's prediction accuracy by computing the mutual
 * information between the ratings and the prediction. This tells us the amount
 * of information our predictions can tell the user about our ratings.
 *
 * @author Daniel Kluver <kluver@cs.umn.edu>
 */
public class EntropyPredictMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(EntropyPredictMetric.class);
    private static final String[] COLUMNS = {"Entropy.ofRating.ByUser", "Entropy.ofPredictions.byUser", "Information.ByUser"};

    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm, TTDataSet dataSet) {
        return new Accum(dataSet.getPreferenceDomain());
    }

    @Override
    public String[] getUserColumnLabels() {
        return COLUMNS;
    }

    @Override
    public String[] getColumnLabels() {
        return COLUMNS;
    }

    class Accum implements TestUserMetricAccumulator {
        private Quantizer quantizer;

        private double informationSum = 0.0;
        private double ratingEntropySum = 0.0;
        private double predictionEntropySum = 0.0;
        private int nusers = 0;

        public Accum(PreferenceDomain preferenceDomain) {
            quantizer = new PreferenceDomainQuantizer(preferenceDomain);
        }

        @Override
        public String[] evaluate(TestUser user) {
            SparseVector ratings = user.getTestRatings();
            SparseVector predictions = user.getPredictions();

            // TODO Re-use accumulators
            MutualInformationAccumulator accum = new MutualInformationAccumulator(quantizer.getCount());

            for (Vectors.EntryPair e: Vectors.paired(ratings, predictions)) {
                accum.count(quantizer.apply(e.getValue1()),
                            quantizer.apply(e.getValue2()));
            }

            if (accum.getCount() > 0) {
                double ratingEntropy = accum.getV1Entropy();
                double predEntropy = accum.getV2Entropy();
                double info = accum.getMutualInformation();
                informationSum += info;
                ratingEntropySum += ratingEntropy;
                predictionEntropySum += predEntropy;
                nusers += 1;
                return new String[] {
                        Double.toString(ratingEntropy),
                        Double.toString(predEntropy),
                        Double.toString(info)
                };
            } else {
                return null;
            }
        }

        @Override
        public String[] finalResults() {
            logger.info("H(rating|user): {}", ratingEntropySum / nusers);
            logger.info("H(prediction|user): {}", predictionEntropySum / nusers);
            logger.info("I(rating;prediction): {}", informationSum / nusers);
            return new String[]{
                    Double.toString(ratingEntropySum / nusers),
                    Double.toString(predictionEntropySum / nusers),
                    Double.toString(informationSum / nusers)
            };
        }
    }
}
