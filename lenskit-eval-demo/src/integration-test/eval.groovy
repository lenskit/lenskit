import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.knn.CosineSimilarity
import org.grouplens.lenskit.knn.Similarity
import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.knn.params.SimilarityDamping
import org.grouplens.lenskit.knn.params.UserSimilarity
import org.grouplens.lenskit.knn.user.UserUserRatingPredictor
import org.grouplens.lenskit.norm.BaselineSubtractingNormalizer
import org.grouplens.lenskit.norm.MeanVarianceNormalizer
import org.grouplens.lenskit.norm.VectorNormalizer
import org.grouplens.lenskit.params.NormalizerBaseline
import org.grouplens.lenskit.params.PredictNormalizer
import org.grouplens.lenskit.params.UserVectorNormalizer
import org.grouplens.lenskit.slopeone.SlopeOneRatingPredictor
import org.grouplens.lenskit.slopeone.WeightedSlopeOneRatingPredictor
import org.grouplens.lenskit.slopeone.params.DeviationDamping
import org.grouplens.lenskit.svd.FunkSVDRatingPredictor
import org.grouplens.lenskit.svd.params.FeatureCount
import org.grouplens.lenskit.svd.params.IterationCount
import org.grouplens.lenskit.baseline.*
import sun.text.normalizer.NormalizerBase

baselines = [GlobalMeanPredictor, UserMeanPredictor, ItemMeanPredictor, ItemUserMeanPredictor]

ml100k = crossfold("ml-100k") {
    source csvfile("ml-100k/u.data") {
        delimiter "\t"
    }
    order "random"
    holdout 10
    partitions 5

    domain {
        minimimum 1.0
        maximum 5.0
        precision 1.0
    }
}

trainTest {
    dataset ml100k

    output "eval-out.csv"
    predictionOutput("eval-preds.csv.gz") {
        compressed true
    }

    for (bl: baselines) {
        algorithm(bl.simpleName.replaceFirst(/Predictor$/, "")) {
            setComponent(RatingPredictor, BaselineRatingPredictor)
            setComponent(BaselinePredictor, bl)
        }
    }

    algorithm("UserUser") {
        setComponent(RatingPredictor, UserUserRatingPredictor)
        setComponent(PredictNormalizer, VectorNormalizer, MeanVarianceNormalizer)
        setComponent(BaselinePredictor, ItemUserMeanPredictor)
        setComponent(NormalizerBaseline, BaselinePredictor, UserMeanPredictor)
        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
        setComponent(UserSimilarity, Similarity, CosineSimilarity)
        set(SimilarityDamping, 100)
        set(NeighborhoodSize, 30)
    }

    algorithm("ItemItem") {
        setComponent(RatingPredictor, ItemItemRatingPredictor)
        setComponent(BaselinePredictor, ItemUserMeanPredictor)
        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
        set(SimilarityDamping, 100)
        set(NeighborhoodSize, 30);
    }

    algorithm("SlopeOne") {
        setComponent(NormalizerBase, BaselinePredictor, GlobalMeanPredictor)
        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
        setComponent(RatingPredictor, SlopeOneRatingPredictor)
        setComponent(BaselinePredictor, ItemUserMeanPredictor)
        setComponent(DeviationDamping, 0)
    }

    algorithm("WeightedSlopeOne") {
        setComponent(NormalizerBase, BaselinePredictor, GlobalMeanPredictor)
        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
        setComponent(RatingPredictor, WeightedSlopeOneRatingPredictor)
        setComponent(BaselinePredictor, ItemUserMeanPredictor)
        setComponent(DeviationDamping, 0)
    }

    algorithm("FunkSVD") {
        setComponent(RatingPredictor, FunkSVDRatingPredictor)
        setComponent(BaselinePredictor, ItemUserMeanPredictor)
        set(FeatureCount, 30)
        set(IterationCount, 100)
    }
}
