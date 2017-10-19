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
import org.junit.Before
import org.junit.Test
import org.lenskit.data.ratings.Rating
import org.lenskit.eval.traintest.TestUser
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper
import org.lenskit.results.Results
import org.lenskit.util.collections.LongUtils

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TopNPrecisionRecallMetricTest {
    LongSet universe = LongUtils.packedSet(1l..50)
    TopNPrecisionRecallMetric metric
    TopNPrecisionRecallMetric.Context accum
    TestUser user

    @Before
    void createMetric() {
        metric = new TopNPrecisionRecallMetric(ItemSelector.compileSelector('user.testItems'), null)
        accum = new TopNPrecisionRecallMetric.Context(universe)
        user = TestUser.newBuilder()
                       .setUserId(42)
                       .addTestEntity(Rating.create(42L, 1L, 3.5),
                                      Rating.create(42L, 5L, 2.5))
                       .build()
    }

    @Test
    void testConfigure() {
        def jsb = new JsonBuilder()
        jsb {
            type 'pr'
            suffix 'bar'
        }
        def mlh = new MetricLoaderHelper(ClassLoaders.inferDefault(), 'topn-metrics')
        def metric = mlh.createMetric(TopNMetric, jsb.toString())
        assertThat(metric, instanceOf(TopNPrecisionRecallMetric))
        def pr = metric as TopNPrecisionRecallMetric
        assertThat(pr.suffix, equalTo('bar'));
    }

    @Test
    void testAllGood() {
        def recs = Results.newResultList([Results.create(1, 4.0),
                                          Results.create(5, 3.5)])
        def result = metric.measureUser(null, user, -1, recs, accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat result.precision, closeTo(1.0d, 0.00001d)
        assertThat result.recall, closeTo(1.0d, 0.00001d)
        assertThat result.f1, closeTo(1.0d, 0.00001d)
        def agg = metric.getAggregateMeasurements(accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat agg.precision, closeTo(1.0d, 0.00001d)
        assertThat agg.recall, closeTo(1.0d, 0.00001d)
        assertThat agg.f1, closeTo(1.0d, 0.00001d)
    }

    @Test
    void testNoGood() {
        def recs = Results.newResultList([Results.create(10, 4.0),
                                          Results.create(25, 3.5)])
        def result = metric.measureUser(null, user, -1, recs, accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat result.precision, closeTo(0.0d, 0.00001d)
        assertThat result.recall, closeTo(0.0d, 0.00001d)
        assertThat result.f1, closeTo(0.0d, 0.00001d)
        def agg = metric.getAggregateMeasurements(accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat agg.precision, closeTo(0.0d, 0.00001d)
        assertThat agg.recall, closeTo(0.0d, 0.00001d)
        assertThat agg.f1, closeTo(0.0d, 0.00001d)
    }

    @Test
    void testOneGood() {
        def recs = Results.newResultList([Results.create(1, 4.0),
                                          Results.create(25, 3.5),
                                          Results.create(32, 2.5)])
        def result = metric.measureUser(null, user, -1, recs, accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat result.precision, closeTo(1.0d / 3, 0.00001d)
        assertThat result.recall, closeTo(0.5d, 0.00001d)
        assertThat result.f1, closeTo(0.4d, 0.00001d)
        def agg = metric.getAggregateMeasurements(accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat agg.precision, closeTo(1.0d / 3, 0.00001d)
        assertThat agg.recall, closeTo(0.5d, 0.00001d)
        assertThat agg.f1, closeTo(0.4d, 0.00001d)
    }
}
