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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.util.collections.LongUtils;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PrecomputedItemScorerTest {
    @Test
    public void testEmptyScorer() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder().build();
        assertThat(scorer.score(42, 1),
                   nullValue());
    }

    @Test
    public void testAddScore() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(42, 1, 4)
                                          .build();
        assertThat(scorer.score(42, 1).getScore(),
                   closeTo(4, 1.0e-5));
        assertThat(scorer.score(42, 2), nullValue());
        assertThat(scorer.score(39, 1), nullValue());
    }

    @Test
    public void testMultiScore() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(42, 1, 4)
                                          .build();
        LongSet items = LongUtils.packedSet(1, 3);
        Map<Long, Double> results = scorer.score(42, items);
        assertThat(results.containsKey(1L), equalTo(true));
        assertThat(results.containsKey(3L), equalTo(false));
        assertThat(results.get(1L), closeTo(4, 1.0e-5));
    }

    @Test
    public void testAddMultipleScores() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(42, 3, 4)
                                          .addScore(42, 7, 2)
                                          .build();
        LongSet items = LongUtils.packedSet(1, 3, 5, 7, 8);
        Map<Long, Double> results = scorer.score(42, items);
        assertThat(results.keySet().size(), equalTo(2));
        assertThat(results.containsKey(1L), equalTo(false));
        assertThat(results.containsKey(3L), equalTo(true));
        assertThat(results.containsKey(5L), equalTo(false));
        assertThat(results.containsKey(7L), equalTo(true));
        assertThat(results.containsKey(8L), equalTo(false));
        assertThat(results.get(3L), closeTo(4, 1.0e-5));
        assertThat(results.get(7L), closeTo(2, 1.0e-5));
    }
}
