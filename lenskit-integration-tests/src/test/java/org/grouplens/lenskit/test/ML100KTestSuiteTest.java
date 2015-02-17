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
package org.grouplens.lenskit.test;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineBuilder;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.PrefetchingItemDAO;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test the ML-100K test suite setup.
 */
public class ML100KTestSuiteTest extends ML100KTestSuite {
    @Test
    public void testFullItemDAO() throws RecommenderBuildException {
        LenskitConfiguration cfg = new LenskitConfiguration();
        cfg.addRoot(DAOFetcher.class);
        LenskitRecommenderEngineBuilder bld = LenskitRecommenderEngine.newBuilder();
        LenskitRecommender rec = bld.addConfiguration(getDaoConfig())
                                    .addConfiguration(cfg)
                                    .build()
                                    .createRecommender();
        DAOFetcher df = rec.get(DAOFetcher.class);
        assertThat(df.activeItemDAO, notNullValue());
        assertThat(df.fullItemDAO, notNullValue());
        assertThat(df.activeItemDAO, sameInstance(df.fullItemDAO));
        assertThat(df.activeItemDAO.getItemIds(),
                   hasSize(df.fullItemDAO.getItemIds().size()));
    }

    @Test
    public void testSubsetItemDAO() throws RecommenderBuildException {
        LenskitConfiguration cfg = new LenskitConfiguration();
        cfg.addRoot(DAOFetcher.class);
        LenskitRecommenderEngineBuilder bld = LenskitRecommenderEngine.newBuilder();
        LenskitRecommender rec = bld.addConfiguration(getItemSubsetConfig())
                                    .addConfiguration(cfg)
                                    .build()
                                    .createRecommender();
        DAOFetcher df = rec.get(DAOFetcher.class);
        assertThat(df.activeItemDAO, notNullValue());
        assertThat(df.fullItemDAO, notNullValue());
        assertThat(df.activeItemDAO, not(sameInstance(df.fullItemDAO)));
        assertThat(df.activeItemDAO.getItemIds(),
                   hasSize(df.fullItemDAO.getItemIds().size() - SUBSET_DROP_SIZE));
    }

    private static class DAOFetcher {
        private final ItemDAO activeItemDAO;
        private final ItemDAO fullItemDAO;

        @Inject
        public DAOFetcher(ItemDAO items, PrefetchingItemDAO allItems) {
            activeItemDAO = items;
            fullItemDAO = allItems;
        }
    }
}
