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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.Math.log;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NDCGTopNMetric extends AbstractMetric<MeanAccumulator, NDCGTopNMetric.Result, NDCGTopNMetric.Result> {
    private static final Logger logger = LoggerFactory.getLogger(NDCGTopNMetric.class);

    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final String suffix;

    /**
     * Construct a new nDCG Top-N metric.
     * @param listSize The number of recommendations to fetch.
     * @param candidates The candidate selector.
     * @param exclude The exclude selector.
     */
    public NDCGTopNMetric(String lbl, int listSize, ItemSelector candidates, ItemSelector exclude) {
        super(Result.class, Result.class);
        suffix = lbl;
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
    }

    @Override
    public MeanAccumulator createAccumulator(Attributed algo, TTDataSet ds, Recommender rec) {
        return new MeanAccumulator();
    }

    @Override
    protected String getSuffix() {
        return suffix;
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

    @Override
    public Result doMeasureUser(TestUser user, MeanAccumulator accum) {
        List<ScoredId> recommendations;
        recommendations = user.getRecommendations(listSize, candidates, exclude);
        if (recommendations == null) {
            return null;
        }

        SparseVector ratings = user.getTestRatings();
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

        accum.add(score);
        return new Result(score);
    }

    @Override
    protected Result getTypedResults(MeanAccumulator accum) {
        return new Result(accum.getMean());
    }

    public static class Result {
        @ResultColumn("TopN.nDCG")
        public final double nDCG;

        public Result(double v) {
            nDCG = v;
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, NDCGTopNMetric>{
        private String suffix;
        
        /**
         * Get the column label for this metric.
         * @return The column label.
         */
        public String getSuffix() {
            return suffix;
        }

        /**
         * Set the column label for this metric.
         * @param l The column label
         * @return The builder (for chaining).
         */
        public Builder setSuffix(String l) {
            Preconditions.checkNotNull(l, "label cannot be null");
            suffix = l;
            return this;
        }

        @Override
        public NDCGTopNMetric build() {
            return new NDCGTopNMetric(suffix, listSize, candidates, exclude);
        }
    }

}
