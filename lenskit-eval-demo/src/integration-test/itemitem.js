/* Import classes and parameters */
importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.params);
importPackage(org.grouplens.lenskit.baseline);
importPackage(org.grouplens.lenskit.norm);
importPackage(org.grouplens.lenskit.knn.item);
importPackage(org.grouplens.lenskit.knn.params);

/* Add and configure recommender */
var rec = recipe.addAlgorithm();
rec.name = "ItemItem";

/* Configure item-item recommender to use and normalize
 * with the item-user-mean baseline, damp the
 * similarities, and use a neighborhood size of 30. */
rec.factory.setComponent(RatingPredictor,
                         ItemItemRatingPredictor);
rec.factory.setComponent(BaselinePredictor,
                         ItemUserMeanPredictor);
rec.factory.setComponent(UserVectorNormalizer,
                         VectorNormalizer,
                         BaselineSubtractingNormalizer);
rec.factory.set(SimilarityDamping, 100);
rec.factory.set(NeighborhoodSize, 30);
