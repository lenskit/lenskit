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
