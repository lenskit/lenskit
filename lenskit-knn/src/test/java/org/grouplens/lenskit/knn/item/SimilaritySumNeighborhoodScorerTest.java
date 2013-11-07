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

import com.google.common.collect.Lists;
import org.grouplens.lenskit.scored.PackedScoredIdList;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.junit.Assert.assertThat;

public class SimilaritySumNeighborhoodScorerTest {
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
        List<ScoredId> nbrs = Collections.emptyList();
        SparseVector scores = MutableSparseVector.create();
        assertThat(scorer.score(nbrs, scores), notANumber());
    }

    @Test
    public void testEmptyNbrs() {
        List<ScoredId> nbrs = Collections.emptyList();
        SparseVector scores = MutableSparseVector.wrap(new long[]{5}, new double[]{3.7}).freeze();
        assertThat(scorer.score(nbrs, scores), notANumber());
    }

    @Test
    public void testOneNbr() {
        List<ScoredId> nbrs = Lists.newArrayList(ScoredIds.newBuilder()
                                                          .setId(5)
                                                          .setScore(1.0)
                                                          .build());
        SparseVector scores = MutableSparseVector.wrap(new long[]{5}, new double[]{3.7}).freeze();
        assertThat(scorer.score(nbrs, scores), closeTo(1.0));
    }

    @Test
    public void testMultipleNeighbors() {
        List<ScoredId> nbrs = ScoredIds.newListBuilder()
                                       .add(2, 0.5)
                                       .add(5, 1.0)
                                       .add(7, 0.92)
                                       .build();
        long[] scoreKeys = {2, 3, 5, 7};
        double[] scoreValues = {3.7, 4.2, 1.2, 7.8};
        SparseVector scores = MutableSparseVector.wrap(scoreKeys, scoreValues).freeze();
        assertThat(scorer.score(nbrs, scores), closeTo(2.42));
    }
}
