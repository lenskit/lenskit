package org.grouplens.lenskit.core;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.BaselineRatingPredictor;
import org.grouplens.lenskit.baseline.ConstantPredictor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class LenskitRecommenderEngineTest {
    private LenskitRecommenderEngineFactory factory;

    @Before
    public void setup() {
        DAOFactory dao = new EventCollectionDAO.Factory(Collections.<Event>emptyList());
        factory = new LenskitRecommenderEngineFactory(dao);
    }

    @Test
    public void testBasicRec() {
        factory.bind(RatingPredictor.class)
               .to(BaselineRatingPredictor.class);
        factory.bind(ItemRecommender.class)
               .to(ScoreBasedItemRecommender.class);
        factory.bind(BaselinePredictor.class)
               .to(ConstantPredictor.class);

        LenskitRecommenderEngine engine = factory.create();
        LenskitRecommender rec = engine.open();
        try {
            assertThat(rec.getItemRecommender(),
                       instanceOf(ScoreBasedItemRecommender.class));
            assertThat(rec.getItemScorer(),
                       instanceOf(BaselineRatingPredictor.class));
            assertThat(rec.getRatingPredictor(),
                       instanceOf(BaselineRatingPredictor.class));
            assertThat(rec.get(BaselinePredictor.class),
                       instanceOf(ConstantPredictor.class));
        } finally {
            rec.close();
        }
    }

    @Test
    public void testArbitraryRoot() {
        factory.bind(BaselinePredictor.class)
               .to(ConstantPredictor.class);
        factory.addRoot(BaselinePredictor.class);

        LenskitRecommenderEngine engine = factory.create();
        LenskitRecommender rec = engine.open();
        try {
            assertThat(rec.get(BaselinePredictor.class),
                       instanceOf(ConstantPredictor.class));
        } finally {
            rec.close();
        }
    }
}
