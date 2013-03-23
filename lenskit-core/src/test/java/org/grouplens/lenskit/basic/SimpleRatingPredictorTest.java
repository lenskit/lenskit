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

/**
 * Created with IntelliJ IDEA. User: michael Date: 3/23/13 Time: 3:48 PM To change this template use
 * File | Settings | File Templates.
 */
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
                    scores.clear(e);
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
        pred = new SimpleRatingPredictor(null, new Scorer(), domain);
        unclamped = new SimpleRatingPredictor(null, new Scorer(), null);
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
