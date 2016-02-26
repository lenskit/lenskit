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
import org.junit.Test
import org.lenskit.eval.traintest.TestUser
import org.lenskit.eval.traintest.metrics.Discounts
import org.lenskit.results.Results
import org.lenskit.specs.DynamicSpec
import org.lenskit.specs.SpecUtils

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TopNNDCGMetricTest {
    @Test
    public void testConfigure() {
        def jsb = new JsonBuilder()
        jsb {
            type 'ndcg'
            discount 'exp(5)'
        }
        def node = SpecUtils.parse(DynamicSpec, jsb.toString())
        def metric = SpecUtils.buildObject(TopNMetric, node)
        assertThat(metric, instanceOf(TopNNDCGMetric))
        def ndcg = metric as TopNNDCGMetric
        assertThat(ndcg.discount, equalTo(Discounts.exp(5)))
    }

    @Test
    public void testSameOrder() {
        def metric = new TopNNDCGMetric()
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 5.0)
                           .addTestRating(2, 4.5)
                           .build()
        def recs = Results.newResultList(Results.create(1, 3.0),
                                         Results.create(2, 2.5))
        def result = metric.measureUser(user, -1, recs, context)
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
    public void testIgnoresScores() {
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
        def result = metric.measureUser(user, -1, recs, context)
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
    public void testOutOfOrder() {
        // use half-life discounting, because log 2 doesn't change for 2 items
        def metric = new TopNNDCGMetric(Discounts.exp(2));
        def context = metric.createContext(null, null, null)
        def user = TestUser.newBuilder()
                           .addTestRating(1, 5.0)
                           .addTestRating(2, 2.5)
                           .build()
        // the order is right, but the recommendation values are out of order
        // this is fine, nDCG should only consider order.
        def recs = Results.newResultList(Results.create(2, 3.0),
                                         Results.create(1, 2.5))
        def result = metric.measureUser(user, -1, recs, context)
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
}
