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
package org.lenskit.eval.traintest.metrics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @Nonnull
    public abstract Map<String,Object> getValues();

    /**
     * Add a suffix to this metric result's column names.  The resulting keys are of the form "key.sfx".
     * @param sfx The suffix to add, or `null` for no change.
     * @return The converted metric.
     */
    @Nonnull
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
     * Add a prefix to this metric result's column names.  The resulting keys are of the form "pfx.key".
     * @param pfx The prefix to add, or `null` for no change.
     * @return The converted metric.
     */
    @Nonnull
    public MetricResult withPrefix(String pfx) {
        if (pfx == null) {
            return this;
        }

        Map<String,Object> newData = new HashMap<>();
        for (Map.Entry<String,Object> e: getValues().entrySet()) {
            newData.put(pfx + "." + e.getKey(), e.getValue());
        }
        return fromMap(newData);
    }

    /**
     * Create an empty metric result.
     * @return An empty metric result.
     */
    @Nonnull
    public static MetricResult empty() {
        return fromMap(Collections.<String, Object>emptyMap());
    }

    /**
     * Convert a null result to an empty result.
     * @param result The result.
     * @return The result, or {@link #empty()} if it is null.
     */
    @Nonnull
    public static MetricResult fromNullable(@Nullable MetricResult result) {
        return result != null ? result : empty();
    }

    /**
     * Create an empty metric result.
     * @return An empty metric result.
     */
    @Nonnull
    public static MetricResult fromMap(Map<String,?> values) {
        return new MapMetricResult(values);
    }

    /**
     * Create a singleton map result.
     * @param name The column name.
     * @param value The column value.
     * @return The map result.
     */
    @Nonnull
    public static MetricResult singleton(String name, Object value) {
        return fromMap(Collections.singletonMap(name, value));
    }
}
