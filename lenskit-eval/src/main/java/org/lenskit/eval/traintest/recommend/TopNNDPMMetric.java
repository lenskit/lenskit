/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
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
public class TopNNDPMMetric extends ListOnlyTopNMetric<MeanAccumulator> {
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
    public MeanAccumulator createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new MeanAccumulator();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(MeanAccumulator context) {
        return MetricResult.singleton(DEFAULT_COLUMN, context.getMean());
    }

    @Nonnull
    @Override
    public MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recommendations, MeanAccumulator context) {
        if (recommendations == null) {
            return MetricResult.empty();
        }

        Long2DoubleMap ratings = user.getTestRatings();

        long[] actual = recommendations.toLongArray();

        double dpm = computeDPM(actual, ratings);

        double normalizingFactor = computeNormalizingFactor(actual, ratings);

        double nDPM = dpm / normalizingFactor; // Normalized nDPM

        context.add(nDPM);

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
