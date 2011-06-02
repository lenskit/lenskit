recipe.addEval("Coverage");
recipe.addEval("NDCG");
recipe.addEval("HLUtility");
recipe.addEval("MAE");
recipe.addEval("RMSE");

importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.params);
importPackage(org.grouplens.lenskit.baseline);
importPackage(org.grouplens.lenskit.norm);
importPackage(org.grouplens.lenskit.knn);
importPackage(org.grouplens.lenskit.knn.item);
importPackage(org.grouplens.lenskit.knn.params);

var rec = recipe.addAlgorithm();
rec.name = "ItemItem";
rec.preload = true;

rec.factory.setComponent(RatingPredictor, ItemItemRatingPredictor);
rec.factory.setComponent(BaselinePredictor, ItemUserMeanPredictor);
rec.factory.setComponent(UserRatingVectorNormalizer, BaselineSubtractingNormalizer);
rec.factory.setComponent(SimilarityMatrixAccumulatorFactory, TruncatingSimilarityMatrixAccumulator.Factory)
rec.factory.set(SimilarityDamping, 100);
rec.factory.set(NeighborhoodSize, 30);
