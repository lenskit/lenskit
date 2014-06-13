/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.PrecomputedItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FallbackItemScorerTest {
    ItemScorer primary;
    ItemScorer baseline;
    ItemScorer scorer;

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
        assertThat(scorer.score(42, 39), equalTo(3.5));
    }

    @Test
    public void testFallbackItem() {
        // score for item only known by secondary
        assertThat(scorer.score(42, 30), equalTo(4.0));
    }

    @Test
    public void testFallbackUser() {
        // score for user only known by secondary
        assertThat(scorer.score(15, 30), equalTo(5.0));
    }

    @Test
    public void testMultiple() {
        MutableSparseVector msv = MutableSparseVector.create(10, 30, 39);
        scorer.score(42, msv);
        assertThat(msv.size(), equalTo(2));
        assertThat(msv.get(39), equalTo(3.5));
        assertThat(msv.getChannel(FallbackItemScorer.SCORE_SOURCE_SYMBOL).get(39),
                   equalTo(ScoreSource.PRIMARY));
        assertThat(msv.get(30), equalTo(4.0));
        assertThat(msv.getChannel(FallbackItemScorer.SCORE_SOURCE_SYMBOL).get(30),
                   equalTo(ScoreSource.BASELINE));
        assertThat(msv.unsetKeySet(), contains(10L));
    }
}
