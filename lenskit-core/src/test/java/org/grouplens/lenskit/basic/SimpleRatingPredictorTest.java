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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class SimpleRatingPredictorTest {
    private class Scorer extends AbstractItemScorer {
        public Scorer() {
            super(null);
        }

        @Override
        public void score(@Nonnull UserHistory<? extends Event> profile,
                          @Nonnull MutableSparseVector scores) {
            score(profile.getUserId(), scores);
        }

        @Override
        public void score(long user, @Nonnull MutableSparseVector scores) {
            for (VectorEntry e: scores.fast(VectorEntry.State.EITHER)) {
                switch ((int) e.getKey()) {
                case 1:
                    scores.set(e, 4.0);
                    break;
                case 2:
                    scores.set(e, 5.5);
                    break;
                case 3:
                    scores.set(e, -1);
                    break;
                default:
                    scores.unset(e);
                }
            }
        }
    }

    RatingPredictor pred;
    RatingPredictor unclamped;

    private static final double EPSILON = 1.0e-5;

    @Before
    public void setUp() throws Exception {
        PreferenceDomain domain = new PreferenceDomain(1, 5, 1);
        pred = new SimpleRatingPredictor(null, new Scorer(), null, domain);
        unclamped = new SimpleRatingPredictor(null, new Scorer(), null, null);
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
        MutableSparseVector v = new MutableSparseVector(keys);
        pred.predict(42, v);
        assertThat(v.get(1),
                   closeTo(4.0, EPSILON));
        assertThat(v.get(2),
                   closeTo(5.0, EPSILON));
        assertThat(v.get(3),
                   closeTo(1.0, EPSILON));
        assertThat(v.get(4, 0.0), closeTo(0.0, EPSILON));
    }
}
