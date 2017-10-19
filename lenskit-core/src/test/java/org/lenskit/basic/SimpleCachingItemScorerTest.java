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


import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class SimpleCachingItemScorerTest {
    SimpleCachingItemScorer cachedScorer;
    ItemScorer mockScorer;

    @Before
    public void Setup() {
        mockScorer = PrecomputedItemScorer.newBuilder()
                                    .addScore(1, 3, 3.5)
                                    .addScore(2, 4, 5)
                                    .addScore(2, 6, 3)
                                    .addScore(3, 1, 5)
                                    .addScore(3, 2, 4.5)
                                    .addScore(3, 3, 2.5)
                                    .addScore(3, 4, 1)
                                    .build();
        cachedScorer = new SimpleCachingItemScorer(mockScorer);
    }

    @Test
    public void testScore() {
        assertThat(cachedScorer.score(1, 3),
                   equalTo((Result) Results.create(3, 3.5)));
    }

    @Test
    public void testCacheUser() {
        assertThat(cachedScorer.score(1, 3),
                   equalTo((Result) Results.create(3, 3.5)));
        assertThat(cachedScorer.getId(), equalTo(1L));
        assertThat(cachedScorer.score(2, 6),
                   equalTo((Result) Results.create(6, 3.0)));
        assertThat(cachedScorer.getId(), equalTo(2L));
    }

    @Test
    public void testCachedScores() {
        Long user = 3L;
        LongSortedSet items = LongUtils.packedSet(1, 2);
        cachedScorer.score(user, items);
        assertThat(cachedScorer.getCache().keySet(), equalTo((Set) items));
        cachedScorer.score(user, 4);
        assertThat(cachedScorer.getCache().keySet(), equalTo((Set) LongUtils.packedSet(1, 2, 4)));
    }
}
