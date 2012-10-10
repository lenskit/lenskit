package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Do major tests on the item-item recommender.
 *
 * @author Michael Ekstrand
 */
public class TestItemItemBuildSerialize {
    private File dataDir = new File(System.getProperty("lenskit.ml100k.directory"));
    private File inputFile = new File(dataDir, "u.data");

    private DAOFactory daoFactory;

    @Before
    public void createDAOFactory() {
        daoFactory = new SimpleFileRatingDAO.Factory(inputFile, "\t");
    }

    @Test
    public void testBuildAndSerializeModel() throws RecommenderBuildException, IOException {
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(daoFactory);
        factory.bind(ItemRecommender.class)
               .to(ItemItemRecommender.class);
        factory.bind(ItemScorer.class)
               .to(ItemItemRatingPredictor.class);
        factory.within(ItemVectorSimilarity.class)
               .bind(VectorSimilarity.class)
               .to(CosineVectorSimilarity.class);
        factory.bind(UserVectorNormalizer.class)
               .to(BaselineSubtractingUserVectorNormalizer.class);
        factory.bind(BaselinePredictor.class)
               .to(ItemUserMeanPredictor.class);

        LenskitRecommenderEngine engine = factory.create();
        assertThat(engine, notNullValue());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        engine.write(out);
        byte[] bytes = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        LenskitRecommenderEngine loaded = LenskitRecommenderEngine.load(daoFactory, in);
        assertThat(loaded, notNullValue());
    }
}
