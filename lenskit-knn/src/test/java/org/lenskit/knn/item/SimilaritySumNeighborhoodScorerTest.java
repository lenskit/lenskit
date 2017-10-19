/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        List<ItemItemResult> results = new ArrayList<>();
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.detailed(results);

        scorer.score(42, nbrs, scores, accum);

        assertThat(results, hasSize(0));
    }

    @Test
    public void testEmptyNbrs() {
        Long2DoubleMap nbrs = Long2DoubleMaps.EMPTY_MAP;
        Long2DoubleMap scores = Long2DoubleMaps.singleton(5, 3.7);
        List<ItemItemResult> results = new ArrayList<>();
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.detailed(results);

        scorer.score(42, nbrs, scores, accum);

        assertThat(results, hasSize(0));
    }

    @Test
    public void testOneNbr() {
        Long2DoubleMap nbrs = Long2DoubleMaps.singleton(5, 1.0);
        Long2DoubleMap scores = Long2DoubleMaps.singleton(5, 3.7);
        List<ItemItemResult> results = new ArrayList<>();
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.detailed(results);

        scorer.score(42, nbrs, scores, accum);
        assertThat(results, hasSize(1));
        assertThat(results.get(0).getScore(), closeTo(1.0));
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

        List<ItemItemResult> results = new ArrayList<>();
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.detailed(results);

        scorer.score(42, nbrs, scores, accum);
        assertThat(results, hasSize(1));
        assertThat(results.get(0).getScore(), closeTo(2.42));
    }
}
