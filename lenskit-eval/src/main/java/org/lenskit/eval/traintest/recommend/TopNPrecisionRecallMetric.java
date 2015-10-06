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
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.StringUtils;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.util.math.Scalars;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A metric to compute the precision and recall of a recommender given a 
 * set of candidate items to recommend from and a set of desired items.  The aggregate results are
 * means of the user results.
 * 
 * This can be used to compute metrics like fallout (probability that a 
 * recommendation is bad) by configuring bad items as the test item set.
 *
 * This metric is registered under the name `pr`.  It has two configuration parameters:
 *
 * `suffix`
 * :   a suffix to append to the column name
 *
 * `goodItems`
 * :   an item selector expression. The default is the user's test items.
 */
public class TopNPrecisionRecallMetric extends TopNMetric<TopNPrecisionRecallMetric.Context> {
    private final String suffix;
    private final ItemSelector goodItems;

    /**
     * Construct a new precision-recall metric using the user's test items as good.
     */
    public TopNPrecisionRecallMetric() {
        this(ItemSelector.userTestItems(), null);
    }

    /**
     * Construct a precision-reacll metric from a spec.
     * @param spec The precision-recall metric.
     */
    @JsonCreator
    public TopNPrecisionRecallMetric(PRMetricSpec spec) {
        this(ItemSelector.compileSelector(StringUtils.defaultString(spec.getGoodItems(), "user.testItems")),
             spec.getSuffix());
    }

    /**
     * Construct a new recall and precision top n metric
     * @param good an item selector for the good items.
     * @param sfx the suffix label for this evaluation, or {@code null} for no suffix.
     */
    public TopNPrecisionRecallMetric(ItemSelector good, String sfx) {
        super(PresRecResult.class, PresRecResult.class, sfx);
        suffix = sfx;
        goodItems = good;
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultList recs, Context context) {
        int tp = 0;

        LongSet items = goodItems.selectItems(context.universe, user);

        for(Result res: recs) {
            if(items.contains(res.getId())) {
                tp += 1;
            }
        }

        if (items.size() > 0 && recs.size() > 0) {
            // if both the items set and recommendations are non-empty (no division by 0).
            double precision = (double) tp / recs.size();
            double recall = (double) tp / items.size();
            context.addUser(precision, recall);
            return new PresRecResult(precision, recall);
        } else {
            return MetricResult.empty();
        }
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, org.lenskit.api.Recommender recommender) {
        return new Context(dataSet.getAllItems());
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return context.finish().withSuffix(suffix);
    }

    public static class PresRecResult extends TypedMetricResult {
        @MetricColumn("Precision")
        public final double precision;
        @MetricColumn("Recall")
        public final double recall;

        public PresRecResult(double prec, double rec) {
            precision = prec;
            recall = rec;
        }

        @MetricColumn("F1")
        public double getF1() {
            double denom = precision + recall;
            if (Scalars.isZero(denom)) {
                // we'll pretend the harmonic mean of 0 is 0.
                return 0;
            } else {
                return 2 * precision * recall / denom;
            }
        }
    }

    public static class Context {
        LongSet universe;
        double totalPrecision = 0;
        double totalRecall = 0;
        int nusers = 0;

        public Context(LongSet items) {
            universe = items;
        }

        private void addUser(double prec, double rec) {
            totalPrecision += prec;
            totalRecall += rec;
            nusers += 1;
        }

        public PresRecResult finish() {
            if (nusers > 0) {
                return new PresRecResult(totalPrecision / nusers, totalRecall / nusers);
            } else {
                return null;
            }
        }
    }
}
