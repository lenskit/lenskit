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
