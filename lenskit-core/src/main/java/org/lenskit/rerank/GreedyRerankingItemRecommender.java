package org.lenskit.rerank;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.basic.AbstractItemRecommender;
import org.lenskit.results.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * A hybrid item recommender that uses a greedy re-ranking strategy to allow re-ranking of items iteratively. This
 * general algorithm is commonly employed to efficiently optimize for set properties of a recommendation list such as
 * inter-item diveristy.
 *
 * This algorithm takes a baseline ranking algorithm, gets the top-n recommendations and re-ranks them iteratively.
 * To select each recommended item, first a scoring algorithm is ran based on the currently selected recommendations
 * and each candidate item. The item with the highest score is then added to the recommended list. This process repeates
 * until enough items are recommended.
 *
 * @author Daniel Kluver
 */
public class GreedyRerankingItemRecommender extends AbstractItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(GreedyRerankingItemRecommender.class);
    private final ItemRecommender baseRecommender;
    private final Rescorer rescorer;

    @Inject
    public GreedyRerankingItemRecommender(ItemRecommender baseRecommender, Rescorer rescorer) {
        this.baseRecommender = baseRecommender;
        this.rescorer = rescorer;
    }


    @Override
    protected ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidateItems, @Nullable LongSet exclude) {
        List<Result> candidates = baseRecommender.recommendWithDetails(user, -1, candidateItems, exclude);
        //modifiable copy
        candidates = new ArrayList<>(candidates);
        if (n<0) {
            n = candidates.size();
        }

        List<GreedyRerankingResult> results = new ArrayList<>(n);
        for (int i = 0; i<n; i++) {
            Result bestResult = null;
            double bestScore = 0;
            for (Result option : candidates) {
                double score = rescorer.score(results, option);
                if (bestResult == null || score > bestScore) {
                    bestResult = option;
                    bestScore = score;
                }
            }
            if (bestResult != null) {
                candidates.remove(bestResult);
                GreedyRerankingResult result = new GreedyRerankingResult(bestResult.getId(), bestResult.getScore(), i, bestScore);
                results.add(result);
            } else {
                break;
            }
        }
        return Results.newResultList(results);
    }
}
