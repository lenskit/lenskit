import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.UserMeanBaseline
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.predict.KnownRatingRatingPredictor
import org.lenskit.predict.RatingPredictorItemScorer

bind RatingPredictor to KnownRatingRatingPredictor
bind ItemScorer to RatingPredictorItemScorer
