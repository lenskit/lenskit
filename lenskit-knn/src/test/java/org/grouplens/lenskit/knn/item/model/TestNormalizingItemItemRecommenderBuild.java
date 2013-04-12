package org.grouplens.lenskit.knn.item.model;

import org.grouplens.lenskit.*;
import org.grouplens.lenskit.basic.SimpleRatingPredictor;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.knn.item.ItemItemGlobalRecommender;
import org.grouplens.lenskit.knn.item.ItemItemGlobalScorer;
import org.grouplens.lenskit.knn.item.ItemItemRecommender;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.transform.normalize.DefaultItemVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.transform.truncate.NoOpTruncator;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestNormalizingItemItemRecommenderBuild {
    private LenskitRecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));
        DAOFactory daof = new EventCollectionDAO.Factory(rs);

        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(daof);
        factory.bind(ItemItemModel.class).toProvider(NormalizingItemItemModelBuilder.class);
        factory.bind(ItemScorer.class).to(ItemItemScorer.class);
        factory.bind(ItemRecommender.class).to(ItemItemRecommender.class);
        factory.bind(GlobalItemRecommender.class).to(ItemItemGlobalRecommender.class);
        factory.bind(GlobalItemScorer.class).to(ItemItemGlobalScorer.class);
        // this is the default
//        factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class,
//                             IdentityVectorNormalizer.class);

        engine = factory.create();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testItemItemRecommenderEngineCreate() {
        Recommender rec = engine.open();

        assertThat(rec.getItemScorer(),
                instanceOf(ItemItemScorer.class));
        assertThat(rec.getRatingPredictor(),
                instanceOf(SimpleRatingPredictor.class));
        assertThat(rec.getItemRecommender(),
                instanceOf(ItemItemRecommender.class));
        assertThat(rec.getGlobalItemRecommender(),
                instanceOf(ItemItemGlobalRecommender.class));
        assertThat(rec.getGlobalItemScorer(),
                instanceOf(ItemItemGlobalScorer.class));
    }

    @Test
    public void testConfigSeparation() {
        LenskitRecommender rec1 = null;
        LenskitRecommender rec2 = null;
        try {
            rec1 = engine.open();
            rec2 = engine.open();

            assertThat(rec1.getItemScorer(),
                    not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(ItemItemModel.class),
                    allOf(not(nullValue()),
                            sameInstance(rec2.get(ItemItemModel.class))));
        } finally {
            if (rec2 != null) {
                rec2.close();
            }
            if (rec1 != null) {
                rec1.close();
            }
        }
    }
}
