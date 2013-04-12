package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.knn.item.model.NormalizingItemItemModelBuilder;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.DefaultItemVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.truncate.NoOpTruncator;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

public class TestNormalizingItemItemAccuracy extends TestItemItemAccuracy {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitRecommenderEngineFactory factory) {
        factory.bind(ItemItemModel.class)
                .toProvider(NormalizingItemItemModelBuilder.class);
        factory.bind(ItemScorer.class)
                .to(ItemItemScorer.class);
        factory.bind(BaselinePredictor.class)
                .to(ItemUserMeanPredictor.class);
        factory.bind(UserVectorNormalizer.class)
                .to(BaselineSubtractingUserVectorNormalizer.class);
        factory.bind(ItemVectorNormalizer.class)
                .to(DefaultItemVectorNormalizer.class);
        factory.bind(VectorTruncator.class)
                .to(NoOpTruncator.class);
        factory.in(ItemSimilarity.class)
                .bind(VectorSimilarity.class)
                .to(CosineVectorSimilarity.class);
        factory.in(ItemSimilarity.class)
                .set(Damping.class)
                .to(100.0);
        factory.set(NeighborhoodSize.class).to(30);
    }
}
