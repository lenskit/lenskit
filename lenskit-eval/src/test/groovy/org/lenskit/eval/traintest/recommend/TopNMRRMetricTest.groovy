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
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.lenskit.eval.traintest.TestUser
import org.lenskit.results.Results
import org.lenskit.specs.DynamicSpec
import org.lenskit.specs.SpecUtils
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
    public void createMetric() {
        metric = new TopNMRRMetric(ItemSelector.fixed(3, 7, 9, 42), null)

        accum = new TopNMRRMetric.Context(universe)
    }

    @Test
    public void testEmptyRank() {
        def user = TestUser.newBuilder()
                           .setUserId(42)
                           .build()
        def result = metric.measureUser(user, Results.newResultList([]), accum)
        assertThat result.rank, nullValue()
        assertThat result.recipRank, equalTo(0.0d)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.0d)
        assertThat agg.goodMRR, closeTo(0.0d)
    }

    @Test
    public void testFirstRank() {
        def user = TestUser.newBuilder()
                               .build()
        def recs = Results.newResultList(Results.create(3, 3.5))
        def result = metric.measureUser(user, recs, accum)
        assertThat result.rank, equalTo(1)
        assertThat result.recipRank, closeTo(1.0)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(1)
        assertThat agg.goodMRR, closeTo(1)
    }

    @Test
    public void testSecondRank() {
        def user = TestUser.newBuilder()
                          .build()
        def recs = Results.newResultList([Results.create(5, 4.0),
                                          Results.create(3, 3.5)])
        def result = metric.measureUser(user, recs, accum)
        assertThat result.rank, equalTo(2)
        assertThat result.recipRank, closeTo(0.5)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.5)
        assertThat agg.goodMRR, closeTo(0.5)
    }

    @Test
    public void testNoRank() {
        def user = TestUser.newBuilder()
                           .build()
        def recs = Results.newResultList([Results.create(5, 4.0),
                                          Results.create(10, 3.5)])
        def result = metric.measureUser(user, recs, accum)
        assertThat result.rank, nullValue()
        assertThat result.recipRank, equalTo(0.0d)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.0)
        assertThat agg.goodMRR, closeTo(0.0)
    }

    @Test
    public void testLowRank() {
        def rng = new Random()
        // get 19 bad items
        def recs = (11..29).collect { Results.create(it, rng.nextDouble()) }
        // and 1 good
        recs << Results.create(42, Math.E)
        def user = TestUser.newBuilder()
                           .build()
        def result = metric.measureUser(user, Results.newResultList(recs), accum)
        assertThat result.rank, equalTo(20)
        assertThat result.recipRank, closeTo(0.05)
        def agg = metric.getAggregateMeasurements(accum)
        assertThat agg.mrr, closeTo(0.05)
        assertThat agg.goodMRR, closeTo(0.05)
    }

    @Test
    public void testMixed() {
        def rng = new Random()
        // get 19 bad items
        def recs = (11..29).collect { Results.create(it, rng.nextDouble()) }
        // and 1 good
        recs << Results.create(42, Math.E)
        def u1 = TestUser.newBuilder()
                         .build()
        metric.measureUser(u1, Results.newResultList(recs), accum)
        def u2 = TestUser.newBuilder()
                         .build()
        def r2 = Results.newResultList([Results.create(5, 3.5)])
        metric.measureUser(u2, r2, accum)

        def agg = metric.getAggregateMeasurements(accum)
        // MRR should average 0.05 and 0
        assertThat agg.mrr, closeTo(0.025)
        // Good MRR should only have 0.05
        assertThat agg.goodMRR, closeTo(0.05)
    }

    @Test
    public void testConfigure() {
        def jsb = new JsonBuilder()
        jsb {
            type 'mrr'
            suffix 'Good'
            goodItems 'allItems'
        }
        def node = SpecUtils.parse(DynamicSpec, jsb.toString())
        def metric = SpecUtils.buildObject(TopNMRRMetric, node)
        assertThat(metric, instanceOf(TopNMRRMetric))
        def mrr = metric as TopNMRRMetric
        assertThat(metric.suffix, equalTo("Good"))
        assertThat(metric.goodItems, instanceOf(ItemSelector.GroovyItemSelector))
    }
}
