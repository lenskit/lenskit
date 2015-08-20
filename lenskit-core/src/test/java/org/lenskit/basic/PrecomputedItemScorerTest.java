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
