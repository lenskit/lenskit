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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.FallbackItemScorer;
import org.grouplens.lenskit.baseline.ScoreSource;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class SimpleRatingPredictorTest {
    RatingPredictor pred;
    RatingPredictor unclamped;

    private static final double EPSILON = 1.0e-5;

    @Before
    public void setUp() throws Exception {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                          .addScore(40, 1, 4.0)
                                          .addScore(40, 2, 5.5)
                                          .addScore(40, 3, -1)
                                          .build();
        PreferenceDomain domain = new PreferenceDomain(1, 5, 1);
        pred = new SimpleRatingPredictor(scorer, null, domain);
        unclamped = new SimpleRatingPredictor(scorer, null, null);
    }

    @Test
    public void testBasicPredict() {
        assertThat(pred.predict(40, 1),
                   closeTo(4.0, EPSILON));
    }

    @Test
    public void testBasicPredictHigh() {
        assertThat(pred.predict(40, 2),
                   closeTo(5.0, EPSILON));
    }

    @Test
    public void testBasicPredictLow() {
        assertThat(pred.predict(40, 3),
                   closeTo(1.0, EPSILON));
    }

    @Test
    public void testUnclampedPredict() {
        assertThat(unclamped.predict(40, 1),
                   closeTo(4.0, EPSILON));
    }

    @Test
    public void testUnclampedPredictHigh() {
        assertThat(unclamped.predict(40, 2),
                   closeTo(5.5, EPSILON));
    }

    @Test
    public void testUnclampedPredictLow() {
        assertThat(unclamped.predict(40, 3),
                   closeTo(-1, EPSILON));
    }

    @Test
    public void testVectorPredict() {
        LongList keys = new LongArrayList();
        keys.add(1);
        keys.add(2);
        keys.add(3);
        keys.add(4);
        MutableSparseVector v = MutableSparseVector.create(keys);
        pred.predict(40, v);
        assertThat(v.get(1),
                   closeTo(4.0, EPSILON));
        assertThat(v.get(2),
                   closeTo(5.0, EPSILON));
        assertThat(v.get(3),
                   closeTo(1.0, EPSILON));
        assertThat(v.get(4, 0.0), closeTo(0.0, EPSILON));
        assertThat(v.getChannel(SimpleRatingPredictor.PREDICTION_SOURCE_SYMBOL).get(1),
                   equalTo(ScoreSource.PRIMARY));
    }

    /**
     * Make sure that score sources are routed properly through the rating predictor and
     * fallback scorer.
     */
    @Test
    public void testDoubleFallback() {
        ItemScorer primary = PrecomputedItemScorer.newBuilder()
                                           .addScore(42, 1, 3.5)
                                           .build();
        ItemScorer base1 = PrecomputedItemScorer.newBuilder()
                                         .addScore(42, 1, 2.5)
                                         .addScore(42, 2, 2.5)
                                         .build();
        ItemScorer base2 = PrecomputedItemScorer.newBuilder()
                                         .addScore(42, 1, 3.0)
                                         .addScore(42, 2, 3.0)
                                         .addScore(42, 3, 3.0)
                                         .build();
        ItemScorer scorer = new FallbackItemScorer(primary, base1);
        RatingPredictor pred = new SimpleRatingPredictor(scorer, base2, null);
        MutableSparseVector vec = MutableSparseVector.create(1, 2, 3);
        pred.predict(42, vec);
        assertThat(vec.size(), equalTo(3));
        assertThat(vec.get(1), equalTo(3.5));
        assertThat(vec.get(2), equalTo(2.5));
        assertThat(vec.get(3), equalTo(3.0));
        assertThat(vec.getChannel(FallbackItemScorer.SCORE_SOURCE_SYMBOL).get(1),
                   equalTo(ScoreSource.PRIMARY));
        assertThat(vec.getChannel(FallbackItemScorer.SCORE_SOURCE_SYMBOL).get(2),
                   equalTo(ScoreSource.BASELINE));
        assertThat(vec.getChannel(FallbackItemScorer.SCORE_SOURCE_SYMBOL).get(3),
                   nullValue());
        assertThat(vec.getChannel(SimpleRatingPredictor.PREDICTION_SOURCE_SYMBOL).get(1),
                   equalTo(ScoreSource.PRIMARY));
        assertThat(vec.getChannel(SimpleRatingPredictor.PREDICTION_SOURCE_SYMBOL).get(2),
                   equalTo(ScoreSource.PRIMARY));
        assertThat(vec.getChannel(SimpleRatingPredictor.PREDICTION_SOURCE_SYMBOL).get(3),
                   equalTo(ScoreSource.BASELINE));
    }
}
