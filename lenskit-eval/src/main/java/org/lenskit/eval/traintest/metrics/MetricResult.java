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
package org.lenskit.eval.traintest.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing metric results.
 */
public abstract class MetricResult {
    /**
     * Get the column values for this metric result.
     * @return The values for the result.
     */
    public abstract Map<String,Object> getValues();

    /**
     * Add a suffix to this metric result's column names.
     * @param sfx The suffix to add, or `null` for no change.
     * @return The converted metric.
     */
    public MetricResult withSuffix(String sfx) {
        if (sfx == null) {
            return this;
        }

        Map<String,Object> newData = new HashMap<>();
        for (Map.Entry<String,Object> e: getValues().entrySet()) {
            newData.put(e.getKey() + "." + sfx, e.getValue());
        }
        return fromMap(newData);
    }

    /**
     * Create an empty metric result.
     * @return An empty metric result.
     */
    public static MetricResult empty() {
        return fromMap(Collections.<String, Object>emptyMap());
    }

    /**
     * Create an empty metric result.
     * @return An empty metric result.
     */
    public static MetricResult fromMap(Map<String,?> values) {
        return new MapMetricResult(values);
    }

    /**
     * Create a singleton map result.
     * @param name The column name.
     * @param value The column value.
     * @return The map result.
     */
    public static MetricResult singleton(String name, Object value) {
        return fromMap(Collections.singletonMap(name, value));
    }
}
