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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Measure the nDCG of the top-N recommendations, using ratings as scores.
 *
 * This metric is registered with the type name `ndcg`.
 */
public class TopNNDCGMetric extends ListOnlyTopNMetric<Mean> {
    private static final Logger logger = LoggerFactory.getLogger(TopNNDCGMetric.class);
    public static final String DEFAULT_COLUMN = "nDCG";
    private final String columnName;
    private final Discount discount;
    private final String gainAttribute;

    /**
     * Create an nDCG metric with log-2 discounting.
     */
    public TopNNDCGMetric() {
        this(Discounts.log2(), null);
    }

    /**
     * Create an nDCG metric with a default name.
     * @param disc The discount to apply.
     */
    public TopNNDCGMetric(Discount disc) {
        this(disc, null);
    }

    /**
     * Construct a top-N nDCG metric from a spec.
     * @param spec The spec.
     */
    @JsonCreator
    public TopNNDCGMetric(Spec spec) {
        this(spec.getParsedDiscount(), spec.getColumnName(), spec.getGainAttribute());
    }

    /**
     * Construct a new nDCG Top-N metric.
     * @param disc The discount to apply.
     * @param name The column name to use.
     */
    public TopNNDCGMetric(Discount disc, String name) {
        this(disc, name, CommonAttributes.RATING.getName());
    }

    /**
     * Construct a new nDCG Top-N metric.
     * @param disc The discount to apply.
     * @param name The column name to use.
     */
    public TopNNDCGMetric(Discount disc, String name, String attr) {
        super(Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)),
              Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)));
        columnName = StringUtils.defaultString(name, DEFAULT_COLUMN);
        discount = disc;
        gainAttribute = attr;
    }

    @Nullable
    @Override
    public Mean createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Mean();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Mean context) {
        return MetricResult.singleton(columnName, context.getResult());
    }

    @Nonnull
    @Override
    public MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recommendations, Mean context) {
        if (recommendations == null) {
            return MetricResult.empty();
        }

        Long2DoubleMap ratings = new Long2DoubleOpenHashMap();
        for (Entity e: user.getTestHistory()) {
            long item = e.getLong(CommonAttributes.ITEM_ID);
            Object av = e.get(gainAttribute);
            if (av instanceof Number) {
                ratings.put(item, ((Number) av).doubleValue());
            } else {
                throw new IllegalArgumentException("value " + av + " for attribute " + gainAttribute + " is not numeric");
            }
        }

        List<Long> ideal =
                ratings.keySet()
                       .stream()
                       .sorted(LongUtils.keyValueComparator(ratings).reversed())
                       .limit(targetLength >= 0 ? targetLength : ratings.size())
                       .collect(Collectors.toList());
        double idealGain = computeDCG(ideal, ratings);

        double gain = computeDCG(recommendations, ratings);

        double score = gain / idealGain;

        synchronized (context) {
            context.increment(score);
        }
        return MetricResult.singleton(columnName, score);
    }

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     */
    double computeDCG(List<Long> items, Long2DoubleFunction values) {
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
        private String attribute;

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

        public String getGainAttribute() {
            if (attribute == null) {
                return CommonAttributes.RATING.getName();
            } else {
                return attribute;
            }
        }

        public void setGainAttribute(String attr) {
            attribute = attr;
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
