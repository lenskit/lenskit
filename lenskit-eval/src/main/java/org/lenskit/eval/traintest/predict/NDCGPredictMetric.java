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
package org.lenskit.eval.traintest.predict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparators;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.api.ResultMap;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.Discount;
import org.lenskit.eval.traintest.metrics.Discounts;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Evaluate a recommender's predictions with normalized discounted cumulative gain.
 *
 * <p>This is a prediction evaluator that uses base-2 nDCG to evaluate recommender
 * accuracy. The items are ordered by predicted preference and the nDCG is
 * computed using the user's real rating as the gain for each item. Doing this
 * only over the queried items, rather than in the general recommend condition,
 * avoids penalizing recommenders for recommending items that would be better
 * if the user had known about them and provided ratings (e.g., for doing their
 * job).
 *
 * <p>nDCG is computed per-user and then averaged over all users.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NDCGPredictMetric extends PredictMetric<Mean> {
    private static final Logger logger = LoggerFactory.getLogger(NDCGPredictMetric.class);
    public static final String DEFAULT_COLUMN = "Predict.nDCG";
    private final String columnName;
    private final Discount discount;

    /**
     * Create a new log_2 nDCG metric with column name "Predict.nDCG".
     */
    public NDCGPredictMetric() {
        this(Discounts.log2(), DEFAULT_COLUMN);
    }

    /**
     * Create a new nDCG metric with column name "Predict.nDCG".
     * @param disc The discount.
     */
    public NDCGPredictMetric(Discount disc) {
        this(disc, DEFAULT_COLUMN);
    }

    /**
     * Construct a predict metric from a spec.
     * @param spec The metric spec.
     */
    @JsonCreator
    public NDCGPredictMetric(Spec spec) {
        this(spec.getParsedDiscount(),
             StringUtils.defaultString(spec.getColumnName(), DEFAULT_COLUMN));
    }

    /**
     * Create a new nDCG metric.
     * @param disc The discount.
     * @param name The column name.
     */
    public NDCGPredictMetric(Discount disc, String name) {
        super(Lists.newArrayList(name, name+".Raw"), Lists.newArrayList(name));
        columnName = name;
        discount = disc;
    }

    @Nullable
    @Override
    public Mean createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Mean();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Mean context) {
        logger.warn("Predict nDCG is deprecated, use nDCG in a rank context");
        return MetricResult.singleton(columnName, context.getResult());
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultMap predictions, Mean context) {
        if (predictions == null || predictions.isEmpty()) {
            return MetricResult.empty();
        }
        Long2DoubleMap ratings = user.getTestRatings();
        long[] ideal = ratings.keySet().toLongArray();
        LongArrays.quickSort(ideal, LongComparators.oppositeComparator(LongUtils.keyValueComparator(ratings)));
        long[] actual = LongUtils.asLongSet(predictions.keySet()).toLongArray();
        LongArrays.quickSort(actual, LongComparators.oppositeComparator(
                LongUtils.keyValueComparator(
                        LongUtils.asLong2DoubleMap(predictions.scoreMap()))));
        double idealGain = computeDCG(ideal, ratings);
        double gain = computeDCG(actual, ratings);
        logger.debug("user {} has gain of {} (ideal {})", user.getUserId(), gain, idealGain);
        double score = gain / idealGain;
        synchronized (context) {
            context.increment(score);
        }
        ImmutableMap.Builder<String,Double> results = ImmutableMap.builder();
        return MetricResult.fromMap(results.put(columnName, score)
                                           .put(columnName + ".Raw", gain)
                                           .build());
    }

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     */
    double computeDCG(long[] items, Long2DoubleFunction values) {
        double gain = 0;
        int rank = 0;

        for (long item: items) {
            final double v = values.get(item);
            rank++;
            gain += v * discount.discount(rank);
        }

        return gain;
    }

    /**
     * Specification for configuring nDCG metrics.
     */
    @JsonIgnoreProperties("type")
    public static class Spec {
        private String name;
        private String discount;

        public String getColumnName() {
            return name;
        }

        public void setColumnName(String name) {
            this.name = name;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        public Discount getParsedDiscount() {
            if (discount == null) {
                return Discounts.log2();
            } else {
                return Discounts.parse(discount);
            }
        }
    }
}
