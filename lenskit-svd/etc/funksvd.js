// Configure the gradient descent SVD to behave mostly like FunkSVD
rec.name = "FunkSVD"
rec.builder = new org.grouplens.lenskit.svd.SVDRecommenderBuilder();
rec.builder.modelBuilder.baseline = new org.grouplens.lenskit.baseline.ItemUserMeanPredictor.Builder();
rec.builder.modelBuilder.baseline.smoothing = 25
// rec.module.clampingFunction = org.grouplens.lenskit.svd.RatingRangeClamp