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
package org.grouplens.lenskit.mf.funksvd;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.test.CrossfoldTestSuite;
import org.grouplens.lenskit.util.table.Table;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

/**
 * Do major tests on the FunkSVD recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestFunkSVDAccuracy extends CrossfoldTestSuite {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        config.bind(ItemScorer.class)
              .to(FunkSVDItemScorer.class);
        config.bind(BaselinePredictor.class)
              .to(ItemUserMeanPredictor.class);
        config.within(ItemUserMeanPredictor.class)
              .set(MeanDamping.class)
              .to(10);
        config.set(FeatureCount.class).to(25);
        config.set(IterationCount.class).to(125);
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("MAE").average(),
                   closeTo(0.74, 0.025));
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.92 , 0.05));
    }
}
