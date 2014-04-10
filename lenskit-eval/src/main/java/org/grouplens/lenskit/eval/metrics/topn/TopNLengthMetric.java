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

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;

import java.util.List;

/**
 * Metric that measures how long a TopN list actually is.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNLengthMetric extends AbstractMetric<MeanAccumulator, TopNLengthMetric.Result, TopNLengthMetric.Result> {
    private final String suffix;
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;

    public TopNLengthMetric(String sfx, int listSize, ItemSelector candidates, ItemSelector exclude) {
        super(Result.class, Result.class);
        suffix = sfx;
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
    }

    @Override
    protected String getSuffix() {
        return suffix;
    }

    @Override
    public MeanAccumulator createAccumulator(Attributed algo, TTDataSet ds, Recommender rec) {
        return new MeanAccumulator();
    }

    @Override
    public Result doMeasureUser(TestUser user, MeanAccumulator accumulator) {
        List<ScoredId> recs;
        recs = user.getRecommendations(listSize, candidates, exclude);
        if (recs == null) {
            return null;
        }
        int n = recs.size();
        accumulator.add(n);
        return new Result(n);
    }

    @Override
    protected Result getTypedResults(MeanAccumulator accum) {
        return new Result(accum.getMean());
    }

    public static class Result {
        @ResultColumn("TopN.ActualLength")
        public final double length;

        public Result(double len) {
            length = len;
        }
    }

    /**
     * Build a Top-N length metric to measure Top-N lists.
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, TopNLengthMetric> {
        private String suffix;

        /**
         * Get the column suffix for this metric.
         * @return The column suffix.
         */
        public String getSuffix() {
            return suffix;
        }

        /**
         * Set the column suffix for this metric.
         * @param l The column suffix
         * @return The builder (for chaining).
         */
        public Builder setSuffix(String l) {
            suffix = l;
            return this;
        }

        @Override
        public TopNLengthMetric build() {
            return new TopNLengthMetric(suffix, listSize, candidates, exclude);
        }
    }

}
