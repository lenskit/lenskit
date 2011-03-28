rec.name = "FunkSVD"
rec.module = org.grouplens.lenskit.svd.GradientDescentSVDModule
rec.module.core.meanDamping = 25
rec.module.core.baseline = org.grouplens.lenskit.baseline.ItemUserMeanPredictor
rec.module.clampingFunction = org.grouplens.lenskit.svd.RatingRangeClamp