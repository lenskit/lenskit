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
package org.lenskit.eval.traintest.recommend

import groovy.json.JsonBuilder
import it.unimi.dsi.fastutil.longs.LongSet
import org.junit.Before
import org.junit.Test
import org.lenskit.data.ratings.Rating
import org.lenskit.eval.traintest.TestUser
import org.lenskit.results.Results
import org.lenskit.specs.DynamicSpec
import org.lenskit.specs.SpecUtils
import org.lenskit.util.collections.LongUtils

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TopNPrecisionRecallMetricTest {
    LongSet universe = LongUtils.packedSet(1l..50)
    TopNPrecisionRecallMetric metric
    TopNPrecisionRecallMetric.Context accum
    TestUser user

    @Before
    public void createMetric() {
        metric = new TopNPrecisionRecallMetric(ItemSelector.compileSelector('user.testItems'), null)
        accum = new TopNPrecisionRecallMetric.Context(universe)
        user = TestUser.newBuilder()
                       .setUserId(42)
                       .addTestEvent(Rating.create(42L, 1L, 3.5),
                                     Rating.create(42L, 5L, 2.5))
                       .build()
    }

    @Test
    public void testConfigure() {
        def jsb = new JsonBuilder()
        jsb {
            type 'pr'
            suffix 'bar'
        }
        def node = SpecUtils.parse(DynamicSpec, jsb.toString())
        def metric = SpecUtils.buildObject(TopNMetric, node)
        assertThat(metric, instanceOf(TopNPrecisionRecallMetric))
        def ndcg = metric as TopNPrecisionRecallMetric
        assertThat(ndcg.suffix, equalTo('bar'));
    }

    @Test
    public void testAllGood() {
        def recs = Results.newResultList([Results.create(1, 4.0),
                                          Results.create(5, 3.5)])
        def result = metric.measureUser(user, recs, accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat result.precision, closeTo(1.0d, 0.00001d)
        assertThat result.recall, closeTo(1.0d, 0.00001d)
        assertThat result.f1, closeTo(1.0d, 0.00001d)
        def agg = metric.getAggregateMeasurements(accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat agg.precision, closeTo(1.0d, 0.00001d)
        assertThat agg.recall, closeTo(1.0d, 0.00001d)
        assertThat agg.f1, closeTo(1.0d, 0.00001d)
    }

    @Test
    public void testNoGood() {
        def recs = Results.newResultList([Results.create(10, 4.0),
                                          Results.create(25, 3.5)])
        def result = metric.measureUser(user, recs, accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat result.precision, closeTo(0.0d, 0.00001d)
        assertThat result.recall, closeTo(0.0d, 0.00001d)
        assertThat result.f1, closeTo(0.0d, 0.00001d)
        def agg = metric.getAggregateMeasurements(accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat agg.precision, closeTo(0.0d, 0.00001d)
        assertThat agg.recall, closeTo(0.0d, 0.00001d)
        assertThat agg.f1, closeTo(0.0d, 0.00001d)
    }

    @Test
    public void testOneGood() {
        def recs = Results.newResultList([Results.create(1, 4.0),
                                          Results.create(25, 3.5),
                                          Results.create(32, 2.5)])
        def result = metric.measureUser(user, recs, accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat result.precision, closeTo(1.0d / 3, 0.00001d)
        assertThat result.recall, closeTo(0.5d, 0.00001d)
        assertThat result.f1, closeTo(0.4d, 0.00001d)
        def agg = metric.getAggregateMeasurements(accum) as TopNPrecisionRecallMetric.PresRecResult
        assertThat agg.precision, closeTo(1.0d / 3, 0.00001d)
        assertThat agg.recall, closeTo(0.5d, 0.00001d)
        assertThat agg.f1, closeTo(0.4d, 0.00001d)
    }
}
