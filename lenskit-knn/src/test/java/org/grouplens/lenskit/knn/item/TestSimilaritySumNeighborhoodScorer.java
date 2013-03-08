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
package org.grouplens.lenskit.knn.item;

import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.junit.Assert.assertThat;

import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class TestSimilaritySumNeighborhoodScorer {
    SimilaritySumNeighborhoodScorer scorer;

    @Before
    public void createScorer() {
        scorer = new SimilaritySumNeighborhoodScorer();
    }

    public static Matcher<Double> closeTo(double x) {
        return Matchers.closeTo(x, 1.0e-5);
    }

    @Test
    public void testEmpty() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        SparseVector scores = new MutableSparseVector();
        assertThat(scorer.score(nbrs, scores), notANumber());
    }

    @Test
    public void testEmptyNbrs() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        SparseVector scores = MutableSparseVector.wrap(new long[]{5}, new double[]{3.7}).freeze();
        assertThat(scorer.score(nbrs, scores), notANumber());
    }

    @Test
    public void testOneNbr() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        nbrs.add(5, 1.0);
        SparseVector scores = MutableSparseVector.wrap(new long[]{5}, new double[]{3.7}).freeze();
        assertThat(scorer.score(nbrs, scores), closeTo(1.0));
    }

    @Test
    public void testMultipleNeighbors() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        nbrs.add(5, 1.0);
        nbrs.add(7, 0.92);
        nbrs.add(2, 0.5);
        long[] keys = {2, 3, 5, 7};
        double[] ratings = {3.7, 4.2, 1.2, 7.8};
        SparseVector scores = MutableSparseVector.wrap(keys, ratings).freeze();
        assertThat(scorer.score(nbrs, scores), closeTo(2.42));
    }
}
