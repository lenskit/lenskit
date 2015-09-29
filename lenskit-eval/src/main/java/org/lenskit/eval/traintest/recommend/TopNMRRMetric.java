/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.traintest.recommend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.specs.AbstractSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Compute the mean reciprocal rank.
 * 
 * This metric is registered with the type name `mrr`.
 */
public class TopNMRRMetric extends TopNMetric<TopNMRRMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TopNMRRMetric.class);

    private final ItemSelector goodItems;
    private final String suffix;

    @JsonCreator
    public TopNMRRMetric(Spec spec) {
        this(ItemSelector.compileSelector(spec.getGoodItems()),
             spec.getSuffix());
    }

    /**
     * Construct a new recall and precision top n metric
     * @param goodItems The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     * @param sfx A suffix to append to the metric.
     */
    public TopNMRRMetric(ItemSelector goodItems, String sfx) {
        super(AggregateResult.class, UserResult.class, sfx);
        this.goodItems = goodItems;
        suffix = sfx;
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, org.lenskit.api.Recommender recommender) {
        return new Context(dataSet.getTestData().getItemDAO().getItemIds());
    }

    @Nonnull
    @Override
    public AggregateResult getAggregateMeasurements(Context context) {
        return new AggregateResult(context);
    }

    @Nonnull
    @Override
    public UserResult measureUser(TestUser user, ResultList recommendations, Context context) {
        LongSet good = goodItems.selectItems(context.universe, user);
        if (good.isEmpty()) {
            logger.warn("no good items for user {}", user.getUserId());
        }

        Integer rank = null;
        int i = 0;
        for(Result res: recommendations) {
            i++;
            if(good.contains(res.getId())) {
                rank = i;
                break;
            }
        }

        UserResult result = new UserResult(rank);
        context.addUser(result);
        return result;
    }

    public static class UserResult extends TypedMetricResult {
        @ResultColumn("Rank")
        public final Integer rank;

        public UserResult(Integer r) {
            rank = r;
        }

        @ResultColumn("RecipRank")
        public double getRecipRank() {
            return rank == null ? 0 : 1.0 / rank;
        }
    }

    public static class AggregateResult extends TypedMetricResult {
        /**
         * The MRR over all users.  Users for whom no good items are included, and have a reciprocal
         * rank of 0.
         */
        @ResultColumn("MRR")
        public final double mrr;
        /**
         * The MRR over those users for whom a good item could be recommended.
         */
        @ResultColumn("MRR.OfGood")
        public final double goodMRR;

        public AggregateResult(Context accum) {
            this.mrr = accum.allMean.getMean();
            this.goodMRR = accum.goodMean.getMean();
        }
    }

    public static class Context {
        private final LongSet universe;
        private final MeanAccumulator allMean = new MeanAccumulator();
        private final MeanAccumulator goodMean = new MeanAccumulator();

        Context(LongSet universe) {
            this.universe = universe;
        }

        void addUser(UserResult ur) {
            allMean.add(ur.getRecipRank());
            if (ur.rank != null) {
                goodMean.add(ur.getRecipRank());
            }
        }
    }

    /**
     * Specification class for configuring the metric.
     */
    @JsonIgnoreProperties({"type"})
    public static class Spec extends AbstractSpec {
        private String goodItems;
        private String suffix;

        public String getGoodItems() {
            return goodItems;
        }

        public void setGoodItems(String goodItems) {
            this.goodItems = goodItems;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
    }
}
