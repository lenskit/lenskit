/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.knn;


import static org.grouplens.lenskit.knn.SpearmanRankCorrelation.rank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.grouplens.lenskit.data.vector.ImmutableSparseVector;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SpearmanRankCorrelationTest {
    
    @Test
    public void testRankEmpty() {
        assertTrue(rank(new MutableSparseVector()).isEmpty());
    }
    
    @Test
    public void testRankSingle() {
        SparseVector v = MutableSparseVector.wrap(new long[]{1}, new double[]{5});
        SparseVector r = rank(v);
        assertEquals(1, r.size());
        assertEquals(1, r.get(1), 1.0e-6);
    }
    
    @Test
    public void testRankSeveral() {
        long[] keys = { 1, 2, 3, 4, 5 };
        double[] values = { 7, 2, 3, 1, 5};
        SparseVector v = ImmutableSparseVector.wrap(keys, values);
        SparseVector r = rank(v);
        assertEquals(5, r.size());
        assertEquals(1, r.get(1), 1.0e-6);
        assertEquals(2, r.get(5), 1.0e-6);
        assertEquals(3, r.get(3), 1.0e-6);
        assertEquals(4, r.get(2), 1.0e-6);
        assertEquals(5, r.get(4), 1.0e-6);
    }
    
    @Test
    public void testRankTie() {
        long[] keys = { 1, 2, 3, 4, 5 };
        double[] values = { 7, 2, 3, 1, 3};
        SparseVector v = ImmutableSparseVector.wrap(keys, values);
        SparseVector r = rank(v);
        assertEquals(5, r.size());
        assertEquals(1, r.get(1), 1.0e-6);
        assertEquals(2.5, r.get(3), 1.0e-6);
        assertEquals(2.5, r.get(5), 1.0e-6);
        assertEquals(4, r.get(2), 1.0e-6);
        assertEquals(5, r.get(4), 1.0e-6);
    }

}
