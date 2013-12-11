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
package org.grouplens.lenskit.eval.metrics.topn;

import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Metric that measures how long a TopN list actually is.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNLengthMetric extends AbstractTestUserMetric {
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    public TopNLengthMetric(String lbl, int listSize, ItemSelector candidates, ItemSelector exclude) {
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        columns = ImmutableList.of(lbl);
    }

    @Override
    public Accum makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
        return new Accum();
    }

    @Override
    public List<String> getColumnLabels() {
        return columns;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return columns;
    }

    class Accum implements TestUserMetricAccumulator {
        double total = 0;
        int nusers = 0;

        @Nonnull
        @Override
        public List<Object> evaluate(TestUser user) {
            List<ScoredId> recs;
            recs = user.getRecommendations(listSize, candidates, exclude);
            if (recs == null) {
                return userRow();
            }
            int n = recs.size();
            total += n;
            nusers += 1;
            return userRow(n);
        }

        @Nonnull
        @Override
        public List<Object> finalResults() {
            if (nusers > 0) {
                return finalRow(total / nusers);
            } else {
                return finalRow();
            }
        }
    }
}
