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
package org.lenskit.knn.user

import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer
import org.lenskit.LenskitConfiguration
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.config.ConfigHelpers
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
import static org.junit.Assert.assertThat

/**
 * Do major tests on the item-item recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UserUserAccuracyTest extends CrossfoldTestSuite {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to UserUserItemScorer
            bind (BaselineScorer, ItemScorer) to UserMeanItemScorer
            within (UserVectorNormalizer) {
                bind VectorNormalizer to MeanCenteringVectorNormalizer
            }
            set NeighborhoodSize to 30
        }
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("MAE.ByRating").average(),
                   closeTo(0.71d, 0.025d))
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.91d, 0.05d))
    }
}
