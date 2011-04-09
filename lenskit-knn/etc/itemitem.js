/* Configuration script to run a pretty good item-item recommender. */
rec.name = "ItemItem"
rec.module = org.grouplens.lenskit.knn.item.ItemItemCFModule
rec.module.knn.similarityDamping = 50
rec.module.core.baseline = org.grouplens.lenskit.baseline.ItemUserMeanPredictor
rec.module.core.userRatingVectorNormalizer = org.grouplens.lenskit.norm.BaselineSubtractingNormalizer
