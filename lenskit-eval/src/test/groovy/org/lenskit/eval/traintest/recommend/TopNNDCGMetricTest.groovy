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
import org.grouplens.grapht.util.ClassLoaders
import org.junit.Test
import org.lenskit.eval.traintest.TestUser
import org.lenskit.eval.traintest.metrics.Discounts
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper
import org.lenskit.results.Results

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TopNNDCGMetricTest {
    @Test
    void testConfigure() {
        def jsb = new JsonBuilder()
        jsb {
            type 'ndcg'
            discount 'exp(5)'
        }
        def mlh = new MetricLoaderHelper(ClassLoaders.inferDefault(), 'topn-metrics')
        def metric = mlh.createMetric(TopNMetric, jsb.toString())
        assertThat(metric, instanceOf(TopNNDCGMetric))
        def ndcg = metric as TopNNDCGMetric
        assertThat(ndcg.discount, equalTo(Discounts.exp(5)))
    }

    @Test
    void testSameOrder() {
        def metric = new TopNNDCGMetric()
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 5.0)
                           .addTestRating(2, 4.5)
                           .build()
        def recs = Results.newResultList(Results.create(1, 3.0),
                                         Results.create(2, 2.5))
        def result = metric.measureUser(null, user, -1, recs, context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(1.0d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(1.0d, 1.0e-6d))
    }

    @Test
    void testIgnoresScores() {
        def metric = new TopNNDCGMetric()
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 5.0)
                           .addTestRating(2, 4.5)
                           .build()
        // the order is right, but the recommendation values are out of order
        // this is fine, nDCG should only consider order.
        def recs = Results.newResultList(Results.create(1, 1.0),
                                         Results.create(2, 2.5))
        def result = metric.measureUser(null, user, -1, recs, context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(1.0d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(1.0d, 1.0e-6d))
    }

    @Test
    void testOutOfOrder() {
        // use half-life discounting, because log 2 doesn't change for 2 items
        def metric = new TopNNDCGMetric(Discounts.exp(2))
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 5.0)
                           .addTestRating(2, 2.5)
                           .build()
        // the order is right, but the recommendation values are out of order
        // this is fine, nDCG should only consider order.
        def recs = Results.newResultList(Results.create(2, 3.0),
                                         Results.create(1, 2.5))
        def result = metric.measureUser(null, user, -1, recs, context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(5 / 6.25d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(5 / 6.25d, 1.0e-6d))
    }

    @Test
    void testTooManyRatings() {
        // test that we only consider first `n` test ratings if target rec list is shorter
        def metric = new TopNNDCGMetric(Discounts.exp(2))
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 1.0)
                           .addTestRating(2, 5.0)
                           .addTestRating(3, 2.5)
                           .build()
        // the order is right, but the recommendation values are out of order
        // this is fine, nDCG should only consider order.
        def recs = Results.newResultList(Results.create(2, 3.0),
                                         Results.create(3, 2.5))
        def result = metric.measureUser(null, user, 2, recs, context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        // should be 1 because only the first 2 test ratings are considered
        assertThat(result.values['TopN.nDCG'],
                   closeTo(1, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo(1, 1.0e-6d))
    }

    @Test
    void testAggregate() {
        def metric = new TopNNDCGMetric(Discounts.exp(2))
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 5.0)
                           .addTestRating(2, 4.5)
                           .build()
        def recs = Results.newResultList(Results.create(1, 3.0),
                                         Results.create(2, 2.5))
        metric.measureUser(null, user, -1, recs, context)

        user = TestUser.newBuilder()
                       .addTestRating(1, 5.0)
                       .addTestRating(2, 2.5)
                       .build()

        recs = Results.newResultList(Results.create(2, 3.0),
                                         Results.create(1, 2.5))
        metric.measureUser(null, user, -1, recs, context)

        def result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('TopN.nDCG'))
        assertThat(result.values['TopN.nDCG'],
                   closeTo((1 + 5/6.25d) * 0.5, 1.0e-6d))
    }
}
