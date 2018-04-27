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
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.util.math.Scalars;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Measure the nDPM of the top-N recommendations, using rankings.
 * This metric is registered with the type name `ndpm`.
 * The paper used as a reference for this implementation is http://www2.cs.uregina.ca/~yyao/PAPERS/jasis_ndpm.pdf.
 */
public class TopNNDPMMetric extends ListOnlyTopNMetric<Mean> {
    public static final String DEFAULT_COLUMN = "TopN.nDPM";

    /**
     * Construct a top-N nDCG metric from a spec.
     * @param spec The spec.
     */
    @JsonCreator
    public TopNNDPMMetric(Spec spec) {this(spec.getColumnName());
    }

    /**
     * Construct a new nDPM Top-N metric.
     */
    public TopNNDPMMetric(String name) {
        super(Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)),
              Collections.singletonList(StringUtils.defaultString(name, DEFAULT_COLUMN)));
    }

    @Nullable
    @Override
    public Mean createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Mean();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Mean context) {
        return MetricResult.singleton(DEFAULT_COLUMN, context.getResult());
    }

    @Nonnull
    @Override
    public MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recommendations, Mean context) {
        if (recommendations == null) {
            return MetricResult.empty();
        }

        Long2DoubleMap ratings = user.getTestRatings();

        long[] actual = recommendations.toLongArray();

        double dpm = computeDPM(actual, ratings);

        double normalizingFactor = computeNormalizingFactor(actual, ratings);

        double nDPM = dpm / normalizingFactor; // Normalized nDPM

        synchronized (context) {
            context.increment(nDPM);
        }

        return MetricResult.singleton(DEFAULT_COLUMN, nDPM);
    }

    /**
     * Compute dpm of list of items, with respect to user's ratings.
     */

    double computeDPM(long [] actual_item, Long2DoubleFunction value) {
        int nCompatible = 0;
        int nDisagree = 0;

        for(int i = 0; i < actual_item.length; i++){
            for(int j = i+1; j < actual_item.length; j++){
                double valueOne;
                double valueTwo;

                if (value.containsKey(actual_item[i])) {
                    valueOne = value.get(actual_item[i]);

                    if (value.containsKey(actual_item[j])) {
                        valueTwo = value.get(actual_item[j]);

                        if (Scalars.isZero(valueOne - valueTwo)) {
                            nCompatible++;
                        }
                        if(valueOne < valueTwo){
                            nDisagree++;
                        }
                    }
                }
            }
        }

        double dpm = (2 * nDisagree) + nCompatible;

        return dpm;
    }

    double computeNormalizingFactor(long [] actual_item, Long2DoubleFunction value) {
        int npairs = 0;

        for(int i = 0; i < actual_item.length; i++) {
            for(int j = i+1; j < actual_item.length; j++) {
                double valueOne;
                double valueTwo;

                if (value.containsKey(actual_item[i])) {
                    valueOne = value.get(actual_item[i]);

                    if (value.containsKey(actual_item[j])) {
                        valueTwo = value.get(actual_item[j]);
                        if(valueOne < valueTwo || valueOne > valueTwo) {
                            npairs++;
                        }
                    }
                }
            }
        }

        double denominator;

        if(npairs > 0){
            denominator = 2 * npairs;
        }
        else{
            denominator = 1;
        }

        return denominator;
    }

    /**
     * Specification for configuring nDPM metrics.
     */
    @JsonIgnoreProperties("type")
    public static class Spec {

        public String getColumnName() {
            return DEFAULT_COLUMN;
        }
    }
}
