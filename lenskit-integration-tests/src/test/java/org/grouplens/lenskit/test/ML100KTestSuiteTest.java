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
