import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.test.TestComponent

bind RatingPredictor to TestComponent
bind ItemScorer to ItemMeanRatingItemScorer
