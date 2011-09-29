importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.params);
importPackage(org.grouplens.lenskit.baseline);
importPackage(org.grouplens.lenskit.norm);
importPackage(org.grouplens.lenskit.slopeone);
importPackage(org.grouplens.lenskit.slopeone.params);

var rec = recipe.addAlgorithm();
rec.name = "SlopeOne";

// rec.factory.bind(PredictNormalizer, IdentityUserRatingVectorNormalizer);
rec.factory.setComponent(NormalizerBaseline, BaselinePredictor, GlobalMeanPredictor);
rec.factory.setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer);
rec.factory.setComponent(RatingPredictor, WeightedSlopeOneRatingPredictor);
rec.factory.setComponent(BaselinePredictor, ItemUserMeanPredictor);
rec.factory.set(DeviationDamping, 0);
