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
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultMap;
import org.lenskit.util.collections.LongUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FallbackItemScorerTest {
    ItemScorer primary;
    ItemScorer baseline;
    FallbackItemScorer scorer;

    @Before
    public void setupScorer() {
        primary = PrecomputedItemScorer.newBuilder()
                                .addScore(42, 39, 3.5)
                                .build();
        baseline = PrecomputedItemScorer.newBuilder()
                                 .addScore(42, 39, 2.0)
                                 .addScore(42, 30, 4.0)
                                 .addScore(15, 30, 5.0)
                                 .build();
        scorer = new FallbackItemScorer(primary, baseline);
    }

    @Test
    public void testScoreItemPrimary() {
        // score known by the primary
        FallbackResult r = scorer.score(42, 39);
        assertThat(r, notNullValue());
        assertThat(r.getScore(), equalTo(3.5));
        assertThat(r.isFromPrimary(), equalTo(true));
    }

    @Test
    public void testFallbackItem() {
        // score for item only known by secondary
        FallbackResult r = scorer.score(42, 30);
        assertThat(r, notNullValue());
        assertThat(r.getScore(), equalTo(4.0));
        assertThat(r.isFromPrimary(), equalTo(false));
    }

    @Test
    public void testFallbackUser() {
        // score for user only known by secondary
        FallbackResult r = scorer.score(15, 30);
        assertThat(r, notNullValue());
        assertThat(r.getScore(), equalTo(5.0));
        assertThat(r.isFromPrimary(), equalTo(false));
    }

    @Test
    public void testNoRec() {
        FallbackResult r = scorer.score(15, 39);
        assertThat(r, nullValue());
    }

    @Test
    public void testMultipleDetails() {
        LongSet items = LongUtils.packedSet(10, 30, 39);
        ResultMap results = scorer.scoreWithDetails(42, items);
        assertThat(results.size(), equalTo(2));
        assertThat(results.getScore(39), equalTo(3.5));
        assertThat(results.getScore(30), equalTo(4.0));

        assertThat(results.get(39L), instanceOf(FallbackResult.class));
        FallbackResult r39 = (FallbackResult) results.get(39L);
        assertThat(r39.getScore(), equalTo(3.5));
        assertThat(r39.isFromPrimary(), equalTo(true));
        assertThat(r39.getInnerResult(), notNullValue());
        assertThat(r39.getInnerResult().getScore(), equalTo(3.5));

        assertThat(results.get(30L), instanceOf(FallbackResult.class));
        FallbackResult r30 = (FallbackResult) results.get(30L);
        assertThat(r30.getScore(), equalTo(4.0));
        assertThat(r30.isFromPrimary(), equalTo(false));
        assertThat(r30.getInnerResult(), notNullValue());
        assertThat(r30.getInnerResult().getScore(), equalTo(4.0));

        assertThat(results.get(10L), nullValue());
    }
}
