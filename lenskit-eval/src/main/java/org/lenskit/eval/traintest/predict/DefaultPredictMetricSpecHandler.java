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
package org.lenskit.eval.traintest.predict;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.eval.traintest.metrics.Discount;
import org.lenskit.eval.traintest.metrics.Discounts;
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper;
import org.lenskit.specs.AbstractSpec;
import org.lenskit.specs.DynamicSpec;
import org.lenskit.specs.SpecHandler;

import java.io.IOException;

/**
 * Default spec handler for predict metrics.  It handles all the default metrics, and can instantiate additional
 * type-only metrics from <tt>META-INF/lenskit/predict-metrics.properties</tt> files on the classpath.
 */
@AutoService(SpecHandler.class)
public class DefaultPredictMetricSpecHandler implements SpecHandler {
    private MetricLoaderHelper helper;

    public DefaultPredictMetricSpecHandler() throws IOException {
        helper = new MetricLoaderHelper(ClassLoaders.inferDefault(DefaultPredictMetricSpecHandler.class),
                                        "predict-metrics");
    }

    @Override
    public <T> T build(Class<T> type, AbstractSpec spec) {
        if (!PredictMetric.class.isAssignableFrom(type)) {
            return null;
        }
        if (!(spec instanceof DynamicSpec)) {
            return null;
        }
        JsonNode node = ((DynamicSpec) spec).getJSON();
        return helper.createMetric(type, node);
    }
}
