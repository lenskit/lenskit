/* Configuration script to run a pretty good item-item recommender. */
rec.name = "ItemItem";
rec.builder = org.grouplens.lenskit.knn.item.ItemItemRecommenderBuilder;
rec.builder.baselinePredictor = new org.grouplens.lenskit.baseline.ItemUserMeanPredictor.Builder();
rec.builder.normalizedRatingBuildContext.normalizer = new org.grouplens.lenskit.norm.BaselineSubtractingNormalizer.Builder();
rec.builder.normalizedRatingBuildContext.normalizer.baselinePredictor = rec.builder.baselinePredictor;