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
import org.grouplens.grapht.util.ClassLoaders
import org.junit.Test
import org.lenskit.data.entities.EntityFactory
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
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
                   closeTo(1.0d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
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
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
                   closeTo(1.0d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
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
        // the recommendations are out of order
        def recs = Results.newResultList(Results.create(2, 3.0),
                                         Results.create(1, 2.5))
        def result = metric.measureUser(null, user, -1, recs, context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
                   closeTo(5 / 6.25d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
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
        assertThat(result.values.keySet(), contains('nDCG'))
        // should be 1 because only the first 2 test ratings are considered
        assertThat(result.values['nDCG'],
                   closeTo(1, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
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
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
                   closeTo((1 + 5/6.25d) * 0.5, 1.0e-6d))
    }

    @Test
    void testUseEntity() {
        def jsb = new JsonBuilder()
        jsb {
            type 'ndcg'
            discount 'exp(2)'
            gainAttribute 'count'
        }
        def mlh = new MetricLoaderHelper(ClassLoaders.inferDefault(), 'topn-metrics')
        def metric = mlh.createMetric(TopNMetric, jsb.toString())
        // use half-life discounting, because log 2 doesn't change for 2 items
        def context = metric.createContext(null, null, null)
        def fac = new EntityFactory()
        def user = TestUser.newBuilder()
                           .setUserId(42)
                           .addTestEntity(fac.likeBatch(42, 1, 5),
                                          fac.likeBatch(42, 2, 3))
                           .build()
        // The recommendations are out of order - so we should discount
        def recs = Results.newResultList(Results.create(2, 3.0),
                                         Results.create(1, 2.5))
        def result = metric.measureUser(null, user, -1, recs, context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
                   closeTo(5.5d / 6.5d, 1.0e-6d))

        result = metric.getAggregateMeasurements(context)
        assertThat(result, notNullValue())
        assertThat(result.values.keySet(), contains('nDCG'))
        assertThat(result.values['nDCG'],
                   closeTo(5.5d / 6.5d, 1.0e-6d))
    }
}
