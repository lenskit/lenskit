/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.eval.metrics.Metric;

import java.io.IOException;
import java.util.List;

/**
 * Create a metric for a train-test evaluation.  This interface allows a metric to control
 * its instantiation and lifecycle.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public abstract class MetricFactory<T> {
    public abstract Metric<T> createMetric(TrainTestEvalTask task) throws IOException;

    public abstract List<String> getColumnLabels();

    public abstract List<String> getUserColumnLabels();

    /**
     * Create a metric factory that returns the provided pre-instantiated metric.
     * @param m The metric.
     * @return A metric factory that returns {@code m}.
     */
    public static <T> MetricFactory<T> forMetric(Metric<T> m) {
        return new Preinstantiated<T>(m);
    }

    private static class Preinstantiated<T> extends MetricFactory<T> {
        private final Metric<T> metric;

        private Preinstantiated(Metric<T> m) {
            this.metric = m;
        }

        @Override
        public Metric<T> createMetric(TrainTestEvalTask task) {
            return metric;
        }

        @Override
        public List<String> getColumnLabels() {
            return metric.getColumnLabels();
        }

        @Override
        public List<String> getUserColumnLabels() {
            return metric.getUserColumnLabels();
        }
    }
}
