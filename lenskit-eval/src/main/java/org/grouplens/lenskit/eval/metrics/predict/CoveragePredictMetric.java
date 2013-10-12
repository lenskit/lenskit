/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Simple evaluator that records user, rating and prediction counts and computes
 * recommender coverage over the queried items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CoveragePredictMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(CoveragePredictMetric.class);
    private static final ImmutableList<String> COLUMNS =
            ImmutableList.of("NUsers", "NAttempted", "NGood", "Coverage");
    private static final ImmutableList<String> USER_COLUMNS =
            ImmutableList.of("NAttempted", "NGood", "Coverage");

    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
        return new Accum();
    }

    @Override
    public List<String> getColumnLabels() {
        return COLUMNS;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return USER_COLUMNS;
    }

    class Accum implements TestUserMetricAccumulator {
        private int npreds = 0;
        private int ngood = 0;
        private int nusers = 0;

        @Nonnull
        @Override
        public Object[] evaluate(TestUser user) {
            SparseVector ratings = user.getTestRatings();
            SparseVector predictions;
            try {
                predictions = user.getPredictions();
            } catch (UnsupportedOperationException e) {
                return new Object[USER_COLUMNS.size()];
            }
            int n = 0;
            int good = 0;
            for (VectorEntry e : ratings.fast()) {
                n += 1;
                if (predictions.containsKey(e.getKey())) {
                    good += 1;
                }
            }
            npreds += n;
            ngood += good;
            nusers += 1;
            return new Object[]{n, good,
                                n > 0 ? (((double) good) / n) : null
            };
        }

        @Nonnull
        @Override
        public Object[] finalResults() {
            double coverage = (double) ngood / npreds;
            logger.info("Coverage: {}", coverage);

            return new Object[]{nusers, npreds, ngood, coverage};
        }

    }
}
