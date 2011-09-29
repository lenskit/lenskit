importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.params);
importPackage(org.grouplens.lenskit.baseline);
importPackage(org.grouplens.lenskit.norm);
importPackage(org.grouplens.lenskit.knn);
importPackage(org.grouplens.lenskit.knn.user);
importPackage(org.grouplens.lenskit.knn.params);

var rec = recipe.addAlgorithm();
rec.name = "UserUser";

rec.factory.setComponent(RatingPredictor, UserUserRatingPredictor);
rec.factory.setComponent(PredictNormalizer, VectorNormalizer, MeanVarianceNormalizer);
rec.factory.setComponent(BaselinePredictor, ItemUserMeanPredictor);
rec.factory.setComponent(NormalizerBaseline, BaselinePredictor, UserMeanPredictor);
rec.factory.setComponent(UserVectorNormalizer,
                         VectorNormalizer,
                         BaselineSubtractingNormalizer);
rec.factory.setComponent(UserSimilarity, Similarity, CosineSimilarity);
rec.factory.set(SimilarityDamping, 100);
rec.factory.set(NeighborhoodSize, 30);
