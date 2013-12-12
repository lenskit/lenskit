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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.symbols.Symbol;

import java.util.List;

/**
 * A suite of metrics for a train-test evaluaiton.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MeasurementSuite {
    private final List<TestUserMetric> testUserMetrics;
    private final List<ModelMetric> modelMetrics;
    private final List<Pair<Symbol, String>> predictionChannels;

    public MeasurementSuite(List<TestUserMetric> uMetrics,
                            List<ModelMetric> mMetrics,
                            List<Pair<Symbol, String>> pChannels) {
        testUserMetrics = uMetrics;
        modelMetrics = mMetrics;
        predictionChannels = pChannels;
    }

    public List<TestUserMetric> getTestUserMetrics() {
        return testUserMetrics;
    }

    public List<ModelMetric> getModelMetrics() {
        return modelMetrics;
    }

    /**
     * Get the number of columns used for aggregates of user metrics.
     * @return The number of aggregate user metric columns.
     */
    public int getAggregateUserColumnCount() {
        int n = 0;
        for (TestUserMetric m: testUserMetrics) {
            n += m.getColumnLabels().size();
        }
        return n;
    }

    /**
     * Get the number of columns used for per-user metrics.
     * @return The number of per-user metric columns.
     */
    public int getUserColumnCount() {
        int n = 0;
        for (TestUserMetric m: testUserMetrics) {
            n += m.getUserColumnLabels().size();
        }
        return n;
    }

    /**
     * Get the number of columns used for model metrics.
     * @return The model metric column count.
     */
    public int getModelColumnCount() {
        int n = 0;
        for (ModelMetric m: modelMetrics) {
            n += m.getColumnLabels().size();
        }
        return n;
    }

    public Iterable<Metric<TrainTestEvalTask>> getAllMetrics() {
        return Iterables.concat(testUserMetrics, modelMetrics);
    }

    public List<Pair<Symbol, String>> getPredictionChannels() {
        return predictionChannels;
    }
}
