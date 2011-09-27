importPackage(org.grouplens.lenskit);
importPackage(org.grouplens.lenskit.params);
importPackage(org.grouplens.lenskit.baseline);

["GlobalMean", "UserMean", "ItemMean", "ItemUserMean"].forEach(function(name) {
	var rec = recipe.addAlgorithm();
	rec.name = name;
	rec.factory.setComponent(RatingPredictor, BaselineRatingPredictor);
	rec.factory.setComponent(BaselinePredictor,
			org.grouplens.lenskit.baseline[name + "Predictor"]);
});
