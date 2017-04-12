/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.traintest.recommend

import groovy.json.JsonBuilder
import it.unimi.dsi.fastutil.longs.LongSet
import org.grouplens.grapht.util.ClassLoaders
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.lenskit.eval.traintest.TestUser
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper
import org.lenskit.results.Results
import org.lenskit.util.collections.LongUtils

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TopNMRRMetricTest {
    LongSet universe = LongUtils.packedSet(1l..50)
    TopNMRRMetric metric
    TopNMRRMetric.Context accum

    static Matcher<Double> closeTo(double x) {
        return closeTo(x, 1.0e-6d)
    }

    @Before
    void createMetric() {
        metric = new TopNMRRMetric(ItemSelector.fixed(3, 7, 9, 42), null)

        accum = new TopNMRRMetric.Context(universe)
    }

    @Test
    void testEmptyRank() {
        def user = TestUser.newBuilder()
                           .setUserId(42)
                           .build()
        def result = metric.measureUser(null, user, -1, Results.newResultList([]), accum)
        assertThat result.rank, nullValue()
        assertThat result.recipRank, equalTo(0.0d)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.0d)
    }

    @Test
    void testFirstRank() {
        def user = TestUser.newBuilder()
                               .build()
        def recs = Results.newResultList(Results.create(3, 3.5))
        def result = metric.measureUser(null, user, -1, recs, accum)
        assertThat result.rank, equalTo(1)
        assertThat result.recipRank, closeTo(1.0)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(1)
    }

    @Test
    void testSecondRank() {
        def user = TestUser.newBuilder()
                          .build()
        def recs = Results.newResultList([Results.create(5, 4.0),
                                          Results.create(3, 3.5)])
        def result = metric.measureUser(null, user, -1, recs, accum)
        assertThat result.rank, equalTo(2)
        assertThat result.recipRank, closeTo(0.5)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.5)
    }

    @Test
    void testNoRank() {
        def user = TestUser.newBuilder()
                           .build()
        def recs = Results.newResultList([Results.create(5, 4.0),
                                          Results.create(10, 3.5)])
        def result = metric.measureUser(null, user, -1, recs, accum)
        assertThat result.rank, nullValue()
        assertThat result.recipRank, equalTo(0.0d)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.0)
    }

    @Test
    void testLowRank() {
        def rng = new Random()
        // get 19 bad items
        def recs = (11..29).collect { Results.create(it, rng.nextDouble()) }
        // and 1 good
        recs << Results.create(42, Math.E)
        def user = TestUser.newBuilder()
                           .build()
        def result = metric.measureUser(null, user, -1, Results.newResultList(recs), accum)
        assertThat result.rank, equalTo(20)
        assertThat result.recipRank, closeTo(0.05)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.05)
    }

    @Test
    void testMixed() {
        def rng = new Random()
        // get 19 bad items
        def recs = (11..29).collect { Results.create(it, rng.nextDouble()) }
        // and 1 good
        recs << Results.create(42, Math.E)
        def u1 = TestUser.newBuilder()
                         .build()
        metric.measureUser(null, u1, -1, Results.newResultList(recs), accum)
        def u2 = TestUser.newBuilder()
                         .build()
        def r2 = Results.newResultList([Results.create(5, 3.5)])
        metric.measureUser(null, u2, -1, r2, accum)

        def agg = metric.getAggregateMeasurements(accum)
        // MRR should average 0.05 and 0
        assertThat agg.mrr, closeTo(0.025)
        // Good MRR should only have 0.05
    }

    @Test
    void testConfigure() {
        def jsb = new JsonBuilder()
        jsb {
            type 'mrr'
            suffix 'Good'
            goodItems 'allItems'
        }
        def mlh = new MetricLoaderHelper(ClassLoaders.inferDefault(), 'topn-metrics')
        def metric = mlh.createMetric(TopNMetric, jsb.toString())
        assertThat(metric, instanceOf(TopNMRRMetric))
        def mrr = metric as TopNMRRMetric
        assertThat(mrr.suffix, equalTo("Good"))
        assertThat(mrr.goodItems, instanceOf(ItemSelector.GroovyItemSelector))
    }
}
