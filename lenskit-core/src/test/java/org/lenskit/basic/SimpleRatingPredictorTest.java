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

import org.lenskit.data.ratings.PreferenceDomain;
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
