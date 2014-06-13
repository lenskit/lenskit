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
package org.grouplens.lenskit.basic;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.PrecomputedItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Test;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PrecomputedItemScorerTest {
    @Test
    public void testEmptyScorer() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder().build();
        assertThat(scorer.score(42, 1),
                   notANumber());
    }

    @Test
    public void testAddScore() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(42, 1, 4)
                                          .build();
        assertThat(scorer.score(42, 1),
                   closeTo(4, 1.0e-5));
        assertThat(scorer.score(42, 2), notANumber());
        assertThat(scorer.score(39, 1), notANumber());
    }

    @Test
    public void testVectorScore() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(42, 1, 4)
                                          .build();
        MutableSparseVector msv = MutableSparseVector.create(1, 3);
        scorer.score(42, msv);
        assertThat(msv.containsKey(1), equalTo(true));
        assertThat(msv.containsKey(3), equalTo(false));
        assertThat(msv.get(1), closeTo(4, 1.0e-5));
    }

    @Test
    public void testAddMultipleScores() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(42, 3, 4)
                                          .addScore(42, 7, 2)
                                          .build();
        MutableSparseVector msv = MutableSparseVector.create(1, 3, 5, 7, 8);
        scorer.score(42, msv);
        assertThat(msv.keySet().size(), equalTo(2));
        assertThat(msv.containsKey(1), equalTo(false));
        assertThat(msv.containsKey(3), equalTo(true));
        assertThat(msv.containsKey(5), equalTo(false));
        assertThat(msv.containsKey(7), equalTo(true));
        assertThat(msv.containsKey(8), equalTo(false));
        assertThat(msv.get(3), closeTo(4, 1.0e-5));
        assertThat(msv.get(7), closeTo(2, 1.0e-5));

        scorer.score(5, msv);
        assertThat(msv.keySet().isEmpty(), equalTo(true));
    }

    @Test
    public void testAddScoreVector() {
        MutableSparseVector uv = MutableSparseVector.create(2, 3, 5);
        uv.set(2, 3);
        uv.set(3, 4);
        uv.set(5, 2);
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addUser(3, uv)
                                          .build();
        MutableSparseVector output = MutableSparseVector.create(2, 3, 4, 5);
        scorer.score(3, output);
        assertThat(output, equalTo(uv));
    }
}
