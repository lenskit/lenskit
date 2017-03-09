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
package org.grouplens.lenskit.transform.truncate;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.math.Vectors;

import java.io.Serializable;

/**
 * A {@code VectorTruncator} that will retain the top n entries.
 */
@Shareable
public class TopNTruncator implements VectorTruncator, Serializable {

    private static final long serialVersionUID = 1L;

    private final Threshold threshold;
    private final int n;

    public TopNTruncator(int n, Threshold threshold) {
        this.n = n;
        if (threshold != null) {
            this.threshold = threshold;
        } else {
            this.threshold = null;
        }
    }

    public TopNTruncator(int n) {
        this(n, null);
    }

    @Override
    public Long2DoubleMap truncate(Long2DoubleMap v) {
        TopNLong2DoubleAccumulator accumulator = new TopNLong2DoubleAccumulator(n);
        for (Long2DoubleMap.Entry e : Vectors.fastEntries(v)) {
            double x = e.getDoubleValue();
            if (threshold == null || threshold.retain(x)) {
                accumulator.put(e.getLongKey(), x);
            }
        }
        return accumulator.finishMap();
    }
}
