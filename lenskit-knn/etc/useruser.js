recipe.addEval("Coverage");
recipe.addEval("NDCG");
recipe.addEval("HLUtility");
recipe.addEval("MAE");
recipe.addEval("RMSE");

importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.params);
importPackage(org.grouplens.lenskit.baseline);
importPackage(org.grouplens.lenskit.norm);
importPackage(org.grouplens.lenskit.knn.user);
importPackage(org.grouplens.lenskit.knn.params);

var rec = recipe.addAlgorithm();
rec.name = "UserUser";
rec.preload = true;

rec.factory.setComponent(RatingPredictor, UserUserRatingPredictor);
rec.factory.setComponent(NeighborhoodFinder, SimpleNeighborhoodFinder);
rec.factory.setComponent(PredictNormalizer, UserRatingVectorNormalizer, UserVarianceNormalizer);
rec.factory.setComponent(BaselinePredictor, ItemUserMeanPredictor);
rec.factory.setComponent(NormalizerBaseline, BaselinePredictor, UserMeanPredictor);
rec.factory.setComponent(UserRatingVectorNormalizer, BaselineSubtractingNormalizer);
rec.factory.set(SimilarityDamping, 100);
rec.factory.set(NeighborhoodSize, 30);
