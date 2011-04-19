// Configure the gradient descent SVD to behave mostly like FunkSVD
rec.name = "FunkSVD"
rec.builder = new org.grouplens.lenskit.svd.FunkSVDRecommenderBuilder();
rec.builder.featureCount = 25;
rec.builder.baseline = new org.grouplens.lenskit.baseline.ItemUserMeanPredictor.Builder();
rec.builder.baseline.smoothing = 25;
rec.builder.clampingFunction = new org.grouplens.lenskit.svd.RatingRangeClamp(0,5);