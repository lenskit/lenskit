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
package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.history.BasicUserHistory;
import org.grouplens.lenskit.util.test.MockItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class BaselineItemScorerTest {
    @Test
    public void testBaseline() {
        BaselineItemScorer scorer = new BaselineItemScorer(null, new ConstantPredictor(5), null);
        assertThat(scorer.getBaseline(),
                   instanceOf(ConstantPredictor.class));
        MutableSparseVector v = MutableSparseVector.create(3, 5, 7);
        scorer.score(42, v);
        int entriesSeen = 0;
        for (VectorEntry e: v) {
            assertThat(e.getValue(), equalTo(5.0));
            entriesSeen += 1;
        }
        assertThat(entriesSeen, equalTo(3));
    }

    @Test
    public void testSupplyMissing() {
        ItemScorer primary = MockItemScorer.newBuilder()
                                           .addScore(2, 3, 4)
                                           .build();
        ItemScorer scorer = new BaselineItemScorer(null, new ConstantPredictor(5), primary);
        MutableSparseVector v = MutableSparseVector.create(3, 5, 7);
        scorer.score(2, v);
        assertThat(v.get(3), equalTo(4.0));
        assertThat(v.get(5), equalTo(5.0));
        assertThat(v.get(7), equalTo(5.0));
    }

    @Test
    public void testBaselineWithProfile() {
        BaselineItemScorer scorer = new BaselineItemScorer(null, new ConstantPredictor(5), null);
        assertThat(scorer.getBaseline(),
                   instanceOf(ConstantPredictor.class));
        MutableSparseVector v = MutableSparseVector.create(3, 5, 7);
        scorer.score(new BasicUserHistory<Event>(42, Collections.EMPTY_LIST), v);
        int entriesSeen = 0;
        for (VectorEntry e: v) {
            assertThat(e.getValue(), equalTo(5.0));
            entriesSeen += 1;
        }
        assertThat(entriesSeen, equalTo(3));
    }

    @Test
    public void testSupplyMissingWithProfile() {
        ItemScorer primary = MockItemScorer.newBuilder()
                                           .addScore(2, 3, 4)
                                           .build();
        ItemScorer scorer = new BaselineItemScorer(null, new ConstantPredictor(5), primary);
        MutableSparseVector v = MutableSparseVector.create(3, 5, 7);
        scorer.score(new BasicUserHistory<Event>(2, Collections.EMPTY_LIST), v);
        assertThat(v.get(3), equalTo(4.0));
        assertThat(v.get(5), equalTo(5.0));
        assertThat(v.get(7), equalTo(5.0));
    }
}
