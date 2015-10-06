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
import org.lenskit.eval.traintest.metrics.Discounts
import org.lenskit.specs.DynamicSpec
import org.lenskit.specs.SpecUtils

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
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
        assertThat(ndcg.discount, equalTo(Discounts.exp(5)));
    }
}
