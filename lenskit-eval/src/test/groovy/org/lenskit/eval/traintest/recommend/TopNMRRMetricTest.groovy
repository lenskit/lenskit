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
