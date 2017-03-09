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
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.inject.Shareable;
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * A {@code VectorTruncator} that will retain entries with values
 * that are accepted by some {@code Threshold}.
 */
@Shareable
public class ThresholdTruncator implements VectorTruncator, Serializable {

    private static final long serialVersionUID = 1L;

    private final Threshold threshold;

    @Inject
    public ThresholdTruncator(Threshold threshold) {
        this.threshold = threshold;
    }

    @Override
    public Long2DoubleMap truncate(Long2DoubleMap v) {
        Long2DoubleMap res = new Long2DoubleOpenHashMap(v.size());
        for (Long2DoubleMap.Entry e: Vectors.fastEntries(v)) {
            if (threshold.retain(e.getDoubleValue())) {
                res.put(e.getLongKey(), e.getDoubleValue());
            }
        }
        return res;
    }
}
