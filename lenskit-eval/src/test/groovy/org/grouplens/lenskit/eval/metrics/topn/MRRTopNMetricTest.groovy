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
package org.grouplens.lenskit.eval.metrics.topn

import it.unimi.dsi.fastutil.longs.LongSet
import org.grouplens.lenskit.collections.LongUtils
import org.grouplens.lenskit.eval.traintest.MockTestUser
import org.grouplens.lenskit.scored.ScoredIds
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class MRRTopNMetricTest {
    LongSet universe = LongUtils.packedSet(1l..50)
    MRRTopNMetric metric
    MRRTopNMetric.Context accum

    static Matcher<Double> closeTo(double x) {
        return closeTo(x, 1.0e-6d)
    }

    @Before
    public void createMetric() {
        metric = new MRRTopNMetric.Builder()
                .setGoodItems(ItemSelectors.fixed(3, 7, 9, 42))
                .setListSize(0)
                .build();
        accum = new MRRTopNMetric.Context(universe)
    }

    @Test
    public void testEmptyRank() {
        def user = MockTestUser.newBuilder()
                               .setRecommendations([])
                               .build()
        def result = metric.doMeasureUser(user, accum)
        assertThat result.rank, nullValue()
        assertThat result.recipRank, equalTo(0.0d)
        def agg = metric.getTypedResults(accum)
        assertThat agg.mrr, closeTo(0.0d)
        assertThat agg.goodMRR, closeTo(0.0d)
    }

    @Test
    public void testFirstRank() {
        def user = MockTestUser.newBuilder()
                               .setRecommendations([ScoredIds.create(3, 3.5)])
                               .build()
        def result = metric.doMeasureUser(user, accum)
        assertThat result.rank, equalTo(1)
        assertThat result.recipRank, closeTo(1.0)
        def agg = metric.getTypedResults(accum)
        assertThat agg.mrr, closeTo(1)
        assertThat agg.goodMRR, closeTo(1)
    }

    @Test
    public void testSecondRank() {
        def user = MockTestUser.newBuilder()
                               .setRecommendations([ScoredIds.create(5, 4.0),
                                                    ScoredIds.create(3, 3.5)])
                               .build()
        def result = metric.doMeasureUser(user, accum)
        assertThat result.rank, equalTo(2)
        assertThat result.recipRank, closeTo(0.5)
        def agg = metric.getTypedResults(accum)
        assertThat agg.mrr, closeTo(0.5)
        assertThat agg.goodMRR, closeTo(0.5)
    }

    @Test
    public void testNoRank() {
        def user = MockTestUser.newBuilder()
                               .setRecommendations([ScoredIds.create(5, 4.0),
                                                    ScoredIds.create(10, 3.5)])
                               .build()
        def result = metric.doMeasureUser(user, accum)
        assertThat result.rank, nullValue()
        assertThat result.recipRank, equalTo(0.0d)
        def agg = metric.getTypedResults(accum)
        assertThat agg.mrr, closeTo(0.0)
        assertThat agg.goodMRR, closeTo(0.0)
    }

    @Test
    public void testLowRank() {
        def rng = new Random()
        // get 19 bad items
        def recs = (11..29).collect { ScoredIds.create(it, rng.nextDouble()) }
        // and 1 good
        recs << ScoredIds.create(42, Math.E)
        def user = MockTestUser.newBuilder()
                               .setRecommendations(recs)
                               .build()
        def result = metric.doMeasureUser(user, accum)
        assertThat result.rank, equalTo(20)
        assertThat result.recipRank, closeTo(0.05)
        def agg = metric.getTypedResults(accum)
        assertThat agg.mrr, closeTo(0.05)
        assertThat agg.goodMRR, closeTo(0.05)
    }

    @Test
    public void testMixed() {
        def rng = new Random()
        // get 19 bad items
        def recs = (11..29).collect { ScoredIds.create(it, rng.nextDouble()) }
        // and 1 good
        recs << ScoredIds.create(42, Math.E)
        def u1 = MockTestUser.newBuilder()
                               .setRecommendations(recs)
                               .build()
        metric.doMeasureUser(u1, accum)
        def u2 = MockTestUser.newBuilder()
                             .setRecommendations([ScoredIds.create(5, 3.5)])
                             .build()
        metric.doMeasureUser(u2, accum)

        def agg = metric.getTypedResults(accum)
        // MRR should average 0.05 and 0
        assertThat agg.mrr, closeTo(0.025)
        // Good MRR should only have 0.05
        assertThat agg.goodMRR, closeTo(0.05)
    }
}
