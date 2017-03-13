package org.lenskit.rerank;

import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import java.util.List;

/**
 * Interface for classes that produce scores for the value of adding an item to a list of recommended items. This class
 * is used in {@link GreedyRerankingItemRecommender} to define the metric to optimize when recommending items.
 *
 * While this interface does not have support for hard filtering, a filter behavior can be approximated by returning
 * {@link Double#MIN_VALUE}. Filtered items may still be returned, but only after all non-filtered items are eliminated.
 *
 * @author Daniel Kluver
 */
public interface Rescorer {

    /**
     * Compute a recommendation score for a given item in the context of a list of already chosen items.
     * @param items - The already chosen items and their <i>baseline scores</i> (not the scores returned by this object).
     *              It is likely that the baseline scores will be predictions.
     * @param nextItem - The posisble item to be added (and its baseline score).
     * @return - a score representing how good of an item this is to recommend. Higher scores are more likely to be
     * selected for recommendation.
     */
    double score(List<? extends Result> items, Result nextItem);
}
