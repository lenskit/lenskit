/* Configuration script to run a pretty good item-item recommender. */
rec.name = "ItemItem";
rec.builder = org.grouplens.lenskit.knn.item.ItemItemRecommenderEngineBuilder;
rec.builder.baselinePredictor = new org.grouplens.lenskit.baseline.ItemUserMeanPredictor.Builder();
rec.builder.normalizer = new org.grouplens.lenskit.norm.BaselineSubtractingNormalizer.Builder();
rec.builder.normalizer.baselinePredictor = rec.builder.baselinePredictor;