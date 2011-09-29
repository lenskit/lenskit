/* Import classes and parameters */
importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.baseline);
importPackage(org.grouplens.lenskit.norm);
importPackage(org.grouplens.lenskit.svd);
importPackage(org.grouplens.lenskit.svd.params);

/* Add and configure recommender */
var rec = recipe.addAlgorithm();
rec.name = "FunkSVD";

/* Configure item-item recommender to use and normalize
 * with the item-user-mean baseline, damp the
 * similarities, and use a neighborhood size of 30. */
rec.factory.setComponent(RatingPredictor,
                         FunkSVDRatingPredictor);
rec.factory.setComponent(BaselinePredictor,
                         ItemUserMeanPredictor);
rec.factory.set(FeatureCount, 30);
//rec.factory.set(TrainingThreshold, 0.001);
rec.factory.set(IterationCount, 100);
