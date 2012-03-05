/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.metrics.predict;

import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNDCG {
    long[] items = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    double[] ratings1 = {5, 4, 4, 3, 5, 3, 4, 3, 2, 5};
    double[] ratings2 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
    double[] ratings3 = {4, 5, 4, 2, 3, 1, 3, 4, 5, 2};

    double[] predictions1 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
    double[] predictions2 = {4, 4, 5, 2, 3, 2, 3, 4, 4, 3};
    double[] predictions3 = {4, 4, 5, 3, 3, 4, 5, 4, 4, 4};

    SparseVector rv1 = ImmutableSparseVector.wrap(items, ratings1);
    SparseVector rv2 = ImmutableSparseVector.wrap(items, ratings2);
    SparseVector rv3 = ImmutableSparseVector.wrap(items, ratings3);

    SparseVector pv1 = ImmutableSparseVector.wrap(items, predictions1);
    SparseVector pv2 = ImmutableSparseVector.wrap(items, predictions2);
    SparseVector pv3 = ImmutableSparseVector.wrap(items, predictions3);

    @Test
    public void testComputeDCG() {
        // score rating vectors
        assertEquals(22.0418,
                     NDCGPredictMetric.computeDCG(rv1.keysByValue(true), rv1),
                     0.0001);
        assertEquals(21.0954,
                     NDCGPredictMetric.computeDCG(rv2.keysByValue(true), rv2),
                     0.0001);
        assertEquals(20.0742,
                     NDCGPredictMetric.computeDCG(rv3.keysByValue(true), rv3),
                     0.0001);

        // score prediction vectors
        // note that the correctness of these tests depends on keysByValue using
        // a stable sort.
        assertEquals(21.0116,
                     NDCGPredictMetric.computeDCG(pv1.keysByValue(true), rv1),
                     0.0001);
        assertEquals(20.2048,
                     NDCGPredictMetric.computeDCG(pv2.keysByValue(true), rv2),
                     0.0001);
        assertEquals(17.9835,
                     NDCGPredictMetric.computeDCG(pv3.keysByValue(true), rv3),
                     0.0001);
    }

    @Test
    public void testAccumulator() {
        NDCGPredictMetric.Accum acc = (new NDCGPredictMetric()).makeAccumulator(null, null);
        acc.evaluatePredictions(1, rv1, pv1);
        assertEquals(1, acc.nusers);
        assertEquals(0.9533, acc.total, 0.0001);
        acc.evaluatePredictions(2, rv2, pv2);
        assertEquals(2, acc.nusers);
        assertEquals(1.9110, acc.total, 0.0001);
        acc.evaluatePredictions(3, rv3, pv3);
        assertEquals(3, acc.nusers);
        assertEquals(2.8069, acc.total, 0.0001);
    }
}
