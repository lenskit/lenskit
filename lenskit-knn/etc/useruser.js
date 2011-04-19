rec.name = "UserUser";
rec.builder = new org.grouplens.lenskit.knn.user.UserUserRecommenderBuilder();
rec.builder.neighborhoodFinder.neighborhoodSize = 30;
rec.builder.normalizer = new org.grouplens.lenskit.norm.BaselineSubtractingNormalizer.Builder();
rec.builder.normalizer.baselinePredictor = new org.grouplens.lenskit.baseline.UserMeanPredictor.Builder();
rec.builder.neighborhoodFinder.normalizer = rec.builder.normalizer