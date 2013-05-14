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
package org.grouplens.lenskit.vectors.similarity;

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestPearsonCorrelation {
    private static final double EPSILON = 1.0e-5;

    VectorSimilarity sim;

    @Before
    public void setUp() {
        sim = new PearsonCorrelation();
    }

    @Test
    public void testEmptyVector() {
        SparseVector v = new ImmutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
        assertThat(sim.similarity(v, v), closeTo(0, EPSILON));
    }

    @Test
    public void testSelfSimilarity() {
        long keys[] = {1, 5, 7};
        double values[] = {1.5, 2.5, 2};
        SparseVector v = MutableSparseVector.wrap(keys, values).freeze();
        assertThat(sim.similarity(v, v), closeTo(1, EPSILON));
        assertThat(sim.similarity(v, v.mutableCopy().freeze()), closeTo(1, EPSILON));
    }

    @Test
    public void testDisjointSimilarity() {
        long keys[] = {1, 5, 7};
        double values[] = {1.5, 2.5, 2};
        long keys2[] = {2, 4, 8};
        SparseVector v1 = MutableSparseVector.wrap(keys, values).freeze();
        SparseVector v2 = MutableSparseVector.wrap(keys2, values).freeze();
        assertThat(sim.similarity(v1, v2), closeTo(0, EPSILON));
    }

    @Test
    public void testSimilarity() {
        long k1[] = {1, 5, 7};
        double val1[] = {1.5, 2.5, 2};
        long k2[] = {1, 5, 6};
        double val2[] = {2, 2.5, 1.7};
        SparseVector v1 = MutableSparseVector.wrap(k1, val1).freeze();
        SparseVector v2 = MutableSparseVector.wrap(k2, val2).freeze();
        assertThat(sim.similarity(v1, v2), closeTo(0.806404996, EPSILON));
    }
}
