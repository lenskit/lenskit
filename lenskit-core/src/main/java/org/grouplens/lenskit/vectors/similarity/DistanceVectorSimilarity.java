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
package org.grouplens.lenskit.vectors.similarity;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.util.MathUtils;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Distance similarity for vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class DistanceVectorSimilarity implements VectorSimilarity, Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Construct a new distance similarity function.
     * It computes similarity as (1-|v1-v2|_2). after normalizing vectors to be unit vectors
     * Similarity is in range [-1,1];
     */
    @Inject
    public DistanceVectorSimilarity() {
    }

    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        final double distance;
        // One of the vector is empty
        if (MathUtils.isZero(vec1.norm()) || MathUtils.isZero(vec2.norm())){
            return Double.NaN;
        }

        LongSet ts = LongUtils.setUnion(vec1.keySet(),vec2.keySet());

        MutableSparseVector v1 = MutableSparseVector.create(ts);
        v1.fill(0);
        v1.set(vec1);
        v1.multiply(1.0 / v1.norm());
        v1.addScaled(vec2, -1.0 / vec2.norm());

        distance = v1.norm();
        return 1-distance;
    }

    @Override
    public boolean isSparse() {
        return false;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}
