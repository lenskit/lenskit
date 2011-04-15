// Configure the gradient descent SVD to behave mostly like FunkSVD
rec.name = "FunkSVD"
rec.builder = new org.grouplens.lenskit.svd.FunkSVDRecommenderBuilder();
rec.builder.baseline = new org.grouplens.lenskit.baseline.ItemUserMeanPredictor.Builder();
rec.builder.baseline.smoothing = 25
// rec.module.clampingFunction = org.grouplens.lenskit.svd.RatingRangeClamp