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
package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 * An accumulator for means associated with IDs.
 *
 * @since 1.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class IdMeanAccumulator {
    private Long2DoubleMap sums = new Long2DoubleOpenHashMap();
    private Long2IntMap counts = new Long2IntOpenHashMap();
    private double globalSum;
    private int globalCount;

    /**
     * Accumulate a value with an ID.
     * @param id The ID.
     * @param val The value.
     */
    public void put(long id, double val) {
        globalSum += val;
        globalCount += 1;
        // the maps return 0 for missing keys
        sums.put(id, sums.get(id) + val);
        counts.put(id, counts.get(id) + 1);
    }

    /**
     * Get the global mean.
     * @return The global mean value.
     */
    public double globalMean() {
        return globalSum / globalCount;
    }

    /**
     * Get the per-ID means.  Equivalent to {@code computeIdMeans(0, 0)}.
     * @return A vector of means for each ID that has been accumulated.
     * @see {@link #computeIdMeans(double, double)}
     */
    public ImmutableSparseVector idMeans() {
        return computeIdMeans(0, 0);
    }

    /**
     * Compute the means for each ID.  This is a generalized mean function, capable of offsetting
     * the individual values and damping the overall mean.  For an ID with \(n\) values \(x_1,\dots,x_n\),
     * offset \(y\) and damping \(\gamma\), it computes \(\frac{\sum_{i=1}^n x_i - ny}{n + \gamma}\).
     * If \(y\) is the global mean, this computes each ID's average deviation from the global mean.
     * If \(\gamma\) is additionally positive, then these average deviations are then damped towards
     * 0, effectively pretending that each ID has an additional \(\gamma\) values at exactly the
     * global mean.  If \(y=0\) and \(gamma > 0\), it pretends each ID has additional values at 0.
     *
     * <p>The prior (assumed value for additional values) is always 0 in the output domain.  If
     * \(y>0\), then the values are offset first, and then damped towards 0.  This method does
     * not yet support damping towards some other value; if you need actual damped means, where
     * each is damped towards the global mean, add the global mean to the resulting vector.
     *
     * @param offset  An offset to subtract from each value prior to averaging.
     * @param damping The damping term (see {@link #idMeanOffsets(double)}).
     * @return The vector of means.
     */
    public ImmutableSparseVector computeIdMeans(double offset, double damping) {
        MutableSparseVector v = new MutableSparseVector(sums);
        for (VectorEntry e: v.fast()) {
            final int n = counts.get(e.getKey());
            // if n <= 0, how did we get this item?
            assert n > 0;
            // compute a total to subtract offset from each accumulated value
            final double noff = n * offset;
            v.set(e, (e.getValue() - noff) / (n + damping));
        }
        return v.freeze();
    }

    /**
     * Compute mean offsets for each item.  Equivalent to {@code computeIdMeans(globalMean(), damping)}.
     *
     * @param damping The damping term.
     * @return A vector of mean offsets.
     * @see {@link #computeIdMeans(double, double)}}
     */
    public ImmutableSparseVector idMeanOffsets(double damping) {
        return computeIdMeans(globalMean(), damping);
    }

    /**
     * Compute offsets from the global mean for each ID.  This is equivalent to calling
     * {@linkplain #idMeanOffsets(double) idMeanOffsets(0)}.
     * @return a vector of offsets.
     */
    public ImmutableSparseVector idMeanOffsets() {
        return idMeanOffsets(0);
    }
}
