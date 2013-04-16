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
package org.grouplens.lenskit.eval

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselineItemScorer
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.GlobalMeanPredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.event.SimpleRating
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.eval.data.GenericDataSource
import org.grouplens.lenskit.vectors.MutableSparseVector
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TrainModelCommandTest extends ConfigTestBase {
    def ratings = [
            new SimpleRating(1, 1, 1, 3.5),
            new SimpleRating(2, 1, 2, 4.0),
            new SimpleRating(3, 2, 1, 3.5),
            new SimpleRating(3, 2, 3, 5.0)
    ]
    def daoFactory = new EventCollectionDAO.Factory(ratings);
    def dataSource = new GenericDataSource("test-data", daoFactory);

    @Test
    void testTrainModel() {
        def obj = eval {
            trainModel {
                algorithm {
                    bind ItemScorer to BaselineItemScorer
                    bind BaselinePredictor to GlobalMeanPredictor
                }
                input dataSource
                action {
                    assertThat(it.itemScorer, notNullValue());
                    assertThat(it.ratingPredictor, notNullValue());
                    assertThat(it.get(BaselinePredictor), notNullValue());
                    return it.itemScorer.baseline
                }
            }
        }
        assertThat(obj, instanceOf(GlobalMeanPredictor))
        def v = obj.predict(42, new MutableSparseVector(), [1l,2l,4l])
        assertThat(v.get(1), closeTo(4.0d, 1.0e-5d))
        assertThat(v.get(2), closeTo(4.0d, 1.0e-5d))
        assertThat(v.get(4), closeTo(4.0d, 1.0e-5d))
    }

    @Test
    void testTrainModelWithName() {
        def obj = eval {
            trainModel("foobar") {
                algorithm {
                    bind ItemScorer to BaselineItemScorer
                    bind BaselinePredictor to ItemUserMeanPredictor
                }
                input dataSource
                action {
                    assertThat(it.itemScorer, notNullValue());
                    assertThat(it.ratingPredictor, notNullValue());
                    assertThat(it.get(BaselinePredictor), notNullValue());
                    return it.itemScorer.baseline
                }
            }
        }
        assertThat(obj, instanceOf(ItemUserMeanPredictor))
    }
}
