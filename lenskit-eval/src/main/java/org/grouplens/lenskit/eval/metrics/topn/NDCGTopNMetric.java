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
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static java.lang.Math.log;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NDCGTopNMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(NDCGTopNMetric.class);

    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    /**
     * Construct a new nDCG Top-N metric.
     * @param listSize The number of recommendations to fetch.
     * @param candidates The candidate selector.
     * @param exclude The exclude selector.
     */
    public NDCGTopNMetric(String lbl, int listSize, ItemSelector candidates, ItemSelector exclude) {
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

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     */
    static double computeDCG(LongList items, SparseVector values) {
        final double lg2 = log(2);

        double gain = 0;
        int rank = 0;

        LongIterator iit = items.iterator();
        while (iit.hasNext()) {
            final long item = iit.nextLong();
            final double v = values.get(item, 0);
            rank++;
            if (rank < 2) {
                gain += v;
            } else {
                gain += v * lg2 / log(rank);
            }
        }

        return gain;
    }

    class Accum implements TestUserMetricAccumulator {
        double total = 0;
        int nusers = 0;

        @Nonnull
        @Override
        public List<Object> evaluate(TestUser user) {
            List<ScoredId> recommendations;
            recommendations = user.getRecommendations(listSize, candidates, exclude);
            if (recommendations == null) {
                return userRow();
            }
            return evaluateRecommendations(user.getTestRatings(), recommendations);
        }

        List<Object> evaluateRecommendations(SparseVector ratings, List<ScoredId> recommendations) {
            LongList ideal = ratings.keysByValue(true);
            if (ideal.size() > listSize) {
                ideal = ideal.subList(0, listSize);
            }
            double idealGain = computeDCG(ideal, ratings);

            LongList actual = new LongArrayList(recommendations.size());
            for (ScoredId id: CollectionUtils.fast(recommendations)) {
                actual.add(id.getId());
            }
            double gain = computeDCG(actual, ratings);

            double score = gain / idealGain;
            total += score;
            nusers += 1;
            return userRow(score);
        }

        @Nonnull
        @Override
        public List<Object> finalResults() {
            if (nusers > 0) {
                double v = total / nusers;
                logger.info("Top-N nDCG: {}", v);
                return finalRow(v);
            } else {
                return finalRow();
            }
        }
    }
}
