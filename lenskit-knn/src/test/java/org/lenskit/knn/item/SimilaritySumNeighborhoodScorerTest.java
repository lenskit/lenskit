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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
        Long2DoubleMap nbrs = Long2DoubleMaps.EMPTY_MAP;
        Long2DoubleMap scores = Long2DoubleMaps.EMPTY_MAP;
        assertThat(scorer.score(42, nbrs, scores), nullValue());
    }

    @Test
    public void testEmptyNbrs() {
        Long2DoubleMap nbrs = Long2DoubleMaps.EMPTY_MAP;
        Long2DoubleMap scores = Long2DoubleMaps.singleton(5, 3.7);
        assertThat(scorer.score(42, nbrs, scores), nullValue());
    }

    @Test
    public void testOneNbr() {
        Long2DoubleMap nbrs = Long2DoubleMaps.singleton(5, 1.0);
        Long2DoubleMap scores = Long2DoubleMaps.singleton(5, 3.7);
        assertThat(scorer.score(42, nbrs, scores).getScore(), closeTo(1.0));
    }

    @Test
    public void testMultipleNeighbors() {
        Long2DoubleMap nbrs = new Long2DoubleOpenHashMap();
        nbrs.put(2, 0.5);
        nbrs.put(5, 1.0);
        nbrs.put(7, 0.92);

        Long2DoubleMap scores = new Long2DoubleOpenHashMap();
        scores.put(2, 3.7);
        scores.put(3, 4.2);
        scores.put(5, 1.2);
        scores.put(7, 7.8);
        assertThat(scorer.score(42, nbrs, scores).getScore(), closeTo(2.42));
    }
}
