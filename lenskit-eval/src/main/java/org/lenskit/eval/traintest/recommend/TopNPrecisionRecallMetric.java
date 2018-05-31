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
package org.lenskit.eval.traintest.recommend;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.StringUtils;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.util.math.Scalars;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
public class TopNPrecisionRecallMetric extends ListOnlyTopNMetric<TopNPrecisionRecallMetric.Context> {
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
    public MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recs, Context context) {
        int tp = 0;

        LongSet items = goodItems.selectItems(context.universe, rec, user);

        for (long item: recs) {
            if(items.contains(item)) {
                tp += 1;
            }
        }

        if (items.size() > 0 && recs.size() > 0) {
            // if both the items set and recommendations are non-empty (no division by 0).
            double precision = (double) tp / recs.size();
            double recall = (double) tp / items.size();
            context.addUser(precision, recall);
            return new PresRecResult(precision, recall).withSuffix(suffix);
        } else {
            context.addUser(0, 0);
            return new PresRecResult(0, 0).withSuffix(suffix);
        }
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine rec) {
        return new Context(dataSet.getAllItems());
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return MetricResult.fromNullable(context.finish())
                           .withSuffix(suffix);
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
        final LongSet universe;
        double totalPrecision = 0;
        double totalRecall = 0;
        int nusers = 0;

        public Context(LongSet items) {
            universe = items;
        }

        private synchronized void addUser(double prec, double rec) {
            totalPrecision += prec;
            totalRecall += rec;
            nusers += 1;
        }

        @Nullable
        public PresRecResult finish() {
            if (nusers > 0) {
                return new PresRecResult(totalPrecision / nusers, totalRecall / nusers);
            } else {
                return null;
            }
        }
    }
}
