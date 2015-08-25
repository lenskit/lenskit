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

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SimpleRatingPredictorTest {
    SimpleRatingPredictor pred;
    SimpleRatingPredictor unclamped;

    private static final double EPSILON = 1.0e-5;

    @Before
    public void setUp() throws Exception {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(40, 1, 4.0)
                                                 .addScore(40, 2, 5.5)
                                                 .addScore(40, 3, -1)
                                                 .build();
        PreferenceDomain domain = new PreferenceDomain(1, 5, 1);
        pred = new SimpleRatingPredictor(scorer, domain);
        unclamped = new SimpleRatingPredictor(scorer, null);
    }

    @Test
    public void testBasicPredict() {
        assertThat(pred.predict(40, 1).getScore(),
                   closeTo(4.0, EPSILON));
        assertThat(pred.predict(40, 1).getOriginalResult(),
                   equalTo((Result) Results.create(1, 4.0)));
    }

    @Test
    public void testBasicPredictHigh() {
        assertThat(pred.predict(40, 2).getScore(),
                   closeTo(5.0, EPSILON));
    }

    @Test
    public void testBasicPredictLow() {
        assertThat(pred.predict(40, 3).getScore(),
                   closeTo(1.0, EPSILON));
    }

    @Test
    public void testUnclampedPredict() {
        assertThat(unclamped.predict(40, 1).getScore(),
                   closeTo(4.0, EPSILON));
    }

    @Test
    public void testUnclampedPredictHigh() {
        assertThat(unclamped.predict(40, 2).getScore(),
                   closeTo(5.5, EPSILON));
    }

    @Test
    public void testUnclampedPredictLow() {
        assertThat(unclamped.predict(40, 3).getScore(),
                   closeTo(-1, EPSILON));
    }

    @Test
    public void testBulkPredict() {
        Map<Long,Double> scores = pred.predict(40, LongUtils.packedSet(1, 2, 3, 4));
        assertThat(scores.size(), equalTo(3));
        assertThat(scores, hasEntry(1L, 4.0));
        assertThat(scores, hasEntry(2L, 5.0));
        assertThat(scores, hasEntry(3L, 1.0));
    }

    @Test
    public void testBulkUnclampedPredict() {
        Map<Long,Double> scores = unclamped.predict(40, LongUtils.packedSet(1, 2, 3, 4));
        assertThat(scores.size(), equalTo(3));
        assertThat(scores, hasEntry(1L, 4.0));
        assertThat(scores, hasEntry(2L, 5.5));
        assertThat(scores, hasEntry(3L, -1.0));
    }

    @Test
    public void testBulkPredictWithDetails() {
        ResultMap scores = pred.predictWithDetails(40, LongUtils.packedSet(1, 2, 3, 4));
        assertThat(scores.size(), equalTo(3));
        assertThat(scores.getScore(1), equalTo(4.0));
        assertThat(scores.getScore(2), equalTo(5.0));
        assertThat(scores.getScore(3), equalTo(1.0));
        assertThat(scores.get(4L), nullValue());
    }
}
