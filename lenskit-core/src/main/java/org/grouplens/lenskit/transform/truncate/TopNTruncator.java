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
package org.grouplens.lenskit.transform.truncate;

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import java.io.Serializable;

/**
 * A {@code VectorTruncator} that will retain the top n entries.
 */
@Shareable
public class TopNTruncator implements VectorTruncator, Serializable {

    private static final long serialVersionUID = 1L;

    private final ThresholdTruncator threshold;
    private final int n;

    public TopNTruncator(int n, Threshold threshold) {
        this.n = n;
        if (threshold != null) {
            this.threshold = new ThresholdTruncator(threshold);
        } else {
            this.threshold = null;
        }
    }

    public TopNTruncator(int n) {
        this(n, null);
    }

    @Override
    public void truncate(MutableSparseVector v) {
        if (threshold != null) {
            threshold.truncate(v);
        }

        TopNScoredItemAccumulator accumulator = new TopNScoredItemAccumulator(n);
        for (VectorEntry e : v.fast(VectorEntry.State.SET)) {
            accumulator.put(e.getKey(), e.getValue());
        }
        MutableSparseVector truncated = accumulator.finishVector();

        // retain only the truncated keys
        v.keySet().retainAll(truncated.keySet());
    }
}
