package org.grouplens.lenskit;

/**
 * {@link GlobalItemScorer} that scores by predicted rating.  The scores returned by
 * this scorer's methods are predicted ratings in the same scale as the input
 * ratings.
 *
 * @author Steven Chang <schang@cs.umn.edu>
 *
 */

public interface GlobalRatingPredictor extends GlobalItemScorer {

}
